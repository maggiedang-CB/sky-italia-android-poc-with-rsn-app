package com.nbcsports.regional.nbc_rsn.team_feed.components;

import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.exoplayer2.ui.PlayerView;
import com.nbcsports.regional.nbc_rsn.R;
import com.nbcsports.regional.nbc_rsn.common.Team;
import com.nbcsports.regional.nbc_rsn.team_view.PeacockImageView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ViewHolderTypeAudio extends ViewHolderTypeBase {
    //Included in XML layout: teamview_card_type_base_date1_and_region, populated by team view template.

    @BindView(R.id.f1_standard_player)
    PlayerView f1_standard_player;

    @BindView(R.id.f1_standard_video_artwork)
    PeacockImageView f1_standard_video_artwork;

    @BindView(R.id.playhead_icon)
    ImageView playhead_icon;

    @BindView(R.id.description)
    TextView description;

    @BindView(R.id.title)
    TextView title;

    @BindView(R.id.duration_indicator_layout)
    LinearLayout durationIndicatorLayout;

    @BindView(R.id.duration)
    TextView duration;

    @BindView(R.id.duration_indicator_bar)
    ImageView duration_indicator_bar;

    ViewHolderTypeAudio(View view, int itemViewType) {
        super(view, itemViewType);
        ButterKnife.bind(this, view);
    }

    public void setCardAttributes(Team team, boolean playNonFeatureGifs, GradientDrawable teamColorGradient, int intPrimaryColor, int intSecondaryColor, String regionBackgroundURL, boolean isLightTeam) {

        setBG(teamColorGradient, intPrimaryColor, regionBackgroundURL, isLightTeam);

        duration_indicator_bar.setBackgroundColor(intSecondaryColor);

        //Included in XML layout: teamview_card_type_base_date1_and_region, populated by team view template.
        f1_standard_player.setVisibility(View.GONE);
        f1_standard_video_artwork.setVisibility(View.VISIBLE);
        f1_standard_video_artwork.loadImage(playNonFeatureGifs, mItem.getImageAssetUrl(), team.getPrimaryColor(), null);

        playhead_icon.setImageResource(R.drawable.podcast_white);

        description.setText(mItem.getDescription());
        checkAndAdjustTitleTextSize(title);
        title.setText(mItem.getTitle());

        if (mItem.getContentDuration() == null || mItem.getContentDuration().isEmpty()) {
            durationIndicatorLayout.setVisibility(View.INVISIBLE);
        } else {
            durationIndicatorLayout.setVisibility(View.VISIBLE);
            duration.setText(mItem.getContentDuration());
        }
    }

    @Override
    public String toString() {
        return "ViewHolderTypeAudio" + title.getText();
    }
}
