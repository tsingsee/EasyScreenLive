package org.easydarwin.easyscreenlive.ui.playlist;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import org.easydarwin.easyscreenlive.service.OnLiveManagerService;
import org.easydarwin.easyscreenlive.R;
import org.easydarwin.easyscreenlive.base.BaseFragment;
import org.easydarwin.easyscreenlive.ui.player.PreviewActivity;

/**
 * Created by gavin on 2018/1/20.
 */

public class PlayListFragment extends BaseFragment implements PlayListContract.View{
    View view;
    EditText editTextPlayUrl;
    Button addutton;
    ListView listViewPlay;
    private PlayListAdapter listAdapter;
    static public FragmentPlayListHandle fragmentPlayListHandle = null;

    static public final int UPDATA_ONLIVE_LIST_VIEW = 99;

    private PlayListContract.Presenter presenter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_play_list, container, false);
        editTextPlayUrl = view.findViewById(R.id.edit_play_url);
        addutton     = view.findViewById(R.id.button_add);

        listAdapter = new PlayListAdapter(getActivity(), OnLiveManagerService.onLiveInfoList);
        listViewPlay    = view.findViewById(R.id.list_view_url);
        listViewPlay.setAdapter(listAdapter);

        fragmentPlayListHandle = new FragmentPlayListHandle();

        addutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                listAdapter.addChannelData("rtsp://192.168.1.107:8554/12345");
//                listAdapter.addChannelData(editTextPlayUrl.getText().toString());
                Log.i(TAG, "play: " + editTextPlayUrl.getText().toString());
                Intent intent=new Intent(getActivity(), PreviewActivity.class);
                intent.putExtra("PLAY_URL",editTextPlayUrl.getText().toString());
                intent.putExtra("PUSH_TYPE", 1);
                getActivity().startActivity(intent);
            }
        });
//        editTextPlayUrl.setVisibility(View.INVISIBLE);
//        imageButton.setVisibility(View.INVISIBLE);

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


    @Override
    public void showProgress() {
        showProgressDialog();
    }

    @Override
    public void dismissProgress() {
        dismissProgressDialog();
    }

    @Override
    public void showTip(String message) {
        showToast(message);
    }

    @Override
    public boolean isActive() {
        return isAdded();
    }

    @Override
    public void setPresenter(PlayListContract.Presenter presenter) {
        this.presenter = presenter;
    }
}
