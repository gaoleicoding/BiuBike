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

//    public int statusBarHeight = 0,titleHeight;


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

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            //透明状态栏
//            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//            //透明导航栏
//            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
//        }
//        statusBarHeight = getStatusBarHeight();
//        titleHeight=dp2px(this,50);
    }

    /**
     * 设置沉浸式状态栏
     */
//    protected void setStatusBar() {
 /*       if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            final ViewGroup linear_bar = (ViewGroup) findViewById(R.id.title_layout);
            final int statusHeight = getStatusBarHeight();
            linear_bar.post(new Runnable() {
                @Override
                public void run() {
                    Log.d("gaolei","statusHeight--------"+statusHeight);
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) linear_bar.getLayoutParams();
                    params.height = statusHeight + titleHeight;
                    linear_bar.setLayoutParams(params);
                }
            });
        }
    }

    protected void setStatusBarLayout() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            final ViewGroup linear_bar = (ViewGroup) findViewById(R.id.title_layout);
            final int statusHeight = getStatusBarHeight();
            linear_bar.post(new Runnable() {
                @Override
                public void run() {
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) linear_bar.getLayoutParams();
                    params.height = statusHeight ;
                    linear_bar.setLayoutParams(params);
                }
            });
        }
    }*/

    /**
     * 获取状态栏的高度
     *
     * @return
     */
//    protected int getStatusBarHeight() {
//        try {
//            Class<?> c = Class.forName("com.android.internal.R$dimen");
//            Object obj = c.newInstance();
//            Field field = c.getField("status_bar_height");
//            int x = Integer.parseInt(field.get(obj).toString());
//            return getResources().getDimensionPixelSize(x);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return 0;
//    }
//
//    public void finishActivity(View view) {
//        finish();
//    }
}
