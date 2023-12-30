package com.wecareapp.android.ui.activity

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.util.Patterns
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import com.wecareapp.android.Constants
import com.wecareapp.android.R
import com.wecareapp.android.locations.GPSTracker
import com.wecareapp.android.managers.LocaleManager
import com.wecareapp.android.model.Profile
import com.wecareapp.android.ui.fragment.BaseFragment
import com.wecareapp.android.utilities.PopUtils
import com.wecareapp.android.utilities.Utility
import com.wecareapp.android.webservices.viewmodels.PatientViewModel
import okhttp3.MultipartBody
import java.util.*


open class BaseActivity : AppCompatActivity() {

    var userCustomerType: Int? = 1
    var user: Profile? = null
    protected var isLogged = false
    protected val REQUEST_CAMERA = 0
    protected val REQUEST_GALLERY = 1
    protected val REQUEST_VIDEO = 2
    protected val REQUEST_AUDIO = 3
    protected val REQUEST_DOCUMENT = 4
    protected var gps: GPSTracker? = null
    protected var firebaseToken: String = ""
    protected var lang: String = "en"
    protected var userId: String? = ""
    protected var userTypeStr: String = "patient"

    val PERMISSION_REQ_ID_RECORD_AUDIO = 22
    val PERMISSION_REQ_ID_CAMERA = PERMISSION_REQ_ID_RECORD_AUDIO + 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        firebaseToken = preferences.getString(Constants.FIREBASE_TOKEN, "") ?: ""

        Log.d("Firebase", "Firebase Token: $firebaseToken")

        lang = Utility.getSharedPreference(this, Constants.LANGUAGE, "ar") ?: ""
        setAppLocale(lang)

        isLogged = Utility.getSharedPreferenceBoolean(this, Constants.IS_LOGIN, false)

        user = Gson().fromJson(
            Utility.getSharedPreference(this, Constants.USER_DATA),
            Profile::class.java
        )

