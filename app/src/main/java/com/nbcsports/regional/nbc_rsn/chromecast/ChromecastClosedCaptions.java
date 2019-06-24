package com.nbcsports.regional.nbc_rsn.chromecast;

import androidx.annotation.NonNull;

import java.io.IOException;

import com.google.android.gms.cast.Cast.MessageReceivedCallback;
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import org.json.JSONException;
import org.json.JSONObject;

import timber.log.Timber;

public class ChromecastClosedCaptions implements MessageReceivedCallback, ResultCallback<Status> {
    static final String CAPTIONS_NAMESPACE = "urn:x-cast:com.nbcsports.liveextra.captions";
    private CastSession castSession;

    /**
     * Send a message to the receiver app (i.e. enable or disable closed captioning here)
     */
    public void sendMessage(final boolean enabled) {
        if (castSession == null) return;

        PendingResult<Status> pr = castSession.sendMessage(CAPTIONS_NAMESPACE, String.valueOf(enabled));
        if (pr != null){
            pr.setResultCallback(this);
        }
    }

    /* package visible */
    void startCustomMessageChannel(CastSession castSession) {
        this.castSession = castSession;
        if (castSession != null) {
            try {
                castSession.setMessageReceivedCallbacks(CAPTIONS_NAMESPACE, this);
            } catch (IOException e) {
                Timber.d("ChromecastClosedCaptions.startCustomMessageChannel error: %s", e.getMessage());
            }
        }
    }

    void closeCustomMessageChannel() {
        if (castSession != null) {
            try {
                castSession.removeMessageReceivedCallbacks(CAPTIONS_NAMESPACE);
            } catch (IOException e) {
                Timber.d("ChromecastClosedCaptions.closeCustomMessageChannel error: %s", e.getMessage());
            }
        }
    }

    /**
     * Receive message from the receiver app
     */
    @Override
    public void onMessageReceived(CastDevice castDevice, String namespace, String message) {
        Timber.d("ChromecastClosedCaptions.onMessageReceived: %s\n%s", namespace, message);
    }

    /**
     * check the status of the sent message
     */
    @Override
    public void onResult(@NonNull final Status status) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("success", status.isSuccess());
            jsonObject.put("message", status.getStatusMessage());
            jsonObject.put("code", status.getStatusCode());
            jsonObject.put("zzp", status.zzp());
            jsonObject.put("isCanceled", status.isCanceled());
            jsonObject.put("isInterrupted", status.isInterrupted());
            jsonObject.put("toString", status.toString());
        } catch (JSONException e) {
            Timber.d("ChromecastClosedCaptions.onResult: %s", e.getMessage());
        }
        Timber.d("This is the enter point: ChromecastClosedCaptions.onResult status: %s", jsonObject.toString());
    }
}
