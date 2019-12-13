package com.example.music_redo.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Parcelable;
import android.widget.RemoteViews;

import com.example.music_redo.MusicList;
import com.example.music_redo.R;

import java.io.Serializable;
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
    static final int MODE_INIT = 6;

    public PlayWidgetProvider() {
        super();
        MusicList.infoLog("create provider: " + this.toString());
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
    }



    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        MusicList.infoLog("widget provider app widget options changed");
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
    }

    @Override
    public void onRestored(Context context, int[] oldWidgetIds, int[] newWidgetIds) {
        super.onRestored(context, oldWidgetIds, newWidgetIds);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        MusicList.infoLog("provider update");
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        // 初始化ui
        if (remoteViews == null) {
            remoteViews = new RemoteViews(context.getPackageName(), R.layout.play_widget);
        }

        // TODO 初始化变量
        MusicList.appWidgetManager = appWidgetManager;
        if (MusicList.appWidgetIds == null) {
            MusicList.appWidgetIds = new ArrayList<>();
        }
        MusicList.appWidgetIds.clear();
        for (int i = 0; i < appWidgetIds.length; i ++) {
            MusicList.appWidgetIds.add(appWidgetIds[i]);
        }
        Intent intent = new Intent(context, PlayWidgetService.class);
        intent.putExtra("mode", MODE_INIT);
        context.startService(intent);

        // 初始化监听
        initNext(context);
        initPrev(context);
        appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
    }

    public void initPlay(Context context) {
    }

    public void initPause(Context context) {
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
