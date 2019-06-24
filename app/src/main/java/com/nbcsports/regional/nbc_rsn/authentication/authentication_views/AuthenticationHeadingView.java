package com.nbcsports.regional.nbc_rsn.authentication.authentication_views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.util.AttributeSet;
import android.widget.TextView;

import com.nbcsports.regional.nbc_rsn.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AuthenticationHeadingView extends ConstraintLayout {

    @BindView(R.id.heading_description)
    TextView textView;

    public AuthenticationHeadingView(Context context) {
        super(context);
        init();
    }

    public AuthenticationHeadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public AuthenticationHeadingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init() {
        inflate(getContext(), R.layout.authentication_heading, this);
        ButterKnife.bind(this);
    }

    private void init(Context context, AttributeSet attrs) {
        inflate(getContext(), R.layout.authentication_heading, this);
        ButterKnife.bind(this);
        updateViewStyle(context, attrs);
    }

    private void updateViewStyle(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SettingsHeadingView);
        String text = typedArray.getString(R.styleable.SettingsHeadingView_headingText);
        String textColor = typedArray.getString(R.styleable.SettingsHeadingView_headingTextColor);
        float textAlpha = typedArray.getFloat(R.styleable.SettingsHeadingView_headingTextAlpha, 1.0f);
        typedArray.recycle();
        textView.setText(text != null ? text : "");
        textView.setTextColor(textColor != null ? Color.parseColor(textColor) : Color.BLACK);
        textView.setAlpha(textAlpha);
    }
}
