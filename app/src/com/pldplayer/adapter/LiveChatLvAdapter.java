package com.pldplayer.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pldplayer.R;
import com.pldplayer.utils.ViewUtil;

/**
 * @author 任益波
 * @date 2016/12/7
 * @description
 */
public class LiveChatLvAdapter extends CommonAdapter<String> {

    public LiveChatLvAdapter(Context context) {
        super(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (null == convertView) {
            convertView = getInflateView(R.layout.item_live_chat);
            holder = new ViewHolder();
            holder.mContentTv = getElement(R.id.itemLiveChatContentTv);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        final String mChat = list.get(position);
        holder.mContentTv.setText(ViewUtil.getSpannedHtml(mContext.getResources(),
                R.string.liveChatNormal, new Object[]{"我", mChat}));
        return convertView;
    }

    private class ViewHolder {
        public TextView mContentTv;
    }
}
