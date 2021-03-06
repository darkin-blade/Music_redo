package com.example.music_redo.device;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.example.music_redo.MusicList;
import com.example.music_redo.R;

import java.util.ArrayList;

import static com.example.music_redo.player.PlayTime.bluetoothAdapter;

public class BluetoothList extends DialogFragment {
    View myView;
    Activity myActivity;
    Context myContext;

    public int window_num;

    public LinearLayout layout;
    Button button_1;
    Button button_2;
    Button button_3;
    Button button_4;

    DeviceReceiver receiver;
    BluetoothEdit bluetoothEdit;

    static public ArrayList<BluetoothDevice> devices;// 蓝牙设备列表
    static public ArrayList<String> addresses;// 蓝牙设备地址列表

    @Override
    public void show(FragmentManager fragmentManager, String tag) {
        super.show(fragmentManager, tag);
    }

    @Override
    public void onDismiss(final DialogInterface dialog) {
        // TODO 处理泄漏
        myActivity.unregisterReceiver(receiver);

        // 停止扫描
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }

        super.onDismiss(dialog);
        Activity activity = getActivity();
        if (activity instanceof DialogInterface.OnDismissListener) {
            ((DialogInterface.OnDismissListener) activity).onDismiss(dialog);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_FRAME, android.R.style.Theme);// 关闭背景(点击外部不能取消)
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.bluetooth_list, container);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(0x00000000));// 背景透明

        initData();
        initUI();

        return myView;
    }

    public void initData() {
        MusicList.infoLog("init data");
        myActivity = getActivity();
        myContext = getContext();

        window_num = MusicList.window_num;// 保存之前的窗口号
        MusicList.window_num = MusicList.BLUETOOTH_LIST;// 修改窗口编号

        bluetoothEdit = new BluetoothEdit();
        receiver = new DeviceReceiver(getActivity());
        devices = new ArrayList<BluetoothDevice>();
        addresses = new ArrayList<>();

        // 注册receiver
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);// TODO 和配对有关
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);// 搜索设备
        intentFilter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);// TODO
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);// 搜索完毕
        myActivity.registerReceiver(receiver, intentFilter);// 注册广播 TODO 泄漏
        receiver.registerReceiver(getContext());
    }

    public void initUI() {// TODO 初始化按钮
        button_1 = myView.findViewById(R.id.button_1);
        button_2 = myView.findViewById(R.id.button_2);
        button_3 = myView.findViewById(R.id.button_3);
        button_4 = myView.findViewById(R.id.button_4);
        layout = myView.findViewById(R.id.main_list);
        layout.removeAllViews();

        button_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicList.dialog_result = "";
                dismiss();
            }
        });

        button_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanDevice();
            }
        });

        button_3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listPaired();
            }
        });

        button_4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {// 取消搜索
                if (bluetoothAdapter.isDiscovering()) {
                    bluetoothAdapter.cancelDiscovery();
                }
            }
        });
    }

    public void scanDevice() {
        // 清空
        layout.removeAllViews();
        if (bluetoothAdapter.isEnabled() == false) {
            bluetoothAdapter.enable();
        }
        if (bluetoothAdapter.isDiscovering() == true) {
            bluetoothAdapter.cancelDiscovery();// 强制暂停
        }

        // 清空之前的数据
        devices.clear();
        addresses.clear();

        // TODO 开始扫描
        bluetoothAdapter.startDiscovery();
        MusicList.infoToast(getContext(), "start scanning");

    }

    public void listPaired() {
        // 清空
        layout.removeAllViews();
        if (bluetoothAdapter.isEnabled() == false) {
            bluetoothAdapter.enable();
        }
        if (bluetoothAdapter.isDiscovering() == true) {
            bluetoothAdapter.cancelDiscovery();// 强制暂停
        }

        // 清空之前的数据
        devices.clear();
        addresses.clear();

        devices.addAll(bluetoothAdapter.getBondedDevices());
        for (int i = 0; i < devices.size(); i ++) {
            BluetoothDevice tmp = devices.get(i);
            create_item(tmp.getName(), tmp.getAddress(), tmp, 1);
        }
    }

    // TODO 列举item的参数
    public static final int
            item_height = 130,
            detail_margin_left = 10;

    public void create_item(final String item_name, final String item_detail, final BluetoothDevice device, int mode) {// mode: 0:未配对 1:已配对
        // TODO 解决一些智障蓝牙设备的连接问题
        if (getContext() == null) {
            MusicList.infoLog("is null: " + (myContext == null));
            return;
        }

        // 每一项 LL
        LinearLayout.LayoutParams itemParam = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, item_height);
        // 文字区 LL
        LinearLayout.LayoutParams detailParam = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        // 歌单名
        LinearLayout.LayoutParams nameParam = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT,1);
        // 歌曲数目
        LinearLayout.LayoutParams numberParam = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1);;

        // 新建实例
        final LinearLayout item = new LinearLayout(getContext());
        LinearLayout detail = new LinearLayout(getContext());
        TextView name = new TextView(getContext());
        TextView number = new TextView(getContext());

        item.setBackgroundResource(R.color.grey);
        item.setLayoutParams(itemParam);

        detailParam.setMargins(detail_margin_left, detail_margin_left, detail_margin_left, detail_margin_left);
        detail.setOrientation(LinearLayout.HORIZONTAL);// 水平
        detail.setBackgroundResource(R.color.grey);
        detail.setLayoutParams(detailParam);

        name.setGravity(Gravity.CENTER);
        name.setText(item_name);
        name.setLayoutParams(nameParam);

        number.setGravity(Gravity.CENTER);
        number.setText(item_detail);
        number.setLayoutParams(numberParam);

        // 区别已配对设备
        if (mode == 0) {// 未配对
            name.setTextColor(Color.rgb(0, 0, 0));
            number.setTextColor(Color.rgb(0, 0, 0));
        } else {// 已配对
            name.setTextColor(Color.rgb(230, 100, 60));
            number.setTextColor(Color.rgb(230, 100, 60));
        }

        // 合并ui
        detail.addView(name);
        detail.addView(number);
        item.addView(detail);
        layout.addView(item);

        item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO 蓝牙配对
                bluetoothEdit.show(getFragmentManager(), "edit bluetooth", device, item);
            }
        });
    }
}
