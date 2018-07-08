package org.easydarwin.easyscreenlive.screen_live.base;

/**
 * Created by gavin on 2017/12/30.
 */

public interface EasyVideoStreamCallback {
    void videoDataBack(long timestamp, byte[] pBuffer, int offset, int length);
}
