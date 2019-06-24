package com.nbcsports.regional.nbc_rsn.authentication.entitlement;

import android.content.Context;

import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.nbcsports.regional.nbc_rsn.authentication.Auth;
import com.nbcsports.regional.nbc_rsn.authentication.clientless_response.UserMetadata;
import com.nbcsports.regional.nbc_rsn.authentication.entitlement.sdk.EntitlementResponse;
import com.nbcsports.regional.nbc_rsn.authentication.entitlement.sdk.EntitlementService;
import com.nbcsports.regional.nbc_rsn.common.Asset;
import com.nbcsports.regional.nbc_rsn.common.Config;
import com.nbcsports.regional.nbc_rsn.common.MediaSource;
import com.nbcsports.regional.nbc_rsn.common.TotalCastResponse;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.functions.Function;
import timber.log.Timber;

import static com.nbcsports.regional.nbc_rsn.authentication.entitlement.sdk.EntitlementResponse.ENTITLED;
import static com.nbcsports.regional.nbc_rsn.persistentplayer.Error._498;

public class EntitlementRSN {

    private final MediaSource mediaSource;
    private final String deviceId;
    private final TotalCastResponse totalCastResponse;
    private final EntitlementService nbcEntitlementService;
    private static Auth staticAuth;
    private final Config.Entitlements entitlementConfig;
    private final Config config;

    public EntitlementRSN(Context context, MediaSource mediaSource, Config config, String deviceId, TotalCastResponse totalCastResponse) {
        this.mediaSource = mediaSource;
        this.deviceId = deviceId;
        this.totalCastResponse = totalCastResponse;
        nbcEntitlementService = new EntitlementService(context, config.getEntitlements());
        this.entitlementConfig = config.getEntitlements();
        this.config = config;
    }

    public Function<Auth, Observable<EntitlementResponse>> check() {

        return auth -> {

            Observable<EntitlementResponse> action;
            this.staticAuth = auth;

            Timber.d("user metadata: %s", auth.getUserMetadata());
            Timber.d("mediaSource: %s", mediaSource.getAsset());

            if (! config.getEntitlements().getEnabled()){
                // if disabled in config, skip
                action = skip();
            } else if (mediaSource.getAsset() == null
                    || mediaSource.getAsset().getEntitlementId() == null){
                // If Entitlement Id is null, that means the media source is for VOD
                // So no need to go through the Entitlement service
                action = skip();
            } else if (auth.isTempPass(config)||auth.isTempPassAuthN(config)) {
                // if temp pass, skip
                action = skip();
            } else if (auth.getUserMetadata() == null) {
                action = skip();
            } else if (auth.getUserMetadata() != null
                    && auth.getUserMetadata().getData() != null
                    && StringUtils.equalsIgnoreCase(
                            auth.getUserMetadata().getData().getMvpd(),
                            config.getAdobePass().getTempPassProvider())){
                action = skip();
            } else {
                // check entitlements
                UserMetadata.Data data = auth.getUserMetadata().getData();
                String theZip = "";
                if (!StringUtils.isEmpty(data.getEncryptedZip())) {
                    theZip = data.getEncryptedZip();
                } else {
                    theZip = data.getZip();
                }
                String userId = data.getUserID();
                String upstreamUserID = data.getUpstreamUserID();
                String mvpd = data.getMvpd();

                Asset asset = mediaSource.getAsset();
                String entitlementId = asset.getEntitlementId();
                String channel = asset.getChannel();
                String blackoutID = asset.getBlackoutID();
                boolean isMLB = StringUtils.equalsIgnoreCase(asset.getLeague(), "mlb");

                String geoZipCode = "";
                if (totalCastResponse != null){
                    geoZipCode = totalCastResponse.getPostal_code();
                }

                action = nbcEntitlementService.checkEntitlement(theZip,
                        geoZipCode,
                        entitlementId,
                        "",
                        channel,
                        mvpd,
                        null,
                        blackoutID,
                        upstreamUserID,
                        userId,
                        deviceId,
                        entitlementConfig.getGmoDeviceType(),
                        isMLB);
            }
            return action;
        };
    }

    public Function<EntitlementResponse, Observable<Auth>> addToAuth() {
        return entitlementResponse -> {
            Observable<Auth> o = Observable.create(emitter -> {
                if (entitlementResponse.getValue() == ENTITLED) {
                    emitter.onNext(staticAuth);
                    emitter.onComplete();
                } else {
                    Map<String, List<String>> headerFields = new HashMap<>();
                    ArrayList<String> fields = new ArrayList<>();
                    fields.add(Short.toString(entitlementResponse.getValue()));
                    headerFields.put("entitlement_error", fields);
                    HttpDataSource.InvalidResponseCodeException exception = new HttpDataSource.InvalidResponseCodeException(_498, headerFields, null);
                    emitter.onError(exception);
                }
            });
            return o;
        };
    }

    public Observable<EntitlementResponse> skip() {
        Observable<EntitlementResponse> o = Observable.create(emitter -> {
            emitter.onNext(new EntitlementResponse(ENTITLED));
            emitter.onComplete();
        });
        return o;
    }
}
