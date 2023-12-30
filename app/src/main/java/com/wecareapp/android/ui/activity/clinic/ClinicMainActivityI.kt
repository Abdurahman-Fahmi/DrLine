package com.wecareapp.android.ui.activity.clinic

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.wecareapp.android.Constants
import com.wecareapp.android.R
import com.wecareapp.android.model.Doctor
import com.wecareapp.android.model.Profile
import com.wecareapp.android.ui.activity.BaseActivity
import com.wecareapp.android.ui.fragment.ICallBackToActivity
import com.wecareapp.android.ui.fragment.PatientContactsFragment
import com.wecareapp.android.ui.fragment.PatientNotificationsFragment
import com.wecareapp.android.ui.fragment.clinic.ClinicProfileFragment
import com.wecareapp.android.ui.fragment.consultant.ConsultantMyAppointmentFragment
import com.wecareapp.android.ui.fragment.patient.PatientHomeClinicsFragment
import com.wecareapp.android.utilities.PopUtils
import com.wecareapp.android.utilities.Utility
import com.wecareapp.android.webservices.viewmodels.PatientViewModel
import kotlinx.android.synthetic.main.activity_patient_main.*
import kotlinx.android.synthetic.main.fragment_patient_profile.*
import okhttp3.MultipartBody
import q.rorbin.badgeview.Badge
import q.rorbin.badgeview.QBadgeView

class ClinicMainActivityI : BaseActivity(), ICallBackToActivity,
    BottomNavigationView.OnNavigationItemSelectedListener {

    private var profile: Profile? = null
    private var email: String? = ""
    private var customerType: String? = ""
    private var contactType: String? = ""
    private var action: String? = ""

    private var isFrom: String = ""
    private var messageType: String = ""
    private var chatCount: String = ""
    private var notiCount: String = ""
    private var qChatBadge: Badge? = null
    private var qNotiBadge: Badge? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_clinic_main)


        if (intent.extras != null) {
            email = intent?.extras?.getString("email", "")
            profile =
                if (intent?.extras?.getSerializable("profile") == null) null else intent?.extras?.getSerializable(
                    "profile"
                ) as Profile
            customerType = intent?.extras?.getString("customer_type", "")
            contactType = intent?.extras?.getString("contact_type", "")
            action = intent?.extras?.getString("action", "")
            isFrom = intent?.extras?.getString("isFrom").toString()
            messageType = intent?.extras?.getString("messageType").toString()
        }

        bottomNavigationView.selectedItemId = R.id.nav_profile

        if (savedInstanceState == null) {
            openFragment(ClinicProfileFragment.newInstance(this))
        }

        bottomNavigationView.setOnNavigationItemSelectedListener(this)
    }

    private fun openClinicBalance() {
        startActivity(Intent(this, ClinicBalanceActivity::class.java))
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    override fun onBackPressed() {
        PopUtils.exitDialog(this, getString(R.string.msg_do_you_want_exit)) {
            finishAffinity()
        }
    }

    override fun onBackClicked(home: Boolean) {
        if (home) {
            bottomNavigationView.selectedItemId = R.id.nav_profile
        } else
            onBackPressed()
    }

    override fun hideKeyboard() {
        // Check if no view has focus:
        val view = this.currentFocus
        view?.let { v ->
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(v.windowToken, 0)
        }
    }

    override fun openChat() {
        Utility.setSharedPrefStringData(this, Constants.MESSAGE_COUNT, "0")
        getBadge()
        openFragment(PatientContactsFragment.newInstance(this))
    }

    override fun openChat(doctor: Doctor?) {

    }

    override fun onLogout() {
        logoutUser()
    }

    private fun logoutUser() {
        if (checkingAccess()) {
            val viewModel =
                ViewModelProvider(this).get(PatientViewModel::class.java)

            val request = MultipartBody.Builder()

            request.setType(MultipartBody.FORM)
                .addFormDataPart("action", "logout")
                .addFormDataPart("customer_type", userCustomerType.toString() ?: "")
                .addFormDataPart("user_id", userId ?: "")
                .addFormDataPart("firebase_token", firebaseToken)
                .addFormDataPart("platform", "android")

            val requestBody = request.build()

            viewModel.commonRequest(requestBody).observe(this, Observer {
                Utility.hideLoadingDialog()
                progressBar.visibility = View.GONE

                if (it == null)
                    return@Observer
                if (it.status == "200") {
                    logout()
                }
            })
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.nav_home -> {
                openFragment(PatientHomeClinicsFragment.newInstance(this))
                true
            }
            R.id.nav_notification -> {
                Utility.setSharedPrefStringData(this, Constants.NOTIFICATION_COUNT, "0")
                getBadge()
                openFragment(PatientNotificationsFragment.newInstance(this))
                true
            }
            R.id.nav_chat -> {
                openChat()
                true
            }
            R.id.nav_appointment -> {
                openFragment(ConsultantMyAppointmentFragment(this))
                true
            }
            R.id.nav_profile -> {
                openFragment(ClinicProfileFragment.newInstance(this))
                true
            }
            R.id.nav_balance -> {
                openClinicBalance()
                false
            }
            else -> false
        }
    }

    private fun getBadge() {
        val view = bottomNavigationView.getChildAt(0) as BottomNavigationMenuView
        val notiBadge = view.getChildAt(0)
//        val chatBadge = view.getChildAt(1)

        if (qNotiBadge == null)
            qNotiBadge = QBadgeView(this).bindTarget(notiBadge)

//        if (qChatBadge == null)
//            qChatBadge = QBadgeView(this).bindTarget(chatBadge)

        chatCount = Utility.getSharedPreference(this, Constants.MESSAGE_COUNT) ?: "0"
        notiCount = Utility.getSharedPreference(this, Constants.NOTIFICATION_COUNT) ?: "0"

//        if (!(chatCount.isEmpty() || chatCount == "0")) {
//            qChatBadge?.badgeText = chatCount
//        } else
//            qChatBadge?.hide(false)

        if (!(notiCount.isEmpty() || notiCount == "0")) {
            qNotiBadge?.badgeText = notiCount
        } else
            qNotiBadge?.hide(false)
    }

    private val runnable: Runnable = Runnable {
        startUpdate()
    }

    private fun startUpdate() {
        getBadge()
        handler.postDelayed(runnable, 1000)
    }

    override fun onStop() {
        super.onStop()
        stopUpdate()
    }

    private fun stopUpdate() {
        handler.removeCallbacksAndMessages(null)
    }

    private val handler: Handler = Handler()
    override fun onResume() {
        super.onResume()
        startUpdate()
    }
}