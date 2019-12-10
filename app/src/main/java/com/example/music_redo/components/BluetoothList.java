package com.example.music_redo.components;

import android.app.Activity;
import android.content.DialogInterface;
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

public class BluetoothList extends DialogFragment {
    public View myView;

    public int window_num;

    Button button_1;
    Button button_2;
    Button button_3;
    Button button_4;
    Button button_5;
    LinearLayout layout;

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
        myView = inflater.inflate(R.layout.bluetooth_list, container);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(0x00000000));// 背景透明

        initData();
        initUI();
        listMix();

        return myView;
    }

    public void initData() {
        window_num = MusicList.window_num;// 保存之前的窗口号
        MusicList.window_num = MusicList.BLUETOOTH_LIST;// 修改窗口编号
    }

    public void initUI() {// TODO 初始化按钮
        button_1 = myView.findViewById(R.id.button_1);
        button_2 = myView.findViewById(R.id.button_2);
        button_3 = myView.findViewById(R.id.button_3);
        button_4 = myView.findViewById(R.id.button_4);
        button_5 = myView.findViewById(R.id.button_5);
        layout = myView.findViewById(R.id.main_list);

        button_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicList.dialog_result = "";
                dismiss();
            }
        });
    }

    public void listMix() {
        // 清空
        layout.removeAllViews();

        // 列举所有歌单
//        Cursor cursor = MusicList.database.query(
//                "mix_list",// 歌单列表
//                new String[]{"name"},
//                null,
//                null,
//                null,
//                null,
//                "name");
//
//        if (cursor.moveToFirst()) {// TODO 判断非空
//            do {
//                String mix_name = cursor.getString(0);// 获取歌单名
//                Cursor cursor_count = MusicList.database.query(
//                        mix_name,// 歌单详情
//                        new String[]{"path", "name", "count"},
//                        null,
//                        null,
//                        null,
//                        null,
//                        "name");// 统计歌单内歌曲数目
//                create_item(mix_name, "total: " + cursor_count.getCount(), 0);// TODO 列举歌单
//            } while (cursor.moveToNext());
//        } else {
//            MusicList.infoToast(getContext(), "no mix");
//        }
//        cursor.close();

    }

    // TODO 列举歌单的参数
    public static final int
            item_height = 130,
            detail_margin_left = 10;

    public void create_item(final String item_name, final String item_detail, int mode) {// mode: 0:歌单 1:歌曲
        LinearLayout layout = myView.findViewById(R.id.mix_list);
        // 每一项 LL
        LinearLayout.LayoutParams itemParam = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, item_height);
        // 文字区 LL
        LinearLayout.LayoutParams detailParam = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        // 歌单名
        LinearLayout.LayoutParams nameParam = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT,2);
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

        // 合并ui
        detail.addView(name);
        detail.addView(number);
        item.addView(detail);
        layout.addView(item);

        item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO 添加到该歌单
                for (int i = 0; i < MusicList.listManager.musicSelected.size(); i ++) {
                    String tmp = MusicList.listManager.musicSelected.get(i);
                    MusicList.addMusic(item_name, tmp);
                }
                MusicList.dialog_result = "add to";
                dismiss();// TODO
            }
        });
    }
}
