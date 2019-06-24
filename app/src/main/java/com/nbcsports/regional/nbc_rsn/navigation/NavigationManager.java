package com.nbcsports.regional.nbc_rsn.navigation;

import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.nbcsports.regional.nbc_rsn.MainActivity;
import com.nbcsports.regional.nbc_rsn.R;
import com.nbcsports.regional.nbc_rsn.RsnApplication;
import com.nbcsports.regional.nbc_rsn.SplashScreenFragment;
import com.nbcsports.regional.nbc_rsn.authentication.StreamAuthenticationContract;
import com.nbcsports.regional.nbc_rsn.authentication.StreamAuthenticationFragment;
import com.nbcsports.regional.nbc_rsn.common.Config;
import com.nbcsports.regional.nbc_rsn.common.EdgeSwipeViewPager;
import com.nbcsports.regional.nbc_rsn.common.FeedComponent;
import com.nbcsports.regional.nbc_rsn.common.MediaSource;
import com.nbcsports.regional.nbc_rsn.common.Team;
import com.nbcsports.regional.nbc_rsn.common.TeamManager;
import com.nbcsports.regional.nbc_rsn.data_menu.roster.RosterFragment;
import com.nbcsports.regional.nbc_rsn.data_menu.schedule.DataMenuScheduleFragment;
import com.nbcsports.regional.nbc_rsn.data_menu.score.DataMenuScoreFragment;
import com.nbcsports.regional.nbc_rsn.data_menu.standings.StandingsFragment;
import com.nbcsports.regional.nbc_rsn.debug_options.DebugFragment;
import com.nbcsports.regional.nbc_rsn.deeplink.DeeplinkManager;
import com.nbcsports.regional.nbc_rsn.editorial_detail.EditorialDetailTemplateFragment;
import com.nbcsports.regional.nbc_rsn.fabigation.FabMenuFragment;
import com.nbcsports.regional.nbc_rsn.faboutro.FabOutroFragment;
import com.nbcsports.regional.nbc_rsn.moreteams.MoreTeamsFragment;
import com.nbcsports.regional.nbc_rsn.settings.SettingsFragment;
import com.nbcsports.regional.nbc_rsn.settings.media_settings.MediaSettingsFragment;
import com.nbcsports.regional.nbc_rsn.settings.team_news.TeamNewsFragment;
import com.nbcsports.regional.nbc_rsn.stepped_story.SteppedStoryFragment;
import com.nbcsports.regional.nbc_rsn.team_feed.template.TeamFeedFragment;
import com.nbcsports.regional.nbc_rsn.team_view.TeamContainerFragment;
import com.nbcsports.regional.nbc_rsn.team_view.TeamsPagerFragment;
import com.nbcsports.regional.nbc_rsn.teamselector.TeamSelectorFragment;
import com.nbcsports.regional.nbc_rsn.utils.ActivityUtils;
import com.nbcsports.regional.nbc_rsn.utils.AnimationUtil;
import com.nbcsports.regional.nbc_rsn.utils.FtueUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import lombok.Getter;
import timber.log.Timber;

/**
 * Created by jason on 2018-08-07.
 * <p>
 * To Use this class, if your new fragment does not have any special operation,
 * Simply add it into constructor and addThenHideFragments, then calling showFragment will complete the transaction.
 * If your fragment contains additional actions (Show Fab, Screen Orientation etc),
 * it is recommended to create separate method here
 */

public class NavigationManager {

    public static final String ARGUMENT_TEAM = "argument_team";
    public static final String ARGUMENT_TEAM_FEED_COMPONENTS_LIST = "argument_team_feed_components_list";
    public static final String ARGUMENT_SELECTED_FEED_COMPONENT_POSITION = "argument_selected_feed_component_position";
    public static final String CONTENT_TYPE_STEPPED_STORY = "stepped_story";

