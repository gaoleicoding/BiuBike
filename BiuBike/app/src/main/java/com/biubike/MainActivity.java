package com.biubike;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
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
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.trace.api.entity.OnEntityListener;
import com.baidu.trace.api.track.LatestPoint;
import com.baidu.trace.api.track.LatestPointRequest;
import com.baidu.trace.api.track.LatestPointResponse;
import com.baidu.trace.api.track.OnTrackListener;
import com.baidu.trace.model.OnTraceListener;
import com.baidu.trace.model.ProcessOption;
import com.baidu.trace.model.PushMessage;
import com.baidu.trace.model.StatusCodes;
import com.baidu.trace.model.TraceLocation;
import com.biubike.activity.CodeUnlockActivity;
import com.biubike.activity.MyRouteActivity;
import com.biubike.activity.WalletActivity;
import com.biubike.base.BaseActivity;
import com.biubike.bean.BikeInfo;
import com.biubike.bean.RoutePoint;
import com.biubike.bean.RoutePoints;
import com.biubike.callback.AllInterface;
import com.biubike.custom.LeftDrawerLayout;
import com.biubike.fragment.LeftMenuFragment;
import com.biubike.map.EagleEyeUtil;
import com.biubike.service.RouteService;
import com.biubike.map.LocationManager;
import com.biubike.track.model.CurrentLocation;
import com.biubike.util.BitmapUtil;
import com.biubike.util.CommonUtil;
import com.biubike.util.Constants;
import com.biubike.util.MapUtil;
import com.biubike.util.NetUtil;
import com.biubike.util.Utils;
import com.biubike.util.ViewUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.biubike.bean.BikeInfo.infos;

public class MainActivity extends BaseActivity implements View.OnClickListener, AllInterface.OnMenuSlideListener {
    public static final int REQUEST_CODE_UNLOCK_SUCCESS = 1001;
    private double currentLatitude, currentLongitude, changeLatitude, changeLongitude;
    private ImageView btn_locale, btn_refresh, menu_icon;
    private TextView current_addr;
    private TextView title, book_bt, end_route;
    private LinearLayout llBikeLayout, llBikeDetail, llPrice;
    private TextView prompt, textview_time, textview_distance, textview_price, unlock;
    private TextView bike_distance, bike_time, bike_price;
    private long exitTime = 0;
    private View divider;
    //自定义图标
    private BitmapDescriptor dragLocationIcon, bikeIcon, startBmp;
    //    private RoutePlanSearch mSearch = null;    // 搜索模块，也可去掉地图模块独立使用
//    private PlanNode startNodeStr;
//    private WalkingRouteResult nowResultwalk = null;
//    private RouteLine routeLine = null;
    private boolean isServiceLive = false;
    //    private OverlayManager routeOverlay = null;
    private LatLng currentLL;
    private LeftDrawerLayout mLeftDrawerLayout;
    private View shadowView;
    // 定位相关
    private LocationClient mlocationClient;
    public MyLocationListenner myListener;
    private MyLocationConfiguration.LocationMode mCurrentMode;
    //    private MyOrientationListener myOrientationListener;
    private MapView mMapView;
    private BaiduMap mBaiduMap;
    private boolean isFirstLoc = true; // 是否首次定位
    private List<LatLng> pointsList;
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
     * 打包周期
     */
    public int packInterval = Constants.DEFAULT_PACK_INTERVAL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());//在Application的onCreate()不行，必须在activity的onCreate()中
        setContentView(R.layout.activity_main);
        EventBus.getDefault().register(this);
        initMap();
        initView();
        isServiceLive = Utils.isServiceWork(this, "com.biubike.service.RouteService");
        if (isServiceLive)
            beginService();

        FragmentManager fm = getSupportFragmentManager();
        LeftMenuFragment mMenuFragment = (LeftMenuFragment) fm.findFragmentById(R.id.id_container_menu);
        mLeftDrawerLayout.setOnMenuSlideListener(this);

        if (mMenuFragment == null) {
            fm.beginTransaction().add(R.id.id_container_menu, mMenuFragment = new LeftMenuFragment()).commit();
        }
        pointsList = new ArrayList<>();
