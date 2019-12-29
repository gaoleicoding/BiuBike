package com.biubike;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.trace.api.entity.OnEntityListener;
import com.baidu.trace.api.track.DistanceResponse;
import com.baidu.trace.api.track.HistoryTrackResponse;
import com.baidu.trace.api.track.LatestPoint;
import com.baidu.trace.api.track.LatestPointRequest;
import com.baidu.trace.api.track.LatestPointResponse;
import com.baidu.trace.api.track.OnTrackListener;
import com.baidu.trace.api.track.TrackPoint;
import com.baidu.trace.model.OnTraceListener;
import com.baidu.trace.model.ProcessOption;
import com.baidu.trace.model.PushMessage;
import com.baidu.trace.model.SortType;
import com.baidu.trace.model.StatusCodes;
import com.baidu.trace.model.TraceLocation;
import com.baidu.trace.model.TransportMode;
import com.biubike.activity.CodeUnlockActivity;
import com.biubike.activity.MyRouteActivity;
import com.biubike.activity.WalletActivity;
import com.biubike.base.BaseActivity;
import com.biubike.bean.BikeInfo;
import com.biubike.callback.AllInterface;
import com.biubike.custom.LeftDrawerLayout;
import com.biubike.database.RouteDBHelper;
import com.biubike.fragment.LeftMenuFragment;
import com.biubike.map.BitmapUtil;
import com.biubike.map.EagleEyeUtil;
import com.biubike.map.LocationManager;
import com.biubike.map.MapUtil;
import com.biubike.map.TraceUtil;
import com.biubike.track.model.CurrentLocation;
import com.biubike.util.NetUtil;
import com.biubike.util.Utils;
import com.biubike.util.ViewUtil;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.biubike.bean.BikeInfo.infos;

public class MainActivity extends BaseActivity implements View.OnClickListener, AllInterface.OnMenuSlideListener {
    public static final int REQUEST_CODE_UNLOCK_SUCCESS = 1001;
    private ImageView btn_locale, btn_refresh, menu_icon;
    private TextView current_addr;
    private TextView title, book_bt, end_route;
    private LinearLayout llBikeLayout, llBikeDetail, llPrice;
    private TextView prompt, textview_time, textview_distance, textview_price, unlock;
    private TextView bike_distance, bike_time, bike_price;
    private long startTime = 0, endTime = 0, exitTime = 0;
    private View divider;
    //自定义图标
    private BitmapDescriptor dragLocationIcon, bikeIcon, startBmp;

    //    private LatLng currentLL;
    private LatLng currentLatLng, changeLatLng;
    private LeftDrawerLayout mLeftDrawerLayout;
    private View shadowView;
    // 定位相关
    private LocationClient mlocationClient;
    public MyLocationListenner myListener;
    private MapView mMapView;
    private BaiduMap mBaiduMap;

    private OnTrackListener trackDistanceListener = null;
    private OnTrackListener trackHistoryListener = null;
    /**
     * 轨迹监听器(用于接收纠偏后实时位置回调)
     */
    private OnTrackListener trackListener = null;


    /**
     * Entity监听器(用于接收实时定位回调)
     */
    private OnEntityListener entityListener = null;
    private OnTraceListener mTraceListener = null;

    /**
     * 实时定位任务
     */
    private RealTimeHandler realTimeHandler = new RealTimeHandler();
    private RealTimeLocRunnable realTimeLocRunnable = null;

    private boolean isRealTimeRunning = true;
    /**
     * 地图工具
     */
    private MapUtil mapUtil = null;

    /**
     * 轨迹点集合
     */
    private List<LatLng> trackPoints;
    // 查询周期(单位:秒)
    private int queryInterval = 2000;
    private String showTime;
    private String showDistance;
    private String showPrice;
    private boolean isGetLocation = false;
    private Notification notification;
    private RemoteViews contentView;
    private NotificationManager notificationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());//在Application的onCreate()不行，必须在activity的onCreate()中
        setContentView(R.layout.activity_main);
