package com.nbcsports.regional.nbc_rsn.authentication.clientless_api;

public class StreamConstants {

    public static final String POST = "POST";
    public static final String GET = "GET";
    public static final String DELETE = "DELETE";
    public static final String ACCEPT_JSON = "application/json";
    public static final String SHARED_PREF_STREAM_AUTHENTICATION = "shared.pref.stream.authentication";
    public static final String TEMP_PASS_EXPIRY = "temp.pass.expiry";
    public static final boolean USE_FIXED_REQUESTOR_ID = true;
    public static final String FIXED_REQUESTOR_ID = "nbcsports"; //CSNChicago

    public static class AuthZ {
        public enum Type {
           REGULAR, TEMP_PASS_LONGTTL
        }
    }

}
