package com.example.music_redo.widget;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.widget.RemoteViews;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.example.music_redo.MusicList;
import com.example.music_redo.R;

public class PlayNotification extends AppCompatActivity {
    Activity myActivity;
    Context myContext;

    String packageName;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO setContentView
    }

    public PlayNotification(String packageName, Activity activity, Context context) {
        this.packageName = packageName;
        myActivity = activity;
        myContext = context;
    }

    public void initData() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(myContext);
                    RemoteViews remoteViews = new RemoteViews(packageName, R.layout.play_notification);
                    NotificationManager manager = (NotificationManager) myActivity.getSystemService(Context.NOTIFICATION_SERVICE);

                    builder.setSmallIcon(R.drawable.ic_launcher_background);
//                    builder.setContent(remoteViews);

                    manager.notify(1, builder.build());// TODO id
                }
            }).start();
        }
    }
}
