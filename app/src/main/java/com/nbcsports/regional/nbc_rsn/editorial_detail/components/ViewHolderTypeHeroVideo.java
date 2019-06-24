package com.nbcsports.regional.nbc_rsn.editorial_detail.components;

import android.graphics.Color;
import androidx.viewpager.widget.ViewPager;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nbcsports.regional.nbc_rsn.R;
import com.nbcsports.regional.nbc_rsn.common.MediaSource;
import com.nbcsports.regional.nbc_rsn.common.Team;
import com.nbcsports.regional.nbc_rsn.common.components.ViewHolderPersistentPlayerMedium;
import com.nbcsports.regional.nbc_rsn.editorial_detail.models.EditorialDetailItem;
import com.nbcsports.regional.nbc_rsn.localization.LocalizationManager;
import com.nbcsports.regional.nbc_rsn.persistentplayer.layouts.Medium;
import com.nbcsports.regional.nbc_rsn.team_feed.components.FragmentLifeCycleListener;
import com.nbcsports.regional.nbc_rsn.common.FeedComponent;
import com.nbcsports.regional.nbc_rsn.utils.DateFormatUtils;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import lombok.Getter;

public class ViewHolderTypeHeroVideo extends ViewHolderPersistentPlayerMedium implements FragmentLifeCycleListener {

    private final static String BR_TAG                = "<br>";
    private final static String P_TAG                 = "</*p>";
    private final static String NEW_LINE_CHAR_PATTERN = "(\r)?\n";

    private FragmentLifeCycleListener.Interface editorialDetailComponentsAdapter;
    private Team team;

    public EditorialDetailItem editorialDetailItem;

    @BindView(R.id.title)
    TextView title;

    @BindView(R.id.author_image)
    ImageView authorImage;

    @BindView(R.id.author_name)
    TextView authorName;

    @BindView(R.id.header_flag_top_color)
    View headerFlagTopColor;

    @BindView(R.id.header_flag_bottom_color)
    View headerFlagBottomColor;

    @BindView(R.id.date)
    TextView date;

    @BindView(R.id.time)
    TextView time;

    @BindView(R.id.persistent_player_medium)
    Medium medium;

    @Getter
    @BindView(R.id.share_button)
    public ImageView shareButton;

    @Getter @BindView(R.id.exit_button)
    public ImageView exitButton;

    public ViewHolderTypeHeroVideo(FragmentLifeCycleListener.Interface editorialDetailComponentsAdapter, View view, Team team, ViewPager viewPager, int itemViewType) {
        super(editorialDetailComponentsAdapter, view, viewPager, itemViewType);
        this.team = team;
        this.editorialDetailComponentsAdapter = editorialDetailComponentsAdapter;
        editorialDetailComponentsAdapter.addFragmentLifeCycleListener(this);
        this.itemViewType = itemViewType;
    }

    @Override
    protected Team getTeam() {
        return team;
    }

    public void updateView() {
        if (editorialDetailItem == null) return;

        date.setText(DateFormatUtils.getMonthDayYearText(editorialDetailItem.getPublishedDate()).toUpperCase());
        time.setText(DateFormatUtils.getHourMinText(editorialDetailItem.getPublishedDate()));

        // Remove html tags from display text
        String rawDisplayText = editorialDetailItem.getHeroText();
        rawDisplayText = rawDisplayText.replaceAll(NEW_LINE_CHAR_PATTERN, "")
                .replaceAll(BR_TAG, "").replaceAll(P_TAG, "");

        title.setText(rawDisplayText);

        String formattedAuthor = String.format("BY %s", editorialDetailItem.getAuthor());
        if (LocalizationManager.isInitialized()){
            formattedAuthor = LocalizationManager.Common.getAuthor(editorialDetailItem.getAuthor());
        }
        authorName.setText(formattedAuthor);

        if (!TextUtils.isEmpty(editorialDetailItem.getAuthorImage())) {
            Picasso.get()
                    .load(editorialDetailItem.getAuthorImage())
                    .error(R.drawable.ic_peacock)
                    .into(authorImage);
        }

        if (team != null){
            if (team.getPrimaryColor() != null && !team.getPrimaryColor().isEmpty()) {
                headerFlagTopColor.setBackgroundColor(Color.parseColor(team.getPrimaryColor()));
            }

            if (team.getSecondaryColor() != null && !team.getSecondaryColor().isEmpty()) {
                headerFlagBottomColor.setBackgroundColor(Color.parseColor(team.getSecondaryColor()));
            }
        }

    }

    public void setMediaSource(MediaSource mediaSource, FeedComponent feedComponent, String title){
        if (mediaSource == null) return;

        if (feedComponent != null){
            mediaSource.setTitle(feedComponent.getTitle());
            mediaSource.setBackupImage(feedComponent.getImageAssetUrl());
        }

        if (title != null){
            mediaSource.setTitle(title);
        }

        if (mediaSource.getImage() == null){
            mediaSource.setImage(""); // set with default empty String
        }
        super.setMediaSource(mediaSource);
    }

    public void setItem(EditorialDetailItem editorialDetailItem) {
        this.editorialDetailItem = editorialDetailItem;
    }

    public EditorialDetailItem getItem() {
        return editorialDetailItem;
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
    }

    public void bind(EditorialDetailItem editorialDetailItem) {
        this.editorialDetailItem = editorialDetailItem;
    }
}