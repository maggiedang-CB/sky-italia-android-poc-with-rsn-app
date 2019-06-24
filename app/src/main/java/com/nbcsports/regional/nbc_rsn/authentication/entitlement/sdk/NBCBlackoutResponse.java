package com.nbcsports.regional.nbc_rsn.authentication.entitlement.sdk;

import com.google.gson.annotations.Expose;


public class NBCBlackoutResponse {

    @Expose
    String authenticated;

    public boolean isEntitled() {
        return authenticated!=null && authenticated.equals("1");
    }

}
