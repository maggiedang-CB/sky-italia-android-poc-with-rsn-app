package com.nbcsports.regional.nbc_rsn.persistentplayer;

import com.google.android.exoplayer2.upstream.HttpDataSource;

import java.io.IOException;

public class Error {

    public static final int _403 = 403;
    public static final int _410 = 410;
    public static final int _499 = 499; // custom error for geo lock temp pass
    public static final int _498 = 498; // custom error for entitlement check

    public static boolean is403(IOException error) {

        if (error instanceof HttpDataSource.InvalidResponseCodeException){
            HttpDataSource.InvalidResponseCodeException httpError = (HttpDataSource.InvalidResponseCodeException) error;
            return httpError.responseCode == _403;
        } else {
            return false;
        }
    }

    public static boolean is410(IOException error) {

        if (error == null){ return false; }

        HttpDataSource.InvalidResponseCodeException httpError = null;

        if (error instanceof HttpDataSource.InvalidResponseCodeException){
            httpError = (HttpDataSource.InvalidResponseCodeException) error;
        } else if (error.getCause() instanceof HttpDataSource.InvalidResponseCodeException){
            httpError = (HttpDataSource.InvalidResponseCodeException) error.getCause();
        }

        if (httpError != null){
            return httpError.responseCode == _410;
        } else {
            return false;
        }
    }

    public static boolean is499(IOException error) {

        if (error == null){ return false; }

        HttpDataSource.InvalidResponseCodeException httpError = null;

        if (error instanceof HttpDataSource.InvalidResponseCodeException){
            httpError = (HttpDataSource.InvalidResponseCodeException) error;
        } else if (error.getCause() instanceof HttpDataSource.InvalidResponseCodeException){
            httpError = (HttpDataSource.InvalidResponseCodeException) error.getCause();
        }

        if (httpError != null){
            return httpError.responseCode == _499;
        } else {
            return false;
        }
    }

    public static boolean is498(IOException error) {
        if (error == null){ return false; }

        HttpDataSource.InvalidResponseCodeException httpError = null;

        if (error instanceof HttpDataSource.InvalidResponseCodeException){
            httpError = (HttpDataSource.InvalidResponseCodeException) error;
        } else if (error.getCause() instanceof HttpDataSource.InvalidResponseCodeException){
            httpError = (HttpDataSource.InvalidResponseCodeException) error.getCause();
        }

        if (httpError != null){
            return httpError.responseCode == _498;
        } else {
            return false;
        }
    }
}
