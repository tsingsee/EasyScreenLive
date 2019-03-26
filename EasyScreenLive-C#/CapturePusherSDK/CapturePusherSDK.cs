using System;
using System.Collections.Generic;
using System.Linq;
using System.Runtime.InteropServices;
using System.Windows.Forms;

namespace CapturePusher
{
    public class CapturePusherSDK
    {
        /// <summary>
        /// 初始化推送接口，返回推送句柄
        /// </summary>
        /// <returns></returns>
        public static IntPtr EasyScreenLive_CreateEx()
        {
            var ret = EasyScreenLive_Create();
            return ret;
        }

        [DllImport(@"Lib\libEasyScreenLive.dll", CallingConvention = CallingConvention.Cdecl, EntryPoint = "?EasyScreenLive_Create@@YAPAXPAD0000@Z")]
        private static extern IntPtr EasyScreenLive_Create(
            string EasyIPCamera_Key = "6D72754B7A4969576B5A7341753242636F3539457065314659584E3555324E795A57567554476C325A53356C65475653567778576F502B6C3430566863336C4559584A33615735555A57467453584E55614756435A584E30514449774D54686C59584E35",
            string EasyRTMP_Key = "79397037795969576B5A754144474A636F35337A4A65314659584E3555324E795A57567554476C325A53356C65475775567778576F502B6C3430566863336C4559584A33615735555A57467453584E55614756435A584E30514449774D54686C59584E35",
            string EasyRTSP_Key = "6A36334A743469576B5A7341753242636F3539457065314659584E3555324E795A57567554476C325A53356C65475857567778576F502B6C3430566863336C4559584A33615735555A57467453584E55614756435A584E30514449774D54686C59584E35",
            string RTMPClient_KEY = null,
            string RTSPClient_KEY = null
            );

        /// <summary>
        /// 推送實例銷毀
        /// </summary>
        /// <param name="pusherHandle">EasyPusher_Create返回值</param>
        [DllImport(@"Lib\libEasyScreenLive.dll", CallingConvention = CallingConvention.Cdecl, EntryPoint = "?EasyScreenLive_Release@@YAXPAX@Z")]
        public static extern void EasyScreenLive_Release(IntPtr pusherHandle);

        /// <summary>
        /// 判断系统是否支持英伟达硬件编码
        /// </summary>
        /// <param name="pusherHandle">EasyScreenLive_IsSupportNvEncoder返回值</param>
        [DllImport(@"Lib\libEasyScreenLive.dll", CallingConvention = CallingConvention.Cdecl, EntryPoint = "?EasyScreenLive_IsSupportNvEncoder@@YA_NPAX@Z")]
        public static extern bool EasyScreenLive_IsSupportNvEncoder(IntPtr pusherHandle);

        /// <summary>
        /// 设置屏幕采集是否采集鼠标光标
        /// </summary>
        /// <param name="pusherHandle">EasyScreenLive_IsSupportNvEncoder返回值</param>
        [DllImport(@"Lib\libEasyScreenLive.dll", CallingConvention = CallingConvention.Cdecl, EntryPoint = "?EasyScreenLive_SetCaptureCursor@@YAHPAX_N@Z")]
        public static extern int EasyScreenLive_SetCaptureCursor(IntPtr pusherHandle, bool bShow);
        
        /// <summary>
        /// 開始音視頻數據捕捉
        /// 返回1正常
        /// </summary>
        /// <param name="pusherHandle">EasyPusher_Create返回值</param>
        /// <param name="eSourceType">推送數據源</param>
        /// <param name="hCapWnd">預覽句柄</param>
        /// <param name="encodeType">编码类型</param>
        /// <param name="szDataType">推送數據類型</param>
        /// <param name="nBitRate">比特率</param>
        /// <returns></returns>
        public static int EasyScreenLive_StartCapture(IntPtr pusherHandle, SOURCE_TYPE eSourceType, string szURL, IntPtr hCapWnd, EncoderType encodeType, string szDataType = "RGB24", int nWidth=1280, int nHeight=720, int nBitRate = 2048, bool bTranscode=false, ENCODE_MODE encMode=0)
        {
            var ret = EasyScreenLive_StartCapture(pusherHandle, eSourceType, szURL, 0, 0, hCapWnd, encodeType, nVideoWidth : nWidth, nVideoHeight: nHeight, nBitRate: nBitRate, szDataType: szDataType, bTransCode:bTranscode, encodeMode:encMode);

            return ret;
        }

        /// <summary>
        /// 開始音視頻數據捕捉
        /// 返回1正常
        /// </summary>
        /// <param name="pusherHandle">EasyPusher_Create返回值</param>
        /// <param name="eSourceType">推送數據源</param>
        /// <param name="nCamId">視頻設備ID</param>
        /// <param name="nAudioId">音頻設備ID</param>
        /// <param name="hCapWnd">預覽句柄</param>
        /// <param name="nVideoWidth"></param>
        /// <param name="nVideoHeight"></param>
        /// <param name="nFps"></param>
        /// <param name="nBitRate">比特率</param>
        /// <param name="szDataType">推送數據類型</param>
        /// <param name="nSampleRate"></param>
        /// <param name="nChannel">推送通道號</param>
        /// <returns></returns>
        [DllImport(@"Lib\libEasyScreenLive.dll", CallingConvention = CallingConvention.Cdecl, EntryPoint = "?EasyScreenLive_StartCapture@@YAHPAXW4tagSOURCE_TYPE@@PADHH0HHHHH2HH_NW4tagENCODE_MODE@@@Z")]
        private static extern int EasyScreenLive_StartCapture(IntPtr pusherHandle, SOURCE_TYPE eSourceType, string szURL, int nCamId, int nAudioId, IntPtr hCapWnd, EncoderType encodeType,
            int nVideoWidth = 1280, int nVideoHeight = 720, int nFps = 30, int nBitRate = 1024, string szDataType = "RGB24",  //VIDEO PARAM
            int nSampleRate = 44100, int nChannel = 2, bool bTransCode = false, ENCODE_MODE encodeMode = 0);

