package com.example.music_redo.widget;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.widget.RemoteViews;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.music_redo.MusicList;
import com.example.music_redo.R;

public class PlayNotification extends Service {
//    Activity myActivity;
//    Context myContext;

    NotificationCompat.Builder builder;
    NotificationChannel channel;
    NotificationManager manager;
    RemoteViews remoteViews;

    // 用于部件交互
    static final int MODE_START = 0;
    static final int MODE_PAUSE = 1;
    static final int MODE_NEXT = 2;
    static final int MODE_PREV = 3;
    static final int MODE_PLAY= 3;

    @Override
    public void onCreate() {
        super.onCreate();
        initData();
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

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        initView();

        MusicList.infoLog("notification on start command");
        int cmd_mode = intent.getIntExtra("mode", -1);
        switch (cmd_mode) {
            case MODE_START:// 初次启动
                remoteViews.setImageViewResource(R.id.button_play, R.drawable.player_pause);
                builder.setContent(remoteViews);
                startForeground(100, builder.build());
                break;
        }

        return START_STICKY;// TODO 防止被杀
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
