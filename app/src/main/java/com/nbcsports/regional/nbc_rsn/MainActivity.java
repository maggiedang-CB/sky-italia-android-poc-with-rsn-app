package com.nbcsports.regional.nbc_rsn;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.graphics.Color;
import android.location.Location;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.text.Html;
import android.text.format.DateFormat;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import com.clearbridge.bottom_notification_banner.BottomNotificationBanner;
import com.clearbridge.bottom_notification_banner.BottomNotificationData;
import com.clearbridge.notification_banner.NotificationBanner;
import com.clearbridge.notification_banner.NotificationData;
import com.f2prateek.rx.preferences2.RxSharedPreferences;
import com.nbcsports.regional.nbc_rsn.analytics.TrackingHelper.TrackingHelper;
import com.nbcsports.regional.nbc_rsn.analytics.kochava.KochavaAnalytic;
import com.nbcsports.regional.nbc_rsn.analytics.kochava.KochavaContract;
import com.nbcsports.regional.nbc_rsn.authentication.StreamAuthenticationContract;
import com.nbcsports.regional.nbc_rsn.authentication.StreamAuthenticationPresenter;
import com.nbcsports.regional.nbc_rsn.common.Config;
import com.nbcsports.regional.nbc_rsn.common.IpGeolocation;
import com.nbcsports.regional.nbc_rsn.common.LiveAssetManager;
import com.nbcsports.regional.nbc_rsn.common.Localization;
import com.nbcsports.regional.nbc_rsn.common.Team;
import com.nbcsports.regional.nbc_rsn.common.TeamManager;
import com.nbcsports.regional.nbc_rsn.data_bar.DataBarManager;
import com.nbcsports.regional.nbc_rsn.data_menu.DataMenuDataManager;
import com.nbcsports.regional.nbc_rsn.deeplink.DeeplinkManager;
import com.nbcsports.regional.nbc_rsn.editorial_detail.EditorialDataManager;
import com.nbcsports.regional.nbc_rsn.fabigation.FabMenu;
import com.nbcsports.regional.nbc_rsn.fabigation.FabMenuAdapter;
import com.nbcsports.regional.nbc_rsn.fabigation.FabMenuInterface;
import com.nbcsports.regional.nbc_rsn.localization.LocalizationManager;
import com.nbcsports.regional.nbc_rsn.localization.models.Localizations;
import com.nbcsports.regional.nbc_rsn.navigation.NavigationManager;
import com.nbcsports.regional.nbc_rsn.persistentplayer.Builder;
import com.nbcsports.regional.nbc_rsn.persistentplayer.PersistentPlayer;
import com.nbcsports.regional.nbc_rsn.persistentplayer.PersistentPlayerContract;
import com.nbcsports.regional.nbc_rsn.persistentplayer.PlayerConstants;
import com.nbcsports.regional.nbc_rsn.persistentplayer.layouts.Landscape;
import com.nbcsports.regional.nbc_rsn.persistentplayer.layouts.Mini;
import com.nbcsports.regional.nbc_rsn.rating_review.RatingReview;
import com.nbcsports.regional.nbc_rsn.rating_review.SHOW_RATE_DIALOG_STATE;
import com.nbcsports.regional.nbc_rsn.settings.SettingsFragment;
import com.nbcsports.regional.nbc_rsn.team_view.TeamContainerFragment;
import com.nbcsports.regional.nbc_rsn.team_view.TeamsPagerFragment;
import com.nbcsports.regional.nbc_rsn.teamselector.TeamSelectorFragment;
import com.nbcsports.regional.nbc_rsn.urban_airship.NotificationsManagerKt;
import com.nbcsports.regional.nbc_rsn.utils.ActivityUtils;
import com.nbcsports.regional.nbc_rsn.utils.AudioStreamVolumeObserver;
import com.nbcsports.regional.nbc_rsn.utils.DataMenuUtils;
import com.nbcsports.regional.nbc_rsn.utils.DateFormatUtils;
import com.nbcsports.regional.nbc_rsn.utils.FtueUtil;
import com.nbcsports.regional.nbc_rsn.utils.LocationUtils;
import com.nbcsports.regional.nbc_rsn.utils.NativeShareUtils;
import com.nbcsports.regional.nbc_rsn.utils.PreferenceUtils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import lombok.Getter;
import timber.log.Timber;

