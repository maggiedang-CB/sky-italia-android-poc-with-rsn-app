package com.nbcsports.regional.nbc_rsn.persistentplayer.switchscreen;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.jakewharton.rxbinding2.view.RxView;
import com.nbcsports.regional.nbc_rsn.R;
import com.nbcsports.regional.nbc_rsn.common.Asset;
import com.nbcsports.regional.nbc_rsn.common.MediaSource;
import com.nbcsports.regional.nbc_rsn.deeplink.Deeplink;
import com.nbcsports.regional.nbc_rsn.common.Team;
import com.nbcsports.regional.nbc_rsn.common.TeamManager;
import com.nbcsports.regional.nbc_rsn.team_view.PeacockImageView;
import com.nbcsports.regional.nbc_rsn.team_view.PicassoLoadListener;
import com.nbcsports.regional.nbc_rsn.utils.DisplayUtils;
import com.nbcsports.regional.nbc_rsn.localization.LocalizationManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import lombok.Getter;
import lombok.Setter;

public class SwitchScreenAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int HEADER = 0;
    private static final int ITEM = 1;

    private static final int HEADER_POSITION = 0;

    private static final int NUM_HEADER = 1;
    private static final int NUM_MOST_RECENT = 1;

    private static final float ITEM_WIDTH_PERCENT = 0.5f;
    private static final float MARGIN_WIDTH_PERCENT = 0.05f;

    private static final int SMALL_STATUS_FONT_SIZE = 10; // sp
    private static final int SMALL_TITLE_FONT_SIZE = 12; // sp

    private static final int LARGE_STATUS_FONT_SIZE = 14; // sp
    private static final int LARGE_TITLE_FONT_SIZE = 18; // sp

    @Getter
    @Setter
    private boolean isLandscape = true; // default set as landscape

    @Getter @Setter private static List<Asset> liveAssets = new ArrayList<>();
    @Getter private static MediaSource lastViewedMediaSource;

    private final SwitchScreenView mSwitchScreen;

    public SwitchScreenAdapter(SwitchScreenView parent) {
        mSwitchScreen = parent;
    }

    public void setData(@Nullable List<Asset> newData) {
        if (newData == null){
            liveAssets = new ArrayList<>();
            return;
        }
        liveAssets = newData;
        notifyDataSetChanged();
    }

    public void setMostRecentMediaSource(@Nullable MediaSource mediaSource) {
        lastViewedMediaSource = mediaSource;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == HEADER) {
            View videoHeaderView = LayoutInflater.from(parent.getContext()).inflate(R.layout.persistent_player_switch_screen_header, parent, false);
            setHeaderOnTouchListeners(videoHeaderView);
            return new SwitchScreenHeaderHolder(videoHeaderView);
        } else {
            View videoItemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.persistent_player_switch_screen_item, parent, false);
            float screenWidth = (float) DisplayUtils.getScreenWidth(parent.getContext());
            int itemWidth = (int) (screenWidth * ITEM_WIDTH_PERCENT);
            int marginWidth = (int) (screenWidth * MARGIN_WIDTH_PERCENT);

            ViewGroup.LayoutParams lp = videoItemView.getLayoutParams();
            lp.width = itemWidth;
            videoItemView.setLayoutParams(lp);

            FrameLayout image = videoItemView.findViewById(R.id.image_container);

            ViewGroup.LayoutParams vlp = image.getLayoutParams();
            vlp.width = itemWidth - marginWidth;
            image.setLayoutParams(vlp);

            setItemOnTouchListeners(videoItemView);
            return new SwitchScreenItemHolder(videoItemView);
        }
    }

    @SuppressWarnings("all")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (position == HEADER_POSITION) return;

        SwitchScreenItemHolder itemHolder = (SwitchScreenItemHolder) holder;

        if (isLastViewedPosition(position)){
            itemHolder.bindTo(lastViewedMediaSource, isLandscape);

        } else {
            itemHolder.bindTo(getDataItem(position), isLandscape);
        }
    }

    private void setHeaderOnTouchListeners(View switchScreenHeader){
        View nonClickableMain = switchScreenHeader.findViewById(R.id.non_clickable_area_main);
        passTouchToController(nonClickableMain);

        View nonClickableTop = switchScreenHeader.findViewById(R.id.non_clickable_area_top);
        passTouchToController(nonClickableTop);

        View nonClickableBottom = switchScreenHeader.findViewById(R.id.non_clickable_area_bottom);
        passTouchToController(nonClickableBottom);

        View switchScreenBarClickableArea = switchScreenHeader.findViewById(R.id.persistent_player_switch_screen_click_area);
        keepTouchInSwitchScreen(switchScreenBarClickableArea);

        View switchScreenBar = switchScreenHeader.findViewById(R.id.persistent_player_switch_screen_button);
        keepTouchInSwitchScreen(switchScreenBar);
    }

    private void setItemOnTouchListeners(View switchScreenItem){
        View nonClickableAreaBottom = switchScreenItem.findViewById(R.id.non_clickable_area_bottom);
        passTouchToController(nonClickableAreaBottom);

        View nonClickableAreaTop = switchScreenItem.findViewById(R.id.non_clickable_area_top);
        passTouchToController(nonClickableAreaTop);
    }

    @SuppressWarnings("all")
    private void passTouchToController(View view){
        view.setOnTouchListener((v, event) -> {
            mSwitchScreen.onVideoControllerTouchEvent(event);
            return false;
        });
    }


    private void keepTouchInSwitchScreen(View view){
        view.setOnTouchListener((v, event) -> {
            mSwitchScreen.onSwitchScreenBarTouchEvent(event);
            return false;
        });
    }
    
    private Asset getDataItem(int position) {
        return liveAssets.get(position - 1);
    }

    @Override
    public int getItemCount() {
        return lastViewedMediaSource == null ? liveAssets.size() + NUM_HEADER : liveAssets.size() + (NUM_HEADER + NUM_MOST_RECENT);
    }

    @Override
    public int getItemViewType(int position) {
        return position == HEADER_POSITION ? HEADER : ITEM; //The most recent video is considered as an ITEM
    }

    private boolean isLastViewedPosition(int position){
        return position == liveAssets.size() + 1 && lastViewedMediaSource != null;
    }

    class SwitchScreenHeaderHolder extends RecyclerView.ViewHolder {

        SwitchScreenHeaderHolder(View itemView) {
            super(itemView);
        }
    }

    class SwitchScreenItemHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.video_status)
        TextView videoStatus;

        @BindView(R.id.video_title)
        TextView videoTitle;

        @BindView(R.id.video_image)
        PeacockImageView videoImage;

        SwitchScreenItemHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            if (mSwitchScreen != null){
                if (mSwitchScreen.compositeDisposable == null){
                    mSwitchScreen.compositeDisposable = new CompositeDisposable();
                }
                mSwitchScreen.compositeDisposable.add(getVideoClickedDisposable());
            }
        }

        /***
         * Updates data to display in SwitchScreen. This method is used for items that will be
         *  displayed as "Last Viewed"
         *
         * @param mediaSource   - MediaSource that will be displayed in the SwitchScreen.
         * @param isLandscape   - The current orientation of the device.
         */
        private void bindTo(MediaSource mediaSource, boolean isLandscape) {
            if (mediaSource == null) return;

            videoTitle.setText(mediaSource.getTitle());

            if (LocalizationManager.isInitialized()){
                videoStatus.setText(LocalizationManager.VideoPlayer.LastViewed);
            }
            
            updateTextSize(isLandscape);

            if (mediaSource == null) return;
            Team team = null;
            if (mediaSource.getDeeplink() != null){
                team = mediaSource.getDeeplink().getTeam();
            }
            String teamColor = team != null ? team.getPrimaryColor() : null;
            PicassoLoadListener backUpLoadListener = getBackUpLoadListener(mediaSource.getBackupImage(), teamColor);

            String imageUrl = Asset.Companion.getThumbnailUrl(mSwitchScreen.getContext(), mediaSource.getImage());
            videoImage.loadImage(false,imageUrl, teamColor, backUpLoadListener);
        }

        /***
         *
         * Updates data for current Live Assets. This method is used for assets that will be
         *  displayed as "Live"
         *
         * @param liveAsset     - Live asset to be displayed in SwitchScreen
         * @param isLandscape   - The current orientation of the device
         */
        private void bindTo(Asset liveAsset, boolean isLandscape){
            if (liveAsset == null) return;

            videoTitle.setText(liveAsset.getTitle());

            if (LocalizationManager.isInitialized()){
                videoStatus.setText(LocalizationManager.VideoPlayer.Live.toUpperCase());
            }

            updateTextSize(isLandscape);

            if (liveAsset.getImage() == null || liveAsset.getImage().isEmpty()) return;

            Team team = null;
            if (TeamManager.Companion.getInstance() != null){
                team = TeamManager.Companion.getInstance().getUserTeamByTeamDisplayName(liveAsset.getHomeTeam());
            }
            String teamColor = team != null ? team.getPrimaryColor() : null;
            String imageUrl = Asset.Companion.getThumbnailUrl(mSwitchScreen.getContext(), liveAsset.getImage());

            videoImage.loadImage(false, imageUrl, teamColor, null);
        }

        private void updateTextSize(boolean isLandscape){
            videoTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP,
                    isLandscape ? LARGE_TITLE_FONT_SIZE : SMALL_TITLE_FONT_SIZE);

            videoStatus.setTextSize(TypedValue.COMPLEX_UNIT_SP,
                    isLandscape ? LARGE_STATUS_FONT_SIZE : SMALL_STATUS_FONT_SIZE);
        }

        private PicassoLoadListener getBackUpLoadListener(String backUpImageUrl, String teamColor){
            return new PicassoLoadListener() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError(Throwable e) {
                    videoImage.loadImage(false, backUpImageUrl, teamColor, null);
                }
            };
        }

        Disposable getVideoClickedDisposable() {
            return RxView.clicks(videoImage)
                    .debounce(250, TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(v -> {
                        int position = getAdapterPosition();
                        if (position == -1 || mSwitchScreen.mOnItemClickListener == null) return;

                        if (isLastViewedPosition(position)) {
                            mSwitchScreen.mOnItemClickListener.onClick(lastViewedMediaSource);
                            // possible for lastViewedMediaSource to be null now via onclick callback

                        } else {
                            Asset liveAsset = getDataItem(position);

                            Deeplink deeplink = Deeplink.getDeeplinkForTeamView(liveAsset.getHomeTeam());

                            String requestorId = liveAsset.getRequestorId();
                            if (requestorId.isEmpty()) {
                                requestorId = liveAsset.getChannel();
                            }
                            mSwitchScreen.mOnItemClickListener.onClick(new MediaSource(
                                    liveAsset.getPid(),
                                    liveAsset.getId(),
                                    liveAsset.getTitle(),
                                    liveAsset.getAndroidStreamUrl(),
                                    liveAsset.getImage(),
                                    requestorId,
                                    true,
                                    deeplink,
                                    liveAsset.getChannel(),
                                    liveAsset
                            ));
                        }
                    });
        }
    }
}
