package com.wecareapp.android.ui.fragment.consultant

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.snackbar.Snackbar
import com.wecareapp.android.R
import com.wecareapp.android.adapter.AppointmentsAdapter
import com.wecareapp.android.adapter.ClinicAppointmentDoctorListAdapter
import com.wecareapp.android.adapter.DoctorSlotsAdapter
import com.wecareapp.android.model.Appointment
import com.wecareapp.android.model.Doctor
import com.wecareapp.android.model.DoctorSlotDataItem
import com.wecareapp.android.ui.fragment.BaseFragment
import com.wecareapp.android.ui.fragment.ICallBackToActivity
import com.wecareapp.android.utilities.Utility
import com.wecareapp.android.webservices.viewmodels.ConsultantViewModel
import com.wecareapp.android.webservices.viewmodels.PatientViewModel
import devs.mulham.horizontalcalendar.HorizontalCalendar
import devs.mulham.horizontalcalendar.utils.HorizontalCalendarListener
import kotlinx.android.synthetic.main.fragment_consultant_my_appointments.*
import kotlinx.android.synthetic.main.fragment_consultant_my_appointments.ivBack
import kotlinx.android.synthetic.main.fragment_consultant_my_appointments.ivProfile
import kotlinx.android.synthetic.main.fragment_consultant_my_appointments.progressBar
import kotlinx.android.synthetic.main.fragment_consultant_my_appointments.recyclerView
import kotlinx.android.synthetic.main.fragment_consultant_my_appointments.swipeRefresh
import kotlinx.android.synthetic.main.fragment_consultant_my_appointments.tvClinicName
import kotlinx.android.synthetic.main.fragment_consultant_my_appointments.tvNoData
import kotlinx.android.synthetic.main.fragment_consultant_my_appointments.tvUserName
import kotlinx.android.synthetic.main.fragment_patient_my_appointments.*
import okhttp3.MultipartBody
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class ConsultantMyAppointmentFragment(private val listener: ICallBackToActivity) : BaseFragment(),
    DoctorSlotsAdapter.Listener, ClinicAppointmentDoctorListAdapter.Listener,
    AppointmentsAdapter.Listener {

    private var selectedDate: Calendar? = null
    private var doctor: Doctor? = null
    private lateinit var viewModel: PatientViewModel
    private lateinit var adapter: AppointmentsAdapter
    private lateinit var adapterDoctors: ClinicAppointmentDoctorListAdapter
    private var list: MutableList<Appointment?> = mutableListOf()
    private var listDoctors: MutableList<Doctor?> = mutableListOf()
    private var showDocs = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this).get(PatientViewModel::class.java)
        // Inflate the layout for this fragment
        return inflater.inflate(
            R.layout.fragment_consultant_my_appointments, container, false
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
            adapter = AppointmentsAdapter(list, this)
            recyclerView.adapter = adapter

            rvDoctors.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapterDoctors = ClinicAppointmentDoctorListAdapter(listDoctors, this)
            rvDoctors.adapter = adapterDoctors

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
                    selectedDate = date
                    list.clear()
                    getAppointments(doctor, selectedDate, true)
                }
            }

            if (userCustomerType != 2) {
                getDoctorsList()
            } else {
                getAppointments(doctor, selectedDate, true)
                tvUserName.text = user?.userName

                val requestOptions: RequestOptions = RequestOptions()
                    .placeholder(R.drawable.ic_clinic_doctor)
                    .error(R.drawable.ic_clinic_doctor)
                    .fitCenter()

                val img = user?.profileImage
                Glide.with(ivProfile.context)
                    .load(img)
                    .fitCenter()
                    .apply(requestOptions)
                    .into(ivProfile)
            }

            swipeRefresh.setOnRefreshListener {
                getAppointments(doctor, selectedDate, true)
            }

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

            updateWish()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateWish() {
        if (userCustomerType != 3) {
            ivDocList.visibility = View.GONE
            rvDoctors.visibility = View.GONE
            ivProfile.isEnabled = false
            ivProfile.isClickable = false
        }

        val c = Calendar.getInstance()
        val timeOfDay = c[Calendar.HOUR_OF_DAY]
        val wish =
            when (timeOfDay) {
                in 0..11 -> {
                    getString(R.string.wish_good_morning)
                }
                in 12..15 -> {
                    getString(R.string.wish_good_afternoon)
                }
                in 16..20 -> {
                    getString(R.string.wish_good_evening)
                }
                in 21..23 -> {
                    getString(R.string.wish_good_night)
                }
                else -> ""
            }
        tvClinicName.text = wish
        tvClinicName.visibility = View.VISIBLE
    }

    private fun getDoctorsList() {
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
                .addFormDataPart("customer_type", userCustomerType.toString() ?: "1")
            val requestBody = request.build()

            viewModel.getDoctorList(requestBody).observe(viewLifecycleOwner, Observer {
                Utility.hideLoadingDialog()
                progressBar.visibility = View.GONE
                if (it == null)
                    return@Observer
                if (it.status == "200") {
                    it.data?.let { it1 -> listDoctors.addAll(it1) }
                    adapterDoctors.notifyDataSetChanged()

                    doctor = if (it.data?.size!! > 0) it.data[0] else null
                    setDoctorProfile(doctor)
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

    private fun setDoctorProfile(doctor: Doctor?) {
        getAppointments(doctor, selectedDate, false)
        tvUserName.text = doctor?.userName

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
    }

    private fun getAppointments(doctor: Doctor?, calendar: Calendar?, clearData: Boolean) {
        if (!checkingAccess())
            return
        if (clearData)
            list.clear()

        swipeRefresh.isRefreshing = true

        var docId = if (userCustomerType == 2) {
            user?.contactId.toString()
        } else {
            doctor?.doctorId.toString()
        }

        val cal = calendar ?: Calendar.getInstance()
        val df = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val date = df.format(cal.time)

        val request = MultipartBody.Builder()
        request.setType(MultipartBody.FORM)
            .addFormDataPart("action", "getDoctorAppointments")
            .addFormDataPart("user_id", userId.toString())
            .addFormDataPart("doctor_id", docId)
            .addFormDataPart("customer_type", userCustomerType.toString())
            .addFormDataPart("start", "0")
            .addFormDataPart("date", date)

        val requestBody = request.build()

        val viewModel = ViewModelProvider(this).get(ConsultantViewModel::class.java)
        viewModel.getDoctorAppointments(requestBody).observe(viewLifecycleOwner, Observer {
            Utility.hideLoadingDialog()
            progressBar.visibility = View.GONE
            swipeRefresh.isRefreshing = false
            if (it == null)
                return@Observer
            if (it.status == "200") {
                if (userCustomerType == 2)
                    it.appointments?.let { it1 -> list.addAll(it1) }
                else
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
//            .setOnClickListener {
//                deleteDialog.dismiss()
//            }
//        deleteDialogView.findViewById<View>(R.id.no).setOnClickListener { deleteDialog.dismiss() }

        deleteDialog.show()
    }

    override fun onItemSelected(category: Doctor?, adapterPosition: Int) {
        this.doctor = category
        getAppointments(category, selectedDate, false)
        ivDocList.setImageResource(R.drawable.ic_arrow_down_clinic_doc)
        rvDoctors.visibility = View.GONE
        setDoctorProfile(doctor)
    }

    override fun onItemSelected(clinic: Appointment?, adapterPosition: Int) {

    }

    override fun addReminder(appointment: Appointment?, adapterPosition: Int) {

    }

    override fun endReview(appointment: Appointment?, adapterPosition: Int) {

    }
}