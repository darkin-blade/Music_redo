package com.example.music_redo.widget;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.music_redo.R;

public class PlayNotification extends Service {
//    Activity myActivity;
//    Context myContext;

    String packageName;

    RemoteViews remoteViews;

    @Override
    public void onCreate() {
        super.onCreate();
        initData();
        initView();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void initData() {
        if (remoteViews == null) {// TODO
            remoteViews.removeAllViews(R.layout.play_notification);
            remoteViews = null;
        }
        remoteViews = new RemoteViews(this.getPackageName(), R.layout.play_notification);
    }

    public void initView() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(myContext);
                    RemoteViews remoteViews = new RemoteViews(packageName, R.layout.play_notification);
                    NotificationManager manager = (NotificationManager) myActivity.getSystemService(Context.NOTIFICATION_SERVICE);

                    builder.setSmallIcon(R.drawable.ic_launcher_background);
                    builder.setCustomContentView(remoteViews);
                    builder.setStyle(new NotificationCompat.DecoratedCustomViewStyle());

                    manager.notify(1, builder.build());// TODO id
                }
            }).start();
        }
    }
}
