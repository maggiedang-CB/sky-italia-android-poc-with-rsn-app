package com.nbcsports.regional.nbc_rsn.team_feed.template;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.clearbridge.pull_to_refresh.RefreshLayout;
import com.nbcsports.regional.nbc_rsn.MainActivity;
import com.nbcsports.regional.nbc_rsn.R;
import com.nbcsports.regional.nbc_rsn.analytics.TrackingHelper.PageInfo;
import com.nbcsports.regional.nbc_rsn.authentication.MvpdLogoListener;
import com.nbcsports.regional.nbc_rsn.common.BaseFragment;
import com.nbcsports.regional.nbc_rsn.common.Config;
import com.nbcsports.regional.nbc_rsn.common.EdgeSwipeViewPager;
import com.nbcsports.regional.nbc_rsn.common.FeedComponent;
import com.nbcsports.regional.nbc_rsn.common.IntentHelper;
import com.nbcsports.regional.nbc_rsn.common.RecyclerVisibilityScrollListener;
import com.nbcsports.regional.nbc_rsn.common.Team;
import com.nbcsports.regional.nbc_rsn.common.TeamManager;
import com.nbcsports.regional.nbc_rsn.data_bar.DataBarScrollListener;
import com.nbcsports.regional.nbc_rsn.deeplink.Deeplink;
import com.nbcsports.regional.nbc_rsn.deeplink.DeeplinkManager;
import com.nbcsports.regional.nbc_rsn.editorial_detail.EditorialDetailInterface;
import com.nbcsports.regional.nbc_rsn.lifecycle.ApplicationLifecycleListener;
import com.nbcsports.regional.nbc_rsn.lifecycle.ApplicationLifecycleManager;
import com.nbcsports.regional.nbc_rsn.persistentplayer.Injection;
import com.nbcsports.regional.nbc_rsn.persistentplayer.PersistentPlayer;
import com.nbcsports.regional.nbc_rsn.persistentplayer.PersistentPlayerContract;
import com.nbcsports.regional.nbc_rsn.persistentplayer.PlayerConstants;
import com.nbcsports.regional.nbc_rsn.team_feed.FeedNavManager;
import com.nbcsports.regional.nbc_rsn.team_feed.TeamFeedContract;
import com.nbcsports.regional.nbc_rsn.team_feed.TeamFeedPresenter;
import com.nbcsports.regional.nbc_rsn.team_feed.auto_refresh.AutoRefreshManager;
import com.nbcsports.regional.nbc_rsn.team_feed.components.TeamViewComponentsAdapter;
import com.nbcsports.regional.nbc_rsn.team_view.TeamContainerFragment;
import com.nbcsports.regional.nbc_rsn.team_view.TeamsPagerAdapter;
import com.nbcsports.regional.nbc_rsn.team_view.TeamsPagerFragment;
import com.nbcsports.regional.nbc_rsn.urban_airship.NotificationsManagerKt;
import com.nbcsports.regional.nbc_rsn.utils.ActivityUtils;
import com.nbcsports.regional.nbc_rsn.utils.DisplayUtils;
import com.nbcsports.regional.nbc_rsn.utils.FtueUtil;

import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import lombok.Getter;
import timber.log.Timber;

import static com.nbcsports.regional.nbc_rsn.common.Constants.CONFIG_KEY;
import static com.nbcsports.regional.nbc_rsn.common.Constants.TEAM_KEY;

