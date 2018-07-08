package org.easydarwin.easyscreenlive.screen_live.hw;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;


import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static android.media.MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedPlanar;
import static android.media.MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar;
import static android.media.MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar;
import static android.media.MediaCodecInfo.CodecCapabilities.COLOR_TI_FormatYUV420PackedSemiPlanar;


/**
 * Created by apple on 2017/5/13.
 */
public class HWConsumer extends Thread implements VideoConsumer {
    private static final String TAG = "Pusher";
    private final Context mContext;
    private int mHeight;
    private int mWidth;
    private MediaCodec mMediaCodec;
    private ByteBuffer[] inputBuffers;
    private ByteBuffer[] outputBuffers;
    private volatile boolean mVideoStarted;
    private MediaFormat newFormat;
    private CodecInfo codecInfo;

    public HWConsumer(Context context, String mMimeType) {
        mContext = context;

        ArrayList<CodecInfo> infos = listEncoders(mMimeType);
        for(int i=0; i<infos.size(); i++ ) {
            CodecInfo ci = infos.get(i);
            Log.i(TAG, "support CodecName:" + ci.mMimeType + "  inColorFormat:"+ ci.mInColorFormat);
        }
        codecInfo = new CodecInfo();

        CodecInfo ci = infos.get(0);
        codecInfo.mMimeType = mMimeType;
        codecInfo.mCodecOutName = ci.mCodecOutName;
        codecInfo.mInColorFormat = ci.mInColorFormat;
    }

    public static int getColorFormat(MediaCodecInfo codecInfo, String mimeType) {
        MediaCodecInfo.CodecCapabilities capabilities = codecInfo.getCapabilitiesForType(mimeType);
        int[] cf = new int[capabilities.colorFormats.length];
        System.arraycopy(capabilities.colorFormats, 0, cf, 0, cf.length);
        List<Integer> sets = new ArrayList<>();
        for (int i = 0; i < cf.length; i++) {
            sets.add(cf[i]);
        }
        if (sets.contains(COLOR_FormatYUV420SemiPlanar)) {
            return COLOR_FormatYUV420SemiPlanar;
        } else if (sets.contains(COLOR_FormatYUV420Planar)) {
            return COLOR_FormatYUV420Planar;
        } else if (sets.contains(COLOR_FormatYUV420PackedPlanar)) {
            return COLOR_FormatYUV420PackedPlanar;
        } else if (sets.contains(COLOR_TI_FormatYUV420PackedSemiPlanar)) {
            return COLOR_TI_FormatYUV420PackedSemiPlanar;
        }
        return 0;
    }

    public static boolean codecMatch(String mimeType, MediaCodecInfo codecInfo) {
        String[] types = codecInfo.getSupportedTypes();
        for (String type : types) {
            if (type.equalsIgnoreCase(mimeType)) {
                return true;
            }
        }
        return false;
    }

