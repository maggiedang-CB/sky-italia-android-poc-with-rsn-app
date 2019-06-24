package com.nbcsports.regional.nbc_rsn.editorial_detail.components;

import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nbcsports.regional.nbc_rsn.R;
import com.nbcsports.regional.nbc_rsn.common.Team;

/**
 * Created by arkady on 2018-05-21.
 */
public class ViewHolderEditorialTypeHeader extends ViewHolderEditorialTypeBase {

    private TextView headerTeam;
    private View headerFlagTopColor;
    private View headerFlagBottomColor;
    private ImageView headerPeacockLogo;

    public ViewHolderEditorialTypeHeader(View view, int itemViewType) {
        super(view, itemViewType);
        headerTeam = view.findViewById(R.id.header_team);
        headerFlagTopColor = view.findViewById(R.id.header_flag_top_color);
        headerFlagBottomColor = view.findViewById(R.id.header_flag_bottom_color);
        headerPeacockLogo = view.findViewById(R.id.header_peacock);
    }

    @Override
    public String toString() {
        return "ViewHolderEditorialTypeHeader";
    }

    public void setHeaderTextAndColors(Team team) {
        // Team name
        headerTeam.setText("TODO: collapsed header state");//( team.getDisplayName() );
        headerTeam.setTextSize(12f);
        headerTeam.setTextColor(Color.RED);
        // Team flag
        headerFlagTopColor.setVisibility(View.INVISIBLE);//.setBackgroundColor(Color.parseColor( team.getSecondaryColor() ));
        headerFlagBottomColor.setVisibility(View.INVISIBLE);//.setBackgroundColor(Color.parseColor( team.getPrimaryColor() ));
        // Peacock logo color treatment.
        // For dark profile teams = primary color
        // For light profile teams = secondary color
        headerPeacockLogo.setVisibility(View.INVISIBLE);//.setColorFilter( Color.parseColor(team.getLightProfileTeam() ? team.getSecondaryColor() : team.getPrimaryColor() ) );
    }
}
