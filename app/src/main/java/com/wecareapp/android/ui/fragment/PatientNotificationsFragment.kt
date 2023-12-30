package com.wecareapp.android.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.wecareapp.android.R
import com.wecareapp.android.adapter.NotificationsListAdapter
import com.wecareapp.android.model.Notification
import com.wecareapp.android.utilities.Utility
import com.wecareapp.android.webservices.viewmodels.PatientViewModel
import kotlinx.android.synthetic.main.fragment_notifications_patient.*
import okhttp3.MultipartBody
import org.json.JSONObject

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class PatientNotificationsFragment : BaseFragment(),
    NotificationsListAdapter.Listener {

    private var list: MutableList<Notification?> = mutableListOf()
    private lateinit var adapter: NotificationsListAdapter
    private lateinit var listener: ICallBackToActivity

    companion object {
        fun newInstance(
            bundle: Bundle,
            listener: ICallBackToActivity
        ): PatientNotificationsFragment {
            val fragment = PatientNotificationsFragment()
            fragment.arguments = bundle
            fragment.setListener(listener)
            return fragment
        }

        fun newInstance(listener: ICallBackToActivity): PatientNotificationsFragment {
            val fragment = PatientNotificationsFragment()
            fragment.setListener(listener)
            return fragment
        }

        fun newInstance(bundle: Bundle): PatientNotificationsFragment {
            val fragment = PatientNotificationsFragment()
            fragment.arguments = bundle
            return fragment
        }

        fun newInstance(): PatientNotificationsFragment {
            return PatientNotificationsFragment()
        }
    }

    private fun setListener(listener: ICallBackToActivity) {
        this.listener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(
            R.layout.fragment_notifications_patient, container, false
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            ivBack.setOnClickListener {
                listener.onBackClicked(true)
            }
            list.clear()
            recyclerView.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = NotificationsListAdapter(list, this)

            recyclerView.adapter = adapter
            loadData()
            ivDelete.setOnClickListener {
                deleteNotification()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun loadData() {
        if (checkingAccess()) {
            progressBar.visibility = View.VISIBLE

            val jsonObject = JSONObject()
            jsonObject.put("action", "notifications")
            jsonObject.put("user_id", userId ?: "")
            jsonObject.put("customer_type", "1")
            Utility.showLog("jsonObject", "" + jsonObject)

            val request = MultipartBody.Builder()

            request.setType(MultipartBody.FORM)
                .addFormDataPart("action", "notifications")
                .addFormDataPart("user_id", userId ?: "")
//                .addFormDataPart("customer_type", "1")

//            if (1 == 1) {
//                request.addFormDataPart("user_id", "3")
//            } else if (2 == 2) {
//                request.addFormDataPart("doctor_id", "3")
//            } else if (3 == 3) {
//                request.addFormDataPart("clinic_id", "3")
//            }

            val requestBody = request.build()

            val viewModel = ViewModelProvider(this).get(PatientViewModel::class.java)
            viewModel.getNotifications(requestBody).observe(viewLifecycleOwner, Observer {
                Utility.hideLoadingDialog()
                progressBar.visibility = View.GONE
                if (it == null)
                    return@Observer
                if (it.status == "200") {
                    it.data?.let { it1 -> list.addAll(it1) }
                    if (list.isEmpty()) {
                        tvNoData.visibility = View.VISIBLE
                        recyclerView.visibility = View.INVISIBLE
                    } else {
                        tvNoData.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                        adapter.notifyDataSetChanged()
                    }
                } else {
                    Snackbar.make(
                        requireActivity().findViewById(android.R.id.content),
                        it.message ?: getString(R.string.err_msg_oops_something_went_to_wrong),
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            })
        }
    }


    private fun deleteNotification() {
        if (checkingAccess()) {
            progressBar.visibility = View.VISIBLE

            val jsonObject = JSONObject()
            jsonObject.put("action", "delete_notifications")
            jsonObject.put("user_id", userId ?: "")
            jsonObject.put("customer_type", userCustomerType)
            Utility.showLog("jsonObject", "" + jsonObject)

            val request = MultipartBody.Builder()

            request.setType(MultipartBody.FORM)
                .addFormDataPart("action", "delete_notifications")
                .addFormDataPart("user_id", userId ?: "")
                .addFormDataPart("customer_type", userCustomerType.toString())

            when (userCustomerType) {
                1 -> {
                    request.addFormDataPart("user_id", userId ?: "")
                }
                2 -> {
                    request.addFormDataPart("doctor_id", userId ?: "")
                }
                3 -> {
                    request.addFormDataPart("clinic_id", userId ?: "")
                }
            }

            val requestBody = request.build()

            val viewModel = ViewModelProvider(this).get(PatientViewModel::class.java)
            viewModel.commonRequest(requestBody).observe(viewLifecycleOwner, Observer {
                Utility.hideLoadingDialog()
                progressBar.visibility = View.GONE
                if (it == null)
                    return@Observer
                if (it.status == "200") {
                    list.clear()
                    if (list.isEmpty()) {
                        tvNoData.visibility = View.VISIBLE
                        recyclerView.visibility = View.INVISIBLE
                    } else {
                        tvNoData.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                        adapter.notifyDataSetChanged()
                    }
                } else {
                    Snackbar.make(
                        requireActivity().findViewById(android.R.id.content),
                        it.message ?: getString(R.string.err_msg_oops_something_went_to_wrong),
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            })
        }
    }

    override fun onItemSelected(notification: Notification?, adapterPosition: Int) {

    }
}