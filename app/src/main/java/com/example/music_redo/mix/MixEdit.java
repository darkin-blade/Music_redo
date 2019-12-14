package com.example.music_redo.mix;

import android.app.Activity;
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

public class MixEdit extends DialogFragment {
    public View myView;
    Button button_cancel;
    Button button_delete;
    Button button_new;
    TextView textView;// 显示选中的歌单数目

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
        myView = inflater.inflate(R.layout.mix_edit, container);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(0x00000000));// 背景透明

        initData();
        initUI();

        return myView;
    }
    
    public void initData() {
        MusicList.window_num = MusicList.MIX_EDIT;
    }
    
    public void initUI() {
        textView = myView.findViewById(R.id.edit_title);
        button_cancel = myView.findViewById(R.id.button_cancel);
        button_delete = myView.findViewById(R.id.button_delete);
        button_new = myView.findViewById(R.id.button_new);

        textView.setText(MusicList.listManager.mixSelected.size() + " mix selected");

        button_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicList.dialog_result = "cancel";
                dismiss();
            }
        });

        button_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < MusicList.listManager.mixSelected.size(); i ++) {
                    String tmp = MusicList.listManager.mixSelected.get(i);
                    int result = MusicDataBase.deleteMix(tmp);
                }
                MusicList.listManager.mixSelected.clear();
                MusicList.dialog_result = "delete";
                dismiss();
            }
        });

        button_new.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicList.dialog_result = "";
                dismiss();
                MusicList.mixNew.show(getFragmentManager(), "new mix");// TODO 编号
            }
        });
    }
}
