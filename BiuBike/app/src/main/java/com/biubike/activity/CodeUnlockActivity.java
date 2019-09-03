package com.biubike.activity;

import android.hardware.Camera;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.biubike.MainActivity;
import com.biubike.R;
import com.biubike.base.BaseActivity;

/**
 * Created by gaolei on 16/12/29.
 */

public class CodeUnlockActivity extends BaseActivity {

    private boolean isFlashOpen = false;
    private ImageView ivFlash;
    public static boolean unlockSuccess = false;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_code_unlock);
        ivFlash = findViewById(R.id.iv_flash);
    }

    public void switchFlashlight(View view) {
        if (!isFlashOpen) {
            Camera camera = Camera.open();
            Camera.Parameters mParameters = camera.getParameters();
            mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);//打开Camera.Parameters.FLASH_MODE_OFF则为关闭
            camera.setParameters(mParameters);
            isFlashOpen = true;
            ivFlash.setImageResource(R.mipmap.ic_flash_open);
        } else {
            Camera camera = Camera.open();
            Camera.Parameters mParameters = camera.getParameters();
            mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);//打开Camera.Parameters.FLASH_MODE_OFF则为关闭
            camera.setParameters(mParameters);
            camera.release();
            isFlashOpen = false;
            ivFlash.setImageResource(R.mipmap.ic_flash_close);
        }
    }

    public void unlockSucess(View view) {
        unlockSuccess = true;
        setResult(RESULT_OK);
        finish();
    }

}
