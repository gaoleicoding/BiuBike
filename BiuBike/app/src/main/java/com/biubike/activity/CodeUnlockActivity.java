package com.biubike.activity;

import android.Manifest;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.biubike.R;
import com.biubike.base.BaseActivity;
import com.biubike.util.PermissionUtil;
import com.tbruyelle.rxpermissions2.RxPermissions;

import io.reactivex.internal.functions.Functions;
import io.reactivex.internal.observers.LambdaObserver;

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

    public void switchFlashlight() {
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

    public void onRestart() {
        super.onRestart();
        requestCameraPermission(ivFlash);
    }
    public void requestCameraPermission(View view) {
        RxPermissions rxPermission = new RxPermissions(CodeUnlockActivity.this);
        rxPermission
                .requestEachCombined(Manifest.permission.CAMERA)
                .subscribe(new LambdaObserver<>(permission -> {
                    if (permission.granted) {
                        // All permissions are granted !
                        switchFlashlight();
                    } else if (permission.shouldShowRequestPermissionRationale) {
                        // At least one denied permission without ask never again
                        requestCameraPermission(ivFlash);
                    } else {
                        // At least one denied permission with ask never again
                        // Need to go to the settings
                        String[] permissions = PermissionUtil.getDeniedPermissions(CodeUnlockActivity.this, Manifest.permission.CAMERA);
                        PermissionUtil.PermissionDialog(CodeUnlockActivity.this, PermissionUtil.permissionText(permissions));
                    }
                }, Functions.ON_ERROR_MISSING, Functions.EMPTY_ACTION, Functions.emptyConsumer()));

    }
}
