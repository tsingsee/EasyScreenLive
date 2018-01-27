package org.easydarwin.easyscreenlive.ui.playlist;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.easydarwin.easyscreenlive.utils.OnLiveInfo;
import org.easydarwin.easyscreenlive.R;
import org.easydarwin.easyscreenlive.ui.player.PreviewActivity;

import java.util.List;

/**
 * Created by gavin on 2018/1/11.
 */

public class PlayListAdapter extends BaseAdapter implements View.OnClickListener, View.OnLongClickListener{
    private final String TAG = "PlayListAdapter";


    private Context context;
    List<OnLiveInfo> onLiveList;

    public PlayListAdapter(Context context, List<OnLiveInfo> onLiveList) {
        this.context    = context;
        this.onLiveList = onLiveList;
    }

    /** 添加item数据 */
    public void addChannelData(OnLiveInfo onLiveInfo) {
        onLiveList.add(onLiveInfo);
        notifyDataSetChanged();
    }

    /** 移除item数据 */
    public void delChanneData(int positoon) {
        onLiveList.remove(positoon);
        notifyDataSetChanged();
    }


    public List<OnLiveInfo> getVideoChannelList() {
        return onLiveList;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return onLiveList.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return onLiveList.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    /**
     * listview要判断item的位置，第一条，最后一条和中间的item是不一样的。
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
//        Log.i(TAG, "---" + position);
        ViewHolder holder = null;
//        VideoChannelData data = videoChannelList.get(position);
        OnLiveInfo data = onLiveList.get(position);
        if (convertView == null) {
//            Log.i(TAG, "crate---" + position);
            convertView = View.inflate(context, R.layout.list_item_video_channel, null);
            holder = new ViewHolder();
            holder.name     = (TextView) convertView.findViewById(R.id.channel_name);
            holder.status   = (TextView) convertView.findViewById(R.id.channel_status);
            holder.type     = (TextView) convertView.findViewById(R.id.channel_type);
            holder.url      = (TextView) convertView.findViewById(R.id.channel_url);


            convertView.setTag(holder);
            convertView.setOnClickListener(this);
            convertView.setOnLongClickListener(this);
        }
        holder = (ViewHolder)convertView.getTag();
        holder.position = position;
        holder.name.setText(data.getDevName());
        holder.status.setText(data.getMsg());
        if (data.getPushType() == 1) {
            holder.type.setText(R.string.multicast_type);
        } else {
            holder.type.setText(R.string.unmulticast_type);
        }
        holder.url.setText(data.getURL());
//        String text = data.getSrcIP();
//        holder.url.setText(text);

        return convertView;
    }



    static class ViewHolder{
        TextView name;
        TextView status;
        TextView type;
        TextView url;
        int position;
    }

    @Override
    public void onClick(View v) {
        Log.i(TAG, "------");
        ViewHolder holder = (ViewHolder) v.getTag();
        if (holder != null) {
            OnLiveInfo info = onLiveList.get(holder.position);
            Log.i(TAG, "" + holder.url.getText());
            if (info != null && info.getCmd() == OnLiveInfo.INFO_CMD_SHARED_SCREEN) {
                Intent intent=new Intent(context, PreviewActivity.class);
                intent.putExtra("PLAY_URL",info.getURL());
                context.startActivity(intent);
            } else {
                Toast.makeText(context, "设备未进行屏幕推送",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onLongClick(View var1) {
        ViewHolder holder = (ViewHolder) var1.getTag();
        if (holder == null) {
            return true;
        }
        final int position = holder.position;
        if (position == -1) {
            return true;
        }

        new AlertDialog.Builder(context).setMessage("确定要删除该地址吗？").setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                delChanneData(position);
            }
        }).setNegativeButton("取消", null).show();

        return true;
    }
}
