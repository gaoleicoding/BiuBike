package com.biubike.activity;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.UiSettings;
import com.baidu.mapapi.model.LatLng;
import com.biubike.R;
import com.biubike.base.BaseActivity;
import com.biubike.bean.RoutePoint;
import com.biubike.util.Utils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.iflytek.cloud.thirdparty.T;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by gaolei on 16/12/29.
 */

public class RouteDetailActivity extends BaseActivity {

    private MapView route_detail_mapview;
    BaiduMap routeBaiduMap;
    private BitmapDescriptor startBmp, endBmp, currentBmp;
    private MylocationListener mlistener;
    LocationClient mlocationClient;
    TextView total_time, total_distance, total_price, tv_route_replay, tv_title;
    public ArrayList<RoutePoint> routePoints;
    public static boolean completeRoute = false;
    String time, distance, price, routePointsStr;
    RelativeLayout replay_progress_layout, route_mapview_layout;
    List<LatLng> points, subList;
    int routePointsLength, currentIndex = 2;
    boolean isInReplay = false, pauseReplay = false;
    ImageView img_replay;
    SeekBar seekbar_progress;
    TextView tv_current_time, tv_current_speed;

    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            Log.d("gaolei", "currentIndex------------" + currentIndex);
            routeBaiduMap.clear();
            subList = points.subList(0, currentIndex);
            if (subList.size() >= 2) {
                OverlayOptions ooPolyline = new PolylineOptions().width(10)
                        .color(0xFF36D19D).points(subList);
                routeBaiduMap.addOverlay(ooPolyline);
            }
            if (subList.size() >= 1) {
                LatLng latLng = points.get(subList.size() - 1);
                MarkerOptions options = new MarkerOptions().position(latLng)
                        .icon(currentBmp);
                // 在地图上添加Marker，并显示
                routeBaiduMap.addOverlay(options);
                MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(latLng);
                // 移动到某经纬度
                routeBaiduMap.animateMapStatus(update);
            }
            if (currentIndex < routePointsLength) {
                tv_current_time.setText(Utils.getDateFromMillisecond(routePoints.get(currentIndex).time));
                tv_current_speed.setText(routePoints.get(currentIndex).speed + "km/h");
                int progress = (int) currentIndex * 100 / routePointsLength;
                seekbar_progress.setProgress(progress);
            }
            if (currentIndex < routePointsLength - 1) {
                currentIndex = currentIndex + 2;
                handler.sendEmptyMessageDelayed(1, 2000);
            } else {
                seekbar_progress.setProgress(100);
            }
        }
    };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_detail);
        setStatusBar();
        route_detail_mapview = (MapView) findViewById(R.id.route_detail_mapview);
        total_time = (TextView) findViewById(R.id.total_time);
        total_distance = (TextView) findViewById(R.id.total_distance);
        total_price = (TextView) findViewById(R.id.total_pricce);
        tv_route_replay = (TextView) findViewById(R.id.tv_route_replay);
        tv_title = (TextView) findViewById(R.id.tv_title);
        tv_current_time = (TextView) findViewById(R.id.tv_current_time);
        tv_current_speed = (TextView) findViewById(R.id.tv_current_speed);
        img_replay = (ImageView) findViewById(R.id.img_replay);
        seekbar_progress = (SeekBar) findViewById(R.id.seekbar_progress);
        replay_progress_layout = (RelativeLayout) findViewById(R.id.replay_progress_layout);
        route_mapview_layout = (RelativeLayout) findViewById(R.id.route_mapview_layout);
        routeBaiduMap = route_detail_mapview.getMap();
        route_detail_mapview.showZoomControls(false);
        startBmp = BitmapDescriptorFactory.fromResource(R.mipmap.route_start);
        endBmp = BitmapDescriptorFactory.fromResource(R.mipmap.route_end);
        currentBmp = BitmapDescriptorFactory.fromResource(R.mipmap.icon_geo);

        Intent intent = getIntent();
        String time = intent.getStringExtra("totalTime");
        String distance = intent.getStringExtra("totalDistance");
        String price = intent.getStringExtra("totalPrice");
        routePointsStr = intent.getStringExtra("routePoints");
        routePoints = new Gson().fromJson(routePointsStr, new TypeToken<List<RoutePoint>>() {
        }.getType());
        routePointsLength = routePoints.size();
        drawRoute();

        total_time.setText("骑行时长：" + time + "分钟");
        total_distance.setText("骑行距离：" + distance + "米");
        total_price.setText("余额支付：" + price + "元");
        seekbar_progress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            /**
             * 拖动条停止拖动的时候调用
             */
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                handler.sendEmptyMessage(1);
            }

            /**
             * 拖动条开始拖动的时候调用
             */
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            /**
             * 拖动条进度改变的时候调用
             */
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                currentIndex = (int) routePointsLength * progress / 100;

            }
        });

    }

    public void drawRoute() {
        points = new ArrayList<LatLng>();

        for (int i = 0; i < routePoints.size(); i++) {
            RoutePoint point = routePoints.get(i);
            LatLng latLng = new LatLng(point.getRouteLat(), point.getRouteLng());
            Log.d("gaolei", "point.getRouteLat()----show-----" + point.getRouteLat());
            Log.d("gaolei", "point.getRouteLng()----show-----" + point.getRouteLng());
            points.add(latLng);
        }
        if (points.size() > 2) {
            OverlayOptions ooPolyline = new PolylineOptions().width(10)
                    .color(0xFF36D19D).points(points);
            routeBaiduMap.addOverlay(ooPolyline);
            RoutePoint startPoint = routePoints.get(0);
            LatLng startPosition = new LatLng(startPoint.getRouteLat(), startPoint.getRouteLng());

            MapStatus.Builder builder = new MapStatus.Builder();
            builder.target(startPosition).zoom(18.0f);
            routeBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
            RoutePoint endPoint = routePoints.get(routePoints.size() - 1);
            LatLng endPosition = new LatLng(endPoint.getRouteLat(), endPoint.getRouteLng());
            addOverLayout(startPosition, endPosition);
        }

    }


    public class MylocationListener implements BDLocationListener {
        //定位请求回调接口
        private boolean isFirstIn = true;

        //定位请求回调函数,这里面会得到定位信息
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            //判断是否为第一次定位,是的话需要定位到用户当前位置
            if (isFirstIn) {
                Log.d("gaolei", "onReceiveLocation----------RouteDetail-----" + bdLocation.getAddrStr());

                isFirstIn = false;

            }
        }
    }

    private void addOverLayout(LatLng startPosition, LatLng endPosition) {
        //先清除图层
        // mBaiduMap.clear();
        // 定义Maker坐标点
        // 构建MarkerOption，用于在地图上添加Marker
        MarkerOptions options = new MarkerOptions().position(startPosition)
                .icon(startBmp);
        // 在地图上添加Marker，并显示
        routeBaiduMap.addOverlay(options);
        MarkerOptions options2 = new MarkerOptions().position(endPosition)
                .icon(endBmp);
        // 在地图上添加Marker，并显示
        routeBaiduMap.addOverlay(options2);

    }

    private void addOverLayout2(LatLng startPosition, LatLng endPosition) {
        //先清除图层
        // mBaiduMap.clear();
        // 定义Maker坐标点
        // 构建MarkerOption，用于在地图上添加Marker
        MarkerOptions options = new MarkerOptions().position(startPosition)
                .icon(startBmp);
        // 在地图上添加Marker，并显示
        routeBaiduMap.addOverlay(options);
        MarkerOptions options2 = new MarkerOptions().position(endPosition)
                .icon(endBmp);
        // 在地图上添加Marker，并显示
        routeBaiduMap.addOverlay(options2);

    }

    public void onDestroy() {
        super.onDestroy();

        completeRoute = false;
    }

    public void finishActivity(View view) {
        if (isInReplay) {
            backFromReplay();
            return;
        }
        completeRoute = true;
        finish();
    }

    public void pauseReplay(View view) {
        if (!pauseReplay) {
            img_replay.setImageResource(R.mipmap.replay_stop);
            pauseReplay = true;
            handler.removeCallbacksAndMessages(null);
        } else {
            img_replay.setImageResource(R.mipmap.replay_start);
            pauseReplay = false;
            handler.sendEmptyMessageDelayed(1, 1000);
        }
    }

    public void startReplay(View view) {
        isInReplay = true;
        tv_title.setText(R.string.route_replay);
        tv_route_replay.setVisibility(View.GONE);
        routeBaiduMap.clear();
        replay_progress_layout.setVisibility(View.VISIBLE);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) route_mapview_layout.getLayoutParams();
        params.height = Utils.getScreenHeight(this) - statusBarHeight - titleHeight;
        route_mapview_layout.setLayoutParams(params);

        handler.sendEmptyMessageDelayed(1, 1000);
    }

    public void backFromReplay() {
        isInReplay = false;
        tv_title.setText(R.string.route_detail);
        tv_route_replay.setVisibility(View.VISIBLE);
        routeBaiduMap.clear();
        subList.clear();
        currentIndex = 2;
        drawRoute();
        replay_progress_layout.setVisibility(View.GONE);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) route_mapview_layout.getLayoutParams();
        params.height = Utils.dp2px(this, 240);
        route_mapview_layout.setLayoutParams(params);
        handler.removeCallbacksAndMessages(null);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (isInReplay) {
                backFromReplay();
                return false;
            }
            completeRoute = true;
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
