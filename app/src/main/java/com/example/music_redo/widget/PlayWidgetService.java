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
        MusicList.infoLog("widget service create");
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
                break;
            case MODE_CLOSE:
                break;
        }

        Intent tmp = new Intent("com.example.music_redo.UPDATE_ALL");
        sendBroadcast(tmp);
        MusicList.infoLog("mode: " + cmd_mode);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
