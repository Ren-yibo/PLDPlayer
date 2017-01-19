package com.pldplayer.utils;

import android.content.Context;
import android.content.res.Resources;
import android.text.Html;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.view.View;
import android.widget.TextView;

/**
 * @author 任益波
 * @date 2016/8/11 16:38
 * @description
 */
public class ViewUtil {

    /**
     * 获得string文件中的字符串
     *
     * @param resId
     * @return
     */
    public static String getResourceStr(Context context, int resId) {
        return context.getResources().getString(resId);
    }

    /**
     * 获得格式化后的字条串
     *
     * @param resId
     * @param args
     * @return
     */
    public static String getResourceStr(Context context, int resId, Object... args) {
        return String.format(getResourceStr(context, resId), args);
    }

    /**
     * 获得Html格式的Spanned对象
     *
     * @param resource
     * @param id
     * @param objects
     * @return
     */
    public static Spanned getSpannedHtml(Resources resource, int id, Object... objects) {
        return Html.fromHtml(String.format(resource.getString(id), objects));
    }

    /**
     * 设置值后，光标自动到最后
     *
     * @param mEditTv
     * @param str
     */
    public static void setSpanText(TextView mEditTv, String str) {
        mEditTv.setText(str);
        CharSequence text = mEditTv.getText();
        if (text instanceof Spannable) {
            Spannable spanText = (Spannable) text;
            Selection.setSelection(spanText, text.length());
        }
    }


    /**
     * @param convertView
     * @param id
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <V extends View> V getElement(View convertView, int id) {
        View targetView = convertView.findViewById(id);
        return (V) targetView;
    }

    /**
     * 设置TextView文本
     *
     * @param context
     * @param textview
     * @param obj
     */
    public static void setTextObj(Context context, TextView textview, Object obj) {
        if (obj instanceof Integer) {
            textview.setText(context.getResources().getString((Integer) obj));
        } else if (obj instanceof String) {
            textview.setText((String) obj);
        } else if (obj instanceof SpannableStringBuilder) {
            textview.setText((SpannableStringBuilder) obj);
        }
    }

}
