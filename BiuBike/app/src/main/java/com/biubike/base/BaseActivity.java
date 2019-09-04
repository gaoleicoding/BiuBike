package com.biubike.base;

import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;

import java.lang.reflect.Field;


public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //7.0以上TRANSLUCENT_STATUS时部分手机状态栏有灰色遮罩，去掉它
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            View decorView = getWindow().getDecorView();
            /* getWindow().getDecorView()被调用后, getWindow().getAttributes().flags才有值。
             * getDecorView()正常调用时机：
             * ①setContentView()中；
             * ②onCreate()和onPostCreate()之间；
             * ③④⑤等等...
             * 所以下面的代码应当放在setContentView()或onPostCreate()中，
             * 但有的activity没有setContentView()，有的activity会在onCreate()中finish()，
             * 所以此处在onCreate()中手动调用一下getDecorView()。
             */
            if ((getWindow().getAttributes().flags & WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS) != 0) {
                try {
                    Field field = decorView.getClass().getDeclaredField("mSemiTransparentStatusBarColor");
                    field.setAccessible(true);
                    field.setInt(decorView, Color.TRANSPARENT);
                } catch (Exception ignored) {
                }
            }
        }
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }
}
