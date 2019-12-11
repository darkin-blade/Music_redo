package com.example.music_redo.widget;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.music_redo.MusicList;
import com.example.music_redo.R;

public class PlayNotification extends Service {
//    Activity myActivity;
//    Context myContext;

    String packageName;

    NotificationCompat.Builder builder;
    NotificationChannel channel;
    NotificationManager manager;
    RemoteViews remoteViews;

    @Override
    public void onCreate() {
        super.onCreate();
        initData();
        initView();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void initData() {
        if (remoteViews != null) {// TODO
            remoteViews.removeAllViews(R.layout.play_notification);
            remoteViews = null;
        }
        remoteViews = new RemoteViews(this.getPackageName(), R.layout.play_notification);
    }

    public void initView() {
        if (channel == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {// TODO
                channel = new NotificationChannel("default", "default", NotificationManager.IMPORTANCE_DEFAULT);
                manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                manager.createNotificationChannel(channel);
            }
        }

        if (builder == null) {
            builder = new NotificationCompat.Builder(this, "default")
                    .setSmallIcon(R.drawable.ic_launcher_background)
                    .setContent(remoteViews)
                    .setOnlyAlertOnce(false)// TODO
                    .setAutoCancel(false)// TODO
                    .setOngoing(true);// TODO
        }

    }
}
