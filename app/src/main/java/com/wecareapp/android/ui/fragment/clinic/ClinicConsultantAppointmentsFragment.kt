package com.wecareapp.android.ui.fragment.clinic

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.wecareapp.android.R
import com.wecareapp.android.adapter.Custom2Adapter
import com.wecareapp.android.adapter.DoctorSlotsAdapter
import com.wecareapp.android.model.DoctorSlotDataItem
import com.wecareapp.android.ui.fragment.BaseFragment
import com.wecareapp.android.ui.fragment.ICallBackToActivity
import com.wecareapp.android.utilities.Utility
import com.wecareapp.android.webservices.viewmodels.PatientViewModel
import devs.mulham.horizontalcalendar.HorizontalCalendar
import devs.mulham.horizontalcalendar.utils.HorizontalCalendarListener
import kotlinx.android.synthetic.main.fragment_consultant_my_appointments.*
import okhttp3.MultipartBody
import java.util.*


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class ClinicConsultantAppointmentsFragment(private val listener: ICallBackToActivity) :
    BaseFragment(),
    DoctorSlotsAdapter.Listener {

    private lateinit var viewModel: PatientViewModel
    private lateinit var adapter: DoctorSlotsAdapter
    private var list: MutableList<DoctorSlotDataItem?> = mutableListOf()
    private var showDocs = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this).get(PatientViewModel::class.java)
        // Inflate the layout for this fragment
        return inflater.inflate(
            R.layout.fragment_clinic_consultant_appointments, container, false
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            ivBack.setOnClickListener {
                listener.onBackClicked(true)
            }
            recyclerView.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            list.add(DoctorSlotDataItem())
            list.add(DoctorSlotDataItem())
            adapter = DoctorSlotsAdapter(list, this)
            recyclerView.adapter = adapter

//            rvDoctors.adapter = adapter
            rvDoctors.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            rvDoctors.adapter = Custom2Adapter(dummyList, R.layout.list_item_appointments)

            val startDate: Calendar = Calendar.getInstance()
            startDate.add(Calendar.MONTH, -1)

            val endDate: Calendar = Calendar.getInstance()
            endDate.add(Calendar.MONTH, 1)

            val horizontalCalendar: HorizontalCalendar =
                HorizontalCalendar.Builder(getView(), R.id.calendarView)
                    .range(startDate, endDate)
                    .datesNumberOnScreen(5)
                    .defaultSelectedDate(Calendar.getInstance())
                    .configure().showTopText(false).end()
                    .build()

            horizontalCalendar.calendarListener = object : HorizontalCalendarListener() {
                override fun onDateSelected(date: Calendar?, position: Int) {
                    //do something
                    getAppointments()
                }
            }
            getAppointments()

            ivProfile.setOnClickListener {
                showDocs = !showDocs
                if (showDocs) {
                    ivDocList.setImageResource(R.drawable.ic_arrow_up_clinic_doc)
                    rvDoctors.visibility = View.VISIBLE
                } else {
                    ivDocList.setImageResource(R.drawable.ic_arrow_down_clinic_doc)
                    rvDoctors.visibility = View.GONE
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getAppointments() {
        if (!checkingAccess())
            return
        swipeRefresh.isRefreshing = true


        val request = MultipartBody.Builder()
        request.setType(MultipartBody.FORM)
            .addFormDataPart("action", "doctor_slots")
            .addFormDataPart("user_id", "5")
            .addFormDataPart("customer_type", "1")
            .addFormDataPart("start", "0")
            .addFormDataPart("slots_list_date", "2020-10-20")

        val requestBody = request.build()

        viewModel.getDoctorSlots(requestBody).observe(viewLifecycleOwner, Observer {
            Utility.hideLoadingDialog()
            progressBar.visibility = View.GONE
            swipeRefresh.isRefreshing = false
            /*if (it == null)
                return@Observer
            if (it.status == "200") {
                it.inbox?.let { it1 -> list.addAll(it1) }
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
            }*/
        })
    }

    private fun addRating() {
        if (!checkingAccess())
            return
        swipeRefresh.isRefreshing = true


        val request = MultipartBody.Builder()
        request.setType(MultipartBody.FORM)
            .addFormDataPart("action", "add_rating")
            .addFormDataPart("user_id", "5")
            .addFormDataPart("customer_type", "1")
            .addFormDataPart("start", "0")

        val requestBody = request.build()

        viewModel.getChatList(requestBody).observe(viewLifecycleOwner, Observer {
            Utility.hideLoadingDialog()
            progressBar.visibility = View.GONE
            swipeRefresh.isRefreshing = false
            /*if (it == null)
                return@Observer
            if (it.status == "200") {
                it.inbox?.let { it1 -> list.addAll(it1) }
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
            }*/
        })
    }

    override fun onItemSelected(doctor: DoctorSlotDataItem?, adapterPosition: Int) {

    }

    override fun onClickedEndReview(doctor: DoctorSlotDataItem?, adapterPosition: Int) {
        val factory = LayoutInflater.from(context)
        val deleteDialogView: View = factory.inflate(R.layout.dialog_doctor_end_review, null)
        val deleteDialog: AlertDialog = AlertDialog.Builder(requireContext()).create()
        deleteDialog.setView(deleteDialogView)
//        deleteDialogView.findViewById<View>(R.id.no)
//            .setOnClickListener { //your business logic
//                deleteDialog.dismiss()
//            }
//        deleteDialogView.findViewById<View>(R.id.no).setOnClickListener { deleteDialog.dismiss() }

        deleteDialog.show()
    }
}