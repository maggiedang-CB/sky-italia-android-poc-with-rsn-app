package com.nbcsports.regional.nbc_rsn.authentication.clientless_api;

import android.util.Base64;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nbcsports.regional.nbc_rsn.authentication.Auth;
import com.nbcsports.regional.nbc_rsn.authentication.clientless_response.NbcToken;

import io.reactivex.Observable;
import io.reactivex.functions.Function;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

// CDN Token Generation Service
public class NBCTokenService {

    private final OkHttpClient client;
    private final Gson gson;

    public NBCTokenService(OkHttpClient client, Gson gson){
        this.client = client;
        this.gson = gson;
    }

    public Function<Auth, Observable<Auth>> post(String pid, String url, String primaryCDN, String serviceUrl){

        return auth -> {

            TokenGenerationRequest tokenGenerationRequest = new TokenGenerationRequest(
                auth.getRequestorId(),
                auth.getResourcePassNBCXml(),
                pid,
                auth.getShortMediaToken().getSerializedToken(),
                url,
                "NBCSports", primaryCDN, "Android" );

            Observable<Auth> o = Observable.create(emitter -> {

                Request request = new Request.Builder()
                        .url(serviceUrl)
                        .post(tokenGenerationRequest.toRequestBody())
                        .build();

                Response response = client.newCall(request).execute();

                if (response.isSuccessful()){
                    NbcToken nbcToken = gson.fromJson(response.body().charStream(), NbcToken.class);
                    auth.setNbcToken(nbcToken);
                    emitter.onNext(auth);
                } else {
                    emitter.onError(new Exception("error"));
                }

                emitter.onComplete();

            });
            return o;
        };
    }

    private class TokenGenerationRequest {
        String requestorId;
        String resourceId;
        String pid;
        String token;
        String url;
        String application;
        String cdn;
        String platform;
        String version;

        public TokenGenerationRequest(String requestorId, String resourceId, String pid, String token, String url, String application, String cdn, String platform) {
            this.requestorId = requestorId;
            this.resourceId = Base64.encodeToString(resourceId.getBytes(), Base64.NO_WRAP);
            this.pid = pid;
            this.token = token;
            this.url = url;
            this.application = application;
            this.cdn = cdn;
            this.platform = platform;
            this.version = "v1";
        }

        @Override
        public String toString() {
            Gson gson = new GsonBuilder().disableHtmlEscaping().create();
            return gson.toJson(this);
        }

        public RequestBody toRequestBody() {
            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), this.toString());
            return requestBody;
        }
    }
}
