package com.nbcsports.regional.nbc_rsn.authentication.clientless_api;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nbcsports.regional.nbc_rsn.authentication.Auth;
import com.nbcsports.regional.nbc_rsn.common.Config;
import com.nbcsports.regional.nbc_rsn.common.MvpdProviderUrl;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.functions.Function;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import timber.log.Timber;

public class GetMvpdLogoAPI {

    private String mvpdID;
    private Config config;
    private final Gson gson;
    private Type providerListType = new TypeToken<List<MvpdProviderUrl>>() {}.getType();

    public GetMvpdLogoAPI (String mvpdID, Config config, Gson gson) {
        this.mvpdID = mvpdID;
        this.config = config;
        this.gson = gson;
    }

    public String getProviderJsonUrl() {
        if (config != null) {
            return config.getMvpdProviders().getLogosUrl();
        }
        return null;
    }

    public Function<Auth, Observable<Auth>> retrieveUrl () {

        return auth -> {
            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url(getProviderJsonUrl())
                    .get()
                    .build();

            Observable<Auth> o = Observable.create(emitter -> {

                try {
                    Response response = client.newCall(request).execute();
                    ArrayList<MvpdProviderUrl> mvpdProviders = gson.fromJson(response.body().charStream(), providerListType);
                    String landscapeUrl = null;
                    String teamViewUrl = null;
                    String mvpdUrl = null;
                    for (MvpdProviderUrl mvpdProvider : mvpdProviders) {
                        if (!mvpdProvider.getProviderName().isEmpty() && mvpdID != null && mvpdProvider.getProviderID().equalsIgnoreCase(mvpdID)) {
                            landscapeUrl = mvpdProvider.getIphoneLogo();
                            teamViewUrl = mvpdProvider.getLargeLogo();
                            mvpdUrl = mvpdProvider.getMvpdUrl();
                        }
                    }
                    auth.setLandScapeLogoUrl(landscapeUrl);
                    auth.setTeamViewLogoUrl(teamViewUrl);
                    auth.setMvpdRedirectUrl(mvpdUrl);
                    emitter.onNext(auth);
                    emitter.onComplete();
                } catch (IOException e) {
                    Timber.e(e);
                    emitter.onError(e);
                }
            });
            return o;
        };
    }
}
