package com.nbcsports.regional.nbc_rsn.team_feed.components;


import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nbcsports.regional.nbc_rsn.R;
import com.nbcsports.regional.nbc_rsn.RsnApplication;
import com.nbcsports.regional.nbc_rsn.common.Constants;
import com.nbcsports.regional.nbc_rsn.common.Team;
import com.nbcsports.regional.nbc_rsn.team_view.PeacockImageView;
import com.nbcsports.regional.nbc_rsn.common.FeedComponent;
import com.nbcsports.regional.nbc_rsn.utils.DateFormatUtils;
import com.nbcsports.regional.nbc_rsn.utils.DisplayUtils;

import timber.log.Timber;

/**
 * Created by arkady on 2018-05-03.
 */
public class ViewHolderTypeFeedStandard extends ViewHolderTypeBase {

    private TeamViewComponentsAdapter teamViewComponentsAdapter;

    private View firstFeedCardBackground;
    private ImageView imageOrGif;
    private PeacockImageView peacockImageView;
    private TextView topic_tag_text;
    private ImageView playhead_icon;
    private TextView title;
    private TextView author_and_publish_time;
    private ImageView peacockPlaceHolder;
    // This would be an alternative Author/Publish_time implementation as per the xml layout
    //private TextView author;
    //private TextView publish_time;

    public ViewHolderTypeFeedStandard(TeamViewComponentsAdapter teamViewComponentsAdapter, View view, int itemViewType) {
        super(view, itemViewType);

        this.teamViewComponentsAdapter = teamViewComponentsAdapter;

        firstFeedCardBackground = view.findViewById(R.id.firstFeedCardBackground);
        peacockImageView = view.findViewById(R.id.peacock_image_view);
        imageOrGif = view.findViewById(R.id.image_or_gif);
        peacockPlaceHolder = view.findViewById(R.id.peacock_place_holder);
        topic_tag_text = view.findViewById(R.id.topic_tag_text);
        playhead_icon = view.findViewById(R.id.playhead_icon);
        title = view.findViewById(R.id.title);
        author_and_publish_time = view.findViewById(R.id.author_and_publish_time);
        // This would be an alternative Author/Publish_time implementation as per the xml layout
        //author = view.findViewById( R.id.author);
        //publish_time = view.findViewById( R.id.publish_time );
    }

