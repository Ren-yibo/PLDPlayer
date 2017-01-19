package com.pldroid.player.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.pili.pldroid.player.IMediaController;

import java.util.Locale;

/**
 * @author 任益波
 * @date 2016/12/1
 * @description
 */
public abstract class BaseMediaController extends FrameLayout implements IMediaController {

    private static final String TAG = "PLMediaController";
    private IMediaController.MediaPlayerControl mPlayer;
    private Context mContext;
    private PopupWindow mWindow;
    private int mAnimStyle;
    private View mAnchor;
    private View mRoot;
    protected ProgressBar mProgress;
    protected TextView mEndTime, mCurrentTime;
    private long mDuration;
    private boolean mShowing;
    private boolean mDragging;
    private boolean mInstantSeeking = true;
    private static int sDefaultTimeout = 3000;
    private static final int SEEK_TO_POST_DELAY_MILLIS = 200;

    private static final int FADE_OUT = 1;
    private static final int SHOW_PROGRESS = 2;
    private boolean mFromXml = false;
    protected ImageButton mPauseButton;
    protected ImageButton mBackBtn;
    protected TextView mTitleTv;
    protected String mTitleStr;
    private int mLayoutId;
    private IDoBack doBackCallBak;

    private static final int IC_MEDIA_PAUSE_ID = Resources.getSystem().getIdentifier("ic_media_pause", "drawable", "android");
    private static final int IC_MEDIA_PLAY_ID = Resources.getSystem().getIdentifier("ic_media_play", "drawable", "android");

    private AudioManager mAM;
    private Runnable mLastSeekBarRunnable;
    protected boolean mDisableProgress = false;

    private int mRootWidth = 0;
    private int mRootHeight = 0;

    public BaseMediaController(Context context, AttributeSet attrs) {
        super(context, attrs);
        mRoot = this;
        mFromXml = true;
        initController(context);
    }

    public BaseMediaController(Context context) {
        super(context);
        if (!mFromXml && initController(context))
            initFloatingWindow();
    }

    private boolean initController(Context context) {
        mContext = context.getApplicationContext();
        mAM = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        return true;
    }

    @Override
    public void onFinishInflate() {
        if (mRoot != null) {
            initControllerView(mRoot);
            if (mRootWidth != 0 && mRootHeight != 0) {
                reMeasureSize(mRootWidth, mRootHeight);
            }
        }
        super.onFinishInflate();
    }

    private void initFloatingWindow() {
        mWindow = new PopupWindow(mContext);
        mWindow.setFocusable(false);
        mWindow.setBackgroundDrawable(null);
        mWindow.setOutsideTouchable(true);
        mAnimStyle = android.R.style.Animation;
    }

