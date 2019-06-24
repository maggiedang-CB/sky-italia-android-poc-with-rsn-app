package com.nbcsports.regional.nbc_rsn.data_menu.components

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.nbcsports.regional.nbc_rsn.R
import com.nbcsports.regional.nbc_rsn.common.Config
import com.nbcsports.regional.nbc_rsn.common.Team
import com.nbcsports.regional.nbc_rsn.data_menu.DataMenuFragment
import com.nbcsports.regional.nbc_rsn.data_menu.intent.DataMenuItemClickListenerFactory
import com.nbcsports.regional.nbc_rsn.data_menu.models.DataMenuOverviewDataModel
import io.reactivex.disposables.CompositeDisposable

class DataMenuAdapter(private val dataMenuFragment: DataMenuFragment, private val team: Team, private val config: Config) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var dataMenuItemClickListenerFactory: DataMenuItemClickListenerFactory? = null
    private var compositeDisposable: CompositeDisposable? = null

    // TODO: Change type of list to appropriate one
    private var dataMenuItemList: MutableList<DataMenuOverviewDataModel>? = null

    enum class ViewType {
        VIEW_TYPE_CAROUSEL,
        VIEW_TYPE_CTA,
        VIEW_TYPE_UNKNOWN
    }

    init {
        dataMenuItemClickListenerFactory = DataMenuItemClickListenerFactory(dataMenuFragment, team, this)
        dataMenuItemList = mutableListOf()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        // Initialize composite disposable
        if (compositeDisposable == null || (compositeDisposable as CompositeDisposable).isDisposed) {
            compositeDisposable = CompositeDisposable()
        }
        // Return appropriate view holder base on view type
        return when (com.nbcsports.regional.nbc_rsn.extensions.fromInt<ViewType>(viewType)) {
            ViewType.VIEW_TYPE_CAROUSEL -> DataMenuItemCarouselViewHolder(
                    dataMenuFragment.childFragmentManager,
                    LayoutInflater.from(parent.context).inflate(R.layout.data_menu_item_carousel_layout, parent, false),
                    viewType, team)
            ViewType.VIEW_TYPE_CTA -> {
                val dataMenuItemCTAViewHolder = DataMenuItemCTAViewHolder(
                        LayoutInflater.from(parent.context).inflate(R.layout.data_menu_item_cat_layout, parent, false),
                        viewType, team)
                // Set up item click listener
                dataMenuItemClickListenerFactory?.getClickListener(dataMenuItemCTAViewHolder)?.let {
                    compositeDisposable?.add(it)
                }
                dataMenuItemCTAViewHolder
            }
            else -> DataMenuItemEmptyViewHolder(
                    LayoutInflater.from(parent.context).inflate(R.layout.data_menu_item_empty_layout, parent, false),
                    viewType, team)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            // TODO: Should change logic here
            0 -> ViewType.VIEW_TYPE_CAROUSEL
            else -> ViewType.VIEW_TYPE_CTA
        }.ordinal
    }

    override fun getItemCount(): Int {
        // TODO: Should return list's size
        return dataMenuItemList?.size ?: 0
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        // TODO: Need to have some implementation here
        if (viewHolder is DataMenuItemCarouselViewHolder) {
            dataMenuItemList?.let {
                viewHolder.bind(it[position])
            }
        } else if (viewHolder is DataMenuItemCTAViewHolder) {
            dataMenuItemList?.let {
                viewHolder.bind(it[position])
            }
        }
    }

    fun onDestroy() {
        if (compositeDisposable != null && !(compositeDisposable as CompositeDisposable).isDisposed) {
            (compositeDisposable as CompositeDisposable).clear()
        }
        compositeDisposable = null
    }

    fun setData(itemList: List<DataMenuOverviewDataModel>) {
        // TODO: Should modify list here
        if (dataMenuItemList == null) {
            dataMenuItemList = mutableListOf()
        }
        dataMenuItemList?.apply {
            clear()
            addAll(itemList)
            when {
                // Using notifyItemRangeChanged() in stead of notifyDataSetChanged() to refresh
                // the whole list is because notifyItemRangeChanged is smoother and shows some
                // VH animation when updating
                this.isNotEmpty() -> notifyItemRangeChanged(0, size)
                else -> notifyDataSetChanged()
            }
        }
    }

    fun getDataMenuItemList(): List<DataMenuOverviewDataModel> {
        return dataMenuItemList ?: listOf()
    }

}