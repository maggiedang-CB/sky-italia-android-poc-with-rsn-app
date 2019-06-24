package com.nbcsports.regional.nbc_rsn.editorial_detail;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.clearbridge.pull_to_refresh.ExitLayout;
import com.nbcsports.regional.nbc_rsn.MainContract;
import com.nbcsports.regional.nbc_rsn.MainPresenter;
import com.nbcsports.regional.nbc_rsn.R;
import com.nbcsports.regional.nbc_rsn.analytics.TrackingHelper.PageInfo;
import com.nbcsports.regional.nbc_rsn.analytics.TrackingHelper.TrackingHelper;
import com.nbcsports.regional.nbc_rsn.common.BaseFragment;
import com.nbcsports.regional.nbc_rsn.common.Config;
import com.nbcsports.regional.nbc_rsn.common.Constants;
import com.nbcsports.regional.nbc_rsn.common.RecyclerVisibilityScrollListener;
import com.nbcsports.regional.nbc_rsn.common.Team;
import com.nbcsports.regional.nbc_rsn.data_bar.DataBarScrollListener;
import com.nbcsports.regional.nbc_rsn.editorial_detail.models.EditorialDetailItem;
import com.nbcsports.regional.nbc_rsn.localization.LocalizationManager;
import com.nbcsports.regional.nbc_rsn.stepped_story.SteppedStoryFragment;
import com.nbcsports.regional.nbc_rsn.team_feed.template.TeamFeedFragment;
import com.nbcsports.regional.nbc_rsn.common.FeedComponent;
import com.nbcsports.regional.nbc_rsn.utils.ColorUtil;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import lombok.Getter;
import timber.log.Timber;

import static com.nbcsports.regional.nbc_rsn.navigation.NavigationManager.ARGUMENT_SELECTED_FEED_COMPONENT_POSITION;
import static com.nbcsports.regional.nbc_rsn.navigation.NavigationManager.ARGUMENT_TEAM;
import static com.nbcsports.regional.nbc_rsn.navigation.NavigationManager.ARGUMENT_TEAM_FEED_COMPONENTS_LIST;

public class EditorialDetailTemplateFragment extends BaseFragment implements EditorialDetailContract.View {
    private EditorialDetailComponentsAdapter editorialDetailComponentsAdapter;

    private boolean whiteFooter = true; //Color of the final element of the list.
    private final String BLACK_PEACOCK_ANIMATION = "peacock_animation_black.json";
    private final String WHITE_PEACOCK_ANIMATION = "peacock_animation_white.json";

    private final static String BR_TAG = "<br>";
    private final static String P_TAG = "</*p>";
    private final static String NEW_LINE_CHAR_PATTERN = "(\r)?\n";

    private final float EXIT_ANIM_MIN_PROGRESS = 0.08f;
    private final float EXIT_ANIM_MAX_PROGRESS = 0.44f;
    private final int ON_EXIT_ALPHA_ANIM_DUR = 500;

    @Getter
    private String componentId;

    private ArrayList<FeedComponent> teamFeedComponentList;

    @Getter
    public int selectedFeedComponentPosition;
    public int upNextFeedComponentPosition;

    private GradientDrawable teamColorGradient;

    @Getter
    @BindView(R.id.item_list)
    public ExitLayout exitLayout;

    @BindView(R.id.collapsed_header_state)
    FrameLayout collapsedHeaderState;

    @BindView(R.id.collapsed_hero_title)
    TextView collapsedHeroTitle;

    @Getter
    @BindView(R.id.editorial_background_to_fade_to)
    LinearLayout backgroundFadeTo;

    public long collapsedHeaderStateAnimationDuration;
    boolean isCollapsedHeaderStateHidden = false;
    private RecyclerVisibilityScrollListener scrollListener;
    private ViewPager teamsPager;
    private EditorialDetailsPageChangeListener editorialDetailsPageChangeListener;

