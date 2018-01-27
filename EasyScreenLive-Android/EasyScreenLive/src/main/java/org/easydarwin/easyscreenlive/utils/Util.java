/*
	Copyright (c) 2013-2017 EasyDarwin.ORG.  All rights reserved.
	Github: https://github.com/EasyDarwin
	WEChat: EasyDarwin
	Website: http://www.easydarwin.org
*/

package org.easydarwin.easyscreenlive.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.util.Log;

import org.easydarwin.easyscreenlive.config.Config;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

/**
 * 类Util的实现描述：//TODO 类实现描述
 *
 * @author HELONG 2016/3/8 17:42
 */
public class Util {

    /**
     * 将YUV420SP数据顺时针旋转90度
     *
     * @param data        要旋转的数据
     * @param imageWidth  要旋转的图片宽度
     * @param imageHeight 要旋转的图片高度
     * @return 旋转后的数据
     */
    public static byte[] rotateNV21Degree90(byte[] data, int imageWidth, int imageHeight) {
        byte[] yuv = new byte[imageWidth * imageHeight * 3 / 2];
        // Rotate the Y luma
        int i = 0;
        for (int x = 0; x < imageWidth; x++) {
            for (int y = imageHeight - 1; y >= 0; y--) {
                yuv[i] = data[y * imageWidth + x];
                i++;
            }
        }
        // Rotate the U and V color components
        i = imageWidth * imageHeight * 3 / 2 - 1;
        for (int x = imageWidth - 1; x > 0; x = x - 2) {
            for (int y = 0; y < imageHeight / 2; y++) {
                yuv[i] = data[(imageWidth * imageHeight) + (y * imageWidth) + x];
                i--;
                yuv[i] = data[(imageWidth * imageHeight) + (y * imageWidth) + (x - 1)];
                i--;
            }
        }
        return yuv;
    }

    /**
     * 将YUV420SP数据逆时针旋转90度
     *
     * @param src        要旋转的数据
     * @param srcWidth  要旋转的图片宽度
     * @param height 要旋转的图片高度
     * @return 旋转后的数据
     */
    public static byte[] rotateNV21Negative90(byte[] src, int srcWidth, int height)
    {
        byte[] dst = new byte[srcWidth * height * 3 / 2];
        int nWidth = 0, nHeight = 0;
        int wh = 0;
        int uvHeight = 0;
        if(srcWidth != nWidth || height != nHeight)
        {
            nWidth = srcWidth;
            nHeight = height;
            wh = srcWidth * height;
            uvHeight = height >> 1;//uvHeight = height / 2
        }

        //旋转Y
        int k = 0;
        for(int i = 0; i < srcWidth; i++){
            int nPos = srcWidth - 1;
            for(int j = 0; j < height; j++)
            {
                dst[k] = src[nPos - i];
                k++;
                nPos += srcWidth;
            }
        }

        for(int i = 0; i < srcWidth; i+=2){
            int nPos = wh + srcWidth - 1;
            for(int j = 0; j < uvHeight; j++) {
                dst[k] = src[nPos - i - 1];
                dst[k + 1] = src[nPos - i];
                k += 2;
                nPos += srcWidth;
            }
        }
        return dst;
    }
    /**
     * 将YUV420SP数据顺时针旋转90度
     *
     * @param src        要旋转的数据
     * @param srcWidth  要旋转的图片宽度
     * @param srcHeight 要旋转的图片高度
     * @return 旋转后的数据
     */
    public static byte[] rotateNV21Positive90(byte[] src, int srcWidth, int srcHeight)
    {
        byte[] dst = new byte[srcWidth * srcHeight * 3 / 2];
        int nWidth = 0, nHeight = 0;
        int wh = 0;
        int uvHeight = 0;
        if(srcWidth != nWidth || srcHeight != nHeight)
        {
            nWidth = srcWidth;
            nHeight = srcHeight;
            wh = srcWidth * srcHeight;
            uvHeight = srcHeight >> 1;//uvHeight = height / 2
        }

        //旋转Y
        int k = 0;
        for(int i = 0; i < srcWidth; i++) {
            int nPos = 0;
            for(int j = 0; j < srcHeight; j++) {
                dst[k] = src[nPos + i];
                k++;
                nPos += srcWidth;
            }
        }

        for(int i = 0; i < srcWidth; i+=2){
            int nPos = wh;
            for(int j = 0; j < uvHeight; j++) {
                dst[k] = src[nPos + i];
                dst[k + 1] = src[nPos + i + 1];
                k += 2;
                nPos += srcWidth;
            }
        }
        return dst;
    }

