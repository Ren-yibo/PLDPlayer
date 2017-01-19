package com.pldroid.player.activity;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.pili.pldroid.player.PLMediaPlayer;
import com.pili.pldroid.player.widget.PLVideoTextureView;
import com.pili.pldroid.player.widget.PLVideoView;
import com.pldroid.R;
import com.pldroid.player.widget.LiveController;

/**
 * @author 任益波
 * @date 2016/12/6
 * @description
 */
public class LivePlayerActivity extends BasePlayerActivity implements
        View.OnClickListener,
        LiveController.IMsgClickCallBack,
        LiveController.IRecordClickCallBack {

    private ImageView mRefreshIv;

    @Override
    protected void beforeOnCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_video_base);
        setMediaTitle("LiveTest");
        mVideoPath = "rtmp://live.hkstv.hk.lxdns.com/live/hks";
    }

    @Override
    protected void afterOnCreate(Bundle savedInstanceState) {
        setScreenOrientation(SCREEN_ORIENTATION_LAND);
        mVideoView = (PLVideoTextureView) findViewById(R.id.videoPLView);
        mVideoView.setDisplayAspectRatio(PLVideoView.ASPECT_RATIO_FIT_PARENT);
        ViewGroup.LayoutParams mParams = mVideoView.getLayoutParams();
        mParams.width = getScreenWidth();
        mParams.height = getScreenHeight();
        mVideoView.setLayoutParams(mParams);
        mBufferingIndicator = (LinearLayout) findViewById(R.id.videoBuffrigLay);
        mRefreshIv = (ImageView) findViewById(R.id.videoRefreshIv);
        mRefreshIv.setOnClickListener(this);
        initSetting(true);
        ((LiveController) mMediaController).setMsgClickCallBack(this);
        ((LiveController) mMediaController).setRecordClickCallBack(this);
    }

    @Override
    public void doBack() {
        super.doBack();
        finish();
    }

    @Override
    public void doMsgClick() {

    }

    @Override
    public void doRecordClick(boolean isVisible) {

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
        mRefreshIv.setVisibility(View.VISIBLE);
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
    public void onPrepared(PLMediaPlayer plMediaPlayer) {
        super.onPrepared(plMediaPlayer);
        mBufferingIndicator.setVisibility(View.GONE);
        mRefreshIv.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.videoRefreshIv) {
            mVideoView.setVideoPath(mVideoPath);
            mVideoView.start();
        }
    }
}
