package com.nbcsports.regional.nbc_rsn.authentication.clientless_api;

import com.nbcsports.regional.nbc_rsn.authentication.Auth;
import com.nbcsports.regional.nbc_rsn.authentication.StreamAuthenticationPresenter;
import com.nbcsports.regional.nbc_rsn.common.Config;

import io.reactivex.Observable;
import io.reactivex.functions.Function;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import timber.log.Timber;

import static com.google.common.net.HttpHeaders.AUTHORIZATION;
import static com.nbcsports.regional.nbc_rsn.authentication.clientless_api.StreamConstants.DELETE;

public class LogoutAPI {

    private final String baseUrl;
    private final OkHttpClient client;
    private final Config config;

    public LogoutAPI(String baseUrl, OkHttpClient client, Config config) {
        this.baseUrl = baseUrl;
        this.client = client;
        this.config = config;
    }

    public Observable<Auth> delete(String requestorId, String deviceId) {

        Auth auth = new Auth();

        String authorization = AuthorizationHeader.generateAuthorization(DELETE, requestorId, getUrlInitiateLogout(deviceId));

        Request request = new Request.Builder()
                .addHeader(AUTHORIZATION, authorization)
                .url(getUrlInitiateLogout(deviceId))
                .delete()
                .build();

        return Observable.create(emitter -> {

            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                Timber.d("log out successful");
                emitter.onNext(auth);
                emitter.onComplete();
            } else {
                emitter.onError(new Exception(response.body().toString()));
                Timber.d("log out fail");
            }
        });
    }

    private String getUrlInitiateLogout(String deviceId){
        return baseUrl
                + config.getAdobePass().getLogoutPath()
                + String.format("?deviceId=%s", deviceId);
    }

    public Function<Auth,Observable<Auth>> setAuthorizedRequestorId(StreamAuthenticationPresenter streamAuthenticationPresenter) {
        return auth -> {
            streamAuthenticationPresenter.setAuthenticated(false);
            streamAuthenticationPresenter.setAuthorizedRequestorId(null);
            streamAuthenticationPresenter.setAuthorized(false);
            Observable<Auth> o = Observable.just(auth);
            return o;
        };
    }
}
