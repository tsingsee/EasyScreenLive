package org.easydarwin.easyscreenlive.ui.playlist;

/**
 * Created by gavin on 2018/1/23.
 */

public class PlayListPresenter implements PlayListContract.Presenter {
    private PlayListContract.View view;
    static private PlayListPresenter playListPresenter = null;

    static public PlayListPresenter getInterface() {
        if (playListPresenter == null) {
            playListPresenter = new PlayListPresenter();
        }
        return playListPresenter;
    }

    public void setPlayListView(PlayListContract.View view) {
        this.view = view;
        this.view.setPresenter(this);
    }

    public void updteOnliveList()
    {
        if(view != null)
            view.updateOnliveList();
    }
}
