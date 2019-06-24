package com.nbcsports.regional.nbc_rsn.settings;

import android.animation.LayoutTransition;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.f2prateek.rx.preferences2.Preference;
import com.jakewharton.rxbinding2.view.RxView;
import com.jakewharton.rxbinding2.widget.RxCompoundButton;
import com.nbcsports.regional.nbc_rsn.BuildConfig;
import com.nbcsports.regional.nbc_rsn.ConfigListener;
import com.nbcsports.regional.nbc_rsn.MainActivity;
import com.nbcsports.regional.nbc_rsn.MainContract;
import com.nbcsports.regional.nbc_rsn.MainPresenter;
import com.nbcsports.regional.nbc_rsn.R;
import com.nbcsports.regional.nbc_rsn.analytics.TrackingHelper.PageInfo;
import com.nbcsports.regional.nbc_rsn.analytics.TrackingHelper.TrackingHelper;
import com.nbcsports.regional.nbc_rsn.authentication.Auth;
import com.nbcsports.regional.nbc_rsn.authentication.Injection;
import com.nbcsports.regional.nbc_rsn.authentication.StreamAuthenticationContract;
import com.nbcsports.regional.nbc_rsn.common.BaseFragment;
import com.nbcsports.regional.nbc_rsn.common.Config;
import com.nbcsports.regional.nbc_rsn.common.IpGeolocation;
import com.nbcsports.regional.nbc_rsn.common.SettingsSupport;
import com.nbcsports.regional.nbc_rsn.common.Team;
import com.nbcsports.regional.nbc_rsn.data_menu.datamenuftue.DataMenuFtueManager;
import com.nbcsports.regional.nbc_rsn.fabigation.fabtapftue.FabTapFtueManager;
import com.nbcsports.regional.nbc_rsn.lifecycle.ApplicationLifecycleManager;
import com.nbcsports.regional.nbc_rsn.localization.LocalizationManager;
import com.nbcsports.regional.nbc_rsn.localization.models.Localizations;
import com.nbcsports.regional.nbc_rsn.navigation.NavigationManager;
import com.nbcsports.regional.nbc_rsn.persistentplayer.PersistentPlayer;
import com.nbcsports.regional.nbc_rsn.persistentplayer.PersistentPlayerContract;
import com.nbcsports.regional.nbc_rsn.persistentplayer.view.progressbar.PersistentPlayerProgressBarManager;
import com.nbcsports.regional.nbc_rsn.settings.settings_views.SettingsActionableView;
import com.nbcsports.regional.nbc_rsn.settings.settings_views.SettingsHeaderView;
import com.nbcsports.regional.nbc_rsn.settings.settings_views.SettingsHeadingView;
import com.nbcsports.regional.nbc_rsn.settings.settings_views.SettingsToggleButton;
import com.nbcsports.regional.nbc_rsn.settings.settings_views.SettingsToggleTeamsView;
import com.nbcsports.regional.nbc_rsn.team_feed.auto_refresh.AutoRefreshManager;
import com.nbcsports.regional.nbc_rsn.urban_airship.NotificationsManagerKt;
import com.nbcsports.regional.nbc_rsn.utils.DisplayUtils;
import com.nbcsports.regional.nbc_rsn.utils.KeyboardUtils;
import com.nbcsports.regional.nbc_rsn.utils.NativeShareUtils;
import com.nbcsports.regional.nbc_rsn.utils.PreferenceUtils;
import com.urbanairship.UAirship;

import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import timber.log.Timber;

public class SettingsFragment extends BaseFragment implements SettingsContract.View, SettingsAdapter.TeamCountingListener, ConfigListener {

    private final String PREFERENCES_FAB_RTL = "_isFabRtl";
    private static final int MY_TEAMS_BOTTOM_MARGIN_DP = 4;
    private static final float MY_TEAMS_SIDE_MARGIN_PERCENT = 0.04f;
    private final long BUTTON_DISABLE_TIME = 1000;

