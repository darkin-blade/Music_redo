package com.example.music_redo.components;

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

public class MusicEdit extends DialogFragment {
    public View myView;
    Button button_add;
    Button button_cancel;
    Button button_delete;
    Button button_move;
    Button button_rename;
    TextView textView;// 显示选中的歌曲数目

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
        myView = inflater.inflate(R.layout.music_edit, container);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(0x00000000));// 背景透明

        initData();
        initUI();

        return myView;
    }

    public void initData() {
        MusicList.window_num = MusicList.MUSIC_EDIT;
    }

    public void initUI() {
        textView = myView.findViewById(R.id.edit_title);
        button_add = myView.findViewById(R.id.button_add);
        button_cancel = myView.findViewById(R.id.button_cancel);
        button_delete = myView.findViewById(R.id.button_delete);
        button_move = myView.findViewById(R.id.button_move);
        button_rename = myView.findViewById(R.id.button_rename);

        textView.setText(MusicList.listManager.musicSelected.size() + " music selected");

        button_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicList.dialog_result = "";
                dismiss();
                MusicList.musicSelect.show(getFragmentManager(), "add music");
            }
        });

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
                for (int i = 0; i < MusicList.listManager.musicSelected.size(); i ++) {
                    String tmp = MusicList.listManager.musicSelected.get(i);
                    int result = MusicList.deleteMusic(MusicList.listManager.curMix, tmp);
                }
                MusicList.listManager.musicSelected.clear();
                MusicList.dialog_result = "delete";
                dismiss();
            }
        });

        button_move.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicList.dialog_result = "";
                dismiss();
                MusicList.musicMove.show(getFragmentManager(), "move music");
            }
        });

        button_rename.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicList.dialog_result = "";
                dismiss();
                MusicList.mixRename.show(getFragmentManager(), "rename mix");
            }
        });
    }
}