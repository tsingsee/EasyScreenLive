package org.easydarwin.easyscreenlive.config;

import org.easydarwin.easyscreenlive.util.Util;

/**
 * Created by gavin on 2018/1/21.
 */

public class LiveRtspConfig {
    public boolean  isRunning;
    public String   localIp;
    public String   strName;
    public int      port;
    public String   URL;

    public String   multicastIP;
    public int      multicastPort;
    public int      multicastTTL;
    public int      enableMulticast;

    public LiveRtspConfig(){
       isRunning        = false;
       enableMulticast  = 0;
       multicastIP      = "";
       localIp          = "";
       port             = 8554;
       multicastPort    = 0;
       URL              = "";
       strName          = "";
       multicastTTL     = 7;
    }
}
