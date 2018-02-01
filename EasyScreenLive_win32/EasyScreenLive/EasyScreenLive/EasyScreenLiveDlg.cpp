
// EasyScreenLiveDlg.cpp : 实现文件
//

#include "stdafx.h"
#include "EasyScreenLive.h"
#include "EasyScreenLiveDlg.h"
#include "afxdialogex.h"
#include <string>
using namespace std;

#include <WinSock2.h>
#pragma comment(lib,"ws2_32")        //链接到ws2_32动态链接库

#ifdef _DEBUG
#define new DEBUG_NEW
#endif


// 用于应用程序“关于”菜单项的 CAboutDlg 对话框

class CAboutDlg : public CDialogEx
{
public:
	CAboutDlg();

// 对话框数据
	enum { IDD = IDD_ABOUTBOX };

	protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV 支持

// 实现
protected:
	DECLARE_MESSAGE_MAP()
};

CAboutDlg::CAboutDlg() : CDialogEx(CAboutDlg::IDD)
{
}

void CAboutDlg::DoDataExchange(CDataExchange* pDX)
{
	CDialogEx::DoDataExchange(pDX);
}

BEGIN_MESSAGE_MAP(CAboutDlg, CDialogEx)
END_MESSAGE_MAP()


// CEasyScreenLiveDlg 对话框




CEasyScreenLiveDlg::CEasyScreenLiveDlg(CWnd* pParent /*=NULL*/)
	: CDialogEx(CEasyScreenLiveDlg::IDD, pParent)
{
	m_hIcon = AfxGetApp()->LoadIcon(IDR_MAINFRAME);
	m_pusher = NULL;
	m_bCapture = FALSE;
	m_bPushing = FALSE;
	m_bPublishServer = FALSE;
}

void CEasyScreenLiveDlg::DoDataExchange(CDataExchange* pDX)
{
	CDialogEx::DoDataExchange(pDX);
}

BEGIN_MESSAGE_MAP(CEasyScreenLiveDlg, CDialogEx)
	ON_WM_SYSCOMMAND()
	ON_WM_PAINT()
	ON_WM_QUERYDRAGICON()
	ON_BN_CLICKED(IDC_BUTTON_PUSH, &CEasyScreenLiveDlg::OnBnClickedButtonPush)
	ON_BN_CLICKED(IDC_BUTTON_CAPTURE, &CEasyScreenLiveDlg::OnBnClickedButtonCapture)
	ON_WM_DESTROY()
	ON_CBN_SELCHANGE(IDC_COMBO_PUSHSOURCE, &CEasyScreenLiveDlg::OnCbnSelchangeComboPushsource)
	ON_BN_CLICKED(IDC_BUTTON_PUBLISH_SERVER, &CEasyScreenLiveDlg::OnBnClickedButtonPublishServer)
END_MESSAGE_MAP()


// CEasyScreenLiveDlg 消息处理程序

BOOL CEasyScreenLiveDlg::OnInitDialog()
{
	CDialogEx::OnInitDialog();

	// 将“关于...”菜单项添加到系统菜单中。

	// IDM_ABOUTBOX 必须在系统命令范围内。
	ASSERT((IDM_ABOUTBOX & 0xFFF0) == IDM_ABOUTBOX);
	ASSERT(IDM_ABOUTBOX < 0xF000);

	CMenu* pSysMenu = GetSystemMenu(FALSE);
	if (pSysMenu != NULL)
	{
		BOOL bNameValid;
		CString strAboutMenu;
		bNameValid = strAboutMenu.LoadString(IDS_ABOUTBOX);
		ASSERT(bNameValid);
		if (!strAboutMenu.IsEmpty())
		{
			pSysMenu->AppendMenu(MF_SEPARATOR);
			pSysMenu->AppendMenu(MF_STRING, IDM_ABOUTBOX, strAboutMenu);
		}
	}

	// 设置此对话框的图标。当应用程序主窗口不是对话框时，框架将自动
	//  执行此操作
	SetIcon(m_hIcon, TRUE);			// 设置大图标
	SetIcon(m_hIcon, FALSE);		// 设置小图标

	// TODO: 在此添加额外的初始化代码

	GetDlgItem(IDC_EDIT_IP)->SetWindowText(_T("www.easydss.com"));
	GetDlgItem(IDC_EDIT_PORT)->SetWindowText(_T("10085"));	
	GetDlgItem(IDC_EDIT_STREAMNAME)->SetWindowText(_T("Sword"));	
	GetDlgItem(IDC_EDIT_LISTEN_PORT)->SetWindowText(_T("8554"));	
	GetDlgItem(IDC_EDIT_BITRATE)->SetWindowText(_T("2048"));

	
	
	CComboBox* pComboSource  = (CComboBox*)GetDlgItem( IDC_COMBO_PUSHSOURCE);
	if (pComboSource)
	{
		pComboSource->AddString(_T("摄像头采集"));
		pComboSource->AddString(_T("屏幕采集"));
		pComboSource->SetCurSel(1);
	}

	CComboBox* pComboEncoder  = (CComboBox*)GetDlgItem( IDC_COMBO_ENCODER_TYPE);
	if (pComboEncoder)
	{
		pComboEncoder->AddString(_T("默认编码器"));
		pComboEncoder->AddString(_T("快速软编码"));
		pComboEncoder->AddString(_T("快速硬编码"));
		pComboEncoder->SetCurSel(1);
	}	

	CComboBox* pComboTransType  = (CComboBox*)GetDlgItem( IDC_COMBO_TRANSPORT_TYPE);
	if (pComboTransType)
	{
		pComboTransType->AddString(_T("单播"));
		pComboTransType->AddString(_T("组播"));
		pComboTransType->SetCurSel(0);
	}	

	return TRUE;  // 除非将焦点设置到控件，否则返回 TRUE
}