        if (user != null) {
            userCustomerType = user?.customerType
            userId = user?.contactId

            userTypeStr = when (user?.customerType) {
                2 -> "consultant"
                3 -> "clinic"
                else -> "patient"
            }
        }
    }

    protected fun openFragment(fragment: Fragment) {
        try {
            val tag = fragment.javaClass.name
//        hideAllFragments()
            var frgm = supportFragmentManager.findFragmentByTag(tag)
            val transaction = supportFragmentManager.beginTransaction()
            if (frgm == null) {
                frgm = fragment
                transaction.replace(R.id.container, frgm, tag)
                if (!fragment.isAdded)
                    transaction.addToBackStack(tag)
            } else {
                transaction.replace(R.id.container, frgm)
                transaction.show(frgm)
            }
            transaction.commit()
            supportFragmentManager.executePendingTransactions()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun hideAllFragments() {
        if (supportFragmentManager.backStackEntryCount <= 0)
            return
        val transaction = supportFragmentManager.beginTransaction()
        for (i in 0 until supportFragmentManager.backStackEntryCount) {
            val tag = supportFragmentManager.getBackStackEntryAt(i).name
            val f = supportFragmentManager.findFragmentByTag(tag)
            f?.let { transaction.hide(it) }
        }
        transaction.commit()
        supportFragmentManager.executePendingTransactions()
    }

    fun hideFragment(position: Int) {
        if (supportFragmentManager.backStackEntryCount <= 0)
            return
        val transaction = supportFragmentManager.beginTransaction()
        val tag = supportFragmentManager.getBackStackEntryAt(position).name
        val f = supportFragmentManager.findFragmentByTag(tag)
        f?.let { transaction.hide(it) }
        transaction.commit()
        supportFragmentManager.executePendingTransactions()
    }

    fun showFragment(position: Int) {
        if (supportFragmentManager.backStackEntryCount <= 0)
            return
        val transaction = supportFragmentManager.beginTransaction()
        val tag = supportFragmentManager.getBackStackEntryAt(position).name
        val f = supportFragmentManager.findFragmentByTag(tag)
        f?.let { transaction.show(it) }
        transaction.commit()
        supportFragmentManager.executePendingTransactions()
    }

    open fun getActiveFragment(): BaseFragment? {
        try {
            if (supportFragmentManager.backStackEntryCount == 0) {
                return null
            }
            val tag =
                supportFragmentManager.getBackStackEntryAt(supportFragmentManager.backStackEntryCount - 1).name
            return supportFragmentManager.findFragmentByTag(tag) as BaseFragment?
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    open fun clearAllFragment() {
        try {
            val fragments = supportFragmentManager.fragments
            for (fragment in fragments) {
                if (fragment != null)
                    supportFragmentManager.popBackStackImmediate()
//                    supportFragmentManager.beginTransaction().remove(fragment).commitNowAllowingStateLoss()
            }
            supportFragmentManager.executePendingTransactions()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    open fun setAppLocale(lang: String?) {
        if (TextUtils.isEmpty(lang) || lang == "en") {
            LocaleManager.setNewLocale(this, Locale("en"))
        } else {
            LocaleManager.setNewLocale(this, Locale("ar"))
        }
    }

    open fun hideKeyboard() {
        // Check if no view has focus:
        val view = this.currentFocus
        view?.let { v ->
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(v.windowToken, 0)
        }
    }

    override fun onResume() {
        super.onResume()
        updateOnlineStatus()
        startUserOnlineStatusUpdate()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    protected open fun checkingAccess(): Boolean {
        return if (isNetworkAvailable(this)) {
            true
        } else {
            PopUtils.alertDialog(this, getString(R.string.err_msg_plz_check_internet)) {
            }
            false
        }
    }

    open fun isNetworkAvailable(context: Context?): Boolean {
        if (context != null) {
            val connectivityManager =
                context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            return activeNetworkInfo != null && activeNetworkInfo.isConnected
        }
        return false
    }

    internal fun openLoginActivity() {
        val intent = Intent(this, SignInActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    protected fun privacyOpen(tvTcPp: TextView) {
        if (TextUtils.isEmpty(lang) || lang.contentEquals("en")) {
            customTextView(tvTcPp)
        } else tvTcPp.setOnClickListener {
            openWebViewLink(
                getString(R.string.privacy_policy_link)
            )
        }
    }

    protected fun customTextView(view: TextView) {
        val spanTxt = SpannableStringBuilder(
            "By continuing, you're agreed to our \n"
        )
        spanTxt.append(" Terms & Conditions")
        spanTxt.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                openWebViewLink(getString(R.string.tc_link))
            }
        }, spanTxt.length - "Terms & Conditions".length, spanTxt.length, 0)
        spanTxt.append(" and")
        //        spanTxt.setSpan(new ForegroundColorSpan(Color.WHITE), 32, spanTxt.length(), 0);
        spanTxt.append(" Privacy Policy")
        spanTxt.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                openWebViewLink(getString(R.string.privacy_policy_link))
            }
        }, spanTxt.length - "Privacy Policy".length, spanTxt.length, 0)
        //        spanTxt.setSpan(new ForegroundColorSpan(Color.WHITE), 32, spanTxt.length(), 0);
        view.movementMethod = LinkMovementMethod.getInstance()
        view.setText(spanTxt, TextView.BufferType.SPANNABLE)
    }

    protected fun openWebViewLink(link: String?) {
        val intent = Intent(this, WebViewActivity::class.java)
        intent.putExtra("link", link)
        startActivity(intent)
    }

    protected fun logout() {
        Utility.setSharedPrefStringData(this, Constants.ACTOR_ID, "")
        Utility.setSharedPrefStringData(this, Constants.ACTOR_TYPE, "")
        Utility.setSharedPrefStringData(this, Constants.CUSTOMER_ID, "")
        Utility.setSharedPrefStringData(this, Constants.CUSTOMER_TYPE, "")
        Utility.setSharedPrefStringData(this, Constants.LOGIN_TYPE, "")
        Utility.clearSharedPreference(this)

        openLoginActivity()
    }


    private val LOG_TAG = BaseActivity::class.java.simpleName
    fun checkSelfPermission(permission: String, requestCode: Int): Boolean {
        Log.i(LOG_TAG, "checkSelfPermission $permission $requestCode")
        if (ContextCompat.checkSelfPermission(
                this,
                permission
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(permission),
                requestCode
            )
            return false
        }
        return true
    }

    companion object {
        private var handler: Handler? = Handler()
    }

    private val runnable: Runnable = Runnable {
        updateOnlineStatus()
        startUserOnlineStatusUpdate()
    }


    private fun startUserOnlineStatusUpdate() {
        if (handler == null)
            handler = Handler()
        if (handler != null &&
            Utility.getSharedPreferenceInteger(this, Constants.LOGIN_TYPE, -1) != -1
        ) {
            handler?.postDelayed(runnable, 30 * 1000)
        } else stopUserOnlineStatusUpdate()
    }

    private fun stopUserOnlineStatusUpdate() {
        if (handler != null)
            handler?.removeCallbacksAndMessages(null)
    }

    override fun onStop() {
        super.onStop()
        stopUserOnlineStatusUpdate()
    }

    private fun updateOnlineStatus() {
        if (checkingAccess()) {
            val viewModel =
                ViewModelProvider(this).get(PatientViewModel::class.java)

            val request = MultipartBody.Builder()

            request.setType(MultipartBody.FORM)
                .addFormDataPart("action", "available_status")
                .addFormDataPart("customer_type", userCustomerType.toString() ?: "")
                .addFormDataPart("user_id", userId ?: "")
                .addFormDataPart("firebase_token", firebaseToken)
                .addFormDataPart("platform", "android")

            val requestBody = request.build()

            viewModel.commonRequest(requestBody)
        }
    }


    public fun isValidMail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    public fun isValidMobile(phone: String): Boolean {
        return Patterns.PHONE.matcher(phone).matches() && (phone.length in 7..14)
    }

}