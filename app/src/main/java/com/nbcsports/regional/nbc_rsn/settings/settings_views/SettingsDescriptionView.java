package com.nbcsports.regional.nbc_rsn.settings.settings_views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.util.AttributeSet;
import android.widget.TextView;

import com.nbcsports.regional.nbc_rsn.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SettingsDescriptionView extends ConstraintLayout {

    @BindView(R.id.debug_description)
    TextView debugDescription;

    public SettingsDescriptionView(Context context) {
        super(context);
        init();
    }

    public SettingsDescriptionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public SettingsDescriptionView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init() {
        inflate(getContext(), R.layout.settings_debug_subtext, this);
        ButterKnife.bind(this);
    }

    private void init(Context context, AttributeSet attrs) {
        inflate(getContext(), R.layout.settings_debug_subtext, this);
        ButterKnife.bind(this);
        updateViewStyle(context, attrs);
    }

    private void updateViewStyle(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SettingsDescriptionView);
        String text = typedArray.getString(R.styleable.SettingsDescriptionView_descriptionText);
        String textColor = typedArray.getString(R.styleable.SettingsDescriptionView_descriptionColor);
        float textAlpha = typedArray.getFloat(R.styleable.SettingsDescriptionView_descriptionAlpha, 0.65f);
        typedArray.recycle();
        debugDescription.setText(text != null ? text : "");
        debugDescription.setTextColor(textColor != null ? Color.parseColor(textColor) : Color.WHITE);
        debugDescription.setAlpha(textAlpha);
    }

    public void setDescription(String description) {
        debugDescription.setText(description);
    }

    public TextView getDebugDescription() {
        return debugDescription;
    }
}
