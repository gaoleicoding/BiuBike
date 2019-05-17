package com.biubike.activity;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;

import com.biubike.MainActivity;
import com.biubike.R;
import com.biubike.util.PermissionUtil;

import java.util.Arrays;
import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;


public class SplashActivity extends Activity implements EasyPermissions.PermissionCallbacks, EasyPermissions.RationaleCallbacks {

    //要申请的权限
    private String[] mPermissions = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA, Manifest.permission.READ_PHONE_STATE};
    public static final int CODE = 0x001;
    private final int DISMISS_SPLASH = 0;
    String TAG="gaolei";


    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DISMISS_SPLASH:
                    Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                    break;
            }
        }
    };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        EasyPermissions.requestPermissions(this, PermissionUtil.permissionText(mPermissions), CODE, mPermissions);
    }


    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        if(perms.size()==5)
        handler.sendEmptyMessageDelayed(DISMISS_SPLASH, 2000);
        Log.d(TAG,"onPermissionsGranted---------"+Arrays.toString(perms.toArray()) );

    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        Log.d(TAG,"onPermissionsDenied---------"+Arrays.toString(perms.toArray()) );

        //存在被永久拒绝(拒绝&不再询问)的权限
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            mPermissions = PermissionUtil.getDeniedPermissions(this, mPermissions);
            PermissionUtil.PermissionDialog(this, PermissionUtil.permissionText(mPermissions) + "App正常使用请设置以下权限");
        } else {
            EasyPermissions.requestPermissions(this, PermissionUtil.permissionText(mPermissions), CODE, mPermissions);
        }

    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
         Log.d(TAG,"onRequestPermissionsResult---------permissions："+Arrays.toString(permissions) );
        //将请求结果传递EasyPermission库处理
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onRationaleAccepted(int requestCode) {
        Log.d(TAG,"onRationaleAccepted---------");

    }

    @Override
    public void onRationaleDenied(int requestCode) {
        Log.d(TAG,"onRationaleDenied---------");
    }
}
