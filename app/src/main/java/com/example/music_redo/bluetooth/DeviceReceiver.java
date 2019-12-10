package com.example.music_redo.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;

import com.example.music_redo.MediaReceiver;
import com.example.music_redo.MusicList;

import static com.example.music_redo.bluetooth.BluetoothList.addresses;
import static com.example.music_redo.bluetooth.BluetoothList.devices;

public class DeviceReceiver extends BroadcastReceiver {
    Activity myActivity;

    public DeviceReceiver(Activity activity) {
        myActivity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action != null) {
            MusicList.infoLog("action: " + action);// TODO debug
            switch (action) {
                case BluetoothDevice.ACTION_FOUND:// 找到新的蓝牙设备
                    final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (device.getName() != null) {
                        // TODO 设备去重
                        if (addresses.indexOf(device.getAddress()) < 0) {
                            devices.add(device);// 添加设备
                            addresses.add(device.getAddress());
                            myActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    MusicList.bluetoothList.create_item(device.getName(), device.getAddress(), device, 0);
                                }
                            });
                        }
                        MusicList.infoLog("device [" + device.getName() + "], address [" + device.getAddress() + "]");
                    }
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:// 扫描完毕
                    MusicList.bluetoothList.listDevice();// 列举设备
                    MusicList.infoLog("discovery finished");
                    break;
                case BluetoothDevice.ACTION_PAIRING_REQUEST:
                    MusicList.infoLog("pairing request");
                    break;
                case BluetoothDevice.ACTION_BOND_STATE_CHANGED:// TODO 和配对有关
                    BluetoothDevice tmp = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    switch (tmp.getBondState()) {
                        case BluetoothDevice.BOND_BONDED:
                            MusicList.infoLog("bond bonded");
                            break;
                        case BluetoothDevice.BOND_BONDING:
                            MusicList.infoLog("bond bonding");
                            break;
                        case BluetoothDevice.BOND_NONE:
                            MusicList.infoLog("bond none");
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
