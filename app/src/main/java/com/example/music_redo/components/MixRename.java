package com.example.music_redo.components;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.example.music_redo.MusicList;
import com.example.music_redo.R;

public class MixRename extends DialogFragment {
    public View myView;
    Button button_rename;
    Button button_cancel;
    EditText editText;// 新建歌单的歌单名
    TextView textView;// 标题

    @Override
    public void show(FragmentManager fragmentManager, String tag) {
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
        myView = inflater.inflate(R.layout.mix_rename, container);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(0x00000000));// 背景透明

        initData();
        initUI();

        return myView;
    }

    public void initData() {
        MusicList.window_num = MusicList.MIX_RENAME;
    }

    public void initUI() {
        textView = myView.findViewById(R.id.edit_title);
        editText = myView.findViewById(R.id.mix_name);
        button_rename = myView.findViewById(R.id.button_rename);
        button_cancel = myView.findViewById(R.id.button_cancel);

        textView.setText("Rename " + MusicList.listManager.curMix);

        button_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicList.dialog_result = -1;
                dismiss();
            }
        });

        button_rename.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tmp = editText.getText().toString();
                int result = MusicList.renameMix(MusicList.listManager.curMix, tmp);// TODO 重命名歌单
                switch (result) {
                    case 0:
                        MusicList.infoLog("rename mix " + tmp + " succeed");
                        break;
                    default:
                        MusicList.infoLog("rename mix " + tmp + " failed");
                        MusicList.infoToast(getContext(), "rename mix " + tmp + " failed");
                        break;
                }

                // 刷新ui
                MusicList.dialog_result = 1;
                dismiss();
            }
        });
    }
}
