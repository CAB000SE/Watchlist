package com.example.jorda.watchlist;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Random;

public class Receiver extends BroadcastReceiver {

    public static String NOTIFICATION = "notification";


    public void onReceive(Context context, Intent intent) {

        //opens the notification manager
        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);


        Notification notification = intent.getParcelableExtra(NOTIFICATION);

        //generates another id to use - hte likely hood of 2 ids ever being the same is tiny
        int notificationID2 = AddPage.notificationID;

        //finally uses the notification
        notificationManager.notify(notificationID2, notification);

    }
}