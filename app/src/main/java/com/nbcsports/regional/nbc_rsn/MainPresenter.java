package com.nbcsports.regional.nbc_rsn;

import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings;
import android.view.View;

import com.clearbridge.bottom_notification_banner.BottomNotificationData;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nbcsports.regional.nbc_rsn.common.Config;
import com.nbcsports.regional.nbc_rsn.common.Constants;
import com.nbcsports.regional.nbc_rsn.common.FreeWheelHash;
import com.nbcsports.regional.nbc_rsn.common.LiveAssetManager;
import com.nbcsports.regional.nbc_rsn.data_bar.Affiliate;
import com.nbcsports.regional.nbc_rsn.data_bar.Affiliates;
import com.nbcsports.regional.nbc_rsn.data_bar.DataBarManager;
import com.nbcsports.regional.nbc_rsn.fabigation.FabMenu;
import com.nbcsports.regional.nbc_rsn.localization.LocalizationManager;
import com.nbcsports.regional.nbc_rsn.localization.models.Localizations;
import com.nbcsports.regional.nbc_rsn.common.Team;
import com.nbcsports.regional.nbc_rsn.common.IpGeolocation;
import com.nbcsports.regional.nbc_rsn.data_bar.LogoConfig;
import com.nbcsports.regional.nbc_rsn.urban_airship.NotificationsManagerKt;
import com.nbcsports.regional.nbc_rsn.utils.DateFormatUtils;
import com.nbcsports.regional.nbc_rsn.utils.PreferenceUtils;
import com.nbcsports.regional.nbc_rsn.utils.TotalCast;

import org.apache.commons.lang3.ArrayUtils;
import org.joda.time.DateTimeZone;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.nbcsports.regional.nbc_rsn.debug_options.DebugPresenter.CONFIGURATION_URL;


public class MainPresenter implements MainContract.Presenter {

    private final MainContract.View mainContractView;
    private final List<ConfigListener> configListeners = new ArrayList<>();
    private final OkHttpClient client;
    private final TotalCast totalCast;
    private Gson gson = new Gson();
    private Config lastKnownConfig;
    private CompositeDisposable getConfigCompositeDisposable;

    public MainPresenter(MainContract.View mainContractView) {
        this.mainContractView = checkNotNull(mainContractView);
        mainContractView.setMainPresenter(this);

        client = new OkHttpClient();
        totalCast = new TotalCast(client, gson, mainContractView);
    }

    @Override
    public void configGet() {
        getConfigFromServer()
                .flatMap(getFreeWheelHashes())
                .flatMap(getTeamsContentForPreLoad())
                .flatMap(getLiveAssetsForPreLoad())
                .flatMap(getNTPTimeForPreLoad())
                .flatMap(totalCast.totalRecall())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Config>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Config config) {
                        //System.out.println("This is the enter point: configGet onNext");
                        lastKnownConfig = config;
                        callConfigListeners(config);

                        // get logos
                        if (config != null && config.getDataBar() != null && config.getDataBar().getTeamLogos() != null) {
                            String logoUrl = config.getDataBar().getTeamLogos();
                            getTeamLogosFromServer(logoUrl);

                            if (config.getImagesBaseUrl() != null && !config.getImagesBaseUrl().isEmpty()) {
                                LiveAssetManager.getInstance().setAssetImageBaseUrl(config.getImagesBaseUrl());
                            }
                        }

                    }

