package org.easydarwin.easyscreenlive.util;

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
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;

import java.io.IOException;
import java.nio.ByteBuffer;

import static android.content.Context.MEDIA_PROJECTION_SERVICE;

/**
 * Created by gavin on 2017/12/30.
 */

public class EasyScreenCap extends EasyVideoSouce{
    private final static String TAG = "EasyScreenCap";

    public Context mContext;
    private int windowWidth = 1920;
    private int windowHeight = 1080;
    private int mFrameRate = 30;
    private int mBitRate = 4000 * 1000;

    private int screenDensity;
    private MediaProjectionManager mMpmngr;
    private MediaCodec mMediaCodec;
    private Surface mSurface;
    private byte[] mPpsSps;
    private MediaCodec.BufferInfo mBufferInfo = new MediaCodec.BufferInfo();
    private MediaProjection mMpj;
    private VirtualDisplay mVirtualDisplay;
    private long lastKeyFrmaeTime;

    boolean mediaCodecRunning = false;


    public EasyScreenCap(Context context) {
        mContext = context;
    }

    public int  initScreenCapture(Intent mResultIntent, int mResultCode, int w, int h, int fps, int bitRate) {
        windowWidth = w;
        windowHeight = h;
        mFrameRate = fps;
        mBitRate = bitRate;
        WindowManager wm;
        wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(displayMetrics);
        screenDensity = displayMetrics.densityDpi;
        mMpmngr = (MediaProjectionManager) mContext.getApplicationContext().getSystemService(MEDIA_PROJECTION_SERVICE);
        mMpj = mMpmngr.getMediaProjection(mResultCode, mResultIntent);
        if (mMpj == null) {
            mMpmngr = null;
            return -1;
        }
        return 0;
    }

    public int uninitScreenCapture() {
        if (mMpj != null) {
            mMpj.stop();
        }
        mMpj = null;
        mMpmngr = null;
        return 0;
    }

    public boolean isMediaCodecRunning() {
        return mediaCodecRunning;
    }

    public int startMediaCodec() {
        synchronized (this) {
            if (mediaCodecRunning) {
                Log.e(TAG, "alread startMediaCodec");
                return -1;
            }
            lastKeyFrmaeTime = System.currentTimeMillis();
            MediaFormat mediaFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, windowWidth, windowHeight);
            mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, mBitRate);
            mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, mFrameRate);
            mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
            mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, mFrameRate);
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

            mVirtualDisplay = mMpj.createVirtualDisplay("record_screen", windowWidth, windowHeight, screenDensity,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR | DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC |
                            DisplayManager.VIRTUAL_DISPLAY_FLAG_PRESENTATION, mSurface, null, null);

            mMediaCodec.start();
            mediaCodecRunning = true;
            return 0;
        }

    }

    /**
     * 停止编码并释放编码资源占用
     */
    public void stopMediaCodec() {
        Log.i(TAG, "stopMediaCodec");
        synchronized (this) {
            if (!mediaCodecRunning) {
                return;
            }

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
            mediaCodecRunning = false;
        }
    }

    private void requestKeyFram() {
        if (mMediaCodec != null) {
            Bundle param = new Bundle();
            param.putInt(MediaCodec.PARAMETER_KEY_REQUEST_SYNC_FRAME, 0);
            mMediaCodec.setParameters(param);
        }
    }

    public byte[] getVideoOutData() {
        synchronized (this) {
            if (mediaCodecRunning) {
                int index = mMediaCodec.dequeueOutputBuffer(mBufferInfo, 10000);
                if (index == MediaCodec.INFO_TRY_AGAIN_LATER) {//请求超时
                    return null;
                } else if (index >= 0) {//有效输出
                    ByteBuffer outputBuffer = mMediaCodec.getOutputBuffer(index);
                    byte[] outData = new byte[mBufferInfo.size];
                    outputBuffer.get(outData);

                    //记录pps和sps
                    int type = outData[4] & 0x07;

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
//                       收到一个关键帧，重置关键帧时间
                        lastKeyFrmaeTime = System.currentTimeMillis();
                    }
//                  每秒过一秒申请一个关键帧
                    if (System.currentTimeMillis() - lastKeyFrmaeTime > 1000) {
                        requestKeyFram();
                        lastKeyFrmaeTime = System.currentTimeMillis();
                    }
                    mMediaCodec.releaseOutputBuffer(index, false);
                    return outData;
                }
            }
            return null;
        }
    }
}