//        EventBus.getDefault().register(this);
        initView();
        initMap();
        initNotification();

        FragmentManager fm = getSupportFragmentManager();
        LeftMenuFragment mMenuFragment = (LeftMenuFragment) fm.findFragmentById(R.id.id_container_menu);
        mLeftDrawerLayout.setOnMenuSlideListener(this);

        if (mMenuFragment == null) {
            fm.beginTransaction().add(R.id.id_container_menu, mMenuFragment = new LeftMenuFragment()).commit();
        }
        trackPoints = new ArrayList<>();
        if (Utils.isServiceWork(this, "com.baidu.trace.LBSTraceService")) {
            beginService();
        }
    }

    private void initView() {
        current_addr = findViewById(R.id.current_addr);
        llBikeLayout = findViewById(R.id.ll_bike_layout);
        llBikeDetail = findViewById(R.id.ll_bike_detail);
        bike_time = findViewById(R.id.bike_time);
        bike_distance = findViewById(R.id.bike_distance);
        bike_price = findViewById(R.id.bike_price);
        textview_time = findViewById(R.id.textview_time);
        textview_distance = findViewById(R.id.textview_distance);
        textview_price = findViewById(R.id.textview_price);
        unlock = findViewById(R.id.unlock);
        divider = findViewById(R.id.divider);
        llPrice = findViewById(R.id.ll_bike_price);

        prompt = findViewById(R.id.prompt);
        mLeftDrawerLayout = findViewById(R.id.id_drawerlayout);
        shadowView = findViewById(R.id.shadow);
        menu_icon = findViewById(R.id.menu_icon);
        menu_icon.setOnClickListener(this);
        shadowView.setOnClickListener(this);
        btn_locale = findViewById(R.id.btn_locale);
        btn_refresh = findViewById(R.id.btn_refresh);
        end_route = findViewById(R.id.end_route);
        title = findViewById(R.id.title);
        book_bt = findViewById(R.id.book_bt);
        book_bt.setOnClickListener(this);
        btn_locale.setOnClickListener(this);
        btn_refresh.setOnClickListener(this);
        end_route.setOnClickListener(this);

        dragLocationIcon = BitmapDescriptorFactory.fromResource(R.mipmap.drag_location);
        bikeIcon = BitmapDescriptorFactory.fromResource(R.mipmap.bike_icon);
        startBmp = BitmapDescriptorFactory.fromResource(R.mipmap.route_start);
        initListener();
    }

    private void initMap() {

        // 地图初始化
        mMapView = findViewById(R.id.id_bmapView);
        mMapView.showZoomControls(true);
        //去掉百度Logo的小技巧
        View child = mMapView.getChildAt(1);
        if ((child instanceof ImageView)) {
            child.setVisibility(View.INVISIBLE);
        }
        mBaiduMap = mMapView.getMap();
        mMapView.setOnClickListener(this);
        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        // 定位初始化
        mBaiduMap.setOnMapStatusChangeListener(changeListener);
        mapUtil = MapUtil.getInstance();
        mapUtil.init(mMapView);
        BitmapUtil.init();
        mlocationClient = new LocationClient(this);
        myListener = new MyLocationListenner();
        mlocationClient.registerLocationListener(myListener);
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); // 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setIsNeedAddress(true);//如想获得具体位置就需要设置为true
        option.setIgnoreKillProcess(false);
        mlocationClient.setLocOption(option);
        if (!mlocationClient.isStarted()) {
            mlocationClient.start();
        }
        initMarkerClickEvent();
        if (!Utils.isServiceWork(this, "com.baidu.trace.LBSTraceService")) {
            getMyLocation();
        }
    }

    /**
     * 定位SDK监听函数，调用的时机：
     * 1、第一次进入此Activity
     * 2、调用mlocationClient.requestLocation()
     * 3、RouteService中设置了setScanSpan(2000)，所以行程中会每隔2s调用一次
     */
    public class MyLocationListenner extends BDAbstractLocationListener {

        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            // map view 销毁后不在处理新接收的位置
            if (bdLocation == null || mMapView == null) {
                return;
            }

            double currentLatitude = bdLocation.getLatitude();
            double currentLongitude = bdLocation.getLongitude();
            current_addr.setText(bdLocation.getAddrStr());
            currentLatLng = new LatLng(currentLatitude, currentLongitude);
            LocationManager.getInstance().setCurrentLL(currentLatLng);
            LocationManager.getInstance().setAddress(bdLocation.getAddrStr());

            Log.d("gaolei", "currentLL----------" + currentLatLng);
            Log.d("gaolei", "getAddrStr()----------" + bdLocation.getAddrStr());
            mapUtil.setMapZoomStatus(currentLatLng, 19);
            addOverLayout(currentLatLng);
            mlocationClient.stop();

        }
    }

    public void getMyLocation() {
        isGetLocation = true;
        getCurrentLocation(entityListener, trackListener);
//        if (!mlocationClient.isStarted()) {
//            mlocationClient.requestLocation();
//        }
    }

    public void openMenu() {
        mLeftDrawerLayout.openDrawer();
        shadowView.setVisibility(View.VISIBLE);
    }

    public void closeMenu() {
        mLeftDrawerLayout.closeDrawer();
        shadowView.setVisibility(View.GONE);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.book_bt:
                Intent intent = new Intent(this, CodeUnlockActivity.class);
                startActivityForResult(intent, REQUEST_CODE_UNLOCK_SUCCESS);
                cancelBook();
                break;

            case R.id.btn_locale:
                getMyLocation();

                break;
            case R.id.btn_refresh:
                addOverLayout(changeLatLng);
                break;
            case R.id.end_route:
                toastDialog();

                break;
            case R.id.menu_icon:
                openMenu();
                break;

            case R.id.shadow:
                closeMenu();
                break;
        }
    }

    static class RealTimeHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    }

    private void initListener() {

        trackListener = new OnTrackListener() {

            @Override
            public void onLatestPointCallback(LatestPointResponse response) {
                if (StatusCodes.SUCCESS != response.getStatus()) {
                    return;
                }

                LatestPoint point = response.getLatestPoint();
                if (null == point || TraceUtil.isZeroPoint(point.getLocation().getLatitude(), point.getLocation()
                        .getLongitude())) {
                    return;
                }

                currentLatLng = MapUtil.convertTrace2Map(point.getLocation());
                CurrentLocation.locTime = point.getLocTime();
                CurrentLocation.latitude = currentLatLng.latitude;
                CurrentLocation.longitude = currentLatLng.longitude;
//                trackPoints.add(currentLatLng);
//                mapUtil.drawHistoryTrack(trackPoints, SortType.asc);
                if (null != mapUtil) {
                    Toast.makeText(MainActivity.this, "trackListener:" + currentLatLng.toString(), Toast.LENGTH_LONG).show();
                    mapUtil.updateStatus(currentLatLng, true);
                }


                if (isGetLocation) {
                    showCurrentLocationInfo(currentLatLng);
                    isGetLocation = false;
                }

            }

        };
        trackDistanceListener = new OnTrackListener() {

            @Override
            public void onDistanceCallback(DistanceResponse response) {
                // 里程回调

                int totalDistance = (int) response.getDistance();

                long totalTime = (System.currentTimeMillis() - startTime) / 1000 / 60;

                if (totalDistance > 1000) {
                    DecimalFormat df = new DecimalFormat("#.##");
                    showDistance = df.format((float) totalDistance / 1000) + "千米";
                } else {
                    showDistance = totalDistance + "米";
                }
                if (totalTime > 60) {
                    showTime = totalTime / 60 + "时" + totalTime % 60 + "分";
                } else {
                    showTime = totalTime + "分钟";
                }

                int totalPrice = (int) (Math.floor(totalTime / 30) * 1 + 1);

                bike_distance.setText(showDistance);
                bike_time.setText(showTime);
                showPrice = totalPrice + "元";
                bike_price.setText(showPrice);
                Log.d("gaolei", "rideDistance：" + showDistance);
                Log.d("gaolei", "minute：" + showTime);
                Log.d("gaolei", "(System.currentTimeMillis() - startTime) / 1000 ：" + (System.currentTimeMillis() - startTime) / 1000);
                Log.d("gaolei", "price：" + showPrice);
                startNotifi(showTime, showDistance, showPrice);
            }
        };

        trackHistoryListener = new OnTrackListener() {
            @Override
            public void onHistoryTrackCallback(HistoryTrackResponse response) {
                // 历史轨迹回调
                List<TrackPoint> points = response.getTrackPoints();
                if (null != points) {
                    for (TrackPoint trackPoint : points) {
                        double lat = trackPoint.getLocation().getLatitude();
                        double lng = trackPoint.getLocation().getLongitude();
                        if (!TraceUtil.isZeroPoint(lat, lng)) {
                            trackPoints.add(new LatLng(lat, lng));
                        }
                    }
                }
                mapUtil.drawHistoryTrack(trackPoints, SortType.asc);
            }
        };


        entityListener = new OnEntityListener() {

            @Override
            public void onReceiveLocation(TraceLocation location) {

                if (StatusCodes.SUCCESS != location.getStatus() || TraceUtil.isZeroPoint(location.getLatitude(),
                        location.getLongitude())) {
                    return;
                }
                currentLatLng = mapUtil.convertTraceLocation2Map(location);
                if (null == currentLatLng) {
                    return;
                }
                CurrentLocation.locTime = TraceUtil.toTimeStamp(location.getTime());
                CurrentLocation.latitude = currentLatLng.latitude;
                CurrentLocation.longitude = currentLatLng.longitude;
                if (null != mapUtil) {
                    mapUtil.updateStatus(currentLatLng, true);
                    Toast.makeText(MainActivity.this, "entityListener:" + currentLatLng.toString(), Toast.LENGTH_LONG).show();
                }

                if (isGetLocation) {
                    showCurrentLocationInfo(currentLatLng);
                    isGetLocation = false;
                }
            }

        };

        mTraceListener = new OnTraceListener() {

            /**
             * 绑定服务回调接口
             * @param errorNo  状态码
             * @param message 消息
             *                <p>
             *                <pre>0：成功 </pre>
             *                <pre>1：失败</pre>
             */
            @Override
            public void onBindServiceCallback(int errorNo, String message) {
                ViewUtil.showToast(MainActivity.this,
                        String.format("onBindServiceCallback, errorNo:%d, message:%s ", errorNo, message));
                if (!EagleEyeUtil.get().isTraceStarted) {
                    startTrace();
                    startGather();
                }
            }

            /**
             * 开启服务回调接口
             * @param errorNo 状态码
             * @param message 消息
             *                <p>
             *                <pre>0：成功 </pre>
             *                <pre>10000：请求发送失败</pre>
             *                <pre>10001：服务开启失败</pre>
             *                <pre>10002：参数错误</pre>
             *                <pre>10003：网络连接失败</pre>
             *                <pre>10004：网络未开启</pre>
             *                <pre>10005：服务正在开启</pre>
             *                <pre>10006：服务已开启</pre>
             */
            @Override
            public void onStartTraceCallback(int errorNo, String message) {
                if (StatusCodes.SUCCESS == errorNo || StatusCodes.START_TRACE_NETWORK_CONNECT_FAILED <= errorNo) {
                    EagleEyeUtil.get().isTraceStarted = true;
                    SharedPreferences.Editor editor = EagleEyeUtil.get().trackConf.edit();
                    editor.putBoolean("is_trace_started", true);
                    editor.apply();
                    EagleEyeUtil.get().registerReceiver();
                }
                Toast.makeText(MainActivity.this,
                        String.format("onStartTraceCallback, errorNo:%d, message:%s ", errorNo, message), Toast.LENGTH_SHORT);
            }

            /**
             * 停止服务回调接口
             * @param errorNo 状态码
             * @param message 消息
             *                <p>
             *                <pre>0：成功</pre>
             *                <pre>11000：请求发送失败</pre>
             *                <pre>11001：服务停止失败</pre>
             *                <pre>11002：服务未开启</pre>
             *                <pre>11003：服务正在停止</pre>
             */
            @Override
            public void onStopTraceCallback(int errorNo, String message) {
                if (StatusCodes.SUCCESS == errorNo || StatusCodes.CACHE_TRACK_NOT_UPLOAD == errorNo) {
                    EagleEyeUtil.get().isTraceStarted = false;
                    EagleEyeUtil.get().isGatherStarted = false;
                    // 停止成功后，直接移除is_trace_started记录（便于区分用户没有停止服务，直接杀死进程的情况）
                    SharedPreferences.Editor editor = EagleEyeUtil.get().trackConf.edit();
                    editor.remove("is_trace_started");
                    editor.remove("is_gather_started");
                    editor.apply();
                    EagleEyeUtil.get().unregisterPowerReceiver();
                }
                ViewUtil.showToast(MainActivity.this,
                        String.format("onStopTraceCallback, errorNo:%d, message:%s ", errorNo, message));
            }

            /**
             * 开启采集回调接口
             * @param errorNo 状态码
             * @param message 消息
             *                <p>
             *                <pre>0：成功</pre>
             *                <pre>12000：请求发送失败</pre>
             *                <pre>12001：采集开启失败</pre>
             *                <pre>12002：服务未开启</pre>
             */
            @Override
            public void onStartGatherCallback(int errorNo, String message) {
                if (StatusCodes.SUCCESS == errorNo || StatusCodes.GATHER_STARTED == errorNo) {
                    EagleEyeUtil.get().isGatherStarted = true;
                    SharedPreferences.Editor editor = EagleEyeUtil.get().trackConf.edit();
                    editor.putBoolean("is_gather_started", true);
                    editor.apply();
                }
                ViewUtil.showToast(MainActivity.this,
                        String.format("onStartGatherCallback, errorNo:%d, message:%s ", errorNo, message));
            }

            /**
             * 停止采集回调接口
             * @param errorNo 状态码
             * @param message 消息
             *                <p>
             *                <pre>0：成功</pre>
             *                <pre>13000：请求发送失败</pre>
             *                <pre>13001：采集停止失败</pre>
             *                <pre>13002：服务未开启</pre>
             */
            @Override
            public void onStopGatherCallback(int errorNo, String message) {
                if (StatusCodes.SUCCESS == errorNo || StatusCodes.GATHER_STOPPED == errorNo) {
                    EagleEyeUtil.get().isGatherStarted = false;
                    SharedPreferences.Editor editor = EagleEyeUtil.get().trackConf.edit();
                    editor.remove("is_gather_started");
                    editor.apply();
                }
                ViewUtil.showToast(MainActivity.this,
                        String.format("onStopGatherCallback, errorNo:%d, message:%s ", errorNo, message));
            }

            /**
             * 推送消息回调接口
             *
             * @param messageType 状态码
             * @param pushMessage 消息
             *                  <p>
             *                  <pre>0x01：配置下发</pre>
             *                  <pre>0x02：语音消息</pre>
             *                  <pre>0x03：服务端围栏报警消息</pre>
             *                  <pre>0x04：本地围栏报警消息</pre>
             *                  <pre>0x05~0x40：系统预留</pre>
             *                  <pre>0x41~0xFF：开发者自定义</pre>
             */
            @Override
            public void onPushCallback(byte messageType, PushMessage pushMessage) {
//                if (messageType < 0x03 || messageType > 0x04) {
//                    ViewUtil.showToast(MainActivity.this, pushMessage.getMessage());
//                    return;
//                }
//                FenceAlarmPushInfo alarmPushInfo = pushMessage.getFenceAlarmPushInfo();
//                if (null == alarmPushInfo) {
//                    ViewUtil.showToast(MainActivity.this,
//                            String.format("onPushCallback, messageType:%d, messageContent:%s ", messageType,
//                                    pushMessage));
//                    return;
//                }
//                StringBuffer alarmInfo = new StringBuffer();
//                alarmInfo.append("您于")
//                        .append(TraceUtil.getHMS(alarmPushInfo.getCurrentPoint().getLocTime() * 1000))
//                        .append(alarmPushInfo.getMonitoredAction() == MonitoredAction.enter ? "进入" : "离开")
//                        .append(messageType == 0x03 ? "云端" : "本地")
//                        .append("围栏：").append(alarmPushInfo.getFenceName());
//
//                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
//                    Notification notification = new Notification.Builder(trackApp)
//                            .setContentTitle(getResources().getString(R.string.alarm_push_title))
//                            .setContentText(alarmInfo.toString())
//                            .setSmallIcon(R.mipmap.icon_app)
//                            .setWhen(System.currentTimeMillis()).build();
//                    notificationManager.notify(notifyId++, notification);
//                }
            }

            @Override
            public void onInitBOSCallback(int errorNo, String message) {
                ViewUtil.showToast(MainActivity.this,
                        String.format("onInitBOSCallback, errorNo:%d, message:%s ", errorNo, message));
            }
        };


    }

    private void showCurrentLocationInfo(LatLng latLng) {
        Geocoder geocoder = new Geocoder(MainActivity.this);
        try {
            List<Address> addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (addressList.size() > 0) {
                current_addr.setText(addressList.get(0).getAddressLine(0));
                Log.d("gaolei", "Address:" + addressList.get(0).toString());
            }
            addOverLayout(currentLatLng);
        } catch (Exception e) {
            Log.d("gaolei", "" + e.getMessage());
        }
    }

    /**
     * 实时定位任务
     *
     * @author baidu
     */
    class RealTimeLocRunnable implements Runnable {

        @Override
        public void run() {
            if (isRealTimeRunning) {
//                getCurrentLocation(entityListener, trackListener);
                getDistance();
                getTraceHistory();
                realTimeHandler.postDelayed(this, queryInterval);
            }
        }
    }

    public void startRealTimeLoc() {
        isRealTimeRunning = true;
        realTimeLocRunnable = new RealTimeLocRunnable();
        realTimeHandler.post(realTimeLocRunnable);
    }

    public void stopRealTimeLoc() {
        isRealTimeRunning = false;
        if (null != realTimeHandler && null != realTimeLocRunnable) {
            realTimeHandler.removeCallbacks(realTimeLocRunnable);
        }
        EagleEyeUtil.get().mTraceClient.stopRealTimeLoc();
    }

    /**
     * 获取当前位置
     */
    public void getCurrentLocation(OnEntityListener entityListener, OnTrackListener trackListener) {
        // 网络连接正常，开启服务及采集，则查询纠偏后实时位置；否则进行实时定位
        if (NetUtil.isNetworkAvailable(this)
                && EagleEyeUtil.get().trackConf.contains("is_trace_started")
                && EagleEyeUtil.get().trackConf.contains("is_gather_started")
                && EagleEyeUtil.get().trackConf.getBoolean("is_trace_started", false)
                && EagleEyeUtil.get().trackConf.getBoolean("is_gather_started", false)) {
            LatestPointRequest request = new LatestPointRequest(EagleEyeUtil.get().getTag(), EagleEyeUtil.get().serviceId, EagleEyeUtil.get().entityName);
            ProcessOption processOption = new ProcessOption();
            processOption.setNeedDenoise(true);
            processOption.setRadiusThreshold(50);//设置精度过滤，0为不需要；精度大于50米的位置点过滤掉
            processOption.setTransportMode(TransportMode.riding);
            processOption.setNeedDenoise(true);//去噪处理
            processOption.setNeedMapMatch(true);//绑路处理
            request.setProcessOption(processOption);
            EagleEyeUtil.get().mTraceClient.queryLatestPoint(request, trackListener);
        } else {
            EagleEyeUtil.get().mTraceClient.queryRealTimeLoc(EagleEyeUtil.get().locRequest, entityListener);
        }
    }

    public void getDistance() {

        EagleEyeUtil.get().getTraceDistance(trackDistanceListener, startTime / 1000, System.currentTimeMillis() / 1000);

    }

    public void getTraceHistory() {

        EagleEyeUtil.get().getTraceHistory(trackHistoryListener, startTime / 1000, System.currentTimeMillis() / 1000);

    }


    @Override
    public void onMenuSlide(float offset) {
        shadowView.setVisibility(offset == 0 ? View.INVISIBLE : View.VISIBLE);
        int alpha = (int) Math.round(offset * 255 * 0.4);
        shadowView.setBackgroundColor(Color.argb(alpha, 0, 0, 0));
    }


    private BaiduMap.OnMapStatusChangeListener changeListener = new BaiduMap.OnMapStatusChangeListener() {
        public void onMapStatusChangeStart(MapStatus mapStatus) {
        }

        @Override
        public void onMapStatusChangeStart(MapStatus mapStatus, int i) {

        }

        public void onMapStatusChangeFinish(MapStatus mapStatus) {
            String _str = mapStatus.toString();
            String _regex = "target lat: (.*)\ntarget lng";
            String _regex2 = "target lng: (.*)\ntarget screen x";
            double changeLatitude = Double.parseDouble(latlng(_regex, _str));
            double changeLongitude = Double.parseDouble(latlng(_regex2, _str));
            changeLatLng = new LatLng(changeLatitude, changeLongitude);

        }

        public void onMapStatusChange(MapStatus mapStatus) {
        }
    };

    private String latlng(String regexStr, String str) {
        Pattern pattern = Pattern.compile(regexStr);
        Matcher matcher = pattern.matcher(str);
        while (matcher.find()) {
            str = matcher.group(1);
        }
        return str;
    }

    public void addInfosOverlay(List<BikeInfo> infos) {
        LatLng latLng = null;
        OverlayOptions overlayOptions;
        Marker marker = null;
        for (BikeInfo info : infos) {
            // 位置
            latLng = new LatLng(info.getLatitude(), info.getLongitude());
            // 图标
            overlayOptions = new MarkerOptions().position(latLng)
                    .icon(bikeIcon).zIndex(5);
            marker = (Marker) (mBaiduMap.addOverlay(overlayOptions));
            Bundle bundle = new Bundle();
            bundle.putSerializable("info", info);
            marker.setExtraInfo(bundle);
        }
        // 将地图移到到最后一个经纬度位置
        MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(latLng);
        mBaiduMap.setMapStatus(u);
    }


    private void addOverLayout(LatLng point) {
        //先清除图层
        mBaiduMap.clear();
        infos.clear();
        double _latitude = point.latitude;
        double _longitude = point.longitude;

        MarkerOptions options = new MarkerOptions().position(point)
                .icon(dragLocationIcon);
        // 在地图上添加显示当前位置Marker
        mBaiduMap.addOverlay(options);

        infos.add(new BikeInfo(_latitude - new Random().nextInt(5) * 0.0005, _longitude - new Random().nextInt(5) * 0.0005, R.mipmap.bike_mobai, "001",
                "100米", "1分钟"));
        infos.add(new BikeInfo(_latitude - new Random().nextInt(5) * 0.0005, _longitude - new Random().nextInt(5) * 0.0005, R.mipmap.bike_youbai, "002",
                "200米", "2分钟"));
        infos.add(new BikeInfo(_latitude - new Random().nextInt(5) * 0.0005, _longitude - new Random().nextInt(5) * 0.0005, R.mipmap.bike_ofo, "003",
                "300米", "3分钟"));
        infos.add(new BikeInfo(_latitude - new Random().nextInt(5) * 0.0005, _longitude - new Random().nextInt(5) * 0.0005, R.mipmap.bike_xiaolan, "004",
                "400米", "4分钟"));
        BikeInfo bikeInfo = new BikeInfo(_latitude - 0.0005, _longitude - 0.0005, R.mipmap.bike_xiaolan, "005",
                "50米", "0.5分钟");
        infos.add(bikeInfo);
        addInfosOverlay(infos);
        initNearestBike(bikeInfo, new LatLng(_latitude - 0.0005, _longitude - 0.0005));
    }

    private void initMarkerClickEvent() {
        // 对Marker的点击
        mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(final Marker marker) {
                // 获得marker中的数据
                if (marker != null && marker.getExtraInfo() != null) {
                    BikeInfo bikeInfo = (BikeInfo) marker.getExtraInfo().get("info");
                    if (bikeInfo != null)
                        showBikeWalkingPlan(bikeInfo);
                }
                return true;
            }
        });
    }

    private void initNearestBike(final BikeInfo bikeInfo, LatLng ll) {
        ImageView nearestIcon = new ImageView(getApplicationContext());
        nearestIcon.setImageResource(R.mipmap.nearest_icon);
        InfoWindow.OnInfoWindowClickListener listener = null;
        listener = new InfoWindow.OnInfoWindowClickListener() {
            public void onInfoWindowClick() {
                showBikeWalkingPlan(bikeInfo);
                mBaiduMap.hideInfoWindow();
            }
        };
        InfoWindow mInfoWindow = new InfoWindow(BitmapDescriptorFactory.fromView(nearestIcon), ll, -108, listener);
        mBaiduMap.showInfoWindow(mInfoWindow);
    }

    private void showBikeWalkingPlan(BikeInfo bikeInfo) {
        mlocationClient.requestLocation();
        llBikeLayout.setVisibility(View.VISIBLE);
        bike_time.setText(bikeInfo.getTime());
        bike_distance.setText(bikeInfo.getDistance());

        llPrice.setVisibility(View.GONE);
    }


    @Override
    protected void onResume() {
        mMapView.onResume();
        super.onResume();

    }


    public void gotoCodeUnlock(View view) {
        Intent intent = new Intent(this, CodeUnlockActivity.class);
        startActivityForResult(intent, REQUEST_CODE_UNLOCK_SUCCESS);
    }

    public void gotoMyRoute(View view) {
        startActivity(new Intent(this, MyRouteActivity.class));
    }

    public void gotoWallet(View view) {
        startActivity(new Intent(this, WalletActivity.class));
    }

    public void gotoNavigation(View view) {
//        startActivity(new Intent(this, NavigationActivity.class));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (resultCode == RESULT_OK) {
            beginService();
            if (currentLatLng != null) {
                MarkerOptions options = new MarkerOptions().position(currentLatLng)
                        .icon(startBmp);
                // 在地图上添加Marker，并显示
                mBaiduMap.addOverlay(options);
                MapUtil.getInstance().setMapZoomStatus(currentLatLng, 22);
            }

            startTrace();
            startGather();
            startTime = System.currentTimeMillis();

        }
    }

    private void startTrace() {
        EagleEyeUtil.get().mTraceClient.startTrace(EagleEyeUtil.get().mTrace, mTraceListener);
        startRealTimeLoc();
    }

    private void stopTrace() {
        EagleEyeUtil.get().mTraceClient.stopTrace(EagleEyeUtil.get().mTrace, mTraceListener);
        stopRealTimeLoc();
    }

    private void startGather() {

        EagleEyeUtil.get().mTraceClient.startGather(mTraceListener);
    }

    private void stopGather() {
        EagleEyeUtil.get().mTraceClient.stopGather(mTraceListener);
    }

    private void backFromRouteDetail() {
        title.setText(getString(R.string.bybike));
        textview_time.setText(getString(R.string.foot));
        textview_distance.setText(getString(R.string.distance));
        textview_price.setText(getString(R.string.price));

        textview_time.setTextSize(16);
        textview_distance.setTextSize(16);
        textview_price.setTextSize(16);
        bike_time.setTextSize(16);
        bike_distance.setTextSize(16);
        bike_price.setTextSize(16);

        llBikeLayout.setVisibility(View.GONE);
        prompt.setVisibility(View.GONE);
        current_addr.setVisibility(View.VISIBLE);
        menu_icon.setVisibility(View.VISIBLE);
        book_bt.setVisibility(View.VISIBLE);
        unlock.setVisibility(View.VISIBLE);
        divider.setVisibility(View.VISIBLE);
        btn_refresh.setVisibility(View.VISIBLE);
        btn_locale.setVisibility(View.VISIBLE);
        end_route.setVisibility(View.GONE);


    }

    private void beginService() {
        if (!Utils.isGpsOPen(this)) {
            Utils.showDialog(this);
            return;
        }

        title.setText(getString(R.string.routing));
        textview_time.setText(getString(R.string.bike_time));
        textview_distance.setText(getString(R.string.bike_distance));
        textview_price.setText(getString(R.string.bike_price));
        prompt.setText(getString(R.string.routing_prompt));


        textview_time.setTextSize(20);
        textview_distance.setTextSize(20);
        textview_price.setTextSize(20);
        bike_time.setTextSize(20);
        bike_distance.setTextSize(20);
        bike_price.setTextSize(20);
        llPrice.setVisibility(View.VISIBLE);
        prompt.setVisibility(View.VISIBLE);
        llBikeLayout.setVisibility(View.VISIBLE);
        current_addr.setVisibility(View.GONE);
        menu_icon.setVisibility(View.GONE);
        unlock.setVisibility(View.GONE);
        divider.setVisibility(View.GONE);
        btn_refresh.setVisibility(View.GONE);

        llBikeDetail.setVisibility(View.VISIBLE);
        book_bt.setVisibility(View.GONE);

        btn_locale.setVisibility(View.GONE);
        end_route.setVisibility(View.VISIBLE);
        if (!Utils.isServiceWork(this, "com.baidu.trace.LBSTraceService")) {
            mBaiduMap.clear();
            bike_time.setText("0分钟");
            bike_distance.setText("0米");
            bike_price.setText("0元");

        }

    }

    private void cancelBook() {
        llBikeLayout.setVisibility(View.GONE);
        prompt.setVisibility(View.GONE);
        MapUtil.getInstance().setMapZoomStatus(currentLatLng, 19);

    }


    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 退出时销毁定位
        mlocationClient.stop();
        // 关闭定位图层
        mBaiduMap.setMyLocationEnabled(false);
        mMapView.onDestroy();
        mMapView = null;
        EventBus.getDefault().unregister(this);

    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (llBikeLayout.getVisibility() == View.VISIBLE) {
                if (!Utils.isServiceWork(this, "com.baidu.trace.LBSTraceService"))
                    cancelBook();
                return true;
            }
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    protected void toastDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("确认要结束进程吗？");
        builder.setTitle("提示");
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                CodeUnlockActivity.unlockSuccess = false;

                stopRealTimeLoc();
                stopTrace();
                stopGather();

                endTime = System.currentTimeMillis();

                backFromRouteDetail();

                getMyLocation();

                Gson gson = new Gson();
                String routeListStr = gson.toJson(trackPoints);
                insertCycleData(routeListStr);
                notificationManager.cancel(1);
                trackPoints.clear();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    public void insertCycleData(String routeListStr) {
        if (trackPoints.size() < 2) return;
        ContentValues values = new ContentValues();
        // 向该对象中插入键值对，其中键是列名，值是希望插入到这一列的值，值必须和数据当中的数据类型一致
        values.put("cycle_date", Utils.getDateFromMillisecond(startTime));
        values.put("cycle_start_time", startTime);
        values.put("cycle_end_time", endTime);
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

    private void startNotifi(String time, String distance, String price) {
//        startForeground(1, notification);
        contentView.setTextViewText(R.id.bike_time, time);
        contentView.setTextViewText(R.id.bike_distance, distance);
        contentView.setTextViewText(R.id.bike_price, price);
        notificationManager.notify(1, notification);
    }

    private void initNotification() {
        int icon = R.mipmap.bike_icon2;
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
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


        if (notificationManager.getNotificationChannel(channelId) != null) return;

        NotificationChannel notificationChannel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW);
        notificationChannel.enableVibration(false);
        notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        notificationChannel.setShowBadge(true);
        notificationChannel.setBypassDnd(true);

        notificationManager.createNotificationChannel(notificationChannel);

    }

}