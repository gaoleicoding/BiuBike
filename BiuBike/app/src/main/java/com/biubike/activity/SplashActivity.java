package com.biubike.activity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.biubike.MainActivity;
import com.biubike.R;
import com.biubike.util.PermissionUtil;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.internal.functions.Functions;
import io.reactivex.internal.observers.LambdaObserver;


public class SplashActivity extends AppCompatActivity {

    //要申请的权限
    private String[] mPermissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO};

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        requestPermissions();
    }

    public void onRestart() {
        super.onRestart();
        requestPermissions();
    }

    private void requestPermissions() {
        RxPermissions rxPermission = new RxPermissions(SplashActivity.this);
        rxPermission
                .requestEachCombined(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.RECORD_AUDIO)
                .subscribe(new LambdaObserver<>(permission -> {
                    if (permission.granted) {
                        // All permissions are granted !
                        Observable.just("")
                                .delay(1, TimeUnit.SECONDS)
                                .subscribe(new LambdaObserver<>(s -> {
                                    Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                }, Functions.ON_ERROR_MISSING, Functions.EMPTY_ACTION, Functions.emptyConsumer()));
                    } else if (permission.shouldShowRequestPermissionRationale) {
                        // At least one denied permission without ask never again
                        requestPermissions();
                    } else {
                        // At least one denied permission with ask never again
                        // Need to go to the settings
                        String[] permissions = PermissionUtil.getDeniedPermissions(SplashActivity.this, mPermissions);
                        PermissionUtil.PermissionDialog(SplashActivity.this, PermissionUtil.permissionText(permissions));
                    }
                }, Functions.ON_ERROR_MISSING, Functions.EMPTY_ACTION, Functions.emptyConsumer()));

    }


}
