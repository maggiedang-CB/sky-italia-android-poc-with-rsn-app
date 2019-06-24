package com.nbcsports.regional.nbc_rsn.settings.settings_views;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.TextView;

import com.nbcsports.regional.nbc_rsn.R;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SettingsHeaderView extends ConstraintLayout {


    @BindView(R.id.setting_background)
    ImageView settingsBackground;

    @BindView(R.id.settings_label)
    TextView settingLabel;

    @BindView(R.id.setting_background_container)
    ImageView gradient;

    private final String GRADIENT_START = "#4C000000";
    private final String GRADIENT_END = "#FF000000";

    public SettingsHeaderView(Context context) {
        super(context);
        init();
    }

    public SettingsHeaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SettingsHeaderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.settings_header, this);
        ButterKnife.bind(this);
        GradientDrawable filter = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{Color.parseColor(GRADIENT_START), Color.parseColor(GRADIENT_END)});
        gradient.setBackground(filter);
    }

    public void updateBackgroundImage(String imageUrl) {
        if (!imageUrl.isEmpty()) {
            Picasso.get().load(imageUrl).fit().into(settingsBackground);
        }
    }

    public void updateSettingsLabel(String label) {
        if (!label.isEmpty()) {
            settingLabel.setText(label);
        }
    }

}
