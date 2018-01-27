package org.easydarwin.easyscreenlive.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gson.Gson;

import org.easydarwin.easyscreenlive.utils.OnLiveInfo;
import org.easydarwin.easyscreenlive.config.LiveRtspConfig;
import org.easydarwin.easyscreenlive.ui.playlist.PlayListFragment;
import org.easydarwin.easyscreenlive.utils.Util;

public class OnLiveManagerService extends Service {
    private final String TAG = "OnLiveManagerService";
    Context mContext;

    private SendOnlineBroadcastThread   sendOnlineBroadcastThread;
    private ReceiveOnlineBroadcastThread receiveOnlineBroadcastThread;
    private int broadcastPort = 8765;

    public static  List<OnLiveInfo> onLiveInfoList = new ArrayList<>();
    OnLiveManagerHandle onLiveManagerHandle = new OnLiveManagerHandle();

    public static final int HANDLE_CMD_TIMEER = 1;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "on create");
        LiveRtspConfig.localIp = Util.getLocalIpAddress(this);
        sendOnlineBroadcastThread = new SendOnlineBroadcastThread();
        sendOnlineBroadcastThread.start();
        receiveOnlineBroadcastThread = new ReceiveOnlineBroadcastThread();
        receiveOnlineBroadcastThread.start();
        onLiveManagerHandle.sendEmptyMessageDelayed(HANDLE_CMD_TIMEER, 0);
        mContext = this;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    static public List<OnLiveInfo> getOnLiveInfoList() {
        return onLiveInfoList;
    }


    public class OnLiveManagerHandle extends Handler{
        @Override
        public void handleMessage(Message msg)
        {
            switch(msg.what)
            {
                case HANDLE_CMD_TIMEER: {
                    boolean isChanged = false;
                    long timeNow = System.currentTimeMillis();
                    for(int i = 0 ,len= onLiveInfoList.size(); i<len; ++i){
                        if(onLiveInfoList.get(i).getDelTime() < timeNow){
//                            Log.e(TAG, "HANDLE_CMD_TIMEER" + (onLiveInfoList.get(i).getDelTime()-timeNow));
                            onLiveInfoList.remove(i);
                            --len;
                            --i;
                            isChanged = true;
                        }
                    }
                    if (isChanged) {
                        updateOnliveListView();
                    }
                    sendEmptyMessageDelayed(HANDLE_CMD_TIMEER, 1000);
                }
                    break;
            }
        }
    }

    private void updateOnliveListView() {
        if (PlayListFragment.fragmentPlayListHandle != null) {
            PlayListFragment.fragmentPlayListHandle.sendEmptyMessage(PlayListFragment.UPDATA_ONLIVE_LIST_VIEW);
        }
    }

    class SendOnlineBroadcastThread extends Thread {
        public void run() {
            while (sendOnlineBroadcastThread != null) {
                sendOnlineBroadcast();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void OnBroadcastCmd(OnLiveInfo onLiveInfo){
        switch (onLiveInfo.getCmd()) {
            case OnLiveInfo.INFO_CMD_ONLIVE:
            case OnLiveInfo.INFO_CMD_SHARED_SCREEN: {
                if(onLiveInfo.getSrcIP() == null || onLiveInfo.getSrcIP().equals("")) {
                    break;
                }
                boolean isNeedAdd = true;
                boolean isChanged = false;
                for (int i = 0; i< onLiveInfoList.size(); i++){
                    if (onLiveInfoList.get(i).getSrcIP().equals(onLiveInfo.getSrcIP())) {
                        if (onLiveInfoList.get(i).getCmd() != onLiveInfo.getCmd()) {
                            onLiveInfoList.set(i, onLiveInfo);
                            isChanged = true;
                        }
                        onLiveInfoList.get(i).setDelTime(System.currentTimeMillis() + 5*1000);
                        isNeedAdd = false;
                    }
                }
                if (isNeedAdd) {
                    onLiveInfo.setDelTime(System.currentTimeMillis() + 5*1000);
                    onLiveInfoList.add(onLiveInfo);
                    isChanged = true;
                }
                if (isChanged) {
                    updateOnliveListView();
                }
            }
                break;
            default:
                break;
        }
    }

    class ReceiveOnlineBroadcastThread extends Thread {
        public void run() {
            try {
                DatagramPacket receive = new DatagramPacket(new byte[1024], 1024);
                DatagramSocket server = new DatagramSocket(broadcastPort);
                while (receiveOnlineBroadcastThread != null)
                {
                    server.receive(receive);
                    byte[] recvByte = Arrays.copyOfRange(receive.getData(), 0,
                            receive.getLength());
                    String jsonString = new String(recvByte);

                    Gson gson = new Gson();
                    OnLiveInfo onLiveInfo = gson.fromJson(jsonString, OnLiveInfo.class);
                    OnBroadcastCmd(onLiveInfo);
//                    Log.i(TAG, "recv:" + onLiveInfo.getDevName());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 发送在线广播
     */
    void sendOnlineBroadcast() {
        try {
//          InetAddress inetAddr = InetAddress.getByName("192.168.3.255");
            InetAddress inetAddr = InetAddress.getByName("255.255.255.255");
            DatagramSocket client = new DatagramSocket();

            Gson gson = new Gson();
            OnLiveInfo onLiveInfo = new OnLiveInfo();
//            Log.e(TAG, "src ip" + liveRtspConfig.localIp);
            onLiveInfo.setSrcIP(LiveRtspConfig.localIp);
            if (!LiveRtspConfig.isRunning) {
                onLiveInfo.setCmd(OnLiveInfo.INFO_CMD_ONLIVE);
                onLiveInfo.setPushType(OnLiveInfo.PUSH_TYPE_UNMULTICAST);
                onLiveInfo.setURL("");
            } else {
                onLiveInfo.setCmd(OnLiveInfo.INFO_CMD_SHARED_SCREEN);
                onLiveInfo.setPushType(LiveRtspConfig.enableMulticast);
                onLiveInfo.setURL(LiveRtspConfig.URL);
            }

            String jsonObject = gson.toJson(onLiveInfo);
//            Log.i(TAG, "send:" + jsonObject);
            byte[] msg =  jsonObject.getBytes();
//            Log.e(TAG, "Client send msg complete:" + msg.length);
            DatagramPacket sendPack = new DatagramPacket(msg, msg.length, inetAddr,
                    broadcastPort);
            client.send(sendPack);

            client.close();
        }catch (IOException e) {
            Log.i(TAG, "sendOnlineBroadcast fail:");
            e.printStackTrace();
        }
    }

    public void onDestroy() {
        Thread t = sendOnlineBroadcastThread;
        if (t != null) {
            sendOnlineBroadcastThread = null;
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Thread t2 = receiveOnlineBroadcastThread;
        if (t2 != null) {
            receiveOnlineBroadcastThread = null;
            try {
                t2.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


        super.onDestroy();
    }
}
