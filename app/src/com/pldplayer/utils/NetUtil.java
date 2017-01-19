package com.pldplayer.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * @author 任益波
 * @date 2016/10/28
 * @description:网络工具类
 */
public class NetUtil {

    /**
     * 无网络连接
     */
    public static final int NETWORK_NONE = -1;

    /**
     * 手机网络连接
     */
    public static final int NETWORK_MOBILE = 0;

    /**
     * Wifi网络连接
     */
    public static final int NETWORK_WIFI = 1;

    public static int getNetWorkState(Context context) {
        ConnectivityManager mConnectManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mNetInfo = mConnectManager.getActiveNetworkInfo();
        if (mNetInfo != null && mNetInfo.isConnected()) {
            if (mNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                return NETWORK_WIFI;
            } else if (mNetInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                return NETWORK_MOBILE;
            }
        } else {
            return NETWORK_NONE;
        }
        return NETWORK_NONE;
    }

}
