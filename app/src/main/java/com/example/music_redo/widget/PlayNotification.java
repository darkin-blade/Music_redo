package com.example.music_redo.widget;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
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
    Context myContext;

    NotificationCompat.Builder builder;
    NotificationChannel channel;
    NotificationManager manager;
    RemoteViews remoteViews;

    // 用于部件交互
    static final int MODE_START = 0;
    static final int MODE_PAUSE = 1;
    static final int MODE_NEXT = 2;
    static final int MODE_PREV = 3;
    static final int MODE_PLAY = 4;
    static final int MODE_CLOSE = 5;
    static final int MODE_OPEN = 5;

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

        int cmd_mode = intent.getIntExtra("mode", -1);
        boolean from_notification = intent.getBooleanExtra("fromNotification", false);
        switch (cmd_mode) {
            case MODE_START:// 从内部启动
                initPause();
                break;
            case MODE_PAUSE:
                if (from_notification == true) {// 从通知栏暂停
                    MusicList.playTime.pause();
                }
                initPlay();// 更改为播放
                break;
            case MODE_PLAY:
                if (from_notification == true) {// 从通知栏播放
                    MusicList.playTime.play(0);
                }
                initPause();// 更改为暂停
                break;
            case MODE_NEXT:
                initPause();
                MusicList.infoLog("notification next");
                if (from_notification == true) {
                    MusicList.playTime.next();
                }
                break;
            case  MODE_PREV:
                initPause();
                if (from_notification == true) {
                    MusicList.playTime.prev();
                }
                break;
            case MODE_CLOSE:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    manager.deleteNotificationChannel("default");
                    channel = null;
                } else {
                    stopForeground(true);
                }
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

        initNext();
        initPrev();
        initClose();
        initOpen();
    }

    public void initPlay() {
        remoteViews.setImageViewResource(R.id.button_play, R.drawable.player_play);
        builder.setContent(remoteViews);

        Intent intent = new Intent(this, PlayNotification.class);
        intent.putExtra("mode", MODE_PLAY);
        intent.putExtra("fromNotification", true);
        PendingIntent pendingIntent = PendingIntent.getService(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        remoteViews.setOnClickPendingIntent(R.id.button_play, pendingIntent);

        startForeground(100, builder.build());
    }

    public void initPause() {
        remoteViews.setImageViewResource(R.id.button_play, R.drawable.player_pause);
        builder.setContent(remoteViews);

        Intent intent = new Intent(this, PlayNotification.class);
        intent.putExtra("mode", MODE_PAUSE);
        intent.putExtra("fromNotification", true);
        PendingIntent pendingIntent = PendingIntent.getService(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        remoteViews.setOnClickPendingIntent(R.id.button_play, pendingIntent);

        startForeground(100, builder.build());
    }

    public void initNext() {
        Intent intent = new Intent(this, PlayNotification.class);
        intent.putExtra("mode", MODE_NEXT);
        intent.putExtra("fromNotification", true);
        PendingIntent pendingIntent = PendingIntent.getService(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        remoteViews.setOnClickPendingIntent(R.id.button_next, pendingIntent);
    }

    public void initPrev() {
        Intent intent = new Intent(this, PlayNotification.class);
        intent.putExtra("mode", MODE_PREV);
        intent.putExtra("fromNotification", true);
        PendingIntent pendingIntent = PendingIntent.getService(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        remoteViews.setOnClickPendingIntent(R.id.button_prev, pendingIntent);
    }

    public void initClose() {
        Intent intent = new Intent(this, PlayNotification.class);
        intent.putExtra("mode", MODE_CLOSE);
        intent.putExtra("fromNotification", true);
        PendingIntent pendingIntent = PendingIntent.getService(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        remoteViews.setOnClickPendingIntent(R.id.button_close, pendingIntent);
    }

    public void initOpen() {
        // TODO
    }

}