    @Getter
    private Fragment currentFragment;
    private Fragment previousFragment;
    private int previousStatusBarColor;
    @Getter
    public FabMenuFragment menuFragment;
    @Getter
    private TeamsPagerFragment teamsPagerFragment;
    @Getter
    private MoreTeamsFragment moreteamsFragment;
    @Getter
    private TeamSelectorFragment teamselectorFragment;
    @Getter
    private SettingsFragment settingsFragment;
    @Getter
    private TeamNewsFragment teamNewsFragment;
    @Getter
    private MediaSettingsFragment mediaSettingsFragment;
    @Getter
    private FabOutroFragment fabOutroFragment;
    @Getter
    private SplashScreenFragment splashScreenFragment;
    @Getter
    private DebugFragment debugFragment;
    @Getter
    public RosterFragment rosterFragment;
    @Getter
    public DataMenuScheduleFragment dataMenuScheduleFragment;
    @Getter
    public StandingsFragment standingsFragment;
    @Getter
    public DataMenuScoreFragment scoresFragment;

    private StreamAuthenticationFragment streamAuthenticationFragment;

    private static NavigationManager navigationManager = null;
    private WeakReference<MainActivity> mainActivityReference;
    private FragmentManager manager;
    @Getter
    private Stack<Fragment> settingsFragmentStack = new Stack<>();

    public NavigationManager() {
        splashScreenFragment = new SplashScreenFragment();
        teamsPagerFragment = new TeamsPagerFragment();
        menuFragment = new FabMenuFragment();
        moreteamsFragment = new MoreTeamsFragment();
        teamselectorFragment = new TeamSelectorFragment();
        settingsFragment = new SettingsFragment();
        teamNewsFragment = new TeamNewsFragment();
        mediaSettingsFragment = new MediaSettingsFragment();
        fabOutroFragment = new FabOutroFragment();
        debugFragment = new DebugFragment();
    }

    public void setActivityWeakReference(MainActivity activity) {
        if (mainActivityReference == null || mainActivityReference.get() == null) {
            mainActivityReference = new WeakReference<>(activity);
        }
    }

    public static void release() {
        getInstance().mainActivityReference.clear();
        getInstance().mainActivityReference = null;
        navigationManager = null;
    }

    public void addThenHideFragments(MainActivity activity) {
        setActivityWeakReference(activity);
        MainActivity main = mainActivityReference.get();
        manager = main.getSupportFragmentManager();
        ActivityUtils.addThenHideFragmentToActivity(manager, splashScreenFragment, R.id.fragment_container);
        ActivityUtils.addThenHideFragmentToActivity(manager, menuFragment, R.id.fragment_container);
        ActivityUtils.addThenHideFragmentToActivity(manager, teamsPagerFragment, R.id.fragment_container);
        ActivityUtils.addThenHideFragmentToActivity(manager, moreteamsFragment, R.id.fragment_container);
        ActivityUtils.addThenHideFragmentToActivity(manager, teamselectorFragment, R.id.fragment_container);
        ActivityUtils.addThenHideFragmentToActivity(manager, settingsFragment, R.id.fragment_container);
        ActivityUtils.addThenHideFragmentToActivity(manager, teamNewsFragment, R.id.fragment_container);
        ActivityUtils.addThenHideFragmentToActivity(manager, mediaSettingsFragment, R.id.fragment_container);
        ActivityUtils.addThenHideFragmentToActivity(manager, fabOutroFragment, R.id.fragment_container);
        ActivityUtils.addThenHideFragmentToActivity(manager, debugFragment, R.id.fragment_container);
    }

    public void showSplashScreen() {
        showFragment(splashScreenFragment);
    }

    //Please keep these no animation files here since they are related to fragment transactions
    public void showSettingsFramgent() {
        showSettingsFragment(R.anim.no_anim, R.anim.no_anim);
    }

    public void showSettingsFragment(int enterAnimation, int exitAnimation) {
        showFragment(settingsFragment, enterAnimation, exitAnimation);
    }

    public void showTeamPagerFragment() {
        showTeamPagerFragment(R.anim.no_anim, R.anim.no_anim);
    }

