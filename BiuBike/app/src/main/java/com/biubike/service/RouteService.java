package com.biubike.service;

/**
 * Created by gaolei on 17/2/4.
 */

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import com.biubike.MainActivity;
import com.biubike.R;
import com.biubike.activity.RouteDetailActivity;
import com.biubike.bean.RoutePoint;
import com.biubike.bean.RoutePoints;
import com.biubike.database.RouteDBHelper;
import com.biubike.util.Utils;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;

import java.text.DecimalFormat;
import java.util.ArrayList;

import static android.net.NetworkInfo.State;


//        记录轨迹思路:
//        用Service获取经纬度，onCreate中开始采集经纬度点，保存到到List
//        每隔2秒取样一次，若经纬度未发生变化，丢弃该次取样
//        在onDestroy中，将List转成JSON格式，然后存储到sqlite
//
//        显示轨迹思路:
//        读取sqlite下所有轨迹文件，显示到recyclerview
//        在OnItemClick中将文件名称通过intent.putExtra传递给显示轨迹的Activity
//        根据文件名将对应的JSON内容转成ArrayList
//        然后将以上ArrayList的点集依次连线，并绘制到百度地图上
//        设置起始点Marker，Zoom级别,中心点为起始点
//        轨迹点小于2个无法绘制轨迹


public class RouteService extends Service {

    private LocationClient mlocationClient = null;
    private MylocationListener mlistener;
    protected String rt_price;
    public ArrayList<RoutePoint> routPointList = new ArrayList<>();
    public int totalDistance = 0;
    public int totalPrice = 0;
    public long beginTime = 0, totalTime = 0;
    private String showDistance, showTime, showPrice;
    private Notification notification;
    private RemoteViews contentView;

    public void onCreate() {
        super.onCreate();
        beginTime = System.currentTimeMillis();

        totalTime = 0;
        totalDistance = 0;
        totalPrice = 0;
        routPointList.clear();
        initLocation();//初始化LocationgClient
        initNotification();
        Utils.acquireWakeLock(this);
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    private void initNotification() {
        int icon = R.mipmap.bike_icon2;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel("id_route", "route");
        }
        contentView = new RemoteViews(getPackageName(), R.layout.notification_layout);
        notification = new NotificationCompat.Builder(this, "id_route").setContent(contentView).setSmallIcon(icon).build();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.putExtra("flag", "notification");
        notification.contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void createNotificationChannel(String channelId, String channelName) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (notificationManager.getNotificationChannel(channelId) != null) return;

        NotificationChannel notificationChannel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW);
        notificationChannel.enableVibration(false);
        notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        notificationChannel.setShowBadge(true);
        notificationChannel.setBypassDnd(true);

