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
    static final int MODE_PLAY= 4;

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
                if (from_notification == true) {
                    MusicList.playTime.next();
                }
                initPause();
                break;
            case  MODE_PREV:
                if (from_notification == true) {
                    MusicList.playTime.prev();
                }
                initPause();
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

//        Intent mainIntent = new Intent(mContext, MainActivity.class);
//        mRemoteViews.setOnClickPendingIntent(R.id.ll_customNotification, PendingIntent.getActivity(mContext, 0, mainIntent, 0));

        Intent INext = new Intent(this, PlayNotification.class);
        INext.putExtra("mode", MODE_NEXT);
        INext.putExtra("fromNotification", true);// TODO 避免死循环
        PendingIntent PINext = PendingIntent.getService(this, 1, INext, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(PINext);
        remoteViews.setOnClickPendingIntent(R.id.button_next, PINext);

        Intent IPrev = new Intent(this, PlayNotification.class);
        IPrev.putExtra("mode", MODE_PREV);
        IPrev.putExtra("fromNotification", true);// TODO 避免死循环
        PendingIntent PIPrev = PendingIntent.getService(this, 1, IPrev, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(PIPrev);
        remoteViews.setOnClickPendingIntent(R.id.button_prev, PIPrev);

    }

    public void initPlay() {
        remoteViews.setImageViewResource(R.id.button_play, R.drawable.player_play);
        builder.setContent(remoteViews);
        startForeground(100, builder.build());

        Intent IPlay = new Intent(this, PlayNotification.class);
        IPlay.putExtra("mode", MODE_PLAY);
        IPlay.putExtra("fromNotification", true);
        PendingIntent PIPlay = PendingIntent.getService(this, 1, IPlay, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(PIPlay);
        remoteViews.setOnClickPendingIntent(R.id.button_play, PIPlay);
    }

    public void initPause() {
        remoteViews.setImageViewResource(R.id.button_play, R.drawable.player_pause);
        builder.setContent(remoteViews);
        startForeground(100, builder.build());

        Intent IPause = new Intent(this, PlayNotification.class);
        IPause.putExtra("mode", MODE_PAUSE);
        IPause.putExtra("fromNotification", true);
        PendingIntent PIPause = PendingIntent.getService(this, 1, IPause, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(PIPause);
        remoteViews.setOnClickPendingIntent(R.id.button_play, PIPause);
    }

}
