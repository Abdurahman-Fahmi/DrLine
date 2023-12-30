package com.wecareapp.android.ui.activity.consultant

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import com.wecareapp.android.Constants
import com.wecareapp.android.R
import com.wecareapp.android.model.Doctor
import com.wecareapp.android.model.InboxItem
import com.wecareapp.android.model.Profile
import com.wecareapp.android.ui.activity.BaseActivity
import com.wecareapp.android.ui.fragment.*
import com.wecareapp.android.ui.fragment.consultant.ConsultantMyAppointmentFragment
import com.wecareapp.android.ui.fragment.consultant.ConsultantProfileFragment
import com.wecareapp.android.ui.fragment.patient.PatientHomeClinicsFragment
import com.wecareapp.android.utilities.PopUtils
import com.wecareapp.android.utilities.Utility
import com.wecareapp.android.webservices.viewmodels.PatientViewModel
import kotlinx.android.synthetic.main.activity_patient_main.*
import kotlinx.android.synthetic.main.fragment_contacts_patient.*
import kotlinx.android.synthetic.main.fragment_patient_profile.*
import kotlinx.android.synthetic.main.fragment_patient_profile.progressBar
import okhttp3.MultipartBody
import q.rorbin.badgeview.Badge
import q.rorbin.badgeview.QBadgeView

class ConsultantMainActivityI : BaseActivity(), ICallBackToActivity {

    private var profile: Profile? = null
    private var email: String? = ""
    private var customerType: Int? = 1
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
        setContentView(R.layout.activity_consultant_main)

        if (intent.extras != null) {
            email = intent?.extras?.getString("email", "")
            profile =
                if (intent?.extras?.getSerializable("profile") == null) null else intent?.extras?.getSerializable(
                    "profile"
                ) as Profile
            customerType = intent?.extras?.getInt("customer_type", 1)
            contactType = intent?.extras?.getString("contact_type", "")
            action = intent?.extras?.getString("action", "")
            isFrom = intent?.extras?.getString("isFrom").toString()
            messageType = intent?.extras?.getString("messageType").toString()
        }

        bottomNavigationView.selectedItemId = R.id.nav_profile

        if (savedInstanceState == null) {
            openFragment(ConsultantProfileFragment(this))
        }

        bottomNavigationView.setOnNavigationItemSelectedListener { item: MenuItem ->
            return@setOnNavigationItemSelectedListener when (item.itemId) {
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
                    openFragment(ConsultantProfileFragment(this))
                    true
                }
                else -> false
            }
        }
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

    private fun getBadge() {
        val view = bottomNavigationView.getChildAt(0) as BottomNavigationMenuView
        val notiBadge = view.getChildAt(0)
        val chatBadge = view.getChildAt(2)

        if (qNotiBadge == null)
            qNotiBadge = QBadgeView(this).bindTarget(notiBadge)

        if (qChatBadge == null)
            qChatBadge = QBadgeView(this).bindTarget(chatBadge)

        chatCount = Utility.getSharedPreference(this, Constants.MESSAGE_COUNT) ?: "0"
        notiCount = Utility.getSharedPreference(this, Constants.NOTIFICATION_COUNT) ?: "0"
        if (!(chatCount.isEmpty() || chatCount == "0")) {
            qChatBadge?.badgeText = chatCount
        } else
            qChatBadge?.hide(false)
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
//        callAPIForContacts()
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

    private fun callAPIForContacts() {
        try {
            if (!checkingAccess())
                return

            val request = MultipartBody.Builder()
            request.setType(MultipartBody.FORM)
                .addFormDataPart("action", "loadInbox")
                .addFormDataPart("user_id", userId ?: "")
                .addFormDataPart("customer_type", userCustomerType.toString() ?: "1")
                .addFormDataPart("start", "0")
//
//            if (!TextUtils.isEmpty(searchKey)) {
//                searchKey?.let { request.addFormDataPart("search", it) }
//            }

            val requestBody = request.build()

            val viewModel: PatientViewModel =
                ViewModelProvider(this).get(PatientViewModel::class.java)
            viewModel.getChatList(requestBody).observe(this, Observer {
                Utility.hideLoadingDialog()
                progressBar.visibility = View.GONE
                if (it == null)
                    return@Observer
                if (it.status == "200") {
                    val list: MutableList<InboxItem?> = mutableListOf()
                    it.inbox?.let { it1 -> list.addAll(it1) }
                    var messageCount = 0
                    for (i in list) {
                        messageCount += i?.unreadCount?.toInt() ?: 0
                    }
                    Utility.setSharedPrefStringData(
                        this,
                        Constants.MESSAGE_COUNT,
                        messageCount.toString()
                    )
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}