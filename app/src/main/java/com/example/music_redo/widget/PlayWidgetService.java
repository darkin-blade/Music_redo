package com.example.music_redo.widget;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;

import com.example.music_redo.MusicList;
import com.example.music_redo.R;

import java.util.ArrayList;

public class PlayWidgetService extends Service {// 用于部件交互

    static final int MODE_PLAY = 0;
    static final int MODE_PAUSE = 1;
    static final int MODE_NEXT = 2;
    static final int MODE_PREV = 3;
    static final int MODE_UPDATE = 4;// 更新进度条
    static final int MODE_CLOSE = 5;
    static final int MODE_INIT = 6;

    RemoteViews remoteViews;
    AppWidgetManager appWidgetManager;
    ArrayList<Integer> appWidgetIds;

    int isInit;

    @Override
    public void onCreate() {// TODO 自动创建?
        super.onCreate();
        MusicList.infoLog("widget service init");
        isInit = 0;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int cmd_mode = intent.getIntExtra("mode", -1);
        boolean from_widget_provider = intent.getBooleanExtra("fromWidgetProvider", false);

        // TODO 更新ui
        MusicList.infoLog("widget service: " + cmd_mode);
        if (remoteViews == null) {
            remoteViews = new RemoteViews(this.getPackageName(), R.layout.play_widget);// TODO context
        }

        switch (cmd_mode) {
            case MODE_PLAY:
                initPause();
                break;
            case MODE_PAUSE:
                break;
            case MODE_NEXT:
                if (from_widget_provider == true) {
                    MusicList.playTime.next();
                }
                initPause();
                break;
            case MODE_PREV:
                if (from_widget_provider == true) {
                    MusicList.playTime.prev();
                }
                initPause();
                break;
            case MODE_UPDATE:
                update();
                break;
            case MODE_INIT:
                init();
                break;
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        isInit = 0;
        super.onDestroy();
    }

    public void init() {
        MusicList.infoLog("init mode");

        // TODO 接收变量
        appWidgetManager = MusicList.appWidgetManager;
        if (appWidgetIds == null) {
            appWidgetIds = new ArrayList<>();
        }
        appWidgetIds.clear();
        appWidgetIds.addAll(MusicList.appWidgetIds);

        // 初始化监听
        initNext();
        initPrev();

        // 更新ui
        int[] tmp = new int[appWidgetIds.size()];
        for (int i = 0; i < appWidgetIds.size(); i ++) {
            tmp[i] = appWidgetIds.get(i);
        }
        appWidgetManager.updateAppWidget(tmp, remoteViews);

        isInit = 1;
    }

    public void update() {// TODO 更新桌面部件进度条
        MusicList.infoLog("update mode");

        if (isInit != 1) {
            return;
        }

        if (MusicList.playTime == null) {
            return;
        }

        remoteViews.setProgressBar(R.id.music_bar, MusicList.playTime.total_time, MusicList.playTime.cur_time, false);
        int[] tmp = new int[appWidgetIds.size()];
        for (int i = 0; i < appWidgetIds.size(); i ++) {
            tmp[i] = appWidgetIds.get(i);
        }
        appWidgetManager.updateAppWidget(tmp, remoteViews);
    }

    public void initPlay() {
    }

    public void initPause() {
    }

    public void initNext() {
        Intent intent = new Intent(this, PlayWidgetService.class);
        intent.putExtra("fromWidgetProvider", true);
        intent.putExtra("mode", MODE_NEXT);
        PendingIntent pendingIntent = PendingIntent.getService(this, 2, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.button_next, pendingIntent);
    }

    public void initPrev() {
        Intent intent = new Intent(this, PlayWidgetService.class);
        intent.putExtra("fromWidgetProvider", true);
        intent.putExtra("mode", MODE_PREV);
        PendingIntent pendingIntent = PendingIntent.getService(this, 3, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.button_prev, pendingIntent);
    }
}
