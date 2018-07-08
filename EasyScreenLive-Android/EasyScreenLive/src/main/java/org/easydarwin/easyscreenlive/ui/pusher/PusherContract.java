package org.easydarwin.easyscreenlive.ui.pusher;

import android.content.Context;
import android.content.Intent;
import android.view.Surface;
import android.view.SurfaceView;

import org.easydarwin.easyscreenlive.ui.base.BasePresenter;
import org.easydarwin.easyscreenlive.ui.base.BaseView;

/**
 * Created by gavin on 2018/1/23.
 */

public class PusherContract {
    public interface View extends BaseView<PusherContract.Presenter> {
        void changeViewStatus(int status, String URL);
    }

    public interface Presenter extends BasePresenter {
        void initView(Context context);

        /**
         *
         * @param context
         * @param pushDev 0 - 屏幕  1-前摄像头 2-后置摄像头 3-停止
         */
        void onStartPush(Context context, int pushDev,Intent capScreenIntent,
                         int capScreenCode, SurfaceView mSurfaceView);
        void onStopPush();
        int  getPushStatus();

        void onStartPushSuccess(Context context,int isEnableMulticast, String URL);
        void onStartPushFail(Context context, int result);
        void onStopPushSuccess(Context context);
    }
}
