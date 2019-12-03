package com.example.music_redo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainPlayer extends AppCompatActivity {
    // ui组件
    // 按钮
    static public View button_play;
    static public View button_next;
    static public View button_prev;
    static public View button_bluetooth;
    static public View button_mix;
    static public Button button_edit;
    static public Button button_mode;
    // 进度控制
    static public MediaPlayer player;// 媒体播放器
    static public SeekBar seekBar;// 进度条
    static public TextView totalTime;// 音乐总时长
    static public TextView curTime;// 音乐已播放时长
    // 列表部分
    static public TextView musicName;// 歌名
    static public ScrollView scrollView;// 滚动界面
    static public LinearLayout mainList;// 列表部分

    // ui功能
    // dialog界面
    static public MainEdit mainEdit;// 主界面 编辑歌曲
    static public MixEdit mixEdit;// 编辑歌单
    static public MusicEdit musicEdit;// 歌单界面 编辑歌曲
    static public MixNew mixNew;// 新建歌单
    // ui界面
    static public MusicSelect musicSelect;// 文件浏览器
    // TODO 播放模式
    // TODO 蓝牙管理

    // 公共变量
    static public String appPath;
    static public SQLiteDatabase database;

    // 功能代号
    static int window_num;
    static final int MAIN_PLAYER = 0;// 主界面
    static final int MIX_LIST = 1;// 歌单列表
    static final int MUSIC_LIST = 2;// 歌曲列表
    static final int ADD_LIST = 3;// `添加至`列表
    static final int MAIN_EDIT = 4;// 主界面管理歌曲
    static final int MIX_EDIT = 5;// 歌单列表管理歌单
    static final int MUSIC_EDIT = 6;// 歌曲列表管理歌曲
    static final int MIX_NEW = 7;// 新建歌单

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_player);

        initApp();
        initPlayer();
        initUI();
        initBluetooth();
        initData();
    }

    public void initApp() {// 初始化app核心变量
        ;
    }

    public void initBluetooth() {
        ;
    }

    public void initPlayer() {
        ;
    }

    public void initUI() {// 初始化ui,layout和dialog
        ;
    }

    public void initData() {
        ;
    }

    static public void infoLog(String log) {
        Log.i("fuck", log);
    }

    static public void infoToast(Context context, String log) {
        if (context == null) {// 增加容错
            return;
        }
        Toast toast = Toast.makeText(context, log, Toast.LENGTH_SHORT);
        View view = toast.getView();
        TextView textView = view.findViewById(android.R.id.message);
        textView.setTextColor(Color.rgb(0x00, 0x00, 0x00));
        toast.show();
    }

    static public String pathSimplify(String path) {// TODO 简化路径
        path = path.replaceAll("/+\\./", "/");// 除去`/.`
        infoLog("path: " + path);

        Pattern pattern = Pattern.compile("/+\\.[\\.]+");
        Matcher matcher = pattern.matcher(path);
        int count = 0;
        while (matcher.find()) {
            count ++;
        }

        path = path.replaceAll("/+\\.[\\.]+", "");// 除去`/..`

        int index = path.length() - 1;
        if (path.charAt(index) == '/') {
            index --;
        }

        while (index >= 0) {
            if (path.charAt(index) == '/') {
                count --;
                if (count == 0) {
                    infoLog("simplified path: " + path.substring(0, index));
                    return path.substring(0, index);
                }
            }
            index --;
        }

        return "/";
    }
}
