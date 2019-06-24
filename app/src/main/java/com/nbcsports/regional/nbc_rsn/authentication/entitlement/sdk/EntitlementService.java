package com.nbcsports.regional.nbc_rsn.authentication.entitlement.sdk;

import android.content.Context;
import android.content.ContextWrapper;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.nbcsports.regional.nbc_rsn.common.Config;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import okhttp3.OkHttpClient;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class EntitlementService
        extends ContextWrapper {

    private final String GMO_HEADER = "NBC-None key=[key],version=3.0,timestamp==";

    private final EntitlementAPI entitlementAPI;
    private EntitlementAPI.GMOAPI gmoApi;
    private EntitlementAPI.NBCAPI nbcApi;
    private ObservableEmitter<EntitlementResponse> subscriber;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Config.Entitlements config;

    private String encryptedZip;
    private String geoZipCode = "";
    private String entitlementId;
    private String resourceId;
    private String channel;
    private String mvpdId;
    private ArrayList<String> digitalMVPDList;
    private String blackoutId;
    private String upstreamUserId;
    private String userId;
    private String deviceId;
    private String deviceType;
    private Boolean isMLB;

    public EntitlementService(Context context, Config.Entitlements config) {
        super(context);
        this.entitlementAPI = new EntitlementAPI(new Gson(), new OkHttpClient(), config);
        this.config = config;
    }
//    checkEntitlement(encryptedZip: String, entitlementID:String, resourceId: String, requestorID: String, mvpdID: String, digitalMvpdList: [String], blackoutId: Int32, upstreamUserId: String, UserId: String,
//     digitalProviderArray: NSArray, completion: (_ entitledToWatch:Bool, _ errorCode :EntitlementErrorResult) -> Void)
//    entitlementID:String, resource:String, currentMVPDID: String, encryptedZipCode: String,
//
//     Returns true if entitled. If not, throws EntitlementError
    public Observable<EntitlementResponse> checkEntitlement(final String encryptedZipCode, final String geoZipCode, final String entitlementId, final String resourceId,
                                                            final String channel, final String mvpdId, final ArrayList<String> digitalMVPDList, final String blackoutId,
                                                            final String upstreamUserId, final String userId, final String deviceId, final String deviceType, final Boolean isMLB  ) {

        this.encryptedZip = encryptedZipCode;
        this.entitlementId = entitlementId;
        this.resourceId = resourceId;
        this.channel = channel;
        this.mvpdId = mvpdId;
        this.digitalMVPDList = digitalMVPDList;
        this.blackoutId = blackoutId;
        this.upstreamUserId = upstreamUserId;
        this.userId = userId;
        this.isMLB = isMLB;
        this.geoZipCode = geoZipCode;
        this.deviceId = deviceId;
        this.deviceType = deviceType;

        return Observable.create(new ObservableOnSubscribe<EntitlementResponse>() {

            @Override
            public void subscribe(ObservableEmitter<EntitlementResponse> emitter) throws Exception {
                EntitlementService.this.subscriber = emitter;
                if((TextUtils.isEmpty(entitlementId) || entitlementId.equals("0")) && !requiresEntitlementRightsCheck()) {
                    subscriber.onNext(new EntitlementResponse(EntitlementResponse.ENTITLED));
                    subscriber.onComplete();
                    return;
                }

                requestGMOEntitlement();
            }
        });
    }

    private void SetError(EntitlementResponse response) {
        EntitlementService.this.subscriber.onNext(response);
        EntitlementService.this.subscriber.onComplete();
    }

    private void requestGMOEntitlement() {

        String authorizationHeader = GMO_HEADER.replace("[key]", "appletv_nbcsports") + System.currentTimeMillis();

        final GMOAccessRequest gmoRequest = GMOAccessRequest.buildBlackoutRequest(channel, mvpdId, encryptedZip,
                StringUtils.equalsIgnoreCase(mvpdId, "comcast_sso"));

        if (gmoApi == null) gmoApi =  entitlementAPI.getGmoRestAdapter();

        gmoApi.gmoEntitlementLookup(authorizationHeader, entitlementId, gmoRequest, new Callback<GMOAccessResponse>() {
            @Override
            public void success(GMOAccessResponse gmoAccessResponse, Response response) {
                if (gmoAccessResponse.isEntitled() && gmoAccessResponse.hasStreamingRights()) {
                    requestGmoEntitlementStatus();
                } else {
                    SetError(new EntitlementResponse(EntitlementResponse.GMO_ACCESS_ENTITLED_NO));
                    return;
                }
            }

            @Override
            public void failure(RetrofitError error) {
                EntitlementResponse response = new EntitlementResponse(EntitlementResponse.GMO_ACCESS_UNAVAILABLE_ERROR);

                EntitlementService.this.subscriber.onNext(response);
                EntitlementService.this.subscriber.onComplete();
                return;
            }
        });

    }

    private void requestGmoEntitlementStatus() {

        GMOAccessRequest gmoRequest = GMOAccessRequest.buildEntitlementRequest(channel, mvpdId, encryptedZip,
                StringUtils.equalsIgnoreCase(mvpdId, "comcast_sso"));
        String authorizationHeader = GMO_HEADER.replace("[key]", "appletv_nbcsports") + System.currentTimeMillis();

        if (gmoApi == null) gmoApi = entitlementAPI.getGmoRestAdapter();

        if (requiresEntitlementRightsCheck()) {
            gmoApi.gmoRetransmissionRightsLookup(authorizationHeader, gmoRequest.user, new Callback<GMOAccessResponse>() {
                @Override
                public void success(GMOAccessResponse gmoAccessResponse, Response response) {
                    if (gmoAccessResponse.isEntitled() && gmoAccessResponse.hasStreamingRights()) {
                        // If mlb check MLB otherwise check blackout
                        checkMLBOrBlackout();
                    } else {
                        SetError(new EntitlementResponse(EntitlementResponse.GMO_ACCESS_ENTITLED_NO));
                        return;
                    }
                }

                @Override
                public void failure(RetrofitError error) {
                    EntitlementService.this.subscriber.onNext(new EntitlementResponse(EntitlementResponse.GMO_ACCESS_UNAVAILABLE_ERROR));
                    EntitlementService.this.subscriber.onComplete();
                    return;
                }
            });

        } else {
            checkMLBOrBlackout();
        }
    }

    private void checkMLBOrBlackout() {
        if (isMLB) {
            requestMLBRights();
        } else {
            checkForBlackout();
        }
    }

    private void requestMLBRights() {

        if (nbcApi == null) nbcApi = entitlementAPI.getNBCRestAdapter();

        if (geoZipCode == null) geoZipCode = "";

        if (blackoutId == null || blackoutId.isEmpty() || blackoutId.equals("0")) {
            successfulCompletion();
        } else {
            nbcApi.mlbTravelRightsLookup(blackoutId, geoZipCode, upstreamUserId, deviceId, deviceType, "", new Callback<NBCBlackoutResponse>() {
                @Override
                public void success(NBCBlackoutResponse nbcBlackoutResponse, Response response) {
                    if (nbcBlackoutResponse.isEntitled()) {
                        EntitlementService.this.subscriber.onNext(new EntitlementResponse(EntitlementResponse.ENTITLED));
                        EntitlementService.this.subscriber.onComplete();
                        return;
                    } else {
                        SetError(new EntitlementResponse(EntitlementResponse.NBC_MLB_TRAVELING_RIGHTS_NO));
                        return;
                    }
                }

                @Override
                public void failure(RetrofitError error) {
                    EntitlementService.this.subscriber.onNext(new EntitlementResponse(EntitlementResponse.NBC_BLACKOUT_SERVICE_UNAVAILABLE));
                    EntitlementService.this.subscriber.onComplete();
                    return;
                }
            });
        }
    }

    private void checkForBlackout() {

        if (blackoutId == null || blackoutId.isEmpty() || blackoutId.equals("0")) {
            successfulCompletion();
        } else {
            if (nbcApi == null) nbcApi = entitlementAPI.getNBCRestAdapter();
            nbcApi.blackoutLookup(blackoutId, geoZipCode, new Callback<NBCBlackoutResponse>() {
                @Override
                public void success(NBCBlackoutResponse nbcBlackoutResponse, Response response) {
                    if (nbcBlackoutResponse.isEntitled()) {
                        successfulCompletion();
                        return;
                    } else {
                        SetError(new EntitlementResponse(EntitlementResponse.NBC_CONTENT_BLACKEDOUT));
                        return;
                    }
                }

                @Override
                public void failure(RetrofitError error) {
                    EntitlementService.this.subscriber.onNext(new EntitlementResponse(EntitlementResponse.NBC_BLACKOUT_SERVICE_UNAVAILABLE));
                    EntitlementService.this.subscriber.onComplete();
                    return;
                }
            });
        }
    }

    private void successfulCompletion() {
        EntitlementService.this.subscriber.onNext(new EntitlementResponse(EntitlementResponse.ENTITLED));
        EntitlementService.this.subscriber.onComplete();
    }

    private boolean requiresEntitlementRightsCheck() {
        if (config != null && config.getEntitlementChannels().size() > 0) {
            for (String entitlementChannel : config.getEntitlementChannels()) {
                if (entitlementChannel.equalsIgnoreCase(channel)) return true;
            }
        }
        return false;
    }
}
