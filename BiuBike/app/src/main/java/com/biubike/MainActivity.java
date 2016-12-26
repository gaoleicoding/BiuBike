package com.biubike;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
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
import com.baidu.mapapi.map.InfoWindow.OnInfoWindowClickListener;
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

import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import overlayutil.OverlayManager;
import overlayutil.WalkingRouteOverlay;

import static com.biubike.BikeInfo.infos;

public class MainActivity extends Activity implements View.OnClickListener, OnGetRoutePlanResultListener {

    private MapView mMapView = null;
    private BaiduMap mBaiduMap;
    private LocationClient mlocationClient;
    private MylocationListener mlistener;
    private Context context;

    private double mLatitude,mLongitude,changeLatitude,changeLongitude;
    private float mCurrentX;

    private ImageView btn_locale,btn_refresh;
    private TextView current_addr, book_bt, cancel_book;
    private LinearLayout bike_layout, bike_distance_layout, bike_info_layout, confirm_cancel_layout;
    private TextView bike_distance, bike_time, bike_code, book_countdown, prompt;

    //自定义图标
    private BitmapDescriptor mIconLocation, dragLocationIcon, bikeIcon, nearestIcon;
    RoutePlanSearch mSearch = null;    // 搜索模块，也可去掉地图模块独立使用
    private MyOrientationListener myOrientationListener;
    //定位图层显示方式
    private MyLocationConfiguration.LocationMode locationMode;
    private BikeInfo bInfo;

    String cityName = "";
    PlanNode startNodeStr, endNodeStr;
    int nodeIndex = -1;
    WalkingRouteResult nowResultwalk = null;
    boolean useDefaultIcon = false,hasPlanRoute=false;
    RouteLine route = null;
    OverlayManager routeOverlay = null;
    LatLng currentLL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // requestWindowFeature(Window.FEATURE_NO_TITLE);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        this.context = this;
//        checkSystemWritePermission();
        initView();
        initLocation();
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
        bike_code = (TextView) findViewById(R.id.bike_code);
        book_countdown = (TextView) findViewById(R.id.book_countdown);
        prompt = (TextView) findViewById(R.id.prompt);
        cancel_book = (TextView) findViewById(R.id.cancel_book);

        mBaiduMap = mMapView.getMap();

