package com.biubike;

import android.view.inputmethod.InputMethodManager;

import java.text.DecimalFormat;

import static com.biubike.CodeUnlockActivity.imm;

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
}
