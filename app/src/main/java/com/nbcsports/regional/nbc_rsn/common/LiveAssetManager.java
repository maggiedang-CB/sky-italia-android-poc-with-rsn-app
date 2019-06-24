package com.nbcsports.regional.nbc_rsn.common;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import lombok.Getter;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import timber.log.Timber;

public class LiveAssetManager {

    private List<Asset> liveAssets = new ArrayList<>();
    @Getter public String assetImageBaseUrl = "";

    private static LiveAssetManager instance = null;

    public static LiveAssetManager getInstance() {
        if (instance == null) {
            instance = new LiveAssetManager();
        }
        return instance;
    }

    public static void release(){
        instance = null;
    }

    public void setAssetImageBaseUrl(String baseUrl){
        if (assetImageBaseUrl.isEmpty()){
            assetImageBaseUrl = baseUrl;
        }
    }

    @SuppressWarnings("all")
    public void liveAssetsGet(String assetsUrl) {
        if (assetsUrl == null || !liveAssets.isEmpty()) return;

        getAssetsFromServer(assetsUrl)
                .flatMap(filterListAssets())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribeWith(new Observer<List<Asset>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(List<Asset> assets) {
                        liveAssets = assets;
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e("Failed in retreiving live assets", e);
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    private Observable<List<Asset>> getAssetsFromServer(String url) {

        final OkHttpClient client = new OkHttpClient();

        final Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        return Observable.create(emitter -> {
            try {
                Response response = client.newCall(request).execute();
                Type listType = new TypeToken<List<Asset>>() {
                }.getType();
                List<Asset> liveAssets = new Gson().fromJson(response.body().charStream(), listType);
                emitter.onNext(liveAssets);
                emitter.onComplete();
            } catch (IOException e) {
                Timber.e(e);
                emitter.onError(e);
            }
        });
    }

    private Function<List<Asset>, ObservableSource<List<Asset>>> filterListAssets(){
        return assets -> {
            List<Asset> filteredAssets = new ArrayList<>();
            for (Asset asset : assets) {
                if (asset.getTitle().contains("RSN")) {
                    filteredAssets.add(asset);
                }
            }
            return Observable.just(filteredAssets);
        };
    }

    public void setLiveAssets(List<Asset> assets){
        List<Asset> filteredAssets = new ArrayList<>();
        for (Asset asset : assets) {
            if (asset.getTitle().contains("RSN")) {
                filteredAssets.add(asset);
            }
        }
        liveAssets = filteredAssets;
    }

    public List<Asset> getLiveAssets() {
        return liveAssets;
    }
}
