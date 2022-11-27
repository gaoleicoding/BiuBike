package com.biubike.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;

import java.util.ArrayList;

public class PermissionUtil {


    /**
     * 检查所需权限中缺少的权限
     * 防止权限重复获取
     *
     * @param context
     * @param permissions
     * @return 返回缺少的权限，null 意味着没有缺少权限
     */
    public static String[] getDeniedPermissions(Activity context, String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ArrayList<String> deniedPermissionList = new ArrayList<>();
            for (String permission : permissions) {
                if (context.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                    deniedPermissionList.add(permission);
                }
            }
            int size = deniedPermissionList.size();
            if (size > 0) {
                return deniedPermissionList.toArray(new String[deniedPermissionList.size()]);
            }
        }
        return null;
    }

    /**
     * 提醒用户手动设置权限的对话框
     *
     * @param activity
     * @param text
     */
    public static void PermissionDialog(final Activity activity, String text) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setCancelable(false);
        builder.setMessage(text);
        builder.setNegativeButton("算了", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                activity.finish();//没有权限，关闭当前页面
            }
        });
        builder.setPositiveButton("去设置", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //去应用管理界面手动给予权限
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.parse("package:" + activity.getPackageName()));
                activity.startActivity(intent);
            }
        });
        builder.create().show();
    }

    /**
     * 权限的提示文字
     * 可以根据应用需求自行更改
     *
     * @param perms
     * @return
     */
    public static String permissionText(String[] perms) {
        StringBuilder sb = new StringBuilder();
        for (String s : perms) {
            if (s.equals(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                sb.append("位置权限(用于获取你自行车的位置;\n");
            } else if (s.equals(Manifest.permission.CAMERA)) {
                sb.append("相机权限(用于手电筒，扫描二维码);\n");
            } else if (s.equals(Manifest.permission.RECORD_AUDIO)) {
                sb.append("麦克风权限(用于导航语音);\n");
            } else if (s.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                sb.append("存储卡权限(用于缓存数据减少流量);\n");
            } else if (s.equals(Manifest.permission.WAKE_LOCK)) {
                sb.append("常驻通知栏(用于App在行程中不被中断);\n");
            }
        }
        return "程序正常需要如下权限：\n\n" + sb.toString();
    }
}

