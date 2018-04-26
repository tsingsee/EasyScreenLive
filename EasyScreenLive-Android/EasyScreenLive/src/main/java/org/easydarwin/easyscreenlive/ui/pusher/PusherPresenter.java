package org.easydarwin.easyscreenlive.ui.pusher;

import android.content.Context;

import org.easydarwin.easyscreenlive.config.LiveRtspConfig;
import org.easydarwin.easyscreenlive.screen_live.CapScreenService;
import org.easydarwin.easyscreenlive.config.Config;
import org.easydarwin.easyscreenlive.screen_live.ScreenLiveManager;
import org.easydarwin.rtspservice.JniEasyScreenLive;

/**
 * Created by gavin on 2018/1/23.
 */

public class PusherPresenter implements PusherContract.Presenter {
    private PusherContract.View view;
    static private PusherPresenter pusherPresenter = null;

    static public PusherPresenter getInterface() {
        if (pusherPresenter == null) {
            pusherPresenter = new PusherPresenter();
        }
        return pusherPresenter;
    }

    private PusherPresenter() {
    }

    public void setPusherView(PusherContract.View view) {
        this.view = view;
        this.view.setPresenter(this);
    }

    @Override
    public void initView(Context context) {
        if (view != null && view.isActive()) {
            if (ScreenLiveManager.getPushServiceStatus() == ScreenLiveManager.EASY_PUSH_SERVICE_STATUS.STATUS_LEISURE) {
                view.changeViewStatus(getPushStatus(), "");
            } else {
                view.changeViewStatus(getPushStatus(), Config.getRtspUrl(context));
            }
        }
    }

    @Override
    public int getPushStatus() {
        return ScreenLiveManager.getPushServiceStatus();
    }

    @Override
    public void onStartPush(Context context, int pushDev) {
        if (getPushStatus() == ScreenLiveManager.EASY_PUSH_SERVICE_STATUS.STATUS_LEISURE) {
            if (pushDev == 0) {
                CapScreenService.sendCmd(CapScreenService.EASY_PUSH_SERVICE_CMD.CMD_START_PUSH_SCREEN);
            } else if (pushDev == 1){
                CapScreenService.sendCmd(CapScreenService.EASY_PUSH_SERVICE_CMD.CMD_START_PUSH_CAMREA_FRONT);
            } else if (pushDev == 2) {
                CapScreenService.sendCmd(CapScreenService.EASY_PUSH_SERVICE_CMD.CMD_START_PUSH_CAMREA_BACK);
            }
        } else {
            CapScreenService.sendCmd(CapScreenService.EASY_PUSH_SERVICE_CMD.CMD_STOP_PUSH);
        }
    }


    @Override
    public void onStartPushSuccess(Context context, String URL) {
        if(view != null && view.isActive()) {
            view.showTip("屏幕推流成功"+
                    (LiveRtspConfig.enableMulticast==1?"(组播方式) ":"（单播方式）")+"\n"
                    + "URL:"+LiveRtspConfig.URL);
            view.changeViewStatus(getPushStatus(),URL);
        }
    }

    @Override
    public void onStartPushFail(Context context, int result) {
        if(view != null && view.isActive()) {
            view.changeViewStatus(getPushStatus(),"");
            if (result == JniEasyScreenLive.EasyErrorCode.EASY_SDK_ACTIVE_FAIL) {
                view.showTip("许可证过期，屏幕推流失败");
            } else {
                view.showTip("屏幕推流失败:" + result + ", RTSP端口号默认加1");
            }
            view.changeViewStatus(getPushStatus(),"");
        }
    }

    @Override
    public void onStopPushSuccess(Context context) {
        if(view != null && view.isActive()) {
            view.changeViewStatus(getPushStatus(),"");
        }
    }

    @Override
    public void onViewStop() {
        if (ScreenLiveManager.getPushServiceStatus() == ScreenLiveManager.EASY_PUSH_SERVICE_STATUS.STATUS_PUSH_CAMREA_BACK
                || ScreenLiveManager.getPushServiceStatus() == ScreenLiveManager.EASY_PUSH_SERVICE_STATUS.STATUS_PUSH_CAMREA_FRONT) {
            CapScreenService.sendCmd(CapScreenService.EASY_PUSH_SERVICE_CMD.CMD_STOP_PUSH);
        }
    }
}
