package com.nbcsports.regional.nbc_rsn.data_menu.viewholders

import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.nbcsports.regional.nbc_rsn.R
import com.nbcsports.regional.nbc_rsn.data_menu.DataMenuContract
import com.nbcsports.regional.nbc_rsn.data_menu.models.PeriodDetail
import com.nbcsports.regional.nbc_rsn.team_feed.components.ViewHolderTypeBase

class DataMenuScoringSummaryVH(
        root: View,
        factory: DataMenuContract.ScoreCardFactory,
        viewType: Int
) : ViewHolderTypeBase(root, viewType) {

    private val scoringSummaryMainRecyclerView: RecyclerView? = root.findViewById(R.id.data_menu_scoring_summary_main_recycler_view)

    private var scoringSummaryMainAdapter = DataMenuScoringSummaryAdapter(factory.getScoreSummaryPresenter())

    override fun bind() {
        scoringSummaryMainRecyclerView?.apply {
            layoutManager = LinearLayoutManager(context)
            itemAnimator = DefaultItemAnimator()
            adapter = scoringSummaryMainAdapter
        }
    }

}

class DataMenuScoringSummaryAdapter(private val presenter: DataMenuContract.ScoreSummaryPresenter): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    enum class ViewType {
        HEADER,
        PERIOD_DETAILS,
        UNKNOWN
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (com.nbcsports.regional.nbc_rsn.extensions.fromInt<ViewType>(viewType)){
            ViewType.HEADER -> DataMenuScoringSummaryHeaderVH(
                    LayoutInflater.from(parent.context).inflate(R.layout.view_holder_data_menu_scoring_summary_header, parent, false),
                    viewType)
            ViewType.PERIOD_DETAILS -> DataMenuScoringSummaryPeriodDetailsVH(
                    LayoutInflater.from(parent.context).inflate(R.layout.view_holder_data_menu_scoring_summary_period_details, parent, false),
                    viewType, presenter)
            else -> DataMenuScoringSummaryEmptyVH(
                    LayoutInflater.from(parent.context).inflate(R.layout.data_menu_item_empty_layout, parent, false),
                    viewType
            )
        }
    }

    override fun getItemCount(): Int {
        return presenter.getScoringSummaryPeriodDetailsList().size.let {
            if (it == 0) 0 else it + 1
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (position){
            0 -> ViewType.HEADER
            else ->ViewType.PERIOD_DETAILS
        }.ordinal
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        when(viewHolder) {
            is DataMenuScoringSummaryPeriodDetailsVH -> {
                val itemPeriodDetail: PeriodDetail = presenter.getScoringSummaryPeriodDetailsList()[position - 1]
                viewHolder.bind(itemPeriodDetail)
            }
        }
    }



}