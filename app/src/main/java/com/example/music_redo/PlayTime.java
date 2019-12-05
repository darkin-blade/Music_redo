package com.example.music_redo;

import android.app.Activity;
import android.content.Context;

public class PlayTime {
    public Context myContext = null;
    public Activity myActivity = null;

    // 时间管理
    public int total_time;
    public int cur_time;
    public int cumulate_time;
    static public Thread musicPlay;

    public PlayTime(Context context, Activity activity) {
        myContext = context;
        myActivity = activity;
    }

    public void init() {
        total_time = 0;
        cur_time = 0;
        cumulate_time = 0;
    }
}
