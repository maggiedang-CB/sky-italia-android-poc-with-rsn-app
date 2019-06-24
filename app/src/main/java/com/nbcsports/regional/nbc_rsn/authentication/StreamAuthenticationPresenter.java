package com.nbcsports.regional.nbc_rsn.authentication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.provider.Settings;
import androidx.annotation.DrawableRes;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import android.view.View;
import android.widget.ImageView;

import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.gms.common.util.ArrayUtils;
import com.google.gson.Gson;
import com.nbcsports.regional.nbc_rsn.MainContract;
import com.nbcsports.regional.nbc_rsn.MainPresenter;
import com.nbcsports.regional.nbc_rsn.R;
import com.nbcsports.regional.nbc_rsn.RsnApplication;
import com.nbcsports.regional.nbc_rsn.analytics.TrackingHelper.AuthInfo;
import com.nbcsports.regional.nbc_rsn.analytics.TrackingHelper.TrackingHelper;
import com.nbcsports.regional.nbc_rsn.authentication.clientless_api.AuthNCheckAPI;
import com.nbcsports.regional.nbc_rsn.authentication.clientless_api.AuthNInitiateAPI;
import com.nbcsports.regional.nbc_rsn.authentication.clientless_api.AuthNRegCodeAPI;
import com.nbcsports.regional.nbc_rsn.authentication.clientless_api.AuthNTokenAPI;
import com.nbcsports.regional.nbc_rsn.authentication.clientless_api.AuthZTokenAPI;
import com.nbcsports.regional.nbc_rsn.authentication.clientless_api.GetMvpdLogoAPI;
import com.nbcsports.regional.nbc_rsn.authentication.clientless_api.LogoutAPI;
import com.nbcsports.regional.nbc_rsn.authentication.clientless_api.NBCTokenService;
import com.nbcsports.regional.nbc_rsn.authentication.clientless_api.ResourcePassNBCXml;
import com.nbcsports.regional.nbc_rsn.authentication.clientless_api.ShortMediaTokenAPI;
import com.nbcsports.regional.nbc_rsn.authentication.clientless_api.StreamConstants;
import com.nbcsports.regional.nbc_rsn.authentication.clientless_api.TempPassAPI;
import com.nbcsports.regional.nbc_rsn.authentication.clientless_api.UserMetaDataAPI;
import com.nbcsports.regional.nbc_rsn.authentication.clientless_response.AuthNToken;
import com.nbcsports.regional.nbc_rsn.authentication.entitlement.EntitlementRSN;
import com.nbcsports.regional.nbc_rsn.authentication.entitlement.sdk.EntitlementResponse;
import com.nbcsports.regional.nbc_rsn.chromecast.ChromecastAuthorizationListener;
import com.nbcsports.regional.nbc_rsn.common.Config;
import com.nbcsports.regional.nbc_rsn.common.LiveAssetManager;
import com.nbcsports.regional.nbc_rsn.common.MediaSource;
import com.nbcsports.regional.nbc_rsn.common.TotalCastResponse;
import com.nbcsports.regional.nbc_rsn.navigation.NavigationManager;
import com.nbcsports.regional.nbc_rsn.persistentplayer.Error;
import com.nbcsports.regional.nbc_rsn.persistentplayer.PersistentPlayer;
import com.nbcsports.regional.nbc_rsn.persistentplayer.PersistentPlayerContract;
import com.nbcsports.regional.nbc_rsn.persistentplayer.layouts.Medium;
import com.nbcsports.regional.nbc_rsn.persistentplayer.view.PersistentPlayerView;
import com.nbcsports.regional.nbc_rsn.urban_airship.NotificationsManagerKt;
import com.nbcsports.regional.nbc_rsn.utils.NativeShareUtils;
import com.nbcsports.regional.nbc_rsn.utils.PreferenceUtils;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import okhttp3.OkHttpClient;
import timber.log.Timber;

import static com.nbcsports.regional.nbc_rsn.authentication.clientless_api.StreamConstants.FIXED_REQUESTOR_ID;
import static com.nbcsports.regional.nbc_rsn.authentication.clientless_api.StreamConstants.SHARED_PREF_STREAM_AUTHENTICATION;
import static com.nbcsports.regional.nbc_rsn.authentication.clientless_api.StreamConstants.TEMP_PASS_EXPIRY;
import static com.nbcsports.regional.nbc_rsn.authentication.clientless_api.StreamConstants.USE_FIXED_REQUESTOR_ID;
import static com.nbcsports.regional.nbc_rsn.persistentplayer.Error._499;
import static com.nbcsports.regional.nbc_rsn.persistentplayer.PlayerConstants.Type.MEDIUM;

public class StreamAuthenticationPresenter implements StreamAuthenticationContract.Presenter {

    public static final String LAST_SUCCESSFUL_MVPD = "last.successful.mvpd";
    private final OkHttpClient client;
    private final StreamAuthenticationContract.View contractView;
    private final MainContract.Presenter mainActivity;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private CompositeDisposable chromecastCompositeDisposable = new CompositeDisposable();
    private final Gson gson;
    private DisposableObserver<Auth> pollAuthNTokenObservable;
    private DisposableObserver<Auth> chromecastPollAuthNTokenObservable;

