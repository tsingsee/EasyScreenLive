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
        int serverId0 = -1;
        int serverId1 = -1;
        int serverId2 = -1;
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
            pusherPtr = CapturePusher.CapturePusherSDK.EasyScreenLive_CreateEx();

        }

        private void Form1_FormClosing(object sender, FormClosingEventArgs e)
        {
            // 取消关闭窗体
            e.Cancel = true;
            // 将窗体变为最小化
            this.WindowState = FormWindowState.Minimized;
            this.ShowInTaskbar = false; //不显示在系统任务栏   

        }

        // 托盘双击显示
        private void notifyIcon1_MouseDoubleClick(object sender, MouseEventArgs e)
        {
            if (this.WindowState == FormWindowState.Minimized)
            {
                this.Show();
                this.WindowState = FormWindowState.Normal;
                this.ShowInTaskbar = true;
                
            }
        }

        // 托盘右键退出
        private void toolStripMenuItem1_Click(object sender, EventArgs e)
        {
            if (isPusherRtsp)
                CapturePusher.CapturePusherSDK.EasyScreenLive_StopPush(pusherPtr, PUSH_TYPE.PUSH_RTSP);
            if (isPusherRtmp)
                CapturePusher.CapturePusherSDK.EasyScreenLive_StopPush(pusherPtr, PUSH_TYPE.PUSH_RTMP);
            if (isServerOpen)
            {
                CapturePusher.CapturePusherSDK.EasyScreenLive_StopServer(pusherPtr, serverId0);
                CapturePusher.CapturePusherSDK.EasyScreenLive_StopServer(pusherPtr, serverId1);
                CapturePusher.CapturePusherSDK.EasyScreenLive_StopServer(pusherPtr, serverId2);
            }
            if (isCapture)
                CapturePusher.CapturePusherSDK.EasyScreenLive_StopCapture(pusherPtr);
            if (isInit)
                CapturePusher.CapturePusherSDK.EasyScreenLive_Release(pusherPtr);

            Application.Exit();
        }

        private void btnCapture_Click(object sender, EventArgs e)
        {
            isInit = pusherPtr != IntPtr.Zero;

            //pusherSubPtr = CapturePusher.CapturePusherSDK.EasyScreenLive_Create();
            
            if (!isInit)
                return;
            if (!isCapture)
            {
                EncoderType encodeType = (EncoderType)Enum.Parse(typeof(EncoderType), "快速软编码");
                SOURCE_TYPE captureType = SOURCE_TYPE.SOURCE_SCREEN_CAPTURE;
                string szDataType = captureType == SOURCE_TYPE.SOURCE_LOCAL_CAMERA ? "YUY2" : "RGB24";
                int captureRet = 0;
                ENCODE_MODE encMode = ENCODE_MODE.H264;
                captureRet = CapturePusher.CapturePusherSDK.EasyScreenLive_StartCapture(pusherPtr, captureType, "rtsp://192.168.0.35:8555/12345",this.panel1.Handle/*IntPtr.Zero*/, encodeType, szDataType, 1280, 720, 1024, false, encMode);

                isCapture = captureRet >= 0;

                if (isCapture)
                    btnCapture.Text = "停止";
                Log(string.Format("开启{0}{1}", "屏幕采集", isCapture ? "成功" : "失敗"));
            }
            else
            {
                if (pusherPtr != IntPtr.Zero)
                {
                    if (isPusherRtsp)
                    {
                        CapturePusher.CapturePusherSDK.EasyScreenLive_StopPush(pusherPtr, PUSH_TYPE.PUSH_RTSP);
                        this.button1.Text = "屏幕推送";
                        isPusherRtsp = false;
                    }
                    if (isPusherRtmp)
                    {
                        CapturePusher.CapturePusherSDK.EasyScreenLive_StopPush(pusherPtr, PUSH_TYPE.PUSH_RTMP);
                        isPusherRtsp = false;
                    }
                    if (isServerOpen)
                    {
                        CapturePusher.CapturePusherSDK.EasyScreenLive_StopServer(pusherPtr, serverId0);
                        CapturePusher.CapturePusherSDK.EasyScreenLive_StopServer(pusherPtr, serverId1);
                        CapturePusher.CapturePusherSDK.EasyScreenLive_StopServer(pusherPtr, serverId2);
                        isServerOpen = false;
                    }
                    CapturePusher.CapturePusherSDK.EasyScreenLive_StopCapture(pusherPtr);
                }

                isCapture = false;

                Log("停止采集");
                btnCapture.Text = "采集屏幕";
            }
        }

        private void button1_Click(object sender, EventArgs e)
        {
            if (isPusherRtsp)
            {
                CapturePusher.CapturePusherSDK.EasyScreenLive_StopPush(pusherPtr, PUSH_TYPE.PUSH_RTSP);
                isPusherRtsp = false;
                button1.Text = "屏幕推送";
                Log("停止rtsp推送!");
            }
            else
            {
                string streamIp = this.textBox3.Text;
                int streamPort = int.Parse(this.textBox2.Text);
                string streamName = this.textBox1.Text;
                int pusherret = CapturePusher.CapturePusherSDK.EasyScreenLive_StartPush(pusherPtr, PUSH_TYPE.PUSH_RTSP, streamIp, streamPort, streamName,1);

                isPusherRtsp = pusherret == 1;
                if (isPusherRtsp)
                {
                    button1.Text = "停止推送";
                    Log(string.Format("开启RTSP推送:rtsp://{0}:{1}/{2}", streamIp, streamPort, streamName));
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
