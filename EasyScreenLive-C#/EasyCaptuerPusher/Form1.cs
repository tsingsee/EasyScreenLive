using System;
using System.Windows.Forms;
using CapturePusher;
using System.Net;
using System.Net.Sockets;
using System.Runtime.InteropServices;

namespace EasyCaptuerPusher
{
    public partial class Form1 : Form
    {
        IntPtr pusherPtr = IntPtr.Zero;
        IntPtr pusherSubPtr = IntPtr.Zero;

        bool isInit = false;
        bool isPusherRtsp = false;
        bool isPusherRtmp = false;
        bool isCapture = false;
        bool isServerOpen = false;
        public Form1()
        {
            InitializeComponent();
        }

        private void Form1_Load(object sender, EventArgs e)
        {
        }

        private void Form1_FormClosing(object sender, FormClosingEventArgs e)
        {
            if (isPusherRtsp)
                CapturePusher.CapturePusherSDK.EasyScreenLive_StopPush(pusherPtr, PUSH_TYPE.PUSH_RTSP);
            if (isPusherRtmp)
                CapturePusher.CapturePusherSDK.EasyScreenLive_StopPush(pusherPtr, PUSH_TYPE.PUSH_RTMP);
            if (isServerOpen)
                CapturePusher.CapturePusherSDK.EasyScreenLive_StopServer(pusherPtr);
            if (isCapture)
                CapturePusher.CapturePusherSDK.EasyScreenLive_StopCapture(pusherPtr);
            if(pusherSubPtr != IntPtr.Zero)
            {
                if (isPusherRtsp)
                    CapturePusher.CapturePusherSDK.EasyScreenLive_StopPush(pusherSubPtr, PUSH_TYPE.PUSH_RTSP);
                if (isPusherRtmp)
                    CapturePusher.CapturePusherSDK.EasyScreenLive_StopPush(pusherSubPtr, PUSH_TYPE.PUSH_RTMP);
                if (isServerOpen)
                    CapturePusher.CapturePusherSDK.EasyScreenLive_StopServer(pusherSubPtr);
                if (isCapture)
                    CapturePusher.CapturePusherSDK.EasyScreenLive_StopCapture(pusherSubPtr);
            }

        }

        private void btnStop_Click(object sender, EventArgs e)
        {
            if (!isCapture) return;
            if (!isServerOpen)
            {
                if (!isInit) return;
                int serverPort = int.Parse(textBox5.Text);
                int enable_multicast = comboBox2.Text == "组播" ? 1 : 0;
                string multicast_addr = textBox6.Text;
                short ttl = short.Parse(textBox4.Text);
                EASYLIVE_CHANNEL_INFO_T[] stru = new EASYLIVE_CHANNEL_INFO_T[3];
                int size = (Marshal.SizeOf(typeof(EASYLIVE_CHANNEL_INFO_T)));
                IntPtr pData = Marshal.AllocHGlobal(size * 3);

                for (int i=0; i<3; i++)
                {
                    stru[i].videoRTPPortNum = 6000 + i * 2;
                    stru[i].audioRTPPortNum = 6001 + i * 2;
                    //stru[i].videoRTPPortNum = 6000 ;
                    //stru[i].audioRTPPortNum = 6002;
                    stru[i].enable_multicast = enable_multicast;
                    stru[i].multicast_addr = multicast_addr;
                    stru[i].ttl = ttl; 
                    stru[i].name = "channel=" + i;
                    stru[i].id = i;
                    Marshal.StructureToPtr(stru[i], pData+size*i,true);
                }
                //var struStr = new EASYLIVE_CHANNEL_INFO_T[] { stru };
                int ret = 0;

                ret = CapturePusher.CapturePusherSDK.EasyScreenLive_StartServer(pusherPtr, serverPort, string.Empty, string.Empty, pData, 1);
                isServerOpen = ret == 0;
                Log(string.Format("开启RTSP服务: rtsp://{0}:{1}/channel=0 {2}", GetLocalIP(), serverPort, isServerOpen ? "成功" : "失敗"));
                if (pusherSubPtr != IntPtr.Zero)
                {
                    ret = CapturePusher.CapturePusherSDK.EasyScreenLive_StartServer(pusherSubPtr, serverPort+1, string.Empty, string.Empty, pData, 1);
                    isServerOpen = ret == 0;
                    Log(string.Format("开启RTSP服务: rtsp://{0}:{1}/channel=0 {2}", GetLocalIP(), serverPort+1, isServerOpen ? "成功" : "失敗"));
                }


                if (isServerOpen)
                    btnStop.Text = "Stop";
                Marshal.FreeHGlobal(pData);//Release memory space.  
            }
            else
            {
                CapturePusher.CapturePusherSDK.EasyScreenLive_StopServer(pusherPtr);
                if (pusherSubPtr != IntPtr.Zero)
                {
                    CapturePusher.CapturePusherSDK.EasyScreenLive_StopServer(pusherSubPtr);
                }
                btnStop.Text = "RTSP Server";
                isServerOpen = false;
                Log("停止RTSP服务");
            }
        }

