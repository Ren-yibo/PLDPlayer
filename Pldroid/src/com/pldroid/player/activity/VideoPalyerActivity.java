package com.pldroid.player.activity;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.pili.pldroid.player.PLMediaPlayer;
import com.pili.pldroid.player.widget.PLVideoTextureView;
import com.pili.pldroid.player.widget.PLVideoView;
import com.pldroid.R;
import com.pldroid.player.widget.VideoController;

/**
 * @author 任益波
 * @date 2016/12/2
 * @description
 */
public class VideoPalyerActivity extends BasePlayerActivity implements
        View.OnClickListener,
        VideoController.IResizeCallBack {

    private ImageView mRefreshIv;
    private boolean mVideoSizeWrap = false;

    @Override
    protected void beforeOnCreate(Bundle savedInstanceState) {
        setContentView(com.pldroid.R.layout.activity_video_base);
        setMediaTitle("Rolling");
        mVideoPath = "http://47.90.90.123/Rolling.mp4";
    }

    @Override
    protected void afterOnCreate(Bundle savedInstanceState) {
        setScreenOrientation(SCREEN_ORIENTATION_PORT);
        mVideoView = (PLVideoTextureView) findViewById(R.id.videoPLView);
        mVideoView.setDisplayAspectRatio(PLVideoView.ASPECT_RATIO_16_9);
        ViewGroup.LayoutParams mParams = mVideoView.getLayoutParams();
        mParams.width = getScreenWidth();
        mParams.height = getScreenWidth() / 16 * 9;
        mVideoView.setLayoutParams(mParams);
        mBufferingIndicator = (LinearLayout) findViewById(R.id.videoBuffrigLay);
        mRefreshIv = (ImageView) findViewById(R.id.videoRefreshIv);
        mRefreshIv.setOnClickListener(this);
        initSetting(false);
        ((VideoController) mMediaController).setResizeCallBack(this);
        ((VideoController) mMediaController).setResizeScreenselected(false);
    }

    @Override
    public void doBack() {
        super.doBack();
        if (!isScreenOriatationPortrait()) {
            //全屏下变为竖屏
            setScreenOrientation(SCREEN_ORIENTATION_PORT);
        } else {
            finish();
        }
    }

    @Override
    public void doResize() {
        if (isScreenOriatationPortrait()) {
            setScreenOrientation(SCREEN_ORIENTATION_LAND);
//            mMediaController.reMeasureSize(getScreenWidth(), getScreenHeight());
        } else {
            setScreenOrientation(SCREEN_ORIENTATION_PORT);
//            mMediaController.reMeasureSize(getScreenWidth(), getScreenWidth() / 16 * 9);
        }

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (isScreenOriatationPortrait()) {
            ((VideoController) mMediaController).setResizeScreenselected(false);
        } else {
            ((VideoController) mMediaController).setResizeScreenselected(true);
        }
    }

    @Override
    public void onPrepared(PLMediaPlayer plMediaPlayer) {
        super.onPrepared(plMediaPlayer);
        mRefreshIv.setVisibility(View.GONE);
    }

    @Override
    public void onVideoSizeChanged(PLMediaPlayer plMediaPlayer, int width, int height) {
        super.onVideoSizeChanged(plMediaPlayer, width, height);
        if (!mVideoSizeWrap) {
            mVideoSizeWrap = true;
            ViewGroup.LayoutParams mParams = mVideoView.getLayoutParams();
            mParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            mParams.height = WindowManager.LayoutParams.MATCH_PARENT;
            mVideoView.setLayoutParams(mParams);
        }
    }

    /**
     * 网络中断的回调:显示弹窗设置网络，显示刷新
     */
    @Override
    protected void onNetDiscount() {
        mVideoView.pause();
        if (!mVideoView.isPlaying()) {
            mRefreshIv.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 连接超时的回调：判断网络是否连接，显示刷新
     */
    @Override
    protected void onTimeout() {
        mBufferingIndicator.setVisibility(View.GONE);
        mRefreshIv.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.videoRefreshIv) {
            mVideoView.setVideoPath(mVideoPath);
            mVideoView.start();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (!isScreenOriatationPortrait()) {
                setScreenOrientation(SCREEN_ORIENTATION_PORT);
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}
