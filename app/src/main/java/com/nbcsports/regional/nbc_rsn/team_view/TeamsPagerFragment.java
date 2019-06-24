package com.nbcsports.regional.nbc_rsn.team_view;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.nbcsports.regional.nbc_rsn.ConfigListener;
import com.nbcsports.regional.nbc_rsn.MainActivity;
import com.nbcsports.regional.nbc_rsn.MainContract;
import com.nbcsports.regional.nbc_rsn.MainPresenter;
import com.nbcsports.regional.nbc_rsn.R;
import com.nbcsports.regional.nbc_rsn.analytics.TrackingHelper.PageInfo;
import com.nbcsports.regional.nbc_rsn.common.BaseFragment;
import com.nbcsports.regional.nbc_rsn.common.Config;
import com.nbcsports.regional.nbc_rsn.common.IpGeolocation;
import com.nbcsports.regional.nbc_rsn.common.Team;
import com.nbcsports.regional.nbc_rsn.common.TeamManager;
import com.nbcsports.regional.nbc_rsn.localization.models.Localizations;

import java.lang.reflect.Field;
import java.util.List;

import butterknife.BindView;
import lombok.Getter;
import timber.log.Timber;

/**
 * A fragment representing a teamview.
 * <p>
 * TODO: MVP
 */
public class TeamsPagerFragment extends BaseFragment implements ConfigListener {

    public static final int WRAPS_AROUND = 10000;

    private MainContract.Presenter mainPresenter;
    private TeamsPagerAdapter mPagerAdapter;
    /**
     * A OnPageChangeListener is used to update the teamview with a team data and attributes
     * when a new team is selected in the ViewPager.
     */
    private TeamsOnPageChangeListener onPageChangeListener;

    @Getter
    @BindView(R.id.viewpager)
    public ViewPager viewPager;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public TeamsPagerFragment() {
    }

    @Override
    public int getLayout() {
        return R.layout.teams;
    }

    // The method that will return teams for Team View
    // Any reference to teams should use this method so that we are using the same list
    public List<Team> getTeamsForTeamView() {
        return ((MainActivity) getActivity()).getTeamManager().getUsersTeams();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        mainPresenter = MainPresenter.Injection.providePresenter(getContext());
        mainPresenter.configAddListener(this);

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        //this.trackingPageName = "TeamsPagerFragment";
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mPagerAdapter = new TeamsPagerAdapter(getFragmentManager(), viewPager, getTeamsForTeamView());
        onPageChangeListener = new TeamsOnPageChangeListener(mPagerAdapter, getActivity(), menuInterface);
        viewPager.addOnPageChangeListener(onPageChangeListener);
        // slow down default scrolling speed
        try {
            Field mScroller = ViewPager.class.getDeclaredField("mScroller");
            mScroller.setAccessible(true);
            AccelerateDecelerateInterpolator sInterpolator = new AccelerateDecelerateInterpolator();
            FixedSpeedScroller fixedSpeedScroller = new FixedSpeedScroller(viewPager.getContext(), sInterpolator);
            mScroller.set(viewPager, fixedSpeedScroller);
        } catch (IllegalAccessException e) {
        } catch (NoSuchFieldException e) {
        }

        viewPager.setAdapter(mPagerAdapter);

        // onTeamSelected(BACKGROUND): selectedTeam will be empty at first launch and force quit
        // but returning from background will have selected team
        Team selectedTeam = getSelectedTeam();
        if (selectedTeam != null) {
            Timber.d("onTeamSelected(BACKGROUND) onViewCreated() selectedTeam: %s", selectedTeam);
            mPagerAdapter.setTeams(getTeamsForTeamView());
            onTeamSelected(selectedTeam);
        }
    }

    public void resetPagerAdapter() {
        // Re-use the Config for the new PagerAdapter
        Config config = mPagerAdapter.getConfig();

        // Release stuff (if any, like registered listeners or observers) related to old instance of pager adapter
        // (we don't have anything to unregister at this point).

        // Create a new adapter
        mPagerAdapter = new TeamsPagerAdapter(getFragmentManager(), viewPager, getTeamsForTeamView());
        // Link existing stuff to the new adapter - same as in TeamsPagerFragment.onViewCreated
        onPageChangeListener.setViewPagerAdapter(mPagerAdapter);
        viewPager.setAdapter(mPagerAdapter);
        mPagerAdapter.setTeams(getTeamsForTeamView());
        // Link existing Config to the new adapter - same as in TeamsPagerFragment.onReceived
        mPagerAdapter.setConfig(config);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mainPresenter.configRemoveListener(this);
        if (viewPager != null)
            viewPager.removeOnPageChangeListener(onPageChangeListener);
    }

