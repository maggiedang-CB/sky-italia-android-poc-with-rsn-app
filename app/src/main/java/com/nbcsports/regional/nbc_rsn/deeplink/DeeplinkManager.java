package com.nbcsports.regional.nbc_rsn.deeplink;

import android.content.Intent;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.nbcsports.regional.nbc_rsn.MainActivity;
import com.nbcsports.regional.nbc_rsn.common.TeamManager;
import com.nbcsports.regional.nbc_rsn.navigation.NavigationManager;
import com.nbcsports.regional.nbc_rsn.team_feed.template.TeamFeedFragment;
import com.nbcsports.regional.nbc_rsn.common.FeedComponent;
import com.nbcsports.regional.nbc_rsn.urban_airship.NotificationsManagerKt;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.Getter;

import static com.nbcsports.regional.nbc_rsn.deeplink.Deeplink.ACTION_LINK_TO;
import static com.nbcsports.regional.nbc_rsn.deeplink.Deeplink.ACTION_OPEN;
import static com.nbcsports.regional.nbc_rsn.deeplink.DeeplinkManager.State.COMPLETE;
import static com.nbcsports.regional.nbc_rsn.deeplink.DeeplinkManager.State.FREE;

/***
 * Deeplink flow:
 *
 * 1. EntryActivity.onCreate()
 *  if Intent is valid:
 *      set State = PENDING
 *      set deeplinkIntent = getIntent()
 *
 * 2. MainActivity.onResume() or MainActivity.onSplashScreenComplete() - (in the method that occurs later)
 *   if State = PENDING and Config != null:
 *       set State = IN_PROGRESS
 *       deeplink = createDeeplinkFromIntent()
 *   MainActivity.goToSelectedTeam()
 *
 *   if Deeplink type == TEAM:
 *       set State = COMPLETE
 *
 * 3. (if Deeplink type == COMPONENT)
 *   TeamViewComponentsAdapter.onAttachedToRecyclerView()
 *       -> TeamFeedFragment.onAdapterAttachedToRecyclerView()
 *           -> TeamFeedFragment.deeplinkToComponent()
 *               -> DeeplinkManager.doTeamViewDeeplink() || DeeplinkManager.doEditorialDeeplink()
 *
 */
public class DeeplinkManager {
    /***
     * State:
     *
     *  FREE         - There is no deeplink that needs to be executed. (Default value for deeplinkState)
     *
     *  PENDING      - this.deeplink is waiting to be executed. EntryActivity has recognized a
     *                  valid Deeplink Intent.
     *
     *  IN_PROGRESS  - this.deeplink is currently being executed.
     *
     *  COMPLETE     - this.deeplink has succeeded or failed. Once deeplinkState is set to null,
     *                  deeplinkState will automatically be set to FREE.
     */
    public enum State {
        FREE, PENDING, IN_PROGRESS, COMPLETE
    }

    private State deeplinkState = FREE;

    /***
     * The Intent object that the Deeplink Object is derived from. This Intent is parsed and a
     *  Deeplink is created.
     */
    private Intent deeplinkIntent;

    /***
     * max. value that teamViewDeeplinkAttempts and openArticleAttempts can reach. this value is
     *  equal to the number of active TeamFeedFragments at once
     */
    private static final int MAX_CONCURRENT_DEEPLINK_ATTEMPTS = 3;

    /***
     * tracker for the number of times of doTeamViewDeeplink() is called. resets to 0 after
     *  reaching MAX_CONCURRENT_DEEPLINK_ATTEMPTS
     */
    private AtomicInteger teamViewDeeplinkAttempts = new AtomicInteger(0);

    /***
     * tracker for the number of times of doEditorialDeeplink() is called. resets to 0 after
     *  reaching MAX_CONCURRENT_DEEPLINK_ATTEMPTS
     */
    private AtomicInteger openArticleAttempts = new AtomicInteger(0);

    /***
     * Deeplink that is currently/will be executed by this DeeplinkManager
     */
    @Getter
    private Deeplink deeplink;

    private static DeeplinkManager deeplinkManager = null;

    private DeeplinkManager() {
    }

    public static DeeplinkManager getInstance() {
        if (deeplinkManager == null) {
            deeplinkManager = new DeeplinkManager();
        }
        return deeplinkManager;
    }

    public static void release() {
        deeplinkManager = null;
    }

    public void setState(State state){
        deeplinkState = state;
        switch (deeplinkState){

            case FREE:
                // do nothing
                break;

            case PENDING:
                // do nothing
                break;

            case IN_PROGRESS:
                Deeplink deeplink = Deeplink.getDeeplinkFromIntent(deeplinkIntent);
                if (deeplink != null
                        && deeplink.getTeam() != null
                        && TeamManager.Companion.getInstance() != null
                        && TeamManager.Companion.getInstance().getUsersTeams().contains(deeplink.getTeam())){
                    this.deeplink = deeplink;
                    TeamManager.Companion.getInstance().setSelectedTeam(deeplink.getTeam());
                } else {
                    setState(COMPLETE);
                }
                break;

            case COMPLETE:
                this.deeplink = null;
                this.deeplinkIntent = null;
                NotificationsManagerKt.INSTANCE.hideBanner(); // hide banner if showing
                // reset to state FREE
                setState(FREE);
                break;
        }
    }

