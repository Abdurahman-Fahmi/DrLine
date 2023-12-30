package com.wecareapp.android.utilities

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.wecareapp.android.BuildConfig
import com.wecareapp.android.Constants
import com.wecareapp.android.Login.Companion.logMessageOnOrOff
import com.wecareapp.android.R
import com.wecareapp.android.customviews.SnackBar
import java.io.ByteArrayOutputStream

object Utility {
    var progressDialog: ProgressDialog? = null
    fun isNetworkAvailable(context: Context): Boolean {
        return try {
            val connMgr = context
                .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            if (connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI)?.state == NetworkInfo.State.CONNECTED
                || connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI)?.state == NetworkInfo.State.CONNECTING
            ) {
                true
            } else connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)?.state == NetworkInfo.State.CONNECTED
                    || connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)?.state == NetworkInfo.State.CONNECTING
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    val isMarshmallowOS: Boolean
        get() = Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1

    fun isValueNullOrEmpty(value: String?): Boolean {
        var isValue = false
        if (value == null || value == null || value == "" || value == "null" || value.trim { it <= ' ' }.length == 0) {
            isValue = true
        }
        return isValue
    }

    fun showToastMessage(context: Context?, message: String?) {
        try {
            if (!isValueNullOrEmpty(message) && context != null) {
                val toast = Toast.makeText(
                    context.applicationContext, message,
                    Toast.LENGTH_SHORT
                )
                toast.show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun showLog(logMsg: String?, logVal: String?) {
        try {
            if (BuildConfig.DEBUG) {
                if (logMessageOnOrOff) {
                    if (!isValueNullOrEmpty(logMsg) && !isValueNullOrEmpty(logVal)) {
                        Log.e(logMsg, logVal!!)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getResourcesString(context: Context?, id: Int): String? {
        var value: String? = null
        if (context != null && id != -1) {
            value = context.resources.getString(id)
        }
        return value
    }

    fun setSharedPrefStringData(context: Context?, key: String?, value: String?) {
        try {
            if (context != null) {
                val sharedPreferences =
                    context.getSharedPreferences(Constants.APP_PREF, Context.MODE_PRIVATE)
                val sharedPreferenceEditor = sharedPreferences.edit()
                sharedPreferenceEditor.putString(key, value)
                sharedPreferenceEditor.apply()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setSharedPrefIntegerData(context: Context?, key: String?, value: Int?) {
        try {
            if (context != null) {
                val sharedPreferences =
                    context.getSharedPreferences(Constants.APP_PREF, Context.MODE_PRIVATE)
                val sharedPreferenceEditor = sharedPreferences.edit()
                sharedPreferenceEditor.putInt(key, value!!)
                sharedPreferenceEditor.apply()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setSharedPrefStringData(context: Context?, key: String?, value: Boolean) {
        try {
            if (context != null) {
                val sharedPreferences =
                    context.getSharedPreferences(Constants.APP_PREF, Context.MODE_PRIVATE)
                val sharedPreferenceEditor = sharedPreferences.edit()
                sharedPreferenceEditor.putBoolean(key, value)
                sharedPreferenceEditor.apply()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun clearSharedPreference(context: Context?) {
        try {
            if (context != null) {
                val sharedPreferences =
                    context.getSharedPreferences(Constants.APP_PREF, Context.MODE_PRIVATE)
                val sharedPreferenceEditor = sharedPreferences.edit()
                sharedPreferenceEditor.clear()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getSharedPreference(context: Context, key: String?): String? {
        return try {
            val sharedPreferences =
                context.getSharedPreferences(Constants.APP_PREF, Context.MODE_PRIVATE)
            sharedPreferences.getString(key, "")
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    fun getSharedPreference(context: Context, key: String?, defValue: String?): String? {
        return try {
            val sharedPreferences =
                context.getSharedPreferences(Constants.APP_PREF, Context.MODE_PRIVATE)
            sharedPreferences.getString(key, defValue)
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    fun getSharedPreferenceBoolean(context: Context, key: String?, defaultValue: Boolean): Boolean {
        return try {
            val sharedPreferences =
                context.getSharedPreferences(Constants.APP_PREF, Context.MODE_PRIVATE)
            sharedPreferences.getBoolean(key, defaultValue)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun getSharedPreferenceInteger(context: Context, key: String?, defaultValue: Int): Int {
        return try {
            val sharedPreferences =
                context.getSharedPreferences(Constants.APP_PREF, Context.MODE_PRIVATE)
            sharedPreferences.getInt(key, defaultValue)
        } catch (e: Exception) {
            e.printStackTrace()
            defaultValue
        }
    }

    fun setSnackBarEnglish(parent: AppCompatActivity, mView: View?, message: String?) {
        val snackBarIconTitle = SnackBar()
        snackBarIconTitle.view(mView)
            .text(message, "OK")
            .textColors(Color.WHITE, Color.WHITE)
            .backgroundColor(parent.resources.getColor(R.color.colorPrimary))
            .duration(SnackBar.SnackBarDuration.LONG)
        snackBarIconTitle.show()
    }

    fun hideKeyboard(context: Context) {
        val inputManager = context
            .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        // check if no view has focus:
        val v = (context as Activity).currentFocus ?: return
        inputManager.hideSoftInputFromWindow(v.windowToken, 0)
    }

    /**
     * Method to show dialog with given message
     *
     * @param title        dialog heading
     * @param isCancelable whether dialog is cancellable or not
     */
    fun showLoadingDialog(context: Context?, title: String?, isCancelable: Boolean) {
        try {
            if (progressDialog != null && progressDialog!!.isShowing) {
                return
            }
            progressDialog = ProgressDialog(context)
            progressDialog!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
            progressDialog!!.isIndeterminate = true
            progressDialog!!.setMessage(title)
            progressDialog!!.setCancelable(isCancelable)
            progressDialog!!.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Hides loading dialog if shown
     */
    fun hideLoadingDialog() {
        try {
            if (progressDialog != null && progressDialog!!.isShowing) progressDialog!!.dismiss()
            progressDialog = null
        } catch (e: Exception) {
            progressDialog = null
        }
    }

    fun getDeviceId(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }

    fun getImageLink(link: String?): String? {
        if (link == null) return null else if (link.contains("http") || link.contains("www.") || link.contains(
                ".com"
            )
        ) return link
        return Constants.BASE_URL + "" + link
    }

    fun getImageUri(context: Context?, inImage: Bitmap?): Uri? {
        val bytes = ByteArrayOutputStream()
        inImage?.compress(Bitmap.CompressFormat.JPEG, 90, bytes)
        val path = MediaStore.Images.Media.insertImage(
            context?.contentResolver,
            inImage,
            "image",
            null
        )
        return Uri.parse(path)
    }

}