package com.nbcsports.regional.nbc_rsn.authentication.clientless_api;

import com.google.gson.Gson;
import com.nbcsports.regional.nbc_rsn.authentication.Auth;
import com.nbcsports.regional.nbc_rsn.authentication.clientless_response.ClientlessResponse;
import com.nbcsports.regional.nbc_rsn.common.Config;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Function;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.google.common.net.HttpHeaders.ACCEPT;
import static com.google.common.net.HttpHeaders.AUTHORIZATION;
import static com.nbcsports.regional.nbc_rsn.authentication.clientless_api.StreamConstants.ACCEPT_JSON;
import static com.nbcsports.regional.nbc_rsn.authentication.clientless_api.StreamConstants.POST;

public class AuthNRegCodeAPI {

    private final String baseUrl;
    private final OkHttpClient client;
    private final Config config;
    private final Gson gson;

    public AuthNRegCodeAPI(String baseUrl, OkHttpClient client, Config config, Gson gson){
        this.baseUrl = baseUrl;
        this.client = client;
        this.config = config;
        this.gson = gson;
    }

    public Function<Auth, ObservableSource<Auth>> create(String requestorId, String mvpd, String deviceId) {

        return auth -> {

            String authorization = AuthorizationHeader.generateAuthorization(POST, requestorId, getUriRegistrationRecord(requestorId));

            RequestBody body = new FormBody.Builder()
                    .add("deviceId", deviceId)
                    .add("mvpd", mvpd)
                    .add("registrationURL", "https://tv.xfinity.com/")
                    .build();

            Request request = new Request.Builder()
                    .addHeader(AUTHORIZATION, authorization)
                    .addHeader(ACCEPT, ACCEPT_JSON)
                    .url(baseUrl + getUriRegistrationRecord(requestorId))
                    .post(body)
                    .build();

            Observable<Auth> o = Observable.create(emitter -> {

                Response response = client.newCall(request).execute();
                if (response.isSuccessful()){
                    ClientlessResponse clientlessResponse = gson.fromJson(response.body().charStream(), ClientlessResponse.class);
                    auth.setCreateRegistrationRecordSuccessful(true);
                    auth.setRegCode(clientlessResponse.getCode());
                } else {
                    auth.setCreateRegistrationRecordSuccessful(false);
                }
                emitter.onNext(auth);
                emitter.onComplete();
            });

            return o;
        };
    }

    private String getUriRegistrationRecord(String requestorId) throws Exception {
        String reggie = config.getAdobePass().getReggieCodePath().replace("{REQUESTOR_ID}", requestorId);
        return reggie;
    }
}
