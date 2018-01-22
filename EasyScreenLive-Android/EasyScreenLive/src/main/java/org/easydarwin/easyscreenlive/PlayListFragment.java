package org.easydarwin.easyscreenlive;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

/**
 * Created by gavin on 2018/1/20.
 */

public class PlayListFragment extends Fragment {
    View view;
    EditText editTextPlayUrl;
    ImageButton imageButton;
    ListView listViewPlay;
    private PlayListAdapter listAdapter;
    static public FragmentPlayListHandle fragmentPlayListHandle = null;

    static public final int UPDATA_ONLIVE_LIST_VIEW = 99;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_play_list, container, false);
        editTextPlayUrl = view.findViewById(R.id.edit_play_url);
        imageButton     = view.findViewById(R.id.image_button_start_play);

        listAdapter = new PlayListAdapter(getActivity(), OnLiveManagerService.onLiveInfoList);
        listViewPlay    = view.findViewById(R.id.list_view_url);
        listViewPlay.setAdapter(listAdapter);

        fragmentPlayListHandle = new FragmentPlayListHandle();

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                listAdapter.addChannelData("rtsp://192.168.1.107:8554/12345");
//                listAdapter.addChannelData(editTextPlayUrl.getText().toString());
            }
        });
        editTextPlayUrl.setVisibility(View.INVISIBLE);
        imageButton.setVisibility(View.INVISIBLE);

        return view;
    }

    public class FragmentPlayListHandle extends Handler{
        @Override
        public void handleMessage(Message msg)
        {
            switch(msg.what)
            {
                case UPDATA_ONLIVE_LIST_VIEW: {
                    listAdapter.notifyDataSetChanged();
                }
                break;
            }
        }
    }
}