public class TeamFeedFragment extends BaseFragment
        implements ViewPager.OnPageChangeListener,
        EditorialDetailInterface, TeamFeedContract.View,
        IntentHelper {

    private DataBarScrolling callback;
    private PersistentPlayer persistentPlayer;
    private FeedNavManager feedNavManager;

    public interface DataBarScrolling {
        void onDataBarScroll(int dx, int dy);
    }


    private final int REFRESH_DELAY_DURATION = 3000;
    private final int REFRESH_TRANSLATION_DURATION = 300;

    private TeamViewComponentsAdapter teamViewRecyclerAdapter;
    private TeamFeedPresenter teamFeedPresenter = null;

    private GradientDrawable teamColorGradient;

    @Getter
    TeamsPagerAdapter adapter;

    @Getter
    ViewPager viewPager;

    @BindView(R.id.item_list)
    @Getter
    RefreshLayout refreshLayout;

    @Getter
    @BindView(R.id.teamview_page_background_to_fade_to)
    LinearLayout backgroundFadeTo;

    private RecyclerVisibilityScrollListener recyclerVisibilityScrollListener;

    private DataBarScrollListener databarScrollListener;

    @Getter
    private ApplicationLifecycleListener applicationLifecycleListener;

    @BindView(R.id.team_view_template_coordinator_layout)
    FrameLayout teamViewBackground;

    private static final int FREE_ASSET = 1;
    private static final int NON_FREE_ASSET = 0;

    private static final int FAB_FLICK_DELAYED_CHECK_DURATION = 2000;

    public static TeamFeedFragment newInstance(Team team, Config config) {

        TeamFeedFragment fragment = new TeamFeedFragment();

        Bundle args = new Bundle();
        args.putParcelable(TEAM_KEY, team);
        args.putParcelable(CONFIG_KEY, config);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public int getLayout() {
        return R.layout.team_feed;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TeamContainerFragment teamViewContainer = (TeamContainerFragment) getParentFragment();
        viewPager = teamViewContainer.getPager();
        adapter = teamViewContainer.getAdapter();
        persistentPlayer = Injection.providePlayer((PersistentPlayerContract.Main.View) getContext());

        feedNavManager = FeedNavManager.Companion.newInstance(getChildFragmentManager());
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Team team = getArguments() != null ? getArguments().getParcelable(TEAM_KEY) : null;
        Config config = getArguments() != null ? getArguments().getParcelable(CONFIG_KEY) : null;
        assert team != null && config != null;
        teamFeedPresenter = new TeamFeedPresenter(this, team, config);

        if (getParentFragment() instanceof DataBarScrolling) {
            callback = (DataBarScrolling) getParentFragment();
        } else {
            throw new RuntimeException(context.toString() + " must implement DataBarScrollingListener ");
        }
        if (getParentFragment() instanceof MvpdLogoListener) {
            teamFeedPresenter.setMvpdLogoListener((MvpdLogoListener) getParentFragment());
        } else {
            throw new RuntimeException(context.toString() + " must implement DataBarScrollingListener ");
        }
    }

    @Override
    public void onViewCreated(@Nullable View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);
        recyclerVisibilityScrollListener = new RecyclerVisibilityScrollListener();
        refreshLayout.getRecyclerView().addOnScrollListener(recyclerVisibilityScrollListener);
        databarScrollListener = new DataBarScrollListener(callback);
        refreshLayout.getRecyclerView().addOnScrollListener(databarScrollListener);

        teamViewBackground.setBackgroundColor(teamFeedPresenter.getPrimaryColour());
        refreshLayout.addLottieAnimation("peacock_animation_black.json", true, 0.08f, 0.44f);
        refreshLayout.setOnRefreshListener(new RefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {
                refreshLayout.getRecyclerView().animate().alpha(0.0f).setDuration(REFRESH_TRANSLATION_DURATION).start();
                teamFeedPresenter.refresh(false);
                // Adding below condition can fix an issue where mini showing dark screen
                // when refresh on other team feed.
                if (persistentPlayer.getType() != PlayerConstants.Type.MINI){
                    persistentPlayer.setMediaSource(null);
                    persistentPlayer.release();
                }
            }

            @Override
            public void onRefreshSuccess(Object o) {
                refreshLayout.getRecyclerView().setAdapter((TeamViewComponentsAdapter) o);
                refreshLayout.getRecyclerView().animate().alpha(1.0f).setDuration(REFRESH_TRANSLATION_DURATION).start();
            }

            @Override
            public void onRefreshError() {
                refreshLayout.getRecyclerView().animate().alpha(1.0f).setDuration(REFRESH_TRANSLATION_DURATION).start();
            }
        });

        if (viewPager != null)
            viewPager.addOnPageChangeListener(this);

        // Set up application lifecycle listener,
        // live assets auto refresh disposable observer
        // and team feed auto refresh disposable observer
        setUpLifeCycleListenerAndAutoRefreshDO();

        // Load team data with appropriate method
        teamFeedPresenter.loadTeamDataWithAppropriateMethod();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (teamViewRecyclerAdapter != null) {
            teamViewRecyclerAdapter.onResume();
        }
    }

    /**
     * Called by TeamViewComponentsAdapter, when it is attached to the RecyclerView.
     */
    public boolean deeplinkToComponent() {

        DeeplinkManager manager = DeeplinkManager.getInstance();

        if (!manager.isState(DeeplinkManager.State.IN_PROGRESS)){
            return false;
        }

        if (manager.getDeeplink().isType(Deeplink.Type.TEAM)){
            manager.doTeamViewDeeplink(this);

        } else {
            manager.doEditorialDeeplink( this);
        }
        return true;
    }

    /**
     * WARNING: DO NOT USE unless you really have no other way.
     * This method only exists because {@link DeeplinkManager} needs to use it and it can't be
     * refactored easily.
     * It's painful...
     */
    public Team getTeam() {
        return teamFeedPresenter.getTeam();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (teamViewRecyclerAdapter != null) {
            teamViewRecyclerAdapter.onPause();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (viewPager != null)
            viewPager.removeOnPageChangeListener(this);
        if (refreshLayout != null)
            refreshLayout.getRecyclerView().removeOnScrollListener(recyclerVisibilityScrollListener);
        // Remove application lifecycle listener,
        // live assets auto refresh disposable observer
        // and team feed auto refresh disposable observer
        if (applicationLifecycleListener != null) {
            ApplicationLifecycleManager.removeApplicationLifecycleListener(applicationLifecycleListener);
        }
        teamFeedPresenter.unsubscribe();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (teamViewRecyclerAdapter != null) {
            teamViewRecyclerAdapter.onDestroy();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // It's to disable swiping if persistent player is in landscape
        if (viewPager instanceof EdgeSwipeViewPager) {
            ((EdgeSwipeViewPager) viewPager).setFragmentOrientation(newConfig.orientation);
        }
    }

    // region Update Adapter methods
    public void setupRecyclerView(List<FeedComponent> items, int theFeedLabelIndex, Team team) {
        // Create and set adapter
        if (items != null) {
            TeamsPagerFragment teamsPagerFragment = findTeamViewFragment();
            if (teamViewRecyclerAdapter == null) {
                teamViewRecyclerAdapter =
                        new TeamViewComponentsAdapter(teamsPagerFragment, items, team,
                                teamColorGradient, viewPager, this, persistentPlayer);
            }
            teamViewRecyclerAdapter.setTheFeedLabelIndex(theFeedLabelIndex);
            teamViewRecyclerAdapter.setMediumViewHolderReused(false);
            refreshLayout.getRecyclerView().setAdapter(teamViewRecyclerAdapter);
            teamViewRecyclerAdapter.onResume();
            //Once the data is loaded, remove the background color to make it consistent with the refresh animation
            teamViewBackground.setBackgroundColor(Color.WHITE);
        }
    }

    /**
     * Update recycler view adapter.
     * @return true if recycler view has been updated using provided data
     */
    @Override
    public boolean updatedAdapter(@NonNull List<FeedComponent> items, int theFeedLabelIndex, @NonNull Team team) {
        if (!refreshLayout.isRefreshing())
            return false;

        TeamsPagerFragment teamsPagerFragment = findTeamViewFragment();
        if (teamViewRecyclerAdapter == null) {
            teamViewRecyclerAdapter =
                    new TeamViewComponentsAdapter(teamsPagerFragment, items, team,
                            teamColorGradient, viewPager, this, persistentPlayer);
            teamViewRecyclerAdapter.setTheFeedLabelIndex(theFeedLabelIndex);
        }
        if (refreshLayout != null) {
            refreshLayout.postDelayed(() -> {
                //Confirm that after the 3000 delay, refreshLayout is still not null
                if (refreshLayout != null) {
                    teamViewRecyclerAdapter.setMediumViewHolderReused(false);
                    teamViewRecyclerAdapter.setTheFeedLabelIndex(theFeedLabelIndex);
                    teamViewRecyclerAdapter.updateData(items);
                    refreshLayout.setReceivedSuccess(teamViewRecyclerAdapter);
                }
            }, REFRESH_DELAY_DURATION);
        }
        teamViewRecyclerAdapter.setRefreshed(true);
        return true;
    }

    /**
     * This method is used to update teamViewRecyclerAdapter for the app
     * Background --> Foreground
     */
    @Override
    public void updateAdapterForBackAndFore(@NonNull List<FeedComponent> items, int theFeedLabelIndex, @NonNull Team team) {
        TeamsPagerFragment teamsPagerFragment = findTeamViewFragment();
        if (teamViewRecyclerAdapter == null) {
            teamViewRecyclerAdapter =
                    new TeamViewComponentsAdapter(teamsPagerFragment, items, team,
                            teamColorGradient, viewPager, this, persistentPlayer);
            teamViewRecyclerAdapter.setTheFeedLabelIndex(theFeedLabelIndex);
            refreshLayout.getRecyclerView().setAdapter(teamViewRecyclerAdapter);
        } else {
            teamViewRecyclerAdapter.setTheFeedLabelIndex(theFeedLabelIndex);
            // Check if medium is previously and currently showing
            if (teamFeedPresenter.isMediumViewHolderShowingPreviouslyAndCurrently(teamViewRecyclerAdapter.getComponents(), items)){
                teamViewRecyclerAdapter.setMediumViewHolderReused(false);
                teamViewRecyclerAdapter.updateDataWhenBackAndFore(items);
            } else {
                teamViewRecyclerAdapter.setMediumViewHolderReused(false);
                teamViewRecyclerAdapter.updateData(items);
            }
        }
        teamViewRecyclerAdapter.onResume();
        //Once the data is loaded, remove the background color to make it consistent with the refresh animation
        teamViewBackground.setBackgroundColor(Color.WHITE);
    }

    /**
     * This method is used to update teamViewRecyclerAdapter for
     * live assets auto refresh is triggered
     */
    @Override
    public void updateAdapterForLiveAssetsAutoRefresh(@NonNull List<FeedComponent> items, int theFeedLabelIndex, @NonNull Team team) {
        TeamsPagerFragment teamsPagerFragment = findTeamViewFragment();
        if (teamViewRecyclerAdapter == null) {
            teamViewRecyclerAdapter =
                    new TeamViewComponentsAdapter(teamsPagerFragment, items, team,
                            teamColorGradient, viewPager, this, persistentPlayer);
            teamViewRecyclerAdapter.setTheFeedLabelIndex(theFeedLabelIndex);
            refreshLayout.getRecyclerView().setAdapter(teamViewRecyclerAdapter);
        } else {
            teamViewRecyclerAdapter.setTheFeedLabelIndex(theFeedLabelIndex);
            // Check if medium is previously and currently showing
            if (teamFeedPresenter.isMediumViewHolderShowingPreviouslyAndCurrently(teamViewRecyclerAdapter.getComponents(), items)){
                if (teamFeedPresenter.isMediaSourceTheSamePreviouslyAndCurrently(teamViewRecyclerAdapter.getComponents(), items)){
                    teamViewRecyclerAdapter.setMediumViewHolderReused(false);
                    teamViewRecyclerAdapter.updateDataWhenLiveAssetsAutoRefreshAndSameMediaSource(items);
                } else {
                    teamViewRecyclerAdapter.setMediumViewHolderReused(true);
                    // Reset orientation here to prevent the app stuck on landscape
                    persistentPlayer.resetOrientation();
                    teamViewRecyclerAdapter.updateDataWhenLiveAssetsAutoRefreshAndDiffMediaSource(items);
                }
            } else {
                teamViewRecyclerAdapter.setMediumViewHolderReused(false);
                if (teamFeedPresenter.isMediumViewHolderOnlyShowingPreviously(teamViewRecyclerAdapter.getComponents(), items)){
                    // Reset orientation here to prevent the app stuck on landscape
                    persistentPlayer.resetOrientation();
                } else if (teamFeedPresenter.isMediumViewHolderOnlyShowingCurrently(teamViewRecyclerAdapter.getComponents(), items)){
                    // Reset orientation here to prevent the app stuck on landscape
                    persistentPlayer.resetOrientation();
                }
                // Reset orientation here to prevent the app stuck on landscape
                // Updating medium view holder show or hide may affect feed promo view holder
                // so the whole team feed list need to be updated
                teamViewRecyclerAdapter.updateData(items);
            }
        }
        teamViewRecyclerAdapter.onResume();
        //Once the data is loaded, remove the background color to make it consistent with the refresh animation
        teamViewBackground.setBackgroundColor(Color.WHITE);
    }

    /**
     * This method is used to update teamViewRecyclerAdapter for
     * team feed auto refresh is triggered
     */
    @Override
    public void updateAdapterForTeamFeedAutoRefresh(@NonNull List<FeedComponent> items, int theFeedLabelIndex, @NonNull Team team) {
        updateAdapterForBackAndFore(items, theFeedLabelIndex, team);
    }
    // endregion

    // region View methods
    @Override
    public void showRefreshError() {
        if (refreshLayout != null && refreshLayout.isRefreshing()) {
            refreshLayout.setReceivedError();
        }
    }

    @Override
    public void setStatusBarColor(int color) {
        ActivityUtils.setStatusBarColor(getActivity(), color);
    }

    @Override
    public void setTeamColorGradient(@NonNull GradientDrawable gradient) {
        teamColorGradient = gradient;
    }

    public boolean resumeAdapter() {
        if (teamViewRecyclerAdapter != null) {
            // This is the default implementation, not sure whether we need it
            teamViewRecyclerAdapter.onResume();
            return true;
        } else {
            //Check for err messages (or any error conditions)
            // When return false, presenter will try to fetch
            return false;
        }
    }
    // endregion

    private TeamsPagerFragment findTeamViewFragment() {
        if (getActivity() == null) {
            return null;
        }

        List<Fragment> fragments = getActivity().getSupportFragmentManager().getFragments();
        for (Fragment fragment : fragments) {
            if (fragment instanceof TeamsPagerFragment) {
                TeamsPagerFragment teamsPagerFragment = (TeamsPagerFragment) fragment;
                return teamsPagerFragment;
            }
        }
        return null;
    }

    @Override
    public void onPageScrolled(int viewPagerPosition, float positionOffset, int positionOffsetPixels) {
        //TODO: Because this method is called about 20 or 40 times per each page swiping, would need to find solutions
        //TODO: (1) to not call getTeamPosition() again and again;
        //TODO: (2) call teamViewRecyclerAdapter.onPause() only once.

        int teamPosition =  adapter.getItemPositionForPager(teamFeedPresenter.getTeam());
        int viewPagerPositionMod = adapter.getViewPagerPosition(viewPagerPosition);
        if (viewPagerPositionMod != teamPosition && positionOffset == 0f) {

            if (teamViewRecyclerAdapter != null)
                teamViewRecyclerAdapter.onPause();
        }
    }

    @Override
    public void onPageSelected(int viewPagerPosition) {
        if(getActivity()==null) return;
        if(getActivity().isFinishing()) return;
        if(getActivity().isDestroyed()) return;

        Timber.d(
                "onPageSelected: PgPos=%d, selectedTeam: %s",
                viewPagerPosition,
                TeamManager.Companion.getInstance().getSelectedTeam().getDisplayName()
        );
        teamFeedPresenter.tryRefresh();

        // This will show the fab flick animation if available when
        // 1. User switch team using the edge
        // Note: this won't trigger the fab flick animation if using fab menu to switch team
        // Because onPageSelected() method will be called before
        // DisplayUtils.isVisible(refreshLayout) is true
        if (!deeplinkToComponent()) {
            if (teamFeedPresenter.isUserRequiredToSeeFtueFlick()){
                showFabFlickRepeated();
            }
        }
        // Start fab tap ftue disposable observer when
        // 1. User switch team using the edge or fab flick
        // Note: the disposable observer here will complete if using fab menu to switch team
        // Because onPageSelected() method will be called before
        // DisplayUtils.isVisible(refreshLayout) is true
        startFabTapFtue();
        // Start data menu ftue disposable observer when
        // 1. User switch team using the edge or fab flick
        // Note: the disposable observer here will complete if using fab menu to switch team
        // Because onPageSelected() method will be called before
        // DisplayUtils.isVisible(refreshLayout) is true
        startDataMenuFtue();
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        teamFeedPresenter.recordPageScrollStateChanged(state);
    }

    public void showDataBar() {
        Fragment fragment = getParentFragment();
        if (fragment instanceof TeamContainerFragment){
            ((TeamContainerFragment)fragment).showDataBar();
        }
    }

    public void hideDataBar() {
        Fragment fragment = getParentFragment();
        if (fragment instanceof TeamContainerFragment){
            ((TeamContainerFragment)fragment).hideDataBar();
        }
    }

    // region EditorialDetailTemplateFragment animation
    @Override
    public void closePage(boolean animated) {
        feedNavManager.closePage(animated, backgroundFadeTo);

        if(getChildFragmentManager().getBackStackEntryCount() == 1 && adapter.getCurrentFragment() != null) {
            TeamContainerFragment teamContainerFragment =  (TeamContainerFragment) adapter.getCurrentFragment();
            teamContainerFragment.showMvpdHeader();
        }
    }

    @Override
    public void closeAllPages(boolean animated) {
        feedNavManager.closeAllPages(animated, backgroundFadeTo);

        if(adapter.getCurrentFragment() != null) {
            TeamContainerFragment teamContainerFragment = (TeamContainerFragment) adapter.getCurrentFragment();
            teamContainerFragment.showMvpdHeader();
        }
    }
    // endregion

    /**
     * This method is used to check
     *
     * 1. If at least one editorial details view is opened
     *
     * @return true if at least one editorial details view is opened
     *         false otherwise
     */
    @Override
    public boolean hasAtLeastOneStackableViewOpened() {
        return feedNavManager.hasAtLeastOneStackableViewOpen();
    }

    public RecyclerView getRecyclerView() {
        return refreshLayout.getRecyclerView();
    }

    /***
     * This function will call MainActivity's doFabFtueFlickAnim() method when the following
     *  conditions are met:
     *      1. This fragment's refreshLayout is still visible by the user (this fragment is still visible)
     *      2. Activity is not null
     *      3. The fab menu has never been flicked before
     *      4. 2 seconds after the TeamViewComponentAdapter (calling class) has been attached to
     *       the recycler view via onAttachedToRecyclerView() and every 3 seconds after the first showing.
     *
     *  If the Ftue Notification Banner has been displayed once, it will never be displayed again.
     *   This is ensured by calling FtueUtil's recordFabFlickMsgViewed().
     */
    public void showFabFlickRepeated(){
        compositeDisposable.add(Observable.interval(1000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .takeWhile(aLong ->
                        DisplayUtils.isVisible(refreshLayout)
                                && getActivity() != null
                                && FtueUtil.fabHasNeverBeenFlicked())
                .doOnNext(aLong -> {
                    if (!FtueUtil.hasViewedFabFlickMsg()){
                        // need to show notification
                        NotificationsManagerKt.INSTANCE.showFtueFabFlickMessage();
                        FtueUtil.recordFabFlickMsgViewed();
                    }
                })
                .takeWhile(aLong -> {
                    // only animate flick either when the banner is showing
                    if (!NotificationsManagerKt.INSTANCE.isFtueFabFlickBannerShowing()){
                        // User just dismissed banner, this is equivalent of using the fab.
                        FtueUtil.setFabFlung(((MainActivity) getActivity()));
                        return false;
                    } else {
                        return true;
                    }
                })
                .subscribeWith(new DisposableObserver<Long>() {
                                   @Override
                                   public void onNext(Long aLong) {
                                       if (!DisplayUtils.isVisible(refreshLayout) || getActivity() == null) return;

                                       if (aLong == 1 || (aLong - 1) % 3 == 0) {
                                           // show on 2nd second or every 3 seconds after the 2nd second.
                                           ((MainActivity) getActivity()).doFabFtueFlickAnim();
                                       }
                                   }

                                   @Override
                                   public void onError(Throwable e) {
                                       this.dispose();
                                   }

                                   @Override
                                   public void onComplete() {}
                               }
                ));
    }

    public void onAdapterAttachedToRecyclerView() {
        // todo: ideally this check should be done in presenter
        // This will show the fab flick animation if available when
        // 1. User switch team using the fab menu
        // Note: this won't trigger the fab flick animation if using edge to switch team
        // Because onAdapterAttachedToRecyclerView() method will be called when the view
        // is created, which mean team == TeamManager.getInstance()?.getSelectedTeam() is
        // always false
        if (!deeplinkToComponent()) {
            if (teamFeedPresenter.isUserRequiredToSeeFtueFlick()){
                showFabFlickRepeated();
            }
        }
        // Start fab tap ftue disposable observer when
        // 1. User switch team using the fab menu
        // Note: the disposable observer here will complete if using edge or
        // fab flick to switch team
        // Because only the newest created team feed will call onAdapterAttachedToRecyclerView(),
        // which means DisplayUtils.isVisible(refreshLayout) is false and complete
        // the disposable observer
        startFabTapFtue();
        // Start data menu ftue disposable observer when
        // 1. User switch team using the fab menu
        // Note: the disposable observer here will complete if using edge or
        // fab flick to switch team
        // Because only the newest created team feed will call onAdapterAttachedToRecyclerView(),
        // which means DisplayUtils.isVisible(refreshLayout) is false and complete
        // the disposable observer
        startDataMenuFtue();
    }

    public List<FeedComponent> getFeedComponents(){
        return teamViewRecyclerAdapter.getComponents();
    }

    /***
     * Finds the index of the component in the list of FeedComponents components that matches the
     *  deeplink's componentId
     *
     * @param componentId - List of FeedComponent from the TeamFeedFragment
     * @return - index of the matching FeedComponent. Returns -1 if not found.
     */
    public int getFeedComponentIndex(String componentId){
        List<FeedComponent> components = teamViewRecyclerAdapter.getComponents();
        if (components == null || components.isEmpty()) return -1;
        for (int i = 0; i < components.size(); i++) {
            FeedComponent fc = components.get(i);
            if (fc == null) continue;
            if (StringUtils.equalsIgnoreCase(fc.getComponentId(), componentId)){
                return i;
            }
        }
        return -1;
    }


    @Override
    public PageInfo getPageInfo() {
        return teamFeedPresenter.getPageInfo();
    }

    public void refreshAndResetAutoRefresh() {
        // boolean value is true when calling from ApplicationLifecycleListener
        // if this needs to be called from other places, maybe have this boolean pass in
        teamFeedPresenter.refresh(true);
        teamFeedPresenter.setUpLiveAssetsAndTeamFeedAutoRefreshDO();
    }

    /**
     * Set up application lifecycle listener,
     * live assets auto refresh disposable observer
     * and team feed auto refresh disposable observer
     */
    public void setUpLifeCycleListenerAndAutoRefreshDO() {
        if (teamFeedPresenter.isMoreThanOneTeamInUsersTeams()){
            if (teamFeedPresenter.isTeamDisplayNameTheSameAsSelected()){
                // Remove all application lifecycle first
                ApplicationLifecycleManager.removeAllListeners();
                // Check if application lifecycle listener is null
                // If so, create one
                if (applicationLifecycleListener == null){
                    applicationLifecycleListener = new ApplicationLifecycleListener(this);
                }
                // Then add the application lifecycle listener
                ApplicationLifecycleManager.addApplicationLifecycleListener(applicationLifecycleListener);

                // Remove all live assets auto refresh disposable observer
                // and team feed auto refresh disposable observer
                AutoRefreshManager.removeAllObservers();
                // Set up live assets auto refresh disposable observer
                // and team feed auto refresh disposable observer
                // Add them into AutoRefreshManager for management
                teamFeedPresenter.setUpLiveAssetsAndTeamFeedAutoRefreshDO();
            }
        } else {
            /*
            * Client still wants infinite carousel even when there's only one team selected.
            * Therefore, we're duplicating the same team fragment to create the infinite carousel.
            * When that happens, we want to allow auto-update on all fragments.
            * */
            if (applicationLifecycleListener == null){
                applicationLifecycleListener = new ApplicationLifecycleListener(this);
            }
            ApplicationLifecycleManager.addApplicationLifecycleListener(applicationLifecycleListener);

            // Set up live assets auto refresh disposable observer
            // and team feed auto refresh disposable observer
            // Add them into AutoRefreshManager for management
            teamFeedPresenter.setUpLiveAssetsAndTeamFeedAutoRefreshDO();
        }
    }

    /**
     * This method is used to scroll the team feed to the top
     * and base on the animated parameter, choose using
     * smoothScrollToPosition(...) or scrollToPosition(...)
     *
     * @param animated
     */
    public void scrollTeamFeedToTheTop(boolean animated) {
        if (refreshLayout.isRefreshing() || getRecyclerView() == null
                || getRecyclerView().getAdapter() == null
                || getRecyclerView().getAdapter().getItemCount() == 0){
            return;
        }
        if (animated){
            getRecyclerView().smoothScrollToPosition(0);
        } else {
            getRecyclerView().scrollToPosition(0);
        }
    }

    @Override
    public Fragment getTopChildFragment() {
        return getChildFragmentManager().findFragmentById(R.id.editorial_detail);
    }

    @Override
    public Activity getActivityRef() {
        return getActivity();
    }

    /**
     * This method is used to prepare fab tap ftue disposable observer
     */
    public void startFabTapFtue() {
        if (teamFeedPresenter.isUserRequiredToSeeFtueTap()){
            teamFeedPresenter.startFabTapFtueDisposableObserver(1000L, refreshLayout);
        }
    }

    /**
     * This method is used to prepare data menu ftue disposable observer
     */
    public void startDataMenuFtue() {
        if (teamFeedPresenter.isUserRequiredToSeeFtueDataMenu()){
            teamFeedPresenter.startDataMenuFtueDisposableObserver(1000L, refreshLayout);
        }
    }
}
