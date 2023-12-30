package com.wecareapp.android.ui.activity

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.wecareapp.android.R
import com.wecareapp.android.utilities.Utility
import com.wecareapp.android.webservices.viewmodels.SignUpViewModel
import kotlinx.android.synthetic.main.activity_otp.*
import okhttp3.MultipartBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit


class OtpActivity : BaseActivity(), View.OnClickListener {
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var storedVerificationId: String
    private var email: String? = ""
    private var customerType: String? = ""
    private var contactType: String? = ""
    private var isFrom: String? = ""
    private val TAG = "OtpActivity";

    //firebase auth object
    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp)

        //initializing objects
        mAuth = FirebaseAuth.getInstance();

        if (intent.extras != null) {
            email = intent?.extras?.getString("email", "")
            customerType = intent?.extras?.getString("customer_type", "")
            contactType = intent?.extras?.getString("contact_type", "")
            isFrom = intent?.extras?.getString("action", "")
        }

        etEmail.setText(email)

        tvVerify.setOnClickListener(this)
        tvResendNow.setOnClickListener(this)

        sendSMSCode()
    }

    override fun onClick(v: View?) {
        if (v?.id == R.id.tvVerify) {
            verify()
        }
        if (v?.id == R.id.tvResendNow) {
            resendNow()
        }
    }

    private fun verify() {
        val otp = otpView.text.toString()

        if (otp.isNullOrEmpty() || otp.length < 4) {
            Snackbar.make(
                findViewById(android.R.id.content),
                getString(R.string.err_msg_please_enter_otp),
                Snackbar.LENGTH_SHORT
            ).show()
            return
        }

        if (email?.contains("@") == false) {
//            verifySMSCode(otp, email!!)
            verifyVerificationCode(otp)
            return
        }

        Utility.showLoadingDialog(this, getString(R.string.msg_please_wait_verifying), false)

        val jsonObject = JSONObject()
        jsonObject.put("action", "otp_validity")
        jsonObject.put("user_contact", email)
        jsonObject.put("contact_type", contactType)

        if (!TextUtils.isEmpty(customerType))
            jsonObject.put("customer_type", customerType)

        Utility.showLog("jsonObject", "" + jsonObject)

        val requestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("action", "otp_validity")
            .addFormDataPart("user_contact", email ?: "")
            .addFormDataPart("otp", otp)
            .addFormDataPart("contact_type", contactType ?: "")

        if (isFrom?.contentEquals("forgot_password") == true)
            requestBody.addFormDataPart("action", "forgot_otp_validity")
        else
            requestBody.addFormDataPart("action", "otp_validity")

        if (!TextUtils.isEmpty(customerType)) {
            requestBody.addFormDataPart("customer_type", customerType ?: "")
        }

        val rb = requestBody.build()

        val signUpViewModel = ViewModelProvider(this).get(SignUpViewModel::class.java)
        signUpViewModel.action(rb).observe(this, Observer {
            Utility.hideLoadingDialog()
            if (it == null)
                return@Observer
            if (it.status == "200") {
                finish()
                if (isFrom?.contentEquals("forgot_password") == true) {
                    startActivity(Intent(this, ChangePasswordActivity::class.java).apply {
                        putExtra("email", email)
                        putExtra("action", isFrom)
                        putExtra("contact_type", contactType)
                    })
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                } else
                    Toast.makeText(this, getString(R.string.msg_success_reg), Toast.LENGTH_LONG)
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

    private fun verifySMSCode(otp: String, phone: String) {
        val phoneNumber = if (email?.contains("+") == true) email else "+91$email"
        val smsCode = otp

        val firebaseAuth = Firebase.auth
        val firebaseAuthSettings = firebaseAuth.firebaseAuthSettings

        firebaseAuthSettings.setAutoRetrievedSmsCodeForPhoneNumber(phoneNumber, smsCode)

        Utility.showLoadingDialog(this, getString(R.string.msg_please_wait_verifying), true)

        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(phoneNumber!!)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    Utility.hideLoadingDialog()
                    finish()
                    Toast.makeText(
                        applicationContext,
                        getString(R.string.msg_success_reg),
                        Toast.LENGTH_LONG
                    ).show()
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    Utility.hideLoadingDialog()
                    Toast.makeText(
                        applicationContext,
                        getString(R.string.try_again),
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun resendNow() {
        otpView.setText("")
        resend()
    }

    private fun resend() {
        if (email?.contains("@") == false) {
            sendSMSCode()
            return
        }

        Utility.showLoadingDialog(this, getString(R.string.msg_please_wait_resending_otp), false)

        val requestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("action", "resend_otp")
            .addFormDataPart("user_contact", email ?: "")
            .addFormDataPart("customer_type", customerType ?: "")
            .addFormDataPart("contact_type", contactType ?: "").build()

        val signUpViewModel = ViewModelProvider(this).get(SignUpViewModel::class.java)
        signUpViewModel.action(requestBody).observe(this, Observer {
            Utility.hideLoadingDialog()
            if (it == null)
                return@Observer
            if (it.status == "200") {
                Toast.makeText(
                    this,
                    getString(R.string.msg_sent_to_your_contact),
                    Toast.LENGTH_LONG
                ).show()
            } else {
                Snackbar.make(
                    findViewById(android.R.id.content),
                    it.message ?: getString(R.string.err_msg_oops_something_went_to_wrong),
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun sendSMSCode() {
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                Log.d(TAG, "onVerificationCompleted:$credential")

                val code: String? = credential.smsCode
//                verifySMSCode(code!!, email!!)
                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w(TAG, "onVerificationFailed", e)

                if (e is FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                } else if (e is FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                }

                // Show a message and update the UI
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d(TAG, "onCodeSent:$verificationId")

                Toast.makeText(
                    applicationContext,
                    getString(R.string.msg_sent_to_your_contact),
                    Toast.LENGTH_LONG
                ).show()

                // Save verification ID and resending token so we can use them later
                storedVerificationId = verificationId
                resendToken = token
            }
        }

        if (email?.contains("@") == false) {
            val phoneNumber = if (email?.contains("+") == true) email else "+91$email"
            val options = PhoneAuthOptions.newBuilder(Firebase.auth)
                .setPhoneNumber(phoneNumber!!)       // Phone number to verify
                .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                .setActivity(this)                 // Activity (for callback binding)
                .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
                .build()
            PhoneAuthProvider.verifyPhoneNumber(options)
        }
    }

    private fun verifyVerificationCode(otp: String) {
        try {
            //creating the credential
            val credential = PhoneAuthProvider.getCredential(storedVerificationId, otp)

            //signing the user
            signInWithPhoneAuthCredential(credential)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        //initializing objects
        val mAuth = FirebaseAuth.getInstance();
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(
                this@OtpActivity
            ) { task ->
                if (task.isSuccessful) {
                    //verification successful we will start the profile activity
//                    val intent =
//                        Intent(this@OtpActivity, ProfileActivity::class.java)
//                    intent.flags =
//                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//                    startActivity(intent)
                    finish()
                    if (isFrom?.contentEquals("forgot_password") == true) {
                        startActivity(Intent(this, ChangePasswordActivity::class.java).apply {
                            putExtra("email", email)
                            putExtra("action", isFrom)
                            putExtra("contact_type", contactType)
                        })
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    } else
                        Toast.makeText(this, getString(R.string.msg_success_reg), Toast.LENGTH_LONG)
                            .show()
                } else {
                    Toast.makeText(
                        applicationContext,
                        getString(R.string.try_again),
                        Toast.LENGTH_LONG
                    ).show()
/*
                    //verification unsuccessful.. display an error message
                    var message = "Somthing is wrong, we will fix it soon..."
                    if (task.getException() is FirebaseAuthInvalidCredentialsException) {
                        message = "Invalid code entered..."
                    }
                    val snackbar = Snackbar.make(
                        findViewById(R.id.parent),
                        message, Snackbar.LENGTH_LONG
                    )
                    snackbar.setAction("Dismiss") { }
                    snackbar.show()*/
                }
            }
    }
}