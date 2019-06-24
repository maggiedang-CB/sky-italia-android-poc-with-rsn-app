package com.nbcsports.regional.nbc_rsn.common.components;

import android.graphics.Color;
import androidx.viewpager.widget.ViewPager;
import android.view.View;

import com.nbcsports.regional.nbc_rsn.MainActivity;
import com.nbcsports.regional.nbc_rsn.R;
import com.nbcsports.regional.nbc_rsn.common.MediaSource;
import com.nbcsports.regional.nbc_rsn.common.Team;
import com.nbcsports.regional.nbc_rsn.persistentplayer.PlayerConstants;
import com.nbcsports.regional.nbc_rsn.persistentplayer.layouts.Medium;
import com.nbcsports.regional.nbc_rsn.team_feed.components.FragmentLifeCycleListener;
import com.nbcsports.regional.nbc_rsn.team_feed.components.ViewHolderTypeBase;

import butterknife.BindView;
import butterknife.ButterKnife;

import static androidx.viewpager.widget.ViewPager.SCROLL_STATE_DRAGGING;
import static androidx.viewpager.widget.ViewPager.SCROLL_STATE_IDLE;
import static androidx.viewpager.widget.ViewPager.SCROLL_STATE_SETTLING;

public class ViewHolderPersistentPlayerMedium extends ViewHolderTypeBase
        implements FragmentLifeCycleListener, ViewPager.OnPageChangeListener {

    private final View view;
    private final ViewPager viewPager;
    private final Interface teamViewComponentsAdapter;
    private MediaSource mediaSource;
    protected boolean wasVisible;

    @BindView(R.id.persistent_player_medium)
    protected
    Medium medium;
    private boolean wasSettling;
    private boolean wasDragging;

    public ViewHolderPersistentPlayerMedium(FragmentLifeCycleListener.Interface teamViewComponentsAdapter, View view, ViewPager viewPager, int itemViewType) {
        super(view, itemViewType);
        ButterKnife.bind(this, view);
        this.view = view;
        this.viewPager = viewPager;
        this.teamViewComponentsAdapter = teamViewComponentsAdapter;
        teamViewComponentsAdapter.addFragmentLifeCycleListener(this);
    }

    public void setMediaSource(MediaSource mediaSource) {
        if (mediaSource == null || mediaSource.getImage() == null) return;
        this.mediaSource = mediaSource;
        medium.setMediaSource(mediaSource);
    }

    public void setTeamPrimaryColorToMediumView() {
        int teamPrimaryColor = Color.parseColor(getTeam().getPrimaryColor());
        medium.setTeamPrimaryColor(teamPrimaryColor);
    }

    @Override
    public void setVisible(boolean visible) {

        Team selectedTeam = ((MainActivity) view.getContext()).getTeamManager().getSelectedTeam();
        if (getTeam() == selectedTeam) {
            medium.show(visible);
        }

        wasVisible = visible;
    }

    @Override
    public void onResume() {

    }

    @Override
    public void onPause() {
        if (wasVisible) {
            medium.hide();
        }
    }

    protected Team getTeam(){
        return mItem.getTeam();
    }

    @Override
    public void addPageChangeListener() {
        super.addPageChangeListener();
        viewPager.addOnPageChangeListener(this);
    }

    @Override
    public void removePageChangeListener() {
        super.removePageChangeListener();
        viewPager.removeOnPageChangeListener(this);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {

        Team selectedTeam = ((MainActivity) view.getContext()).getTeamManager().getSelectedTeam();
        Team teamInMedium = getTeam();
        //medium.getPersistentPlayer().log(this, "onPageSelected team: %s, selectedTeam: %s", getTeam().getDisplayName(), selectedTeam.getDisplayName());
        if (selectedTeam != teamInMedium){

        } else {
            if (mItem != null) {
                medium.setMediaSource(mItem.getMediaSource());
            }
            medium.show(true);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

        Team selectedTeam = ((MainActivity) view.getContext()).getTeamManager().getSelectedTeam();
        Team teamInMedium = getTeam();
        boolean teamIsInView = selectedTeam == teamInMedium;

        switch (state) {
            case SCROLL_STATE_DRAGGING: // 1
                wasDragging = true;
                if (teamIsInView) {
                    onPause();
                }
                break;
            case SCROLL_STATE_SETTLING: // 2
                wasSettling = true;
                if (teamIsInView) {
                    onPause();
                }
                break;
            case SCROLL_STATE_IDLE: // 0
                if (wasDragging) {
                    // 1 -> 0
                    medium.getPersistentPlayer().setPlayWhenReady(medium.getPersistentPlayer().isPlayWhenReady());
                } else if (wasSettling){
                    // 2 -> 0
                    if (medium.getPersistentPlayer().getType() == PlayerConstants.Type.MEDIUM) {
                        medium.getPersistentPlayer().setPlayWhenReady(medium.getPersistentPlayer().isPlayWhenReady());
                    }
                }
                wasSettling = false;
                wasDragging = false;

                // since multiple medium can exist (teams that have live game, hero videos)
                if (selectedTeam != teamInMedium) {
                    // if the medium player is no longer in view, we need to call medium.hide() to ensure
                    // all the logic in hide() is triggered, just like when medium is scrolled away
                    medium.getPersistentPlayer().log(this, "onPageScrollStateChanged [NOT IN VIEW] teamInMedium: %s", teamInMedium.getDisplayName());
                    medium.hide();
                    if (medium.getPersistentPlayer().getType() == PlayerConstants.Type.MEDIUM) {
                        // we also release the player because user is not interested in watching the player anymore
                        // this is important for analytics that is listening to release() because this will cause
                        // the video session to be reported as closed.
                        // And we only do this for MEDIUM because MINI is still playing when swiping
                        medium.getPersistentPlayer().release();
                    }
                } else {
                    // if medium is in view, we just play the video
                    medium.getPersistentPlayer().log(this, "onPageScrollStateChanged [IN VIEW] teamInMedium: %s", teamInMedium.getDisplayName());
                    if (mItem != null) {
                        medium.setMediaSource(mItem.getMediaSource());
                    }
                    medium.show(true);
                }
                break;
        }
    }
}
