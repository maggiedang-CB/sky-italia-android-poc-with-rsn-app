package com.nbcsports.regional.nbc_rsn.persistentplayer.switchscreen;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

import com.nbcsports.regional.nbc_rsn.common.Asset;
import com.nbcsports.regional.nbc_rsn.common.LiveAssetManager;
import com.nbcsports.regional.nbc_rsn.common.MediaSource;
import com.nbcsports.regional.nbc_rsn.common.Team;
import com.nbcsports.regional.nbc_rsn.common.TeamManager;
import com.nbcsports.regional.nbc_rsn.persistentplayer.Injection;
import com.nbcsports.regional.nbc_rsn.persistentplayer.PersistentPlayer;
import com.nbcsports.regional.nbc_rsn.persistentplayer.PersistentPlayerContract;
import com.nbcsports.regional.nbc_rsn.persistentplayer.layouts.Landscape;
import com.nbcsports.regional.nbc_rsn.persistentplayer.layouts.Medium;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import io.reactivex.disposables.CompositeDisposable;

public class SwitchScreenView extends RecyclerView {

    private static boolean mSwitchScreenTouch = false;
    private static int mFullAlphaBackground = Integer.parseInt("CC", 16); //Based on alpha hex value of CC from spec. Decimal value is 204.
    private static int SCROLL_COMPLETE_RATIO = 3;
    private int mWidth = 0;
    private Landscape landscape;
    private Medium medium;
    private LinearLayoutManager mLayoutManager;
    private SwitchScreenAdapter mAdapter;
    protected CompositeDisposable compositeDisposable;
    private boolean mDrawerOpenState = false;
    private static final String TRANSPARENT_COLOR_STR = "#00000000";
    OnItemClickListener mOnItemClickListener;

    private PersistentPlayer persistentPlayer;
    private static final int NOT_FREE = 0;

    public SwitchScreenView(Context context) {
        super(context);
        init();
    }

    public SwitchScreenView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SwitchScreenView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {

        mLayoutManager = getLinearLayoutManager();

        setLayoutManager(mLayoutManager);

        mAdapter = new SwitchScreenAdapter(this);

        setAdapter(mAdapter);

        persistentPlayer = Injection.providePlayer((PersistentPlayerContract.Main.View) getContext());


    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        closeDrawer();
        mAdapter.notifyDataSetChanged();
    }

    public void bindPlayer(Landscape landscape) {
        this.landscape = landscape;
        mAdapter.setLandscape(true);
    }

    public void bindPlayer(Medium medium){
        this.medium = medium;
        mAdapter.setLandscape(false);
    }

    public void updateSwitchScreenOrientation(boolean isLandscape){
        setAdapter(mAdapter);
        mAdapter.setLandscape(isLandscape);
        mAdapter.notifyDataSetChanged();
    }

    /***
     * Animates closing the drawer
     */
    public void closeDrawer() {
        smoothScrollToPosition(0);
    }

    public boolean isDrawerOpen() {
        return mLayoutManager.findLastVisibleItemPosition() != 0;
    }

    public void setOnItemClickListener(OnItemClickListener itemClickListener) {
        mOnItemClickListener = itemClickListener;
    }

    public void onVideoPlay(MediaSource newMediaSource, boolean userAuthenticated) {
        if (medium != null){
            medium.getPersistentPlayer().getPlayerEngine().addListener(medium);
        }

        closeDrawer();
        ViewingHistory.addMediaSource(newMediaSource);

        MediaSource mostRecentMediaSource = ViewingHistory.getMostRecentMediaSource();

        if (mostRecentMediaSource == null
                || mostRecentMediaSource.getTitle().equalsIgnoreCase(newMediaSource.getTitle()) // user is watching the same mediaSource
                || !isUserTeam(mostRecentMediaSource.getDeeplink().getTeam().getTeamId())){ // most recent media source is of a team that user has not selected

            mostRecentMediaSource = ViewingHistory.getSecondMostRecentMediaSource(); // use the second most recent media source

            // if the second most recent media source is of a team that user has not selected
            if (mostRecentMediaSource != null && !isUserTeam(mostRecentMediaSource.getDeeplink().getTeam().getTeamId())){
                mostRecentMediaSource = null;
            }
        }

        List<Asset> assets = filterLiveAssets(LiveAssetManager.getInstance().getLiveAssets(), newMediaSource, userAuthenticated);


        if (mostRecentMediaSource == null && (assets == null || assets.isEmpty())) {
            setVisibility(View.GONE);
        } else if (persistentPlayer.is247()) {
            setVisibility(View.GONE);
        } else {
            mAdapter.setData(assets); // nullable
            mAdapter.setMostRecentMediaSource(mostRecentMediaSource); //nullable
            mAdapter.notifyDataSetChanged();
            setVisibility(View.VISIBLE);
        }
    }

