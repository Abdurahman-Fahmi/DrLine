package com.wecareapp.android.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.wecareapp.android.R
import com.wecareapp.android.model.Doctor
import com.wecareapp.android.utilities.PopUtils
import com.wecareapp.android.utilities.Utility
import com.wecareapp.android.webservices.viewmodels.ClinicViewModel
import com.wecareapp.android.webservices.viewmodels.ConsultantViewModel
import kotlinx.android.synthetic.main.activity_cash_out.*
import kotlinx.android.synthetic.main.activity_cash_out.progressBar
import kotlinx.android.synthetic.main.activity_cash_out.swipeRefresh
import kotlinx.android.synthetic.main.fragment_consultant_profile.*
import okhttp3.MultipartBody
import org.json.JSONObject

class CashoutActivity : BaseActivity() {
    private var profile: Doctor? = null
    private lateinit var viewModel: ConsultantViewModel
    private var doctor: Doctor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cash_out)
        doctor = intent.extras?.getSerializable("doctor") as Doctor?

        viewModel = ViewModelProvider(this).get(ConsultantViewModel::class.java)

        ivBack.setOnClickListener {
            onBackPressed()
        }

        ivCashOut.setOnClickListener {
            requestWithdraw()
        }

        tvProfileBoost.setOnClickListener {
            startActivity(Intent(this, ProfileBoostActivity::class.java))
        }
        swipeRefresh.setOnRefreshListener {
            getBalance()
        }
        getBalance()
    }

    private fun getBalance() {
        if (userCustomerType == 3)
            getDoctorBalanceDashboard(doctor)
        else
            loadData()
    }

    private fun getDoctorBalanceDashboard(doctor: Doctor?) {
        if (!checkingAccess())
            return
        swipeRefresh.isRefreshing = true

        val docId = doctor?.doctorId ?: userId.toString()

        val request = MultipartBody.Builder()
        request.setType(MultipartBody.FORM)
            .addFormDataPart("action", "getDoctorBalanceDashboard")
            .addFormDataPart("user_id", userId.toString())
            .addFormDataPart("clinic_id", userId.toString())
            .addFormDataPart("doctor_id", docId)
            .addFormDataPart("start", "0")

        val requestBody = request.build()

        val viewModel = ViewModelProvider(this).get(ClinicViewModel::class.java)
        viewModel.getDoctorBalanceDashboard(requestBody).observe(this, Observer {
            Utility.hideLoadingDialog()
            progressBar.visibility = View.GONE
            swipeRefresh.isRefreshing = false
            if (it == null)
                return@Observer
            if (it.stauts == "200") {
                tvWalletBalance.text = it.currency + " " + (it.wallet ?: "0")
            } else {
                Snackbar.make(
                    findViewById(android.R.id.content),
                    it.message ?: getString(R.string.err_msg_oops_something_went_to_wrong),
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        })
    }


    private fun loadData() {
        if (checkingAccess()) {
            progressBar.visibility = View.VISIBLE

            val jsonObject = JSONObject()
            jsonObject.put("action", "getConsultantProfile")
            jsonObject.put("user_id", userId)
            Utility.showLog("jsonObject", "" + jsonObject)

            val request = MultipartBody.Builder()

            request.setType(MultipartBody.FORM)
                .addFormDataPart("action", "getConsultantProfile")
                .addFormDataPart("user_id", userId ?: "")
            val requestBody = request.build()

            viewModel.getConsultantProfile(requestBody).observe(this, Observer {
                Utility.hideLoadingDialog()
                progressBar.visibility = View.GONE
                if (it == null)
                    return@Observer
                if (it.status == "200") {

                    profile = it.profile?.get(0)
                    tvWalletBalance.text = profile?.currency + " " + (profile?.userBalance ?: "0")

                    /*tvClinicName.text = profile?.userName
                    tvClinicLocation.text = profile?.categoryName
                    tvAbout.text = profile?.about
                    tvTotalConsultations.text = profile?.totalBookings
                    tvPerHour.text = (profile?.perHour ?: "0") + "/h"
                    tvAvailableTime.text =
                        DateUtils.displayTimeSlot(profile?.fromAvailableTime) +
                                " - " + DateUtils.displayTimeSlot(profile?.toAvailableTime)

                    appCompatRatingBar.rating = profile?.rating?.toFloat() ?: 0F

                    val requestOptions: RequestOptions = RequestOptions()
                        .placeholder(R.drawable.ic_clinic_doctor)
                        .error(R.drawable.ic_clinic_doctor)
                        .fitCenter()

                    val img = profile?.profileImage
                    Glide.with(ivProfile.context)
                        .load(img)
                        .fitCenter()
                        .apply(requestOptions)
                        .into(ivProfile)*/
                } else {
                    Snackbar.make(
                        findViewById(android.R.id.content),
                        it.message ?: getString(R.string.err_msg_oops_something_went_to_wrong),
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            })
        }
    }


    private fun requestWithdraw() {
        if (checkingAccess()) {
            progressBar.visibility = View.VISIBLE

            val jsonObject = JSONObject()
            jsonObject.put("action", "withdrawRequest")
            jsonObject.put("user_id", userId)
            Utility.showLog("jsonObject", "" + jsonObject)

            val request = MultipartBody.Builder()

            request.setType(MultipartBody.FORM)
                .addFormDataPart("action", "withdrawRequest")
                .addFormDataPart("user_id", userId ?: "")
                .addFormDataPart("request_amount", etAmount.text.toString() ?: "")
                .addFormDataPart("bank_name", etBankName.text.toString() ?: "")
                .addFormDataPart("account_holder_name", etAccountTitle.text.toString() ?: "")
                .addFormDataPart("account_number", etAccountNumber.text.toString() ?: "")
                .addFormDataPart("account_iba", etIban.text.toString() ?: "")
                .addFormDataPart("swift_code", etSwiftCode.text.toString() ?: "")

            if (userCustomerType == 2) {
                request.addFormDataPart("doctor_id", userId ?: "")
            }

            val requestBody = request.build()

            viewModel.getConsultantProfile(requestBody).observe(this, Observer {
                Utility.hideLoadingDialog()
                progressBar.visibility = View.GONE
                if (it == null)
                    return@Observer
                if (it.status == "200") {

//                    profile = it.profile?.get(0)
//                    tvWalletBalance.text = profile?.currency + "" + (profile?.userBalance ?: "0")

                    /*tvClinicName.text = profile?.userName
                    tvClinicLocation.text = profile?.categoryName
                    tvAbout.text = profile?.about
                    tvTotalConsultations.text = profile?.totalBookings
                    tvPerHour.text = (profile?.perHour ?: "0") + "/h"
                    tvAvailableTime.text =
                        DateUtils.displayTimeSlot(profile?.fromAvailableTime) +
                                " - " + DateUtils.displayTimeSlot(profile?.toAvailableTime)

                    appCompatRatingBar.rating = profile?.rating?.toFloat() ?: 0F

                    val requestOptions: RequestOptions = RequestOptions()
                        .placeholder(R.drawable.ic_clinic_doctor)
                        .error(R.drawable.ic_clinic_doctor)
                        .fitCenter()

                    val img = profile?.profileImage
                    Glide.with(ivProfile.context)
                        .load(img)
                        .fitCenter()
                        .apply(requestOptions)
                        .into(ivProfile)*/
//                    Snackbar.make(
//                        findViewById(android.R.id.content),
//                        it.message ?: getString(R.string.err_msg_oops_something_went_to_wrong),
//                        Snackbar.LENGTH_SHORT
//                    ).show()
//                    finish()
                    PopUtils.alertDialog(
                        this,
                        it.message ?: getString(R.string.err_msg_oops_something_went_to_wrong),
                        getString(R.string.ok)
                    ) {
                        finish()
                    }
                } else {
                    Snackbar.make(
                        findViewById(android.R.id.content),
                        it.message ?: getString(R.string.err_msg_oops_something_went_to_wrong),
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            })
        }
    }

}