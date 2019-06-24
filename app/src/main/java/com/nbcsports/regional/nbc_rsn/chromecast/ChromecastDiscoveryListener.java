package com.nbcsports.regional.nbc_rsn.chromecast;

public interface ChromecastDiscoveryListener {
    void onAtLeastOneDeviceAvailable();
    void onNoDevicesAvailable();
}
