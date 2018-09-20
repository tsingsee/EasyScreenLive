
// EasyScreenLiveDlg.h : 头文件
//

#pragma once

//#include "skindialog.h"


#include "../bin/libEasyScreenLiveAPI.h"
#pragma comment(lib, "../bin/libEasyScreenLive.lib")

#define MAX_CHANNEL_NUM 8

// CEasyScreenLiveDlg 对话框
class CEasyScreenLiveDlg : public CDialogEx
{
// 构造
public:
	CEasyScreenLiveDlg(CWnd* pParent = NULL);	// 标准构造函数

// 对话框数据
	enum { IDD = IDD_EasyScreenLive_DIALOG };

	protected:
	virtual void DoDataExchange(CDataExchange* pDX);	// DDX/DDV 支持

// 实现
protected:
	HICON m_hIcon;

	// 生成的消息映射函数
	virtual BOOL OnInitDialog();
	afx_msg void OnSysCommand(UINT nID, LPARAM lParam);
	afx_msg void OnPaint();
	afx_msg HCURSOR OnQueryDragIcon();
	DECLARE_MESSAGE_MAP()
	
private:
	EASYSLIVE_HANDLE m_pusher;
	BOOL m_bCapture;
	BOOL m_bPushingRtsp;
	BOOL m_bPushingRtmp;
	BOOL m_bPublishServer;

public:
	afx_msg void OnBnClickedButtonCapture();
	afx_msg void OnBnClickedButtonPushRtmp();
	afx_msg void OnBnClickedButtonPushRtsp();

	afx_msg void OnDestroy();
	afx_msg void OnCbnSelchangeComboPushsource();
	afx_msg void OnBnClickedButtonPublishServer();
	void OnLog(CString sLog);
	afx_msg void OnBnClickedCheckCapcursor();
};