    public void showTeamPagerFragment(int enterAnimation, int exitAnimation) {
        MainActivity main = mainActivityReference.get();
        TeamManager teamManager = main.getTeamManager();

        Team team = teamManager.getSelectedTeam();

        if (team != null) {
            Timber.e("onTeamSelected(FAB MENU) selectedTeam: %s", team.getDisplayName());

            // To avoid the app crash (in EdgeSwipeViewPager.super.onTouchEvent)
            // on team view edge-swiping and simultaneous fab menu holding/dragging,
            // disable team view edge-swiping before doing setTeams on TeamsPagerAdapter.
            ViewPager viewPager = teamsPagerFragment.getViewPager();
            if (viewPager instanceof EdgeSwipeViewPager) {
                Timber.d("MainActivity.exitMenu calling setPagingEnabled(false)");
                ((EdgeSwipeViewPager) viewPager).setPagingEnabled(false);
            }

            teamsPagerFragment.getPagerAdapter().setTeams(main.getTeamManager().getUsersTeams());
            teamsPagerFragment.onTeamSelected(team);
            if (teamsPagerFragment.getView() != null) {
                teamsPagerFragment.getView().setBackgroundColor(Color.parseColor(team.getPrimaryColor()));
            }
        }
        main.showFab();
        main.switchFabLogo(team);
        main.getPersistentPlayer().showMiniIfAvailable(true);
        showFragment(teamsPagerFragment, enterAnimation, exitAnimation);
    }

    public void showMoreTeamFragment() {
        showMoreTeamFragment(R.anim.no_anim, R.anim.no_anim);
    }

    public void showMoreTeamFragment(int enterAnimation, int exitAnimation) {
        ActivityUtils.setStatusBarColor(mainActivityReference.get(), Color.BLACK);
        showFragment(moreteamsFragment, enterAnimation, exitAnimation);
    }

    public void showDebugFragment(StreamAuthenticationContract.Presenter authPresenter) {
        MainActivity main = mainActivityReference.get();
        debugFragment.updateAuthPresenter(authPresenter);
        showAndAddFragmentToBackStack(debugFragment, main, Color.BLACK, R.string.debug_tag);
    }

    /**
     * This function check whether if the app requires a restart after exiting debug
     * <p>
     * Return true if no changes in debugging, and performing normal pop fragment function
     * <p>
     * Return false otherwise
     *
     * @return
     */

    public boolean closeDebugFragment() {
        MainActivity main = mainActivityReference.get();
        if (currentFragment instanceof DebugFragment && ((DebugFragment) currentFragment).getPresenter() != null
                && ((DebugFragment) currentFragment).getPresenter().isRestartingAfterExit()) {
            ((DebugFragment) currentFragment).getPresenter().openRestartDialog(main);
            return true;
        }
        return false;
    }

    public void showTeamSelectorFragment() {
        mainActivityReference.get().hideFab();
        if (!(currentFragment instanceof SplashScreenFragment)) {
            settingsFragmentStack.push(currentFragment);
        }
        ActivityUtils.setStatusBarColor(mainActivityReference.get(), Color.BLACK);
        showFragment(teamselectorFragment);
    }

    public void closeTeamSelectorFragment() {
        if (settingsFragmentStack.empty()) {
            // This piece of code fixes the following scenario
            // Let's say dev has an extra team that doesn't exists in prod, then follow the flow
            // Select only extra team on dev -> switch to prod on debug menu -> team selector view
            // is shown (because there is no extra team on prod) -> select any teams -> black screen
            // is shown (without calling showTeamPagerFragment())
            // FYI, calling goToSelectedTeamView() won't work with this scenario,
            // because ActivityUtils.showFragment(...) won't hide team selector fragment
            showTeamPagerFragment();
            return;
        }
        mainActivityReference.get().showFab();
        Fragment previousFragment = settingsFragmentStack.pop();

        showFragment(previousFragment);
    }

