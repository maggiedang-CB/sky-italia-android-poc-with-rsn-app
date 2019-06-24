package com.nbcsports.regional.nbc_rsn.fabigation;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nbcsports.regional.nbc_rsn.BuildConfig;
import com.nbcsports.regional.nbc_rsn.MainActivity;
import com.nbcsports.regional.nbc_rsn.R;
import com.nbcsports.regional.nbc_rsn.analytics.TrackingHelper.PageInfo;
import com.nbcsports.regional.nbc_rsn.common.BaseFragment;
import com.nbcsports.regional.nbc_rsn.common.Team;
import com.nbcsports.regional.nbc_rsn.common.TeamManager;
import com.nbcsports.regional.nbc_rsn.localization.LocalizationManager;
import com.nbcsports.regional.nbc_rsn.utils.ActivityUtils;
import com.nbcsports.regional.nbc_rsn.utils.DisplayUtils;
import com.nbcsports.regional.nbc_rsn.utils.PreferenceUtils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;

import butterknife.BindView;
import timber.log.Timber;

import static com.nbcsports.regional.nbc_rsn.utils.DisplayUtils.animateColorChange;
import static com.nbcsports.regional.nbc_rsn.utils.DisplayUtils.animateStatusBarColorChange;

public class FabMenuFragment extends BaseFragment implements FabMenu.FabPositionInterface {

    @BindView(R.id.team_recycler_view)
    FabMenuRecyclerView recyclerView;

    @BindView(R.id.menu_layout)
    View menuLayout;

    @BindView(R.id.version)
    TextView version;

    @BindView(R.id.white_filter)
    View whiteFilter;

    @BindView(R.id.transition_view)
    CloneView transitionView;

    @BindView(R.id.transition_view_color)
    ConstraintLayout transitionViewColorContainer;

    @BindView(R.id.first_launch_bg)
    ConstraintLayout firstLaunchAnimationView;
    // fab positions
    private float FAB_START_POSITION;
    private int colorFrom = Color.TRANSPARENT;
    private FabMenu fabMenu;

    private final int MENU_START_INDEX = 0;
    private LinearLayoutManager layoutManager;
    private FabMenuAdapter fabMenuAdapter;

    private final long VIBRATION_DURATION = 10L;

    //Variable that handles when the recyclerview does have view to use
    private boolean isFirstLaunch = true;

    //Fab card are resize to 0.625, to get the original fab card, use 1/0.625 = 1.6
    private static final float FAB_CARD_RESIZE_RATIO = 1.6f;

    @Override
    public int getLayout() {
        return R.layout.fragment_menu;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        //this.trackingPageName = "FabMenuFragment";
        super.onCreate(savedInstanceState);
    }


    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        DisplayUtils.applyWhiteGradient(whiteFilter);

        fabMenu = (FabMenu) getActivity().findViewById(R.id.fab);
        fabMenu.setFabPositionInterface(this);

        initRecyclerView();

        //Adjust the first launch views to full screen
        firstLaunchAnimationView.setScaleX(FAB_CARD_RESIZE_RATIO);
        firstLaunchAnimationView.setScaleY(FAB_CARD_RESIZE_RATIO);
        // set default color
        if (menuLayout.getBackground() instanceof ColorDrawable)
            colorFrom = ((ColorDrawable) menuLayout.getBackground()).getColor();