        mBaiduMap.setOnMapStatusChangeListener(changeListener);
        btn_locale = (ImageView) findViewById(R.id.btn_locale);
        btn_refresh = (ImageView) findViewById(R.id.btn_refresh);
        book_bt = (TextView) findViewById(R.id.book_bt);
        book_bt.setOnClickListener(this);
        cancel_book.setOnClickListener(this);
        btn_locale.setOnClickListener(this);
        btn_refresh.setOnClickListener(this);
        dragLocationIcon = BitmapDescriptorFactory.fromResource(R.mipmap.drag_location);
        bikeIcon = BitmapDescriptorFactory.fromResource(R.mipmap.bike_icon);
//      nearestIcon = BitmapDescriptorFactory.fromResource("R.mipmap.location_tips");
    }

    private void initLocation() {
        locationMode = MyLocationConfiguration.LocationMode.NORMAL;

        //定位服务的客户端。宿主程序在客户端声明此类，并调用，目前只支持在主线程中启动
        mlocationClient = new LocationClient(this);
        mlistener = new MylocationListener();
        initMarkerClickEvent();
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
        int span = 1000;
        mOption.setScanSpan(span);
        //设置 LocationClientOption
        mlocationClient.setLocOption(mOption);

        //初始化图标,BitmapDescriptorFactory是bitmap 描述信息工厂类.
        mIconLocation = BitmapDescriptorFactory
                .fromResource(R.mipmap.location_marker);


        myOrientationListener = new MyOrientationListener(context);
        //通过接口回调来实现实时方向的改变
        myOrientationListener.setOnOrientationListener(new MyOrientationListener.OnOrientationListener() {
            @Override
            public void onOrientationChanged(float x) {
                mCurrentX = x;
            }
        });
        mSearch = RoutePlanSearch.newInstance();
        mSearch.setOnGetRoutePlanResultListener(this);

    }

    public void getMyLocation() {
        LatLng latLng = new LatLng(mLatitude, mLongitude);
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
                prompt.setVisibility(View.GONE);
                countDownTimer.cancel();
                bike_info_layout.setVisibility(View.GONE);
                confirm_cancel_layout.setVisibility(View.GONE);
                bike_distance_layout.setVisibility(View.VISIBLE);
                book_bt.setVisibility(View.VISIBLE);
                break;
            case R.id.btn_locale:
                getMyLocation();
                break;
            case R.id.btn_refresh:
                addOverLayout(changeLatitude, changeLongitude);
                drawPlanRoute(endNodeStr);
                break;
        }
    }

    @Override
    public void onGetWalkingRouteResult(WalkingRouteResult result) {
        {
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
//                mBtnPre.setVisibility(View.VISIBLE);
//                mBtnNext.setVisibility(View.VISIBLE);

                if (result.getRouteLines().size() > 1) {
                    nowResultwalk = result;

                    MyTransitDlg myTransitDlg = new MyTransitDlg(MainActivity.this,
                            result.getRouteLines(),
                            RouteLineAdapter.Type.WALKING_ROUTE);
                    myTransitDlg.setOnItemInDlgClickLinster(new OnItemInDlgClickListener() {
                        public void onItemClick(int position) {
                            route = nowResultwalk.getRouteLines().get(position);
                            WalkingRouteOverlay overlay = new MyWalkingRouteOverlay(mBaiduMap);
                            mBaiduMap.setOnMarkerClickListener(overlay);
                            routeOverlay = overlay;
                            overlay.setData(nowResultwalk.getRouteLines().get(position));
                            overlay.addToMap();
                            overlay.zoomToSpan();
                        }

                    });
                    myTransitDlg.show();

                } else if (result.getRouteLines().size() == 1) {
                    // 直接显示
                    route = result.getRouteLines().get(0);
                    WalkingRouteOverlay overlay = new MyWalkingRouteOverlay(mBaiduMap);
                    mBaiduMap.setOnMarkerClickListener(overlay);
                    routeOverlay = overlay;
                    overlay.setData(result.getRouteLines().get(0));
                    overlay.addToMap();
                    overlay.zoomToSpan();
//                    MapStatus.Builder builder = new MapStatus.Builder();
//                    builder.target(currentLL).zoom(24.0f);
//                    mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
                } else {
                    Log.d("route result", "结果数<0");
                    return;
                }

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


    //所有的定位信息都通过接口回调来实现
    public class MylocationListener implements BDLocationListener {
        //定位请求回调接口
        private boolean isFirstIn = true;

        //定位请求回调函数,这里面会得到定位信息
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            //BDLocation 回调的百度坐标类，内部封装了如经纬度、半径等属性信息
            //MyLocationData 定位数据,定位数据建造器
            /*
            * 可以通过BDLocation配置如下参数
            * 1.accuracy 定位精度
            * 2.latitude 百度纬度坐标
            * 3.longitude 百度经度坐标
            * 4.satellitesNum GPS定位时卫星数目 getSatelliteNumber() gps定位结果时，获取gps锁定用的卫星数
            * 5.speed GPS定位时速度 getSpeed()获取速度，仅gps定位结果时有速度信息，单位公里/小时，默认值0.0f
            * 6.direction GPS定位时方向角度
            * */
            mLatitude = bdLocation.getLatitude();
            mLongitude = bdLocation.getLongitude();
            cityName = bdLocation.getCity();

            MyLocationData data = new MyLocationData.Builder()
                    .direction(mCurrentX)//设定图标方向
                    .accuracy(bdLocation.getRadius())//getRadius 获取定位精度,默认值0.0f
                    .latitude(mLatitude)//百度纬度坐标
                    .longitude(mLongitude)//百度经度坐标
                    .build();
            //设置定位数据, 只有先允许定位图层后设置数据才会生效，参见 setMyLocationEnabled(boolean)
            mBaiduMap.setMyLocationData(data);
//
            //配置定位图层显示方式,三个参数的构造器
            /*
            * 1.定位图层显示模式
            * 2.是否允许显示方向信息
            * 3.用户自定义定位图标
            *
            * */
            MyLocationConfiguration configuration
                    = new MyLocationConfiguration(locationMode, true, mIconLocation);
            //设置定位图层配置信息，只有先允许定位图层后设置定位图层配置信息才会生效，参见 setMyLocationEnabled(boolean)
            mBaiduMap.setMyLocationConfigeration(configuration);
            //判断是否为第一次定位,是的话需要定位到用户当前位置
            if (isFirstIn) {
                currentLL = new LatLng(bdLocation.getLatitude(),
                        bdLocation.getLongitude());
                startNodeStr = PlanNode.withLocation(currentLL);
                MapStatus.Builder builder = new MapStatus.Builder();
                builder.target(currentLL).zoom(18.0f);
                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
//                //地理坐标基本数据结构
//                LatLng latLng = new LatLng(bdLocation.getLatitude(), bdLocation.getLongitude());
//                //描述地图状态将要发生的变化,通过当前经纬度来使地图显示到该位置
//                MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(latLng);
//                //改变地图状态
//                mBaiduMap.setMapStatus(msu);
                isFirstIn = false;
                current_addr.setText(bdLocation.getAddrStr());
                addOverLayout(mLatitude, mLongitude);
//
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
//            Toast.makeText(context, _latitude + "," + _longitude, Toast.LENGTH_SHORT).show();


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
                BikeInfo bikeInfo = (BikeInfo) marker.getExtraInfo().get("info");
                updateBikeInfo(bikeInfo);
                return true;
            }
        });
    }

    private void initNearestBike(final BikeInfo bikeInfo, LatLng ll) {
//        TextView nearestView = new TextView(getApplicationContext());
//        nearestView.setBackgroundResource(R.mipmap.location_tips);
//        nearestView.setPadding(30, 20, 30, 50);
//        nearestView.setText("距离最近");
        nearestIcon = BitmapDescriptorFactory.fromResource(R.mipmap.nearest_icon);
        if (mBaiduMap != null) {
            Point p = mBaiduMap.getProjection().toScreenLocation(ll);
            Log.d("gaolei", "point------------" + p.toString());
            p.y -= 100;
            LatLng llInfo = mBaiduMap.getProjection().fromScreenLocation(p);
            // 为弹出的InfoWindow添加点击事件
            InfoWindow mInfoWindow = new InfoWindow(nearestIcon, llInfo, 0, new OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick() {
                    updateBikeInfo(bikeInfo);
                }
            });
            // 显示InfoWindow
            mBaiduMap.showInfoWindow(mInfoWindow);
        }
    }

    private void updateBikeInfo(BikeInfo bikeInfo) {
        if(!hasPlanRoute) {
            bike_layout.setVisibility(View.VISIBLE);
            bike_time.setText(bikeInfo.getTime());
            bike_distance.setText(bikeInfo.getDistance());
            prompt.setVisibility(View.VISIBLE);
            bInfo = bikeInfo;
            endNodeStr = PlanNode.withLocation(new LatLng(bikeInfo.getLatitude(),bikeInfo.getLongitude()));
            drawPlanRoute(endNodeStr);
//            hasPlanRoute=true;
        }
    }
