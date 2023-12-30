package com.wecareapp.android.ui.fragment

import android.Manifest
import android.app.Activity
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.CalendarContract
import android.provider.CalendarContract.Reminders
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.wecareapp.android.Constants
import com.wecareapp.android.R
import com.wecareapp.android.locations.GPSTracker
import com.wecareapp.android.model.Doctor
import com.wecareapp.android.model.InboxItem
import com.wecareapp.android.model.Profile
import com.wecareapp.android.ui.activity.ChatActivity
import com.wecareapp.android.ui.activity.SignInActivity
import com.wecareapp.android.utilities.PopUtils
import com.wecareapp.android.utilities.Utility
import java.util.*


open class BaseFragment : Fragment() {

    protected val dummyList = arrayListOf<String>()
    protected lateinit var gpsTracker: GPSTracker
    protected var userId: String? = null
    protected var firebaseToken: String = ""
    protected var lang: String = "en"
    protected var userTypeStr: String = "patient"
    protected var userCustomerType: Int? = 1
    var user: Profile? = null
    protected var isLogged = false

    protected val REQUEST_CAMERA = 0
    protected val REQUEST_GALLERY = 1
    protected val REQUEST_VIDEO = 2
    protected val REQUEST_AUDIO = 3
    protected val REQUEST_DOCUMENT = 4

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gpsTracker = GPSTracker(context)

        val preferences =
            PreferenceManager.getDefaultSharedPreferences(activity?.applicationContext)
        firebaseToken = preferences.getString(Constants.FIREBASE_TOKEN, "") ?: ""

        lang = Utility.getSharedPreference(requireContext(), Constants.LANGUAGE, "en").toString()

        isLogged = Utility.getSharedPreferenceBoolean(requireContext(), Constants.IS_LOGIN, false)

        user = Gson().fromJson(
            Utility.getSharedPreference(requireContext(), Constants.USER_DATA),
            Profile::class.java
        )

        if (user != null) {
            userCustomerType = user?.customerType

            userId = user?.contactId
//            userId = 23.toString()

            userTypeStr = when (user?.customerType) {
                2 -> "consultant"
                3 -> "clinic"
                else -> "patient"
            }
        }

