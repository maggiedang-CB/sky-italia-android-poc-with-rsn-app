package com.nbcsports.regional.nbc_rsn.teamselector

import android.graphics.Color
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView

import com.nbcsports.regional.nbc_rsn.R
import com.nbcsports.regional.nbc_rsn.common.Team
import com.nbcsports.regional.nbc_rsn.extensions.overrideColor
import com.nbcsports.regional.nbc_rsn.utils.DisplayUtils
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.team_icon_item.view.*

import java.util.ArrayList
import java.util.Collections
import kotlin.math.roundToInt


internal class TeamIconAdapter : RecyclerView.Adapter<TeamIconAdapter.ViewHolder>() {

    private val ICON_POSITION: Double = 0.79

    private val teams = ArrayList<Team>()
    private lateinit var recyclerView: RecyclerView

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.team_icon_item, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindTo(teams[position])
    }

    fun setTeams(teams: ArrayList<Team>) {
        if (teams.isEmpty()) return
        this.teams.clear()
        this.teams.addAll(teams)
        notifyDataSetChanged()
    }

    fun getTeams(): ArrayList<Team> {
        return teams
    }

    override fun getItemCount(): Int {
        return teams.size
    }

    fun insertItem(team: Team?) {
        if (team == null || teams.contains(team)) return
        teams.add(team)
        // when there's one item, this inserts into position 0
        notifyItemInserted(teams.size - 1)
    }

    fun onItemMove(source: Int, target: Int) {
        Collections.swap(teams, source, target)
        if (!recyclerView.isComputingLayout) {
            notifyItemMoved(source, target)
        }
    }

    fun removeItem(position: Int) {
        if (position < 0) return
        teams.removeAt(position)
        notifyItemRemoved(position)
    }

    fun removeTeam(team: Team) {
        val index = teams.indexOf(team)
        if (index == -1) return
        removeItem(index)
    }

    internal inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val logo: ImageView = view.iv_team_icon
        val container: View = view.team_icon_container
        var teamData: Team? = null

        init {
            val screenHeight = DisplayUtils.getScreenHeight(context = view.context)
            val ICON_HEIGHT = (0.058f * screenHeight).roundToInt()

            view.layoutParams.width = ICON_HEIGHT
            view.layoutParams.height = ICON_HEIGHT
        }

        fun bindTo(team: Team?) {
            teamData = team
            /**
             * Set the recyclerview margin to correctly position the icons.
             */
            val params = container.layoutParams as RecyclerView.LayoutParams
            val screenHeight = DisplayUtils.getScreenHeight(context = view.context)

            val pos = (screenHeight * ICON_POSITION).toInt()
            params.topMargin = pos
            container.layoutParams = params

            if (container.visibility != View.VISIBLE) {
                container.visibility = View.VISIBLE
            }

            if (team != null && team.logoUrl.isNotEmpty()) {
                Picasso.get()
                        .load(team.logoUrl)
                        .resizeDimen(R.dimen.team_icon_size, R.dimen.team_icon_size)
                        .centerInside()
                        .into(logo)

                view.background.overrideColor(Color.parseColor(team.primaryColor))
            }
        }
    }
}
