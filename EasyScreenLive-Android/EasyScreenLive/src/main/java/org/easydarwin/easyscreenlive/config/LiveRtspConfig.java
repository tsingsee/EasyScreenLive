package org.easydarwin.easyscreenlive.config;

import android.content.Context;

/**
 * Created by gavin on 2018/1/21.
 */

public class LiveRtspConfig {
    private LiveRtspConfig() {
    }

    static public boolean  isRunning    = false;
    static public String   localIp      = "";
    static public String   strName      = "";
    static public int      port         = 0;
    static public String   URL          = "";

    static public int      enableAudio  = 0;

    static public String   multicastIP  = "";
    static public int      multicastPort = 0;
    static public int      multicastTTL  = 7;
    static public int      enableMulticast = 0;
    static public int      bitRate=1024;
    static public int      enableArq=0;
    static public int      enableFec=0;
    static public int      fecGroudSize=10;
    static public int      fecParam=40;

    static public void initLiveRtspConfig(Context context) {
        LiveRtspConfig.port             = Integer.parseInt(Config.getRtspPort(context));
        LiveRtspConfig.URL              = Config.getRtspUrl(context);
        LiveRtspConfig.strName          = Config.getStreamName(context);

        LiveRtspConfig.enableAudio      = Integer.parseInt(Config.getEnableAudio(context));

        LiveRtspConfig.enableMulticast  = Integer.parseInt(Config.getLiveType(context));
        LiveRtspConfig.multicastIP      = "239.255.42.42";
        LiveRtspConfig.multicastPort    = Integer.parseInt(Config.getMulPort(context));
        LiveRtspConfig.multicastTTL     = 7;
        LiveRtspConfig.bitRate          = Config.getBitRate(context)*1024;
        LiveRtspConfig.enableArq        = Integer.parseInt(Config.getEnableArq(context));
        LiveRtspConfig.enableFec        = Integer.parseInt(Config.getEnablefec(context));
        LiveRtspConfig.fecGroudSize     = Config.getFecGroudSize(context);
        LiveRtspConfig.fecParam         = Config.getFecParam(context);
    }
}