        dummyList.add("")
        dummyList.add("")
        dummyList.add("")
        dummyList.add("")
        dummyList.add("")
        dummyList.add("")
        dummyList.add("")
    }

    protected fun openLoginActivity() {
        val intent = Intent(context, SignInActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        activity?.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    protected open fun checkingAccess(): Boolean {
        return if (isNetworkAvailable(context)) {
            true
        } else {
            PopUtils.alertDialog(context, getString(R.string.err_msg_plz_check_internet)) {
            }
            false
        }
    }

    open fun isNetworkAvailable(context: Context?): Boolean {
        if (context != null) {
            val connectivityManager =
                context.getSystemService(AppCompatActivity.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            return activeNetworkInfo != null && activeNetworkInfo.isConnected
        }
        return false
    }

    protected fun openFragment(fragment: Fragment) {
        try {
            val tag = fragment.javaClass.name
//        hideAllFragments()
            var frgm = requireActivity().supportFragmentManager.findFragmentByTag(tag)
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
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
            requireActivity().supportFragmentManager.executePendingTransactions()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun hideAllFragments() {
        if (requireActivity().supportFragmentManager.backStackEntryCount <= 0)
            return
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        for (i in 0 until requireActivity().supportFragmentManager.backStackEntryCount) {
            val tag = requireActivity().supportFragmentManager.getBackStackEntryAt(i).name
            val f = requireActivity().supportFragmentManager.findFragmentByTag(tag)
            f?.let { transaction.hide(it) }
        }
        transaction.commit()
        requireActivity().supportFragmentManager.executePendingTransactions()
    }

    open fun checkLocationPermission(): Boolean {
        if (context?.let {
                ContextCompat.checkSelfPermission(
                    it,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            } != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (context?.let {
                        ActivityCompat.checkSelfPermission(
                            it,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        )
                    } != PackageManager.PERMISSION_GRANTED && context?.let {
                        ActivityCompat.checkSelfPermission(
                            it,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    } != PackageManager.PERMISSION_GRANTED
                ) {
                    requestPermissions(
                        arrayOf(
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ), 1
                    )
                }
            }
            return false
        } else
            return true
    }

    open fun check() {
        val cal: Calendar = Calendar.getInstance()
        val intent = Intent(Intent.ACTION_EDIT)
        intent.type = "vnd.android.cursor.item/event"
        intent.putExtra("beginTime", cal.getTimeInMillis())
        intent.putExtra("allDay", false)
        intent.putExtra("rrule", "FREQ=DAILY")
        intent.putExtra("endTime", cal.getTimeInMillis() + 60 * 60 * 1000)
        intent.putExtra("title", "A Test Event from android app")
        startActivity(intent)

    }


    open fun getCalendarUriBase(act: Activity): String? {
        var calendarUriBase: String? = null
        var calendars: Uri = Uri.parse("content://calendar/calendars")
        var managedCursor: Cursor? = null
        try {
            managedCursor = act.managedQuery(calendars, null, null, null, null)
        } catch (e: java.lang.Exception) {
        }
        if (managedCursor != null) {
            calendarUriBase = "content://calendar/"
        } else {
            calendars = Uri.parse("content://com.android.calendar/calendars")
            try {
                managedCursor = act.managedQuery(calendars, null, null, null, null)
            } catch (e: java.lang.Exception) {
            }
            if (managedCursor != null) {
                calendarUriBase = "content://com.android.calendar/"
            }
        }
        return calendarUriBase
    }

    fun check2() {
        /*// get calendar
        val cal = Calendar.getInstance()
        val EVENTS_URI = Uri.parse(getCalendarUriBase(this).toString() + "events")
        val cr: ContentResolver = getContentResolver()

        // event insert
        var values = ContentValues()
        values.put("calendar_id", 1)
        values.put("title", "Reminder Title")
        values.put("allDay", 0)
        values.put(
            "dtstart",
            cal.timeInMillis + 11 * 60 * 1000
        ) // event starts at 11 minutes from now

        values.put("dtend", cal.timeInMillis + 60 * 60 * 1000) // ends 60 minutes from now

        values.put("description", "Reminder description")
        values.put("visibility", 0)
        values.put("hasAlarm", 1)
        val event = cr.insert(EVENTS_URI, values)

        // reminder insert

        // reminder insert
        val REMINDERS_URI = Uri.parse(getCalendarUriBase(this).toString() + "reminders")
        values = ContentValues()
        values.put("event_id", event!!.lastPathSegment!!.toLong())
        values.put("method", 1)
        values.put("minutes", 10)
        cr.insert(REMINDERS_URI, values)*/
    }

    /** Adds Events and Reminders in Calendar.  */
    open fun addReminderInCalendar() {
        val cal = Calendar.getInstance()
        val EVENTS_URI = Uri.parse(getCalendarUriBase(true) + "events")
        val cr: ContentResolver? = activity?.contentResolver
        val timeZone = TimeZone.getDefault()

        /** Inserting an event in calendar.  */
        var values = ContentValues()
        values.put(CalendarContract.Events.CALENDAR_ID, 1)
        values.put(CalendarContract.Events.TITLE, "Sanjeev Reminder 01")
        values.put(CalendarContract.Events.DESCRIPTION, "A test Reminder.")
        values.put(CalendarContract.Events.ALL_DAY, 0)
        // event starts at 11 minutes from now
        values.put(CalendarContract.Events.DTSTART, cal.timeInMillis + 11 * 60 * 1000)
        // ends 60 minutes from now
        values.put(CalendarContract.Events.DTEND, cal.timeInMillis + 60 * 60 * 1000)
        values.put(CalendarContract.Events.EVENT_TIMEZONE, timeZone.id)
        values.put(CalendarContract.Events.HAS_ALARM, 1)
        val event = cr?.insert(EVENTS_URI, values)

        // Display event id
        Toast.makeText(
            activity?.applicationContext,
            "Event added :: ID :: " + event!!.lastPathSegment,
            Toast.LENGTH_SHORT
        ).show()
        /** Adding reminder for event added.  */
        val REMINDERS_URI = Uri.parse(getCalendarUriBase(true) + "reminders")
        values = ContentValues()
        values.put(Reminders.EVENT_ID, event!!.lastPathSegment!!.toLong())
        values.put(Reminders.METHOD, Reminders.METHOD_ALERT)
        values.put(Reminders.MINUTES, 10)
        cr?.insert(REMINDERS_URI, values)
    }

    /** Returns Calendar Base URI, supports both new and old OS.  */
    open fun getCalendarUriBase(eventUri: Boolean): String {
        var calendarURI: Uri? = null
        try {
            calendarURI = if (Build.VERSION.SDK_INT <= 7) {
                if (eventUri) Uri.parse("content://calendar/") else Uri.parse("content://calendar/calendars")
            } else {
                if (eventUri) Uri.parse("content://com.android.calendar/") else Uri
                    .parse("content://com.android.calendar/calendars")
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return calendarURI.toString()
    }

    fun openChat(clinic: InboxItem?, doctor: Doctor?) {
        activity?.startActivity(Intent(context, ChatActivity::class.java).apply {
            putExtra("chat", "chat")
            putExtra("contact", clinic)
            putExtra("doctor", doctor)
        })
        activity?.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }
}
