package com.pldplayer.activity;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ListView;
import android.widget.Toast;

import com.pili.pldroid.player.PLMediaPlayer;
import com.pldplayer.R;
import com.pldplayer.adapter.LiveChatLvAdapter;
import com.pldplayer.utils.NetUtil;
import com.pldplayer.view.MessageSendPop;
import com.pldroid.player.activity.LivePlayerActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 任益波
 * @date 2017/1/18
 * @description
 */
public class LiveActivity extends LivePlayerActivity implements
        MessageSendPop.IChatMsgSendListener {

    private MessageSendPop mSendPop;
    private ListView mChatLv;
    private List<String> mList;
    private LiveChatLvAdapter mAdapter;

    @Override
    protected void beforeOnCreate(Bundle savedInstanceState) {
//        super.beforeOnCreate(savedInstanceState);
        setContentView(R.layout.activity_live);
        setMediaTitle("LiveTest");
        mVideoPath = "rtmp://live.hkstv.hk.lxdns.com/live/hks";
    }

    @Override
    protected void afterOnCreate(Bundle savedInstanceState) {
        super.afterOnCreate(savedInstanceState);
        mSendPop = new MessageSendPop(this);
        mSendPop.setSendMsgListener(this);
        mChatLv = (ListView) findViewById(R.id.liveChatLv);
        mAdapter = new LiveChatLvAdapter(this);
        mChatLv.setAdapter(mAdapter);
        mList = new ArrayList<>();
    }

    @Override
    public void onPrepared(PLMediaPlayer plMediaPlayer) {
        super.onPrepared(plMediaPlayer);
        mVideoView.start();
    }

    @Override
    protected void onNetDiscount() {
        super.onNetDiscount();
        Toast.makeText(this, "Net Error", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onTimeout() {
        super.onTimeout();
        if (NetUtil.getNetWorkState(this) == NetUtil.NETWORK_NONE) {
            Toast.makeText(this, "Net Disconnection", Toast.LENGTH_SHORT).show();
        } else if (NetUtil.getNetWorkState(this) == NetUtil.NETWORK_MOBILE) {
            Toast.makeText(this, "pay attention to Mobile Net!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void sendChatMsg(String msg) {
        if (TextUtils.isEmpty(msg)) {
            Toast.makeText(this, "Chat Message cannot null", Toast.LENGTH_SHORT).show();
            return;
        }
        if (mList == null || mAdapter == null) {
            return;
        }
        mList.add(new String(msg));
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAdapter.invalidateData(mList);
            }
        });
    }

    /**
     * 主动打开软键盘
     */
    private void openSoftKeyBoard() {
        InputMethodManager m = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        m.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
    }

    @Override
    public void doMsgClick() {
        super.doMsgClick();
        openSoftKeyBoard();
        mSendPop.showAtLocation(mVideoView, Gravity.BOTTOM, 0, 0);
    }

    @Override
    public void doRecordClick(boolean isVisible) {
        super.doRecordClick(isVisible);
        mChatLv.setVisibility(isVisible ? View.VISIBLE : View.INVISIBLE);
    }
}
