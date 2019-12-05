package com.example.music_redo.components;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;

import com.example.music_redo.MusicList;

import java.util.ArrayList;

public class PlayList {
    Context myContext;

    // 播放管理
    // 每次加载必须要恢复的数据
    String curMix;// 当前歌单
    String curMusic;// 当前歌曲
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

    public void recover() {// 恢复数据
        try {
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
//                MusicList.playTime.cur_time = cursor.getInt(3);
//                MusicList.playTime.total_time = cursor.getInt(4);
                cursor.close();

                // 手动加载歌单
                if (curMix.length() > 0 && curMusic.length() > 0) {// 有效数据
                    curMusicList.clear();
                    curMixLen = 0;

                    cursor = MusicList.database.query(
                            curMix,// 当前歌单
                            new String[]{"path", "name", "count"},
                            null,
                            null,
                            null,
                            null,
                            "name");

                    if (cursor.moveToFirst()) {// 歌单非空
                        do {
                            String music_name = cursor.getString(0);// 获取歌名
                            curMusicList.add(music_name);
                            curMixLen ++;
                        } while (cursor.moveToNext());

                        // TODO 加载歌单
                        curMusicIndex = curMusicList.indexOf(curMusic);// 获取当前播放的音乐的索引 此步可能会重复 且如果没有播放音乐时该索引可能为负
                    } else {
                        ;// TODO 出现异常
                    }
                    cursor.close();
                }
            } else {
                MusicList.infoLog("cannot find user data");
            }
        } catch (SQLException e) {// TODO 容错
            e.printStackTrace();
            MusicList.infoLog("cannot find table");
            return;
        }
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
}