    // When user gets redirected back to the app from provider web login page,
    // the app use this flag to check if the authn flow has started.
    // - Set to true in doAuthN()
    // - Set to false in most onError() callback.
    @Getter
    private boolean started;
    @Setter
    private StreamAuthenticationFragment view;
    @Getter @Setter
    private boolean authorized;
    @Setter
    private Config config;

    // This is the requestor pid that is authorized successfully.
    // - Used in log out
    // - Used in reset temp pass
    // - Set to null in log out
    @Getter
    @Setter
    private String authorizedRequestorId;

    @Getter
    @Setter
    private boolean authenticated;

    @Getter
    @Setter
    private Auth lastKnownAuth;
    private DisposableObserver<Long> tempPassObservable;

    @Getter
    private boolean isSignedIn;

    public StreamAuthenticationPresenter(StreamAuthenticationContract.View contractView) {
        this.contractView = contractView;
        this.contractView.setPresenter(this);
        client = new OkHttpClient();
        gson = new Gson();
        mainActivity = MainPresenter.Injection.providePresenter(contractView.getContext());
    }

    /**
     * Check authN AND authZ, if successful play asset, if fail attempt to play one more time
     * @param context
     * @param currentView
     * @param mediaSource
     */
    @Override
    public void checkAuthAndPlay(Context context,
                                 PersistentPlayerContract.View currentView,
                                 MediaSource mediaSource) {

        PersistentPlayer persistentPlayer = com.nbcsports.regional.nbc_rsn.persistentplayer.Injection.
                providePlayer((PersistentPlayerContract.Main.View) context);

        persistentPlayer.log(this,
                "checkAuthAndPlay: StreamAuthenticationPresenter persistentPlayer.isPlaying() %s, isPaused %s",
                persistentPlayer.isPlaying(), currentView.isPaused());

        // check if the media source is equal.
        // - important to not run the code when transitioning
        // - skip this if type is 247, old media source is the same as new
        boolean isMediaSourceEqual = ( ! persistentPlayer.is247()
                && PersistentPlayer.isMediaSourceEqual(persistentPlayer.getMediaSource(), mediaSource));

        if ( ! isMediaSourceEqual) {

            if (persistentPlayer.getType() == MEDIUM){
                persistentPlayer.resetOrientation();
            }

            persistentPlayer.setMediaSource(mediaSource);
            String teamId = null;
            if(mediaSource.getDeeplink() != null) {
                teamId = mediaSource.getDeeplink().getTeam().getTeamId();
            }
            Fragment currentFragment = NavigationManager.getInstance().getCurrentFragment();

            NativeShareUtils.ShareInfo shareInfo = NativeShareUtils.generateShareInfo(currentFragment, null, mediaSource.getTitle(), mediaSource, teamId);
            persistentPlayer.updateShareInfo(shareInfo);

            // use fixed requestor pid instead of requestor pid from asset
            String requestorId;
            if (USE_FIXED_REQUESTOR_ID){
                requestorId = FIXED_REQUESTOR_ID;
            } else {
                requestorId = mediaSource.getRequestorId();
            }
            // Check authn, then authz,
            // if successful next step is poll for token.
            // if not, check temp pass.
            // if there is any error, make one last attempt to play content, then error out
            Timber.d("checkAuthAndPlay: start checking with requestorId:%s", requestorId);

            // show progress bar so that user is notified that the app is attempting to play content
            currentView.showProgress(true);

            // Check if live asset is free or not
            // 1. If it is free, then play it without checking auth and geo
            // 2. Otherwise, go through the normal process
            if (mediaSource.getLive() && mediaSource.getAsset() != null
                    && mediaSource.getAsset().isFree()){
                delayAndGoThroughFreeLiveAsset((TokenListener) currentView);
                return;
            }

            checkAuth(requestorId, mediaSource, new DisposableObserver<Auth>() {
                @Override
                public void onNext(Auth auth) {

                    if (auth.isCheckAuthNSuccess()) {

                        currentView.showProgress(true);
                        Timber.d("checkAuthAndPlay: success with requestorId:%s", requestorId);
                        pollAuthNToken(requestorId, mediaSource, (TokenListener) currentView);

                    } else {
                        Timber.d("checkAuthAndPlay: fail with requestorId:%s, attempting temp pass..", requestorId);
                        // try temp pass
                        checkTempPassGeoBlock(mediaSource, (TokenListener) currentView);
                    }
                }

                @Override
                public void onError(Throwable e) {
                    if (Error.is498(new IOException(e)) && mediaSource.getLive()){
                        ((TokenListener) currentView).onError(e);
                    } else {
                        persistentPlayer.play();
                    }
                }

                @Override
                public void onComplete() {
                }
            });
        }
    }

    /**
     * Use to check status of authn by retrieving authn token.
     * - Method is used to check mvpd logo
     * @param requestorId
     * @param disposableObserver
     */
    @Override
    public void checkAuthNStatus(String requestorId, DisposableObserver<Auth> disposableObserver) {

        AuthNCheckAPI capi = new AuthNCheckAPI(getBaseUrl(), client, config);
        Observable<Auth> checkAuthN = capi.check(requestorId, getDeviceId());

        AuthNTokenAPI tapi = new AuthNTokenAPI(getBaseUrl(), config, client, gson);
        Function<Auth, Observable<Auth>> retrieve = tapi.retrieveAuth(requestorId, getDeviceId());

        checkAuthN
                .flatMap(retrieve)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableObserver<Auth>() {

                    @Override
                    public void onNext(Auth auth) {
                        authenticated = auth.isCheckAuthNSuccess();
                        if (auth.getAuthNToken() != null) {
                            obtainMvpdUrl(checkAuthN, auth.getAuthNToken(), disposableObserver);
                            isSignedIn = true;
                        } else {
                            disposableObserver.onNext(auth);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        authenticated = false;
                        disposableObserver.onError(e);
                    }

                    @Override
                    public void onComplete() {
                        disposableObserver.onComplete();
                    }
                });
    }