void CEasyScreenLiveDlg::OnSysCommand(UINT nID, LPARAM lParam)
{
	if ((nID & 0xFFF0) == IDM_ABOUTBOX)
	{
		CAboutDlg dlgAbout;
		dlgAbout.DoModal();
	}
	else
	{
		CDialogEx::OnSysCommand(nID, lParam);
	}
}

// 如果向对话框添加最小化按钮，则需要下面的代码
//  来绘制该图标。对于使用文档/视图模型的 MFC 应用程序，
//  这将由框架自动完成。

void CEasyScreenLiveDlg::OnPaint()
{
	if (IsIconic())
	{
		CPaintDC dc(this); // 用于绘制的设备上下文

		SendMessage(WM_ICONERASEBKGND, reinterpret_cast<WPARAM>(dc.GetSafeHdc()), 0);

		// 使图标在工作区矩形中居中
		int cxIcon = GetSystemMetrics(SM_CXICON);
		int cyIcon = GetSystemMetrics(SM_CYICON);
		CRect rect;
		GetClientRect(&rect);
		int x = (rect.Width() - cxIcon + 1) / 2;
		int y = (rect.Height() - cyIcon + 1) / 2;

		// 绘制图标
		dc.DrawIcon(x, y, m_hIcon);
	}
	else
	{
		CDialogEx::OnPaint();
	}
}

//当用户拖动最小化窗口时系统调用此函数取得光标
//显示。
HCURSOR CEasyScreenLiveDlg::OnQueryDragIcon()
{
	return static_cast<HCURSOR>(m_hIcon);
}


void CEasyScreenLiveDlg::OnBnClickedButtonCapture()
{
	// TODO: 在此添加控件通知处理程序代码
	CButton* pCapture = (CButton*)GetDlgItem(IDC_BUTTON_CAPTURE);
	CButton* pBtnPush = (CButton*)GetDlgItem(IDC_BUTTON_PUSH);
	CButton* pBtnPublishServer = (CButton*)GetDlgItem(IDC_BUTTON_PUBLISH_SERVER);

	if (!m_bCapture)
	{
		HWND hShowVideo = GetDlgItem(IDC_STATIC_VIDEO)->GetSafeHwnd();
		
		if(!m_pusher )
			m_pusher =  EasyScreenLive_Create();

		if (m_pusher)
		{
			CString sSourceType;
			CString sLog;
			SOURCE_TYPE sourceType = SOURCE_LOCAL_CAMERA;
			int nEncoderType = 0;
			string sFormat = "RGB24";
			CComboBox* pComboxSourceMode = (CComboBox*)GetDlgItem(IDC_COMBO_PUSHSOURCE);
			if (pComboxSourceMode)
			{
				sourceType = (SOURCE_TYPE)pComboxSourceMode->GetCurSel();
			}
			CComboBox* pComboEncoder  = (CComboBox*)GetDlgItem( IDC_COMBO_ENCODER_TYPE);
			if (pComboEncoder)
			{
				nEncoderType = pComboEncoder->GetCurSel();
			}

			if (sourceType == SOURCE_LOCAL_CAMERA)
			{
				sFormat = "YUY2";
				sSourceType = _T("本地摄像机采集");
			} 
			else 		if (sourceType == SOURCE_SCREEN_CAPTURE)
			{
				sFormat = "RGB24";
				sSourceType = _T("屏幕采集");
			}

			wchar_t wszBitRate[128] = {0,};
			char szBitRate[128] = {0,};
			int nBitRate  = 2048;
			GetDlgItem(IDC_EDIT_BITRATE)->GetWindowText(wszBitRate, sizeof(wszBitRate));
			if (wcslen(wszBitRate)  > 0)//当前为空		
			{
				__WCharToMByte(wszBitRate, szBitRate, sizeof(szBitRate)/sizeof(szBitRate[0]));
				nBitRate = atoi( szBitRate );
			}

			int ret = EasyScreenLive_StartCapture(m_pusher, sourceType, 0, 0, hShowVideo, nEncoderType, 640,480,25,nBitRate, (char*)sFormat.c_str(),8000,1);
			if (ret)
			{
				m_bCapture = TRUE;
				pCapture->SetWindowText(_T("Stop"));
				sLog.Format(_T("开启%s成功。"), sSourceType);	
				OnLog( sLog );
			}else
			{
				sLog.Format(_T("开启%s失败。"), sSourceType);
				OnLog( sLog );
			}
		}
	} 
	else
	{		
		if (m_pusher)
		{
			EasyScreenLive_StopPush(m_pusher, PUSH_RTMP);
			EasyScreenLive_StopServer(m_pusher);
			EasyScreenLive_StopCapture(m_pusher);
			EasyScreenLive_Release(m_pusher);
			m_pusher = NULL;
		}
		pCapture->SetWindowText(_T("Capture"));
		m_bCapture = FALSE;
		pBtnPush->SetWindowText(_T("Push"));
		m_bPushing = FALSE;
		pBtnPublishServer->SetWindowText(_T("Publish Server"));
		m_bPublishServer = FALSE;
		OnLog( _T("停止采集") );
	}
}


