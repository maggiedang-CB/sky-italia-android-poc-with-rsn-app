package com.nbcsports.regional.nbc_rsn.teamselector;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.graphics.Rect;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.PathInterpolator;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.jakewharton.rxbinding2.view.RxView;
import com.nbcsports.regional.nbc_rsn.MainActivity;
import com.nbcsports.regional.nbc_rsn.R;
import com.nbcsports.regional.nbc_rsn.analytics.TrackingHelper.PageInfo;
import com.nbcsports.regional.nbc_rsn.analytics.TrackingHelper.TrackingHelper;
import com.nbcsports.regional.nbc_rsn.common.BaseFragment;
import com.nbcsports.regional.nbc_rsn.common.Team;
import com.nbcsports.regional.nbc_rsn.common.TeamManager;
import com.nbcsports.regional.nbc_rsn.localization.LocalizationManager;
import com.nbcsports.regional.nbc_rsn.navigation.NavigationManager;
import com.nbcsports.regional.nbc_rsn.urban_airship.NotificationsManagerKt;
import com.nbcsports.regional.nbc_rsn.utils.DisplayUtils;
import com.nbcsports.regional.nbc_rsn.utils.FtueUtil;
import com.nbcsports.regional.nbc_rsn.utils.PreferenceUtils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import jp.wasabeef.blurry.Blurry;
import timber.log.Timber;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class TeamSelectorFragment extends BaseFragment implements TeamSelectorListener, IconTouchHelperCallback.EventCallback {

    @BindView(R.id.team_selector_layout)
    ConstraintLayout layout;

    @BindView(R.id.trash_container)
    FrameLayout trashContainer;

    @BindView(R.id.trashcan)
    View trashcan;

    @BindView(R.id.team_selection_recyclerview)
    RecyclerView selectTeamRecyclerView;

    @BindView(R.id.trashcan_hitbox)
    View hitbox;

    @BindView(R.id.hold_drag_label)
    TextView tvHoldDrag;

    @BindView(R.id.icon_background)
    View bgIcon;

    @BindView(R.id.icon_recyclerview)
    RecyclerView iconRecyclerView;

    @BindView(R.id.save_teams)
    TextView tvSaveTeams;

    // Bottom hitbox
    @BindView(R.id.hold_drag_line)
    View holdDragLine;

    @BindView(R.id.team_selector_loader_background_relative_layout)
    RelativeLayout loaderBackgroundRelativeLayout;

    @BindView(R.id.loading_text_view)
    TextView loadingTextView;

    @BindView(R.id.team_selector_loader_lottie_animation_view)
    LottieAnimationView loaderLottieAnimationView;

    @BindView(R.id.location_progress_circle)
    ProgressBar progressBar;

    private final long TRASH_ANIMATION = 100L;

    private ItemTouchHelper helper;

    private TeamIconAdapter iconAdapter;
    private TeamSelectionAdapter selectionAdapter;

    private boolean isBlurStarted;

    private int viewHeight = 0;

    private DisposableObserver<Long> loaderBackgroundAnimationObserver,
            loaderLottieAnimationObserver;

    @Override
    public int getLayout() {
        return R.layout.fragment_team_selector;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        clearTeamLoaderLoadingAnimation(hidden);
        if (!hidden) {
            updateTeamList();

            TrackingHelper.Companion.trackPageEvent(getPageInfo());
        }
    }

    public void updateTeamList() {
        if (iconAdapter == null || selectionAdapter == null) return;
        selectTeamRecyclerView.smoothScrollToPosition(0);

        TeamManager teamManager = ((MainActivity) getActivity()).getTeamManager();
        ArrayList<Team> usersTeams = teamManager.getUsersTeams();
        toggleOverlayVisibility(!usersTeams.isEmpty()); // show overlay if user has selected teams
        iconAdapter.setTeams(usersTeams);
        selectionAdapter.setSelectedTeams(usersTeams);
        selectionAdapter.setTeamMap(teamManager.getMasterHashMap());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        //this.trackingPageName = "TeamSelectorFragment";
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initIconRecyclerView();
        initSelectTeamRecyclerView();

        // Add Save Teams button
        compositeDisposable.add(getSaveTeamsDisposable());
        toggleOverlayVisibility(false); // default hide the overlay

        // Load team loader loading lottie animation
        loaderLottieAnimationView.setAnimation("peacock_animation_white.json");
    }

    private Disposable getSaveTeamsDisposable() {
        return RxView.clicks(tvSaveTeams)
                .debounce(500L, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        v -> saveTeams(),
                        e -> e.printStackTrace()
                );
    }

    private void saveTeams() {
        List<Team> newList = iconAdapter.getTeams();

        if (newList.size() > 0 && !isBlurStarted) {

            NotificationsManagerKt.INSTANCE.updateNotificationOptInStatuses(
                    ((MainActivity) getActivity()).getTeamManager().getUsersTeams(),
                    newList
            );

            // generate team list
            StringBuilder builder = new StringBuilder();
            for (Team t : newList) {
                builder.append(t.getTeamId()).append(",");
            }
            String s = builder.toString();
            Timber.e("saving teamIds %s", s);
            PreferenceUtils.INSTANCE.setString("teamlist", s);

            // update team manager
            Team selectedTeam = newList.get(0);
            if (newList.contains(((MainActivity) getActivity()).getTeamManager().getSelectedTeam()) &&
                    ((MainActivity) getActivity()).getTeamManager().getSelectedTeam() != null) {
                selectedTeam = ((MainActivity) getActivity()).getTeamManager().getSelectedTeam();
            }
            ((MainActivity) getActivity()).getTeamManager().setUserList(newList);
            ((MainActivity) getActivity()).getTeamManager().setSelectedTeam(selectedTeam);
            ((MainActivity) getActivity()).getDataBarManager().setTeams(newList);
            NavigationManager.getInstance().getTeamsPagerFragment().resetPagerAdapter();
            showTeamLoaderLoadingAnimation();
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initIconRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        iconRecyclerView.setLayoutManager(layoutManager);
        iconAdapter = new TeamIconAdapter();
        iconAdapter.setTeams(new ArrayList<>());
        iconRecyclerView.setAdapter(iconAdapter);

        ItemTouchHelper.Callback callback = new IconTouchHelperCallback(iconAdapter, hitbox, this);

        iconRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {

            private float startOffset = 0.031f;
            private float dividerOffset = 0.033f;

            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);

                int screenWidth = DisplayUtils.getScreenWidth(parent.getContext());
                int headMargin = (int) (screenWidth * startOffset);
                int dividerMargin = (int) (screenWidth * dividerOffset);
                int position = parent.getChildAdapterPosition(view);

                // item margin
                if (isRTL()) {
                    if (position == 0) {
                        outRect.left = dividerMargin;
                        outRect.right = headMargin;
                    } else {
                        outRect.left = dividerMargin;
                        outRect.right = 0;
                    }

                } else {
                    if (position == 0) {
                        outRect.left = headMargin;
                        outRect.right = dividerMargin;
                    } else {
                        outRect.left = 0;
                        outRect.right = dividerMargin;
                    }
                }
            }

            private boolean isRTL() {
                return PreferenceUtils.INSTANCE.getBoolean("_isFabRtl", false);
            }
        });

        helper = new ItemTouchHelper(callback);
        helper.attachToRecyclerView(iconRecyclerView);

        // enable/disable pass through touching
        iconRecyclerView.setOnTouchListener((v, event) -> {
            if (trashcan.getVisibility() != VISIBLE && event.getY() <= selectTeamRecyclerView.getHeight()) {
                selectTeamRecyclerView.dispatchTouchEvent(event);
                return true;
            }
            return false;
        });

        /*
         To solve the bug in RSNAPP-219/711 where the icon tray repositions incorrectly on S8/S8+,
         we're updating the icon adapter so it re-measures on every onBindViewHolder
          */
        iconRecyclerView.setOnSystemUiVisibilityChangeListener(visibility -> {
            if (getContext() != null) {
                int screenHeight = DisplayUtils.getScreenHeight(getContext());
                if (viewHeight != screenHeight) {
                    iconAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    private void initSelectTeamRecyclerView() {
        if (selectTeamRecyclerView == null) return;

        selectTeamRecyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()) {
            @Override
            public boolean canScrollVertically() {
                return progressBar != null && progressBar.getVisibility() == GONE;
            }
        });
        selectionAdapter = new TeamSelectionAdapter(this);
        selectTeamRecyclerView.setAdapter(selectionAdapter);

        int sideMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15, getResources().getDisplayMetrics());
        selectTeamRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                outRect.left = sideMargin;
                outRect.right = sideMargin;
            }
        });
        selectionAdapter.setSelectedTeams(((MainActivity) getActivity()).getTeamManager().getUsersTeams());
        selectionAdapter.setTeamMap(((MainActivity) getActivity()).getTeamManager().getMasterHashMap());
    }

    @Override
    public void onTeamDeselected(Team team) {
        Timber.e("deselected: %s", team.getDisplayName());
        iconAdapter.removeTeam(team);

        if (iconAdapter.getItemCount() == 0) {
            toggleOverlayVisibility(false);
        }
    }

    @Override
    public void onTeamSelected(Team team) {
        Timber.e("selected: %s", team.getTeamId());
        iconAdapter.insertItem(team);
        toggleOverlayVisibility(true);
    }

    @Override
    public void onStart(@org.jetbrains.annotations.Nullable RecyclerView.ViewHolder viewHolder) {

        trashcan.animate().alpha(1f).setDuration(TRASH_ANIMATION).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                trashcan.setVisibility(VISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        if (!isBlurStarted) {
            try {
                Blurry.with(getContext())
                        .radius(25)
                        .sampling(2)
                        .animate((int) TRASH_ANIMATION)
                        .onto(trashContainer);
            } catch (OutOfMemoryError ignored) {

            }

            isBlurStarted = true;
        }

    }

    @Override
    public void onDrop(RecyclerView.ViewHolder viewHolder, boolean shouldDelete) {

        trashcan.animate().alpha(0f).setDuration(TRASH_ANIMATION).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                trashcan.setVisibility(GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        if (isBlurStarted) {
            try {
                Blurry.delete(trashContainer);
            } catch (OutOfMemoryError ignored) {
           
            }
            isBlurStarted = false;
        }


        // delete this data
        if (shouldDelete) {
            iconAdapter.removeItem(viewHolder.getAdapterPosition());
            selectionAdapter.deselectTeam(((TeamIconAdapter.ViewHolder) viewHolder).getTeamData());
        }
    }

    private void toggleOverlayVisibility(boolean isVisible) {
        if ((iconRecyclerView.getVisibility() == VISIBLE && isVisible) ||
                (iconRecyclerView.getVisibility() == GONE && !isVisible)) return;

        tvSaveTeams.setVisibility(isVisible ? VISIBLE : GONE);
        iconRecyclerView.setVisibility(isVisible ? VISIBLE : GONE);
        holdDragLine.setVisibility(isVisible ? VISIBLE : GONE);
        tvHoldDrag.setVisibility(isVisible ? VISIBLE : GONE);
        bgIcon.setVisibility(isVisible ? VISIBLE : GONE);

        ConstraintSet set = new ConstraintSet();
        set.clone(layout);
        set.clear(R.id.trash_container, ConstraintSet.BOTTOM);
        if (isVisible) {
            set.connect(trashContainer.getId(), ConstraintSet.BOTTOM, holdDragLine.getId(), ConstraintSet.TOP);
        } else {
            set.connect(trashContainer.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
        }
        set.applyTo(layout);
    }

    @Override
    public void onHoverStart(@NotNull RecyclerView.ViewHolder viewHolder) {
        trashcan.setScaleX(1.1f);
        trashcan.setScaleY(1.1f);
    }

    @Override
    public void onHoverExit(@NotNull RecyclerView.ViewHolder viewHolder) {
        trashcan.setScaleX(1f);
        trashcan.setScaleY(1f);
    }

    @Override
    public void onLocalizationManagerInitialized() {
        setUpTextViewWithLocalizedText(tvHoldDrag, tvSaveTeams);
    }

    // If it has presenter in the future, then move this method into presenter
    // Refer to example of MediaSettingsPresenter
    public void setUpTextViewWithLocalizedText(android.view.View... views) {
        for (android.view.View itemView : views) {
            switch (itemView.getId()) {
                case R.id.hold_drag_label:
                    ((TextView) itemView).setText(LocalizationManager.TeamSelector.HoldAndDrag);
                    break;
                case R.id.save_teams:
                    ((TextView) itemView).setText(LocalizationManager.TeamSelector.SaveTeams);
                    break;
            }
        }
    }

    /**
     * Show team loader loading animation relative views
     * And play animations
     */
    private void showTeamLoaderLoadingAnimation() {
        if (loaderBackgroundAnimationObserver != null) {
            compositeDisposable.remove(loaderBackgroundAnimationObserver);
        }
        loadingTextView.setVisibility(View.VISIBLE);
        loaderBackgroundAnimationObserver = Observable.just(0L)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<Long>() {
                    @Override
                    public void onNext(Long aLong) {
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onComplete() {
                        showTeamLoaderLoadingAnimationHelper();
                    }
                });
        compositeDisposable.add(loaderBackgroundAnimationObserver);

        hideLoaderLottieAnimationView();
    }

    /**
     * Show team loader loading animation helper
     */
    private void showTeamLoaderLoadingAnimationHelper() {
        loaderBackgroundRelativeLayout.setAlpha(0.0f);
        loaderBackgroundRelativeLayout.setVisibility(VISIBLE);
        loaderBackgroundRelativeLayout.animate().alpha(1.0f)
                .setInterpolator(new PathInterpolator(0.25f, 0.1f, 0.25f, 1.0f))
                .setDuration(500L)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        animation.removeAllListeners();
                        if (loaderLottieAnimationView != null) {
                            loaderLottieAnimationView.setAlpha(1.0f);
                            loaderLottieAnimationView.setVisibility(VISIBLE);
                            loaderLottieAnimationView.playAnimation();
                        }
                    }
                }).start();

    }

    /**
     * Hide loaderLottieAnimationView with animation
     * At the end of animation, go to appropriate fragment
     */
    private void hideLoaderLottieAnimationView() {
        if (loaderLottieAnimationObserver != null) {
            compositeDisposable.remove(loaderLottieAnimationObserver);
        }
        loaderLottieAnimationObserver = Observable
                .timer(6000L, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<Long>() {
                    @Override
                    public void onNext(Long aLong) {
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onComplete() {
                        hideLoaderLottieAnimationViewHelper();
                    }
                });
        compositeDisposable.add(loaderLottieAnimationObserver);
    }

    /**
     * Hide loader lottie animation view helper
     */
    private void hideLoaderLottieAnimationViewHelper() {
        loaderLottieAnimationView.animate().alpha(0.0f)
                .setInterpolator(new PathInterpolator(0.25f, 0.1f, 0.25f, 1.0f))
                .setDuration(1000L)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        if (loaderLottieAnimationView != null) {
                            loaderLottieAnimationView.removeAllAnimatorListeners();
                            if (loaderLottieAnimationView.isAnimating()) {
                                loaderLottieAnimationView.cancelAnimation();
                            }
                        }
                        goToAppropriateView();
                    }
                }).start();
        loadingTextView.setVisibility(View.INVISIBLE);
    }

    // Cancel team loader loading animation
    // And hide all relative views
    private void clearTeamLoaderLoadingAnimation(boolean hidden) {
        if (!hidden) {
            loaderBackgroundRelativeLayout.setVisibility(GONE);
            loaderBackgroundRelativeLayout.setAlpha(1.0f);
            loaderBackgroundRelativeLayout.clearAnimation();
            loaderLottieAnimationView.setAlpha(0.0f);
            loaderLottieAnimationView.clearAnimation();
            if (loaderLottieAnimationView.isAnimating()) {
                loaderLottieAnimationView.cancelAnimation();
            }
        } else {
            loaderBackgroundRelativeLayout.clearAnimation();
            loaderLottieAnimationView.clearAnimation();
            if (loaderLottieAnimationView.isAnimating()) {
                loaderLottieAnimationView.cancelAnimation();
            }
        }
    }

    // Go to appropriate view base on conditions
    private void goToAppropriateView() {
        if (FtueUtil.isAppFirstLaunch()) {
            NavigationManager.getInstance().showFabOutroFragment();
        } else {
            NavigationManager.getInstance().closeTeamSelectorFragment();
        }
    }

    public void setLoadingSpinnerVisibility(boolean visible) {
        progressBar.setVisibility(visible ? VISIBLE : GONE);
        selectionAdapter.setProgressBarIsVisible(visible);
    }

    @Override
    public PageInfo getPageInfo() {
        return new PageInfo(false, "", "", "Settings", "My Teams", "", "", "", "", "", "");
    }
}
