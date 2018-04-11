/*
	Copyright (c) 2012-2017 EasyDarwin.ORG.  All rights reserved.
	Github: https://github.com/EasyDarwin
	WEChat: EasyDarwin
	Website: http://www.easydarwin.org
*/

package org.easydarwin.easyscreenlive.config;

import android.content.Context;
import android.content.SharedPreferences;

import org.easydarwin.easyscreenlive.utils.Util;

import static android.content.Context.MODE_PRIVATE;

/**
 * 类Config的实现描述：//TODO 类实现描述
 *
 * Created by Kim on 8/8/2016.
 */
public class Config {
    public static final String SERVER_PORT = "serverPort";
    public static final String STREAM_ID = "streamId";
    public static final String DEFAULT_SERVER_PORT = "8554";
//    public static final String DEFAULT_STREAM_ID = String.valueOf((int) (Math.random() * 1000000 + 100000));
    public static final String DEFAULT_STREAM_ID = String.valueOf((int) (12345));
    public static final String PREF_NAME = "easy_pref";
    public static final String K_RESOLUTION = "k_resolution";
    public static final String LIVE_TYPE = "easy_live_type";
    public static final String LIVE_MUL_PORT = "easy_mul_port";
    public static final String ENABLE_AUDIO_PUSH = "enable_audio_push";


    public static final String LIVE_TYPE_MULTICAST = "1";
    public static final String LIVE_TYPE_UNICAST   = "0";

    private static final String LIVE_BIT_RATE = "live_bit_rate";
    private static final String LIVE_FEC_ENABLE = "live_fec_enable";
    private static final String LIVE_FEC_PARAM = "live_fec_param";

    public static void saveStringIntoPref(Context context, String key, String value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(CommonConstants.SP_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

    static public void saveEnableAudio(Context context,String value) {
        saveStringIntoPref(context, Config.ENABLE_AUDIO_PUSH,  value);
    }

    static public  String getEnableAudio(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(CommonConstants.SP_NAME, MODE_PRIVATE);
        String liveType = sharedPreferences.getString(Config.ENABLE_AUDIO_PUSH, "0");
        return liveType;
    }

    static public void saveEnablefec(Context context,String value) {
        saveStringIntoPref(context, Config.LIVE_FEC_ENABLE,  value);
    }

    static public  String getEnablefec(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(CommonConstants.SP_NAME, MODE_PRIVATE);
        String liveType = sharedPreferences.getString(Config.LIVE_FEC_ENABLE, "0");
        return liveType;
    }

    static  public int getFecParam(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(CommonConstants.SP_NAME, MODE_PRIVATE);
        String param = sharedPreferences.getString(Config.LIVE_FEC_PARAM, "40");
        int fecParam = Integer.parseInt(param);
        return fecParam;
    }

    static public void saveFecParam(Context context, int fecParam) {
        saveStringIntoPref(context,Config.LIVE_FEC_PARAM, ""+fecParam);
    }

    static public void saveMulPort(Context context,String value ) {
        saveStringIntoPref(context, Config.LIVE_MUL_PORT,  value);
    }

    public static String getMulPort(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(CommonConstants.SP_NAME, MODE_PRIVATE);
        String liveType = sharedPreferences.getString(Config.LIVE_MUL_PORT, "1234");
        return liveType;
    }

    static public void saveLiveType(Context context,String value ) {
        saveStringIntoPref(context, Config.LIVE_TYPE,  value);
    }

    public static String getLiveType(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(CommonConstants.SP_NAME, MODE_PRIVATE);
        String liveType = sharedPreferences.getString(Config.LIVE_TYPE, Config.LIVE_TYPE_UNICAST);
        return liveType;
    }


    static public void  saveRtspPort(Context context,String value ){
        saveStringIntoPref(context, Config.SERVER_PORT,  value);
    }

    public static String getRtspPort(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(CommonConstants.SP_NAME, MODE_PRIVATE);
        String port = sharedPreferences.getString(Config.SERVER_PORT, Config.DEFAULT_SERVER_PORT);
        return port;
    }

    static public void saveStreamName(Context context,String value ){
        saveStringIntoPref(context,Config.STREAM_ID, value);
    }

    static  public String getStreamName(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(CommonConstants.SP_NAME, MODE_PRIVATE);
        String id = sharedPreferences.getString(Config.STREAM_ID, Config.DEFAULT_STREAM_ID);
        return id;
    }

    static public String getRtspUrl(Context context) {
        final String strId =Config.getStreamName(context);
        String strPort = Config.getRtspPort(context);
        String url = "rtsp://"+ Util.getLocalIpAddress(context)+":"+strPort+"/"+strId;
        return url;
    }

    static  public int getBitRate(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(CommonConstants.SP_NAME, MODE_PRIVATE);
        String bitRate = sharedPreferences.getString(Config.LIVE_BIT_RATE, "2048");
        int _bitRate = Integer.parseInt(bitRate);
        return _bitRate;
    }

    static public void saveBitRate(Context context, int bitRate) {
        saveStringIntoPref(context,Config.LIVE_BIT_RATE, ""+bitRate);
    }

}
