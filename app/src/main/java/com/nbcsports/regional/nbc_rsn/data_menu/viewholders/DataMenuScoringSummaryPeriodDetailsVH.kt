package com.nbcsports.regional.nbc_rsn.data_menu.viewholders

import androidx.core.widget.TextViewCompat
import androidx.appcompat.widget.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nbcsports.regional.nbc_rsn.MainActivity
import com.nbcsports.regional.nbc_rsn.R
import com.nbcsports.regional.nbc_rsn.data_menu.DataMenuContract
import com.nbcsports.regional.nbc_rsn.data_menu.DataMenuDataUtils
import com.nbcsports.regional.nbc_rsn.data_menu.models.PeriodDetail
import com.nbcsports.regional.nbc_rsn.data_menu.models.PeriodDetailGoal
import com.nbcsports.regional.nbc_rsn.localization.LocalizationManager
import com.nbcsports.regional.nbc_rsn.team_feed.components.ViewHolderTypeBase
import com.nbcsports.regional.nbc_rsn.utils.StringUtils
import com.squareup.picasso.Picasso
import kotlin.math.roundToInt

class DataMenuScoringSummaryPeriodDetailsVH(itemView: View, itemViewType: Int, val presenter: DataMenuContract.ScoreSummaryPresenter) : ViewHolderTypeBase(itemView, itemViewType) {

    private val periodDetailsTitleTextView: AppCompatTextView? = itemView.findViewById(R.id.data_menu_scoring_summary_period_details_title_text_view)
    private val periodDetailsTeamListRecyclerView: RecyclerView? = itemView.findViewById(R.id.data_menu_scoring_summary_period_details_team_list_recycler_view)
    private val periodDetailsStatsListRecyclerView: RecyclerView? = itemView.findViewById(R.id.data_menu_scoring_summary_period_details_stats_list_recycler_view)
    private val periodDetailsSummaryDescriptionRecyclerView: RecyclerView? = itemView.findViewById(R.id.data_menu_scoring_summary_period_details_summary_description_recycler_view)

    private var periodDetailsTeamAdapter: PeriodDetailsTeamAdapter? = null
    private var periodDetailsStatsAdapter: PeriodDetailsStatsAdapter? = null
    private var periodDetailsSummaryDescriptionAdapter: PeriodDetailsSummaryDescriptionAdapter? = null

    private fun initTeamListRecyclerView(periodDetailGoalList: List<PeriodDetailGoal>) {
        periodDetailsTeamAdapter = PeriodDetailsTeamAdapter(periodDetailGoalList)
        periodDetailsTeamListRecyclerView?.apply {
            layoutManager = LinearLayoutManager(context)
            itemAnimator = DefaultItemAnimator()
            adapter = periodDetailsTeamAdapter
        }
    }

    private fun initStatsListRecyclerView(periodDetailGoalList: List<PeriodDetailGoal>, presenter: DataMenuContract.ScoreSummaryPresenter) {
        val periodDetailsStatsList: MutableList<String> = mutableListOf()
        // Away
        periodDetailsStatsList.add(presenter.getAwayTeamAbbreviation().toUpperCase())
        periodDetailsStatsList.addAll(periodDetailGoalList.map { it.currentScore?.awayScore?.toString() ?: "" })
        // Home
        periodDetailsStatsList.add(presenter.getHomeTeamAbbreviation().toUpperCase())
        periodDetailsStatsList.addAll(periodDetailGoalList.map { it.currentScore?.homeScore?.toString() ?: "" })

        periodDetailsStatsAdapter = PeriodDetailsStatsAdapter(periodDetailsStatsList)
        periodDetailsStatsListRecyclerView?.apply {
            layoutManager = GridLayoutManager(context, periodDetailGoalList.size + 1, GridLayoutManager.HORIZONTAL, false)
            itemAnimator = DefaultItemAnimator()
            adapter = periodDetailsStatsAdapter
        }
    }

    private fun initSummaryDescriptionRecyclerView(periodDetailGoalList: List<PeriodDetailGoal>) {
        val summaryDescriptionsList: MutableList<String> = mutableListOf()
        for (periodDetailGoal in periodDetailGoalList){
            val resultString = StringBuilder()

            // Time
            periodDetailGoal.time?.let {
                if (it.minutes < 10) {
                    resultString.append(0)
                }
                resultString.append(it.minutes).append(":")

                if (it.seconds < 10) {
                    resultString.append(0)
                }
                resultString.append(it.seconds)
            }
            resultString.append(" - ")

            // Player
            periodDetailGoal.player.let {
                resultString.append(it.firstName).append(" ").append(it.lastName)
            }

            // Goal number
            periodDetailGoal.goalNumber?.let {
                resultString.append(" (").append(it.season).append(")")
            }

            // Asset players
            for (periodDetailGoalAssist in periodDetailGoal.assists.toList()){
                when {
                    periodDetailGoalAssist.player.sequence > 1 -> {
                        periodDetailGoalAssist.player.let {
                            resultString.append(" and ").append(it.firstName).append(" ").append(it.lastName)
                        }
                    }
                    else -> {
                        periodDetailGoalAssist.player.let {
                            resultString.append(" ").append(it.firstName).append(" ").append(it.lastName)
                        }
                    }
                }
            }

            summaryDescriptionsList.add(resultString.toString())
        }

        periodDetailsSummaryDescriptionAdapter = PeriodDetailsSummaryDescriptionAdapter(summaryDescriptionsList)
        periodDetailsSummaryDescriptionRecyclerView?.apply {
            layoutManager = LinearLayoutManager(context)
            itemAnimator = DefaultItemAnimator()
            adapter = periodDetailsSummaryDescriptionAdapter
        }
    }

