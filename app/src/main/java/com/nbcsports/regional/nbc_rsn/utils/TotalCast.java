package com.nbcsports.regional.nbc_rsn.utils;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import androidx.core.app.ActivityCompat;

import com.google.gson.Gson;
import com.nbcsports.regional.nbc_rsn.MainActivity;
import com.nbcsports.regional.nbc_rsn.MainContract;
import com.nbcsports.regional.nbc_rsn.RsnApplication;
import com.nbcsports.regional.nbc_rsn.common.Config;
import com.nbcsports.regional.nbc_rsn.common.TotalCastResponse;

import java.io.IOException;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import lombok.Setter;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.content.Context.LOCATION_SERVICE;

public class TotalCast {

    private static final String AK = "53cffe0151901e90d46378bc";
    private final OkHttpClient client;
    private final Gson gson;
    private final LocationManager locationManager;

    @Setter
    Config config;
    private TotalCastResponse totalCastResponse;

    public TotalCast(OkHttpClient client, Gson gson, MainContract.View mainContractView) {

        this.client = client;
        this.gson = gson;
        locationManager = (LocationManager) ((MainActivity) mainContractView).getSystemService(LOCATION_SERVICE);
    }

    public void postAsync(Location location) {
        Observable.just(1)
                .map(integer -> {
                    post(location);
                    return integer;
                })
                .subscribeOn(Schedulers.io())
                .subscribe(System.out::println);
    }

    public void post(Location location) {

        if (config == null) {
            return;
        }
        if (config.getTotalCast() == null) {
            return;
        }
        if (config.getTotalCast().getPostBody() == null) {
            return;
        }
        if (location == null) {
            return;
        }

        String urlParameters = config.getTotalCast().getPostBody()
                .replace("{API_KEY}", AK)
                .replace("{LATITUDE}", Double.toString(location.getLatitude()))
                .replace("{LONGITUDE}", Double.toString(location.getLongitude()));

        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        RequestBody body = RequestBody.create(mediaType, urlParameters);

        Request request = new Request.Builder()
                .url(config.getTotalCast().getZipLookupUrl())
                .post(body)
                .build();

        try {
            Response response = client.newCall(request).execute();
            totalCastResponse = gson.fromJson(response.body().charStream(), TotalCastResponse.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public TotalCastResponse get() {
        return totalCastResponse;
    }

    public Function<Config, ObservableSource<Config>> totalRecall() {
        return config1 -> {
            if (ActivityCompat.checkSelfPermission(RsnApplication.getInstance(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(RsnApplication.getInstance(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return Observable.just(config1);
            }
            setConfig(config1);
            Location gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            Location networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            if (gpsLocation != null)
                post(gpsLocation);
            else if (networkLocation != null)
                post(networkLocation);

            return Observable.just(config1);
        };
    }
}