    private EditorialDetailPresenter presenter;
    private TeamFeedFragment.DataBarScrolling callback;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the fragment.
     */
    public EditorialDetailTemplateFragment() {
    }

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        if (nextAnim != 0) {
            // (re)set collapsedHeaderState visibility to INVISIBLE, until fragment-show animation ends
            if (collapsedHeaderState != null) { //TODO: Update
                collapsedHeaderState.setVisibility(View.INVISIBLE);
            }

            Animation anim = AnimationUtils.loadAnimation(getActivity(), nextAnim);

            anim.setAnimationListener(new Animation.AnimationListener() {

                @Override
                public void onAnimationStart(Animation animation) {
                    //Log.d(TAG, "Animation started.");
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                    //Log.d(TAG, "Animation repeating.");
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    //Log.d(TAG, "Animation ended.");

                    // initially, have the collapsedHeaderState hidden
                    if (collapsedHeaderState != null) { //TODO: Update
                        hideCollapsedHeaderState(true, 0L);
                        // set collapsedHeaderState visible,
                        // while it's still hidden (i.e. animated out of screen)

                        collapsedHeaderState.postDelayed(() -> {
                            if (collapsedHeaderState != null) { //Check again after delay TODO: Update
                                collapsedHeaderState.setVisibility(View.VISIBLE);
                            }
                        }, 100);
                    }
                }
            });
            return anim;
        } else {
            return null;
        }
    }

    private void setupExitAnimation() {
        if (exitLayout == null) return;

        exitLayout.addLottieAnimation(
                whiteFooter ? BLACK_PEACOCK_ANIMATION : WHITE_PEACOCK_ANIMATION,
                true,
                EXIT_ANIM_MIN_PROGRESS,
                EXIT_ANIM_MAX_PROGRESS);
        exitLayout.setOffset(100);
        if (LocalizationManager.isInitialized()) {
            exitLayout.setText(LocalizationManager.Common.Close);
        }
        exitLayout.setTextColor(whiteFooter ? Color.BLACK : Color.WHITE);
        exitLayout.setBackgroundColor(whiteFooter ? Color.WHITE : Color.BLACK);
        exitLayout.setOnExitListener(() -> {
            //OnExit()
            exitLayout.setIconAlpha(0.0f, ON_EXIT_ALPHA_ANIM_DUR);
            exitLayout.setTextViewAlpha(0.0f, ON_EXIT_ALPHA_ANIM_DUR);

            doExit(true);
        });
    }

    @Override
    public void doExit(boolean animated) {
        if (editorialDetailComponentsAdapter != null) {
            editorialDetailComponentsAdapter.onDestroy();
        }

        Fragment parentFragment = getParentFragment();
        if (parentFragment instanceof TeamFeedFragment) {
            ((TeamFeedFragment) parentFragment).closePage(animated);
        }
    }

    @Override
    public int getLayout() {
        return R.layout.editorial_detail_components_list;
    }

    RecyclerView.OnScrollListener headerStateScrollListener = new RecyclerView.OnScrollListener() {
        private int firstVisibleItemPosition = -1;
        private int directionY = 0;

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            directionY = dy;
            if (directionY > 0) {
                // Scrolling up,
                // show collapsedHeaderState
                if (collapsedHeaderState != null) { //TODO: update
                    hideCollapsedHeaderState(false, collapsedHeaderStateAnimationDuration);
                }
            } else {
                // Scrolling down,
                // hide collapsedHeaderState
                if (firstVisibleItemPosition == 0) {
                    // The editorialDetailItem #0 is in of the visible area, so we can hide the collapsed header
                    if (collapsedHeaderState != null) {  //TODO: update
                        hideCollapsedHeaderState(true, collapsedHeaderStateAnimationDuration);
                    }
                }
            }
        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);

            LinearLayoutManager lm = ((LinearLayoutManager) recyclerView.getLayoutManager());
            firstVisibleItemPosition = lm.findFirstVisibleItemPosition();

            if (newState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
                // Do nothing
            } else if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                // Do nothing
            } else if (newState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                if (directionY <= 0) {
                    // We were scrolling down,
                    // hide collapsedHeaderState
                    if (firstVisibleItemPosition == 0) {
                        // The editorialDetailItem #0 is in of the visible area, so we can hide the collapsed header
                        if (collapsedHeaderState != null) {  //TODO: update
                            hideCollapsedHeaderState(true, collapsedHeaderStateAnimationDuration);
                        }
                    }
                }
            } else {
                // Should not happen
            }
        }
    };

    @Override
    public void onViewCreated(@Nullable View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MainContract.View mainActivity = MainPresenter.Injection.provideView(getActivity());
        Config config = mainActivity.getMainPresenter().getLastKnownConfig();
        new EditorialDetailPresenter(this, config);

        setupExitAnimation();

        // get the default animation duration of collapsedHeaderState, in case if we want to change and restore it
        collapsedHeaderStateAnimationDuration = collapsedHeaderState.animate().getDuration();
        exitLayout.getRecyclerView().addOnScrollListener(headerStateScrollListener);

        if (callback != null) {
            DataBarScrollListener dataBarScrollListener = new DataBarScrollListener(callback);
            exitLayout.getRecyclerView().addOnScrollListener(dataBarScrollListener);
        }

        scrollListener = new RecyclerVisibilityScrollListener();
        exitLayout.getRecyclerView().addOnScrollListener(scrollListener);

        Team team = getArguments() != null ? getArguments().getParcelable(ARGUMENT_TEAM) : null;
        presenter.setTeam(team);

        //teamViewFeed = getArguments() != null ? getArguments().getParcelable(ARGUMENT_TEAM_VIEW_FEED) : null;
        teamFeedComponentList = getArguments() != null ? getArguments().getParcelableArrayList(ARGUMENT_TEAM_FEED_COMPONENTS_LIST) : null;
        presenter.setTeamFeedComponentList(teamFeedComponentList);

        selectedFeedComponentPosition = getArguments() != null ? getArguments().getInt(ARGUMENT_SELECTED_FEED_COMPONENT_POSITION) : -1;
        presenter.setSelectedFeedComponentPosition(selectedFeedComponentPosition);

        FeedComponent selectedFeedComponent = teamFeedComponentList.get(selectedFeedComponentPosition);
        componentId = selectedFeedComponent != null ? selectedFeedComponent.getComponentId() : "";
        presenter.setComponentId(componentId);

        presenter.getTeamviewItemData();

        Timber.d("onViewCreated()");
        if (getParentFragment() instanceof TeamFeedFragment) {
            TeamFeedFragment teamFeedFragment = (TeamFeedFragment) getParentFragment();
            teamsPager = teamFeedFragment.getViewPager();
            editorialDetailsPageChangeListener = new EditorialDetailsPageChangeListener(
                    teamFeedFragment.getAdapter(),
                    getActivity(),
                    team,
                    teamFeedFragment.getChildFragmentManager(),
                    this
            );
        }
    }

    public void hideCollapsedHeaderState(boolean hide, long duration) {
        int direction = -1; // -1 means 'up', 1 means 'down'
        if (hide) { // hide
            if (!isCollapsedHeaderStateHidden) {
                // slide the collapsedHeaderState to the 'direction', by its height
                collapsedHeaderState.animate().translationY(direction * collapsedHeaderState.getHeight())
                        .setDuration(duration)
                        .start(); //?
                isCollapsedHeaderStateHidden = true;
            }
        } else { // show
            if (isCollapsedHeaderStateHidden) {
                // slide the collapsedHeaderState back to its original position
                collapsedHeaderState.animate().translationY(0)
                        .setDuration(duration)
                        .start(); //?
                isCollapsedHeaderStateHidden = false;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (editorialDetailComponentsAdapter != null) {
            editorialDetailComponentsAdapter.onResume();
        }

        TrackingHelper.Companion.trackPageEvent(getPageInfo());

        Timber.d("onResume()");
        addTeamsOnPageChangeListener();
    }

    private void addTeamsOnPageChangeListener() {
        if (teamsPager != null)
            teamsPager.addOnPageChangeListener(editorialDetailsPageChangeListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (editorialDetailComponentsAdapter != null) {
            editorialDetailComponentsAdapter.onPause();
        }
        Timber.d("onPause()");
        removeTeamsOnPageChangeListener();
    }

    private void removeTeamsOnPageChangeListener() {
        if (teamsPager != null)
            teamsPager.removeOnPageChangeListener(editorialDetailsPageChangeListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (editorialDetailComponentsAdapter != null) {
            editorialDetailComponentsAdapter.onDestroy();
        }
        Timber.d("onDestroy()");
        if (presenter != null) presenter.unsubscribe();

        // "unsave" the saved scrubber position
        MainContract.View mainActivity = MainPresenter.Injection.provideView(getActivity());
        if(mainActivity.getPersistentPlayer() != null) {
            mainActivity.getPersistentPlayer().resetSavedScrubberPosition();
        }

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // With delay because
        // 1. There is isCurrentSteppedFragment() check in showDataBar(), which means
        //    there is race condition (due to the fragment transition animation)
        Observable.timer(600L, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> showDataBar());

        if (getParentFragment() instanceof TeamFeedFragment
                && getParentFragment().getParentFragment() != null
                && getParentFragment().getParentFragment() instanceof TeamFeedFragment.DataBarScrolling
        ) {
            callback = (TeamFeedFragment.DataBarScrolling) (getParentFragment().getParentFragment());
        }
    }

    @Override
    public void onDetach() {
        // With delay because
        // 1. There is isCurrentSteppedFragment() check, which means there is race condition
        //    (due to the fragment transition animation)
        // 2. There is isCurrentSteppedFragment() check in showDataBar() also
        // TODO: When databar is required in SteppedStoryFragment, instead of hiding databar,
        // TODO: do databar modification here
        Observable.timer(600L, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> {
                    if (isCurrentSteppedFragment()) {
                        hideDataBar();
                    } else {
                        showDataBar();
                    }
                    // Using ParentFragment here instead of this Fragment
                    // Because at this point, this.getActivity is equal to null
                    if (presenter != null && getParentFragment() != null){
                        presenter.setAppropriateStatusBarColor(getParentFragment().getActivity(), isCurrentSteppedFragment());
                    }
                });
        super.onDetach();
    }

    @Override
    public void showTeamviewListItems(@NonNull Team team,
                                      @NonNull ArrayList<FeedComponent> teamFeedComponentList,
                                      @NonNull List<EditorialDetailItem> items) {
        //// Set color of the collapsed header
        //int intPrimaryColor = Color.parseColor(team.getPrimaryColor());
        //collapsedHeaderState.setBackgroundColor(intPrimaryColor);

        // Determine and set collapsed header's title - before we add any header, footer, etc, to the list of items.
        if (items.size() > 0) {
            int firstItemPosition = 0;
            EditorialDetailItem.Type type = items.get(firstItemPosition).getType();
            if (type == EditorialDetailItem.Type.COMPONENT && collapsedHeroTitle != null) {
                // Remove html tags from display text
                String rawDisplayText = items.get(firstItemPosition).getHeroText();
                rawDisplayText = rawDisplayText.replaceAll(NEW_LINE_CHAR_PATTERN, "")
                        .replaceAll(BR_TAG, "").replaceAll(P_TAG, "");
                collapsedHeroTitle.setText(rawDisplayText);
            }
        }

        // Add header?
        boolean addHeader = false; //= true;
        if (addHeader) {
            EditorialDetailItem header = new EditorialDetailItem(EditorialDetailItem.Type.HEADER);
            if (EditorialDetailItem.Type.COMPONENT == header.getType()) {
                throw new IllegalStateException("Wrong header item's type: EditorialDetailItem.Type.COMPONENT, expected: EditorialDetailItem.Type.HEADER");
            }
            header.set(team);
            items.add(0, header);
        }

        boolean addTestTweet = false; // testing set to true
        if (addTestTweet) {
            if (items.size() > 6) {
                EditorialDetailItem topItem = new EditorialDetailItem(EditorialDetailItem.Type.COMPONENT);
                topItem.setComponentType(Constants.EDITORIAL_COMPONENT_TYPE_EMBEDDED_TWEET);
                topItem.setTweetId("866812044166561799L");
                items.add(3, topItem);

                EditorialDetailItem middleItem = new EditorialDetailItem(EditorialDetailItem.Type.COMPONENT);
                middleItem.setComponentType(Constants.EDITORIAL_COMPONENT_TYPE_EMBEDDED_TWEET);
                middleItem.setTweetId("845851240630235136L");
                items.add(5, middleItem);

                EditorialDetailItem bottomItem = new EditorialDetailItem(EditorialDetailItem.Type.COMPONENT);
                bottomItem.setComponentType(Constants.EDITORIAL_COMPONENT_TYPE_EMBEDDED_TWEET);
                bottomItem.setTweetId("845847874629971968L");
                items.add(bottomItem);
            } else {
                EditorialDetailItem bottomItem = new EditorialDetailItem(EditorialDetailItem.Type.COMPONENT);
                bottomItem.setComponentType(Constants.EDITORIAL_COMPONENT_TYPE_EMBEDDED_TWEET);
                bottomItem.setTweetId("845847874629971968L");
                items.add(bottomItem);
            }
        }

        // add (an artificial) Recirculation item that let user go to the next card's editorial content
        boolean addRecirculationItem = true;
        boolean didAddRecirculationItem = false;
        if (addRecirculationItem) {
            // try to add a Recirculation item and set its content
            upNextFeedComponentPosition = selectedFeedComponentPosition + 1;
            if (upNextFeedComponentPosition < teamFeedComponentList.size()) {
                FeedComponent fc = teamFeedComponentList.get(upNextFeedComponentPosition);
                // Note, the FeedComponent.Type.FOOTER is the type of the very last item that is expected to be at teem feed list.
                // So in this card's editorial list, add a Recirculation item only if the next item in the teem feed list
                // is a real card, i.e. of type COMPONENT or PERSISTENT_PLAYER_MEDIUM (under index 0) or FEED_PROMO (under index 11 or last but before FOOTER),
                // but not of types HEADER, THEFEED_LABEL or FOOTER.

                //Find next none feed promo, componentId != "" and content type not empty feed to display
                //Reset position to redirect to f1 card
                while ((FeedComponent.Type.FEED_PROMO == fc.getType()) || (fc.getComponentId().isEmpty()) || (fc.getContentType().isEmpty())) {
                    if (upNextFeedComponentPosition < (teamFeedComponentList.size() - 1)) {
                        fc = teamFeedComponentList.get(++upNextFeedComponentPosition);
                    } else {
                        upNextFeedComponentPosition = 0;
                    }
                }
                if (FeedComponent.Type.HEADER != fc.getType() && FeedComponent.Type.THEFEED_LABEL != fc.getType()
                        && FeedComponent.Type.FOOTER != fc.getType()) {
                    EditorialDetailItem item = new EditorialDetailItem(EditorialDetailItem.Type.COMPONENT);
                    item.setComponentType(Constants.CARD_TYPE_Feed_Text_Only);
                    item.setHeroText(fc.getTitle());
                    item.setAuthor(fc.getAuthor());
                    item.setTag(fc.getTag());
                    //item.setComponentAssetUrl(fc.???);
                    item.setPublishedDate(fc.getPublishedDate());

                    items.add(item);
                    didAddRecirculationItem = true;
                }
            }
        }

        // Add footer?
        if (!didAddRecirculationItem) {
            EditorialDetailItem footer = new EditorialDetailItem(EditorialDetailItem.Type.FOOTER);
            footer.set(team);
            items.add(footer);
        }

        if (exitLayout == null) {
            return;
        }

        //  Get color as colored gradient to be applied on all f1 standard cards
        teamColorGradient = ColorUtil.INSTANCE.makeGradientDrawable(team.getPrimaryColor());

        setupRecyclerView(exitLayout.getRecyclerView(), items, team, teamFeedComponentList);
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView,
                                   List<EditorialDetailItem> items,
                                   Team team, ArrayList<FeedComponent> teamFeedComponentList) {
        if (items == null) return;

        if (editorialDetailComponentsAdapter == null) {
            ViewPager viewPager = null;
            if (getParentFragment() instanceof TeamFeedFragment) {
                TeamFeedFragment teamFeedFragment = (TeamFeedFragment) getParentFragment();
                viewPager = teamFeedFragment.getViewPager();
            }

            editorialDetailComponentsAdapter =
                    new EditorialDetailComponentsAdapter(
                            this, items, team, teamFeedComponentList,
                            teamColorGradient, viewPager
                    );
        }

        recyclerView.setAdapter(editorialDetailComponentsAdapter);
    }

    @OnClick(R.id.share_button_in_collapsed_header)
    public void share() {
        if (collapsedHeroTitle != null && collapsedHeroTitle.getText() != null) {
            presenter.share(collapsedHeroTitle.getText().toString());
        }
    }

    public FeedComponent getFeedComponent(int index) {
        return teamFeedComponentList.get(index);
    }

    @Override
    public PageInfo getPageInfo() {
        return presenter == null ? null : presenter.getPageInfo();
    }

    @Override
    public void setPresenter(@NotNull EditorialDetailPresenter editorialDetailPresenter) {
        presenter = editorialDetailPresenter;
    }

    @NotNull
    @Override
    public EditorialDetailsPageChangeListener getEditorialDetailsPageChangeListener() {
        return editorialDetailsPageChangeListener;
    }

    private boolean isCurrentSteppedFragment() {
        Fragment parentFragment = getParentFragment();
        if (parentFragment instanceof TeamFeedFragment) {
            Fragment fragment = parentFragment.getChildFragmentManager().findFragmentById(R.id.editorial_detail);
            return fragment instanceof SteppedStoryFragment;
        }
        return false;
    }

    private void showDataBar() {
        Fragment parentFragment = getParentFragment();
        if (parentFragment instanceof TeamFeedFragment) {
            ((TeamFeedFragment) parentFragment).showDataBar();
        }
    }

    private void hideDataBar() {
        Fragment parentFragment = getParentFragment();
        if (parentFragment instanceof TeamFeedFragment) {
            ((TeamFeedFragment) parentFragment).hideDataBar();
        }
    }
}