private void drawPlanRoute(PlanNode endNodeStr){

if(endNodeStr!=null)
    mSearch.walkingSearch((new WalkingRoutePlanOption())
            .from(startNodeStr).to(endNodeStr));
}
    private CountDownTimer countDownTimer = new CountDownTimer(10 * 60 * 1000, 1000) {

        @Override
        public void onTick(long millisUntilFinished) {
            book_countdown.setText(millisUntilFinished / 60000 + "分" + ((millisUntilFinished / 1000) % 60) + "秒");
        }

        @Override
        public void onFinish() {
            book_countdown.setText("预约结束");
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
        countDownTimer.cancel();
    }

    @Override
    protected void onStart() {
        super.onStart();
        //开启定位
        mBaiduMap.setMyLocationEnabled(true);
        if (!mlocationClient.isStarted()) {
            mlocationClient.start();
        }
        myOrientationListener.start();

    }

    protected void onRestart() {
        super.onRestart();
        countDownTimer.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //停止定位
        mBaiduMap.setMyLocationEnabled(false);
        mlocationClient.stop();
        myOrientationListener.stop();
        countDownTimer.cancel();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
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
            if (useDefaultIcon) {
//                return BitmapDescriptorFactory.fromResource(R.drawable.icon_st);
            }
            return null;
        }

        @Override
        public BitmapDescriptor getTerminalMarker() {
            if (useDefaultIcon) {
//                return BitmapDescriptorFactory.fromResource(R.drawable.icon_en);
            }
            return null;
        }
    }
}