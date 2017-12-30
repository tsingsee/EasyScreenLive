package org.easydarwin.easyscreenlive;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;
import android.widget.Toast;

import org.easydarwin.easyscreenlive.config.Config;
import org.easydarwin.easyscreenlive.hw.EncoderDebugger;
import org.easydarwin.easyscreenlive.util.Util;
import org.easydarwin.rtspservice.JniEasyScreenLive;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 *
 */
public class CapScreenService extends Service implements JniEasyScreenLive.IPCameraCallBack{

    private final static String TAG = "CapScreenService";
    private int windowWidth;
    private int windowHeight;
    private int screenDensity;
    private int mFrameRate = 20;
    private int mBitRate;
    private MediaProjectionManager mMpmngr;
    public Context mContext;

    private MediaCodec mMediaCodec;
    private Surface mSurface;
    private Thread mPushThread;
    private byte[] mPpsSps;
    private MediaCodec.BufferInfo mBufferInfo = new MediaCodec.BufferInfo();
    private MediaProjection mMpj;
    private VirtualDisplay mVirtualDisplay;

    static boolean mServiceIsStart = false;
    static JniEasyScreenLive jniEasyScreenLive;
    private int mChannelId = 1;

    private byte[] mVps = new byte[255];
    private byte[] mSps = new byte[255];
    private byte[] mPps = new byte[128];
    private byte[] mMei = new byte[128];
    boolean mediaCodecRunning = false;

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "on create");
        mContext = this;
        configureMedia();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mServiceIsStart = true;

        mMpmngr = (MediaProjectionManager) getApplicationContext().getSystemService(MEDIA_PROJECTION_SERVICE);
        mMpj = mMpmngr.getMediaProjection(MainActivity.mResultCode, MainActivity.mResultIntent);
        if (mMpj == null) {
            MainActivity.mResultCode = 0;
            MainActivity.mResultIntent = null;
            mServiceIsStart = false;
            return START_STICKY;
        }

        jniEasyScreenLive = new JniEasyScreenLive();
        mChannelId = jniEasyScreenLive.registerCallback(this);
        jniEasyScreenLive.active(this);

        final String strId =Config.getStreamName(this);
        final String URL = Config.getRtspUrl(this);

        final int iport = Integer.parseInt(Config.getRtspPort(this));
        final int enableMulticast = Integer.parseInt(Config.getLiveType(this));
        final int multicastPort = Integer.parseInt(Config.getMulPort(this));
        final int mulTTL = 7;
        final String multicastIP = "239.255.42.42";

