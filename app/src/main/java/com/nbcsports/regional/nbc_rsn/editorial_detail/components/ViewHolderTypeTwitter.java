package com.nbcsports.regional.nbc_rsn.editorial_detail.components;

import android.content.Context;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.view.View;
import android.widget.FrameLayout;

import com.nbcsports.regional.nbc_rsn.R;
import com.nbcsports.regional.nbc_rsn.editorial_detail.models.EditorialDetailItem;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.tweetui.TweetUtils;
import com.twitter.sdk.android.tweetui.TweetView;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class ViewHolderTypeTwitter extends ViewHolderEditorialTypeBase {

    private Context context;

    @BindView(R.id.component_editorial_twitter_root_constraint_layout)
    ConstraintLayout rootConstraintLayout;

    @BindView(R.id.component_editorial_twitter_frame_layout)
    FrameLayout twitterFrameLayout;

    public ViewHolderTypeTwitter(Context context, View view, int itemViewType) {
        super(view, itemViewType);
        ButterKnife.bind(this, view);
        this.context = context;
    }

    public void prepareTwitterAndShow() {
        long tweetIdLong = 0;
        try {
            String tweetIdString = editorialDetailItem.getTweetId();
            if (tweetIdString != null) {
                tweetIdString = tweetIdString.replaceAll("L", "")
                        .replaceAll("l", "");
            }
            tweetIdLong = Long.parseLong(tweetIdString);
        } catch (NumberFormatException e) {
            Timber.e("NumberFormatException: %s", e.getLocalizedMessage());
        }
        TweetUtils.loadTweet(tweetIdLong, new Callback<Tweet>() {
            @Override
            public void success(Result<Tweet> result) {
                rootConstraintLayout.setVisibility(View.VISIBLE);
                twitterFrameLayout.removeAllViews();
                twitterFrameLayout.addView(new TweetView(context, result.data,
                        R.style.tw__TweetLightStyle));
            }

            @Override
            public void failure(TwitterException exception) {
                rootConstraintLayout.setVisibility(View.GONE);
                twitterFrameLayout.removeAllViews();
                Timber.e("TwitterException: %s", exception.getLocalizedMessage());
            }
        });
    }

    @Override
    public void bind(EditorialDetailItem editorialDetailItem) {
        super.bind(editorialDetailItem);
        prepareTwitterAndShow();
    }
}
