package com.wecareapp.android.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.wecareapp.android.R
import com.wecareapp.android.adapter.ReviewsListAdapter
import com.wecareapp.android.model.Rating
import com.wecareapp.android.utilities.Utility
import com.wecareapp.android.webservices.viewmodels.ConsultantViewModel
import kotlinx.android.synthetic.main.activity_cash_out.*
import kotlinx.android.synthetic.main.activity_consultant_reviews.*
import kotlinx.android.synthetic.main.activity_consultant_reviews.ivBack
import kotlinx.android.synthetic.main.activity_consultant_reviews.progressBar
import kotlinx.android.synthetic.main.activity_consultant_reviews.recyclerView
import kotlinx.android.synthetic.main.activity_consultant_reviews.tvNoData
import kotlinx.android.synthetic.main.activity_pay.*
import kotlinx.android.synthetic.main.fragment_notifications_patient.*
import okhttp3.MultipartBody
import org.json.JSONObject

class PayActivity : BaseActivity(), ReviewsListAdapter.Listener {
    private lateinit var viewModel: ConsultantViewModel
    private var list: MutableList<Rating?> = mutableListOf()
    private lateinit var adapter: ReviewsListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pay)

        viewModel = ViewModelProvider(this).get(ConsultantViewModel::class.java)

        ivBack.setOnClickListener {
            onBackPressed()
        }

        ivPayNow.setOnClickListener {
            startActivity(Intent(this, PaymentSuccessActivity::class.java))
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
                        tvNoData.visibility = View.VISIBLE
                        recyclerView.visibility = View.INVISIBLE
                    } else {
                        tvNoData.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
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