import static com.nbcsports.regional.nbc_rsn.urban_airship.NotificationsManagerKt.SYSTEM_SHOWN_DURATION;

/**
 * Created by justin on 2018-03-23.
 */
public class MainActivity extends AppCompatActivity
        implements FabMenuInterface, PersistentPlayerContract.Main.View,
        MainContract.View, ConfigListener, StreamAuthenticationContract.View, KochavaContract.View, AudioStreamVolumeObserver.OnAudioStreamVolumeChangedListener {

    @BindView(R.id.fragment_container)
    FrameLayout fragmentContainer;

    @BindView(R.id.fab_logo_container)
    ImageView fabLogo;

    @BindView(R.id.fab_logo_animator)
    ImageView fabAnimator;

    @BindView(R.id.fab_logo_af)
    ImageView fabAF;

    @BindView(R.id.notification_banner)
    NotificationBanner notificationBanner;

    @BindView(R.id.bottom_notification_banner)
    BottomNotificationBanner bottomNotificationBanner;

    @BindView(R.id.slide_indicator_background_relative_layout)
    RelativeLayout slideIndicatorBackgroundRelativeLayout;

    @BindView(R.id.slide_indicator_text_view)
    TextView slideIndicatorTextView;

    @BindView(R.id.slide_indicator_image_view)
    ImageView slideIndicatorImageView;

    private PersistentPlayer persistentPlayer;
    private FabMenu fab;

    private final String PREFERENCES_FAB_RTL = "_isFabRtl";
    private final Long LOGO_CHANGE_ANIMATION = 500L;
    private MainContract.Presenter mainPresenter;

    private Config config;
    private Localizations localizations;
    private TeamManager teamManager = TeamManager.Companion.getInstance();
    private NavigationManager navigationManager;

    private final int LOCATION_SERVICES_REQUEST_CODE = 10;
    private final String PREFERENCES_INSTALLATION_DATE_KEY = "_installationDate";

    @Getter
    private StreamAuthenticationContract.Presenter streamAuthenticationPresenter;
    private KochavaContract.Presenter kochava;
    private AudioStreamVolumeObserver audioStreamVolumeObserver;

    private AlertDialog rateReviewDialog;

    public Config getConfig() {
        return config;
    }

    public Localizations getLocalizations() {
        return localizations;
    }

    public TeamManager getTeamManager() {
        return teamManager;
    }

    public DataBarManager getDataBarManager() {
        return DataBarManager.INSTANCE;
    }

    public EditorialDataManager getEditorialDataManager() {
        return EditorialDataManager.Companion.getInstance();
    }

    public NavigationManager getNavigationManager() {
        return navigationManager;
    }

    private ContentObserver deviceRotationSettingObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            persistentPlayer.setPortraitOnly(MainPresenter.deviceIsLockedToPortrait(getContext()));
        }
    };

    public boolean isActivityVisible = false;

    private final long CONFIG_GET_DELAY_WHEN_RECONNECT = 5000L;

    /**
     * Please do not remove this as work continues to figure how to save state outside of activity
     * to prevent TransactionTooLarge exception
     *
     * @param outState
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.clear();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        if (notificationBanner != null) {
            notificationBanner.bringToFront();
        }

        if (bottomNotificationBanner != null){
            bottomNotificationBanner.bringToFront();
        }

        NotificationsManagerKt.INSTANCE.setActivityWeakReference(this);

        initPreferences();
        updateFabControls(PreferenceUtils.INSTANCE.getBoolean(PREFERENCES_FAB_RTL, false));

        navigationManager = NavigationManager.getInstance();
        navigationManager.addThenHideFragments(this);
        navigationManager.showSplashScreen();

        TrackingHelper.Companion.configureAnalytics(this.getApplication());

        new MainPresenter(this);
        mainPresenter.configAddListener(this);
        mainPresenter.configGet();

        new StreamAuthenticationPresenter(this);

        // Create persistent player, this player is attached to MainActivity
        persistentPlayer = new Builder()
                .setPersistentPlayerContract(this)
                .setMainActivityContract(this)
                .setFragmentManager(getSupportFragmentManager())
                .setLandscapeFragmentID(R.id.persistent_player_landscape)
                .setMiniFragmentID(R.id.persistent_player_mini)
                .create();

        new KochavaAnalytic(this);

        kochava.trackAppLaunch();
    }

    public void showRateReviewDialog() {
        if (rateReviewDialog != null) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        String color = "#969696";
        String title = getString(R.string.rate_app_title);
        String message = getString(R.string.rate_app_message);
        String positiveText = getString(R.string.rate_and_review);
        String remindLaterText = getString(R.string.remind_me_later);
        String negativeText = getString(R.string.send_feedback);

        if (LocalizationManager.isInitialized()) {
            title = LocalizationManager.AppReview.AreYouEnjoyingApp;
            positiveText = LocalizationManager.AppReview.Yes;
            remindLaterText = LocalizationManager.AppReview.Postpone;
            negativeText = LocalizationManager.AppReview.No;
        }


        builder.setTitle(title)
                .setMessage(Html.fromHtml(String.format("<font color='%s'>%s</font>", color, message)))
                .setCancelable(false)
                .setPositiveButton(positiveText, (dialog, which) -> {

                    PreferenceUtils.INSTANCE.setString(RatingReview.INSTANCE.get_PREF_KEY_SHOW_RATE_DIALOG_STATE(), SHOW_RATE_DIALOG_STATE.DONE.name());

                    // go to Play Store
                    final String appPackageName = getPackageName();
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                    } catch (android.content.ActivityNotFoundException anfe) {
                        //For device that does not have play store installed
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                    }

                    dialog.dismiss();
                })
                .setNeutralButton(remindLaterText, (dialog, which) -> {

                    // clear history
                    RatingReview.INSTANCE.clearHistory(this);

                    dialog.dismiss();
                })
                .setNegativeButton(negativeText, (dialog, which) -> {

                    PreferenceUtils.INSTANCE.setString(RatingReview.INSTANCE.get_PREF_KEY_SHOW_RATE_DIALOG_STATE(), SHOW_RATE_DIALOG_STATE.DONE.name());

                    // go to email
                    String emailTo = "mailto:" + Uri.encode("support@nbcsports.com");
                    String emailSubject = "?subject=";
                    String emailBody = "&body=";

                    Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                    Uri uri = Uri.parse(emailTo + emailSubject + emailBody);
                    emailIntent.setData(uri);
                    startActivity(emailIntent);

                    dialog.dismiss();
                });
        rateReviewDialog = builder.create();
        builder.show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        getContentResolver().unregisterContentObserver(deviceRotationSettingObserver);
        NativeShareUtils.release();

        getDataBarManager().unsubscribe();

        // remove volume observer
        if (audioStreamVolumeObserver != null) {
            audioStreamVolumeObserver.stop();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Activity is visible after onStart(), but can be not interactable.
        // It is interactable after onResume().
        isActivityVisible = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        isActivityVisible = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        getContentResolver().registerContentObserver(
                Settings.System.getUriFor(Settings.System.ACCELEROMETER_ROTATION),
                true, deviceRotationSettingObserver);

        persistentPlayer.setPortraitOnly(MainPresenter.deviceIsLockedToPortrait(getContext()));

        DateFormatUtils.is24HourFormat = DateFormat.is24HourFormat(this);

        DeeplinkManager manager = DeeplinkManager.getInstance();
        if (manager.isState(DeeplinkManager.State.PENDING) && !navigationManager.isSplashScreenActive()) {
            navigationManager.loadFirstScreen();
        }

        getDataBarManager().subscribe();

        // add volume observer
        if (audioStreamVolumeObserver == null) {
            audioStreamVolumeObserver = new AudioStreamVolumeObserver(this);
        }
        audioStreamVolumeObserver.start(AudioManager.STREAM_MUSIC, this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mainPresenter.configRemoveListener(this);
        LocationUtils.releaseLocationListener();
        DeeplinkManager.release();
        NotificationsManagerKt.INSTANCE.release();
        LiveAssetManager.release();
        NavigationManager.release();
        NativeShareUtils.release();
        EditorialDataManager.release();
    }

    private void initPreferences() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        PreferenceUtils.INSTANCE.setRxPreferences(RxSharedPreferences.create(preferences));
    }

    public void updateFabControls(boolean isFabRtl) {
        if (fab == null) {
            fab = findViewById(R.id.fab);
        }

        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) fab.getLayoutParams();

        if (isFabRtl) {
            fab.initFabMenu(this, FabMenu.MenuOrientation.RIGHT_TO_LEFT);
            params.horizontalBias = (float) 0.95;
        } else {
            fab.initFabMenu(this, FabMenu.MenuOrientation.LEFT_TO_RIGHT);
            params.horizontalBias = (float) 0.05;
        }

        fab.setLayoutParams(params);
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public void showMini() {
        Mini mini = (Mini) getSupportFragmentManager()
                .findFragmentById(R.id.persistent_player_mini);

        if (mini != null) {
            mini.show(getSupportFragmentManager());
        }
    }

    @Override
    public void hideMini() {
        Mini mini = (Mini) getSupportFragmentManager()
                .findFragmentById(R.id.persistent_player_mini);

        if (mini != null) {
            mini.hide(getSupportFragmentManager());
        }
    }

    @Override
    public void fadeInAndFadeOurMini(float percent) {
        Mini mini = (Mini) getSupportFragmentManager()
                .findFragmentById(R.id.persistent_player_mini);

        if (mini != null) {
            mini.changeMiniAlpha(percent);
        }
    }

    @Override
    public void showLandscape(PlayerConstants.Type source) {
        Landscape landscape = (Landscape) getSupportFragmentManager()
                .findFragmentById(R.id.persistent_player_landscape);

        if (landscape != null) {
            landscape.show(getSupportFragmentManager(), source);
        }
    }

    @Override
    public void hideLandscape() {
        Landscape landscape = (Landscape) getSupportFragmentManager()
                .findFragmentById(R.id.persistent_player_landscape);

        if (landscape != null) {
            landscape.hide(getSupportFragmentManager());
        }
    }

    @Override
    public PersistentPlayer getPersistentPlayer() {
        return persistentPlayer;
    }

    @Override
    public void onBackPressed() {
        if (closeLandscape()) {
            return;
        }
        if (DataMenuUtils.INSTANCE.getDATA_MENU_ROSTER_IS_OPENED()) {
            if (navigationManager.getRosterFragment() != null) {
                navigationManager.popAndRemoveFragmentFromBackStack(navigationManager.getRosterFragment());
            }
        } else if (DataMenuUtils.INSTANCE.getDATA_MENU_SCORE_IS_OPENED()) {
            if (navigationManager.getScoresFragment() != null) {
                navigationManager.popAndRemoveFragmentFromBackStack(navigationManager.getScoresFragment());
            }
        } else if (DataMenuUtils.INSTANCE.getDATA_MENU_SCHEDULE_IS_OPENED()) {
            if (navigationManager.getDataMenuScheduleFragment() != null) {
                navigationManager.popAndRemoveFragmentFromBackStack(navigationManager.getDataMenuScheduleFragment());
            }
        } else if (DataMenuUtils.INSTANCE.getDATA_MENU_STANDINGS_IS_OPENED()) {
            if (navigationManager.getStandingsFragment() != null) {
                navigationManager.popAndRemoveFragmentFromBackStack(navigationManager.getStandingsFragment());
            }
        } else if (DataMenuUtils.INSTANCE.getDATA_MENU_IS_OPENED()) {
            TeamContainerFragment currentTeamViewTemplate = getCurrentTeamContainerFragment();
            if (currentTeamViewTemplate != null) {
                currentTeamViewTemplate.closeDataMenu();
            }
        } else if (navigationManager.closeAndPopFragmentForStackableView()) {
            return;
        } else if (navigationManager.closeDebugFragment()) {
            return;
        } else if (!navigationManager.closeAndRemoveFragmentFromBackStack()) {
            return;
        } else {
            super.onBackPressed();
        }
    }

    public boolean closeLandscape() {
        if (persistentPlayer.isLandscapeVisible(getSupportFragmentManager())) {
            Landscape landscape = (Landscape) getSupportFragmentManager()
                    .findFragmentById(R.id.persistent_player_landscape);
            landscape.onClose();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // this is here as a reminder that Main has this in the manifest
        // android:configChanges="orientation|screenSize"
        // layout-land is ignored because of it
        persistentPlayer.onConfigurationChanged(newConfig);
    }

    @Override
    public void enterMenu() {
        int enterAnimationId = R.anim.no_anim;
        int exitAnimationId = R.anim.exit_team;

        if (FtueUtil.isAppFirstLaunch()) {
            clearFabAnimations();
            fab.setUpOnTouchListener(true);
        }

        if (teamManager != null && teamManager.getSelectedTeam() != null) {
            //Update status bar when leaving Settings or More Teams fragment.
            Team selectedTeam = teamManager.getSelectedTeam();
            if (teamManager.getMoreTeamsList().contains(selectedTeam)) {
                // If user is leaving a team view accessed via more teams, or leaving settings fragment:
                //  Re-entering more teams fragment will not take user to the same team view.
                //  Therefore update status bar and fab menu.
                ActivityUtils.setStatusBarColor(this,
                        Color.parseColor(teamManager.getTeamsForMenu().get(0).getPrimaryColor()));
                switchFabLogo(null);
            } else {
                ActivityUtils.setStatusBarColor(this, Color.parseColor(selectedTeam.getPrimaryColor()));
            }

        }


        if (navigationManager.getCurrentFragment() instanceof SettingsFragment) {
            //Shows the status bar with color
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            exitAnimationId = R.anim.exit_setting;
        } else {
            navigationManager.getMenuFragment().animateTransitionView(R.anim.enter_menu, null, navigationManager.getCurrentFragment(), true);
        }

        navigationManager.showMenuFragment(enterAnimationId, exitAnimationId);
        persistentPlayer.showMiniIfAvailable(false);
    }

    @Override
    public void exitMenu(FabMenuAdapter.FabCardType fabCardType) {
        int enterAnimationId = R.anim.enter_team;
        int exitAnimationId = R.anim.exit_menu;
        Timber.e("exit to : %s", fabCardType.name());

        fab.setVisibility(View.VISIBLE);
        switch (fabCardType) {
            case Settings:
                navigationManager.showSettingsFragment(enterAnimationId, exitAnimationId);
                switchFabLogo(null);
                break;
            case MoreTeams:
                navigationManager.showMoreTeamFragment(enterAnimationId, exitAnimationId);
                ActivityUtils.setStatusBarColor(this, Color.BLACK);
                switchFabLogo(null);
                break;
            case FabOutro:
                navigationManager.showFabOutroFragment(enterAnimationId, exitAnimationId);
                ActivityUtils.setStatusBarColor(this, Color.BLACK);
                switchFabLogo(null);
                break;
            default:
            case Card:
                navigationManager.showTeamPagerFragment(enterAnimationId, exitAnimationId);
                if (FtueUtil.fabHasNeverBeenFlicked()) {
                    FtueUtil.recordFabUse();
                }
                break;
        }

        navigationManager.getMenuFragment().animateTransitionView(R.anim.exit_transition_view, fabCardType, navigationManager.getMenuFragment(), false);
    }

    public void onTeamsPageChange() {
        TeamContainerFragment currentTeamViewTemplate = getCurrentTeamContainerFragment();
        if (currentTeamViewTemplate != null) {
            currentTeamViewTemplate.displayDataBarElevation();
        }
        persistentPlayer.resetSavedScrubberPosition();
    }

    @Override
    public void tapFab(boolean isTapFab) {
        if (navigationManager.getCurrentFragment() == navigationManager.getTeamsPagerFragment()) {
            TeamContainerFragment currentTeamViewTemplate = getCurrentTeamContainerFragment();
            //This check handles user clicks the fab button when exiting fab menu
            if (currentTeamViewTemplate.isAdded()) {
                // This check if at least one editorial details view is opened
                // If so close all all editorial details view
                // Otherwise, scroll team feed to the top
                if (currentTeamViewTemplate.hasAtLeastOneStackableViewOpen()) {
                    // This check if user has never tap to close article
                    // and this method is called by user tapping the fab to close article
                    // If so, record user tap to close article
                    if (isTapFab && FtueUtil.getFabTapToClose() == 0) {
                        FtueUtil.recordFabTapToClose();
                    }
                    // Check if data menu ftue is in process and not done yet and is shown long
                    // enough, if so, marks data menu ftue as done (scenario 5)
                    if (!FtueUtil.hasDoneDataMenuFtue()
                            && NotificationsManagerKt.INSTANCE.isFtueDataMenuBottomBannerShowing()
                            && FtueUtil.isDataMenuMsgShownLongEnough()){
                        // Set data menu ftue done to true
                        FtueUtil.setHasDoneDataMenuFtue(true);
                    }
                    currentTeamViewTemplate.closeAllPages();
                } else {
                    currentTeamViewTemplate.scrollTeamFeedToTheTop();
                }
            }
        }

        //The following commented code is responsible for displaying the authentic fan which should not show up for this release
        //Please leave it since we might revisit AF in the later releases
/*
        currentFragment = ActivityUtils.hideThenShowFragment(getSupportFragmentManager(),
                currentFragment,
                authenticfanFragment.isVisible() ? teamsPagerFragment : authenticfanFragment);
        fabAF.setImageResource(R.drawable.authentic_fan_on_black_full_white_copy);
        if (authenticfanFragment.isVisible()) {
            fabLogo.setVisibility(View.VISIBLE);
            fabAF.setVisibility(View.INVISIBLE);
        } else {
            fabLogo.setVisibility(View.INVISIBLE);
            fabAF.setVisibility(View.VISIBLE);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }*/
    }

    @Override
    public void flingLeft() {
        fling(false);
        FtueUtil.setFabFlung(this);
    }

    @Override
    public void flingRight() {
        fling(true);
        FtueUtil.setFabFlung(this);
    }

    @Override
    public void switchFabLogo(Team team) {
        if (team == null || team.getLogoUrl() == null || team.getLogoUrl().isEmpty()
                || (NotificationsManagerKt.INSTANCE.isFtueFabTapBannerShowing()
                && FtueUtil.hasViewedFabTapMsg())) {
            fabLogo.setImageResource(0);
            return;
        }

        Picasso.get().load(team.getLogoUrl()).fetch(new Callback() {
            @Override
            public void onSuccess() {
                fabLogo.setAlpha(0f);
                Picasso.get().load(team.getLogoUrl()).into(fabLogo);
                fabLogo.animate().setDuration(LOGO_CHANGE_ANIMATION).alpha(1f).start();
            }

            @Override
            public void onError(Exception e) {
                // do nothing
                fabLogo.setImageResource(0);
            }
        });
    }

    public void fling(boolean flingDirectionRight) {
        if (navigationManager.getCurrentFragment() instanceof TeamsPagerFragment) {
            TeamsPagerFragment teamsPagerFragment = ((TeamsPagerFragment) navigationManager.getCurrentFragment());
            Team team = teamsPagerFragment.showTeam(flingDirectionRight);
            if (team != null) {
                switchFabLogo(team);
                teamManager.setSelectedTeam(team);
            }
        }
    }

    @Override
    public void setMainPresenter(MainContract.Presenter mainPresenter) {
        this.mainPresenter = mainPresenter;
    }

    @Override
    public void setPresenter(StreamAuthenticationContract.Presenter presenter) {
        this.streamAuthenticationPresenter = presenter;
    }

    @Override
    public StreamAuthenticationContract.Presenter getPresenter() {
        return streamAuthenticationPresenter;
    }

    @Override
    public void onReceived(Config config) {
        Timber.d("rsn config: %s", config);

        //TODO: Where to keep the received Config data, like highlights and playNonFeatureGifs?
        //TODO: Can we keep reference to the received Config object, in Main? See Main.getConfig().
        //TODO: (Or may be define extra members in the TeamManager class instance?0
        this.config = config;

        mainPresenter.localizationsGet(config.getLocalization().getUrl());
        // Set TeamManager data
        List<Team> teamList = config.getTeams();
        teamManager.setMasterList(teamList);

        // Get Affiliates
        mainPresenter.affiliatesGet();

        // TODO: update the user's personal list later
        // filters can be added in MainPresenter.configGet()
        String teamIds = PreferenceUtils.INSTANCE.getString("teamlist", "");
        if (!teamIds.isEmpty()) {
            // if the team list is empty, user will be directed to the team selector
            teamManager.restoreUserList(teamIds);
        }

        // update the databar data
        if (config.getDataBar() != null) {
            getDataBarManager().setConfigInfo(config.getDataBar());
            String databarLogoUrl = config.getDataBar().getTeamLogos();
        }

        if (config.getDataMenu() != null) {
            DataMenuDataManager.INSTANCE.setConfigInfo(config.getDataMenu());
        }

        getDataBarManager().setTeams(teamManager.getUsersTeams());


        NotificationsManagerKt.INSTANCE.loadNotificationTags(config);
        if (isFirstAppLaunch()) {
            NotificationsManagerKt.INSTANCE.UAFirstLaunch();
        }

        LocationUtils.onConfigReceived(this);
        if (streamAuthenticationPresenter != null) {
            streamAuthenticationPresenter.setConfig(config);
        }


        // init RatingReview with config values
        if (config.getReviewPromptDaysMaximum() > 0 && config.getReviewPromptLaunchesThreshold() > 0) {
            RatingReview.INSTANCE.init(config.getReviewPromptLaunchesThreshold(), config.getReviewPromptDaysMaximum());

            if (RatingReview.INSTANCE.shouldShowRateDialog(this)
                    || PreferenceUtils.INSTANCE.getString(RatingReview.INSTANCE.get_PREF_KEY_SHOW_RATE_DIALOG_STATE(), "").equalsIgnoreCase(SHOW_RATE_DIALOG_STATE.YES.name())) {
                PreferenceUtils.INSTANCE.setString(RatingReview.INSTANCE.get_PREF_KEY_SHOW_RATE_DIALOG_STATE(), SHOW_RATE_DIALOG_STATE.YES.name());
            } else {
                PreferenceUtils.INSTANCE.setString(RatingReview.INSTANCE.get_PREF_KEY_SHOW_RATE_DIALOG_STATE(), SHOW_RATE_DIALOG_STATE.NO.name());
            }
        }
    }

    @Override
    public void onLoadConfigError(Throwable e) {
        mainPresenter.configGetWithDelay(CONFIG_GET_DELAY_WHEN_RECONNECT, e, this);
    }

    @Override
    public void closeStreamAuthenticationFragment() {
        // Check whether state of fragment manager is saved
        // If so, that means fragmentManager.popBackStackImmediate()
        // will throw an IllegalStateException
        // Otherwise, calling fragmentManager.popBackStackImmediate()
        // is safe
        if (getSupportFragmentManager() != null && !getSupportFragmentManager().isStateSaved()) {
            NavigationManager.getInstance().closeStreamAuthenticationFragment(persistentPlayer.getType() != PlayerConstants.Type.LANDSCAPE);
        }
    }

    @Override
    public void onReceivedLocalizations(Localizations localizations) {
        this.localizations = localizations;
        navigationManager.getTeamselectorFragment().onLocalizationManagerInitialized();
        navigationManager.getSettingsFragment().onLocalizationManagerInitialized();
        navigationManager.getTeamNewsFragment().onLocalizationManagerInitialized();
        navigationManager.getMediaSettingsFragment().onLocalizationManagerInitialized();
        navigationManager.getFabOutroFragment().onLocalizationManagerInitialized();
    }

    @Override
    public MainContract.Presenter getMainPresenter() {
        return mainPresenter;
    }

    @Override
    public void hideFab() {
        fab.setVisibility(View.GONE);
    }

    @Override
    public void showFab() {
        fab.setVisibility(View.VISIBLE);
    }

    @Override
    public void showFabWithAnimation(long startDelay, boolean enableCustomOnTouch) {
        FtueUtil.showFabWithAnimation(startDelay, enableCustomOnTouch, fab, fabLogo, fabAnimator);
    }

    @Override
    public void setSlideIndicatorVisibleWithAnimation(boolean show) {
        FtueUtil.setSlideIndicatorVisibleWithAnimation(show, slideIndicatorBackgroundRelativeLayout,
                slideIndicatorTextView, slideIndicatorImageView);
    }

    @Override
    public void clearFabAnimations() {
        FtueUtil.clearFabAnimations(fabLogo, fabAnimator);
    }

    @Override
    public void fadeInAndFadeOurFab(float percent) {
        if (mainPresenter != null) {
            mainPresenter.fadeInAndFadeOurFab(percent, fab);
        }
    }

    @Override
    public void disableFabTouch(boolean disableTouch) {
        if (fab != null) {
            fab.setTouchDisabled(disableTouch).subscribe();
        }
    }

    public void displayChooser(Intent chooserIntent) {
        startActivity(chooserIntent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == LOCATION_SERVICES_REQUEST_CODE) { //For location services.
            LocationUtils.onRequestPermissionsResultReceived(this, requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case LOCATION_SERVICES_REQUEST_CODE:
                switch (resultCode) {
                    case RESULT_CANCELED:
                        PreferenceUtils.INSTANCE.setBoolean(LocationUtils.DENY_LOCATION_SERVICES, true);
                        LocationUtils.onDeviceLocationUnavailable(this);
                        break;
                }
                break;
        }
    }

    boolean isFirstAppLaunch() {

        String installDate = PreferenceUtils.INSTANCE.getString(PREFERENCES_INSTALLATION_DATE_KEY, "");

        if (!installDate.isEmpty()) {
            return false;
        }

        DateFormatSymbols dfs = new DateFormatSymbols();
        String[] months = dfs.getShortMonths();

        int month = Calendar.getInstance().get(Calendar.MONTH);
        int year = Calendar.getInstance().get(Calendar.YEAR);

        String datePrefValue = String.format(Locale.US, "%s %d", months[month], year);
        PreferenceUtils.INSTANCE.setString(PREFERENCES_INSTALLATION_DATE_KEY, datePrefValue);

        return true;
    }

    public String getPreferenceInstallationDateKey() {
        return PREFERENCES_INSTALLATION_DATE_KEY;
    }

    @Override
    public void updateTeamLogos(HashMap<Integer, String> map) {
        if (map != null) teamManager.setLogoUrlMap(map);
    }

    public NotificationBanner getNotificationBanner() {
        return notificationBanner;
    }

    @Override
    public boolean getActivityVisibility() {
        return isActivityVisible;
    }

    @Override
    public BottomNotificationBanner getBottomNotificationBanner() {
        return bottomNotificationBanner;
    }

    public void showNotification(NotificationData notification, int duration) {
        if (notificationBanner != null && isActivityVisible) {
            notificationBanner.setVisibility(View.VISIBLE);
            notificationBanner.show(notification, duration);
        }
    }

    public void showNotification(NotificationData notification) {
        showNotification(notification, SYSTEM_SHOWN_DURATION);
    }

    public void removePostedNotification(int notificationId) {
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel(notificationId);
        }
    }

    public void showBottomNotification(BottomNotificationData bottomNotificationData, int duration) {
        if (mainPresenter != null){
            mainPresenter.showBottomNotification(bottomNotificationData, duration);
        }
    }

    public void showBottomNotification(BottomNotificationData bottomNotificationData) {
        showBottomNotification(bottomNotificationData, SYSTEM_SHOWN_DURATION);
    }

    public void hideBottomNotification() {
        if (mainPresenter != null){
            mainPresenter.hideBottomNotification();
        }
    }

    public void getIpGeolocation() {
        mainPresenter.geolocationGet(config.getGeolocationCheckUrl());
    }

    @Override
    public void onReceivedIpGeolocation(IpGeolocation ipGeolocation) {
        if (ipGeolocation == null) return;

        Location ipLocation = new Location("");
        ipLocation.setLongitude(Double.parseDouble(ipGeolocation.getLongitude()));
        ipLocation.setLatitude(Double.parseDouble(ipGeolocation.getLatitude()));

        LocationUtils.setGeoLocation(ipLocation);
        this.onReceivedLocation(ipLocation);
    }

    public void doFabFtueFlickAnim() {
        fab.showFtueFabAnim(PreferenceUtils.INSTANCE.getBoolean(PREFERENCES_FAB_RTL, false));
    }

    public void onReceivedLocation(Location location) {
        if (navigationManager.getCurrentFragment() instanceof TeamSelectorFragment && location != null) {
            TeamSelectorFragment teamSelectorFragment = (TeamSelectorFragment) navigationManager.getCurrentFragment();
            teamSelectorFragment.updateTeamList();
            teamSelectorFragment.setLoadingSpinnerVisibility(false);
        }
        LocationUtils.releaseLocationListener();
        mainPresenter.getTotalCast().postAsync(location);
    }

    @Override
    public void setKochava(KochavaContract.Presenter presenter) {
        kochava = presenter;
    }

    @Override
    public KochavaContract.Presenter getKochava() {
        return kochava;
    }

    @Override
    public void onAudioStreamVolumeChanged(int audioStreamType, float volume) {
        persistentPlayer.log("onAudioStreamVolumeChanged() volume: %s", volume);
        if (persistentPlayer.getPlayerEngine() != null) {
            persistentPlayer.mute(volume <= 0f);
            persistentPlayer.getPlayerEngine().setVolume(volume);
        }
    }

    private TeamContainerFragment getCurrentTeamContainerFragment() {
        return (TeamContainerFragment) navigationManager
                .getTeamsPagerFragment()
                .getPagerAdapter()
                .getCurrentFragment();
    }
}
