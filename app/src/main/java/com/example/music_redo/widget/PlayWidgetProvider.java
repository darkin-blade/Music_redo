package com.example.music_redo.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.example.music_redo.MusicList;

import java.util.ArrayList;

public class PlayWidgetProvider extends AppWidgetProvider {

    static final int MODE_PLAY = 0;
    static final int MODE_PAUSE = 1;
    static final int MODE_NEXT = 2;
    static final int MODE_PREV = 3;
    static final int MODE_UPDATE = 4;// 更新进度条
    static final int MODE_CLOSE = 5;
    static final int MODE_INIT = 6;

    static public ArrayList<Integer> appWidgetIds;// TODO 一定要使用static

    public PlayWidgetProvider() {
        super();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        // TODO 初始化变量
        if (appWidgetIds == null) {
            appWidgetIds = new ArrayList<>();
            MusicList.infoLog("init ids");
        }

        String action = intent.getAction();
        MusicList.infoLog("widget provider receive " + action);
        if (AppWidgetManager.ACTION_APPWIDGET_UPDATE.equals(action)) {// TODO 增加widget
            Bundle extras = intent.getExtras();
            if (extras != null) {
                int[] ids = extras.getIntArray(AppWidgetManager.EXTRA_APPWIDGET_IDS);
                if (ids != null && ids.length > 0) {
                    for (int i = 0; i < ids.length; i ++) {
                        appWidgetIds.add(ids[i]);
                    }
                }
            }
        } else if (AppWidgetManager.ACTION_APPWIDGET_DELETED.equals(action)) {// TODO 删除widget
            Bundle extras = intent.getExtras();
            if (extras != null && extras.containsKey(AppWidgetManager.EXTRA_APPWIDGET_ID)) {
                int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID);
                this.appWidgetIds.remove(appWidgetId);
            }
        } else {
            return;
        }

        int[] ids = new int[appWidgetIds.size()];
        for (int i = 0; i < appWidgetIds.size(); i ++) {
            ids[i] = appWidgetIds.get(i);
            MusicList.infoLog("id: " + ids[i]);
        }

        Intent tmp = new Intent(context, PlayWidgetService.class);
        tmp.putExtra("mode", MODE_INIT);
        tmp.putExtra("appWidgetIds", ids);
        context.startService(tmp);
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
        super.onRestored(context, oldWidgetIds, newWidgetIds);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        MusicList.infoLog("provider update");
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
    }
}
