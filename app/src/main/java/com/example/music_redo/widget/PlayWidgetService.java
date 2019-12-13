package com.example.music_redo.widget;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.example.music_redo.MusicList;

public class PlayWidgetService extends Service {// 用于部件交互

    static final int MODE_PLAY = 0;
    static final int MODE_PAUSE = 1;
    static final int MODE_NEXT = 2;
    static final int MODE_PREV = 3;
    static final int MODE_UPDATE = 4;// 更新进度条
    static final int MODE_CLOSE = 5;

    @Override
    public void onCreate() {// TODO 自动创建?
        super.onCreate();
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
        updateUI(cmd_mode, from_widget_provider);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void updateUI(int mode, boolean flag) {// 调用provider
        Intent intent = new Intent("com.example.music_redo.UPDATE_ALL");
        intent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        intent.putExtra("mode", mode);
        intent.putExtra("from_widget_service", flag);
        sendBroadcast(intent);
    }
}
