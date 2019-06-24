package com.nbcsports.regional.nbc_rsn.settings.settings_views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nbcsports.regional.nbc_rsn.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import lombok.Getter;

public class SettingsActionableView extends ConstraintLayout {

    @Getter
    @BindView(R.id.actionable_description)
    TextView textView;

    @Getter
    @BindView(R.id.toggle_button)
    SettingsToggleButton toggleButton;

    @Getter
    @BindView(R.id.icon_container)
    LinearLayout functionButton;

    @Getter
    @BindView(R.id.actionable_sub_description)
    TextView subDescription;

    @Getter
    @BindView(R.id.function_icon)
    ImageView functionIcon;

    @BindView(R.id.setting_actionable_line)
    View line;

    public SettingsActionableView(Context context) {
        super(context);
        init();
    }

    public SettingsActionableView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public SettingsActionableView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init() {
        inflate(getContext(), R.layout.settings_actionable, this);
        ButterKnife.bind(this);
    }

    private void init(Context context, AttributeSet attrs) {
        inflate(getContext(), R.layout.settings_actionable, this);
        ButterKnife.bind(this);
        updateViewStyle(context, attrs);
    }

    private void updateViewStyle(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SettingsActionableView);
        String text = typedArray.getString(R.styleable.SettingsActionableView_actionableText);
        String textColor = typedArray.getString(R.styleable.SettingsActionableView_actionableTextColor);
        float textAlpha = typedArray.getFloat(R.styleable.SettingsActionableView_actionableTextAlpha, 1.0f);
        boolean isSubTextVisible = typedArray.getBoolean(R.styleable.SettingsActionableView_actionableSubTextEnabled, false);
        Drawable functionIconResource = typedArray.getDrawable(R.styleable.SettingsActionableView_functionIcon);
        boolean isBottomLineVisible = typedArray.getBoolean(R.styleable.SettingsActionableView_actionableUnderLineEnabled, true);

        boolean useToggleButton = typedArray.getBoolean(R.styleable.SettingsActionableView_actionableToggleButtonEnabled, false);
        toggleButton.setVisibility(useToggleButton ? View.VISIBLE : View.GONE);
        functionButton.setVisibility(useToggleButton ? View.GONE : View.VISIBLE);
        subDescription.setVisibility(isSubTextVisible ? View.VISIBLE : View.GONE);
        line.setVisibility(isBottomLineVisible ? View.VISIBLE : View.GONE);

        if(functionIconResource != null) {
            functionIcon.setImageDrawable(functionIconResource);
        } else {
            functionIcon.setImageResource(R.drawable.cancel_exit_copy);
        }

        typedArray.recycle();
        textView.setText(text != null ? text : "");
        textView.setTextColor(textColor != null ? Color.parseColor(textColor) : Color.WHITE);
        textView.setAlpha(textAlpha);
    }
}
