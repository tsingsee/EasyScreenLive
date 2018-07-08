package org.easydarwin.easyscreenlive.screen_live;

import android.content.Context;
import android.content.Intent;

import org.easydarwin.easyscreenlive.config.Config;

/**
 * EasyScreenLive 推送屏幕参数设置
 */

public class LiveRtspConfig {
    public LiveRtspConfig() {
    }

    /**
     * 推送设备
     * 0 - 屏幕
     * 1 - 前摄像头
     * 2 - 后摄像头
     */
    public int      pushdev      = 0;
    public boolean  isRunning    = false;
    public String   localIp      = "";
    /**
     * rtsp url 名
     */
    public String   strName      = "";
    /**
     * rtsp 端口，默认8554
     */
    public int      port         = 0;
    public String   URL          = "";

    /**
     * 是否使能音频，需动态控制音频时，这个在打开rtsp时，必须为1
     */
    public int      enableAudio  = 0;

    /**
     * 组播地址，设置为组播时生效
     */
    public String   multicastIP  = "";
    /**
     * 组播端口，设置为组播时生效
     */
    public int      multicastPort = 0;
    /**
     * 组播TTL，设置为组播时生效
     */
    public int      multicastTTL  = 7;
    /**
     * 是否开启组播标志位
     */
    public int      enableMulticast = 0;
    /**
     * 视频编码码流大小
     */
    public int      bitRate=1024;
    /**
     * 暂时未实现，设置为0
     */
    public int      enableArq=0;
    /**
     * 是否开启组播纠错，设置为组播时生效
     */
    public int      enableFec=0;
    /**
     * fec参数，开启组播纠错时生效
     */
    public int      fecGroudSize=10;
    /**
     * fec参数，开启组播纠错时生效
     */
    public int      fecParam=40;
    /**
     * 是否使用可以固定帧率方式抓屏，现在分辨率适配有些问题
     */
    public boolean  isUsedCaptureImageReader = false;
    /**
     * 推送设备为屏幕时，权限生请返回值，如推送摄像头则无效
     */
    public Intent   capScreenIntent = null;
    public int      capScreenCode    = 0;

    public void intConfig(LiveRtspConfig config) {
        this.pushdev            = config.pushdev;
        this.isRunning          = config.isRunning;
        this.localIp            = config.localIp;
        this.strName            = config.strName;
        this.port               = config.port;
        this.URL                = config.URL;

        this.enableAudio        = config.enableAudio;

        this.multicastIP        = config.multicastIP;
        this.multicastPort      = config.multicastPort;
        this.multicastTTL       = config.multicastTTL;
        this.enableMulticast    = config.enableMulticast;
        this.bitRate            = config.bitRate;
        this.enableArq          = config.enableArq;
        this.enableFec          = config.enableFec;
        this.fecGroudSize       = config.fecGroudSize;
        this.fecParam           = config.fecParam;
        isUsedCaptureImageReader= config.isUsedCaptureImageReader;
        capScreenIntent         = config.capScreenIntent;
        capScreenCode           = config.capScreenCode;

    }

    public void initLiveRtspConfig(Context context) {
        port             = Integer.parseInt(Config.getRtspPort(context));
        URL              = Config.getRtspUrl(context);
        strName          = Config.getStreamName(context);

        enableAudio      = Integer.parseInt(Config.getEnableAudio(context));

        enableMulticast  = Integer.parseInt(Config.getLiveType(context));
        multicastIP      = "239.255.42.42";
        multicastPort    = Integer.parseInt(Config.getMulPort(context));
        multicastTTL     = 7;
        bitRate          = Config.getBitRate(context)*1024;
        enableArq        = Integer.parseInt(Config.getEnableArq(context));
        enableFec        = Integer.parseInt(Config.getEnablefec(context));
        fecGroudSize     = Config.getFecGroudSize(context);
        fecParam         = Config.getFecParam(context);
        isUsedCaptureImageReader = Config.getEnableFrame(context).equals("1");
    }
}
