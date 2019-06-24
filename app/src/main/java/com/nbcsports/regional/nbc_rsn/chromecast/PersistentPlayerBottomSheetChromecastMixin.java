package com.nbcsports.regional.nbc_rsn.chromecast;

import androidx.mediarouter.app.MediaRouteButton;
import android.view.View;

import com.google.android.gms.cast.framework.CastButtonFactory;

/**
 * This mixin includes the Chromecast functionalities used in
 *
 * @see com.nbcsports.regional.nbc_rsn.persistentplayer.PersistentPlayerBottomSheet
 */
public interface PersistentPlayerBottomSheetChromecastMixin extends ChromecastMixinsBase {

    /**
     * Setups the Chromecast icon, ideally call this in onCreate
     */
    default void setupChromecastIcon(MediaRouteButton chromecastIcon, View chromecastView) {
        CastButtonFactory.setUpMediaRouteButton(getActivity().getApplicationContext(), chromecastIcon);
        chromecastView.setOnClickListener(view -> chromecastIcon.performClick());
    }

    ChromecastDeviceDiscovery getChromecastDeviceDiscovery();

    /**
     * Ideally call this in onResume
     */
    default void addSessionManagerListenerAndDeviceDiscovery() {
        if (!isChromecastEnabled()) return;

        addSessionManagerListener();

        if (getChromecastDeviceDiscovery() != null) {
            getChromecastDeviceDiscovery().addCallback();
        }
    }

    /**
     * ideally call this in onPause
     */
    default void removeDeviceDiscovery() {
        if (!isChromecastEnabled()) return;

        if (getChromecastDeviceDiscovery() != null) {
            getChromecastDeviceDiscovery().removeCallback();
        }
    }
}