//        mLeftDrawerLayout.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
//            @Override
//            public WindowInsets onApplyWindowInsets(View view, WindowInsets windowInsets) {
//                return windowInsets.consumeSystemWindowInsets();
//            }
//        });

    }

    private void initMap() {

        // 地图初始化
        mMapView = findViewById(R.id.id_bmapView);
        mMapView.showZoomControls(true);
        //去掉百度Logo的小技巧
        View child = mMapView.getChildAt(1);
        if (child != null && (child instanceof ImageView)) {
            child.setVisibility(View.INVISIBLE);
        }
        mBaiduMap = mMapView.getMap();
        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        // 定位初始化
        mlocationClient = new LocationClient(this);
        myListener = new MyLocationListenner();
        mlocationClient.registerLocationListener(myListener);
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); // 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setIsNeedAddress(true);//如想获得具体位置就需要设置为true
        mlocationClient.setLocOption(option);
        if (!mlocationClient.isStarted()) {
            mlocationClient.start();
        }
        mCurrentMode = MyLocationConfiguration.LocationMode.FOLLOWING;
        mBaiduMap.setMyLocationConfigeration(new MyLocationConfiguration(
                mCurrentMode, true, null));

//        mSearch = RoutePlanSearch.newInstance();
//        mSearch.setOnGetRoutePlanResultListener(this);
        initMarkerClickEvent();
    }

    /**
     * 定位SDK监听函数，调用的时机：
     * 1、第一次进入此Activity
     * 2、调用mlocationClient.requestLocation()
     * 3、RouteService中设置了setScanSpan(2000)，所以行程中会每隔2s调用一次
     */
    public class MyLocationListenner implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            // map view 销毁后不在处理新接收的位置
            if (bdLocation == null || mMapView == null) {
                return;
            }

            currentLatitude = bdLocation.getLatitude();
            currentLongitude = bdLocation.getLongitude();
            current_addr.setText(bdLocation.getAddrStr());
            currentLL = new LatLng(bdLocation.getLatitude(),
                    bdLocation.getLongitude());
            LocationManager.getInstance().setCurrentLL(currentLL);
            LocationManager.getInstance().setAddress(bdLocation.getAddrStr());