                    @Override
                    public void onError(Throwable e) {
                        callConfigError(e);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    @Override
    public void configGetWithDelay(long delay, Throwable e, Context context) {
        if (getConfigCompositeDisposable == null){
            NotificationsManagerKt.INSTANCE.handleException(e, null);
            getConfigCompositeDisposable = new CompositeDisposable();
        } else {
            getConfigCompositeDisposable.clear();
        }
        getConfigCompositeDisposable.add(io.reactivex.Observable
                .timer(delay, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<Long>() {
                    @Override
                    public void onNext(Long aLong) {}
                    @Override
                    public void onError(Throwable e) {}
                    @Override
                    public void onComplete() {
                        configGet();
                    }
                }));
    }

    @Override
    public void affiliatesGet() {
        if (lastKnownConfig == null) return;

        // only if the config is complete do we get the affiliates
        getAffiliatesFromServer(lastKnownConfig.getAffiliates())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<Affiliates>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Affiliates affiliates) {
                        List<Affiliate> affiliateList = affiliates.getAffiliates();
                        DataBarManager.INSTANCE.setAffiliates(affiliateList);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private Function<Config, ObservableSource<Config>> getFreeWheelHashes() {
        return config -> {
            //System.out.println("This is the enter point: getFreeWheelHashes");
            Request request = new Request.Builder()
                    .url(config.getMvpdProviders().getFreewheelHashesUrl())
                    .get()
                    .build();
            Response response = client.newCall(request).execute();
            Type freeWheelListType = new TypeToken<List<FreeWheelHash>>() {
            }.getType();
            ArrayList<FreeWheelHash> freeWheelHashes = gson.fromJson(response.body().charStream(), freeWheelListType);
            config.setFreeWheelHashes(freeWheelHashes);
            return Observable.just(config);
        };
    }

    /**
     * Get team feeds content from server
     * And save them into share preference
     *
     * For each team, get pre load time stamp (device time)
     * and save it into share preference
     *
     * @return Function<Config, ObservableSource<Config>>
     */
    private Function<Config, ObservableSource<Config>> getTeamsContentForPreLoad() {
        return config -> {

            List<Team> teamsList = config.getTeams();
            for (Team team : teamsList){
                String teamIdString = team.getTeamId();
                String teamFullIdString = teamIdString + Constants.PRE_LOAD_TEAM_CONTENT_SUFFIX;
                Request request = new Request.Builder()
                        .url(team.getContentUrl())
                        .get()
                        .build();
                Response response = client.newCall(request).execute();
                // Save team feeds content into share preference
                PreferenceUtils.INSTANCE.setString(teamFullIdString,
                        response.body().string());

                // Save pre load time stamp into share preference
                String teamPreLoadTimeStamp = teamIdString + Constants.PRE_LOAD_TEAM_TIME_STAMP_SUFFIX;
                String deviceTimeString = DateFormatUtils.getCurrentDateTimeWithTimeZone(DateTimeZone.UTC)
                        .toString(DateFormatUtils.NTP_DATE_PATTERN);
                PreferenceUtils.INSTANCE.setString(teamPreLoadTimeStamp, deviceTimeString);
            }
            return Observable.just(config);
        };
    }

    /**
     * Get live assets from server
     * And save it into share preference
     *
     * @return Function<Config, ObservableSource<Config>>
     */
    private Function<Config, ObservableSource<Config>> getLiveAssetsForPreLoad() {
        return config -> {

            Request request = new Request.Builder()
                    .url(config.getLiveAssetsUrl())
                    .get()
                    .build();
            Response response = client.newCall(request).execute();
            PreferenceUtils.INSTANCE.setString(Constants.PRE_LOAD_LIVE_ASSETS_KEY,
                    response.body().string());

            return Observable.just(config);
        };
    }

    /**
     * Get NTP time from time.google.com
     * And save it into share preference
     *
     * @return Function<Config, ObservableSource<Config>>
     */
    private Function<Config, ObservableSource<Config>> getNTPTimeForPreLoad() {
        return config -> {

            try {
                final OkHttpClient client = new OkHttpClient();
                final Request request = new Request.Builder()
                        .url("https://time.google.com")
                        .get()
                        .build();
                Response response = client.newCall(request).execute();
                PreferenceUtils.INSTANCE.setString(Constants.PRE_LOAD_NTP_TIME_KAY,
                        response.header(Constants.NTP_DATE_KEY));
            } catch (Exception e){
                String deviceTimeString = DateFormatUtils.getCurrentDateTimeWithTimeZone(DateTimeZone.UTC)
                        .toString(DateFormatUtils.NTP_DATE_PATTERN);
                PreferenceUtils.INSTANCE.setString(Constants.PRE_LOAD_NTP_TIME_KAY,
                        deviceTimeString);
            }

            return Observable.just(config);
        };
    }

    private Function<Config, ObservableSource<Config>> filterTeamsByDMA(int dma) {
        return config -> {
            List<Team> filteredTeams = new ArrayList<>();
            for (Team team : config.getTeams()) {
                if (ArrayUtils.contains(team.getRsndmas(), dma)) {
                    filteredTeams.add(team);
                }
            }
            config.setTeams(filteredTeams);
            Observable<Config> o = Observable.just(config);

            return o;
        };
    }

    private void callConfigListeners(Config config) {
        for (ConfigListener listener : configListeners) {
            listener.onReceived(config);
        }
    }

    private void callConfigError(Throwable e) {
        for (ConfigListener listener : configListeners) {
            listener.onLoadConfigError(e);
        }
    }

    private Observable<Config> getConfigFromServer() {

        String url = PreferenceUtils.INSTANCE.getString(CONFIGURATION_URL, BuildConfig.CONFIG_URL);


        if(HttpUrl.parse(url) == null) {
            //If the url is not valid, load with default URL
            PreferenceUtils.INSTANCE.setString(CONFIGURATION_URL, BuildConfig.CONFIG_URL);
            url = PreferenceUtils.INSTANCE.getString(CONFIGURATION_URL, BuildConfig.CONFIG_URL);
        }


        final Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        return Observable.create(emitter -> {
            //System.out.println("This is the enter point: getConfigFromServer");
            try {
                Response response = client.newCall(request).execute();
                Config config = gson.fromJson(response.body().charStream(), Config.class);
                emitter.onNext(config);
                emitter.onComplete();
            } catch (IOException e) {
                Timber.e(e);
                emitter.onError(e);
            }
        });
    }

    private Observable<Affiliates> getAffiliatesFromServer(String url) {
        final Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        return Observable.create(emitter -> {
            try {
                Response response = client.newCall(request).execute();
                Affiliates affiliates = gson.fromJson(response.body().charStream(), Affiliates.class);
                emitter.onNext(affiliates);
                emitter.onComplete();
            } catch (IOException e) {
                Timber.e(e);
                emitter.onError(e);
            }
        });
    }

    private void getTeamLogosFromServer(String logoUrl) {
        if (logoUrl == null || logoUrl.isEmpty()) return;

        final Request request = new Request.Builder()
                .url(logoUrl)
                .get()
                .build();

        Observable observable = Observable.create(emitter -> {
            try {
                Response response = client.newCall(request).execute();
                LogoConfig logoConfig = gson.fromJson(response.body().charStream(), LogoConfig.class);
                emitter.onNext(logoConfig);
                emitter.onComplete();
            } catch (IOException e) {
                Timber.e(e);
            }
        });

        observable.observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<LogoConfig>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(LogoConfig logoConfig) {

                        List<LogoConfig.League> leagueList = logoConfig.getLeagues();
                        HashMap<Integer, String> map = new HashMap<>();

                        for (LogoConfig.League league : leagueList) {
                            List<LogoConfig.League.Logo> logoList = league.getLogos();

                            for (LogoConfig.League.Logo logo : logoList) {
                                map.put(logo.getStatsTeamId(), logo.getLogoUrl());
                            }
                        }

                        mainContractView.updateTeamLogos(map);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    @Override
    public Config getLastKnownConfig() {
        return this.lastKnownConfig;
    }

    @Override
    public void localizationsGet(String localizationsUrl) {
        getLocalizationsFromServer(localizationsUrl)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new Observer<Localizations>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(Localizations localizations) {
                        initializeLocalizationManager(localizations);
                        callConfigListeners(localizations);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e);
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    private Observable<Localizations> getLocalizationsFromServer(String localizationsUrl) {

        final OkHttpClient client = new OkHttpClient();

        final Request request = new Request.Builder()
                .url(localizationsUrl)
                .get()
                .build();

        return Observable.create(emitter -> {
            try {
                Response response = client.newCall(request).execute();
                Localizations localizations = gson.fromJson(response.body().charStream(), Localizations.class);
                emitter.onNext(localizations);
                emitter.onComplete();
            } catch (IOException e) {
                Timber.e(e);
                emitter.onError(e);
            }
        });
    }

    @Override
    public void geolocationGet(String geolocationUrl) {
        getUserIpGeolocation(geolocationUrl)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribeWith(new Observer<IpGeolocation>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(IpGeolocation ipGeolocation) {
                        callGeolocationListeners(ipGeolocation);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private Observable<IpGeolocation> getUserIpGeolocation(String geolocationUrl) {
        final Request request = new Request.Builder()
                .url(geolocationUrl)
                .get()
                .build();

        return Observable.create(emitter -> {
            try {
                Response response = client.newCall(request).execute();
                IpGeolocation ipGeolocation = gson.fromJson(response.body().charStream(), IpGeolocation.class);
                emitter.onNext(ipGeolocation);
                emitter.onComplete();
            } catch (IOException e) {
                Timber.e(e);
                emitter.onError(e);
            }
        });
    }

    private void callConfigListeners(Localizations localizations) {
        for (ConfigListener listener : configListeners) {
            listener.onReceivedLocalizations(localizations);
        }
    }

    private void callGeolocationListeners(IpGeolocation geolocation) {
        for (ConfigListener listener : configListeners) {
            listener.onReceivedIpGeolocation(geolocation);
        }
    }

    private void initializeLocalizationManager(Localizations localizations) {
        LocalizationManager.init(localizations);
    }

    @Override
    public void configAddListener(ConfigListener configListener) {
        configListeners.add(configListener);
    }

    @Override
    public void configRemoveListener(ConfigListener configListener) {
        configListeners.remove(configListener);
    }

    @Override
    public TotalCast getTotalCast() {
        totalCast.setConfig(lastKnownConfig);
        return totalCast;
    }

    public static class Injection {
        public static MainContract.Presenter providePresenter(Context context) {
            try {
                MainContract.View mainActivityView = (MainContract.View) context;
                return mainActivityView.getMainPresenter();
            } catch (ClassCastException castException) {
                return null;
            }
        }

        public static MainContract.View provideView(Context context) {
            try {
                MainContract.View mainActivityView = (MainContract.View) context;
                return mainActivityView;
            } catch (ClassCastException castException) {
                return null;
            }
        }
    }

    // Reads a flag indicating whether the accelerometer will be used to change screen orientation,
    // accordingly to user's setting (Settings > Display > Device rotation).
    // If true, the accelerometer will not be used unless explicitly requested by the application;
    // if false, the accelerometer will be used by default unless explicitly disabled by the application.
    public static boolean deviceIsLockedToPortrait(Context context) {
        ContentResolver cr = context.getContentResolver();
        return (Settings.System.getInt(cr, Settings.System.ACCELEROMETER_ROTATION, 0) == 0);
    }

    @Override
    public void fadeInAndFadeOurFab(float percent, View... views) {
        View fabMenu = null;
        for (View itemView : views){
            switch (itemView.getId()){
                case R.id.fab:
                    fabMenu = itemView;
                    break;
            }
        }
        if (fabMenu instanceof FabMenu){
            fabMenu.setAlpha(1.0f - percent);
        }
    }

    /**
     * This method is used to show bottom notification banner
     *
     * @param bottomNotificationData - contains title, message and etc.
     * @param duration - auto hide banner duration, will be used when persistUntilDismissed is
     *                   set to false
     */
    @Override
    public void showBottomNotification(BottomNotificationData bottomNotificationData, int duration) {
        if (mainContractView.getBottomNotificationBanner() != null
                && mainContractView.getActivityVisibility()){
            mainContractView.getBottomNotificationBanner().show(bottomNotificationData, duration);
        }
    }

    /**
     * This method is used to hide bottom notification banner
     */
    @Override
    public void hideBottomNotification() {
        if (mainContractView.getBottomNotificationBanner() != null){
            mainContractView.getBottomNotificationBanner().hide();
        }
    }
}
