package org.easydarwin.easyscreenlive.screen_live;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;


/**
 *
 */
public class CapScreenService extends Service{

    private final static String TAG = "CapScreenService";
    public Context mContext;

    public static ServiceCommondHandle serviceCommondHandle = null;
    OnLiveManager onLiveManager = null;
    ScreenLiveManager screenLiveManager = null;


    public static class EASY_PUSH_SERVICE_CMD {
        static public final int CMD_STOP_PUSH               = 0;
        static public final int CMD_START_PUSH_SCREEN       = 1;
        static public final int CMD_START_PUSH_CAMREA_BACK  = 2;
        static public final int CMD_START_PUSH_CAMREA_FRONT = 3;
        static public final int CMD_UPDATA_ONLIVE_LIST      = 4;

    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("0",
                    "EasyScreenLive",
                    NotificationManager.IMPORTANCE_DEFAULT);
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);

            Notification notification = new NotificationCompat.Builder(this)
                    .setContentTitle("EasyScreenLive")
                    .setContentText("EasyScreenLive").build();
            startForeground(1, notification);
        }


        serviceCommondHandle = new ServiceCommondHandle();
        mContext = this;
        if (onLiveManager == null) {
            onLiveManager = new OnLiveManager(this);
            onLiveManager.create();
            serviceCommondHandle.sendEmptyMessageAtTime(EASY_PUSH_SERVICE_CMD.CMD_UPDATA_ONLIVE_LIST, 0);
        }

        if (screenLiveManager == null) {
            screenLiveManager = new ScreenLiveManager(this);
        }
    }

    static public void sendCmd(int cmd) {
        if (serviceCommondHandle != null) {
            serviceCommondHandle.sendEmptyMessage(cmd);
        }
    }

    public void clearHandleMessage() {
        if(serviceCommondHandle != null){
            for (int i=0; i<100; i++) {
                serviceCommondHandle.removeMessages(i);
            }
        }
    }


    public class ServiceCommondHandle extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what)
            {
                case EASY_PUSH_SERVICE_CMD.CMD_START_PUSH_CAMREA_BACK:
                case EASY_PUSH_SERVICE_CMD.CMD_START_PUSH_CAMREA_FRONT:
                case EASY_PUSH_SERVICE_CMD.CMD_START_PUSH_SCREEN:
                case EASY_PUSH_SERVICE_CMD.CMD_STOP_PUSH:{
                    if (screenLiveManager != null) {
                        screenLiveManager.onScreenLiveCmd(msg);
                    }
                }
                break;
                case EASY_PUSH_SERVICE_CMD.CMD_UPDATA_ONLIVE_LIST:
                    onLiveManager.onTimerUpdateOnliveListView();
                    sendEmptyMessageDelayed(EASY_PUSH_SERVICE_CMD.CMD_UPDATA_ONLIVE_LIST, 1000);
                    break;
                default:
                    break;
            }
            clearHandleMessage();
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return  super.onStartCommand(intent, flags, startId);
    }

    public void onDestroy() {
        Log.e(TAG, "onDestroy");
        Toast.makeText(mContext, "结束推流", Toast.LENGTH_SHORT).show();
        serviceCommondHandle = null;

        if (onLiveManager != null) {
            onLiveManager.destory();
            onLiveManager = null;
        }
        if (screenLiveManager != null) {
            screenLiveManager.destory();
            screenLiveManager = null;
        }
        super.onDestroy();
    }







}//end
