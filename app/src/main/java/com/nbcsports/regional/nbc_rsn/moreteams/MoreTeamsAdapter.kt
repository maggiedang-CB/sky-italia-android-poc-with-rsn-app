package com.nbcsports.regional.nbc_rsn.moreteams

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jakewharton.rxbinding2.view.RxView
import com.nbcsports.regional.nbc_rsn.R
import com.nbcsports.regional.nbc_rsn.common.Team
import com.nbcsports.regional.nbc_rsn.localization.LocalizationManager
import com.squareup.picasso.Picasso
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.team_item.view.*
import java.util.concurrent.TimeUnit

class MoreTeamsAdapter constructor(
        val teamList: ArrayList<Team>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val viewClickSubject = PublishSubject.create<Team>()

    enum class SelectorViewType {
        HEADER,
        ITEM,
        FOOTER
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> SelectorViewType.HEADER.ordinal
            teamList.size + 1 -> SelectorViewType.FOOTER.ordinal
            else -> SelectorViewType.ITEM.ordinal
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (com.nbcsports.regional.nbc_rsn.extensions.fromInt<SelectorViewType>(viewType)) {
            SelectorViewType.HEADER -> HeaderViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.team_selector_header, parent, false))
            SelectorViewType.FOOTER -> FooterViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.more_teams_footer, parent, false))
            else -> ItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.team_item, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as? HeaderViewHolder)?.init()
        (holder as? ItemViewHolder)?.bindTo(teamList.get(position - 1))
    }

    override fun getItemCount(): Int {
        return teamList.size + 2
    }

    fun setTeamList(teams: List<Team>) {
        if (teams.isEmpty()) return
        teamList.clear()
        teamList.addAll(teams)
        notifyDataSetChanged()
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        viewClickSubject.onComplete()
    }

    fun onItemMove(source: Int, target: Int) {
        val targetTeam = teamList.get(source)
        teamList.removeAt(source)
        teamList.add(target, targetTeam)
        notifyItemMoved(source, target)
    }

    inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val headerTitle = itemView.more_teams_label

        fun init() {
            if (LocalizationManager.isInitialized()){
                headerTitle.text = LocalizationManager.MoreTeamsSelector.MoreTeams
            }
        }
    }

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name = itemView.more_teams_label
        val city = itemView.team_city
        val imageView = itemView.team_logo

        fun bindTo(team: Team) {
            if (team.logoUrl.isNotEmpty()) {
                Picasso.get()
                        .load(team.logoUrl)
                        .placeholder(R.drawable.ic_peacock)
                        .resizeDimen(R.dimen.logo_width, R.dimen.logo_height)
                        .onlyScaleDown()
                        .centerInside()
                        .into(imageView)
            }

            name.text = team.displayName
            city.text = team.cityName

            RxView.clicks(itemView)
                    .debounce(500L, TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .map { _ -> team }
                    .subscribe(viewClickSubject)
        }
    }

    inner class FooterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    }
}