    protected View makeControllerView() {
        return ((LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(mLayoutId, this);
    }

    /**
     * 设置布局
     *
     * @param resId
     */
    protected void setLayoutId(int resId) {
        mLayoutId = resId;
    }

    /**
     * 初始化控制器的控件
     *
     * @param v
     */
    abstract void initControllerView(View v);

    /**
     * Control the action when the seekbar dragged by user
     *
     * @param seekWhenDragging True the media will seek periodically
     */
    public void setInstantSeeking(boolean seekWhenDragging) {
        mInstantSeeking = seekWhenDragging;
    }

    private void disableUnsupportedButtons() {
        try {
            if (mPauseButton != null && !mPlayer.canPause())
                mPauseButton.setEnabled(false);
        } catch (IncompatibleClassChangeError ex) {
        }
    }

    /**
     * <p>
     * Change the animation style resource for this controller.
     * </p>
     * <p/>
     * <p>
     * If the controller is showing, calling this method will take effect only
     * the next time the controller is shown.
     * </p>
     *
     * @param animationStyle animation style to use when the controller appears and disappears.
     *                       Set to -1 for the default animation, 0 for no animation,
     *                       or a resource identifier for an explicit animation.
     */
    public void setAnimationStyle(int animationStyle) {
        mAnimStyle = animationStyle;
    }

    public interface OnShownListener {
        public void onShown();
    }

    private OnShownListener mShownListener;

    public void setOnShownListener(OnShownListener l) {
        mShownListener = l;
    }

    public interface OnHiddenListener {
        public void onHidden();
    }

    private OnHiddenListener mHiddenListener;

    public void setOnHiddenListener(OnHiddenListener l) {
        mHiddenListener = l;
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            long pos;
            switch (msg.what) {
                case FADE_OUT:
                    hide();
                    break;
                case SHOW_PROGRESS:
                    pos = setProgress();
                    if (!mDragging && mShowing) {
                        msg = obtainMessage(SHOW_PROGRESS);
                        sendMessageDelayed(msg, 1000 - (pos % 1000));
                        updatePausePlay();
                    }
                    break;
            }
        }
    };

    private long setProgress() {
        if (mPlayer == null || mDragging)
            return 0;

        long position = mPlayer.getCurrentPosition();
        long duration = mPlayer.getDuration();
        if (mProgress != null) {
            if (duration > 0) {
                long pos = 1000L * position / duration;
                mProgress.setProgress((int) pos);
            }
            int percent = mPlayer.getBufferPercentage();
            mProgress.setSecondaryProgress(percent * 10);
        }

        mDuration = duration;

        if (mEndTime != null)
            mEndTime.setText(generateTime(mDuration));
        if (mCurrentTime != null)
            mCurrentTime.setText(generateTime(position));

        return position;
    }

    private static String generateTime(long position) {
        int totalSeconds = (int) (position / 1000);

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        if (hours > 0) {
            return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes,
                    seconds).toString();
        } else {
            return String.format(Locale.US, "%02d:%02d", minutes, seconds)
                    .toString();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //        show(sDefaultTimeout);
        if (isShowing()) {
            hide();
        } else {
            show(sDefaultTimeout);
        }
        return true;
    }

    @Override
    public boolean onTrackballEvent(MotionEvent ev) {
        show(sDefaultTimeout);
        return false;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        if (event.getRepeatCount() == 0
                && (keyCode == KeyEvent.KEYCODE_HEADSETHOOK
                || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE || keyCode == KeyEvent.KEYCODE_SPACE)) {
            doPauseResume();
            show(sDefaultTimeout);
            if (mPauseButton != null)
                mPauseButton.requestFocus();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP) {
            if (mPlayer.isPlaying()) {
                mPlayer.pause();
                updatePausePlay();
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_BACK
                || keyCode == KeyEvent.KEYCODE_MENU) {
            hide();
            return true;
        } else {
            show(sDefaultTimeout);
        }
        return super.dispatchKeyEvent(event);
    }

    protected OnClickListener mPauseListener = new OnClickListener() {
        public void onClick(View v) {
            doPauseResume();
            show(sDefaultTimeout);
        }
    };

    private void updatePausePlay() {
        if (mRoot == null || mPauseButton == null)
            return;

        if (mPlayer.isPlaying())
            mPauseButton.setImageResource(IC_MEDIA_PAUSE_ID);
        else
            mPauseButton.setImageResource(IC_MEDIA_PLAY_ID);
    }

    private void doPauseResume() {
        if (mPlayer.isPlaying())
            mPlayer.pause();
        else
            mPlayer.start();
        updatePausePlay();
    }

    protected SeekBar.OnSeekBarChangeListener mSeekListener = new SeekBar.OnSeekBarChangeListener() {

        public void onStartTrackingTouch(SeekBar bar) {
            mDragging = true;
            show(3600000);
            mHandler.removeMessages(SHOW_PROGRESS);
            if (mInstantSeeking)
                mAM.setStreamMute(AudioManager.STREAM_MUSIC, true);
        }

        public void onProgressChanged(SeekBar bar, int progress, boolean fromuser) {
            if (!fromuser)
                return;

            final long newposition = (long) (mDuration * progress) / 1000;
            String time = generateTime(newposition);
            if (mInstantSeeking) {
                mHandler.removeCallbacks(mLastSeekBarRunnable);
                mLastSeekBarRunnable = new Runnable() {
                    @Override
                    public void run() {
                        mPlayer.seekTo(newposition);
                    }
                };
                mHandler.postDelayed(mLastSeekBarRunnable, SEEK_TO_POST_DELAY_MILLIS);
            }
            if (mCurrentTime != null)
                mCurrentTime.setText(time);
        }

        public void onStopTrackingTouch(SeekBar bar) {
            if (!mInstantSeeking)
                mPlayer.seekTo(mDuration * bar.getProgress() / 1000);

            show(sDefaultTimeout);
            mHandler.removeMessages(SHOW_PROGRESS);
            mAM.setStreamMute(AudioManager.STREAM_MUSIC, false);
            mDragging = false;
            mHandler.sendEmptyMessageDelayed(SHOW_PROGRESS, 1000);
        }
    };

    public interface IDoBack {
        void doBack();
    }

    public void setIDoBackCallBack(IDoBack doBack) {
        doBackCallBak = doBack;
    }

    protected OnClickListener mDoBackListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (null != doBackCallBak) {
                doBackCallBak.doBack();
            } else {
                Log.e("MediaController", "doBackCallBack is null");
            }
        }
    };

    public void setMediaTitle(String title) {
        mTitleStr = title;
        if (null != mTitleTv) {
            mTitleTv.setText(mTitleStr);
        }
    }

