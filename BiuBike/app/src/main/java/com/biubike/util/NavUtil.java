package com.biubike.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.navi.BaiduMapAppNotSupportNaviException;
import com.baidu.mapapi.navi.BaiduMapNavigation;
import com.baidu.mapapi.navi.NaviParaOption;
import com.baidu.mapapi.utils.DistanceUtil;
import com.baidu.mapapi.utils.OpenClientUtil;
import com.baidu.mapapi.utils.route.BaiduMapRoutePlan;
import com.baidu.mapapi.utils.route.RouteParaOption;
import com.baidu.navisdk.adapter.BNRoutePlanNode;
import com.baidu.navisdk.adapter.BNaviSettingManager;
import com.baidu.navisdk.adapter.BaiduNaviManager;
import com.biubike.R;
import com.biubike.activity.BDInnerNaviActivity;
import com.biubike.custom.NaviSelectDialog;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static com.biubike.application.DemoApplication.APP_FOLDER_NAME;
import static com.biubike.application.DemoApplication.mSDCardPath;

/**
 * Created by GaoLei on 17/3/31.
 * 这个工具类实现了调用内置导航和打开第三方App导航
 * 1.assets中的文件必须拷贝到项目
 * 2.想使用内置导航，必须初始化导航， NavUtil.initNavi(this);
 */
public class NavUtil {
    public static final int BaiduNavi = 1, GaodeNavi = 2, InnerNavi = 0;
    public static List<Activity> activityList = new LinkedList<Activity>();
    public static final String ROUTE_PLAN_NODE = "routePlanNode";
    static String authinfo = null;
    /**
     * 弹出导航选择dialog
     */
    public static void showChoiceNaviWayDialog(final Activity activity, final LatLng startLL, final LatLng endLL, final String start_place, final String destination) {


        final NaviSelectDialog rcd = new NaviSelectDialog(activity);
        rcd.setCanceledOnTouchOutside(false);
        rcd.setCancelable(false);
        final ArrayList<String> mapApps = new ArrayList<String>();
        mapApps.add(activity.getString(R.string.inner_navi));
        if (Utils.hasApp(activity, Utils.APP_BAIDU_MAP)) {
            mapApps.add(activity.getString(R.string.baidu_navi));
        }
        if (Utils.hasApp(activity, Utils.APP_AMAP)) {
            mapApps.add(activity.getString(R.string.gaode_navi));
        }
        rcd.setItems(mapApps, new NaviSelectDialog.OnDlgItemClickListener() {
            @Override
            public void onEnsureClicked(Dialog dialog, String value, boolean isChecked) {
                dialog.dismiss();
                if (activity.getString(R.string.inner_navi).equals(value)) {
                    launchNavigatorViaPoints(activity, startLL, endLL);
                    //                   startInnerNavi(activity, startLL, endLL);
                }
                if (activity.getString(R.string.baidu_navi).equals(value)) {
//                    startNative_Baidu(activity, startLL, endLL, start_place, destination);
                    startBikingNavi(activity, startLL, endLL);
                } else if (activity.getString(R.string.gaode_navi).equals(value)) {
                    startGaodeNavi(activity, startLL, endLL, start_place);
                }
                if (isChecked) {
                    //记住我的选择
                }
            }

            public void onCancleClicked(Dialog dialog) {
                dialog.dismiss();
            }
        }, true).show();
    }

