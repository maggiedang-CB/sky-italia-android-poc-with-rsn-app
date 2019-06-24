package com.nbcsports.regional.nbc_rsn.chromecast;

import android.content.Context;
import androidx.mediarouter.media.MediaRouteSelector;
import androidx.mediarouter.media.MediaRouter;
import androidx.mediarouter.media.MediaRouter.Callback;
import androidx.mediarouter.media.MediaRouter.RouteInfo;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.CastMediaControlIntent;

import com.nbcsports.regional.nbc_rsn.R;

import timber.log.Timber;

public class ChromecastDeviceDiscovery extends MediaRouter.Callback {

    private WeakReference<ChromecastDiscoveryListener> chromecastDiscoveryListenerWeak;

    private MediaRouter mMediaRouter;
    private MediaRouteSelector mMediaRouteSelector;

    private TreeSet<RouteInfo> mRouteInfos = new TreeSet<>((routeInfo, t1) -> (routeInfo.getId().compareTo(t1.getId())));
    private CastDevice mSelectedDevice;
    private Set<Callback> addedCallbacks = new HashSet<>();

    public ChromecastDeviceDiscovery(Context context, ChromecastDiscoveryListener chromecastDiscoveryListener) {
        this.chromecastDiscoveryListenerWeak = new WeakReference<>(chromecastDiscoveryListener);

        // Get media router instance
        mMediaRouter = MediaRouter.getInstance(context.getApplicationContext());

        // Create a MediaRouteSelector for the type of routes your app supports
        mMediaRouteSelector = new MediaRouteSelector.Builder()
                //.addControlCategory(MediaControlIntent.CATEGORY_REMOTE_PLAYBACK)
                .addControlCategory(CastMediaControlIntent.categoryForCast(context.getResources().getString(R.string.chromecast_app_id)))
                .build();

        // Check if there is any available chromecast device
        // If so, show chromecast button
        // Otherwise, hide chromecast button
        findCurrentlyAvailableChromecasts();
    }

    private ChromecastDiscoveryListener getListener() {
        return chromecastDiscoveryListenerWeak.get();
    }

    private void fireListenerConditional() {
        if (getListener() != null && isAtLeastOneAvailable()) {
            getListener().onAtLeastOneDeviceAvailable();
        }
    }

    private void fireListenerRemoveConditional() {
        if (getListener() != null && !isAtLeastOneAvailable()) {
            getListener().onNoDevicesAvailable();
        }
    }

    private void findCurrentlyAvailableChromecasts() {
        boolean isAnyDeviceAvailable = false;
        List<RouteInfo> discoveredInfos = mMediaRouter.getRoutes();
        // Check if there is any available chromecast
        for (RouteInfo info: discoveredInfos) {
            if (info.getDeviceType() == RouteInfo.DEVICE_TYPE_TV){
                isAnyDeviceAvailable = true;
                break;
            }
        }
        // If at least one chromecast available, then show chromecast button
        // Otherwise, hide chromecast button
        if (isAnyDeviceAvailable && getListener() != null){
            getListener().onAtLeastOneDeviceAvailable();
        } else if (getListener() != null){
            getListener().onNoDevicesAvailable();
        }
        // Print currently available chromecast devices for debug
        printCurrentlyAvailableDevices(discoveredInfos);
    }

    private boolean addDeviceToList(RouteInfo routeInfo) {
        if (routeInfo.getDeviceType() == RouteInfo.DEVICE_TYPE_TV) {
            return mRouteInfos.add(routeInfo);
        }
        return false;
    }

    @Override
    public void onRouteAdded(MediaRouter router, MediaRouter.RouteInfo info) {
        // Add route to list of discovered routes
        synchronized (this) {
            addDeviceToList(info);
            if (mRouteInfos.size() > 0) {
                fireListenerConditional();
            }
        }
        // Print mRouteInfos list added chromecast devices for debug
        printAddedAvailableDevices();
    }

    @Override
    public void onRouteRemoved(MediaRouter router, MediaRouter.RouteInfo info) {
        // Remove route from list of discovered routes
        synchronized (this) {
            mRouteInfos.remove(info);
            if (mRouteInfos.size() == 0) {
                fireListenerRemoveConditional();
            }
        }
        // Print mRouteInfos list added chromecast devices for debug
        printAddedAvailableDevices();
    }

    private void printCurrentlyAvailableDevices(List<RouteInfo> discoveredInfos) {
        int i = 1;
        for (RouteInfo info : discoveredInfos) {
            Timber.d("This is the enter point: chromecast currently available chromecast: %s.%s", i, info != null? info.getName() : null);
            i++;
        }
    }

    private void printAddedAvailableDevices() {
        int i = 1;
        for (RouteInfo info : mRouteInfos) {
            Timber.d("This is the enter point: chromecast added available chromecast: %s.%s", i, info != null? info.getName() : null);
            i++;
        }
    }

    @Override
    public void onRouteSelected(MediaRouter router, RouteInfo info) {
        mSelectedDevice = CastDevice.getFromBundle(info.getExtras());
    }

    @Override
    public void onRouteUnselected(MediaRouter router, RouteInfo info) {
        mSelectedDevice = null;
    }

    public void addCallback() {
        if (!addedCallbacks.contains(this)) {
            mMediaRouter.addCallback(mMediaRouteSelector, this, MediaRouter.CALLBACK_FLAG_REQUEST_DISCOVERY);
            addedCallbacks.add(this);
        }
    }

    public void removeCallback() {
        mMediaRouter.removeCallback(this);
        addedCallbacks.remove(this);
    }

    public boolean isAtLeastOneAvailable() {
        return !mRouteInfos.isEmpty();
    }

    public CastDevice getSelectedDevice() {
        return mSelectedDevice;
    }
}