    /**
     * Set the view that acts as the anchor for the control view.
     * <p/>
     * - This can for example be a VideoView, or your Activity's main view.
     * - AudioPlayer has no anchor view, so the view parameter will be null.
     *
     * @param view The view to which to anchor the controller when it is visible.
     */
    @Override
    public void setAnchorView(View view) {
        mAnchor = view;
        if (mAnchor == null) {
            sDefaultTimeout = 0; // show forever
        }
        if (!mFromXml) {
            removeAllViews();
            mRoot = makeControllerView();
            mWindow.setContentView(mRoot);
            if (mRootHeight != 0 && mRootWidth != 0) {
                mWindow.setWidth(mRootWidth);
                mWindow.setHeight(mRootHeight);
            } else {
                mWindow.setWidth(LayoutParams.MATCH_PARENT);
                mWindow.setHeight(LayoutParams.WRAP_CONTENT);
            }
        }
        initControllerView(mRoot);
    }

    @Override
    public void setMediaPlayer(MediaPlayerControl player) {
        mPlayer = player;
        updatePausePlay();
    }

    @Override
    public void show() {
        show(sDefaultTimeout);
    }

    /**
     * Show the controller on screen. It will go away automatically after
     * 'timeout' milliseconds of inactivity.
     *
     * @param timeout The timeout in milliseconds. Use 0 to show the controller until hide() is called.
     */
    @Override
    public void show(int timeout) {
        if (!mShowing) {
            if (mAnchor != null && mAnchor.getWindowToken() != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    mAnchor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                }
            }
            if (mPauseButton != null)
                mPauseButton.requestFocus();
            disableUnsupportedButtons();

            if (mFromXml) {
                setVisibility(View.VISIBLE);
            } else {
                int[] location = new int[2];

                if (mAnchor != null) {
//                    mAnchor.getLocationOnScreen(location);
//                    Rect anchorRect = new Rect(location[0], location[1],
//                            location[0] + mAnchor.getWidth(), location[1]
//                            + mAnchor.getHeight());
//
//                    mWindow.setAnimationStyle(mAnimStyle);
//                    mWindow.showAtLocation(mAnchor, Gravity.TOP,
//                            anchorRect.left, 0);
                    mWindow.setAnimationStyle(mAnimStyle);
                    mWindow.showAtLocation(mAnchor, Gravity.TOP, 0, 0);
                } else {
//                    Rect anchorRect = new Rect(location[0], location[1],
//                            location[0] + mRoot.getWidth(), location[1]
//                            + mRoot.getHeight());
//
//                    mWindow.setAnimationStyle(mAnimStyle);
//                    mWindow.showAtLocation(mRoot, Gravity.TOP,
//                            anchorRect.left, 0);
                    mWindow.setAnimationStyle(mAnimStyle);
                    mWindow.showAtLocation(mRoot, Gravity.TOP, 0, 0);
                }
            }
            mShowing = true;
            if (mShownListener != null)
                mShownListener.onShown();
        }
        updatePausePlay();
        mHandler.sendEmptyMessage(SHOW_PROGRESS);

        if (timeout != 0) {
            mHandler.removeMessages(FADE_OUT);
            mHandler.sendMessageDelayed(mHandler.obtainMessage(FADE_OUT),
                    timeout);
        }
    }

    @Override
    public boolean isShowing() {
        return mShowing;
    }

    @Override
    public void hide() {
        if (mShowing) {
            if (mAnchor != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    //mAnchor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
                }
            }
            try {
                mHandler.removeMessages(SHOW_PROGRESS);
                if (mFromXml)
                    setVisibility(View.GONE);
                else
                    mWindow.dismiss();
            } catch (IllegalArgumentException ex) {
                Log.d(TAG, "MediaController already removed");
            }
            mShowing = false;
            if (mHiddenListener != null)
                mHiddenListener.onHidden();
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (mPauseButton != null) {
            mPauseButton.setEnabled(enabled);
        }
        if (mProgress != null && !mDisableProgress)
            mProgress.setEnabled(enabled);
        disableUnsupportedButtons();
        super.setEnabled(enabled);
    }

    /**
     * 返回当前进度条播放位置的百分比
     *
     * @return
     */
    public double getProgress() {
        if (mProgress == null) {
            return 0;
        }
        return mProgress.getProgress() * 1.0 / 1000 * 100;
    }

    /**
     * 返回播放器中当前缓存的百分比
     *
     * @return
     */
    public double getSecondaryProgerss() {
        if (mProgress == null) {
            return 0;
        }
        return mProgress.getSecondaryProgress() * 1.0 / 1000 * 100;
    }

    /**
     * 重新设置控制器的尺寸
     *
     * @param width
     * @param height
     */
    public void reMeasureSize(int width, int height) {
        if (mRootWidth == width && mRootHeight == height) {
            return;
        }
        mWindow.setWidth(width);
        mWindow.setHeight(height);
        requestLayout();
        mRootWidth = width;
        mRootHeight = height;
        hide();
    }

}
