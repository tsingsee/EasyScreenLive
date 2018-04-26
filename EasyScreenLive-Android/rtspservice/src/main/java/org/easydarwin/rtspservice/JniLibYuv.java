package org.easydarwin.rtspservice;

/**
 * Created by gavin on 2018/4/26.
 */

public class JniLibYuv {
    static {
        System.loadLibrary("yuv_shared");
        System.loadLibrary("jni_libyuv");
    }

    //argb 转yuv i420
    public static native  void argbtoi420(byte[] src_argb,
                                          byte[] dst_I420,
                                          int width, int height);

    //对yuv 片数据 进行处理 包括裁剪和旋转
    public static native  void convertToI420(byte[] src_frame, int src_size,
                                             byte[] dst_y, int dst_stride_y,
                                             byte[] dst_u, int dst_stride_u,
                                             byte[] dst_v, int dst_stride_v,
                                             int crop_x, int crop_y,
                                             int src_width, int src_height,
                                             int crop_width, int crop_height,
                                             int rotation,
                                             int format);

    //将yuv数据转为argb
    public static native void  convertToArgb(byte[] src_frame, int src_size,
                                             byte[] dst_argb, int dst_stride_argb,
                                             int crop_x, int crop_y,
                                             int src_width, int src_height,
                                             int crop_width, int crop_height,
                                             int rotation,
                                             int format);


//    public static native void yuvConvert(byte[] data, int width, int height, int mode);

    public static native void yuvI420ToNV21(byte[] i420Src, byte[] nv21Src,
                                            int width, int height);


    public static final int I420_TO_YV12 = 1; //yuv_to_yvu,
    public static final int I420_TO_NV12 = 2; //yuv_to_yuvuv,
    public static final int I420_TO_YV21 = 3; //yuv_to_yvuvu
    /**
     * @param srcdata
     * @param dstData
     * @param width
     * @param height
     * @param mode
     */
    public static native void yuvConvert(byte[] srcdata, byte[] dstData,
                                            int width, int height, int mode);
}
