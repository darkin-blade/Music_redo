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
                break;
            case MODE_PLAY:
                break;
            case MODE_NEXT:
                break;
            case  MODE_PREV:
                break;
            case MODE_CLOSE:
                close();
                break;
        }

        return START_STICKY;// TODO 防止被杀
    }

    @Override
    public void onDestroy() {
        MusicList.infoLog("destroy service");
        super.onDestroy();
    }

    public void initView() {
        if (channel == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {// TODO
                MusicList.infoLog("create channel");
                channel = new NotificationChannel("default", "default", NotificationManager.IMPORTANCE_DEFAULT);
                channel.setImportance(NotificationManager.IMPORTANCE_LOW);
                manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                manager.createNotificationChannel(channel);
            }
        }

        if (builder == null) {
            MusicList.infoLog("create builder");
            builder = new NotificationCompat.Builder(this, "default")
                    .setSmallIcon(R.drawable.ic_launcher_background)
                    .setContent(remoteViews)
                    .setWhen(-System.currentTimeMillis())
                    .setOnlyAlertOnce(true)// TODO
                    .setAutoCancel(false)// TODO
                    .setOngoing(true);// TODO
        }

//        initNext();
//        initPrev();
        initClose();
//        initOpen();
    }

    public void close() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            manager.deleteNotificationChannel("default");
//            channel = null;
//        }
        // 避免震动
        stopForeground(true);
    }

    public void initPlay() {
        remoteViews.setImageViewResource(R.id.button_play, R.drawable.player_play);

        Intent intent = new Intent(this, PlayNotification.class);
        intent.putExtra("mode", MODE_PLAY);
        intent.putExtra("fromNotification", true);
        remoteViews.setOnClickPendingIntent(R.id.button_play, PendingIntent.getActivity(this, 0, intent, 0));
    }

    public void initPause() {
        remoteViews.setImageViewResource(R.id.button_play, R.drawable.player_pause);
        if (MusicList.playList.curMusic.length() <= 0 || MusicList.playList.curMix.length() <= 0) {
            remoteViews.setTextViewText(R.id.cur_music, "no music");
        } else {
            remoteViews.setTextViewText(R.id.cur_music, MusicList.playList.curMix + "    " + MusicList.playList.curMusic.replaceAll(".*/", ""));
        }

        Intent intent = new Intent(this, PlayNotification.class);
        intent.putExtra("mode", MODE_PAUSE);
        intent.putExtra("fromNotification", true);
        remoteViews.setOnClickPendingIntent(R.id.button_play, PendingIntent.getActivity(this, 0, intent, 0));

        builder.setContent(remoteViews);
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

    public void initOpen() {// 切换到应用程序
        Intent intent = new Intent(this, PlayNotification.class);
        remoteViews.setOnClickPendingIntent(R.id.button_open, PendingIntent.getActivity(this, 0, intent, 0));
    }

}
