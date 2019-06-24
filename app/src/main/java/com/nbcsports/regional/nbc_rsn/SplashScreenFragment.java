package com.nbcsports.regional.nbc_rsn;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;

import com.airbnb.lottie.LottieAnimationView;
import com.nbcsports.regional.nbc_rsn.analytics.TrackingHelper.PageInfo;
import com.nbcsports.regional.nbc_rsn.analytics.TrackingHelper.TrackingHelper;
import com.nbcsports.regional.nbc_rsn.common.BaseFragment;
import com.nbcsports.regional.nbc_rsn.common.TeamManager;
import com.nbcsports.regional.nbc_rsn.localization.LocalizationManager;
import com.nbcsports.regional.nbc_rsn.navigation.NavigationManager;
import com.nbcsports.regional.nbc_rsn.utils.ActivityUtils;
import com.nbcsports.regional.nbc_rsn.utils.FtueUtil;
import com.nbcsports.regional.nbc_rsn.utils.LocationUtils;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Predicate;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class SplashScreenFragment extends BaseFragment {

    @BindView(R.id.splash_screen_anim)
    LottieAnimationView splashScreenAnim;

    private boolean loaded = false;

    @Override
    public int getLayout() {
        return R.layout.fragment_splash_screen;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        //this.trackingPageName = "SplashScreenFragment";
        start();
        checkConfigLoaded();
    }

    @Override
    public void onResume() {
        super.onResume();

        TrackingHelper.Companion.trackPageEvent(getPageInfo());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        NavigationManager.getInstance().onSplashScreenComplete();
    }

    private void start() {
        if (getActivity() == null || getActivity().getResources() == null) return;

        MainActivity mainActivity = ((MainActivity) getActivity());
        mainActivity.hideFab();
        ActivityUtils.setStatusBarColor(mainActivity, Color.BLACK);

        splashScreenAnim.setVisibility(View.VISIBLE);
        splashScreenAnim.useHardwareAcceleration();

        Resources res = mainActivity.getResources();

        String animAssetDir = res.getString(R.string.splash_screen_animation_asset_directory);
        splashScreenAnim.setImageAssetsFolder(animAssetDir);

        String animFileName = res.getString(R.string.splash_screen_animation_filename);
        splashScreenAnim.setAnimation(animFileName);

        splashScreenAnim.playAnimation();
    }

    private void finish(){
        splashScreenAnim.useHardwareAcceleration(false);
        splashScreenAnim.clearAnimation();
        if (splashScreenAnim.isAnimating()){
            splashScreenAnim.cancelAnimation();
        }

        MainActivity mainActivity = ((MainActivity) getActivity());
        if (mainActivity == null) return;

        TeamManager teamManager = TeamManager.Companion.getInstance();

        if (teamManager != null && teamManager.getUsersTeams().isEmpty()) {
            NavigationManager.getInstance().showTeamSelectorFragment();
        } else if (teamManager != null && !teamManager.getUsersTeams().isEmpty()
                && FtueUtil.isAppFirstLaunch()){
            NavigationManager.getInstance().showFabOutroFragment();
        }

        // prompt for location only after splash screen has ended
        LocationUtils.requestUserForLocationPermissions(mainActivity);

        // delete this fragment once splash screen has completed
        mainActivity.getSupportFragmentManager()
                .beginTransaction()
                .remove(this)
                .commitAllowingStateLoss();
    }

    private void checkConfigLoaded(){

        if (getActivity() == null || getActivity().getResources() == null) return;

        MainActivity mainActivity = ((MainActivity) getActivity());
        Resources res = mainActivity.getResources();

        int checkInterval = res.getInteger(R.integer.splash_screen_check_duration_ms);

        compositeDisposable.add(Observable.interval(checkInterval, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.computation())
                .takeUntil(configLoaded())
                .subscribeWith(getLoadChecker(mainActivity)));
    }

    private Predicate<Long> configLoaded(){
        return aLong -> loaded;
    }

    private DisposableObserver<Long> getLoadChecker(MainActivity mainActivity){
        return new DisposableObserver<Long>() {

            @Override
            public void onNext(Long aLong) {
                loaded = mainActivity.getConfig() != null && LocalizationManager.isInitialized();
                //System.out.println("This is the enter point: loaded: "+loaded);
            }

            @Override
            public void onError(Throwable e) {
                Timber.e(e);
            }

            @Override
            public void onComplete() {
                //System.out.println("This is the enter point: splash screen onComplete");
                finish();
            }

        };
    }

    @Override
    public PageInfo getPageInfo() {
        return new PageInfo(false, "", "", "Splash", "", "", "", "", "", "", "");
    }
}
