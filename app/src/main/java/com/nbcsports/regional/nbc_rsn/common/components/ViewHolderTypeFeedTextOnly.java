package com.nbcsports.regional.nbc_rsn.common.components;

import android.graphics.Color;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Guideline;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import com.nbcsports.regional.nbc_rsn.R;
import com.nbcsports.regional.nbc_rsn.common.Team;
import com.nbcsports.regional.nbc_rsn.team_feed.components.ViewHolderTypeBase;
import com.nbcsports.regional.nbc_rsn.utils.DateFormatUtils;

public class ViewHolderTypeFeedTextOnly extends ViewHolderTypeBase {

    private Team team;

    private View topSeparatorLine;
    private View bottomSeparatorLine;
    private View bottomSpacing;
    private View firstFeedCardBackground;
    private TextView upnextLabel;
    private View firstFeedCardBackgroundExtra;
    private TextView topic_tag_text;
    private TextView title;
    private TextView titleEditorial;
    private View titleEditorialIndicator;
    private View title_indicator;
    private TextView author_and_publish_time;
    // This would be an alternative Author/Publish_time implementation as per the xml layout
    //private TextView author;
    //private TextView publish_time;

    public ViewHolderTypeFeedTextOnly(Team team, View view, int itemViewType) {
        super(view, itemViewType);

        this.team = team;

        firstFeedCardBackground = view.findViewById(R.id.firstFeedCardBackground);
        firstFeedCardBackgroundExtra = view.findViewById(R.id.firstFeedCardBackgroundExtra);
        topic_tag_text = view.findViewById(R.id.topic_tag_text);
        title = view.findViewById(R.id.title);
        titleEditorialIndicator = view.findViewById(R.id.title_editorial_indicator);
        titleEditorial = view.findViewById(R.id.title_editorial);
        title_indicator = view.findViewById(R.id.title_indicator);
        author_and_publish_time = view.findViewById(R.id.author_and_publish_time);
        // This would be an alternative Author/Publish_time implementation as per the xml layout
        //author = view.findViewById( R.id.author);
        //publish_time = view.findViewById( R.id.publish_time );

        topSeparatorLine = view.findViewById(R.id.topSeparatorLineInEditorial);
        bottomSeparatorLine = view.findViewById(R.id.bottomSeparatorLineInEditorial);
        bottomSpacing = view.findViewById(R.id.bottom_space);

        upnextLabel = view.findViewById(R.id.upnext_label);
    }

    public void setCardAttributes(boolean isFirstFeedCard, boolean isInEditorial) {

        String topicTag = mItem.getTag();
        topic_tag_text.setText(topicTag.toUpperCase());

        int teamColor = Color.BLACK;
        if (team != null) {
            teamColor = team.getTeamColor();
        }

        if (isFirstFeedCard) {
            firstFeedCardBackground.setBackgroundColor(teamColor);
            if (firstFeedCardBackgroundExtra != null) {
                firstFeedCardBackgroundExtra.setBackgroundColor(teamColor);
            }
        } else {
            firstFeedCardBackground.setBackgroundColor(Color.TRANSPARENT);
            if (firstFeedCardBackgroundExtra != null) {
                firstFeedCardBackgroundExtra.setBackgroundColor(Color.TRANSPARENT);
            }
        }
        topic_tag_text.setTextColor(teamColor);

        if (isInEditorial) {
            title.setVisibility(View.INVISIBLE);
            title_indicator.setVisibility(View.INVISIBLE);
            titleEditorial.setVisibility(View.VISIBLE);
            titleEditorial.setText(mItem.getTitle());
            titleEditorialIndicator.setVisibility(View.VISIBLE);
            titleEditorialIndicator.setBackgroundColor(teamColor);
        } else {
            title.setVisibility(View.VISIBLE);
            title_indicator.setVisibility(View.INVISIBLE);
            title.setText(mItem.getTitle());
            title_indicator.setBackgroundColor(teamColor);
        }


        String authorAndPublishDate = String.format("%s / %s", mItem.getAuthor(), DateFormatUtils.getTimeAgoText(mItem.getPublishedDate()));
        SpannableString ss = new SpannableString(authorAndPublishDate);
        ss.setSpan(new ForegroundColorSpan(Color.BLACK), 0, mItem.getAuthor().length(), 0);
        ss.setSpan(new ForegroundColorSpan(Color.GRAY), mItem.getAuthor().length() + 1, ss.length(), 0);
        author_and_publish_time.setText(ss);
        // This would be an alternative Author/Publish_time implementation as per the xml layout
        //author.setText(editorialDetailItem.getAuthor());
        //publish_time.setText(editorialDetailItem.getPublishedDate());
    }

    public void setCardAttributesForEditorialDetailFragment() {
        topSeparatorLine.setVisibility(View.VISIBLE);
        upnextLabel.setVisibility(View.VISIBLE);

        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) title.getLayoutParams();
        Guideline guideline = mView.findViewById(R.id.guideline7_editorial);
        params.startToStart = guideline != null ? guideline.getId()
                : mView.getId(); // Align to parent's start position if the guideline is not found.
        title.setLayoutParams(params);

        // As per clarification from C&T on RSNAPP-291 (Android Basic Article Template + Recirculation + Close, Visual QA Bugs),
        // regarding recirculation styling updates:
        // - Make the text smaller on the recirculation title
        // - replace Author Name with a line (same line as exists above recirculation
        boolean doStyleRecirculation = true;
        if (doStyleRecirculation) {
            title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 26.0f); // versus default "36sp" in xml layout
            bottomSeparatorLine.setVisibility(View.VISIBLE);
            bottomSpacing.setVisibility(View.VISIBLE);
            author_and_publish_time.setVisibility(View.GONE);
        }
    }

    @Override
    public String toString() {
        return "ViewHolderTypeFeedTextOnly" + " " + title.getText();
    }
}
