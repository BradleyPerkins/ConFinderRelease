package com.bradperkins.confinder.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import com.bradperkins.confinder.R;
import com.bradperkins.confinder.activities.ConDetailActivity;
import com.bradperkins.confinder.activities.MainActivity;
import com.bradperkins.confinder.objects.Con;
import com.bradperkins.confinder.objects.NotificationCons;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.NotificationTarget;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class NotificationReceiver extends BroadcastReceiver {

    private final static String CHANNEL_ID = "con_notify";

    private int pos;
    //Favorites
    private ArrayList<Con> notifyList;
    private ArrayList<NotificationCons> nCons;


    @Override
    public void onReceive(Context context, Intent intent) {

        SharedPreferences notifyPref = context.getSharedPreferences("NOTIFY_PREF" , context.MODE_PRIVATE);
        boolean isNotified = notifyPref.getBoolean("NOTIFY_PREF_BOOL", false);

        if (isNotified) {
            if (intent.getAction().equals("CON_NOTIFICATION")) {

                String dateStr = new SimpleDateFormat("MMddyy").format(new Date());
                SimpleDateFormat sdf = new SimpleDateFormat("MMddyy");
                Calendar c = Calendar.getInstance();
                try {
                    c.setTime(sdf.parse(dateStr));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                c.add(Calendar.DATE, 1);
                dateStr = sdf.format(c.getTime());

                //Replace the double in if statement with date
                double date = Double.parseDouble(dateStr);
                ArrayList<Con> conList = DataCache.loadFavConData(context);

//            notifyList = new ArrayList<>();
//            nCons = new ArrayList<>();
//
                //List of cons that match date
                for (int i = 0; i < conList.size(); i++) {
                    if (conList.get(i).getId() == date) {
                        Log.i("Notify", "1: " + conList.get(i).getTitle());

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            CharSequence name = "channel_name)";
                            String description = "channel_description)";
                            int importance = NotificationManager.IMPORTANCE_DEFAULT;
                            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
                            channel.setDescription(description);
                            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
                            notificationManager.createNotificationChannel(channel);
                        }

                        Intent detailIntent = new Intent(context, ConDetailActivity.class);
                        detailIntent.putExtra(MainActivity.LIST_EXTRA, conList);
                        detailIntent.putExtra(MainActivity.POSITION_EXTRA, i);
                        PendingIntent pendingIntent = PendingIntent.getActivity(context, 100, detailIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                        final RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.custom_notification);
                        remoteViews.setImageViewResource(R.id.remoteview_notification_icon, R.mipmap.ic_launcher_round);

                        remoteViews.setTextViewText(R.id.remoteview_notification_headline, "Convention Tomorrow");
                        remoteViews.setTextViewText(R.id.remoteview_notification_short_message, conList.get(i).getTitle());
                        remoteViews.setTextViewText(R.id.remoteview_notification_details, conList.get(i).getDate());

                        NotificationCompat.Builder builder =
                                new NotificationCompat.Builder(context, CHANNEL_ID)
                                        .setSmallIcon(R.mipmap.ic_launcher_foreground)
                                        .setContentTitle("Convention Tomorrow")
                                        .setContentText(conList.get(i).getTitle())
                                        .setContent(remoteViews)
                                        .setContentIntent(pendingIntent)
                                        .setAutoCancel(true)
                                        .setCustomBigContentView(remoteViews)
                                        .setPriority(NotificationCompat.PRIORITY_MIN);

                        final Notification notification = builder.build();
                        NotificationTarget notificationTarget;

                        int NOTIFICATION_ID = 0x01002;
                        notificationTarget = new NotificationTarget(
                                context,
                                R.id.remoteview_notification_icon,
                                remoteViews,
                                notification,
                                NOTIFICATION_ID);

                        NotificationTarget notificationIconTarget = new NotificationTarget(
                                context,
                                R.id.remoteview_notification_icon_logo,
                                remoteViews,
                                notification,
                                NOTIFICATION_ID);

                        Glide.with(context.getApplicationContext()).asBitmap().load(conList.get(i).getImage()).into(notificationTarget);
                        Glide.with(context.getApplicationContext()).asBitmap().load(R.mipmap.ic_launcher_foreground).into(notificationIconTarget);
                    }
                }
            }
        }
    }


}
