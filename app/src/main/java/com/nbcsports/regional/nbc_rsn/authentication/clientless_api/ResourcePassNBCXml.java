package com.nbcsports.regional.nbc_rsn.authentication.clientless_api;

import com.nbcsports.regional.nbc_rsn.authentication.Auth;
import com.nbcsports.regional.nbc_rsn.common.Config;

import java.io.IOException;

import io.reactivex.Observable;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ResourcePassNBCXml {

    private final OkHttpClient client;
    private final Config config;

    public ResourcePassNBCXml(Config config, OkHttpClient client){
        this.config = config;
        this.client = client;
    }

    public Observable<Auth> get(String requestorId){

        Auth auth = new Auth();
        auth.setRequestorId(requestorId);

        String url = config.getAdobePass().getTokenizationUrl();

        Request request = new Request.Builder()
                .get()
                .url(url)
                .build();


        Observable<Auth> o = Observable.create(emitter -> {

            try {

                Response response = client.newCall(request).execute();

                if (response.isSuccessful()) {
                    String resourceIdTemplate = response.body().string();
                    String xml = checkResourceAuthorization(requestorId, resourceIdTemplate);
                    auth.setResourcePassNBCXml(xml);
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
    }

    private String checkResourceAuthorization(String channel, String resourceIdTemplate) {
        resourceIdTemplate = resourceIdTemplate.replaceAll("\\r|\\n", "");
        String resourceId;
        if ("nbcentertainment".equalsIgnoreCase(channel)) {
            resourceId = resourceIdTemplate.replaceFirst("<title>([a-zA-Z0-9\\s]*)</title>", "<title>" + "nbc_linear" + "</title>");
        } else if ("nbcdeportes".equalsIgnoreCase(channel)) { //// workaround, set nbcdeportes to authorize as mun2
            resourceId = resourceIdTemplate.replaceFirst("<title>([a-zA-Z0-9\\s]*)</title>", "<title>" + "mun2" + "</title>");
        } else {
            resourceId = resourceIdTemplate.replaceFirst("<title>([a-zA-Z0-9\\s]*)</title>", "<title>" + channel + "</title>");
        }
        resourceId = resourceId.replaceAll(">\\s*", ">");
        return resourceId;
    }
}
