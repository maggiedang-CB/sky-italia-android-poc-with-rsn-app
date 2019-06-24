package com.nbcsports.regional.nbc_rsn.team_feed.components;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nbcsports.regional.nbc_rsn.R;
import com.nbcsports.regional.nbc_rsn.localization.LocalizationManager;
import com.nbcsports.regional.nbc_rsn.utils.DisplayUtils;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

public class ViewHolderTypeFeedPromo extends ViewHolderTypeBase {

    private ConstraintLayout rootViewConstraintLayout;
    private ImageView feedPromoImageView;
    private RelativeLayout feedPromoForegroundRelativeLayout;
    private TextView feedPromoHeaderTextView;

    private float colorPercentage = COLOR_PERCENTAGE_LIGHT_TEAM;
    private float foregroundAlpha = 0.15f;
    private Drawable regionImage;
    private FeedPromoCustomTarget fpcTarget;

    public ViewHolderTypeFeedPromo(View view, int itemViewType) {
        super(view, itemViewType);
        this.rootViewConstraintLayout          = view.findViewById(R.id.root_view_constraint_layout);
        this.feedPromoImageView                = view.findViewById(R.id.feed_promo_image_view);
        this.feedPromoForegroundRelativeLayout = view.findViewById(R.id.feed_promo_foreground_relative_layout);
        this.feedPromoHeaderTextView           = view.findViewById(R.id.feed_promo_header_text_view);
    }

    private void setBG(int intPrimaryColor, String liveFeedPromoImageURL, boolean lightTeam) {
        if (rootViewConstraintLayout != null){
            Drawable whiteBackground = new ColorDrawable(Color.WHITE);
            if(lightTeam) {
                colorPercentage = COLOR_PERCENTAGE_LIGHT_TEAM;
            } else {
                colorPercentage = COLOR_PERCENTAGE_NOT_LIGHT_TEAM;
            }
            Picasso picasso = Picasso.get();
            picasso.setLoggingEnabled(true);
            //To prevent target getting garbage collected
            fpcTarget = new FeedPromoCustomTarget(intPrimaryColor, whiteBackground, colorPercentage);
            picasso.load(liveFeedPromoImageURL).priority(Picasso.Priority.HIGH).into(fpcTarget);
        }
    }

    public void setCardAttributes(int intPrimaryColor, String liveFeedPromoImageURL, boolean isLightTeam) {
        if (LocalizationManager.isInitialized()){
            feedPromoHeaderTextView.setText(LocalizationManager.TeamView.LiveFeedPromo);
        }
        setBG(intPrimaryColor, liveFeedPromoImageURL, isLightTeam);
        feedPromoForegroundRelativeLayout.setBackgroundColor(intPrimaryColor);
        feedPromoForegroundRelativeLayout.setAlpha(foregroundAlpha);
    }

    @Override
    public String toString() {
        return "ViewHolderTypeFeedPromo" + feedPromoHeaderTextView.getText();
    }

    private class FeedPromoCustomTarget implements Target {
        private final int intPrimaryColor;
        private final Drawable whiteBackground;
        private final int colorPercent;

        FeedPromoCustomTarget(int intPrimaryColor, Drawable whiteBackground, float colorPercent) {
            this.intPrimaryColor = intPrimaryColor;
            this.whiteBackground = whiteBackground;
            this.colorPercent = (int)Math.round(colorPercent * 255);
        }

        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            bitmaps = bitmap;
            if(bitmaps != null) {
                regionImage = new BitmapDrawable(mView.getResources(), bitmaps);
                if (regionImage != null) {
                    //Team color layer (Multiply blend mode)
                    int colorFilter  = DisplayUtils.getColorWithAlpha(intPrimaryColor, colorPercent);
                    regionImage.setColorFilter(colorFilter, PorterDuff.Mode.MULTIPLY);
                    //Add in gradient effect
                    LayerDrawable ld = new LayerDrawable(new Drawable[]{whiteBackground, regionImage});
                    feedPromoImageView.setImageDrawable(ld);
                }
            }
        }

        @Override
        public void onBitmapFailed(Exception e, Drawable errorDrawable) {}

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {}
    }

}
