package org.easydarwin.easyscreenlive.base;

/**
 * Created by gavin on 2018/1/26.
 */

public interface EasyAudioStreamCallback {
    void audioDataBack(long timestamp, byte[] pBuffer, int offset, int length);
}
