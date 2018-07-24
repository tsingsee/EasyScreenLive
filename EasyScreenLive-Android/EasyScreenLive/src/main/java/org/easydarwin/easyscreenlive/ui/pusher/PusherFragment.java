package org.easydarwin.easyscreenlive.ui.pusher;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import org.easydarwin.easyscreenlive.R;
import org.easydarwin.easyscreenlive.ui.base.BaseFragment;

import org.easydarwin.easyscreenlive.screen_live.CapScreenService;
import org.easydarwin.easyscreenlive.ui.ScreenLiveActivity;
import org.easydarwin.easyscreenlive.screen_live.EasyScreenLiveAPI;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MEDIA_PROJECTION_SERVICE;

/**
 * Created by gavin on 2018/1/20.
 */

public class PusherFragment extends BaseFragment implements PusherContract.View, SurfaceHolder.Callback{
    private final String TAG = "PusherFragment";

    static public Intent mResultIntent;
    static public int mResultCode;


    View view;
    static public SurfaceView mSurfaceView;
    TextView textViewStatus;
    TextView textViewUrl;
    ImageButton imageButton;
    private PusherContract.Presenter presenter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_push, container, false);
        textViewStatus  = view.findViewById(R.id.textview_status);
        textViewUrl     = view.findViewById(R.id.test_url);
        imageButton     = view.findViewById(R.id.image_button_start_push);
        mSurfaceView = (SurfaceView) view.findViewById(R.id.sv_surfaceview);
        mSurfaceView.getHolder().addCallback(this);

        if (mResultIntent== null && mResultCode == 0) {
            textViewStatus.setText("申请权限");
        }

        if (presenter != null) {
            presenter.initView(getActivity());
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
        if (presenter != null) {
            if (presenter.getPushStatus() == EasyScreenLiveAPI.EASY_PUSH_SERVICE_STATUS.STATUS_LEISURE) {
                showMultiBtnDialog();
            } else {
                presenter.onStartPush(getActivity(),
                        CapScreenService.EASY_PUSH_SERVICE_CMD.CMD_STOP_PUSH,
                        mResultIntent,
                        mResultCode,null);
                mSurfaceView.setBackground(getResources().getDrawable(R.color.white_background));
            }
        }

    }

    /* @setNeutralButton 设置中间的按钮
    * 若只需一个按钮，仅设置 setPositiveButton 即可
    */
    private void showMultiBtnDialog(){
        final String[] items = { "横屏推送", "竖屏推送","前置摄像头","后置摄像头"};
        AlertDialog.Builder listDialog =
                new AlertDialog.Builder(getActivity());
        listDialog.setTitle("选择推送内容");
        listDialog.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (presenter != null) {
                    presenter.onStartPush(getActivity(), which,
                            PusherFragment.mResultIntent,
                            PusherFragment.mResultCode,
                            mSurfaceView);
                }
            }
        });
        listDialog.show();
    }


    @Override
    public void changeViewStatus(int status, String URL) {
        switch (status) {
            case EasyScreenLiveAPI.EASY_PUSH_SERVICE_STATUS.STATUS_LEISURE:
                textViewStatus.setText(getActivity().getString(R.string.wait_push));
                imageButton.setBackground(getActivity().getDrawable(R.drawable.ic_start));
                mSurfaceView.setBackground(getResources().getDrawable(R.color.white_background));
                break;
            case EasyScreenLiveAPI.EASY_PUSH_SERVICE_STATUS.STATUS_PUSH_CAMREA_BACK:
                imageButton.setBackground(getActivity().getDrawable(R.drawable.ic_stop));
                textViewStatus.setText(getActivity().getString(R.string.back_camera_pushing));
                mSurfaceView.setBackground(getResources().getDrawable(R.color.transparent_background));
                break;
            case EasyScreenLiveAPI.EASY_PUSH_SERVICE_STATUS.STATUS_PUSH_CAMREA_FRONT:
                imageButton.setBackground(getActivity().getDrawable(R.drawable.ic_stop));
                textViewStatus.setText(getActivity().getString(R.string.front_camera_pushing));
                mSurfaceView.setBackground(getResources().getDrawable(R.color.transparent_background));
                break;
            case EasyScreenLiveAPI.EASY_PUSH_SERVICE_STATUS.STATUS_PUSH_SCREEN:
                imageButton.setBackground(getActivity().getDrawable(R.drawable.ic_stop));
                textViewStatus.setText(getActivity().getString(R.string.screen_pushing));
                mSurfaceView.setBackground(getResources().getDrawable(R.color.white_background));
                break;
            default:
                    break;
        }

        textViewUrl.setText("URL:" + URL);
    }

    private void checkCapScreenPermission() {
        if (mResultCode == 0 && mResultIntent == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                MediaProjectionManager mMpMngr = (MediaProjectionManager) getActivity().getApplicationContext().
                        getSystemService(MEDIA_PROJECTION_SERVICE);
                startActivityForResult(mMpMngr.createScreenCaptureIntent(), ScreenLiveActivity.REQUEST_MEDIA_PROJECTION);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ScreenLiveActivity.REQUEST_MEDIA_PROJECTION) {
            if (resultCode == RESULT_OK) {
                PusherFragment.mResultCode = resultCode;
                PusherFragment.mResultIntent = data;
                textViewStatus.setText(R.string.wait_push);
            }
        } else {
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
        if (mResultCode == 0 && mResultIntent == null) {
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
        if (presenter != null) {
            //presenter.onStopPush();
        }
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

    @Override
    public void showProgress() {
        showProgressDialog();
    }

    @Override
    public void dismissProgress() {
        dismissProgressDialog();
    }

    @Override
    public void showTip(String message) {
        showToast(message);
    }

    @Override
    public boolean isActive() {
        return isAdded();
    }

    @Override
    public void setPresenter(PusherContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }

}