    public void showStreamAuthenticationFragment(MediaSource mediaSource) {
        MainActivity main = mainActivityReference.get();
        FragmentManager fm = main.getSupportFragmentManager();
        Bundle bundle = new Bundle();
        bundle.putParcelable(StreamAuthenticationFragment.MEDIA_SOURCE, mediaSource);
        bundle.putBoolean(
                StreamAuthenticationFragment.IS_247,
                (main != null && main.getPersistentPlayer() != null && main.getPersistentPlayer().is247())
        );
        streamAuthenticationFragment = new StreamAuthenticationFragment();
        streamAuthenticationFragment.setArguments(bundle);
        ActivityUtils.addFragmentToActivity(fm, streamAuthenticationFragment, R.id.fragment_container);
        showAndAddFragmentToBackStack(streamAuthenticationFragment, main, Color.BLACK, R.string.stream_auth_tag);
    }

    public void closeStreamAuthenticationFragment(boolean isReturningToLandscape) {
        if (mainActivityReference == null) return;
        MainActivity main = mainActivityReference.get();

        if (main == null) return;

        if (!isReturningToLandscape) {
            main.showFab();
        }
        closeAndRemoveFragmentFromBackStack();
        ActivityUtils.removeFragmentToActivity(main.getSupportFragmentManager(), streamAuthenticationFragment);
    }

    public void showFabOutroFragment() {
        MainActivity main = mainActivityReference.get();
        main.switchFabLogo(null);
        main.hideFab();
        showFragment(fabOutroFragment);
    }

    public void showFabOutroFragment(int enterAnimation, int exitAnimation) {
        fabOutroFragment.onFabOutroShownAlready();
        showFragment(fabOutroFragment, enterAnimation, exitAnimation);
    }

    public void goToSelectedTeamView() {
        MainActivity main = mainActivityReference.get();
        if (main.getTeamManager().getSelectedTeam() == null
                || FtueUtil.isAppFirstLaunch()) return;

        Timber.d("Current selected team %s", main.getTeamManager().getSelectedTeam());

        main.showFab();
        Team team = main.getTeamManager().getSelectedTeam();

        main.switchFabLogo(team);

        ActivityUtils.showFragment(main.getSupportFragmentManager(), teamsPagerFragment);
        currentFragment = teamsPagerFragment;
        teamsPagerFragment.onTeamSelected(team);
    }

    public void showTeamNewsFragment() {
        MainActivity main = mainActivityReference.get();
        settingsFragmentStack.push(currentFragment);
        if (currentFragment instanceof SettingsFragment) {
            teamNewsFragment.setArrivingFromSettingsFragment(true);
        } else {
            teamNewsFragment.setArrivingFromSettingsFragment(false);
        }
        showAndAddFragmentToBackStack(teamNewsFragment, main, Color.BLACK, R.string.team_news_tag);
    }

    private void showAndAddFragmentToBackStack(Fragment fragmentToShow, MainActivity main, int statusBarColor, int tagId) {
        previousFragment = currentFragment;
        currentFragment = fragmentToShow;
        previousStatusBarColor = ActivityUtils.getStatusBarColor(main);
        ActivityUtils.showAndAddFragmentToBackStack(main.getString(tagId), main.getSupportFragmentManager(), fragmentToShow);
        ActivityUtils.setStatusBarColor(main, statusBarColor);
        main.hideFab();
    }

    public void showMediaSettingFragment() {
        MainActivity main = mainActivityReference.get();
        if (currentFragment instanceof SettingsFragment) {
            mediaSettingsFragment.setArrivingFromSettingsFragment(true);
        } else {
            mediaSettingsFragment.setArrivingFromSettingsFragment(false);
        }
        showAndAddFragmentToBackStack(mediaSettingsFragment, main, Color.BLACK, R.string.media_setting_tag);
    }

    /**
     * This function pops the last fragment from the back stack
     * and evaluate when pressing the back button should the app close,
     * <p>
     * Return true if is exiting the APP after pressing back button
     * <p>
     * Return false otherwise
     *
     * @return
     */
    public boolean closeAndRemoveFragmentFromBackStack() {
        MainActivity main = mainActivityReference.get();
        FragmentManager fm = main.getSupportFragmentManager();
        if (fm.getBackStackEntryCount() > 0) {
            if (fm.getBackStackEntryCount() == 1) {
                main.showFab();
            }
            ActivityUtils.popToLastStack(fm);
            ActivityUtils.setStatusBarColor(main, previousStatusBarColor);
            currentFragment = previousFragment;
            return false;
        }
        return true;
    }

