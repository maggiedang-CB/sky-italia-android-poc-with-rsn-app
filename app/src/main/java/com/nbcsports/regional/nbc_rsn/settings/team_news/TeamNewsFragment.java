package com.nbcsports.regional.nbc_rsn.settings.team_news;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nbcsports.regional.nbc_rsn.MainActivity;
import com.nbcsports.regional.nbc_rsn.R;
import com.nbcsports.regional.nbc_rsn.analytics.TrackingHelper.PageInfo;
import com.nbcsports.regional.nbc_rsn.common.BaseFragment;
import com.nbcsports.regional.nbc_rsn.common.Team;
import com.nbcsports.regional.nbc_rsn.navigation.NavigationManager;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.OnClick;


public class TeamNewsFragment extends BaseFragment implements TeamNewsContract.View {

    TeamNewsPresenter presenter;

    @BindView(R.id.fragment_root)
    NestedScrollView root;

    @BindView(R.id.back_button)
    ImageView backButton;

    @BindView(R.id.exit_button)
    ImageView exitButton;

    @BindView(R.id.team_news_text)
    TextView teamNewsHeaderText;

    @BindView(R.id.edit_notification_settings_text_view)
    TextView editNotificationSettingsTextView;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    @BindView(R.id.all_changed_saved_text_view)
    TextView allChangedSavedTextView;

    TeamNewsAdapter adapter;

    private boolean arrivingFromSettingsFragment = true;

    @Override
    public int getLayout() {
        return R.layout.fragment_team_news;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        //this.trackingPageName = "TeamNewsFragment";
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        presenter = new TeamNewsPresenter(this);
        initRecyclerView();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            updateViewsBasedOnPreviousFragment();

            if (getActivity() == null) {
                return;
            }

            ArrayList<Team> teams = ((MainActivity) getActivity()).getTeamManager().getUsersTeams();
            adapter.setTeamData(teams, presenter.getTeamNewsOptStatuses(teams));
            root.scrollTo(0, 0);
        }
    }

    public void setArrivingFromSettingsFragment(boolean arrivingFromSettingsFragment) {
        this.arrivingFromSettingsFragment = arrivingFromSettingsFragment;
    }

    @Override
    public void updateViewsBasedOnPreviousFragment() {
        backButton.setVisibility(arrivingFromSettingsFragment ? View.VISIBLE : View.GONE);
        exitButton.setVisibility(arrivingFromSettingsFragment ? View.GONE : View.VISIBLE);
    }

    private void initRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });
        adapter = new TeamNewsAdapter();
        recyclerView.setAdapter(adapter);

        adapter.setTeamNewsPresenter(presenter);

        recyclerView.getItemAnimator().setChangeDuration(0);
    }

    @OnClick(R.id.back_button)
    public void goBackToSettings() {
        backButton.setVisibility(View.GONE);
        exitButton.setVisibility(View.GONE);
        NavigationManager.getInstance().closeAndRemoveFragmentFromBackStack();
    }

    @Override
    @OnClick(R.id.exit_button)
    public void exitTeamNewsFragment() {
        backButton.setVisibility(View.GONE);
        exitButton.setVisibility(View.GONE);
        NavigationManager.getInstance().closeAndRemoveFragmentFromBackStack();
    }

    @Override
    public void onLocalizationManagerInitialized() {
        presenter.setUpTextViewWithLocalizedText(teamNewsHeaderText, editNotificationSettingsTextView,
                allChangedSavedTextView);
        if (adapter != null){
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public PageInfo getPageInfo() {
        return new PageInfo(false, "", "", "", "", "", "", "", "", "", "");
    }
}