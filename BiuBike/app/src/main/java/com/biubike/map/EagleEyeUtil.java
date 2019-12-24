package com.biubike.map;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.PowerManager;

import com.baidu.trace.LBSTraceClient;
import com.baidu.trace.Trace;
import com.baidu.trace.api.entity.LocRequest;
import com.baidu.trace.api.track.DistanceRequest;
import com.baidu.trace.api.track.DistanceResponse;
import com.baidu.trace.api.track.HistoryTrackRequest;
import com.baidu.trace.api.track.HistoryTrackResponse;
import com.baidu.trace.api.track.OnTrackListener;
import com.baidu.trace.api.track.SupplementMode;
import com.baidu.trace.model.BaseRequest;
import com.baidu.trace.model.ProcessOption;
import com.baidu.trace.model.StatusCodes;
import com.baidu.trace.model.TransportMode;
import com.biubike.receiver.TrackReceiver;
import com.biubike.util.ContextUtil;

import java.util.concurrent.atomic.AtomicInteger;

import static android.content.Context.MODE_PRIVATE;

public class EagleEyeUtil {

    // 轨迹服务ID
    public long serviceId = 217606;
    // 设备标识
    public String entityName = "gaolei";
    public AtomicInteger mSequenceGenerator = new AtomicInteger();
    public LocRequest locRequest = null;
    public SharedPreferences trackConf = null;
    private PowerManager powerManager = null;

    private PowerManager.WakeLock wakeLock = null;
    // 定位周期(单位:秒)
    public int gatherInterval = 2;
    // 打包回传周期(单位:秒)
    public int packInterval = 4;
    /**
     * 轨迹服务
     */
    public Trace mTrace = null;
    private TrackReceiver trackReceiver = null;
    /**
     * 服务是否开启标识
     */
    public boolean isTraceStarted = false;

    /**
     * 采集是否开启标识
     */
    public boolean isGatherStarted = false;
    public boolean isRegisterReceiver = false;

    private EagleEyeUtil() {
        locRequest = new LocRequest(serviceId);
        powerManager = (PowerManager) ContextUtil.getAppContext().getSystemService(Context.POWER_SERVICE);

        trackConf = ContextUtil.getApp().getSharedPreferences("track_conf", MODE_PRIVATE);
        initTrace();
    }

    public static class InstanceHolder {
        static EagleEyeUtil instance = new EagleEyeUtil();
    }

    public static EagleEyeUtil get() {

        return InstanceHolder.instance;

    }

    public LBSTraceClient mTraceClient;


    /*
     简介

     鹰眼为每一个应用提供1000万终端以上轨迹管理能力，并且支持全球轨迹追踪。
     鹰眼提供多种API和SDK供开发者从各种终端追踪轨迹，如：手机、GPS定位器、智能后视镜等硬件设备。此外，鹰眼也支持开发者从服务端上传轨迹。
     鹰眼为开发者免费存储最近1年的轨迹数据。鹰眼采用多机房多实例分布式存储，并定期自动备份，保障数据存储安全。
     */

    public void initTrace() {


        // 是否需要对象存储服务，默认为：false，关闭对象存储服务。注：鹰眼 Android SDK v3.0以上版本支持随轨迹上传图像等对象数据，若需使用此功能，该参数需设为 true，且需导入bos-android-sdk-1.0.2.jar。
        boolean isNeedObjectStorage = false;
        // 初始化轨迹服务
        mTrace = new Trace(serviceId, entityName, isNeedObjectStorage);
        // 初始化轨迹服务客户端
        mTraceClient = new LBSTraceClient(ContextUtil.getAppContext());

        // 设置定位和打包周期
        mTraceClient.setInterval(gatherInterval, packInterval);


    }

   /*
    简介

    轨迹查询：利用鹰眼高性能轨迹查询服务，开发者实时查询任意时段的轨迹。
    实时查询是指：轨迹点一旦成功上传到鹰眼云端，在小于100毫秒的时间内，即可通过接口查询到该轨迹点。
    鹰眼还提供轨迹批量导出功能，供开发者下载历史轨迹数据。
    轨迹纠偏：为纠正轨迹漂移，鹰眼提供专业的轨迹纠偏绑路。利用该服务，开发者可纠正轨迹漂移，展示平滑连贯的轨迹。
    鹰眼分别针对驾车、骑行、步行提供了不同的轨迹纠偏绑路算法，适用于多种交通工具的轨迹校正。
    */

