package com.biubike.application;

import android.app.Application;
import android.os.Environment;

import com.baidu.mapapi.SDKInitializer;
import com.biubike.map.TraceUtil;

import java.io.File;

/**
 * Created by gaolei on 16/12/28.
 */

public class DemoApplication extends Application {

    public static String mSDCardPath;
    public static final String APP_FOLDER_NAME = "BiuBike";

    public void onCreate() {
        super.onCreate();
        // 若为创建独立进程，则不初始化成员变量
        if ("com.baidu.track:remote".equals(TraceUtil.getCurProcessName(this))) {
            return;
        }
        SDKInitializer.initialize(getApplicationContext());
//        CrashHandler crashHandler=CrashHandler.get();
//        crashHandler.init(this);
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
