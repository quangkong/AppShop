package com.example.appshop;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.example.appshop.activity.seller.OrderDetailsSeller;
import com.example.appshop.activity.user.OrderDatailsUser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Random;

public class MyFirebaseMessaging extends FirebaseMessagingService {

    private static final String NOTIFICATION_CHANNEL_ID = "MY_NOTIFICATION_CHANNEL_ID";

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        //all notifications will be received here
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        //get data from notifition
        String notificationType = remoteMessage.getData().get("notificationType");
        if (notificationType.equals("NewOrder")){
            String buyerUid = remoteMessage.getData().get("buyerUid");
            String notificationTitle = remoteMessage.getData().get("notificationTitle");
            String sellerUid = remoteMessage.getData().get("sellerUid");
            String orderId = remoteMessage.getData().get("orderId");
            String notificationDescription = remoteMessage.getData().get("notificationMessage");
            if (firebaseUser != null && firebaseAuth.getUid().equals(sellerUid)){
                //user is signed in and is same user to which notification is sent
                showNotification(buyerUid, notificationTitle, sellerUid, orderId, notificationDescription, notificationType);

            }
        }
        if (notificationType.equals("OrderStatusChanged")){
            String buyerUid = remoteMessage.getData().get("buyerUid");
            String notificationTitle = remoteMessage.getData().get("notificationTitle");
            String sellerUid = remoteMessage.getData().get("sellerUid");
            String orderId = remoteMessage.getData().get("orderId");
            String notificationDescription = remoteMessage.getData().get("notificationMessage");
            if (firebaseUser != null && firebaseAuth.getUid().equals(buyerUid)){
                //user is signed in and is same user to which notification is sent
                showNotification(buyerUid, notificationTitle, sellerUid, orderId, notificationDescription, notificationType);
            }
        }
        super.onMessageReceived(remoteMessage);
    }

    private void showNotification(String buyerUid, String notificationTitle, String sellerUid, String orderId, String notificationDescription, String notificationType){
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        //id for notification, random
        int notificationId = new Random().nextInt(3000);

        //check if android version is Oreo/0 or above
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            setupNotificationChannel(notificationManager);
        }

        //handle notification click, start order activity
        Intent intent = null;
        if(notificationType.equals("NewOrder")){
            //open OrderDetailsSeller
            intent = new Intent(this, OrderDetailsSeller.class);
            intent.putExtra("orderBy", buyerUid);
            intent.putExtra("orderId", orderId);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        }
        else if (notificationType.equals("OrderStatusChanged")){
            //open OrderDatailsUser
            intent = new Intent(this, OrderDatailsUser.class);
            intent.putExtra("orderId", orderId);
            intent.putExtra("orderTo", sellerUid);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        //Large icon
        Bitmap lareIcon = BitmapFactory.decodeResource(getResources(), R.drawable.logoo);

        //sound of notification
        Uri notificationSounUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        notificationBuilder.setSmallIcon(R.drawable.logoo)
                .setLargeIcon(lareIcon)
                .setContentTitle(notificationTitle)
                .setContentText(notificationDescription)
                .setSound(notificationSounUri)
                .setAutoCancel(true) //cancel/dismiss when clicked
                .setContentIntent(pendingIntent);

        //show notification
        notificationManager.notify(notificationId, notificationBuilder.build());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setupNotificationChannel(NotificationManager notificationManager) {
        CharSequence charSequence = "Some Sample Text";
        String channelDescription = "Channel Description here";

        NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, charSequence, NotificationManager.IMPORTANCE_HIGH);
        notificationChannel.setDescription(channelDescription);
        notificationChannel.enableLights(true);
        notificationChannel.setLightColor(Color.RED);
        notificationChannel.enableVibration(true);
        if(notificationManager != null){
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }
}
