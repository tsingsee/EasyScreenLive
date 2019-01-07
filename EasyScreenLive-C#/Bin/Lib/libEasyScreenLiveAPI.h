
#pragma once

#define LIB_EASYSLIVE_API __declspec(dllexport)

#define EASYSLIVE_HANDLE void*

// 接口函数定义 [12/14/2017 Dingshuai]
typedef enum tagSOURCE_TYPE
{
	SOURCE_LOCAL_CAMERA = 0,//本地音视频
	SOURCE_SCREEN_CAPTURE =1,//屏幕捕获
	SOURCE_RTSP_STREAM=2,//RTSP流
	SOURCE_RTMP_STREAM=3,//RTMP流
	SOURCE_FILE_STREAM = 4,       //文件流推送(mp4,ts,flv???)
	// 	Any other Source to push

}SOURCE_TYPE;


typedef enum tagPUSH_TYPE
{
	PUSH_NONE = 0,
	PUSH_RTSP ,
	PUSH_RTMP ,
	//ADD ANY OTHER PUSH TYPE

}PUSH_TYPE;

typedef struct tagEASYLIVE_DEVICE_INFO_T
{
	char	friendlyName[64];

	tagEASYLIVE_DEVICE_INFO_T	*pNext;
}EASYLIVE_DEVICE_INFO_T;

typedef struct tagEASYLIVE_DEVICE_LIST_T
{
	int			count;
	EASYLIVE_DEVICE_INFO_T		*pDevice;
}EASYLIVE_DEVICE_LIST_T;

typedef struct __EASYLIVE_CHANNEL_INFO_T
{
	int		id;
	int		enable_multicast;		//是否启用组播
	int videoRTPPortNum;
	int audioRTPPortNum;
	int	ttl;
	int enableFec;	// 是否使能fec
	int fecGroudSize;                       // fec组大小
	int fecParam;	// fec 冗余包  fecParam/%
	int isEnableArq; // 使能 arq
	char	multicast_addr[36];
	char	name[64];

}EASYLIVE_CHANNEL_INFO_T;

LIB_EASYSLIVE_API EASYSLIVE_HANDLE EasyScreenLive_Create(char* EasyIPCamera_Key, char* EasyRTMP_Key, char* EasyRTSP_Key, char* EasyRTSPClient_Key = NULL, char* EasyRTMPClient_Key = NULL);
LIB_EASYSLIVE_API void EasyScreenLive_Release(EASYSLIVE_HANDLE handler);

LIB_EASYSLIVE_API int EasyScreenLive_GetActiveDays(EASYSLIVE_HANDLE handler);

LIB_EASYSLIVE_API bool EasyScreenLive_IsSupportNvEncoder(EASYSLIVE_HANDLE handler);

//设置屏幕采集是否采集鼠标光标
LIB_EASYSLIVE_API int EasyScreenLive_SetCaptureCursor(EASYSLIVE_HANDLE handler, bool  bShow);

//nEncoderType 编码类型： 
//		0=默认编码器（效率最低，通用性强） 
//		1=软编码（效率高，通用性不强）
//		2=硬件编码（效率最高，通用性最低，需要英伟达独立显卡支持）
LIB_EASYSLIVE_API int EasyScreenLive_StartCapture(EASYSLIVE_HANDLE handler, SOURCE_TYPE eSourceType, char* szURL,int nCamId, int nAudioId,  EASYSLIVE_HANDLE hCapWnd, int nEncoderType,
	int nVideoWidth=640, int nVideoHeight=480, int nFps=25, int nBitRate=2048, char* szDataType = "YUY2",  //VIDEO PARAM
	int nSampleRate=44100, int nChannel=2, bool bTranscode = false);//AUDIO PARAM
//停止采集
LIB_EASYSLIVE_API void EasyScreenLive_StopCapture(EASYSLIVE_HANDLE handler);

//开始推送
LIB_EASYSLIVE_API int EasyScreenLive_StartPush(EASYSLIVE_HANDLE handler, PUSH_TYPE pushType, char* ServerIp, int nPushPort, char* sPushName, int rtpOverTcp, int nPushBufSize = 1024, bool bServerRecord = false);
//停止推送
LIB_EASYSLIVE_API void EasyScreenLive_StopPush(EASYSLIVE_HANDLE handler, PUSH_TYPE pushType);

//开始RTSP服务
LIB_EASYSLIVE_API int EasyScreenLive_StartServer(EASYSLIVE_HANDLE handler, int listenport, char *username, char *password,  EASYLIVE_CHANNEL_INFO_T *channelInfo, int channelNum);
//停止服务
LIB_EASYSLIVE_API int EasyScreenLive_StopServer(EASYSLIVE_HANDLE handler, int serverId);

// DShow 采集枚举设备
// 枚举视频采集设备
LIB_EASYSLIVE_API EASYLIVE_DEVICE_LIST_T* EasyScreenLive_GetAudioInputDevList(EASYSLIVE_HANDLE handler);
// 枚举音频采集设备
 LIB_EASYSLIVE_API EASYLIVE_DEVICE_LIST_T* EasyScreenLive_GetCameraList(EASYSLIVE_HANDLE handler);

