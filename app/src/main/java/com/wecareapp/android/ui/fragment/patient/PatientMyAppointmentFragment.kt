package com.wecareapp.android.ui.fragment.patient

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatRatingBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.snackbar.Snackbar
import com.wecareapp.android.R
import com.wecareapp.android.adapter.AppointmentsAdapter
import com.wecareapp.android.adapter.DoctorSlotsAdapter
import com.wecareapp.android.model.Appointment
import com.wecareapp.android.model.DoctorSlotDataItem
import com.wecareapp.android.ui.fragment.BaseFragment
import com.wecareapp.android.ui.fragment.ICallBackToActivity
import com.wecareapp.android.utilities.Utility
import com.wecareapp.android.webservices.viewmodels.PatientViewModel
import devs.mulham.horizontalcalendar.HorizontalCalendar
import devs.mulham.horizontalcalendar.utils.HorizontalCalendarListener
import kotlinx.android.synthetic.main.fragment_patient_my_appointments.*
import okhttp3.MultipartBody
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class PatientMyAppointmentFragment : BaseFragment(),
    DoctorSlotsAdapter.Listener, AppointmentsAdapter.Listener {

    private var selectedDate: Calendar = Calendar.getInstance()
    private lateinit var viewModel: PatientViewModel
    private lateinit var adapter: AppointmentsAdapter
    private var list: MutableList<Appointment?> = mutableListOf()
    private lateinit var listener: ICallBackToActivity

    companion object {
        fun newInstance(
            bundle: Bundle,
            listener: ICallBackToActivity
        ): PatientMyAppointmentFragment {
            val fragment = PatientMyAppointmentFragment()
            fragment.arguments = bundle
            fragment.setListener(listener)
            return fragment
        }

        fun newInstance(listener: ICallBackToActivity): PatientMyAppointmentFragment {
            val fragment = PatientMyAppointmentFragment()
            fragment.setListener(listener)
            return fragment
        }

        fun newInstance(bundle: Bundle): PatientMyAppointmentFragment {
            val fragment = PatientMyAppointmentFragment()
            fragment.arguments = bundle
            return fragment
        }

        fun newInstance(): PatientMyAppointmentFragment {
            return PatientMyAppointmentFragment()
        }
    }

    private fun setListener(listener: ICallBackToActivity) {
        this.listener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this).get(PatientViewModel::class.java)
        // Inflate the layout for this fragment
        return inflater.inflate(
            R.layout.fragment_patient_my_appointments, container, false
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
            list.clear()
            adapter = AppointmentsAdapter(list, this)
            adapter.setUserType(userCustomerType)
            recyclerView.adapter = adapter

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
                    selectedDate = date ?: Calendar.getInstance()
                    list.clear()
                    getAppointments(selectedDate, false)
                }
            }

            getAppointments(selectedDate, true)

            swipeRefresh.setOnRefreshListener {
                getAppointments(selectedDate, true)
            }

            tvUserName.text = user?.userName

            val requestOptions: RequestOptions = RequestOptions()
                .placeholder(R.drawable.ic_clinic_doctor)
                .error(R.drawable.ic_clinic_doctor)
                .fitCenter()

            val img = user?.profileImage
            Glide.with(this)
                .load(img)
                .fitCenter()
                .apply(requestOptions)
                .into(ivProfile)

            updateWish()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateWish() {
        if (userCustomerType != 3) {
//            ivDocList.visibility = View.GONE
//            rvDoctors.visibility = View.GONE
            ivProfile.isEnabled = false
            ivProfile.isClickable = false
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
        } else {
            tvClinicName.visibility = View.GONE
        }
    }

    private fun getAppointments(calendar: Calendar?, clearData: Boolean) {
        if (!checkingAccess())
            return
        if (clearData)
            list.clear()

        swipeRefresh.isRefreshing = true

        val cal = calendar ?: Calendar.getInstance()
        val df = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val date = df.format(cal.time)

        val request = MultipartBody.Builder()
        request.setType(MultipartBody.FORM)
            .addFormDataPart("action", "getPatientDoctorAppointments")
            .addFormDataPart("user_id", userId.toString())
            .addFormDataPart("customer_type", "1")
            .addFormDataPart("start", "0")
            .addFormDataPart("date", date)

        val requestBody = request.build()

        viewModel.getPatientDoctorAppointments(requestBody).observe(viewLifecycleOwner, Observer {
            Utility.hideLoadingDialog()
            progressBar.visibility = View.GONE
            swipeRefresh.isRefreshing = false
            if (it == null)
                return@Observer
            if (it.status == "200") {
                it.appointments?.let { it1 -> list.addAll(it1) }
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


    override fun onItemSelected(doctor: DoctorSlotDataItem?, adapterPosition: Int) {

    }

    override fun onClickedEndReview(doctor: DoctorSlotDataItem?, adapterPosition: Int) {
        val factory = LayoutInflater.from(context)
        val deleteDialogView: View = factory.inflate(R.layout.dialog_doctor_end_review, null)
        val deleteDialog: AlertDialog = AlertDialog.Builder(requireContext()).create()
        deleteDialog.setView(deleteDialogView)
        /* deleteDialogView.findViewById<View>(R.id.no)
             .setOnClickListener { //your business logic
                 deleteDialog.dismiss()
             }
         deleteDialogView.findViewById<View>(R.id.no).setOnClickListener { deleteDialog.dismiss() }
 */
        deleteDialog.show()
    }

    override fun onItemSelected(clinic: Appointment?, adapterPosition: Int) {

    }

    override fun addReminder(appointment: Appointment?, adapterPosition: Int) {
        addReminder(appointment)
    }

    private fun addReminder(appointment: Appointment?) {
        if (checkingAccess()) {
            progressBar.visibility = View.VISIBLE


            val df = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val date = df.format(Date())
            var selectedDate1: String? = null
            if (selectedDate1 == null)
                selectedDate1 = df.format(Calendar.getInstance().time)

//            val selectedTime = "00:00 am"

            val t = "00:00 am"
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
            jsonObject.put("customer_type", userCustomerType)
            jsonObject.put("doctor_id", appointment?.doctorId)
            jsonObject.put("book_date", selectedDate ?: "")
            jsonObject.put("book_time", d2)
            jsonObject.put("slot_price", "20")
            Utility.showLog("jsonObject", "" + jsonObject)

            val request = MultipartBody.Builder()

            request.setType(MultipartBody.FORM)
                .addFormDataPart("action", "addSlotReminder")
                .addFormDataPart("user_id", userId.toString())
                .addFormDataPart("customer_type", userCustomerType.toString())
                .addFormDataPart("doctor_id", appointment?.doctorId.toString())
                .addFormDataPart("book_date", selectedDate1 ?: "")
                .addFormDataPart("appointment_date", selectedDate1 ?: "")
                .addFormDataPart("book_time", d2)
                .addFormDataPart("appointment_time", d2)
                .addFormDataPart("booking_id", appointment?.bookingId ?: "0")
            val requestBody = request.build()

            viewModel.commonRequest(requestBody).observe(viewLifecycleOwner, Observer {
                Utility.hideLoadingDialog()
                progressBar.visibility = View.GONE
                if (it == null)
                    return@Observer
//                getAppointments(selectedDate, true)
                if (it.status == "200") {
                    Snackbar.make(
                        requireActivity().findViewById(android.R.id.content),
                        getString(R.string.your_reminder_reg)
                            ?: getString(R.string.err_msg_oops_something_went_to_wrong),
                        Snackbar.LENGTH_SHORT
                    ).show()
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


    override fun endReview(appointment: Appointment?, adapterPosition: Int) {
        val factory = LayoutInflater.from(context)
        val dialogView: View = factory.inflate(R.layout.dialog_doctor_end_review, null)
        val deleteDialog: AlertDialog = AlertDialog.Builder(requireContext()).create()
        deleteDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        deleteDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
        deleteDialog.setView(dialogView)
        val tvComment: EditText = dialogView.findViewById(R.id.tvComment)
        val ivImage: ImageView = dialogView.findViewById(R.id.ivImage)
        val tvClinicName: TextView = dialogView.findViewById(R.id.tvClinicName)
        val appCompatRatingBar: AppCompatRatingBar =
            dialogView.findViewById(R.id.appCompatRatingBar)

        tvClinicName.text = appointment?.name

        val requestOptions: RequestOptions = RequestOptions()
            .placeholder(R.drawable.ic_clinic_doctor)
            .error(R.drawable.ic_clinic_doctor)
            .fitCenter()

        val img = appointment?.image
        Glide.with(this)
            .load(img)
            .fitCenter()
            .apply(requestOptions)
            .into(ivImage)

        dialogView.findViewById<View>(R.id.ivClose)
            .setOnClickListener {
                deleteDialog.dismiss()
            }

        dialogView.findViewById<View>(R.id.ivDone).setOnClickListener {
            updateReview(
                appointment?.bookingId ?: "",
                appCompatRatingBar.rating.toString(),
                tvComment.text.toString()
            )
            deleteDialog.dismiss()
        }

        deleteDialog.show()
    }

    private fun updateReview(bookingId: String, rating: String, comment: String) {
        if (!checkingAccess())
            return
        swipeRefresh.isRefreshing = true

        val request = MultipartBody.Builder()
        request.setType(MultipartBody.FORM)
            .addFormDataPart("action", "add_rating")
            .addFormDataPart("user_id", userId ?: "")
            .addFormDataPart("customer_type", userCustomerType.toString())
            .addFormDataPart("start", "0")
            .addFormDataPart("comment", comment)
            .addFormDataPart("rating", rating)
            .addFormDataPart("booking_id", bookingId)

        val requestBody = request.build()

        viewModel.commonRequest(requestBody).observe(viewLifecycleOwner, Observer {
            Utility.hideLoadingDialog()
            progressBar.visibility = View.GONE
            swipeRefresh.isRefreshing = false
            if (it == null)
                return@Observer
            if (it.status == "200") {
                getAppointments(selectedDate, true)
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