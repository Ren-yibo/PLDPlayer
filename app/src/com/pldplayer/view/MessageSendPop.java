package com.pldplayer.view;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.pldplayer.R;


/**
 * @author 任益波
 * @date 2016/11/14
 * @description:直播发消息弹窗
 */
public class MessageSendPop extends PopupWindow {
    private View mRootView;
    private TextView mSendTv;
    private EditText mMsgEt;
    private IChatMsgSendListener mSendListener;

    public MessageSendPop(Context context) {
        LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mRootView = mInflater.inflate(R.layout.pop_message_send, null);
        mSendTv = (TextView) mRootView.findViewById(R.id.popMsgSendTv);
        mMsgEt = (EditText) mRootView.findViewById(R.id.popMsgSendEt);
        this.setContentView(mRootView);
        this.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        this.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        this.setFocusable(true);
        this.setOutsideTouchable(true);
        // 刷新状态
        this.update();
        // 实例化一个ColorDrawable颜色为半透明
        ColorDrawable dw = new ColorDrawable(0000000000);
        // 点back键和其他地方使其消失,设置了这个才能触发OnDismisslistener ，设置其他控件变化等操作
        this.setBackgroundDrawable(dw);
        //设置Popwindow显示在软键盘之上
        this.setSoftInputMode(PopupWindow.INPUT_METHOD_NEEDED);
        this.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        mSendTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null == mSendListener) {
                    Log.e("MessageSendPop", "Send Message Callback Not Null");
                } else {
                    mSendListener.sendChatMsg(mMsgEt.getText().toString());
                    mMsgEt.setText("");
                }
            }
        });
    }

    public void setSendMsgListener(IChatMsgSendListener listener) {
        this.mSendListener = listener;
    }

    public interface IChatMsgSendListener {
        public void sendChatMsg(String msg);
    }
}
