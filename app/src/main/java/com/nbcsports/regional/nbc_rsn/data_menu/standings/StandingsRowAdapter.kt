package com.nbcsports.regional.nbc_rsn.data_menu.standings

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.nbcsports.regional.nbc_rsn.R
import com.nbcsports.regional.nbc_rsn.common.TeamManager
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.standings_row_item.view.*

class StandingsRowAdapter : RecyclerView.Adapter<StandingsRowAdapter.ItemViewHolder>() {

    private val rows = mutableListOf<StandingsRow>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.standings_row_item, parent, false))
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {

        val standingsRow = rows[position]

        val teamId = standingsRow.teamId
        TeamManager.getInstance()?.getLogoUrl(teamId)?.let {
            Picasso.get().load(it)
                    .error(R.drawable.ic_peacock_square)
                    .resizeDimen(R.dimen.fab_logo_max_size, R.dimen.fab_logo_max_size)
                    .centerInside()
                    .into(holder.itemView.standings_row_logo)
        }

        holder.itemView.standings_row_team_name.text = standingsRow.name
        holder.itemView.standings_row_playoff_spot.text = standingsRow.superscript
        holder.itemView.standings_row_record.text = standingsRow.record
        holder.itemView.standings_row_gb.text = standingsRow.gamesBehind
    }

    override fun getItemCount(): Int {
        return rows.size
    }

    fun setRows(list: List<StandingsRow>?) {
        if (list == null) return
        rows.clear()
        rows.addAll(list)
        notifyDataSetChanged()
    }

    inner class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view)
}
