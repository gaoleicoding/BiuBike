package com.biubike.application;

import android.app.AlarmManager;
import android.app.Application;
import android.content.Context;

import com.baidu.mapapi.SDKInitializer;

/**
 * Created by gaolei on 16/12/28.
 */

public class DemoApplication extends Application {

    public static AlarmManager am;
    public void onCreate(){
        super.onCreate();
        SDKInitializer.initialize(getApplicationContext());
        am = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
    }
}
