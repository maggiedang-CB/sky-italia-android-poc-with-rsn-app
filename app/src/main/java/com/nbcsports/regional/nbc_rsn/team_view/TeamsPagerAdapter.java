package com.nbcsports.regional.nbc_rsn.team_view;

import android.os.Bundle;
import android.os.Parcelable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.text.TextUtils;
import android.view.ViewGroup;

import com.nbcsports.regional.nbc_rsn.common.Config;
import com.nbcsports.regional.nbc_rsn.common.Team;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

// TeamView
public class TeamsPagerAdapter extends FragmentStatePagerAdapter {

    /*
     * The magic that RV's scroll position can be recovered after recreation is FragmentStatePagerAdapter.
     * It saves the state into Bundle using the fragment position as index, when fragment is destroyed.
     * Then when new fragments are created, it feeds the saved state to the new fragment, so it looks like
     * that we're using the same fragment, but its actually different fragment with the same state.
     *
     * However, all instance variables will be lost. So if you have instance variables that you want to recover,
     * you need to override `onSaveInstanceState()` and `onRestoreInstanceState(state: Parcelable?)` to do it manually.
     * */

    public static final int PAD = 10;
    public static final int BLANK_SCREEN_FIX_LIMIT = 5;
    private final ViewPager viewPager;
    private final List<Team> userTeams;
    private List<Team> teams = new ArrayList<>();
    private Fragment currentFragment;

    @Setter @Getter
    private Config config;

    public TeamsPagerAdapter(FragmentManager fm, ViewPager viewPager, List<Team> userTeams) {
        super(fm);
        this.viewPager = viewPager;
        this.userTeams = userTeams;
    }

    // Never maintain any states from the base class to avoid TransactionTooLargeException
    @Override
    public Parcelable saveState() {
        Bundle bundle = (Bundle) super.saveState();
        if (bundle != null){
            bundle.putParcelableArray("states", null);
        }
        return bundle;
    }

    @Override
    public Fragment getItem(int position) {

        // check to prevent 'ArithmeticException: divide by zero' in case it's called
        // before teams are available.
        if (teams.size() == 0){ return null; }

        Team team = teams.get(position % teams.size());
//        Timber.d("getItem position: %s, team: %s", position, team.getDisplayName());
        return TeamContainerFragment.Companion.newInstance(team, this, viewPager, config);
    }

    @Override
    public int getCount() {
        return teams.size() * TeamsPagerFragment.WRAPS_AROUND;
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    public void setTeams(List<Team> teams) {

        if (teams == null){ return; }

        this.teams.clear();

        // if less than 5 teams, pad the teams so that wraps around effect will work
        // https://github.com/antonyt/InfiniteViewPager/issues/2
        List<Team> paddedArray = new ArrayList<>();

        if (teams.size() < BLANK_SCREEN_FIX_LIMIT) {
            for (int i = 0; i < PAD; i++) {
                for (Team team : teams) {
                    paddedArray.add(team);
                }
            }
        } else {
            paddedArray = teams;
        }

        this.teams.addAll(paddedArray);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        return (TeamContainerFragment) super.instantiateItem(container, position % teams.size());
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        super.destroyItem(container, position % teams.size(), object);
    }

    public List<Team> getTeams() {
        return teams;
    }

    public int getItemPositionForPager(Team team) {
        int i = 0;
        int position = -1;
        for (Team _team : getTeams()) {
            if (TextUtils.isEmpty(_team.getTeamId())) {
                break;
            }

            if (_team.getTeamId().equalsIgnoreCase(team.getTeamId())) {
                position = i;
                break;
            }
            i++;
        }
        return position;
    }

    public int getViewPagerPosition(int viewPagerPosition) {
        // need to mod with original team size()
        int mod;
        if (userTeams.size() < BLANK_SCREEN_FIX_LIMIT) {
            mod = getTeams().size() / PAD;
        } else {
            mod = getTeams().size();
        }
        return viewPagerPosition % mod;
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        currentFragment = ((Fragment) object);
        super.setPrimaryItem(container, position, object);
    }

    public Fragment getCurrentFragment() {
        return currentFragment;
    }
}
