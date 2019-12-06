package com.example.music_redo;

import android.content.Context;
import android.database.Cursor;
import android.view.Gravity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class ListManager {
    Context myContext;

    // 浏览管理
    public String curMix;

    // 复选管理
    public ArrayList<String> mixSelected;
    public ArrayList<String> musicSelected;

    public ListManager(Context context) {
        myContext = context;
    }

    public void init() {
        curMix = null;
        mixSelected = new ArrayList<String>();
        musicSelected = new ArrayList<String>();
    }

    public void listMix() {
        curMix = null;// 置null当前歌单
        musicSelected.clear();// 清空选中歌曲
        MusicList.itemList.removeAllViews();// 清空ui
        MusicList.window_num = MusicList.MIX_LIST;

        Cursor cursor = MusicList.database.query(
                "mix_list",// 歌单列表
                new String[]{"name"},
                null,
                null,
                null,
                null,
                "name");

        if (cursor.moveToFirst()) {
            // 列举所有歌单
            do {
                String mix_name = cursor.getString(0);// 获取歌单名
                Cursor cursor_count = MusicList.database.query(
                        mix_name,// 歌单详情
                        new String[]{"path", "name", "count"},
                        null,
                        null,
                        null,
                        null,
                        "name");// 统计歌单内歌曲数目
                create_item(new String[]{mix_name, "total: " + cursor_count.getCount()}, 0);// TODO 列举歌单
            } while (cursor.moveToNext());
        } else {
            MusicList.infoToast(myContext, "no mix");
        }
        cursor.close();
    }

    public void listMusic(String mixName) {
        curMix = mixName;// 置null当前歌单
        mixSelected.clear();// 清空选中歌曲
        MusicList.itemList.removeAllViews();// 清空ui
        MusicList.window_num = MusicList.MUSIC_LIST;

        Cursor cursor = MusicList.database.query(
                curMix,// 歌单详情
                new String[]{"path", "name", "count"},
                null,
                null,
                null,
                null,
                "name");

        if (cursor.moveToFirst()) {
            // 列举所有歌曲
            do {
                String music_path = cursor.getString(0);// 获取歌曲绝对路径
                String music_name = cursor.getString(1);// 获取歌曲名
                int play_times = cursor.getInt(2);// 获取播放次数
                create_item(new String[]{music_name, "play times: " + play_times, music_path}, 1);
            } while (cursor.moveToNext());
        } else {
            MusicList.infoToast(myContext, "no music");
        }
        cursor.close();
    }

    // ui参数
    public static final int box_width = 60;
    public static final int item_height = 130;
    public static final int detail_margin_right = 80;
    public static final int detail_margin_left = 10;
    public static final int box_margin_top = 35;
    public static final int box_margin_right = 10;

    public void create_item(final String[] item_detail, int mode) {
        // mode: 0:歌单 1:歌曲
        // item_detail: {[歌名], [播放次数], [绝对路径]} {[歌单名], [歌曲数目]}

        // 每一项 LL
        LinearLayout.LayoutParams itemParam = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, item_height);
        // 每一项,用于定位 RL
        LinearLayout.LayoutParams containParam = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        // 文字区 LL
        LinearLayout.LayoutParams detailParam = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        // 歌单名
        LinearLayout.LayoutParams nameParam = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT,2);
        // 歌曲数目
        LinearLayout.LayoutParams numberParam = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1);;
        // 复选框
        LinearLayout.LayoutParams checkboxParam = new LinearLayout.LayoutParams(box_width, box_width);

        // 新建实例
        final LinearLayout item = new LinearLayout(myContext);
        RelativeLayout contain = new RelativeLayout(myContext);
        LinearLayout detail = new LinearLayout(myContext);
        final TextView name = new TextView(myContext);
        final TextView number = new TextView(myContext);
        final CheckBox checkBox = new CheckBox(myContext);

        item.setBackgroundResource(R.color.grey);
        item.setLayoutParams(itemParam);

        contain.setLayoutParams(containParam);

        detailParam.setMargins(detail_margin_left, detail_margin_left, detail_margin_right, detail_margin_left);
        detail.setOrientation(LinearLayout.HORIZONTAL);// 水平
        detail.setBackgroundResource(R.color.grey);
        detail.setLayoutParams(detailParam);

        name.setGravity(Gravity.CENTER);
        name.setText(item_detail[0]);// 歌单名/歌曲名
        name.setLayoutParams(nameParam);

        number.setGravity(Gravity.CENTER);
        number.setText(item_detail[1]);// 歌曲数/播放数
        number.setLayoutParams(numberParam);

        checkboxParam.setMargins(box_margin_right, box_margin_top, box_margin_right, box_margin_top);
        checkBox.setButtonDrawable(R.drawable.checkbox_library);
        checkBox.setLayoutParams(checkboxParam);

        // 合并ui
        detail.addView(name);
        detail.addView(number);
        contain.addView(checkBox);
        contain.addView(detail);
        item.addView(contain);
        MusicList.itemList.addView(item);

        // 动态修改布局
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) checkBox.getLayoutParams();
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);// 选框靠右
        checkBox.setLayoutParams(params);

        if (mode == 0) {// 当前为歌单列表
            // 查看歌单详情
            item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listMusic(item_detail[0]);// 点击查看对应歌单详情
                    MusicList.playList.highlightMusic();
                    MusicList.mixName.setText(curMix);
                }
            });

            // 复选功能: 添加专辑
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (checkBox.isChecked()) {
                        item.setBackgroundResource(R.color.grey_light);
                        mixSelected.add(item_detail[0]);
                    } else {
                        item.setBackgroundResource(R.color.grey);
                        mixSelected.remove(item_detail[0]);
                    }
                }
            });
        } else if (mode == 1) {// 当前为歌曲列表
            item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (MusicList.playList.loadMix(curMix, item_detail[2], 0) == 0) {// TODO 加载专辑曲目并播放歌曲
                        MusicList.playList.highlightMusic();
                    }
                }
            });

            // 复选功能: 添加歌曲绝对路径
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (checkBox.isChecked()) {
                        item.setBackgroundResource(R.color.grey_light);
                        musicSelected.add(item_detail[2]);
                    } else {
                        item.setBackgroundResource(R.color.grey);
                        musicSelected.remove(item_detail[2]);
                    }
                }
            });
        }
    }
}
