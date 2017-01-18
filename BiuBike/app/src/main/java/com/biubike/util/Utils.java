package com.biubike.util;

import android.content.Context;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import java.text.DecimalFormat;

import static com.biubike.activity.CodeUnlockActivity.imm;

/**
 * Created by gaolei on 16/12/30.
 */

public class Utils {

    public static void hideIMM() {
        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT,
                InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public static void showIMM() {
        imm.toggleSoftInput(0, InputMethodManager.RESULT_SHOWN);
    }
    // 距离转换
    public static String distanceFormatter(int distance) {
        if (distance < 1000) {
            return distance + "米";
        } else if (distance % 1000 == 0) {
            return distance / 1000 + "公里";
        } else {
            DecimalFormat df = new DecimalFormat("0.0");
            int a1 = distance / 1000; // 十位

            double a2 = distance % 1000;
            double a3 = a2 / 1000; // 得到个位

            String result = df.format(a3);
            double total = Double.parseDouble(result) + a1;
            return total + "公里";
        }
    }

    // 时间转换
    public static String timeFormatter(int minute) {
        if (minute < 60) {
            return minute + "分钟";
        } else if (minute % 60 == 0) {
            return minute / 60 + "小时";
        } else {
            int hour = minute / 60;
            int minute1 = minute % 60;
            return hour + "小时" + minute1 + "分钟";
        }
    }
    public static int dp2px(Context context, int dp) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    public static int px2dp(Context context, int px) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (px / scale + 0.5f);
    }

    public static int getScreenWidth(Context context)
    {
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.widthPixels;
    }

    public static void setSpannableStr(TextView textView, String str, int startIndex, int endIndex,float proporation) {
        SpannableString spannableString = new SpannableString(str);
        ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.parseColor("#393939"));
        spannableString.setSpan(colorSpan, startIndex, endIndex, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        RelativeSizeSpan sizeSpan01 = new RelativeSizeSpan(proporation);
        spannableString.setSpan(sizeSpan01, startIndex, endIndex, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

        textView.setText(spannableString);
    }
}
