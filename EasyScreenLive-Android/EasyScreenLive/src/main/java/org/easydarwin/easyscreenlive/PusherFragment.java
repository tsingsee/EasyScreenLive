package org.easydarwin.easyscreenlive;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import org.easydarwin.easyscreenlive.config.Config;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MEDIA_PROJECTION_SERVICE;
import static org.easydarwin.easyscreenlive.ScreenLiveActivity.REQUEST_MEDIA_PROJECTION;

/**
 * Created by gavin on 2018/1/20.
 */

public class PusherFragment extends Fragment {
    private final String TAG = "PusherFragment";

    public static Intent mResultIntent;
    public static int mResultCode;

    View view;
    TextView textViewStatus;
    TextView textViewUrl;
    ImageButton imageButton;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_push, container, false);
        textViewStatus  = view.findViewById(R.id.textview_status);
        textViewUrl     = view.findViewById(R.id.test_url);
        imageButton     = view.findViewById(R.id.image_button_start_push);



        if (mResultIntent== null && mResultCode == 0) {
            textViewStatus.setText("申请权限");
        } else {
            if (CapScreenService.mServiceIsStart) {
                textViewStatus.setText("停止推屏");
                textViewUrl.setText("URL:" + Config.getRtspUrl(getActivity()));
            } else {
                textViewStatus.setText("开始推屏");
                textViewUrl.setText("");
            }
        }

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPushScreen();
            }
        });

        return view;
    }

    public void onPushScreen() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP){
            new AlertDialog.Builder(getActivity()).
                    setMessage("推送屏幕需要安卓5.0以上,您当前系统版本过低,不支持该功能。").
                    setTitle("抱歉").show();
            return;
        }

        if (mResultCode == 0 && mResultIntent == null) {
            checkCapScreenPermission();
        } else {
            Intent intent = new Intent(getActivity().getApplicationContext(), CapScreenService.class);
            if (CapScreenService.mServiceIsStart) {
                getActivity().stopService(intent);
                textViewStatus.setText("开始推屏");
                textViewUrl.setText("");
            } else {
                getActivity().startService(intent);
                textViewStatus.setText("停止推屏");
                textViewUrl.setText("URL:" + Config.getRtspUrl(getActivity()));
            }
        }
    }

    private void checkCapScreenPermission() {
        if (mResultCode == 0 && mResultIntent == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                MediaProjectionManager mMpMngr = (MediaProjectionManager) getActivity().getApplicationContext().
                        getSystemService(MEDIA_PROJECTION_SERVICE);
                startActivityForResult(mMpMngr.createScreenCaptureIntent(), REQUEST_MEDIA_PROJECTION);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_MEDIA_PROJECTION) {
            if (resultCode == RESULT_OK) {
                PusherFragment.mResultCode = resultCode;
                PusherFragment.mResultIntent = data;
                textViewStatus.setText("开始推屏");
            }
        } else {
//            checkCapScreenPermission();
            Log.e(TAG, "get capture permission fail!");
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.d(TAG, "onAttach");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        if (PusherFragment.mResultCode == 0 && mResultIntent == null) {
            checkCapScreenPermission();
        }
        Log.d(TAG, "onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG, "onDetach");
    }

}