    fun bind(itemPeriodDetail: PeriodDetail) {
        // Title
        periodDetailsTitleTextView?.apply {
            text = when {
                itemPeriodDetail.period == 4 -> {
                    if (LocalizationManager.isInitialized()) {
                        LocalizationManager.DataMenu.BoxscoreNHLOvertime
                    } else {
                        "Overtime"
                    }
                }
                itemPeriodDetail.period > 4 -> {
                    if (LocalizationManager.isInitialized()) {
                        "${StringUtils.getOrdinalString(itemPeriodDetail.period - 3)} ${LocalizationManager.DataMenu.BoxscoreNHLOvertime}"
                    } else {
                        "${StringUtils.getOrdinalString(itemPeriodDetail.period - 3)} Overtime"
                    }
                }
                else -> {
                    if (LocalizationManager.isInitialized()) {
                        LocalizationManager.DataMenu.getBoxscoreNHLPeriod(StringUtils.getOrdinalString(itemPeriodDetail.period).toLowerCase())
                    } else {
                        StringUtils.getOrdinalString(itemPeriodDetail.period).toLowerCase() + " Period"
                    }
                }
            }
        }

        // Team
        initTeamListRecyclerView(itemPeriodDetail.goals.toList())

        initStatsListRecyclerView(itemPeriodDetail.goals.toList(), presenter)

        initSummaryDescriptionRecyclerView(itemPeriodDetail.goals.toList())
    }

    override fun toString(): String {
        return "DataMenuScoringSummaryPeriodDetailsVH"
    }

}

/**
 * Team and time
 */
class PeriodDetailsTeamAdapter(private val periodDetailGoalList: List<PeriodDetailGoal>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    enum class ViewType {
        TEAM_AND_TIME,
        UNKNOWN
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(com.nbcsports.regional.nbc_rsn.extensions.fromInt<ViewType>(viewType)) {
            ViewType.TEAM_AND_TIME -> PeriodDetailsTeamVH(
                    LayoutInflater.from(parent.context).inflate(R.layout.view_holder_data_menu_scoring_summary_period_details_team, parent, false),
                    viewType)
            else -> DataMenuScoringSummaryEmptyVH(
                    LayoutInflater.from(parent.context).inflate(R.layout.data_menu_item_empty_layout, parent, false),
                    viewType)
        }
    }

    override fun getItemCount(): Int {
        return periodDetailGoalList.size
    }

    override fun getItemViewType(position: Int): Int {
        return ViewType.TEAM_AND_TIME.ordinal
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        when (viewHolder){
            is PeriodDetailsTeamVH -> viewHolder.bind(periodDetailGoalList[position])
        }
    }

}

class PeriodDetailsTeamVH(itemView: View, itemViewType: Int) : ViewHolderTypeBase(itemView, itemViewType) {

    private var teamIconImageView: ImageView? = null
    private var teamTimeTextView: AppCompatTextView? = null

    init {
        teamIconImageView = itemView.findViewById(R.id.period_details_team_icon_image_view)
        teamTimeTextView = itemView.findViewById(R.id.period_details_team_time_text_view)
    }

    fun bind(itemPeriodDetailGoal: PeriodDetailGoal) {
        // Image
        teamIconImageView?.let {
            updateLogo(itemPeriodDetailGoal.teamId, it)
        }

        // Time
        itemPeriodDetailGoal.time?.let {
            teamTimeTextView?.text = "${DataMenuDataUtils.getTimeString(it.minutes)}:${DataMenuDataUtils.getTimeString(it.seconds)}"
        }
    }

    private fun updateLogo(teamId: Int?, targetView: ImageView) {
        val teamManager= (itemView.context as MainActivity).teamManager
        teamId?.let { teamManager.getLogoUrl(it) }?.let { logoUrl ->
            Picasso.get().load(logoUrl)
                    .error(R.drawable.ic_peacock_square)
                    .resizeDimen(R.dimen.fab_logo_max_size, R.dimen.fab_logo_max_size)
                    .centerInside()
                    .into(targetView)
        }
    }

