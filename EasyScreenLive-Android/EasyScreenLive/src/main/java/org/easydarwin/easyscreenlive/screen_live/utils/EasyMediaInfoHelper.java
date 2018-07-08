package org.easydarwin.easyscreenlive.screen_live.utils;

import android.content.Context;
import android.util.Base64;

import org.easydarwin.easyscreenlive.screen_live.hw.EncoderDebugger;
import org.easydarwin.rtspservice.JniEasyScreenLive;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by gavin on 2017/12/30.
 */
public class EasyMediaInfoHelper {
    private byte[] tmpData = new byte[255];

    private int u32VideoCodec ;				/* 视频编码类型 */
    private int u32VideoFps;				/* 视频帧率 */

    private int u32AudioCodec;				/* 音频编码类型 */
    private int u32AudioSamplerate;		/* 音频采样率 */
    private int u32AudioChannel;			/* 音频通道数 */
    private int u32AudioBitsPerSample;		/* 音频采样精度 */

    private int u32VpsLength;			/* 视频vps帧长度 */
    private int u32SpsLength;			/* 视频sps帧长度 */
    private int u32PpsLength;			/* 视频pps帧长度 */
    private int u32SeiLength;			/* 视频sei帧长度 */
    private byte[] mVps = new byte[255];
    private byte[] mSps = new byte[255];
    private byte[] mPps = new byte[128];
    private byte[] mMei = new byte[128];

    public EasyMediaInfoHelper() {
    }

    public void setAACMediaInfo(int u32AudioSamplerate, int u32AudioChannel, int u32AudioBitsPerSample) {
        u32AudioCodec = JniEasyScreenLive.AudioCodec.EASY_SDK_AUDIO_CODEC_AAC;
        this.u32AudioChannel = u32AudioChannel;
        this.u32AudioSamplerate = u32AudioSamplerate;
        this.u32AudioBitsPerSample = u32AudioBitsPerSample;
    }

    public void setH264MediaInfo(Context context, int w, int h, int frameRate) {
        u32VideoCodec = JniEasyScreenLive.VideoCodec.EASY_SDK_VIDEO_CODEC_H264;
        u32VideoFps = frameRate;
        EncoderDebugger debugger = EncoderDebugger.debug(context, w, h, frameRate);
        mSps = Base64.decode(debugger.getB64SPS(), Base64.NO_WRAP);
        mPps = Base64.decode(debugger.getB64PPS(), Base64.NO_WRAP);
        u32SpsLength = mSps.length;
        u32PpsLength = mPps.length;
        u32VpsLength = 0;
        u32SeiLength = 0;
    }

    public void fillMediaInfo(byte[] mediaInfo) {
        ByteBuffer buffer = ByteBuffer.wrap(mediaInfo);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(u32VideoCodec);
        buffer.putInt(u32VideoFps);

        buffer.putInt(u32AudioCodec);
        buffer.putInt(u32AudioSamplerate);
        buffer.putInt(u32AudioChannel);
        buffer.putInt(u32AudioBitsPerSample);
//        buffer.putInt(CapScreenService.audioStream.getAudioEncCodec());
//        buffer.putInt(CapScreenService.audioStream.getSamplingRate());
//        buffer.putInt(CapScreenService.audioStream.getChannelNum());
//        buffer.putInt(CapScreenService.audioStream.getBitsPerSample());

        buffer.putInt(u32VpsLength);//vps length
        buffer.putInt(u32SpsLength);
        buffer.putInt(u32PpsLength);
        buffer.putInt(u32SeiLength);
        buffer.put(mVps);
        if (mVps.length<255) {
            buffer.put(tmpData, 0, 255 - mVps.length);
        }
        buffer.put(mSps,0,mSps.length);
        if(mSps.length < 255) {
            buffer.put(tmpData, 0, 255 - mSps.length);
        }
        buffer.put(mPps,0,mPps.length);
        if(mPps.length < 128) {
            buffer.put(tmpData, 0, 128 - mPps.length);
        }
        buffer.put(mMei);
        if(mMei.length < 128) {
            buffer.put(tmpData, 0, 128 - mMei.length);
        }
    }
}
