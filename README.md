# EasyScreenLive #

EasyScreenLive是由[紫鲸云](http://www.pvale.com "紫鲸云")团队开发和运维的一款简单、高效、稳定的集采集、编码、组播、推流和流媒体RTSP服务于一身的同屏功能组件，具低延时，高性能，低丢包率等特点。目前支持Windows，Android两个平台，通过EasyScreenLive我们就可以避免接触到稍显复杂的音视频源采集，编码和流媒体推送以及RTSP/RTP/RTCP/RTMP服务流程，只需要调用EasyScreenLive的几个API接口，就能轻松、稳定地把流媒体音视频数据RTMP推送给EasyDSS等RTMP服务器以及发布RTSP Server服务，RTSP同屏服务支持组播和单播两种模式。

**使用场景**：大屏显示投屏，无纸化会议同屏演示，课堂同屏等，可以配合全屏显示，反向模拟触控实现远程控制功能（Android控制Windows，Windows控制Android，Windows控制Windows等）

## 调用示例 ##

- **EasyScreenLive_Android**：实时采集安卓采集手机桌面屏幕（Android 5.0及5.0+支持），进行H264/AAC编码后，调用libEasyIPCamera进行同屏直播；

- **EasyScreenLive_win32**：实时采集USB摄像头或者桌面屏幕，以及音频输入设备，进行H264/AAC编码后，调用libEasyIPCamera进行同屏直播，调用libEasyRTMP推行RTMP到EasyDSS等RTMP协议流媒体服务器；


	Windows编译方法，

    	Visual Studio 2010 编译：.\EasyScreenLive_win32\libEasyScreenLive\libEasyScreenLive.sln

	Android编译方法，
		
		Android Studio编译：EasyScreenLive-Android

- **我们同时提供Windows、Android版本的libEasyScreenLive库**：

	<table>
	<tr><td><b>支持平台</b></td><td><b>芯片</b></td></tr>
	<tr><td>Windows</td><td>x86</td></tr>
	<tr><td>Windows</td><td>x64</td></tr>
	<tr><td>Android</td><td>armeabi</td></tr>
	<tr><td>Android</td><td>armeabi-v7a</td></tr>
	<tr><td>Android</td><td>arm64-v8a</td></tr>

	</table>


## 特殊说明 ##

libEasyScreenLive windows版本库支持输入源:

typedef enum tagSOURCE_TYPE

{
	SOURCE_LOCAL_CAMERA = 0,	//本地音视频
	
    SOURCE_SCREEN_CAPTURE =1,	//屏幕捕获
      
	SOURCE_FILE_STREAM = 2,		//文件流推送(mp4,ts,flv???)

}SOURCE_TYPE;

libEasyScreenLive windows版本库支持视频(H264)编码器:

typedef enum tagENCODER_TYPE

{
	ENCODER_DEFAULT 		= 0, //默认编码器
	
	ENCODER_FASTSOFTWARE 	= 1,//快速软编码
	
	ENCODER_FASTHARDWARE 	= 2,//快速硬件编码，需要Nvidia显卡支持

}ENCODER_TYPE;

libEasyScreenLive windows版本库支持推送流:

typedef enum tagPUSH_TYPE

{
	PUSH_NONE = 0,
	
	PUSH_RTSP ,		//推送RTSP
	
	PUSH_RTMP ,		//推送RTMP
	
}PUSH_TYPE;

## 最新版本下载 ##

- 历史版本下载：[https://github.com/EasyDSS/EasyScreenLive/releases](https://github.com/EasyDSS/EasyScreenLive/releases)

- Android 版：[https://fir.im/EasyScreenLive](https://fir.im/EasyScreenLive)

> EasyScreenLive中已经内嵌了对应的播放器功能，如果需要使用额外的播放器来进行播放，可选择使用EasyPlayer-RTSP播放器：[https://github.com/EasyDSS/EasyPlayer-RTSP](https://github.com/EasyDSS/EasyPlayer-RTSP "EasyPlayer-RTSP")

![EasyScreenLive-Android](http://www.easydarwin.org/github/images/easyscreenlive/easyscreenlivefirimandroid201802.png)



Windows版本截图如下所示：
![EasyScreenLive](http://img.blog.csdn.net/20171229174054227?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvU3dvcmRUd2VsdmU=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)



延时对比：
![Delay](http://img.blog.csdn.net/20180118144954476?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvU3dvcmRUd2VsdmU=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)


硬件解码
![HardCodecPlayer](http://img.blog.csdn.net/20180118145734798?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvU3dvcmRUd2VsdmU=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

Android版本延时对比

![android_delay](https://github.com/EasyDSS/EasyScreenLive/raw/master/screenshots/android_delay.jpg)


## 技术支持 ##

- 邮件：[support@easydarwin.org](mailto:support@easydarwin.org) 

- QQ交流群：[694451013](https://jq.qq.com/?_wv=1027&k=5GaYB7K "EasyScreenLive")

> **我们同时提供Windows、Android版本的EasyScreenLive同屏技术**：EasyScreenLive商业使用需要经过授权才能永久使用，商业授权方案可以通过以上渠道进行更深入的技术与合作咨询；


## 获取更多信息 ##

**EasyDarwin**开源流媒体服务器：[www.EasyDarwin.org](http://www.easydarwin.org)

**EasyDSS**商用流媒体解决方案：[www.EasyDSS.com](http://www.easydss.com)

**EasyNVR**无插件直播方案：[www.EasyNVR.com](http://www.easynvr.com)

Copyright &copy; EasyDarwin Team 2012-2018

![EasyDarwin](http://www.easydarwin.org/skin/easydarwin/images/wx_qrcode.jpg)
