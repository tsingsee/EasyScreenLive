package org.easydarwin.easyscreenlive.ui.player;

import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.os.ResultReceiver;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Toast;

import org.easydarwin.easyscreenlive.R;
import org.easydarwin.video.Client;
import org.easydarwin.video.EasyPlayerClient;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class PreviewActivity extends AppCompatActivity implements TextureView.SurfaceTextureListener{

    private final String TAG = "PreviewActivity";
    protected EasyPlayerClient mStreamRender;
    protected ResultReceiver mResultReceiver;
    protected TextureView mSurfaceView;

    private String playUrl = "";
    private int pushType = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);
        mSurfaceView = (TextureView) findViewById(R.id.surface_view);
        mSurfaceView.setOpaque(false);
        mSurfaceView.setSurfaceTextureListener(this);
        Intent intent = getIntent();
        playUrl = intent.getStringExtra("PLAY_URL");
        pushType = intent.getIntExtra("PUSH_TYPE", 0);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }


    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
//        if (isHidden()){
//            return;
//        }
        Log.i(TAG, "onSurfaceTextureAvailable");
        startRending(surface);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        Log.i(TAG, "onSurfaceTextureSizeChanged");
//        if (mAttacher != null) {
//            mAttacher.update();
//        }
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        Log.i(TAG, "onSurfaceTextureDestroyed");
        stopRending();

        return true;
    }
    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        Log.i(TAG, "onSurfaceTextureUpdated");

    }


    private void stopRending() {
        if (mStreamRender != null) {
//            sendResult(RESULT_REND_STOPED, null);
            mStreamRender.stop();
            mStreamRender = null;
        }
    }

    protected void startRending(SurfaceTexture surface) {
        mStreamRender = new EasyPlayerClient(this, "", new Surface(surface), mResultReceiver);

//        boolean autoRecord = PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("auto_record", false);

//        File f = new File(TheApp.sMoviePath);
//        f.mkdirs();
//        String mUrl = "rtsp://192.168.1.107:8554/12345";
        try {
//            Client.TRANSTYPE_UDP : Client.TRANSTYPE_TCP
//            mStreamRender.start(mUrl, mType, Client.EASY_SDK_VIDEO_FRAME_FLAG | Client.EASY_SDK_AUDIO_FRAME_FLAG, "", "", autoRecord ? new File(f, new SimpleDateFormat("yy-MM-dd HH:mm:ss").format(new Date()) + ".mp4").getPath() : null);
            if (pushType == 1) {
//              单播
                mStreamRender.start(playUrl, Client.TRANSTYPE_UDP,
                        Client.EASY_SDK_VIDEO_FRAME_FLAG | Client.EASY_SDK_AUDIO_FRAME_FLAG, "", "", null);
            } else {
//              组播
                mStreamRender.start(playUrl, Client.TRANSTYPE_UDP,
                        Client.EASY_SDK_VIDEO_FRAME_FLAG | Client.EASY_SDK_AUDIO_FRAME_FLAG, "", "", null);
            }
        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            return;
        }
//        sendResult(RESULT_REND_STARTED, null);

    }
}
