package com.biubike;

import android.view.inputmethod.InputMethodManager;

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
}
