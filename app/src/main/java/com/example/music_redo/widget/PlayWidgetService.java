package com.example.music_redo.widget;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
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
    static public ArrayList<Integer> appWidgetIds;

    static public int isInit;

    @Override
    public void onCreate() {// TODO
        super.onCreate();
        MusicList.infoLog("widget service create");
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
        boolean from_widget_service = intent.getBooleanExtra("fromWidgetService", false);
        int[] ids = intent.getIntArrayExtra("appWidgetIds");

        // TODO 更新ui
//        MusicList.infoLog("widget service: " + cmd_mode);
        if (remoteViews == null) {
            remoteViews = new RemoteViews(this.getPackageName(), R.layout.play_widget);// TODO context
        }

        switch (cmd_mode) {
            case MODE_PLAY:
                if (from_widget_service == true) {
                    MusicList.playTime.play(0);
                }
                initPause();
                break;
            case MODE_PAUSE:
                if (from_widget_service == true) {
                    MusicList.playTime.pause();
                }
                initPlay();
                break;
            case MODE_NEXT:
                if (from_widget_service == true) {
                    MusicList.playTime.next();
                }
                initPause();
                break;
            case MODE_PREV:
                if (from_widget_service == true) {
                    MusicList.playTime.prev();
                }
                initPause();
                break;
            case MODE_UPDATE:
                update();
                break;
            case MODE_INIT:
                init(ids);
                break;
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        isInit = 0;
        MusicList.infoLog("widget service destroy");
        super.onDestroy();
    }

    public void init(int[] ids) {
        MusicList.infoLog("init mode");

        // TODO 接收变量
        appWidgetManager = AppWidgetManager.getInstance(this);
        if (appWidgetIds == null) {
            appWidgetIds = new ArrayList<>();
        }
        appWidgetIds.clear();
        if (ids != null) {
            for (int i = 0; i < ids.length; i ++) {
                appWidgetIds.add(ids[i]);
            }
        }

        // 初始化监听
        initPlay();// 初始为暂停
        initNext();
        initPrev();

        // 更新ui
        updateUI();

        isInit = 1;
    }

    public void update() {// 更新桌面部件进度条
        MusicList.infoLog("update mode");

        if (isInit != 1) {
            return;
        }

        if (MusicList.playTime == null) {
            return;
        }

        remoteViews.setProgressBar(R.id.music_bar, MusicList.playTime.total_time, MusicList.playTime.cur_time, false);
        updateUI();
    }

    public void updateUI() {
        int[] tmp = new int[appWidgetIds.size()];
        for (int i = 0; i < appWidgetIds.size(); i ++) {
            tmp[i] = appWidgetIds.get(i);
        }
        appWidgetManager.updateAppWidget(tmp, remoteViews);
    }

    public void initPlay() {
             remoteViews.setImageViewResource(R.id.button_play, R.drawable.player_play);
   Intent intent = new Intent(this, PlayWidgetService.class);
        intent.putExtra("fromWidgetService", true);
        intent.putExtra("mode", MODE_PLAY);
        PendingIntent pendingIntent = PendingIntent.getService(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.button_play, pendingIntent);

        if (isInit == 1) {
            updateUI();
        }
    }

    public void initPause() {
        if (MusicList.playList.curMusic.length() <= 0 || MusicList.playList.curMix.length() <= 0) {
            remoteViews.setTextViewText(R.id.cur_music, "no music");
        } else {
            remoteViews.setTextViewText(R.id.cur_music, MusicList.playList.curMix + "    " + MusicList.playList.curMusic.replaceAll(".*/", ""));
        }

        remoteViews.setImageViewResource(R.id.button_play, R.drawable.player_pause);
        Intent intent = new Intent(this, PlayWidgetService.class);
        intent.putExtra("fromWidgetService", true);
        intent.putExtra("mode", MODE_PAUSE);
        PendingIntent pendingIntent = PendingIntent.getService(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.button_play, pendingIntent);

        if (isInit == 1) {
            updateUI();
        }
    }

    public void initNext() {
        Intent intent = new Intent(this, PlayWidgetService.class);
        intent.putExtra("fromWidgetService", true);
        intent.putExtra("mode", MODE_NEXT);
        PendingIntent pendingIntent = PendingIntent.getService(this, 2, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.button_next, pendingIntent);
    }

    public void initPrev() {
        Intent intent = new Intent(this, PlayWidgetService.class);
        intent.putExtra("fromWidgetService", true);
        intent.putExtra("mode", MODE_PREV);
        PendingIntent pendingIntent = PendingIntent.getService(this, 3, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.button_prev, pendingIntent);
    }
}
