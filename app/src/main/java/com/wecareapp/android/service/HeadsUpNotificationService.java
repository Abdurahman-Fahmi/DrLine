package com.wecareapp.android.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.TaskStackBuilder;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.wecareapp.android.R;
import com.wecareapp.android.ui.activity.SplashActivity;
import com.wecareapp.android.utilities.SoundPoolManager;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;
import java.util.Random;

public class HeadsUpNotificationService extends Service {
    private String CHANNEL_ID = "VoipChannel";
    private String CHANNEL_NAME = "Voip Channel";
    private static final int NOTIF_CALL_ID = 1314;
    public static final String ACTION_START_FOREGROUND_SERVICE = "ACTION_START_FOREGROUND_SERVICE";

    @Override
    public void onCreate() {
        super.onCreate();
//        startS(null);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

//        Bundle data = new Bundle();
//        data.putString("type", "video_call");
//        data.putString("channelName", "channelName");
//        data.putString("agoraToken", "agoraToken");
        Bundle data = null;
        if (intent != null && intent.getExtras() != null) {
            data = intent.getBundleExtra(ConstantApp.FCM_DATA_KEY);
        }

        if (data == null)
            data = new Bundle();

//        data.putString("type", "video_call");
//        data.putString("channelName", "channelName");
//        data.putString("agoraToken", "agoraToken");
        Uri urie = RingtoneManager.getActualDefaultRingtoneUri(
                getApplicationContext(), RingtoneManager.TYPE_RINGTONE);
        createChannel();
//        String input = intent.getStringExtra("inputExtra");
//        Intent notificationIntent = new Intent(this, PatientMainActivity.class);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this,
//                0, notificationIntent, 0);

//        val customView = RemoteViews(packageName, R.layout.custom_call_notification);

        SoundPoolManager.getInstance(this).playRinging();
        RemoteViews customView = new RemoteViews(getPackageName(), R.layout.layout_custom_call_notification);

        Intent receiveCallAction = new Intent(this, HeadsUpNotificationActionReceiver.class);
        receiveCallAction.putExtra(ConstantApp.CALL_RESPONSE_ACTION_KEY, ConstantApp.CALL_RECEIVE_ACTION);
        receiveCallAction.putExtra(ConstantApp.FCM_DATA_KEY, data);
        receiveCallAction.setAction("RECEIVE_CALL");

        Intent cancelCallAction = new Intent(this, HeadsUpNotificationActionReceiver.class);
        cancelCallAction.putExtra(ConstantApp.CALL_RESPONSE_ACTION_KEY, ConstantApp.CALL_CANCEL_ACTION);
        cancelCallAction.putExtra(ConstantApp.FCM_DATA_KEY, data);
        cancelCallAction.setAction("CANCEL_CALL");

        Intent splashIntent = new Intent(this, SplashActivity.class);
        splashIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(splashIntent);

        // pending intent req code changed
        int pendingIntentId = new Random().nextInt(60000);

        PendingIntent receiveCallPendingIntent = PendingIntent.getBroadcast(this, 1200, receiveCallAction, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent cancelCallPendingIntent = PendingIntent.getBroadcast(this, 1201, cancelCallAction, PendingIntent.FLAG_UPDATE_CURRENT);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this,  /* Request code */pendingIntentId, intent,
//                PendingIntent.FLAG_ONE_SHOT); //FLAG_ONE_SHOT

        String callType;
        if (data.getString("type").contentEquals("audio_call")) {
            callType = "Incoming Voice Call";
        } else {
            callType = "Incoming Video Call";
        }
//        String img = "https://image.shutterstock.com/image-photo/majestic-sunset-mountains-landscape-wonderful-600w-1487897981.jpg"; data.getString("callee_image");

        Bitmap bitmap = null;
        byte[] bytes = data.getByteArray("bitmapbytes");
        bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        bitmap = getCircleBitmap(bitmap);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
//                .setContentTitle("remoteUserName")
//                .setContentText(input)
                .setSmallIcon(R.drawable.ic_app_icon)
                .setLargeIcon(bitmap)
//                .setContentIntent(pendingIntent)
                .setSound(urie)
                .setContentText(callType)
                .setContentTitle(data.getString("callee_name"))
//                    .setStyle(new
//                            NotificationCompat.BigTextStyle()
//                            .bigText("long notification content")
//                    )
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .setExtras(data)
//                .setWhen(0)
                .setOngoing(true)
                .addAction(R.drawable.ic_call_green, "Receive Call", receiveCallPendingIntent)
                .addAction(R.drawable.ic_call_red, "Cancel call", cancelCallPendingIntent)
                .setAutoCancel(true)
//                    .setSound(urie)
                // Apply the media style template
//                    .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
//                            .setShowActionsInCompactView(1 /* #1: pause button \*/))
//                            .setMediaSession(mediaSession.getSessionToken()))
//                    .setStyle(new NotificationCompat.MessagingStyle("k"))
//                            .setShowActionsInCompactView(1 /* #1: pause button \*/))
//                            .setMediaSession(mediaSession.getSessionToken()))
//                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
//                .setFullScreenIntent(receiveCallPendingIntent, true);
//                        .setSound(Uri.parse("android.resource://" + this.getPackageName() + "/" + R.raw.voip_ringtone))

        // Changing Default mode of notification
//            notificationBuilder.setDefaults(Notification.DEFAULT_VIBRATE);
        notificationBuilder.setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
        notificationBuilder.setVibrate(new long[]{1000, 1000, 1000, 1000, 1000});

        notificationBuilder.setOngoing(true);
//            NotificationCompat.DecoratedCustomViewStyle v = new NotificationCompat.DecoratedCustomViewStyle();
//            v.makeBigContentView(customView);
//            notificationBuilder.setStyle(v);
//            notificationBuilder.setCustomContentView(customView);
//            notificationBuilder.setCustomBigContentView(customView);
        Notification incomingCallNotification = notificationBuilder.build();

        if (incomingCallNotification != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                startForeground(NOTIF_CALL_ID, incomingCallNotification, ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL | ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION);
            else {
                startForeground(NOTIF_CALL_ID, incomingCallNotification);
            }
        }

        new Handler().postDelayed(() -> {
            SoundPoolManager.getInstance(this).stopRinging();
            stopSelf();
        }, 3 * 60 * 1000);    //will stop service after 10 seconds

//        startS(intent);

        return START_STICKY;
    }

    //Simple method for image downloading
    public Bitmap getBitmapFromUrl(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Bitmap getCircleBitmap(Bitmap bitmap) {
        Bitmap output;
        Rect srcRect, dstRect;
        float r;
        final int width = bitmap.getWidth();
        final int height = bitmap.getHeight();

        if (width > height) {
            output = Bitmap.createBitmap(height, height, Bitmap.Config.ARGB_8888);
            int left = (width - height) / 2;
            int right = left + height;
            srcRect = new Rect(left, 0, right, height);
            dstRect = new Rect(0, 0, height, height);
            r = height / 2;
        } else {
            output = Bitmap.createBitmap(width, width, Bitmap.Config.ARGB_8888);
            int top = (height - width) / 2;
            int bottom = top + width;
            srcRect = new Rect(0, top, width, bottom);
            dstRect = new Rect(0, 0, width, width);
            r = width / 2;
        }

        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawCircle(r, r, r, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, srcRect, dstRect, paint);

        bitmap.recycle();

        return output;
    }

    private void startS(Intent intent) {
        Bundle data = null;
        if (intent != null && intent.getExtras() != null) {
            data = intent.getBundleExtra(ConstantApp.FCM_DATA_KEY);
        }
        try {
            Intent receiveCallAction = new Intent(this, HeadsUpNotificationActionReceiver.class);
            receiveCallAction.putExtra(ConstantApp.CALL_RESPONSE_ACTION_KEY, ConstantApp.CALL_RECEIVE_ACTION);
            receiveCallAction.putExtra(ConstantApp.FCM_DATA_KEY, data);
            receiveCallAction.setAction("RECEIVE_CALL");

            Intent cancelCallAction = new Intent(this, HeadsUpNotificationActionReceiver.class);
            cancelCallAction.putExtra(ConstantApp.CALL_RESPONSE_ACTION_KEY, ConstantApp.CALL_CANCEL_ACTION);
            cancelCallAction.putExtra(ConstantApp.FCM_DATA_KEY, data);
            cancelCallAction.setAction("CANCEL_CALL");

            PendingIntent receiveCallPendingIntent = PendingIntent.getBroadcast(this, 1200, receiveCallAction, PendingIntent.FLAG_UPDATE_CURRENT);
            PendingIntent cancelCallPendingIntent = PendingIntent.getBroadcast(this, 1201, cancelCallAction, PendingIntent.FLAG_UPDATE_CURRENT);
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                createNotificationChannel("my_service", "My Background Service");
//            } else {
            createChannel();
            NotificationCompat.Builder notificationBuilder = null;
            if (data != null) {

//                Uri urie = RingtoneManager.getActualDefaultRingtoneUri(
//                        getApplicationContext(), RingtoneManager.TYPE_RINGTONE);
//
                Uri urie = Uri.parse("android.resource://" + this.getPackageName() + "/" + R.raw.android_ring);

                notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setContentText(data.getString("remoteUserName"))
                        .setContentTitle("Incoming Voice Call")
                        .setSmallIcon(R.drawable.ic_app_icon)
                        .setPriority(NotificationManager.IMPORTANCE_HIGH)
                        .setCategory(NotificationCompat.CATEGORY_CALL)
                        .addAction(R.drawable.ic_call_green, "Receive Call", receiveCallPendingIntent)
                        .addAction(R.drawable.ic_call_red, "Cancel call", cancelCallPendingIntent)
                        .setAutoCancel(true)
//                        .setSound(Uri.parse("android.resource://" + this.getPackageName() + "/" + R.raw.voip_ringtone))
                        .setSound(urie)
//                        .setDefaults( DEFAULT_SOUND | DEFAULT_VIBRATE)
                        .setFullScreenIntent(receiveCallPendingIntent, true);
            }

            Notification incomingCallNotification = null;
            if (notificationBuilder != null) {
                incomingCallNotification = notificationBuilder.build();
            }

            if (incomingCallNotification != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                    startForeground(NOTIF_CALL_ID, incomingCallNotification, ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL | ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION);
                else
                    startForeground(NOTIF_CALL_ID, incomingCallNotification);
            }
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }


//        (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
//            newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, tag).apply {
//                acquire()
//            }
//        }
    }


    /*
    Create noticiation channel if OS version is greater than or eqaul to Oreo
    */
    public void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            Uri urie = RingtoneManager.getActualDefaultRingtoneUri(
                    getApplicationContext(), RingtoneManager.TYPE_RINGTONE);
//            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, urie);

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            channel.setLightColor(Color.BLUE);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            channel.setDescription("Call Notifications");
            channel.setVibrationPattern(new long[]{1000, 1000, 1000, 1000, 1000});
            channel.enableVibration(true);
            channel.setSound(Uri.parse("android.resource://" + this.getPackageName() + "/" + R.raw.android_ring),
                    new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .setLegacyStreamType(AudioManager.STREAM_RING)
                            .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION).build());

//            channel.setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 });
//            channel.setDefaults(DEFAULT_SOUND | DEFAULT_VIBRATE)
            // Creating an Audio Attribute
//            AudioAttributes audioAttributes = new AudioAttributes.Builder()
//                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
//                    .setLegacyStreamType(AudioManager.STREAM_RING)
//                    .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
//                    .build();

            // Creating an Audio Attribute
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build();
//            channel.setSound(urie, audioAttributes);
//            channel.setSound(Settings.System.DEFAULT_NOTIFICATION_URI, audioAttributes);
            Objects.requireNonNull(this.getSystemService(NotificationManager.class)).createNotificationChannel(channel);
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private void createNotificationChannel(String channelId, String channelName) {
        Intent resultIntent = new Intent(this, MainActivity.class);
// Create the TaskStackBuilder and add the intent, which inflates the back stack
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT);
        channel.setLightColor(Color.BLUE);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(channel);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId);
        Notification notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("App is running in background")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setContentIntent(resultPendingIntent) //intent
                .build();
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(1, notificationBuilder.build());
        startForeground(1, notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private boolean isAppVisible() {
        return ProcessLifecycleOwner
                .get()
                .getLifecycle()
                .getCurrentState()
                .isAtLeast(Lifecycle.State.STARTED);
    }
}