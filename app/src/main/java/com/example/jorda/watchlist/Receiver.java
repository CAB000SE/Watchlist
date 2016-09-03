package com.example.jorda.watchlist;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class Receiver extends BroadcastReceiver {

    public static String NOTIFICATION = "notification";

    public void onReceive(Context context, Intent intent) {

        //opens the notification manager
        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);


        Notification notMain = intent.getParcelableExtra(NOTIFICATION);

        int intNotificationID = AddPage.intNotificationID;

        //finally uses the notification
        notificationManager.notify(intNotificationID, notMain);

    }
}