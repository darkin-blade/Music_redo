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

public class PlayWidgetProvider extends AppWidgetProvider {

    RemoteViews remoteViews;

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
    }

    @Override
    public void onEnabled(Context context) {
        MusicList.infoLog("on enabled");
        super.onEnabled(context);
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
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

        if (remoteViews == null) {
            remoteViews = new RemoteViews(context.getPackageName(), R.layout.play_widget);
            Intent intent = new Intent(context, PlayWidgetService.class);
            intent.putExtra("fromWidgetProvider", true);
            intent.putExtra("mode", -1);
            PendingIntent pendingIntent = PendingIntent.getService(context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.button_play, pendingIntent);
            appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
        }
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
    }
}
