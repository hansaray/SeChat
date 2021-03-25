package com.simpla.sechat.Extensions;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.simpla.sechat.MessageActivity;
import com.simpla.sechat.NavigateActivity;
import com.simpla.sechat.R;

import java.util.Objects;

public class MyNotificationService extends FirebaseMessagingService {

    public static final String CHANNEL_ID = "default_channel_id";

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "channel"; //getString(R.string.channel_name);
            String description = "description";  //getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.setShowBadge(true);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            assert notificationManager != null;
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        createNotificationChannel();
        String title = Objects.requireNonNull(remoteMessage.getNotification()).getTitle();
        String body = remoteMessage.getNotification().getBody();
        String data = remoteMessage.getData().get("clickedUserId");
        if (title != null && title.equals("New Message")) {
            if (!MessageActivity.activityControl && !NavigateActivity.activityControl) {
                showNotificationMessage(title, body, data);
            }
        }
    }

    private void showNotificationMessage(String title, String body, String data){
        Intent pendingIntent = new Intent(this,NavigateActivity.class);
        pendingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        pendingIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        pendingIntent.putExtra("clickedUserId",data);

        PendingIntent pendingIntent1 = PendingIntent.getActivity(this,10,pendingIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        Notification builder = new NotificationCompat.Builder(this,CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_chat_splash)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.drawable.ic_chat_splash))
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentTitle(title)
                .setContentText(body)
                .setColor(getResources().getColor(R.color.mainBlue))
                .setAutoCancel(true)
                .setContentIntent(pendingIntent1)
                .build();
        @SuppressLint("ServiceCast")
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert notificationManager != null;
        notificationManager.notify((int) System.currentTimeMillis(),builder );
    }

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        writeToStorage(s);
    }

    private void writeToStorage(String token){
        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .child("fcm_token").setValue(token);
        }
    }
}
