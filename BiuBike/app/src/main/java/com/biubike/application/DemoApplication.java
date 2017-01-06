package com.biubike.application;

import android.app.Application;

import com.baidu.mapapi.SDKInitializer;

/**
 * Created by gaolei on 16/12/28.
 */

public class DemoApplication extends Application {
    public void onCreate(){
        super.onCreate();
        SDKInitializer.initialize(getApplicationContext());
    }
}
