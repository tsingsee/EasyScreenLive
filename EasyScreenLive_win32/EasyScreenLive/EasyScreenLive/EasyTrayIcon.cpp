////////////////////////////////////////////////////////////////
// CTrayIcon Copyright 1996 Microsoft Systems Journal.
//
// If this code works, it was written by Paul DiLascia.
// If not, I don't know who wrote it.

// MyTrayIcon.cpp : implementation file
//

#include "stdafx.h"
#include "EasyTrayIcon.h"
#include "resource.h"

#include <afxpriv.h>    // for AfxLoadString()

#ifdef _DEBUG
#define new DEBUG_NEW
#undef THIS_FILE
static char THIS_FILE[] = __FILE__;
#endif

/////////////////////////////////////////////////////////////////////////////
// CEasyTrayIcon

IMPLEMENT_DYNCREATE(CEasyTrayIcon, CCmdTarget)

CEasyTrayIcon::CEasyTrayIcon()
{
	// Initialize NOTIFYICONDATA
	memset(&m_nid, 0 , sizeof(m_nid));
	m_nid.cbSize = sizeof(m_nid);
	
	UINT uID = IDR_MAINFRAME;
	m_nid.uID = uID;	// never changes after construction
    
	// Use resource string as tip if there is one
	AfxLoadString(uID, m_nid.szTip, sizeof(m_nid.szTip));

}

CEasyTrayIcon::~CEasyTrayIcon()
{
	SetIcon(0); // remove icon from system tray
}


BEGIN_MESSAGE_MAP(CEasyTrayIcon, CCmdTarget)
	//{{AFX_MSG_MAP(CEasyTrayIcon)
		// NOTE - the ClassWizard will add and remove mapping macros here.
	//}}AFX_MSG_MAP
END_MESSAGE_MAP()

/////////////////////////////////////////////////////////////////////////////
// CEasyTrayIcon message handlers

void CEasyTrayIcon::SetNotificationWnd(CWnd* pNotifyWnd, UINT uCbMsg)
{
	// If the following assert fails, you're probably
	// calling me before you created your window. Oops.
	ASSERT(pNotifyWnd==NULL || ::IsWindow(pNotifyWnd->GetSafeHwnd()));
	m_nid.hWnd = pNotifyWnd->GetSafeHwnd();

	ASSERT(uCbMsg==0 || uCbMsg>=WM_USER);
	m_nid.uCallbackMessage = uCbMsg;
}

//////////////////
// This is the main variant for setting the icon.
// Sets both the icon and tooltip from resource ID
// To remove the icon, call SetIcon(0)
//
BOOL CEasyTrayIcon::SetIcon(UINT uID,LPCTSTR strTip )
{ 
	HICON hicon=NULL;
	if (uID) {
		//AfxLoadString(uID, m_nid.szTip, sizeof(m_nid.szTip));
		if (strTip)
		{
			lstrcpy(m_nid.szTip,strTip);
		}
		hicon = AfxGetApp()->LoadIcon(IDR_MAINFRAME); //uID);
	}
	return SetIcon(hicon, NULL);
}

//////////////////
// Common SetIcon for all overloads. 
//
BOOL CEasyTrayIcon::SetIcon(HICON hicon, LPCTSTR lpTip) 
{
	UINT msg;
	m_nid.uFlags = 0;

	// Set the icon
	if (hicon) 
	{
		// Add or replace icon in system tray
		msg = m_nid.hIcon ? NIM_MODIFY : NIM_ADD;
		m_nid.hIcon = hicon;
		m_nid.uFlags |= NIF_ICON;
	}
	else 
	{ // remove icon from tray
		if (m_nid.hIcon==NULL)
			return TRUE;		// already deleted
		msg = NIM_DELETE;
	}

	// Use the tip, if any
	if (lpTip)
		memcpy(m_nid.szTip, lpTip, wcslen(lpTip)+1);
	if (m_nid.szTip[0])
		m_nid.uFlags |= NIF_TIP;

	// Use callback if any
	if (m_nid.uCallbackMessage && m_nid.hWnd)
		m_nid.uFlags |= NIF_MESSAGE;

	// Do it
	BOOL bRet = Shell_NotifyIcon(msg, &m_nid);
	if (msg==NIM_DELETE || !bRet)
		m_nid.hIcon = NULL;	// failed
	return bRet;
}

/////////////////
// Default event handler handles right-menu and doubleclick.
// Call this function from your own notification handler.
//
LRESULT CEasyTrayIcon::OnTrayNotification(WPARAM wID, LPARAM lEvent)
{
	if (wID!=m_nid.uID ||
		(lEvent!=WM_RBUTTONUP && lEvent!=WM_LBUTTONDBLCLK&&lEvent!=WM_LBUTTONUP))
		return 0;

	// If there's a resource menu with the same ID as the icon, use it as 
	// the right-button popup menu. CTrayIcon will interprets the first
	// item in the menu as the default command for WM_LBUTTONDBLCLK
	// 


	
	CMenu menu;
	if (!menu.LoadMenu(IDR_MENU_TRAYICON))// //m_nid.uID))
		return 0;
	CMenu* pSubMenu = menu.GetSubMenu(0);
	if (!pSubMenu) 
		return 0;

	if (lEvent==WM_RBUTTONUP) 
	{

		// Make first menu item the default (bold font)
		//::SetMenuDefaultItem(pSubMenu->m_hMenu, 0, TRUE);

		// Display the menu at the current mouse location. There's a "bug"
		// (Microsoft calls it a feature) in Windows 95 that requires calling
		// SetForegroundWindow. To find out more, search for Q135788 in MSDN.
		//
		CPoint mouse;
		GetCursorPos(&mouse);
		::SetForegroundWindow(m_nid.hWnd);	
		::TrackPopupMenu(pSubMenu->m_hMenu, 0, mouse.x, mouse.y, 0,
			m_nid.hWnd, NULL);
			

	}
	else if(lEvent==WM_LBUTTONUP)
	{
		ShowWindow(m_nid.hWnd,SW_SHOW);
	}


//	else  // double click: execute first menu item
//		::SendMessage(m_nid.hWnd, WM_COMMAND, pSubMenu->GetMenuItemID(0), 0);

	return 1; // handled
}
