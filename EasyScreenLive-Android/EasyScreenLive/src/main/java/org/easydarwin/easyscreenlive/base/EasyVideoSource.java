package org.easydarwin.easyscreenlive.base;

/**
 * Created by gavin on 2018/1/25.
 */

abstract public class EasyVideoSource {
    public static final int  SOURCE_TYPE_CAMERA = 0;
    public static final int  SOURCE_TYPE_SCREEN = 1;

    public int SOURCE_TYPE;
    public EasyVideoStreamCallback easyVideoStreamCallback;
    public abstract int init(int w, int h, int fps, int bitRate, EasyVideoStreamCallback cb);
    public abstract int uninit();
    public abstract int startStream();
    public abstract int stopStream();
}