    public void getTraceQuery(long startTime,long endTime) {
        // 请求标识
        int tag = 1;

        // 创建历史轨迹请求实例
        HistoryTrackRequest historyTrackRequest = new HistoryTrackRequest(tag, serviceId, entityName);
//        // 开始时间2017-4-17 0:0:0的UNIX 时间戳
//        long startTime = 1492358400;
//        // 结束时间2017-4-17 23:59:59的UNIX 时间戳
//        long endTime = 1492444799;
        // 设置开始时间
        historyTrackRequest.setStartTime(startTime);
        // 设置结束时间
        historyTrackRequest.setEndTime(endTime);
        // 设置需要纠偏
        historyTrackRequest.setProcessed(true);
        // 创建纠偏选项实例
        ProcessOption processOption = new ProcessOption();
        // 设置需要去噪
        processOption.setNeedDenoise(true);
        // 设置需要抽稀
        processOption.setNeedVacuate(true);
        // 设置需要绑路
        processOption.setNeedMapMatch(true);
        // 设置精度过滤值(定位精度大于100米的过滤掉)
        processOption.setRadiusThreshold(100);
        // 设置交通方式为驾车
        processOption.setTransportMode(TransportMode.riding);
        // 设置纠偏选项
        historyTrackRequest.setProcessOption(processOption);
        // 设置里程填充方式为驾车
        historyTrackRequest.setSupplementMode(SupplementMode.riding);
        // 初始化轨迹监听器
        OnTrackListener mTrackListener = new OnTrackListener() {
            @Override
            public void onHistoryTrackCallback(HistoryTrackResponse response) {
                // 历史轨迹回调
            }
        };
        // 查询轨迹
        mTraceClient.queryHistoryTrack(historyTrackRequest, mTrackListener);
    }

    /*
        简介

        鹰眼Android SDK提供了queryDistance()方法，用于计算指定时间段内的轨迹里程，
        支持：计算纠偏后的里程，用路线规划补偿中断轨迹的里程。
        */
    public void getTraceDistance(OnTrackListener mTrackListener,long startTime,long endTime) {
        // 请求标识
        int tag = 2;

        // 创建里程查询请求实例
        DistanceRequest distanceRequest = new DistanceRequest(tag, serviceId, entityName);
//        // 开始时间(单位：秒)
//        long startTime = System.currentTimeMillis() / 1000 - 12 * 60 * 60;
//        // 结束时间(单位：秒)
//        long endTime = System.currentTimeMillis() / 1000;
        // 设置开始时间
        distanceRequest.setStartTime(startTime);
        // 设置结束时间
        distanceRequest.setEndTime(endTime);
        // 设置需要纠偏
        distanceRequest.setProcessed(true);
        // 创建纠偏选项实例
        ProcessOption processOption = new ProcessOption();
        // 设置需要去噪
        processOption.setNeedDenoise(true);
        // 设置需要绑路
        processOption.setNeedMapMatch(true);
        // 设置交通方式为驾车
        processOption.setTransportMode(TransportMode.riding);
        // 设置纠偏选项
        distanceRequest.setProcessOption(processOption);
        // 设置里程填充方式为驾车
        distanceRequest.setSupplementMode(SupplementMode.riding);
//        // 初始化轨迹监听器
//        OnTrackListener mTrackListener = new OnTrackListener() {
//            @Override
//            public void onDistanceCallback(DistanceResponse response) {
//                // 里程回调
//            }
//        };
        // 查询里程
        mTraceClient.queryDistance(distanceRequest, mTrackListener);
    }


    /**
     * 初始化请求公共参数
     *
     * @param request
     */
    public void initRequest(BaseRequest request) {
        request.setTag(getTag());
        request.setServiceId(serviceId);
    }

    /**
     * 获取请求标识
     *
     * @return
     */
    public int getTag() {
        return mSequenceGenerator.incrementAndGet();
    }

    /**
     * 注册广播（电源锁、GPS状态）
     */
    public void registerReceiver() {
        if (isRegisterReceiver) {
            return;
        }

        if (null == wakeLock) {
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "track upload");
        }
        if (null == trackReceiver) {
            trackReceiver = new TrackReceiver(wakeLock);
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        filter.addAction(StatusCodes.GPS_STATUS_ACTION);
        ContextUtil.getAppContext().registerReceiver(trackReceiver, filter);
        isRegisterReceiver = true;

    }

    public void unregisterPowerReceiver() {
        if (!isRegisterReceiver) {
            return;
        }
        if (null != trackReceiver) {
            ContextUtil.getAppContext().unregisterReceiver(trackReceiver);
        }
        isRegisterReceiver = false;
    }
}