    @BindView(R.id.root_view)
    NestedScrollView nestedScrollView;

    @BindView(R.id.status_bar)
    View statusBar;

    @BindView(R.id.toggle_rtl)
    ToggleButton toggleButton;

    @BindView(R.id.setting_recycler_view)
    RecyclerView settingRecyclerView;

    @BindView(R.id.header)
    SettingsHeaderView settingsHeader;

    @BindView(R.id.my_teams_settings_heading_view)
    SettingsHeadingView myTeamsSettingsHeadingView;

    @BindView(R.id.toggle_teams)
    SettingsToggleTeamsView toggleTeamsOption;

    @BindView(R.id.edit_reorder_teams)
    SettingsActionableView tvEditTeams;

    @BindView(R.id.notification_settings_heading_view)
    SettingsHeadingView notificationSettingsHeadingView;

    @BindView(R.id.allow_notifications)
    SettingsActionableView allowNotifications;

    @BindView(R.id.breaking_news)
    SettingsActionableView breakingNews;

    @BindView(R.id.team_news)
    SettingsActionableView teamNews;

    @BindView(R.id.data_settings_heading_view)
    SettingsHeadingView dataSettingsHeadingView;

    @BindView(R.id.media_settings)
    SettingsActionableView mediaSettings;

    @BindView(R.id.provider_settings_heading_view)
    SettingsHeadingView providerSettingsHeadingView;

    @BindView(R.id.support_faq)
    SettingsActionableView supportFaq;

    @BindView(R.id.support_feedback)
    SettingsActionableView supportFeedback;

    @BindView(R.id.support_privacy)
    SettingsActionableView supportPrivacy;

    @BindView(R.id.support_share)
    SettingsActionableView supportShare;

    @BindView(R.id.support_term_of_use)
    SettingsActionableView supportTerm;

    @BindView(R.id.support_update_app)
    SettingsActionableView supportUpdate;

    @BindView(R.id.support)
    SettingsHeadingView support;

    @BindView(R.id.settings_layout)
    LinearLayout settingsLayout;

    @BindView(R.id.logout)
    SettingsActionableView logoutView;

    @BindView(R.id.settings_version)
    TextView version;

    private SettingsContract.Presenter presenter;
    private SettingsAdapter adapter;
    private StreamAuthenticationContract.Presenter authPresenter;
    private MainContract.Presenter mainPresenter;
    private List<SettingsSupport> settingsSupportList;
    private int configAppVersion;
    private float distanceToMyTeamHeading;
    private PersistentPlayer persistentPlayer;
    private boolean isAccessibleDebugMode;
    private static int timesClickOnVersion;
    private static final int clickTimeRequireForDebug = 13;
    private AlertDialog debugLogin;
    private EditText password = null;

    public enum supportActions {
        url, updateApp, email, shareApp
    }

    @Override
    public int getLayout() {
        return R.layout.fragment_settings;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        //this.trackingPageName = "SettingsFragment";
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        authPresenter = Injection.provideStreamAuthentication((StreamAuthenticationContract.View) getActivity());
        mainPresenter = MainPresenter.Injection.providePresenter(getContext());
        mainPresenter.configAddListener(this);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        new SettingsPresenter(this);

        version.setText(String.format("v%s-%s", BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE));
        isAccessibleDebugMode = false;
        updateViews();

        Preference<Boolean> isFabRtl = PreferenceUtils.INSTANCE.getRxPreferences().getBoolean(PREFERENCES_FAB_RTL, false);
        compositeDisposable.addAll(
                // Update checkbox when preference changes
                isFabRtl.asObservable()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(RxCompoundButton.checked(toggleButton)),

                // update preference when checkbox state changes
                RxCompoundButton.checkedChanges(toggleButton)
                        .skipInitialValue()
                        .subscribe(isChecked -> {
                            isFabRtl.set(isChecked);
                            ((MainActivity) getActivity()).updateFabControls(isChecked);
                        }),

                RxView.clicks(tvEditTeams)
                        .debounce(500L, TimeUnit.MILLISECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(v -> {
                            //Shows the status bar with color
                            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
                            NavigationManager.getInstance().showTeamSelectorFragment();
                        }),

                RxView.clicks(mediaSettings)
                        .debounce(500L, TimeUnit.MILLISECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(v -> {
                            if (getActivity() != null) {
                                NavigationManager.getInstance().showMediaSettingFragment();
                            }
                        }));


        if(getActivity() != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            View debugView = getLayoutInflater().inflate(R.layout.settings_debug_log_in, null);
            password = debugView.findViewById(R.id.debug_password);
            final Button submitButton = debugView.findViewById(R.id.debug_submit_password);
            final Button cancelButton = debugView.findViewById(R.id.debug_cancel_password);
            builder.setView(debugView);
            debugLogin = builder.create();
            submitButton.setOnClickListener(view1 -> checkPassword(password, debugLogin));

            cancelButton.setOnClickListener(view12 -> debugLogin.dismiss());
            password.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    checkPassword(password, debugLogin);
                }
                return false;
            });
            debugLogin.setCanceledOnTouchOutside(false);
        }

