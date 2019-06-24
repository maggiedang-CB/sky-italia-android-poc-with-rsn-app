package com.nbcsports.regional.nbc_rsn.authentication.clientless_api;

import com.google.gson.Gson;
import com.nbcsports.regional.nbc_rsn.authentication.Auth;
import com.nbcsports.regional.nbc_rsn.authentication.clientless_response.ShortMediaToken;

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

public class ShortMediaTokenAPI {

    private final Gson gson;
    private final OkHttpClient client;
    private final String baseUrl;

    public ShortMediaTokenAPI(String baseUrl, OkHttpClient client, Gson gson){
        this.baseUrl = baseUrl;
        this.client = client;
        this.gson = gson;
    }

    private String uri(String deviceId, String requestorId, String resourceId){
        String endpoint = String.format("/api/v1/tokens/media?deviceId=%s&requestor=%s&resource=%s",
                deviceId, requestorId, resourceId);
        return endpoint;
    }

    public Function<Auth, Observable<Auth>> obtain(String deviceId, String requestorId){

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
                        ShortMediaToken token = gson.fromJson(response.body().charStream(), ShortMediaToken.class);
                        auth.setShortMediaToken(token);
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
