package com.nbcsports.regional.nbc_rsn.data_menu.schedule

import android.os.Handler
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.nbcsports.regional.nbc_rsn.R
import com.nbcsports.regional.nbc_rsn.data_menu.DataMenuContract
import com.nbcsports.regional.nbc_rsn.data_menu.models.RotoSchedule
import com.nbcsports.regional.nbc_rsn.data_menu.viewholders.DataMenuScheduleFilterVH
import com.nbcsports.regional.nbc_rsn.data_menu.viewholders.DataMenuScheduleVH
import com.nbcsports.regional.nbc_rsn.extensions.e

class DataMenuScheduleAdapter : RecyclerView.Adapter<DataMenuScheduleItemView>(), DataMenuContract.ScheduleList {

    private val VIEW_TYPE_FILTER_BAR = 0
    private val VIEW_TYPE_ITEM = 1

    private val presenter = DataMenuScheduleListPresenter(this)

    private lateinit var recyclerView: RecyclerView

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataMenuScheduleItemView {
        val statsTeamId = presenter.getStatsTeamId()

        return when (viewType) {
            VIEW_TYPE_FILTER_BAR -> DataMenuScheduleFilterVH(
                    root = LayoutInflater.from(parent.context).inflate(R.layout.view_holder_data_menu_schedule_filter, parent, false),
                    filterListener = presenter,
                    statsTeamId = statsTeamId
            )
            else -> DataMenuScheduleVH(root = LayoutInflater.from(parent.context).inflate(R.layout.view_holder_data_menu_schedule, parent, false))
        }
    }

    override fun getItemCount(): Int {
        return presenter.getCount()
    }

    override fun onBindViewHolder(viewHolder: DataMenuScheduleItemView, position: Int) {
        presenter.bindView(position, viewHolder)
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> VIEW_TYPE_FILTER_BAR
            else -> VIEW_TYPE_ITEM
        }
    }

    fun subscribe() {
        presenter.subscribe()
    }

    fun unsubscribe() {
        presenter.unsubscribe()
    }

    override fun notifyDataSetUpdated() {
        notifyDataSetChanged()
    }
}

abstract class DataMenuScheduleItemView(root: View) : RecyclerView.ViewHolder(root) {
    abstract fun bind(event: RotoSchedule, vararg others: Any?)
}