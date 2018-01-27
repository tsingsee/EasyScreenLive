package org.easydarwin.easyscreenlive.ui.pusher;

import android.content.Context;

import org.easydarwin.easyscreenlive.base.BasePresenter;
import org.easydarwin.easyscreenlive.base.BaseView;

/**
 * Created by gavin on 2018/1/23.
 */

public class PusherContract {
    public interface View extends BaseView<PusherContract.Presenter> {
//        void setStatusViewText(String status);
//        void setUrlViewText(String URL);
        void changeViewStatus(int status, String URL);
    }

    public interface Presenter extends BasePresenter {
        void initView(Context context);

        /**
         *
         * @param context
         * @param pushDev 0 - 屏幕  1-前摄像头 2-后置摄像头 3-停止
         */
        void onStartPush(Context context, int pushDev);
        void onViewStop();
        int  getPushStatus();

        void onStartPushSuccess(Context context, String URL);
        void onStartPushFail(Context context, int result);
        void onStopPushSuccess(Context context);
    }
}