    /**
     * Let user log in
     * @param mvpd
     * @param rId
     */
    @Override
    public void doAuthN(final String mvpd, final String rId) {

        String requestorId = getRequestId(rId);

        AuthNCheckAPI capi = new AuthNCheckAPI(getBaseUrl(), client, config);
        Observable<Auth> checkAuthN = capi.check(requestorId, getDeviceId());

        AuthNRegCodeAPI rapi = new AuthNRegCodeAPI(getBaseUrl(), client, config, gson);
        Function<Auth, ObservableSource<Auth>> create = rapi.create(requestorId, mvpd, getDeviceId());

        AuthNInitiateAPI iapi = new AuthNInitiateAPI(getBaseUrl(), client, config);
        Function<Auth, ObservableSource<Auth>> initiate = iapi.initiate(requestorId, mvpd);

        checkAuthN
                .flatMap(create)
                .flatMap(initiate)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Auth>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(Auth auth) {
                        Timber.d("onNext auth: %s", auth);

                        String expires = "";
                        if(auth != null && auth.getAuthNToken() != null)
                        {
                            expires = auth.getAuthNToken().getExpires();
                            expires = auth.getAuthNToken().getExpires();
                        }



                        if (auth.getRedirectUrl() != null) {

                            Intent i = new Intent(Intent.ACTION_VIEW);
                            i.setData(Uri.parse(auth.getRedirectUrl()));
                            contractView.getContext().startActivity(i);
                            started = true;

                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e("onLoadConfigError e: %s", e);
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    @Override
    public void doLogout(String rId, DisposableObserver<Auth> observer) {

        String requestorId = getRequestId(rId);

        LogoutAPI lapi = new LogoutAPI(getBaseUrl(), client, config);
        Observable<Auth> delete = lapi.delete(requestorId, getDeviceId());
        Function<Auth, Observable<Auth>> set = lapi.setAuthorizedRequestorId(this);

        delete.flatMap(set)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(observer);

        setLastKnownAuth(null);
        isSignedIn = false;
    }

    @Override
    public void pollAuthNToken(String rId, @NonNull MediaSource mediaSource, final TokenListener listener) {

        String requestorId = getRequestId(rId);

        AuthNTokenAPI tapi = new AuthNTokenAPI(getBaseUrl(), config, client, gson);
        Function<Long, Observable<Auth>> retrieve = tapi.retrieve(requestorId, getDeviceId());

        Timber.d("pollAuthNToken starting..");
        // show progress if listener is instance of PersistentPlayerContract.View
        if (listener instanceof PersistentPlayerContract.View) {
            ((PersistentPlayerContract.View) listener).showProgress(true);
        }
        pollAuthNTokenObservable = Observable.interval(0, 1, TimeUnit.SECONDS)
                .takeWhile(val -> val < config.getAdobePass().getAuthnTokenTimeoutSeconds())
                .flatMap(retrieve)
                .flatMap(checkTempPassRxAuth(mediaSource, config))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<Auth>() {

                    @Override
                    public void onNext(Auth auth) {
                        Timber.d("pollAuthNToken onNext auth: %s", auth);
                        if (auth.getAuthNToken() != null) {
                            compositeDisposable.clear();
                            PreferenceUtils.INSTANCE.setString(LAST_SUCCESSFUL_MVPD, auth.getAuthNToken().getMvpd());
                            setLastKnownAuth(auth);
                            doAuthorize(auth.getAuthNToken(), requestorId, mediaSource, StreamConstants.AuthZ.Type.REGULAR, listener);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.d("pollAuthNToken onError e: %s", e);
                        if (listener != null){
                            listener.onError(e);
                        }
                        started = false;
                        if (listener instanceof PersistentPlayerContract.View) {
                            ((PersistentPlayerContract.View) listener).showProgress(false);
                        }
                    }

                    @Override
                    public void onComplete() {
                        Timber.d("pollAuthNToken onComplete");
                        started = false;

                        // close the auth overlay and display error banner
                        if (view != null) {
                            view.loadingScreen.setVisibility(View.GONE);
                            NotificationsManagerKt.INSTANCE.showAuthNError();
                        }
                    }
                });
        compositeDisposable.add(pollAuthNTokenObservable);
    }

    @Override
    public void pollAuthNtokenStop() {
        Timber.d("pollAuthNtokenStop()");
        if (compositeDisposable != null)
            compositeDisposable.clear();
    }

    @Override
    public void resetTempPass(String rId, DisposableObserver<Auth> observer) {

        String requestorId = getRequestId(rId);

        TempPassAPI tapi = new TempPassAPI(getBaseUrl(), config, client);
        Observable<Auth> tempPassDelete = tapi.delete(requestorId, getDeviceId());

        tempPassDelete
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(observer);
    }

    @Override
    public void checkTempPassGeoBlock(MediaSource mediaSource, TokenListener listener) {
        tempPassObservable = Observable.interval(0, 1, TimeUnit.SECONDS)
                .takeWhile(aLong -> aLong < 10) // attempt to read total cast response every second, exit after 10 seconds
                .skipWhile(aLong -> mainActivity.getTotalCast().get() == null)
                .flatMap(checkTempPassRxLong(mediaSource))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<Long>() {
                    @Override
                    public void onNext(Long aLong) {
                        Timber.d("checkTempPassGeoBlock: checking.. %s", mainActivity.getTotalCast().get());
                        if (mainActivity.getTotalCast().get() != null){
                            Timber.d("checkTempPassGeoBlock: not blocked");
                            compositeDisposable.remove(tempPassObservable);
                            doTempPass(mediaSource, listener);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (listener != null) {
                            Timber.d("checkTempPassGeoBlock onError: blocked %s", e);
                            listener.onError(e);
                        }
                    }

                    @Override
                    public void onComplete() {
                        compositeDisposable.remove(tempPassObservable);
                        if (listener != null) {
                            HttpDataSource.InvalidResponseCodeException exception = new HttpDataSource.InvalidResponseCodeException(_499, null, null);
                            Timber.d("checkTempPassGeoBlock onComplete: blocked %s", exception);
                            listener.onError(exception);
                        }
                    }
                });
        compositeDisposable.add(tempPassObservable);
    }

    private void doAuthorize(AuthNToken authNToken, String rId, @NonNull MediaSource mediaSource, StreamConstants.AuthZ.Type type, TokenListener listener) {

        String requestorId = getRequestId(rId);

        //Mvpd Bar logo Url
        GetMvpdLogoAPI getMvpdLogoAPI = new GetMvpdLogoAPI(authNToken.getMvpd(), config, gson);
        Function<Auth, Observable<Auth>> retrieveUrl = getMvpdLogoAPI.retrieveUrl();
        // PassNBCXml resource
        ResourcePassNBCXml rapi = new ResourcePassNBCXml(config, client);
        Observable<Auth> passNBCXml = rapi.get(mediaSource.getRequestorId());

        // AuthZ token
        AuthZTokenAPI zApi = new AuthZTokenAPI(getBaseUrl(), client, gson, config);
        Function<Auth, Observable<Auth>> initiate = zApi.initiate(getDeviceId(), requestorId);
        Function<Auth, Observable<Auth>> retrieve = zApi.retrieve(getDeviceId(), requestorId);

        // Short Media Token
        ShortMediaTokenAPI sApi = new ShortMediaTokenAPI(getBaseUrl(), client, gson);
        Function<Auth, Observable<Auth>> obtain = sApi.obtain(getDeviceId(), requestorId);

        // NBC Token
        NBCTokenService napi = new NBCTokenService(client, gson);
        String pid = mediaSource.getPid();
        String streamUrl = mediaSource.getStreamUrl();
        String primaryCDN = (mediaSource.getAsset() != null
                && mediaSource.getAsset().getPrimaryCDN() != null)
                ? mediaSource.getAsset().getPrimaryCDN() : "akamai";
        String serviceUrl = (config != null && config.getAdobePass() != null
                && config.getAdobePass().getMultiCDNTokenizationUrl() != null
                && !config.getAdobePass().getMultiCDNTokenizationUrl().isEmpty())
                ? config.getAdobePass().getMultiCDNTokenizationUrl()
                : "https://token.playmakerservices.com/cdn";
        Function<Auth, Observable<Auth>> nbcToken = napi.post(pid, streamUrl, primaryCDN, serviceUrl);

        passNBCXml
                .flatMap(initiate)
                .flatMap(retrieve)
                .flatMap(retrieveUrl)
                .flatMap(obtain)
                .flatMap(nbcToken)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<Auth>() {
                    @Override
                    public void onNext(Auth auth) {
                        Timber.d("doAuthorize onNext auth: %s", auth);

                        if (auth.getNbcToken() != null) {
                            if(auth.getAuthNToken() == null) {
                                auth.setAuthNToken(authNToken);
                            }
                            if (listener != null)
                                listener.onSuccess(auth);
                            String business = "";
                            if(mediaSource.getDeeplink() != null && mediaSource.getDeeplink().getTeam() != null) {
                                business = mediaSource.getDeeplink().getTeam().getRegionName();
                            }

                            if(authNToken.getMvpd().toLowerCase().contains("temppass")) {
                                AuthInfo info = new AuthInfo("Pass:Temp:Authorize:Success", "nbcs.temppassauthorizesuccess", authNToken.getMvpd(),
                                        "", "Authorized - 10 Min Temp", business, "");
                                TrackingHelper.Companion.trackAuthEvent(info);
                            }
                            else {
                                AuthInfo info = new AuthInfo("Pass:Authorize:Success", "nbcs.passauthorizesuccess", authNToken.getMvpd(),
                                        "", "Authorized", business, authNToken.getUserId());
                                TrackingHelper.Companion.trackAuthEvent(info);
                            }
                            authorized = true;
                            authorizedRequestorId = requestorId;
                            savePreferenceIfTempPass(type, auth.getAuthZToken().getExpires());
                            setLastKnownAuth(auth);
                        }
                        started = false;
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.d("doAuthorize onError e:%s", e);

                        if (listener != null)
                            listener.onError(e);

                        authorized = false;
                        started = false;
                    }

                    @Override
                    public void onComplete() {
                        started = false;
                    }
                });
    }

    private void savePreferenceIfTempPass(StreamConstants.AuthZ.Type type, String expiry) {

        if (type != StreamConstants.AuthZ.Type.TEMP_PASS_LONGTTL) {
            return;
        }

        long expiryLong = Long.parseLong(expiry);
        SharedPreferences.Editor editor = contractView.getContext().getSharedPreferences(SHARED_PREF_STREAM_AUTHENTICATION, Context.MODE_PRIVATE).edit();
        editor.putLong(TEMP_PASS_EXPIRY, expiryLong);
        editor.apply();
        Timber.d("temp pass expiry saved");
    }

    private void doTempPass(MediaSource mediaSource, TokenListener listener) {

        // test requestor pid
        String requestorId = getRequestId(mediaSource.getRequestorId());

        TempPassAPI tapi = new TempPassAPI(getBaseUrl(), config, client);
        Observable<Auth> tempPass = tapi.create(requestorId, getDeviceId());

        AuthNTokenAPI napi = new AuthNTokenAPI(getBaseUrl(), config, client, gson);
        Function<Auth, Observable<Auth>> retrieve = napi.retrieveAuth(requestorId, getDeviceId());

        tempPass
                .flatMap(retrieve)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<Auth>() {
                    @Override
                    public void onNext(Auth auth) {
                        if (auth.isCheckAuthNSuccess() && auth.getAuthNToken() != null) {
                            Timber.d("temp pass: onNext() auth.getAuthNToken(): %s", auth.getAuthNToken());
                            doAuthorize(auth.getAuthNToken(), requestorId, mediaSource, StreamConstants.AuthZ.Type.TEMP_PASS_LONGTTL, listener);

                            String business = "";
                            if(mediaSource.getDeeplink() != null && mediaSource.getDeeplink().getTeam() != null) {
                                business = mediaSource.getDeeplink().getTeam().getRegionName();
                            }
                            String guid = "";
                            String mvpd = "TempPass_10min";
                            if(auth.getAuthNToken() != null) {
                                guid = auth.getAuthNToken().getUserId();
                                mvpd = auth.getAuthNToken().getMvpd();
                            }
                            AuthInfo info = new AuthInfo("Pass:Authenticate:Success Temp 10", "nbcs.temppassauthensuccess",mvpd,
                                    "Authenticated - 10 Min Temp", "", business, "");
                            TrackingHelper.Companion.trackAuthEvent(info);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.d("temp pass: onError() e:%s", e);
                        if (listener != null) {
                            listener.onError(e);
                        }
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private String getBaseUrl() {
        return config.getAdobePass().getBaseUrl();
    }

    private String getDeviceId() {
        return Settings.Secure.getString(RsnApplication.getInstance().getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    private String getRequestId(String rId) {
        String requestorId;
        if (USE_FIXED_REQUESTOR_ID) {
            requestorId = StreamConstants.FIXED_REQUESTOR_ID;
        } else {
            requestorId = rId;
        }
        return requestorId;
    }

    private void obtainMvpdUrl(Observable<Auth> checkAuthN, AuthNToken authNToken, DisposableObserver<Auth> disposableObserver) {
        GetMvpdLogoAPI mvpdLogoAPI = new GetMvpdLogoAPI(authNToken.getMvpd(), config, gson);
        Function<Auth, Observable<Auth>> retrieveLogoUrl = mvpdLogoAPI.retrieveUrl();
        checkAuthN
                .flatMap(retrieveLogoUrl)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableObserver<Auth>() {
                    @Override
                    public void onNext(Auth auth) {
                        disposableObserver.onNext(auth);
                    }

                    @Override
                    public void onError(Throwable e) {
                        disposableObserver.onError(e);
                    }

                    @Override
                    public void onComplete() {
                        disposableObserver.onComplete();
                    }
                });
    }

    private void checkAuth(String rId, MediaSource mediaSource, DisposableObserver<Auth> disposableObserver) {

        String requestorId = getRequestId(rId);

        AuthNCheckAPI capi = new AuthNCheckAPI(getBaseUrl(), client, config);
        Observable<Auth> checkAuthN = capi.check(requestorId, getDeviceId());

        UserMetaDataAPI uapi = new UserMetaDataAPI(getBaseUrl(), client, config, gson);
        Function<Auth, Observable<Auth>> getUserMetaData = uapi.get(requestorId, getDeviceId());

        EntitlementRSN entitlementService = new EntitlementRSN(contractView.getContext(),
                mediaSource,
                config,
                getDeviceId(),
                mainActivity.getTotalCast().get());
        Function<Auth, Observable<EntitlementResponse>> checkEntitlement = entitlementService.check();
        Function<EntitlementResponse, Observable<Auth>> addEntitlementResponseToAuth = entitlementService.addToAuth();

        checkAuthN
                .flatMap(getUserMetaData)
                .flatMap(checkEntitlement)
                .flatMap(addEntitlementResponseToAuth)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableObserver<Auth>() {
                    @Override
                    public void onNext(Auth auth) {
                        authenticated = auth.isCheckAuthNSuccess();
                        disposableObserver.onNext(auth);
                    }

                    @Override
                    public void onError(Throwable e) {
                        authenticated = false;
                        disposableObserver.onError(e);
                    }

                    @Override
                    public void onComplete() {
                        disposableObserver.onComplete();
                    }
                });
    }

    @Override
    public Config getConfig() {
        return config;
    }

    /**
     * TODO: This needs to be refactored
     * Show the Sign-In-to-Watch overlay view in the right Medium/Landscape/Mini view (belonging to the currently shown team view)
     */
    @Override
    public void showSignInToWatchOverlay(PersistentPlayer persistentPlayer,
                                         PersistentPlayerView playerView,
                                         MediaSource mediaSource,
                                         android.view.View persistentPlayerView,
                                         int teamPrimaryColor,
                                         IOException error) {

        if (playerView == null || playerView.getTempPassCountdownContainer() == null
                || playerView.getSignInToWatchOverlayBackground() == null
                || playerView.getTempPassExpiredContainer() == null) return;

        if (persistentPlayerView instanceof Medium) { ((Medium) persistentPlayerView).setKebabClickable(false); }

        if (teamPrimaryColor == -1){ teamPrimaryColor = Color.BLACK; }
        ImageView signInToWatchOverlayBackground = playerView.getSignInToWatchOverlayBackground();
        if (mediaSource != null) {
            setSignInToWatchBackgroundImage(signInToWatchOverlayBackground, mediaSource.getImage(), teamPrimaryColor);
        }

        // There multiple calls of this callback with Response code 403, so we are handling the first only
        View signInToWatchOverlay = playerView.getTempPassCountdownContainer();
        if (signInToWatchOverlay.getVisibility() != View.VISIBLE) {

            if (Error.is403(error)) {
                // Stop player, just in case
                persistentPlayer.setPlayWhenReady(false);
                playerView.showTempPassExpired();
            }
        }

        ConstraintLayout tempPassExpiredContainer = playerView.getTempPassExpiredContainer();
        if (tempPassExpiredContainer.getVisibility() != View.VISIBLE) {

            if (Error.is410(error)){
                // Stop player, just in case
                persistentPlayer.setPlayWhenReady(false);
                playerView.showTempPassExpired();

            } else if (Error.is499(error)) {

                playerView.showTempPassGeoBlocked();
            }
        }
    }

    /***
     *
     * Helper function for showSignInToWatchOverlay()
     */
    private static void setSignInToWatchBackgroundImage(ImageView signInToWatchOverlayBackground, String backgroundImageId, int teamPrimaryColor){
        String liveImageBaseUrl = LiveAssetManager.getInstance().getAssetImageBaseUrl();
        String imageUrlSizeSuffix = signInToWatchOverlayBackground.getContext().getResources().getString(R.string.image_size_suffix);
        String imageUrl = String.format("%s%s%s", liveImageBaseUrl, backgroundImageId, imageUrlSizeSuffix);

        Drawable placeholderDrawable = setLayerDrawableColor(R.drawable.image_placeholder, teamPrimaryColor);

        signInToWatchOverlayBackground.setVisibility(View.VISIBLE);

        Picasso.get().load(imageUrl)
                .placeholder(placeholderDrawable)
                .error(placeholderDrawable)
                .into(signInToWatchOverlayBackground);
    }

    /***
     * Helper function for setSignInToWatchBackgroundImage()
     */
    private static Drawable setLayerDrawableColor(@DrawableRes int resourceId, int color) {

        Drawable drawable = RsnApplication.getInstance().getResources().getDrawable(resourceId);
        if(drawable instanceof LayerDrawable) {
            LayerDrawable layerDrawable = (LayerDrawable) drawable;

            //Drawable coloredDrawable = layerDrawable.getDrawable(0);
            Drawable coloredDrawable = layerDrawable.findDrawableByLayerId(R.id.coloredDrawable);

            if(coloredDrawable instanceof GradientDrawable) {
                GradientDrawable gradientDrawable = (GradientDrawable) coloredDrawable;
                gradientDrawable.setColor(color);
            } else {
                //coloredDrawable.setColorFilter(color, PorterDuff.Mode.SRC);//works ok , PorterDuff.Mode.MULTIPLY);// does not work
                coloredDrawable.setTint(color); //works ok
            }

            return layerDrawable;
        } else {
            // do nothing
            return drawable;
        }
    }

    private Function<Long, Observable<Long>> checkTempPassRxLong(MediaSource mediaSource) {

        return aLong -> {

            Observable<Long> o = Observable.create(emitter -> {

                if (checkGeoBlock(mediaSource)){
                    emitter.onNext(aLong);
                    emitter.onComplete();
                } else {
                    HttpDataSource.InvalidResponseCodeException exception = new HttpDataSource.InvalidResponseCodeException(_499, null, null);
                    emitter.onError(exception);
                }
            });
            return o;
        };
    }

    private Function<Auth, Observable<Auth>> checkTempPassRxAuth(MediaSource mediaSource, Config config) {

        return auth -> {

            Observable<Auth> o = Observable.create(emitter -> {

                // only do the check if auth is temp pass
                if (auth.isTempPassAuthN(config)) {

                    if (checkGeoBlock(mediaSource)) {
                        emitter.onNext(auth);
                        emitter.onComplete();
                    } else {
                        HttpDataSource.InvalidResponseCodeException exception = new HttpDataSource.InvalidResponseCodeException(_499, null, null);
                        emitter.onError(exception);
                    }
                } else {
                // otherwise just let it pass
                    emitter.onNext(auth);
                    emitter.onComplete();
                }
            });
            return o;
        };
    }

    private boolean checkGeoBlock(MediaSource mediaSource){
        TotalCastResponse totalCastResponse = mainActivity.getTotalCast().get();
        int totalCastDMA = totalCastResponse.getNielsen_dma_id();
        int[] teamDMAs = null;
        if (mediaSource.getDeeplink() != null
                && mediaSource.getDeeplink().getTeam() != null
                && mediaSource.getDeeplink().getTeam().getRsndmas() != null){
            teamDMAs = mediaSource.getDeeplink().getTeam().getRsndmas();
        }

        return ArrayUtils.contains(teamDMAs, totalCastDMA);
    }

    /**
     * This method is used when live asset is free
     *
     * Make delay here is because the listener.onError
     * will be called before the player shows up if
     * there is no delay, which means the video won't
     * play
     *
     * @param listener
     */
    private void delayAndGoThroughFreeLiveAsset(TokenListener listener) {
        Observable.timer(2L, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableObserver<Long>() {
                    @Override
                    public void onNext(Long aLong) {
                        listener.onError(null);
                    }
                    @Override
                    public void onError(Throwable e) {}
                    @Override
                    public void onComplete() {}
                });
    }

    /**
     * This method is the chromecast version of checkAuthAndPlay(...) method
     * TODO: Should be refactored and merge into the generic checkAuthAndPlay(...) method
     *
     * @param mediaSource
     * @param chromecastAuthorizationListener
     */
    @Override
    public void chromecastCheckAuthAndPlay(MediaSource mediaSource,
                                           ChromecastAuthorizationListener chromecastAuthorizationListener) {

        // Use fixed requestor pid instead of requestor pid from asset
        String requestorId;
        if (USE_FIXED_REQUESTOR_ID){
            requestorId = FIXED_REQUESTOR_ID;
        } else {
            requestorId = mediaSource.getRequestorId();
        }
        // Check authn, then authz,
        // if successful next step is poll for token.
        // if not, check temp pass.
        // if there is any error, make one last attempt to play content, then error out
        Timber.d("chromecastCheckAuthAndPlay: start checking with requestorId:%s", requestorId);

        // Start chromecast check auth process
        chromecastCheckAuth(requestorId, mediaSource, new DisposableObserver<Auth>() {
            @Override
            public void onNext(Auth auth) {
                if (auth.isCheckAuthNSuccess()) {
                    Timber.d("chromecastCheckAuthAndPlay: success with requestorId:%s", requestorId);
                    chromecastPollAuthNToken(requestorId, mediaSource, chromecastAuthorizationListener);
                } else {
                    Timber.d("checkAuthAndPlay: fail with requestorId:%s", requestorId);
                    if (chromecastAuthorizationListener != null){
                        chromecastAuthorizationListener.onAuthorizationFailure(null);
                    }
                }
            }

            @Override
            public void onError(Throwable e) {
                Timber.d("checkAuthAndPlay: fail with requestorId:%s", requestorId);
                if (chromecastAuthorizationListener != null){
                    chromecastAuthorizationListener.onAuthorizationFailure(e);
                }
            }

            @Override
            public void onComplete() {}
        });
    }

    /**
     * This method is the chromecast version of checkAuth(...) method
     * TODO: Should be refactored and merge into the generic checkAuth(...) method
     *
     * @param rId
     * @param mediaSource
     * @param disposableObserver
     */
    private void chromecastCheckAuth(String rId, MediaSource mediaSource, DisposableObserver<Auth> disposableObserver) {

        String requestorId = getRequestId(rId);

        AuthNCheckAPI capi = new AuthNCheckAPI(getBaseUrl(), client, config);
        Observable<Auth> checkAuthN = capi.check(requestorId, getDeviceId());

        UserMetaDataAPI uapi = new UserMetaDataAPI(getBaseUrl(), client, config, gson);
        Function<Auth, Observable<Auth>> getUserMetaData = uapi.get(requestorId, getDeviceId());

        EntitlementRSN entitlementService = new EntitlementRSN(contractView.getContext(),
                mediaSource,
                config,
                getDeviceId(),
                mainActivity.getTotalCast().get());
        Function<Auth, Observable<EntitlementResponse>> checkEntitlement = entitlementService.check();
        Function<EntitlementResponse, Observable<Auth>> addEntitlementResponseToAuth = entitlementService.addToAuth();

        checkAuthN
                .flatMap(getUserMetaData)
                .flatMap(checkEntitlement)
                .flatMap(addEntitlementResponseToAuth)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableObserver<Auth>() {
                    @Override
                    public void onNext(Auth auth) {
                        disposableObserver.onNext(auth);
                    }

                    @Override
                    public void onError(Throwable e) {
                        disposableObserver.onError(e);
                    }

                    @Override
                    public void onComplete() {
                        disposableObserver.onComplete();
                    }
                });
    }

    /**
     * This method is the chromecast version of pollAuthNToken(...) method
     * TODO: Should be refactored and merge into the generic pollAuthNToken(...) method
     *
     * @param rId
     * @param mediaSource
     * @param chromecastAuthorizationListener
     */
    private void chromecastPollAuthNToken(String rId, @NonNull MediaSource mediaSource, ChromecastAuthorizationListener chromecastAuthorizationListener) {

        String requestorId = getRequestId(rId);

        AuthNTokenAPI tapi = new AuthNTokenAPI(getBaseUrl(), config, client, gson);
        Function<Long, Observable<Auth>> retrieve = tapi.retrieve(requestorId, getDeviceId());

        chromecastPollAuthNTokenObservable = Observable.interval(0, 1, TimeUnit.SECONDS)
                .takeWhile(val -> val < config.getAdobePass().getAuthnTokenTimeoutSeconds())
                .flatMap(retrieve)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<Auth>() {
                    @Override
                    public void onNext(Auth auth) {
                        Timber.d("chromecastPollAuthNToken onNext auth: %s", auth);
                        if (auth.getAuthNToken() != null){
                            chromecastCompositeDisposable.clear();
                            chromecastDoAuthorize(auth.getAuthNToken(), requestorId, mediaSource,
                                    StreamConstants.AuthZ.Type.REGULAR, chromecastAuthorizationListener);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.d("chromecastPollAuthNToken onError e: %s", e);
                        if (chromecastAuthorizationListener != null){
                            chromecastAuthorizationListener.onAuthorizationFailure(e);
                        }
                    }

                    @Override
                    public void onComplete() {
                        Timber.d("chromecastPollAuthNToken onComplete");
                        // TODO: May need to check when onComplete is called
                        if (chromecastAuthorizationListener != null){
                            chromecastAuthorizationListener.onAuthorizationFailure(null);
                        }
                    }
                });
        chromecastCompositeDisposable.add(chromecastPollAuthNTokenObservable);
    }

    /**
     * This method is the chromecast version of doAuthorize(...) method
     * TODO: Should be refactored and merge into the generic doAuthorize(...) method
     *
     * @param authNToken
     * @param rId
     * @param mediaSource
     * @param type
     * @param chromecastAuthorizationListener
     */
    private void chromecastDoAuthorize(AuthNToken authNToken, String rId, @NonNull MediaSource mediaSource,
                             StreamConstants.AuthZ.Type type,
                             ChromecastAuthorizationListener chromecastAuthorizationListener) {

        String requestorId = getRequestId(rId);

        //Mvpd Bar logo Url
        GetMvpdLogoAPI getMvpdLogoAPI = new GetMvpdLogoAPI(authNToken.getMvpd(), config, gson);
        Function<Auth, Observable<Auth>> retrieveUrl = getMvpdLogoAPI.retrieveUrl();

        // PassNBCXml resource
        ResourcePassNBCXml rapi = new ResourcePassNBCXml(config, client);
        Observable<Auth> passNBCXml = rapi.get(mediaSource.getRequestorId());

        // AuthZ token
        AuthZTokenAPI zApi = new AuthZTokenAPI(getBaseUrl(), client, gson, config);
        Function<Auth, Observable<Auth>> initiate = zApi.initiate(getDeviceId(), requestorId);
        Function<Auth, Observable<Auth>> retrieve = zApi.retrieve(getDeviceId(), requestorId);

        // Short Media Token
        ShortMediaTokenAPI sApi = new ShortMediaTokenAPI(getBaseUrl(), client, gson);
        Function<Auth, Observable<Auth>> obtain = sApi.obtain(getDeviceId(), requestorId);

        // NBC Token
        NBCTokenService napi = new NBCTokenService(client, gson);
        String pid = mediaSource.getPid();
        String streamUrl = mediaSource.getStreamUrl();
        String primaryCDN = (mediaSource.getAsset() != null
                && mediaSource.getAsset().getPrimaryCDN() != null)
                ? mediaSource.getAsset().getPrimaryCDN() : "akamai";
        String serviceUrl = (config != null && config.getAdobePass() != null
                && config.getAdobePass().getMultiCDNTokenizationUrl() != null
                && !config.getAdobePass().getMultiCDNTokenizationUrl().isEmpty())
                ? config.getAdobePass().getMultiCDNTokenizationUrl()
                : "https://token.playmakerservices.com/cdn";
        Function<Auth, Observable<Auth>> nbcToken = napi.post(pid, streamUrl, primaryCDN, serviceUrl);

        passNBCXml
                .flatMap(initiate)
                .flatMap(retrieve)
                .flatMap(retrieveUrl)
                .flatMap(obtain)
                .flatMap(nbcToken)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<Auth>() {
                    @Override
                    public void onNext(Auth auth) {
                        Timber.d("chromecastDoAuthorize onNext auth: %s", auth);
                        if (auth.getNbcToken() != null) {
                            if(auth.getAuthNToken() == null) {
                                auth.setAuthNToken(authNToken);
                            }
                            if (chromecastAuthorizationListener != null){
                                chromecastAuthorizationListener.onAuthorizationSuccess(auth);
                            }
                        } else {
                            if (chromecastAuthorizationListener != null){
                                chromecastAuthorizationListener.onAuthorizationFailure(null);
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.d("chromecastDoAuthorize onError e:%s", e);
                        if (chromecastAuthorizationListener != null){
                            chromecastAuthorizationListener.onAuthorizationFailure(e);
                        }
                    }

                    @Override
                    public void onComplete() {}
                });
    }
}