void CEasyScreenLiveDlg::OnBnClickedButtonPush()
{

	// TODO: 在此添加控件通知处理程序代码
	// 
	CButton* pBtnPush = (CButton*)GetDlgItem(IDC_BUTTON_PUSH);
	if (!m_bPushing)
	{
		UpdateData(TRUE);
		wchar_t wszIP[128] = {0,};
		char szIP[128] = {0,};
		wchar_t wszPort[128] = {0,};
		char szPort[128] = {0,};
		wchar_t wszStreamName[128] = {0,};
		char szStreamName[128] = {0,};
		int nPort = 0;

		GetDlgItem(IDC_EDIT_IP)->GetWindowText(wszIP,  sizeof(wszIP));
		if (wcslen(wszIP)  > 0)//当前为空		
		{
			__WCharToMByte(wszIP, szIP, sizeof(szIP)/sizeof(szIP[0]));
			//nStartTime = atoi( szStartTime );
		}

		GetDlgItem(IDC_EDIT_PORT)->GetWindowText(wszPort,  sizeof(wszPort));	
		if (wcslen(wszPort)  > 0)//当前为空		
		{
			__WCharToMByte(wszPort, szPort, sizeof(szPort)/sizeof(szPort[0]));
			nPort = atoi( szPort );
		}

		GetDlgItem(IDC_EDIT_STREAMNAME)->GetWindowText(wszStreamName,  sizeof(wszStreamName));	
		if (wcslen(wszStreamName)  > 0)//当前为空		
		{
			__WCharToMByte(wszStreamName, szStreamName, sizeof(szStreamName)/sizeof(szStreamName[0]));
		}

		if (m_pusher)
		{
			EasyScreenLive_StartPush(m_pusher, PUSH_RTMP, szIP, nPort,  szStreamName );
			m_bPushing = TRUE;
			pBtnPush->SetWindowText(_T("Stop"));
			CString sLog = _T("");

			sLog.Format(_T("开启RTMP推送: rtmp://%s:%d/live/%s"), wszIP, nPort, wszStreamName);
			OnLog( sLog );
		}
	}
	else
	{
		EasyScreenLive_StopPush(m_pusher, PUSH_RTMP);

		pBtnPush->SetWindowText(_T("Push"));
		m_bPushing = FALSE;
		OnLog( _T("停止推送") );

	}
}

void CEasyScreenLiveDlg::OnBnClickedButtonStop()
{
	// TODO: 在此添加控件通知处理程序代码
	EasyScreenLive_StopPush(m_pusher, PUSH_RTMP);
	EasyScreenLive_StopCapture(m_pusher);

}

void CEasyScreenLiveDlg::OnCbnSelchangeComboPushsource()
{
	// TODO: 在此添加控件通知处理程序代码
	// 
	CComboBox* pComboxSourceMode = (CComboBox*)GetDlgItem(IDC_COMBO_PUSHSOURCE);
	if (pComboxSourceMode)
	{
		int nIdx = pComboxSourceMode->GetCurSel();
	}
}

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

typedef struct tagIPInfo
{
	char ip[30];
}IPInfo;

