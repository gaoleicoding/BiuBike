package com.biubike.map;

import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.text.TextUtils;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.baidu.trace.model.CoordType;
import com.baidu.trace.model.SortType;
import com.baidu.trace.model.TraceLocation;
import com.biubike.track.model.CurrentLocation;
import com.biubike.util.ContextUtil;
import com.biubike.util.Utils;

import java.util.List;

import static com.biubike.map.BitmapUtil.bmArrowPoint;
import static com.biubike.map.BitmapUtil.bmStart;

/**
 * Created by baidu on 17/2/9.
 */

public class MapUtil {

    private static MapUtil INSTANCE = new MapUtil();

    private MapStatus mapStatus = null;

    private Marker mMoveMarker = null;

    public MapView mapView = null;

    public BaiduMap baiduMap = null;

    public LatLng lastPoint = null;

    /**
     * 路线覆盖物
     */
    public Overlay polylineOverlay = null;

    private MapUtil() {
    }

    public static MapUtil getInstance() {
        return INSTANCE;
    }

    public void init(MapView view) {
        mapView = view;
        baiduMap = mapView.getMap();
    }

    public void onPause() {
        if (null != mapView) {
            mapView.onPause();
        }
    }

    public void onResume() {
        if (null != mapView) {
            mapView.onResume();
        }
    }

    public void clear() {
        lastPoint = null;
        if (null != mMoveMarker) {
            mMoveMarker.remove();
            mMoveMarker = null;
        }
        if (null != polylineOverlay) {
            polylineOverlay.remove();
            polylineOverlay = null;
        }
        if (null != baiduMap) {
            baiduMap.clear();
            baiduMap = null;
        }
        mapStatus = null;
        if (null != mapView) {
            mapView.onDestroy();
            mapView = null;
        }
    }

    /**
     * 将轨迹实时定位点转换为地图坐标
     *
     * @param location
     * @return
     */
    public static LatLng convertTraceLocation2Map(TraceLocation location) {
        if (null == location) {
            return null;
        }
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        if (Math.abs(latitude - 0.0) < 0.000001 && Math.abs(longitude - 0.0) < 0.000001) {
            return null;
        }
        LatLng currentLatLng = new LatLng(latitude, longitude);
        if (CoordType.wgs84 == location.getCoordType()) {
            LatLng sourceLatLng = currentLatLng;
            CoordinateConverter converter = new CoordinateConverter();
            converter.from(CoordinateConverter.CoordType.GPS);
            converter.coord(sourceLatLng);
            currentLatLng = converter.convert();
        }
        return currentLatLng;
    }

    /**
     * 将地图坐标转换轨迹坐标
     *
     * @param latLng
     * @return
     */
    public static com.baidu.trace.model.LatLng convertMap2Trace(LatLng latLng) {
        return new com.baidu.trace.model.LatLng(latLng.latitude, latLng.longitude);
    }

    /**
     * 将轨迹坐标对象转换为地图坐标对象
     *
     * @param traceLatLng
     * @return
     */
    public static LatLng convertTrace2Map(com.baidu.trace.model.LatLng traceLatLng) {
        return new LatLng(traceLatLng.latitude, traceLatLng.longitude);
    }

    /**
     * 设置地图中心：使用已有定位信息；
     */
    public void setCenter() {
        if (!TraceUtil.isZeroPoint(CurrentLocation.latitude, CurrentLocation.longitude)) {
            LatLng currentLatLng = new LatLng(CurrentLocation.latitude, CurrentLocation.longitude);
            updateStatus(currentLatLng, false);
            return;
        }
        String lastLocation = EagleEyeUtil.get().trackConf.getString(Constants.LAST_LOCATION, null);
        if (!TextUtils.isEmpty(lastLocation)) {
            String[] locationInfo = lastLocation.split(";");
            if (!TraceUtil.isZeroPoint(Double.parseDouble(locationInfo[1]),
                    Double.parseDouble(locationInfo[2]))) {
                LatLng currentLatLng = new LatLng(Double.parseDouble(locationInfo[1]),
                        Double.parseDouble(locationInfo[2]));
                updateStatus(currentLatLng, false);
                return;
            }
        }
    }

    public void updateStatus(LatLng currentPoint, boolean showMarker) {
        if (null == baiduMap || null == currentPoint) {
            return;
        }

        if (null != baiduMap.getProjection()) {
            Point screenPoint = baiduMap.getProjection().toScreenLocation(currentPoint);
            // 点在屏幕上的坐标超过限制范围，则重新聚焦底图
            if (screenPoint.y < 200 || screenPoint.y > Utils.getScreenHeight(ContextUtil.getAppContext()) - 500
                    || screenPoint.x < 200 || screenPoint.x > Utils.getScreenWidth(ContextUtil.getAppContext()) - 200
                    || null == mapStatus) {
                setMapZoomStatus(currentPoint, 15.0f);
            }
        } else if (null == mapStatus) {
            // 第一次定位时，聚焦底图
            setMapZoomStatus(currentPoint, 15.0f);
        }

        if (showMarker) {
            addMarker(currentPoint);
        }

    }

