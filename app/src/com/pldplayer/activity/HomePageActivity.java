package com.pldplayer.activity;

import android.os.Bundle;
import android.view.View;

import com.pldplayer.R;

/**
 * @author 任益波
 * @date 2017/1/18
 * @description
 */
public class HomePageActivity extends BaseActivity implements View.OnClickListener {

    @Override
    protected void beforeOnCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_homepage);
    }

    @Override
    protected void afterOnCreate(Bundle savedInstanceState) {
        getElemetView(R.id.homepage_live_tv).setOnClickListener(this);
        getElemetView(R.id.homepage_video_tv).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.homepage_live_tv:
                //直播
                startNewActivity(LiveActivity.class);
                break;
            case R.id.homepage_video_tv:
                //视频
                startNewActivity(VideoActivity.class);
                break;
            default:
                break;
        }
    }
}
