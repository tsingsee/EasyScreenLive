package org.easydarwin.easyscreenlive.ui.setting;

import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioButton;

import org.easydarwin.easyscreenlive.R;
import org.easydarwin.easyscreenlive.config.Config;
import org.easydarwin.easyscreenlive.databinding.ActivitySettingBinding;

public class SettingActivity extends AppCompatActivity implements View.OnClickListener,
        RadioButton.OnCheckedChangeListener{
    private final String TAG = "SettingActivity";

    boolean eanbleMulticast = false;
    boolean enableAudioPush = false;
    boolean enableFrame     = false;
    boolean enableFec = false;
    boolean enableArq = false;
    private ActivitySettingBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_setting);

        mBinding.editRtspPort.setText(Config.getRtspPort(this));
        mBinding.editStreamId.setText(Config.getStreamName(this));
        mBinding.editMulticastPort.setText(Config.getMulPort(this));
        mBinding.editBitRate.setText(""+Config.getBitRate(this));
        mBinding.editFecGroudSize.setText(""+Config.getFecGroudSize(this));
        mBinding.editFecParam.setText(""+Config.getFecParam(this));


        mBinding.buttonSave.setOnClickListener(this);
        mBinding.buttonMulticastType.setOnCheckedChangeListener(this);
        mBinding.buttonUnicastType.setOnCheckedChangeListener(this);
        mBinding.switchEnableAudio.setOnClickListener(this);
        mBinding.switchEnableFrame.setOnClickListener(this);
        mBinding.switchEnableFec.setOnClickListener(this);
        mBinding.switchEnableArq.setOnClickListener(this);

        if(Config.getLiveType(this).equals(Config.LIVE_TYPE_MULTICAST)) {
            eanbleMulticast = true;
            mBinding.buttonMulticastType.setChecked(true);
        } else {
            eanbleMulticast = false;
            mBinding.buttonUnicastType.setChecked(true);
        }

        if(Config.getEnableAudio(this).equals("1")) {
            enableAudioPush = true;
            mBinding.switchEnableAudio.setChecked(true);
        } else {
            enableAudioPush = false;
            mBinding.switchEnableAudio.setChecked(false);
        }

        if(Config.getEnableFrame(this).equals("1")) {
            enableFrame = true;
            mBinding.switchEnableFrame.setChecked(true);
        } else {
            enableFrame = false;
            mBinding.switchEnableFrame.setChecked(false);
        }

        if(Config.getEnableArq(this).equals("1")) {
            enableArq = true;
            mBinding.switchEnableArq.setChecked(true);
        } else {
            enableArq = false;
            mBinding.switchEnableArq.setChecked(false);
        }

        if(Config.getEnablefec(this).equals("1")) {
            enableFec = true;
            mBinding.switchEnableFec.setChecked(true);
        } else {
            enableFec = false;
            mBinding.switchEnableFec.setChecked(false);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.switch_enable_audio:
                if(mBinding.switchEnableAudio.isChecked()) {
                    enableAudioPush = true;
                } else {
                    enableAudioPush = false;
                }
                break;
            case R.id.switch_enable_frame:
                if(mBinding.switchEnableFrame.isChecked()) {
                    enableFrame = true;
                } else {
                    enableFrame = false;
                }
                break;
            case R.id.button_save:
                saveConfig();
                break;
            case R.id.switch_enable_fec:
                if(mBinding.switchEnableFec.isChecked()) {
                    enableFec = true;
                } else {
                    enableFec = false;
                }
                break;
            case R.id.switch_enable_arq:
                if(mBinding.switchEnableArq.isChecked()) {
                    enableArq = true;
                } else {
                    enableArq = false;
                }
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
        Config.saveRtspPort(this, mBinding.editRtspPort.getText().toString());
        Config.saveStreamName(this, mBinding.editStreamId.getText().toString());
        Config.saveMulPort(this, mBinding.editMulticastPort.getText().toString());
        Config.saveBitRate(this, Integer.parseInt(mBinding.editBitRate.getText().toString()));
        Config.saveFecGroudSize(this, Integer.parseInt(mBinding.editFecGroudSize.getText().toString()));
        Config.saveFecParam(this, Integer.parseInt(mBinding.editFecParam.getText().toString()));

        if (eanbleMulticast) {
            Config.saveLiveType(this, Config.LIVE_TYPE_MULTICAST);
        } else {
            Config.saveLiveType(this, Config.LIVE_TYPE_UNICAST);
        }
        if (enableAudioPush) {
            Config.saveEnableAudio(this, "1");
        } else {
            Config.saveEnableAudio(this,"0");
        }
        if (enableFrame) {
            Config.saveEnableFrame(this, "1");
        } else {
            Config.saveEnableFrame(this,"0");
        }

        if (enableArq) {
            Config.saveEnableArq(this, "1");
        } else {
            Config.saveEnableArq(this,"0");
        }
        if (enableFec) {
            Config.saveEnablefec(this, "1");
        } else {
            Config.saveEnablefec(this,"0");
        }
    }
}
