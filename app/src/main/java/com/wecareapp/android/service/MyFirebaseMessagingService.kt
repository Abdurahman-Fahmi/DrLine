package com.wecareapp.android.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.TextUtils
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.wecareapp.android.Constants
import com.wecareapp.android.R
import com.wecareapp.android.ui.activity.SplashActivity
import com.wecareapp.android.ui.activity.patient.PatientMainActivity
import com.wecareapp.android.utilities.Loggers
import com.wecareapp.android.utilities.Utility.setSharedPrefStringData
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

class MyFirebaseMessagingService : FirebaseMessagingService() {
    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Loggers.error(TAG, "From: " + remoteMessage.from)

        // Check if message contains a data payload.
        if (remoteMessage.data.isNotEmpty()) {
            Loggers.error(TAG, "Message data payload: " + remoteMessage.data)
            sendNotification(
                remoteMessage.data["title"], remoteMessage.data["body"],
                remoteMessage.data
            )
            Loggers.error(TAG, "Message data payload: " + Gson().toJson(remoteMessage.data))
        } else if (remoteMessage.notification != null) {
            Loggers.error(
                TAG, "Message Notification Body: " + remoteMessage.notification!!
                    .body
            )
            sendNotification(
                remoteMessage.notification!!.title, remoteMessage.notification!!.body,
                remoteMessage.data
            )
        }
    }

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    override fun onNewToken(token: String) {
        Loggers.error(TAG, "Refreshed token: $token")

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(token)
    }
    // [END on_new_token]
    //    /**
    //     * Schedule a job using FirebaseJobDispatcher.
    //     */
    /*
   private void scheduleJob() {
        // [START dispatch_job]
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(this));
        Job myJob = dispatcher.newJobBuilder()
                .setService(MyJobService.class)
                .setTag("my-job-tag")
                .build();
        dispatcher.schedule(myJob);
        // [END dispatch_job]
    }
    */
    /**
     * Handle time allotted to BroadcastReceivers.
     */
    private fun handleNow() {
        Log.d(TAG, "Short lived task is done.")
    }

    /**
     * Persist token to third-party servers.
     *
     *
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private fun sendRegistrationToServer(token: String) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(
            applicationContext
        )
        preferences.edit().putString(Constants.FIREBASE_TOKEN, token).apply()
        Log.d(TAG, "Token : $token")
        //        PreferenceManager preferenceManager = new PreferenceManager(this);
//        preferenceManager.setFirebaseToken(token);
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param title
     * @param messageBody FCM message body received.
     * @param data
     */
    private fun sendNotification(title: String?, messageBody: String?, data: Map<String, String>) {
        var messageCount: String? = ""
        var messageType: String? = ""
        var screen: String? = ""
        var notificationCount: String? = ""
        var channel_id: String? = ""
        var campaign_id: String? = ""
        var postId: String? = ""
        var channelName: String? = ""
        var agoraToken: String? = ""
        var callerName: String? = ""
        var callerImage: String? = ""
        var calleeName: String? = ""
        var calleeImage: String? = ""
        if (data.size > 0) {
            messageType = data["type"]
            channelName = data["channelName"]
            agoraToken = data["agoraToken"]
            messageCount = data["message_count"]
            screen = data["screen"]
            notificationCount = data["notification_count"]
            channel_id = data["channel_id"]
            campaign_id = data["campaign_id"]
            postId = data["postId"]
            callerName = data["caller_name"]
            callerImage = data["caller_image"]
            calleeName = data["callee_name"]
            calleeImage = data["callee_image"]
            val nType = data["nType"]
            if (nType != null) {
                if (nType.contentEquals("videoCall")) messageType =
                    "video_call" else if (nType.contentEquals("voiceCall")) messageType =
                    "audio_call"
            }
            val channelId = data["channelId"]
            if (!TextUtils.isEmpty(channelId)) channelName = channelId
            val tokens = data["tokens"]
            if (!TextUtils.isEmpty(tokens)) agoraToken = tokens
            val senderName = data["senderName"]
            if (!TextUtils.isEmpty(senderName)) calleeName = senderName
            val senderAva = data["senderAva"]
            if (!TextUtils.isEmpty(senderAva)) calleeImage = senderAva
            val callerName2 = data["callerName"]
            if (!TextUtils.isEmpty(callerName2)) callerName = callerName2
            val callerAva = data["callerAva"]
            if (!TextUtils.isEmpty(callerAva)) callerImage = callerAva
        }

        setSharedPrefStringData(this, Constants.MESSAGE_TYPE, messageType)
        if (messageType.equals("count", ignoreCase = true)) {
            setSharedPrefStringData(this, Constants.MESSAGE_COUNT, messageCount)
        } else {
            setSharedPrefStringData(this, Constants.NOTIFICATION_COUNT, "1")
        }

        try {
            if (messageType!!.contentEquals("video_call") || messageType.contentEquals("audio_call")) {
                val bitmap = getBitmapFromUrl(calleeImage)
                val stream = ByteArrayOutputStream()
                bitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                val bytes = stream.toByteArray()
                val serviceIntent =
                    Intent(applicationContext, HeadsUpNotificationService::class.java)
                val mBundle = Bundle()
                mBundle.putString("channelName", channelName)
                mBundle.putString("agoraToken", agoraToken)
                mBundle.putString("type", messageType)
                mBundle.putString("caller_name", callerName)
                mBundle.putString("callee_name", calleeName)
                mBundle.putString("caller_image", callerImage)
                mBundle.putString("callee_image", calleeImage)
                //                mBundle.putParcelable("bitmap", bitmap);
                mBundle.putByteArray("bitmapbytes", bytes)
                serviceIntent.putExtras(mBundle)
                serviceIntent.putExtra(ConstantApp.FCM_DATA_KEY, mBundle)
                serviceIntent.action = HeadsUpNotificationService.ACTION_START_FOREGROUND_SERVICE
                ContextCompat.startForegroundService(this, serviceIntent)
                return
            } else if (messageType!!.contentEquals("reject")) {
                HeadsUpNotificationActionReceiver.stopS(this)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val intent = Intent(this, SplashActivity::class.java)
        intent.putExtra("isFrom", "notification")
        intent.putExtra("messageType", messageType)
        intent.putExtra("campaign_id", campaign_id)
        intent.putExtra("postId", postId)
        intent.putExtra("isFrom", "notification")
        intent.putExtra("isFrom2", "notification")
        intent.putExtra("channelName", channelName)
        intent.putExtra("agoraToken", agoraToken)
        intent.putExtra("caller_name", callerName)
        intent.putExtra("callee_name", calleeName)
        intent.putExtra("caller_image", callerImage)
        intent.putExtra("callee_image", calleeImage)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val stackBuilder = TaskStackBuilder.create(this)
        stackBuilder.addNextIntentWithParentStack(intent)

        // pending intent req code changed
        val pendingIntentId = Random().nextInt(60000)
        val pendingIntent = PendingIntent.getActivity(
            this,  /* Request code */pendingIntentId, intent,
            PendingIntent.FLAG_ONE_SHOT
        ) //FLAG_ONE_SHOT

        //String channelId = getString(R.string.default_notification_channel_id);
        val channelId = ""
        val page = ""
        channel_id = if (page.equals("friend", ignoreCase = true)) {
            "channelfriendid"
        } else if (page.equals("group", ignoreCase = true)) {
            "channelgroupid"
        } else if (page.equals("channel", ignoreCase = true)) {
            "channelid"
        } else {
            getString(R.string.default_notification_channel_id)
        }
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(if (TextUtils.isEmpty(title)) getString(R.string.fcm_message) else title)
            .setContentText(messageBody)
            .setColor(ContextCompat.getColor(this, R.color.colorPrimary))
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setPriority(Notification.PRIORITY_MAX)
            .setContentIntent(pendingIntent)
            .setDefaults(NotificationCompat.DEFAULT_VIBRATE)
        notificationBuilder.setContentIntent(pendingIntent)
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Notification",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }
        val notificationId = Random().nextInt(60000)
        notificationManager.notify(
            notificationId /*ID of notification*/,
            notificationBuilder.build()
        )
    }

    override fun onCreate() {
        super.onCreate()
        val `in` = Intent(this, PatientMainActivity::class.java)
        `in`.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        //        startActivity(in);
    }

    /*

    private void sendNotification(String title, String messageBody, RemoteMessage.Notification notification) {

        String page = "";
        String page_sub = "";
        String group_id = "";
        String channel_id = "";
        String campaign_id = "";
        */
    /*if (data.size() > 0) {
            page = data.get("page");
            page_sub = data.get("page_sub");
            group_id = data.get("group_id");
            channel_id = data.get("channel_id");
            campaign_id = data.get("campaign_id");
        }*/
    /*


        PreferenceManager preferenceManager = new PreferenceManager(this);
        int count = preferenceManager.getNotificationCount();
        count++;
        preferenceManager.setNotificationCount(count);

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("isFrom", "notification");
        intent.putExtra("page", page);
        intent.putExtra("page_sub", page_sub);
        intent.putExtra("group_id", group_id);
        intent.putExtra("channel_id", channel_id);
        intent.putExtra("campaign_id", campaign_id);
        intent.putExtra("isFrom", "notification");
        intent.putExtra("isFrom2", "notification");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(intent);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 */
    /* Request code */ /*
, intent,
                PendingIntent.FLAG_UPDATE_CURRENT); //FLAG_ONE_SHOT

        String channelId = "";
        if (page.equalsIgnoreCase("friend")) {
            channel_id = "channelfriendid";
        } else if (page.equalsIgnoreCase("group")) {
            channel_id = "channelgroupid";
        } else if (page.equalsIgnoreCase("channel")) {
            channel_id = "channelid";
        } else {
            channel_id = getString(R.string.default_notification_channel_id);
        }

        //String channelId = getString(R.string.default_notification_channel_id);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.mipmap.ic_launcher_foreground)
                        .setContentTitle(AppUtils.isEmpty(title) ? getString(R.string.fcm_message) : title)
                        .setContentText(messageBody)
//                        .setColor(ContextCompat.getColor(this, R.color.colorPrimary))
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setPriority(Notification.PRIORITY_MAX)
//                        .setContentIntent(pendingIntent)
                        .setDefaults(NotificationCompat.DEFAULT_VIBRATE);

        if (!TextUtils.isEmpty(page))
            if (!page.equalsIgnoreCase("registration"))
                notificationBuilder.setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Notification",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        int notificationId = new Random().nextInt(60000);
        notificationManager.notify(notificationId  */
    /*ID of notification*/ /*
, notificationBuilder.build());

    }

*/
    //Simple method for image downloading
    fun getBitmapFromUrl(imageUrl: String?): Bitmap? {
        return try {
            val url = URL(imageUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val input = connection.inputStream
            BitmapFactory.decodeStream(input)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    companion object {
        private const val TAG = "MyFirebaseMsgService"
    }
}