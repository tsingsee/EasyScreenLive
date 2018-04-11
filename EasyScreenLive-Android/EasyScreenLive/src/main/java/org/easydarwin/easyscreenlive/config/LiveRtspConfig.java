package org.easydarwin.easyscreenlive.config;

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
    static public int      enableFec=0;
    static public int      fecParam=40;
}
