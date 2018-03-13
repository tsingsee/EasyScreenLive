
// EasyScreenLiveDlg.h : 头文件
//

#pragma once

#include "../bin/libEasyScreenLiveAPI.h"
#pragma comment(lib, "../bin/libEasyScreenLive.lib")


#define EASY_RTSP_KEY "79397037795969576B5A7341596A5261706375647066464659584E355548567A614756794C6D56345A534E58444661672F365867523246326157346D516D466962334E68514449774D545A4659584E355247467964326C75564756686257566863336B3D"

#define EASY_RTMP_KEY "79397037795969576B5A75416D7942617064396A4575314659584E3555324E795A57567554476C325A53356C65475570567778576F50365334456468646D6C754A6B4A68596D397A595541794D4445325257467A65555268636E6470626C526C5957316C59584E35"

#define EASY_IPC_KEY   "6D72754B7A4969576B5A75416D7942617064396A4575314659584E3555324E795A57567554476C325A53356C65475570567778576F50365334456468646D6C754A6B4A68596D397A595541794D4445325257467A65555268636E6470626C526C5957316C59584E35"


#define MAX_CHANNEL_NUM 1

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
	BOOL m_bPushing;
	BOOL m_bPublishServer;

public:
	afx_msg void OnBnClickedButtonCapture();
	afx_msg void OnBnClickedButtonPush();
	afx_msg void OnBnClickedButtonStop();

	afx_msg void OnDestroy();
	afx_msg void OnCbnSelchangeComboPushsource();
	afx_msg void OnBnClickedButtonPublishServer();
	void OnLog(CString sLog);


};
