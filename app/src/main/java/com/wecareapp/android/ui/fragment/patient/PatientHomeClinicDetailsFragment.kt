package com.wecareapp.android.ui.fragment.patient

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.snackbar.Snackbar
import com.wecareapp.android.R
import com.wecareapp.android.adapter.DoctorListAdapter
import com.wecareapp.android.adapter.ServiceCategoryListAdapter
import com.wecareapp.android.model.Category
import com.wecareapp.android.model.Clinic
import com.wecareapp.android.model.Doctor
import com.wecareapp.android.ui.fragment.BaseFragment
import com.wecareapp.android.ui.fragment.ICallBackToActivity
import com.wecareapp.android.utilities.Utility
import com.wecareapp.android.webservices.viewmodels.PatientViewModel
import kotlinx.android.synthetic.main.fragment_home_patient_clinic_details.*
import kotlinx.android.synthetic.main.fragment_home_patient_clinic_details.appCompatRatingBar2
import kotlinx.android.synthetic.main.fragment_home_patient_clinic_details.ivBack
import kotlinx.android.synthetic.main.fragment_home_patient_clinic_details.ivProfile
import kotlinx.android.synthetic.main.fragment_home_patient_clinic_details.progressBar
import kotlinx.android.synthetic.main.fragment_home_patient_clinic_details.tvClinicName
import kotlinx.android.synthetic.main.fragment_patient_consultant_profile.*
import okhttp3.MultipartBody
import org.json.JSONObject

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class PatientHomeClinicDetailsFragment(private val listener: ICallBackToActivity) : BaseFragment(),
    DoctorListAdapter.Listener, ServiceCategoryListAdapter.Listener {

    private var list: MutableList<Doctor?> = mutableListOf()
    private lateinit var adapter: DoctorListAdapter
    private var serviceCategoryList: MutableList<Category?> = mutableListOf()
    private lateinit var serviceCategoryAdapter: ServiceCategoryListAdapter
    private var clinic: Clinic? = null
    private lateinit var viewModel: PatientViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this).get(PatientViewModel::class.java)
        // Inflate the layout for this fragment
        return inflater.inflate(
            R.layout.fragment_home_patient_clinic_details, container, false
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            clinic =
                if (arguments?.getSerializable("clinic") == null) null else (arguments?.getSerializable(
                    "clinic"
                )) as Clinic

            ivBack.setOnClickListener {
                listener.onBackClicked(true)
            }

            list.clear()
            serviceCategoryList.clear()
            recyclerView.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = DoctorListAdapter(list, this, true)
            recyclerView.adapter = adapter

            serviceRecyclerView.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            serviceCategoryAdapter = ServiceCategoryListAdapter(serviceCategoryList, this)
            serviceRecyclerView.adapter = serviceCategoryAdapter

            getCategoriesList()

            if (clinic != null) {
                val requestOptions: RequestOptions = RequestOptions()
                    .placeholder(R.drawable.ic_clinic_doctor)
                    .error(R.drawable.ic_clinic_doctor)
                    .fitCenter()

                val img = clinic?.image
                Glide.with(ivProfile.context)
                    .load(img)
                    .fitCenter()
                    .apply(requestOptions)
                    .into(ivProfile)

                tvClinicName.text = clinic?.userName
//                tvClinicLocation.text = ""
                appCompatRatingBar2.visibility = View.GONE
                appCompatRatingBar2.rating = clinic?.rating?.toFloat() ?: 0F
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getCategoriesList() {
        if (checkingAccess()) {
            progressBar.visibility = View.VISIBLE

            val jsonObject = JSONObject()
            jsonObject.put("action", "categories_list")
            jsonObject.put("user_id", userId)
            jsonObject.put("customer_type", userCustomerType)
            Utility.showLog("jsonObject", "" + jsonObject)

            val request = MultipartBody.Builder()

            request.setType(MultipartBody.FORM)
                .addFormDataPart("action", "categories_list")
                .addFormDataPart("customer_type", "3")

            if (!TextUtils.isEmpty(userId))
                request.addFormDataPart("user_id", clinic?.clinicId ?: "")

            val requestBody = request.build()

            viewModel.getCategoriesList(requestBody).observe(viewLifecycleOwner, Observer {
                Utility.hideLoadingDialog()
                progressBar.visibility = View.GONE
                if (it == null)
                    return@Observer
                if (it.status == "200") {
                    it.data?.let { it1 -> serviceCategoryList.addAll(it1) }
                    if (serviceCategoryList.isEmpty()) {
                        tvNoData.visibility = View.VISIBLE
                        recyclerView.visibility = View.INVISIBLE
                        serviceRecyclerView.visibility = View.INVISIBLE
                    } else {
                        tvNoData.visibility = View.GONE
                        serviceRecyclerView.visibility = View.VISIBLE
                        serviceCategoryAdapter.notifyDataSetChanged()
                        getDoctorsList(it.data?.get(0)?.categoryId)
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

    private fun getDoctorsList(categoryId: String?) {
        if (checkingAccess()) {
            progressBar.visibility = View.VISIBLE

            val jsonObject = JSONObject()
            jsonObject.put("action", "getClinicDoctorsByCategory1")
            jsonObject.put("user_id", userId ?: "")
            jsonObject.put("customer_type", userCustomerType)
            Utility.showLog("jsonObject", "" + jsonObject)

            val request = MultipartBody.Builder()

            request.setType(MultipartBody.FORM)
                .addFormDataPart("action", "getClinicDoctorsByCategory1")

                .addFormDataPart("category_id", categoryId.toString())
//                .addFormDataPart("customer_type", userCustomerType.toString())
                .addFormDataPart("customer_type", "3")
                .addFormDataPart("start", "0")

            if (!TextUtils.isEmpty(userId))
                request.addFormDataPart("user_id", clinic?.clinicId ?: "")

            val requestBody = request.build()

            viewModel.getClinicDoctorsByCategory(requestBody).observe(viewLifecycleOwner, Observer {
                Utility.hideLoadingDialog()
                progressBar.visibility = View.GONE
                if (it == null)
                    return@Observer
                if (it.status == "200") {
                    it.doctors?.let { it1 -> list.addAll(it1) }
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

    override fun onItemSelected(doctor: Doctor?, adapterPosition: Int) {
        val fragment = PatientHomeConsultantProfileFragment.newInstance(listener)
        val bundle = Bundle().apply {
            putSerializable("doctor", doctor)
        }
        fragment.arguments = bundle
        openFragment(fragment)
    }

    override fun bookAppointment(doctor: Doctor?, adapterPosition: Int) {
        if (!isLogged) {
            openLoginActivity()
            return
        }
        val fragment = PatientHomeConsultantAppointmentSelectionFragment(listener)
        fragment.arguments = Bundle().apply {
            putSerializable("doctor", doctor)
        }
        openFragment(fragment)
    }

    override fun onItemSelected(category: Category?, adapterPosition: Int) {
        list.clear()
        for (category in serviceCategoryList) {
            category?.selected = false
        }
        category?.selected = true
        serviceCategoryAdapter.notifyDataSetChanged()
        getDoctorsList(category?.categoryId)
    }
}