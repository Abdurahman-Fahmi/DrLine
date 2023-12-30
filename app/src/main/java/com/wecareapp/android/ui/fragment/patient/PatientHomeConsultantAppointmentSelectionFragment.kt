package com.wecareapp.android.ui.fragment.patient

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.wecareapp.android.R
import com.wecareapp.android.adapter.TimeSlotsAdapter
import com.wecareapp.android.model.Doctor
import com.wecareapp.android.model.SlotResponse
import com.wecareapp.android.model.TimeSlot
import com.wecareapp.android.ui.activity.WebViewActivity
import com.wecareapp.android.ui.fragment.BaseFragment
import com.wecareapp.android.ui.fragment.ICallBackToActivity
import com.wecareapp.android.utilities.DateUtils
import com.wecareapp.android.utilities.Utility
import com.wecareapp.android.webservices.viewmodels.PatientViewModel
import kotlinx.android.synthetic.main.fragment_patient_consultant_appointment_selection.*
import okhttp3.MultipartBody
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class PatientHomeConsultantAppointmentSelectionFragment(private val listener: ICallBackToActivity) :
    BaseFragment(), TimeSlotsAdapter.Listener {

    private var bookingId: String? = ""
    private var selectedDate: String? = null
    private var selectedTime: TimeSlot? = null
    private lateinit var adapter: TimeSlotsAdapter
    private lateinit var timeSlotList: MutableList<TimeSlot>
    private var doctor: Doctor? = null
    private lateinit var viewModel: PatientViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this).get(PatientViewModel::class.java)
        // Inflate the layout for this fragment
        return inflater.inflate(
            R.layout.fragment_patient_consultant_appointment_selection, container, false
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {

            doctor =
                if (arguments?.getSerializable("doctor") == null) null else arguments?.getSerializable(
                    "doctor"
                ) as Doctor

            viewAddReminder.visibility = View.GONE

            if (doctor == null)
                listener.onBackClicked(true)

            val fromTime = doctor?.fromTime
            val toTime = doctor?.toTime

            val timeSlots: MutableList<String> = DateUtils.displayTimeSlots(fromTime!!, toTime!!)

            timeSlotList = mutableListOf<TimeSlot>()
            for (timeSlot in timeSlots) {
                timeSlotList.add(TimeSlot(timeSlot, false))
            }

            val cal = Calendar.getInstance()
            cal.add(Calendar.DATE, 1)

            calendarView.minDate = cal.timeInMillis
            calendarView.performClick()

            try {
                val sdf = SimpleDateFormat("yyyy-MM-dd")
                selectedDate = sdf.format(cal.time)
                loadSlots(selectedDate!!)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
                val m = month + 1
                selectedDate = "$year-$m-$dayOfMonth"
                loadSlots(selectedDate!!)
            }

            ivBack.setOnClickListener {
                listener.onBackClicked(true)
            }

            if (!isLogged)
                openLoginActivity()

            ivPayNow.setOnClickListener {
                if (!isLogged)
                    openLoginActivity()
                else
                    slotRequest()
            }

            viewAddReminder.setOnClickListener {
                addReminder()
            }

//            loadData()

            recyclerView.layoutManager = GridLayoutManager(context, 4)
            adapter = TimeSlotsAdapter(timeSlotList, this)
            recyclerView.adapter = adapter
            recyclerView.itemAnimator = DefaultItemAnimator()

            loadDoctor()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadDoctor() {
        tvConsultantFee.text = (doctor?.currency ?: "SAR") + doctor?.perHour + "/h"

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

    private fun slotRequest() {
        if (checkingAccess()) {
            progressBar.visibility = View.VISIBLE


            val df = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val date = df.format(calendarView.date)

            if (selectedDate == null)
                selectedDate = df.format(Calendar.getInstance().time)

            val t = selectedTime?.timeSlot
            val ff = SimpleDateFormat("hh:mm", Locale.US)
//            val ff = SimpleDateFormat("hh:mm a", Locale.US)
            var d22 = Calendar.getInstance().time
            try {
                d22 = ff.parse(t)
            } catch (e: Exception) {
            }

            val fff = SimpleDateFormat("hh:mm:00", Locale.US)
            val d2 = fff.format(d22)

            val jsonObject = JSONObject()
            jsonObject.put("action", "bookSlot2")
            jsonObject.put("user_id", "2")
            jsonObject.put("customer_type", "1")
            jsonObject.put("doctor_id", doctor?.doctorId)
            jsonObject.put("book_date", "2020-10-20")
            jsonObject.put("book_time", "17:30:00")
            jsonObject.put("slot_price", "20")
            Utility.showLog("jsonObject", "" + jsonObject)

            val request = MultipartBody.Builder()

            request.setType(MultipartBody.FORM)
                .addFormDataPart("action", "bookSlot2")
                .addFormDataPart("user_id", userId.toString())
                .addFormDataPart("customer_type", "1")
                .addFormDataPart("doctor_id", doctor?.doctorId.toString())
                .addFormDataPart("book_date", selectedDate ?: "")
//                .addFormDataPart("book_time", d2)
                .addFormDataPart("book_time", t ?: "")
                .addFormDataPart("slot_price", doctor?.perHour ?: "0")
            val requestBody = request.build()

            viewModel.slotRequest(requestBody).observe(viewLifecycleOwner, Observer {
                Utility.hideLoadingDialog()
                progressBar.visibility = View.GONE
                if (it == null)
                    return@Observer
                if (it.status == "200") {
                    bookingId = it.bookingId
                    Snackbar.make(
                        requireActivity().findViewById(android.R.id.content),
                        it.message
                            ?: getString(R.string.err_msg_oops_something_went_to_wrong),
                        Snackbar.LENGTH_SHORT
                    ).show()

                    openChat(null, doctor)
//                    slotRequest2(it.bookingId)
                } else {
                    Snackbar.make(
                        requireActivity().findViewById(android.R.id.content),
                        it.message
                            ?: getString(R.string.err_msg_oops_something_went_to_wrong),
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            })
        }
    }

    private fun slotRequest2(bookingId: String?) {
        if (checkingAccess()) {
            progressBar.visibility = View.VISIBLE


            val df = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val date = df.format(calendarView.date)

            if (selectedDate == null)
                selectedDate = df.format(Calendar.getInstance().time)

            val t = selectedTime?.timeSlot
            val ff = SimpleDateFormat("hh:mm a", Locale.US)
            var d22 = Calendar.getInstance().time
            try {
                d22 = ff.parse(t)
            } catch (e: Exception) {
            }

            val fff = SimpleDateFormat("hh:mm:00", Locale.US)
            val d2 = fff.format(d22)

            val jsonObject = JSONObject()
            jsonObject.put("action", "slotPaymentNow")
            jsonObject.put("user_id", "2")
            jsonObject.put("customer_type", "1")
            jsonObject.put("doctor_id", doctor?.doctorId)
            jsonObject.put("book_date", "2020-10-20")
            jsonObject.put("book_time", "17:30:00")
            jsonObject.put("slot_price", "20")
            Utility.showLog("jsonObject", "" + jsonObject)

            val request = MultipartBody.Builder()

            request.setType(MultipartBody.FORM)
                .addFormDataPart("action", "slotPaymentNow")
                .addFormDataPart("user_id", userId.toString())
                .addFormDataPart("booking_id", bookingId ?: "")
                .addFormDataPart("customer_type", "1")
                .addFormDataPart("doctor_id", doctor?.doctorId.toString())
                .addFormDataPart("book_date", selectedDate ?: "")
                .addFormDataPart("book_time", d2)
                .addFormDataPart("slot_price", doctor?.perHour ?: "0")
            val requestBody = request.build()

            viewModel.slotRequest2(requestBody).observe(viewLifecycleOwner, Observer {
                Utility.hideLoadingDialog()
                progressBar.visibility = View.GONE
                val str = it.body()?.string() ?: return@Observer;

                if (str.contains("html")) {
                    activity?.startActivity(Intent(activity, WebViewActivity::class.java).apply {
                        putExtra("content", str)
                        putExtra("payment_url", it.raw().request.url.toString())
                    })
                } else {
                    val slotResponse = Gson().fromJson(str, SlotResponse::class.java)
                    /*if (slotResponse.status == "200") {

                    } else {*/
                    Snackbar.make(
                        requireActivity().findViewById(android.R.id.content),
                        slotResponse.message
                            ?: getString(R.string.err_msg_oops_something_went_to_wrong),
                        Snackbar.LENGTH_SHORT
                    ).show()
//                    }
                }
            })
        }
    }

    private fun addReminder() {
        if (checkingAccess()) {
            progressBar.visibility = View.VISIBLE


            val df = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val date = df.format(calendarView.date)

            if (selectedDate == null)
                selectedDate = df.format(Calendar.getInstance().time)

            val t = selectedTime?.timeSlot
            val ff = SimpleDateFormat("hh:mm a", Locale.US)
            var d22 = Calendar.getInstance().time
            try {
                d22 = ff.parse(t)
            } catch (e: Exception) {
            }

            val fff = SimpleDateFormat("hh:mm:00", Locale.US)
            val d2 = fff.format(d22)

            val jsonObject = JSONObject()
            jsonObject.put("action", "addSlotReminder")
            jsonObject.put("user_id", userId.toString())
            jsonObject.put("customer_type", "1")
            jsonObject.put("doctor_id", doctor?.doctorId)
            jsonObject.put("book_date", selectedDate ?: "")
            jsonObject.put("book_time", d2)
            jsonObject.put("slot_price", "20")
            Utility.showLog("jsonObject", "" + jsonObject)

            val request = MultipartBody.Builder()

            request.setType(MultipartBody.FORM)
                .addFormDataPart("action", "addSlotReminder")
                .addFormDataPart("user_id", userId.toString())
                .addFormDataPart("customer_type", "1")
                .addFormDataPart("doctor_id", doctor?.doctorId.toString())
                .addFormDataPart("book_date", selectedDate ?: "")
                .addFormDataPart("appointment_date", selectedDate ?: "")
                .addFormDataPart("book_time", d2)
                .addFormDataPart("appointment_time", d2)
                .addFormDataPart("slot_price", doctor?.perHour ?: "0")
                .addFormDataPart("booking_id", bookingId ?: "0")
            val requestBody = request.build()

            viewModel.commonRequest(requestBody).observe(viewLifecycleOwner, Observer {
                Utility.hideLoadingDialog()
                progressBar.visibility = View.GONE
                if (it == null)
                    return@Observer
                /*if (it.status == "200") {

                } else {*/
                Snackbar.make(
                    requireActivity().findViewById(android.R.id.content),
                    it.message
                        ?: getString(R.string.err_msg_oops_something_went_to_wrong),
                    Snackbar.LENGTH_SHORT
                ).show()
//                }
            })
        }
    }

    private fun loadSlots(selectedDate: String) {
        if (checkingAccess()) {
            progressBar.visibility = View.VISIBLE


            val df = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val date = df.format(calendarView.date)

            if (this.selectedDate == null)
                this.selectedDate = df.format(Calendar.getInstance().time)

            val t = selectedTime?.timeSlot
            val ff = SimpleDateFormat("hh:mm a", Locale.US)
            var d22 = Calendar.getInstance().time
            try {
                d22 = ff.parse(t)
            } catch (e: Exception) {
            }

            val fff = SimpleDateFormat("hh:mm:00", Locale.US)
            val d2 = fff.format(d22)

            val jsonObject = JSONObject()
            jsonObject.put("action", "getDoctorSlots")
            jsonObject.put("user_id", userId.toString())
            jsonObject.put("customer_type", "1")
            jsonObject.put("doctor_id", doctor?.doctorId)
            jsonObject.put("book_date", this.selectedDate ?: "")
            jsonObject.put("book_time", d2)
            jsonObject.put("slot_price", "20")
            Utility.showLog("jsonObject", "" + jsonObject)

            val request = MultipartBody.Builder()

            request.setType(MultipartBody.FORM)
                .addFormDataPart("action", "getDoctorSlots")
                .addFormDataPart("user_id", userId.toString())
                .addFormDataPart("customer_type", userCustomerType.toString())
                .addFormDataPart("doctor_id", doctor?.doctorId.toString())
                .addFormDataPart("book_date", this.selectedDate ?: "")
                .addFormDataPart("date", this.selectedDate ?: "")
                .addFormDataPart("appointment_date", this.selectedDate ?: "")
                .addFormDataPart("book_time", d2)
                .addFormDataPart("appointment_time", d2)
                .addFormDataPart("slot_price", doctor?.perHour ?: "0")
                .addFormDataPart("booking_id", bookingId ?: "0")
            val requestBody = request.build()

            viewModel.getDoctorSlots2(requestBody).observe(viewLifecycleOwner, Observer {
                Utility.hideLoadingDialog()
                progressBar.visibility = View.GONE
                if (it == null)
                    return@Observer
                if (it.status == "200") {
                    timeSlotList.clear()
                    it.slots?.toMutableList()?.let { it1 -> timeSlotList.addAll(it1) }
                    adapter.notifyDataSetChanged()
                } else {
                    Snackbar.make(
                        requireActivity().findViewById(android.R.id.content),
                        it.message
                            ?: getString(R.string.err_msg_oops_something_went_to_wrong),
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            })
        }
    }

    override fun onItemSelected(timeSlot: TimeSlot?, adapterPosition: Int) {
        for ((index, time) in timeSlotList.withIndex()) {
            timeSlotList[index].selected = timeSlot == time
        }
        adapter.notifyDataSetChanged()

        selectedTime = timeSlot
    }
}