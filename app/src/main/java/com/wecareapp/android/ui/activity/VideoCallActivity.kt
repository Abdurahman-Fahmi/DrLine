package com.wecareapp.android.ui.activity

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.PorterDuff
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.SurfaceView
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.wecareapp.android.R
import com.wecareapp.android.model.Doctor
import com.wecareapp.android.model.InboxItem
import com.wecareapp.android.utilities.Utility
import com.wecareapp.android.webservices.viewmodels.PatientViewModel
import io.agora.rtc.IRtcEngineEventHandler
import io.agora.rtc.RtcEngine
import io.agora.rtc.video.VideoCanvas
import io.agora.rtc.video.VideoEncoderConfiguration
import kotlinx.android.synthetic.main.activity_video_chat_view.*
import okhttp3.MultipartBody
import org.json.JSONObject


class VideoCallActivity : BaseActivity() {

    private var chatId: String? = null
    private var callerId: String? = null
    private var senderId: String? = null
    private var channelName: String? = null
    private var agoraToken: String? = null
    private var callerName: String? = null
    private var calleeName: String? = null
    private var contact: InboxItem? = null
    private var doctor: Doctor? = null
    private var mRtcEngine: RtcEngine? = null
    private val mRtcEventHandler = object : IRtcEngineEventHandler() {
        /**
         * Occurs when the first remote video frame is received and decoded.
         * This callback is triggered in either of the following scenarios:
         *
         *     The remote user joins the channel and sends the video stream.
         *     The remote user stops sending the video stream and re-sends it after 15 seconds. Possible reasons include:
         *         The remote user leaves channel.
         *         The remote user drops offline.
         *         The remote user calls the muteLocalVideoStream method.
         *         The remote user calls the disableVideo method.
         *
         * @param uid User ID of the remote user sending the video streams.
         * @param width Width (pixels) of the video stream.
         * @param height Height (pixels) of the video stream.
         * @param elapsed Time elapsed (ms) from the local user calling the joinChannel method until this callback is triggered.
         */
        override fun onFirstRemoteVideoDecoded(uid: Int, width: Int, height: Int, elapsed: Int) {
            runOnUiThread { setupRemoteVideo(uid) }
        }

        /**
         * Occurs when a remote user (Communication)/host (Live Broadcast) leaves the channel.
         *
         * There are two reasons for users to become offline:
         *
         *     Leave the channel: When the user/host leaves the channel, the user/host sends a
         *     goodbye message. When this message is received, the SDK determines that the
         *     user/host leaves the channel.
         *
         *     Drop offline: When no data packet of the user or host is received for a certain
         *     period of time (20 seconds for the communication profile, and more for the live
         *     broadcast profile), the SDK assumes that the user/host drops offline. A poor
         *     network connection may lead to false detections, so we recommend using the
         *     Agora RTM SDK for reliable offline detection.
         *
         * @param uid ID of the user or host who leaves the channel or goes offline.
         * @param reason Reason why the user goes offline:
         *
         *     USER_OFFLINE_QUIT(0): The user left the current channel.
         *     USER_OFFLINE_DROPPED(1): The SDK timed out and the user dropped offline because no data packet was received within a certain period of time. If a user quits the call and the message is not passed to the SDK (due to an unreliable channel), the SDK assumes the user dropped offline.
         *     USER_OFFLINE_BECOME_AUDIENCE(2): (Live broadcast only.) The client role switched from the host to the audience.
         */
        override fun onUserOffline(uid: Int, reason: Int) {
            runOnUiThread { onRemoteUserLeft() }
        }

        /**
         * Occurs when a remote user stops/resumes sending the video stream.
         *
         * @param uid ID of the remote user.
         * @param muted Whether the remote user's video stream playback pauses/resumes:
         * true: Pause.
         * false: Resume.
         */
        override fun onUserMuteVideo(uid: Int, muted: Boolean) {
            runOnUiThread { onRemoteUserVideoMuted(uid, muted) }
        }
    }

