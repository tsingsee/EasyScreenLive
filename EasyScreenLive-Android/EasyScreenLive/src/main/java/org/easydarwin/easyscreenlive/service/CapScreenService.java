package org.easydarwin.easyscreenlive.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import org.easydarwin.easyscreenlive.base.EasyAudioStreamCallback;
import org.easydarwin.easyscreenlive.base.EasyVideoStreamCallback;
import org.easydarwin.easyscreenlive.base.EasyVideoSource;
import org.easydarwin.easyscreenlive.config.Config;
import org.easydarwin.easyscreenlive.config.LiveRtspConfig;
import org.easydarwin.easyscreenlive.ui.pusher.PusherFragment;
import org.easydarwin.easyscreenlive.ui.pusher.PusherPresenter;
import org.easydarwin.easyscreenlive.utils.AudioStream;
import org.easydarwin.easyscreenlive.utils.EasyCameraCap;
import org.easydarwin.easyscreenlive.utils.EasyMediaInfoHelper;
import org.easydarwin.easyscreenlive.utils.EasyScreenCap;
import org.easydarwin.rtspservice.JniEasyScreenLive;


/**
 *
 */
public class CapScreenService extends Service implements JniEasyScreenLive.IPCameraCallBack,
        EasyVideoStreamCallback,
        EasyAudioStreamCallback{

    private final static String TAG = "CapScreenService";
    private int windowWidth = 1280;
    private int windowHeight = 720;
    private int mFrameRate = 30;
    private int mBitRate = 0;//4*1000*1000;


    public Context mContext;
    EasyVideoStreamCallback easyVideoStreamCallback;
    EasyAudioStreamCallback easyAudioStreamCallback;
    public static AudioStream audioStream;


    private JniEasyScreenLive jniEasyScreenLive;
    EasyVideoSource easyVideoSource;
    private int mChannelId = 1;

    private static int pushServiceStatus = EASY_PUSH_SERVICE_STATUS.STATUS_LEISURE;
    public static ServiceCommondHandle serviceCommondHandle = null;

    public static class EASY_PUSH_SERVICE_STATUS {
        static public final int STATUS_LEISURE              =  0; //服务空闲
        static public final int STATUS_PUSH_SCREEN          =  1; //服务推屏
        static public final int STATUS_PUSH_CAMREA_BACK     =  2; //服务推后摄像头
        static public final int STATUS_PUSH_CAMREA_FRONT    =  3; //服务推前摄像头
    }

    public static class EASY_PUSH_SERVICE_CMD {
        static public final int CMD_STOP_PUSH               = 0;
        static public final int CMD_START_PUSH_SCREEN       = 1;
        static public final int CMD_START_PUSH_CAMREA_BACK  = 2;
        static public final int CMD_START_PUSH_CAMREA_FRONT = 3;

    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if(mBitRate == 0) {
            int bitrate = (int) (windowWidth * windowHeight * 20 * 2 * 0.05f);
            if (windowWidth >= 1920 || windowHeight >= 1920) bitrate *= 0.3;
            else if (windowWidth >= 1280 || windowHeight >= 1280) bitrate *= 0.4;
            else if (windowWidth >= 720 || windowHeight >= 720) bitrate *= 0.6;
            mBitRate = bitrate;
        }

        easyVideoStreamCallback = this;
        easyAudioStreamCallback = this;
        serviceCommondHandle = new ServiceCommondHandle();
        mContext = this;
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


    static public int getPushServiceStatus() {
        return pushServiceStatus;
    }

    public class ServiceCommondHandle extends Handler {
        @Override
        public void handleMessage(Message msg)
        {
            switch(msg.what)
            {
                case EASY_PUSH_SERVICE_CMD.CMD_START_PUSH_CAMREA_BACK:
                case EASY_PUSH_SERVICE_CMD.CMD_START_PUSH_CAMREA_FRONT:{
                    if (pushServiceStatus != EASY_PUSH_SERVICE_STATUS.STATUS_LEISURE) {
                        clearHandleMessage();
                        sendCmd(EASY_PUSH_SERVICE_CMD.CMD_STOP_PUSH);
                        return;
                    }

                    int ret = startRtspServer();
                    if (ret != 0) {
                        PusherPresenter.getInterface().onStartPushFail(getApplicationContext(), ret);
                        break;
                    }

                    if (msg.what == EASY_PUSH_SERVICE_CMD.CMD_START_PUSH_CAMREA_BACK) {
                        easyVideoSource = new EasyCameraCap(mContext, PusherFragment.mSurfaceView,
                                android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK);
                    } else {
                        easyVideoSource = new EasyCameraCap(mContext, PusherFragment.mSurfaceView,
                                android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT);
                    }
                    ret = easyVideoSource.init(windowWidth, windowHeight, mFrameRate, mBitRate, easyVideoStreamCallback);
                    if (ret < 0) {
                        Log.e(TAG, "init easyCamreaCap fail");
                        easyVideoSource.uninit();
                        easyVideoSource = null;
                        PusherPresenter.getInterface().onStartPushFail(getApplicationContext(), ret);
                        break;
                    }
                    pushServiceStatus = msg.what;
                    PusherPresenter.getInterface().onStartPushSuccess(getApplicationContext(),LiveRtspConfig.URL);


                }
                break;
                case EASY_PUSH_SERVICE_CMD.CMD_START_PUSH_SCREEN:{
                    if (pushServiceStatus != EASY_PUSH_SERVICE_STATUS.STATUS_LEISURE) {
                        clearHandleMessage();
                        sendCmd(EASY_PUSH_SERVICE_CMD.CMD_STOP_PUSH);
                        return;
                    }

                    int ret = startRtspServer();
                    if (ret != 0) {
                        PusherPresenter.getInterface().onStartPushFail(getApplicationContext(), ret);
                        break;
                    }

                    easyVideoSource = new EasyScreenCap(mContext);
                    ret = easyVideoSource.init(windowWidth, windowHeight, mFrameRate, mBitRate, easyVideoStreamCallback);
                    if (ret < 0) {
                        PusherFragment.mResultIntent = null;
                        PusherFragment.mResultCode = 0;
                        Log.e(TAG, "init easyScreenCap fail");
                        easyVideoSource.uninit();
                        easyVideoSource = null;
                        PusherPresenter.getInterface().onStartPushFail(getApplicationContext(), ret);
                        break;
                    }
                    pushServiceStatus = msg.what;
                    PusherPresenter.getInterface().onStartPushSuccess(getApplicationContext(),LiveRtspConfig.URL);

                }
                break;
                case EASY_PUSH_SERVICE_CMD.CMD_STOP_PUSH:{
                    if (easyVideoSource != null) {
                        easyVideoSource.uninit();
                        easyVideoSource = null;
                    }
                    stopRtspServer();
                    pushServiceStatus = EASY_PUSH_SERVICE_STATUS.STATUS_LEISURE;
                    PusherPresenter.getInterface().onStopPushSuccess(getApplicationContext());
                }
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

    int startRtspServer() {
        if (jniEasyScreenLive != null) {
            return -1;
        }
        jniEasyScreenLive = new JniEasyScreenLive();
        mChannelId = jniEasyScreenLive.registerCallback(this);
        jniEasyScreenLive.active(this);

        LiveRtspConfig.port             = Integer.parseInt(Config.getRtspPort(this));
        LiveRtspConfig.URL              = Config.getRtspUrl(this);
        LiveRtspConfig.strName          = Config.getStreamName(this);

        LiveRtspConfig.enableAudio      = Integer.parseInt(Config.getEnableAudio(this));

        LiveRtspConfig.enableMulticast  = Integer.parseInt(Config.getLiveType(this));
        LiveRtspConfig.multicastIP      = "239.255.42.42";
        LiveRtspConfig.multicastPort    = Integer.parseInt(Config.getMulPort(this));
        LiveRtspConfig.multicastTTL     = 7;


        int ret = jniEasyScreenLive.startup(LiveRtspConfig.port,
                JniEasyScreenLive.AuthType.AUTHENTICATION_TYPE_BASIC,
                "","", "",
                0,mChannelId,LiveRtspConfig.strName.getBytes(),
                LiveRtspConfig.enableMulticast, LiveRtspConfig.multicastIP,
                LiveRtspConfig.multicastPort, LiveRtspConfig.multicastTTL);
        if (ret == 0) {
            LiveRtspConfig.isRunning = true;
        } else if (ret != JniEasyScreenLive.EasyErrorCode.EASY_SDK_ACTIVE_FAIL){
            LiveRtspConfig.port ++;
            Config.saveRtspPort(mContext, ""+ LiveRtspConfig.port ++);
            jniEasyScreenLive.shutdown();
            jniEasyScreenLive = null;
        } else {
            jniEasyScreenLive.shutdown();
            jniEasyScreenLive = null;
        }

        return ret;
    }

    @Override
    public void videoDataBack(long timestamp, byte[] pBuffer, int offset, int length) {
        if(jniEasyScreenLive != null && pBuffer != null ) {
            jniEasyScreenLive.pushFrame(mChannelId, JniEasyScreenLive.FrameFlag.EASY_SDK_VIDEO_FRAME_FLAG,
                    timestamp,
                    pBuffer, offset,length);
        }
    }

    @Override
    public void audioDataBack(long timestamp, byte[] pBuffer, int offset, int length) {
//        Log.e(TAG, "len:" + length);
        jniEasyScreenLive.pushFrame(mChannelId, JniEasyScreenLive.FrameFlag.EASY_SDK_AUDIO_FRAME_FLAG,
                timestamp,
                pBuffer, offset,length);
    }

    private void stopRtspServer() {
        if (jniEasyScreenLive != null) {
            LiveRtspConfig.isRunning = false;
            jniEasyScreenLive.resetChannel(mChannelId);
            jniEasyScreenLive.shutdown();
            jniEasyScreenLive.unrigisterCallback(this);
            jniEasyScreenLive = null;
        }
    }

    private void startPush() {
        if (easyVideoSource == null) {
            return;
        }
        if (easyVideoSource.startStream() != 0) {
            Log.e(TAG, "easyScreenCap.startMediaCodec fail");
        }
    }

    /**
     * 停止编码并释放编码资源占用
     */
    private void stopPush() {
        if(easyVideoSource != null)
            easyVideoSource.stopStream();
    }


    public void onDestroy() {
        Log.e(TAG, "onDestroy");
        Toast.makeText(mContext, "结束推流", Toast.LENGTH_SHORT).show();
        serviceCommondHandle = null;

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
                if(LiveRtspConfig.enableAudio == 1) {
                    if(audioStream == null)
                        audioStream = new AudioStream(easyAudioStreamCallback);
                    Log.e(TAG, "dadasdasdas-d");
                    easyMediaInfoHelper.setAACMediaInfo(audioStream.getSamplingRate(), audioStream.getChannelNum(),
                            audioStream.getBitsPerSample());
                }
                easyMediaInfoHelper.setH264MediaInfo(mContext, windowWidth, windowHeight, mFrameRate);
                easyMediaInfoHelper.fillMediaInfo(mediaInfo);
                break;
            case JniEasyScreenLive.ChannelState.EASY_IPCAMERA_STATE_REQUEST_PLAY_STREAM:
                Log.i(TAG, "Screen Record EASY_IPCAMERA_STATE_REQUEST_PLAY_STREAM");
                startPush();
                if(LiveRtspConfig.enableAudio == 1 && audioStream != null) {
                    audioStream.startRecord();
                    audioStream.startPush();
                    Log.e(TAG, "dadasdasdas-d");
                }

                break;
            case JniEasyScreenLive.ChannelState.EASY_IPCAMERA_STATE_REQUEST_STOP_STREAM:
                Log.i(TAG, "Screen Record EASY_IPCAMERA_STATE_REQUEST_STOP_STREAM");
                if(LiveRtspConfig.enableAudio == 1 && audioStream != null) {
                    audioStream.stopPush();
                    audioStream.stop();
                    Log.e(TAG, "dadasdasdas-d");
                }
                stopPush();
                break;
            default:
                break;
        }
    }
}//end
