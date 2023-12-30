package com.wecareapp.android.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.wecareapp.android.R
import com.wecareapp.android.adapter.PlansListAdapter
import com.wecareapp.android.model.PlansItem
import com.wecareapp.android.model.SlotResponse
import com.wecareapp.android.utilities.Utility
import com.wecareapp.android.webservices.viewmodels.ConsultantViewModel
import com.wecareapp.android.webservices.viewmodels.PatientViewModel
import kotlinx.android.synthetic.main.activity_profile_boost.*
import okhttp3.MultipartBody
import org.json.JSONObject

class ProfileBoostActivity : BaseActivity(), PlansListAdapter.Listener {
    private lateinit var viewModel: ConsultantViewModel
    private var list: MutableList<PlansItem?> = mutableListOf()
    private lateinit var adapter: PlansListAdapter
    private var selectedPlan: PlansItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_boost)

        viewModel = ViewModelProvider(this).get(ConsultantViewModel::class.java)

        list.clear()
        recyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        adapter = PlansListAdapter(list, this)

        recyclerView.adapter = adapter
        loadData()

        ivBack.setOnClickListener {
            onBackPressed()
        }

        ivCashOut.setOnClickListener {
            payNow(selectedPlan)
        }

//        swipeRefresh.setOnRefreshListener {
//            loadData()
//        }
    }

    private fun loadData() {
        if (checkingAccess()) {
            progressBar.visibility = View.VISIBLE

            val jsonObject = JSONObject()
            jsonObject.put("action", "getPlans")
//            jsonObject.put("user_id", userId)
            jsonObject.put("doctor_id", userId)
            jsonObject.put("plans_type", if (userCustomerType == 2) "consultancy" else "clinic")
            Utility.showLog("jsonObject", "" + jsonObject)

            val request = MultipartBody.Builder()

            request.setType(MultipartBody.FORM)
                .addFormDataPart("action", "getPlans")
                .addFormDataPart("doctor_id", user?.contactId ?: "")
                .addFormDataPart(
                    "plans_type",
                    if (userCustomerType == 2) "consultancy" else "clinic"
                )
//                .addFormDataPart("user_id", userId ?: "")
            val requestBody = request.build()

            viewModel.getPlans(requestBody).observe(this, Observer {
                Utility.hideLoadingDialog()
                progressBar.visibility = View.GONE
                if (it == null)
                    return@Observer
                if (it.status == "200") {
                    it.plans?.let { it1 -> list.addAll(it1) }
                    if (list.isEmpty()) {
//                        tvNoData.visibility = View.VISIBLE
                        recyclerView.visibility = View.INVISIBLE
                    } else {
//                        tvNoData.visibility = View.GONE
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

    override fun onItemSelected(clinic: PlansItem?, adapterPosition: Int) {
        for (category in list) {
            category?.selected = false
        }

        clinic?.selected = true
        adapter.notifyDataSetChanged()
        selectedPlan = clinic
//        payNow(clinic)
    }

    private fun payNow(plansItem: PlansItem?) {
        if (checkingAccess()) {
            progressBar.visibility = View.VISIBLE

            val request = MultipartBody.Builder()
            request.setType(MultipartBody.FORM)
                .addFormDataPart("action", "paymentProcessMembership")
                .addFormDataPart("user_id", userId.toString())
                .addFormDataPart("plan_id", plansItem?.id ?: "")
            val requestBody = request.build()

            val viewModel = ViewModelProvider(this).get(PatientViewModel::class.java)
            viewModel.slotRequest2(requestBody).observe(this, Observer {
                Utility.hideLoadingDialog()
                progressBar.visibility = View.GONE

                if (it == null) {
                    Snackbar.make(
                        findViewById(android.R.id.content),
                        getString(R.string.err_msg_oops_something_went_to_wrong),
                        Snackbar.LENGTH_SHORT
                    ).show()
                    return@Observer
                }
                val str = it.body()?.string() ?: return@Observer;

                if (str.contains("html")) {
                    startActivity(Intent(this, WebViewActivity::class.java).apply {
                        putExtra("content", str)
                        putExtra("payment_url", it.raw().request.url.toString())
                    })
                } else {
                    val slotResponse = Gson().fromJson(str, SlotResponse::class.java)
                    /*if (slotResponse.status == "200") {

                    } else {*/
                    Snackbar.make(
                        findViewById(android.R.id.content),
                        slotResponse.message
                            ?: getString(R.string.err_msg_oops_something_went_to_wrong),
                        Snackbar.LENGTH_SHORT
                    ).show()
//                    }
                }
            })
        }
    }
}