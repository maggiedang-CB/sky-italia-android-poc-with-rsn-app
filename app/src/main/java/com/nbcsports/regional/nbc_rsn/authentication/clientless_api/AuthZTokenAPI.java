package com.nbcsports.regional.nbc_rsn.authentication.clientless_api;

import com.google.gson.Gson;
import com.nbcsports.regional.nbc_rsn.authentication.Auth;
import com.nbcsports.regional.nbc_rsn.authentication.clientless_response.AuthZToken;
import com.nbcsports.regional.nbc_rsn.common.Config;

import java.io.IOException;

import io.reactivex.Observable;
import io.reactivex.functions.Function;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.google.common.net.HttpHeaders.ACCEPT;
import static com.google.common.net.HttpHeaders.AUTHORIZATION;
import static com.nbcsports.regional.nbc_rsn.authentication.clientless_api.StreamConstants.ACCEPT_JSON;
import static com.nbcsports.regional.nbc_rsn.authentication.clientless_api.StreamConstants.GET;

public class AuthZTokenAPI {

    private final Gson gson;
    private final OkHttpClient client;
    private final String baseUrl;
    private final Config config;

    public AuthZTokenAPI(String baseUrl, OkHttpClient client, Gson gson, Config config){
        this.baseUrl = baseUrl;
        this.client = client;
        this.gson = gson;
        this.config = config;
    }

    private String uri(String deviceId, String requestorId, String resourceId){
        String endpoint = config.getAdobePass().getAuthzTokenPath()
                .replace("{DEVICE_ID}", deviceId)
                .replace("{REQUESTOR_ID}", requestorId)
                .replace("{RESOURCE}", resourceId);
        return endpoint;
    }

    private String authorizeUri(String deviceId, String requestorId, String resourceId){
        String endpoint = config.getAdobePass().getAuthorizePath()
                .replace("{DEVICE_ID}", deviceId)
                .replace("{REQUESTOR_ID}", requestorId)
                .replace("{RESOURCE}", resourceId);
        return endpoint;
    }

    public Function<Auth, Observable<Auth>> initiate(String deviceId, String requestorId){

        return auth -> {

            String url = authorizeUri(deviceId, requestorId, auth.getResourcePassNBCXml());
            String authorization = AuthorizationHeader.generateAuthorization(GET, requestorId, url);

            Request request = new Request.Builder()
                    .addHeader(ACCEPT, ACCEPT_JSON)
                    .addHeader(AUTHORIZATION, authorization)
                    .url(baseUrl + url)
                    .get()
                    .build();

            Observable<Auth> o = Observable.create(emitter -> {

                try {

                    Response response = client.newCall(request).execute();

                    if (response.isSuccessful()) {
                        AuthZToken authZToken = gson.fromJson(response.body().charStream(), AuthZToken.class);
                        emitter.onNext(auth);

                    } else {
                        emitter.onError(new Exception(response.message()));
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    emitter.onError(e);
                }
                emitter.onComplete();
            });

            return o;
        };
    }

    public Function<Auth, Observable<Auth>> retrieve(String deviceId, String requestorId){

        return auth -> {

            String url = uri(deviceId, requestorId, auth.getResourcePassNBCXml());

            String authorization = AuthorizationHeader.generateAuthorization(GET, requestorId, url);

            Request request = new Request.Builder()
                    .addHeader(ACCEPT, ACCEPT_JSON)
                    .addHeader(AUTHORIZATION, authorization)
                    .url(baseUrl + url)
                    .get()
                    .build();

            Observable<Auth> o = Observable.create(emitter -> {

                try {

                    Response response = client.newCall(request).execute();

                    if (response.isSuccessful()) {
                        AuthZToken authZToken = gson.fromJson(response.body().charStream(), AuthZToken.class);
                        auth.setAuthZToken(authZToken);
                        emitter.onNext(auth);

                    } else {
                        emitter.onError(new Exception(response.message()));
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    emitter.onError(e);
                }
                emitter.onComplete();
            });

            return o;
        };
    }
}
