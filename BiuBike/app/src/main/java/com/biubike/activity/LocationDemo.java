package com.biubike.activity;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.biubike.R;
import com.biubike.map.MyOrientationListener;


/**
 * 此demo用来展示如何结合定位SDK实现定位，并使用MyLocationOverlay绘制定位位置 同时展示如何使用自定义图标绘制并点击时弹出泡泡
 */
public class LocationDemo extends Activity {

    // 定位相关
    LocationClient mLocClient;
    public MyLocationListenner myListener = new MyLocationListenner();
    private LocationMode mCurrentMode;
    BitmapDescriptor mCurrentMarker;
    private static final int accuracyCircleFillColor = 0xAAFFFF88;
    private static final int accuracyCircleStrokeColor = 0xAA00FF00;
    MyOrientationListener myOrientationListener;
    MapView mMapView;
    BaiduMap mBaiduMap;
    private float mCurrentX;

    // UI相关
    OnCheckedChangeListener radioButtonListener;
    Button requestLocButton;
    boolean isFirstLoc = true; // 是否首次定位

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());

        setContentView(R.layout.activity_location);
        requestLocButton = (Button) findViewById(R.id.button1);
        mCurrentMode = LocationMode.FOLLOWING;
//
//        requestLocButton.setText("普通");
//        OnClickListener btnClickListener = new OnClickListener() {
//            public void onClick(View v) {
//                switch (mCurrentMode) {
//                    case NORMAL:
//                        requestLocButton.setText("跟随");
//                        mCurrentMode = LocationMode.FOLLOWING;
//                        mBaiduMap
//                                .setMyLocationConfigeration(new MyLocationConfiguration(
//                                        mCurrentMode, true, mCurrentMarker));
//                        break;
//                    case COMPASS:
//                        requestLocButton.setText("普通");
//                        mCurrentMode = LocationMode.NORMAL;
//                        mBaiduMap
//                                .setMyLocationConfigeration(new MyLocationConfiguration(
//                                        mCurrentMode, true, mCurrentMarker));
//                        break;
//                    case FOLLOWING:
//                        requestLocButton.setText("罗盘");
//                        mCurrentMode = LocationMode.COMPASS;
//                        mBaiduMap
//                                .setMyLocationConfigeration(new MyLocationConfiguration(
//                                        mCurrentMode, true, mCurrentMarker));
//                        break;
//                    default:
//                        break;
//                }
//            }
//        };
//        requestLocButton.setOnClickListener(btnClickListener);
//
//        RadioGroup group = (RadioGroup) this.findViewById(R.id.radioGroup);
//        radioButtonListener = new OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(RadioGroup group, int checkedId) {
//                if (checkedId == R.id.defaulticon) {
//                    // 传入null则，恢复默认图标
//                    mCurrentMarker = null;
//                    mBaiduMap
//                            .setMyLocationConfigeration(new MyLocationConfiguration(
//                                    mCurrentMode, true, null));
//                }
//                if (checkedId == R.id.customicon) {
//                    // 修改为自定义marker
//                    mCurrentMarker = BitmapDescriptorFactory
//                            .fromResource(R.drawable.icon_geo);
//                    mBaiduMap
//                            .setMyLocationConfigeration(new MyLocationConfiguration(
//                                    mCurrentMode, true, mCurrentMarker,
//                                                    accuracyCircleFillColor, accuracyCircleStrokeColor));
//                }
//            }
//        };
//        group.setOnCheckedChangeListener(radioButtonListener);

        initMap();

    }

    private void initMap(){
        // 地图初始化
        mMapView = (MapView) findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        // 定位初始化
        mLocClient = new LocationClient(this);
        mLocClient.registerLocationListener(myListener);
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); // 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(1000);
        mLocClient.setLocOption(option);
        mLocClient.start();
        mBaiduMap
                .setMyLocationConfigeration(new MyLocationConfiguration(
                        mCurrentMode, true, null));
        myOrientationListener = new MyOrientationListener(this);
        //通过接口回调来实现实时方向的改变
        myOrientationListener.setOnOrientationListener(new MyOrientationListener.OnOrientationListener() {
            @Override
            public void onOrientationChanged(float x) {
                mCurrentX = x;
            }
        });
        myOrientationListener.start();
    }

    /**
     * 定位SDK监听函数
     */
    public class MyLocationListenner implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            // map view 销毁后不在处理新接收的位置
            if (location == null || mMapView == null) {
                return;
            }
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    .direction(mCurrentX)//设定图标方向     // 此处设置开发者获取到的方向信息，顺时针0-360
                    .latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            mBaiduMap.setMyLocationData(locData);
            if (isFirstLoc) {
                isFirstLoc = false;
                LatLng ll = new LatLng(location.getLatitude(),
                        location.getLongitude());
                MapStatus.Builder builder = new MapStatus.Builder();
                builder.target(ll).zoom(18.0f);
                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
            }
        }

        public void onReceivePoi(BDLocation poiLocation) {
        }
    }

    @Override
    protected void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        mMapView.onResume();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        // 退出时销毁定位
        mLocClient.stop();
        // 关闭定位图层
        mBaiduMap.setMyLocationEnabled(false);
        mMapView.onDestroy();
        mMapView = null;
        super.onDestroy();
    }

}