    public boolean isState(State state){
        return deeplinkState == state;
    }

    /***
     *
     * @param intent - Possible Deeplink Intent
     * @return true if valid Deeplink Intent and successfully set, false otherwise.
     */
    public boolean setDeeplinkIntent(Intent intent){
        if (intent == null
                || intent.getData() == null
                || intent.getAction() == null
                || !intent.getAction().equals(Intent.ACTION_VIEW)) {
            return false;
        }
        this.deeplinkIntent = intent;
        return true;
    }

    /***
     * If a Deeplink of Type.TEAM is in-progress, then this method will be called 3 times by
     *  each of the TeamFeedFragments that are active.
     * DeeplinkManager's State is set to COMPLETE once all three TeamFeedFragments
     *  have called this method.
     *
     * @param teamFeedFragment - One of three TeamFeedFragments that are active.
     *  [TeamFeedFragment #1 | TeamFeedFragment #2 (Visible to the user) | TeamFeedFragment #3]
     */
    public void doTeamViewDeeplink(TeamFeedFragment teamFeedFragment){

        // close all editorial detail articles and scroll to the top to view live video
        if (isValidDeeplinkParams(teamFeedFragment)){
            teamFeedFragment.closeAllPages(false);
            teamFeedFragment.getRecyclerView().smoothScrollToPosition(0);
        }

        if (teamViewDeeplinkAttempts.addAndGet(1) == MAX_CONCURRENT_DEEPLINK_ATTEMPTS) {
            teamViewDeeplinkAttempts.set(0);
            setState(COMPLETE);
        }
    }

    /***
     * If a Deeplink of Type.COMPONENT is in-progress, then this method will be called 3 times by
     *  each of the TeamFeedFragments that are active.
     * DeeplinkManager's State is set to COMPLETE once all three TeamFeedFragments
     *  have called this method.
     *
     * @param teamFeedFragment - One of three TeamFeedFragments that are active.
     *  [TeamFeedFragment #1 | TeamFeedFragment #2 (Visible to the user) | TeamFeedFragment #3]
     *
     */
    public void doEditorialDeeplink(TeamFeedFragment teamFeedFragment) {

        if (isValidDeeplinkParams(teamFeedFragment)
                && teamFeedFragment.getFeedComponents() != null
                && !teamFeedFragment.getFeedComponents().isEmpty()
                && teamFeedFragment.getFeedComponentIndex(deeplink.getComponentId()) != -1) {

            List<FeedComponent> componentsList = teamFeedFragment.getFeedComponents();
            int componentIndex = teamFeedFragment.getFeedComponentIndex(deeplink.getComponentId());

            if (deeplink.isAction(ACTION_LINK_TO)) {

                // scrolls to the component
                LinearLayoutManager layoutManager = (LinearLayoutManager)
                        teamFeedFragment.getRecyclerView().getLayoutManager();
                layoutManager.scrollToPositionWithOffset(componentIndex, 0);

            } else if (deeplink.isAction(ACTION_OPEN)) {

                // opens up the editorial fragment with relevant component
                NavigationManager.getInstance().openCommonCardDetailsScreen(
                        teamFeedFragment.getBackgroundFadeTo(),
                        teamFeedFragment.getChildFragmentManager(),
                        deeplink.getTeam(),
                        componentsList,
                        componentIndex,
                        teamFeedFragment);
            }
        }

        if (openArticleAttempts.addAndGet(1) == MAX_CONCURRENT_DEEPLINK_ATTEMPTS){
            openArticleAttempts.set(0);
            setState(COMPLETE);
        }
    }

    private boolean isValidDeeplinkParams(TeamFeedFragment teamFeedFragment){
        return  deeplink != null
                && deeplink.getTeam() != null
                && teamFeedFragment != null
                && teamFeedFragment.getTeam() != null
                && teamFeedFragment.getTeam().equals(deeplink.getTeam())
                && teamFeedFragment.getRecyclerView() != null
                && teamFeedFragment.getRecyclerView().getAdapter() != null;
    }

    /***
     * Creates an Intent derived from the Deeplink object for the MainActivity to execute. This
     *  Intent starts the EntryActivity.
     *
     * @param deeplink - Deeplink that is required to execute.
     * @param mainActivity - MainActivity that will start the Intent
     */
    public void beginDeeplinkFromSwitchScreen(Deeplink deeplink, MainActivity mainActivity){
        Intent deeplinkIntent = Deeplink.getIntentFromDeeplink(mainActivity, deeplink);
        if (deeplink != null){
            mainActivity.startActivity(deeplinkIntent);
        }
    }
}