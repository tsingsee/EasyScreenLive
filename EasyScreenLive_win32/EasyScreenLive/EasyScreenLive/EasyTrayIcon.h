#if !defined(AFX_MYTRAYICON_H__0E474F41_F007_11D6_A702_C99C0CE4946C__INCLUDED_)
#define AFX_MYTRAYICON_H__0E474F41_F007_11D6_A702_C99C0CE4946C__INCLUDED_

#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000
// MyTrayIcon.h : header file
//

#define WM_MY_TRAY_NOTIFICATION WM_USER+0
/////////////////////////////////////////////////////////////////////////////
// CEasyTrayIcon command target

class CEasyTrayIcon : public CCmdTarget
{
	DECLARE_DYNCREATE(CEasyTrayIcon)

	CEasyTrayIcon();           // protected constructor used by dynamic creation
	NOTIFYICONDATA m_nid;			// struct for Shell_NotifyIcon args

// Attributes
public:
    ~CEasyTrayIcon();

// Operations
public:
	// Call this to receive tray notifications
	void SetNotificationWnd(CWnd* pNotifyWnd, UINT uCbMsg);

	// SetIcon functions. To remove icon, call SetIcon(0)
	//
	BOOL SetIcon(UINT uID,LPCTSTR strTip=TEXT("")); // main variant you want to use
	BOOL SetIcon(HICON hicon, LPCTSTR lpTip);
	BOOL SetIcon(LPCTSTR lpResName, LPCTSTR lpTip)
		{ return SetIcon(lpResName ? 
			AfxGetApp()->LoadIcon(lpResName) : NULL, lpTip); }
	BOOL SetStandardIcon(LPCTSTR lpszIconName, LPCTSTR lpTip)
		{ return SetIcon(::LoadIcon(NULL, lpszIconName), lpTip); }

	virtual LRESULT OnTrayNotification(WPARAM uID, LPARAM lEvent);

// Overrides
	// ClassWizard generated virtual function overrides
	//{{AFX_VIRTUAL(CEasyTrayIcon)
	//}}AFX_VIRTUAL
	
// Implementation
protected:
//	virtual ~CEasyTrayIcon();

	// Generated message map functions
	//{{AFX_MSG(CEasyTrayIcon)
		// NOTE - the ClassWizard will add and remove member functions here.
	//}}AFX_MSG

	DECLARE_MESSAGE_MAP()
};

/////////////////////////////////////////////////////////////////////////////

//{{AFX_INSERT_LOCATION}}
// Microsoft Visual C++ will insert additional declarations immediately before the previous line.

#endif // !defined(AFX_MYTRAYICON_H__0E474F41_F007_11D6_A702_C99C0CE4946C__INCLUDED_)
