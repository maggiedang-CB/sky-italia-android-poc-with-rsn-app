package com.nbcsports.regional.nbc_rsn.lifecycle;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import com.nbcsports.regional.nbc_rsn.common.components.ViewHolderPersistentPlayerMedium;
import com.nbcsports.regional.nbc_rsn.data_menu.datamenuftue.DataMenuFtueManager;
import com.nbcsports.regional.nbc_rsn.fabigation.fabtapftue.FabTapFtueManager;
import com.nbcsports.regional.nbc_rsn.persistentplayer.view.progressbar.PersistentPlayerProgressBarManager;
import com.nbcsports.regional.nbc_rsn.team_feed.auto_refresh.AutoRefreshManager;
import com.nbcsports.regional.nbc_rsn.team_feed.template.TeamFeedFragment;

import lombok.Getter;
import lombok.Setter;

public class ApplicationLifecycleListener implements LifecycleObserver {

    private boolean hasGoneToBackground = false;

    @Getter
    private TeamFeedFragment teamFeedFragment;

    @Getter @Setter
    private ViewHolderPersistentPlayerMedium previousViewHolderPersistentPlayerMedium;

    public ApplicationLifecycleListener(TeamFeedFragment teamFeedFragment) {
        this.teamFeedFragment = teamFeedFragment;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onMoveToForeground() {
        System.out.println("This is the enter point: RSN app move to foreground");
        if (hasGoneToBackground){
            if (teamFeedFragment != null){
                // Set up live assets auto refresh disposable observer
                // and team feed auto refresh disposable observer again
                teamFeedFragment.refreshAndResetAutoRefresh();
                // Set up fab tap ftue disposable observer again
                teamFeedFragment.startFabTapFtue();
                // Set up data menu ftue disposable observer again
                teamFeedFragment.startDataMenuFtue();
            }
            hasGoneToBackground = false;
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onMoveToBackground() {
        System.out.println("This is the enter point: RSN app move to background");
        // Should remove the auto refresh listener here?
        hasGoneToBackground = true;
        // Remove all live assets auto refresh disposable observer
        // and team feed auto refresh disposable observer
        // Because when app is in background, nothing should be refreshed
        AutoRefreshManager.removeAllObservers();
        // Remove all progress bar disposable observers
        // Because when app is in background, progress bar
        // should not be updated
        PersistentPlayerProgressBarManager.removeAllObservers();
        // Remove all fab tap ftue disposable observers
        // Because when app is in background, no fab tap ftue should be shown
        FabTapFtueManager.Companion.removeAllObservers();
        // Remove all data menu ftue disposable observers
        // Because when app is in background, no data menu ftue should be shown
        DataMenuFtueManager.Companion.removeAllObservers();
    }

}
