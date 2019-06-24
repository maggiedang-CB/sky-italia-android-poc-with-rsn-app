package com.nbcsports.regional.nbc_rsn.authentication.clientless_api;

import com.nbcsports.regional.nbc_rsn.authentication.Auth;
import com.nbcsports.regional.nbc_rsn.common.Config;

import java.io.IOException;

import io.reactivex.Observable;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import timber.log.Timber;

import static com.google.common.net.HttpHeaders.AUTHORIZATION;
import static com.nbcsports.regional.nbc_rsn.authentication.clientless_api.StreamConstants.POST;

public class AuthNCheckAPI {

    private final OkHttpClient client;
    private final Config config;
    private final String baseUrl;

    public AuthNCheckAPI(String baseUrl, OkHttpClient client, Config config){
        this.baseUrl = baseUrl;
        this.client = client;
        this.config = config;
    }

    public Observable<Auth> check(String requestorId, String deviceId){

        Auth auth = new Auth();

        return Observable.create(emitter -> {

            try {

                String authorization = AuthorizationHeader.generateAuthorization(POST, requestorId, getUriRegistrationRecord(requestorId));
                Request request = new Request.Builder()
                        .addHeader(AUTHORIZATION, authorization)
                        .url(getUrlCheckAuthN(deviceId))
                        .get()
                        .build();

                Response response = client.newCall(request).execute();

                if (response.isSuccessful()) {
                    auth.setCheckAuthNSuccess(true);
                    emitter.onNext(auth);
                } else {
                    auth.setCheckAuthNSuccess(false);
                    emitter.onNext(auth);
                }
                emitter.onComplete();

            } catch (IOException e) {

                Timber.e(e);
                auth.setCheckAuthNSuccess(false);
                emitter.onError(e);
            }
        });
    }

    private String getUriRegistrationRecord(String requestorId) throws Exception {
        String reggie = config.getAdobePass().getReggieCodePath().replace("{REQUESTOR_ID}", requestorId);
        return reggie;
    }

    private String getUrlCheckAuthN(String deviceId) {
        return baseUrl + getUriCheckAuthN(deviceId);
    }

    private String getUriCheckAuthN(String deviceId){
        return config.getAdobePass().getCheckAuthenticationPath().replace("{DEVICE_ID}", deviceId);

    }
}
