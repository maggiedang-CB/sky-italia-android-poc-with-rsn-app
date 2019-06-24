package com.nbcsports.regional.nbc_rsn.common;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;

import timber.log.Timber;

public class RecyclerVisibilityScrollListener extends RecyclerView.OnScrollListener {

    private boolean wasVisible;

    public RecyclerVisibilityScrollListener() {
        wasVisible = false;
    }

    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        super.onScrollStateChanged(recyclerView, newState);

        LinearLayoutManager lm = ((LinearLayoutManager) recyclerView.getLayoutManager());
        int first = lm.findFirstVisibleItemPosition();
        int last = lm.findLastVisibleItemPosition();
        Timber.d("onScrollStateChanged first: %s ,last: %s", first, last);

        if (recyclerView.getAdapter() != null) {
            Range<Integer> total = Range.closed(0, recyclerView.getAdapter().getItemCount());
            ImmutableList<Integer> totalList = ContiguousSet.create(total, DiscreteDomain.integers()).asList();
            List<Integer> totalFinal = new ArrayList<>(totalList);

            // Tell the view holder it's visible on screen. This is different than using
            // RecyclerView.Adapter.onViewAttachedToWindow() because that will be called as soon as the
            // view holder is attached to the recyclerview which can happen off screen.
            Range<Integer> range = Range.closed(first, last);
            ImmutableList<Integer> positionsInView = ContiguousSet.create(range, DiscreteDomain.integers()).asList();
            for (Integer i : positionsInView) {

                VisibilityListener viewHolder = (VisibilityListener) recyclerView.findViewHolderForAdapterPosition(i);

                if (viewHolder != null) {

                    if (wasVisible) {
                        // if visibility is the same as last scroll event ignore it.
                        // this is to prevent implementation in setVisible gets fired multiple times.
                        return;
                    }

                    //viewHolder.setVisible(true);

                    wasVisible = true;
                }
                totalFinal.remove(i);
            }

        }
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
    }
}
