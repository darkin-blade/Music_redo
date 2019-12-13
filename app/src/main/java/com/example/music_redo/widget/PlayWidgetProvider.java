package com.example.music_redo.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RemoteViews;

import com.example.music_redo.MusicList;
import com.example.music_redo.R;

import java.util.ArrayList;

public class PlayWidgetProvider extends AppWidgetProvider {

    RemoteViews remoteViews;
    AppWidgetManager appWidgetManager;
    ArrayList<Integer> appWidgetIds;

    static final int MODE_PLAY = 0;
    static final int MODE_PAUSE = 1;
    static final int MODE_NEXT = 2;
    static final int MODE_PREV = 3;
    static final int MODE_UPDATE = 4;// 更新进度条
    static final int MODE_CLOSE = 5;

    @Override
    public void onReceive(Context context, Intent intent) {
        int cmd_mode = intent.getIntExtra("mode", -1);
        boolean from_widget_provider = intent.getBooleanExtra("fromWidgetProvider", false);
        MusicList.infoLog("widget provider receive: " + cmd_mode);

        switch (cmd_mode) {
            case MODE_PLAY:
                break;
            case MODE_PAUSE:
                break;
            case MODE_NEXT:
                break;
            case MODE_PREV:
                break;
            case MODE_UPDATE:
                update();
                break;
        }

        super.onReceive(context, intent);
    }

    public void update() {// TODO 更新桌面部件进度条
        remoteViews.setProgressBar(R.id.music_bar, MusicList.playTime.total_time, MusicList.playTime.cur_time, false);
        int[] tmp = new int[appWidgetIds.size()];
        for (int i = 0; i < appWidgetIds.size(); i ++) {
            tmp[i] = appWidgetIds.get(i);
        }
        appWidgetManager.updateAppWidget(tmp, remoteViews);
    }

    @Override
    public void onEnabled(Context context) {
        MusicList.infoLog("widget provider enabled");
        super.onEnabled(context);
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        MusicList.infoLog("widget provider app widget options changed");
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
    }

    @Override
    public void onRestored(Context context, int[] oldWidgetIds, int[] newWidgetIds) {
        MusicList.infoLog("widget provider");
        super.onRestored(context, oldWidgetIds, newWidgetIds);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        MusicList.infoLog("provider update");
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        this.appWidgetManager = appWidgetManager;
        if (this.appWidgetIds == null) {
            this.appWidgetIds = new ArrayList<>();
        }
        this.appWidgetIds.clear();
        for (int i = 0; i < appWidgetIds.length; i ++) {
            this.appWidgetIds.add(appWidgetIds[i]);
        }

        // TODO 初始化桌面部件
        if (remoteViews == null) {
            remoteViews = new RemoteViews(context.getPackageName(), R.layout.play_widget);
        }

        initNext(context);
        initPrev(context);
        appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
    }

    @Override
    public void onDisabled(Context context) {
        MusicList.infoLog("widget provider disable");
        super.onDisabled(context);
    }

    public void initPlay(Context context) {
        ;
    }

    public void initPause(Context context) {
        ;
    }

    public void initNext(Context context) {
        Intent intent = new Intent(context, PlayWidgetService.class);
        intent.putExtra("fromWidgetProvider", true);
        intent.putExtra("mode", MODE_NEXT);
        PendingIntent pendingIntent = PendingIntent.getService(context, 2, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.button_next, pendingIntent);
    }

    public void initPrev(Context context) {
        Intent intent = new Intent(context, PlayWidgetService.class);
        intent.putExtra("fromWidgetProvider", true);
        intent.putExtra("mode", MODE_PREV);
        PendingIntent pendingIntent = PendingIntent.getService(context, 3, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.button_prev, pendingIntent);
    }
}
