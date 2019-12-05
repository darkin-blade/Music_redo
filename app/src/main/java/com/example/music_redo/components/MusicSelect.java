package com.example.music_redo.components;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.music_redo.MusicList;
import com.example.music_redo.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class MusicSelect extends FileManager {
    public String lastPath = null;// 路径记忆

    public View myView;// find id用

    public Button button_select;// 确定
    public Button button_cancel;// 返回

    public ArrayList<String> musicList;// TODO 当前文件管理器中选定的项目
    public ArrayList<LinearLayout> musicLayouts;// TODO 集中保存所有 imageView 及对应 music 路径
    public ArrayList<String> musicPaths;// TODO 当前目录所有的文件

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.music_select, container);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(0x00000000));// 背景透明

        initData();
        initButton();

        // 调用文件管理器
        if (lastPath == null) {
            lastPath = MusicList.pathSimplify(MusicList.appPath + "/../../../..");// 根目录
        }
        readPath(lastPath);

        return myView;
    }

    public void initData() {
        MusicList.window_num = MusicList.MUSIC_SELECT;
        musicList = new ArrayList<String>();
        musicLayouts = new ArrayList<>();// TODO 优化加载
        musicPaths = new ArrayList<String>();
    }

    public void initButton() {
        button_cancel = myView.findViewById(R.id.button_cancel);
        button_select = myView.findViewById(R.id.button_select);

        button_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                musicList.clear();// 清空
                MusicList.dialog_result = -1;
                dismiss();
            }
        });

        button_select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO 添加到当前选定歌曲
                // TODO 保存歌曲名
                for (int i = 0; i < musicList.size(); i ++) {
                    MusicList.cmd("insert into " + MusicList.listManager.curMix + " (path, name, count)\n" +
                            "  values\n" +
                            "  ('" + musicList.get(i) + "', '" + musicList.get(i).replaceAll(".*/", "")  + "', 0);");
                }
                musicList.clear();// 清空
                MusicList.dialog_result = 1;
                dismiss();
            }
        });
    }

    public void readPath(final String dirPath) {
        // 每次更换目录都要清空
        musicLayouts.clear();// 清空
        musicPaths.clear();
        lastPath = dirPath;

        // 特判根目录
        if (dirPath == null) {
            MusicList.infoToast(getContext(), "can't access this path");
            lastPath = MusicList.appPath;// 重置路径
            dismiss();// 强制返回
            return;
        }

        // 清空并显示父目录
        LinearLayout layout = myView.findViewById(R.id.mix_list);
        layout.removeAllViews();
        createItem(2, "..", dirPath);// 父目录

        // 遍历文件夹
        File dir = new File(dirPath);
        File[] items = dir.listFiles();
        Arrays.sort(items);// 对内容进行排序

        if (items != null) {
            for (int i = 0; i < items.length; i++) {
                if (items[i].isDirectory()) {
                    createItem(1, items[i].getName(), dirPath);
                } else {// TODO 特判音乐文件
                    createItem(0, items[i].getName(), dirPath);
                }
            }
        }

        // TODO 异步加载图片
        loadIcon();
    }

    public int item_height = 130;
    public int type_padding = 20;
    public int name_padding = 30;
    public int box_width = 60;
    public int icon_height = 90;
    public int box_top = 35;
    public int box_right = 10;
    public int name_top = 10;
    public int name_right = 80;

    public void loadIcon() {// 动态加载文件项目
        class LoadImg extends Thread {
            @Override
            public void run() {
                for (int i = 0; i < musicLayouts.size(); i ++) {// 逐个异步加载图片
                    // 生成缩略图
                    // TODO 判断是否为音乐

                    // TODO 是音乐

                    // 加载过慢导致数组越界
                    if (i >= musicLayouts.size()) {
                        break;
                    }

                    final LinearLayout item = musicLayouts.get(i);
                    final RelativeLayout detail = (RelativeLayout) item.getChildAt(1);
                    LinearLayout.LayoutParams boxParam = new LinearLayout.LayoutParams(box_width, box_width);

                    final CheckBox checkBox = new CheckBox(getContext());
                    boxParam.setMargins(box_right, box_top, box_right, box_top);
                    checkBox.setLayoutParams(boxParam);
                    checkBox.setButtonDrawable(R.drawable.checkbox_library);

                    final int finalI = i;
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // TODO 动态生成缩略图

                            // 复选功能
                            // 点击外部
                            item.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (checkBox.isChecked()) {// 直接调用checkbox的监听,不需重复操作
                                        item.setBackgroundResource(R.color.grey);
                                        checkBox.setChecked(false);
                                    } else {
                                        item.setBackgroundResource(R.color.grey_light);
                                        checkBox.setChecked(true);
                                    }
                                }
                            });

                            // 直接点击复选框
                            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                @Override
                                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                    if (checkBox.isChecked()) {
                                        item.setBackgroundResource(R.color.grey_light);
                                        musicList.add(musicPaths.get(finalI));// TODO 添加到list
//                                        MusicList.infoLog("size: " + musicList.size());
                                    } else {
                                        item.setBackgroundResource(R.color.grey);
                                        boolean result = musicList.remove(musicPaths.get(finalI));// TODO 从list移出
//                                        MusicList.infoLog("size: " + musicList.size() + ", " + result);
                                    }
                                }
                            });

                            // 动态添加checkbox
                            detail.addView(checkBox);
                            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) checkBox.getLayoutParams();
                            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);// 单选框靠右
                            checkBox.setLayoutParams(params);
                        }
                    });
                }
            }
        }
        LoadImg loadImg = new LoadImg();
        loadImg.start();
    }

    public LinearLayout createItem(final int itemType, final String itemName, final String itemPath) {
        LinearLayout layout = myView.findViewById(R.id.mix_list);
        LinearLayout.LayoutParams itemParam = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, item_height);
        LinearLayout.LayoutParams typeParam = new LinearLayout.LayoutParams(icon_height, icon_height);
        LinearLayout.LayoutParams iconParam = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        LinearLayout.LayoutParams detailParam = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        LinearLayout.LayoutParams nameParam = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

        // TODO 统一新建元素
        final LinearLayout item = new LinearLayout(getContext());// TODO 参数
        LinearLayout type = new LinearLayout(getContext());// 图标的外圈
        RelativeLayout detail = new RelativeLayout(getContext());
        TextView name = new TextView(getContext());// 文件名

        item.setLayoutParams(itemParam);
        item.setBackgroundResource(R.color.grey);

        typeParam.setMargins(type_padding, type_padding, type_padding, type_padding);
        type.setLayoutParams(typeParam);

        ImageView icon = new ImageView(getContext());// 图标
        icon.setLayoutParams(iconParam);
        if (itemType == 0) {// 文件
            icon.setBackgroundResource(R.drawable.item_file);

            // TODO 记录所有需要加载的文件
            musicLayouts.add(item);// 记录ui
            musicPaths.add(itemPath + "/" + itemName);// 记录路径
        } else {// 文件夹
            icon.setBackgroundResource(R.drawable.item_dir);
        }

        detail.setLayoutParams(detailParam);

        nameParam.setMargins(0, name_top, name_right, name_top);
        name.setLayoutParams(nameParam);
        name.setBackgroundResource(R.color.grey);
        name.setText(itemName);
        name.setPadding(name_padding, name_padding, name_padding, name_padding);
        name.setSingleLine();

        type.addView(icon);
        item.addView(type);
        detail.addView(name);
        item.addView(detail);
        layout.addView(item);

        if (itemType == 2) {// 父文件夹
            item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    File dir = new File(itemPath);
                    readPath(dir.getParent());
                }
            });
        } else if (itemType == 1) {// `点击`遍历子文件夹
            item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    readPath(itemPath + "/" + itemName);
                }
            });
        }

        return item;
    }
}
