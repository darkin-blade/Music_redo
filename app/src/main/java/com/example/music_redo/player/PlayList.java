package com.example.music_redo.player;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.Color;
import android.os.IBinder;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.example.music_redo.MusicList;
import com.example.music_redo.mix.MusicDataBase;

import java.util.ArrayList;

public class PlayList extends Service {
    static public Activity myActivity;

    // 播放管理
    // 每次加载必须要恢复的数据
    static public String curMix;// 当前歌单
    static public String curMusic;// 当前歌曲
    static public int playMode;// 播放模式
    // 次要数据
    static public ArrayList<String> curMusicList;// 当前歌单的所有歌曲
    static public int curMusicIndex;
    static public int curMixLen;

    static public final int CIRCULATE = 0;// 顺序播放
    static public final int RANDOM = 1;// 随机
    static public final int SINGLE = 2;// 单曲循环
    static public final int AVERAGE = 3;// 平均
    static public final int POLARIZATION = 4;// 两极分化

    public PlayList() {
        MusicList.infoLog("playList constructor");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initData();
    }

    public void initData() {
        curMix = "";
        curMusic = "";
        curMusicList = new ArrayList<String>();
        recover();
    }

    public void loadMusic(int mode) {
        /**
         * mode:
         * -1: 指定加载
         * 0: 指定播放
         * 1: 下一首
         * 2: 上一首
         */
        // TODO 播放模式
        if (mode == 1) {// 下一首
            curMusicIndex = (curMusicIndex + 1) % curMixLen;
            curMusic = curMusicList.get(curMusicIndex);
        } else if (mode == 2) {// 上一首
            curMusicIndex = (curMusicIndex + curMixLen - 1) % curMixLen;
            curMusic = curMusicList.get(curMusicIndex);
        }

        // TODO 加载歌曲
        Intent intent = new Intent(this, PlayTime.class);
        intent.putExtra("cmd", "play");
        if (mode != -1) {
            intent.putExtra("mode", 3);
        } else {
            intent.putExtra("mode", 1);// 不进行置零
        }
        startService(intent);
    }

    public int loadMix(String nextMix, String nextMusic, int mode) {
        /**
         * mode:
         * 0: 加载歌单,加载歌曲,播放
         * 1: 加载歌单,加载歌曲
         * 2: 加载歌单
         */
        // TODO 累计播放时间
        MusicList.infoLog("load " + nextMix + " " + nextMusic);

        if (nextMix == null) {
            stopMusic(0);
            return -1;
        } else if (nextMusic == null) {
            stopMusic(1);
            return 1;
        }

        if (mode == 0 && nextMix.equals(curMix) && nextMusic.equals(curMusic)) {
            return 0;
        }

        curMix = nextMix;
        curMusic = nextMusic;
        curMusicIndex = -1;
        curMixLen = 0;
        curMusicList.clear();

        try {// 加载歌单
            Cursor cursor = MusicDataBase.database.query(
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
                        highlightMusic();// TODO
                    } else if (mode == 1) {
                        loadMusic(-1);
                        highlightMusic();// TODO
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

        highlightMusic();

        // TODO 重置player
        Intent intent = new Intent(this, PlayTime.class);
        intent.putExtra("cmd", "reset");
        startService(intent);
    }

    public void recover() {// 恢复数据
        Cursor cursor = MusicDataBase.database.query(
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
            PlayTime.cur_time = cursor.getInt(3);
            PlayTime.total_time = cursor.getInt(4);

            // TODO 加载歌单
            if (MusicList.window_num != 0) {
                MusicList.listManager.listMusic(curMix);
                if (loadMix(curMix, curMusic, 1) == 0) {
                    highlightMusic();
                }
                MusicList.listManager.showMix(curMix);
            }
        } else {
            MusicList.infoLog("cannot find user data");
        }
        cursor.close();
    }

    public void save() {// 保存应用数据到数据库
        MusicDataBase.cmd(this, "drop table user_data;");
        MusicDataBase.cmd(this, "create table if not exists user_data (\n" +
                "  cur_mix varchar(32) default \"\",\n" +
                "  cur_music varchar(128) default \"\",\n" +
                "  play_mode int default 0,\n" +
                "  cur_time int default 0,\n" +
                "  total_time int default 0\n" +
                ");");// 用户数据存储

        int result = MusicDataBase.cmd(this, "insert into user_data (cur_mix, cur_music, play_mode, cur_time, total_time)\n" +
                "  values ('" + curMix + "', '" + curMusic + "', " + playMode + ", "
                + PlayTime.cur_time +", " + PlayTime.total_time +");");

        if (result == 0) {
            MusicList.infoLog("save user data succeed");
        }
    }

    public void highlightMusic() {
        String tmp = curMusic.replaceAll(".*/", "");
        if (tmp.length() <= 0) {
            tmp = "no music";
        } else {
            tmp = curMix + "    " + tmp;
        }
        final String finalTmp = tmp;
        myActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MusicList.musicName.setText(finalTmp);
            }
        });

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
                if (curMix.equals(name.getText().toString())) {// 正在播放该歌单
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
            if (curMix.equals(MusicList.listManager.curMix)) {
                // TODO 高亮music
                for (int i = 0; i < childCount; i ++) {
                    LinearLayout item = (LinearLayout) layout.getChildAt(i);
                    RelativeLayout contain = (RelativeLayout) item.getChildAt(0);
                    LinearLayout detail = (LinearLayout) contain.getChildAt(1);
                    TextView name = (TextView) detail.getChildAt(0);
                    TextView count = (TextView) detail.getChildAt(1);
                    if (curMusicIndex == i) {// 正在播放该歌单
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

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            return START_STICKY;
        }

        String command = intent.getStringExtra("cmd");
        MusicList.infoLog("play list start command " + command);// TODO debug

        if (command != null) {
            if (command.equals("loadMusic")) {// 加载单个音乐
                int mode = intent.getIntExtra("mode", -1);
                loadMusic(mode);
            } else if (command.equals("loadMix")) {// 加载歌单
                String nextMix = intent.getStringExtra("nextMix");
                String nextMusic = intent.getStringExtra("nextMusic");
                int mode = intent.getIntExtra("mode", -1);
                loadMix(nextMix, nextMusic, mode);
            } else if (command.equals("stopMusic")) {// 强制暂停
                int mode = intent.getIntExtra("mode", -1);
                stopMusic(mode);
            } else if (command.equals("save")) {// 保存数据
                save();
            } else if (command.equals("init")) {// TODO 恢复数据
                MusicList.infoLog("play list init");
            } else if (command.equals("highlightMusic")) {// 修改监听
                highlightMusic();
            }
        }

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}