package com.example.music_redo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.example.music_redo.widget.PlayNotification;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.example.music_redo.MusicList.playList;
import static com.example.music_redo.MusicList.player;

public class PlayTime {
    public Context myContext;
    public Activity myActivity ;

    // 时间管理
    public int total_time;
    public int cur_time;
    public int duration;// 播放时间累计
    static public Thread musicPlay;

    // 用于部件交互
    static final int MODE_PLAY = 0;
    static final int MODE_PAUSE = 1;
    static final int MODE_NEXT = 2;
    static final int MODE_PREV = 3;
    static final int MODE_UPDATE = 4;

    public PlayTime(Context context, Activity activity) {
        myContext = context;
        myActivity = activity;
    }

    public void init() {
        total_time = 0;
        cur_time = 0;
        duration = 0;
    }

    public void play(int mode) {
        /**
         * mode:
         * 0: 播放
         * 1: 加载
         * 2: 加载,置零
         * 3: 加载,置零,播放
         */
        // 中断正在播放的线程
        if (musicPlay != null && musicPlay.isAlive()) {
            musicPlay.interrupt();
        }

        if (mode >= 1) {
            // 加载
            try {
                player.reset();
                player.setDataSource(playList.curMusic);
                player.prepare();
                total_time = player.getDuration();

                // 设置时长ui
                SimpleDateFormat format = new SimpleDateFormat("mm:ss");
                Date tmp = new Date(total_time);
                final String formatTime = format.format(tmp);
                myActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        MusicList.totalTime.setText(formatTime);
                    }
                });
            } catch (IOException e) {// TODO prepare failed
                e.printStackTrace();

                // 歌曲无法播放
                MusicList.deleteMusic(playList.curMix, playList.curMusic);
                playList.loadMix(playList.curMix, playList.curMusic, 2);// TODO 只刷新歌单
                if (MusicList.window_num != MusicList.MIX_LIST) {// TODO 如果不是歌单列表
                    MusicList.listManager.listMusic(playList.curMix);
                }
                playList.stopMusic(1);
                return;
            } catch (IllegalStateException e) {
                e.printStackTrace();
                playList.stopMusic(1);
                return;
            }

            if (mode >= 2) {
                // 置零
                cur_time = 0;
            }
            setTime();
            setBar();
            if (mode == 1) {
                getBar();// 控制进度条
            }

            if (mode <= 2) {
                return;
            }
        }

        // 播放
        player.start();
//        MusicList.button_play.setBackgroundResource(R.drawable.player_pause);
        MusicList.button_play.setBackgroundDrawable(myContext.getResources().getDrawable(R.drawable.player_pause));

        callNotification(MODE_PLAY);

        musicPlay = new Thread(new Runnable() {
            @Override
            public void run() {
                while (Thread.currentThread().isInterrupted() == false) {
                    try {
                        if (player.isPlaying() == false) {
                            break;// TODO interrupt
                        }

                        cur_time = player.getCurrentPosition();
                        setTime();
                        setBar();

                        // 统计时间
                        duration ++;
                        MusicList.infoLog("duration: " + duration);
                        Thread.sleep(1000);// 每一秒更新一次 TODO 注意该语句的位置,防止触发OnComplete
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        break;
                    }
                }
            }
        });
        musicPlay.start();
    }

    public void pause() {
        if (player.isPlaying() == true) {
            player.pause();
//            MusicList.button_play.setBackgroundResource(R.drawable.player_play);
            MusicList.button_play.setBackgroundDrawable(myContext.getResources().getDrawable(R.drawable.player_play));
            musicPlay.interrupt();// TODO
            callNotification(MODE_PAUSE);
        }
    }

    public void reset() {
        player.reset();
        cur_time = 0;
        setBar();
    }

    public void next() {
        playList.loadMusic(1);
        playList.highlightMusic();
        callNotification(MODE_NEXT);
    }

    public void prev() {
        playList.loadMusic(2);
        playList.highlightMusic();
        callNotification(MODE_PREV);
    }

    public void callNotification(int mode) {
        Intent intent;
        switch (mode) {
            default:
                intent = new Intent(myContext, PlayNotification.class);
                intent.putExtra("mode", mode);
                myActivity.startService(intent);
                break;
        }
    }

    public void getBar() {// 从进度条更新播放进度
        int curProgress = MusicList.seekBar.getProgress();
        int maxProgress = MusicList.seekBar.getMax();

        // 调整时间
        cur_time = curProgress * total_time / maxProgress;
        player.seekTo(cur_time);
        setTime();
    }

    public void setBar() {// 更新进度条
        final int curProgress = cur_time * 100 / total_time;

        myActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MusicList.seekBar.setProgress(curProgress);
            }
        });
        callNotification(MODE_UPDATE);// 更新状态栏进度条
    }

    public void setTime() {// 更新时间
        SimpleDateFormat format = new SimpleDateFormat("mm:ss");
        Date tmp = new Date(cur_time);
        final String formatTime = format.format(tmp);

        myActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MusicList.curTime.setText(formatTime);
            }
        });
    }
}
