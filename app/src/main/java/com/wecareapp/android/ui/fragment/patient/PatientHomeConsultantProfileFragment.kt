package com.wecareapp.android.ui.fragment.patient

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.snackbar.Snackbar
import com.wecareapp.android.R
import com.wecareapp.android.model.Doctor
import com.wecareapp.android.ui.fragment.BaseFragment
import com.wecareapp.android.ui.fragment.ICallBackToActivity
import com.wecareapp.android.utilities.DateUtils
import com.wecareapp.android.utilities.Utility
import com.wecareapp.android.webservices.viewmodels.PatientViewModel
import kotlinx.android.synthetic.main.activity_chat_patient.ivBack
import kotlinx.android.synthetic.main.fragment_home_patient_clinic.progressBar
import kotlinx.android.synthetic.main.fragment_patient_consultant_profile.*
import okhttp3.MultipartBody
import org.json.JSONObject

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class PatientHomeConsultantProfileFragment :
    BaseFragment() {

    private var doctor: Doctor? = null
    private lateinit var viewModel: PatientViewModel

    private lateinit var listener: ICallBackToActivity

    private fun setListener(listener: ICallBackToActivity) {
        this.listener = listener
    }

    companion object {
        fun newInstance(
            bundle: Bundle,
            listener: ICallBackToActivity
        ): PatientHomeConsultantProfileFragment {
            val fragment = PatientHomeConsultantProfileFragment()
            fragment.arguments = bundle
            fragment.setListener(listener)
            return fragment
        }

        fun newInstance(listener: ICallBackToActivity): PatientHomeConsultantProfileFragment {
            val fragment = PatientHomeConsultantProfileFragment()
            fragment.setListener(listener)
            return fragment
        }

        fun newInstance(bundle: Bundle): PatientHomeConsultantProfileFragment {
            val fragment = PatientHomeConsultantProfileFragment()
            fragment.arguments = bundle
            return fragment
        }

        fun newInstance(): PatientHomeConsultantProfileFragment {
            return PatientHomeConsultantProfileFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this).get(PatientViewModel::class.java)
        // Inflate the layout for this fragment
        return inflater.inflate(
            R.layout.fragment_patient_consultant_profile, container, false
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {

            doctor = arguments?.getSerializable("doctor") as Doctor?

            ivBack.setOnClickListener {
                listener.onBackClicked(false)
            }

            loadData()

            ivChat.setOnClickListener {
                if (doctor == null)
                    return@setOnClickListener

                listener.openChat(doctor)
            }

            llMakeAppointment.setOnClickListener {
                if (doctor == null)
                    return@setOnClickListener

                if (!isLogged) {
                    openLoginActivity()
                    return@setOnClickListener
                }

                val fragment = PatientHomeConsultantAppointmentSelectionFragment(listener)
                fragment.arguments = Bundle().apply {
                    putSerializable("doctor", doctor)
                }
                openFragment(fragment)
            }
            setDoctorData()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadData() {
        if (checkingAccess()) {
            progressBar.visibility = View.VISIBLE
            val jsonObject = JSONObject()
            jsonObject.put("action", "doctors_list")
            jsonObject.put("user_id", "5")
            jsonObject.put("customer_type", "1")
            Utility.showLog("jsonObject", "" + jsonObject)

            val request = MultipartBody.Builder()

            request.setType(MultipartBody.FORM)
                .addFormDataPart("action", "doctors_list")
                .addFormDataPart("user_id", userId.toString())
                .addFormDataPart("customer_type", userCustomerType.toString())
                .addFormDataPart("doctor_id", doctor?.doctorId ?: "0")
            val requestBody = request.build()

            viewModel.getDoctorList(requestBody).observe(viewLifecycleOwner, Observer {
                Utility.hideLoadingDialog()
                progressBar.visibility = View.GONE
                if (it == null)
                    return@Observer
                if (it.status == "200") {
                    var doctor1: Doctor? = null
                    if (it.data != null && it.data.isNotEmpty())
                        doctor1 = it.data[0]

                    if (doctor1 == null || TextUtils.isEmpty(doctor1.doctorId))
                        return@Observer

                    doctor = doctor1

                    setDoctorData()
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

    private fun setDoctorData() {
        if (doctor == null)
            return

        tvClinicName.text = doctor?.userName
        tvLocation.text = doctor?.address
        tvCategoryName.text =
            if (lang.contentEquals("en")) doctor?.categoryName else doctor?.categoryNameAr
        tvAppointmentLabel.text = (doctor?.currency ?: "SAR") + "" + (doctor?.perHour ?: "0") + "/h"
        tvAbout.text = doctor?.about
        appCompatRatingBar2.rating = doctor?.rating?.toFloat() ?: 0F

        val requestOptions: RequestOptions = RequestOptions()
            .placeholder(R.drawable.ic_clinic_doctor)
            .error(R.drawable.ic_clinic_doctor)
            .fitCenter()

        val img = doctor?.image
        Glide.with(ivProfile.context)
            .load(img)
            .fitCenter()
            .apply(requestOptions)
            .into(ivProfile)

        tvSpendsCount.text = "${DateUtils.displayTimeSlot(doctor?.fromTime)} - ${
            DateUtils.displayTimeSlot(doctor?.toTime)
        }"
    }
}