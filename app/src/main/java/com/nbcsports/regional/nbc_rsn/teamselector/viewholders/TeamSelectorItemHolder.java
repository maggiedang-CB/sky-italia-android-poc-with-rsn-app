package com.nbcsports.regional.nbc_rsn.teamselector.viewholders;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nbcsports.regional.nbc_rsn.R;
import com.nbcsports.regional.nbc_rsn.common.Team;
import com.squareup.picasso.Picasso;

public class TeamSelectorItemHolder extends RecyclerView.ViewHolder {

    private final static float SELECTED_ALPHA = 1f;
    private final static float DESELECTED_ALPHA = 0.65f;

    private View backgroundColor;
    private ImageView teamLogo;
    private TextView teamCity;
    private TextView teamName;
    private ImageView selectButton;
    private ConstraintLayout teamContainer; // root view

    public TeamSelectorItemHolder(View view) {
        super(view);
        backgroundColor = view.findViewById(R.id.background_color);
        teamLogo = view.findViewById(R.id.team_logo);
        teamCity = view.findViewById(R.id.team_city);
        teamName = view.findViewById(R.id.team_name);
        selectButton = view.findViewById(R.id.select_button);
        teamContainer = view.findViewById(R.id.container);

        deselect(); //Default
    }

    public void bindTo(Team team, boolean selected) {
        if (team == null) return;

        if (!team.getLogoUrl().isEmpty()) {
            Picasso.get().load(team.getLogoUrl()).into(teamLogo);
        }
        if (!team.getDisplayName().isEmpty()) {
            teamName.setText(team.getDisplayName());
        }
        if (!team.getCityName().isEmpty()) {
            teamCity.setText(team.getCityName());
        }
        if (selected) {
            select();
        } else {
            deselect();
        }
    }

    public void select() {
        Picasso.get().load(R.drawable.field_black).into(selectButton);
        backgroundColor.setAlpha(SELECTED_ALPHA);
        teamLogo.setAlpha(SELECTED_ALPHA);
        teamCity.setAlpha(SELECTED_ALPHA);
        teamName.setAlpha(SELECTED_ALPHA);
    }

    public void deselect() {
        Picasso.get().load(R.drawable.plus).into(selectButton);
        backgroundColor.setAlpha(DESELECTED_ALPHA);
        teamLogo.setAlpha(DESELECTED_ALPHA);
        teamCity.setAlpha(DESELECTED_ALPHA);
        teamName.setAlpha(DESELECTED_ALPHA);
    }

    public void setOnClickListener(View.OnClickListener onClickListener){
        teamContainer.setOnClickListener(onClickListener);
    }
}
