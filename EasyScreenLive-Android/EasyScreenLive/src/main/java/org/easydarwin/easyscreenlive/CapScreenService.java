package org.easydarwin.easyscreenlive;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import org.easydarwin.easyscreenlive.config.Config;
import org.easydarwin.easyscreenlive.util.EasyMediaInfoHelper;
import org.easydarwin.easyscreenlive.util.EasyScreenCap;

import org.easydarwin.rtspservice.JniEasyScreenLive;


/**
 *
 */
public class CapScreenService extends Service implements JniEasyScreenLive.IPCameraCallBack{

    private final static String TAG = "CapScreenService";

    private int windowWidth = 1920;
    private int windowHeight = 1080;
    private int mFrameRate = 30;
    private int mBitRate = 4*1000*1000;


    public Context mContext;
    static boolean mServiceIsStart = false;

    private PushDataThread mPushThread;

    private JniEasyScreenLive jniEasyScreenLive;
    EasyScreenCap easyScreenCap;
    private int mChannelId = 1;


    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "on create");
        mContext = this;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mServiceIsStart = true;
        easyScreenCap =  new EasyScreenCap(this);
        int ret = easyScreenCap.initScreenCapture(PusherFragment.mResultIntent, PusherFragment.mResultCode,
                windowWidth, windowHeight, mFrameRate, mBitRate);
        if (ret < 0) {
            PusherFragment.mResultIntent = null;
            PusherFragment.mResultCode = 0;
            Log.e(TAG, "init easyScreenCap fail");
            easyScreenCap = null;
            return START_STICKY;
        }
        startRtspServer();
        return  super.onStartCommand(intent, flags, startId);
    }

    int startRtspServer() {
        if (jniEasyScreenLive != null) {
            return -1;
        }
        jniEasyScreenLive = new JniEasyScreenLive();
        mChannelId = jniEasyScreenLive.registerCallback(this);
        jniEasyScreenLive.active(this);

        OnLiveManagerService.liveRtspConfig.port             = Integer.parseInt(Config.getRtspPort(this));
        OnLiveManagerService.liveRtspConfig.URL              = Config.getRtspUrl(this);
        OnLiveManagerService.liveRtspConfig.strName          = Config.getStreamName(this);

        OnLiveManagerService.liveRtspConfig.enableMulticast  = Integer.parseInt(Config.getLiveType(this));
        OnLiveManagerService.liveRtspConfig.multicastIP      = "239.255.42.42";
        OnLiveManagerService.liveRtspConfig.multicastPort    = Integer.parseInt(Config.getMulPort(this));
        OnLiveManagerService.liveRtspConfig.multicastTTL     = 7;

        int ret = jniEasyScreenLive.startup(OnLiveManagerService.liveRtspConfig.port,
                JniEasyScreenLive.AuthType.AUTHENTICATION_TYPE_BASIC,
                "","", "",
                0,mChannelId,OnLiveManagerService.liveRtspConfig.strName.getBytes(),
                OnLiveManagerService.liveRtspConfig.enableMulticast, OnLiveManagerService.liveRtspConfig.multicastIP,
                OnLiveManagerService.liveRtspConfig.multicastPort, OnLiveManagerService.liveRtspConfig.multicastTTL);
        if (ret == 0) {
            OnLiveManagerService.liveRtspConfig.isRunning = true;
            Toast.makeText(mContext, "屏幕推流成功"+
                            (OnLiveManagerService.liveRtspConfig.enableMulticast==1?"(组播方式) ":"（单播方式）")+"\n"
                            + "URL:"+OnLiveManagerService.liveRtspConfig.URL,
                    Toast.LENGTH_SHORT).show();
        } else if (ret == JniEasyScreenLive.EasyErrorCode.EASY_SDK_ACTIVE_FAIL) {
            jniEasyScreenLive.shutdown();
            jniEasyScreenLive = null;
            Toast.makeText(mContext, "许可证过期，屏幕推流失败", Toast.LENGTH_SHORT).show();
        } else {
            jniEasyScreenLive.shutdown();
            jniEasyScreenLive = null;
            Toast.makeText(mContext, "屏幕推流失败:" + ret + ", 请修改RTSP端口", Toast.LENGTH_SHORT).show();
        }
        return ret;
    }

    private void stopRtspServer() {
        if (jniEasyScreenLive != null) {
            OnLiveManagerService.liveRtspConfig.isRunning = false;
            jniEasyScreenLive.resetChannel(mChannelId);
            jniEasyScreenLive.shutdown();
            jniEasyScreenLive.unrigisterCallback(this);
            jniEasyScreenLive = null;
        }
    }

    private void startPush() {
        if (easyScreenCap == null) {
            return;
        }
        easyScreenCap.startMediaCodec();
        mPushThread = new PushDataThread();
        mPushThread.start();
    }

    /**
     * 停止编码并释放编码资源占用
     */
    private void stopPush() {
        Thread t = mPushThread;
        if (t != null) {
            mPushThread = null;
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if(easyScreenCap != null)
            easyScreenCap.stopMediaCodec();
    }

    class PushDataThread extends Thread{
        public void run() {
            Log.i(TAG, "startPush thread");
            while (mPushThread != null) {
                byte[] outData = easyScreenCap.getVideoOutData();
                if(jniEasyScreenLive != null && outData != null ) {
                    jniEasyScreenLive.pushFrame(mChannelId, JniEasyScreenLive.FrameFlag.EASY_SDK_VIDEO_FRAME_FLAG,
                            System.currentTimeMillis(),
                            outData, 0,outData.length);
                } else {
                    try {
                        Thread.sleep(10);
                    }catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            Log.i(TAG, "startPush thread");
        }
    }

    public void onDestroy() {
        Log.e(TAG, "onDestroy");
        Toast.makeText(mContext, "结束推流", Toast.LENGTH_SHORT).show();
        stopPush();
        stopRtspServer();
        if (easyScreenCap != null) {
            easyScreenCap.uninitScreenCapture();
            easyScreenCap = null;
        }
        mServiceIsStart = false;

        super.onDestroy();
    }


    @Override
    public void onIPCameraCallBack(int channelId, int channelState, byte[] mediaInfo, int userPtr) {
        if(channelId != mChannelId)
            return;
        switch(channelState){
            case JniEasyScreenLive.ChannelState.EASY_IPCAMERA_STATE_ERROR:
                Log.i(TAG, "Screen Record EASY_IPCAMERA_STATE_ERROR");
                break;
            case JniEasyScreenLive.ChannelState.EASY_IPCAMERA_STATE_REQUEST_MEDIA_INFO:
                Log.i(TAG, "Screen Record EASY_IPCAMERA_STATE_REQUEST_MEDIA_INFO");
                EasyMediaInfoHelper easyMediaInfoHelper = new EasyMediaInfoHelper();
                easyMediaInfoHelper.setH264MediaInfo(mContext, windowWidth, windowHeight, mFrameRate);
                easyMediaInfoHelper.fillMediaInfo(mediaInfo);
                break;
            case JniEasyScreenLive.ChannelState.EASY_IPCAMERA_STATE_REQUEST_PLAY_STREAM:
                Log.i(TAG, "Screen Record EASY_IPCAMERA_STATE_REQUEST_PLAY_STREAM");
                startPush();
                break;
            case JniEasyScreenLive.ChannelState.EASY_IPCAMERA_STATE_REQUEST_STOP_STREAM:
                Log.i(TAG, "Screen Record EASY_IPCAMERA_STATE_REQUEST_STOP_STREAM");
                stopPush();
                break;
            default:
                break;
        }
    }
}//end
