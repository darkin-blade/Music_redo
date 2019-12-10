package com.example.music_redo.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.example.music_redo.MusicList;
import com.example.music_redo.R;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class BluetoothEdit extends DialogFragment {
    View myView;
    BluetoothDevice device;

    TextView textView;
    Button button_1;
    Button button_2;
    Button button_3;
    Button button_4;

    public void show(FragmentManager fragmentManager, String tag, BluetoothDevice device) {
        this.device = device;
        super.show(fragmentManager, tag);
    }

    @Override
    public void onDismiss(final DialogInterface dialog) {
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
        myView = inflater.inflate(R.layout.mix_edit, container);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(0x00000000));// 背景透明

        initData();
        initUI();

        return myView;
    }

    public void initData() {
        MusicList.window_num = MusicList.BLUETOOTH_EDIT;
    }

    public void initUI() {
        textView = myView.findViewById(R.id.edit_title);
        button_1 = myView.findViewById(R.id.button_1);
        button_2 = myView.findViewById(R.id.button_2);
        button_3 = myView.findViewById(R.id.button_3);
        button_4 = myView.findViewById(R.id.button_4);

        textView.setText(device.getName() + "\n" + device.getAddress());

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
                // 连接设备
                try {
                    Method createBond = device.getClass().getMethod("createBond");
                    Boolean result = (Boolean) createBond.invoke(device);
                    MusicList.infoLog("link result: " + result);
                } catch (NoSuchMethodException e) {// getMethod
                    e.printStackTrace();
                } catch (IllegalAccessException e) {// invoke
                    e.printStackTrace();
                } catch (InvocationTargetException e) {// invoke
                    e.printStackTrace();
                }
                MusicList.dialog_result = "";
                dismiss();
            }
        });

        button_3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO 与蓝牙设备进行配对
                MusicList.dialog_result = "";
                dismiss();
            }
        });
    }
}