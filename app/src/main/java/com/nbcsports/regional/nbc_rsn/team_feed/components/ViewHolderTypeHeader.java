package com.nbcsports.regional.nbc_rsn.team_feed.components;

import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nbcsports.regional.nbc_rsn.R;
import com.nbcsports.regional.nbc_rsn.common.Team;

/**
 * Created by pengzhiquah on 2018-04-19.
 */
public class ViewHolderTypeHeader extends ViewHolderTypeBase {

    private TextView headerTeam;
    private View headerFlagTopColor;
    private View headerFlagBottomColor;
    private ImageView headerPeacockLogo;

    public ViewHolderTypeHeader(View view, int itemViewType) {
        super(view, itemViewType);
        headerTeam = view.findViewById(R.id.header_team);
        headerFlagTopColor = view.findViewById(R.id.header_flag_top_color);
        headerFlagBottomColor = view.findViewById(R.id.header_flag_bottom_color);
        headerPeacockLogo = view.findViewById(R.id.header_peacock);
    }

    @Override
    public String toString() {
        return "ViewHolderTypeHeader";
    }

    public void setHeaderTextAndColors(Team team) {
        // Team name
        headerTeam.setText(team.getDisplayName());
        // Team flag
        if (headerFlagTopColor != null) {
            headerFlagTopColor.setBackgroundColor(Color.parseColor(team.getSecondaryColor()));
        }
        if (headerFlagTopColor != null) {
            headerFlagBottomColor.setBackgroundColor(Color.parseColor(team.getPrimaryColor()));
        }
        // Peacock logo color treatment.
        // For dark profile teams = primary color
        // For light profile teams = secondary color
        headerPeacockLogo.setColorFilter(Color.parseColor(
                team.getLightProfileTeam() ? team.getSecondaryColor() : team.getPrimaryColor()));
    }
}
