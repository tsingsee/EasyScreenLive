package org.easydarwin.easyscreenlive;

import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import org.easydarwin.easyscreenlive.config.Config;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,
        RadioButton.OnCheckedChangeListener{

    static public  final int REQUEST_MEDIA_PROJECTION = 1002;
    static private final  String TAG = "ScreenLiveActivity";

    Context context;

    //cap screen Permission
    public static Intent mResultIntent;
    public static int mResultCode;


    private Button startButton;

    EditText editStreamId;
    EditText editRtspPort;
    EditText editMulPort;
    TextView textUrl;

    RadioButton radioButtonMulticast;
    RadioButton radioButtonUnicast;
    LinearLayout linearLayoutMulPort;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;

        setContentView(R.layout.activity_main);
        startButton             = findViewById(R.id.push_btn);
        editRtspPort            = findViewById(R.id.edit_rtsp_port);
        editStreamId            = findViewById(R.id.edit_stream_id);
        editMulPort             = findViewById(R.id.edit_multicast_port);
        textUrl                 = findViewById(R.id.url_text);
        radioButtonMulticast    = findViewById(R.id.button_multicast_type);
        radioButtonUnicast      = findViewById(R.id.button_unicast_type);
        linearLayoutMulPort     = findViewById(R.id.mul_rtsp_prot);

        editRtspPort.setText(Config.getRtspPort(this));
        editStreamId.setText(Config.getStreamName(this));
        editMulPort.setText(Config.getMulPort(this));
        startButton.setOnClickListener(this);
        radioButtonMulticast.setOnCheckedChangeListener(this);
        radioButtonUnicast.setOnCheckedChangeListener(this);

        if(Config.getLiveType(this).equals(Config.LIVE_TYPE_MULTICAST)) {
            radioButtonMulticast.setChecked(true);
        } else {
            radioButtonUnicast.setChecked(true);
        }

        if (mResultCode != 0 && mResultIntent != null) {
            if (CapScreenService.mServiceIsStart) {
                startButton.setText("停止推屏");
            } else {
                startButton.setText("开始推屏");
            }
        } else {
            startButton.setText("申请权限");
        }


        if (CapScreenService.mServiceIsStart) {
            enableLiveTypeRadioGroup(false);
            startButton.setText("停止推屏");
        } else {
            enableLiveTypeRadioGroup(true);
            startButton.setText("开始推屏");
        }

        checkCapScreenPermission();
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.push_btn:
                onPushScreen();
                break;
            default:
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
        switch (buttonView.getId()) {
            case R.id.button_multicast_type:
                Log.e(TAG,"---1---" + isChecked);
                if(isChecked){
                    linearLayoutMulPort.setVisibility(View.VISIBLE);
                    Config.saveLiveType(this, Config.LIVE_TYPE_MULTICAST);
                }
                break;
            case R.id.button_unicast_type:
                Log.e(TAG,"---2---"+ isChecked);
                if(isChecked){
                    linearLayoutMulPort.setVisibility(View.INVISIBLE);
                    Config.saveLiveType(this, Config.LIVE_TYPE_UNICAST);
                }
                break;

            default:
                break;
        }
    }

    public void saveConfig() {
        Config.saveRtspPort(context, editRtspPort.getText().toString());
        Config.saveStreamName(context, editStreamId.getText().toString());
        Config.saveMulPort(context, editMulPort.getText().toString());
    }

    public void onPushScreen() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP){
            new AlertDialog.Builder(this).
                    setMessage("推送屏幕需要安卓5.0以上,您当前系统版本过低,不支持该功能。").
                    setTitle("抱歉").show();
            return;
        }

        saveConfig();

        textUrl.setText("URL:" + Config.getRtspUrl(this));
        if (mResultCode == 0 && mResultIntent == null) {
            checkCapScreenPermission();
        } else {
            Intent intent = new Intent(getApplicationContext(), CapScreenService.class);
            if (CapScreenService.mServiceIsStart) {
                stopService(intent);
                startButton.setText("开始推屏");
                enableLiveTypeRadioGroup(true);
            } else {
                startService(intent);
                startButton.setText("停止推屏");
                enableLiveTypeRadioGroup(false);
            }
        }
    }


    public void enableLiveTypeRadioGroup( boolean enable) {
        radioButtonMulticast.setEnabled(enable);
        radioButtonUnicast.setEnabled(enable);
    }


    private void checkCapScreenPermission() {
        if (mResultCode == 0 && mResultIntent == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                MediaProjectionManager mMpMngr = (MediaProjectionManager) getApplicationContext().
                        getSystemService(MEDIA_PROJECTION_SERVICE);
                startActivityForResult(mMpMngr.createScreenCaptureIntent(), REQUEST_MEDIA_PROJECTION);
            }
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_MEDIA_PROJECTION) {
            if (resultCode == RESULT_OK) {
                mResultCode = resultCode;
                mResultIntent = data;
                startButton.setText("开始推屏");
            }
        } else {
            checkCapScreenPermission();
            Log.e(TAG, "get capture permission fail!");
        }
    }

}
