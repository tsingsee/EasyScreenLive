package org.easydarwin.easyscreenlive.screen_live;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

import org.easydarwin.easyscreenlive.ui.playlist.PlayListFragment;
import org.easydarwin.easyscreenlive.screen_live.utils.OnLiveInfo;
import org.easydarwin.easyscreenlive.screen_live.utils.Util;
import org.easydarwin.easyscreenlive.ui.playlist.PlayListPresenter;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by gavin on 2018/4/21.
 */

public class OnLiveManager extends PlayListPresenter {
    private final String TAG = "OnLiveManager";

    private Context mContext;
    private SendOnlineBroadcastThread   sendOnlineBroadcastThread;
    private ReceiveOnlineBroadcastThread receiveOnlineBroadcastThread;
    private int broadcastPort = 8765;
    public static List<OnLiveInfo> onLiveInfoList = new ArrayList<>();

    OnLiveManager(Context context) {
        mContext = context;
    }

    void create(){
        EasyScreenLiveAPI.liveRtspConfig.localIp = Util.getLocalIpAddress(mContext);
        sendOnlineBroadcastThread = new SendOnlineBroadcastThread();
        sendOnlineBroadcastThread.start();
        receiveOnlineBroadcastThread = new ReceiveOnlineBroadcastThread();
        receiveOnlineBroadcastThread.start();
    }

    void destory() {
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
    }

    void onTimerUpdateOnliveListView() {
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
    }

    private void updateOnliveListView() {
        updteOnliveList();
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

    /**
     * 发送在线广播
     */
    private void sendOnlineBroadcast() {
        try {
//          InetAddress inetAddr = InetAddress.getByName("192.168.3.255");
            InetAddress inetAddr = InetAddress.getByName("255.255.255.255");
            DatagramSocket client = new DatagramSocket();

            Gson gson = new Gson();
            OnLiveInfo onLiveInfo = new OnLiveInfo();
//            Log.e(TAG, "src ip" + liveRtspConfig.localIp);
            onLiveInfo.setSrcIP(EasyScreenLiveAPI.liveRtspConfig.localIp);
            if (!EasyScreenLiveAPI.liveRtspConfig.isRunning) {
                onLiveInfo.setCmd(OnLiveInfo.INFO_CMD_ONLIVE);
                onLiveInfo.setPushType(OnLiveInfo.PUSH_TYPE_UNMULTICAST);
                onLiveInfo.setURL("");
            } else {
                onLiveInfo.setCmd(OnLiveInfo.INFO_CMD_SHARED_SCREEN);
                onLiveInfo.setPushType(EasyScreenLiveAPI.liveRtspConfig.enableMulticast);
                onLiveInfo.setURL(EasyScreenLiveAPI.liveRtspConfig.URL);
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


}
