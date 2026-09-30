package com.tech.eskool.notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.tech.eskool.R;
import com.tech.eskool.StartActivity;
import com.tech.eskool.service.SessionManager;
import com.tech.eskool.util.AppLifecycleObserver;

import java.util.Random;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private final String CHANNEL_ID = "ADMIN_CHANNEL";

//    @Override
//    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
//        Log.i("NOTIFICATION", remoteMessage.toString());
//        Log.i("NOTIFICATION", "Is Foreground? "+AppLifecycleObserver.isInForeground());
//        if (AppLifecycleObserver.isInForeground()) {
//            showAlertDialog(remoteMessage.getData().get("title"), remoteMessage.getData().get("message"));
//        } else {
//            createNotification(remoteMessage);
//        }
//    }
//
//    private void showAlertDialog(String title, String message) {
//        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
//        new SessionManager(this).getAlertWithOk(message);
//    }
//
//    protected void createNotification(RemoteMessage remoteMessage) {
//        final Intent intent = new Intent(this, StartActivity.class);
//        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//        int notificationID = new Random().nextInt(3000);
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            setupChannels(notificationManager);
//        }
//
//        // Set flags to launch the activity with the correct intent behavior
//        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
//        intent.putExtra("title", remoteMessage.getData().get("title"));
//        intent.putExtra("message", remoteMessage.getData().get("message"));
//
//        // PendingIntent to launch the activity
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
//                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
//
//        // Prepare the large icon for the notification
//        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.mipmap.school_logo);
//
//        // Prepare the notification sound
//        Uri notificationSoundUri = Uri.parse("android.resource://" + getApplicationContext().getPackageName() + "/" + R.raw.me_too);
//
//        // Build the notification
//        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, ADMIN_CHANNEL_ID)
//                .setSmallIcon(R.mipmap.school_logo)
//                .setLargeIcon(largeIcon)
//                .setContentTitle(remoteMessage.getData().get("title"))
//                .setContentText(remoteMessage.getData().get("message"))
//                .setAutoCancel(true)
//                .setSound(notificationSoundUri)
//                .setContentIntent(pendingIntent);
//
//        // Set notification color to match your app color template
//        notificationBuilder.setColor(getResources().getColor(R.color.purple_900));
//
//        // Set custom notification sound
//        Uri alarmSound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + getApplicationContext().getPackageName() + "/raw/notification");
//        notificationBuilder.setSound(alarmSound);
//
//        assert notificationManager != null;
//        notificationManager.notify(notificationID, notificationBuilder.build());
//    }
//
//
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setupChannels(NotificationManager notificationManager) {
        CharSequence adminChannelName = "ADMIN_CHANNEL";
        String adminChannelDescription = String.valueOf(R.string.app_name);

        NotificationChannel adminChannel;
        adminChannel = new NotificationChannel(CHANNEL_ID, adminChannelName, NotificationManager.IMPORTANCE_HIGH);
        adminChannel.setDescription(adminChannelDescription);
        adminChannel.enableLights(true);
        adminChannel.setLightColor(Color.RED);
        adminChannel.enableVibration(true);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(adminChannel);
        }
    }

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        Log.i("NOTIFICATION", s);
        SessionManager sessionManager = new SessionManager(getApplicationContext());
        sessionManager.setFcmToken(s);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d("TAG", "From: " + remoteMessage.getFrom());

        createNotification(remoteMessage);
//        if (!remoteMessage.getData().isEmpty()) {
//            Log.d("TAG", "Message data payload: " + remoteMessage.getData());
//            sendNotification(remoteMessage.getData().get("message"));
//        }
//
//
//        if (remoteMessage.getNotification() != null) {
//            Log.d("TAG", "Message Notification Body: " + remoteMessage.getNotification().getBody());
//            sendNotification(remoteMessage.getNotification().getBody());
//        }
    }







    private void createNotification(RemoteMessage remoteMessage) {
        if(AppLifecycleObserver.isInForeground()) {
            final Intent intent = new Intent(this, StartActivity.class);
            intent.putExtra("title", remoteMessage.getData().get("title"));
            intent.putExtra("message", remoteMessage.getData().get("message"));

            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            int notificationID = new Random().nextInt(3000);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                setupChannels(notificationManager);
            }

            Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
            Uri notificationSoundUri = Uri.parse("android.resource://" + getApplicationContext().getPackageName() + "/" + R.raw.me_too);

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_launcher) // Ensure this is a valid small icon
                    .setLargeIcon(largeIcon)
                    .setContentTitle(remoteMessage.getData().get("title"))
                    .setContentText(remoteMessage.getData().get("message"))
                    .setAutoCancel(true)
                    .setSound(notificationSoundUri)
                    .setContentIntent(pendingIntent)
                    .setColor(getResources().getColor(R.color.purple_900));

            assert notificationManager != null;
            notificationManager.notify(notificationID, notificationBuilder.build());
        }else{
            
        }
    }


    private void sendNotification(String messageBody) {
        Intent intent = new Intent(this, StartActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);


        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setContentTitle("FCM Message")
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSmallIcon(R.mipmap.ic_launcher) // Ensure this is a valid small icon
                        .setContentIntent(pendingIntent);


        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "FCM Notifications", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }


        notificationManager.notify(0, notificationBuilder.build());
    }


}
