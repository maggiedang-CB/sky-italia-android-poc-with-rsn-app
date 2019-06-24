package com.nbcsports.regional.nbc_rsn.authentication.clientless_api;

import com.nbcsports.regional.nbc_rsn.authentication.Auth;
import com.nbcsports.regional.nbc_rsn.common.Config;

import java.io.IOException;

import io.reactivex.Observable;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import timber.log.Timber;

import static com.google.common.net.HttpHeaders.ACCEPT;
import static com.google.common.net.HttpHeaders.AUTHORIZATION;
import static com.nbcsports.regional.nbc_rsn.authentication.clientless_api.StreamConstants.ACCEPT_JSON;
import static com.nbcsports.regional.nbc_rsn.authentication.clientless_api.StreamConstants.POST;

public class TempPassAPI {

    private static final String RESET = "https://mgmt.auth.adobe.com/reset-tempass/v2.1/reset?device_id={DEVICE_ID}&requestor_id={REQUESTOR_ID}&mvpd_id={TEMP_PASS}";

    private final Config config;
    private final OkHttpClient client;
    private final String baseUrl;

    public TempPassAPI(String baseUrl, Config config, OkHttpClient client){
        this.config = config;
        this.client = client;
        this.baseUrl = baseUrl;
    }

    private String getPath(){
       return config.getAdobePass().getTempPassUrl();
    }

    private String getUrl(){
        return baseUrl + getPath();
    }

    private String getDeleteUrl(){
        return RESET;
    }

    public Observable<Auth> create(String requestorId, String deviceId){

        Auth auth = new Auth();

        return Observable.create(emitter -> {
            try {

                String authorization = AuthorizationHeader.generateAuthorization(POST, requestorId, getPath());

                RequestBody body = new FormBody.Builder()
                        .add("requestor_id", requestorId)
                        .add("deviceId", deviceId)
                        .add("mso_id", config.getAdobePass().getTempPassProvider())
                        .add("domain_name", "adobe.com")
                        .add("deviceType", "Android")
                        .build();

                Request request = new Request.Builder()
                        .addHeader(AUTHORIZATION, authorization)
                        .addHeader(ACCEPT, ACCEPT_JSON)
                        .url(getUrl())
                        .post(body)
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

    public Observable<Auth> delete(String requestorId, String deviceId){

        Auth auth = new Auth();

        String deleteUrl = getDeleteUrl()
                .replace("{DEVICE_ID}", deviceId)
                .replace("{REQUESTOR_ID}", requestorId)
                .replace("{TEMP_PASS}", config.getAdobePass().getTempPassProvider());

        // It's weird that the apikey and token from RSN cannot be used. Instead
        // we have to use the NBCSports app one. Is this because we are using the requestor nbcsports?
        String apiKey = "WEwV2lPkLvFCSBF83D5viGGwa2mI8y4s";
        String token = "uEBlZUHnAjCPP3yGYRbu3KJfEXJD";

        Request request = new Request.Builder()
                .header("apiKey", apiKey)
                .header("Authorization", "Bearer " + token)
                .url(deleteUrl)
                .delete()
                .build();

        return Observable.create(emitter -> {

            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                Timber.d("temp pass reset successful");
                emitter.onNext(auth);
                emitter.onComplete();
            } else {
                emitter.onError(new Exception(response.body().toString()));
                Timber.d("temp pass reset fail");
            }
        });
    }
}
