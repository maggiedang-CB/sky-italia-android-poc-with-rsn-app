package com.nbcsports.regional.nbc_rsn.authentication.entitlement.sdk;

public class EntitlementResponse {

    public short getValue() { return value; }
    public void setValue(short value) { this.value = value; }
    private short value;

    public EntitlementResponse(short val) {
        this.value = val;
    }

    public static final short
    ENTITLED = 1,

    NBC_CONTENT_BLACKEDOUT = 200,
    NBC_MLB_TRAVELING_RIGHTS_NO = 210,
    NBC_BLACKOUT_SERVICE_UNAVAILABLE = 299,

    GMO_ACCESS_ENTITLED_NO = 310,
    GMO_ACCESS_UNAVAILABLE_ERROR = 399;

}
