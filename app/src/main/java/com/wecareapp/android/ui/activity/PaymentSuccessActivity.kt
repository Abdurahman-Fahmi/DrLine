package com.wecareapp.android.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.wecareapp.android.Constants
import com.wecareapp.android.R
import com.wecareapp.android.adapter.ReviewsListAdapter
import com.wecareapp.android.model.Rating
import com.wecareapp.android.ui.activity.clinic.ClinicMainActivityI
import com.wecareapp.android.ui.activity.consultant.ConsultantMainActivityI
import com.wecareapp.android.ui.activity.patient.PatientMainActivity
import com.wecareapp.android.utilities.Utility
import com.wecareapp.android.webservices.viewmodels.ConsultantViewModel
import kotlinx.android.synthetic.main.activity_payment_success.*
import okhttp3.MultipartBody
import org.json.JSONObject

class PaymentSuccessActivity : BaseActivity(), ReviewsListAdapter.Listener {
    private lateinit var viewModel: ConsultantViewModel
    private var list: MutableList<Rating?> = mutableListOf()
    private lateinit var adapter: ReviewsListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_success)

        viewModel = ViewModelProvider(this).get(ConsultantViewModel::class.java)

        ivBack.setOnClickListener {
            onBackPressed()
        }

        ivHome.setOnClickListener {
            val intent = when (Utility.getSharedPreferenceInteger(this, Constants.LOGIN_TYPE, -1)) {
                1 -> {
                    Intent(this, PatientMainActivity::class.java)
                }
                2 -> {
                    Intent(this, ConsultantMainActivityI::class.java)
                }
                3 -> {
                    Intent(this, ClinicMainActivityI::class.java)
                }
                else -> {
                    Intent(this, SignInActivity::class.java)
                }
            }
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        if (intent.extras != null) {
            val type = intent.extras?.getString("type", "")
            val status = intent.extras?.getString("status", "")
            if (type?.contentEquals("payment") == true) {
                if (status?.contentEquals("success") == true) {
                    tvClinicName.text = getString(R.string.payment_success)
                    ivProfile.setImageResource(R.drawable.ic_checked)
                } else {
                    tvClinicName.text = getString(R.string.payment_failed)
                    ivProfile.setImageResource(R.drawable.ic_baseline_highlight_off_24)
                }
            }
        } else {
            finish()
        }
    }

    private fun loadData() {
        if (checkingAccess()) {
            progressBar.visibility = View.VISIBLE

            val jsonObject = JSONObject()
            jsonObject.put("action", "ratings")
//            jsonObject.put("user_id", userId)
            jsonObject.put("doctor_id", userId)
            Utility.showLog("jsonObject", "" + jsonObject)

            val request = MultipartBody.Builder()

            request.setType(MultipartBody.FORM)
                .addFormDataPart("action", "ratings")
                .addFormDataPart("doctor_id", user?.contactId ?: "")
//                .addFormDataPart("user_id", userId ?: "")
            val requestBody = request.build()

            viewModel.getRatings(requestBody).observe(this, Observer {
                Utility.hideLoadingDialog()
                progressBar.visibility = View.GONE
                if (it == null)
                    return@Observer
                if (it.status == "200") {
                    it.data?.let { it1 -> list.addAll(it1) }
                    if (list.isEmpty()) {
//                        tvNoData.visibility = View.VISIBLE
//                        recyclerView.visibility = View.INVISIBLE
                    } else {
//                        tvNoData.visibility = View.GONE
//                        recyclerView.visibility = View.VISIBLE
                        adapter.notifyDataSetChanged()
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

    override fun onItemSelected(clinic: Rating?, adapterPosition: Int) {

    }

}