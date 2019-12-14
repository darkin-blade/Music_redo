package com.example.music_redo.mix;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.example.music_redo.MusicList;

public class MusicDataBase extends Service {
    static public SQLiteDatabase database;
    static public String appPath;

    static public void initData(Context context) {
        // 初始化路径字符串
        appPath = context.getExternalFilesDir("").getAbsolutePath();

        // 初始化数据库
        database = SQLiteDatabase.openOrCreateDatabase(appPath + "/player.db", null);

        // `歌单列表`table
        cmd("create table if not exists mix_list (\n" +
                "  name varchar (32) not null,\n" +
                "  primary key (name)\n" +
                ") ;");
        // `用户数据`table
        cmd("create table if not exists user_data (\n" +
                "  cur_mix varchar(32) default \"\",\n" +
                "  cur_music varchar(128) default \"\",\n" +
                "  play_mode int default 0,\n" +
                "  cur_time int default 0,\n" +
                "  total_time int default 0\n" +
                ");");
    }

    static public int cmd(Context context, String sql) {// 附加初始化的数据库使用
        if (database == null) {
            initData(context);
        }
        return cmd(sql);
    }

    static public int cmd(String sql) {// 操作数据库
        try {
            database.execSQL(sql);
        } catch (SQLException e) {
            e.printStackTrace();
            MusicList.infoLog("database error: " + sql);
            return -1;
        }
        return 0;
    }

    static public int deleteMusic(String mixName, String musicPath) {
        return cmd("delete from " + mixName + "\n" +
                "where path = '" + musicPath + "';");
    }

    static public int deleteMix(String mixName) {
        int result = cmd("delete from mix_list where name = '" + mixName + "';");// 从歌单列表删除
        if (result != 0) {
            return -1;// 从歌单列表删除歌单失败
        }

        result = cmd("drop table " + mixName +";");
        if (result != 0) {
            return -2;// 删除歌单失败
        } else {
            return 0;
        }
    }

    static public int createMix(String mixName) {
        if (mixName == null || mixName.length() == 0 || mixName.length() >= 32
                || mixName.equals("mix_list") || mixName.equals("user_data") ||
                (mixName.charAt(0) >= '0' && mixName.charAt(0) <= '9')) {// 歌单名不能为空,歌单名不能为关键字
            return -1;// 无效歌单名
        }

        // 插入到歌单列表`mix_list`
        int result = cmd("insert into mix_list (name)\n" +
                "  values\n" +
                "  ('" + mixName + "');");

        if (result != 0) {
            return -2;// 插入歌单列表失败
        }

        // 新建歌单`mixName`
        result = cmd("create table if not exists '" + mixName + "' (\n" +
                "  path varchar (128) not null,\n" +
                "  name varchar (64) not null,\n" +
                "  count int default 0,\n" +
                "  primary key (path)" +
                ");");

        if (result != 0) {
            deleteMix(mixName);
            return -3;// 创建歌单失败
        } else {
            return 0;// 创建歌单成功
        }
    }

    static public int renameMix(String oldName, String newName) {
        // 更改歌单名
        int result = cmd("alter table " + oldName + " rename to " + newName + ";");
        if (result != 0) {
            return -1;// 删除table失败
        }

        result = cmd("update mix_list set name = '" + newName + "' where name = '" + oldName + "';");
        if (result != 0) {
            return -2;// 从歌单列表删除失败
        }
        return 0;
    }

    static public int addMusic(String mixName, String musicPath) {
        return cmd("insert into " + mixName + " (path, name, count)\n" +
                "  values\n" +
                "  ('" + musicPath + "', '" + musicPath.replaceAll(".*/", "") + "', 0);");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
