package com.example.music_redo.widget;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.music_redo.MusicList;
import com.example.music_redo.R;
import com.example.music_redo.player.PlayList;
import com.example.music_redo.player.PlayTime;

public class PlayNotification extends Service {

    NotificationCompat.Builder builder;
    NotificationChannel channel;
    NotificationManager manager;
    RemoteViews remoteViews;

    // 用于部件交互
    static final int MODE_PLAY = 0;
    static final int MODE_PAUSE = 1;
    static final int MODE_NEXT = 2;
    static final int MODE_PREV = 3;
    static final int MODE_UPDATE = 4;// 更新进度条
    static final int MODE_CLOSE = 5;

    int isShown;// 是否显示在状态栏

    public class ScreenReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (Intent.ACTION_SCREEN_ON.equals(action)) {// 亮屏
                MusicList.infoLog("build null? " + (builder == null));
                if (builder != null) {
                    PlayNotification.this.stopForeground(true);
                    PlayNotification.this.startForeground(100, builder.build());
                }
            }
        }
    }

    static public ScreenReceiver screenReceiver;// 监听锁屏

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
        isShown = 0;

        if (remoteViews != null) {// TODO
            remoteViews.removeAllViews(R.layout.play_notification);
            remoteViews = null;
        }
        remoteViews = new RemoteViews(this.getPackageName(), R.layout.play_notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        initView();

        // TODO 关闭
        if (intent == null) {
            return START_STICKY;
        }

        int cmd_mode = intent.getIntExtra("mode", -1);
        boolean from_notification = intent.getBooleanExtra("fromNotification", false);
        Intent playCmd = new Intent(this, PlayTime.class);// TODO
        switch (cmd_mode) {
            case MODE_PLAY:
                if (from_notification == true) {
                    playCmd.putExtra("cmd", "play");
                    playCmd.putExtra("mode", 0);
                    startService(playCmd);
                }
                initPause();
                break;
            case MODE_PAUSE:
                if (from_notification == true) {
                    playCmd.putExtra("cmd", "pause");
                    startService(playCmd);
                }
                initPlay();
                break;
            case MODE_NEXT:
                if (from_notification == true) {
                    playCmd.putExtra("cmd", "next");
                    startService(playCmd);
                }
                initPause();
                break;
            case MODE_PREV:
                if (from_notification == true) {
                    playCmd.putExtra("cmd", "prev");
                    startService(playCmd);
                }
                initPause();
                break;
            case MODE_UPDATE:
                update();
                break;
            case MODE_CLOSE:
                close();
                break;
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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
                    .setOnlyAlertOnce(false)// TODO 没屌用
                    .setAutoCancel(false)// TODO
                    .setOngoing(true);// TODO
        }

        initReceiver();
        initNext();
        initPrev();
        initClose();
        initOpen();
    }

    public void close() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            manager.deleteNotificationChannel("default");
//            channel = null;
//        }
        isShown = 0;

        // TODO 暂停音乐
        Intent intent = new Intent(this, PlayTime.class);
        intent.putExtra("cmd", "pause");
        startService(intent);

        // 避免震动
        stopForeground(true);

        // TODO 关闭服务
        stopSelf();
    }

    public void update() {
        if (isShown == 1) {
            remoteViews.setProgressBar(R.id.music_bar, PlayTime.total_time, PlayTime.cur_time, false);// 明确进度
            builder.setContent(remoteViews);
            startForeground(100, builder.build());
        }
    }

    public void initReceiver() {
        screenReceiver = new ScreenReceiver();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        intentFilter.addAction(Intent.ACTION_USER_PRESENT);

        registerReceiver(screenReceiver, intentFilter);
    }

    public void initPlay() {
        remoteViews.setImageViewResource(R.id.button_play, R.drawable.player_play);
        Intent intent = new Intent(this, PlayNotification.class);
        intent.putExtra("fromNotification", true);
        intent.putExtra("mode", MODE_PLAY);
        PendingIntent pendingIntent = PendingIntent.getService(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        remoteViews.setOnClickPendingIntent(R.id.button_play, pendingIntent);

        if (isShown == 1) {
            builder.setContent(remoteViews);
            startForeground(100, builder.build());
        }
    }

    public void initPause() {
        if (isShown == 0) {
            isShown = 1;
        }

        if (PlayList.curMusic.length() <= 0 || PlayList.curMix.length() <= 0) {
            remoteViews.setTextViewText(R.id.cur_music, "no music");
        } else {
            remoteViews.setTextViewText(R.id.cur_music, PlayList.curMix + "    " + PlayList.curMusic.replaceAll(".*/", ""));
        }

        remoteViews.setImageViewResource(R.id.button_play, R.drawable.player_pause);
        Intent intent = new Intent(this, PlayNotification.class);
        intent.putExtra("fromNotification", true);
        intent.putExtra("mode", MODE_PAUSE);
        PendingIntent pendingIntent = PendingIntent.getService(this, 2, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        remoteViews.setOnClickPendingIntent(R.id.button_play, pendingIntent);

        builder.setContent(remoteViews);
        startForeground(100, builder.build());
    }

    public void initNext() {
        Intent intent = new Intent(this, PlayNotification.class);
        intent.putExtra("fromNotification", true);
        intent.putExtra("mode", MODE_NEXT);
        PendingIntent pendingIntent = PendingIntent.getService(this, 3, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        remoteViews.setOnClickPendingIntent(R.id.button_next, pendingIntent);
    }

    public void initPrev() {
        Intent intent = new Intent(this, PlayNotification.class);
        intent.putExtra("fromNotification", true);
        intent.putExtra("mode", MODE_PREV);
        PendingIntent pendingIntent = PendingIntent.getService(this, 4, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        remoteViews.setOnClickPendingIntent(R.id.button_prev, pendingIntent);
    }

    public void initClose() {
        Intent intent = new Intent(this, PlayNotification.class);
        intent.putExtra("fromNotification", true);
        intent.putExtra("mode", MODE_CLOSE);
        PendingIntent pendingIntent = PendingIntent.getService(this, 5, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        remoteViews.setOnClickPendingIntent(R.id.button_close, pendingIntent);
    }

    public void initOpen() {// 切换到应用程序
        Intent intent = new Intent(this, MusicList.class);
        remoteViews.setOnClickPendingIntent(R.id.button_open, PendingIntent.getActivity(this, 6, intent, 0));
        remoteViews.setOnClickPendingIntent(R.id.play_notification, null);
    }

}
