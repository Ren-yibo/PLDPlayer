package com.pldroid.player.widget;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.pldroid.R;

/**
 * @author 任益波
 * @date 2016/12/6
 * @description
 */
public class LiveController extends BaseMediaController implements View.OnClickListener {

    private ImageView mMsgIv;
    private ImageView mRecordIv;
    private IMsgClickCallBack iMsgClickCallBack;
    private IRecordClickCallBack iRecordClickCallBack;

    public LiveController(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutId(R.layout.layout_live_controller);
    }

    public LiveController(Context context) {
        super(context);
        setLayoutId(R.layout.layout_live_controller);
    }

    @Override
    void initControllerView(View v) {
        mBackBtn = (ImageButton) v.findViewById(R.id.mediaBackIb);
        mBackBtn.setOnClickListener(mDoBackListener);
        mTitleTv = (TextView) v.findViewById(R.id.mediaTitleTv);
        if (!TextUtils.isEmpty(mTitleStr)) {
            mTitleTv.setText(mTitleStr);
        }
        mMsgIv = (ImageView) v.findViewById(R.id.mediaMsgIv);
        if (mMsgIv != null) {
            mMsgIv.setOnClickListener(this);
        }
        mRecordIv = (ImageView) v.findViewById(R.id.mediaRecordIv);
        if (mRecordIv != null) {
            mRecordIv.setOnClickListener(this);
            mRecordIv.setSelected(true);
        }
    }

    @Override
    public void onClick(View v) {
        int mId = v.getId();
        if (mId == R.id.mediaMsgIv) {
            if (iMsgClickCallBack == null) {
                Log.e("LiveController", "MsgClickCallBack is null");
            } else {
                iMsgClickCallBack.doMsgClick();
            }
        }else if (mId == R.id.mediaRecordIv) {
            if (iRecordClickCallBack == null) {
                Log.e("LiveController", "RecordClickCallBack is null");
            } else if (mRecordIv != null) {
                mRecordIv.setSelected(!mRecordIv.isSelected());
                iRecordClickCallBack.doRecordClick(mRecordIv.isSelected());
            }
        }
    }

    /**
     * 点击消息回调
     */
    public interface IMsgClickCallBack {
        void doMsgClick();
    }

    public void setMsgClickCallBack(IMsgClickCallBack i) {
        this.iMsgClickCallBack = i;
    }

    /**
     * 点击消息记录回调
     */
    public interface IRecordClickCallBack {
        void doRecordClick(boolean isVisible);
    }

    public void setRecordClickCallBack(IRecordClickCallBack i) {
        this.iRecordClickCallBack = i;
    }
}
