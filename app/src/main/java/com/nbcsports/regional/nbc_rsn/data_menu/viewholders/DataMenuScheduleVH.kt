package com.nbcsports.regional.nbc_rsn.data_menu.viewholders

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.nbcsports.regional.nbc_rsn.MainActivity
import com.nbcsports.regional.nbc_rsn.R
import com.nbcsports.regional.nbc_rsn.data_bar.*
import com.nbcsports.regional.nbc_rsn.data_menu.models.RotoSchedule
import com.nbcsports.regional.nbc_rsn.data_menu.schedule.DataMenuScheduleItemView
import com.nbcsports.regional.nbc_rsn.extensions.e
import com.nbcsports.regional.nbc_rsn.localization.LocalizationManager
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.view_holder_data_menu_schedule.view.*
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat

class DataMenuScheduleVH(root: View) : DataMenuScheduleItemView(root) {

    private val TBD_LABEL: String =
            if (LocalizationManager.isInitialized()) {
                LocalizationManager.DataMenu.ScheduleTBD.toUpperCase()
            } else {
                itemView.resources.getString(R.string.schedule_summary_tbd)
            }

    private val FINAL_LABEL: String =
            if (LocalizationManager.isInitialized()) {
                LocalizationManager.DataMenu.ScheduleFinal.toUpperCase()
            } else {
                itemView.resources.getString(R.string.schedule_summary_final)
            }

    override fun bind(event: RotoSchedule, vararg others: Any?) {
        // update arrow based on winner
        itemView.game_point_arrow_home.visibility = View.GONE
        itemView.game_point_arrow_away.visibility = View.GONE
        if (event.isHomeWin()) {
            itemView.game_point_arrow_home.visibility = View.VISIBLE
        } else if (event.isAwayWin()) {
            itemView.game_point_arrow_away.visibility = View.VISIBLE
        }

        // update game point
        itemView.game_point_home.text = if (event.isPreGame()) "-" else event.homeScore.toString()
        itemView.game_point_away.text = if (event.isPreGame()) "-" else event.awayScore.toString()

        // update record
        updateRecord(itemView.record_home, Record(
                wins = event.homeWins,
                losses = event.homeLosses,
                ties = if (event.homeTies != -1) event.homeTies else if (event.homeOtLosses != -1) event.homeOtLosses else null
        ))
        updateRecord(itemView.record_away, Record(
                wins = event.awayWins,
                losses = event.awayLosses,
                ties = if (event.awayTies != -1) event.awayTies else if (event.awayOtLosses != -1) event.awayOtLosses else null
        ))

        // update city name
        itemView.city_name_home.text = event.homeAbbr.toUpperCase()
        itemView.city_name_away.text = event.awayAbbr.toUpperCase()

        // update logo
        updateLogo(event.homeGlobalId, itemView.team_logo_home)
        updateLogo(event.awayGlobalId, itemView.team_logo_away)

        // update schedule details
        // different text will be displayed depends on the game's status
        try {
            val startDate = DateTime(event.gameDateTimeUTC).withZone(DateTimeZone.UTC)

            itemView.schedule_summary.text = when {
                event.isGameStatusFinal() -> {
                    val fmt = DateTimeFormat.forPattern("MMM dd").withZone(DateTimeZone.getDefault())
                    // post game, show only date and 'final' label
                    "${fmt.print(startDate)}\n$FINAL_LABEL"
                }
                event.isGameStatusTba() -> {
                    val fmt = DateTimeFormat.forPattern("MMM dd").withZone(DateTimeZone.getDefault())
                    // tbd, show date, time, network, and 'tbd' label
                    "${fmt.print(startDate)}\n${event.station.split(",")[0]}\n$TBD_LABEL"
                }
                else -> {
                    val fmt = DateTimeFormat.forPattern("MMM dd\nh a").withZone(DateTimeZone.getDefault())
                    // upcoming, show date, time, and network
                    "${fmt.print(startDate)} ${event.station.split(",")[0]}"
                }
            }
        } catch (ex: Exception) {
            e("Schedule Exception: $ex")
            return
        }
    }

    private fun updateLogo(teamId: Int?, targetView: ImageView) {
        val teamManager = (itemView.context as MainActivity).teamManager
        teamId?.let { teamManager.getLogoUrl(it) }?.let { logoUrl ->
            Picasso.get().load(logoUrl)
                    .error(R.drawable.ic_peacock_square)
                    .resizeDimen(R.dimen.fab_logo_max_size, R.dimen.fab_logo_max_size)
                    .centerInside()
                    .into(targetView)
        }
    }

    private fun updateRecord(targetView: TextView, record: Record?) {
        targetView.text = DataBarUtil.getRecordForTeam(record, false, false, false)
    }
}