        private void btnCapture_Click(object sender, EventArgs e)
        {
            pusherPtr = CapturePusher.CapturePusherSDK.EasyScreenLive_Create();
            isInit = pusherPtr != IntPtr.Zero;

            //pusherSubPtr = CapturePusher.CapturePusherSDK.EasyScreenLive_Create();
            
            if (!isInit)
                return;
            if (!isCapture)
            {
                EncoderType encodeType = (EncoderType)Enum.Parse(typeof(EncoderType), this.comboBox1.Text);
                SOURCE_TYPE captureType = this.CaptureType.Text == "屏幕采集" ? SOURCE_TYPE.SOURCE_SCREEN_CAPTURE : SOURCE_TYPE.SOURCE_RTSP_STREAM;
                string szDataType = captureType == SOURCE_TYPE.SOURCE_LOCAL_CAMERA ? "YUY2" : "RGB24";
                int captureRet = 0;
                captureRet = CapturePusher.CapturePusherSDK.EasyScreenLive_StartCapture(pusherPtr, captureType, "rtsp://127.0.0.1:8554/channel=0",/*this.panel1.Handle*/IntPtr.Zero, encodeType, szDataType, 960, 540, 1024, false);
                if (pusherSubPtr != IntPtr.Zero)
                {
                    captureRet = CapturePusher.CapturePusherSDK.EasyScreenLive_StartCapture(pusherSubPtr, captureType, "rtsp://127.0.0.1:8554/channel=0", this.panel1.Handle, encodeType, szDataType, 960, 540, 1024, true);
                }
                isCapture = captureRet >= 0;

                if (isCapture)
                    btnCapture.Text = "Stop";
                Log(string.Format("開啓{0}{1}", this.CaptureType.Text, isCapture ? "成功" : "失敗"));
            }
            else
            {
                if (pusherPtr != IntPtr.Zero)
                {
                    if (isPusherRtsp)
                    {
                        CapturePusher.CapturePusherSDK.EasyScreenLive_StopPush(pusherPtr, PUSH_TYPE.PUSH_RTSP);
                        this.button1.Text = "RTSP Push";
                    }
                    if (isPusherRtmp)
                    {
                        CapturePusher.CapturePusherSDK.EasyScreenLive_StopPush(pusherPtr, PUSH_TYPE.PUSH_RTMP);
                        this.button2.Text = "RTMP Push";
                    }
                    if (isServerOpen)
                    {
                        CapturePusher.CapturePusherSDK.EasyScreenLive_StopServer(pusherPtr);
                        this.btnStop.Text = "RTSP Server";
                    }
                    CapturePusher.CapturePusherSDK.EasyScreenLive_StopCapture(pusherPtr);
                    if (isInit)
                        CapturePusher.CapturePusherSDK.EasyScreenLive_Release(pusherPtr);
                }

                if (pusherSubPtr != IntPtr.Zero)
                {
                    if (isPusherRtsp)
                    {
                        CapturePusher.CapturePusherSDK.EasyScreenLive_StopPush(pusherSubPtr, PUSH_TYPE.PUSH_RTSP);
                        this.button1.Text = "RTSP Push";
                    }
                    if (isPusherRtmp)
                    {
                        CapturePusher.CapturePusherSDK.EasyScreenLive_StopPush(pusherSubPtr, PUSH_TYPE.PUSH_RTMP);
                        this.button2.Text = "RTMP Push";
                    }
                    if (isServerOpen)
                    {
                        CapturePusher.CapturePusherSDK.EasyScreenLive_StopServer(pusherSubPtr);
                        this.btnStop.Text = "RTSP Server";
                    }
                    CapturePusher.CapturePusherSDK.EasyScreenLive_StopCapture(pusherSubPtr);
                    if (isInit)
                        CapturePusher.CapturePusherSDK.EasyScreenLive_Release(pusherSubPtr);
                }
                isCapture = false;

                Log("停止采集");
                btnCapture.Text = "Capture";
            }
        }

