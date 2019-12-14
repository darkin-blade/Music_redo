package com.example.music_redo.player;

import android.app.Activity;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.example.music_redo.MusicList;
import com.example.music_redo.R;
import com.example.music_redo.mix.MusicDataBase;
import com.example.music_redo.widget.PlayNotification;
import com.example.music_redo.widget.PlayWidgetService;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PlayTime extends Service {
    static public Activity myActivity;

    static public MediaPlayer player;// 媒体播放器
    public MediaReceiver receiver;// 接收`蓝牙/媒体`信号
    static public BluetoothAdapter bluetoothAdapter;// 蓝牙

    // 时间管理
    static public int total_time;
    static public int cur_time;
    static public int duration;// 播放时间累计
    static public Thread musicPlay;

    // 用于部件交互
    static final int MODE_PLAY = 0;
    static final int MODE_PAUSE = 1;
    static final int MODE_NEXT = 2;
    static final int MODE_PREV = 3;
    static final int MODE_UPDATE = 4;

    public PlayTime() {
        MusicList.infoLog("playTime constructor");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initData();
        initPlayer();
        initReceiver();
    }

    public void initData() {
        total_time = 0;
        cur_time = 0;
        duration = 0;
    }

    public void initPlayer() {
        player = new MediaPlayer();
        player.setLooping(false);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);// 设置为音频
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {// 播放完毕回调函数
            @Override
            public void onCompletion(MediaPlayer mp) {
                // 下一首
                next();
            }
        });
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

    public void play(int mode) {
        /**
         * mode:
         * 0: 播放
         * 1: 加载
         * 2: 加载,置零
         * 3: 加载,置零,播放
         */
        // 中断正在播放的线程
        if (musicPlay != null && musicPlay.isAlive()) {
            musicPlay.interrupt();
        }

        if (mode >= 1) {
            // 加载
            try {
                player.reset();
                player.setDataSource(PlayList.curMusic);
                player.prepare();
                total_time = player.getDuration();
                if (MusicList.window_num != 0) {// TODO
                    MusicList.seekBar.setMax(total_time);
                }

                // 设置时长ui
                SimpleDateFormat format = new SimpleDateFormat("mm:ss");
                Date tmp = new Date(total_time);
                final String formatTime = format.format(tmp);
                updateUI(new Runnable() {
                    @Override
                    public void run() {
                        MusicList.totalTime.setText(formatTime);
                    }
                });
            } catch (IOException e) {// prepare failed
                e.printStackTrace();

                // 歌曲无法播放
                MusicDataBase.deleteMusic(PlayList.curMix, PlayList.curMusic);

                // 只刷新歌单
                Intent intent = new Intent(this, PlayList.class);
                intent.putExtra("cmd", "loadMix");
                intent.putExtra("curMix", PlayList.curMix);
                intent.putExtra("curMusic", PlayList.curMusic);
                intent.putExtra("mode", 2);
                startService(intent);// TODO 不需要highlightMusic

                if (MusicList.window_num != MusicList.MIX_LIST) {// 如果不是歌单列表
                    MusicList.listManager.listMusic(PlayList.curMix);
                }
                intent = new Intent(this, PlayList.class);
                intent.putExtra("cmd", "stopMusic");
                intent.putExtra("mode", 1);
                startService(intent);
                return;
            } catch (IllegalStateException e) {
                e.printStackTrace();

                // TODO
                Intent intent = new Intent(this, PlayList.class);
                intent.putExtra("cmd", "stopMusic");
                intent.putExtra("mode", 1);
                startService(intent);
                return;
            }

            if (mode >= 2) {
                // 置零
                cur_time = 0;
            }
            setTime();
            setBar();
            if (mode == 1) {
                getBar();// 控制进度条
            }

            if (mode <= 2) {
                return;
            }
        }

        // 播放
        player.start();
        if (MusicList.window_num != 0) {
            MusicList.button_play.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.player_pause));
        }

        updateService(MODE_PLAY);

        musicPlay = new Thread(new Runnable() {
            @Override
            public void run() {
                while (Thread.currentThread().isInterrupted() == false) {
                    try {
                        if (player.isPlaying() == false) {
                            break;
                        }

                        cur_time = player.getCurrentPosition();
                        setTime();
                        setBar();

                        // 统计时间
                        duration ++;
//                        MusicList.infoLog("duration: " + duration);
                        Thread.sleep(1000);// 每一秒更新一次 TODO 注意该语句的位置,防止触发OnComplete
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        break;
                    }
                }
            }
        });
        musicPlay.start();
    }

    public void pause() {
        if (player.isPlaying() == true) {
            player.pause();
            if (MusicList.window_num != 0) {
                MusicList.button_play.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.player_play));
            }
            musicPlay.interrupt();
            updateService(MODE_PAUSE);
        }
    }

    public void reset() {
        player.reset();
        cur_time = 0;
        setBar();
    }

    public void next() {
        Intent intent = new Intent(this, PlayList.class);
        intent.putExtra("cmd", "loadMusic");
        intent.putExtra("mode", 1);
        startService(intent);

        intent = new Intent(this, PlayList.class);
        intent.putExtra("cmd", "highlightMusic");
        startService(intent);

        updateService(MODE_NEXT);
    }

    public void prev() {
        Intent intent = new Intent(this, PlayList.class);
        intent.putExtra("cmd", "loadMusic");
        intent.putExtra("mode", 2);
        startService(intent);

        intent = new Intent(this, PlayList.class);
        intent.putExtra("cmd", "highlightMusic");
        startService(intent);

        updateService(MODE_PREV);
    }

    public void getBar() {// 从进度条更新播放进度
        if (MusicList.window_num != 0) {
            int curProgress = MusicList.seekBar.getProgress();

            // 调整时间
            cur_time = curProgress;
            player.seekTo(cur_time);
            setTime();
        }
    }

    public void setBar() {// 更新进度条
        updateUI(new Runnable() {
            @Override
            public void run() {
                MusicList.seekBar.setProgress(cur_time);
            }
        });
    }

    public void setTime() {// 更新时间
        SimpleDateFormat format = new SimpleDateFormat("mm:ss");
        Date tmp = new Date(cur_time);
        final String formatTime = format.format(tmp);

        updateUI(new Runnable() {
            @Override
            public void run() {
                MusicList.curTime.setText(formatTime);
            }
        });
        updateService(MODE_UPDATE);// 更新状态栏进度条
    }

    public void updateService(int mode) {
        Intent intent;
        switch (mode) {
            default:
                intent = new Intent(this, PlayNotification.class);
                intent.putExtra("mode", mode);
                startService(intent);
                intent = new Intent(this, PlayWidgetService.class);
                intent.putExtra("mode", mode);
                startService(intent);
                break;
        }
    }

    public void updateUI(Runnable runnable) {
        if (myActivity == null) {
            return;
        }

        myActivity.runOnUiThread(runnable);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            return  START_STICKY;
        }

        String command = intent.getStringExtra("cmd");
        if (command != null) {
            if (command.equals("init")) {// 初始化
                MusicList.infoLog("play time init");
            } else if (command.equals("play")) {
                int mode = intent.getIntExtra("mode", -1);
                play(mode);
            } else if (command.equals("pause")) {
                pause();
            } else if (command.equals("next")) {
                next();
            } else if (command.equals("prev")) {
                prev();
            } else if (command.equals("reset")) {
                reset();
            } else if (command.equals("getBar")) {
                getBar();
            }
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        // 解决泄漏问题
        unregisterReceiver(receiver);

        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
