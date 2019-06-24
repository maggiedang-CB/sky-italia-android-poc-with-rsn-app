package com.nbcsports.regional.nbc_rsn.data_menu.viewholders

import android.os.Build
import android.view.View
import android.widget.TextView
import com.nbcsports.regional.nbc_rsn.R
import com.nbcsports.regional.nbc_rsn.data_menu.standings.StandingItemViewHolder
import kotlinx.android.synthetic.main.standings_tab_label.view.*

class TabLabelViewHolder(view: View, private val tabClickListener: TabClickListener) : StandingItemViewHolder(view) {

    private var isLeftTabSelected = true
    
    init {
        itemView.tab_left.setOnClickListener {
            if (!isLeftTabSelected) {
                isLeftTabSelected = true
                updateView()
                tabClickListener.updateTabs(true)
            }
        }
        itemView.tab_right.setOnClickListener {
            if (isLeftTabSelected) {
                isLeftTabSelected = false
                updateView()
                tabClickListener.updateTabs(false)
            }
        }
    }

    private fun updateView() {
        if (isLeftTabSelected) {
            toggleView(itemView.tab_left, itemView.tab_right)
        } else {
            toggleView(itemView.tab_right, itemView.tab_left)
        }
    }

    private fun toggleView(v1: TextView, v2: TextView) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            v1.setTextAppearance(R.style.tab_selected)
            v2.setTextAppearance(R.style.tab_unselected)
        } else {
            v1.setTextAppearance(v1.context, R.style.tab_selected)
            v2.setTextAppearance(v2.context, R.style.tab_unselected)
        }
        v1.setBackgroundResource(R.color.deep_blue)
        v2.setBackgroundResource(R.color.grey_2)
    }

    override fun bindView(position: Int) {}

    fun updateLabel(leftLabel: String = "", rightLabel: String = "") {
        itemView.tab_left.text = leftLabel
        itemView.tab_right.text = rightLabel
    }

    fun updateLabel(leftLabel: Int, rightLabel: Int) {
        itemView.tab_left.text = itemView.tab_left.resources.getString(leftLabel)
        itemView.tab_right.text = itemView.tab_left.resources.getString(rightLabel)
    }

    fun update(shouldSelectLeftTab: Boolean) {
        if (isLeftTabSelected != shouldSelectLeftTab) {
            isLeftTabSelected = shouldSelectLeftTab
            updateView()
        }
    }
}

interface TabClickListener {
    fun updateTabs(firstTabSelected: Boolean)
}