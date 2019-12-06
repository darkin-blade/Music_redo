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

import com.example.music_redo.components.MixEdit;
import com.example.music_redo.components.MixNew;
import com.example.music_redo.components.MixRename;
import com.example.music_redo.components.MusicEdit;
import com.example.music_redo.components.MusicMove;
import com.example.music_redo.components.MusicSelect;

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
    static public TextView mixName;// 当前浏览的歌单名
    static public TextView musicName;// 歌名
    static public ScrollView scrollView;// 滚动界面
    static public LinearLayout itemList;// 列表部分

    // ui功能
    // dialog界面
    static public MixEdit mixEdit;// 编辑歌单
    static public MusicEdit musicEdit;// 歌单界面 编辑歌曲
    static public MixNew mixNew;// 新建歌单
    static public MixRename mixRename;// 重命名歌单
    // ui界面
    static public MusicSelect musicSelect;// 文件浏览器
    static public MusicMove musicMove;// 歌曲`添加至`歌单
    // TODO 播放模式
    // TODO 蓝牙管理

    // 公共变量
    static public String appPath;
    static public SQLiteDatabase database;
    // TODO media信号处理
    static public Long myTime = System.currentTimeMillis();// 微秒时间
    static public int clickTimes;// TODO 耳机信号次数

    // 功能代号
    static public int window_num;
    static public String dialog_result;
    static public final int MUSIC_LIST = 0;// 主界面
    static public final int MIX_LIST = 1;// 歌单列表
    static public final int MUSIC_MOVE = 3;// `添加至`列表
    static public final int MUSIC_SELECT = 4;// 文件管理器
    static public final int MIX_EDIT = 5;// 歌单列表管理歌单
    static public final int MUSIC_EDIT = 6;// 歌曲列表管理歌曲
    static public final int MIX_NEW = 7;// 新建歌单
    static public final int MIX_RENAME = 8;// 重命名歌单

    // 核心组件
    static public MediaPlayer player;// 媒体播放器
    public BluetoothAdapter bluetoothAdapter;// 蓝牙
    // 核心功能
    public MediaReceiver receiver;// 接收`蓝牙/媒体`信号
    static public ListManager listManager;
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
        dialog_result = "";

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
        // 功能界面
        // dialog
        mixEdit = new MixEdit();
        mixNew = new MixNew();
        musicEdit = new MusicEdit();
        mixRename = new MixRename();
        // layout ui
        musicMove = new MusicMove();
        musicSelect = new MusicSelect();
        // TODO 播放模式
        // TODO 蓝牙管理

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
        mixName = findViewById(R.id.cur_mix);
        musicName = findViewById(R.id.cur_music);
        scrollView = findViewById(R.id.layout_scroll);
        itemList = findViewById(R.id.item_list);

        button_mix.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (window_num == MUSIC_LIST) {
                    // 切换至歌单列表
                    window_num = MIX_LIST;
                    listManager.listMix();
                    MusicList.playList.highlightMusic();
                    MusicList.listManager.showMix("");// TODO 歌单名
                } else {
                    // 切换到当前歌单
                    if (playList.curMusic.length() > 0) {
                        listManager.listMusic(playList.curMix);
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
                infoLog("edit window_num: " + window_num);
                if (window_num == MUSIC_LIST) {
                    musicEdit.show(getSupportFragmentManager(), "edit music");
                } else if (window_num == MIX_LIST) {
                    mixEdit.show(getSupportFragmentManager(), "edit mix");
                }
            }
        });
    }

    public void initData() {
        // TODO 初始化核心功能
        listManager = new ListManager(this);
        playList = new PlayList(this);
        playTime = new PlayTime(this, this);

        // TODO 恢复数据
        listManager.init();
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

    static public int deleteMusic(String mixName, String musicPath) {
        return cmd("delete from " + mixName + "\n" +
                "where path = '" + musicPath + "';");
    }

    static public int deleteMix(String mixName) {
        int result = cmd("delete from mix_list where name = '" + mixName + "';");// 从歌单列表删除
        if (result != 0) {
            return -1;// 从歌单列表删除歌单失败
        }

        result = cmd("drop table " + mixName +";");
        if (result != 0) {
            return -2;// 删除歌单失败
        } else {
            return 0;
        }
    }

    static public int createMix(String mixName) {
        if (mixName == null || mixName.length() == 0 || mixName.length() >= 32
                || mixName.equals("mix_list") || mixName.equals("user_data") ||
                (mixName.charAt(0) >= '0' && mixName.charAt(0) <= '9')) {// 歌单名不能为空,歌单名不能为关键字
            return -1;// 无效歌单名
        }

        // 插入到歌单列表`mix_list`
        int result = cmd("insert into mix_list (name)\n" +
                "  values\n" +
                "  ('" + mixName + "');");

        if (result != 0) {
            return -2;// 插入歌单列表失败
        }

        // 新建歌单`mixName`
        result = cmd("create table if not exists '" + mixName + "' (\n" +
                "  path varchar (128) not null,\n" +
                "  name varchar (64) not null,\n" +
                "  count int default 0,\n" +
                "  primary key (path)" +
                ");");

        if (result != 0) {
            deleteMix(mixName);
            return -3;// 创建歌单失败
        } else {
            return 0;// 创建歌单成功
        }
    }

    static public int renameMix(String oldName, String newName) {
        // 更改歌单名
        int result = cmd("alter table " + oldName + " rename to " + newName + ";");
        if (result != 0) {
            return -1;// TODO 删除table失败
        }

        result = cmd("update mix_list set name = '" + newName + "' where name = '" + oldName + "';");
        if (result != 0) {
            return -2;// 从歌单列表删除失败
        }
        return 0;
    }

    static public int addMusic(String mixName, String musicPath) {
        return cmd("insert into " + mixName + " (path, name, count)\n" +
                "  values\n" +
                "  ('" + musicPath + "', '" + musicPath.replaceAll(".*/", "") + "', 0);");
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
    public void onPause() {
        playList.save();
        super.onPause();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        infoLog(window_num + ": " + dialog_result);
        switch (window_num) {
            case MIX_NEW:
                window_num = MIX_LIST;
                if (dialog_result.equals("new")) {// 新建歌单
                    listManager.listMix();
                    playList.highlightMusic();
                }
                break;
            case MIX_EDIT:
                window_num = MIX_LIST;
                if (dialog_result.equals("delete")) {// 删除歌单
                    listManager.listMix();
                    if (playList.loadMix(playList.curMix, playList.curMusic, 2) == 0) {// 尝试引发错误
                        playList.highlightMusic();
                    }
                }
                break;
            case MUSIC_SELECT:
                window_num = MUSIC_LIST;
                if (dialog_result.equals("add")) {// 添加歌曲
                    listManager.listMusic(listManager.curMix);// 无条件刷新列表
                    if (playList.loadMix(playList.curMix, playList.curMusic, 2) == 0) {// 更新当前播放列表
                        playList.highlightMusic();
                    }
                }
                break;
            case MUSIC_EDIT:
                window_num = MUSIC_LIST;
                if (dialog_result.equals("delete")) {// 删除歌曲
                    listManager.listMusic(listManager.curMix);// 无条件刷新列表
                    if (playList.loadMix(playList.curMix, playList.curMusic, 2) == 0) {// 更新当前播放列表
                        playList.highlightMusic();
                    }
                }
                break;
            case MIX_RENAME:
                window_num = MUSIC_LIST;
                if (dialog_result.equals("rename")) {// 重命名歌单
                    if (playList.loadMix(playList.curMix, playList.curMusic, 2) == 0) {// 只刷新mix
                        playList.highlightMusic();
                    }
                    listManager.showMix(listManager.curMix);
                }
                break;
            case MUSIC_MOVE:// TODO
                window_num = MUSIC_LIST;
                if (dialog_result.equals("add to")) {// 转移歌曲
                    if (playList.curMix.equals(listManager.curMix)) {// 播放正在浏览的歌单 TODO 此情况不可能
                        listManager.listMusic(listManager.curMix);
                    }
                    if (playList.loadMix(playList.curMix, playList.curMusic, 2) == 0) {// 更新当前播放列表
                        playList.highlightMusic();
                    }
                }
        }
    }
}