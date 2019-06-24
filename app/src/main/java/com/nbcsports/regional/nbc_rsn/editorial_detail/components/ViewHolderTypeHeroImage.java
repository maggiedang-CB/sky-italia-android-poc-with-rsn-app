package com.nbcsports.regional.nbc_rsn.editorial_detail.components;

import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nbcsports.regional.nbc_rsn.R;
import com.nbcsports.regional.nbc_rsn.common.Team;
import com.nbcsports.regional.nbc_rsn.editorial_detail.models.EditorialDetailItem;
import com.nbcsports.regional.nbc_rsn.localization.LocalizationManager;
import com.nbcsports.regional.nbc_rsn.team_feed.components.FragmentLifeCycleListener;
import com.nbcsports.regional.nbc_rsn.team_view.PeacockImageView;
import com.nbcsports.regional.nbc_rsn.utils.DateFormatUtils;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import lombok.Getter;
import timber.log.Timber;

/**
 * Created by arkady on 2018-05-21.
 */
public class ViewHolderTypeHeroImage extends ViewHolderEditorialTypeBase {

    private final static String BR_TAG                = "<br>";
    private final static String P_TAG                 = "</*p>";
    private final static String NEW_LINE_CHAR_PATTERN = "(\r)?\n";

    private FragmentLifeCycleListener.Interface editorialDetailComponentsAdapter;
    private Team team;

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

    @BindView(R.id.hero_image)
    PeacockImageView heroImage;

    @Getter @BindView(R.id.share_button)
    public ImageView shareButton;

    @Getter @BindView(R.id.exit_button)
    public ImageView exitButton;

    public ViewHolderTypeHeroImage(FragmentLifeCycleListener.Interface editorialDetailComponentsAdapter, View view, Team team, int itemViewType) {
        super(view, itemViewType);
        ButterKnife.bind(this, view);
        this.team = team;
        this.editorialDetailComponentsAdapter = editorialDetailComponentsAdapter;
    }

    public void updateView(boolean playNonFeatureGifs) {
        if (editorialDetailItem == null) return;

        date.setText(DateFormatUtils.getMonthDayYearText(editorialDetailItem.getPublishedDate()).toUpperCase());
        time.setText(DateFormatUtils.getHourMinText(editorialDetailItem.getPublishedDate()));

        // Remove html tags from display text
        String rawDisplayText = editorialDetailItem.getHeroText();
        rawDisplayText = rawDisplayText.replaceAll(NEW_LINE_CHAR_PATTERN, "")
                .replaceAll(BR_TAG, "").replaceAll(P_TAG, "");

        //date.setText(DateFormatUtils.getMonthDayYearText(editorialDetailItem.getPublishedDate()).toUpperCase());
        //time.setText(DateFormatUtils.getHourMinText(editorialDetailItem.getPublishedDate()));
        title.setText(rawDisplayText);

        String formattedAuthor = String.format("BY %s", editorialDetailItem.getAuthor());
        if (LocalizationManager.isInitialized()) {
            formattedAuthor = LocalizationManager.Common.getAuthor(editorialDetailItem.getAuthor());
        }

        authorName.setText(formattedAuthor);
        if (!TextUtils.isEmpty(editorialDetailItem.getAuthorImage())) {
            Picasso.get()
                    .load(editorialDetailItem.getAuthorImage())
                    .error(R.drawable.ic_peacock)
                    .into(authorImage);
        }


        if(team != null) {
            Timber.d("play non feature gif is : %s", playNonFeatureGifs);
            heroImage.loadImage(playNonFeatureGifs, editorialDetailItem.getComponentAssetUrl(), team.getPrimaryColor(), null);
        }

        if (team.getPrimaryColor() != null && !team.getPrimaryColor().isEmpty()) {
            headerFlagTopColor.setBackgroundColor(Color.parseColor(team.getPrimaryColor()));
        }

        if (team.getSecondaryColor() != null && !team.getSecondaryColor().isEmpty()) {
            headerFlagBottomColor.setBackgroundColor(Color.parseColor(team.getSecondaryColor()));
        }
    }

    @Override
    public void bind(EditorialDetailItem editorialDetailItem) {
        super.bind(editorialDetailItem);
    }
}