package com.wecareapp.android.ui.activity

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.wecareapp.android.R
import com.wecareapp.android.utilities.Utility
import com.wecareapp.android.webservices.viewmodels.SignUpViewModel
import kotlinx.android.synthetic.main.activity_change_password.*
import okhttp3.MultipartBody

class ChangePasswordActivity : BaseActivity(), View.OnClickListener {
    private var action: String? = ""
    private var email: String? = ""
    private var customerType: String? = ""
    private var contactType: String? = ""
    private var contactOtp: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)

        if (intent.extras != null) {
            action = intent?.extras?.getString("action", "")
            email = intent?.extras?.getString("email", "")
            customerType = intent?.extras?.getString("customer_type", "")
            contactType = intent?.extras?.getString("contact_type", "")
            contactOtp = intent?.extras?.getString("contact_otp", "")
        }
        if (action?.contentEquals("forgot_password") == true) {
            tvLabel.setText(R.string.forgot)
            etOldPassword.visibility = View.GONE
        } else {
            tvLabel.setText(R.string.change)
            etOldPassword.visibility = View.VISIBLE
        }

        tvAction.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        if (v?.id == R.id.tvAction) {
            action()
        }
    }

    private fun action() {
        if (action?.contentEquals("forgot_password") == false) {
            if (etOldPassword.text.isNullOrEmpty()) {
                Snackbar.make(
                    findViewById(android.R.id.content),
                    getString(R.string.err_msg_please_enter_old_password),
                    Snackbar.LENGTH_SHORT
                ).show()
                etOldPassword.requestFocus()
                return
            }
        }
        if (etNewPassword.text.isNullOrEmpty()) {
            Snackbar.make(
                findViewById(android.R.id.content),
                getString(R.string.err_msg_please_enter_new_password),
                Snackbar.LENGTH_SHORT
            ).show()
            etNewPassword.requestFocus()
            return
        }
        if (etConfirmPassword.text.isNullOrEmpty()) {
            Snackbar.make(
                findViewById(android.R.id.content),
                getString(R.string.err_msg_please_enter_confirm_password),
                Snackbar.LENGTH_SHORT
            ).show()
            etConfirmPassword.requestFocus()
            return
        }
        if (!etConfirmPassword.text.toString().contentEquals(etNewPassword.text.toString())) {
            Snackbar.make(
                findViewById(android.R.id.content),
                getString(R.string.err_msg_new_confirm_pwd_not_matched),
                Snackbar.LENGTH_SHORT
            ).show()
            etNewPassword.requestFocus()
            return
        }

        callChangePasswordApi()
    }

    private fun callChangePasswordApi() {

        Utility.showLoadingDialog(this, getString(R.string.msg_please_wait), false)

        val request = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("action", "change_password")
            .addFormDataPart("user_contact", email ?: "")
//            .addFormDataPart("customer_type", customerType ?: "")
            .addFormDataPart("user_password", etNewPassword.text.toString() ?: "")
            .addFormDataPart("contact_type", contactType ?: "")

        if (action?.contentEquals("forgot_password") == false) {
            request.addFormDataPart("contact_otp", contactOtp ?: "")
        } else {
            request.addFormDataPart("old_password", etOldPassword.text.toString() ?: "")
        }
        val requestBody = request.build()

        val signUpViewModel = ViewModelProvider(this).get(SignUpViewModel::class.java)
        signUpViewModel.action(requestBody).observe(this, Observer {
            Utility.hideLoadingDialog()
            if (it == null)
                return@Observer
            if (it.status == "200") {
                finish()
                Toast.makeText(this, getString(R.string.msg_success_change), Toast.LENGTH_LONG)
                    .show()
            } else {
                Snackbar.make(
                    findViewById(android.R.id.content),
                    it.message ?: getString(R.string.err_msg_oops_something_went_to_wrong),
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun callForgotPasswordApi() {
    }
}