    /**
     * 旋转YUV格式数据
     *
     * @param src    YUV数据
     * @param format 0，420P；1，420SP
     * @param width  宽度
     * @param height 高度
     * @param degree 旋转度数
     */
    public static void yuvRotate(byte[] src, int format, int width, int height, int degree) {
        // @todo
        /*
        int offset = 0;
        if (format == 0) {
            JNIUtil.rotateMatrix(src, offset, width, height, degree);
            offset += (width * height);
            JNIUtil.rotateMatrix(src, offset, width / 2, height / 2, degree);
            offset += width * height / 4;
            JNIUtil.rotateMatrix(src, offset, width / 2, height / 2, degree);
        } else if (format == 1) {
            JNIUtil.rotateMatrix(src, offset, width, height, degree);
            offset += width * height;
            JNIUtil.rotateShortMatrix(src, offset, width / 2, height / 2, degree);
        }
        */
    }

    /**
     * 保存数据到本地
     *
     * @param buffer 要保存的数据
     * @param offset 要保存数据的起始位置
     * @param length 要保存数据长度
     * @param path   保存路径
     * @param append 是否追加
     */
    public static void save(byte[] buffer, int offset, int length, String path, boolean append) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(path, append);
            fos.write(buffer, offset, length);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.flush();
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 获取摄像头支持的分辨率
     * @param context
     * @return
     */
    public static List<String> getSupportResolution(Context context){
        List<String> resolutions=new ArrayList<>();
        SharedPreferences sharedPreferences=context.getSharedPreferences(Config.PREF_NAME, Context.MODE_PRIVATE);
        String r=sharedPreferences.getString(Config.K_RESOLUTION, "");
        if(!TextUtils.isEmpty(r)){
            String[] arr=r.split(";");
            if(arr.length>0){
                resolutions= Arrays.asList(arr);
            }
        }

        return resolutions;
    }

    /**
     * 保存支持分辨率
     * @param context
     * @param value
     */
    public static void saveSupportResolution(Context context, String value){
        SharedPreferences sharedPreferences=context.getSharedPreferences(Config.PREF_NAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putString(Config.K_RESOLUTION, value).commit();
    }

    private static String intToIp(int i) {
        return (i & 0xFF ) + "." +
                ((i >> 8 ) & 0xFF) + "." +
                ((i >> 16 ) & 0xFF) + "." +
                ( i >> 24 & 0xFF) ;
    }

    /**
     * 获取IP地址
     */
    public static String getLocalIpAddress(Context context) {
        //获取wifi服务
//        WifiManager wifiManager = (WifiManager) EasyApplication.getEasyApplication().getSystemService(Context.WIFI_SERVICE);
        WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        //判断wifi是否开启
        if (wifiManager.isWifiEnabled()) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            int ipAddress = wifiInfo.getIpAddress();
            String ip = intToIp(ipAddress);
            return ip;
        } else {
            try {
                for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                    NetworkInterface intf = en.nextElement();
                    for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                        InetAddress inetAddress = enumIpAddr.nextElement();
                        if (!inetAddress.isLoopbackAddress() && !inetAddress.isLinkLocalAddress()) {
                            return inetAddress.getHostAddress().toString();
                        }
                    }
                }
            } catch (SocketException ex) {
                Log.e("getLocalIpAddress", ex.toString());
            }
            return null;
        }
    }

    /**
     * 显示视频debug信息
     */
    public static void showDbgMsg(String level, String data){
//        Intent intent = new Intent(StatusInfoView.DBG_MSG);
//        intent.putExtra(StatusInfoView.DBG_LEVEL, level);
//        intent.putExtra(StatusInfoView.DBG_DATA, data);
//        LocalBroadcastManager.getInstance(EasyApplication.getEasyApplication()).sendBroadcast(intent);
    }
}
