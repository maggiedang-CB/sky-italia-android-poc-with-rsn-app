package com.nbcsports.regional.nbc_rsn.editorial_detail;

import android.app.Activity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import com.nbcsports.regional.nbc_rsn.MainActivity;
import com.nbcsports.regional.nbc_rsn.common.EditorialDetailsFeed;
import com.nbcsports.regional.nbc_rsn.common.Team;
import com.nbcsports.regional.nbc_rsn.team_view.TeamsPagerAdapter;

import org.apache.commons.lang3.StringUtils;

import timber.log.Timber;

public class EditorialDetailsPageChangeListener implements ViewPager.OnPageChangeListener {

    private final TeamsPagerAdapter adapter;
    private final Activity activity;
    private final Team team;
    private final FragmentManager fragmentManager;
    private final EditorialDetailsPageSelected listener;
    private final EditorialDetailTemplateFragment editorialDetailTemplateFragment;
    private EditorialDetailsFeed editorialDetailsFeed;

    public EditorialDetailsPageChangeListener(TeamsPagerAdapter adapter,
                                              Activity activity,
                                              Team team,
                                              FragmentManager fragmentManager,
                                              EditorialDetailTemplateFragment editorialDetailTemplateFragment) {

        this.adapter = adapter;
        this.activity = activity;
        this.team = team;
        this.fragmentManager = fragmentManager;
        this.listener = new EditorialDetailsPageSelected();
        this.editorialDetailTemplateFragment = editorialDetailTemplateFragment;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        if ( position % getTeamSize() >= getTeamSize() ) {
            return;
        }
        Team selectedTeam = adapter.getTeams().get(position % getTeamSize());
        if (team != null && selectedTeam != null) {
            //Timber.d("onPageSelected() team: %s, selectedTeam: %s", team.getTeamId(), selectedTeam.getTeamId());
            if (StringUtils.equalsIgnoreCase(team.getTeamId(), selectedTeam.getTeamId())){
                Timber.d("onPageSelected() team: %s", team.getTeamId());
                if (listener != null && editorialDetailsFeed != null && isFragmentAtTop()){
                   listener.pageSelected(editorialDetailsFeed, team);
                }
            }
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    private int getTeamSize(){
        return adapter.getTeams().size();
    }

    public void setEditorialDetailsFeed(EditorialDetailsFeed editorialDetailsFeed) {
        this.editorialDetailsFeed = editorialDetailsFeed;
    }

    public void onPageSelected() {
        Team selectedTeam = ((MainActivity) activity).getTeamManager().getSelectedTeam();
        if (StringUtils.equalsIgnoreCase(team.getTeamId(), selectedTeam.getTeamId())){
            Timber.d("onPageSelected() team: %s", team.getTeamId());
            if (listener != null && editorialDetailsFeed != null){
                listener.pageSelected(editorialDetailsFeed, team);
            }
        }
    }

    private boolean isFragmentAtTop(){
        int fragmentSize = fragmentManager.getFragments().size();
        Fragment topFragment = fragmentManager.getFragments().get(fragmentSize - 1);
        return editorialDetailTemplateFragment == topFragment;
    }
}
