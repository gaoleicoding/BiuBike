//package com.biubike.nav;
//
//import android.app.Activity;
//import android.app.Dialog;
//import android.content.Context;
//import android.content.Intent;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.Message;
//import android.util.Log;
//import android.view.WindowManager;
//import android.widget.PopupWindow;
//import android.widget.Toast;
//
//import com.baidu.mapapi.model.LatLng;
//import com.baidu.mapapi.navi.BaiduMapAppNotSupportNaviException;
//import com.baidu.mapapi.navi.BaiduMapNavigation;
//import com.baidu.mapapi.navi.IllegalNaviArgumentException;
//import com.baidu.mapapi.navi.NaviParaOption;
//import com.baidu.mapapi.utils.DistanceUtil;
//import com.baidu.navisdk.adapter.BNRoutePlanNode;
//import com.baidu.navisdk.adapter.BNaviSettingManager;
//import com.baidu.navisdk.adapter.BaiduNaviManager;
//import com.carecology.gasstation.activity.GasInBDNaviActivity;
//import com.carecology.gasstation.bean.GasStationInfo;
//import com.javadocmd.simplelatlng.LatLngChinaTool;
//import com.yongche.CommonFiled;
//import com.yongche.YcConfig;
//import com.yongche.YongcheApplication;
//import com.yongche.core.Location.LocationAPI;
//import com.yongche.core.Location.utils.LocationConfig;
//import com.yongche.core.Location.utils.YongcheLocation;
//import com.yongche.customview.RedioAndCheckboxDialog;
//import com.yongche.libs.manager.FileManager;
//import com.yongche.libs.utils.ClickUtil;
//import com.yongche.libs.utils.CommonUtil;
//import com.yongche.libs.utils.DriverStatusUtil;
//import com.yongche.libs.utils.YongcheProgress;
//import com.yongche.libs.utils.log.Logger;
//import com.yongche.libs.utils.map.baidu.BDMapUtil;
//import com.yongche.model.OrderEntry;
//import com.yongche.model.OrderStatus;
//import com.yongche.model.ThermodynamicEntry;
//import com.yongche.navigation.BNavigatorSearchPopwindow;
//import com.yongche.navigation.INavCommonData;
//import com.yongche.navigation.SearchAddress;
//import com.yongche.ui.ShowBDMapActivity;
//
//import java.io.File;
//import java.io.Serializable;
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * Created by chenxin on 16/5/31.
// */
//public class NavUtil {
//    public static final String ROUTE_PLAN_NODE = "routePlanNode";
//    public static final String EXTRA_NAVI_MODE = "navimode";
//    public static final String EXTRA_ORDER = "order";
//    public static final String EXTRA_GAS_STATION_INFO = "end_gas_station";
//    private static final String TAG = NavUtil.class.getSimpleName();
//    public static boolean isSlide = false;
//
//    public static String map_type = "百度导航";
//
//    //================================================导航初始化================================================
//
//    private String mSDCardPath;
//    private static final String APP_FOLDER_NAME = "yc_navi";
//
//    private boolean initDirs(Context context) {
//        mSDCardPath = FileManager.getSdcardPath(context);
//        if (mSDCardPath == null) {
//            return false;
//        }
//        File f = new File(mSDCardPath, APP_FOLDER_NAME);
//        if (!f.exists()) {
//            try {
//                f.mkdir();
//            } catch (Exception e) {
//                e.printStackTrace();
//                return false;
//            }
//        }
//        return true;
//    }
//
//    String authinfo = null;
//
//    /**
//     * 初始化
//     *
//     * @param act
//     */
//    public void initNavi(final Activity act) {
//        if (!initDirs(act.getBaseContext())) {
//            return;
//        }
//
//        /**
//         * 百度服务授权和引擎初始化。已内置TTS播报
//         *
//         * 参数：
//         *  1. activity
//         *  2. sdcardRootPath - 系统SD卡根目录
//         *  3. appFolderName - 应用在SD卡中的目录名
//         *  4. naviInitListener - 百度导航初始化监听器
//         *  5. ttsCallback - 外部TTS能力回调接口，若使用百度内置TTS能力，传入null即可
//         *  6. ttsHandler - 异步获取百度内部TTS播报状态
//         *  7. ttsStateListener - 同步获取百度内部TTS播报状态
//         *
//         *  BaiduNaviManger.isNaviInited 百度导航是否初始化。
//         */
//        BaiduNaviManager.getInstance().init(act, mSDCardPath, APP_FOLDER_NAME,
//                new BaiduNaviManager.NaviInitListener() {
//                    @Override
//                    public void onAuthResult(int status, String msg) {
//                        Log.d(TAG, "--status: " + status + "------: " + msg);
//
//                        if (0 == status) {
//                            authinfo = "key校验成功!";
//                        } else {
//                            authinfo = "key校验失败, " + msg;
//                        }
//
//
//                        act.runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                //Toast.makeText(act, authinfo, Toast.LENGTH_SHORT).show();
//                            }
//                        });
//
//                    }
//
//                    @Override
//                    public void initStart() {
//                        initSetting();
//                        //Toast.makeText(act, "百度导航引擎初始化开始", Toast.LENGTH_SHORT).show();
//                    }
//
//                    @Override
//                    public void initSuccess() {
//                        //Toast.makeText(act, "百度导航引擎初始化成功", Toast.LENGTH_SHORT).show();
//                    }
//
//                    @Override
//                    public void initFailed() {
//                        Logger.d(TAG,"initFailed");
//                        //Toast.makeText(act, "百度导航引擎初始化失败", Toast.LENGTH_SHORT).show();
//                    }
//                }, null, ttsHandler, null);
//    }
//
//
//    /**
//     * 自定义图层设置
//     */
//    private void initSetting() {
//        BNaviSettingManager.setDayNightMode(BNaviSettingManager.DayNightMode.DAY_NIGHT_MODE_DAY);
//        BNaviSettingManager.setShowTotalRoadConditionBar(BNaviSettingManager.PreViewRoadCondition.ROAD_CONDITION_BAR_SHOW_ON);
//        BNaviSettingManager.setVoiceMode(BNaviSettingManager.VoiceMode.Veteran);
//        BNaviSettingManager.setPowerSaveMode(BNaviSettingManager.PowerSaveMode.DISABLE_MODE);
//        BNaviSettingManager.setRealRoadCondition(BNaviSettingManager.RealRoadCondition.NAVI_ITS_ON);
//    }
//
//    /**
//     * 内部TTS播报状态回传handler
//     */
//    private Handler ttsHandler = new Handler() {
//        public void handleMessage(Message msg) {
//            int type = msg.what;
//            switch (type) {
//                case BaiduNaviManager.TTSPlayMsgType.PLAY_START_MSG: {
////                    showToastMsg("Handler : TTS play start");
//                    break;
//                }
//                case BaiduNaviManager.TTSPlayMsgType.PLAY_END_MSG: {
////                    showToastMsg("Handler : TTS play end");
//                    break;
//                }
//                default:
//                    break;
//            }
//        }
//    };
//
//
//    // ====================================================调起导航======================================================================
//
//    /**
//     * 第一步：没有下车地点时提供地址搜索框
//     *
//     * @param activity
//     * @param naviMode
//     * @param orderEntry
//     * @param isNeedShowPop 是否需要弹出pop
//     */
//    private static BNavigatorSearchPopwindow pop;
//
//    public static void startNavitor(final Activity activity, final int naviMode, final INavCommonData navData, boolean isNeedShowPop) {
//        // 当前导航至下车地点且下车地点为空，显示搜索框
//        if (navData instanceof OrderEntry) {
//            final OrderEntry orderEntry = (OrderEntry) navData;
//            if (naviMode == ShowBDMapActivity.NAVI_MODE_TO_END_POINT
//                    && orderEntry.getPosition_end_lat() == 0 && orderEntry.getPosition_end_lng() == 0) {
//                if (!isNeedShowPop) return;
//                pop = new BNavigatorSearchPopwindow(activity, new BNavigatorSearchPopwindow.SearchCallBack() {
//                    @Override
//                    public void setResult(SearchAddress address) {
//                        OrderEntry temp;
//                        try {
//                            temp = orderEntry.cloneSelf();
//                            temp.setPosition_end_lat(address.getLatitude());
//                            temp.setPosition_end_lng(address.getLongitude());
//                            temp.setPosition_end(address.getName());
//                            startNavi(activity, naviMode, temp);
//                        } catch (CloneNotSupportedException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                });
//                pop.setSoftInputMode(PopupWindow.INPUT_METHOD_NEEDED);
//                pop.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
//                pop.show();
//                return;
//            }
//        }else if(navData instanceof GasStationInfo) {
//            final GasStationInfo gasStationInfo = (GasStationInfo) navData;
//            if (!isNeedShowPop) return;
//            pop = new BNavigatorSearchPopwindow(activity, new BNavigatorSearchPopwindow.SearchCallBack() {
//                @Override
//                public void setResult(SearchAddress address) {
//                    gasStationInfo.setLatitude(address.getLatitude());
//                    gasStationInfo.setLongitude(address.getLongitude());
//                    gasStationInfo.setAddress(address.getName());
//                    startNavi(activity, naviMode, gasStationInfo);
//                }
//            });
//            pop.setSoftInputMode(PopupWindow.INPUT_METHOD_NEEDED);
//            pop.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
//            pop.show();
//            return;
//        }
//        startNavi(activity, naviMode, navData);
//    }
//
//
//    /**
//     * 第二步：是否有记住默认导航： 跳转至导航或者弹出选择框
//     *
//     * @param activity
//     * @param naviMode
//     * @param navData
//     */
//    public static void startNavi(Activity activity, int naviMode, INavCommonData navData) {
//        if (YcConfig.getIs_baidumap_default() == 1) {
//            //系统设置了默认调百度地图
//            if (YongcheApplication.getApplication().isRememberedNavWay()) {
//                //有默认
//                getDefaultNavWay(activity, naviMode, navData);
//            } else {
//                //无默认导航
//                if (BDMapUtil.hasApp(activity, BDMapUtil.APP_BAIDU_MAP)) {//有百度 —>  默认百度（不弹）
//                    launchNaviBySomeWay(activity, naviMode, navData, BAIDU_NAVI_ID);
//                } else if (!BDMapUtil.hasApp(activity, BDMapUtil.APP_AMAP) && !BDMapUtil.hasApp(activity, BDMapUtil.APP_BAIDU_MAP)) { //无百度、无高德 ——>内置导航
//                    launchNaviBySomeWay(activity, naviMode, navData, INNER_NAVI_ID);
//                } else {//无百度 —> 现有逻辑（弹）
//                    showChoiceNaviWayDialog(activity, naviMode, navData);
//                }
//            }
//        } else {
//            //系统没有设置默认调百度地图
//            if (YongcheApplication.getApplication().isRememberedNavWay()) {
//                //有默认
//                getDefaultNavWay(activity, naviMode, navData);
//            } else {
//                //无默认
//                if (!BDMapUtil.hasApp(activity, BDMapUtil.APP_AMAP) && !BDMapUtil.hasApp(activity, BDMapUtil.APP_BAIDU_MAP)) {
//                    launchNaviBySomeWay(activity, naviMode, navData, INNER_NAVI_ID);
//                } else {//有高德或百度，且没有选择记住，弹出dialog
//                    showChoiceNaviWayDialog(activity, naviMode, navData);
//                }
//            }
//        }
//    }
//
//    public static void startNaviAgain(Activity activity, int naviMode, INavCommonData navData) {
//            //系统设置了默认调百度地图
//            if (YongcheApplication.getApplication().isRememberedNavWay()) {
//                //有默认
//                getDefaultNavWay(activity, naviMode, navData);
//            } else {
//                //无默认导航（弹）
//                showChoiceNaviWayDialog1(activity, naviMode, navData);
//            }
//    }
//
//    /**
//     * 本地设置了默认导航，则按照默认导航
//     */
//    private static void getDefaultNavWay(Activity activity, int naviMode, INavCommonData navData) {
//        String myNavWay = YongcheApplication.getApplication().getMyNavWay();
//        if ("百度地图".equals(myNavWay) && BDMapUtil.hasApp(activity, BDMapUtil.APP_BAIDU_MAP)) {
//            launchNaviBySomeWay(activity, naviMode, navData, BAIDU_NAVI_ID);
//        } else if ("高德地图".equals(myNavWay) && BDMapUtil.hasApp(activity, BDMapUtil.APP_AMAP)) {
//            launchNaviBySomeWay(activity, naviMode, navData, AMAP_NAVI_ID);
//        } else if ("百度地图".equals(myNavWay) && !BDMapUtil.hasApp(activity, BDMapUtil.APP_BAIDU_MAP) && BDMapUtil.hasApp(activity, BDMapUtil.APP_AMAP)
//                || "高德地图".equals(myNavWay) && !BDMapUtil.hasApp(activity, BDMapUtil.APP_AMAP) && BDMapUtil.hasApp(activity, BDMapUtil.APP_BAIDU_MAP)) {
//            showChoiceNaviWayDialog(activity, naviMode, navData);
//        } else {//内置导航
//            launchNaviBySomeWay(activity, naviMode, navData, INNER_NAVI_ID);
//        }
//    }
//
//
//    /**
//     * 第三步：选择启动导航，百度、高德、内置
//     *
//     * @param activity
//     * @param naviMode
//     * @param
//     * @param naviType   类型：当前有：百度、高德、内置
//     */
//    private static void launchNaviBySomeWay(Activity activity, int naviMode, INavCommonData navData, int naviType) {
//        switch (naviType) {
//            case INNER_NAVI_ID:
//                YongcheProgress.showProgress(activity, "正在启动导航");
//                if (!ClickUtil.isFastDoubleClick(5000)) {
//                    if (navData instanceof OrderEntry) {
//                        OrderEntry orderEntry = (OrderEntry) navData;
//                        CommonUtil.MobclickAgentEvent(activity, CommonFiled.v501_Built_in_map);
//                        if (orderEntry.getStatus() == OrderStatus.NOTSTARTED.getValue() || orderEntry.getStatus() == OrderStatus.READY.getValue()) {// 等待就位，点击导航区在线地图
//                            CommonUtil.MobclickAgentEvent(activity, CommonFiled.v36_page_servece_order_already_depart_online_map);
//                        }
//                        if (orderEntry.getStatus() == OrderStatus.STARTED.getValue()) { // 服务中，点击导航区在线地图
//                            CommonUtil.MobclickAgentEvent(activity, CommonFiled.v36_page_servece_order_service_online_map);
//                        }
//                    }
//                    launchInnerNavis(activity, naviMode, navData);
//                }
//                break;
//            case BAIDU_NAVI_ID:
//                //使用了外置导航，增加标记
//                YongcheApplication.getApplication().setNavByMapApp(true);
//                new DriverStatusUtil(activity).setBusy(true);
//                if (BDMapUtil.hasApp(activity, BDMapUtil.APP_BAIDU_MAP)) {
//                    if (navData instanceof OrderEntry) {
//                        OrderEntry orderEntry = (OrderEntry) navData;
//                        CommonUtil.MobclickAgentEvent(activity, CommonFiled.v501_baidu_map);
//                        if (orderEntry.getStatus() == OrderStatus.NOTSTARTED.getValue() || orderEntry.getStatus() == OrderStatus.READY.getValue()) {// 等待就位，点击导航去百度地图
//                            CommonUtil.MobclickAgentEvent(activity, CommonFiled.v36_page_servece_order_already_depart_native_map);
//                        }
//                        if (orderEntry.getStatus() == OrderStatus.STARTED.getValue()) {// 服务中，点击导航去百度地图
//                            CommonUtil.MobclickAgentEvent(activity, CommonFiled.v36_page_servece_order_service_native_map);
//                        }
//                    }
//                    launchBaiduAppNaviByIntent(activity, naviMode, navData);
//                } else {
//                    if (navData instanceof OrderEntry) {
//                        OrderEntry orderEntry = (OrderEntry) navData;
//                        CommonUtil.MobclickAgentEvent(activity, CommonFiled.v501_Built_in_map);
//                        if (orderEntry.getStatus() == OrderStatus.NOTSTARTED.getValue() || orderEntry.getStatus() == OrderStatus.READY.getValue()) {// 等待就位，点击导航区在线地图
//                            CommonUtil.MobclickAgentEvent(activity, CommonFiled.v36_page_servece_order_already_depart_online_map);
//                        }
//                        if (orderEntry.getStatus() == OrderStatus.STARTED.getValue()) { // 服务中，点击导航区在线地图
//                            CommonUtil.MobclickAgentEvent(activity, CommonFiled.v36_page_servece_order_service_online_map);
//                        }
//                    }
//                    launchInnerNavis(activity, naviMode, navData);
//                    YongcheApplication.getApplication().setRememberNavWay(false);//记住了百度地图，但是卸载了app，此时把记住状态还原
//                }
//                break;
//            case AMAP_NAVI_ID:
//                //使用了外置导航，增加标记
//                YongcheApplication.getApplication().setNavByMapApp(true);
//                new DriverStatusUtil(activity).setBusy(true);
//                if (BDMapUtil.hasApp(activity, BDMapUtil.APP_AMAP)) {
//                    if (navData instanceof OrderEntry) {
//                        OrderEntry orderEntry = (OrderEntry) navData;
//                        CommonUtil.MobclickAgentEvent(activity, CommonFiled.v501_gaode);
//                        if (orderEntry.getStatus() == OrderStatus.NOTSTARTED.getValue() || orderEntry.getStatus() == OrderStatus.READY.getValue()) {// 等待就位，点击导航去百度地图
//                            CommonUtil.MobclickAgentEvent(activity, CommonFiled.v36_page_servece_order_already_depart_native_map);
//                        }
//                        if (orderEntry.getStatus() == OrderStatus.STARTED.getValue()) {// 服务中，点击导航去百度地图
//                            CommonUtil.MobclickAgentEvent(activity, CommonFiled.v36_page_servece_order_service_native_map);
//                        }
//                    }
//                    launchAmapApp(activity, navData);
//                } else {
//                    if (navData instanceof OrderEntry) {
//                        OrderEntry orderEntry = (OrderEntry) navData;
//                        CommonUtil.MobclickAgentEvent(activity, CommonFiled.v501_Built_in_map);
//                        if (orderEntry.getStatus() == OrderStatus.NOTSTARTED.getValue() || orderEntry.getStatus() == OrderStatus.READY.getValue()) {// 等待就位，点击导航区在线地图
//                            CommonUtil.MobclickAgentEvent(activity, CommonFiled.v36_page_servece_order_already_depart_online_map);
//                        }
//                        if (orderEntry.getStatus() == OrderStatus.STARTED.getValue()) { // 服务中，点击导航区在线地图
//                            CommonUtil.MobclickAgentEvent(activity, CommonFiled.v36_page_servece_order_service_online_map);
//                        }
//                    }
//                    launchInnerNavis(activity, naviMode, navData);
//                    YongcheApplication.getApplication().setRememberNavWay(false);//记住了高德地图，但是卸载了高德app，此时把记住状态还原
//                }
//                break;
//        }
//    }
//
//    /**
//     * 第四步：调用内置百度导航
//     * - 设置上下车地点
//     *
//     * @param act
//     * @param
//     */
//    public static void launchInnerNavis(Activity act, int naviMod, INavCommonData navData) {
//        BNRoutePlanNode sNode = null;
//        BNRoutePlanNode eNode = null;
//
//        if (LocationAPI.getLastKnownLocation() != null
//                && LocationAPI.getLastKnownLocation().getLatitude() != 0
//                && LocationAPI.getLastKnownLocation().getLongitude() != 0) {
//            YongcheLocation location = LocationAPI.getLastKnownLocation();
//            // 默认百度坐标系
//            com.javadocmd.simplelatlng.LatLng dest = coordConvert(location);
//            if (navData instanceof OrderEntry) {
//                OrderEntry orderEntry = (OrderEntry) navData;
//                if (orderEntry.getStatus() == OrderStatus.NOTSTARTED.getValue()) {
//                    sNode = new BNRoutePlanNode(dest.getLongitude(), dest.getLatitude(), "从这里开始", null, BNRoutePlanNode.CoordinateType.BD09LL);
//                    eNode = new BNRoutePlanNode(orderEntry.getPosition_start_lng(), orderEntry.getPosition_start_lat(), "到这里结束", null, BNRoutePlanNode.CoordinateType.BD09LL);
//                } else if (orderEntry.getStatus() == OrderStatus.READY.getValue()
//                        || orderEntry.getStatus() == OrderStatus.STARTED.getValue()) {
//                    sNode = new BNRoutePlanNode(dest.getLongitude(), dest.getLatitude(), "从这里开始", null, BNRoutePlanNode.CoordinateType.BD09LL);
//                    eNode = new BNRoutePlanNode(orderEntry.getPosition_end_lng(), orderEntry.getPosition_end_lat(), "到这里结束", null, BNRoutePlanNode.CoordinateType.BD09LL);
//                }
//            }else if (navData instanceof GasStationInfo){
//                GasStationInfo gasStationInfo = (GasStationInfo) navData;
//                sNode = new BNRoutePlanNode(dest.getLongitude(), dest.getLatitude(), "从这里开始", null, BNRoutePlanNode.CoordinateType.BD09LL);
//                eNode = new BNRoutePlanNode(gasStationInfo.getLongitude(), gasStationInfo.getLatitude(), "到这里结束", null, BNRoutePlanNode.CoordinateType.BD09LL);
//            }else if(navData instanceof ThermodynamicEntry){
//                ThermodynamicEntry entry = (ThermodynamicEntry) navData;
//                sNode = new BNRoutePlanNode(dest.getLongitude(), dest.getLatitude(), "从这里开始", null, BNRoutePlanNode.CoordinateType.BD09LL);
//                eNode = new BNRoutePlanNode(entry.getLongitude(), entry.getLatitude(), "到这里结束", null, BNRoutePlanNode.CoordinateType.BD09LL);
//            }
//        } else {
//            YongcheProgress.closeProgress();
//            showToastMsg(act, "无法获取当前位置");
//            //sNode = new BNRoutePlanNode(116.309408, 39.98883, "hello word", null, BNRoutePlanNode.CoordinateType.BD09LL);
//            //eNode = new BNRoutePlanNode(orderEntry.getPosition_end_lng(), orderEntry.getPosition_end_lat(), "到这里结束", null, BNRoutePlanNode.CoordinateType.BD09LL);
//        }
//
//        if (sNode != null && eNode != null) {
//            List<BNRoutePlanNode> list = new ArrayList<BNRoutePlanNode>();
//            list.add(sNode);
//            list.add(eNode);
//
//            //距离太近toast提示(100米内)
//            double dis = DistanceUtil.getDistance(new LatLng(sNode.getLatitude(), sNode.getLongitude()), new LatLng(eNode.getLatitude(), eNode.getLongitude()));
//            if (dis <= 100) {
//                YongcheProgress.closeProgress();
//                showToastMsg(act, "起点、途经点、终点距离太近");
//                return;
//            }
//
//            /**
//             * 参数：
//             *  1. activity
//             *  2. List<nodes>  依次-起点，终点，途经点，途经点最多三个。
//             *  3. preference - 算路偏好
//             *  4. isGPSNav true-真实GPS导航  false-模拟导航
//             *  5. listener 开始导航回调监听，用于进入导航界面
//             */
//            BaiduNaviManager.RoutePlanListener listener = new YCRoutePlanListener(sNode, act, navData, naviMod);
//            BaiduNaviManager.getInstance().launchNavigator(act, list, 1, true, listener);
//        }
//    }
//
//    /**
//     * 路线规划监听器，规划成功后跳转至导航过程页面
//     */
//    private static class YCRoutePlanListener implements BaiduNaviManager.RoutePlanListener {
//
//        private BNRoutePlanNode mBNRoutePlanNode = null;
//        private Activity activity;
//        private INavCommonData navData;
//        private int naviMode;
//
//        public YCRoutePlanListener(BNRoutePlanNode node, Activity act, INavCommonData navData, int nMod) {
//            mBNRoutePlanNode = node;
//            activity = act;
//            this.navData = navData;
//            naviMode = nMod;
//        }
//
//        @Override
//        public void onJumpToNavigator() {
//            //TODO 6.0.1 内置地图路线规划成功，可以统计
//            if (navData instanceof OrderEntry) {
//                OrderEntry order = (OrderEntry) navData;
//                Logger.i("NavUtil", order.toString());
//                map_type = "内置导航";
//                CommonUtil.MapNavclickAgentEvent(activity, "", map_type, order);
//
//                YongcheProgress.closeProgress();
//                Intent intent = new Intent(activity, BDNaviActivity.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                Bundle bundle = new Bundle();
//                bundle.putSerializable(ROUTE_PLAN_NODE, (BNRoutePlanNode) mBNRoutePlanNode);
//                intent.putExtra(EXTRA_ORDER, (Serializable) order);
//                intent.putExtra(EXTRA_NAVI_MODE, naviMode);
//                intent.putExtras(bundle);
//                activity.startActivity(intent);
//            }else if (navData instanceof GasStationInfo){
//                YongcheProgress.closeProgress();
//                GasStationInfo gasStationInfo = (GasStationInfo) navData;
//                Intent intent = new Intent(activity, GasInBDNaviActivity.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                intent.putExtra(EXTRA_GAS_STATION_INFO, gasStationInfo);
//                activity.startActivity(intent);
//            }else if (navData instanceof ThermodynamicEntry){
//                YongcheProgress.closeProgress();
//                ThermodynamicEntry entry = (ThermodynamicEntry) navData;
//                Intent intent = new Intent(activity, ThermodynamicNaviActivity.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                Bundle bundle = new Bundle();
//                bundle.putSerializable(ROUTE_PLAN_NODE, (BNRoutePlanNode) mBNRoutePlanNode);
//                intent.putExtra(EXTRA_ORDER, (Serializable) entry);
//                intent.putExtra(EXTRA_NAVI_MODE, naviMode);
//                intent.putExtras(bundle);
//                activity.startActivity(intent);
//            }
//        }
//
//        @Override
//        public void onRoutePlanFailed() {
//
//        }
//    }
//
//    public static void launchBaiduAppNaviByIntent(Activity activity, int naviMode, INavCommonData navData) {
//        //移动APP调起Android百度地图方式举例
////        String intentStr = "intent://map/direction?origin=latlng:%s,%s|name:从这里开始&destination=latlng:%s,%s|name:到这里结束&mode=driving&src=yidao.androiddriver#Intent;scheme=bdapp;package=com.baidu.BaiduMap;end";
//        String intentStr = "intent://map/navi?location=%s,%s&type=BLK&src=yidao.androiddriver#Intent;scheme=bdapp;package=com.baidu.BaiduMap;end";
//        try {
//            YongcheLocation location = LocationAPI.getLastKnownLocation();
//            LatLng currentPoint = null, startPoint = null, endPoint = null;
//            if (location == null || location.getLatitude() == 0 || location.getLongitude() == 0) {
//                Toast.makeText(activity, "无法定位当前位置,请稍候再试", Toast.LENGTH_SHORT).show();
//                return;
//            }
//
//            startPoint = new LatLng(location.getLatitude(), location.getLongitude());
//            if (navData instanceof OrderEntry) {
//                OrderEntry orderEntry = (OrderEntry) navData;
//                if (naviMode == ShowBDMapActivity.NAVI_MODE_TO_END_POINT
//                        && orderEntry.getPosition_end_lat() == 0
//                        && orderEntry.getPosition_end_lng() == 0) {
//                    Toast.makeText(activity, "乘客未设置下车地点，无法完成导航。", Toast.LENGTH_SHORT).show();
//                    return;
//                }
//                startPoint = new LatLng(orderEntry.getPosition_start_lat(), orderEntry.getPosition_start_lng());
//                endPoint = new LatLng(orderEntry.getPosition_end_lat(), orderEntry.getPosition_end_lng());
//
//                //TODO 6.0.1统计百度地图打开的次数
//                map_type = "百度导航";
//                CommonUtil.MapNavclickAgentEvent(activity, "", map_type, orderEntry);
//            }else if (navData instanceof GasStationInfo){
//                GasStationInfo gasStationInfo = (GasStationInfo) navData;
//                if (gasStationInfo.getLatitude() == 0
//                        && gasStationInfo.getLongitude() == 0) {
//                    Toast.makeText(activity, "获取目的地失败，无法完成导航。", Toast.LENGTH_SHORT).show();
//                    return;
//                }
//                endPoint = new LatLng(gasStationInfo.getLatitude(), gasStationInfo.getLongitude());
//            }else if(navData instanceof ThermodynamicEntry){
//                ThermodynamicEntry entry = (ThermodynamicEntry) navData;
//                if (entry.getLatitude() == 0
//                    && entry.getLongitude() == 0) {
//                    Toast.makeText(activity, "获取目的地失败，无法完成导航。", Toast.LENGTH_SHORT).show();
//                    return;
//                }
//                endPoint = new LatLng(entry.getLatitude(), entry.getLongitude());
//            }
//            // 将当前坐标转为百度坐标系
//            if (location.getCoordinateSystem().equals(LocationConfig.COORDINATE_WORLD)) {
//                com.javadocmd.simplelatlng.LatLng bdPos = LatLngChinaTool.World2Baidu(new com.javadocmd.simplelatlng.LatLng(location.getLatitude(), location.getLongitude()));
//                currentPoint = new LatLng(bdPos.getLatitude(), bdPos.getLongitude());
//            } else {
//                currentPoint = new LatLng(location.getLatitude(), location.getLongitude());
//            }
//            if (naviMode == ShowBDMapActivity.NAVI_MODE_TO_START_POINT) {
////                para.startPoint(currentPoint);
////                para.startName("从这里开始");
////                para.endPoint(startPoint);
////                para.endName("到这里结束");
//                intentStr = String.format(intentStr, startPoint.latitude, startPoint.longitude);
//                //计算两点之间的距离(100米内不予导航)
//                double distance = DistanceUtil.getDistance(currentPoint, startPoint);
//                if (distance <= 100) {
//                    showToastMsg(activity, "距离上车地点太近，暂时不提供导航");
//                    return;
//                }
//            } else if (naviMode == ShowBDMapActivity.NAVI_MODE_TO_END_POINT) {
////                para.startPoint(currentPoint);
////                para.startName("从这里开始");
////                para.endPoint(endPoint);
////                para.endName("到这里结束");
//                intentStr = String.format(intentStr, endPoint.latitude, endPoint.longitude);
//                //计算两点之间的距离(100米内不予导航)
//                double distance = DistanceUtil.getDistance(currentPoint, endPoint);
//                if (distance <= 100) {
//                    showToastMsg(activity, "距离目的地太近，暂时不提供导航");
//                    return;
//                }
//            } else {
//                return;
//            }
//
//            CommonUtil.MobclickAgentEvent(activity, CommonFiled.v504_baidunavigation_first);
//            Intent intent = Intent.getIntent(intentStr);
//            activity.startActivity(intent); //启动调用
//
//
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            Toast.makeText(activity, "路线规划失败，无法完成导航。", Toast.LENGTH_SHORT).show();
//        }
//
//    }
//
//    /**
//     * 启动外置百度导航
//     *
//     * @param activity
//     * @param naviMode
//     * @param orderEntry
//     */
//    private static void launchBaiduAppNavi(Activity activity, int naviMode, OrderEntry orderEntry) {
//        NaviParaOption para = new NaviParaOption();
//        YongcheLocation location = LocationAPI.getLastKnownLocation();
//        LatLng currentPoint = null, startPoint = null, endPoint = null;
//        if (location == null || location.getLatitude() == 0 || location.getLongitude() == 0) {
//            Toast.makeText(activity, "无法定位当前位置,请稍候再试", Toast.LENGTH_SHORT).show();
//            return;
//        }
//        if (naviMode == ShowBDMapActivity.NAVI_MODE_TO_END_POINT
//                && orderEntry.getPosition_end_lat() == 0
//                && orderEntry.getPosition_end_lng() == 0) {
//            Toast.makeText(activity, "乘客未设置下车地点，无法完成导航。", Toast.LENGTH_SHORT).show();
//            return;
//        }
//        startPoint = new LatLng(orderEntry.getPosition_start_lat(), orderEntry.getPosition_start_lng());
//        endPoint = new LatLng(orderEntry.getPosition_end_lat(), orderEntry.getPosition_end_lng());
//        // 将当前坐标转为百度坐标系
//        if (location.getCoordinateSystem().equals(LocationConfig.COORDINATE_WORLD)) {
//            com.javadocmd.simplelatlng.LatLng bdPos = LatLngChinaTool.World2Baidu(new com.javadocmd.simplelatlng.LatLng(location.getLatitude(), location.getLongitude()));
//            currentPoint = new LatLng(bdPos.getLatitude(), bdPos.getLongitude());
//        } else {
//            currentPoint = new LatLng(location.getLatitude(), location.getLongitude());
//        }
//        if (naviMode == ShowBDMapActivity.NAVI_MODE_TO_START_POINT) {
//            para.startPoint(currentPoint);
//            para.startName("从这里开始");
//            para.endPoint(startPoint);
//            para.endName("到这里结束");
//            //计算两点之间的距离(100米内不予导航)
//            double distance = DistanceUtil.getDistance(currentPoint, startPoint);
//            if (distance <= 100) {
//                showToastMsg(activity, "距离上车地点太近，暂时不提供导航");
//                return;
//            }
//        } else if (naviMode == ShowBDMapActivity.NAVI_MODE_TO_END_POINT) {
//            para.startPoint(currentPoint);
//            para.startName("从这里开始");
//            para.endPoint(endPoint);
//            para.endName("到这里结束");
//            //计算两点之间的距离(100米内不予导航)
//            double distance = DistanceUtil.getDistance(currentPoint, endPoint);
//            if (distance <= 100) {
//                showToastMsg(activity, "距离目的地太近，暂时不提供导航");
//                return;
//            }
//        } else {
//            return;
//        }
//        try {
//            BaiduMapNavigation.openBaiduMapNavi(para, activity);
//        } catch (BaiduMapAppNotSupportNaviException e) {
//            launchInnerNavis(activity, naviMode, orderEntry);
//
//            e.printStackTrace();
//        } catch (IllegalNaviArgumentException e) {
//            if (orderEntry.getStatus() == OrderStatus.READY.getValue()
//                    && getIntLatLng(orderEntry.getPosition_end_lat()) == 0
//                    && getIntLatLng(orderEntry.getPosition_end_lng()) == 0) {
//                Toast.makeText(activity, "乘客未设置下车地点，无法完成导航。", Toast.LENGTH_SHORT).show();
//            } else {
//                Toast.makeText(activity, "路线规划失败，无法完成导航。", Toast.LENGTH_SHORT).show();
//            }
//            e.printStackTrace();
//        }
//    }
//
//    /**
//     * 调用外置高德导航
//     *
//     * @param activity
//     * @param
//     */
//    private static void launchAmapApp(Activity activity, INavCommonData navData) {
//
//        float lat = 0.0f;
//        float lon = 0.0f;
//
//        String poiName = "default";
//        try {
//            if (navData instanceof OrderEntry) {
//                OrderEntry orderEntry = (OrderEntry) navData;
//
//                if (orderEntry.getStatus() == OrderStatus.NOTSTARTED.getValue()) {
//                    lat = orderEntry.getPosition_start_lat();
//                    lon = orderEntry.getPosition_start_lng();
//                    com.javadocmd.simplelatlng.LatLng src = new com.javadocmd.simplelatlng.LatLng(
//                            lat, lon);
//                    com.javadocmd.simplelatlng.LatLng marsSrc = LatLngChinaTool.Baidu2Mars(src);
//                    lat = (float) marsSrc.getLatitude();
//                    lon = (float) marsSrc.getLongitude();
//                    poiName = orderEntry.getPosition_start();
//
//                } else if (orderEntry.getStatus() == OrderStatus.READY.getValue()
//                        || orderEntry.getStatus() == OrderStatus.STARTED.getValue()) {
//                    lat = orderEntry.getPosition_end_lat();
//                    lon = orderEntry.getPosition_end_lng();
//                    com.javadocmd.simplelatlng.LatLng src = new com.javadocmd.simplelatlng.LatLng(
//                            lat, lon);
//                    com.javadocmd.simplelatlng.LatLng marsSrc = LatLngChinaTool.Baidu2Mars(src);
//                    lat = (float) marsSrc.getLatitude();
//                    lon = (float) marsSrc.getLongitude();
//                    poiName = orderEntry.getPosition_end();
//                }
//
//                //TODO 6.0.1统计高德地图打开的次数
//                map_type = "高德导航";
//                CommonUtil.MapNavclickAgentEvent(activity, "", map_type, orderEntry);
//            }else if (navData instanceof GasStationInfo){
//                GasStationInfo gasStationInfo = (GasStationInfo) navData;
//                lat = (float) gasStationInfo.getLatitude();
//                lon = (float) gasStationInfo.getLongitude();
//                com.javadocmd.simplelatlng.LatLng src = new com.javadocmd.simplelatlng.LatLng(
//                        lat, lon);
//                com.javadocmd.simplelatlng.LatLng marsSrc = LatLngChinaTool.Baidu2Mars(src);
//                lat = (float) marsSrc.getLatitude();
//                lon = (float) marsSrc.getLongitude();
//                poiName = gasStationInfo.getAddress();
//            }else if(navData instanceof ThermodynamicEntry){
//                ThermodynamicEntry entry = (ThermodynamicEntry) navData;
//                lat = (float) entry.getLatitude();
//                lon = (float) entry.getLongitude();
//                com.javadocmd.simplelatlng.LatLng src = new com.javadocmd.simplelatlng.LatLng(
//                    lat, lon);
//                com.javadocmd.simplelatlng.LatLng marsSrc = LatLngChinaTool.Baidu2Mars(src);
//                lat = (float) marsSrc.getLatitude();
//                lon = (float) marsSrc.getLongitude();
//            }
//
//            // 以下是调高德地图导航的方法
//            Intent intent = new Intent(
//            );
//            intent.setData(android.net.Uri
//                    .parse("androidamap://navi?sourceApplication=yongche&poiname=" + poiName + "&lat="
//                            + lat
//                            + "&lon="
//                            + lon + "&dev=0&style=2"));
//            intent.addCategory("android.intent.category.DEFAULT");
//            intent.setPackage("com.autonavi.minimap");
//            activity.startActivity(intent);
//
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            Toast.makeText(activity, "路线规划失败，无法完成导航。", Toast.LENGTH_SHORT).show();
//        }
//
//    }
//
//    /**
//     * 坐标转换
//     *
//     * @param location
//     * @return
//     */
//    public static com.javadocmd.simplelatlng.LatLng coordConvert(YongcheLocation location) {
//        com.javadocmd.simplelatlng.LatLng src = new com.javadocmd.simplelatlng.LatLng(
//                location.getLatitude(), location.getLongitude());
//        // 默认百度坐标系
//        com.javadocmd.simplelatlng.LatLng dest = src;
//        if (location.getCoordinateSystem() == LocationConfig.COORDINATE_WORLD) {
//            dest = LatLngChinaTool.World2Baidu(src);
//        }
//        return dest;
//    }
//
//    /**
//     * 弹出导航选择dialog
//     */
//    private static void showChoiceNaviWayDialog(final Activity activity, final int naviMode, final INavCommonData navData) {
//        final RedioAndCheckboxDialog rcd = new RedioAndCheckboxDialog(activity);
//        rcd.setCanceledOnTouchOutside(false);
//        rcd.setCancelable(false);
//        final ArrayList<String> mapApps = new ArrayList<String>();
//        mapApps.add("内置导航");
//        if (BDMapUtil.hasApp(activity, BDMapUtil.APP_BAIDU_MAP)) {
//            mapApps.add("百度地图");
//        }
//        if (BDMapUtil.hasApp(activity, BDMapUtil.APP_AMAP)) {
//            mapApps.add("高德地图");
//        }
//        String curValue = "内置导航";
//        final String[] values = new String[mapApps.size()];
//        mapApps.toArray(values);
//        rcd.setItems(values, curValue, new RedioAndCheckboxDialog.OnDlgItemClickListener() {
//            @Override
//            public void onEnsureClicked(Dialog dialog, String value, int position, boolean isChecked) {
//                dialog.dismiss();
//                if ("百度地图".equals(value)) {
//                    launchNaviBySomeWay(activity, naviMode, navData, BAIDU_NAVI_ID);
//                } else if ("高德地图".equals(value)) {
//                    launchNaviBySomeWay(activity, naviMode, navData, AMAP_NAVI_ID);
//                } else if ("内置导航".equals(value)) {
//                    launchNaviBySomeWay(activity, naviMode, navData, INNER_NAVI_ID);
//                }
//                if (isChecked) {
//                    //记住我的选择
//                    YongcheApplication.getApplication().setMyNavWay(value);
//                    YongcheApplication.getApplication().setRememberNavWay(true);
//                }
//            }
//
//            @Override
//            public void onCancleClicked(Dialog dialog) {
//                dialog.dismiss();
//            }
//        }, true).show();
//    }
//
//    private static void showChoiceNaviWayDialog1(final Activity activity, final int naviMode, final INavCommonData navData) {
//        final RedioAndCheckboxDialog rcd = new RedioAndCheckboxDialog(activity);
//        rcd.setCanceledOnTouchOutside(false);
//        rcd.setCancelable(false);
//        final ArrayList<String> mapApps = new ArrayList<String>();
//        mapApps.add("内置导航");
//        if (BDMapUtil.hasApp(activity, BDMapUtil.APP_BAIDU_MAP)) {
//            mapApps.add("百度地图");
//        }
//        if (BDMapUtil.hasApp(activity, BDMapUtil.APP_AMAP)) {
//            mapApps.add("高德地图");
//        }
//        String curValue = "内置导航";
//        final String[] values = new String[mapApps.size()];
//        mapApps.toArray(values);
//        rcd.setItems(values, curValue, new RedioAndCheckboxDialog.OnDlgItemClickListener() {
//            @Override
//            public void onEnsureClicked(Dialog dialog, String value, int position, boolean isChecked) {
//                dialog.dismiss();
//                if ("百度地图".equals(value)) {
//                    launchNaviBySomeWay(activity, naviMode, navData, BAIDU_NAVI_ID);
//                } else if ("高德地图".equals(value)) {
//                    launchNaviBySomeWay(activity, naviMode, navData, AMAP_NAVI_ID);
//                } else if ("内置导航".equals(value)) {
//                    launchNaviBySomeWay(activity, naviMode, navData, INNER_NAVI_ID);
//                }
//                if (isChecked) {
//                    //记住我的选择
//                    YongcheApplication.getApplication().setMyNavWay(value);
//                    YongcheApplication.getApplication().setRememberNavWay(true);
//                }
//            }
//
//            @Override
//            public void onCancleClicked(Dialog dialog) {
//                dialog.dismiss();
//            }
//        }, true).show();
//    }
//
//    private static int getIntLatLng(double data) {
//        int re = (int) (data * 1 * 1000 * 1000);
//        return re;
//    }
//
//    public static void showToastMsg(final Activity act, final String msg) {
//        act.runOnUiThread(new Runnable() {
//
//            @Override
//            public void run() {
//                Toast.makeText(act, msg, Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//
//
//}
