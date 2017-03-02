package com.biubike;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
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
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.RouteLine;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.IndoorRouteResult;
import com.baidu.mapapi.search.route.MassTransitRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRoutePlanOption;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.biubike.activity.CodeUnlockActivity;
import com.biubike.activity.MyRouteActivity;
import com.biubike.activity.RouteDetailActivity;
import com.biubike.activity.WalletActivity;
import com.biubike.base.BaseActivity;
import com.biubike.bean.BikeInfo;
import com.biubike.callback.AllInterface;
import com.biubike.custom.LeftDrawerLayout;
import com.biubike.fragment.LeftMenuFragment;
import com.biubike.map.MyOrientationListener;
import com.biubike.map.RouteLineAdapter;
import com.biubike.service.RouteService;
import com.biubike.util.Utils;

import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import overlayutil.OverlayManager;
import overlayutil.WalkingRouteOverlay;

import static com.biubike.bean.BikeInfo.infos;

public class MainActivity extends BaseActivity implements View.OnClickListener, OnGetRoutePlanResultListener, AllInterface.OnMenuSlideListener, AllInterface.IUpdateLocation {

    private MapView mMapView = null;
    public BaiduMap mBaiduMap;
    private LocationClient mlocationClient;
    private MylocationListener mlistener;
    private double currentLatitude, currentLongitude, changeLatitude, changeLongitude;
    private float mCurrentX;
    private ImageView btn_locale, btn_refresh, menu_icon;
    public static TextView current_addr;
    private TextView title, book_bt, cancel_book, btn_question;
    private LinearLayout bike_layout, bike_distance_layout, bike_info_layout, confirm_cancel_layout;
    private TextView bike_code, bike_sound, book_countdown, prompt,
            textview_time, textview_distance, textview_price, unlock;
    public static TextView bike_distance, bike_time, bike_price;
    private long exitTime = 0;
    private View divider;
    private boolean isFirstIn = true;
    public boolean mainActivityFinished = false;

    //自定义图标
    private BitmapDescriptor mIconLocation, dragLocationIcon, bikeIcon, nearestIcon;
    RoutePlanSearch mSearch = null;    // 搜索模块，也可去掉地图模块独立使用
    private MyOrientationListener myOrientationListener;
    //定位图层显示方式
    private MyLocationConfiguration.LocationMode locationMode;
    private BikeInfo bInfo;

    PlanNode startNodeStr, endNodeStr;
    int nodeIndex = -1, distance;
    WalkingRouteResult nowResultwalk = null;
    boolean useDefaultIcon = true, hasPlanRoute = false,isServiceLive=false;
    RouteLine routeLine = null;
    OverlayManager routeOverlay = null;
    LatLng currentLL;
    LeftDrawerLayout mLeftDrawerLayout;
    LeftMenuFragment mMenuFragment;
    private RelativeLayout title_layout, menu_layout, person_layout;
    View shadowView;
    RouteService routeService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // requestWindowFeature(Window.FEATURE_NO_TITLE);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        Log.d("gaolei", "MainkActivity------------onCreate---------" + System.currentTimeMillis());
        routeService = new RouteService();
        routeService.setiUpdateLocation(this);
        setStatusBar();
        initView();
        initLocation();
        isServiceLive=Utils.isServiceWork(this, "com.biubike.service.RouteService");
        if (isServiceLive)
            beginService();

        FragmentManager fm = getSupportFragmentManager();
        mMenuFragment = (LeftMenuFragment) fm.findFragmentById(R.id.id_container_menu);
        mLeftDrawerLayout.setOnMenuSlideListener(this);

