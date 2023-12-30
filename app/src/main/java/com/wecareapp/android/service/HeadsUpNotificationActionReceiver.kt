package com.wecareapp.android.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.wecareapp.android.ui.activity.VideoCallActivity
import com.wecareapp.android.ui.activity.VoiceCallActivity
import com.wecareapp.android.utilities.SoundPoolManager
import com.wecareapp.android.utilities.Utility
import com.wecareapp.android.webservices.RetrofitInstance.Companion.BASE_URL
import okhttp3.*
import org.json.JSONObject


class HeadsUpNotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.extras != null) {
            val action = intent.getStringExtra(ConstantApp.CALL_RESPONSE_ACTION_KEY)
            val data = intent.getBundleExtra(ConstantApp.FCM_DATA_KEY)
            action?.let { performClickAction(context, it, data) }
        }
    }

    private fun performClickAction(context: Context, action: String, data: Bundle?) {
        if (action == ConstantApp.CALL_RECEIVE_ACTION && data != null && data["type"] == "audio_call") {
            var openIntent: Intent? = null
            openIntent = Intent(context, VoiceCallActivity::class.java)
            openIntent.putExtra("channelName", data["channelName"].toString())
            openIntent.putExtra("agoraToken", data["agoraToken"].toString())
            openIntent.putExtra("caller_name", data["caller_name"].toString())
            openIntent.putExtra("callee_name", data["callee_name"].toString())
            openIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(openIntent)
            stopS(context)
        } else if (action == ConstantApp.CALL_RECEIVE_ACTION && data != null && data["type"] == "video_call") {
            var openIntent: Intent? = null
            openIntent = Intent(context, VideoCallActivity::class.java)
            openIntent.putExtra("channelName", data["channelName"].toString())
            openIntent.putExtra("agoraToken", data["agoraToken"].toString())
            openIntent.putExtra("caller_name", data["caller_name"].toString())
            openIntent.putExtra("callee_name", data["callee_name"].toString())
            openIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(openIntent)
            stopS(context)
        } else if (action == ConstantApp.CALL_CANCEL_ACTION) {
            stopS(context)
            cancelCall(context, data)
        }
    }

    companion object {
        fun stopS(context: Context) {
            SoundPoolManager.getInstance(context).stopRinging()
            // Close the notification after the click action is performed.
            context.stopService(Intent(context, HeadsUpNotificationService::class.java))
            val it = Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
            context.sendBroadcast(it)
        }
    }

    private fun cancelCall(context: Context, data: Bundle?) {
        val jsonObject = JSONObject()
        jsonObject.put("action", "sendCallRejectNotification")
//        jsonObject.put("user_id", userId)
        jsonObject.put("nType", "reject")
        jsonObject.put("tokens", data?.get("agoraToken").toString())
        Utility.showLog("jsonObject", "" + jsonObject)

        val request = MultipartBody.Builder()

        request.setType(MultipartBody.FORM)
            .addFormDataPart("action", "sendCallRejectNotification")
//            .addFormDataPart("customer_type", userCustomerType.toString() ?: "")
//            .addFormDataPart("user_id", userId ?: "")
//            .addFormDataPart("User_name", user?.userName ?: "")
//            .addFormDataPart("user_name", user?.userName ?: "")
//
//        request.addFormDataPart("body", "call")
//        request.addFormDataPart("caller_name", callerName ?: "")
//        request.addFormDataPart("callee_name", user?.userName ?: "")
//        request.addFormDataPart("channelName", channelName ?: "")
//        request.addFormDataPart("agoraToken", agoraToken ?: "")
//        request.addFormDataPart("tokens", agoraToken ?: "")
//        request.addFormDataPart("channelId", channelName ?: "")
//        request.addFormDataPart("callerName", callerName ?: "")
//        request.addFormDataPart("callerId", callerId ?: "")
//        request.addFormDataPart("senderId", senderId ?: "")
        request.addFormDataPart("data", "data")
        request.addFormDataPart("content_available", "true")


        val json = jsonObject.toString();
        val loginUrl = "$BASE_URL/webservices/index.php"

//        post ("$BASE_URL/webservices/index.php", json)
//        var viewModel: PatientViewModel = ViewModelProvider(context.applicationContext).get(PatientViewModel::class.java)

        /*if (checkingAccess()) {
            progressBar.visibility = View.VISIBLE
            request.addFormDataPart("nType", "reject")

            val requestBody = request.build()

            viewModel.commonRequest(requestBody).observe(this, Observer {
                Utility.hideLoadingDialog()
                progressBar.visibility = View.GONE
                finish()
            })
        }*/

        val formBody: RequestBody = FormBody.Builder()
            .add("username", "userName")
            .add("password", "password")
            .add("customCredential", "")
            .add("isPersistent", "true")
            .add("setCookie", "true")

            .add("action", "sendCallRejectNotification")
            .add("nType", "reject")
            .add("tokens", data?.get("agoraToken").toString())
            .build()

        val client: OkHttpClient = OkHttpClient.Builder()
//            .addInterceptor(this)
            .build()

        val request2: Request = Request.Builder()
            .url(loginUrl)
            .post(formBody)
            .build()
    }

    /*val JSON: MediaType = parse.parse("application/json; charset=utf-8")

    var client = OkHttpClient()

    @Throws(IOException::class)
    fun post(url: String?, json: String?): String? {
        val body = RequestBody.create(JSON, json!!) // new
        // RequestBody body = RequestBody.create(JSON, json); // old
        val request: Request = Retrofit.Builder()
            .url(url)
            .post(body)
            .build()
        val response = client.newCall(request).execute()
        return response.body!!.string()
    }*/
}