    public void popAndRemoveFragmentFromBackStack(Fragment fragment) {
        MainActivity main = mainActivityReference.get();
        FragmentManager fm = main.getSupportFragmentManager();
        ActivityUtils.removeFragmentAndPopBackStack(fm, fragment, R.anim.no_anim, R.anim.no_anim);
        if (fragment instanceof RosterFragment) {
            rosterFragment = null;
        } else if (fragment instanceof DataMenuScheduleFragment) {
            dataMenuScheduleFragment = null;
        } else if (fragment instanceof StandingsFragment) {
            standingsFragment = null;
        } else if (fragment instanceof DataMenuScoreFragment) {
            scoresFragment = null;
        }
    }

    public static NavigationManager getInstance() {
        if (navigationManager == null) {
            navigationManager = new NavigationManager();
        }
        return navigationManager;
    }

    public void loadFirstScreen() {
        DeeplinkManager manager = DeeplinkManager.getInstance();
        if (manager.isState(DeeplinkManager.State.PENDING)) {
            manager.setState(DeeplinkManager.State.IN_PROGRESS);

            if (!(navigationManager.getCurrentFragment() instanceof TeamsPagerFragment)) {
                // show team feed fragment if not showing already
                if (FtueUtil.isAppFirstLaunch()) {
                    showTeamSelectorFragment();
                } else {
                    navigationManager.showTeamPagerFragment();
                }
            }
        }
        goToSelectedTeamView();
    }

    public void onSplashScreenComplete() {
        splashScreenFragment = null;
        loadFirstScreen();
    }

    public boolean isSplashScreenActive() {
        return splashScreenFragment != null;
    }

    public void showMenuFragment() {
        showMenuFragment(R.anim.no_anim, R.anim.no_anim);
    }

    public void showMenuFragment(int enterAnimation, int exitAnimation) {
        showFragment(menuFragment, enterAnimation, exitAnimation);
    }

    private void showFragment(Fragment fragmentToShow) {
        showFragment(fragmentToShow, R.anim.no_anim, R.anim.no_anim);
    }

    private void showFragment(Fragment fragmentToShow, int enterAnimation, int exitAnimation) {
        MainActivity main = mainActivityReference.get();
        if (currentFragment == null) {
            currentFragment = fragmentToShow;
            ActivityUtils.showFragment(manager, fragmentToShow);
        } else {
            if (currentFragment instanceof SettingsFragment) {
                main.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            }

            if (fragmentToShow instanceof SettingsFragment) {
                main.getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            }

            currentFragment = ActivityUtils.hideThenShowFragment(manager,
                    currentFragment, fragmentToShow,
                    enterAnimation, exitAnimation
            );
        }
    }

