package com.pldroid.player.activity;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import com.pili.pldroid.player.AVOptions;
import com.pili.pldroid.player.PLMediaPlayer;
import com.pili.pldroid.player.PlayerCode;
import com.pili.pldroid.player.widget.PLVideoTextureView;
import com.pili.pldroid.player.widget.PLVideoView;
import com.pldroid.player.widget.BaseMediaController;
import com.pldroid.player.widget.LiveController;
import com.pldroid.player.widget.VideoController;

import tv.danmaku.ijk.media.player.IMediaPlayer;

/**
 * @author 任益波
 * @date 2016/12/2
 * @description
 */
public abstract class BasePlayerActivity extends FragmentActivity implements PLMediaPlayer.OnCompletionListener,
        PLMediaPlayer.OnInfoListener,
        PLMediaPlayer.OnErrorListener,
        PLMediaPlayer.OnVideoSizeChangedListener,
        PLMediaPlayer.OnPreparedListener,
        BaseMediaController.IDoBack{

    protected static final int SCREEN_ORIENTATION_LAND = 1;//横屏
    protected static final int SCREEN_ORIENTATION_PORT = 2;//竖屏

    protected PLVideoTextureView mVideoView;//播放器
    protected View mBufferingIndicator;//等待进度条
    protected BaseMediaController mMediaController;//控制进度条
    private ViewGroup.LayoutParams mLayoutParams;
    private Pair<Integer, Integer> mScreenSize;//手机屏幕参数
    protected String mVideoPath;//视频地址
    private boolean mIsCompleted = false;//是否完成
    private String mMediaTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        beforeOnCreate(savedInstanceState);
        super.onCreate(savedInstanceState);
        afterOnCreate(savedInstanceState);
    }

    /**
     * 用于视图文件的加载
     *
     * @param savedInstanceState
     */
    protected abstract void beforeOnCreate(Bundle savedInstanceState);

    /**
     * 用于View的加载和初始化事件
     *
     * @param savedInstanceState
     */
    protected abstract void afterOnCreate(Bundle savedInstanceState);

    /**
     * 设置播放横竖屏
     *
     * @param orientation
     */
    protected void setScreenOrientation(int orientation) {
        if (orientation == SCREEN_ORIENTATION_LAND) {
            //横屏设置全屏，竖屏不用全屏
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//设置全屏
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        }
    }

    /**
     * 初始化设置
     */
    protected void initSetting(boolean isLive) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);//设置常亮
        AVOptions options = new AVOptions();//配置参数类
        //准备超时时间，包括创建资源、建立连接、请求码等
        options.setInteger(AVOptions.KEY_PREPARE_TIMEOUT, 10 * 1000);
        int codec = 0;
        //解码方式：1->硬解 0->软解
        options.setInteger(AVOptions.KEY_MEDIACODEC, codec);
        options.setInteger(AVOptions.KEY_GET_AV_FRAME_TIMEOUT, 10 * 1000);
        //设置是否自动播放
        options.setInteger(AVOptions.KEY_START_ON_PREPARED, 0);
        //当前视频是否在线直播，是则优化，默认0
        options.setInteger(AVOptions.KEY_LIVE_STREAMING, 1);
        if(!isLive){
            mMediaController = new VideoController(this);
        }else{
            mMediaController = new LiveController(this);
        }
        mMediaController.setMediaPlayer(mVideoView);
        mMediaController.setIDoBackCallBack(this);
        mMediaController.setMediaTitle(mMediaTitle);
        mVideoView.setMediaController(mMediaController);
        //设置等待动画
        mVideoView.setBufferingIndicator(mBufferingIndicator);
        mVideoView.setAVOptions(options);
        mVideoView.setVideoPath(mVideoPath);
        mVideoView.setOnErrorListener(this);
        mVideoView.setOnCompletionListener(this);
        mVideoView.setOnInfoListener(this);
        mVideoView.setOnPreparedListener(this);
        mVideoView.setOnVideoSizeChangedListener(this);
        mBufferingIndicator.setVisibility(View.VISIBLE);
    }

    @Override
    public void doBack() {

    }


    @Override
    public void onCompletion(PLMediaPlayer plMediaPlayer) {
        //监听结束回调，读取码流EOF回调，遇到错误回调
        mBufferingIndicator.setVisibility(View.GONE);
        mIsCompleted = true;
    }

    @Override
    public boolean onError(PLMediaPlayer plMediaPlayer, int errorCode) {
        //产生错误回调
        if (errorCode == PlayerCode.EXTRA_CODE_INVALID_URI || errorCode == PlayerCode.EXTRA_CODE_EOF) {
            //无效的URL或者码流EOF错误
            if (mBufferingIndicator != null)
                mBufferingIndicator.setVisibility(View.GONE);
            return true;
        }
        if (mIsCompleted && errorCode == PlayerCode.EXTRA_CODE_EMPTY_PLAYLIST) {
            //播放结束，列表为空则重新开始
            Log.i("WatchActivity", "Video End");
        } else if (errorCode == PlayerCode.EXTRA_CODE_404_NOT_FOUND) {
            // 播放资源不存在
            if (mBufferingIndicator != null)
                mBufferingIndicator.setVisibility(View.GONE);
            Toast.makeText(this, "播放地址失效", Toast.LENGTH_SHORT).show();
        } else if (errorCode == PlayerCode.EXTRA_CODE_IO_ERROR) {
            // 网络中断
            if (mBufferingIndicator != null)
                mBufferingIndicator.setVisibility(View.GONE);
            Log.e("BasePlayer", "NetError");
            onNetDiscount();
        } else if (errorCode == PlayerCode.EXTRA_CODE_CONNECTION_TIMEOUT) {
            //连接中断
            if (mBufferingIndicator != null) {
                mBufferingIndicator.setVisibility(View.VISIBLE);
            }
            Toast.makeText(this, "连接中断...", Toast.LENGTH_SHORT).show();
            Log.e("BasePlayer", "Link Discount");
            onTimeout();
        } else if (errorCode == -101) {
            //初始化资源无网络情况下
            Log.e("BasePlayer", "NetError");
            onNetDiscount();
        }
        return true;
    }

    @Override
    public boolean onInfo(PLMediaPlayer plMediaPlayer, int what, int extra) {
        //监听播放器的状态
        if (what == IMediaPlayer.MEDIA_INFO_BUFFERING_START) {
            //开始缓冲
            if (mBufferingIndicator != null)
                mBufferingIndicator.setVisibility(View.VISIBLE);
        } else if (what == IMediaPlayer.MEDIA_INFO_BUFFERING_END) {
            //停止缓冲
            if (mBufferingIndicator != null)
                mBufferingIndicator.setVisibility(View.GONE);
        } else if (what == IMediaPlayer.MEDIA_INFO_AUDIO_RENDERING_START) {
            //第一帧视频已经渲染成功
            Log.i("WatchActivity", "Audio Start");
        } else if (what == IMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
            //第一帧音频已经播放成功
            Log.i("WatchActivity", "Video Start");
        }
        return true;
    }

    @Override
    public void onPrepared(PLMediaPlayer plMediaPlayer) {
        //创建资源、建立连接、请求码流等,准备结束
        mBufferingIndicator.setVisibility(View.GONE);
    }

    @Override
    public void onVideoSizeChanged(PLMediaPlayer plMediaPlayer, int width, int height) {
        if (mVideoView != null) {
            if (isScreenOriatationPortrait()) {
                mVideoView.setDisplayAspectRatio(PLVideoView.ASPECT_RATIO_16_9);
                mMediaController.reMeasureSize(getScreenWidth(), getScreenWidth() / 16 * 9);
            } else {
                mVideoView.setDisplayAspectRatio(PLVideoView.ASPECT_RATIO_FIT_PARENT);
                mMediaController.reMeasureSize(getScreenWidth(), getScreenHeight());
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (isScreenOriatationPortrait()) {
            mMediaController.reMeasureSize(getScreenWidth(), getScreenWidth() / 16 * 9);
            mVideoView.setDisplayOrientation(PLVideoView.ASPECT_RATIO_16_9);
        } else {
            mMediaController.reMeasureSize(getScreenWidth(), getScreenHeight());
            mVideoView.setDisplayOrientation(PLVideoView.ASPECT_RATIO_FIT_PARENT);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (null != mVideoView && mVideoView.getCurrentPosition() != 0) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);//设置常亮
            mVideoView.start();
        }
    }

    @Override
    public void onPause() {
        mVideoView.pause();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mVideoView.stopPlayback();
    }

    protected void setMediaTitle(String s) {
        this.mMediaTitle = s;
    }

    protected <W extends View> W getElemetView(int id) {
        return (W) getWindow().getDecorView().findViewById(id);
    }

    /**
     * 手机是否竖屏
     *
     * @return
     */
    protected boolean isScreenOriatationPortrait() {
        return this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
    }

    /**
     * 获取屏幕宽度
     *
     * @return
     */
    protected int getScreenWidth() {
        return this.getResources().getDisplayMetrics().widthPixels;
    }

    /**
     * 获取屏幕高度
     *
     * @return
     */
    protected int getScreenHeight() {
        return this.getResources().getDisplayMetrics().heightPixels;
    }

    /**
     * 网络中断的回调:显示弹窗设置网络，显示刷新
     */
    protected abstract void onNetDiscount();

    /**
     * 连接超时的回调：判断网络是否连接，显示刷新
     */
    protected abstract void onTimeout();
}