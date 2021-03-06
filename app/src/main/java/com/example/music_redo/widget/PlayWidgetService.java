package com.example.music_redo.widget;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;

import com.example.music_redo.MusicList;
import com.example.music_redo.R;
import com.example.music_redo.player.PlayList;
import com.example.music_redo.player.PlayTime;

import java.util.List;

public class PlayWidgetService extends Service {// 用于部件交互

    static final int MODE_PLAY = 0;
    static final int MODE_PAUSE = 1;
    static final int MODE_NEXT = 2;
    static final int MODE_PREV = 3;
    static final int MODE_UPDATE = 4;// 更新进度条
    static final int MODE_CLOSE = 5;
    static final int MODE_INIT = 6;

    static public RemoteViews remoteViews;
    static public AppWidgetManager appWidgetManager;

    static public int isInit;

    @Override
    public void onCreate() {
        MusicList.infoLog("widget service create");
        isInit = 0;

        new Thread(new Runnable() {
            @Override
            public void run() {
                initService();
                while (PlayTime.player == null || PlayList.curMix == null) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {// 中断
                        e.printStackTrace();
                    }
                }
                initListener();// TODO 创建时就进行初始化
                PlayWidgetService.super.onCreate();// TODO
            }
        }).start();

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            return START_STICKY;
        }

        int cmd_mode = intent.getIntExtra("mode", -1);
        boolean from_widget_service = intent.getBooleanExtra("fromWidgetService", false);
        int[] ids = intent.getIntArrayExtra("appWidgetIds");

        // 更新layout
        MusicList.infoLog("widget service: " + cmd_mode);

        Intent playCmd = new Intent(this, PlayTime.class);
        switch (cmd_mode) {
            case MODE_PLAY:
                if (from_widget_service == true) {
                    playCmd.putExtra("cmd", "play");
                    playCmd.putExtra("mode", 0);
                    startService(playCmd);
                }
                initPause();
                break;
            case MODE_PAUSE:
                if (from_widget_service == true) {
                    playCmd.putExtra("cmd", "pause");
                    startService(playCmd);
                }
                initPlay();
                break;
            case MODE_NEXT:
                if (from_widget_service == true) {
                    playCmd.putExtra("cmd", "next");
                    startService(playCmd);
                }
                initPause();
                break;
            case MODE_PREV:
                if (from_widget_service == true) {
                    playCmd.putExtra("cmd", "prev");
                    startService(playCmd);
                }
                initPause();
                break;
            case MODE_UPDATE:
                update();
                break;
            case MODE_INIT:
                initListener();
                break;
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        isInit = 0;
        MusicList.infoLog("widget service destroy\n\n");
        super.onDestroy();
    }

    public void update() {// 更新桌面部件进度条
        if (isInit != 1) {
            return;
        }

        remoteViews.setProgressBar(R.id.music_bar, PlayTime.total_time, PlayTime.cur_time, false);
        updateUI();
    }

    public void updateUI() {
        // TODO
        appWidgetManager = AppWidgetManager.getInstance(this);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            List<AppWidgetProviderInfo> infos = appWidgetManager.getInstalledProvidersForPackage(getPackageName(), null);
            if (infos.size() >= 1) {// 非空
                String providerName = infos.get(0).provider.getClassName();
                int[] ids = appWidgetManager.getAppWidgetIds(new ComponentName(getPackageName(), providerName));
                appWidgetManager.updateAppWidget(ids, remoteViews);
            }
        }
    }

    public void initService() {
        // TODO 初始化其他所有service
        Intent intent;
        if (PlayTime.player == null) {// TODO 播放器无效
            intent = new Intent(this, PlayTime.class);
            intent.putExtra("cmd", "init");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent);
            } else {
                startService(intent);
            }
        }

        if (PlayList.curMix == null) {// TODO 需要恢复歌单
            intent = new Intent(this, PlayList.class);
            intent.putExtra("cmd", "init");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent);
            } else {
                startService(intent);
            }
        }

        // TODO
        if (remoteViews == null) {
            remoteViews = new RemoteViews(this.getPackageName(), R.layout.play_widget);
        }
    }

    public void initListener() {
        // TODO 初始化监听
        if (remoteViews == null) {
            remoteViews = new RemoteViews(this.getPackageName(), R.layout.play_widget);
        }
        if (PlayTime.player.isPlaying()) {
            initPause();
        } else {
            initPlay();// 初始为暂停
        }
        initNext();
        initPrev();

        // 更新ui
        updateUI();

        isInit = 1;
    }

    public void initPlay() {
        initService();

        // TODO 修改ui
        if (PlayList.curMix == null || PlayList.curMusic.length() <= 0 || PlayList.curMix.length() <= 0) {
            remoteViews.setTextViewText(R.id.cur_music, "no music");
        } else {
            remoteViews.setTextViewText(R.id.cur_music, PlayList.curMix + "    " + PlayList.curMusic.replaceAll(".*/", ""));
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            remoteViews.setImageViewResource(R.id.button_play, R.drawable.player_play);
            Intent intent = new Intent(this, PlayWidgetService.class);
            intent.putExtra("fromWidgetService", true);
            intent.putExtra("mode", MODE_PLAY);
            PendingIntent pendingIntent = null;
            pendingIntent = PendingIntent.getForegroundService(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.button_play, pendingIntent);
        }

        if (isInit == 1) {
            updateUI();
        }
    }

    public void initPause() {
        initService();

        // TODO 修改ui
        if (PlayList.curMix == null || PlayList.curMusic.length() <= 0 || PlayList.curMix.length() <= 0) {
            remoteViews.setTextViewText(R.id.cur_music, "no music");
        } else {
            remoteViews.setTextViewText(R.id.cur_music, PlayList.curMix + "    " + PlayList.curMusic.replaceAll(".*/", ""));
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            remoteViews.setImageViewResource(R.id.button_play, R.drawable.player_pause);
            Intent intent = new Intent(this, PlayWidgetService.class);
            intent.putExtra("fromWidgetService", true);
            intent.putExtra("mode", MODE_PAUSE);
            PendingIntent pendingIntent = PendingIntent.getForegroundService(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.button_play, pendingIntent);
        }

        if (isInit == 1) {
            updateUI();
        }
    }

    public void initNext() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            MusicList.infoLog("widget service next");
            Intent intent = new Intent(this, PlayWidgetService.class);
            intent.putExtra("fromWidgetService", true);
            intent.putExtra("mode", MODE_NEXT);
            PendingIntent pendingIntent = PendingIntent.getForegroundService(this, 2, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.button_next, pendingIntent);
        }
    }

    public void initPrev() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            Intent intent = new Intent(this, PlayWidgetService.class);
            intent.putExtra("fromWidgetService", true);
            intent.putExtra("mode", MODE_PREV);
            PendingIntent pendingIntent = PendingIntent.getForegroundService(this, 3, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.button_prev, pendingIntent);
        }
    }
}
