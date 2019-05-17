package com.biubike.activity;

import android.content.Context;
import android.hardware.Camera;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.biubike.R;
import com.biubike.base.BaseActivity;
import com.biubike.util.StatusUtil;
import com.biubike.util.Utils;

/**
 * Created by gaolei on 16/12/29.
 */

public class CodeUnlockActivity extends BaseActivity {

    private boolean isFlashOpen = false;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_code_unlock);

        setStatusBar();
    }

    public void switchFlashlight(View view) {
        if (!isFlashOpen) {
            Camera camera = Camera.open();
            Camera.Parameters mParameters = camera.getParameters();
            mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);//打开Camera.Parameters.FLASH_MODE_OFF则为关闭
            camera.setParameters(mParameters);
            isFlashOpen = true;
        } else {
            Camera camera = Camera.open();
            Camera.Parameters mParameters = camera.getParameters();
            mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);//打开Camera.Parameters.FLASH_MODE_OFF则为关闭
            camera.setParameters(mParameters);
            camera.release();
            isFlashOpen = false;
        }
    }

    public void unlockSucess(View view) {
        StatusUtil.useStatus = 1;
        finish();

    }

}
