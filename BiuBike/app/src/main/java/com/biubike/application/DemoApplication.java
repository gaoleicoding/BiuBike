package com.biubike.application;

import android.app.Application;
import android.os.Environment;

import com.baidu.mapapi.SDKInitializer;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;

import java.io.File;

/**
 * Created by gaolei on 16/12/28.
 */

public class DemoApplication extends Application {

    public static String mSDCardPath;
    public static final String APP_FOLDER_NAME = "BiuBike";

    public void onCreate() {
        super.onCreate();
        SDKInitializer.initialize(getApplicationContext());
        SpeechUtility.createUtility(this, SpeechConstant.APPID +"=58f9ff61");
        CrashHandler crashHandler=CrashHandler.getInstance();
        crashHandler.init(this);
        initDirs();
    }

    private boolean initDirs() {
        mSDCardPath = Environment.getExternalStorageDirectory().toString();
        if (mSDCardPath == null) {
            return false;
        }
        File f = new File(mSDCardPath, APP_FOLDER_NAME);
        if (!f.exists()) {
            try {
                f.mkdirs();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }
}
