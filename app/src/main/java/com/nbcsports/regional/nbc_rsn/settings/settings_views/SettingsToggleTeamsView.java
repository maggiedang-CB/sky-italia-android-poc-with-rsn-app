package com.nbcsports.regional.nbc_rsn.settings.settings_views;

import android.content.Context;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.TextView;

import com.nbcsports.regional.nbc_rsn.R;
import com.nbcsports.regional.nbc_rsn.localization.LocalizationManager;

import butterknife.BindView;
import butterknife.ButterKnife;
import lombok.Getter;

public class SettingsToggleTeamsView extends ConstraintLayout {

    private final int ROTATION_DEGREES_ARROW_DOWN = 0;
    private final int ROTATION_DEGREES_ARROW_UP = 180;
    private final int ROTATION_ANIMATION_DURATION = 300; //Milliseconds

    @Getter
    @BindView(R.id.setting_view_more_team_text_view)
    TextView viewMoreText;

    @BindView(R.id.setting_view_more_team_icon)
    ImageView viewMoreIcon;

    public SettingsToggleTeamsView(Context context) {
        super(context);
        init();
    }

    public SettingsToggleTeamsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SettingsToggleTeamsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.settings_toggle_teams, this);
        ButterKnife.bind(this);
    }

    public void updateText(boolean isViewingMoreTeams) {
        if (isViewingMoreTeams) {
            viewMoreText.setText(LocalizationManager.Settings.ViewLess);
        } else {
            viewMoreText.setText(LocalizationManager.Settings.ViewMore);
        }
    }

    public void rotateIcon(boolean isViewingMore) {
        viewMoreIcon.animate()
                .rotation(isViewingMore ? ROTATION_DEGREES_ARROW_UP : ROTATION_DEGREES_ARROW_DOWN)
                .setDuration(ROTATION_ANIMATION_DURATION)
                .start();
    }
}
