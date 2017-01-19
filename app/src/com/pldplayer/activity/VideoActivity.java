package com.pldplayer.activity;

import android.os.Bundle;
import android.widget.Toast;

import com.pldplayer.R;
import com.pldplayer.utils.NetUtil;
import com.pldroid.player.activity.VideoPalyerActivity;

/**
 * @author 任益波
 * @date 2017/1/19
 * @description
 */
public class VideoActivity extends VideoPalyerActivity {

    @Override
    protected void beforeOnCreate(Bundle savedInstanceState) {
//        super.beforeOnCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        setMediaTitle("Rolling");
        mVideoPath = "http://47.90.90.123/Rolling.mp4";
    }


    @Override
    protected void onNetDiscount() {
        super.onNetDiscount();
        Toast.makeText(this, "Net Disconnection", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onTimeout() {
        super.onTimeout();
        if (NetUtil.getNetWorkState(this) == NetUtil.NETWORK_NONE) {
            Toast.makeText(this, "No net now", Toast.LENGTH_SHORT).show();
        } else if (NetUtil.getNetWorkState(this) == NetUtil.NETWORK_MOBILE) {
            Toast.makeText(this, "Mobile net now", Toast.LENGTH_SHORT).show();
        }
    }
}