bool GetLocalIPs(IPInfo* ips,int maxCnt,int* cnt)
{
	//1.初始化wsa
	WSADATA wsaData;
	int ret=WSAStartup(MAKEWORD(2,2),&wsaData);
	if (ret!=0)
	{
		return false;
	}
	//2.获取主机名
	char hostname[256];
	ret=gethostname(hostname,sizeof(hostname));
	if (ret==SOCKET_ERROR)
	{
		return false;
	}
	//3.获取主机ip
	HOSTENT* host=gethostbyname(hostname);
	if (host==NULL)
	{
		return false;
	}
	//4.逐个转化为char*并拷贝返回
	int n = *cnt=host->h_length<maxCnt?host->h_length:maxCnt;
	for (int i=0;i<n;i++)
	{
		in_addr* addr=(in_addr*)*host->h_addr_list;
		strcpy(ips[i].ip,inet_ntoa(addr[i]));
	}
	return true;
}

void CEasyScreenLiveDlg::OnBnClickedButtonPublishServer()
{
	// TODO: 在此添加控件通知处理程序代码
	// 
	//IDC_EDIT_LISTEN_PORT
	CButton* pBtnPublishServer = (CButton*)GetDlgItem(IDC_BUTTON_PUBLISH_SERVER);
	if (!m_bPublishServer)
	{
		UpdateData(TRUE);
		int nRet = 0;
		if (m_pusher)
		{
			int transportType = 0;
			CComboBox* pComboTransType  = (CComboBox*)GetDlgItem( IDC_COMBO_TRANSPORT_TYPE);
			if (pComboTransType)
			{

				transportType = pComboTransType->GetCurSel();
			}

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
					liveChannel[i].enable_multicast = transportType;
					strcpy(liveChannel[i].multicast_addr, "238.255.255.255");
					liveChannel[i].ttl = 255;
				}
#endif
			}

			wchar_t wszPort[128] = {0,};
			char szPort[128] = {0,};
			int nPort = 0;
			GetDlgItem(IDC_EDIT_LISTEN_PORT)->GetWindowText(wszPort,  sizeof(wszPort));	
			if (wcslen(wszPort)  > 0)//当前为空		
			{
				__WCharToMByte(wszPort, szPort, sizeof(szPort)/sizeof(szPort[0]));
				nPort = atoi( szPort );
			}

			string ip = "127.0.0.1";
#if 0
			int MaxCon = 10;
			int num = 0;
			IPInfo ips[10];
			GetLocalIPs(ips, MaxCon, &num);
#endif
			GetLocalIP(ip);
			//开始RTSP服务
			nRet  = EasyScreenLive_StartServer(m_pusher, nPort, "", "",  liveChannel, MAX_CHANNEL_NUM );
			pBtnPublishServer->SetWindowText(_T("Stop"));
			m_bPublishServer = TRUE;
			CString sLog = _T("");
			for (int nI=0; nI<MAX_CHANNEL_NUM; nI++)
			{
				sLog.Format(_T("开启RTSP服务: rtsp://%s:%d/channel=%d"), CString(ip.c_str()), nPort, nI);
				OnLog( sLog );
			}
		}
	} 
	else
	{
		EasyScreenLive_StopServer(m_pusher);
		pBtnPublishServer->SetWindowText(_T("Publish Server"));
		m_bPublishServer = FALSE;
		OnLog(_T("停止RTSP服务"));
	}

}
void CEasyScreenLiveDlg::OnLog(CString sLog)
{
	CEdit* pLog = (CEdit*)GetDlgItem(IDC_EDIT_LOG);
	if (pLog)
	{
		CString strLog = sLog;
		CString strTime = _T("");
		CTime CurrentTime=CTime::GetCurrentTime(); 
		strTime.Format(_T("%04d/%02d/%02d %02d:%02d:%02d   "),CurrentTime.GetYear(),CurrentTime.GetMonth(),
			CurrentTime.GetDay(),CurrentTime.GetHour(),  CurrentTime.GetMinute(),
			CurrentTime.GetSecond());
		strLog = strTime + strLog + _T("\r\n");
		int nLength  =  pLog->SendMessage(WM_GETTEXTLENGTH);  
		pLog->SetSel(nLength,  nLength);  
		pLog->ReplaceSel(strLog); 
		pLog->SetFocus();
	}
}



void CEasyScreenLiveDlg::OnDestroy()
{
	CDialogEx::OnDestroy();

	// TODO: 在此处添加消息处理程序代码
	if (m_pusher)
	{
		EasyScreenLive_StopPush(m_pusher, PUSH_RTMP);
		EasyScreenLive_StopServer(m_pusher);
		EasyScreenLive_StopCapture(m_pusher);
		EasyScreenLive_Release(m_pusher);
		m_pusher = NULL;
	}
}
