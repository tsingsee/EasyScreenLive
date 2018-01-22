package org.easydarwin.easyscreenlive;

/**
 * Created by gavin on 2018/1/20.
 */

public class OnLiveInfo {

    /**
     * cmd : 1 - 设备在线  2 - 设备共享屏幕
     * msg : 设备在线
     * URL : rtsp://192.168.1.107:8554/12345    rtsp 播放连接
     * pushType : 0                             投屏方式：0-单播  1-组播
     * devType : android                        设备类型  android/windows/mac...
     * devName : xxx的手机                      设备名称
     */
    /*
    {
        "cmd": 0,
        "msg": "设备在线",
        "URL": "rtsp://192.168.1.107:8554/12345 ",
        "pushType": 0,
        "devType":"android",
        "devName":"xxx的手机",
     }
     */

    private int cmd;
    private String msg;
    private String URL;
    private int pushType;
    private String devType;
    private String devName;
    private String srcIP;
    private long   delTime;

    static public final int INFO_CMD_ONLIVE         = 1;
    static public final int INFO_CMD_SHARED_SCREEN  = 2;

    static public final int PUSH_TYPE_UNMULTICAST   = 0;
    static public final int PUSH_TYPE_MULTICAST     = 1;
    public OnLiveInfo() {
        cmd = 0;
        msg = "";
        devName = android.os.Build.MODEL;
        pushType = PUSH_TYPE_UNMULTICAST;
        URL = "";
        devType = "Android";
        srcIP   = "";
    }

    public long getDelTime() {
        return delTime;
    }

    public void setDelTime(long delTime) {
        this.delTime = delTime;
    }

    public String getSrcIP() {
        return srcIP;
    }


    public void setSrcIP(String srcIP) {
        this.srcIP = srcIP;
    }

    public int getCmd() {
        return cmd;
    }

    public void setCmd(int cmd) {
        this.cmd = cmd;
        switch (cmd) {
            case INFO_CMD_ONLIVE:
                msg = "设备在线";
                break;
            case INFO_CMD_SHARED_SCREEN:
                msg = "设备共享屏幕";
                break;
            default:
                break;
        }
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getURL() {
        return URL;
    }

    public void setURL(String URL) {
        this.URL = URL;
    }

    public int getPushType() {
        return pushType;
    }

    public void setPushType(int pushType) {
        this.pushType = pushType;
    }

    public String getDevType() {
        return devType;
    }

    public void setDevType(String devType) {
        this.devType = devType;
    }

    public String getDevName() {
        return devName;
    }

    public void setDevName(String devName) {
        this.devName = devName;
    }
}