    public Marker addOverlay(LatLng currentPoint, BitmapDescriptor icon, Bundle bundle) {
        OverlayOptions overlayOptions = new MarkerOptions().position(currentPoint)
                .icon(icon).zIndex(9).draggable(true);
        Marker marker = (Marker) baiduMap.addOverlay(overlayOptions);
        if (null != bundle) {
            marker.setExtraInfo(bundle);
        }
        return marker;
    }

    /**
     * 添加地图覆盖物
     */
    public void addMarker(LatLng currentPoint) {
        if (null == mMoveMarker) {
            mMoveMarker = addOverlay(currentPoint, bmArrowPoint, null);
            return;
        }

        if (null != lastPoint) {
            moveLooper(currentPoint);
        } else {
            lastPoint = currentPoint;
            mMoveMarker.setPosition(currentPoint);
        }
    }

    /**
     * 移动逻辑
     */
    public void moveLooper(LatLng endPoint) {

        mMoveMarker.setPosition(lastPoint);
        mMoveMarker.setRotate((float) TraceUtil.getAngle(lastPoint, endPoint));

        double slope = TraceUtil.getSlope(lastPoint, endPoint);
        // 是不是正向的标示（向上设为正向）
        boolean isReverse = (lastPoint.latitude > endPoint.latitude);
        double intercept = TraceUtil.getInterception(slope, lastPoint);
        double xMoveDistance = isReverse ? TraceUtil.getXMoveDistance(slope) : -1 * TraceUtil.getXMoveDistance(slope);

        for (double latitude = lastPoint.latitude; latitude > endPoint.latitude == isReverse; latitude =
                latitude - xMoveDistance) {
            LatLng latLng;
            if (slope != Double.MAX_VALUE) {
                latLng = new LatLng(latitude, (latitude - intercept) / slope);
            } else {
                latLng = new LatLng(latitude, lastPoint.longitude);
            }
            mMoveMarker.setPosition(latLng);
        }
    }

    /**
     * 绘制历史轨迹
     */
    public void drawHistoryTrack(List<LatLng> points, SortType sortType) {

        baiduMap.clear();
        if (points == null || points.size() == 0) {
            if (null != polylineOverlay) {
                polylineOverlay.remove();
                polylineOverlay = null;
            }
            return;
        }

        if (points.size() == 1) {
            OverlayOptions startOptions = new MarkerOptions().position(points.get(0)).icon(bmStart)
                    .zIndex(9).draggable(true);
            baiduMap.addOverlay(startOptions);
            setMapZoomStatus(points.get(0), 26.0f);
            return;
        }

        LatLng startPoint;
        LatLng endPoint;
        if (sortType == SortType.asc) {
            startPoint = points.get(0);
            endPoint = points.get(points.size() - 1);
        } else {
            startPoint = points.get(points.size() - 1);
            endPoint = points.get(0);
        }

//        // 添加起点图标
//        OverlayOptions startOptions = new MarkerOptions()
//                .position(startPoint).icon(bmStart)
//                .zIndex(9).draggable(true);
//        // 添加终点图标
//        OverlayOptions endOptions = new MarkerOptions().position(endPoint)
//                .icon(bmEnd).zIndex(9).draggable(true);

        // 添加路线（轨迹）
        OverlayOptions polylineOptions = new PolylineOptions().width(10)
                .color(Color.BLUE).points(points);

//        baiduMap.addOverlay(startOptions);
//        baiduMap.addOverlay(endOptions);
        polylineOverlay = baiduMap.addOverlay(polylineOptions);

        OverlayOptions markerOptions =
                new MarkerOptions().flat(true).anchor(0.5f, 0.5f).icon(bmArrowPoint)
                        .position(points.get(points.size() - 1));
        mMoveMarker = (Marker) baiduMap.addOverlay(markerOptions);

//        animateMapStatus(points);
        }


    private void addOverLayout(LatLng startPosition, LatLng endPosition) {
        //先清除图层
        // mBaiduMap.clear();
        // 定义Maker坐标点
        // 构建MarkerOption，用于在地图上添加Marker
        MarkerOptions options = new MarkerOptions().position(startPosition)
                .icon(bmStart);
        // 在地图上添加Marker，并显示
        baiduMap.addOverlay(options);
//        MarkerOptions options2 = new MarkerOptions().position(endPosition)
//                .icon(endBmp);
//        // 在地图上添加Marker，并显示
//        baiduMap.addOverlay(options2);

    }


    public void setMapZoomStatus(LatLng point, float zoom) {
        MapStatus.Builder builder = new MapStatus.Builder();
        mapStatus = builder.target(point).zoom(zoom).build();
        baiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(mapStatus));
    }

    public void refresh() {
        LatLng mapCenter = baiduMap.getMapStatus().target;
        float mapZoom = baiduMap.getMapStatus().zoom - 1.0f;
        setMapZoomStatus(mapCenter, mapZoom);
    }

}

