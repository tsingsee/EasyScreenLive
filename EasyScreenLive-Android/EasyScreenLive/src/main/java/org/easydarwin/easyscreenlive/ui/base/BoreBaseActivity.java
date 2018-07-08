package org.easydarwin.easyscreenlive.ui.base;


import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;


public class BoreBaseActivity extends AppCompatActivity {

    protected String TAG;
    private Dialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        init();
    }

    private void init() {
        TAG = getClass().getSimpleName();
        progressDialog = DialogUtils.createProgressDialog(this);
    }


    /**
     * 跳转页面,无extra简易型
     *
     * @param tarActivity 目标页面
     */
    public void intent2Activity(Class<? extends Activity> tarActivity) {
        Intent intent = new Intent(this, tarActivity);
        startActivity(intent);
    }

    public void showToast(String msg) {
        ToastUtils.showToast(this, msg, Toast.LENGTH_SHORT);
    }

    public void showLog(String msg) {
        Log.i(TAG, msg);
    }

    public void showProgressDialog() {
        progressDialog.show();
    }

    public void dismissProgressDialog() {
        progressDialog.dismiss();
    }

}
