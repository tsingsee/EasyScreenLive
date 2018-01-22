package org.easydarwin.easyscreenlive;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;


import org.easydarwin.easyscreenlive.databinding.ActivityScreenLiveBinding;

public class ScreenLiveActivity extends AppCompatActivity {

    private static final String TAG = "ScreenLiveActivity";
    private  ActivityScreenLiveBinding mBinding;
    static public  final int REQUEST_MEDIA_PROJECTION = 1002;
    private Context context;
    PusherFragment pusherFragment;
    PlayListFragment playListFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        ViewDataBinding mBinder = DataBindingUtil.setContentView(this, R.layout.activity_screen_live);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_screen_live);
        setSupportActionBar(mBinding.toolbar);
        context = this;

        pusherFragment = new PusherFragment();
        playListFragment = new PlayListFragment();
        getFragmentManager().beginTransaction().replace(R.id.fragmeng_main_layout, pusherFragment).commit();


        mBinding.toolbarSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ScreenLiveActivity.this, SettingActivity.class));
            }
        });

        mBinding.toolbarAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ScreenLiveActivity.this, AboutActivity.class));
            }
        });

        mBinding.fragmengPlayList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().beginTransaction().replace(R.id.fragmeng_main_layout, playListFragment).commit();
            }
        });
        mBinding.fragmengPush.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().beginTransaction().replace(R.id.fragmeng_main_layout, pusherFragment).commit();
            }
        });

        Intent intent = new Intent(getApplicationContext(), OnLiveManagerService.class);
        startService(intent);
    }
}
