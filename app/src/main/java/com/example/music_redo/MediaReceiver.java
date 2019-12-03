package com.example.music_redo;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.view.KeyEvent;

public class MediaReceiver extends BroadcastReceiver {
    public Context myContext = null;

    public MediaReceiver() {// 系统会自动调用无参的构造方法
        ;// 不能直接调用 mContext
    }

    public MediaReceiver(Context context) {// TODO 暂不清楚监测出现重复是否是因为有参构造方法造成
        this.myContext = context;
    }

    @Override
    public void onReceive(Context context, Intent intent) {// 接收信号
//        MainPlayer.infoLog("receive");
        String action = intent.getAction();
        if (action != null) {
            MainPlayer.infoLog("action: " + action);// TODO debug
            switch (action) {
                // 有线耳机状态改变
                case Intent.ACTION_HEADSET_PLUG:
                    int mediaState = intent.getIntExtra("state", 0);// 判断插拔
                    if (mediaState == 0) {// 拔出耳机
                        if (MainPlayer.player.isPlaying()) {
                            MainPlayer.button_play.callOnClick();// TODO 强制暂停
                        }
                        MainPlayer.infoLog("remove headset");
                    } else if (mediaState == 1) {// 插入耳机
                        MainPlayer.infoLog("plug headset");
                    }
                    break;

                // 蓝牙连接状态改变
                case BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED:// 安卓端主动改变蓝牙状态
                    int bluetoothState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);// 获取蓝牙状态
                    MainPlayer.infoToast(myContext, "bluetooth state todo");// TODO
                    switch (bluetoothState) {
                        case BluetoothAdapter.STATE_TURNING_ON:
                            MainPlayer.infoLog("turning on");
                            break;
                        case BluetoothAdapter.STATE_ON:
                            MainPlayer.infoLog("on");
                            break;
                        case BluetoothAdapter.STATE_TURNING_OFF:
                            MainPlayer.infoLog("turning off");
                            break;
                        case BluetoothAdapter.STATE_OFF:
                            MainPlayer.infoLog("off");
                            break;
                    }
                    break;
                case BluetoothDevice.ACTION_ACL_CONNECTED:// 蓝牙设备主动改变状态
                    MainPlayer.infoLog("connected");
                    break;
                case BluetoothDevice.ACTION_ACL_DISCONNECTED:// 蓝牙设备主动改变状态
                    MainPlayer.infoLog("disconnected");
                    if (MainPlayer.player.isPlaying()) {
                        MainPlayer.button_play.callOnClick();// TODO 强制暂停
                    }
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
                    MainPlayer.infoLog("media button: " + keycode);
                    switch (keycode) {
                        case KeyEvent.KEYCODE_MEDIA_NEXT:// TODO 下一首 87
                            MainPlayer.infoLog("next");
                            break;
                        case KeyEvent.KEYCODE_HEADSETHOOK:// 播放/暂停 79
                            // TODO 切歌
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    Long tmp = System.currentTimeMillis();
                                    Long timeDiff = tmp - MainPlayer.myTime;
                                    MainPlayer.myTime = tmp;
                                    MainPlayer.infoLog("time diff: " + timeDiff);
                                    if (timeDiff < 500) {// TODO 累计
                                        MainPlayer.clickTimes ++;
                                    }

                                    int last_click_times = MainPlayer.clickTimes;// 之前累积的次数
                                    try {
                                        Thread.sleep(500);// TODO 延迟
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }

                                    if (last_click_times == MainPlayer.clickTimes) {// TODO 忽略4次以上的点击
                                        if (last_click_times == -1) {
                                            MainPlayer.infoLog("click -1 time???");
                                        } else if (last_click_times == 0) {
                                        } else if (last_click_times == 1) {// TODO 下一首
                                            MainPlayer.infoLog("todo next");
                                        } else if (last_click_times == 2) {// TODO 上一首
                                            MainPlayer.infoLog("todo last");
                                        }
                                        MainPlayer.clickTimes = 0;// TODO 累计清零
                                    } else {
                                        MainPlayer.infoLog("click times: " + last_click_times + "/" + MainPlayer.clickTimes);
                                    }
                                }
                            }).start();
                            break;
                        case KeyEvent.KEYCODE_MEDIA_PLAY:// 播放 126
                        case KeyEvent.KEYCODE_MEDIA_PAUSE:// 暂停 127
                            // TODO 避免重复检测
                            MainPlayer.button_play.callOnClick();// TODO 无差别对待 播放和暂停
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
