package org.easydarwin.easyscreenlive.ui;

import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.text.style.URLSpan;

import org.easydarwin.easyscreenlive.R;
import org.easydarwin.easyscreenlive.databinding.ActivityAboutBinding;


public class AboutActivity extends AppCompatActivity {

    private ActivityAboutBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        DataBindingUtil.setContentView(this, R.layout.activity_about);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_about);
        setSupportActionBar(binding.toolbar);
        {
            binding.title.setText("EasyPlayer RTSP播放器：");
            binding.desc.setText("EasyScreenLive是一款简单、高效、稳定的集采集，编码，" +
                    "推流和流媒体RTSP服务于一身的通用库，具低延时，高效能，低丢包等特点。" +
                    "目前支持Windows，Android平台，通过EasyScreenLive我们就可以避免接触到稍显复杂的音视频源采集，" +
                    "编码和流媒体推送以及RTSP/RTP/RTCP服务流程，只需要调用EasyScreenLive的几个API接口，就能轻松、" +
                    "稳定地把流媒体音视频数据推送给EasyDSS服务器以及发布RTSP服务，RTSP服务支持组播和单播两种模式。项目地址：");

            binding.desc.setMovementMethod(LinkMovementMethod.getInstance());
            SpannableString spannableString = new SpannableString("https://github.com/EasyDSS/easyscreenlive");
            //设置下划线文字
            spannableString.setSpan(new URLSpan("https://github.com/EasyDSS/easyscreenlive"), 0, spannableString.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

            //设置文字的前景色
            spannableString.setSpan(new ForegroundColorSpan(Color.RED), 0, spannableString.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            binding.desc.append(spannableString);

        }
    }
}
