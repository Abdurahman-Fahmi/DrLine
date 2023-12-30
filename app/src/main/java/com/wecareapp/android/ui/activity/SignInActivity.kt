package com.wecareapp.android.ui.activity

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.github.javiersantos.bottomdialogs.BottomDialog
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInResult
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GooglePlayServicesUtil
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.parse.ParseUser
import com.parse.twitter.ParseTwitterUtils
import com.wecareapp.android.Constants
import com.wecareapp.android.R
import com.wecareapp.android.managers.LocaleManager
import com.wecareapp.android.ui.activity.clinic.ClinicMainActivityI
import com.wecareapp.android.ui.activity.consultant.ConsultantMainActivityI
import com.wecareapp.android.ui.activity.patient.PatientMainActivity
import com.wecareapp.android.utilities.CommonCode
import com.wecareapp.android.utilities.Utility
import com.wecareapp.android.webservices.viewmodels.SignInViewModel
import kotlinx.android.synthetic.main.activity_sign_in.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class SignInActivity : BaseActivity(), View.OnClickListener,
    GoogleApiClient.OnConnectionFailedListener {


    var mfbemail = ""
    var pictureUrl = ""
    var mfbname = ""
    var activityName = ""
    var fb_id = ""
    var personPhotoUrl = ""
    var googleId = ""
    var twitterId = ""

    var loginButton: LoginButton? = null
    var callbackManager: CallbackManager? = null
    private var mGoogleApiClient: GoogleApiClient? = null
    var permsRequestCode = 200
    private val RC_SIGN_IN = 0

    var d: Dialog? = null

    /**
     * A flag indicating that a PendingIntent is in progress and prevents us
     * from starting further intents.
     */
    private var mIntentInProgress = false
    private val mSignInClicked = false
    private var mConnectionResult: ConnectionResult? = null

    var commonCode: CommonCode? = null

    var perms = arrayOf(
        "android.permission.GET_ACCOUNTS",
        "android.permission.ACCESS_FINE_LOCATION",
        "android.permission.ACCESS_COARSE_LOCATION"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FacebookSdk.sdkInitialize(applicationContext)
        setContentView(R.layout.activity_sign_in)
//        AppEventsLogger.activateApp(applicationContext)

        loginButton = findViewById(R.id.login_button)

        commonCode = CommonCode(this)

        callbackManager = CallbackManager.Factory.create()
        loginButton?.setReadPermissions(Arrays.asList("email", "public_profile"))

        // Initializing google plus api client
        if (AccessToken.getCurrentAccessToken() != null) {
            requestData()
        }

        loginButton?.registerCallback(callbackManager, object : FacebookCallback<LoginResult?> {
            override fun onSuccess(loginResult: LoginResult?) {
                if (AccessToken.getCurrentAccessToken() != null) {
                    requestData()
                }
            }

            override fun onCancel() {
                // App code
            }

            override fun onError(exception: FacebookException) {
                // App code
            }
        })

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        mGoogleApiClient = GoogleApiClient.Builder(this)
            .enableAutoManage(this, this)
            .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
            .build()

        // Initializing google plus api client
        if (AccessToken.getCurrentAccessToken() != null) {
            requestData()
        }

        tvSignIn.setOnClickListener(this)
        tvSkip.setOnClickListener(this)
        tvSignUp.setOnClickListener(this)
        tvForgotPassword.setOnClickListener(this)
        ivGoogle.setOnClickListener(this)
        ivFacebook.setOnClickListener(this)
        ivTwitter.setOnClickListener(this)
//        tvLanguage.setOnClickListener(this)

        if (TextUtils.isEmpty(lang) || lang.contentEquals("en")) {
            tvLanguage.text = getString(R.string.ar)
            tvLanguage.tag = true
        } else {
            tvLanguage.text = getString(R.string.en)
            tvLanguage.tag = false
        }

        tvLanguage.setOnClickListener {
            var locale = Locale("en")
            if (it.tag == true) {
                tvLanguage.text = getString(R.string.en)
                locale = Locale("ar")
                Utility.setSharedPrefStringData(this, Constants.LANGUAGE, "ar")
            } else {
                tvLanguage.text = getString(R.string.ar)
                locale = Locale("en")
                Utility.setSharedPrefStringData(this, Constants.LANGUAGE, "en")
            }
            LocaleManager.setNewLocale(this, locale)
            val intent = Intent(this, SplashActivity::class.java)
            startActivity(intent)
            finish()
        }

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tvSignIn -> {
                hideKeyboard()
                val email = etEmail.text.toString()
                if (!isValidInput())
                    return
                /*if (email.contentEquals("patient@gmail.com"))
                    openPatientHome()
                if (email.contentEquals("consultant@gmail.com"))
                    openConsultantHome()
                if (email.contentEquals("clinic@gmail.com"))
                    openClinicHome()*/

                login()
            }
            R.id.tvSkip -> {
                Utility.setSharedPrefStringData(this, Constants.IS_LOGIN, false)
                openPatientHome()
            }
            R.id.tvSignUp -> {
                startActivity(Intent(this, SignUpActivity::class.java))
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
            R.id.tvForgotPassword -> {
                startActivity(Intent(this, ForgotPasswordActivity::class.java))
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
            R.id.ivFacebook ->
                openSignUpBottomDialog(1)
            R.id.ivGoogle ->
                openSignUpBottomDialog(2)
            R.id.ivTwitter ->
                openSignUpBottomDialog(3)

        }
    }

    fun requestData() {
        val request = GraphRequest.newMeRequest(
            AccessToken.getCurrentAccessToken()
        ) { _, response ->
            val json = response.jsonObject
            Log.d("responce ", json.toString())
            try {
                if (json != null) {
                    //                        String text = "<b>Name :</b> " + json.getString("name") + "<br><br><b>Email :</b> " + json.getString("email");
                    //                        Log.d("details : ", text);
                    fb_id = json.getString("id")
                    mfbname = json.getString("name")
                    mfbemail = ""
                    val userID = json.getString("id")
                    personPhotoUrl = "https://graph.facebook.com/$userID/picture?type=large"
                    if (json.has("email")) {
                        mfbemail = json.getString("email")
                        socialLogin("facebook")
                    } else {
                        OpenEnterEmailDialog("facebook")
                    }
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
        val parameters = Bundle()
        parameters.putString("fields", "id,name,link,email,picture")
        request.parameters = parameters
        request.executeAsync()
        LoginManager.getInstance().logOut()
    }

    fun OpenEnterEmailDialog(loginType: String?) {
        /*d = Dialog(this)
        d?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        d?.getWindow()?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        d?.setContentView(R.layout.dialog_enter_email)
        d?.setCancelable(false)
        val txtEnterEmail: TextView = d?.findViewById<TextView>(R.id.txtEnterEmail)
        val edtFBEmail: EditText = d?.findViewById<EditText>(R.id.edtFBEmail)
        val btnCancel: Button = d?.findViewById<Button>(R.id.btnCancel)
        val btnLogin: Button = d?.findViewById<Button>(R.id.btnLogin)
        btnLogin.setOnClickListener {
            if (edtFBEmail.text.toString() == "") {
                Toast.makeText(applicationContext, "Please enter email", Toast.LENGTH_SHORT).show()
            } else if (!commonCode.checkEmail(edtFBEmail.text.toString())) {
                Toast.makeText(applicationContext, "Invalid email id", Toast.LENGTH_SHORT).show()
            } else {
                mfbemail = edtFBEmail.text.toString()
                d?.dismiss()
                socialLogin(loginType)
            }
        }
        btnCancel.setOnClickListener { v: View? -> d.dismiss() }
        d?.show()*/
    }

    private fun socialLogin(type: String?) {
        Utility.showLoadingDialog(this, getString(R.string.msg_please_wait_logging), false)
        val email = etEmail.text.toString()
        val pwd = etPassword.text.toString()
        val contactType = if (email.contains("@")) "2" else "1"

        val jsonObject = JSONObject()
        jsonObject.put("action", "login")
        jsonObject.put("user_contact", mfbemail)
        jsonObject.put("user_password", pwd)
        jsonObject.put("contact_type", contactType)
        jsonObject.put("user_token_id", firebaseToken)
        Utility.showLog("jsonObject", "" + jsonObject)

        val body: RequestBody =
            jsonObject.toString().toRequestBody("text/plain".toMediaTypeOrNull())

        val request = MultipartBody.Builder()

        request.setType(MultipartBody.FORM)
            .addFormDataPart("action", "socialLogin")
            .addFormDataPart("user_contact", mfbemail)
            .addFormDataPart("user_password", pwd)
            .addFormDataPart("contact_type", contactType)
            .addFormDataPart("user_token_id", firebaseToken)
            .addFormDataPart("user_device_id", firebaseToken)
            .addFormDataPart("device_token", firebaseToken)
            .addFormDataPart("firebase_token", firebaseToken)
            .addFormDataPart("platform", "android")
            .addFormDataPart("email", mfbemail)
            .addFormDataPart("name", mfbname)
            .addFormDataPart("login_type", type ?: "")

        val requestBody = request.build()

        val signInViewModel = ViewModelProvider(this).get(SignInViewModel::class.java)

        signInViewModel.userLogin(requestBody).observe(this, Observer {
            Utility.hideLoadingDialog()
            if (it == null)
                return@Observer
            if (it.status == "200") {
                if (it.result == null) {
                    return@Observer
                }
                val profile = it.result
                Utility.setSharedPrefStringData(this, Constants.IS_LOGIN, true)
                Utility.setSharedPrefIntegerData(this, Constants.LOGIN_TYPE, profile.customerType)
                Utility.setSharedPrefStringData(
                    this, Constants.USER_DATA, Gson().toJson(profile)
                )
                finish()
                var intent = Intent(this, PatientMainActivity::class.java)

                when (profile.customerType) {
                    1 -> {
                        intent = Intent(this, PatientMainActivity::class.java)
                    }
                    2 -> {
                        intent = Intent(this, ConsultantMainActivityI::class.java)
                    }
                    3 -> {
                        intent = Intent(this, ClinicMainActivityI::class.java)
                    }
                }

                startActivity(intent.apply {
                    putExtra("profile", profile)
                    putExtra("email", email)
                    putExtra("customer_type", profile.customerType)
                    putExtra("contact_type", profile.contactId)
                })
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            } else {
                Snackbar.make(
                    findViewById(android.R.id.content),
                    it.message ?: getString(R.string.err_msg_oops_something_went_to_wrong),
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun openSignUpBottomDialog(i: Int) {
        val view: View = layoutInflater.inflate(R.layout.layout_privacy_bottom, null)
        val dialog =
            BottomDialog.Builder(this) //                .setTitle(getString(R.string.add_review))
                .setCustomView(view)
                .build()
        dialog.show()
        val btnContinue = view.findViewById<View>(R.id.btnContinue)
        val btnCancel = view.findViewById<View>(R.id.btnCancel)
        val tvTcPp = view.findViewById<TextView>(R.id.tvTcPp)
        privacyOpen(tvTcPp)
        btnContinue.setOnClickListener {
            dialog.dismiss()
            when (i) {
                1 -> {
                    loginSignUpFacebook()
                }
                2 -> {
                    loginSignUpGoogle()
                }
                3 -> {
                    twitterLogin()
                }
            }
        }
        btnCancel.setOnClickListener { dialog.dismiss() }
    }

    private fun loginSignUpGoogle() {
        /**
         * Clearing default account every time so that the account picker dialog can be enforced
         */
        if (mGoogleApiClient != null && mGoogleApiClient!!.isConnected) {
            mGoogleApiClient!!.clearDefaultAccountAndReconnect()
        }
        if (commonCode?.checkInternet()!!) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestPermissions(perms, permsRequestCode)
                } else {
                    signInWithGplus()
                }
            } else {
                signInWithGplus()
            }
        } else {
            commonCode?.showNoInternetConnection()
        }
    }

    private fun loginSignUpFacebook() {
        if (commonCode?.checkInternet() == true) {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                if (ActivityCompat.checkSelfPermission(
                        this@SignInActivity,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(
                        this@SignInActivity,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestPermissions(perms, permsRequestCode)
                } else {
                    loginButton!!.performClick()
                }
            } else {
                loginButton!!.performClick()
            }
        } else {
            commonCode?.showNoInternetConnection()
        }
    }

    /*fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>?,
        grantResults: IntArray
    ) {
        if (requestCode == 200) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Now user should be able to use camera
                //signInWithGplus();
            } else {
                // Your app will not have this permission. Turn off all functions
                // that require this permission or it will force close like your
                // original question
            }
        }
    }*/

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val result: GoogleSignInResult? = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            if (result != null) {
                handleSignInResult(result)
            }
        } //for facebook
        else {
            callbackManager!!.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun handleSignInResult(result: GoogleSignInResult) {
        Log.d("social Login", "handleSignInResult:" + result.isSuccess)
        if (result.isSuccess) {
            // Signed in successfully, show authenticated UI.
            val acct: GoogleSignInAccount? = result.signInAccount

//            Log.e("ID", "ID:" + acct.getId());
//            Log.e("ID", "ID:" + acct);
            val personName = acct?.displayName
            //            String personPhoto = acct.getPhotoUrl().toString();
            val email = acct?.email
            googleId = acct?.id!!
            mfbname = personName!!
            mfbemail = email!!
            personPhotoUrl = ""
            socialLogin("google")


//            new InsertUserDetails().execute();
        } else {
            //   Toast.makeText(getApplicationContext(),
            //  "Person information is null", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Sign-in into google
     */
    private fun signInWithGplus() {
        val signInIntent: Intent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient)
        startActivityForResult(
            signInIntent,
            RC_SIGN_IN
        )
    }

    /**
     * Method to resolve any signin errors
     */
    private fun resolveSignInError() {
        if (mConnectionResult?.hasResolution()!!) {
            try {
                mIntentInProgress = true
                mConnectionResult?.startResolutionForResult(
                    this,
                    RC_SIGN_IN
                )
            } catch (e: SendIntentException) {
                mIntentInProgress = false
                mGoogleApiClient?.connect()
            }
        }
    }

    override fun onConnectionFailed(result: ConnectionResult) {
        if (!result.hasResolution()) {
            GooglePlayServicesUtil.getErrorDialog(
                result.errorCode, this,
                0
            ).show()
            return
        }
        if (!mIntentInProgress) {
            // Store the ConnectionResult for later usage
            mConnectionResult = result
            if (mSignInClicked) {
                // The user has already clicked 'sign-in' so we attempt to
                // resolve all
                // errors until the user is signed in, or they cancel.
                resolveSignInError()
            }
        }
    }

//    @Override
//    public void onResult(People.LoadPeopleResult result) {
//        Log.d(TAG, "result.getStatus():" + result.getStatus());
//    }

    //    @Override
    //    public void onResult(People.LoadPeopleResult result) {
    //        Log.d(TAG, "result.getStatus():" + result.getStatus());
    //    }
    fun twitterLogin() {
        ParseTwitterUtils.logIn(this) { user, err ->
            if (err != null) {
                //                    dlg.dismiss();
                ParseUser.logOut()
                Log.e("err", "err", err)
            }
            if (user == null) {
                //                    dlg.dismiss();
                ParseUser.logOut()
                Toast.makeText(
                    this@SignInActivity,
                    "The user cancelled the Twitter login.",
                    Toast.LENGTH_LONG
                ).show()
                Log.d("MyApp", "Uh oh. The user cancelled the Twitter login.")
            } else if (user.isNew) {
                //                    dlg.dismiss();

                //                    Toast.makeText(SocialLoginActivity.this, "User signed up and logged in through Twitter.", Toast.LENGTH_LONG).show();
                Log.d("MyApp", "User signed up and logged in through Twitter!")
                mfbname = user.username
                twitterId = ParseTwitterUtils.getTwitter().userId
                if (user.email != null) {
                    mfbemail = user.email
                    socialLogin("twitter")
                } else {
                    OpenEnterEmailDialog("twitter")
                }

                //                    socialLogin("twitter");

                //                    user.setUsername(ParseTwitterUtils.getTwitter().getScreenName());
                //                    user.saveInBackground(new SaveCallback() {
                //                        @Override
                //                        public void done(ParseException e) {
                //                            if (null == e) {
                ////                                alertDisplayer("First tome login!", "Welcome!");
                //                                Log.e("TWITTER","Twitter login successfully");
                //                            } else {
                //                                ParseUser.logOut();
                //                                Toast.makeText(SocialLoginActivity.this, "It was not possible to save your username.", Toast.LENGTH_LONG).show();
                //                            }
                //                        }
                //                    });
            } else {
                //                    dlg.dismiss();

                //                    Log.e("Name",user.getUsername());
                //                    Log.e("email",user.getUsername());
                //                    Log.e("id",ParseTwitterUtils.getTwitter().getUserId());

                //                    Toast.makeText(SocialLoginActivity.this, "User logged in through Twitter.", Toast.LENGTH_LONG).show();
                Log.d("MyApp", "User logged in through Twitter!")
                //                    alertDisplayer("Oh, you!","Welcome back!");
                mfbname = user.username
                twitterId = ParseTwitterUtils.getTwitter().userId
                if (user.email != null) {
                    mfbemail = user.email
                    socialLogin("twitter")
                } else {
                    OpenEnterEmailDialog("twitter")
                }
            }
        }
    }

    private fun login() {
        if (isValidInput()) {
            Utility.showLoadingDialog(this, getString(R.string.msg_please_wait_logging), false)
            val email = etEmail.text.toString()
            val pwd = etPassword.text.toString()
            val contactType = if (email.contains("@")) "2" else "1"

            val jsonObject = JSONObject()
            jsonObject.put("action", "login")
            jsonObject.put("user_contact", email)
            jsonObject.put("user_password", pwd)
            jsonObject.put("contact_type", contactType)
            jsonObject.put("user_token_id", firebaseToken)
            Utility.showLog("jsonObject", "" + jsonObject)

            val body: RequestBody =
                jsonObject.toString().toRequestBody("text/plain".toMediaTypeOrNull())

            val request = MultipartBody.Builder()

            request.setType(MultipartBody.FORM)
                .addFormDataPart("action", "login")
                .addFormDataPart("user_contact", email)
                .addFormDataPart("user_password", pwd)
                .addFormDataPart("contact_type", contactType)
                .addFormDataPart("user_token_id", firebaseToken)
                .addFormDataPart("user_device_id", firebaseToken)
                .addFormDataPart("platform", "android")

            val requestBody = request.build()

            val signInViewModel = ViewModelProvider(this).get(SignInViewModel::class.java)

            signInViewModel.userLogin(requestBody).observe(this, Observer {
                Utility.hideLoadingDialog()
                if (it == null)
                    return@Observer
                if (it.status == "200") {
                    if (it.result == null) {
                        return@Observer
                    }
                    val profile = it.result
                    Utility.setSharedPrefStringData(this, Constants.IS_LOGIN, true)
                    Utility.setSharedPrefIntegerData(
                        this,
                        Constants.LOGIN_TYPE,
                        profile.customerType
                    )
                    Utility.setSharedPrefStringData(
                        this, Constants.USER_DATA, Gson().toJson(profile)
                    )
                    finish()
                    var intent = Intent(this, PatientMainActivity::class.java)

                    when (profile.customerType) {
                        1 -> {
                            intent = Intent(this, PatientMainActivity::class.java)
                        }
                        2 -> {
                            intent = Intent(this, ConsultantMainActivityI::class.java)
                        }
                        3 -> {
                            intent = Intent(this, ClinicMainActivityI::class.java)
                        }
                    }

                    startActivity(intent.apply {
                        putExtra("profile", profile)
                        putExtra("email", email)
                        putExtra("customer_type", profile.customerType)
                        putExtra("contact_type", profile.contactId)
                    })
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
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

    private fun isValidInput(): Boolean {
        val email = etEmail.text.toString()
        val pwd = etPassword.text.toString()

        var validated = false

        when {
            TextUtils.isEmpty(email) -> {
                Snackbar.make(
                    findViewById(android.R.id.content),
                    getString(R.string.err_msg_please_enter_email),
                    Snackbar.LENGTH_SHORT
                ).show()
                etEmail.requestFocus()
            }
            TextUtils.isEmpty(pwd) -> {
                Snackbar.make(
                    findViewById(android.R.id.content),
                    getString(R.string.err_msg_please_enter_password),
                    Snackbar.LENGTH_SHORT
                ).show()
                etPassword.requestFocus()
            }
            else -> {
                validated = true
            }
        }
        return validated
    }

    private fun openPatientHome() {
        finish()
        startActivity(Intent(this, PatientMainActivity::class.java))
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    private fun openClinicHome() {
        finish()
        startActivity(Intent(this, ClinicMainActivityI::class.java))
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    private fun openConsultantHome() {
        finish()
        startActivity(Intent(this, ConsultantMainActivityI::class.java))
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }
}