    public static ArrayList<CodecInfo> listEncoders(String mime) {
        // 可能有多个编码库，都获取一下。。。
        ArrayList<CodecInfo> codecInfos = new ArrayList<CodecInfo>();
        int numCodecs = MediaCodecList.getCodecCount();
        // int colorFormat = 0;
        // String name = null;
        for (int i1 = 0; i1 < numCodecs; i1++) {
            MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i1);
            if (!codecInfo.isEncoder()) {
                continue;
            }
            if (codecMatch(mime, codecInfo)) {
                String name = codecInfo.getName();
                int colorFormat = getColorFormat(codecInfo, mime);
                if (colorFormat != 0) {
                    CodecInfo ci = new CodecInfo();
                    ci.mMimeType = mime;
                    ci.mCodecOutName = name;
                    ci.mInColorFormat = colorFormat;
                    codecInfos.add(ci);
                }
            }
        }
        return codecInfos;
    }


    @Override
    public void onVideoStart(int width, int height) throws IOException {
        newFormat = null;
        this.mWidth = width;
        this.mHeight = height;
        startMediaCodec();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP + 1) {
            inputBuffers = outputBuffers = null;
        } else {
            inputBuffers = mMediaCodec.getInputBuffers();
            outputBuffers = mMediaCodec.getOutputBuffers();
        }
        start();
        mVideoStarted = true;
    }

    final int millisPerframe = 1000 / 20;
    long lastPush = 0;

    @Override
    public int onVideo(byte[] data, int format) {
        if (!mVideoStarted) return 0;

        try {
            if (lastPush == 0) {
                lastPush = System.currentTimeMillis();
            }
            long time = System.currentTimeMillis() - lastPush;
            if (time >= 0) {
                time = millisPerframe - time;
                if (time > 0) Thread.sleep(time / 2);
            }

            // TODO: 2018/4/26

            if (codecInfo.mInColorFormat == COLOR_FormatYUV420SemiPlanar) {
//                yuvuv_to_yvuvu
//                JNIUtil.yuvConvert(data, mWidth, mHeight, 6);
            } else if (codecInfo.mInColorFormat == COLOR_TI_FormatYUV420PackedSemiPlanar) {
//                JNIUtil.yuvConvert(data, mWidth, mHeight, 6);
            } else if (codecInfo.mInColorFormat == COLOR_FormatYUV420Planar) {
//                yuvuv_to_yvu
//                JNIUtil.yuvConvert(data, mWidth, mHeight, 5);
            } else {
//                JNIUtil.yuvConvert(data, mWidth, mHeight, 5);
            }

            int bufferIndex = mMediaCodec.dequeueInputBuffer(0);
            if (bufferIndex >= 0) {
                ByteBuffer buffer = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    buffer = mMediaCodec.getInputBuffer(bufferIndex);
                } else {
                    buffer = inputBuffers[bufferIndex];
                }
                buffer.clear();
                buffer.put(data);
                buffer.clear();
                mMediaCodec.queueInputBuffer(bufferIndex, 0, data.length, System.nanoTime() / 1000, MediaCodec.BUFFER_FLAG_KEY_FRAME);
            }
            if (time > 0) Thread.sleep(time / 2);
            lastPush = System.currentTimeMillis();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    @Override
    public void run() {
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
//        int outputBufferIndex = 0;
        byte[] mPpsSps = new byte[0];
        byte[] h264 = new byte[mWidth * mHeight];
        do {
            int outputBufferIndex = mMediaCodec.dequeueOutputBuffer(bufferInfo, 10000);
            if (outputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                // no output available yet
            } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                // not expected for an encoder
                outputBuffers = mMediaCodec.getOutputBuffers();
            } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                synchronized (HWConsumer.this) {
                    newFormat = mMediaCodec.getOutputFormat();
                }
            } else if (outputBufferIndex < 0) {
                // let's ignore it
            } else {
                ByteBuffer outputBuffer;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    outputBuffer = mMediaCodec.getOutputBuffer(outputBufferIndex);
                } else {
                    outputBuffer = outputBuffers[outputBufferIndex];
                }
                outputBuffer.position(bufferInfo.offset);
                outputBuffer.limit(bufferInfo.offset + bufferInfo.size);

                boolean sync = false;
                if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {// sps
                    sync = (bufferInfo.flags & MediaCodec.BUFFER_FLAG_SYNC_FRAME) != 0;
                    if (!sync) {
                        byte[] temp = new byte[bufferInfo.size];
                        outputBuffer.get(temp);
                        mPpsSps = temp;
                        mMediaCodec.releaseOutputBuffer(outputBufferIndex, false);
                        continue;
                    } else {
                        mPpsSps = new byte[0];
                    }
                }
                sync |= (bufferInfo.flags & MediaCodec.BUFFER_FLAG_SYNC_FRAME) != 0;
                int len = mPpsSps.length + bufferInfo.size;
                if (len > h264.length) {
                    h264 = new byte[len];
                }
                if (sync) {
                    System.arraycopy(mPpsSps, 0, h264, 0, mPpsSps.length);
                    outputBuffer.get(h264, mPpsSps.length, bufferInfo.size);
//                    mPusher.push(h264, 0, mPpsSps.length + bufferInfo.size, bufferInfo.presentationTimeUs / 1000, 1);
                } else {
                    outputBuffer.get(h264, 0, bufferInfo.size);
//                    mPusher.push(h264, 0, bufferInfo.size, bufferInfo.presentationTimeUs / 1000, 1);
                }


                mMediaCodec.releaseOutputBuffer(outputBufferIndex, false);
            }
        }
        while (mVideoStarted);
    }

    @Override
    public void onVideoStop() {
        do {
            newFormat = null;
            mVideoStarted = false;
            try {
                join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (isAlive());
        if (mMediaCodec != null) {
            stopMediaCodec();
            mMediaCodec = null;
        }
    }


    /**
     * 初始化编码器
     */
    private void startMediaCodec() throws IOException {
            /*
        SD (Low quality) SD (High quality) HD 720p
1 HD 1080p
1
Video resolution 320 x 240 px 720 x 480 px 1280 x 720 px 1920 x 1080 px
Video frame rate 20 fps 30 fps 30 fps 30 fps
Video bitrate 384 Kbps 2 Mbps 4 Mbps 10 Mbps
        */
        int framerate = 20;
//        if (width == 640 || height == 640) {
//            bitrate = 2000000;
//        } else if (width == 1280 || height == 1280) {
//            bitrate = 4000000;
//        } else {
//            bitrate = 2 * width * height;
//        }

        int bitrate = (int) (mWidth * mHeight * 20 * 2 * 0.05f);
        if (mWidth >= 1920 || mHeight >= 1920) bitrate *= 0.3;
        else if (mWidth >= 1280 || mHeight >= 1280) bitrate *= 0.4;
        else if (mWidth >= 720 || mHeight >= 720) bitrate *= 0.6;

        mMediaCodec = MediaCodec.createByCodecName(codecInfo.mMimeType);
        MediaFormat mediaFormat = MediaFormat.createVideoFormat(codecInfo.mMimeType, mWidth, mHeight);
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitrate);
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, framerate);
        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, codecInfo.mInColorFormat);
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
        mMediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mMediaCodec.start();

        Bundle params = new Bundle();
        params.putInt(MediaCodec.PARAMETER_KEY_REQUEST_SYNC_FRAME, 0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mMediaCodec.setParameters(params);
        }
    }

    /**
     * 停止编码并释放编码资源占用
     */
    private void stopMediaCodec() {
        mMediaCodec.stop();
        mMediaCodec.release();
    }

}