    private static void launchNavigatorViaPoints(final Activity activity, LatLng startLL, LatLng endLL) {
        //这里给出一个起终点示例，实际应用中可以通过POI检索、外部POI来源等方式获取起终点坐标

        final BNRoutePlanNode sNode = new BNRoutePlanNode(startLL.longitude, startLL.latitude, null, "从这里开始", BNRoutePlanNode.CoordinateType.BD09LL);
        final BNRoutePlanNode eNode = new BNRoutePlanNode(endLL.longitude, endLL.latitude, null, "到这里结束", BNRoutePlanNode.CoordinateType.BD09LL);
        if (sNode != null && eNode != null) {
            List<BNRoutePlanNode> points = new ArrayList<BNRoutePlanNode>();
            points.add(sNode);
            points.add(eNode);
            //距离太近toast提示(100米内)
            double dis = DistanceUtil.getDistance(new LatLng(sNode.getLatitude(), sNode.getLongitude()), new LatLng(eNode.getLatitude(), eNode.getLongitude()));
            if (dis <= 100) {
                Toast.makeText(activity, "起点、途经点、终点距离太近", Toast.LENGTH_SHORT).show();
                return;
            }
            BaiduNaviManager.getInstance().launchNavigator(activity, points, 1, true, new BaiduNaviManager.RoutePlanListener() {
                public void onJumpToNavigator() {
            /*
             * 设置途径点以及resetEndNode会回调该接口
			 */
                    for (Activity ac : activityList) {
                        if (ac.getClass().getName().endsWith("BNDemoGuideActivity")) {
                            return;
                        }
                    }
                    Intent intent = new Intent(activity, BDInnerNaviActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(ROUTE_PLAN_NODE, (BNRoutePlanNode) sNode);
                    intent.putExtras(bundle);
                    activity.startActivity(intent);
                }

                public void onRoutePlanFailed() {
                    // TODO Auto-generated method stub
                    Toast.makeText(activity, "算路失败", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /**
     * 启动百度地图骑行导航(Native)
     */
    private static void startBikingNavi(Activity activity, LatLng startLL, LatLng endLL) {
        //距离太近toast提示(100米内)
        double dis = DistanceUtil.getDistance(new LatLng(startLL.latitude, startLL.longitude), new LatLng(endLL.latitude, endLL.longitude));
        if (dis <= 100) {
            Toast.makeText(activity, "起点、途经点、终点距离太近", Toast.LENGTH_SHORT).show();
            return;
        }
        // 构建 导航参数
        NaviParaOption para = new NaviParaOption()
                .startPoint(startLL).endPoint(endLL);
        try {
            BaiduMapNavigation.openBaiduMapBikeNavi(para, activity);
        } catch (BaiduMapAppNotSupportNaviException e) {
            e.printStackTrace();
        }
    }

    /**
     * 启动百度地图导航(Native)
     */
    public void startNavi(Activity activity, LatLng pt1, LatLng pt2) {
        // 构建 导航参数
        NaviParaOption para = new NaviParaOption()
                .startPoint(pt1).endPoint(pt2)
                .startName("天安门").endName("百度大厦");
        try {
            BaiduMapNavigation.openBaiduMapNavi(para, activity);
        } catch (BaiduMapAppNotSupportNaviException e) {
            e.printStackTrace();
            showDialog(activity);
        }

    }

    /**
     * 启动百度地图驾车路线规划
     */
    public void startRoutePlanDriving(Activity activity, LatLng pt1, LatLng pt2) {
        // 构建 route搜索参数
        RouteParaOption para = new RouteParaOption()
                .startPoint(pt1)
                .endPoint(pt2);

        try {
            BaiduMapRoutePlan.openBaiduMapDrivingRoute(para, activity);
        } catch (Exception e) {
            e.printStackTrace();
            showDialog(activity);
        }

    }
    /**
     * 通过Uri跳转到百度地图导航
     */
    public static void startNative_Baidu(Activity activity, LatLng pt1, LatLng pt2, String start_address, String end_address) {
        try {
            double dis = DistanceUtil.getDistance(new LatLng(pt1.latitude,pt1.longitude), new LatLng(pt2.latitude,pt2.longitude));
            if (dis <= 100) {
                Toast.makeText(activity, "起点、途经点、终点距离太近", Toast.LENGTH_SHORT).show();
                return;
            }
            String start_latlng = pt1.latitude + "," + pt1.longitude;
            String end_latlng = pt2.latitude + "," + pt2.longitude;
            Intent intent = Intent.getIntent("intent://map/direction?origin=latlng:"+start_latlng+"|name:"+"Start"+"&destination=latlng:"+end_latlng+"|name:"+"End"+"&mode=riding&src=这里随便写#Intent;scheme=bdapp;package=com.baidu.BaiduMap;end");
            Log.d("gaolei", "---------------" + start_address + "," + end_address);
            activity.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(activity, "地址解析错误", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 启动高德地图驾车路线规划
     */
    public static void startGaodeNavi(Activity activity, LatLng pt1, LatLng pt2, String start_place) {
        try {
            Intent intent = new Intent();
            double sLat = pt1.latitude, sLon = pt1.longitude, eLat = pt2.latitude, eLon = pt2.longitude;
            String poiAddress = LocationManager.getInstance().getAddress();
            Log.d("gaolei", "poiAddress---------gaode-----------" + poiAddress);
            intent.setData(android.net.Uri
                    .parse("androidamap://navi?sourceApplication=yongche&poiname=" + start_place + "&lat="
                            + eLat
                            + "&lon="
                            + eLon + "&dev=0&style=2"));
            intent.addCategory("android.intent.category.DEFAULT");
            intent.setPackage("com.autonavi.minimap");
            activity.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 路线规划监听器，规划成功后跳转至导航过程页面
     */
    private static class YCRoutePlanListener implements BaiduNaviManager.RoutePlanListener {

        private BNRoutePlanNode mBNRoutePlanNode = null;
        private Activity activity;

        public YCRoutePlanListener(BNRoutePlanNode node, Activity act) {
            mBNRoutePlanNode = node;
            activity = act;
        }

        @Override
        public void onJumpToNavigator() {
            Intent intent = new Intent(activity, BDInnerNaviActivity.class);
            activity.startActivity(intent);
            activity.startActivity(intent);
        }

        @Override
        public void onRoutePlanFailed() {

        }
    }

    /**
     * 提示未安装百度地图app或app版本过低
     */
    public void showDialog(final Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage("您尚未安装百度地图app或app版本过低，点击确认安装？");
        builder.setTitle("提示");
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                OpenClientUtil.getLatestBaiduMapApp(activity);
            }
        });

        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.create().show();

    }

    public static void initNavi(final Activity activity) {

        BaiduNaviManager.getInstance().init(activity, mSDCardPath, APP_FOLDER_NAME, new BaiduNaviManager.NaviInitListener() {
            public void onAuthResult(int status, String msg) {
                if (0 == status) {
//                    authinfo = "key校验成功!";
                } else {
//                    authinfo = "key校验失败, " + msg;
                }
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        Toast.makeText(activity, authinfo, Toast.LENGTH_LONG).show();
                    }
                });
            }

            public void initSuccess() {
//                Toast.makeText(activity, "百度导航引擎初始化成功", Toast.LENGTH_SHORT).show();
                initSetting();
            }

            public void initStart() {
//                Toast.makeText(activity, "百度导航引擎初始化开始", Toast.LENGTH_SHORT).show();
            }

            public void initFailed() {
//                Toast.makeText(activity, "百度导航引擎初始化失败", Toast.LENGTH_SHORT).show();
            }
        }, null, ttsHandler, ttsPlayStateListener);

    }

    private static void initSetting() {
        // 设置是否双屏显示
        BNaviSettingManager.setShowTotalRoadConditionBar(BNaviSettingManager.PreViewRoadCondition.ROAD_CONDITION_BAR_SHOW_ON);
        // 设置导航播报模式
        BNaviSettingManager.setVoiceMode(BNaviSettingManager.VoiceMode.Veteran);
        // 是否开启路况
        BNaviSettingManager.setRealRoadCondition(BNaviSettingManager.RealRoadCondition.NAVI_ITS_ON);
    }

    /**
     * 内部TTS播报状态回传handler
     */
    private static Handler ttsHandler = new Handler() {
        public void handleMessage(Message msg) {
            int type = msg.what;
            switch (type) {
                case BaiduNaviManager.TTSPlayMsgType.PLAY_START_MSG: {
//                    showToastMsg("Handler : TTS play start");
                    break;
                }
                case BaiduNaviManager.TTSPlayMsgType.PLAY_END_MSG: {
//                    showToastMsg("Handler : TTS play end");
                    break;
                }
                default:
                    break;
            }
        }
    };
    /**
     * 内部TTS播报状态回调接口
     */
    private static BaiduNaviManager.TTSPlayStateListener ttsPlayStateListener = new BaiduNaviManager.TTSPlayStateListener() {

        @Override
        public void playEnd() {
//            showToastMsg("TTSPlayStateListener : TTS play end");
        }

        @Override
        public void playStart() {
//            showToastMsg("TTSPlayStateListener : TTS play start");
        }
    };

}