    public void setCardAttributes(boolean playNonFeatureGifs, boolean isFirstFeedCard) {

        peacockPlaceHolder.setVisibility(View.INVISIBLE);
        String topicTag = mItem.getTag();
        topic_tag_text.setText(topicTag.toUpperCase());

        Team team = teamViewComponentsAdapter.team;
        int teamColor = Color.BLACK;
        if (team != null) {
            teamColor = team.getTeamColor();
        }

        if (isFirstFeedCard) {
            firstFeedCardBackground.setBackgroundColor(teamColor);
        } else {
            firstFeedCardBackground.setBackgroundColor(Color.TRANSPARENT);
        }
        topic_tag_text.setTextColor(teamColor);

        title.setText(mItem.getTitle());

        String cardType = mItem.getCardType();
        String contentType = mItem.getContentType();
        FeedComponent.Type type = mItem.getType();
        int itemViewType = TeamViewComponentsAdapter.getItemViewType(type, cardType, contentType); // type is expected to be Type.COMPONENT

        // Title text size
        switch (itemViewType) {
            // feed standard
            case TeamViewComponentsAdapter.VIEW_TYPE_Feed_Standard_Show:
            case TeamViewComponentsAdapter.VIEW_TYPE_Feed_Standard_Podcast:
            case TeamViewComponentsAdapter.VIEW_TYPE_Feed_Standard_Radio:
                break;
            default:
                break;
        }

        // show or play image or gif
        String imageAssetUrl = mItem.getImageAssetUrl();

        switch (itemViewType) {
            // feed standard
            case TeamViewComponentsAdapter.VIEW_TYPE_Feed_Standard_Image:
            case TeamViewComponentsAdapter.VIEW_TYPE_Feed_Standard_Video:
            case TeamViewComponentsAdapter.VIEW_TYPE_Feed_Standard_Show:
            case TeamViewComponentsAdapter.VIEW_TYPE_Feed_Standard_Podcast:
            case TeamViewComponentsAdapter.VIEW_TYPE_Feed_Standard_Radio:
                if (team != null) {
                    Timber.d("Team Feed image url is : %s", imageAssetUrl);
                    peacockImageView.loadImage(playNonFeatureGifs, imageAssetUrl, team.getPrimaryColor(), null);
                }
                break;
            default:
                throw new IllegalStateException(
                        "Unsuitable or unknown card type '" + cardType + "' or content type '" + contentType + "'."
                                + " Expected card type is '" + Constants.CARD_TYPE_Feed_Standard + "'.");
        }

        // other settings
        switch (itemViewType) {
            // feed standard
            case TeamViewComponentsAdapter.VIEW_TYPE_Feed_Standard_Image:
                playhead_icon.setVisibility(View.GONE);
                break;
            case TeamViewComponentsAdapter.VIEW_TYPE_Feed_Standard_Video:
            case TeamViewComponentsAdapter.VIEW_TYPE_Feed_Standard_Show:
                playhead_icon.setImageResource(R.drawable.ic_play);
                playhead_icon.setBackgroundColor(teamColor);
                break;
            case TeamViewComponentsAdapter.VIEW_TYPE_Feed_Standard_Podcast:
            case TeamViewComponentsAdapter.VIEW_TYPE_Feed_Standard_Radio:
                playhead_icon.setImageResource(R.drawable.podcast_white);
                //playhead_icon.setColorFilter(Color.WHITE);//TODO: remove this line... since icon is white
                playhead_icon.setBackgroundColor(teamColor);
                break;
            default:
                throw new IllegalStateException(
                        "Unsuitable or unknown card type '" + cardType + "' or content type '" + contentType + "'."
                                + " Expected card type is '" + Constants.CARD_TYPE_Feed_Standard + "'.");
        }

        // Author, Episode, Duration, PublishDate

        //Timber.d("The difference in time is : %s",DateFormatUtils.timeUTCtoAgo("2018-03-01T10:15:30Z"));
        String authorAndEpisodeAndDurationAndPublishDate =
                // for Feed Standard Image
                // and Feed Standard Video
                mItem.getAuthor() + (mItem.getAuthor().isEmpty() ? "" : " / ")
                        // for Feed Standard Show
                        // and Feed Standard Podcast
                        + (mItem.getEpisode().isEmpty() ? "" : mItem.getEpisode() + " / ")
                        // for Feed Standard Video
                        // and Feed Standard Show?
                        // and Feed Standard Podcast?
                        + (mItem.getContentDuration().isEmpty() ? "" :
                        PLACEHOLDER_CLOCK + " " + mItem.getContentDuration() + " / ")
                        // for ...all?
                        + DateFormatUtils.getTimeAgoText(mItem.getPublishedDate());

        SpannableString ss = new SpannableString(authorAndEpisodeAndDurationAndPublishDate);

        if (ss.length() > 0) {
            int authorAndEpisodeLength = mItem.getAuthor().length() + mItem.getEpisode().length();
            ss.setSpan(new ForegroundColorSpan(Color.BLACK), 0, authorAndEpisodeLength, 0);
            ss.setSpan(new ForegroundColorSpan(Color.GRAY), authorAndEpisodeLength + 1, ss.length(), 0);

            int index = ss.toString().indexOf(PLACEHOLDER_CLOCK);
            if (index > (-1)) {
                // If using icon in TextView - set it
                Drawable d = RsnApplication.getInstance().getResources().getDrawable(
                        R.drawable.time_symbol_gray,
                        RsnApplication.getInstance().getTheme()
                ); // API 21
                int size = DisplayUtils.dpToPx(11);
                d.setBounds(0, 0, size, size); //d.getIntrinsicWidth(), d.getIntrinsicHeight());
                ImageSpan span = new ImageSpan(d, ImageSpan.ALIGN_BASELINE);
                ss.setSpan(span, index, index + PLACEHOLDER_CLOCK.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            }
        }
        author_and_publish_time.setText(ss);
    }

    @Override
    public String toString() {
        return "ViewHolderTypeFeedStandard" + " " + title.getText();
    }
}
