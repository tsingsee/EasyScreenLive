// easyscreenlive_Console.cpp : 定义控制台应用程序的入口点。
//

#include "stdafx.h"
#include "../bin/libEasyScreenLiveAPI.h"
#pragma comment(lib, "../bin/libEasyScreenLive.lib")
#include <string.h>
#include <WinSock2.h>
#pragma comment(lib,"ws2_32")        //链接到ws2_32动态链接库


#define EASY_RTSP_KEY "79397037795969576B5A7341596A5261706375647066464659584E355548567A614756794C6D56345A534E58444661672F365867523246326157346D516D466962334E68514449774D545A4659584E355247467964326C75564756686257566863336B3D"

//#define EASY_RTMP_KEY "79397037795969576B5A75416D7942617064396A4575314659584E3555324E795A57567554476C325A53356C65475570567778576F50365334456468646D6C754A6B4A68596D397A595541794D4445325257467A65555268636E6470626C526C5957316C59584E35"
#define EASY_RTMP_KEY "79397037795969576B5A734178643961705341356B75684659584E3555324E795A57567554476C325A5639446232786C4C6D56345A56634D5671442B6B75424859585A7062695A4359574A76633246414D6A41784E6B566863336C4559584A33615735555A5746745A57467A65513D3D"


//#define EASY_IPC_KEY   "6D72754B7A4969576B5A75416D7942617064396A4575314659584E3555324E795A57567554476C325A53356C65475570567778576F50365334456468646D6C754A6B4A68596D397A595541794D4445325257467A65555268636E6470626C526C5957316C59584E35"
#define EASY_IPC_KEY "6D72754B7A4969576B5A754147546862704D666D5965684659584E3555324E795A57567554476C325A5639446232786C4C6D56345A56634D5671442F3465424859585A7062695A4359574A76633246414D6A41784E6B566863336C4559584A33615735555A5746745A57467A65513D3D"

const char* server_ip = "www.easydss.com";
int server_port = 10085;
int encode_bitrate = 2048;
const char* stream_name = "Sword";
void* g_pusher = NULL;
SOURCE_TYPE g_sourceType = SOURCE_SCREEN_CAPTURE;
// 	SOURCE_LOCAL_CAMERA = 0,//本地音视频
// 	SOURCE_SCREEN_CAPTURE =1,//屏幕捕获
// 	SOURCE_FILE_STREAM = 2,       //文件流推送(mp4,ts,flv???)
// 	SOURCE_RTSP_STREAM=3,//RTSP流
// 	SOURCE_RTMP_STREAM=4,//RTMP流
//nEncoderType 编码类型： 
//		0=默认编码器（效率最低，通用性强） 
//		1=软编码（效率高，通用性不强）
//		2=硬件编码（效率最高，通用性最低，需要英伟达独立显卡支持）
int g_nEncoderType = 1;
bool g_bRecord = true ;

#define MAX_CHANNEL_NUM 1

#include <string>
using namespace std;

int GetLocalIP( std::string &local_ip )  
{  
	WSADATA wsaData = {0};  
	if (WSAStartup(MAKEWORD(2, 1), &wsaData) != 0)  
		return -1;  
	char szHostName[MAX_PATH] = {0};  
	int nRetCode;  
	nRetCode = gethostname(szHostName, sizeof(szHostName));  
	PHOSTENT hostinfo;  
	if (nRetCode != 0)  
		return WSAGetLastError();          
	hostinfo = gethostbyname(szHostName);  
	local_ip = inet_ntoa(*(struct in_addr*)*hostinfo->h_addr_list);  
	WSACleanup();  
	return 1;  
} 

int _tmain(int argc, _TCHAR* argv[])
{
	if(!g_pusher )
		g_pusher =  EasyScreenLive_Create(EASY_IPC_KEY, EASY_RTMP_KEY, EASY_RTSP_KEY);

	//1 采集
	int ret = EasyScreenLive_StartCapture(g_pusher, g_sourceType, NULL, -1, -1, NULL, g_nEncoderType, 1920,1080,25, encode_bitrate, (char*)("RGB24"),44100,2);

	//2 推送
	//ret = EasyScreenLive_StartPush(g_pusher, PUSH_RTMP, (char*)server_ip, server_port,  (char*)stream_name, 1,1024, g_bRecord );
	//2.1 RTSPServer
	EASYLIVE_CHANNEL_INFO_T	liveChannel[MAX_CHANNEL_NUM];
	memset(&liveChannel[0], 0x00, sizeof(EASYLIVE_CHANNEL_INFO_T)*MAX_CHANNEL_NUM);
	for (int i=0; i<MAX_CHANNEL_NUM; i++)
	{
		liveChannel[i].id = i;
		//strcpy(liveChannel[i].name, channel[i].name);
		sprintf(liveChannel[i].name, "channel=%d", i);
#if 1
		if (i==0)
		{
			liveChannel[i].enable_multicast = 0;
			strcpy(liveChannel[i].multicast_addr, ("238.255.255.255"));//"238.255.255.255"
			liveChannel[i].ttl = 255 ;//255;
		}
#endif
	}
	ret = EasyScreenLive_StartServer(g_pusher, 8554, "", "",  liveChannel, MAX_CHANNEL_NUM );
	string ip;
	GetLocalIP(ip);

	printf("start stream: rtsp://%s:8554/channel=0\n", ip.c_str() );

	printf("Press enter key to exit!!!\n");
	getchar();

	//3 停止推送	
	EasyScreenLive_StopPush(g_pusher, PUSH_RTMP);
	//4 停止采集	
	EasyScreenLive_StopCapture(g_pusher);
	//5. 销毁实例
	EasyScreenLive_Release(g_pusher);

	return 0; 
}

