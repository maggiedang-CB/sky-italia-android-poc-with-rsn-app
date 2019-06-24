package com.nbcsports.regional.nbc_rsn.team_view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.nbcsports.regional.nbc_rsn.R;
import com.nbcsports.regional.nbc_rsn.RsnApplication;
import com.nbcsports.regional.nbc_rsn.utils.ColorUtil;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import butterknife.BindView;
import butterknife.ButterKnife;
import lombok.Getter;

public class PeacockImageView extends ConstraintLayout {

    @Getter
    @BindView(R.id.image_or_gif)
    ImageView image;

    @Getter
    @BindView(R.id.peacock_place_holder)
    ImageView peacock;

    private static final ImageView.ScaleType[] supportScaleTypes = {
            ImageView.ScaleType.CENTER_CROP,
            ImageView.ScaleType.CENTER_INSIDE
    };

    public PeacockImageView(Context context) {
        super(context);
        init();
    }

    public PeacockImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
        initAttrs(context, attrs);
    }

    public PeacockImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
        initAttrs(context, attrs);
    }

    private void init() {
        inflate(getContext(), R.layout.peacock_error_image_view, this);
        ButterKnife.bind(this);
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.PeacockImageView,
                0, 0
        );

        try {
            int scaleTypeInd = a.getInt(R.styleable.PeacockImageView_scaleType, -1);
            if (scaleTypeInd >= 0 && scaleTypeInd < supportScaleTypes.length) {
                image.setScaleType(supportScaleTypes[scaleTypeInd]);
            }
        } finally {
            a.recycle();
        }
    }

    public void loadImage(boolean playNonFeatureGifs, String imageUrl, String color, PicassoLoadListener loadListener) {

        /*
        * This is used to fix an issue on TeamFeedFragment where the imageUrl below is null or empty.
        * Since view holders will be reused by the RecyclerView, so that if the imageUrl below is null or empty,
        * the image below will still using the old image that was set before reuse.
        *
        * So the purpose of the following line is to set the PeacockImageView to its default state.
        * Especially remove the old image by calling 'image.setImageResource(android.R.color.transparent);'
        * */
        showPlaceHolderImage(color);

        if (imageUrl == null || imageUrl.isEmpty()){
            if (loadListener != null){
                peacock.setVisibility(View.INVISIBLE);
                loadListener.onError(new Exception());
            }
            return;
        }

        if (playNonFeatureGifs && imageUrl.endsWith(".gif") ) {
            RequestOptions options = new RequestOptions()
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .priority(Priority.HIGH);
            RsnApplication mApp = RsnApplication.getInstance();
            //Replace all empty spaces (mostly space at the end) to ensure glide function
            imageUrl = imageUrl.replaceAll("\\s", "");
            Glide.with(mApp).load(imageUrl).apply(options).listener(new RequestListener<Drawable>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                    peacock.setVisibility(View.VISIBLE);
                    image.setBackgroundColor(Color.parseColor(color));
                    return false;
                }

                @Override
                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                    peacock.setVisibility(View.INVISIBLE);
                    if (loadListener != null) {
                        loadListener.onSuccess();
                    }
                    return false;
                }
            }).into(image);
        } else {
            Drawable placeholderDrawable = ColorUtil.INSTANCE.setLayerDrawableColor(R.drawable.image_placeholder, Color.parseColor(color));
            peacock.setVisibility(View.VISIBLE);

            image.setVisibility(View.VISIBLE);
            RequestCreator creator = Picasso.get()
                    .load(imageUrl)
                    .fit();
            if (image.getScaleType() == ImageView.ScaleType.CENTER_INSIDE) {
                creator.centerInside();
            } else {
                creator.centerCrop();
            }
            creator.placeholder(placeholderDrawable)
                    .into(image, new Callback() {
                        @Override
                        public void onSuccess() {
                            peacock.setVisibility(View.INVISIBLE);
                            if (loadListener != null) {
                                loadListener.onSuccess();
                            }
                        }

                        @Override
                        public void onError(Exception e) {
                            if (loadListener != null) {
                                loadListener.onError(e);

                            } else {
                                showPlaceHolderImage(color);
                            }
                        }
                    });
        }
    }

    /**
     * This loadImage method is similar to the above one, but there are some difference:
     * 1. At the very beginning, it clears the image view
     * 2. It uses custom context instead of application context, because Glide will subscribes to
     *    the activity's or fragment's lifecycle in order to provide better memory management
     * 3. The clearOnDetach method is called
     *
     * @param customContext
     * @param playNonFeatureGifs
     * @param imageUrl
     * @param color
     * @param loadListener
     */
    public void loadImage(Context customContext, boolean playNonFeatureGifs, String imageUrl, String color, PicassoLoadListener loadListener) {

        // Fixes an issue where on RecyclerView, when ViewHolder being reused,
        // image may show the old gif
        Glide.with(customContext).clear(image);

        /*
         * This is used to fix an issue on TeamFeedFragment where the imageUrl below is null or empty.
         * Since view holders will be reused by the RecyclerView, so that if the imageUrl below is null or empty,
         * the image below will still using the old image that was set before reuse.
         *
         * So the purpose of the following line is to set the PeacockImageView to its default state.
         * Especially remove the old image by calling 'image.setImageResource(android.R.color.transparent);'
         * */
        showPlaceHolderImage(color);

        if (imageUrl == null || imageUrl.isEmpty()){
            if (loadListener != null){
                peacock.setVisibility(View.INVISIBLE);
                loadListener.onError(new Exception());
            }
            return;
        }

        if (customContext != null && playNonFeatureGifs && imageUrl.endsWith(".gif")) {
            RequestOptions options = new RequestOptions()
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .priority(Priority.HIGH);
            //Replace all empty spaces (mostly space at the end) to ensure glide function
            imageUrl = imageUrl.replaceAll("\\s", "");
            Glide.with(customContext).load(imageUrl).apply(options).listener(new RequestListener<Drawable>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                    peacock.setVisibility(View.VISIBLE);
                    image.setBackgroundColor(Color.parseColor(color));
                    return false;
                }

                @Override
                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                    peacock.setVisibility(View.INVISIBLE);
                    if (loadListener != null) {
                        loadListener.onSuccess();
                    }
                    return false;
                }
            }).into(image).clearOnDetach();
        } else {
            Drawable placeholderDrawable = ColorUtil.INSTANCE.setLayerDrawableColor(R.drawable.image_placeholder, Color.parseColor(color));
            peacock.setVisibility(View.VISIBLE);

            image.setVisibility(View.VISIBLE);
            RequestCreator creator = Picasso.get()
                    .load(imageUrl)
                    .fit();
            if (image.getScaleType() == ImageView.ScaleType.CENTER_INSIDE) {
                creator.centerInside();
            } else {
                creator.centerCrop();
            }
            creator.placeholder(placeholderDrawable)
                    .into(image, new Callback() {
                        @Override
                        public void onSuccess() {
                            peacock.setVisibility(View.INVISIBLE);
                            if (loadListener != null) {
                                loadListener.onSuccess();
                            }
                        }

                        @Override
                        public void onError(Exception e) {
                            if (loadListener != null) {
                                loadListener.onError(e);

                            } else {
                                showPlaceHolderImage(color);
                            }
                        }
                    });
        }
    }

    /***
     * Show's the team's color as the background, and a peacock on the right top corner.
     * If the team's color is invalid, this method uses a black background.
     * @param teamColor - Team's primary color
     */
    private void showPlaceHolderImage(String teamColor){
        int colorInt;
        peacock.setVisibility(View.VISIBLE);
        if(teamColor == null || teamColor.isEmpty()) {
            colorInt = Color.BLACK;
        } else {
            colorInt = Color.parseColor(teamColor);
        }
        image.setBackgroundColor(colorInt);
        // Adding this will remove the image resource from the image view
        // in order to show the background color
        image.setImageResource(android.R.color.transparent);
    }
}
