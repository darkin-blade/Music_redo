package com.example.music_redo;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.view.KeyEvent;

import static com.example.music_redo.MusicList.playTime;
import static com.example.music_redo.MusicList.player;

public class MediaReceiver extends BroadcastReceiver {
    public Context myContext = null;

    public MediaReceiver() {// 系统会自动调用无参的构造方法
        ;// 不能直接调用 mContext
    }

    public MediaReceiver(Context context) {
        this.myContext = context;
    }

    @Override
    public void onReceive(Context context, Intent intent) {// 接收信号
        String action = intent.getAction();
        if (action != null) {
            MusicList.infoLog("action: " + action);// TODO debug
            switch (action) {
                // 有线耳机状态改变
                case Intent.ACTION_HEADSET_PLUG:
                    int mediaState = intent.getIntExtra("state", 0);// 判断插拔
                    if (mediaState == 0) {// 拔出耳机
                        playTime.pause();
                    } else if (mediaState == 1) {// 插入耳机
                    }
                    break;

                // 蓝牙连接状态改变
                // TODO 感觉没用
                case BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED:
                    int bluetoothState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);// 获取蓝牙状态
                    switch (bluetoothState) {
                        case BluetoothAdapter.STATE_TURNING_ON:
                            MusicList.infoLog("turning on");
                            break;
                        case BluetoothAdapter.STATE_ON:
                            MusicList.infoLog("on");
                            break;
                        case BluetoothAdapter.STATE_TURNING_OFF:
                            MusicList.infoLog("turning off");
                            break;
                        case BluetoothAdapter.STATE_OFF:
                            MusicList.infoLog("off");
                            break;
                    }
                    break;
                // TODO
                case BluetoothDevice.ACTION_ACL_CONNECTED:// 连接
                    break;
                case BluetoothDevice.ACTION_ACL_DISCONNECTED:// 断开
                    playTime.pause();
                    break;

                // 接收蓝牙/媒体按键信号
                case Intent.ACTION_MEDIA_BUTTON:
                    KeyEvent keyEvent = (KeyEvent) intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);// 获取键码

                    // 如果是down,忽略
                    if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                        break;// TODO
                    }

                    // up
                    int keycode = keyEvent.getKeyCode();
                    MusicList.infoLog("media button: " + keycode);
                    switch (keycode) {
                        case KeyEvent.KEYCODE_MEDIA_NEXT:// TODO 下一首 87
                            MusicList.infoLog("next");
                            playTime.next();
                            break;
                        case KeyEvent.KEYCODE_HEADSETHOOK:// 媒体键(播放/暂停) 79
                            // TODO 切歌
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    Long tmp = System.currentTimeMillis();
                                    Long timeDiff = tmp - MusicList.myTime;
                                    MusicList.myTime = tmp;
                                    MusicList.infoLog("time diff: " + timeDiff);
                                    if (timeDiff < 500) {// TODO 累计
                                        MusicList.clickTimes ++;
                                    }

                                    int last_click_times = MusicList.clickTimes;// 之前累积的次数
                                    try {
                                        Thread.sleep(500);// TODO 延迟
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }

                                    if (last_click_times == MusicList.clickTimes) {// TODO 忽略4次以上的点击
                                        if (last_click_times == -1) {
                                            MusicList.infoLog("click -1 time???");
                                        } else if (last_click_times == 0) {
                                            if (player.isPlaying() == true) {// 播放/暂停
                                                playTime.pause();
                                            } else {
                                                playTime.play(0);
                                            }
                                        } else if (last_click_times == 1) {// 下一首
                                            playTime.next();
                                            MusicList.infoLog("todo next");
                                        } else if (last_click_times == 2) {// 上一首
                                            playTime.prev();
                                            MusicList.infoLog("todo last");
                                        }
                                        MusicList.clickTimes = 0;// TODO 累计清零
                                    } else {
                                        MusicList.infoLog("click times: " + last_click_times + "/" + MusicList.clickTimes);
                                    }
                                }
                            }).start();
                            break;
                        case KeyEvent.KEYCODE_MEDIA_PLAY:// 播放 126
                        case KeyEvent.KEYCODE_MEDIA_PAUSE:// 暂停 127
                            if (player.isPlaying()) {
                                playTime.pause();
                            } else {
                                playTime.play(0);
                            }
                            break;
                    }
                    break;
            }
        }
    }

    public void registerReceiver(Context context) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        ComponentName name = new ComponentName(context.getPackageName(), MediaReceiver.class.getName());
        audioManager.registerMediaButtonEventReceiver(name);
    }

    public void unregisterReceiver(Context context){
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        ComponentName name = new ComponentName(context.getPackageName(), MediaReceiver.class.getName());
        audioManager.unregisterMediaButtonEventReceiver(name);
    }
}
