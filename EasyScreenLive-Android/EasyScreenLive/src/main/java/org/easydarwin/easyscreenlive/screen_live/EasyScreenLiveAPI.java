package org.easydarwin.easyscreenlive.screen_live;

import android.os.Message;
import android.view.SurfaceView;

/**
 * Created by gavin on 2018/7/8.
 */

public class EasyScreenLiveAPI {

    static public class EASY_PUSH_SERVICE_STATUS {
        static public final int STATUS_LEISURE = 0; //服务空闲
        static public final int STATUS_PUSH_SCREEN = 1; //服务推屏
        static public final int STATUS_PUSH_CAMREA_BACK = 2; //服务推后摄像头
        static public final int STATUS_PUSH_CAMREA_FRONT = 3; //服务推前摄像头
    }

    static public LiveRtspConfig liveRtspConfig = new LiveRtspConfig();

    private EasyScreenLiveAPI() {
    }


    static public int startPush(LiveRtspConfig config, SurfaceView mSurfaceView) {
        liveRtspConfig.intConfig(config);
        if (getPushStatus() == EasyScreenLiveAPI.EASY_PUSH_SERVICE_STATUS.STATUS_LEISURE) {
            if (liveRtspConfig.pushdev == 0 || liveRtspConfig.pushdev == 1) {
                CapScreenService.sendCmd(CapScreenService.EASY_PUSH_SERVICE_CMD.CMD_START_PUSH_SCREEN);
            } else if (liveRtspConfig.pushdev == 2) {
                Message msg = new Message();
                msg.what = CapScreenService.EASY_PUSH_SERVICE_CMD.CMD_START_PUSH_CAMREA_FRONT;
                msg.obj = mSurfaceView;
                CapScreenService.sendMsg(msg);
            } else if (liveRtspConfig.pushdev == 3) {
                Message msg = new Message();
                msg.what = CapScreenService.EASY_PUSH_SERVICE_CMD.CMD_START_PUSH_CAMREA_BACK;
                msg.obj = mSurfaceView;
                CapScreenService.sendMsg(msg);
            }
        } else {
            CapScreenService.sendCmd(CapScreenService.EASY_PUSH_SERVICE_CMD.CMD_STOP_PUSH);
        }
        return 0;
    }

    static public int stopPush() {
        if (ScreenLiveManager.getPushServiceStatus() == EasyScreenLiveAPI.EASY_PUSH_SERVICE_STATUS.STATUS_PUSH_CAMREA_BACK
                || ScreenLiveManager.getPushServiceStatus() == EasyScreenLiveAPI.EASY_PUSH_SERVICE_STATUS.STATUS_PUSH_CAMREA_FRONT) {
            CapScreenService.sendCmd(CapScreenService.EASY_PUSH_SERVICE_CMD.CMD_STOP_PUSH);
        }
        return 0;
    }

    static public int getPushStatus() {
        return ScreenLiveManager.getPushServiceStatus();
    }

    static public void setEnablePushAudio(boolean isEnable)
    {
        if(isEnable)
            CapScreenService.sendCmd(CapScreenService.EASY_PUSH_SERVICE_CMD.CMD_START_PUSH_AUDIO);
        else
            CapScreenService.sendCmd(CapScreenService.EASY_PUSH_SERVICE_CMD.CMD_STOP_PUSH_AUDIO);
    }
}