        if (mMenuFragment == null) {
            fm.beginTransaction().add(R.id.id_container_menu, mMenuFragment = new LeftMenuFragment()).commit();
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
        mMapView = (MapView) findViewById(R.id.id_bmapView);
        current_addr = (TextView) findViewById(R.id.current_addr);
        bike_layout = (LinearLayout) findViewById(R.id.bike_layout);
        bike_distance_layout = (LinearLayout) findViewById(R.id.bike_distance_layout);
        bike_info_layout = (LinearLayout) findViewById(R.id.bike_info_layout);
        confirm_cancel_layout = (LinearLayout) findViewById(R.id.confirm_cancel_layout);
        bike_time = (TextView) findViewById(R.id.bike_time);
        bike_distance = (TextView) findViewById(R.id.bike_distance);
        bike_price = (TextView) findViewById(R.id.bike_price);
        bike_price.setText(R.string.price);
        textview_time = (TextView) findViewById(R.id.textview_time);
        textview_distance = (TextView) findViewById(R.id.textview_distance);
        textview_price = (TextView) findViewById(R.id.textview_price);
        unlock = (TextView) findViewById(R.id.unlock);
        divider = (View) findViewById(R.id.divider);


        bike_code = (TextView) findViewById(R.id.bike_code);
        bike_sound = (TextView) findViewById(R.id.bike_sound);
        book_countdown = (TextView) findViewById(R.id.book_countdown);
        prompt = (TextView) findViewById(R.id.prompt);
        cancel_book = (TextView) findViewById(R.id.cancel_book);
        person_layout = (RelativeLayout) findViewById(R.id.person_layout);
        mLeftDrawerLayout = (LeftDrawerLayout) findViewById(R.id.id_drawerlayout);
        shadowView = (View) findViewById(R.id.shadow);
        menu_icon = (ImageView) findViewById(R.id.menu_icon);
        bike_sound.setOnClickListener(this);
        menu_icon.setOnClickListener(this);
        shadowView.setOnClickListener(this);
//        mLeftDrawerLayout.setListener(this);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Utils.dp2px(this, 50));
        layoutParams.setMargins(0, statusBarHeight, 0, 0);//4个参数按顺序分别是左上右下
//        title_layout.setLayoutParams(layoutParams);
        RelativeLayout.LayoutParams layoutParams2 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        Log.d("gaolei", "statusBarHeight---------------" + statusBarHeight);
        layoutParams2.setMargins(40, statusBarHeight + Utils.dp2px(MainActivity.this, 50), 0, 0);//4个参数按顺序分别是左上右下
//      person_layout.setLayoutParams(layoutParams2);

//        String price = "1元";
//        setSpannableStr(bike_price, price, 0, price.length() - 1);

        mBaiduMap = mMapView.getMap();

        mBaiduMap.setOnMapStatusChangeListener(changeListener);
        btn_locale = (ImageView) findViewById(R.id.btn_locale);
        btn_refresh = (ImageView) findViewById(R.id.btn_refresh);
        btn_question = (TextView) findViewById(R.id.btn_question);
        title = (TextView) findViewById(R.id.title);
        book_bt = (TextView) findViewById(R.id.book_bt);
        book_bt.setOnClickListener(this);
        cancel_book.setOnClickListener(this);
        btn_locale.setOnClickListener(this);
        btn_refresh.setOnClickListener(this);
        btn_question.setOnClickListener(this);
        mMapView.setOnClickListener(this);
        dragLocationIcon = BitmapDescriptorFactory.fromResource(R.mipmap.drag_location);
        bikeIcon = BitmapDescriptorFactory.fromResource(R.mipmap.bike_icon);
    }

