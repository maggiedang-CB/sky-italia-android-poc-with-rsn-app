package com.nbcsports.regional.nbc_rsn.authentication;

import com.nbcsports.regional.nbc_rsn.common.Asset;

public interface MvpdLogoListener {
    void onLiveAssetAdded(Asset liveAsset);
    void resetMvpdLogo();
}
