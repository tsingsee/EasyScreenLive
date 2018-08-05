package org.easydarwin.easyscreenlive.screen_live;

import android.content.Context;
import android.media.MediaFormat;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.google.gson.annotations.Until;

import org.easydarwin.easyscreenlive.screen_live.base.EasyAudioStreamCallback;
import org.easydarwin.easyscreenlive.screen_live.base.EasyVideoSource;
import org.easydarwin.easyscreenlive.screen_live.base.EasyVideoStreamCallback;
import org.easydarwin.easyscreenlive.screen_live.hw.CodecManager;
import org.easydarwin.easyscreenlive.screen_live.utils.EasyMediaInfoHelper;
import org.easydarwin.easyscreenlive.screen_live.utils.Util;
import org.easydarwin.easyscreenlive.ui.pusher.PusherPresenter;

import org.easydarwin.rtspservice.JniEasyScreenLive;

/**
 * Created by gavin on 2018/4/21.
 */

class ScreenLiveManager implements JniEasyScreenLive.IPCameraCallBack,
        EasyVideoStreamCallback,
        EasyAudioStreamCallback {
    private final static String TAG = "ScreenLiveManager";

    private int windowWidth = 1280;
    private int windowHeight = 720;
    private int mFrameRate = 20;
    private String MIME_TYPE = MediaFormat.MIMETYPE_VIDEO_HEVC;
//    private String MIME_TYPE = MediaFormat.MIMETYPE_VIDEO_AVC;


    public Context mContext;
    EasyVideoStreamCallback easyVideoStreamCallback;
    EasyAudioStreamCallback easyAudioStreamCallback;
    public static AudioStream audioStream;


    private JniEasyScreenLive jniEasyScreenLive;
    EasyVideoSource easyVideoSource;
    private int mChannelId = 1;
    boolean isEnablePushAudio = true;

    private static int pushServiceStatus = EasyScreenLiveAPI.EASY_PUSH_SERVICE_STATUS.STATUS_LEISURE;


    ScreenLiveManager(Context context) {
        mContext = context;
        easyVideoStreamCallback = this;
        easyAudioStreamCallback = this;
    }

    void destory() {
        stopPush();
        stopRtspServer();
    }

    int checkEncoder() {
        boolean isHardwareEncoder = true;
        int supportH264 = CodecManager.isSupportH264OrH265(isHardwareEncoder, MIME_TYPE, windowWidth, windowHeight);
        if (supportH264 == -1) {
            PusherPresenter.getInterface().onStartPushFail(mContext.getApplicationContext(),
                    MIME_TYPE + "不支持" + (isHardwareEncoder ? "硬件":"软件") +
                            "编码器");
            return -1;
        } else if (supportH264 == -2) {
            PusherPresenter.getInterface().onStartPushFail(mContext.getApplicationContext(),
                    MIME_TYPE + (isHardwareEncoder ? "硬件":"软件") +
                            "编码器不支持 " + windowWidth + " * " + windowHeight + " 分辨率");
            int supportNewH264 = CodecManager.isSupportH264OrH265(isHardwareEncoder, MIME_TYPE,1280, 720);
            if (supportNewH264 == -2) {
                PusherPresenter.getInterface().onStartPushFail(mContext.getApplicationContext(),
                        MIME_TYPE + (isHardwareEncoder ? "硬件":"软件") +
                                "编码器不支持 " + windowWidth + " * " + windowHeight + "和 1280*720"+ " 分辨率" );
            } else {
                PusherPresenter.getInterface().onStartPushFail(mContext.getApplicationContext(),
                        MIME_TYPE + (isHardwareEncoder ? "硬件":"软件") +
                                "编码器不支持 " + windowWidth + " * " + windowHeight + " 分辨率"+ "修改为 1280*720"+ " 分辨率" );
            }
            return -1;
        }
        return 0;
    }

    int onScreenLiveCmd(Message msg) {
        switch(msg.what)
        {
            case CapScreenService.EASY_PUSH_SERVICE_CMD.CMD_START_PUSH_CAMREA_BACK:
            case CapScreenService.EASY_PUSH_SERVICE_CMD.CMD_START_PUSH_CAMREA_FRONT:
            case CapScreenService.EASY_PUSH_SERVICE_CMD.CMD_START_PUSH_SCREEN: {
                if (msg.what == CapScreenService.EASY_PUSH_SERVICE_CMD.CMD_START_PUSH_SCREEN) {
                    int  []resolution = new int[2];
                    Util.getScreenresolution(mContext.getApplicationContext(), resolution);
                    //默认使用 720 分辨率，事件感官效果720和1080差别不大，但是编码速度会加快
                    resolution[0] = 1280;
                    resolution[1] = 720;
                    Util.adapterResolution(resolution[0], resolution[1], resolution);
                    Log.e(TAG, "w" +resolution[0] + " " + resolution[1]);
                    windowWidth  = resolution[0] ;
                    windowHeight = resolution[1];
                }
                if (checkEncoder() < 0) {
                    break;
                }
                int ret = startRtspServer();
                if (ret != 0) {
                    PusherPresenter.getInterface().onStartPushFail(mContext.getApplicationContext(), ret);
                    break;
                }
                SurfaceView mSurfaceView = null;
                if(msg.obj != null)
                {
                    mSurfaceView = (SurfaceView)msg.obj;
                }
                if (msg.what == CapScreenService.EASY_PUSH_SERVICE_CMD.CMD_START_PUSH_CAMREA_BACK) {
                    easyVideoSource = new EasyCameraCap(mContext, mSurfaceView,
                            android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK);
                } else if (msg.what == CapScreenService.EASY_PUSH_SERVICE_CMD.CMD_START_PUSH_CAMREA_FRONT){
                    easyVideoSource = new EasyCameraCap(mContext, mSurfaceView,
                            android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT);
                } else if (msg.what == CapScreenService.EASY_PUSH_SERVICE_CMD.CMD_START_PUSH_SCREEN) {
                    easyVideoSource = new EasyScreenCap(mContext);
                }
                ret = easyVideoSource.init(MIME_TYPE, windowWidth, windowHeight, mFrameRate,
                        EasyScreenLiveAPI.liveRtspConfig.bitRate, easyVideoStreamCallback);
                if (ret < 0) {
                    Log.e(TAG, "init easyCamreaCap fail");
                    easyVideoSource.uninit();
                    easyVideoSource = null;
                    PusherPresenter.getInterface().onStartPushFail(mContext.getApplicationContext(), ret);
                    break;
                }
                pushServiceStatus = msg.what;
                PusherPresenter.getInterface().onStartPushSuccess(mContext.getApplicationContext(),
                        EasyScreenLiveAPI.liveRtspConfig.enableMulticast, EasyScreenLiveAPI.liveRtspConfig.URL);
            }
            break;
            case CapScreenService.EASY_PUSH_SERVICE_CMD.CMD_STOP_PUSH:{
                if (easyVideoSource != null) {
                    easyVideoSource.uninit();
                    easyVideoSource = null;
                }
                stopRtspServer();
                pushServiceStatus = EasyScreenLiveAPI.EASY_PUSH_SERVICE_STATUS.STATUS_LEISURE;
                PusherPresenter.getInterface().onStopPushSuccess(mContext.getApplicationContext());
            }
                break;
            case CapScreenService.EASY_PUSH_SERVICE_CMD.CMD_START_PUSH_AUDIO:
                isEnablePushAudio = true;
                break;
            case CapScreenService.EASY_PUSH_SERVICE_CMD.CMD_STOP_PUSH_AUDIO:
                isEnablePushAudio = false;
            case CapScreenService.EASY_PUSH_SERVICE_CMD.CMD_SCREEN_ROTATE:
                onScreenRotate();
                break;
            default:
                break;
        }
        return 0;
    }

    /**
     * 屏幕旋转了
     */
    void onScreenRotate()
    {
        Context context = mContext.getApplicationContext();
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager windowMgr = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        int roate = windowMgr.getDefaultDisplay().getRotation();
        if (roate == Surface.ROTATION_0 || roate == Surface.ROTATION_180) //竖屏
        {
            if (EasyScreenLiveAPI.liveRtspConfig.pushdev == 0) {
                EasyScreenLiveAPI.liveRtspConfig.pushdev = 1;
                stopPush();
                startPush();
            }

        } else { //横屏
            if (EasyScreenLiveAPI.liveRtspConfig.pushdev == 1) {
                EasyScreenLiveAPI.liveRtspConfig.pushdev = 0;
                stopPush();
                startPush();
            }

        }
        Log.e(TAG, "rotate: " + roate);
    }

    static public int getPushServiceStatus() {
        return pushServiceStatus;
    }

    private int startRtspServer() {
        if (jniEasyScreenLive != null) {
            return -1;
        }

        jniEasyScreenLive = new JniEasyScreenLive();
        mChannelId = jniEasyScreenLive.registerCallback(this);
        jniEasyScreenLive.active(mContext);

        int ret = jniEasyScreenLive.startup(EasyScreenLiveAPI.liveRtspConfig.port,
                JniEasyScreenLive.AuthType.AUTHENTICATION_TYPE_BASIC,
                "","", "",
                0,mChannelId,EasyScreenLiveAPI.liveRtspConfig.strName.getBytes(),
                EasyScreenLiveAPI.liveRtspConfig.enableMulticast, EasyScreenLiveAPI.liveRtspConfig.multicastIP,
                EasyScreenLiveAPI.liveRtspConfig.multicastPort, EasyScreenLiveAPI.liveRtspConfig.multicastTTL,
                EasyScreenLiveAPI.liveRtspConfig.enableArq, EasyScreenLiveAPI.liveRtspConfig.enableFec,
                EasyScreenLiveAPI.liveRtspConfig.fecGroudSize, EasyScreenLiveAPI.liveRtspConfig.fecParam);
        if (ret == 0) {
            EasyScreenLiveAPI.liveRtspConfig.isRunning = true;
        } else if (ret != JniEasyScreenLive.EasyErrorCode.EASY_SDK_ACTIVE_FAIL){
            EasyScreenLiveAPI.liveRtspConfig.port ++;
            //Config.saveRtspPort(mContext, ""+ EasyScreenLiveAPI.liveRtspConfig.port ++);
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
            EasyScreenLiveAPI.liveRtspConfig.isRunning = false;
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
        if(EasyScreenLiveAPI.liveRtspConfig.enableAudio == 1 && audioStream != null) {
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
        if(EasyScreenLiveAPI.liveRtspConfig.enableAudio == 1 && audioStream != null) {
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
        if(isEnablePushAudio) {
            jniEasyScreenLive.pushFrame(mChannelId, JniEasyScreenLive.FrameFlag.EASY_SDK_AUDIO_FRAME_FLAG,
                    timestamp,
                    pBuffer, offset, length);
        }
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
                if(EasyScreenLiveAPI.liveRtspConfig.enableAudio == 1) {
                    if(audioStream == null)
                        audioStream = new AudioStream(easyAudioStreamCallback);
                    easyMediaInfoHelper.setAACMediaInfo(audioStream.getSamplingRate(), audioStream.getChannelNum(),
                            audioStream.getBitsPerSample());
                }
                if (MIME_TYPE.equals(MediaFormat.MIMETYPE_VIDEO_HEVC)) {
                    easyMediaInfoHelper.setH265MediaInfo(mContext, windowWidth, windowHeight, mFrameRate);
                } else {
                    easyMediaInfoHelper.setH264MediaInfo(mContext, windowWidth, windowHeight, mFrameRate);
                }
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