    private Team getSelectedTeam() {
        List<Team> teams = getTeamsForTeamView();
        TeamManager teamManager = ((MainActivity) getActivity()).getTeamManager();
        // Update team based on what's selected
        Team selectedTeam = teamManager.getSelectedTeam();
        // Fail safe in case there is no selected team, in which case it will fall back to first team
        if (selectedTeam == null && !teams.isEmpty()) {
            teamManager.setSelectedTeam(teams.get(0));
            selectedTeam = teamManager.getSelectedTeam();
        }

        if (selectedTeam != null)
            Timber.d("getSelectedTeam selectedTeam: %s", selectedTeam.getDisplayName());
        else
            Timber.d("getSelectedTeam selectedTeam: %s", selectedTeam);

        return selectedTeam;
    }

    public void showTeam(Team team, Boolean smoothScroll) {

        if (team == null) return;

        Timber.d("showTeam selectedTeam: %s", team.getDisplayName());

        int position = getAdapterPositionByTeam(team);
        Timber.d("showTeam getAdapterPositionByTeam position: %s", position);
        if (position > -1) {
            int currentItem = (position + mPagerAdapter.getTeams().size() * 50);
            Timber.d("showTeam selectedTeam: %s, currentItem: %s", team.getDisplayName(), currentItem);

            // Note, it results in calling >>> TeamFeedFragment.onViewCreated() >>> TeamFeedFragment.getTeamviewItemData(Team team, RecyclerView rv)
            viewPager.setAdapter(mPagerAdapter); // this is here to fix the weird scrolling effect

            // Note, it results in calling >>> TeamFeedFragment.onPageSelected(int viewPagerPosition), while getTeamviewItemData() request is still in progress?
            viewPager.setCurrentItem(currentItem, smoothScroll);

            // This would result in calling >>> DataSetObserver.onChanged() if we registered: mPagerAdapter.registerDataSetObserver(new DataSetObserver {...});
            mPagerAdapter.notifyDataSetChanged();
        }

    }

    private int getAdapterPositionByTeam(Team team) {
        // find the position in adapter
        int position = mPagerAdapter.getItemPositionForPager(team);
        return position;
    }

    public Team showTeam(boolean flingDirectionRight) {
        int currentItem = viewPager.getCurrentItem();

        if (flingDirectionRight) {
            int position = viewPager.getCurrentItem() + 1;
            if (position < mPagerAdapter.getCount()) {
                viewPager.setCurrentItem(position, true);
            }
        } else {
            int position = viewPager.getCurrentItem() - 1;
            if (position > -1) {
                viewPager.setCurrentItem(position, true);
            }
        }
        Timber.d("showTeam flingDirectionRight:%s, %s -> %s", flingDirectionRight, currentItem, viewPager.getCurrentItem());

        return mPagerAdapter.getTeams().get(viewPager.getCurrentItem() % getTeamsForTeamView().size());
    }

    @Override
    public void onReceived(Config config) {
        //System.out.println("This is the enter point: TeamsPagerFragment onReceived");
        List<Team> teams = getTeamsForTeamView();
        mPagerAdapter.setTeams(teams);
        mPagerAdapter.setConfig(config);
        Timber.d("onReceived() teams: %s", teams.size());

        // onTeamSelected(FIRST LAUNCH): This handles first launch/force quit scenario
        /*Team selectedTeam = getSelectedTeam();
        if (selectedTeam != null) {
            Timber.d("onTeamSelected(FIRST LAUNCH) onReceived() selectedTeam: %s", selectedTeam.getDisplayName());
            onTeamSelected(selectedTeam);
        }*/
        //Removed because this is called later in NavigationManager goToSelectedTeamView, and the duplication of functionality was causing analytics to be sent multiple times
        //Tested first launch/force quit scenarios and was unable to find a situation where removing this caused problems. 
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

    // onTeamSelected(FAB MENU): This handles selected team from fab menu
    public void onTeamSelected(Team team) {
        showTeam(team, false);
        //trackPage();
    }

    public TeamsPagerAdapter getPagerAdapter() {
        return mPagerAdapter;
    }

    @Override
    public PageInfo getPageInfo() {
        Team selectedTeam = getSelectedTeam();
        return new PageInfo(false, "", selectedTeam.getDisplayName(), "Home", "", "", "", "", "", "", "");
    }

}