    private void setSpannableStr(TextView textView, String str, int startIndex, int endIndex) {
        SpannableString spannableString = new SpannableString(str);
        ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.parseColor("#393939"));
        spannableString.setSpan(colorSpan, startIndex, endIndex, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        RelativeSizeSpan sizeSpan01 = new RelativeSizeSpan(1.3f);
        spannableString.setSpan(sizeSpan01, startIndex, endIndex, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

        textView.setText(spannableString);
    }

    private void initLocation() {
        locationMode = MyLocationConfiguration.LocationMode.NORMAL;

        //定位服务的客户端。宿主程序在客户端声明此类，并调用，目前只支持在主线程中启动
        mlocationClient = new LocationClient(this);
        mlistener = new MylocationListener();
        initMarkerClickEvent();
        //注册监听器
        mlocationClient.registerLocationListener(mlistener);
//        Intent intent=new Intent(this,RouteService.class);
//        startService(intent);
        //配置定位SDK各配置参数，比如定位模式、定位时间间隔、坐标系类型等
        LocationClientOption mOption = new LocationClientOption();
        //设置坐标类型
        mOption.setCoorType("bd09ll");
        //设置是否需要地址信息，默认为无地址
        mOption.setIsNeedAddress(true);
        //设置是否打开gps进行定位
        mOption.setOpenGps(true);
        //设置扫描间隔，单位是毫秒 当<1000(1s)时，定时定位无效
        int span = 10000;
        mOption.setScanSpan(span);
        //设置 LocationClientOption
        mlocationClient.setLocOption(mOption);

        //初始化图标,BitmapDescriptorFactory是bitmap 描述信息工厂类.
        mIconLocation = BitmapDescriptorFactory
                .fromResource(R.mipmap.location_marker);

        myOrientationListener = new MyOrientationListener(this);
        //通过接口回调来实现实时方向的改变
        myOrientationListener.setOnOrientationListener(new MyOrientationListener.OnOrientationListener() {
            @Override
            public void onOrientationChanged(float x) {
                mCurrentX = x;
            }
        });
        mSearch = RoutePlanSearch.newInstance();
        mSearch.setOnGetRoutePlanResultListener(this);
        //开启定位
        mBaiduMap.setMyLocationEnabled(true);
        if (!mlocationClient.isStarted()) {
            mlocationClient.start();
        }
        myOrientationListener.start();
    }

    public void getMyLocation() {
        LatLng latLng = new LatLng(currentLatitude, currentLongitude);
        MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(latLng);
        mBaiduMap.setMapStatus(msu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.id_map_common:
                mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
                break;
            case R.id.id_map_site:
                mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
                break;
            case R.id.id_map_traffic:
                if (mBaiduMap.isTrafficEnabled()) {
                    mBaiduMap.setTrafficEnabled(false);
                    item.setTitle("实时交通(off)");
                } else {
                    mBaiduMap.setTrafficEnabled(true);
                    item.setTitle("实时交通(on)");
                }
                break;
            case R.id.id_map_mlocation:
                getMyLocation();
                break;
            case R.id.id_map_model_common:
                //普通模式
                locationMode = MyLocationConfiguration.LocationMode.NORMAL;
                break;
            case R.id.id_map_model_following:
                //跟随模式
                locationMode = MyLocationConfiguration.LocationMode.FOLLOWING;
                break;
            case R.id.id_map_model_compass:
                //罗盘模式
                locationMode = MyLocationConfiguration.LocationMode.COMPASS;
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.book_bt:
                bike_info_layout.setVisibility(View.VISIBLE);
                confirm_cancel_layout.setVisibility(View.VISIBLE);
                bike_distance_layout.setVisibility(View.GONE);
                book_bt.setVisibility(View.GONE);
                bike_code.setText(bInfo.getName());
                countDownTimer.start();
                break;
            case R.id.cancel_book:
                bike_layout.setVisibility(View.GONE);
                bike_time.setText("");
                bike_distance.setText("");
                bike_price.setText("");
                prompt.setVisibility(View.GONE);
                countDownTimer.cancel();
                bike_info_layout.setVisibility(View.GONE);
                confirm_cancel_layout.setVisibility(View.GONE);
                bike_distance_layout.setVisibility(View.VISIBLE);
                book_bt.setVisibility(View.VISIBLE);
                if (routeOverlay != null)
                    routeOverlay.removeFromMap();
                break;
            case R.id.btn_locale:
                getMyLocation();
                if (routeOverlay != null)
                    routeOverlay.removeFromMap();
                Log.d("gaolei", "currentLatitude-----btn_locale--------" + currentLatitude);
                Log.d("gaolei", "currentLongitude-----btn_locale--------" + currentLongitude);
                startNodeStr = PlanNode.withLocation(currentLL);
                addOverLayout(currentLatitude, currentLongitude);
                break;
            case R.id.btn_refresh:
                if (routeOverlay != null)
                    routeOverlay.removeFromMap();
                Log.d("gaolei", "changeLatitude-----btn_refresh--------" + changeLatitude);
                Log.d("gaolei", "changeLongitude-----btn_refresh--------" + changeLongitude);
                addOverLayout(changeLatitude, changeLongitude);
//                drawPlanRoute(endNodeStr);
                break;
            case R.id.btn_question:
                toastDialog();
                break;
            case R.id.menu_icon:
                Log.d("gaolei", "menu_icon-----click--------openMenu()");
                openMenu();
                break;
            case R.id.bike_sound:
                beginService();
                break;
            case R.id.shadow:
                closeMenu();
                Log.d("gaolei", "shadow-----click--------closeMenu()");

                break;

        }
    }

    @Override
    public void onGetWalkingRouteResult(final WalkingRouteResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(MainActivity.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
        }
        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
            // 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
            // result.getSuggestAddrInfo()
            return;
        }
        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
            nodeIndex = -1;

            if (result.getRouteLines().size() > 1) {
                nowResultwalk = result;

                MyTransitDlg myTransitDlg = new MyTransitDlg(MainActivity.this,
                        result.getRouteLines(),
                        RouteLineAdapter.Type.WALKING_ROUTE);
                myTransitDlg.setOnItemInDlgClickLinster(new OnItemInDlgClickListener() {
                    public void onItemClick(int position) {
                        routeLine = nowResultwalk.getRouteLines().get(position);
                        WalkingRouteOverlay overlay = new MyWalkingRouteOverlay(mBaiduMap);


                        routeOverlay = overlay;
                        //路线查询成功
                        try {
                            overlay.setData(nowResultwalk.getRouteLines().get(position));
                            overlay.addToMap();
                            overlay.zoomToSpan();
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(MainActivity.this, "路径规划异常", Toast.LENGTH_SHORT).show();
                        }
                    }

                });
                myTransitDlg.show();

            } else if (result.getRouteLines().size() == 1) {
                // 直接显示
                routeLine = result.getRouteLines().get(0);
                int totalDistance = routeLine.getDistance();
                int totalTime = routeLine.getDuration() / 60;
                bike_distance.setText(Utils.distanceFormatter(totalDistance));
                bike_time.setText(Utils.timeFormatter(totalTime));
                String distanceStr = Utils.distanceFormatter(totalDistance);
                String timeStr = Utils.timeFormatter(totalTime);
//                setSpannableStr(bike_time, timeStr, 0, timeStr.length() - 2);
//                setSpannableStr(bike_distance, distanceStr, 0, distanceStr.length() - 1);

                Log.d("gaolei", "totalDistance------------------" + totalDistance);

                WalkingRouteOverlay overlay = new MyWalkingRouteOverlay(mBaiduMap);
//                    mBaiduMap.setOnMarkerClickListener(overlay);


                routeOverlay = overlay;
                overlay.setData(result.getRouteLines().get(0));
                overlay.addToMap();
                overlay.zoomToSpan();

            } else {
                Log.d("route result", "结果数<0");
                return;
            }
        }
    }

    public void onGetTransitRouteResult(TransitRouteResult transitRouteResult) {
        System.out.print("");
    }

    public void onGetMassTransitRouteResult(MassTransitRouteResult massTransitRouteResult) {
        System.out.print("");
    }

    public void onGetDrivingRouteResult(DrivingRouteResult drivingRouteResult) {
        System.out.print("");
    }

    public void onGetIndoorRouteResult(IndoorRouteResult indoorRouteResult) {
        System.out.print("");
    }

    public void onGetBikingRouteResult(BikingRouteResult bikingRouteResult) {
        System.out.print("");
    }

    @Override
    public void onMenuSlide(float offset) {
        shadowView.setVisibility(offset == 0 ? View.INVISIBLE : View.VISIBLE);
        int alpha = (int) Math.round(offset * 255 * 0.4);
        String hex = Integer.toHexString(alpha).toUpperCase();
        Log.d("gaolei", "color------------" + "#" + hex + "000000");
        shadowView.setBackgroundColor(Color.argb(alpha, 0, 0, 0));

    }


    //所有的定位信息都通过接口回调来实现
    public class MylocationListener implements BDLocationListener {
        //定位请求回调接口

        //定位请求回调函数,这里面会得到定位信息
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {

             MyLocationData data = new MyLocationData.Builder()
                     .direction(mCurrentX)//设定图标方向
                     .accuracy(bdLocation.getRadius())//getRadius 获取定位精度,默认值0.0f
                     .latitude(currentLatitude)//百度纬度坐标
                     .longitude(currentLongitude)//百度经度坐标
                     .build();
             //设置定位数据, 只有先允许定位图层后设置数据才会生效，参见 setMyLocationEnabled(boolean)
             mBaiduMap.setMyLocationData(data);

             MyLocationConfiguration configuration
                     = new MyLocationConfiguration(locationMode, true, mIconLocation);
             //设置定位图层配置信息，只有先允许定位图层后设置定位图层配置信息才会生效，参见 setMyLocationEnabled(boolean)
             mBaiduMap.setMyLocationConfigeration(configuration);

            //判断是否为第一次定位,是的话需要定位到用户当前位置
            if (isFirstIn) {
                currentLatitude = bdLocation.getLatitude();
                changeLatitude = bdLocation.getLatitude();
                currentLongitude = bdLocation.getLongitude();
                changeLongitude = bdLocation.getLongitude();

                currentLL = new LatLng(bdLocation.getLatitude(),
                        bdLocation.getLongitude());
                startNodeStr = PlanNode.withLocation(currentLL);
                MapStatus.Builder builder = new MapStatus.Builder();
                builder.target(currentLL).zoom(18.0f);
                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
                Log.d("gaolei", "currentLatitude-------------" + currentLatitude);
                Log.d("gaolei", "currentLongitude-------------" + currentLongitude);
                isFirstIn = false;
                current_addr.setText(bdLocation.getAddrStr());
            if(!isServiceLive) {
                addOverLayout(currentLatitude, currentLongitude);
            }

            }
        }
    }

    private BaiduMap.OnMapStatusChangeListener changeListener = new BaiduMap.OnMapStatusChangeListener() {
        public void onMapStatusChangeStart(MapStatus mapStatus) {
        }

        public void onMapStatusChangeFinish(MapStatus mapStatus) {
            String _str = mapStatus.toString();
            String _regex = "target lat: (.*)\ntarget lng";
            String _regex2 = "target lng: (.*)\ntarget screen x";
            changeLatitude = Double.parseDouble(latlng(_regex, _str));
            changeLongitude = Double.parseDouble(latlng(_regex2, _str));
            LatLng changeLL = new LatLng(changeLatitude, changeLongitude);
            startNodeStr = PlanNode.withLocation(changeLL);
            Log.d("gaolei", "changeLatitude-----change--------" + changeLatitude);
            Log.d("gaolei", "changeLongitude-----change--------" + changeLongitude);
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
        OverlayOptions overlayOptions = null;
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
        // 定义Maker坐标点
        LatLng point = new LatLng(_latitude, _longitude);
        // 构建MarkerOption，用于在地图上添加Marker
        MarkerOptions options = new MarkerOptions().position(point)
                .icon(dragLocationIcon);
        // 在地图上添加Marker，并显示
        mBaiduMap.addOverlay(options);
        infos.clear();
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
                        updateBikeInfo(bikeInfo);
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
                updateBikeInfo(bikeInfo);
                mBaiduMap.hideInfoWindow();
            }
        };
        InfoWindow mInfoWindow = new InfoWindow(BitmapDescriptorFactory.fromView(nearestIcon), ll, -108, listener);
        mBaiduMap.showInfoWindow(mInfoWindow);
    }

    private void updateBikeInfo(BikeInfo bikeInfo) {

        if (!hasPlanRoute) {
            bike_layout.setVisibility(View.VISIBLE);
            bike_time.setText(bikeInfo.getTime());
            bike_distance.setText(bikeInfo.getDistance());
            prompt.setVisibility(View.VISIBLE);
            bInfo = bikeInfo;
            endNodeStr = PlanNode.withLocation(new LatLng(bikeInfo.getLatitude(), bikeInfo.getLongitude()));
            drawPlanRoute(endNodeStr);
        }
    }

    private void drawPlanRoute(PlanNode endNodeStr) {
        if (routeOverlay != null)
            routeOverlay.removeFromMap();
        if (endNodeStr != null) {

            Log.d("gaolei", "changeLatitude-----startNode--------" + startNodeStr.getLocation().latitude);
            Log.d("gaolei", "changeLongitude-----startNode--------" + startNodeStr.getLocation().longitude);
            mSearch.walkingSearch((new WalkingRoutePlanOption())
                    .from(startNodeStr).to(endNodeStr));

        }
    }

    private CountDownTimer countDownTimer = new CountDownTimer(10 * 60 * 1000, 1000) {

        @Override
        public void onTick(long millisUntilFinished) {
            book_countdown.setText(millisUntilFinished / 60000 + "分" + ((millisUntilFinished / 1000) % 60) + "秒");
        }

        @Override
        public void onFinish() {
            book_countdown.setText("预约结束");
            Toast.makeText(MainActivity.this, getString(R.string.cancel_book_toast), Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    // 供路线选择的Dialog
    class MyTransitDlg extends Dialog {

        private List<? extends RouteLine> mtransitRouteLines;
        private ListView transitRouteList;
        private RouteLineAdapter mTransitAdapter;

        OnItemInDlgClickListener onItemInDlgClickListener;

        public MyTransitDlg(Context context, int theme) {
            super(context, theme);
        }

        public MyTransitDlg(Context context, List<? extends RouteLine> transitRouteLines, RouteLineAdapter.Type
                type) {
            this(context, 0);
            mtransitRouteLines = transitRouteLines;
            mTransitAdapter = new RouteLineAdapter(context, mtransitRouteLines, type);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_transit_dialog);

            transitRouteList = (ListView) findViewById(R.id.transitList);
            transitRouteList.setAdapter(mTransitAdapter);

            transitRouteList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                    onItemInDlgClickListener.onItemClick( position);
//                    mBtnPre.setVisibility(View.VISIBLE);
//                    mBtnNext.setVisibility(View.VISIBLE);
//                    dismiss();

                }
            });
        }

        public void setOnItemInDlgClickLinster(OnItemInDlgClickListener itemListener) {
            onItemInDlgClickListener = itemListener;
        }
    }

    // 响应DLg中的List item 点击
    interface OnItemInDlgClickListener {
        public void onItemClick(int position);
    }

    private class MyWalkingRouteOverlay extends WalkingRouteOverlay {

        public MyWalkingRouteOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        @Override
        public BitmapDescriptor getStartMarker() {
//            if (useDefaultIcon) {
            return BitmapDescriptorFactory.fromResource(R.mipmap.transparent_icon);
//            }
//            return null;
        }

        @Override
        public BitmapDescriptor getTerminalMarker() {
//            if (useDefaultIcon) {
            return BitmapDescriptorFactory.fromResource(R.mipmap.transparent_icon);
//            }
//            return null;
        }
    }

    public void gotoCodeUnlock(View view) {
        startActivity(new Intent(this, CodeUnlockActivity.class));
    }

    public void gotoMyRoute(View view) {
        startActivity(new Intent(this, MyRouteActivity.class));
    }

    public void gotoWallet(View view) {
        startActivity(new Intent(this, WalletActivity.class));
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("gaolei", "MainActivity------------onStart------------------");
    }

    protected void onRestart() {
        super.onRestart();
        mBaiduMap.setMyLocationEnabled(true);
        mlocationClient.start();
        myOrientationListener.start();
        Log.d("gaolei", "MainActivity------------onRestart------------------");
        if (CodeUnlockActivity.unlockSuccess) {
            beginService();
        }
        if (RouteDetailActivity.completeRoute) {
            isFirstIn = true;
            title.setText(getString(R.string.bybike));
            textview_time.setText(getString(R.string.foot));
            textview_distance.setText(getString(R.string.distance));
            textview_price.setText(getString(R.string.price));
            prompt.setText(getString(R.string.unlock_auto));

            textview_time.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            textview_distance.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            textview_price.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            bike_time.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            bike_distance.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            bike_price.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);

            prompt.setVisibility(View.GONE);
            bike_layout.setVisibility(View.GONE);
            current_addr.setVisibility(View.VISIBLE);
            menu_icon.setVisibility(View.VISIBLE);
            book_bt.setVisibility(View.VISIBLE);
            unlock.setVisibility(View.VISIBLE);
            divider.setVisibility(View.VISIBLE);
            btn_refresh.setVisibility(View.VISIBLE);
            btn_locale.setVisibility(View.VISIBLE);
            btn_question.setVisibility(View.GONE);
            mMapView.showZoomControls(true);
            if (!mlocationClient.isStarted()) {
                mlocationClient.start();
            }
            mlocationClient.requestLocation();
        }

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

        textview_time.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        textview_distance.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        textview_price.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        bike_time.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        bike_distance.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        bike_price.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);

        prompt.setVisibility(View.VISIBLE);
        bike_layout.setVisibility(View.VISIBLE);
        current_addr.setVisibility(View.GONE);
        menu_icon.setVisibility(View.GONE);
        unlock.setVisibility(View.GONE);
        divider.setVisibility(View.GONE);
        btn_refresh.setVisibility(View.GONE);

        countDownTimer.cancel();
        bike_info_layout.setVisibility(View.GONE);
        confirm_cancel_layout.setVisibility(View.GONE);
        bike_distance_layout.setVisibility(View.VISIBLE);
        book_bt.setVisibility(View.GONE);
        if (routeOverlay != null)
            routeOverlay.removeFromMap();

        btn_locale.setVisibility(View.GONE);
        bike_info_layout.setVisibility(View.GONE);
        btn_question.setVisibility(View.VISIBLE);
        mMapView.showZoomControls(false);
        mBaiduMap.clear();
        if(isServiceLive)
            mlocationClient.requestLocation();
        Intent intent = new Intent(this, RouteService.class);
        startService(intent);

        MyLocationConfiguration configuration
                = new MyLocationConfiguration(locationMode, true, mIconLocation);
        //设置定位图层配置信息，只有先允许定位图层后设置定位图层配置信息才会生
    }

    @Override
    protected void onStop() {
        super.onStop();
        //停止定位
        mBaiduMap.setMyLocationEnabled(false);
        mlocationClient.stop();
        myOrientationListener.stop();

        Log.d("gaolei", "MainActivity------------onStop------------------");

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
        countDownTimer.cancel();
        isFirstIn = true;
        Log.d("gaolei", "MainActivity------------onDestroy------------------");

    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
//                finish();
//                System.exit(0);
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addCategory(Intent.CATEGORY_HOME);
                startActivity(intent);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void updateLocation(String totalTime, String totalDistance) {
        Log.d("gaolei", "nowTime------updateLocation--------" + totalTime);
        Log.d("gaolei", "totalDistance---------updateLocation-----" + totalDistance);
        bike_time.setText(totalTime);
        bike_distance.setText(totalDistance);
    }

    @Override
    public void endLocation() {

    }

    public static class LocationReceiver extends BroadcastReceiver {
        public LocationReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("gaolei", "onReceive-------location-------");
            if (Utils.isTopActivity(context)) {
                bike_time.setText(RouteService.totalTime + "分钟");
                bike_distance.setText(RouteService.totalDistance + "米");
                bike_price.setText(RouteService.totalPrice + "元");
                Log.d("gaolei", "MainActivity-------TopActivity---------true");
            } else {
                Log.d("gaolei", "MainActivity-------TopActivity---------false");

            }
        }
    }

    protected void toastDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("确认要结束进程吗？");
        builder.setTitle("提示");
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Intent intent = new Intent(MainActivity.this, RouteService.class);
                stopService(intent);
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