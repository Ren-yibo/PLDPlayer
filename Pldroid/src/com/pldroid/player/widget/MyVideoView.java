//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.pldroid.player.widget;

import android.annotation.TargetApi;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

import com.pili.pldroid.player.AVOptions;
import com.pili.pldroid.player.IMediaController;
import com.pili.pldroid.player.IMediaController.MediaPlayerControl;
import com.pili.pldroid.player.SharedLibraryNameHelper;
import com.pili.pldroid.player.common.Util;

import java.io.IOException;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IMediaPlayer.OnBufferingUpdateListener;
import tv.danmaku.ijk.media.player.IMediaPlayer.OnCompletionListener;
import tv.danmaku.ijk.media.player.IMediaPlayer.OnErrorListener;
import tv.danmaku.ijk.media.player.IMediaPlayer.OnInfoListener;
import tv.danmaku.ijk.media.player.IMediaPlayer.OnPreparedListener;
import tv.danmaku.ijk.media.player.IMediaPlayer.OnSeekCompleteListener;
import tv.danmaku.ijk.media.player.IMediaPlayer.OnVideoSizeChangedListener;
import tv.danmaku.ijk.media.player.IjkLibLoader;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

/**
 * 继承于七牛的VideoView，增加断点逻辑，修改使用时候方便定位到上次暂停点续播
 */
public class MyVideoView extends SurfaceView implements MediaPlayerControl {
    private static final String TAG = "MyVideoView";
    private Uri mUri;
    private static final int STATE_ERROR = -1;
    private static final int STATE_IDLE = 0;
    private static final int STATE_PREPARING = 1;
    private static final int STATE_PREPARED = 2;
    private static final int STATE_PLAYING = 3;
    private static final int STATE_PAUSED = 4;
    private static final int STATE_PLAYBACK_COMPLETED = 5;
    private int mCurrentState;
    private int mTargetState;
    private SurfaceHolder mSurfaceHolder;
    private IMediaPlayer mMediaPlayer;
    private int mVideoWidth;
    private int mVideoHeight;
    private int mVideoSarNum;
    private int mVideoSarDen;
    private int mSurfaceWidth;
    private int mSurfaceHeight;
    private IMediaController mMediaController;
    private OnCompletionListener mOnCompletionListener;
    private OnPreparedListener mOnPreparedListener;
    private OnSeekCompleteListener mOnSeekCompleteListener;
    private OnVideoSizeChangedListener mOnVideoSizeChangedListener;
    private int mCurrentBufferPercentage;
    private OnErrorListener mOnErrorListener;
    private OnInfoListener mOnInfoListener;
    private long mSeekWhenPrepared;
    private boolean mCanPause;
    private boolean mCanSeekBack;
    private boolean mCanSeekForward;
    private AVOptions mAVOptions;
    private int mVideoLayout;
    public static final int VIDEO_LAYOUT_ORIGIN = 0;
    public static final int VIDEO_LAYOUT_SCALE = 1;
    public static final int VIDEO_LAYOUT_STRETCH = 2;
    public static final int VIDEO_LAYOUT_ZOOM = 3;
    private View mMediaBufferingIndicator;
    private static final int MyVideoView_ERROR_TEXT_INVALID_PROGRESSIVE_PLAYBACK_ID = Resources.getSystem().getIdentifier("MyVideoView_error_text_invalid_progressive_playback", "string", "android");
    private static final int MyVideoView_ERROR_TEXT_UNKNOWN_ID = Resources.getSystem().getIdentifier("MyVideoView_error_text_unknown", "string", "android");
    private static final int MyVideoView_ERROR_BUTTON_ID = Resources.getSystem().getIdentifier("MyVideoView_error_button", "string", "android");
    private Context mContext;
    private static volatile boolean mIsLibLoaded = false;
    OnVideoSizeChangedListener mSizeChangedListener;
    OnPreparedListener mPreparedListener;
    private OnSeekCompleteListener mSeekCompleteListener;
    private OnCompletionListener mCompletionListener;
    private OnInfoListener mInfoListener;
    private OnErrorListener mErrorListener;
    private OnBufferingUpdateListener mBufferingUpdateListener;
    Callback mSHCallback;

    private String mVideoPath;
    private long mLastPosition;

