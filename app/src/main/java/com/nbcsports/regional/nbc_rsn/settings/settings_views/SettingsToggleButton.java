package com.nbcsports.regional.nbc_rsn.settings.settings_views;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.nbcsports.regional.nbc_rsn.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SettingsToggleButton extends RelativeLayout {

    @BindView(R.id.button)
    ImageView button;

    @BindView(R.id.button_background)
    ImageView buttonBackground;

    private static final int TOGGLE_ANIMATION_DURATION = 250;
    private static final int NO_TOGGLE_ANIMATION_DURATION = 0;
    private boolean defaultToggleValue = true;
    private boolean toggleOn = true;
    private ToggleListener listener;
    private ManualToggleListener manualListener;

    public SettingsToggleButton(@NonNull Context context) {
        super(context);
        init();
    }

    public SettingsToggleButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SettingsToggleButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.view_toggle_button_layout, this);
        ButterKnife.bind(this);
    }

    public void setOnToggledChangeListener(ToggleListener toggleListener) {
        //Automatically animates the toggle button on click.
        listener = toggleListener;
        setOnClickListener(null);
        setOnClickListener(getClickListenerWithToggleAnimation());
    }

    public void setOnToggledChangeManualListener(ManualToggleListener toggleListener) {
        //Does not automatically animate the toggle button on click.
        manualListener = toggleListener;
        setOnClickListener(null);
        setOnClickListener(getClickListenerWithoutToggleAnimation());
    }

    public void setToggleDefault(boolean toggleOn) {
        defaultToggleValue = toggleOn;
        this.toggleOn = toggleOn;

        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) button.getLayoutParams();
        if (defaultToggleValue) {
            lp.removeRule(RelativeLayout.ALIGN_PARENT_START);
            lp.addRule(RelativeLayout.ALIGN_PARENT_END);
            button.setLayoutParams(lp);
            toggleOn();
        } else {
            lp.removeRule(RelativeLayout.ALIGN_PARENT_END);
            lp.addRule(RelativeLayout.ALIGN_PARENT_START);
            button.setLayoutParams(lp);
            toggleOff();
        }
    }

    public void toggleOn() {
        toggleOn = true;
        setToggleOn(NO_TOGGLE_ANIMATION_DURATION);
    }

    public void toggleOff() {
        toggleOn = false;
        setToggleOff(NO_TOGGLE_ANIMATION_DURATION);
    }

    public void animateToggleOn() {
        toggleOn = true;
        setToggleOn(TOGGLE_ANIMATION_DURATION);
    }

    public void animateToggleOff() {
        toggleOn = false;
        setToggleOff(TOGGLE_ANIMATION_DURATION);
    }

    public void animateToggle(boolean toggleOn){
        this.toggleOn = toggleOn;
        if (toggleOn){
            setToggleOn(TOGGLE_ANIMATION_DURATION);
        } else {
            setToggleOff(TOGGLE_ANIMATION_DURATION);
        }
    }

    public boolean isToggledOn() {
        return toggleOn;
    }

    private View.OnClickListener getClickListenerWithToggleAnimation() {

        return v -> {
            if (toggleOn) {
                setToggleOff(TOGGLE_ANIMATION_DURATION);
            } else {
                setToggleOn(TOGGLE_ANIMATION_DURATION);
            }
            toggleOn = !toggleOn;
            if (listener != null) {
                listener.onToggleChanged(toggleOn);
            }
        };
    }

    private View.OnClickListener getClickListenerWithoutToggleAnimation() {
        return v -> {
            if (manualListener != null) {
                manualListener.onToggleChanged(!toggleOn);
            }
        };
    }

    private void setToggleOn(int duration) {
        button.animate()
                .translationX(defaultToggleValue ? 0 : getWidth() - button.getWidth())
                .withStartAction(() -> buttonBackground.setColorFilter(null))
                .setDuration(duration)
                .start();
    }

    private void setToggleOff(int duration) {
        button.animate()
                .translationX(defaultToggleValue ? -(getWidth() - button.getWidth()) : 0)
                .withStartAction(() -> buttonBackground.setColorFilter(getResources().getColor(R.color.light_grey_1)))
                .setDuration(duration)
                .start();
    }

    public interface ToggleListener {
        void onToggleChanged(boolean toggledOn);
    }

    public interface ManualToggleListener {
        // ManualToggleListener must call animateToggleOn in order to display an animation (This is for the confirmation)
        void onToggleChanged(boolean toggledOn);
    }
}