    public void openCommonCardDetailsScreen(ViewGroup backgroundViewToFadeTo,
                                            FragmentManager fragmentManager,
                                            Team team, List<FeedComponent> componentList,
                                            int componentIndex,
                                            @Nullable TeamFeedFragment teamFeedFragment) {

        if (mainActivityReference == null) return;
        // passing TeamFeedFragment == null with disable closing all the editorial fragments
        //  when top article's componentId is equal to the new article's componentId

        // May be there is a better way to access config
        Config config = mainActivityReference.get().getMainPresenter().getLastKnownConfig();
        if (config == null || config.getEditorialDetailsUrl() == null) return;

        FeedComponent fc = componentList.get(componentIndex);
        Fragment fragment;

        if (fc.getContentType().equalsIgnoreCase(CONTENT_TYPE_STEPPED_STORY)) {
            fragment = new SteppedStoryFragment();
        } else {
            fragment = new EditorialDetailTemplateFragment();
        }

        // Show editorial detail fragment
        Bundle arguments = new Bundle();
        arguments.putParcelable(ARGUMENT_TEAM, team);
        arguments.putParcelableArrayList(ARGUMENT_TEAM_FEED_COMPONENTS_LIST, (ArrayList<FeedComponent>) componentList);
        arguments.putInt(ARGUMENT_SELECTED_FEED_COMPONENT_POSITION, componentIndex);
        fragment.setArguments(arguments);

        List<Fragment> fragments = fragmentManager.getFragments();

        if (topFragmentInStackHasSameComponentId(fragments, fc.getComponentId())) {
            return; // do nothing if the article is already at the top
        } else if (teamFeedFragment != null) {
            // user is opening article via deeplink
            //  top article is not the same as incoming article
            //  remove all articles before opening new article
            teamFeedFragment.closeAllPages(false);
        }

        if (fragment instanceof SteppedStoryFragment) {
            ActivityUtils.setStatusBarColor(mainActivityReference.get(), Color.BLACK);
        } else {
            ActivityUtils.setStatusBarColor(mainActivityReference.get(), Color.parseColor(team.getPrimaryColor()));
        }

        if (FtueUtil.getFabTapToClose() == 0) {
            FtueUtil.recordArticleOpened();
        }

        openStackableScreen(backgroundViewToFadeTo, fragmentManager, fragment);
    }

    private void openStackableScreen(ViewGroup backgroundViewToFadeTo,
                                     FragmentManager fragmentManager, Fragment fragment) {
        TeamsPagerFragment currentTeamsPagerFragment = null;
        if (getCurrentFragment() instanceof TeamsPagerFragment) {
            currentTeamsPagerFragment = (TeamsPagerFragment) getCurrentFragment();
        }
        if (currentTeamsPagerFragment != null) {
            TeamContainerFragment teamContainerFragment = (TeamContainerFragment) currentTeamsPagerFragment.getPagerAdapter().getCurrentFragment();
            teamContainerFragment.hideMvpdHeader();
        }

        int animationDuration = RsnApplication.getInstance().getResources().getInteger(R.integer.fragment_animation_duration2);
        AnimationUtil.INSTANCE.fadeOut(animationDuration, backgroundViewToFadeTo);

        Timber.e("Launch new cID");
        ActivityUtils.showAndAddFragmentWithCustomAnimation(
                fragmentManager,
                fragment,
                R.id.editorial_detail,
                R.anim.zoom_in_slide_up_enter,
                R.anim.fade_out
        );
    }

    private boolean topFragmentInStackHasSameComponentId(List<Fragment> fragments, String newComponentId) {
        for (int i = fragments.size() - 1; i >= 0; i--) {
            if (fragments.get(i) instanceof EditorialDetailTemplateFragment) {
                EditorialDetailTemplateFragment ed = (EditorialDetailTemplateFragment) fragments.get(i);
                if (ed.getComponentId().equalsIgnoreCase(newComponentId)) {
                    return true;
                }
                // Make sure just check the top EditorialDetailTemplateFragment's component id
                break;
            } else if (fragments.get(i) instanceof SteppedStoryFragment) {
                SteppedStoryFragment ed = (SteppedStoryFragment) fragments.get(i);
                if (ed.getComponentId().equalsIgnoreCase(newComponentId)) {
                    return true;
                }
                // Make sure just check the top SteppedStoryFragment's component id
                break;
            }
        }
        return false;
    }

    /**
     * This function closes the last editorial fragment,
     * <p>
     * Return true if there is a editorial page to close
     * <p>
     * Return false otherwise
     *
     * @return
     */
    public boolean closeAndPopFragmentForStackableView() {
        if (currentFragment instanceof TeamsPagerFragment &&
                teamsPagerFragment != null &&
                teamsPagerFragment.getPagerAdapter() != null &&
                teamsPagerFragment.getPagerAdapter().getCurrentFragment() != null) {
            TeamContainerFragment currentTeamViewTemplate = (TeamContainerFragment) teamsPagerFragment.getPagerAdapter().getCurrentFragment();
            return currentTeamViewTemplate.closeLastPage();
        }
        return false;
    }
}