        /// <summary>
        /// 停止數據采集
        /// </summary>
        /// <param name="pusherHandle"></param>
        [DllImport(@"Lib\libEasyScreenLive.dll", CallingConvention = CallingConvention.Cdecl, EntryPoint = "?EasyScreenLive_StopCapture@@YAXPAX@Z")]
        public static extern void EasyScreenLive_StopCapture(IntPtr pusherHandle);
        /// <summary>
        /// 開始數據推送
        /// 返回0正常
        /// </summary>
        /// <param name="pusher">EasyPusher_Create返回值</param>
        /// <param name="pushType">推送類型</param>
        /// <param name="ServerIp">流媒體服務器地址</param>
        /// <param name="nPushPort">流媒體服務器端口</param>
        /// <param name="sPushName">流名稱</param>
        /// <param name="nPushBufSize">默認緩衝</param>
        /// <returns></returns>
        [DllImport(@"Lib\libEasyScreenLive.dll", CallingConvention = CallingConvention.Cdecl, EntryPoint = "?EasyScreenLive_StartPush@@YAHPAXW4tagPUSH_TYPE@@PADH2HH_N@Z")]
        public static extern int EasyScreenLive_StartPush(IntPtr pusher, PUSH_TYPE pushType, string ServerIp, int nPushPort, string sPushName, int rtpOverTcp, int nPushBufSize = 1024, bool bServerRecord = false);

        /// <summary>
        /// 停止推送
        /// </summary>
        /// <param name="pusher">EasyPusher_Create返回值</param>
        /// <param name="pushType">推送類型</param>
        [DllImport(@"Lib\libEasyScreenLive.dll", CallingConvention = CallingConvention.Cdecl, EntryPoint = "?EasyScreenLive_StopPush@@YAXPAXW4tagPUSH_TYPE@@@Z")]
        public static extern void EasyScreenLive_StopPush(IntPtr pusher, PUSH_TYPE pushType);

        /// <summary>
        /// 开始RTSP服务,返回0正常
        /// 注意:進程名稱需要為 EasyScreenLive.exe
        /// </summary>
        /// <param name="pusher">集采句柄</param>
        /// <param name="listenport">服务端口</param>
        /// <param name="username">用户名(可空)</param>
        /// <param name="password">密码(可空)</param>
        /// <param name="channelInfo">通道信息</param>
        /// <param name="channelNum">服务支持通道数，默认为1</param>
        [DllImport(@"Lib\libEasyScreenLive.dll", CallingConvention = CallingConvention.Cdecl, EntryPoint = "?EasyScreenLive_StartServer@@YAHPAXHPAD1PAU__EASYLIVE_CHANNEL_INFO_T@@H@Z")]

        public static extern int EasyScreenLive_StartServer(IntPtr pusher, int listenport, string username, string password, /*ref EASYLIVE_CHANNEL_INFO_T*/IntPtr infosIntptr, int channelNum = 1);

        /// <summary>
        /// 停止服务
        /// </summary>
        /// <param name="pusher">EasyPusher_Create返回值</param>
        /// <param name="pushType">推送類型</param>
        [DllImport(@"Lib\libEasyScreenLive.dll", CallingConvention = CallingConvention.Cdecl, EntryPoint = "?EasyScreenLive_StopServer@@YAHPAXH@Z")]
        public static extern int EasyScreenLive_StopServer(IntPtr serverHandle, int serverId);
    }

    public enum SOURCE_TYPE
    {
        SOURCE_LOCAL_CAMERA = 0,//本地音视频
        SOURCE_SCREEN_CAPTURE = 1,//屏幕捕获
        SOURCE_RTSP_STREAM = 2,//RTSP流
        SOURCE_RTMP_STREAM = 3,//RTMP流
        SOURCE_FILE_STREAM = 4,       //文件流推送(mp4,ts,flv???)
    }

    public enum PUSH_TYPE
    {
        PUSH_NONE = 0,
        PUSH_RTSP,
        PUSH_RTMP,
        //ADD ANY OTHER PUSH TYPE
    }

    /// <summary>
    /// 编码类型
    /// </summary>
    public enum EncoderType
    {
        默认编码器 = 0,//（效率最低，通用性强）
        快速软编码 = 1,//（效率高，通用性不强）
        快速硬编码 = 2//效率最高，通用性最低，需要英伟达独立显卡支持
    }
    public enum ENCODE_MODE
    {
        H264 = 0,
        H265 = 1
    }
    [StructLayoutAttribute(LayoutKind.Sequential, CharSet = CharSet.Ansi, Pack = 1)]
    public struct EASYLIVE_CHANNEL_INFO_T
    {
        public int id;
        public int enable_multicast;       //是否启用组播
        public int videoRTPPortNum;
        public int audioRTPPortNum;
        public int  ttl;
        public int enableFec;   // 是否使能fec
        public int fecGroudSize;// fec组大小
        public int fecParam;    // fec 冗余包  fecParam/%
        public int isEnableArq; // 使能 arq
        [MarshalAs(UnmanagedType.ByValTStr, SizeConst = 36)]
        public string multicast_addr;
        [MarshalAs(UnmanagedType.ByValTStr, SizeConst = 64)]
        public string name;
    }
}
