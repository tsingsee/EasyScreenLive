package org.easydarwin.easyscreenlive.screen_live;

import android.content.Context;
import android.content.Intent;

import org.easydarwin.easyscreenlive.config.Config;

/**
 * Created by gavin on 2018/1/21.
 */

public class LiveRtspConfig {
    public LiveRtspConfig() {
    }
    public int      pushdev      = 0;
    public boolean  isRunning    = false;
    public String   localIp      = "";
    public String   strName      = "";
    public int      port         = 0;
    public String   URL          = "";

    public int      enableAudio  = 0;

    public String   multicastIP  = "";
    public int      multicastPort = 0;
    public int      multicastTTL  = 7;
    public int      enableMulticast = 0;
    public int      bitRate=1024;
    public int      enableArq=0;
    public int      enableFec=0;
    public int      fecGroudSize=10;
    public int      fecParam=40;
    public boolean  isUsedCaptureImageReader = false;
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
        capScreenIntent = config.capScreenIntent;
        capScreenCode   = config.capScreenCode;

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
