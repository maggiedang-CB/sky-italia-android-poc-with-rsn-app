package com.nbcsports.regional.nbc_rsn.fabigation

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.nbcsports.regional.nbc_rsn.MainActivity
import com.nbcsports.regional.nbc_rsn.R
import com.nbcsports.regional.nbc_rsn.common.Team
import com.nbcsports.regional.nbc_rsn.extensions.fromInt
import com.nbcsports.regional.nbc_rsn.localization.LocalizationManager
import com.nbcsports.regional.nbc_rsn.utils.DisplayUtils
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.more_team_item.view.*
import kotlinx.android.synthetic.main.more_team_item.view.more_teams_label as moreTeamItemLabelTextView
import kotlinx.android.synthetic.main.settings_item.view.*
import kotlinx.android.synthetic.main.settings_item.view.more_teams_label as settingsItemLabelTextView
import kotlinx.android.synthetic.main.team_list_item.view.*


/**
 * Created by justin on 2018-03-26.
 */
class FabMenuAdapter(
        private val activity: MainActivity,
        internal var teamList: ArrayList<Team>,
        var isRTL: Boolean
) : RecyclerView.Adapter<FabMenuAdapter.CardViewHolder>() {

    companion object {
        const val TEAM_CARD_WIDTH = 0.75
        const val TEAM_CARD_HEIGHT = 0.7
    }

    private var moreCardIndex: Int = -1

    public enum class FabCardType {
        HeadCard,
        Card,
        MoreTeams,
        Settings,
        FabOutro
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            itemCount - 1 -> FabCardType.Settings.ordinal
            moreCardIndex -> FabCardType.MoreTeams.ordinal
            else -> FabCardType.Card.ordinal
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        return resizeCard(
                context = parent.context,
                holder = when (fromInt<FabCardType>(viewType)) {
                    FabCardType.Settings -> SettingsViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.settings_item, parent, false))
                    FabCardType.MoreTeams -> MoreTeamsViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.more_team_item, parent, false))
                    else -> TeamListViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.team_list_item, parent, false))
                })
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        holder.setGradientColor()

        when (fromInt<FabCardType>(holder.itemViewType)) {
            FabCardType.Settings -> {
                if (LocalizationManager.isInitialized()) {
                    holder.itemView.settingsItemLabelTextView?.text = LocalizationManager.Settings.Settings
                }
            }
            FabCardType.MoreTeams -> {
                if (LocalizationManager.isInitialized()) {
                    holder.itemView.moreTeamItemLabelTextView?.text = LocalizationManager.MoreTeamsSelector.MoreTeamsCard
                    if (activity.teamManager.moreTeamsList.size <= 1) {
                        holder.itemView.team_size?.text = LocalizationManager.MoreTeamsSelector.getNumberOfTeamsSingular("" + activity.teamManager?.moreTeamsList?.size)
                    } else {
                        holder.itemView.team_size?.text = LocalizationManager.MoreTeamsSelector.getNumberOfTeamsPlural("" + activity.teamManager?.moreTeamsList?.size)
                    }
                }
            }
            else -> {
                (holder as TeamListViewHolder).bindTo(
                        getCorrectTeamPosition(position)
                )
            }
        }
    }

    private fun getCorrectTeamPosition(position: Int): Int {
        require(position >= 0 && position < teamList.size + 1)
        return if (moreCardIndex != -1 && position > moreCardIndex) {
            position - 1
        } else {
            position
        }
    }

    fun getTeamForPosition(position: Int): Team {
        require(position >= 0 && position < teamList.size + 1)
        return teamList[getCorrectTeamPosition(position)]
    }

    fun getTeamList(): ArrayList<Team> {
        return teamList
    }

    private fun resizeCard(context: Context, holder: CardViewHolder): CardViewHolder {
        val layoutParams = holder.itemView.layoutParams as RecyclerView.LayoutParams

        layoutParams.width = (DisplayUtils.getScreenWidth(context) * TEAM_CARD_WIDTH).toInt()
        layoutParams.height = (DisplayUtils.getScreenHeight(context) * TEAM_CARD_HEIGHT).toInt()
        holder.itemView.layoutParams = layoutParams
        return holder
    }

    override fun getItemCount(): Int {
        return teamList.size + 1 + (if (moreCardIndex == -1) 0 else 1)
    }

    /**
     * It changes the team list data on the fly.
     */
    fun setTeams(teams: MutableList<Team>?, moreCardIndex: Int, isRTL: Boolean) {
        if (teams == null) return
        this.isRTL = isRTL
        this.teamList.clear()
        this.teamList.addAll(teams)
        this.moreCardIndex = moreCardIndex
        notifyDataSetChanged()
    }

    /**
     * ViewHolders
     *
     */
    abstract inner class CardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract val gradientBackground: View?
        fun setGradientColor() {
            DisplayUtils.applyWhiteGradient(gradientBackground as View)
        }
    }

    open inner class TeamListViewHolder(itemView: View) : CardViewHolder(itemView) {
        override val gradientBackground: View?
            get() = itemView.team_list_bg_container
        val teamLogo = itemView.team_logo
        val appTitle = itemView.team_app_title
        val teamName = itemView.team_name
        val teamRecord = itemView.team_record
        val teamSeason = itemView.team_season
        val cardView = itemView.team_list_cardview
        var primaryColor: String = "#000000"
        var secondaryColor: String = "#000000"

        fun bindTo(position: Int) {
            val item: Team = teamList[position]
            primaryColor = item.primaryColor
            secondaryColor = item.secondaryColor
            cardView?.setCardBackgroundColor(Color.parseColor(primaryColor))

            if (item.logoUrl.isNotEmpty()) {
                Picasso.get()
                        .load(item.logoUrl)
                        .into(teamLogo)
            }

            // Set text of the text items
            appTitle?.text = item.regionName.toUpperCase()
            teamName?.text = item.displayName.toUpperCase()
            teamRecord?.text = LocalizationManager.DataBar.Record.toUpperCase()
            teamSeason?.text = (teamSeason?.text as String).toUpperCase()
        }
    }

    open inner class SettingsViewHolder(itemView: View) : CardViewHolder(itemView) {
        override val gradientBackground: View?
            get() = itemView.setting_gradient

        init {
            Picasso.get()
                    .load(R.drawable.setting_card_placeholder)
                    .fit()
                    .centerCrop()
                    .into(itemView.setting_placeholder)
        }
    }

    open inner class MoreTeamsViewHolder(itemView: View) : CardViewHolder(itemView) {
        override val gradientBackground: View?
            get() = itemView.more_team_bg_container
    }

}

