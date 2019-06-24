package com.nbcsports.regional.nbc_rsn.teamselector

import android.graphics.Canvas
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.ItemTouchHelper
import android.view.View

import com.nbcsports.regional.nbc_rsn.utils.ActivityUtils
import com.nbcsports.regional.nbc_rsn.utils.DisplayUtils

class IconTouchHelperCallback internal constructor(private val adapter: TeamIconAdapter, private val overlay: View, private val callback: EventCallback) : ItemTouchHelper.Callback() {

    private var wasDropped = false

    interface EventCallback {
        fun onStart(viewHolder: RecyclerView.ViewHolder?)
        fun onDrop(viewHolder: RecyclerView.ViewHolder?, shouldDelete: Boolean)
        fun onHoverStart(viewHolder: RecyclerView.ViewHolder)
        fun onHoverExit(viewHolder: RecyclerView.ViewHolder)
    }

    override fun isLongPressDragEnabled(): Boolean {
        return true
    }

    override fun isItemViewSwipeEnabled(): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        // not used
    }

    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        return ItemTouchHelper.Callback.makeMovementFlags(
                ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
                        or ItemTouchHelper.UP or ItemTouchHelper.DOWN
                        or ItemTouchHelper.START or ItemTouchHelper.END, 0
        )
    }

    override fun onMove(recyclerView: RecyclerView, source: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        if (source.itemViewType != target.itemViewType
                || source.adapterPosition == -1
                || target.adapterPosition == -1)
            return false

        adapter.onItemMove(source.adapterPosition, target.adapterPosition)

        return true
    }

    override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)

        if (wasDropped) {
            wasDropped = false

            if (DisplayUtils.isViewOverlapping(viewHolder.itemView, overlay)) {
                viewHolder.itemView.visibility = View.INVISIBLE
                callback.onDrop(viewHolder, true)
            } else {
                callback.onDrop(viewHolder, false)
            }

            // resize everything after a drop
            viewHolder.itemView.let {
                it.animate().scaleX(1f)
                it.animate().scaleY(1f)
            }

        } else {
            if (DisplayUtils.isViewOverlapping(viewHolder.itemView, overlay)) {
                callback.onHoverStart(viewHolder)
            } else {
                callback.onHoverExit(viewHolder)
            }
        }
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        super.onSelectedChanged(viewHolder, actionState)

        // This is called when the user drops the view
        if (actionState == ItemTouchHelper.ACTION_STATE_IDLE) {
            wasDropped = true

        } else if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
            viewHolder?.itemView?.let {
                ActivityUtils.vibrate(it.context, 10L)

                // increase the logo size when selected
                it.animate().scaleX(1.3f)
                it.animate().scaleY(1.3f)
            }

            callback.onStart(viewHolder)
        }
    }
}
