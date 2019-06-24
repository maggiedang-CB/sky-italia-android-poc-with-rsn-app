package com.nbcsports.regional.nbc_rsn.chromecast;

import android.app.Activity;

import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.SessionManagerListener;

import com.google.android.gms.cast.framework.media.RemoteMediaClient;
import com.nbcsports.regional.nbc_rsn.MainActivity;

public interface ChromecastMixinsBase {

    /**
     * This method is already implemented by the Fragment, do not Override if
     * this interface is implemented by a Fragment or any class that already
     * has a getActivity method
     */
    Activity getActivity();
    SessionManagerListener<CastSession> getChromecastListener();

    default boolean isChromecastEnabled() {
        return IChromecastHelper.isChromecastEnabled((MainActivity)getActivity());
    }

    /**
     * @return current CastContext
     * note that CastContext is a singleton, there's no need so save it
     * into an instance variable
     */
    default CastContext getCastContext() {
        if (!isChromecastEnabled()) return null;

        CastContext castContext = null;
        if (getActivity() != null && !getActivity().isFinishing()) {
            castContext = CastContext.getSharedInstance(getActivity().getApplicationContext());
        }
        return castContext;
    }

    default void addSessionManagerListener() {
        if (getCastContext() != null && getChromecastListener() != null) {
            getCastContext().getSessionManager().addSessionManagerListener(getChromecastListener(), CastSession.class);
        }
    }

    default void removeSessionManagerListener() {
        if (getCastContext() != null && getChromecastListener() != null) {
            getCastContext().getSessionManager().removeSessionManagerListener(getChromecastListener(), CastSession.class);
        }
    }

    default void addRemoteMediaClientCallback(RemoteMediaClient.Callback callback) {
        if (getCastContext() != null
                && getCastContext().getSessionManager() != null
                && getCastContext().getSessionManager().getCurrentCastSession() != null
                && getCastContext().getSessionManager().getCurrentCastSession().getRemoteMediaClient() != null
                && callback != null){
            getCastContext().getSessionManager().getCurrentCastSession().getRemoteMediaClient().registerCallback(callback);
        }
    }

    default void removeRemoteMediaClientCallback(RemoteMediaClient.Callback callback) {
        if (getCastContext() != null
                && getCastContext().getSessionManager() != null
                && getCastContext().getSessionManager().getCurrentCastSession() != null
                && getCastContext().getSessionManager().getCurrentCastSession().getRemoteMediaClient() != null
                && callback != null){
            getCastContext().getSessionManager().getCurrentCastSession().getRemoteMediaClient().unregisterCallback(callback);
        }
    }
}
