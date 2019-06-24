package com.nbcsports.regional.nbc_rsn.authentication.clientless_api;


import com.nbcsports.regional.nbc_rsn.authentication.Auth;
import com.nbcsports.regional.nbc_rsn.common.Config;

import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Function;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AuthNInitiateAPI {

    private final OkHttpClient client;
    private final String baseUrl;
    private final Config config;

    public AuthNInitiateAPI(String baseUrl, OkHttpClient client, Config config) {
        this.baseUrl = baseUrl;
        this.client = client;
        this.config = config;
    }

    public Function<Auth, ObservableSource<Auth>> initiate(final String requestorId, String mvpd){

        return auth -> {

            Request request = new Request.Builder()
                    .url(getUrlAuthentication(auth.getRegCode(), requestorId, mvpd))
                    .get()
                    .build();

            OkHttpClient tempClient = client.newBuilder()
                    .followRedirects(false)
                    .followSslRedirects(false)
                    .build();

            Response response = tempClient
                    .newCall(request)
                    .execute();

            String redirectUrl = response.header("Location");
            if (! StringUtils.isEmpty(redirectUrl)){
                auth.setRedirectUrl(redirectUrl);
            }

            Observable<Auth> o = Observable.just(auth);
            return o;
        };
    }

    private String getUrlAuthentication(String regCode, String requestorId, String mvpd) {

        String redirectUrl = "";
        try {
            redirectUrl = URLEncoder.encode(config.getAdobePass().getRedirectUrl(), "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return baseUrl
                + "/api/v1/authenticate"
                + String.format("?reg_code=%s&requestor_id=%s&domain_name=%s&noflash=true&no_iframe=true&mso_id=%s&redirect_url=%s",
                regCode, requestorId, "adobe.com", mvpd, redirectUrl);

    }
}
