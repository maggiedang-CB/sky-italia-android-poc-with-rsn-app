package com.nbcsports.regional.nbc_rsn.data_menu.standings

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.nbcsports.regional.nbc_rsn.R
import com.nbcsports.regional.nbc_rsn.common.TeamManager
import com.nbcsports.regional.nbc_rsn.data_bar.DataBarUtil
import com.nbcsports.regional.nbc_rsn.data_menu.viewholders.TabClickListener
import com.nbcsports.regional.nbc_rsn.data_menu.viewholders.TabLabelViewHolder
import com.nbcsports.regional.nbc_rsn.localization.LocalizationManager
import kotlinx.android.synthetic.main.standings_conference_label.view.*
import kotlinx.android.synthetic.main.standings_end_card.view.*
import kotlinx.android.synthetic.main.standings_table.view.*

internal class StandingsLeagueAdapter : RecyclerView.Adapter<StandingItemViewHolder>(), TabClickListener {

    private var leagueStandings: LeagueStandings? = null
    private var isConferenceSelected: Boolean = true
    private var conferences: List<StandingsConference>? = null
    private var typeList = mutableListOf<Pair<VIEW_TYPE, Any>>()
    private var sportName: String = ""

    enum class VIEW_TYPE {
        TAB,
        CONFERENCE_LABEL,
        TABLE,
        END_CARD
    }

    override fun getItemViewType(position: Int): Int {
        return typeList[position].first.ordinal
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StandingItemViewHolder {
        return when (com.nbcsports.regional.nbc_rsn.extensions.fromInt<VIEW_TYPE>(value = viewType)) {
            VIEW_TYPE.TAB -> {
                val holder = TabLabelViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.standings_tab_label, parent, false), this)

                val selectedTeam = TeamManager.getInstance()?.getSelectedTeam()
                val (_, leagueName) = DataBarUtil.getRequestParams(selectedTeam)
                when (leagueName) {
                    "nfl", "mlb" -> {
                        if (LocalizationManager.isInitialized()) {
                            holder.updateLabel(LocalizationManager.DataMenu.League, LocalizationManager.DataMenu.Division)
                        } else {
                            holder.updateLabel(R.string.league, R.string.division)
                        }
                    }
                    else -> {
                        if (LocalizationManager.isInitialized()) {
                            holder.updateLabel(LocalizationManager.DataMenu.Conference, LocalizationManager.DataMenu.Division)
                        } else {
                            holder.updateLabel(R.string.conference, R.string.division)
                        }
                    }
                }

                holder
            }
            VIEW_TYPE.CONFERENCE_LABEL -> ConferenceLabelViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.standings_conference_label, parent, false))
            VIEW_TYPE.END_CARD -> EndCardViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.standings_end_card, parent, false))
            else -> TableViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.standings_table, parent, false))
        }
    }

    /*
    Update the division tables for items below the tab labels whenever a tab is clicked
     */
    override fun updateTabs(firstTabSelected: Boolean) {
        val originalItemCount = itemCount

        isConferenceSelected = firstTabSelected
        setConferenceDataList()

        if (originalItemCount <= itemCount) {
            notifyItemRangeChanged(1, originalItemCount - 2)
        } else {
            notifyDataSetChanged()
        }
    }

    override fun onBindViewHolder(holder: StandingItemViewHolder, position: Int) {
        holder.bindView(position)
    }

    fun setData(standings: LeagueStandings?, selectConference: Boolean, sportName: String) {
        if (standings == null) return
        this.leagueStandings = standings
        this.isConferenceSelected = selectConference
        this.sportName = sportName

        setConferenceDataList()
        notifyDataSetChanged()
    }

    private fun setConferenceDataList() {
        this.conferences = if (isConferenceSelected) {
            this.leagueStandings?.tabs?.get(0)
        } else {
            this.leagueStandings?.tabs?.get(1)
        }?.conferences

        typeList.clear()
        typeList.add(Pair(VIEW_TYPE.TAB, "tabs"))
        conferences?.forEach { standingsConference ->
            typeList.add(Pair(VIEW_TYPE.CONFERENCE_LABEL, standingsConference.name))
            standingsConference.tables?.forEach { standingsTable ->
                typeList.add(Pair(VIEW_TYPE.TABLE, standingsTable))
            }
        }
        typeList.add(Pair(VIEW_TYPE.END_CARD, "end card"))

    }

    override fun getItemCount(): Int {
        return typeList.size
    }

    inner class ConferenceLabelViewHolder(view: View) : StandingItemViewHolder(view) {
        override fun bindView(position: Int) {
            val label = typeList[position].second
            itemView.standing_conference_label.text = label.toString()
        }
    }

    inner class TableViewHolder(view: View) : StandingItemViewHolder(view) {
        override fun bindView(position: Int) {
            val tableData = typeList[position].second as StandingsTable

            val rv = itemView.standings_card_list
            rv.layoutManager = LinearLayoutManager(rv.context)
            val adapter = StandingsRowAdapter()
            adapter.setRows(tableData.teams)
            rv.adapter = adapter

            itemView.standing_card_title.text = tableData.label
            itemView.standing_label_record.text = tableData.columnLabels?.first
            itemView.standings_label_gb.text = tableData.columnLabels?.second
        }
    }

    inner class EndCardViewHolder(view: View) : StandingItemViewHolder(view) {
        override fun bindView(position: Int) {
            /*
                "ClinchedPlayoffs": "X - Clinched Playoff Spot",
		        "ClinchedDivision": "Y - Clinched Division",
		        "ClinchedConference": "Z - Clinched Conference",
		        "ClinchedHomeField": "Z - Clinched Home Field Advantage",
		        "ClinchedBye": "W - Clinched First Round Bye",
		        "ClinchedTrophy": "W - Clinched President's Trophy"
            */

            val legendText = if (LocalizationManager.isInitialized()) {
                """${LocalizationManager.DataMenu.ClinchedPlayoffs}
                        |${LocalizationManager.DataMenu.ClinchedDivision}
                        |${if (sportName == "football") {
                    LocalizationManager.DataMenu.ClinchedHomeField
                } else {
                    LocalizationManager.DataMenu.ClinchedConference
                }}
                        |${if (sportName == "hockey") {
                    LocalizationManager.DataMenu.ClinchedTrophy
                } else {
                    LocalizationManager.DataMenu.ClinchedBye
                }}
                    """.trimMargin()
            } else {
                ""
            }

            itemView.end_card_legend.text = legendText
        }
    }
}

abstract class StandingItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    abstract fun bindView(position: Int)
}