        int[] supportList = new int[]{R.id.support_faq, R.id.support_update_app, R.id.support_term_of_use, R.id.support_share, R.id.support_feedback, R.id.support_privacy};
        for (int supportId : supportList) {
            compositeDisposable.add(identifyFunctionClicked(view.findViewById(supportId)));
        }
        persistentPlayer = com.nbcsports.regional.nbc_rsn.persistentplayer.Injection.providePlayer((PersistentPlayerContract.Main.View) getContext());
    }

    Disposable identifyFunctionClicked(SettingsActionableView view) {
        //This method does not use the result since it only preventing the rapid button click
        return RxView.clicks(view)
                .throttleFirst(BUTTON_DISABLE_TIME, TimeUnit.MILLISECONDS).subscribe(empty -> {
                    if (LocalizationManager.isInitialized() && settingsSupportList != null && !settingsSupportList.isEmpty()) {
                        for (SettingsSupport support : settingsSupportList) {
                            if (view.getTextView().getText().equals(LocalizationManager.Settings.getSettingsAutoDetect(support.getLocalizationsKey()))) {
                                performAction(support);
                            }
                        }
                    }
                });
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            isAccessibleDebugMode = false;
            // see if this prevent the blank view issue
            if (getView() == null) {
                return;
            }

            updateViews();
            //Fixes issue of user arriving in the middle of the settings fragment.
            settingRecyclerView.setFocusable(false);

            //User arrives at the top of the fragment.
            nestedScrollView.scrollTo(0, 0);
            //Track omniture page event
            TrackingHelper.Companion.trackPageEvent(getPageInfo());

            // Remove all application lifecycle listeners
            // Because if Settings fragment is showing and app go into background,
            // then brought back from foreground, nothing should be refreshed
            ApplicationLifecycleManager.removeAllListeners();

            // Remove all live assets auto refresh disposable observer
            // and team feed auto refresh disposable observer
            // Because if Settings fragment is showing, nothing should be refreshed
            AutoRefreshManager.removeAllObservers();

            // Remove all progress bar disposable observers
            // Because if Settings fragment is showing, progress bar
            // should not be updated
            PersistentPlayerProgressBarManager.removeAllObservers();

            // Remove all fab tap ftue disposable observers
            // Because if Settings fragment is showing, no fab tap ftue should be shown
            FabTapFtueManager.Companion.removeAllObservers();

            // Remove all data menu ftue disposable observers
            // Because if Settings fragment is showing, no data menu ftue should be shown
            DataMenuFtueManager.Companion.removeAllObservers();
        }
    }

    @OnClick(R.id.settings_version)
    public void clickOnVersion() {
        timesClickOnVersion++;
        Timber.d("Click time is %s", timesClickOnVersion);
        if (timesClickOnVersion == clickTimeRequireForDebug) {
            password.setText("");
            if (!debugLogin.isShowing()){
                debugLogin.show();
            }
            timesClickOnVersion = 0;
        }
    }

    private void checkPassword(EditText passwordField, AlertDialog dialog) {
        if (passwordField.getText().toString().equals(getString(R.string.debug_password))) {
            Timber.d("Now you have access");
            Toast.makeText(getActivity(), R.string.debug_password_success_prompt, Toast.LENGTH_LONG).show();
            NavigationManager.getInstance().showDebugFragment(authPresenter);
            dialog.dismiss();
        } else {
            Timber.d("Password error");
            Toast.makeText(getActivity(), R.string.debug_password_fail_prompt, Toast.LENGTH_LONG).show();
        }
    }

    private void updateViews() {
        timesClickOnVersion = 0;
        setPlaceHolderStatusBar();
        addStatusBarAnimation();
        updateSupportUpdateView();
        showUpdatedTeamList();
        updateNotificationViews();
        showRegionBackgroundImage();
        showSettingsLabel();
        showLogout();
    }

    private void showLogout() {
        providerSettingsHeadingView.setVisibility(authPresenter.isSignedIn() ? View.VISIBLE : View.GONE);
        logoutView.setVisibility(authPresenter.isSignedIn() ? View.VISIBLE : View.GONE);
    }

    private void setPlaceHolderStatusBar() {
        Timber.d("Status bar height is : %s", DisplayUtils.getStatusBarHeight());
        ViewGroup.LayoutParams statusBarLayout = statusBar.getLayoutParams();
        statusBarLayout.height = DisplayUtils.getStatusBarHeight();
        statusBar.setLayoutParams(statusBarLayout);
        statusBar.setAlpha(0);
    }

    private void addStatusBarAnimation() {
        nestedScrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            int[] location = new int[2];
            myTeamsSettingsHeadingView.getLocationOnScreen(location);
            int distanceFromTop = location[1];
            if (distanceToMyTeamHeading < distanceFromTop) {
                distanceToMyTeamHeading = distanceFromTop;
            }
            if (distanceFromTop >= 0) {
                statusBar.setAlpha((distanceToMyTeamHeading - distanceFromTop) / distanceToMyTeamHeading);
            }
        });
    }

    private void updateSupportUpdateView() {
        supportUpdate.getSubDescription().setText(String.format("(version %d)", configAppVersion));
        supportUpdate.setVisibility((BuildConfig.VERSION_CODE < configAppVersion) ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setPresenter(SettingsPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void initRecyclerView() {
        if (settingsLayout != null) {
            settingsLayout.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
        }

        if (settingRecyclerView != null) {
            settingRecyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()) {
                @Override
                public boolean canScrollVertically() {
                    return false;
                }
            });
        }

        adapter = new SettingsAdapter(this);
        settingRecyclerView.setAdapter(adapter);

        int bottomMarginPixels = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, MY_TEAMS_BOTTOM_MARGIN_DP, getResources().getDisplayMetrics());
        settingRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                int screenWidth = DisplayUtils.getScreenWidth(parent.getContext());
                int sideMargin = (int) (screenWidth * MY_TEAMS_SIDE_MARGIN_PERCENT);
                outRect.left = sideMargin;
                outRect.right = sideMargin;
                outRect.bottom = bottomMarginPixels;
            }
        });
    }

    @Override
    public void showUpdatedTeamList() {
        if (getActivity() == null || settingRecyclerView == null || adapter == null) return;

        MainActivity mainActivity = (MainActivity) getActivity();
        List<Team> usersTeamList = mainActivity.getTeamManager().getUsersTeams();
        if (usersTeamList != null) {
            adapter.setSelectedTeams(usersTeamList);
        }
    }

    @Override
    public void showRegionBackgroundImage() {
        if (getActivity() == null) return;

        MainActivity mainActivity = (MainActivity) getActivity();
        List<Team> masterList = mainActivity.getTeamManager().getMasterList();

        Team closestTeam = presenter.getTeamWithClosestLocation(masterList);
        if (closestTeam != null) {
            settingsHeader.updateBackgroundImage(closestTeam.getRegionBackgroundUrl());
        }
    }

    @Override
    public void showSettingsLabel() {
        if (LocalizationManager.isInitialized()) {
            settingsHeader.updateSettingsLabel(LocalizationManager.Settings.Settings);
        }
    }

    @Override
    public void setToggleMoreTeamsOptionVisibility(boolean visible) {
        toggleTeamsOption.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setBreakingNewsOptionVisibility(boolean visible) {
        if (visible) {
            breakingNews.setVisibility(View.VISIBLE);
            breakingNews.getToggleButton().setToggleDefault(NotificationsManagerKt.INSTANCE.isOptedInBreakingNews());
        } else {
            breakingNews.setVisibility(View.GONE);
        }
    }

    @Override
    public void setTeamNewsOptionVisibility(boolean visible) {
        teamNews.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    private void showConfirmationDialog() {
        if (this.getContext() == null) {
            return;
        }

        Resources res = getResources();
        String alertTitle = res.getString(R.string.notifications_alert_title);
        String alertMessage = res.getString(R.string.notifications_alert_message);
        String alertTurnOffMessage = res.getString(R.string.notifications_alert_turn_off);
        String alertCancelMessage = res.getString(R.string.notifications_alert_cancel);

        AlertDialog.Builder builder = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ?
                new AlertDialog.Builder(getContext(), R.style.Theme_AppCompat_Light_Dialog)
                : new AlertDialog.Builder(getContext());

        builder.setTitle(alertTitle)
                .setMessage(alertMessage)
                .setPositiveButton(alertTurnOffMessage, (dialog, which) -> {
                    allowNotifications.getToggleButton().animateToggle(false);
                    presenter.setNotifications(false);
                    setBreakingNewsOptionVisibility(false);
                    setTeamNewsOptionVisibility(false);
                })
                .setNegativeButton(alertCancelMessage, (dialog, which) -> {
                    allowNotifications.getToggleButton().toggleOn();
                })
                .show();
    }

    @Override
    public void updateNotificationViews() {
        boolean notificationsEnabled = UAirship.shared().getPushManager().getUserNotificationsEnabled();
        allowNotifications.getToggleButton().setToggleDefault(notificationsEnabled);
        setBreakingNewsOptionVisibility(notificationsEnabled);
        setTeamNewsOptionVisibility(notificationsEnabled);
    }

    @Override
    public void onNumberOfTeamsCounted(boolean moreTeamsThanDefaultNumber) {
        setToggleMoreTeamsOptionVisibility(moreTeamsThanDefaultNumber);
    }

    @Override
    public void onLocalizationManagerInitialized() {
        presenter.setUpTextViewWithLocalizedText(myTeamsSettingsHeadingView, toggleTeamsOption, tvEditTeams,
                notificationSettingsHeadingView, allowNotifications, breakingNews, teamNews,
                dataSettingsHeadingView, mediaSettings, support, supportFaq, supportFeedback, supportPrivacy,
                supportUpdate, supportShare, supportTerm, providerSettingsHeadingView, logoutView);
    }

    @OnClick(R.id.allow_notifications)
    public void toggleNotifications() {
        SettingsToggleButton toggleButton = allowNotifications.getToggleButton();
        boolean toggledOn = toggleButton.isToggledOn();

        if (toggledOn) {
            showConfirmationDialog();
        } else {
            toggleButton.animateToggle(true);
            presenter.setNotifications(true);
            setBreakingNewsOptionVisibility(true);
            setTeamNewsOptionVisibility(true);
        }
    }

    @OnClick(R.id.breaking_news)
    public void toggleBreakingNews() {
        SettingsToggleButton toggleButton = breakingNews.getToggleButton();
        boolean toggledOn = toggleButton.isToggledOn();
        toggleButton.animateToggle(!toggledOn);
        presenter.setBreakingNewsNotifications(!toggledOn);
    }

    @OnClick(R.id.team_news)
    public void goToTeamNews() {
        if (getActivity() != null) {
            NavigationManager.getInstance().showTeamNewsFragment();
        }
    }

    @OnClick(R.id.toggle_teams)
    void toggleTeamsClicked() {
        if (!settingRecyclerView.getItemAnimator().isRunning()) { //Disable double clicks
            adapter.toggleVisibleTeamsCount();
            boolean isViewingMore = adapter.isViewingMoreTeams();
            toggleTeamsOption.updateText(isViewingMore);
            toggleTeamsOption.rotateIcon(isViewingMore);
        }
    }

    @OnClick(R.id.logout)
    void logoutClicked() {
        // FIXME: better way to access config
        authPresenter.doLogout(authPresenter.getAuthorizedRequestorId(), new DisposableObserver<Auth>() {
            @Override
            public void onNext(Auth auth) {
                NotificationsManagerKt.INSTANCE.showLogOut();
                if (persistentPlayer != null){
                    persistentPlayer.release();
                    if (persistentPlayer.getChromecastHelper() != null){
                        persistentPlayer.getChromecastHelper().onLogout();
                    }
                }

                providerSettingsHeadingView.setVisibility(View.GONE);
                logoutView.setVisibility(View.GONE);
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
    }

    public void performAction(SettingsSupport settingsSupport) {
        supportActions action = supportActions.valueOf(settingsSupport.getAction());
        switch (action) {
            case url:
                openWeb(settingsSupport.getUrl());
                break;
            case updateApp:
                //The update App is only visible when current version is less than config version, therefore no need to check it again
                openPlayStore();
                break;
            case email:
                openEmail(settingsSupport.getUrl());
                break;
            case shareApp:
                shareApp();
                break;
        }
    }

    public void openPlayStore() {
        final String appPackageName = getActivity().getPackageName();
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            //For device that does not have play store installed
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
        }
    }

    public void openWeb(String url) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        getView().getContext().startActivity(i);
    }

    public void openEmail(String emailAddress) {
        String emailTo = "mailto:" + Uri.encode(emailAddress);
        String emailSubject = "?subject=";
        String emailBody = "&body=";

        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        Uri uri = Uri.parse(emailTo + emailSubject + emailBody);
        emailIntent.setData(uri);
        getView().getContext().startActivity(emailIntent);
    }

    public void shareApp() {
        String baseShareUrl = NativeShareUtils.getBaseShareUrl(this);
        String shareUrl = baseShareUrl.substring(0, baseShareUrl.indexOf("?"));
        //The share title is blank and waiting for C&T's reply, TODO:update localization once receive feed back
        NativeShareUtils.ShareInfo shareInfo = new NativeShareUtils.ShareInfo((MainActivity) getActivity(), null, shareUrl, shareUrl, null, "", null);
        NativeShareUtils.createAndDisplayChooser(shareInfo);
    }

    @Override
    public void onResume() {
        super.onResume();

        final Runnable hideKeyboard = () -> KeyboardUtils.hideKeyboard(getActivity());
        getView().post(hideKeyboard);


    }

    @Override
    public void onReceived(Config config) {
        //System.out.println("This is the enter point: SettingsFragment onReceived");
        settingsSupportList = config.getSettingsSupport();
        try {
            configAppVersion = Integer.parseInt(config.getCurrentStoreVersion());
        } catch (NumberFormatException e) {
            e.printStackTrace();
            configAppVersion = 0;
        }
        Timber.d("SettingSupport lIst %s", settingsSupportList);
    }

    @Override
    public void onLoadConfigError(Throwable e) {

    }

    @Override
    public void onReceivedLocalizations(Localizations localizations) {

    }

    @Override
    public void onReceivedIpGeolocation(IpGeolocation ipGeolocation) {

    }

    @Override
    public PageInfo getPageInfo() {
        return new PageInfo(false, "", "", "Settings", "", "", "", "", "", "", "");
    }
}
