package com.nbcsports.regional.nbc_rsn.urban_airship;

import com.nbcsports.regional.nbc_rsn.common.MediaSource;
import com.nbcsports.regional.nbc_rsn.persistentplayer.Error;
import com.nbcsports.regional.nbc_rsn.utils.PreferenceUtils;

import java.io.IOException;

public class ErrorChecker {
    private static final String DEFAULT_ERROR_CHECKER = "ERROR_CHECKER_DEFAULT";
    private final MediaSource mediaSource;

    public ErrorChecker(MediaSource mediaSource) {
        this.mediaSource = mediaSource;
    }

    public boolean isShown(IOException authError) {
        String mediaSourceErrorId = convertExceptionToString(authError);
        boolean shown = PreferenceUtils.INSTANCE.getBoolean(mediaSourceErrorId, false);
        return shown;
    }

    public void setShown(IOException authError, boolean shown) {
        String mediaSourceErrorId = convertExceptionToString(authError);
        PreferenceUtils.INSTANCE.setBoolean(mediaSourceErrorId, shown);
    }

    private String convertExceptionToString(IOException authError){

        if (mediaSource == null){
            return DEFAULT_ERROR_CHECKER;
        }

        int error = -1;
        if (Error.is498(authError)){
            error = Error._498;
        } else if (Error.is499(authError)) {
            error = Error._499;
        }
        String mediaSourceErrorId = String.format("%s_%s", mediaSource.getStreamUrl(), Integer.toString(error));
        return mediaSourceErrorId;
    }

    public void reset() {
        if (mediaSource == null){
            PreferenceUtils.INSTANCE.setBoolean(DEFAULT_ERROR_CHECKER, false);
            return;
        }
        String error498 = String.format("%s_%s", mediaSource.getStreamUrl(), Integer.toString(Error._498));
        String error499 = String.format("%s_%s", mediaSource.getStreamUrl(), Integer.toString(Error._499));
        PreferenceUtils.INSTANCE.setBoolean(error498, false);
        PreferenceUtils.INSTANCE.setBoolean(error499, false);
    }
}