    override fun toString(): String {
        return "PeriodDetailsTeamVH"
    }

}

/**
 * Stats
 */
class PeriodDetailsStatsAdapter(private val periodDetailsStatsList: MutableList<String>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    enum class ViewType {
        STATS_TITLE,
        STATS_VALUE,
        UNKNOWN
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(com.nbcsports.regional.nbc_rsn.extensions.fromInt<ViewType>(viewType)) {
            ViewType.STATS_TITLE -> PeriodDetailsStatsTitleVH(
                    LayoutInflater.from(parent.context).inflate(R.layout.view_holder_data_menu_score_stats, parent, false),
                    viewType)
            ViewType.STATS_VALUE -> PeriodDetailsStatsValueVH(
                    LayoutInflater.from(parent.context).inflate(R.layout.view_holder_data_menu_score_stats, parent, false),
                    viewType)
            else -> DataMenuScoringSummaryEmptyVH(
                    LayoutInflater.from(parent.context).inflate(R.layout.data_menu_item_empty_layout, parent, false),
                    viewType)
        }
    }

    override fun getItemCount(): Int {
        return periodDetailsStatsList.size
    }

    override fun getItemViewType(position: Int): Int {
        return when(position % (periodDetailsStatsList.size / 2)) {
            0 -> ViewType.STATS_TITLE
            else -> ViewType.STATS_VALUE
        }.ordinal
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        when (viewHolder){
            is PeriodDetailsStatsTitleVH -> {
                viewHolder.bind(periodDetailsStatsList[position])
            }
            is PeriodDetailsStatsValueVH -> {
                viewHolder.bind(periodDetailsStatsList[position])
            }
            is DataMenuScoringSummaryEmptyVH -> {}
        }
    }

}

class PeriodDetailsStatsTitleVH(itemView: View, itemViewType: Int): ViewHolderTypeBase(itemView, itemViewType) {

    private var contentTextView: AppCompatTextView? = null

    init {
        contentTextView = (itemView as? AppCompatTextView)
        contentTextView?.apply {
            TextViewCompat.setTextAppearance(this, R.style.DataMenuScoringSummaryColumnHeader)
            setPadding(paddingLeft, paddingTop, paddingRight, resources.getDimension(R.dimen.datamenu_score_column_header_bottom_padding).roundToInt())
            invalidate()
        }
    }

    fun bind(text: String) {
        contentTextView?.text = text
    }

    override fun toString(): String {
        return "PeriodDetailsStatsTitleVH"
    }

}

class PeriodDetailsStatsValueVH(itemView: View, itemViewType: Int): ViewHolderTypeBase(itemView, itemViewType) {

    private var contentTextView: AppCompatTextView? = null

    init {
        contentTextView = (itemView as? AppCompatTextView)
    }

    fun bind(text: String) {
        contentTextView?.text = text
    }

    override fun toString(): String {
        return "PeriodDetailsStatsValueVH"
    }

}

/**
 * Summary description
 */
class PeriodDetailsSummaryDescriptionAdapter(private val summaryDescriptionsList: MutableList<String>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    enum class ViewType {
        SUMMARY_DESCRIPTION_CONTENT,
        UNKNOWN
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(com.nbcsports.regional.nbc_rsn.extensions.fromInt<ViewType>(viewType)) {
            ViewType.SUMMARY_DESCRIPTION_CONTENT -> PeriodDetailsSummaryDescriptionVH(
                    LayoutInflater.from(parent.context).inflate(R.layout.view_holder_data_menu_scoring_summary_period_details_summary_description, parent, false),
                    viewType)
            else -> DataMenuScoringSummaryEmptyVH(
                    LayoutInflater.from(parent.context).inflate(R.layout.data_menu_item_empty_layout, parent, false),
                    viewType)
        }
    }

    override fun getItemCount(): Int {
        return summaryDescriptionsList.size
    }

    override fun getItemViewType(position: Int): Int {
        return ViewType.SUMMARY_DESCRIPTION_CONTENT.ordinal
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        when (viewHolder){
            is PeriodDetailsSummaryDescriptionVH -> {
                viewHolder.bind(summaryDescriptionsList[position])
            }
        }
    }

}

class PeriodDetailsSummaryDescriptionVH(itemView: View, itemViewType: Int): ViewHolderTypeBase(itemView, itemViewType) {

    private var periodDetailsSummaryDescriptionTextView: AppCompatTextView? = null

    init {
        periodDetailsSummaryDescriptionTextView = itemView.findViewById(R.id.period_details_summary_description_text_view)
    }

    fun bind(text: String) {
        periodDetailsSummaryDescriptionTextView?.text = text
    }

    override fun toString(): String {
        return "PeriodDetailsSummaryDescriptionVH"
    }

}