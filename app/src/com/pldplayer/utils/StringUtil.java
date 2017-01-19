package com.pldplayer.utils;

import android.text.TextUtils;

/**
 * @author 任益波
 * @date 2016/8/11 16:40
 * @description
 */
public class StringUtil {

    /**
     * 判断字符串是否为空或null
     *
     * @param str
     * @return
     */
    public static boolean isNullString(String str) {
        return TextUtils.isEmpty(str) || str.equals("null");
    }
}