        // Set the version
        if (BuildConfig.IS_PROD) {
            version.setVisibility(View.INVISIBLE);
        } else {
            version.setVisibility(View.VISIBLE);
            version.setText(String.format("v%s-%s", BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE));
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initRecyclerView() {
        layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        updateMenuOrientation();
        recyclerView.setLayoutManager(layoutManager);
        fabMenuAdapter = new FabMenuAdapter((MainActivity) getActivity(), new ArrayList<>(), isRTL());
        recyclerView.addItemDecoration(new FabMenuItemDecoration(0.125f));
        recyclerView.setAdapter(fabMenuAdapter);
        recyclerView.setOnTouchListener((v, event) -> true);
        updateTeamList();
    }

    private void updateTeamList() {
        TeamManager teamManager = ((MainActivity) getActivity()).getTeamManager();
        ArrayList<Team> teamList = new ArrayList<>(teamManager.getTeamsForMenu());

        Timber.e("updateTeamList teamList:%s", teamList);

        boolean isMoreTeamsEmpty = teamManager.getMoreTeamsList().isEmpty();
        Team selectedTeam = teamManager.getSelectedTeam();
        int selectedIndex = teamList.indexOf(selectedTeam);

        // 1. If more teams list is not empty, then set More Teams card to the left of Settings card
        // 2. Otherwise, set it to the default value, which is -1
        int moreCardIndex = isMoreTeamsEmpty ? -1 : teamList.size();

        if (teamList.contains(selectedTeam)) {
            // Selected team is in the menu
            // Rotate to the selected team first
            Collections.rotate(teamList, teamList.size() - selectedIndex);
            menuLayout.setBackgroundColor(Color.parseColor(selectedTeam.getPrimaryColor()));
            colorFrom = Color.parseColor(selectedTeam.getPrimaryColor());
        } else {
            // Selected team is not in the menu
            // Change background color to the first team in team list
            // Note: When More teams card or Settings card is selected,
            //       it does not affect selectedTeam
            if (!teamList.isEmpty() && teamList.get(0) != null){
                Team firstTeamInTeamList = teamList.get(0);
                menuLayout.setBackgroundColor(Color.parseColor(firstTeamInTeamList.getPrimaryColor()));
                colorFrom = Color.parseColor(firstTeamInTeamList.getPrimaryColor());
            }
        }
        fabMenuAdapter.setTeams(teamList, moreCardIndex, isRTL());
    }

    private void updateMenuOrientation() {
        layoutManager.setReverseLayout(isRTL());
        FAB_START_POSITION = isRTL() ? 1f : -0.15f;

        if (fabMenuAdapter != null) {
            fabMenuAdapter.setRTL(isRTL());
            fabMenuAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

        updateMenuOrientation();
        if (!hidden) {
            updateTeamList();

            // reset position when this is visible

            recyclerView.scrollToPosition(MENU_START_INDEX);
        }
    }

    @Override
    public void scrollToPercent(float x) {
        if (recyclerView != null) {
            if (isRTL()) {
                if (x < FAB_START_POSITION) {
                    recyclerView.scrollToPercent(x);
                }
            } else {
                if (x > FAB_START_POSITION) {
                    recyclerView.scrollToPercent(x);
                }
            }
            updateBackgroundColor();
        }
    }

    private void updateBackgroundColor() {
        // don't do anything if this is not the top fragment
        if (!this.isVisible()) return;

        if (recyclerView != null) {
            int pos = recyclerView.getSelectedPosition(isRTL());

            if (pos != RecyclerView.NO_POSITION) {
                RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(pos);

                if (holder == null) {
                    return;
                }

                CardView cardView = (CardView) (holder.itemView);
                int colorTo = cardView != null ? cardView.getCardBackgroundColor().getDefaultColor() : Color.TRANSPARENT;
                if (fabMenuAdapter.getItemViewType(pos) == FabMenuAdapter.FabCardType.MoreTeams.ordinal()) {
                    menuLayout.setBackgroundColor(Color.BLACK);
                    colorTo = Color.BLACK;
                }
                if (colorFrom != colorTo) {

                    if (fabMenuAdapter.getItemViewType(pos) == FabMenuAdapter.FabCardType.Card.ordinal()) {
                        Team team = fabMenuAdapter.getTeamForPosition(pos);
                        menuInterface.switchFabLogo(team);
                    } else {
                        ImageView fab_logo = (ImageView) fabMenu.findViewById(R.id.fab_logo_container);
                        fab_logo.setImageResource(0);
                    }

                    ActivityUtils.vibrate(getContext(), VIBRATION_DURATION);
                    animateColorChange(menuLayout, colorFrom, colorTo);
                    animateStatusBarColorChange(getContext(), colorFrom, colorTo);  // update status bar color
                }
                colorFrom = colorTo;
            }
        }
    }

    private boolean isRTL() {
        return PreferenceUtils.INSTANCE.getBoolean("_isFabRtl", false);
    }

    @Override
    public void center() {
        if (recyclerView != null) {
            recyclerView.scrollToCenterItem(isRTL());

            // settle menu position before resetting
            settleMenuScrolling();

            recyclerView.resetPosition();
        }
    }

    private void settleMenuScrolling() {
        int pos = recyclerView.getSelectedPosition(isRTL());
        int itemViewType = fabMenuAdapter.getItemViewType(pos);
        FabMenuAdapter.FabCardType cardType = FabMenuAdapter.FabCardType.values()[itemViewType];

        // Update TeamManager selected team
        if (cardType.ordinal() == FabMenuAdapter.FabCardType.Card.ordinal()) {
            try {
                Team team = fabMenuAdapter.getTeamForPosition(pos);
                ((MainActivity) getActivity()).getTeamManager().setSelectedTeam(team);
            } catch (IllegalArgumentException e) {
                Timber.e(e);
            }
        }

        // exit to the proper card type
        menuInterface.exitMenu(cardType);
    }

    public void animateTransitionView(int animationId, FabMenuAdapter.FabCardType fabCardType, Fragment fragmentComingFrom, boolean isEnteringToMenu) {
        if (recyclerView == null) {
            return;
        }
        Team selectedTeam = ((MainActivity) getActivity()).getTeamManager().getSelectedTeam();
        //Variable that direct recyclerview to obtain correct card
        int position = 0;
        //Id direct to the correct layout within the page
        int duplicateViewID;
        //Background color for transitionView (Since most view are transparent)
        int backgroundColor;

        if (!isEnteringToMenu) {
            switch (fabCardType) {
                case Settings:
                    position = duplicateViewPosition(LocalizationManager.Settings.Settings, R.id.more_teams_label);
                    backgroundColor = Color.TRANSPARENT;
                    duplicateViewID = R.id.setting_bg_container;
                    break;
                case Card:
                    duplicateViewID = R.id.team_list_bg_container;
                    backgroundColor = Color.parseColor(selectedTeam.getPrimaryColor());
                    position = duplicateViewPosition(selectedTeam.getDisplayName(), R.id.team_name);
                    break;
                default:
                    position = duplicateViewPosition(LocalizationManager.MoreTeamsSelector.MoreTeamsCard, R.id.more_teams_label);
                    backgroundColor = getResources().getColor(R.color.deep_blue);
                    duplicateViewID = R.id.more_team_bg_container;
            }
        } else {
            //RecyclerView will have 2 child when first enter the fab menu, the first card (pos = 0) is the card we want to display
            recyclerView.scrollToPosition(MENU_START_INDEX);
            //We do not need to deal with setting card, since it has a different animation
            duplicateViewID = R.id.team_list_bg_container;
            backgroundColor = Color.parseColor(selectedTeam.getPrimaryColor());
            // Check if selected team is in TeamsForMenu list
            // 1. If so then do nothing
            // 2. Otherwise, set the background color of fab menu to the
            //    primary color of the first team of the TeamsForMenu list
            if (!fabMenuAdapter.getTeamList().contains(selectedTeam)) {
                if (((MainActivity) getActivity()).getTeamManager() != null
                        && ((MainActivity) getActivity()).getTeamManager().getTeamsForMenu() != null
                        && !((MainActivity) getActivity()).getTeamManager().getTeamsForMenu().isEmpty()){
                    backgroundColor = Color.parseColor(((MainActivity) getActivity()).getTeamManager().getTeamsForMenu().get(0).getPrimaryColor());
                }
            }
            position = 0;
        }

        //When there is no known view holders, manually create the view
        if(fabCardType == FabMenuAdapter.FabCardType.FabOutro) {
            transitionViewColorContainer.clearAnimation();
            transitionViewColorContainer.setAnimation(fabTransitionAnimation(transitionViewColorContainer, animationId));
        } else if (recyclerView.getChildCount() == 0 || recyclerView.getChildAt(position) == null
                || (recyclerView.getChildAt(position).findViewById(duplicateViewID) == null && isEnteringToMenu)) {
            firstLaunchAnimationView.setVisibility(View.VISIBLE);
            transitionViewColorContainer.setVisibility(View.INVISIBLE);
            ImageView teamLogo = firstLaunchAnimationView.findViewById(R.id.team_logo);
            TextView appTitle = firstLaunchAnimationView.findViewById(R.id.team_app_title);
            TextView teamName = firstLaunchAnimationView.findViewById(R.id.team_name);
            TextView teamRecordTextView = firstLaunchAnimationView.findViewById(R.id.team_record);
            LinearLayout gradientFilter = firstLaunchAnimationView.findViewById(R.id.gradient_filter);

            if (!selectedTeam.getLogoUrl().isEmpty()) {
                Picasso.get()
                        .load(selectedTeam.getLogoUrl())
                        .into(teamLogo);
            }
            appTitle.setText(selectedTeam.getRegionName().toUpperCase());
            teamName.setText(selectedTeam.getDisplayName().toUpperCase());
            teamRecordTextView.setText(LocalizationManager.DataBar.Record);

            DisplayUtils.applyColorGradient(gradientFilter, Color.parseColor(selectedTeam.getPrimaryColor()));
            firstLaunchAnimationView.clearAnimation();
            transitionViewColorContainer.clearAnimation();

            firstLaunchAnimationView.setAnimation(fabTransitionAnimation(firstLaunchAnimationView, animationId));
        } else {
            transitionView.setSource(recyclerView.getChildAt(position).findViewById(duplicateViewID));
            transitionView.setBackgroundColor(Color.TRANSPARENT);
            transitionViewColorContainer.setBackgroundColor(backgroundColor);
            transitionView.setVisibility(View.VISIBLE);
            firstLaunchAnimationView.clearAnimation();
            transitionViewColorContainer.clearAnimation();
            transitionViewColorContainer.setAnimation(fabTransitionAnimation(transitionViewColorContainer, animationId));
        }
    }

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        if (getContext() != null && enter) {
            Animation anim = AnimationUtils.loadAnimation(getContext(), nextAnim);
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    fabMenu.setMenuAnimating(true);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    fabMenu.setMenuOpened(true);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });

            return anim;
        } else {
            return super.onCreateAnimation(transit, enter, nextAnim);
        }
    }


    public int duplicateViewPosition(String displayText, int viewID) {
        //Iterate through the available child and find the correct view to duplicate
        for (int i = 0; i < recyclerView.getChildCount(); i++) {
            TextView view = recyclerView.getChildAt(i).findViewById(viewID);
            if (view != null) {
                //Check if the team name of 2 view matches, if true, return the correct position to display
                if (view.getText().toString().equalsIgnoreCase(displayText)) {
                    return i;
                }
            }
        }
        return 0;
    }

    public Animation fabTransitionAnimation(View viewToAnimate, int animationId) {
        Animation transitionAnimation = AnimationUtils.loadAnimation(getContext(), animationId);
        transitionAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                viewToAnimate.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                viewToAnimate.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        return transitionAnimation;
    }
    @Override
    public PageInfo getPageInfo() {
        return new PageInfo( false, "", "", "", "", "", "", "", "", "", "");
    }
}
