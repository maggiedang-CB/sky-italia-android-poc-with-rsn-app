package com.nbcsports.regional.nbc_rsn.authentication;

public interface TokenListener {
    void onSuccess(Auth auth);
    void onError(Throwable e);
}
