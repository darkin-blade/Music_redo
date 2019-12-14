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
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.music_redo.device.BluetoothList;
import com.example.music_redo.device.MediaReceiver;
import com.example.music_redo.mix.MixEdit;
import com.example.music_redo.mix.MixNew;
import com.example.music_redo.mix.MixRename;
import com.example.music_redo.mix.MusicEdit;
import com.example.music_redo.mix.MusicMove;
import com.example.music_redo.mix.MusicSelect;
import com.example.music_redo.player.ListManager;
import com.example.music_redo.player.PlayList;
import com.example.music_redo.player.PlayTime;

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

    // dialog界面
    static public MixEdit mixEdit;// 编辑歌单
    static public MusicEdit musicEdit;// 歌单界面 编辑歌曲
    static public MixNew mixNew;// 新建歌单
    static public MixRename mixRename;// 重命名歌单

    // 核心功能
    // 设备管理
    public MediaReceiver receiver;// 接收`蓝牙/媒体`信号
    static public BluetoothList bluetoothList;// 蓝牙管理
    static public BluetoothAdapter bluetoothAdapter;// 蓝牙
    // 播放管理
    // TODO 播放模式
    // TODO 歌单管理
    // TODO 时间管理
    // 歌单管理
    static public ListManager listManager;// 主页面ui管理
    static public MusicSelect musicSelect;// 文件浏览器
    static public MusicMove musicMove;// 歌曲`添加至`歌单
    // 部件管理
    // TODO 锁屏
    // TODO 桌面部件
    // TODO 通知栏部件

    // 公共变量
    // TODO media信号处理
    static public Long myTime = System.currentTimeMillis();// 微秒时间
    static public int clickTimes;// 耳机信号次数

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
    static public final int BLUETOOTH_LIST = 9;// 蓝牙管理
    static public final int BLUETOOTH_EDIT = 10;// `蓝牙设备控制`窗口

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_player);

        initApp();
        initUI();
        initReceiver();
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

        // TODO 初始化数据库
    }

    public void initReceiver() {// TODO 将按键与其他action分离
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();// 获取蓝牙适配器
        receiver = new MediaReceiver();

        IntentFilter intentFilter = new IntentFilter();

        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);// 监视蓝牙设备与APP连接的状态
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        intentFilter.addAction(Intent.ACTION_HEADSET_PLUG);// 监听有线耳机的插拔
        intentFilter.addAction(Intent.ACTION_MEDIA_BUTTON);// TODO 重复?

        registerReceiver(this.receiver, intentFilter);// 注册广播
        receiver.registerReceiver(this);

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
        bluetoothList = new BluetoothList();// 蓝牙管理
        // 其余部件
        // TODO 通知栏部件
        // TODO 锁屏部件
        // TODO 桌面部件

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

        button_play.setBackgroundDrawable(getResources().getDrawable(R.drawable.player_play));// 启动时为暂停
        button_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MusicList.this, PlayTime.class);
                if (PlayTime.player.isPlaying() == true) {
                    intent.putExtra("cmd", "pause");
                } else {
                    intent.putExtra("cmd", "play");
                    intent.putExtra("mode", 0);
                }
                startService(intent);
            }
        });

        button_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 下一首
                Intent intent = new Intent(MusicList.this, PlayTime.class);
                intent.putExtra("cmd", "next");
                startService(intent);
            }
        });

        button_prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 上一首
                Intent intent = new Intent(MusicList.this, PlayTime.class);
                intent.putExtra("cmd", "prev");
                startService(intent);
            }
        });

        button_mix.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (window_num == MUSIC_LIST) {
                    // 切换至歌单列表
                    window_num = MIX_LIST;
                    listManager.listMix();
                    MusicList.listManager.showMix("mix_list");

                    Intent intent = new Intent(MusicList.this, PlayList.class);
                    intent.putExtra("cmd", "highlightMusic");
                    startService(intent);
                } else {
                    // 切换到当前歌单
                    if (PlayList.curMusic.length() > 0) {
                        listManager.listMusic(PlayList.curMix);
                        window_num = MUSIC_LIST;
                    } else {
                        infoToast(MusicList.this, "no current mix");
                    }
                }
            }
        });

        button_bluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 打开蓝牙管理器
                bluetoothList.show(getSupportFragmentManager(), "bluetooth");
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

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                ;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                ;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Intent intent = new Intent(MusicList.this, PlayTime.class);
                intent.putExtra("cmd", "getBar");
                startService(intent);
            }
        });

        musicName.setSelected(true);// 跑马灯
    }

    public void initData() {
        // 初始化核心功能
        listManager = new ListManager(this);

        // TODO 注意初始化顺序
        PlayTime.myActivity = this;
        PlayList.myActivity = this;

        Intent intent = new Intent(this, PlayTime.class);
        intent.putExtra("cmd", "init");
        startService(intent);
        listManager.init();
        intent = new Intent(this, PlayList.class);
        intent.putExtra("cmd", "init");
        startService(intent);
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

    static public String pathSimplify(String path) {// 简化路径
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
        Intent intent = new Intent(this, PlayList.class);
        intent.putExtra("cmd", "save");
        startService(intent);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        // 解决泄漏问题
        unregisterReceiver(receiver);

        Intent intent = new Intent(this, PlayList.class);
        intent.putExtra("cmd", "save");
        startService(intent);
        super.onDestroy();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        Intent intent = new Intent(this, PlayList.class);
        switch (window_num) {
            case MIX_NEW:
                window_num = MIX_LIST;
                if (dialog_result.equals("new")) {// 新建歌单
                    listManager.listMix();
                    intent.putExtra("cmd", "highlightMusic");
                }
                break;
            case MIX_EDIT:
                window_num = MIX_LIST;
                if (dialog_result.equals("delete")) {// 删除歌单
                    listManager.listMix();

                    // 尝试引发错误
                    intent.putExtra("curMix", PlayList.curMix);
                    intent.putExtra("curMusic", PlayList.curMusic);
                    intent.putExtra("mode", 2);
                }
                break;
            case MUSIC_SELECT:
                window_num = MUSIC_LIST;
                if (dialog_result.equals("add")) {// 添加歌曲
                    listManager.listMusic(listManager.curMix);// 无条件刷新列表

                    // 更新当前播放列表
                    intent.putExtra("curMix", PlayList.curMix);
                    intent.putExtra("curMusic", PlayList.curMusic);
                    intent.putExtra("mode", 2);
                }
                break;
            case MUSIC_EDIT:
                window_num = MUSIC_LIST;
                if (dialog_result.equals("delete")) {// 删除歌曲
                    listManager.listMusic(listManager.curMix);// 无条件刷新列表

                    // 更新当前播放列表
                    intent.putExtra("curMix", PlayList.curMix);
                    intent.putExtra("curMusic", PlayList.curMusic);
                    intent.putExtra("mode", 2);                }
                break;
            case MIX_RENAME:
                window_num = MUSIC_LIST;
                if (dialog_result.equals("rename")) {// 重命名歌单

                    // 只刷新mix
                    intent.putExtra("curMix", PlayList.curMix);
                    intent.putExtra("curMusic", PlayList.curMusic);
                    intent.putExtra("mode", 2);
                    listManager.showMix(listManager.curMix);
                }
                break;
            case MUSIC_MOVE:
                window_num = MUSIC_LIST;
                if (dialog_result.equals("add to")) {// 转移歌曲
                    if (PlayList.curMix.equals(listManager.curMix)) {// 播放正在浏览的歌单,此情况不可能?
                        listManager.listMusic(listManager.curMix);
                    }

                    // 更新当前播放列表
                    intent.putExtra("curMix", PlayList.curMix);
                    intent.putExtra("curMusic", PlayList.curMusic);
                    intent.putExtra("mode", 2);
                }
                break;
            case BLUETOOTH_LIST:// 蓝牙管理器
                window_num = bluetoothList.window_num;
                break;
            case BLUETOOTH_EDIT:// `修改蓝牙设备`窗口
                window_num = BLUETOOTH_LIST;
                break;
            default:
                return;
        }
        startService(intent);// TODO
    }
}