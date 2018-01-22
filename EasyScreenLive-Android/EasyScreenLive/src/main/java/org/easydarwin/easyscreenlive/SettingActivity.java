package org.easydarwin.easyscreenlive;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;

import org.easydarwin.easyscreenlive.config.Config;

public class SettingActivity extends AppCompatActivity implements View.OnClickListener,
        RadioButton.OnCheckedChangeListener{
    private final String TAG = "SettingActivity";

    private Button saveButton;
    EditText editStreamId;
    EditText editRtspPort;
    EditText editMulPort;
    RadioButton radioButtonMulticast;
    RadioButton radioButtonUnicast;
    boolean eanbleMulticast = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewDataBinding mBinder = DataBindingUtil.setContentView(this, R.layout.activity_setting);
//        setSupportActionBar(mBinder);

        saveButton             = findViewById(R.id.button_save);
        editRtspPort            = findViewById(R.id.edit_rtsp_port);
        editStreamId            = findViewById(R.id.edit_stream_id);
        editMulPort             = findViewById(R.id.edit_multicast_port);
        radioButtonMulticast    = findViewById(R.id.button_multicast_type);
        radioButtonUnicast      = findViewById(R.id.button_unicast_type);


        editRtspPort.setText(Config.getRtspPort(this));
        editStreamId.setText(Config.getStreamName(this));
        editMulPort.setText(Config.getMulPort(this));

        saveButton.setOnClickListener(this);
        radioButtonMulticast.setOnCheckedChangeListener(this);
        radioButtonUnicast.setOnCheckedChangeListener(this);

        if(Config.getLiveType(this).equals(Config.LIVE_TYPE_MULTICAST)) {
            eanbleMulticast = true;
            radioButtonMulticast.setChecked(true);
        } else {
            eanbleMulticast = false;
            radioButtonUnicast.setChecked(true);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_save:
                saveConfig();
                break;
            default:
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
        switch (buttonView.getId()) {
            case R.id.button_multicast_type:
                if(isChecked){
                    eanbleMulticast = true;
                }
                break;
            case R.id.button_unicast_type:
                if(isChecked){
                    eanbleMulticast = false;
                }
                break;

            default:
                break;
        }
    }

    public void saveConfig() {
        Config.saveRtspPort(this, editRtspPort.getText().toString());
        Config.saveStreamName(this, editStreamId.getText().toString());
        Config.saveMulPort(this, editMulPort.getText().toString());

        if (eanbleMulticast) {
            Config.saveLiveType(this, Config.LIVE_TYPE_MULTICAST);
        } else {
            Config.saveLiveType(this, Config.LIVE_TYPE_UNICAST);
        }
    }
}
