package com.example.music_redo;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.Color;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class PlayList {
    Context myContext;

    // 播放管理
    // 每次加载必须要恢复的数据
    public String curMix;// 当前歌单
    public String curMusic;// 当前歌曲
    int playMode;// 播放模式
    // 次要数据
    ArrayList<String> curMusicList;// 当前歌单的所有歌曲
    int curMusicIndex;
    int curMixLen;

    static public final int CIRCULATE = 0;// 顺序播放
    static public final int RANDOM = 1;// 随机
    static public final int SINGLE = 2;// 单曲循环
    static public final int AVERAGE = 3;// 平均
    static public final int POLARIZATION = 4;// 两极分化

    public PlayList(Context context) {
        myContext = context;
    }

    public void init() {
        curMix = "";
        curMusic = "";
        curMusicList = new ArrayList<String>();
        recover();
    }

    public void loadMusic(int mode) {
        if (mode == 1) {// 下一首
            curMusicIndex = (curMusicIndex + 1) % curMixLen;
            curMusic = curMusicList.get(curMusicIndex);
        } else if (mode == 2) {// 上一首
            curMusicIndex = (curMusicIndex + curMixLen - 1) % curMixLen;
            curMusic = curMusicList.get(curMusicIndex);
        }

        ;// TODO 加载歌曲时长

        if (mode != -1) {
            ;// TODO 播放歌曲
        }
    }

    public int loadMix(String nextMix, String nextMusic, int mode) {
        MusicList.infoLog("load " + nextMix + " " + nextMusic);

        if (nextMix == null) {
            stopMusic(0);
            return -1;
        } else if (nextMusic == null) {
            stopMusic(1);
            return 1;
        }

        if (mode == 0 && nextMix.equals(curMix) && nextMusic.equals(curMusic)) {// TODO null
            return 0;
        }

        curMix = nextMix;
        curMusic = nextMusic;
        curMusicIndex = -1;
        curMixLen = 0;
        curMusicList.clear();

        try {// 加载歌单
            Cursor cursor = MusicList.database.query(
                    curMix,// 当前歌单
                    new String[]{"path", "name", "count"},
                    null,
                    null,
                    null,
                    null,
                    "name");

            if (cursor.moveToFirst()) {// 歌单非空
                do {
                    String music_name = cursor.getString(0);// 获取歌曲路径
                    curMusicList.add(music_name);
                    curMixLen ++;
                } while (cursor.moveToNext());
                cursor.close();
                curMusicIndex = curMusicList.indexOf(curMusic);

                if (curMusicIndex < 0) {
                    stopMusic(1);
                    return 1;
                } else {
                    if (mode == 0) {
                        loadMusic(0);
                    } else if (mode == 1) {
                        loadMusic(-1);
                    } else if (mode == 2) {
                    }
                    return 0;
                }
            } else {
                stopMusic(1);// 暂停歌曲
                return 1;
            }
        } catch (SQLiteException e) {
            e.printStackTrace();
            stopMusic(0);// 歌单异常
            return -1;
        } catch (IllegalStateException e) {
            e.printStackTrace();
            stopMusic(0);// 歌单异常
            return -1;
        }
    }

    public void stopMusic(int mode) {
        if (mode == 0) {// 完全停止播放
            curMix = "";
            curMusic = "";
            curMusicIndex = -1;
            curMixLen = 0;
            curMusicList.clear();
        } else {// 停止当前歌曲
            curMusic = "";
            curMusicIndex = -1;
        }

        MusicList.musicName.setText("no music");
        highlightMusic();
        ;// TODO 重置player
    }

    public void recover() {// 恢复数据
        Cursor cursor = MusicList.database.query(
                "user_data",
                new String[] {"cur_mix", "cur_music", "play_mode", "cur_time", "total_time"},
                null,
                null,
                null,
                null,
                "cur_music");// 没用

        if (cursor.moveToFirst()) {
            // 恢复数据
            curMix = cursor.getString(0);
            curMusic = cursor.getString(1);
            playMode = cursor.getInt(2);
            MusicList.playTime.cur_time = cursor.getInt(3);
            MusicList.playTime.total_time = cursor.getInt(4);

            // 加载歌单
            if (loadMix(curMix, curMusic, 1) == 0) {
                highlightMusic();
            }
            MusicList.listManager.showMix(curMix);
            // TODO 恢复进度
        } else {
            MusicList.infoLog("cannot find user data");
        }
        cursor.close();
    }

    public void save() {// TODO 保存应用数据到数据库
        MusicList.cmd("drop table user_data;");
        MusicList.cmd("create table if not exists user_data (\n" +
                "  cur_mix varchar(32) default \"\",\n" +
                "  cur_music varchar(128) default \"\",\n" +
                "  play_mode int default 0,\n" +
                "  cur_time int default 0,\n" +
                "  total_time int default 0\n" +
                ");");// 用户数据存储

        int result = MusicList.cmd("insert into user_data (cur_mix, cur_music, play_mode, cur_time, total_time)\n" +
                "  values ('" + curMix + "', '" + curMusic + "', " + playMode + ", "
                + MusicList.playTime.cur_time +", " + MusicList.playTime.total_time +");");

        if (result == 0) {
            MusicList.infoLog("save user data succeed");
        }
    }

    public void highlightMusic() {
        MusicList.musicName.setText(curMix + "    " + curMusic.replaceAll(".*/", ""));

        // ui界面
        LinearLayout layout = MusicList.itemList;
        LinearLayout dest = null;
        int childCount = layout.getChildCount();
        if (MusicList.window_num == MusicList.MIX_LIST) {
            // TODO 高亮mix
            for (int i = 0; i < childCount; i ++) {
                LinearLayout item = (LinearLayout) layout.getChildAt(i);
                RelativeLayout contain = (RelativeLayout) item.getChildAt(0);
                LinearLayout detail = (LinearLayout) contain.getChildAt(1);
                TextView name = (TextView) detail.getChildAt(0);
                TextView count = (TextView) detail.getChildAt(1);
                if (MusicList.playList.curMix.equals(name.getText().toString())) {// 正在播放该歌单
                    dest = item;
                    name.setTextColor(Color.rgb(230, 100, 60));
                    count.setTextColor(Color.rgb(230, 100, 60));
                } else {
                    name.setTextColor(Color.rgb(0, 0, 0));
                    count.setTextColor(Color.rgb(0, 0, 0));
                }
            }

            // TODO 增加监听
            if (dest != null) {
                final LinearLayout finalDest = dest;
                ((LinearLayout) MusicList.musicName.getParent()).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MusicList.scrollView.scrollTo(0, finalDest.getTop());
                    }
                });
            }
        } else if (MusicList.window_num == MusicList.MUSIC_LIST) {
            if (MusicList.playList.curMix.equals(MusicList.listManager.curMix)) {
                // TODO 高亮music
                for (int i = 0; i < childCount; i ++) {
                    LinearLayout item = (LinearLayout) layout.getChildAt(i);
                    RelativeLayout contain = (RelativeLayout) item.getChildAt(0);
                    LinearLayout detail = (LinearLayout) contain.getChildAt(1);
                    TextView name = (TextView) detail.getChildAt(0);
                    TextView count = (TextView) detail.getChildAt(1);
                    if (MusicList.playList.curMusicIndex == i) {// 正在播放该歌单
                        dest = item;
                        name.setTextColor(Color.rgb(230, 100, 60));
                        count.setTextColor(Color.rgb(230, 100, 60));
                    } else {
                        name.setTextColor(Color.rgb(0, 0, 0));
                        count.setTextColor(Color.rgb(0, 0, 0));
                    }
                }

                // TODO 增加监听
                if (dest != null) {
                    final LinearLayout finalDest = dest;
                    ((LinearLayout) MusicList.musicName.getParent()).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            MusicList.scrollView.scrollTo(0, finalDest.getTop());
                        }
                    });
                }
            }
        }
    }
}