        private void button1_Click(object sender, EventArgs e)
        {
            if (isPusherRtsp)
            {
                CapturePusher.CapturePusherSDK.EasyScreenLive_StopPush(pusherPtr, PUSH_TYPE.PUSH_RTSP);
                isPusherRtsp = false;
                button1.Text = "RTSP Push";
                Log("停止rtsp推送!");
            }
            else
            {
                string streamIp = this.textBox3.Text;
                int streamPort = int.Parse(this.textBox2.Text);
                string streamName = this.textBox1.Text;
                int pusherret = CapturePusher.CapturePusherSDK.EasyScreenLive_StartPush(pusherPtr, PUSH_TYPE.PUSH_RTSP, streamIp, streamPort, streamName);
                isPusherRtsp = pusherret == 0;
                if (isPusherRtsp)
                {
                    button1.Text = "Stop";
                    Log(string.Format("开启RTSP推送:rtsp://{0}:{1}/{2}", streamIp, streamPort, streamName));
                }
            }
        }

        private void button2_Click(object sender, EventArgs e)
        {
            if (isPusherRtmp)
            {
                CapturePusher.CapturePusherSDK.EasyScreenLive_StopPush(pusherPtr, PUSH_TYPE.PUSH_RTMP);
                isPusherRtmp = false;
                button2.Text = "RTMP Push";
                Log("停止rtmp推送!");
            }
            else
            {
                string streamIp = this.tbIP.Text;
                int streamPort = int.Parse(this.tbPort.Text);
                string streamName = this.tbStream.Text;
                int pusherret = CapturePusher.CapturePusherSDK.EasyScreenLive_StartPush(pusherPtr, PUSH_TYPE.PUSH_RTMP, streamIp, streamPort, streamName, bServerRecord: true);
                isPusherRtmp = pusherret == 0;
                if (isPusherRtmp)
                {
                    button2.Text = "Stop";
                    Log(string.Format("开启RTMP推送:rtmp://{0}:{1}/hls/{2}", streamIp, streamPort, streamName));
                }
            }
        }

        private void Log(string message)
        {
            this.richTextBox1.AppendText(DateTime.Now.ToString() + "  :" + message + "\n");
        }

        /// <summary>  
        /// 获取当前使用的IP  
        /// </summary>  
        /// <returns></returns>  
        private static string GetLocalIP()
        {
            try
            {
                string HostName = Dns.GetHostName(); //得到主机名
                IPHostEntry IpEntry = Dns.GetHostEntry(HostName);
                for (int i = 0; i < IpEntry.AddressList.Length; i++)
                {
                    //从IP地址列表中筛选出IPv4类型的IP地址
                    //AddressFamily.InterNetwork表示此IP为IPv4,
                    //AddressFamily.InterNetworkV6表示此地址为IPv6类型
                    if (IpEntry.AddressList[i].AddressFamily == AddressFamily.InterNetwork)
                    {
                        return IpEntry.AddressList[i].ToString();
                    }
                }
                return "";
            }
            catch (Exception ex)
            {
                MessageBox.Show("获取本机IP出错:" + ex.Message);
                return "";
            }
        }
    }
}