    private List<Asset> filterLiveAssets(List<Asset> assets, MediaSource mediaSource, boolean authenticated){

        if (assets == null){ // received empty list of assets
            return new ArrayList<>();
        } else if (mediaSource == null){
            return assets;
        }

        List<Asset> filteredOutAssets = new ArrayList<>();

        // remove assets that:
        //  1. have same title as current mediaSource
        //  2. are not free when user is not authenticated
        for (Asset asset : assets){
            if (StringUtils.equalsIgnoreCase(asset.getTitle(), mediaSource.getTitle()) || (!authenticated && asset.getFree() == NOT_FREE) ) {
                filteredOutAssets.add(asset);
            }
        }

        List<Asset> filtered = new ArrayList<>(assets);
        filtered.removeAll(filteredOutAssets);

        return filtered;
    }

    private boolean isUserTeam(String teamId){
        for (Team userTeam: TeamManager.Companion.getInstance().getUsersTeams()){
            if (userTeam.getTeamId().equals(teamId)){
                return true;
            }
        }
        return false;
    }

    private LinearLayoutManager getLinearLayoutManager(){
        // https://stackoverflow.com/q/32241948 to control speed of scroll
        return new LinearLayoutManager(this.getContext(), LinearLayoutManager.HORIZONTAL, false){

            private static final float MILLISECONDS_PER_INCH = 75f;

            @Override
            public void smoothScrollToPosition(RecyclerView recyclerView, State state, int position) {

                LinearSmoothScroller smoothScroller = new LinearSmoothScroller(recyclerView.getContext()) {

                    @Override
                    protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
                        return MILLISECONDS_PER_INCH / displayMetrics.densityDpi;
                    }
                };

                smoothScroller.setTargetPosition(position);

                startSmoothScroll(smoothScroller);
            }
        };
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        super.onMeasure(widthSpec, heightSpec);
        mWidth = View.MeasureSpec.getSize(widthSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        addOnDrawerOpenListener();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (compositeDisposable != null && !compositeDisposable.isDisposed()) compositeDisposable.dispose();
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    protected void dispatchVisibilityChanged(View changedView, int visibility) {
        super.dispatchVisibilityChanged(changedView, visibility);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        super.onTouchEvent(e);
        //return mSwitchScreenTouch;

        //Prevents touch going to controller when drawer is open
        return isDrawerOpen() || mSwitchScreenTouch;
    }

    void onSwitchScreenBarTouchEvent(MotionEvent e) {
        mSwitchScreenTouch = true;
        onTouchEvent(e);
    }

    void onVideoControllerTouchEvent(MotionEvent e) {
        mSwitchScreenTouch = false;
        onTouchEvent(e);
    }

    private void addOnDrawerOpenListener() {
        clearOnScrollListeners();
        addOnScrollListener(new OnScrollListener() {

            boolean drawerOpening = false;

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

                drawerOpening = dx > 0;

                // Percent relative to a THIRD of the screen. If scrolled a third, then percent == 1.0f
                // Possible for this value to be greater than 1 when scrolled past a THIRD of screen.
                float scrolled = computeHorizontalScrollOffset() / ((float) mWidth / SCROLL_COMPLETE_RATIO);

                //Obtain full background alpha (ie. 204 upon reaching a third of screen)
                int desiredAlphaInBase255 = (int) (scrolled * mFullAlphaBackground);
                desiredAlphaInBase255 = desiredAlphaInBase255 >= mFullAlphaBackground ? mFullAlphaBackground : desiredAlphaInBase255;

                setBackgroundColor(isDrawerOpen() ? Color.argb(desiredAlphaInBase255, 0, 0, 0) : Color.parseColor(TRANSPARENT_COLOR_STR));

                View verticalBar = mLayoutManager.findViewByPosition(0);
                if (verticalBar != null) { //If header is visible:
                    verticalBar.setAlpha(scrolled > 1 ? 0 : 1 - scrolled);
                }
                notifyDrawerStateChanged();
            }
        });
    }

    public void drawerOpen() {
        int sizeWithoutHeader = mAdapter.getItemCount() - 1;
        if (sizeWithoutHeader > 2){
            sizeWithoutHeader = 2;
        }
        mLayoutManager.smoothScrollToPosition(this, new RecyclerView.State(), sizeWithoutHeader);
    }

    /***
     * Notifies PersistentPlayer that the state of the drawer has changed onto the
     *  onDrawerStateChanged(boolean isDrawerOpen) method.
     *
     * Method will pass true if drawer is open, false if closed.
     */
    private void notifyDrawerStateChanged() {
        if (mDrawerOpenState != isDrawerOpen()) {
            if (landscape != null) {
                landscape.onDrawerStateChanged(isDrawerOpen());
            }
            mDrawerOpenState = isDrawerOpen();
        }
    }

    public interface OnItemClickListener {
        void onClick(MediaSource mediaSource);
    }

}
