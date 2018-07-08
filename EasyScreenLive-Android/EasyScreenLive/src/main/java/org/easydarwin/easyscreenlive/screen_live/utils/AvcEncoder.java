package org.easydarwin.easyscreenlive.screen_live.utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import android.annotation.SuppressLint;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Environment;



public class AvcEncoder 
{
	private final static String TAG = "MeidaCodec";
	
	private int TIMEOUT_USEC = 12000;

	private MediaCodec mediaCodec;
	int m_width;
	int m_height;
	int m_framerate;
	byte[] m_info = null;
	 
	public byte[] configbyte; 


	@SuppressLint("NewApi")
	public AvcEncoder(int width, int height, int framerate, int bitrate) { 
		
		m_width  = width;
		m_height = height;
		m_framerate = framerate;
	
	    MediaFormat mediaFormat = MediaFormat.createVideoFormat("video/avc", width, height);
	    mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar);    
	    mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, width*height*5);
	    mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 30);
	    mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
	    try {
			mediaCodec = MediaCodec.createEncoderByType("video/avc");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
	    mediaCodec.start();
	    createfile();
	}
	
	private static String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/test1.h264";
	private BufferedOutputStream outputStream;
	FileOutputStream outStream;
	private void createfile(){
		File file = new File(path);
		if(file.exists()){
			file.delete();
		}
	    try {
	        outputStream = new BufferedOutputStream(new FileOutputStream(file));
	    } catch (Exception e){ 
	        e.printStackTrace();
	    }
	}

	@SuppressLint("NewApi")
	private void StopEncoder() {
	    try {
	        mediaCodec.stop();
	        mediaCodec.release();
	    } catch (Exception e){ 
	        e.printStackTrace();
	    }
	}
	
	ByteBuffer[] inputBuffers;
	ByteBuffer[] outputBuffers;

	public boolean isRuning = false;
	
	public void StopThread(){
		isRuning = false;
        try {
        	StopEncoder();
			outputStream.flush();
	        outputStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	int count = 0;

	public void StartEncoderThread(){
		Thread EncoderThread = new Thread(new Runnable() {

			@SuppressLint("NewApi")
			@Override
			public void run() {
				isRuning = true;
				byte[] input = null;
				long pts =  0;
				long generateIndex = 0;

				while (isRuning) {
//					if (MainActivity.YUVQueue.size() >0){
//						input = MainActivity.YUVQueue.poll();
//						byte[] yuv420sp = new byte[m_width*m_height*3/2];
//						NV21ToNV12(input,yuv420sp,m_width,m_height);
//						input = yuv420sp;
//					}
					if (input != null) {
						try {
							long startMs = System.currentTimeMillis();
							ByteBuffer[] inputBuffers = mediaCodec.getInputBuffers();
							ByteBuffer[] outputBuffers = mediaCodec.getOutputBuffers();
							int inputBufferIndex = mediaCodec.dequeueInputBuffer(-1);
							if (inputBufferIndex >= 0) {
								pts = computePresentationTime(generateIndex);
								ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
								inputBuffer.clear();
								inputBuffer.put(input);
								mediaCodec.queueInputBuffer(inputBufferIndex, 0, input.length, pts, 0);
								generateIndex += 1;
							}
							
							MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
							int outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, TIMEOUT_USEC);
							while (outputBufferIndex >= 0) {
								//Log.i("AvcEncoder", "Get H264 Buffer Success! flag = "+bufferInfo.flags+",pts = "+bufferInfo.presentationTimeUs+"");
								ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
								byte[] outData = new byte[bufferInfo.size];
								outputBuffer.get(outData);
								if(bufferInfo.flags == 2){
									configbyte = new byte[bufferInfo.size];
									configbyte = outData;
								}else if(bufferInfo.flags == 1){
									byte[] keyframe = new byte[bufferInfo.size + configbyte.length];
									System.arraycopy(configbyte, 0, keyframe, 0, configbyte.length);
									System.arraycopy(outData, 0, keyframe, configbyte.length, outData.length);
									
									outputStream.write(keyframe, 0, keyframe.length);
								}else{
									outputStream.write(outData, 0, outData.length);
								}

								mediaCodec.releaseOutputBuffer(outputBufferIndex, false);
								outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, TIMEOUT_USEC);
							}

						} catch (Throwable t) {
							t.printStackTrace();
						}
					} else {
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}
		});
		EncoderThread.start();
		
	}
	
	private void NV21ToNV12(byte[] nv21,byte[] nv12,int width,int height){
		if(nv21 == null || nv12 == null)return;
		int framesize = width*height;
		int i = 0,j = 0;
		System.arraycopy(nv21, 0, nv12, 0, framesize);
		for(i = 0; i < framesize; i++){
			nv12[i] = nv21[i];
		}
		for (j = 0; j < framesize/2; j+=2)
		{
		  nv12[framesize + j-1] = nv21[j+framesize];
		}
		for (j = 0; j < framesize/2; j+=2)
		{
		  nv12[framesize + j] = nv21[j+framesize-1];
		}
	}
	
    /**
     * Generates the presentation time for frame N, in microseconds.
     */
    private long computePresentationTime(long frameIndex) {
        return 132 + frameIndex * 1000000 / m_framerate;
    }
}