    private lateinit var viewModel: PatientViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_chat_view)

        viewModel = ViewModelProvider(this).get(PatientViewModel::class.java)

        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )

        doctor = intent.extras?.getSerializable("doctor") as Doctor?
        contact = intent.extras?.getSerializable("contact") as InboxItem?
        channelName = intent.extras?.getString("channelName")
        agoraToken = intent.extras?.getString("agoraToken")
        callerName = intent.extras?.getString("caller_name")
        calleeName = intent.extras?.getString("callee_name")
        chatId = intent.extras?.getString("chatId")
        callerId = intent.extras?.getString("callerId")
        senderId = intent.extras?.getString("senderId")

        if (checkSelfPermission(
                Manifest.permission.RECORD_AUDIO,
                PERMISSION_REQ_ID_RECORD_AUDIO
            ) && checkSelfPermission(Manifest.permission.CAMERA, PERMISSION_REQ_ID_CAMERA)
        ) {
            initAgoraEngineAndJoinChannel()
        }

        ivBack.setOnClickListener {
            onBackPressed()
        }

        if (!TextUtils.isEmpty(callerName)) {
            tvClinicName.text = callerName
        }
    }

    private fun initAgoraEngineAndJoinChannel() {
        initializeAgoraEngine()
        setupVideoProfile()
        setupLocalVideo()
        joinChannel()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        Log.i(LOG_TAG, "onRequestPermissionsResult " + grantResults[0] + " " + requestCode)

        when (requestCode) {
            PERMISSION_REQ_ID_RECORD_AUDIO -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkSelfPermission(Manifest.permission.CAMERA, PERMISSION_REQ_ID_CAMERA)
                } else {
                    showLongToast("No permission for " + Manifest.permission.RECORD_AUDIO)
                    finish()
                }
            }
            PERMISSION_REQ_ID_CAMERA -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initAgoraEngineAndJoinChannel()
                } else {
                    showLongToast("No permission for " + Manifest.permission.CAMERA)
                    finish()
                }
            }
        }
    }

    private fun showLongToast(msg: String) {
        this.runOnUiThread { Toast.makeText(applicationContext, msg, Toast.LENGTH_LONG).show() }
    }

    override fun onResume() {
        super.onResume()
        joinChannel()
    }

    override fun onPause() {
        super.onPause()
        leaveChannel()
        onRemoteUserLeft()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()

        leaveChannel()
        /*
          Destroys the RtcEngine instance and releases all resources used by the Agora SDK.

          This method is useful for apps that occasionally make voice or video calls,
          to free up resources for other operations when not making calls.
         */
        RtcEngine.destroy()
        mRtcEngine = null
    }

    fun onLocalVideoMuteClicked(view: View) {
        val iv = view as ImageView
        if (iv.isSelected) {
            iv.isSelected = false
            iv.clearColorFilter()
        } else {
            iv.isSelected = true
            iv.setColorFilter(resources.getColor(R.color.colorPrimary), PorterDuff.Mode.MULTIPLY)
        }

        // Stops/Resumes sending the local video stream.
        mRtcEngine!!.muteLocalVideoStream(iv.isSelected)

        val container = findViewById<FrameLayout>(R.id.local_video_view_container)
        val surfaceView = container.getChildAt(0) as SurfaceView
        surfaceView.setZOrderMediaOverlay(!iv.isSelected)
        surfaceView.visibility = if (iv.isSelected) View.GONE else View.VISIBLE
    }

    fun onLocalAudioMuteClicked(view: View) {
        val iv = view as ImageView
        if (iv.isSelected) {
            iv.isSelected = false
            iv.clearColorFilter()
        } else {
            iv.isSelected = true
            iv.setColorFilter(resources.getColor(R.color.colorPrimary), PorterDuff.Mode.MULTIPLY)
        }

        // Stops/Resumes sending the local audio stream.
        mRtcEngine!!.muteLocalAudioStream(iv.isSelected)
    }

    fun onSwitchCameraClicked(view: View) {
        // Switches between front and rear cameras.
        mRtcEngine!!.switchCamera()
    }

    fun onEncCallClicked(view: View) {
        cancelCall()
    }

    private fun initializeAgoraEngine() {
        try {
            mRtcEngine =
                RtcEngine.create(baseContext, getString(R.string.agora_app_id), mRtcEventHandler)
        } catch (e: Exception) {
            Log.e(LOG_TAG, Log.getStackTraceString(e))

            throw RuntimeException(
                "NEED TO check rtc sdk init fatal error\n" + Log.getStackTraceString(
                    e
                )
            )
        }
    }

    private fun setupVideoProfile() {
        // In simple use cases, we only need to enable video capturing
        // and rendering once at the initialization step.
        // Note: audio recording and playing is enabled by default.
        mRtcEngine!!.enableVideo()
//      mRtcEngine!!.setVideoProfile(Constants.VIDEO_PROFILE_360P, false) // Earlier than 2.3.0

        // Please go to this page for detailed explanation
        // https://docs.agora.io/en/Video/API%20Reference/java/classio_1_1agora_1_1rtc_1_1_rtc_engine.html#af5f4de754e2c1f493096641c5c5c1d8f
        mRtcEngine!!.setVideoEncoderConfiguration(
            VideoEncoderConfiguration(
                VideoEncoderConfiguration.VD_640x360,
                VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15,
                VideoEncoderConfiguration.STANDARD_BITRATE,
                VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT
            )
        )
    }

    private fun setupLocalVideo() {
        // This is used to set a local preview.
        // The steps setting local and remote view are very similar.
        // But note that if the local user do not have a uid or do
        // not care what the uid is, he can set his uid as ZERO.
        // Our server will assign one and return the uid via the event
        // handler callback function (onJoinChannelSuccess) after
        // joining the channel successfully.
        val container = findViewById<FrameLayout>(R.id.local_video_view_container)
        val surfaceView = RtcEngine.CreateRendererView(baseContext)
        surfaceView.setZOrderMediaOverlay(true)
        container.addView(surfaceView)
        // Initializes the local video view.
        // RENDER_MODE_FIT: Uniformly scale the video until one of its dimension fits the boundary. Areas that are not filled due to the disparity in the aspect ratio are filled with black. 
        mRtcEngine!!.setupLocalVideo(VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_FILL, 0))
    }

    private fun joinChannel() {
        try {// 1. Users can only see each other after they join the
            // same channel successfully using the same app id.
            // 2. One token is only valid for the channel name that
            // you use to generate this token.
            var token: String? = getString(R.string.agora_access_token)
            var cName: String? = "drline"

            if (agoraToken != null)
                token = agoraToken

            if (channelName != null)
                cName = channelName

//        if (chatId != null)
//            cName = chatId

            if (token!!.isEmpty()) {
                token = null
            }

            Log.e("Call", "Token : $token")
            Log.e("Call", "Channel Name : $cName")

            mRtcEngine!!.joinChannel(
                token,
                cName,
                "Extra Optional Data",
                0
            ) // if you do not specify the uid, we will generate the uid for you
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupRemoteVideo(uid: Int) {
        // Only one remote video view is available for this
        // tutorial. Here we check if there exists a surface
        // view tagged as this uid.
        val container = findViewById<FrameLayout>(R.id.remote_video_view_container)

        if (container.childCount >= 1) {
            return
        }

        /*
          Creates the video renderer view.
          CreateRendererView returns the SurfaceView type. The operation and layout of the view
          are managed by the app, and the Agora SDK renders the view provided by the app.
          The video display view must be created using this method instead of directly
          calling SurfaceView.
         */
        val surfaceView = RtcEngine.CreateRendererView(baseContext)
        container.addView(surfaceView)
        // Initializes the video view of a remote user.
        mRtcEngine!!.setupRemoteVideo(VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_FILL, uid))

        surfaceView.tag = uid // for mark purpose
        val tipMsg = findViewById<TextView>(R.id.quick_tips_when_use_agora_sdk) // optional UI
        tipMsg.visibility = View.GONE
    }

    private fun leaveChannel() {
        mRtcEngine!!.leaveChannel()
    }

    private fun onRemoteUserLeft() {
        val container = findViewById<FrameLayout>(R.id.remote_video_view_container)
        container.removeAllViews()

        val tipMsg = findViewById<TextView>(R.id.quick_tips_when_use_agora_sdk) // optional UI
        tipMsg.visibility = View.VISIBLE
    }

    private fun onRemoteUserVideoMuted(uid: Int, muted: Boolean) {
        try {
            val container = findViewById<FrameLayout>(R.id.remote_video_view_container)

            val surfaceView = container.getChildAt(0) as SurfaceView

            val tag = surfaceView.tag
            if (tag != null && tag as Int == uid) {
                surfaceView.visibility = if (muted) View.GONE else View.VISIBLE
            }
        } catch (e: Exception) {
        }
    }

    companion object {

        private val LOG_TAG = VideoCallActivity::class.java.simpleName

        private const val PERMISSION_REQ_ID_RECORD_AUDIO = 22
        private const val PERMISSION_REQ_ID_CAMERA = PERMISSION_REQ_ID_RECORD_AUDIO + 1
    }

    private fun cancelCall() {
        val jsonObject = JSONObject()
        jsonObject.put("action", "sendCallRejectNotification")
        jsonObject.put("user_id", userId)
        jsonObject.put("customer_type", userId)
        Utility.showLog("jsonObject", "" + jsonObject)

        val request = MultipartBody.Builder()

        request.setType(MultipartBody.FORM)
            .addFormDataPart("action", "sendCallRejectNotification")
            .addFormDataPart("customer_type", userCustomerType.toString() ?: "")
            .addFormDataPart("user_id", userId ?: "")
            .addFormDataPart("User_name", user?.userName ?: "")
            .addFormDataPart("user_name", user?.userName ?: "")

        request.addFormDataPart("body", "call")
        request.addFormDataPart("caller_name", callerName ?: "")
        request.addFormDataPart("callee_name", user?.userName ?: "")
        request.addFormDataPart("channelName", channelName ?: "")
        request.addFormDataPart("agoraToken", agoraToken ?: "")
        request.addFormDataPart("tokens", agoraToken ?: "")
        request.addFormDataPart("channelId", channelName ?: "")
        request.addFormDataPart("callerName", callerName ?: "")
        request.addFormDataPart("callerId", callerId ?: "")
        request.addFormDataPart("senderId", senderId ?: "")
        request.addFormDataPart("data", "data")
        request.addFormDataPart("content_available", "true")
        if (checkingAccess()) {
            progressBar.visibility = View.VISIBLE
            request.addFormDataPart("nType", "reject")

            val requestBody = request.build()

            viewModel.commonRequest(requestBody).observe(this, Observer {
                Utility.hideLoadingDialog()
                progressBar.visibility = View.GONE
                finish()
            })
        }
    }

}
