package com.nbcsports.regional.nbc_rsn.moreteams;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

import com.nbcsports.regional.nbc_rsn.MainActivity;
import com.nbcsports.regional.nbc_rsn.R;
import com.nbcsports.regional.nbc_rsn.analytics.TrackingHelper.PageInfo;
import com.nbcsports.regional.nbc_rsn.common.BaseFragment;
import com.nbcsports.regional.nbc_rsn.common.Team;
import com.nbcsports.regional.nbc_rsn.data_menu.datamenuftue.DataMenuFtueManager;
import com.nbcsports.regional.nbc_rsn.fabigation.fabtapftue.FabTapFtueManager;
import com.nbcsports.regional.nbc_rsn.lifecycle.ApplicationLifecycleManager;
import com.nbcsports.regional.nbc_rsn.navigation.NavigationManager;
import com.nbcsports.regional.nbc_rsn.persistentplayer.view.progressbar.PersistentPlayerProgressBarManager;
import com.nbcsports.regional.nbc_rsn.team_feed.auto_refresh.AutoRefreshManager;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

public class MoreTeamsFragment extends BaseFragment {

    @BindView(R.id.recyclerview)
    RecyclerView recyclerView;

    private MoreTeamsAdapter adapter;
    private boolean isMoreTeamVisible;

    @Override
    public int getLayout() {
        return R.layout.fragment_more_team;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        init();
    }

    private void init() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new MoreTeamsAdapter(new ArrayList<>());

        compositeDisposable.add(adapter.getViewClickSubject()
                .subscribe(
                        team -> {
                            if (isMoreTeamVisible) {
                                ((MainActivity) getActivity()).getTeamManager().setSelectedTeam(team);
                                menuInterface.switchFabLogo(team);
                                NavigationManager.getInstance().showTeamPagerFragment(R.anim.enter_team_from_more_team, R.anim.exit_more_team_into_team);
                            }
                        },
                        e -> e.printStackTrace()
                ));

        recyclerView.setAdapter(adapter);
        updateTeamList();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        isMoreTeamVisible = false;
        if (!hidden) {
            updateTeamList();
            isMoreTeamVisible = true;
            // Remove all application lifecycle listeners
            // Because if More Teams fragment is showing and app go into background,
            // then brought back from foreground, nothing should be refreshed
            ApplicationLifecycleManager.removeAllListeners();
            // Remove all live assets auto refresh disposable observer
            // and team feed auto refresh disposable observer
            // Because if More Teams fragment is showing, nothing should be refreshed
            AutoRefreshManager.removeAllObservers();
            // Remove all progress bar disposable observers
            // Because if More Teams fragment is showing, progress bar
            // should not be updated
            PersistentPlayerProgressBarManager.removeAllObservers();
            // Remove all fab tap ftue disposable observers
            // Because if More Teams fragment is showing, no fab tap ftue should be shown
            FabTapFtueManager.Companion.removeAllObservers();
            // Remove all data menu ftue disposable observers
            // Because if More Teams fragment is showing, no data menu ftue should be shown
            DataMenuFtueManager.Companion.removeAllObservers();
        }
    }

    private void updateTeamList() {
        List<Team> moreTeamsList = ((MainActivity) getActivity()).getTeamManager().getMoreTeamsList();
        adapter.setTeamList(moreTeamsList);
    }

    @Override
    public PageInfo getPageInfo() {
        return new PageInfo(false, "", "", "", "", "", "", "", "", "", "");
    }
}
