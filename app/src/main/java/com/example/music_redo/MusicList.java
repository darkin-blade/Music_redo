package com.example.music_redo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.SQLException;
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

import com.example.music_redo.components.MainList;
import com.example.music_redo.components.MediaReceiver;
import com.example.music_redo.components.PlayList;
import com.example.music_redo.components.PlayTime;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MusicList extends AppCompatActivity implements DialogInterface.OnDismissListener {
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
    static public SeekBar seekBar;// 进度条
    static public TextView totalTime;// 音乐总时长
    static public TextView curTime;// 音乐已播放时长
    // 列表部分
    static public TextView musicName;// 歌名
    static public ScrollView scrollView;// 滚动界面
    static public LinearLayout itemList;// 列表部分

    // ui功能
    // dialog界面
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
    // TODO media信号处理
    static public Long myTime = System.currentTimeMillis();// 微秒时间
    static public int clickTimes;// TODO 耳机信号次数

    // 功能代号
    static int window_num;
    static final int MUSIC_LIST = 0;// 主界面
    static final int MIX_LIST = 1;// 歌单列表
    static final int ADD_LIST = 3;// `添加至`列表
    static final int MIX_EDIT = 5;// 歌单列表管理歌单
    static final int MUSIC_EDIT = 6;// 歌曲列表管理歌曲
    static final int MIX_NEW = 7;// 新建歌单

    // 核心组件
    static public MediaPlayer player;// 媒体播放器
    public BluetoothAdapter bluetoothAdapter;// 蓝牙
    // 核心功能
    public MediaReceiver receiver;// 接收`蓝牙/媒体`信号
    static public MainList mainList;
    static public PlayList playList;
    static public PlayTime playTime;

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

    public void initApp() {
        // 检查权限
        String permission = "android.permission.WRITE_EXTERNAL_STORAGE";
        int check_result = ActivityCompat.checkSelfPermission(this, permission);// `允许`返回0,`拒绝`返回-1
        if (check_result != PackageManager.PERMISSION_GRANTED) {// 没有`写`权限
            ActivityCompat.requestPermissions(this, new String[]{permission}, 1);// 获取`写`权限
        }

        // 初始化功能编号
        window_num = MUSIC_LIST;

        // 初始化路径字符串
        appPath = getExternalFilesDir("").getAbsolutePath();

        // 初始化数据库
        database = SQLiteDatabase.openOrCreateDatabase(appPath + "/player.db", null);
        // `歌单列表`table
        cmd("create table if not exists mix_list (\n" +
                "  name varchar (32) not null,\n" +
                "  primary key (name)\n" +
                ") ;");
        // `用户数据`table
        cmd("create table if not exists user_data (\n" +
                "  cur_mix varchar(32) default \"\",\n" +
                "  cur_music varchar(128) default \"\",\n" +
                "  play_mode int default 0,\n" +
                "  cur_time int default 0,\n" +
                "  total_time int default 0\n" +
                ");");
    }

    public void initBluetooth() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();// 获取蓝牙适配器
        receiver = new MediaReceiver(this);

        IntentFilter intentFilter = new IntentFilter();

        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);// 监视蓝牙设备与APP连接的状态
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        intentFilter.addAction(Intent.ACTION_HEADSET_PLUG);// 监听有线耳机的插拔
        intentFilter.addAction(Intent.ACTION_MEDIA_BUTTON);// TODO 重复?

        registerReceiver(this.receiver, intentFilter);// 注册广播 TODO 有报错
        receiver.registerReceiver(this);

    }

    public void initPlayer() {
        ;
    }

    public void initUI() {// 初始化ui,layout和dialog
        // 部件
        // 按钮
        button_play = findViewById(R.id.button_play);
        button_next = findViewById(R.id.button_next);
        button_prev = findViewById(R.id.button_prev);
        button_mix = findViewById(R.id.button_mix);
        button_bluetooth = findViewById(R.id.button_bluetooth);
        button_edit = findViewById(R.id.button_edit);
        button_mode = findViewById(R.id.button_mode);
        // 播放器进度部件
        seekBar = findViewById(R.id.music_bar);// 进度条
        totalTime = findViewById(R.id.total_time);// 音乐总时长
        curTime = findViewById(R.id.cur_time);// 音乐进度
        // 主体部件
        musicName = findViewById(R.id.music_name);
        scrollView = findViewById(R.id.layout_scroll);
        itemList = findViewById(R.id.item_list);

        button_mix.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (window_num == MUSIC_LIST) {
                    // 切换至歌单列表
                    mainList.listMix();
                    window_num = MIX_LIST;
                } else {
                    // 切换到当前歌单
                    if (playList.curMusic.length() > 0) {
                        mainList.listMusic(playList.curMix);
                        window_num = MUSIC_LIST;
                    } else {
                        infoToast(MusicList.this, "no current mix");
                    }
                }
            }
        });

        button_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (window_num) {
                    case MUSIC_LIST:
                        break;
                    case MIX_LIST:
                        break;
                }
            }
        });
    }

    public void initData() {
        // TODO 初始化核心功能
        mainList = new MainList(this);
        playList = new PlayList(this);
        playTime = new PlayTime(this, this);

        // TODO 恢复数据
        mainList.init();
        playList.init();
        playTime.init();
    }

    static public int cmd(String sql) {// 操作数据库
        try {
            database.execSQL(sql);
        } catch (SQLException e) {
            e.printStackTrace();
            MusicList.infoLog("database error: " + sql);
            return -1;
        }
        return 0;
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

        Pattern pattern = Pattern.compile("/+\\.[\\.]+");
        Matcher matcher = pattern.matcher(path);
        int count = 0;
        while (matcher.find()) {
            count++;
        }

        path = path.replaceAll("/+\\.[\\.]+", "");// 除去`/..`

        int index = path.length() - 1;
        if (path.charAt(index) == '/') {
            index--;
        }

        while (index >= 0) {
            if (path.charAt(index) == '/') {
                count--;
                if (count == 0) {
                    infoLog("simplified path: " + path.substring(0, index));
                    return path.substring(0, index);
                }
            }
            index--;
        }

        return "/";
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        ;// TODO
    }
}