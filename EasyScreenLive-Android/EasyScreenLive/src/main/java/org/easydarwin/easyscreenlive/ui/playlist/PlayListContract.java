package org.easydarwin.easyscreenlive.ui.playlist;

import org.easydarwin.easyscreenlive.ui.base.BasePresenter;
import org.easydarwin.easyscreenlive.ui.base.BaseView;

/**
 * Created by gavin on 2018/1/23.
 */

public class PlayListContract {
    interface View extends BaseView<Presenter> {
        void updateOnliveList();
    }

    interface Presenter extends BasePresenter {
    }
}
