package com.biubike.util;

import android.app.Application;
import android.content.Context;

public class ContextUtil {
    private static Application mApplication;

    public static Context getAppContext() {
        return getApp().getApplicationContext();
    }

    public static synchronized Application getApp() {
        if (mApplication == null) {
            try {
                mApplication = (Application) Class.forName("android.app.ActivityThread").getMethod("currentApplication").invoke(null, (Object[]) null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return mApplication;
    }
}