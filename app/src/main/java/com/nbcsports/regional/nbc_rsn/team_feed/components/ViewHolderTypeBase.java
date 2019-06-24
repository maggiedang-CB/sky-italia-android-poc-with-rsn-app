package com.nbcsports.regional.nbc_rsn.team_feed.components;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nbcsports.regional.nbc_rsn.R;
import com.nbcsports.regional.nbc_rsn.common.Constants;
import com.nbcsports.regional.nbc_rsn.common.VisibilityListener;
import com.nbcsports.regional.nbc_rsn.common.FeedComponent;
import com.nbcsports.regional.nbc_rsn.utils.DisplayUtils;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import butterknife.ButterKnife;
import timber.log.Timber;

import static android.graphics.PorterDuff.Mode.MULTIPLY;

public class ViewHolderTypeBase extends RecyclerView.ViewHolder implements VisibilityListener {

    public final View mView;
    public FeedComponent mItem;

    View date1ContainerView;

    TextView date1View;

    View rsnName;

    TextView regionView;

    ImageView fOneStandardViewImage;

    ImageView fOneStandardViewImageGradient;

    View fOneStandardView;

    View descriptionContainer;

    View descriptionContainerBackground;

    public Bitmap bitmaps;
    private float colorPercentage = COLOR_PERCENTAGE_LIGHT_TEAM;
    private Drawable regionImage;
    private TeamViewComponentsAdapter fragmentLifeCycleListener;
    public CustomTarget target;
    private final int TRANSPARENCY = (int) Math.round(255 * 0.8);

    public static final float COLOR_PERCENTAGE_LIGHT_TEAM = 1.0f;
    public static final float COLOR_PERCENTAGE_NOT_LIGHT_TEAM = 0.6f;
    /**
     * Placeholder text for the clock icon. Use {@link ImageSpan} to insert the icon.
     */
    protected static final String PLACEHOLDER_CLOCK = "{ic_clock}";

    public int itemViewType;

    public ViewHolderTypeBase(View view, int itemViewType) {

        super(view);

        ButterKnife.bind(view);

        mView = view;
        regionImage = null;
        date1View = view.findViewById(R.id.date1);
        regionView = (TextView) view.findViewById(R.id.region);
        date1ContainerView = view.findViewById(R.id.date1_container);
        rsnName = view.findViewById(R.id.rsn_name);
        //The bottom 5 is not using butterKnife due to the picasso
        fOneStandardViewImage = (ImageView) view.findViewById(R.id.f1_standard_view_background);
        fOneStandardViewImageGradient = (ImageView) view.findViewById(R.id.f1_standard_view_gradient);
        fOneStandardView = view.findViewById(R.id.f1_standard_view);
        descriptionContainer = view.findViewById(R.id.description_container);
        descriptionContainerBackground = view.findViewById(R.id.description_container_background);

        this.itemViewType = itemViewType;
    }

    public void setBG(GradientDrawable teamColorGradient, int intPrimaryColor, String regionBackgroundURL, boolean lightTeam) {
        // Set the card's background to be colored gradient blended (layered) with image.
        if (fOneStandardView != null) {
            Drawable whiteBackground = new ColorDrawable(Color.WHITE);
            if (lightTeam) {
                colorPercentage = COLOR_PERCENTAGE_LIGHT_TEAM;
            } else {
                colorPercentage = COLOR_PERCENTAGE_NOT_LIGHT_TEAM;
            }
            Picasso picasso = Picasso.get();
            picasso.setLoggingEnabled(true);
            //To prevent target getting garbage collected
            target = new CustomTarget(intPrimaryColor, whiteBackground, colorPercentage);
            fOneStandardViewImage.post(() -> picasso.load(regionBackgroundURL).priority(Picasso.Priority.HIGH).into(target));
            //A separate imageView is required, since the gradient does not start from the top (Confirmed with UI and IOS Dev)
            fOneStandardViewImageGradient.setImageDrawable(teamColorGradient);
        }

        // Set description box background to be colored. It's also layered in xml.
        if (descriptionContainerBackground != null && descriptionContainer != null) {
            descriptionContainer.setBackgroundColor(intPrimaryColor);
            descriptionContainerBackground.setBackgroundColor(intPrimaryColor);
            int color = Color.parseColor("#CC000000");
            PorterDuffColorFilter filter = new PorterDuffColorFilter(color, MULTIPLY);
            descriptionContainerBackground.getBackground().setColorFilter(filter);
        }
    }

    class CustomTarget implements Target {

        private final int intPrimaryColor;
        private final Drawable whiteBackground;
        private final int colorPercent;

        CustomTarget(int intPrimaryColor, Drawable whiteBackground, float colorPercent) {
            this.intPrimaryColor = intPrimaryColor;
            this.whiteBackground = whiteBackground;
            this.colorPercent = (int) Math.round(colorPercent * 255);
        }

        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            bitmaps = bitmap;
            if (bitmaps != null) {
                regionImage = new BitmapDrawable(mView.getResources(), bitmaps);
                if (regionImage != null) {
                    //80% transparency
                    regionImage.setAlpha(TRANSPARENCY);
                    //Team color layer (Multiply blend mode)
                    int colorFilter = DisplayUtils.getColorWithAlpha(intPrimaryColor, colorPercent);
                    regionImage.setColorFilter(colorFilter, PorterDuff.Mode.MULTIPLY);
                    //Add in gradient effect
                    LayerDrawable ld = new LayerDrawable(new Drawable[]{whiteBackground, regionImage});
                    fOneStandardViewImage.setBackground(ld);
                }
            }
        }

        @Override
        public void onBitmapFailed(Exception e, Drawable errorDrawable) {

        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }
    }

    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            Timber.d("Visible! editorialDetailItem: %s", mItem == null ? "" : mItem.getTitle());
        } else {
            Timber.d("Not visible! editorialDetailItem: %s", mItem == null ? "" : mItem.getTitle());
        }
    }

    public void checkAndAdjustTitleTextSize(TextView title) {
        if (mItem.getTitle().length() > 50) {
            // Set font size as per project "F1 Long Title" in Zeplin
            title.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 42.0f); // Note: not COMPLEX_UNIT_SP, to be consistent with bias
            // Adjust app:layout_constraintVertical_bias, to ensure the title is aligned at bottom of box for F1 Standard
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) title.getLayoutParams();
            params.verticalBias = 0.105f;
            title.setLayoutParams(params);
        }
    }

    public void addPageChangeListener() {

    }

    public void removePageChangeListener() {

    }

    public int getTRANSPARENCY() {
        return TRANSPARENCY;
    }

    public void bind() {

    }

    protected int getContentIcon() {
        switch (mItem.getContentType()) {
            case Constants.CONTENT_TYPE_VIDEO:
                return R.drawable.ic_play;
            case Constants.CONTENT_TYPE_AUDIO:
                return R.drawable.podcast_white;
            default:
                return 0;
        }
    }
}