    public MyVideoView(Context var1) {
        super(var1);
        this.mCurrentState = 0;
        this.mTargetState = 0;
        this.mSurfaceHolder = null;
        this.mMediaPlayer = null;
        this.mVideoLayout = 1;
        this.mSizeChangedListener = new OnVideoSizeChangedListener() {
            public void onVideoSizeChanged(IMediaPlayer var1, int var2, int var3, int var4, int var5) {
                if (MyVideoView.this.mOnVideoSizeChangedListener != null) {
                    MyVideoView.this.mOnVideoSizeChangedListener.onVideoSizeChanged(var1, var2, var3, var4, var5);
                }

                MyVideoView.this.mVideoWidth = var1.getVideoWidth();
                MyVideoView.this.mVideoHeight = var1.getVideoHeight();
                MyVideoView.this.mVideoSarNum = var4;
                MyVideoView.this.mVideoSarDen = var5;
                if (MyVideoView.this.mVideoWidth != 0 && MyVideoView.this.mVideoHeight != 0) {
                    MyVideoView.this.getHolder().setFixedSize(MyVideoView.this.mVideoWidth, MyVideoView.this.mVideoHeight);
                    MyVideoView.this.requestLayout();
                }

            }
        };
        this.mPreparedListener = new OnPreparedListener() {
            public void onPrepared(IMediaPlayer var1) {
                Log.i("MyVideoView", "onPrepared");
                MyVideoView.this.mCurrentState = 2;
                MyVideoView.this.mCanPause = MyVideoView.this.mCanSeekBack = MyVideoView.this.mCanSeekForward = true;
                if (MyVideoView.this.mOnPreparedListener != null) {
                    MyVideoView.this.mOnPreparedListener.onPrepared(MyVideoView.this.mMediaPlayer);
                }

                if (MyVideoView.this.mMediaController != null) {
                    MyVideoView.this.mMediaController.setEnabled(true);
                }

                MyVideoView.this.mVideoWidth = var1.getVideoWidth();
                MyVideoView.this.mVideoHeight = var1.getVideoHeight();
                long var2 = MyVideoView.this.mSeekWhenPrepared;
                if (var2 != 0L) {
                    MyVideoView.this.seekTo(var2);
                }

                if (MyVideoView.this.mVideoWidth != 0 && MyVideoView.this.mVideoHeight != 0) {
                    MyVideoView.this.getHolder().setFixedSize(MyVideoView.this.mVideoWidth, MyVideoView.this.mVideoHeight);
                    if (MyVideoView.this.mSurfaceWidth == MyVideoView.this.mVideoWidth && MyVideoView.this.mSurfaceHeight == MyVideoView.this.mVideoHeight) {
                        if (MyVideoView.this.mTargetState == 3) {
                            MyVideoView.this.start();
                            if (MyVideoView.this.mMediaController != null) {
                                MyVideoView.this.mMediaController.show();
                            }
                        } else if (!MyVideoView.this.isPlaying() && (var2 != 0L || MyVideoView.this.getCurrentPosition() > 0L) && MyVideoView.this.mMediaController != null) {
                            MyVideoView.this.mMediaController.show(0);
                        }
                    }
                } else if (MyVideoView.this.mTargetState == 3) {
                    MyVideoView.this.start();
                }

            }
        };
        this.mSeekCompleteListener = new OnSeekCompleteListener() {
            public void onSeekComplete(IMediaPlayer var1) {
                Log.d("MyVideoView", "onSeekComplete");
                if (MyVideoView.this.mOnSeekCompleteListener != null) {
                    MyVideoView.this.mOnSeekCompleteListener.onSeekComplete(var1);
                }

            }
        };
        this.mCompletionListener = new OnCompletionListener() {
            public void onCompletion(IMediaPlayer var1) {
                Log.i("MyVideoView", "onCompletion");
                MyVideoView.this.mCurrentState = 5;
                MyVideoView.this.mTargetState = 5;
                if (MyVideoView.this.mMediaController != null) {
                    MyVideoView.this.mMediaController.hide();
                }

                if (MyVideoView.this.mOnCompletionListener != null) {
                    MyVideoView.this.mOnCompletionListener.onCompletion(MyVideoView.this.mMediaPlayer);
                }

            }
        };
        this.mInfoListener = new OnInfoListener() {
            public boolean onInfo(IMediaPlayer var1, int var2, int var3) {
                Log.i("MyVideoView", "Info: " + var2 + "," + var3);
                if (MyVideoView.this.mOnInfoListener != null) {
                    MyVideoView.this.mOnInfoListener.onInfo(var1, var2, var3);
                } else if (MyVideoView.this.mMediaPlayer != null) {
                    if (var2 == 701) {
                        if (MyVideoView.this.mMediaBufferingIndicator != null) {
                            MyVideoView.this.mMediaBufferingIndicator.setVisibility(VISIBLE);
                        }
                    } else if (var2 == 702 && MyVideoView.this.mMediaBufferingIndicator != null) {
                        MyVideoView.this.mMediaBufferingIndicator.setVisibility(GONE);
                    }
                }

                return true;
            }
        };
        this.mErrorListener = new OnErrorListener() {
            public boolean onError(IMediaPlayer var1, int var2, int var3) {
                Log.d("MyVideoView", "Error: " + var2 + "," + var3);
                MyVideoView.this.mCurrentState = -1;
                MyVideoView.this.mTargetState = -1;
                if (MyVideoView.this.mMediaController != null) {
                    MyVideoView.this.mMediaController.hide();
                }

                if (MyVideoView.this.mOnErrorListener != null && MyVideoView.this.mOnErrorListener.onError(MyVideoView.this.mMediaPlayer, var2, var3)) {
                    return true;
                } else {
                    if (MyVideoView.this.getWindowToken() != null) {
                        Resources var4 = MyVideoView.this.mContext.getResources();
                        int var5;
                        if (var2 == 200) {
                            var5 = MyVideoView.MyVideoView_ERROR_TEXT_INVALID_PROGRESSIVE_PLAYBACK_ID;
                        } else {
                            var5 = MyVideoView.MyVideoView_ERROR_TEXT_UNKNOWN_ID;
                        }

                        (new Builder(MyVideoView.this.mContext)).setMessage(var5).setPositiveButton(MyVideoView.MyVideoView_ERROR_BUTTON_ID, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (MyVideoView.this.mOnCompletionListener != null) {
                                    MyVideoView.this.mOnCompletionListener.onCompletion(MyVideoView.this.mMediaPlayer);
                                }
                            }
                        }).setCancelable(false).show();
                    }

                    return true;
                }
            }
        };
        this.mBufferingUpdateListener = new OnBufferingUpdateListener() {
            public void onBufferingUpdate(IMediaPlayer var1, int var2) {
                MyVideoView.this.mCurrentBufferPercentage = var2;
            }
        };
        this.mSHCallback = new Callback() {
            public void surfaceChanged(SurfaceHolder var1, int var2, int var3, int var4) {
                MyVideoView.this.mSurfaceWidth = var3;
                MyVideoView.this.mSurfaceHeight = var4;
                boolean var5 = MyVideoView.this.mTargetState == 3;
                boolean var6 = MyVideoView.this.mVideoWidth == var3 && MyVideoView.this.mVideoHeight == var4;
                if (MyVideoView.this.mMediaPlayer != null && var5 && var6) {
                    if (MyVideoView.this.mSeekWhenPrepared != 0L) {
                        MyVideoView.this.seekTo(MyVideoView.this.mSeekWhenPrepared);
                    }

                    MyVideoView.this.start();
                }

            }

            public void surfaceCreated(SurfaceHolder var1) {
                MyVideoView.this.mSurfaceHolder = var1;
                MyVideoView.this.openVideo();
            }

            public void surfaceDestroyed(SurfaceHolder var1) {
                MyVideoView.this.mSurfaceHolder = null;
                if (MyVideoView.this.mMediaController != null) {
                    MyVideoView.this.mMediaController.hide();
                }

                MyVideoView.this.release(true);
            }
        };
        this.initMyVideoView(var1);
    }

    public MyVideoView(Context var1, AttributeSet var2) {
        this(var1, var2, 0);
        this.initMyVideoView(var1);
    }

    public MyVideoView(Context var1, AttributeSet var2, int var3) {
        super(var1, var2, var3);
        this.mCurrentState = 0;
        this.mTargetState = 0;
        this.mSurfaceHolder = null;
        this.mMediaPlayer = null;
        this.mVideoLayout = 1;
        this.mSizeChangedListener = new OnVideoSizeChangedListener() {
            public void onVideoSizeChanged(IMediaPlayer var1, int var2, int var3, int var4, int var5) {
                if (MyVideoView.this.mOnVideoSizeChangedListener != null) {
                    MyVideoView.this.mOnVideoSizeChangedListener.onVideoSizeChanged(var1, var2, var3, var4, var5);
                }

                MyVideoView.this.mVideoWidth = var1.getVideoWidth();
                MyVideoView.this.mVideoHeight = var1.getVideoHeight();
                MyVideoView.this.mVideoSarNum = var4;
                MyVideoView.this.mVideoSarDen = var5;
                if (MyVideoView.this.mVideoWidth != 0 && MyVideoView.this.mVideoHeight != 0) {
                    MyVideoView.this.getHolder().setFixedSize(MyVideoView.this.mVideoWidth, MyVideoView.this.mVideoHeight);
                    MyVideoView.this.requestLayout();
                }

            }
        };
        this.mPreparedListener = new OnPreparedListener() {
            public void onPrepared(IMediaPlayer var1) {
                Log.i("MyVideoView", "onPrepared");
                MyVideoView.this.mCurrentState = 2;
                MyVideoView.this.mCanPause = MyVideoView.this.mCanSeekBack = MyVideoView.this.mCanSeekForward = true;
                if (MyVideoView.this.mOnPreparedListener != null) {
                    MyVideoView.this.mOnPreparedListener.onPrepared(MyVideoView.this.mMediaPlayer);
                }

                if (MyVideoView.this.mMediaController != null) {
                    MyVideoView.this.mMediaController.setEnabled(true);
                }

                MyVideoView.this.mVideoWidth = var1.getVideoWidth();
                MyVideoView.this.mVideoHeight = var1.getVideoHeight();
                long var2 = MyVideoView.this.mSeekWhenPrepared;
                if (var2 != 0L) {
                    MyVideoView.this.seekTo(var2);
                }

                if (MyVideoView.this.mVideoWidth != 0 && MyVideoView.this.mVideoHeight != 0) {
                    MyVideoView.this.getHolder().setFixedSize(MyVideoView.this.mVideoWidth, MyVideoView.this.mVideoHeight);
                    if (MyVideoView.this.mSurfaceWidth == MyVideoView.this.mVideoWidth && MyVideoView.this.mSurfaceHeight == MyVideoView.this.mVideoHeight) {
                        if (MyVideoView.this.mTargetState == 3) {
                            MyVideoView.this.start();
                            if (MyVideoView.this.mMediaController != null) {
                                MyVideoView.this.mMediaController.show();
                            }
                        } else if (!MyVideoView.this.isPlaying() && (var2 != 0L || MyVideoView.this.getCurrentPosition() > 0L) && MyVideoView.this.mMediaController != null) {
                            MyVideoView.this.mMediaController.show(0);
                        }
                    }
                } else if (MyVideoView.this.mTargetState == 3) {
                    MyVideoView.this.start();
                }

            }
        };
        this.mSeekCompleteListener = new OnSeekCompleteListener() {
            public void onSeekComplete(IMediaPlayer var1) {
                Log.d("MyVideoView", "onSeekComplete");
                if (MyVideoView.this.mOnSeekCompleteListener != null) {
                    MyVideoView.this.mOnSeekCompleteListener.onSeekComplete(var1);
                }

            }
        };
        this.mCompletionListener = new OnCompletionListener() {
            public void onCompletion(IMediaPlayer var1) {
                Log.i("MyVideoView", "onCompletion");
                MyVideoView.this.mCurrentState = 5;
                MyVideoView.this.mTargetState = 5;
                if (MyVideoView.this.mMediaController != null) {
                    MyVideoView.this.mMediaController.hide();
                }

                if (MyVideoView.this.mOnCompletionListener != null) {
                    MyVideoView.this.mOnCompletionListener.onCompletion(MyVideoView.this.mMediaPlayer);
                }

            }
        };
        this.mInfoListener = new OnInfoListener() {
            public boolean onInfo(IMediaPlayer var1, int var2, int var3) {
                Log.i("MyVideoView", "Info: " + var2 + "," + var3);
                if (MyVideoView.this.mOnInfoListener != null) {
                    MyVideoView.this.mOnInfoListener.onInfo(var1, var2, var3);
                } else if (MyVideoView.this.mMediaPlayer != null) {
                    if (var2 == 701) {
                        if (MyVideoView.this.mMediaBufferingIndicator != null) {
                            MyVideoView.this.mMediaBufferingIndicator.setVisibility(VISIBLE);
                        }
                    } else if (var2 == 702 && MyVideoView.this.mMediaBufferingIndicator != null) {
                        MyVideoView.this.mMediaBufferingIndicator.setVisibility(GONE);
                    }
                }

                return true;
            }
        };
        this.mErrorListener = new OnErrorListener() {
            public boolean onError(IMediaPlayer var1, int var2, int var3) {
                Log.d("MyVideoView", "Error: " + var2 + "," + var3);
                MyVideoView.this.mCurrentState = -1;
                MyVideoView.this.mTargetState = -1;
                if (MyVideoView.this.mMediaController != null) {
                    MyVideoView.this.mMediaController.hide();
                }

                if (MyVideoView.this.mOnErrorListener != null && MyVideoView.this.mOnErrorListener.onError(MyVideoView.this.mMediaPlayer, var2, var3)) {
                    return true;
                } else {
                    if (MyVideoView.this.getWindowToken() != null) {
                        Resources var4 = MyVideoView.this.mContext.getResources();
                        int var5;
                        if (var2 == 200) {
                            var5 = MyVideoView.MyVideoView_ERROR_TEXT_INVALID_PROGRESSIVE_PLAYBACK_ID;
                        } else {
                            var5 = MyVideoView.MyVideoView_ERROR_TEXT_UNKNOWN_ID;
                        }

                        (new Builder(MyVideoView.this.mContext)).setMessage(var5).setPositiveButton(MyVideoView.MyVideoView_ERROR_BUTTON_ID, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (MyVideoView.this.mOnCompletionListener != null) {
                                    MyVideoView.this.mOnCompletionListener.onCompletion(MyVideoView.this.mMediaPlayer);
                                }
                            }
                        }).setCancelable(false).show();
                    }

                    return true;
                }
            }
        };
        this.mBufferingUpdateListener = new OnBufferingUpdateListener() {
            public void onBufferingUpdate(IMediaPlayer var1, int var2) {
                MyVideoView.this.mCurrentBufferPercentage = var2;
            }
        };
        this.mSHCallback = new Callback() {
            public void surfaceChanged(SurfaceHolder var1, int var2, int var3, int var4) {
                MyVideoView.this.mSurfaceWidth = var3;
                MyVideoView.this.mSurfaceHeight = var4;
                boolean var5 = MyVideoView.this.mTargetState == 3;
                boolean var6 = MyVideoView.this.mVideoWidth == var3 && MyVideoView.this.mVideoHeight == var4;
                if (MyVideoView.this.mMediaPlayer != null && var5 && var6) {
                    if (MyVideoView.this.mSeekWhenPrepared != 0L) {
                        MyVideoView.this.seekTo(MyVideoView.this.mSeekWhenPrepared);
                    }

                    MyVideoView.this.start();
                }

            }

            public void surfaceCreated(SurfaceHolder var1) {
                MyVideoView.this.mSurfaceHolder = var1;
                MyVideoView.this.openVideo();
            }

            public void surfaceDestroyed(SurfaceHolder var1) {
                MyVideoView.this.mSurfaceHolder = null;
                if (MyVideoView.this.mMediaController != null) {
                    MyVideoView.this.mMediaController.hide();
                }

                MyVideoView.this.release(true);
            }
        };
        this.initMyVideoView(var1);
    }

    @TargetApi(21)
    public MyVideoView(Context var1, AttributeSet var2, int var3, int var4) {
        super(var1, var2, var3, var4);
        this.mCurrentState = 0;
        this.mTargetState = 0;
        this.mSurfaceHolder = null;
        this.mMediaPlayer = null;
        this.mVideoLayout = 1;
        this.mSizeChangedListener = new OnVideoSizeChangedListener() {
            public void onVideoSizeChanged(IMediaPlayer var1, int var2, int var3, int var4, int var5) {
                if (MyVideoView.this.mOnVideoSizeChangedListener != null) {
                    MyVideoView.this.mOnVideoSizeChangedListener.onVideoSizeChanged(var1, var2, var3, var4, var5);
                }

                MyVideoView.this.mVideoWidth = var1.getVideoWidth();
                MyVideoView.this.mVideoHeight = var1.getVideoHeight();
                MyVideoView.this.mVideoSarNum = var4;
                MyVideoView.this.mVideoSarDen = var5;
                if (MyVideoView.this.mVideoWidth != 0 && MyVideoView.this.mVideoHeight != 0) {
                    MyVideoView.this.getHolder().setFixedSize(MyVideoView.this.mVideoWidth, MyVideoView.this.mVideoHeight);
                    MyVideoView.this.requestLayout();
                }

            }
        };
        this.mPreparedListener = new OnPreparedListener() {
            public void onPrepared(IMediaPlayer var1) {
                Log.i("MyVideoView", "onPrepared");
                MyVideoView.this.mCurrentState = 2;
                MyVideoView.this.mCanPause = MyVideoView.this.mCanSeekBack = MyVideoView.this.mCanSeekForward = true;
                if (MyVideoView.this.mOnPreparedListener != null) {
                    MyVideoView.this.mOnPreparedListener.onPrepared(MyVideoView.this.mMediaPlayer);
                }

                if (MyVideoView.this.mMediaController != null) {
                    MyVideoView.this.mMediaController.setEnabled(true);
                }

                MyVideoView.this.mVideoWidth = var1.getVideoWidth();
                MyVideoView.this.mVideoHeight = var1.getVideoHeight();
                long var2 = MyVideoView.this.mSeekWhenPrepared;
                if (var2 != 0L) {
                    MyVideoView.this.seekTo(var2);
                }

                if (MyVideoView.this.mVideoWidth != 0 && MyVideoView.this.mVideoHeight != 0) {
                    MyVideoView.this.getHolder().setFixedSize(MyVideoView.this.mVideoWidth, MyVideoView.this.mVideoHeight);
                    if (MyVideoView.this.mSurfaceWidth == MyVideoView.this.mVideoWidth && MyVideoView.this.mSurfaceHeight == MyVideoView.this.mVideoHeight) {
                        if (MyVideoView.this.mTargetState == 3) {
                            MyVideoView.this.start();
                            if (MyVideoView.this.mMediaController != null) {
                                MyVideoView.this.mMediaController.show();
                            }
                        } else if (!MyVideoView.this.isPlaying() && (var2 != 0L || MyVideoView.this.getCurrentPosition() > 0L) && MyVideoView.this.mMediaController != null) {
                            MyVideoView.this.mMediaController.show(0);
                        }
                    }
                } else if (MyVideoView.this.mTargetState == 3) {
                    MyVideoView.this.start();
                }

            }
        };
        this.mSeekCompleteListener = new OnSeekCompleteListener() {
            public void onSeekComplete(IMediaPlayer var1) {
                Log.d("MyVideoView", "onSeekComplete");
                if (MyVideoView.this.mOnSeekCompleteListener != null) {
                    MyVideoView.this.mOnSeekCompleteListener.onSeekComplete(var1);
                }

            }
        };
        this.mCompletionListener = new OnCompletionListener() {
            public void onCompletion(IMediaPlayer var1) {
                Log.i("MyVideoView", "onCompletion");
                MyVideoView.this.mCurrentState = 5;
                MyVideoView.this.mTargetState = 5;
                if (MyVideoView.this.mMediaController != null) {
                    MyVideoView.this.mMediaController.hide();
                }

                if (MyVideoView.this.mOnCompletionListener != null) {
                    MyVideoView.this.mOnCompletionListener.onCompletion(MyVideoView.this.mMediaPlayer);
                }

            }
        };
        this.mInfoListener = new OnInfoListener() {
            public boolean onInfo(IMediaPlayer var1, int var2, int var3) {
                Log.i("MyVideoView", "Info: " + var2 + "," + var3);
                if (MyVideoView.this.mOnInfoListener != null) {
                    MyVideoView.this.mOnInfoListener.onInfo(var1, var2, var3);
                } else if (MyVideoView.this.mMediaPlayer != null) {
                    if (var2 == 701) {
                        if (MyVideoView.this.mMediaBufferingIndicator != null) {
                            MyVideoView.this.mMediaBufferingIndicator.setVisibility(VISIBLE);
                        }
                    } else if (var2 == 702 && MyVideoView.this.mMediaBufferingIndicator != null) {
                        MyVideoView.this.mMediaBufferingIndicator.setVisibility(GONE);
                    }
                }

                return true;
            }
        };
        this.mErrorListener = new OnErrorListener() {
            public boolean onError(IMediaPlayer var1, int var2, int var3) {
                Log.d("MyVideoView", "Error: " + var2 + "," + var3);
                MyVideoView.this.mCurrentState = -1;
                MyVideoView.this.mTargetState = -1;
                if (MyVideoView.this.mMediaController != null) {
                    MyVideoView.this.mMediaController.hide();
                }

                if (MyVideoView.this.mOnErrorListener != null && MyVideoView.this.mOnErrorListener.onError(MyVideoView.this.mMediaPlayer, var2, var3)) {
                    return true;
                } else {
                    if (MyVideoView.this.getWindowToken() != null) {
                        Resources var4 = MyVideoView.this.mContext.getResources();
                        int var5;
                        if (var2 == 200) {
                            var5 = MyVideoView.MyVideoView_ERROR_TEXT_INVALID_PROGRESSIVE_PLAYBACK_ID;
                        } else {
                            var5 = MyVideoView.MyVideoView_ERROR_TEXT_UNKNOWN_ID;
                        }

                        (new Builder(MyVideoView.this.mContext)).setMessage(var5).setPositiveButton(MyVideoView.MyVideoView_ERROR_BUTTON_ID, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (MyVideoView.this.mOnCompletionListener != null) {
                                    MyVideoView.this.mOnCompletionListener.onCompletion(MyVideoView.this.mMediaPlayer);
                                }
                            }
                        }).setCancelable(false).show();
                    }

                    return true;
                }
            }
        };
        this.mBufferingUpdateListener = new OnBufferingUpdateListener() {
            public void onBufferingUpdate(IMediaPlayer var1, int var2) {
                MyVideoView.this.mCurrentBufferPercentage = var2;
            }
        };
        this.mSHCallback = new Callback() {
            public void surfaceChanged(SurfaceHolder var1, int var2, int var3, int var4) {
                MyVideoView.this.mSurfaceWidth = var3;
                MyVideoView.this.mSurfaceHeight = var4;
                boolean var5 = MyVideoView.this.mTargetState == 3;
                boolean var6 = MyVideoView.this.mVideoWidth == var3 && MyVideoView.this.mVideoHeight == var4;
                if (MyVideoView.this.mMediaPlayer != null && var5 && var6) {
                    if (MyVideoView.this.mSeekWhenPrepared != 0L) {
                        MyVideoView.this.seekTo(MyVideoView.this.mSeekWhenPrepared);
                    }

                    MyVideoView.this.start();
                }

            }

            public void surfaceCreated(SurfaceHolder var1) {
                MyVideoView.this.mSurfaceHolder = var1;
                MyVideoView.this.openVideo();
            }

            public void surfaceDestroyed(SurfaceHolder var1) {
                MyVideoView.this.mSurfaceHolder = null;
                if (MyVideoView.this.mMediaController != null) {
                    MyVideoView.this.mMediaController.hide();
                }

                MyVideoView.this.release(true);
            }
        };
        this.initMyVideoView(var1);
    }

    protected void onMeasure(int var1, int var2) {
        int var3 = getDefaultSize(this.mVideoWidth, var1);
        int var4 = getDefaultSize(this.mVideoHeight, var2);
        if (this.mVideoWidth > 0 && this.mVideoHeight > 0) {
            int var5 = MeasureSpec.getMode(var1);
            int var6 = MeasureSpec.getSize(var1);
            int var7 = MeasureSpec.getMode(var2);
            int var8 = MeasureSpec.getSize(var2);
            if (var5 == 1073741824 && var7 == 1073741824) {
                var3 = var6;
                var4 = var8;
                if (this.mVideoWidth * var8 < var6 * this.mVideoHeight) {
                    var3 = var8 * this.mVideoWidth / this.mVideoHeight;
                } else if (this.mVideoWidth * var8 > var6 * this.mVideoHeight) {
                    var4 = var6 * this.mVideoHeight / this.mVideoWidth;
                }
            } else if (var5 == 1073741824) {
                var3 = var6;
                var4 = var6 * this.mVideoHeight / this.mVideoWidth;
                if (var7 == -2147483648 && var4 > var8) {
                    var4 = var8;
                }
            } else if (var7 == 1073741824) {
                var4 = var8;
                var3 = var8 * this.mVideoWidth / this.mVideoHeight;
                if (var5 == -2147483648 && var3 > var6) {
                    var3 = var6;
                }
            } else {
                var3 = this.mVideoWidth;
                var4 = this.mVideoHeight;
                if (var7 == -2147483648 && var4 > var8) {
                    var4 = var8;
                    var3 = var8 * this.mVideoWidth / this.mVideoHeight;
                }

                if (var5 == -2147483648 && var3 > var6) {
                    var3 = var6;
                    var4 = var6 * this.mVideoHeight / this.mVideoWidth;
                }
            }
        }

        this.setMeasuredDimension(var3, var4);
    }

    public int resolveAdjustedSize(int var1, int var2) {
        return getDefaultSize(var1, var2);
    }

    private void initMyVideoView(Context var1) {
        this.mContext = var1;
        this.mVideoWidth = 0;
        this.mVideoHeight = 0;
        this.getHolder().addCallback(this.mSHCallback);
        this.setFocusable(true);
        this.setFocusableInTouchMode(true);
        this.requestFocus();
        this.mCurrentState = 0;
        this.mTargetState = 0;
    }

    public void setVideoPath(String var1) {
        mVideoPath = var1;
        this.setVideoURI(Uri.parse(var1));
    }

    public void setVideoURI(Uri var1) {
        this.mUri = var1;
        this.mSeekWhenPrepared = 0L;
        this.openVideo();
        this.requestLayout();
        this.invalidate();
    }

    public void stopPlayback() {
        if (this.mMediaPlayer != null) {
            this.mMediaPlayer.stop();
            this.mMediaPlayer.release();
            this.mMediaPlayer = null;
            this.mCurrentState = 0;
            this.mTargetState = 0;
            AudioManager var1 = (AudioManager) this.mContext.getSystemService(Context.AUDIO_SERVICE);
            var1.abandonAudioFocus((OnAudioFocusChangeListener) null);
        }

    }

    private void setLiveStreamingOptions(IjkMediaPlayer var1) {
        Log.i("MyVideoView", "setLiveStreamingOptions");
        var1.setOption(1, "rtmp_live", 1L);
        var1.setOption(1, "rtmp_buffer", this.mAVOptions != null && this.mAVOptions.containsKey("rtmp_buffer") ? (long) this.mAVOptions.getInteger("rtmp_buffer") : 100L);
    }

    private void setOptions(IjkMediaPlayer var1) {
        Log.i("MyVideoView", "setOptions");
    }

    private void openVideo() {
        if (this.mUri != null && this.mSurfaceHolder != null) {
            this.release(false);
            AudioManager var1 = (AudioManager) this.mContext.getSystemService(Context.AUDIO_SERVICE);
            var1.requestAudioFocus((OnAudioFocusChangeListener) null, 3, 1);

            try {
                this.mCurrentBufferPercentage = 0;
                IjkMediaPlayer var2 = null;
                if (this.mUri != null) {
                    var2 = new IjkMediaPlayer(new IjkLibLoader() {
                        public void loadLibrary(String var1) throws UnsatisfiedLinkError, SecurityException {
                            if (!MyVideoView.mIsLibLoaded) {
                                Log.i("MyVideoView", "newLibName:" + SharedLibraryNameHelper.getInstance().getSharedLibraryName());
                                MyVideoView.mIsLibLoaded = true;
                                SharedLibraryNameHelper.getInstance().a();
                                IjkMediaPlayer.native_setLogLevel(6);
                            }

                        }
                    });
                    var2.setOption(4, "overlay-format", 842225234L);
                    var2.setOption(4, "framedrop", 12L);
                    var2.setOption(1, "http-detect-range-support", 0L);
                    Log.i("MyVideoView", "mUri.getPath:" + this.mUri.toString());
                    String var3 = "analyzeduration";
                    var2.setOption(1, var3, this.mAVOptions.containsKey(var3) ? (long) this.mAVOptions.getInteger(var3) : 0L);
                    String var4 = "probesize";
                    var2.setOption(1, var4, this.mAVOptions.containsKey(var4) ? (long) this.mAVOptions.getInteger(var4) : 102400L);
                    boolean var5 = false;
                    if (this.mAVOptions != null && this.mAVOptions.containsKey("live-streaming") && this.mAVOptions.getInteger("live-streaming") != 0) {
                        var5 = true;
                    }

                    if (var5) {
                        this.setLiveStreamingOptions(var2);
                    } else {
                        this.setOptions(var2);
                    }

                    var2.setOption(4, "live-streaming", (long) (var5 ? 1 : 0));
                    var2.setOption(4, "get-av-frame-timeout", this.mAVOptions != null && this.mAVOptions.containsKey("get-av-frame-timeout") ? (long) (this.mAVOptions.getInteger("get-av-frame-timeout") * 1000) : 10000000L);
                    var2.setOption(4, "mediacodec", this.mAVOptions != null && this.mAVOptions.containsKey("mediacodec") ? (long) this.mAVOptions.getInteger("mediacodec") : 1L);
                    var2.setOption(2, "skip_loop_filter", 0L);
                    var2.setOption(4, "start-on-prepared", 1L);
                }

                this.mMediaPlayer = var2;
                this.mMediaPlayer.setOnPreparedListener(this.mPreparedListener);
                this.mMediaPlayer.setOnVideoSizeChangedListener(this.mSizeChangedListener);
                this.mMediaPlayer.setOnCompletionListener(this.mCompletionListener);
                this.mMediaPlayer.setOnErrorListener(this.mErrorListener);
                this.mMediaPlayer.setOnBufferingUpdateListener(this.mBufferingUpdateListener);
                this.mMediaPlayer.setOnInfoListener(this.mInfoListener);
                this.mMediaPlayer.setOnSeekCompleteListener(this.mSeekCompleteListener);
                if (this.mUri != null) {
                    this.mMediaPlayer.setDataSource(this.mUri.toString());
                }

                this.mMediaPlayer.setDisplay(this.mSurfaceHolder);
                this.mMediaPlayer.setScreenOnWhilePlaying(true);
                this.mMediaPlayer.prepareAsync();
                this.mCurrentState = 1;
                this.attachMediaController();
            } catch (IOException var6) {
                Log.e("MyVideoView", "Unable to open content: " + this.mUri, var6);
                this.mCurrentState = -1;
                this.mTargetState = -1;
                this.mErrorListener.onError(this.mMediaPlayer, 1, 0);
            } catch (IllegalArgumentException var7) {
                Log.e("MyVideoView", "Unable to open content: " + this.mUri, var7);
                this.mCurrentState = -1;
                this.mTargetState = -1;
                this.mErrorListener.onError(this.mMediaPlayer, 1, 0);
            }
        }
    }

    public void setMediaController(IMediaController var1) {
        if (this.mMediaController != null) {
            this.mMediaController.hide();
        }

        this.mMediaController = var1;
        this.attachMediaController();
    }

    private void attachMediaController() {
        if (this.mMediaPlayer != null && this.mMediaController != null) {
            this.mMediaController.setMediaPlayer(this);
            Object var1 = this.getParent() instanceof View ? (View) this.getParent() : this;
            this.mMediaController.setAnchorView((View) var1);
            this.mMediaController.setEnabled(this.isInPlaybackState());
        }

    }

    public void setOnPreparedListener(OnPreparedListener var1) {
        this.mOnPreparedListener = var1;
    }

    public void setOnCompletionListener(OnCompletionListener var1) {
        this.mOnCompletionListener = var1;
    }

    public void setOnErrorListener(OnErrorListener var1) {
        this.mOnErrorListener = var1;
    }

    public void setOnInfoListener(OnInfoListener var1) {
        this.mOnInfoListener = var1;
    }

    public void setOnVideoSizeChangedListener(OnVideoSizeChangedListener var1) {
        this.mOnVideoSizeChangedListener = var1;
    }

    private void release(boolean var1) {
        if (this.mMediaPlayer != null) {
            this.mMediaPlayer.reset();
            this.mMediaPlayer.release();
            this.mMediaPlayer = null;
            this.mCurrentState = 0;
            if (var1) {
                this.mTargetState = 0;
            }

            AudioManager var2 = (AudioManager) this.mContext.getSystemService(Context.AUDIO_SERVICE);
            var2.abandonAudioFocus((OnAudioFocusChangeListener) null);
        }

    }

    public boolean onTouchEvent(MotionEvent var1) {
        if (this.isInPlaybackState() && this.mMediaController != null) {
            this.toggleMediaControlsVisiblity();
        }
        return false;
    }

    public boolean onTrackballEvent(MotionEvent var1) {
        if (this.isInPlaybackState() && this.mMediaController != null) {
            this.toggleMediaControlsVisiblity();
        }

        return false;
    }

    public boolean onKeyDown(int var1, KeyEvent var2) {
        boolean var3 = var1 != 4 && var1 != 24 && var1 != 25 && var1 != 164 && var1 != 82 && var1 != 5 && var1 != 6;
        if (this.isInPlaybackState() && var3 && this.mMediaController != null) {
            if (var1 == 79 || var1 == 85) {
                if (this.mMediaPlayer.isPlaying()) {
                    this.pause();
                    this.mMediaController.show();
                } else {
                    this.start();
                    this.mMediaController.hide();
                }

                return true;
            }

            if (var1 == 126) {
                if (!this.mMediaPlayer.isPlaying()) {
                    this.start();
                    this.mMediaController.hide();
                }

                return true;
            }

            if (var1 == 86 || var1 == 127) {
                if (this.mMediaPlayer.isPlaying()) {
                    this.pause();
                    this.mMediaController.show();
                }

                return true;
            }

            this.toggleMediaControlsVisiblity();
        }

        return super.onKeyDown(var1, var2);
    }

    private void setVideoLayout(int var1) {
        LayoutParams var2 = this.getLayoutParams();
        Pair var3 = Util.getResolution(this.mContext);
        int var4 = ((Integer) var3.first).intValue();
        int var5 = ((Integer) var3.second).intValue();
        float var6 = (float) var4 / (float) var5;
        int var7 = this.mVideoSarNum;
        int var8 = this.mVideoSarDen;
        if (this.mVideoHeight > 0 && this.mVideoWidth > 0) {
            float var9 = (float) this.mVideoWidth / (float) this.mVideoHeight;
            if (var7 > 0 && var8 > 0) {
                var9 = var9 * (float) var7 / (float) var8;
            }

            this.mSurfaceHeight = this.mVideoHeight;
            this.mSurfaceWidth = this.mVideoWidth;
            if (0 == var1 && this.mSurfaceWidth < var4 && this.mSurfaceHeight < var5) {
                var2.width = (int) ((float) this.mSurfaceHeight * var9);
                var2.height = this.mSurfaceHeight;
            } else if (var1 == 3) {
                var2.width = var6 > var9 ? var4 : (int) (var9 * (float) var5);
                var2.height = var6 < var9 ? var5 : (int) ((float) var4 / var9);
            } else {
                boolean var10 = var1 == 2;
                var2.width = !var10 && var6 >= var9 ? (int) (var9 * (float) var5) : var4;
                var2.height = !var10 && var6 <= var9 ? (int) ((float) var4 / var9) : var5;
            }

            this.setLayoutParams(var2);
            this.getHolder().setFixedSize(this.mSurfaceWidth, this.mSurfaceHeight);
            String var11 = String.format("VIDEO: %dx%dx%f[SAR:%d:%d], Surface: %dx%d, LP: %dx%d, Window: %dx%dx%f", new Object[]{Integer.valueOf(this.mVideoWidth), Integer.valueOf(this.mVideoHeight), Float.valueOf(var9), Integer.valueOf(this.mVideoSarNum), Integer.valueOf(this.mVideoSarDen), Integer.valueOf(this.mSurfaceWidth), Integer.valueOf(this.mSurfaceHeight), Integer.valueOf(var2.width), Integer.valueOf(var2.height), Integer.valueOf(var4), Integer.valueOf(var5), Float.valueOf(var6)});
            Log.i("MyVideoView", var11);
        }

        this.mVideoLayout = var1;
    }

    private void toggleMediaControlsVisiblity() {
        if (this.mMediaController.isShowing()) {
            this.mMediaController.hide();
        } else {
            this.mMediaController.show();
        }

    }

    public void setMediaBufferingIndicator(View var1) {
        if (this.mMediaBufferingIndicator != null) {
            this.mMediaBufferingIndicator.setVisibility(GONE);
        }

        this.mMediaBufferingIndicator = var1;
    }

    public void setVolume(float var1, float var2) {
        if (this.mMediaPlayer != null) {
            this.mMediaPlayer.setVolume(var1, var2);
        }

    }

    public void start() {
        if (this.mCurrentState == 5 && !Util.isUrlLocalFile(this.mUri.toString())) {
            this.setVideoURI(this.mUri);
            this.mTargetState = 3;
        } else {
            if (this.isInPlaybackState()) {
                this.mMediaPlayer.start();
                this.mCurrentState = 3;
            }

            this.mTargetState = 3;
        }
    }

    public void restart(){
        if (isInPlaybackState()) {
            //遇到IO错误的时候需要重新建立连接
            setVideoPath(mVideoPath);
            seekTo(mLastPosition);
        }
        start();
    }

    public void pause() {

        mLastPosition = getCurrentPosition();

        if (this.isInPlaybackState() && this.mMediaPlayer.isPlaying()) {
            this.mMediaPlayer.pause();
            this.mCurrentState = 4;
        }

        this.mTargetState = 4;
    }

    public long getDuration() {
        return this.isInPlaybackState() ? this.mMediaPlayer.getDuration() : -1L;
    }

    public long getCurrentPosition() {
        long mCurrentPosition = this.isInPlaybackState() ? this.mMediaPlayer.getCurrentPosition() : 0L;
        if (mCurrentPosition <= 0 && null != mMediaController) {
            mCurrentPosition = (int)
                    ((BaseMediaController) this.mMediaController).getProgress() * getDuration() / 100;
        }
        return mCurrentPosition;
    }

    public void seekTo(long var1) {
        if (this.isInPlaybackState()) {
            this.mMediaPlayer.seekTo(var1);
            this.mSeekWhenPrepared = 0L;
        } else {
            this.mSeekWhenPrepared = var1;
        }

    }

    public boolean isPlaying() {
        return this.isInPlaybackState() && this.mMediaPlayer.isPlaying();
    }

    public int getBufferPercentage() {
        return this.mMediaPlayer != null ? this.mCurrentBufferPercentage : 0;
    }

    public boolean canPause() {
        return this.mCanPause;
    }

    public boolean canSeekBackward() {
        return this.mCanSeekBack;
    }

    public boolean canSeekForward() {
        return this.mCanSeekForward;
    }

    public boolean isInPlaybackState() {
        return this.mMediaPlayer != null && this.mCurrentState != -1 && this.mCurrentState != 0 && this.mCurrentState != 1;
    }

    public void setAVOptions(AVOptions var1) {
        if (var1 == null) {
            throw new IllegalArgumentException("Illegal options:" + var1);
        } else {
            this.mAVOptions = var1;
        }
    }

    public AVOptions getAVOptions() {
        return this.mAVOptions;
    }

    /**
     * 记录暂停点
     */
    public void setPointPosition(){
        mLastPosition = getCurrentPosition();
    }

    /**
     * 返回最近记录的暂停点
     * @return
     */
    public long getLastPosition(){
        return  mLastPosition;
    }
}
