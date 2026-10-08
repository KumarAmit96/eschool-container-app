package com.tech.eskool.notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.tech.eskool.Log;
import com.tech.eskool.R;
import com.tech.eskool.StartActivity;
import com.tech.eskool.service.SessionManager;

import java.util.Map;
import java.util.Random;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FCM";

    // A channel's sound can't be changed after it is created, so the custom sound needs a new id.
    private static final String CHANNEL_ID = "ESKOOL_ALERTS";
    private static final String OLD_CHANNEL_ID = "ADMIN_CHANNEL";

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        new SessionManager(getApplicationContext()).setFcmToken(token);
    }

    /**
     * Called for data messages in foreground and background, and for notification messages
     * while the app is in the foreground. (Notification messages in the background are shown
     * by the system using the default icon/colour from the manifest.)
     */
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        Log.d(TAG, "Message from: " + remoteMessage.getFrom());

        Map<String, String> data = remoteMessage.getData();
        String title = data.get("title");
        String message = data.get("message");
        RemoteMessage.Notification notification = remoteMessage.getNotification();
        if (notification != null) {
            if (title == null) title = notification.getTitle();
            if (message == null) message = notification.getBody();
        }
        if (title == null) title = getString(R.string.app_name);
        if (message == null || message.isEmpty()) {
            Log.w(TAG, "Message without text ignored");
            return;
        }
        showNotification(title, message);
    }

    private void showNotification(String title, String message) {
        if (!NotificationManagerCompat.from(this).areNotificationsEnabled()) {
            Log.w(TAG, "Notifications are disabled by the user");
            return;
        }
        ensureChannel(this);

        int notificationId = new Random().nextInt(100000);

        // Opens the app; StartActivity passes title/message on to the dashboard, which shows them.
        Intent intent = new Intent(this, StartActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("title", title);
        intent.putExtra("message", message);
        // Unique request code per notification, otherwise every notification opens the latest text.
        PendingIntent pendingIntent = PendingIntent.getActivity(this, notificationId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_notification)
                .setLargeIcon(largeIcon)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setSound(soundUri(this)) // used below Android 8; the channel's sound is used above
                .setContentIntent(pendingIntent)
                .setColor(ContextCompat.getColor(this, R.color.brand_accent));

        try {
            NotificationManagerCompat.from(this).notify(notificationId, builder.build());
        } catch (SecurityException e) {
            // POST_NOTIFICATIONS revoked between the check above and now.
            Log.w(TAG, "Notification permission missing", e);
        }
    }

    private static Uri soundUri(Context context) {
        return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + context.getPackageName() + "/" + R.raw.me_too);
    }

    /** Creates the notification channel; called at app start (MyApplication) and before each notification. */
    public static void ensureChannel(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null || manager.getNotificationChannel(CHANNEL_ID) != null) return;

        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "School notifications",
                NotificationManager.IMPORTANCE_HIGH);
        channel.setDescription(context.getString(R.string.app_name) + " alerts");
        channel.enableLights(true);
        channel.setLightColor(Color.RED);
        channel.enableVibration(true);
        channel.setSound(soundUri(context), new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build());
        manager.createNotificationChannel(channel);
        manager.deleteNotificationChannel(OLD_CHANNEL_ID);
    }
}
