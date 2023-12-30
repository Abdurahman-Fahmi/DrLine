package com.wecareapp.android.ui.activity

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Base64
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import com.wecareapp.android.BuildConfig
import com.wecareapp.android.Constants.LOGIN_TYPE
import com.wecareapp.android.R
import com.wecareapp.android.ui.activity.clinic.ClinicMainActivityI
import com.wecareapp.android.ui.activity.consultant.ConsultantMainActivityI
import com.wecareapp.android.ui.activity.patient.PatientMainActivity
import com.wecareapp.android.utilities.Utility
import kotlinx.android.synthetic.main.activity_splash.*
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

class SplashActivity : BaseActivity() {
    private val ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE: Int = 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val bundle = intent.extras

//        val serviceIntent = Intent(applicationContext, HeadsUpNotificationService::class.java)
//        val mBundle = Bundle()
//        mBundle.putString("channelName", "channelName")
//        mBundle.putString("agoraToken", "agoraToken")
//        mBundle.putString("type", "video_call")
//        serviceIntent.putExtras(mBundle)
//        serviceIntent.putExtra(ConstantApp.FCM_DATA_KEY, mBundle)
//        serviceIntent.action = HeadsUpNotificationService.ACTION_START_FOREGROUND_SERVICE
//        ContextCompat.startForegroundService(this, serviceIntent)

        if (bundle != null) {
            val messageType = bundle.getString("messageType")
            if (messageType?.contentEquals("video_call") == true) {
                val intent = Intent(this, VideoCallActivity::class.java)
                bundle.let { intent.putExtras(bundle) }
                startActivity(intent)
                finish()
            } else if (messageType?.contentEquals("audio_call") == true) {
                val intent = Intent(this, VoiceCallActivity::class.java)
                bundle.let { intent.putExtras(bundle) }
                startActivity(intent)
                finish()
            } else {
                mainScreen(bundle)
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + BuildConfig.APPLICATION_ID)
            )
//            startActivityForResult(
//                intent,
//                ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE
//            )
        }


        tvStart.setOnClickListener {
            mainScreen(bundle)
        }

        try {
            val info = packageManager.getPackageInfo(
                "com.caberz.drline",
                PackageManager.GET_SIGNATURES
            )
            for (signature in info.signatures) {
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT))
                val key = Base64.encodeToString(md.digest(), Base64.DEFAULT)
                print("key : $key")
            }
        } catch (e: PackageManager.NameNotFoundException) {
        } catch (e: NoSuchAlgorithmException) {
        }
    }

    private fun mainScreen(bundle: Bundle?) {
        when (Utility.getSharedPreferenceInteger(this, LOGIN_TYPE, -1)) {
            1 -> {
                val intent = Intent(this, PatientMainActivity::class.java)
                bundle?.let { intent.putExtras(bundle) }
                startActivity(intent)
                finish()
            }
            2 -> {
                val intent = Intent(this, ConsultantMainActivityI::class.java)
                bundle?.let { intent.putExtras(bundle) }
                startActivity(intent)
                finish()
            }
            3 -> {
                val intent = Intent(this, ClinicMainActivityI::class.java)
                bundle?.let { intent.putExtras(bundle) }
                startActivity(intent)
                finish()
            }
            else -> {
                finish()
                startActivity(Intent(this, SignInActivity::class.java))
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
        }

    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        val bundle = intent?.extras
    }
}