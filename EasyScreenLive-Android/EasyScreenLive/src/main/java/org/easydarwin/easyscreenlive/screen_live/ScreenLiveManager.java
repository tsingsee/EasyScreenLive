package org.easydarwin.easyscreenlive.screen_live;

import android.content.Context;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import org.easydarwin.easyscreenlive.base.EasyAudioStreamCallback;
import org.easydarwin.easyscreenlive.base.EasyVideoSource;
import org.easydarwin.easyscreenlive.base.EasyVideoStreamCallback;
import org.easydarwin.easyscreenlive.config.Config;
import org.easydarwin.easyscreenlive.config.LiveRtspConfig;
import org.easydarwin.easyscreenlive.ui.pusher.PusherFragment;
import org.easydarwin.easyscreenlive.ui.pusher.PusherPresenter;
import org.easydarwin.easyscreenlive.utils.EasyMediaInfoHelper;
import org.easydarwin.rtspservice.JniEasyScreenLive;

/**
 * Created by gavin on 2018/4/21.
 */

public class ScreenLiveManager implements JniEasyScreenLive.IPCameraCallBack,
        EasyVideoStreamCallback,
        EasyAudioStreamCallback {
    private final static String TAG = "ScreenLiveManager";

    private int windowWidth = 1280;
    private int windowHeight = 720;
    private int mFrameRate = 30;


    public Context mContext;
    EasyVideoStreamCallback easyVideoStreamCallback;
    EasyAudioStreamCallback easyAudioStreamCallback;
    public static AudioStream audioStream;


    private JniEasyScreenLive jniEasyScreenLive;
    EasyVideoSource easyVideoSource;
    private int mChannelId = 1;

    private static int pushServiceStatus = EASY_PUSH_SERVICE_STATUS.STATUS_LEISURE;


    public static class EASY_PUSH_SERVICE_STATUS {
        static public final int STATUS_LEISURE              =  0; //服务空闲
        static public final int STATUS_PUSH_SCREEN          =  1; //服务推屏
        static public final int STATUS_PUSH_CAMREA_BACK     =  2; //服务推后摄像头
        static public final int STATUS_PUSH_CAMREA_FRONT    =  3; //服务推前摄像头
    }

    ScreenLiveManager(Context context) {
        mContext = context;
        easyVideoStreamCallback = this;
        easyAudioStreamCallback = this;
    }

    void destory() {
        stopPush();
        stopRtspServer();
    }


    int onScreenLiveCmd(Message msg) {
        switch(msg.what)
        {
            case CapScreenService.EASY_PUSH_SERVICE_CMD.CMD_START_PUSH_CAMREA_BACK:
            case CapScreenService.EASY_PUSH_SERVICE_CMD.CMD_START_PUSH_CAMREA_FRONT:
            case CapScreenService.EASY_PUSH_SERVICE_CMD.CMD_START_PUSH_SCREEN: {
                int ret = startRtspServer();
                if (ret != 0) {
                    PusherPresenter.getInterface().onStartPushFail(mContext.getApplicationContext(), ret);
                    break;
                }

                if (msg.what == CapScreenService.EASY_PUSH_SERVICE_CMD.CMD_START_PUSH_CAMREA_BACK) {
                    easyVideoSource = new EasyCameraCap(mContext, PusherFragment.mSurfaceView,
                            android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK);
                } else if (msg.what == CapScreenService.EASY_PUSH_SERVICE_CMD.CMD_START_PUSH_CAMREA_FRONT){
                    easyVideoSource = new EasyCameraCap(mContext, PusherFragment.mSurfaceView,
                            android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT);
                } else if (msg.what == CapScreenService.EASY_PUSH_SERVICE_CMD.CMD_START_PUSH_SCREEN) {
                    easyVideoSource = new EasyScreenCap(mContext);
                }

                if (msg.what == CapScreenService.EASY_PUSH_SERVICE_CMD.CMD_START_PUSH_SCREEN) {
                    Context context = mContext.getApplicationContext();
                    DisplayMetrics dm = new DisplayMetrics();
                    WindowManager windowMgr = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
                    windowMgr.getDefaultDisplay().getRealMetrics(dm);
                    // 获取高度
                    windowHeight = dm.heightPixels;
                    // 获取宽度
                    windowWidth = dm.widthPixels;
                }

                ret = easyVideoSource.init(windowWidth, windowHeight, mFrameRate,
                        LiveRtspConfig.bitRate, easyVideoStreamCallback);
                if (ret < 0) {
                    Log.e(TAG, "init easyCamreaCap fail");
                    easyVideoSource.uninit();
                    easyVideoSource = null;
                    PusherPresenter.getInterface().onStartPushFail(mContext.getApplicationContext(), ret);
                    break;
                }
                pushServiceStatus = msg.what;
                PusherPresenter.getInterface().onStartPushSuccess(mContext.getApplicationContext(),LiveRtspConfig.URL);
            }
            break;
            case CapScreenService.EASY_PUSH_SERVICE_CMD.CMD_STOP_PUSH:{
                if (easyVideoSource != null) {
                    easyVideoSource.uninit();
                    easyVideoSource = null;
                }
                stopRtspServer();
                pushServiceStatus = EASY_PUSH_SERVICE_STATUS.STATUS_LEISURE;
                PusherPresenter.getInterface().onStopPushSuccess(mContext.getApplicationContext());
            }
            break;

            default:
                break;
        }
        return 0;
    }

    static public int getPushServiceStatus() {
        return pushServiceStatus;
    }

    private int startRtspServer() {
        if (jniEasyScreenLive != null) {
            return -1;
        }

        LiveRtspConfig.initLiveRtspConfig(mContext);

        jniEasyScreenLive = new JniEasyScreenLive();
        mChannelId = jniEasyScreenLive.registerCallback(this);
        jniEasyScreenLive.active(mContext);

        int ret = jniEasyScreenLive.startup(LiveRtspConfig.port,
                JniEasyScreenLive.AuthType.AUTHENTICATION_TYPE_BASIC,
                "","", "",
                0,mChannelId,LiveRtspConfig.strName.getBytes(),
                LiveRtspConfig.enableMulticast, LiveRtspConfig.multicastIP,
                LiveRtspConfig.multicastPort, LiveRtspConfig.multicastTTL,
                LiveRtspConfig.enableArq, LiveRtspConfig.enableFec,LiveRtspConfig.fecGroudSize, LiveRtspConfig.fecParam);
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
        if (easyVideoSource != null && easyVideoSource.startStream() != 0) {
            Log.e(TAG, "easyScreenCap.startMediaCodec fail");
        }
        if(LiveRtspConfig.enableAudio == 1 && audioStream != null) {
            audioStream.startRecord();
            audioStream.startPush();
        }
    }

    /**
     * 停止编码并释放编码资源占用
     */
    private void stopPush() {
        if(easyVideoSource != null)
            easyVideoSource.stopStream();
        if(LiveRtspConfig.enableAudio == 1 && audioStream != null) {
            audioStream.stopPush();
            audioStream.stop();
        }
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
        jniEasyScreenLive.pushFrame(mChannelId, JniEasyScreenLive.FrameFlag.EASY_SDK_AUDIO_FRAME_FLAG,
                timestamp,
                pBuffer, offset,length);
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
                    easyMediaInfoHelper.setAACMediaInfo(audioStream.getSamplingRate(), audioStream.getChannelNum(),
                            audioStream.getBitsPerSample());
                }
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

}

