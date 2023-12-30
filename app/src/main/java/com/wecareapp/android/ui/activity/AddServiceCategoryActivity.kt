package com.wecareapp.android.ui.activity

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.wecareapp.android.R
import com.wecareapp.android.adapter.AddServiceCategoryListAdapter
import com.wecareapp.android.model.Category
import com.wecareapp.android.utilities.Utility
import com.wecareapp.android.webservices.viewmodels.ClinicViewModel
import com.wecareapp.android.webservices.viewmodels.PatientViewModel
import kotlinx.android.synthetic.main.activity_add_service_category.*
import okhttp3.MultipartBody
import org.json.JSONObject


class AddServiceCategoryActivity : BaseActivity(),
    AddServiceCategoryListAdapter.Listener {
    private var isFrom: String? = null
    private var selectedCategory: Category? = null
    private lateinit var viewModel: ClinicViewModel
    private var list: MutableList<Category?> = mutableListOf()
    private lateinit var adapter: AddServiceCategoryListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_service_category)

        if (intent.extras != null) {
            isFrom = intent?.extras?.getString("action", "")
        }

        if (!TextUtils.isEmpty(isFrom)) {
            actionAddCategory.text = getString(R.string.select)
        }

        viewModel = ViewModelProvider(this).get(ClinicViewModel::class.java)

        ivBack.setOnClickListener {
            onBackPressed()
        }

        actionAddCategory.setOnClickListener {
            addCategory()
        }

        recyclerView.layoutManager =
            GridLayoutManager(this, 3)
        adapter = AddServiceCategoryListAdapter(list, this)
        recyclerView.adapter = adapter

        loadCategoriesData()
    }

    private fun addCategory() {
        if (!TextUtils.isEmpty(isFrom)) {
            val resultIntent = Intent()
            resultIntent.putExtra("category", selectedCategory)
            setResult(RESULT_OK, resultIntent)
            finish()
            return
        }
        if (checkingAccess()) {
            progressBar.visibility = View.VISIBLE

            val jsonObject = JSONObject()
            jsonObject.put("action", "add_category")
            jsonObject.put("clinic_id", userId)
            Utility.showLog("jsonObject", "" + jsonObject)

            val request = MultipartBody.Builder()

            request.setType(MultipartBody.FORM)
                .addFormDataPart("action", "add_category")
                .addFormDataPart("customer_type", "3")
                .addFormDataPart("clinic_id", user?.contactId ?: "")
                .addFormDataPart("category_id", selectedCategory?.categoryId ?: "")

            val requestBody = request.build()

            val viewModel = ViewModelProvider(this).get(PatientViewModel::class.java)
            viewModel.commonRequest(requestBody).observe(this, Observer {
                Utility.hideLoadingDialog()
                progressBar.visibility = View.GONE
                if (it == null)
                    return@Observer

                if (it.status == "200") {
                    val returnIntent = Intent()
                    setResult(RESULT_OK, returnIntent)
                    finish()
                }

                Snackbar.make(
                    findViewById(android.R.id.content),
                    it.message ?: getString(R.string.err_msg_oops_something_went_to_wrong),
                    Snackbar.LENGTH_SHORT
                ).show()
            })
        }
    }

    private fun loadCategoriesData() {
        if (checkingAccess()) {
            progressBar.visibility = View.VISIBLE

            val jsonObject = JSONObject()
            jsonObject.put("action", "getCategoriesList")
            jsonObject.put("user_id", userId)
            Utility.showLog("jsonObject", "" + jsonObject)

            val request = MultipartBody.Builder()

            request.setType(MultipartBody.FORM)
                .addFormDataPart("action", "getCategoriesList")
                .addFormDataPart("customer_type", "1")
                .addFormDataPart("user_id", user?.contactId ?: "")

            val requestBody = request.build()

            val viewModel = ViewModelProvider(this).get(PatientViewModel::class.java)
            viewModel.getCategoriesList(requestBody).observe(this, Observer {
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

    override fun onItemSelected(category: Category?, adapterPosition: Int) {
        tvClinicName.setText(category?.categoryName)
        selectedCategory = category;
    }

}