        notificationManager.createNotificationChannel(notificationChannel);
    }

    private void initLocation() {

        //定位图层显示方式
        MyLocationConfiguration.LocationMode locationMode = MyLocationConfiguration.LocationMode.NORMAL;

        //定位服务的客户端。宿主程序在客户端声明此类，并调用，目前只支持在主线程中启动
        mlocationClient = new LocationClient(this);
        mlistener = new MylocationListener();
//        initMarkerClickEvent();
        //注册监听器
        mlocationClient.registerLocationListener(mlistener);
        //配置定位SDK各配置参数，比如定位模式、定位时间间隔、坐标系类型等
        LocationClientOption mOption = new LocationClientOption();
        //设置坐标类型
        mOption.setCoorType("bd09ll");
        //设置是否需要地址信息，默认为无地址
        mOption.setIsNeedAddress(true);
        //设置是否打开gps进行定位
        mOption.setOpenGps(true);
        //设置扫描间隔，单位是毫秒 当<1000(1s)时，定时定位无效
        mOption.setScanSpan(2000);
        //设置 LocationClientOptionƒ20
        mlocationClient.setLocOption(mOption);

        if (!mlocationClient.isStarted()) {
            mlocationClient.start();
        }
    }


    public IBinder onBind(Intent intent) {
        return null;
    }

    public boolean onUnBind(Intent intent) {
        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mlocationClient.stop();
        Gson gson = new Gson();
        String routeListStr = gson.toJson(routPointList);
        Bundle bundle = new Bundle();
        bundle.putString("totalTime", showTime);
        bundle.putString("totalDistance", showDistance);
        bundle.putString("totalPrice", showPrice);
        bundle.putString("routePoints", routeListStr);
        Intent intent = new Intent(this, RouteDetailActivity.class);
        intent.putExtras(bundle);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        if (routPointList.size() > 2)
            insertData(routeListStr);
        Utils.releaseWakeLock();
        stopForeground(true);
        showTime = "";
        showDistance = "";
        showPrice = "";
    }


    //所有的定位信息都通过接口回调来实现
    public class MylocationListener implements BDLocationListener {
        //定位请求回调接口
        private boolean isFirstIn = true;

        //定位请求回调函数,这里面会得到定位信息
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            if (null == bdLocation) return;

            //"4.9E-324"表示目前所处的环境（室内或者是网络状况不佳）造成无法获取到经纬度
            if ("4.9E-324".equals(String.valueOf(bdLocation.getLatitude())) || "4.9E-324".equals(String.valueOf(bdLocation.getLongitude()))) {
                return;
            }//过滤百度定位失败

            double routeLat = bdLocation.getLatitude();
            double routeLng = bdLocation.getLongitude();
            RoutePoint routePoint = new RoutePoint();
            routePoint.setRouteLat(routeLat);
            routePoint.setRouteLng(routeLng);
            if (routPointList.size() == 0)
                routPointList.add(routePoint);
            else {
                RoutePoint lastPoint = routPointList.get(routPointList.size() - 1);

                if (routeLat == lastPoint.getRouteLat() && routeLng == lastPoint.getRouteLng()) {

                } else {

                    LatLng lastLatLng = new LatLng(lastPoint.getRouteLat(),
                            lastPoint.getRouteLng());
                    LatLng currentLatLng = new LatLng(routeLat, routeLng);
                    if (routeLat > 0 && routeLng > 0) {
                        double distantce = DistanceUtil.getDistance(lastLatLng, currentLatLng);
//                        大于2米算作有效加入列表
                        if (distantce > 2) {
                            //distance单位是米 转化为km/h
                            routePoint.speed = Double.parseDouble(String.format("%.1f", (distantce / 1000) * 30 * 60));
                            routePoint.time = System.currentTimeMillis();
                            routPointList.add(routePoint);
                            totalDistance += distantce;
                        }
                    }
                }
            }

            totalTime = (int) (System.currentTimeMillis() - beginTime) / 1000 / 60;
            totalPrice = (int) (Math.floor(totalTime / 60) * 1 + 1);


            if (totalDistance > 1000) {
                DecimalFormat df = new DecimalFormat("#.00");
                showDistance = df.format((float) totalDistance / 1000) + "千米";
            } else showDistance = totalDistance + "米";
            if (totalTime > 60) {
                showTime = totalTime / 60 + "时" + totalTime % 60 + "分";
            } else showTime = totalTime + "分钟";
            showPrice = totalPrice + "元";

            showRouteInfo(showTime, showDistance, showPrice, routPointList);


        }
    }

    private void showRouteInfo(String time, String distance, String price, ArrayList<RoutePoint> routPointList) {
        Log.d("gaolei", "totalTime---------post-----" + showTime);
        Log.d("gaolei", "totalDistance-----post---------" + showDistance);
        Log.d("gaolei", "totalPrice-------post-------" + showPrice);
        Log.d("gaolei", "routPointList.size()-------post-------" + routPointList.size());
        EventBus.getDefault().post(new RoutePoints(routPointList, time, distance, price));
        startNotifi(time, distance, price);
    }

    private void startNotifi(String time, String distance, String price) {
        startForeground(1, notification);
        contentView.setTextViewText(R.id.bike_time, time);
        contentView.setTextViewText(R.id.bike_distance, distance);
        contentView.setTextViewText(R.id.bike_price, price);
        rt_price = price;
    }

    public static class NetWorkReceiver extends BroadcastReceiver {
        public NetWorkReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            State wifiState;
            State mobileState = null;
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            wifiState = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
            mobileState = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
            if (wifiState != null && mobileState != null
                    && State.CONNECTED != wifiState
                    && State.CONNECTED == mobileState) {
//                Toast.makeText(context, context.getString(R.string.net_mobile), Toast.LENGTH_SHORT).show();
                // 手机网络连接成功
            } else if (wifiState != null && mobileState != null
                    && State.CONNECTED != wifiState
                    && State.CONNECTED != mobileState) {
//                Toast.makeText(context, context.getString(R.string.net_none), Toast.LENGTH_SHORT).show();

                // 手机没有任何的网络
            } else if (wifiState != null && State.CONNECTED == wifiState) {
                // 无线网络连接成功
//                Toast.makeText(context, context.getString(R.string.net_wifi), Toast.LENGTH_SHORT).show();

            }
        }
    }

    public void insertData(String routeListStr) {
        ContentValues values = new ContentValues();
        // 向该对象中插入键值对，其中键是列名，值是希望插入到这一列的值，值必须和数据当中的数据类型一致
        values.put("cycle_date", Utils.getDateFromMillisecond(beginTime));
        values.put("cycle_time", showTime);
        values.put("cycle_distance", showDistance);
        values.put("cycle_price", showPrice);
        values.put("cycle_points", routeListStr);
        // 创建DatabaseHelper对象
        RouteDBHelper dbHelper = new RouteDBHelper(this);
        // 得到一个可写的SQLiteDatabase对象
        SQLiteDatabase sqliteDatabase = dbHelper.getWritableDatabase();
        // 调用insert方法，就可以将数据插入到数据库当中
        // 第一个参数:表名称
        // 第二个参数：SQl不允许一个空列，如果ContentValues是空的，那么这一列被明确的指明为NULL值
        // 第三个参数：ContentValues对象
        sqliteDatabase.insert("cycle_route", null, values);
        sqliteDatabase.close();
    }
}
