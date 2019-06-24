package com.nbcsports.regional.nbc_rsn;

import com.nbcsports.regional.nbc_rsn.common.Config;
import com.nbcsports.regional.nbc_rsn.localization.models.Localizations;
import com.nbcsports.regional.nbc_rsn.common.IpGeolocation;

public interface ConfigListener {
    void onReceived( Config config );
    void onLoadConfigError(Throwable e);
    void onReceivedLocalizations(Localizations localizations);
    void onReceivedIpGeolocation(IpGeolocation ipGeolocation);
}
