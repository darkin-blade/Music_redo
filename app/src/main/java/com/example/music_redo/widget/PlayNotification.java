package com.example.music_redo.widget;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.widget.RemoteViews;

import com.example.music_redo.MusicList;
import com.example.music_redo.R;

public class PlayNotification {
    Activity myActivity;
    Context myContext;

    String packageName;


    public PlayNotification(String packageName, Activity activity, Context context) {
        this.packageName = packageName;
        myActivity = activity;
        myContext = context;
    }

    public void initData() {
        Notification.Builder builder = new Notification.Builder(myContext);

        builder.setSmallIcon(R.drawable.ic_launcher_background);

        RemoteViews remoteViews = new RemoteViews(packageName, R.layout.play_notification);
        builder.setContent(remoteViews);

        NotificationManager manager = (NotificationManager) myActivity.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {// TODO
            MusicList.infoLog("show notify");
            manager.notify(1, builder.build());// TODO id
        }
    }
}
