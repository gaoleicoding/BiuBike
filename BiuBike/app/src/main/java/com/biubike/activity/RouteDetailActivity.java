package com.biubike.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.trace.api.track.HistoryTrackResponse;
import com.baidu.trace.api.track.OnTrackListener;
import com.baidu.trace.api.track.TrackPoint;
import com.baidu.trace.model.SortType;
import com.biubike.R;
import com.biubike.base.BaseActivity;
import com.biubike.map.EagleEyeUtil;
import com.biubike.map.MapUtil;
import com.biubike.map.TraceUtil;
import com.biubike.util.Utils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by gaolei on 16/12/29.
 */

public class RouteDetailActivity extends BaseActivity {

    private String TAG = "RouteDetailActivity";
    private BaiduMap baiduMap;
    private BitmapDescriptor startBmp, endBmp, currentBmp;
    private TextView total_time, total_distance, total_price, tv_route_replay, tv_title;
    private RelativeLayout replay_progress_layout, route_mapview_layout;
    private int routePointsLength, currentIndex = 0, spanIndex = 0;
    private boolean isInReplay = false, pauseReplay = false;
    private ImageView img_replay;
    private SeekBar seekbar_progress;
    private TextView tv_current_time, tv_current_speed;
    private final int UPDATE_PROGRESS = 1;
    private List<TrackPoint> routePoints;
    private List<LatLng> points;
    private List<LatLng> subList;

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_PROGRESS:
                    currentIndex = currentIndex + spanIndex;
                    Log.d(TAG, "currentIndex：" + currentIndex);
                    Log.d(TAG, "spanIndex：" + spanIndex);
                    Log.d(TAG, "points.size()：" + routePoints.size());
                    if (currentIndex < routePointsLength) {
                        subList =  new ArrayList(points.subList(0, currentIndex));
                    }
                    if (subList.size() >= 1) {
                        playTraceHistory(subList);
                    }
                    if (currentIndex < routePointsLength) {
                        tv_current_time.setText(routePoints.get(currentIndex).getCreateTime());
                        DecimalFormat df = new DecimalFormat("#.##");
                        String showSpeed = df.format(routePoints.get(currentIndex).getSpeed()) + "千米";
//                        String showSpeed =routePoints.get(currentIndex).getSpeed() + "千米";
                        tv_current_speed.setText(showSpeed);
                        int progress = currentIndex * 100 / routePointsLength;
                        seekbar_progress.setProgress(progress);

                        handler.sendEmptyMessageDelayed(UPDATE_PROGRESS, 500);
                    } else {
                        playTraceHistory(points);
                        seekbar_progress.setProgress(100);
                        handler.removeCallbacksAndMessages(null);
                        Toast.makeText(RouteDetailActivity.this, "轨迹回放结束", Toast.LENGTH_LONG).show();
                    }
            }
        }
    };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_detail);
        points = new ArrayList<>();
        initMap();
        total_time = findViewById(R.id.total_time);
        total_distance = findViewById(R.id.total_distance);
        total_price = findViewById(R.id.total_pricce);
        tv_route_replay = findViewById(R.id.tv_route_replay);
        tv_title = findViewById(R.id.tv_title);
        tv_current_time = findViewById(R.id.tv_current_time);
        tv_current_speed = findViewById(R.id.tv_current_speed);
        img_replay = findViewById(R.id.img_replay);
        seekbar_progress = findViewById(R.id.seekbar_progress);
        replay_progress_layout = findViewById(R.id.replay_progress_layout);
        route_mapview_layout = findViewById(R.id.route_mapview_layout);
        route_mapview_layout.requestDisallowInterceptTouchEvent(true);

        startBmp = BitmapDescriptorFactory.fromResource(R.mipmap.route_start);
        endBmp = BitmapDescriptorFactory.fromResource(R.mipmap.route_end);
        currentBmp = BitmapDescriptorFactory.fromResource(R.mipmap.bike_icon);

        Intent intent = getIntent();
        long startTime = intent.getLongExtra("startTime", 0) / 1000;
        long endTime = intent.getLongExtra("endTime", 0) / 1000;

        getTraceHistory(startTime, endTime);

    }


    public void getTraceHistory(long startTime, long endTime) {

        OnTrackListener trackHistoryListener = new OnTrackListener() {
            @Override
            public void onHistoryTrackCallback(HistoryTrackResponse response) {
                routePoints = response.getTrackPoints();
                routePointsLength = routePoints.size();

                if (null != routePoints) {
                    for (TrackPoint trackPoint : routePoints) {
                        double lat = trackPoint.getLocation().getLatitude();
                        double lng = trackPoint.getLocation().getLongitude();
                        if (!TraceUtil.isZeroPoint(lat, lng)) {
                            points.add(new LatLng(lat, lng));
                        }
                    }
                }
/*
        每2s采集一个点，下面是实际行驶时间和轨迹回放时间的对应策略
*/
                if (0 <= routePointsLength && routePointsLength < 20) {
                    spanIndex = 1;
                }
                //实际2～100s-->播放1～25s
                if (20 <= routePointsLength && routePointsLength < 50) {
                    spanIndex = 2;
                }
                //实际100~200s-->播放12~25s
                if (50 <= routePointsLength && routePointsLength < 100) {
                    spanIndex = 4;
                }
                //实际200~1000s-->播放16~83s
                if (100 <= routePointsLength && routePointsLength < 500) {
                    spanIndex = 6;
                }
                //实际1000~4000s-->播放62~250s
                if (500 <= routePointsLength && routePointsLength < 2000) {
                    spanIndex = 8;
                }
                //实际4000~20000s-->播放166~833s
                if (2000 <= routePointsLength && routePointsLength < 10000) {
                    spanIndex = 12;
                }
                //实际10000~20000s-->播放156s
                if (10000 <= routePointsLength) {
                    spanIndex = 64;
                }
                drawRoute(points);

                Intent intent = getIntent();
                String time = intent.getStringExtra("totalTime");
                String distance = intent.getStringExtra("totalDistance");
                String price = intent.getStringExtra("totalPrice");
                total_time.setText("骑行时长：" + time);
                total_distance.setText("骑行距离：" + distance);
                total_price.setText("余额支付：" + price);
                seekbar_progress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    /**
                     * 拖动条停止拖动的时候调用
                     */
                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        handler.sendEmptyMessageDelayed(UPDATE_PROGRESS, 500);
                    }

                    /**
                     * 拖动条开始拖动的时候调用
                     */
                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        handler.removeCallbacksAndMessages(null);
                    }

                    /**
                     * 拖动条进度改变的时候调用
                     */
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress,
                                                  boolean fromUser) {
                        currentIndex = routePointsLength * progress / 100;

                    }
                });

            }
        };
        EagleEyeUtil.get().getTraceHistory(trackHistoryListener, startTime, endTime);

    }

    private void initMap() {
        MapView mapView = findViewById(R.id.route_detail_mapview);
        mapView.requestDisallowInterceptTouchEvent(true);
        mapView.showZoomControls(true);
        baiduMap = mapView.getMap();

    }

    public void drawRoute(List<LatLng> points) {

        if (points.size() > 2) {
            OverlayOptions ooPolyline = new PolylineOptions().width(10)
                    .color(0xFF36D19D).points(points);
            baiduMap.addOverlay(ooPolyline);
            LatLng startPosition = points.get(0);

            MapStatus.Builder builder = new MapStatus.Builder();
            builder.target(startPosition).zoom(18.0f);
            baiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
            LatLng endPosition = points.get(routePoints.size() - 1);
            addOverLayout(startPosition, endPosition);
        }

    }


    private void playTraceHistory(List<LatLng> list) {
        if (list.size() < 2) return;
        OverlayOptions ooPolyline = new PolylineOptions().width(10)
                .color(0xFF36D19D).points(list);
        baiduMap.clear();
        baiduMap.addOverlay(ooPolyline);
        LatLng newLatLng = points.get(list.size() - 1);
        MarkerOptions options = new MarkerOptions().position(newLatLng)
                .icon(currentBmp);
        // 在地图上添加Marker，并显示
        baiduMap.addOverlay(options);
        MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(newLatLng);
        // 移动到某经纬度
        baiduMap.animateMapStatus(update);
    }

    private void addOverLayout(LatLng startPosition, LatLng endPosition) {
        //先清除图层
        // mBaiduMap.clear();
        // 定义Maker坐标点
        // 构建MarkerOption，用于在地图上添加Marker
        MarkerOptions options = new MarkerOptions().position(startPosition)
                .icon(startBmp);
        // 在地图上添加Marker，并显示
        baiduMap.addOverlay(options);
        MarkerOptions options2 = new MarkerOptions().position(endPosition)
                .icon(endBmp);
        // 在地图上添加Marker，并显示
        baiduMap.addOverlay(options2);

    }

    public void onDestroy() {
        super.onDestroy();
    }

    public void finishActivity(View view) {
        if (isInReplay) {
            backFromReplay();
            return;
        }
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
            handler.sendEmptyMessageDelayed(UPDATE_PROGRESS, 1000);
        }
    }

    public void startReplay(View view) {
        isInReplay = true;
        tv_title.setText(R.string.route_replay);
        tv_route_replay.setVisibility(View.GONE);
        baiduMap.clear();
        replay_progress_layout.setVisibility(View.VISIBLE);
        ViewGroup.LayoutParams params = route_mapview_layout.getLayoutParams();
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        route_mapview_layout.setLayoutParams(params);

        handler.sendEmptyMessageDelayed(UPDATE_PROGRESS, 500);
    }


    public void backFromReplay() {
        isInReplay = false;
        tv_title.setText(R.string.route_detail);
        tv_route_replay.setVisibility(View.VISIBLE);
        baiduMap.clear();
        subList.clear();
        currentIndex = 1;
        drawRoute(points);
        replay_progress_layout.setVisibility(View.GONE);
        ViewGroup.LayoutParams params = route_mapview_layout.getLayoutParams();
        params.height = Utils.dp2px(this, 240);
        route_mapview_layout.setLayoutParams(params);
        handler.removeCallbacksAndMessages(null);
        seekbar_progress.setProgress(0);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (isInReplay) {
                backFromReplay();
                return false;
            }
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
