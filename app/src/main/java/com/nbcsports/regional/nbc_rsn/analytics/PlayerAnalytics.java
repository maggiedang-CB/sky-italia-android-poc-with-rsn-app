package com.nbcsports.regional.nbc_rsn.analytics;

import com.adobe.mediacore.MediaPlayer;

public interface PlayerAnalytics {
    void attachPlayer(MediaPlayer player);
    void onPause();
    void onResume();
    void onDestroy();
    //void switchAsset(AssetViewModel viewModel);
}
