package com.wecareapp.android.ui.activity.patient

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.wecareapp.android.Constants
import com.wecareapp.android.R
import com.wecareapp.android.model.Doctor
import com.wecareapp.android.model.InboxItem
import com.wecareapp.android.model.Profile
import com.wecareapp.android.ui.activity.BaseActivity
import com.wecareapp.android.ui.activity.ChatActivity
import com.wecareapp.android.ui.fragment.ICallBackToActivity
import com.wecareapp.android.ui.fragment.PatientContactsFragment
import com.wecareapp.android.ui.fragment.PatientNotificationsFragment
import com.wecareapp.android.ui.fragment.patient.PatientHomeClinicsFragment
import com.wecareapp.android.ui.fragment.patient.PatientMyAppointmentFragment
import com.wecareapp.android.ui.fragment.patient.PatientProfileFragment
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

class PatientMainActivity : BaseActivity(), View.OnClickListener, ICallBackToActivity,
    BottomNavigationView.OnNavigationItemSelectedListener {

    private var profile: Profile? = null
    private var email: String? = ""
    private var customerType: String? = ""
    private var contactType: String? = ""
    private var action: String? = ""

    private val exitToast: Toast? = null

    private var isFrom: String = ""
    private var messageType: String = ""
    private var chatCount: String = ""
    private var notiCount: String = ""
    private var qChatBadge: Badge? = null
    private var qNotiBadge: Badge? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_main)

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

        if (savedInstanceState == null) {
            if (isFrom.isNullOrEmpty())
                openFragment(PatientHomeClinicsFragment.newInstance(this))
            else {
                if (messageType.isNullOrEmpty()) {
                    openNotification()
                } else {
                    openChat()
                }
            }
        }

        bottomNavigationView.selectedItemId = R.id.nav_home

        if (savedInstanceState == null) {
            openFragment(PatientHomeClinicsFragment.newInstance(this))
        }
        bottomNavigationView.setOnNavigationItemSelectedListener(this)
    }

    override fun onClick(v: View?) {

    }

    override fun onBackPressed() {
        try {
            if (supportFragmentManager.backStackEntryCount > 1) {
                supportFragmentManager.popBackStack()
                showFragment(supportFragmentManager.backStackEntryCount - 1)
                /*val frgm = supportFragmentManager.fragments[supportFragmentManager.fragments.size - 1]
                if (frgm != null) {
                    val transaction = supportFragmentManager.beginTransaction()
    //                transaction.replace(R.id.container, frgm)
                    transaction.show(frgm)
                    transaction.commit()
                }*/
            } else {
                val f = getActiveFragment()
                if (f?.tag != PatientHomeClinicsFragment::class.java.name) {
                    bottomNavigationView.selectedItemId = R.id.nav_home
                    //                bottomNavigationView.menu.findItem(R.id.nav_home).isChecked = true
                    //                onNavigationItemSelected(bottomNavigationView.menu.findItem(R.id.nav_home))
                } else
                    PopUtils.exitDialog(this, getString(R.string.msg_do_you_want_exit)) {
                        finishAffinity()
                    }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onBackClicked(home: Boolean) {
        if (home) {
            bottomNavigationView.selectedItemId = R.id.nav_home
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
                clearAllFragment()
                openFragment(PatientHomeClinicsFragment.newInstance(this))
                true
            }
            R.id.nav_notification -> {
                openNotification()
                true
            }
            R.id.nav_chat -> {
                openChat()
                true
            }
            R.id.nav_appointment -> {
                if (!isLogged)
                    openLoginActivity()
                else {
                    clearAllFragment()
                    openFragment(PatientMyAppointmentFragment.newInstance(this))
                }
                true
            }
            R.id.nav_profile -> {
                if (!isLogged)
                    openLoginActivity()
                else {
                    clearAllFragment()
                    openFragment(PatientProfileFragment.newInstance(this))
                }
                true
            }
            else -> false
        }
    }

    override fun openChat() {
        if (!isLogged)
            openLoginActivity()
        else {
            Utility.setSharedPrefStringData(this, Constants.MESSAGE_COUNT, "0")
            getBadge()
            main_content.background =
                ContextCompat.getDrawable(this, R.drawable.ic_chat_background_container)
            openFragment(PatientContactsFragment.newInstance(this))
        }
    }

    override fun openChat(doctor: Doctor?) {
        if (!isLogged)
            openLoginActivity()
        else {
            if (doctor == null || TextUtils.isEmpty(doctor.doctorId))
                return

            Utility.setSharedPrefStringData(this, Constants.MESSAGE_COUNT, "0")
            getBadge()

            main_content.background =
                ContextCompat.getDrawable(this, R.drawable.ic_chat_background_container)

            startActivity(Intent(this, ChatActivity::class.java).apply {
                putExtra("chat", "chat")
                putExtra("doctor", doctor)
            })
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
//            openFragment(PatientContactsFragment(this))
        }
    }

    private fun openNotification() {
        if (!isLogged) {
            openLoginActivity()
        } else {
            Utility.setSharedPrefStringData(this, Constants.NOTIFICATION_COUNT, "0")
            getBadge()
            clearAllFragment()
            openFragment(PatientNotificationsFragment.newInstance(this))
        }
    }

    private fun getBadge() {
        val view = bottomNavigationView.getChildAt(0) as BottomNavigationMenuView
        val notiBadge = view.getChildAt(0)
        val chatBadge = view.getChildAt(1)

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