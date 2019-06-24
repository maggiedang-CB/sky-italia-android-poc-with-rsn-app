package com.nbcsports.regional.nbc_rsn.authentication.clientless_api;

import com.google.gson.Gson;
import com.nbcsports.regional.nbc_rsn.authentication.Auth;
import com.nbcsports.regional.nbc_rsn.authentication.clientless_response.UserMetadata;
import com.nbcsports.regional.nbc_rsn.common.Config;

import io.reactivex.Observable;
import io.reactivex.functions.Function;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import timber.log.Timber;

import static com.google.common.net.HttpHeaders.ACCEPT;
import static com.google.common.net.HttpHeaders.AUTHORIZATION;
import static com.nbcsports.regional.nbc_rsn.authentication.clientless_api.StreamConstants.ACCEPT_JSON;
import static com.nbcsports.regional.nbc_rsn.authentication.clientless_api.StreamConstants.GET;

public class UserMetaDataAPI {
    private final String baseUrl;
    private final OkHttpClient client;
    private final Config config;
    private final Gson gson;

    public UserMetaDataAPI(String baseUrl, OkHttpClient client, Config config, Gson gson) {

        this.baseUrl = baseUrl;
        this.client = client;
        this.config = config;
        this.gson = gson;
    }

    public Function<Auth, Observable<Auth>> get(String requestorId, String deviceId) {

        String path = getPath(requestorId, deviceId);
        String authorization = AuthorizationHeader.generateAuthorization(GET, requestorId, path);

        Request request = new Request.Builder()
                .addHeader(ACCEPT, ACCEPT_JSON)
                .addHeader(AUTHORIZATION, authorization)
                .url(baseUrl + path)
                .get()
                .build();

        return auth -> {
            Observable<Auth> o = Observable.create(emitter -> {
                Timber.d("get user metadata");
                Response response = client.newCall(request).execute();
                UserMetadata userMetadata = gson.fromJson(response.body().charStream(), UserMetadata.class);
                if (response.isSuccessful()){
                    auth.setUserMetadata(userMetadata);
                }
                emitter.onNext(auth);
                emitter.onComplete();

                // this call will not trigger onError since subsequent calls should not be blocked
                // emitter.onError(new Exception(response.message()));
            });
            return o;
        };
    }

    private String getPath(String requestorId, String deviceId) {
        return config.getAdobePass()
                .getGetUserMetadataPath()
                .replace("{REQUESTOR_ID}", requestorId)
                .replace("{DEVICE_ID}", deviceId);
    }
}