//        new Thread(new Runnable() {
//            @Override
//            public void run() {
                int ret = jniEasyScreenLive.startup(iport,
                        JniEasyScreenLive.AuthType.AUTHENTICATION_TYPE_BASIC,
                        "","", "",
                        0,mChannelId,strId.getBytes(),
                        enableMulticast, multicastIP, multicastPort, mulTTL);
                if (ret == 0) {
                    Toast.makeText(mContext, "屏幕推流成功"+
                                    (enableMulticast==1?"(组播方式) ":"（单播方式）")+"\n"
                                    + "URL:"+URL,
                            Toast.LENGTH_LONG).show();
                } else if (ret == JniEasyScreenLive.EasyErrorCode.EASY_SDK_ACTIVE_FAIL) {
                    Toast.makeText(mContext, "许可证过期，屏幕推流失败", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(mContext, "屏幕推流失败:" + ret, Toast.LENGTH_SHORT).show();
                }
//            }
//        }).start();
        return  super.onStartCommand(intent, flags, startId);
    }



    private void configureMedia() {
        windowWidth = 1920;
        windowHeight = 1080;
        mFrameRate = 30;
        mBitRate = 4000*1000;
        WindowManager wm;
        wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(displayMetrics);
        screenDensity = displayMetrics.densityDpi;


        EncoderDebugger debugger = EncoderDebugger.debug(getApplicationContext(), windowWidth, windowHeight, mFrameRate);
        mSps = Base64.decode(debugger.getB64SPS(), Base64.NO_WRAP);
        mPps = Base64.decode(debugger.getB64PPS(), Base64.NO_WRAP);
    }

    private void startMediaCodec() {
        if (mediaCodecRunning) {
            return;
        }
        mediaCodecRunning = true;
        MediaFormat mediaFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, windowWidth, windowHeight);
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, mBitRate);
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, mFrameRate);
        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 30);
        mediaFormat.setInteger(MediaFormat.KEY_CHANNEL_COUNT, 1);
        mediaFormat.setInteger(MediaFormat.KEY_REPEAT_PREVIOUS_FRAME_AFTER, 1000000 / mFrameRate);
        try {
            //video/avc=H.264
            mMediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mMediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mSurface = mMediaCodec.createInputSurface();

        if (mMpj == null) {
            MainActivity.mResultCode = 0;
            MainActivity.mResultIntent = null;
            return;
        }
        mVirtualDisplay = mMpj.createVirtualDisplay("record_screen", windowWidth, windowHeight, screenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR | DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC |
                        DisplayManager.VIRTUAL_DISPLAY_FLAG_PRESENTATION, mSurface, null, null);

        mMediaCodec.start();
        startPush();
    }

    /**
     * 停止编码并释放编码资源占用
     */
    private void stopMediaCodec() {
        Log.i(TAG, "stopMediaCodec");
        if (!mediaCodecRunning) {
            return;
        }
        mediaCodecRunning = false;

        stopPush();
        if (mVirtualDisplay != null) {
            mVirtualDisplay.release();
            mVirtualDisplay = null;
        }

        if (mMediaCodec != null) {
            mMediaCodec.stop();
            mMediaCodec.release();
            mMediaCodec = null;
        }
        if (mSurface != null) {
            mSurface.release();
        }


    }

    private void requestKeyFram() {
        if (mMediaCodec != null) {
            Bundle param = new Bundle();
            param.putInt(MediaCodec.PARAMETER_KEY_REQUEST_SYNC_FRAME, 0);
            mMediaCodec.setParameters(param);
        }
    }

    private void startPush() {
        if (mPushThread != null) return;
        mPushThread = new Thread() {
            @Override
            public void run() {
                long lastKeyFrmaeTime = System.currentTimeMillis();

                Log.i(TAG, "startPush thread");
                while (mPushThread != null) {
                    int index = mMediaCodec.dequeueOutputBuffer(mBufferInfo, 10000);
                    if (index == MediaCodec.INFO_TRY_AGAIN_LATER) {//请求超时
                        try {
                            // wait 10ms
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                        }
                    } else if (index >= 0) {//有效输出

                        ByteBuffer outputBuffer = mMediaCodec.getOutputBuffer(index);

                        byte[] outData = new byte[mBufferInfo.size];
                        outputBuffer.get(outData);

//                        String data0 = String.format("%x %x %x %x %x %x %x %x %x %x ", outData[0], outData[1], outData[2], outData[3], outData[4], outData[5], outData[6], outData[7], outData[8], outData[9]);
//                        Log.e("out_data", data0);

                        //记录pps和sps
                        int type = outData[4] & 0x07;
//                        Log.i(TAG, "H264 data: size=" + mBufferInfo.size + "   type:" + type );

                        if (type == 7 || type == 8) {
                            mPpsSps = outData;
                        } else if (type == 5) {
                            //在关键帧前面加上pps和sps数据
                            if (mPpsSps != null) {
                                byte[] iframeData = new byte[mPpsSps.length + outData.length];
                                System.arraycopy(mPpsSps, 0, iframeData, 0, mPpsSps.length);
                                System.arraycopy(outData, 0, iframeData, mPpsSps.length, outData.length);
                                outData = iframeData;
                            }
//                            收到一个关键帧，重置关键帧时间
                            lastKeyFrmaeTime=System.currentTimeMillis();
                        }
//                        每秒过一秒申请一个关键帧
                        if (System.currentTimeMillis() - lastKeyFrmaeTime > 1000) {
                            requestKeyFram();
                            lastKeyFrmaeTime=System.currentTimeMillis();
                        }
                        if(jniEasyScreenLive != null && mediaCodecRunning) {
                            jniEasyScreenLive.pushFrame(mChannelId, JniEasyScreenLive.FrameFlag.EASY_SDK_VIDEO_FRAME_FLAG,
                                    System.currentTimeMillis(),
                                    outData, 0,outData.length);
                        }

                        mMediaCodec.releaseOutputBuffer(index, false);
                    }
                }
                Log.e(TAG, "startPush thread");
            }
        };
        mPushThread.start();
    }


    public void onDestroy() {
        Log.e(TAG, "onDestroy");
        stopMediaCodec();

        jniEasyScreenLive.resetChannel(mChannelId);
        jniEasyScreenLive.shutdown();
        jniEasyScreenLive.unrigisterCallback(this);
        jniEasyScreenLive = null;

        if (mMpj != null) {
            mMpj.stop();
            mMpj = null;
        }

        mServiceIsStart = false;
        super.onDestroy();
    }

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
    }

    @Override
    public void onIPCameraCallBack(int channelId, int channelState, byte[] mediaInfo, int userPtr) {
        Log.d(TAG, "kim onIPCameraCallBack, channelId="+channelId+", mChannelId="+mChannelId+", channelState="+channelState);
        if(channelId != mChannelId)
            return;
        switch(channelState){
            case JniEasyScreenLive.ChannelState.EASY_IPCAMERA_STATE_ERROR:
                Log.d(TAG, "Screen Record EASY_IPCAMERA_STATE_ERROR");
//                Util.showDbgMsg(StatusInfoView.DbgLevel.DBG_LEVEL_WARN, "Screen Record EASY_IPCAMERA_STATE_ERROR");
                break;
            case JniEasyScreenLive.ChannelState.EASY_IPCAMERA_STATE_REQUEST_MEDIA_INFO:
//                Util.showDbgMsg(StatusInfoView.DbgLevel.DBG_LEVEL_INFO, "Screen Record EASY_IPCAMERA_STATE_REQUEST_MEDIA_INFO");
                ByteBuffer buffer = ByteBuffer.wrap(mediaInfo);
                buffer.order(ByteOrder.LITTLE_ENDIAN);
                buffer.putInt(JniEasyScreenLive.VideoCodec.EASY_SDK_VIDEO_CODEC_H264);
//                buffer.putInt(mFrameRate);
                buffer.putInt(30);
//                buffer.putInt(mAudioStream.getAudioEncCodec());
//                buffer.putInt(mAudioStream.getSamplingRate());
//                buffer.putInt(mAudioStream.getChannelNum());
//                buffer.putInt(mAudioStream.getBitsPerSample());
                buffer.putInt(0);
                buffer.putInt(0);
                buffer.putInt(0);
                buffer.putInt(0);

                buffer.putInt(0);//vps length
                buffer.putInt(mSps.length);
                buffer.putInt(mPps.length);
                buffer.putInt(0);
                buffer.put(mVps);
                buffer.put(mSps,0,mSps.length);
                if(mSps.length < 255) {
                    buffer.put(mVps, 0, 255 - mSps.length);
                }
                buffer.put(mPps,0,mPps.length);
                if(mPps.length < 128) {
                    buffer.put(mVps, 0, 128 - mPps.length);
                }
                buffer.put(mMei);
                break;
            case JniEasyScreenLive.ChannelState.EASY_IPCAMERA_STATE_REQUEST_PLAY_STREAM:
                startMediaCodec();
//                Util.showDbgMsg(StatusInfoView.DbgLevel.DBG_LEVEL_INFO, "Screen Record EASY_IPCAMERA_STATE_REQUEST_PLAY_STREAM");
                //mAudioStream.startPush();
                break;
            case JniEasyScreenLive.ChannelState.EASY_IPCAMERA_STATE_REQUEST_STOP_STREAM:
//                Util.showDbgMsg(StatusInfoView.DbgLevel.DBG_LEVEL_INFO, "Screen Record EASY_IPCAMERA_STATE_REQUEST_STOP_STREAM");
                stopMediaCodec();
                //mAudioStream.stopPush();
                break;
            default:
                break;
        }
    }
}//end
