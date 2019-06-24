package com.nbcsports.regional.nbc_rsn.authentication.entitlement.sdk;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jakewharton.retrofit.Ok3Client;
import com.nbcsports.regional.nbc_rsn.common.Config;

import okhttp3.OkHttpClient;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;


public class EntitlementAPI {

    private final Gson gson;
    private final OkHttpClient client;
    private Config.Entitlements config;

    // BASE_URL: https://access-cloudpath.media
    public interface GMOAPI {

        @POST("/access/sports/rights")
        void gmoRetransmissionRightsLookup(@Header("Authorization") String authorization, @Body GMOAccessRequest.User body, Callback<GMOAccessResponse> callback);

        @POST("/access/sports/entitlement/{entitlementId}")
        void gmoEntitlementLookup(@Header("Authorization") String authorization, @Path("entitlementId") String eventId, @Body GMOAccessRequest body, Callback<GMOAccessResponse> callback);

        // Need to add connected call to get list of active teams
    }

    // BASE_URL: http://api.nbcsports.com
    public interface NBCAPI {
        @GET("/geo.asmx/MLEAuth2_new_travel_rights")
        void mlbTravelRightsLookup(@Query("evt") String blackoutId,
                                   @Query("z") String zipCode,
                                   @Query("accountID") String upstreamUserId,
                                   @Query("deviceID") String deviceId,
                                   @Query("deviceType") String deviceType,
                                   @Query("callback") String nocallback,
                                   Callback<NBCBlackoutResponse> callback);

        @GET("/geo.asmx/MLEAuth2?evt={blackoutId}&z={zipCode}&callback=")
        void blackoutLookup(@Path("blackoutId") String blackoutId, @Path("zipCode") String zipCode, Callback<NBCBlackoutResponse> callback);

    }

    public EntitlementAPI(Gson gson, OkHttpClient client, Config.Entitlements config) {
        this.gson = gson;
        this.client = client;
        this.config = config;
    }

    public EntitlementAPI.GMOAPI getGmoRestAdapter() {
        Gson gsonNoHtmlEscape = new GsonBuilder().disableHtmlEscaping().create();
        RestAdapter adapter = new RestAdapter.Builder()
                .setEndpoint(config.getGmoDomain())
                .setClient(new Ok3Client(client))
                //.setLogLevel(BuildConfig.DEBUG ? RestAdapter.LogLevel.FULL : RestAdapter.LogLevel.NONE)
                .setConverter(new GsonConverter(gsonNoHtmlEscape))
                .build();
        return adapter.create(EntitlementAPI.GMOAPI.class);
    }

    public EntitlementAPI.NBCAPI getNBCRestAdapter() {
        Gson gsonNoHtmlEscape = new GsonBuilder().disableHtmlEscaping().create();
        RestAdapter adapter = new RestAdapter.Builder()
                .setEndpoint(config.getNbcDomain())
                .setClient(new Ok3Client(client))
                //.setLogLevel(BuildConfig.DEBUG ? RestAdapter.LogLevel.FULL : RestAdapter.LogLevel.NONE)
                .setConverter(new GsonConverter(gsonNoHtmlEscape))
                .build();
        return adapter.create(EntitlementAPI.NBCAPI.class);
    }
}
