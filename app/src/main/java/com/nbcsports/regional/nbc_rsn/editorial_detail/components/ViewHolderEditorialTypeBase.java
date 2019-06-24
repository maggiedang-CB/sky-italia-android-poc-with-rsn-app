package com.nbcsports.regional.nbc_rsn.editorial_detail.components;

import android.graphics.Color;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.nbcsports.regional.nbc_rsn.R;
import com.nbcsports.regional.nbc_rsn.common.VisibilityListener;
import com.nbcsports.regional.nbc_rsn.editorial_detail.models.EditorialDetailItem;
import com.nbcsports.regional.nbc_rsn.team_feed.components.TeamViewComponentsAdapter;

import butterknife.BindView;

import static android.graphics.PorterDuff.Mode.MULTIPLY;

public class ViewHolderEditorialTypeBase extends RecyclerView.ViewHolder implements VisibilityListener {
    @Nullable
    @BindView(R.id.date)
    TextView date;

    @Nullable
    @BindView(R.id.time)
    TextView time;

    public final View mView;
    public EditorialDetailItem editorialDetailItem;

    public final View date1ContainerView;
    public final TextView date1View;
    public final View rsnName;
    public final TextView regionView;
    private TeamViewComponentsAdapter fragmentLifeCycleListener;
    private final View fOneStandardView;
    private View descriptionContainerBackground;
    private final View descriptionContainer;

    public int itemViewType;

    public ViewHolderEditorialTypeBase(View view, int itemViewType) {
        super(view);

        mView = view;
        this.itemViewType = itemViewType;

        date1ContainerView = (View) view.findViewById(R.id.date1_container);
        date1View = (TextView) view.findViewById(R.id.date1);
        rsnName = (View) view.findViewById(R.id.rsn_name);
        regionView = (TextView) view.findViewById(R.id.region);

        fOneStandardView = view.findViewById(R.id.f1_standard_view);
        descriptionContainer = view.findViewById(R.id.description_container);
        descriptionContainerBackground = view.findViewById(R.id.description_container_background);
    }


    public void setBG(GradientDrawable teamColorGradient, int intPrimaryColor) {
        // Set the card's background to be colored gradient blended (layered) with image.
        if (fOneStandardView != null) {
            Drawable drawable = mView.getResources().getDrawable(R.drawable.rectangle_6_copy_7);
            LayerDrawable ld = new LayerDrawable(new Drawable[]{drawable, teamColorGradient});
            fOneStandardView.setBackground(ld);
        }

        // Set description box background to be colored. It's also layered in xml.
        if (descriptionContainerBackground != null && descriptionContainer != null) {
            descriptionContainer.setBackgroundColor(intPrimaryColor);
            descriptionContainerBackground.setBackgroundColor(intPrimaryColor);
            int color = Color.parseColor("#CC000000");
            PorterDuffColorFilter filter = new PorterDuffColorFilter(color, MULTIPLY);
            descriptionContainerBackground.getBackground().setColorFilter(filter);

        }
    }

    @Override
    public void setVisible(boolean visible) {

    }

    public EditorialDetailItem getItem() {
        return editorialDetailItem;
    }

    public void setItem(EditorialDetailItem editorialDetailItem) {
        this.editorialDetailItem = editorialDetailItem;
    }

    public void bind(EditorialDetailItem editorialDetailItem) {
        this.editorialDetailItem = editorialDetailItem;
    }
}
