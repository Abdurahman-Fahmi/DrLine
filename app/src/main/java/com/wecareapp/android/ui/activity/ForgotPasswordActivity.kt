package com.wecareapp.android.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.wecareapp.android.R
import com.wecareapp.android.utilities.Utility
import com.wecareapp.android.webservices.viewmodels.SignUpViewModel
import kotlinx.android.synthetic.main.activity_forgot_password.*
import okhttp3.MultipartBody

class ForgotPasswordActivity : BaseActivity(), View.OnClickListener {
    private var email: String? = ""
    private var customerType: String? = ""
    private var contactType: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        tvSend.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        if (v?.id == R.id.tvSend) {
            action()
        }
    }

    private fun action() {
        if (etEmail.text.isNullOrEmpty()) {
            Snackbar.make(
                findViewById(android.R.id.content),
                getString(R.string.err_msg_please_enter_email_contact),
                Snackbar.LENGTH_SHORT
            ).show()
            etEmail.requestFocus()
            return
        }

        Utility.showLoadingDialog(this, getString(R.string.msg_please_wait), false)

        val email = etEmail.text.toString()
        val contactType = if (email.contains("@")) "2" else "1"
        val requestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("action", "forgot_password")
            .addFormDataPart("user_contact", email ?: "")
//            .addFormDataPart("customer_type", customerType ?: "")
            .addFormDataPart("contact_type", contactType ?: "").build()

        val signUpViewModel = ViewModelProvider(this).get(SignUpViewModel::class.java)
        signUpViewModel.action(requestBody).observe(this, Observer {
            Utility.hideLoadingDialog()
            if (it == null)
                return@Observer
            if (it.status == "200") {
                finish()
                startActivity(Intent(this, OtpActivity::class.java).apply {
                    putExtra("email", email)
                    putExtra("action", "forgot_password")
                    putExtra("contact_type", contactType)
                })
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
//                Toast.makeText(this, getString(R.string.msg_success_reg), Toast.LENGTH_LONG).show()
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