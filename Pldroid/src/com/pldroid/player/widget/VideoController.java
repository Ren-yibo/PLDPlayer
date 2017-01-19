package com.pldroid.player.widget;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.pldroid.R;

/**
 * @author 任益波
 * @date 2016/12/1
 * @description
 */
public class VideoController extends BaseMediaController {

    private ImageButton mResuzeIb;
    private IResizeCallBack mResizeCallBack;


    public VideoController(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutId(R.layout.layout_video_controller);
    }

    public VideoController(Context context) {
        super(context);
        setLayoutId(R.layout.layout_video_controller);
    }

    @Override
    void initControllerView(View v) {
        mPauseButton = (ImageButton) v.findViewById(R.id.mediaPauseIb);
        if (mPauseButton != null) {
            mPauseButton.requestFocus();
            mPauseButton.setOnClickListener(mPauseListener);
        }
        mProgress = (ProgressBar) v.findViewById(R.id.mediaSeekBar);
        if (mProgress != null) {
            if (mProgress instanceof SeekBar) {
                SeekBar seekBar = (SeekBar) mProgress;
                seekBar.setOnSeekBarChangeListener(mSeekListener);
                seekBar.setThumbOffset(1);
            }
            mProgress.setMax(1000);
            mProgress.setEnabled(!mDisableProgress);
        }
        mCurrentTime = (TextView) v.findViewById(R.id.mediaStartTimeTv);
        mEndTime = (TextView) v.findViewById(R.id.mediaEndTimeTv);
        mBackBtn = (ImageButton) v.findViewById(R.id.mediaBackIb);
        mBackBtn.setOnClickListener(mDoBackListener);
        mTitleTv = (TextView) v.findViewById(R.id.mediaTitleTv);
        if (!TextUtils.isEmpty(mTitleStr)) {
            mTitleTv.setText(mTitleStr);
        }
        mResuzeIb = (ImageButton) v.findViewById(R.id.mediaResizeIb);
        mResuzeIb.setOnClickListener(mResizeListener);
    }

    public interface IResizeCallBack {
        void doResize();
    }

    public void setResizeCallBack(IResizeCallBack mCallBack) {
        mResizeCallBack = mCallBack;
    }

    private OnClickListener mResizeListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (null != mResizeCallBack) {
                mResizeCallBack.doResize();
            } else {
                Log.e("VideoController", "ResizeCallBack is null");
            }
        }
    };

    public void setResizeScreenselected(boolean isSelected) {
        if(null != mResuzeIb){
            mResuzeIb.setSelected(isSelected);
        }
    }
}
