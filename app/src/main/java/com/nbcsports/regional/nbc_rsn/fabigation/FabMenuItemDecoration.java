package com.nbcsports.regional.nbc_rsn.fabigation;

import android.graphics.Rect;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

import com.nbcsports.regional.nbc_rsn.utils.DisplayUtils;
import com.nbcsports.regional.nbc_rsn.utils.PreferenceUtils;

public class FabMenuItemDecoration extends RecyclerView.ItemDecoration {

    private float offset;

    public FabMenuItemDecoration(float offset) {
        this.offset = offset;
    }

    private boolean isRTL() {
        return PreferenceUtils.INSTANCE.getBoolean("_isFabRtl", false);
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);

        int screenWidth = DisplayUtils.getScreenWidth(parent.getContext());
        int endCardPadding = (int) (screenWidth * offset);
        int itemCount = parent.getAdapter().getItemCount();
        int position = parent.getChildAdapterPosition(view);

        if (position == 0 && isRTL() || (position == itemCount - 1 && !isRTL())) {
            outRect.left = 0;
            outRect.right = endCardPadding;
        } else if ((position == 0 && !isRTL()) || (position == itemCount - 1 && isRTL())) {
            outRect.left = endCardPadding;
            outRect.right = 0;
        }
    }
}
