package com.example.music_redo;

import android.app.Activity;
import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Date;

public class PlayTime {
    public Context myContext = null;
    public Activity myActivity = null;

    // 时间管理
    public int total_time;
    public int cur_time;
    public int duration;// 播放时间累计
    static public Thread musicPlay;

    public PlayTime(Context context, Activity activity) {
        myContext = context;
        myActivity = activity;
    }

    public void init() {
        total_time = 0;
        cur_time = 0;
        duration = 0;
    }

    public void load() {
        // 获取总时长
        total_time = MusicList.player.getDuration();

        // 设置时长ui
        SimpleDateFormat format = new SimpleDateFormat("mm:ss");
        Date tmp = new Date(total_time);
        String formatTime = format.format(tmp);
        MusicList.totalTime.setText(formatTime);
    }

    public void play(int mode) {
        /**
         * mode:
         * 0: 播放
         * 1: 加载
         * 2: 加载,置零
         * 3: 加载,置零,播放
         */
        if (mode >= 1) {
            ;
        }
    }

    public void pause() {
        ;
    }

    public void reset() {
        ;
    }

    public void getBar() {// 从进度条更新播放进度
        int curProgress = MusicList.seekBar.getProgress();
        int maxProgress = MusicList.seekBar.getMax();

        cur_time = curProgress * total_time / maxProgress;
        MusicList.player.seekTo(cur_time);// 调整时间

        myActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setTime();
            }
        });
    }

    public void setBar() {// 更新进度条
        int curProgress = cur_time * 100 / total_time;
        MusicList.seekBar.setProgress(curProgress);
    }

    public void setTime() {// 更新时间
        SimpleDateFormat format = new SimpleDateFormat("mm:ss");

        // 设置当前进度
        Date tmp = new Date(cur_time);
        String formatTime = format.format(tmp);
        MusicList.curTime.setText(formatTime);
    }
}