//            startNodeStr = PlanNode.withLocation(currentLL);

            Log.d("gaolei", "currentLL----------" + currentLL);
            //可能会调用多次，而我们下面的逻辑只想第一次进入的时候调用一次，所以要判断一下isFirstLoc
            if (isFirstLoc) {
                isFirstLoc = false;
                MapStatus.Builder builder = new MapStatus.Builder();
                //地图缩放比设置为18
                builder.target(currentLL).zoom(18.0f);
                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
                if (!isServiceLive) {
                    addOverLayout(currentLatitude, currentLongitude);
                }
            }
        }
    }


    public void openMenu() {
        mLeftDrawerLayout.openDrawer();
        shadowView.setVisibility(View.VISIBLE);
    }

    public void closeMenu() {
        mLeftDrawerLayout.closeDrawer();
        shadowView.setVisibility(View.GONE);
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

//        mBaiduMap.setOnMapStatusChangeListener(changeListener);
        btn_locale = findViewById(R.id.btn_locale);
        btn_refresh = findViewById(R.id.btn_refresh);
        end_route = findViewById(R.id.end_route);
        title = findViewById(R.id.title);
        book_bt = findViewById(R.id.book_bt);
        book_bt.setOnClickListener(this);
        btn_locale.setOnClickListener(this);
        btn_refresh.setOnClickListener(this);
        end_route.setOnClickListener(this);
        mMapView.setOnClickListener(this);
        dragLocationIcon = BitmapDescriptorFactory.fromResource(R.mipmap.drag_location);
        bikeIcon = BitmapDescriptorFactory.fromResource(R.mipmap.bike_icon);
        startBmp = BitmapDescriptorFactory.fromResource(R.mipmap.route_start);
        initListener();
        mapUtil = MapUtil.getInstance();
        mapUtil.init(findViewById(R.id.id_bmapView));
        mapUtil.setCenter();
        BitmapUtil.init();

    }

    public void getMyLocation() {
        mlocationClient.requestLocation();

        LatLng latLng = new LatLng(currentLatitude, currentLongitude);
        MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(latLng);
        // 移动到某经纬度
        mBaiduMap.animateMapStatus(update);
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

                addOverLayout(currentLatitude, currentLongitude);
                break;
            case R.id.btn_refresh:
                addOverLayout(changeLatitude, changeLongitude);
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
                if (null == point || CommonUtil.isZeroPoint(point.getLocation().getLatitude(), point.getLocation()
                        .getLongitude())) {
                    return;
                }

                LatLng currentLatLng = mapUtil.convertTrace2Map(point.getLocation());
                if (null == currentLatLng) {
                    return;
                }
                CurrentLocation.locTime = point.getLocTime();
                CurrentLocation.latitude = currentLatLng.latitude;
                CurrentLocation.longitude = currentLatLng.longitude;

                if (null != mapUtil) {
                    mapUtil.updateStatus(currentLatLng, true);
                    Toast.makeText(MainActivity.this,"trackListener:"+currentLatLng.toString(),Toast.LENGTH_LONG).show();
                }
            }
        };

        entityListener = new OnEntityListener() {

            @Override
            public void onReceiveLocation(TraceLocation location) {

                if (StatusCodes.SUCCESS != location.getStatus() || CommonUtil.isZeroPoint(location.getLatitude(),
                        location.getLongitude())) {
                    return;
                }
                LatLng currentLatLng = mapUtil.convertTraceLocation2Map(location);
                if (null == currentLatLng) {
                    return;
                }
                CurrentLocation.locTime = CommonUtil.toTimeStamp(location.getTime());
                CurrentLocation.latitude = currentLatLng.latitude;
                CurrentLocation.longitude = currentLatLng.longitude;

                if (null != mapUtil) {
                    mapUtil.updateStatus(currentLatLng, true);
                    Toast.makeText(MainActivity.this,"entityListener:"+currentLatLng.toString(),Toast.LENGTH_LONG).show();
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
//                        .append(CommonUtil.getHMS(alarmPushInfo.getCurrentPoint().getLocTime() * 1000))
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


    /**
     * 实时定位任务
     *
     * @author baidu
     */
    class RealTimeLocRunnable implements Runnable {

        private int interval = 0;

        public RealTimeLocRunnable(int interval) {
            this.interval = interval;
        }

        @Override
        public void run() {
            if (isRealTimeRunning) {
                getCurrentLocation(entityListener, trackListener);
                realTimeHandler.postDelayed(this, interval * 1000);
            }
        }
    }

    public void startRealTimeLoc(int interval) {
        isRealTimeRunning = true;
        realTimeLocRunnable = new RealTimeLocRunnable(interval);
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
            processOption.setRadiusThreshold(100);
            request.setProcessOption(processOption);
            EagleEyeUtil.get().mTraceClient.queryLatestPoint(request, trackListener);
        } else {
            EagleEyeUtil.get().mTraceClient.queryRealTimeLoc(EagleEyeUtil.get().locRequest, entityListener);
        }
    }


//    @Override
//    public void onGetWalkingRouteResult(final WalkingRouteResult result) {
//        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
//            Toast.makeText(MainActivity.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
//        }
//        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
//            // 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
//            // result.getSuggestAddrInfo()
//            return;
//        }
//        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
//
//            if (result.getRouteLines().size() > 1) {
//                nowResultwalk = result;
//
//                MyTransitDlg myTransitDlg = new MyTransitDlg(MainActivity.this,
//                        result.getRouteLines(),
//                        RouteLineAdapter.Type.WALKING_ROUTE);
//                myTransitDlg.setOnItemInDlgClickLinster(new OnItemInDlgClickListener() {
//                    public void onItemClick(int position) {
//                        routeLine = nowResultwalk.getRouteLines().get(position);
//                        WalkingRouteOverlay overlay = new MyWalkingRouteOverlay(mBaiduMap);
//
//
//                        routeOverlay = overlay;
//                        //路线查询成功
//                        try {
//                            overlay.setData(nowResultwalk.getRouteLines().get(position));
//                            overlay.addToMap();
//                            overlay.zoomToSpan();
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                            Toast.makeText(MainActivity.this, "路径规划异常", Toast.LENGTH_SHORT).show();
//                        }
//                    }
//
//                });
//                myTransitDlg.show();
//
//            } else if (result.getRouteLines().size() == 1) {
//                // 直接显示
//                routeLine = result.getRouteLines().get(0);
//                int totalDistance = routeLine.getDistance();
//                int totalTime = routeLine.getDuration() / 60;
//                bike_distance.setText(Utils.distanceFormatter(totalDistance));
//                bike_time.setText(Utils.timeFormatter(totalTime));
//
//                WalkingRouteOverlay overlay = new MyWalkingRouteOverlay(mBaiduMap);
//                routeOverlay = overlay;
//                overlay.setData(result.getRouteLines().get(0));
//                overlay.addToMap();
//                overlay.zoomToSpan();
//            } else {
//                Log.d("route result", "结果数<0");
//                return;
//            }
//        }
//    }

//    public void onGetTransitRouteResult(TransitRouteResult transitRouteResult) {
//        System.out.print("");
//    }
//
//    public void onGetMassTransitRouteResult(MassTransitRouteResult massTransitRouteResult) {
//        System.out.print("");
//    }
//
//    public void onGetDrivingRouteResult(DrivingRouteResult drivingRouteResult) {
//        System.out.print("");
//    }
//
//    public void onGetIndoorRouteResult(IndoorRouteResult indoorRouteResult) {
//        System.out.print("");
//    }
//
//    public void onGetBikingRouteResult(BikingRouteResult bikingRouteResult) {
//        System.out.print("");
//    }

    @Override
    public void onMenuSlide(float offset) {
        shadowView.setVisibility(offset == 0 ? View.INVISIBLE : View.VISIBLE);
        int alpha = (int) Math.round(offset * 255 * 0.4);
        shadowView.setBackgroundColor(Color.argb(alpha, 0, 0, 0));
    }

//
//    private BaiduMap.OnMapStatusChangeListener changeListener = new BaiduMap.OnMapStatusChangeListener() {
//        public void onMapStatusChangeStart(MapStatus mapStatus) {
//        }
//
//        public void onMapStatusChangeFinish(MapStatus mapStatus) {
//            String _str = mapStatus.toString();
//            String _regex = "target lat: (.*)\ntarget lng";
//            String _regex2 = "target lng: (.*)\ntarget screen x";
//            changeLatitude = Double.parseDouble(latlng(_regex, _str));
//            changeLongitude = Double.parseDouble(latlng(_regex2, _str));
//
//        }
//
//        public void onMapStatusChange(MapStatus mapStatus) {
//        }
//    };

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


    private void addOverLayout(double _latitude, double _longitude) {
        //先清除图层
        mBaiduMap.clear();
        infos.clear();
//        mlocationClient.requestLocation();
//        if (routeOverlay != null)
//            routeOverlay.removeFromMap();

        LatLng point = new LatLng(_latitude, _longitude);
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
//        PlanNode endNodeStr = PlanNode.withLocation(new LatLng(bikeInfo.getLatitude(), bikeInfo.getLongitude()));
//        drawWalkingPlanRoute(endNodeStr);
        llPrice.setVisibility(View.GONE);
    }

//    private void drawWalkingPlanRoute(PlanNode endNodeStr) {
//        if (routeOverlay != null)
//            routeOverlay.removeFromMap();
//        if (endNodeStr != null) {
//
//            mSearch.walkingSearch((new WalkingRoutePlanOption())
//                    .from(startNodeStr).to(endNodeStr));
//
//        }
//    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    // 供路线选择的Dialog
//    class MyTransitDlg extends Dialog {
//
//        private List<? extends RouteLine> mtransitRouteLines;
//        private ListView transitRouteList;
//        private RouteLineAdapter mTransitAdapter;
//
//        OnItemInDlgClickListener onItemInDlgClickListener;
//
//        public MyTransitDlg(Context context, int theme) {
//            super(context, theme);
//        }
//
//        public MyTransitDlg(Context context, List<? extends RouteLine> transitRouteLines, RouteLineAdapter.Type
//                type) {
//            this(context, 0);
//            mtransitRouteLines = transitRouteLines;
//            mTransitAdapter = new RouteLineAdapter(context, mtransitRouteLines, type);
//            requestWindowFeature(Window.FEATURE_NO_TITLE);
//        }
//
//        @Override
//        protected void onCreate(Bundle savedInstanceState) {
//            super.onCreate(savedInstanceState);
//            setContentView(R.layout.activity_transit_dialog);
//
//            transitRouteList = findViewById(R.id.transitList);
//            transitRouteList.setAdapter(mTransitAdapter);
//
//            transitRouteList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//
//                @Override
//                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
////                    onItemInDlgClickListener.onItemClick( position);
////                    mBtnPre.setVisibility(View.VISIBLE);
////                    mBtnNext.setVisibility(View.VISIBLE);
////                    dismiss();
//
//                }
//            });
//        }
//
//        public void setOnItemInDlgClickLinster(OnItemInDlgClickListener itemListener) {
//            onItemInDlgClickListener = itemListener;
//        }
//    }

    // 响应DLg中的List item 点击
    interface OnItemInDlgClickListener {
        void onItemClick(int position);

    }

//    private class MyWalkingRouteOverlay extends WalkingRouteOverlay {
//
//        public MyWalkingRouteOverlay(BaiduMap baiduMap) {
//            super(baiduMap);
//        }
//
//        @Override
//        public BitmapDescriptor getStartMarker() {
//            return BitmapDescriptorFactory.fromResource(R.mipmap.transparent_icon);
//        }
//
//        @Override
//        public BitmapDescriptor getTerminalMarker() {
//            return BitmapDescriptorFactory.fromResource(R.mipmap.transparent_icon);
//        }
//    }

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
//            MarkerOptions options = new MarkerOptions().position(new LatLng(currentLatitude,currentLongitude))
//                    .icon(startBmp);
//            // 在地图上添加Marker，并显示
//            mBaiduMap.addOverlay(options);
            startRealTimeLoc(Constants.LOC_INTERVAL);
            startOrStopTrace();
            startOrStopGather();
        }
    }

    private void startOrStopTrace() {
        if (EagleEyeUtil.get().isTraceStarted) {
            EagleEyeUtil.get().mTraceClient.stopTrace(EagleEyeUtil.get().mTrace, mTraceListener);
            stopRealTimeLoc();
        } else {
            EagleEyeUtil.get().mTraceClient.startTrace(EagleEyeUtil.get().mTrace, mTraceListener);
            if (Constants.DEFAULT_PACK_INTERVAL != packInterval) {
                stopRealTimeLoc();
                startRealTimeLoc(packInterval);
            }
        }
    }

    private void startOrStopGather() {
        if (EagleEyeUtil.get().isGatherStarted) {
            EagleEyeUtil.get().mTraceClient.stopGather(mTraceListener);
        } else {
            EagleEyeUtil.get().mTraceClient.startGather(mTraceListener);
        }
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

        getMyLocation();

        addOverLayout(currentLatitude, currentLongitude);
    }

    private void beginService() {
        if (!Utils.isGpsOPen(this)) {
            Utils.showDialog(this);
            return;
        }

        mCurrentMode = MyLocationConfiguration.LocationMode.FOLLOWING;
        mBaiduMap.setMyLocationConfigeration(new MyLocationConfiguration(
                mCurrentMode, true, null));
        title.setText(getString(R.string.routing));
        textview_time.setText(getString(R.string.bike_time));
        textview_distance.setText(getString(R.string.bike_distance));
        textview_price.setText(getString(R.string.bike_price));
        prompt.setText(getString(R.string.routing_prompt));

        bike_time.setText("0分钟");
        bike_distance.setText("0米");
        bike_price.setText("0元");
        llPrice.setVisibility(View.VISIBLE);

        textview_time.setTextSize(20);
        textview_distance.setTextSize(20);
        textview_price.setTextSize(20);
        bike_time.setTextSize(20);
        bike_distance.setTextSize(20);
        bike_price.setTextSize(20);

        prompt.setVisibility(View.VISIBLE);
        llBikeLayout.setVisibility(View.VISIBLE);
        current_addr.setVisibility(View.GONE);
        menu_icon.setVisibility(View.GONE);
        unlock.setVisibility(View.GONE);
        divider.setVisibility(View.GONE);
        btn_refresh.setVisibility(View.GONE);

        llBikeDetail.setVisibility(View.VISIBLE);
        book_bt.setVisibility(View.GONE);
//        if (routeOverlay != null)
//            routeOverlay.removeFromMap();

        btn_locale.setVisibility(View.GONE);
        end_route.setVisibility(View.VISIBLE);
        mBaiduMap.clear();
//        mlocationClient.requestLocation();
//        Intent intent = new Intent(this, RouteService.class);
//        startService(intent);

    }

    private void cancelBook() {
        llBikeLayout.setVisibility(View.GONE);
        prompt.setVisibility(View.GONE);
//        if (routeOverlay != null)
//            routeOverlay.removeFromMap();
        MapStatus.Builder builder = new MapStatus.Builder();
        //地图缩放比设置为18
        builder.target(currentLL).zoom(18.0f);
        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
    }

    @Override
    protected void onResume() {
        mMapView.onResume();
        super.onResume();
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
                if (!Utils.isServiceWork(this, "com.biubike.service.RouteService"))
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void updateRoute(RoutePoints routePoints) {
        if (Utils.isTopActivity(this)) {

            String showTime = routePoints.getTime();
            String showDistance = routePoints.getDistance();
            String showPrice = routePoints.getPrice();
            ArrayList<RoutePoint> routPointList = routePoints.getRouteList();

            Log.d("gaolei", "totalTime---------get-----" + showTime);
            Log.d("gaolei", "totalDistance-----get---------" + showDistance);
            Log.d("gaolei", "totalPrice-------get-------" + showPrice);
            Log.d("gaolei", "routPointList.size()-------get-------" + routPointList.size());

            setPolyline(routPointList);
            bike_time.setText(showTime);
            bike_distance.setText(showDistance);
            bike_price.setText(showPrice);
            RoutePoint routePoint = routPointList.get(routPointList.size() - 1);
            LatLng latLng = new LatLng(routePoint.routeLat, routePoint.routeLng);
            MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(latLng);
            // 移动到某经纬度
            mBaiduMap.animateMapStatus(update);
        }
    }

    public void setPolyline(ArrayList<RoutePoint> routePoints) {
        pointsList.clear();
        for (int i = 0; i < routePoints.size(); i++) {
            RoutePoint point = routePoints.get(i);
            LatLng latLng = new LatLng(point.getRouteLat(), point.getRouteLng());
            pointsList.add(latLng);
        }
        if (pointsList.size() < 2) return;
        OverlayOptions ooPolyline = new PolylineOptions().width(10)
                .color(0xFF36D19D).points(pointsList);
        mBaiduMap.clear();
        mBaiduMap.addOverlay(ooPolyline);

        LatLng newLatLng = pointsList.get(pointsList.size() - 1);
        MarkerOptions options = new MarkerOptions().position(newLatLng)
                .icon(bikeIcon);
        // 在地图上添加Marker，并显示
        mBaiduMap.addOverlay(options);
    }

    protected void toastDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("确认要结束进程吗？");
        builder.setTitle("提示");
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Intent intent = new Intent(MainActivity.this, RouteService.class);
//                stopService(intent);
                CodeUnlockActivity.unlockSuccess = false;
                mCurrentMode = MyLocationConfiguration.LocationMode.NORMAL;
                mBaiduMap.setMyLocationConfigeration(new MyLocationConfiguration(
                        mCurrentMode, true, null));
                backFromRouteDetail();
                stopRealTimeLoc();
                startOrStopTrace();
                startOrStopGather();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

}