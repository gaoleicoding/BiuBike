/*
package com.biubike.service;


import com.baidu.location.LocationClient;

*
 * Created by gaolei on 17/2/4.




import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.biubike.bean.RoutePoint;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.R.attr.type;


//        当前位置:我的异常网» Android » Android使用百度LBS SDK（4）记录和显示行走轨迹
//        Android使用百度LBS SDK（4）记录和显示行走轨迹
//        www.MyException.Cn 网友分享于：2015-04-01浏览：0次
//
//
//        Android使用百度LBS SDK（四）记录和显示行走轨迹
//        记录轨迹思路
//        用Service获取经纬度，onCreate中开始采集经纬度点，保存到ArrayList
//        每隔5秒取样一次，若经纬度未发生变化，丢弃该次取样
//        在onDestroy中，将ArrayList转成JSON格式，然后存储到SDCard中
//        显示轨迹思路
//        读取目录下所有轨迹文件，并生成ListView
//        在OnItemClick中将文件名称通过intent.putExtra传递给显示轨迹的Activity
//        根据文件名将对应的JSON内容转成ArrayList
//        然后将以上ArrayList的点集依次连线，并绘制到百度地图上
//        设置起始点Marker，Zoom级别,中心点为起始点
//        轨迹点小于2个无法绘制轨迹，给出提示
//        初步Demo效果图，获取的经纬度有偏移，明天看看哪里的问题：
//        LBS
//        先贴一个保存经纬度点的Service的核心代码：

public class RouteRecordService extends Service {
    private LocationClient mLocationClient;
    private final String ROUTE_PATH = "/sdcard/Route/";
    private String startTime = "";
    private String stopTime = "";

    private List<RoutePoint> list = new ArrayList<RoutePoint>();
    private RouteAdapter adapter = new RouteAdapter();

    private int startId = 1; // 轨迹点初始ID
    private int defaultDelay = 5000; // 轨迹点取样间隔时间:ms
    private final static double ERROR_CODE = 55.555;
    private double routeLng;
    private double routeLat;

    private boolean isEncrypt = false; // true:读取百度加密经纬度 false:读取设备提供经纬度
    private boolean isDebug = true;

    // 设备定位经纬度
    private enum DeviceLocType {
        LATITUDE, LONGITUDE
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        InitLocation(LocationClientOption.LocationMode.Hight_Accuracy, "gcj02", 1000, false);
        // 初始化路径
        File filestoreMusic = new File(ROUTE_PATH);
        if (!filestoreMusic.exists()) {
            filestoreMusic.mkdir();
        }
        startTime = getTimeStr();
        if (isDebug) {
            Toast.makeText(getApplicationContext(), "Start Record Route",
                    Toast.LENGTH_SHORT).show();
        }

        // 开启轨迹记录线程
        new Thread(new RouteRecordThread()).start();
    }

    public class RouteRecordThread implements Runnable {

        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(defaultDelay);
                    Message message = new Message();
                    message.what = 1;
                    recordHandler.sendMessage(message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    final Handler recordHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    startRecordRoute();
            }
            super.handleMessage(msg);
        }
    };

    private void startRecordRoute() {
        // 获取设备经纬度
        if (!isEncrypt) {
            routeLat = getDeviceLocation(DeviceLocType.LATITUDE);
            routeLng = getDeviceLocation(DeviceLocType.LONGITUDE);
            if (isDebug)
                Toast.makeText(getApplicationContext(),
                        "Device Loc:" + routeLat + "," + routeLng,
                        Toast.LENGTH_SHORT).show();
        }

        RoutePoint routePoint = new RoutePoint();
        if (routeLng != 5.55 && routeLat != 5.55) {
            if (list.size() > 0
                    && list.get(list.size() - 1).getLat() == routeLat
                    && (list.get(list.size() - 1).getLng() == routeLng)) {
                if (isDebug) {
                    // Toast.makeText(getApplicationContext(),
                    // "Route not change",
                    // Toast.LENGTH_SHORT).show();
                }
            } else {
                routePoint.setId(startId++);
                routePoint.setLng(routeLng);
                routePoint.setLat(routeLat);
                list.add(routePoint);
            }
        }
    }



*
     * 获取设备提供的经纬度，Network或GPS
     *
     * @param type
     *            请求经度还是纬度
     * @return


    private double getDeviceLocation(DeviceLocType type) {
        double deviceLat = ERROR_CODE;
        double deviceLng = ERROR_CODE;

        LocationManager locationManager = (LocationManager) getSystemService(getApplicationContext().LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Location location = locationManager
                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {
                deviceLat = location.getLatitude();
                deviceLng = location.getLongitude();
            } else {
                locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER, 1000, 0,
                        new deviceLocationListener());
                Location location1 = locationManager
                        .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if (location1 != null) {
                    deviceLat = location1.getLatitude(); // 经度
                    deviceLng = location1.getLongitude(); // 纬度
                }
            }
        } else {
            locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, 1000, 0,
                    new deviceLocationListener());
            Location location = locationManager
                    .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (location != null) {
                deviceLat = location.getLatitude(); // 经度
                deviceLng = location.getLongitude(); // 纬度
            }
        }
        if (type == DeviceLocType.LATITUDE)
            return deviceLat;
        else if (type == DeviceLocType.LONGITUDE)
            return deviceLng;
        else
            return ERROR_CODE;
    }



*
     * 设备位置监听器
     *


    class deviceLocationListener implements LocationListener {

        // Provider的状态在可用、暂时不可用和无服务三个状态直接切换时触发此函数
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        // Provider被enable时触发此函数，比如GPS被打开
        @Override
        public void onProviderEnabled(String provider) {

        }

        // Provider被disable时触发此函数，比如GPS被关闭
        @Override
        public void onProviderDisabled(String provider) {
        }

        // 当坐标改变时触发此函数，如果Provider传进相同的坐标，它就不会被触发
        @Override
        public void onLocationChanged(Location location) {
            // routeLat = location.getLatitude(); // 经度
            // routeLng = location.getLongitude(); // 纬度
        }
    };

    private String getTimeStr() {
        long nowTime = System.currentTimeMillis();
        Date date = new Date(nowTime);
        String strs = "" + ERROR_CODE;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
            strs = sdf.format(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return strs;
    }



*
     * 初始化轨迹文件路径和名称
     *
     * @return String


    private String getFilePath() {
        stopTime = getTimeStr();
        String format = ".json";
        if (isDebug)
            format = ".txt";
        return ROUTE_PATH + startTime + "-" + stopTime + format;
    }

    class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            // 读取百度加密经纬度
            if (isEncrypt) {
                routeLng = location.getLongitude();
                routeLat = location.getLatitude();
            }
        }
    }

}

*/
