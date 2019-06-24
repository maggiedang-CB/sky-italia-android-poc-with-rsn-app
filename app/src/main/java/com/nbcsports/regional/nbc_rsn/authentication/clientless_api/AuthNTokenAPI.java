package com.nbcsports.regional.nbc_rsn.authentication.clientless_api;

import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.gson.Gson;
import com.nbcsports.regional.nbc_rsn.authentication.Auth;
import com.nbcsports.regional.nbc_rsn.authentication.clientless_response.AuthNToken;
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

public class AuthNTokenAPI {

    private final String baseUrl;
    private final Config config;
    private final OkHttpClient client;
    private final Gson gson;

    public AuthNTokenAPI(String baseUrl, Config config, OkHttpClient client, Gson gson) {
        this.baseUrl = baseUrl;
        this.config = config;
        this.client = client;
        this.gson = gson;
    }

    public Function<Long, Observable<Auth>> retrieve(String requestorId, String deviceId) {

        Auth auth = new Auth();
        String url = getUriRetrieveAuthenticationToken(requestorId, deviceId);

        String authorization = AuthorizationHeader.generateAuthorization(GET, requestorId, url);

        Request request = new Request.Builder()
                .addHeader(ACCEPT, ACCEPT_JSON)
                .addHeader(AUTHORIZATION, authorization)
                .url(baseUrl + url)
                .get()
                .build();

        return second -> {

            Observable<Auth> o = Observable.create(emitter -> {

                try {
                    Timber.d("retrieveAuthNToken()");
                    Response response = client.newCall(request).execute();

                    if (response.isSuccessful()) {
                        //Storing the JSON info into body for debugging purpose
                        String body = response.body().source().readUtf8();
                        AuthNToken authNToken = gson.fromJson(body, AuthNToken.class);
                        auth.setAuthNToken(authNToken);
                        emitter.onNext(auth);

                    } else {
                        //emitter.onError(new Exception(response.message()));
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    if ( ! emitter.isDisposed() ) {
                        emitter.onError(e);
                    }
                }
                emitter.onComplete();
            });

            return o;
        };
    }

    public Function<Auth, Observable<Auth>> retrieveAuth(String requestorId, String deviceId) {

        String url = getUriRetrieveAuthenticationToken(requestorId, deviceId);

        String authorization = AuthorizationHeader.generateAuthorization(GET, requestorId, url);

        Request request = new Request.Builder()
                .addHeader(ACCEPT, ACCEPT_JSON)
                .addHeader(AUTHORIZATION, authorization)
                .url(baseUrl + url)
                .get()
                .build();

        return auth -> {

            Observable<Auth> o = Observable.create(emitter -> {

                try {
                    Timber.d("retrieveAuthNToken()");
                    Response response = client.newCall(request).execute();

                    if (response.isSuccessful()) {
                        AuthNToken authNToken = gson.fromJson(response.body().charStream(), AuthNToken.class);
                        auth.setCheckAuthNSuccess(true);
                        auth.setAuthNToken(authNToken);
                        emitter.onNext(auth);

                    } else {
                        HttpDataSource.InvalidResponseCodeException e = new HttpDataSource.InvalidResponseCodeException(response.code(), null, null);
                        emitter.onError(e);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    if ( ! emitter.isDisposed() ) {
                        emitter.onError(e);
                    }
                }
                emitter.onComplete();
            });

            return o;
        };
    }

    private String getUriRetrieveAuthenticationToken(String requestor, String deviceId){
        return config.getAdobePass().getAuthnTokenPath()
                + String.format("?requestor=%s&deviceId=%s", requestor, deviceId);
    }
}
