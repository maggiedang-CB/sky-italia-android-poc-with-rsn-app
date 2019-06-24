package com.nbcsports.regional.nbc_rsn.team_feed.components;

import com.clearbridge.pull_to_refresh.PullLayout;

// TODO: LifeCycle Observer!
public interface FragmentLifeCycleListener {

    void onResume();

    void onPause();

    interface Interface {

        void addFragmentLifeCycleListener(FragmentLifeCycleListener fragmentLifeCycleListener);

        void showNativeShare(String title, String s);

        PullLayout getRefreshLayout();
    }
}
