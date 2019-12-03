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

    public MediaReceiver(Context context) {
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
                        // TODO 强制暂停
                    } else if (mediaState == 1) {// 插入耳机
                    }
                    break;

                // 蓝牙连接状态改变
                // 安卓端主动改变蓝牙状态
                case BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED:
                    int bluetoothState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);// 获取蓝牙状态
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
                // 蓝牙设备主动改变状态
                case BluetoothDevice.ACTION_ACL_CONNECTED:// 蓝牙连接设备
                    break;
                case BluetoothDevice.ACTION_ACL_DISCONNECTED:// 蓝牙断开设备
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
                            break;
                        case KeyEvent.KEYCODE_MEDIA_PLAY:// 播放 126
                        case KeyEvent.KEYCODE_MEDIA_PAUSE:// 暂停 127
                            // TODO 无差别对待 播放和暂停
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
