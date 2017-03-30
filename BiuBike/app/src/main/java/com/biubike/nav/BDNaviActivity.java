//package com.biubike.nav;
//
//import android.content.Context;
//import android.content.Intent;
//import android.database.ContentObserver;
//import android.database.Cursor;
//import android.net.Uri;
//import android.os.Build;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.Message;
//import android.text.TextUtils;
//import android.util.Log;
//import android.view.View;
//import android.view.ViewStub;
//import android.view.WindowManager;
//import android.widget.FrameLayout;
//import android.widget.PopupWindow;
//import android.widget.TextView;
//
//import com.baidu.navisdk.adapter.BNRouteGuideManager;
//import com.baidu.navisdk.adapter.BNRoutePlanNode;
//import com.baidu.navisdk.adapter.BNaviBaseCallbackModel;
//import com.baidu.navisdk.adapter.BaiduNaviCommonModule;
//import com.baidu.navisdk.adapter.NaviModuleFactory;
//import com.baidu.navisdk.adapter.NaviModuleImpl;
//import com.yongche.NewBaseActivity;
//import com.yongche.R;
//import com.yongche.YongcheConfig;
//import com.yongche.data.ChatColumn;
//import com.yongche.data.OrderColumn;
//import com.yongche.data.YongcheProviderData;
//import com.yongche.libs.utils.NetUtil;
//import com.yongche.libs.utils.StringUtil;
//import com.yongche.libs.utils.YongcheProgress;
//import com.yongche.libs.utils.log.Logger;
//import com.yongche.model.OrderEntry;
//import com.yongche.navigation.BNavigatorSearchPopwindow;
//import com.yongche.navigation.SearchAddress;
//import com.yongche.net.service.CommonService;
//import com.yongche.ui.ShowBDMapActivity;
//import com.yongche.ui.chat.ChatEntity;
//
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
///**
// * 导航页面
// *
// * Created by chenxin on 16/6/2.
// */
//public class BDNaviActivity extends NewBaseActivity implements View.OnClickListener {
//    private static String TAG = BDNaviActivity.class.getSimpleName();
//    private Context mContext;
//
//    /**
//     * 算路节点
//     */
//    private BNRoutePlanNode mBNRoutePlanNode = null;
//
//    /**
//     * 导航模块生命周期
//     */
//    private BaiduNaviCommonModule mBaiduNaviCommonModule = null;
//
//    /*
//     * 对于导航模块有两种方式来实现发起导航。 1：使用通用接口来实现 2：使用传统接口来实现
//     *
//     * 是否使用通用接口
//     */
//    private boolean useCommonInterface = true;
//
//
//    /**
//     * 是否正在导航
//     */
//    public static boolean isInNaviMode = false;
//
//    public static final String EXTRA_NAVI_MODE = "navimode";
//    public static final String EXTRA_ORDER = "order";
//    /**
//     * 更新 聊天界面 布局红点
//     */
//    private final int MSG_UPDATE_CHAT_VIEW = 0x004;
//    private int navimode = ShowBDMapActivity.NAVI_MODE_TO_END_POINT;
//    private int person_chat = 0;
//    private OrderEntry order;
//    private ChatObserver mChatObserver;
//
//    private FrameLayout map_content;
//    private TextView tvPassengerName, tvStartAddress, tvChat, tvCall;
//    private TextView btnModifiAdd, tvAddress;
//    /**
//     * 搜索结果显示window
//     */
//    private BNavigatorSearchPopwindow mPop;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        mContext = this;
//        isInNaviMode = true;
//        navimode = getIntent().getIntExtra(EXTRA_NAVI_MODE, ShowBDMapActivity.NAVI_MODE_TO_END_POINT);
//        order = (OrderEntry) getIntent().getSerializableExtra(EXTRA_ORDER);
//        super.onCreate(savedInstanceState);
//    }
//
//    @Override
//    protected void onNewIntent(Intent intent) {
//        super.onNewIntent(intent);
//        mContext = this;
//        isInNaviMode = true;
//        navimode = getIntent().getIntExtra(EXTRA_NAVI_MODE, ShowBDMapActivity.NAVI_MODE_TO_END_POINT);
//        order = (OrderEntry) getIntent().getSerializableExtra(EXTRA_ORDER);
//
//        if (tvAddress!=null){
//            tvAddress.setText(StringUtil.subString(order.getPosition_end()));
//        }
//
//    }
//    @Override
//    public void setContentView() {
//        isInNaviMode = true;
//        setContentView(R.layout.activity_inner_navi);
//    }
//
//    @Override
//    public void initTitle() {
//        btnBack.setOnClickListener(this);
//        tvTitle.setText(getString(R.string.navagation_title));
//    }
//
//    @Override
//    public void initView() {
//        initTopCustomView();
//        map_content = (FrameLayout) findViewById(R.id.map_content);
//        init();
//    }
//
//
//
//    /**
//     * 初始化顶部栏
//     */
//    private void initTopCustomView() {
//        if (navimode == ShowBDMapActivity.NAVI_MODE_TO_START_POINT) {
//            ViewStub viewSub = (ViewStub) findViewById(R.id.vs_navi2origin);
//            viewSub.inflate();
//            tvPassengerName = (TextView) findViewById(R.id.tv_passenger_name);
//            tvStartAddress = (TextView) findViewById(R.id.tv_start_address);
//            tvChat = (TextView) findViewById(R.id.chat);
//            tvCall = (TextView) findViewById(R.id.call);
//
//            String passenger_name = order.getPassenger_name();
//            tvPassengerName.setText(!TextUtils.isEmpty(passenger_name)?passenger_name : "null");
//            tvStartAddress.setText(StringUtil.subString(order.getPosition_start()));
//            tvChat.setOnClickListener(this);
//            tvCall.setOnClickListener(this);
//
//            mChatObserver = new ChatObserver(null);
//            getContentResolver().registerContentObserver(YongcheConfig.CHAT_URI, true, mChatObserver);
//
//        } else {
//            ViewStub viewSub = (ViewStub) findViewById(R.id.vs_navi2terminal);
//            viewSub.inflate();
//            btnModifiAdd = (TextView) findViewById(R.id.btn_modifi_address);
//            tvAddress = (TextView) findViewById(R.id.tv_address);
//
//            btnModifiAdd.setOnClickListener(this);
//            tvAddress.setText(StringUtil.subString(order.getPosition_end()));
//        }
//    }
//
//    /**
//     * 导航模块初始化
//     */
//    private void init() {
//        createHandler();
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
//        }
//
//        View view = null;
//        if (useCommonInterface) {
//            //使用通用接口
//            mBaiduNaviCommonModule = NaviModuleFactory.getNaviModuleManager().getNaviCommonModule(
//                    NaviModuleImpl.BNaviCommonModuleConstants.ROUTE_GUIDE_MODULE, this,
//                    BNaviBaseCallbackModel.BNaviBaseCallbackConstants.CALLBACK_ROUTEGUIDE_TYPE, mOnNavigationListener);
//            if(mBaiduNaviCommonModule != null) {
//                mBaiduNaviCommonModule.onCreate();
//                // view 获取导航过程View对象，将其加入应用布局内即可显示导航过程。
//                view = mBaiduNaviCommonModule.getView();
//            }
//
//        } else {
//            /*
//             * BNRouteGuideManager 导航管理类。
//             * 使用传统接口
//             * view 获取导航过程View对象，将其加入应用布局内即可显示导航过程。
//             */
//            view = BNRouteGuideManager.getInstance().onCreate(this,mOnNavigationListener);
//        }
//
//        if (view != null) {
//            map_content.addView(view);
//
//        }
//
//        Intent intent = getIntent();
//        if (intent != null) {
//            Bundle bundle = intent.getExtras();
//            if (bundle != null) {
//                mBNRoutePlanNode = (BNRoutePlanNode) bundle.getSerializable(NavUtil.ROUTE_PLAN_NODE);
//            }
//        }
//    }
//
//    private static final int MSG_SHOW = 1;
//    private static final int MSG_HIDE = 2;
//    private static final int MSG_RESET_NODE = 3;
//    private Handler hd = null;
//
//    private void createHandler() {
//        if (hd == null) {
//            hd = new Handler(getMainLooper()) {
//                public void handleMessage(android.os.Message msg) {
//                    switch (msg.what) {
//                        case MSG_SHOW:
//                            addCustomizedLayerItems();
//                            break;
//
//                        case MSG_HIDE:
//                            BNRouteGuideManager.getInstance().showCustomizedLayer(false);
//                            break;
//
//                        /*
//                         * 导航中重新设置终点并直接算路导航
//                         */
//                        case MSG_RESET_NODE:
////                            BNRouteGuideManager.getInstance().resetEndNodeInNavi(
////                                new BNRoutePlanNode(116.21142, 40.85087, "百度大厦11", null, BNRoutePlanNode.CoordinateType.GCJ02));
//                            break;
//
//                        case MSG_UPDATE_CHAT_VIEW:
//                            if(null != tvChat) {
//                                if (person_chat > 0) {
//                                    tvChat.setBackgroundDrawable(getResources().getDrawable(R.drawable.xml_bg_btn_contact_passenger_point));
//                                } else {
//                                    tvChat.setBackgroundDrawable(getResources().getDrawable(R.drawable.xml_bg_btn_contact_passenger_normal));
//                                }
//                            }
//                            break;
//                    }
//                }
//            };
//        }
//    }
//
//    /**
//     * 添加自定义图层
//     */
//    private void addCustomizedLayerItems() {
//        /*
//         *  CustomizedLayerItem
//         *  自定义图层元素类
//         */
//        List<BNRouteGuideManager.CustomizedLayerItem> items = new ArrayList<BNRouteGuideManager.CustomizedLayerItem>();
//        BNRouteGuideManager.CustomizedLayerItem item1 = null;
//        if (mBNRoutePlanNode != null) {
//            /*
//             * 参数：  经度，纬度，坐标类型，自定义图片，对齐方式
//             */
//            item1 = new BNRouteGuideManager.CustomizedLayerItem(
//                    mBNRoutePlanNode.getLongitude(),
//                    mBNRoutePlanNode.getLatitude(),
//                    mBNRoutePlanNode.getCoordinateType(),
//                    getResources().getDrawable(R.mipmap.ic_launcher),
//                    BNRouteGuideManager.CustomizedLayerItem.ALIGN_CENTER);
//            items.add(item1);
//
//            BNRouteGuideManager.getInstance().setCustomizedLayerItems(items);
//        }
//        // 是否显示自定义图层
//        BNRouteGuideManager.getInstance().showCustomizedLayer(true);
//    }
//
//    /**
//     * 导航过程监听器
//     */
//    private BNRouteGuideManager.OnNavigationListener mOnNavigationListener = new BNRouteGuideManager.OnNavigationListener() {
//
//        /*
//         * 导航过程结束
//         */
//        @Override
//        public void onNaviGuideEnd() {
//            finish();
//        }
//
//        /*
//         * 通用动作对调接口
//         */
//        @Override
//        public void notifyOtherAction(int actionType, int arg1, int arg2, Object obj) {
//
//            // 0 表示到达目的地，导航自动退出的标识。
//            if (actionType == 0) {
//                Log.i(TAG, "notifyOtherAction actionType = " + actionType + ",导航到达目的地！");
//            }
//            Log.i(TAG, "actionType:" + actionType + "arg1:" + arg1 + "arg2:" + arg2 + "obj:" + obj!=null?obj.toString():"null");
//        }
//
//    };
//
//
//
//    /**
//     * 导航生命周期管理
//     */
//    @Override
//    protected void onResume() {
//        super.onResume();
//
//        checkoutSqlNum();
//
//        if(useCommonInterface) {
//            if(mBaiduNaviCommonModule != null) {
//                mBaiduNaviCommonModule.onResume();
//            }
//        } else {
//            BNRouteGuideManager.getInstance().onResume();
//        }
//
//        if (hd != null) {
//            hd.sendEmptyMessageAtTime(MSG_SHOW, 2000);
//        }
//    }
//
//    protected void onPause() {
//        super.onPause();
//
//        if(useCommonInterface) {
//            if(mBaiduNaviCommonModule != null) {
//                mBaiduNaviCommonModule.onPause();
//            }
//        } else {
//            BNRouteGuideManager.getInstance().onPause();
//        }
//
//    };
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        if(useCommonInterface) {
//            if(mBaiduNaviCommonModule != null) {
//                mBaiduNaviCommonModule.onDestroy();
//            }
//        } else {
//            BNRouteGuideManager.getInstance().onDestroy();
//        }
//        // BaiduNavActivity.activityList.remove(this);
//        isInNaviMode = false;
//        if(null != mPop && mPop.isShowing()) {
//            mPop.dismiss();
//            mPop = null;
//        }
//    }
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//        if(useCommonInterface) {
//            if(mBaiduNaviCommonModule != null) {
//                mBaiduNaviCommonModule.onStop();
//            }
//        } else {
//            BNRouteGuideManager.getInstance().onStop();
//        }
//
//    }
//
//    @Override
//    public void onBackPressed() {
//        if(useCommonInterface) {
//            if(mBaiduNaviCommonModule != null) {
//                mBaiduNaviCommonModule.onBackPressed(false);
//            }
//        } else {
//            BNRouteGuideManager.getInstance().onBackPressed(false);
//        }
//    }
//
//    public void onConfigurationChanged(android.content.res.Configuration newConfig) {
//        super.onConfigurationChanged(newConfig);
//        if(useCommonInterface) {
//            if(mBaiduNaviCommonModule != null) {
//                mBaiduNaviCommonModule.onConfigurationChanged(newConfig);
//            }
//        } else {
//            BNRouteGuideManager.getInstance().onConfigurationChanged(newConfig);
//        }
//
//    };
//
//    @Override
//    public void onClick(View v) {
//        switch (v.getId()) {
//            case R.id.back:
//                onBackPressed();
//                break;
//            case R.id.chat:
//                chatWithPassenger();
//                break;
//
//            case R.id.call:
//                Intent intentCall = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + order.getPassenger_contact()));
//                startActivity(intentCall);
//                break;
//
//            case R.id.btn_modifi_address:
//                mPop = new BNavigatorSearchPopwindow(this, new BNavigatorSearchPopwindow.SearchCallBack() {
//                    @Override
//                    public void setResult(SearchAddress address) {
//                        order.setPosition_end_lat(address.getLatitude());
//                        order.setPosition_end_lng(address.getLongitude());
//                        order.setPosition_end(address.getName());
//                        //TODO 待处理
//                        YongcheProgress.showProgress(BDNaviActivity.this,"正在重新规划路线");
//                        NavUtil.launchInnerNavis(BDNaviActivity.this,navimode,order);
//                    }
//                });
//                mPop.setSoftInputMode(PopupWindow.INPUT_METHOD_NEEDED);
//                mPop.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
//                mPop.show();
//                break;
//        }
//    }
//
//    /**
//     * 聊天数据库监听
//     */
//    private class ChatObserver extends ContentObserver {
//
//        public ChatObserver(Handler handler) {
//            super(handler);
//        }
//
//        @Override
//        public void onChange(boolean selfChange) {
//            super.onChange(selfChange);
//            checkoutSqlNum();
//        }
//    }
//
//    /**
//     * 与乘客聊天
//     */
//    public void chatWithPassenger() {
//        // 如果数据库没有 会话id 先获取
//        if (NetUtil.isNetConnected(this)) {
//            if (order != null && TextUtils.isEmpty(order.getChat_id_passengers())) {
//                YongcheProgress.showProgress(this, "正在为您连接乘客...");
//                getChatId(0, order.getId());
//                return;
//            }
//        }
//        initChatActivity(0);
//    }
//
//    /**
//     * 获取与乘客聊天的会话id
//     * @param is_crm
//     * @param order_id
//     */
//    private void getChatId(final int is_crm, final long order_id) {
//        Map<String, Object> params = new HashMap<String, Object>();
//        params.put("service_order_id", order_id);
//        params.put("is_crm", 0);
//        CommonService.ICommonGetCallback callback = new CommonService.ICommonGetCallback() {
//
//            @Override
//            public void onCommonGetSuccess(JSONObject obj) {
//                try {
//                    Logger.d(TAG, obj.toString());
//                    int code = obj.isNull("code") ? 0 : obj.getInt("code");
//                    if (code == 200) {
//                        YongcheProgress.closeProgress();
//                        String chatID_person = obj.isNull("msg") ? "" : obj.getString("msg");
//                        YongcheProviderData.getInStance(BDNaviActivity.this).upDateOrderEntryForChat(OrderColumn.CHAT_ID_PASSENGERS, chatID_person, order_id);
//                        order.setChat_id_passengers(chatID_person);
//                        initChatActivity(is_crm);
//                    } else {
//                        YongcheProgress.closeProgress();
//                    }
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                    YongcheProgress.closeProgress();
//                }
//            }
//
//            @Override
//            public void onCommonGetFail(int errorCode, String errorMsg) {
//                YongcheProgress.closeProgress();
//            }
//        };
//
//        CommonService service = new CommonService(this, callback, CommonService.REQUEST_GET_TYPE);
//        service.setRequestParams(YongcheConfig.URL_GET_SESSION, params);
//        service.execute("");
//    }
//
//    /**
//     * 1-客服 0-乘客
//     */
//    private void initChatActivity(int is_crm) {
//        Bundle bundle = new Bundle();
//        bundle.putSerializable("order", order);
//        bundle.putInt("is_crm", is_crm);
//        startActivity(YongcheConfig.ACTION_CHAT, bundle);
//    }
//
//    /**
//     * 查询数据库聊天记录
//     */
//    private void checkoutSqlNum() {
//        person_chat = 0;
//        new Thread() {
//            Cursor cursor;
//
//            @Override
//            public void run() {
//                try {
//                    cursor = mContext.getContentResolver()
//                            .query(YongcheConfig.CHAT_URI,
//                                    null,
//                                    ChatColumn.CHAT_ORDER_ID + " = "
//                                            + order.getId() + " AND "
//                                            + ChatColumn.CHAT_READ_STATE
//                                            + " = "
//                                            + ChatEntity.READ_STATE_READ_NO,
//                                    null, null);
//                    if (cursor.getCount() > 0) {
//                        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor
//                                .moveToNext()) {
//                            ChatEntity chatEntity = ChatEntity.parseCursor(
//                                    cursor, 480);
//                            if (chatEntity.getChat_object() == 1) {
//                                person_chat++;
//                            } else if (chatEntity.getChat_object() == 3) {
//                            }
//                        }
//                    }
//
//                    Message message = new Message();
//                    message.what = MSG_UPDATE_CHAT_VIEW;
//                    hd.sendMessage(message);
//
//                } catch (Exception e) {
//                    e.printStackTrace();
//                } finally {
//                    if (cursor != null) {
//                        cursor.close();
//                    }
//                }
//            }
//        }.start();
//    }
//
//}
