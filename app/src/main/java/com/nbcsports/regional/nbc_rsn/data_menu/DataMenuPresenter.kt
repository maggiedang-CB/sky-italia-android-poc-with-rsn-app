package com.nbcsports.regional.nbc_rsn.data_menu

import com.nbcsports.regional.nbc_rsn.common.Team
import com.nbcsports.regional.nbc_rsn.data_bar.*
import com.nbcsports.regional.nbc_rsn.data_menu.intent.*
import com.nbcsports.regional.nbc_rsn.data_menu.models.*
import com.nbcsports.regional.nbc_rsn.extensions.d
import com.nbcsports.regional.nbc_rsn.localization.LocalizationManager
import com.nbcsports.regional.nbc_rsn.utils.DateFormatUtils
import com.nbcsports.regional.nbc_rsn.utils.StringUtils
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class DataMenuPresenter(private val view: DataMenuContract.View) : DataMenuContract.Presenter {

    init {
        view.setPresenter(this)
    }

    override fun subscribe(team: Team?) {
        // Set up data subscription (using Roto)
        subscribeRoto(team)
        // Set up data menu place holder
        view.setMainRecyclerViewData(getPlaceHolderItemList())
    }

    override fun unsubscribe() {
        DataMenuDataManager.unsubscribeAll()
    }

    /**
     * This method is used to subscribe watcher to Stats.com data (deprecated)
     */
    @Deprecated("Replace with subscribeRoto(...)")
    private fun subscribeStatsCom(team: Team?) {
        team?.league?.let {
            when (it.toLowerCase()) {
                "mlb" -> {
                    DataMenuDataManager.subscribe(object : DataMenuStandingWatcher {
                        override fun onDataReady(data: StatsTeam) {
                            d(String.format("This is the enter point: DataMenuDataManager.subscribe mlb: %s", data.toString()))
                            view.setMainRecyclerViewData(getMLBOverviewDataFormattedList(data))
                        }
                    })
                }
                "nba" -> {
                    DataMenuDataManager.subscribe(object : DataMenuOverviewWatcherNBA {
                        override fun onDataReady(data: DataMenuOverviewModel<StatsNBATeamStats>) {
                            d(String.format("This is the enter point: DataMenuDataManager.subscribe nba: %s", data.toString()))
                            view.setMainRecyclerViewData(getNBAOverviewDataFormattedList(data))
                        }
                    })
                }
                "nfl" -> {
                    DataMenuDataManager.subscribe(object : DataMenuOverviewWatcherNFL {
                        override fun onDataReady(data: DataMenuOverviewModel<StatsNFLTeamStats>) {
                            d(String.format("This is the enter point: DataMenuDataManager.subscribe nfl: %s", data.toString()))
                            view.setMainRecyclerViewData(getNFLOverviewDataFormattedList(data))
                        }
                    })
                }
                "nhl" -> {
                    DataMenuDataManager.subscribe(object : DataMenuOverviewWatcherNHL {
                        override fun onDataReady(data: DataMenuOverviewModel<StatsNHLTeamStats>) {
                            d(String.format("This is the enter point: DataMenuDataManager.subscribe nhl: %s", data.toString()))
                            view.setMainRecyclerViewData(getNHLOverviewDataFormattedList(data))
                        }
                    })
                }
            }
        }
    }

    /**
     * This method is used to subscribe watcher to Roto data
     */
    private fun subscribeRoto(team: Team?) {
        team?.league?.let {
            when (it.toLowerCase()){
                "mlb" -> {
                    DataMenuDataManager.subscribe(object : DataMenuRotoOverviewWatcherMLB {
                        override fun onDataReady(data: RotoResponseOverview<RotoStandings, RotoMLBCarousel>) {
                            d(String.format("This is the enter point: DataMenuDataManager.subscribe mlb: %s", data.toString()))
                            view.setMainRecyclerViewData(getMLBRotoOverviewDataFormattedList(data))
                        }
                    })
                }
                "nba" -> {
                    DataMenuDataManager.subscribe(object : DataMenuRotoOverviewWatcherNBA {
                        override fun onDataReady(data: RotoResponseOverview<RotoStandings, RotoNBACarousel>) {
                            d(String.format("This is the enter point: DataMenuDataManager.subscribe nba: %s", data.toString()))
                            view.setMainRecyclerViewData(getNBARotoOverviewDataFormattedList(data))
                        }
                    })
                }
                "nfl" -> {
                    DataMenuDataManager.subscribe(object : DataMenuRotoOverviewWatcherNFL {
                        override fun onDataReady(data: RotoResponseOverview<RotoStandings, RotoNFLCarousel>) {
                            d(String.format("This is the enter point: DataMenuDataManager.subscribe nfl: %s", data.toString()))
                            view.setMainRecyclerViewData(getNFLRotoOverviewDataFormattedList(data))
                        }
                    })
                }
                "nhl" -> {
                    DataMenuDataManager.subscribe(object : DataMenuRotoOverviewWatcherNHL {
                        override fun onDataReady(data: RotoResponseOverview<RotoStandings, RotoNHLCarousel>) {
                            d(String.format("This is the enter point: DataMenuDataManager.subscribe nhl: %s", data.toString()))
                            view.setMainRecyclerViewData(getNHLRotoOverviewDataFormattedList(data))
                        }
                    })
                }
            }
        }
    }

    /**
     * MLB Stats.com (deprecated)
     */
    @Deprecated("Replace with getMLBRotoOverviewDataFormattedList(...)")
    private fun getMLBOverviewDataFormattedList(data: StatsTeam): List<DataMenuOverviewDataModel> {
        val formattedList: MutableList<DataMenuOverviewDataModel> = mutableListOf()

        // Set up carousels
        val carouselsOverviewDataModel = DataMenuOverviewDataModel()
        val carouselsDataList: MutableList<DataMenuOverviewCarouselDataModel> = mutableListOf()

        // MLB record carousel model
        val mlbRecordDataModel = DataMenuOverviewCarouselDataModel()
        mlbRecordDataModel.carouselIsPlaceHolder = false
        if (LocalizationManager.isInitialized()) {
            mlbRecordDataModel.carouselTitle = LocalizationManager.DataMenu.Record
            mlbRecordDataModel.carouselLabel = LocalizationManager.DataMenu
                    .getDivisionRank(StringUtils.getOrdinalString(data.division?.rank ?: 0).toLowerCase())
        }
        mlbRecordDataModel.carouselProgress = (data.record?.percentage?.toFloat() ?: 0f) * 100.0f
        mlbRecordDataModel.carouselValue = DataBarUtil.getRecordForTeam(data.record, false, true, false)

        carouselsDataList.add(mlbRecordDataModel)

        // MLB games back carousel model
        val mlbGamesBackDataModel = DataMenuOverviewCarouselDataModel()
        mlbGamesBackDataModel.carouselIsPlaceHolder = false
        if (LocalizationManager.isInitialized()) {
            mlbGamesBackDataModel.carouselTitle = LocalizationManager.DataMenu.GamesBack
            mlbGamesBackDataModel.carouselLabel = LocalizationManager.DataMenu
                    .getDivisionRank(StringUtils.getOrdinalString(data.division?.rank ?: 0).toLowerCase())
        }
        mlbGamesBackDataModel.carouselProgress = (data.division?.gamesBehind?.toFloat() ?: 0f) / 20.0f * 100.0f
        mlbGamesBackDataModel.carouselValue = data.division?.gamesBehind?.toString() ?: ""

        carouselsDataList.add(mlbGamesBackDataModel)

        // MLB last ten carousel model
        val mlbLastTenDataModel = DataMenuOverviewCarouselDataModel()
        mlbLastTenDataModel.carouselIsPlaceHolder = false

        if (LocalizationManager.isInitialized()){
            mlbLastTenDataModel.carouselTitle = LocalizationManager.DataMenu.LastTen
            data.streaks?.let {
                for (streak in it){
                    if (!streak.kind.isEmpty()){
                        mlbLastTenDataModel.carouselLabel = LocalizationManager.DataMenu
                                .getStreak("" + streak.kind.substring(0, 1).toUpperCase() + streak.games)
                        break
                    }
                }
            }
        }
        data.recordDetails?.let {
            for (recordDetail in it){
                if (recordDetail.name.trim().equals("last 10", true)){
                    mlbLastTenDataModel.carouselProgress = recordDetail.wins.toFloat() / 10.0f * 100.0f
                    mlbLastTenDataModel.carouselValue = "" + recordDetail.wins + " - " + recordDetail.losses
                    break
                }
            }
        }

        carouselsDataList.add(mlbLastTenDataModel)

        carouselsOverviewDataModel.carouselList = carouselsDataList

        formattedList.add(carouselsOverviewDataModel)

        // CTA button
        // 1. Roster (off season) or Score (not off season)
        // 2. Schedule
        // 3. Standings
        if (view.isDataBarGameStateOffseason() && view.getConfig()?.dataMenu?.activeDetailsPages?.roster == true) {
            val ctaOneOverviewDataModel = DataMenuOverviewDataModel()
            formattedList.add(ctaOneOverviewDataModel)
            ctaOneOverviewDataModel.ctaId = DataMenuCTAId.ROSTER
            if (LocalizationManager.isInitialized()) {
                ctaOneOverviewDataModel.ctaTitle = LocalizationManager.DataMenu.Roster
                ctaOneOverviewDataModel.ctaSubtitle = ""
                DataMenuDataManager.subscribe(object : DataMenuRosterWatcher {
                    override fun onDataReady(data: RotoResponse<RotoPlayer>) {
                        ctaOneOverviewDataModel.ctaSubtitle = LocalizationManager.DataMenu.getPlayers(data.roster?.size.toString())
                        view.refreshAdapterContent(formattedList.indexOf(ctaOneOverviewDataModel))
                    }
                })
            }
        } else if (!view.isDataBarGameStateOffseason() && view.getConfig()?.dataMenu?.activeDetailsPages?.score == true) {
            val ctaOneOverviewDataModel = DataMenuOverviewDataModel()
            formattedList.add(ctaOneOverviewDataModel)
            ctaOneOverviewDataModel.ctaId = DataMenuCTAId.SCORE
            if (LocalizationManager.isInitialized()) {
                ctaOneOverviewDataModel.ctaTitle = LocalizationManager.DataMenu.Score
                ctaOneOverviewDataModel.ctaSubtitle = ""
                DataMenuDataManager.subscribe(object : DataMenuBoxScoreWatcherMLB {
                    override fun onDataReady(data: BoxEvent<BoxScoreMLB>) {
                        val homeTeam = data.teams.firstOrNull { it.isHome() }?.abbreviation?.toUpperCase()
                                ?: ""
                        val awayTeam = data.teams.firstOrNull { it.isAway() }?.abbreviation?.toUpperCase()
                                ?: ""
                        ctaOneOverviewDataModel.ctaSubtitle = LocalizationManager.DataMenu.getAwayVsHome(awayTeam, homeTeam)
                        view.refreshAdapterContent(formattedList.indexOf(ctaOneOverviewDataModel))
                    }
                })
            }
        }

        if (view.getConfig()?.dataMenu?.activeDetailsPages?.schedule == true) {
            val ctaTwoOverviewDataModel = DataMenuOverviewDataModel()
            formattedList.add(ctaTwoOverviewDataModel)
            ctaTwoOverviewDataModel.ctaId = DataMenuCTAId.SCHEDULE
            if (LocalizationManager.isInitialized()) {
                ctaTwoOverviewDataModel.ctaTitle = LocalizationManager.DataMenu.Schedule
                DataMenuDataManager.subscribe(object : DataMenuRotoScheduleWatcher {
                    override fun onDataReady(data: RotoResponse<RotoSchedule>) {
                        // For each event type, get the first pre game event if any,
                        // and use the local start date
                        data.schedule?.let {
                            val event = it.firstOrNull { e1 -> e1.isPreGame() }
                            val preGameStartDate = DateFormatUtils.getDateAsCalendarForFormat(event?.gameDateTime, "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                                    ?: return
                            val formatter: DateFormat = SimpleDateFormat("MMM d yyyy", Locale.ENGLISH)
                            ctaTwoOverviewDataModel.ctaSubtitle = LocalizationManager.DataMenu.getNextGame(formatter.format(preGameStartDate.time))
                            view.refreshAdapterContent(formattedList.indexOf(ctaTwoOverviewDataModel))
                        }
                    }
                })
            }
        }

        if (view.getConfig()?.dataMenu?.activeDetailsPages?.standings == true) {
            val ctaThreeOverviewDataModel = DataMenuOverviewDataModel()
            ctaThreeOverviewDataModel.ctaId = DataMenuCTAId.STANDINGS
            if (LocalizationManager.isInitialized()) {
                ctaThreeOverviewDataModel.ctaTitle = LocalizationManager.DataMenu.Standings
                ctaThreeOverviewDataModel.ctaSubtitle = LocalizationManager.DataMenu
                        .getDivisionRank(StringUtils.getOrdinalString(data.division?.rank ?: 0
                                ?: 0).toLowerCase())
            }
            formattedList.add(ctaThreeOverviewDataModel)
        }

        return formattedList
    }

    /**
     * MLB Roto
     */
    private fun getMLBRotoOverviewDataFormattedList(data: RotoResponseOverview<RotoStandings, RotoMLBCarousel>): List<DataMenuOverviewDataModel> {
        val formattedList: MutableList<DataMenuOverviewDataModel> = mutableListOf()

        // Set up carousels
        val carouselsOverviewDataModel = DataMenuOverviewDataModel()
        val carouselsDataList: MutableList<DataMenuOverviewCarouselDataModel> = mutableListOf()

        // MLB record carousel model (using carouselResponse)
        val mlbRecordDataModel = DataMenuOverviewCarouselDataModel()
        mlbRecordDataModel.carouselIsPlaceHolder = false
        if (LocalizationManager.isInitialized()) {
            mlbRecordDataModel.carouselTitle = LocalizationManager.DataMenu.Record
            mlbRecordDataModel.carouselLabel = LocalizationManager.DataMenu
                    .getDivisionRank(StringUtils.getOrdinalString(data.carouselResponse?.divisionRank ?: 0).toLowerCase())
        }
        val wins: Int = data.carouselResponse?.wins ?: 0
        val losses: Int = data.carouselResponse?.losses ?: 0
        mlbRecordDataModel.carouselProgress = when {
            (wins + losses) > 0 -> ((wins * 1.0f) / ((wins + losses) * 1.0f)) * 100.0f
            else -> 0f
        }
        mlbRecordDataModel.carouselValue = DataBarUtil.getRotoRecordForTeam(
                wins = wins, losses = losses, otl = 0, ties = 0, shootoutLosses = 0,
                isDataBar = false, isDataMenuCarousel = true)

        carouselsDataList.add(mlbRecordDataModel)

        // MLB games back carousel model (using carouselResponse)
        val mlbGamesBackDataModel = DataMenuOverviewCarouselDataModel()
        mlbGamesBackDataModel.carouselIsPlaceHolder = false
        if (LocalizationManager.isInitialized()) {
            mlbGamesBackDataModel.carouselTitle = LocalizationManager.DataMenu.GamesBack
            mlbGamesBackDataModel.carouselLabel = LocalizationManager.DataMenu
                    .getDivisionRank(StringUtils.getOrdinalString(data.carouselResponse?.divisionRank ?: 0).toLowerCase())
        }
        mlbGamesBackDataModel.carouselProgress = (data.carouselResponse?.divisionGamesBehind?.toFloat() ?: 0f) / 20.0f * 100.0f
        mlbGamesBackDataModel.carouselValue = String.format("%.1f", data.carouselResponse?.divisionGamesBehind?.toFloat() ?: 0f)

        carouselsDataList.add(mlbGamesBackDataModel)

        // MLB last ten carousel model (using carouselResponse)
        val mlbLastTenDataModel = DataMenuOverviewCarouselDataModel()
        mlbLastTenDataModel.carouselIsPlaceHolder = false

        if (LocalizationManager.isInitialized()){
            mlbLastTenDataModel.carouselTitle = LocalizationManager.DataMenu.LastTen
            mlbLastTenDataModel.carouselLabel = LocalizationManager.DataMenu.getStreak(
                    "${data.carouselResponse?.streaksKind?.substring(0, 1)?.toUpperCase() ?: ""}${data.carouselResponse?.streaksGames ?: 0}"
            )
        }
        mlbLastTenDataModel.carouselProgress = ((data.carouselResponse?.winsAmongLastTenGames ?: 0) * 1.0f) / 10.0f * 100.0f
        mlbLastTenDataModel.carouselValue = "${data.carouselResponse?.winsAmongLastTenGames ?: 0} - ${data.carouselResponse?.lossesAmongLastTenGames ?: 0}"

        carouselsDataList.add(mlbLastTenDataModel)

        carouselsOverviewDataModel.carouselList = carouselsDataList

        formattedList.add(carouselsOverviewDataModel)

        // CTA button
        // 1. Roster (off season) or Score (not off season)
        // 2. Schedule
        // 3. Standings
        if (view.isDataBarGameStateOffseason() && view.getConfig()?.dataMenu?.activeDetailsPages?.roster == true) {
            val ctaOneOverviewDataModel = DataMenuOverviewDataModel()
            formattedList.add(ctaOneOverviewDataModel)
            ctaOneOverviewDataModel.ctaId = DataMenuCTAId.ROSTER
            if (LocalizationManager.isInitialized()) {
                ctaOneOverviewDataModel.ctaTitle = LocalizationManager.DataMenu.Roster
                ctaOneOverviewDataModel.ctaSubtitle = ""
                DataMenuDataManager.subscribe(object : DataMenuRosterWatcher {
                    override fun onDataReady(data: RotoResponse<RotoPlayer>) {
                        ctaOneOverviewDataModel.ctaSubtitle = LocalizationManager.DataMenu.getPlayers(data.roster?.size.toString())
                        view.refreshAdapterContent(formattedList.indexOf(ctaOneOverviewDataModel))
                    }
                })
            }
        } else if (!view.isDataBarGameStateOffseason() && view.getConfig()?.dataMenu?.activeDetailsPages?.score == true) {
            val ctaOneOverviewDataModel = DataMenuOverviewDataModel()
            formattedList.add(ctaOneOverviewDataModel)
            ctaOneOverviewDataModel.ctaId = DataMenuCTAId.SCORE
            if (LocalizationManager.isInitialized()) {
                ctaOneOverviewDataModel.ctaTitle = LocalizationManager.DataMenu.Score
                ctaOneOverviewDataModel.ctaSubtitle = ""
                DataMenuDataManager.subscribe(object : DataMenuBoxScoreWatcherMLB {
                    override fun onDataReady(data: BoxEvent<BoxScoreMLB>) {
                        val homeTeam = data.teams.firstOrNull { it.isHome() }?.abbreviation?.toUpperCase()
                                ?: ""
                        val awayTeam = data.teams.firstOrNull { it.isAway() }?.abbreviation?.toUpperCase()
                                ?: ""
                        ctaOneOverviewDataModel.ctaSubtitle = LocalizationManager.DataMenu.getAwayVsHome(awayTeam, homeTeam)
                        view.refreshAdapterContent(formattedList.indexOf(ctaOneOverviewDataModel))
                    }
                })
            }
        }

        if (view.getConfig()?.dataMenu?.activeDetailsPages?.schedule == true) {
            val ctaTwoOverviewDataModel = DataMenuOverviewDataModel()
            formattedList.add(ctaTwoOverviewDataModel)
            ctaTwoOverviewDataModel.ctaId = DataMenuCTAId.SCHEDULE
            if (LocalizationManager.isInitialized()) {
                ctaTwoOverviewDataModel.ctaTitle = LocalizationManager.DataMenu.Schedule
                DataMenuDataManager.subscribe(object : DataMenuRotoScheduleWatcher {
                    override fun onDataReady(data: RotoResponse<RotoSchedule>) {
                        // For each event type, get the first pre game event if any,
                        // and use the local start date
                        data.schedule?.let {
                            val event = it.firstOrNull { e1 -> e1.isPreGame() }
                            val preGameStartDate = DateFormatUtils.getDateAsCalendarForFormat(event?.gameDateTime, "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                                    ?: return
                            val formatter: DateFormat = SimpleDateFormat("MMM d yyyy", Locale.ENGLISH)
                            ctaTwoOverviewDataModel.ctaSubtitle = LocalizationManager.DataMenu.getNextGame(formatter.format(preGameStartDate.time))
                            view.refreshAdapterContent(formattedList.indexOf(ctaTwoOverviewDataModel))
                        }
                    }
                })
            }
        }

        if (view.getConfig()?.dataMenu?.activeDetailsPages?.standings == true) {
            val ctaThreeOverviewDataModel = DataMenuOverviewDataModel()
            ctaThreeOverviewDataModel.ctaId = DataMenuCTAId.STANDINGS
            if (LocalizationManager.isInitialized()) {
                ctaThreeOverviewDataModel.ctaTitle = LocalizationManager.DataMenu.Standings
                ctaThreeOverviewDataModel.ctaSubtitle = LocalizationManager.DataMenu
                        .getDivisionRank(StringUtils.getOrdinalString(data.carouselResponse?.divisionRank
                                ?: 0).toLowerCase())
            }
            formattedList.add(ctaThreeOverviewDataModel)
        }

        return formattedList
    }

    /**
     * NBA Stats.com (deprecated)
     */
    @Deprecated("Replace with getNBARotoOverviewDataFormattedList(...)")
    private fun getNBAOverviewDataFormattedList(data: DataMenuOverviewModel<StatsNBATeamStats>): List<DataMenuOverviewDataModel> {
        val formattedList: MutableList<DataMenuOverviewDataModel> = mutableListOf()

        // Set up carousels
        val carouselsOverviewDataModel = DataMenuOverviewDataModel()
        val carouselsDataList: MutableList<DataMenuOverviewCarouselDataModel> = mutableListOf()

        // NBA record carousel model
        val nbaRecordDataModel = DataMenuOverviewCarouselDataModel()
        nbaRecordDataModel.carouselIsPlaceHolder = false
        if (LocalizationManager.isInitialized()) {
            nbaRecordDataModel.carouselTitle = LocalizationManager.DataMenu.Record
            nbaRecordDataModel.carouselLabel = LocalizationManager.DataMenu
                    .getConferenceRank(StringUtils.getOrdinalString(data.standingTeam.conference?.rank ?: 0).toLowerCase())
        }
        nbaRecordDataModel.carouselProgress = (data.standingTeam.record?.percentage?.toFloat() ?: 0f) * 100.0f
        nbaRecordDataModel.carouselValue = DataBarUtil.getRecordForTeam(data.standingTeam.record, false, true, false)

        carouselsDataList.add(nbaRecordDataModel)

        // NBA field goal carousel model
        val nbaFieldGoalDataModel = DataMenuOverviewCarouselDataModel()
        nbaFieldGoalDataModel.carouselIsPlaceHolder = false
        if (LocalizationManager.isInitialized()) {
            nbaFieldGoalDataModel.carouselTitle = LocalizationManager.DataMenu.FieldGoalPercent
        }
        data.statsEventSplit.teamStats?.let {
            for (statsNBATeamStats in it) {
                if (statsNBATeamStats.teamOwnFlag) {
                    nbaFieldGoalDataModel.carouselProgress = statsNBATeamStats.fieldGoals.percentage.toFloat() * 100.0f
                    // % is not in String.format(...) is because % is for input value
                    // So need to put % outside of String.format(...)
                    nbaFieldGoalDataModel.carouselValue = String.format("%.1f", (statsNBATeamStats.fieldGoals.percentage.toFloat() * 100.0f)) + "%"
                    if (LocalizationManager.isInitialized()) {
                        nbaFieldGoalDataModel.carouselLabel = LocalizationManager.DataMenu
                                .getPointsPerGame(statsNBATeamStats.pointsPerGame)
                    }
                    break
                }
            }
        }

        carouselsDataList.add(nbaFieldGoalDataModel)

        // NBA three point carousel model
        val nbaThreePointDataModel = DataMenuOverviewCarouselDataModel()
        nbaThreePointDataModel.carouselIsPlaceHolder = false
        if (LocalizationManager.isInitialized()) {
            nbaThreePointDataModel.carouselTitle = LocalizationManager.DataMenu.ThreePointPercent
        }
        data.statsEventSplit.teamStats?.let {
            for (statsNBATeamStats in it) {
                if (statsNBATeamStats.teamOwnFlag) {
                    nbaThreePointDataModel.carouselProgress = statsNBATeamStats.threePointFieldGoals.percentage.toFloat() * 100.0f
                    // % is not in String.format(...) is because % is for input value
                    // So need to put % outside of String.format(...)
                    nbaThreePointDataModel.carouselValue = String.format("%.1f", (statsNBATeamStats.threePointFieldGoals.percentage.toFloat() * 100.0f)) + "%"
                    if (LocalizationManager.isInitialized()) {
                        nbaThreePointDataModel.carouselLabel = LocalizationManager.DataMenu
                                .getEffectiveFieldGoalPercent(statsNBATeamStats.effectiveFieldGoalPercentage)
                    }
                    break
                }
            }
        }

        carouselsDataList.add(nbaThreePointDataModel)

        carouselsOverviewDataModel.carouselList = carouselsDataList

        formattedList.add(carouselsOverviewDataModel)

        // CTA button
        // 1. Roster (off season) or Score (not off season)
        // 2. Schedule
        // 3. Standings
        if (view.isDataBarGameStateOffseason() && view.getConfig()?.dataMenu?.activeDetailsPages?.roster == true) {
            val ctaOneOverviewDataModel = DataMenuOverviewDataModel()
            formattedList.add(ctaOneOverviewDataModel)
            ctaOneOverviewDataModel.ctaId = DataMenuCTAId.ROSTER
            if (LocalizationManager.isInitialized()) {
                ctaOneOverviewDataModel.ctaTitle = LocalizationManager.DataMenu.Roster
                ctaOneOverviewDataModel.ctaSubtitle = ""
                DataMenuDataManager.subscribe(object : DataMenuRosterWatcher {
                    override fun onDataReady(data: RotoResponse<RotoPlayer>) {
                        ctaOneOverviewDataModel.ctaSubtitle = LocalizationManager.DataMenu.getPlayers(data.roster?.size.toString())
                        view.refreshAdapterContent(formattedList.indexOf(ctaOneOverviewDataModel))
                    }
                })
            }
        } else if (!view.isDataBarGameStateOffseason() && view.getConfig()?.dataMenu?.activeDetailsPages?.score == true) {
            val ctaOneOverviewDataModel = DataMenuOverviewDataModel()
            formattedList.add(ctaOneOverviewDataModel)
            ctaOneOverviewDataModel.ctaId = DataMenuCTAId.SCORE
            if (LocalizationManager.isInitialized()) {
                ctaOneOverviewDataModel.ctaTitle = LocalizationManager.DataMenu.Score
                ctaOneOverviewDataModel.ctaSubtitle = ""
                DataMenuDataManager.subscribe(object : DataMenuBoxScoreWatcherNBA {
                    override fun onDataReady(data: BoxEvent<BoxScoreNBA>) {

                        val homeTeam = data.teams.firstOrNull { it.isHome() }?.abbreviation?.toUpperCase()
                                ?: ""
                        val awayTeam = data.teams.firstOrNull { it.isAway() }?.abbreviation?.toUpperCase()
                                ?: ""

                        ctaOneOverviewDataModel.ctaSubtitle = LocalizationManager.DataMenu.getAwayVsHome(awayTeam, homeTeam)
                        view.refreshAdapterContent(formattedList.indexOf(ctaOneOverviewDataModel))
                    }
                })
            }
        }

        if (view.getConfig()?.dataMenu?.activeDetailsPages?.schedule == true) {
            val ctaTwoOverviewDataModel = DataMenuOverviewDataModel()
            formattedList.add(ctaTwoOverviewDataModel)
            ctaTwoOverviewDataModel.ctaId = DataMenuCTAId.SCHEDULE
            if (LocalizationManager.isInitialized()) {
                ctaTwoOverviewDataModel.ctaTitle = LocalizationManager.DataMenu.Schedule

                DataMenuDataManager.subscribe(object : DataMenuRotoScheduleWatcher {
                    override fun onDataReady(data: RotoResponse<RotoSchedule>) {
                        data.schedule?.let {
                            val event = it.firstOrNull { e1 -> e1.isPreGame() }
                            val preGameStartDate = DateFormatUtils.getDateAsCalendarForFormat(event?.gameDateTime, "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                                    ?: return
                            val formatter: DateFormat = SimpleDateFormat("MMM d yyyy", Locale.ENGLISH)
                            ctaTwoOverviewDataModel.ctaSubtitle = LocalizationManager.DataMenu.getNextGame(formatter.format(preGameStartDate.time))
                            view.refreshAdapterContent(formattedList.indexOf(ctaTwoOverviewDataModel))
                        }
                    }
                })
            }
        }

        if (view.getConfig()?.dataMenu?.activeDetailsPages?.standings == true) {
            val ctaThreeOverviewDataModel = DataMenuOverviewDataModel()
            ctaThreeOverviewDataModel.ctaId = DataMenuCTAId.STANDINGS
            if (LocalizationManager.isInitialized()) {
                ctaThreeOverviewDataModel.ctaTitle = LocalizationManager.DataMenu.Standings
                ctaThreeOverviewDataModel.ctaSubtitle = LocalizationManager.DataMenu
                        .getDivisionRank(StringUtils.getOrdinalString(data.standingTeam.division?.rank ?: 0).toLowerCase())
            }
            formattedList.add(ctaThreeOverviewDataModel)
        }

        return formattedList
    }

    /**
     * NBA Roto
     */
    private fun getNBARotoOverviewDataFormattedList(data: RotoResponseOverview<RotoStandings, RotoNBACarousel>): List<DataMenuOverviewDataModel> {
        val formattedList: MutableList<DataMenuOverviewDataModel> = mutableListOf()

        // Set up carousels
        val carouselsOverviewDataModel = DataMenuOverviewDataModel()
        val carouselsDataList: MutableList<DataMenuOverviewCarouselDataModel> = mutableListOf()

        // NBA record carousel model (using standingsResponse)
        val nbaRecordDataModel = DataMenuOverviewCarouselDataModel()
        nbaRecordDataModel.carouselIsPlaceHolder = false
        if (LocalizationManager.isInitialized()) {
            nbaRecordDataModel.carouselTitle = LocalizationManager.DataMenu.Record
            nbaRecordDataModel.carouselLabel = LocalizationManager.DataMenu
                    .getConferenceRank(StringUtils.getOrdinalString(data.standingsResponse?.rankConference ?: 0).toLowerCase())
        }
        val wins: Int = data.standingsResponse?.wins ?: 0
        val losses: Int = data.standingsResponse?.losses ?: 0
        nbaRecordDataModel.carouselProgress = when {
            (wins + losses) > 0 -> ((wins * 1.0f) / ((wins + losses) * 1.0f)) * 100.0f
            else -> 0f
        }
        nbaRecordDataModel.carouselValue = DataBarUtil.getRotoRecordForTeam(
                wins = wins, losses = losses, otl = 0, ties = 0, shootoutLosses = 0,
                isDataBar = false, isDataMenuCarousel = true)

        carouselsDataList.add(nbaRecordDataModel)

        // NBA field goal carousel model (using carouselResponse)
        val nbaFieldGoalDataModel = DataMenuOverviewCarouselDataModel()
        nbaFieldGoalDataModel.carouselIsPlaceHolder = false
        if (LocalizationManager.isInitialized()) {
            nbaFieldGoalDataModel.carouselTitle = LocalizationManager.DataMenu.FieldGoalPercent
            nbaFieldGoalDataModel.carouselLabel = LocalizationManager.DataMenu
                    .getPointsPerGame(String.format("%.1f", (data.carouselResponse?.pointsPerGame?.toFloat() ?: 0f)))
        }
        nbaFieldGoalDataModel.carouselProgress = (data.carouselResponse?.fieldGoalPercentage?.toFloat() ?: 0f) * 100.0f
        // % is not in String.format(...) is because % is for input value
        // So need to put % outside of String.format(...)
        nbaFieldGoalDataModel.carouselValue = String.format("%.1f", ((data.carouselResponse?.fieldGoalPercentage?.toFloat() ?: 0f) * 100.0f)) + "%"

        carouselsDataList.add(nbaFieldGoalDataModel)

        // NBA three point carousel model (using carouselResponse)
        val nbaThreePointDataModel = DataMenuOverviewCarouselDataModel()
        nbaThreePointDataModel.carouselIsPlaceHolder = false
        if (LocalizationManager.isInitialized()) {
            nbaThreePointDataModel.carouselTitle = LocalizationManager.DataMenu.ThreePointPercent
            nbaThreePointDataModel.carouselLabel = LocalizationManager.DataMenu
                    .getEffectiveFieldGoalPercent(".${String.format("%.0f", ((data.carouselResponse?.effectiveFieldGoalPercentage?.toFloat() ?: 0f) * 1000.0f))}")
        }
        nbaThreePointDataModel.carouselProgress = (data.carouselResponse?.threePointFieldGoals?.toFloat() ?: 0f) * 100.0f
        // % is not in String.format(...) is because % is for input value
        // So need to put % outside of String.format(...)
        nbaThreePointDataModel.carouselValue = String.format("%.1f", ((data.carouselResponse?.threePointFieldGoals?.toFloat() ?: 0f) * 100.0f)) + "%"

        carouselsDataList.add(nbaThreePointDataModel)

        carouselsOverviewDataModel.carouselList = carouselsDataList

        formattedList.add(carouselsOverviewDataModel)

        // CTA button
        // 1. Roster (off season) or Score (not off season)
        // 2. Schedule
        // 3. Standings
        if (view.isDataBarGameStateOffseason() && view.getConfig()?.dataMenu?.activeDetailsPages?.roster == true) {
            val ctaOneOverviewDataModel = DataMenuOverviewDataModel()
            formattedList.add(ctaOneOverviewDataModel)
            ctaOneOverviewDataModel.ctaId = DataMenuCTAId.ROSTER
            if (LocalizationManager.isInitialized()) {
                ctaOneOverviewDataModel.ctaTitle = LocalizationManager.DataMenu.Roster
                ctaOneOverviewDataModel.ctaSubtitle = ""
                DataMenuDataManager.subscribe(object : DataMenuRosterWatcher {
                    override fun onDataReady(data: RotoResponse<RotoPlayer>) {
                        ctaOneOverviewDataModel.ctaSubtitle = LocalizationManager.DataMenu.getPlayers(data.roster?.size.toString())
                        view.refreshAdapterContent(formattedList.indexOf(ctaOneOverviewDataModel))
                    }
                })
            }
        } else if (!view.isDataBarGameStateOffseason() && view.getConfig()?.dataMenu?.activeDetailsPages?.score == true) {
            val ctaOneOverviewDataModel = DataMenuOverviewDataModel()
            formattedList.add(ctaOneOverviewDataModel)
            ctaOneOverviewDataModel.ctaId = DataMenuCTAId.SCORE
            if (LocalizationManager.isInitialized()) {
                ctaOneOverviewDataModel.ctaTitle = LocalizationManager.DataMenu.Score
                ctaOneOverviewDataModel.ctaSubtitle = ""
                DataMenuDataManager.subscribe(object : DataMenuBoxScoreWatcherNBA {
                    override fun onDataReady(data: BoxEvent<BoxScoreNBA>) {

                        val homeTeam = data.teams.firstOrNull { it.isHome() }?.abbreviation?.toUpperCase()
                                ?: ""
                        val awayTeam = data.teams.firstOrNull { it.isAway() }?.abbreviation?.toUpperCase()
                                ?: ""

                        ctaOneOverviewDataModel.ctaSubtitle = LocalizationManager.DataMenu.getAwayVsHome(awayTeam, homeTeam)
                        view.refreshAdapterContent(formattedList.indexOf(ctaOneOverviewDataModel))
                    }
                })
            }
        }

        if (view.getConfig()?.dataMenu?.activeDetailsPages?.schedule == true) {
            val ctaTwoOverviewDataModel = DataMenuOverviewDataModel()
            formattedList.add(ctaTwoOverviewDataModel)
            ctaTwoOverviewDataModel.ctaId = DataMenuCTAId.SCHEDULE
            if (LocalizationManager.isInitialized()) {
                ctaTwoOverviewDataModel.ctaTitle = LocalizationManager.DataMenu.Schedule

                DataMenuDataManager.subscribe(object : DataMenuRotoScheduleWatcher {
                    override fun onDataReady(data: RotoResponse<RotoSchedule>) {
                        data.schedule?.let {
                            val event = it.firstOrNull { e1 -> e1.isPreGame() }
                            val preGameStartDate = DateFormatUtils.getDateAsCalendarForFormat(event?.gameDateTime, "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                                    ?: return
                            val formatter: DateFormat = SimpleDateFormat("MMM d yyyy", Locale.ENGLISH)
                            ctaTwoOverviewDataModel.ctaSubtitle = LocalizationManager.DataMenu.getNextGame(formatter.format(preGameStartDate.time))
                            view.refreshAdapterContent(formattedList.indexOf(ctaTwoOverviewDataModel))
                        }
                    }
                })
            }
        }

        // Standings CTA (may need to change)
        if (view.getConfig()?.dataMenu?.activeDetailsPages?.standings == true) {
            val ctaThreeOverviewDataModel = DataMenuOverviewDataModel()
            ctaThreeOverviewDataModel.ctaId = DataMenuCTAId.STANDINGS
            if (LocalizationManager.isInitialized()) {
                ctaThreeOverviewDataModel.ctaTitle = LocalizationManager.DataMenu.Standings
                ctaThreeOverviewDataModel.ctaSubtitle = LocalizationManager.DataMenu
                        .getDivisionRank(StringUtils.getOrdinalString(data.standingsResponse?.rankDivision
                                ?: 0).toLowerCase())
            }
            formattedList.add(ctaThreeOverviewDataModel)
        }

        return formattedList
    }

    /**
     * NFL Stats.com (deprecated)
     */
    @Deprecated("Replace with getNFLRotoOverviewDataFormattedList(...)")
    private fun getNFLOverviewDataFormattedList(data: DataMenuOverviewModel<StatsNFLTeamStats>): List<DataMenuOverviewDataModel> {
        val formattedList: MutableList<DataMenuOverviewDataModel> = mutableListOf()

        // Set up carousels
        val carouselsOverviewDataModel = DataMenuOverviewDataModel()
        val carouselsDataList: MutableList<DataMenuOverviewCarouselDataModel> = mutableListOf()

        // NFL record carousel model
        val nflRecordDataModel = DataMenuOverviewCarouselDataModel()
        nflRecordDataModel.carouselIsPlaceHolder = false
        if (LocalizationManager.isInitialized()) {
            nflRecordDataModel.carouselTitle = LocalizationManager.DataMenu.Record
            nflRecordDataModel.carouselLabel = LocalizationManager.DataMenu
                    .getDivisionRank(StringUtils.getOrdinalString(data.standingTeam.division?.rank ?: 0).toLowerCase())
        }

        nflRecordDataModel.carouselProgress = (data.standingTeam.record?.percentage?.toFloat() ?: 0f) * 100.0f
        nflRecordDataModel.carouselValue = DataBarUtil.getRecordForTeam(data.standingTeam.record, false, true, true)

        carouselsDataList.add(nflRecordDataModel)

        // NFL red zone carousel model
        val nflRedZoneDataModel = DataMenuOverviewCarouselDataModel()
        nflRedZoneDataModel.carouselIsPlaceHolder = false
        if (LocalizationManager.isInitialized()) {
            nflRedZoneDataModel.carouselTitle = LocalizationManager.DataMenu.RedZonePercent
        }
        data.statsEventSplit.teamStats?.let {
            for (statsNFLTeamStats in it) {
                if (statsNFLTeamStats.teamOwnFlag) {
                    nflRedZoneDataModel.carouselProgress = statsNFLTeamStats.redZoneEfficiency.percentage.toFloat()
                    // % is not in String.format(...) is because % is for input value
                    // So need to put % outside of String.format(...)
                    nflRedZoneDataModel.carouselValue = String.format("%.1f", statsNFLTeamStats.redZoneEfficiency.percentage.toFloat()) + "%"
                    if (LocalizationManager.isInitialized()) {
                        nflRedZoneDataModel.carouselLabel = LocalizationManager.DataMenu
                                .getRedZoneScores("" + statsNFLTeamStats.redZoneEfficiency.made)
                    }
                    break
                }
            }
        }

        carouselsDataList.add(nflRedZoneDataModel)

        // NFL third down carousel model
        val nflThirdDownDataModel = DataMenuOverviewCarouselDataModel()
        nflThirdDownDataModel.carouselIsPlaceHolder = false
        if (LocalizationManager.isInitialized()) {
            nflThirdDownDataModel.carouselTitle = LocalizationManager.DataMenu.ThirdDownPercent
        }
        data.statsEventSplit.teamStats?.let {
            for (statsNFLTeamStats in it) {
                if (statsNFLTeamStats.teamOwnFlag) {
                    nflThirdDownDataModel.carouselProgress = statsNFLTeamStats.thirdDownEfficiency.percentage.toFloat()
                    // % is not in String.format(...) is because % is for input value
                    // So need to put % outside of String.format(...)
                    nflThirdDownDataModel.carouselValue = String.format("%.1f", statsNFLTeamStats.thirdDownEfficiency.percentage.toFloat()) + "%"
                    if (LocalizationManager.isInitialized()) {
                        nflThirdDownDataModel.carouselLabel = LocalizationManager.DataMenu
                                .getThirdDownEfficiency(String.format("%.2f", (statsNFLTeamStats.thirdDownEfficiency.attempts.toFloat() / statsNFLTeamStats.gamesPlayed.toFloat())))
                    }
                    break
                }
            }
        }

        carouselsDataList.add(nflThirdDownDataModel)

        carouselsOverviewDataModel.carouselList = carouselsDataList

        formattedList.add(carouselsOverviewDataModel)

        // CTA button
        // 1. Roster (off season) or Score (not off season)
        // 2. Schedule
        // 3. Standings
        if (view.isDataBarGameStateOffseason() && view.getConfig()?.dataMenu?.activeDetailsPages?.roster == true) {
            val ctaOneOverviewDataModel = DataMenuOverviewDataModel()
            formattedList.add(ctaOneOverviewDataModel)
            ctaOneOverviewDataModel.ctaId = DataMenuCTAId.ROSTER
            if (LocalizationManager.isInitialized()) {
                ctaOneOverviewDataModel.ctaTitle = LocalizationManager.DataMenu.Roster
                ctaOneOverviewDataModel.ctaSubtitle = ""
                DataMenuDataManager.subscribe(object : DataMenuRosterWatcher {
                    override fun onDataReady(data: RotoResponse<RotoPlayer>) {
                        ctaOneOverviewDataModel.ctaSubtitle = LocalizationManager.DataMenu.getPlayers(data.roster?.size.toString())
                        view.refreshAdapterContent(formattedList.indexOf(ctaOneOverviewDataModel))
                    }
                })
            }
        } else if (!view.isDataBarGameStateOffseason() && view.getConfig()?.dataMenu?.activeDetailsPages?.score == true) {
            val ctaOneOverviewDataModel = DataMenuOverviewDataModel()
            formattedList.add(ctaOneOverviewDataModel)
            ctaOneOverviewDataModel.ctaId = DataMenuCTAId.SCORE
            if (LocalizationManager.isInitialized()) {
                ctaOneOverviewDataModel.ctaTitle = LocalizationManager.DataMenu.Score
                ctaOneOverviewDataModel.ctaSubtitle = ""
                DataMenuDataManager.subscribe(object : DataMenuBoxScoreWatcherNFL {
                    override fun onDataReady(data: BoxEvent<BoxScoreNFL>) {
                        val homeTeam = data.teams.firstOrNull { it.isHome() }?.abbreviation?.toUpperCase()
                                ?: ""
                        val awayTeam = data.teams.firstOrNull { it.isAway() }?.abbreviation?.toUpperCase()
                                ?: ""
                        ctaOneOverviewDataModel.ctaSubtitle = LocalizationManager.DataMenu.getAwayVsHome(awayTeam, homeTeam)
                        view.refreshAdapterContent(formattedList.indexOf(ctaOneOverviewDataModel))
                    }
                })
            }
        }

        if (view.getConfig()?.dataMenu?.activeDetailsPages?.schedule == true) {
            val ctaTwoOverviewDataModel = DataMenuOverviewDataModel()
            formattedList.add(ctaTwoOverviewDataModel)
            ctaTwoOverviewDataModel.ctaId = DataMenuCTAId.SCHEDULE
            if (LocalizationManager.isInitialized()) {
                ctaTwoOverviewDataModel.ctaTitle = LocalizationManager.DataMenu.Schedule

                DataMenuDataManager.subscribe(object : DataMenuRotoScheduleWatcher {
                    override fun onDataReady(data: RotoResponse<RotoSchedule>) {
                        data.schedule?.let {
                            val event = it.firstOrNull { e1 -> e1.isPreGame() }
                            val preGameStartDate = DateFormatUtils.getDateAsCalendarForFormat(event?.gameDateTime, "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                                    ?: return
                            val formatter: DateFormat = SimpleDateFormat("MMM d yyyy", Locale.ENGLISH)
                            ctaTwoOverviewDataModel.ctaSubtitle = LocalizationManager.DataMenu.getNextGame(formatter.format(preGameStartDate.time))
                            view.refreshAdapterContent(formattedList.indexOf(ctaTwoOverviewDataModel))
                        }
                    }
                })
            }
        }

        if (view.getConfig()?.dataMenu?.activeDetailsPages?.standings == true) {
            val ctaThreeOverviewDataModel = DataMenuOverviewDataModel()
            ctaThreeOverviewDataModel.ctaId = DataMenuCTAId.STANDINGS
            if (LocalizationManager.isInitialized()) {
                ctaThreeOverviewDataModel.ctaTitle = LocalizationManager.DataMenu.Standings
                ctaThreeOverviewDataModel.ctaSubtitle = LocalizationManager.DataMenu
                        .getDivisionRank(StringUtils.getOrdinalString(data.standingTeam.division?.rank ?: 0).toLowerCase())
            }
            formattedList.add(ctaThreeOverviewDataModel)
        }

        return formattedList
    }

    /**
     * NFL Roto
     */
    private fun getNFLRotoOverviewDataFormattedList(data: RotoResponseOverview<RotoStandings, RotoNFLCarousel>): List<DataMenuOverviewDataModel> {
        val formattedList: MutableList<DataMenuOverviewDataModel> = mutableListOf()

        // Set up carousels
        val carouselsOverviewDataModel = DataMenuOverviewDataModel()
        val carouselsDataList: MutableList<DataMenuOverviewCarouselDataModel> = mutableListOf()

        // NFL record carousel model (using standingsResponse)
        val nflRecordDataModel = DataMenuOverviewCarouselDataModel()
        nflRecordDataModel.carouselIsPlaceHolder = false
        if (LocalizationManager.isInitialized()) {
            nflRecordDataModel.carouselTitle = LocalizationManager.DataMenu.Record
            nflRecordDataModel.carouselLabel = LocalizationManager.DataMenu
                    .getDivisionRank(StringUtils.getOrdinalString(data.standingsResponse?.rankDivision ?: 0).toLowerCase())
        }
        val wins: Int = data.standingsResponse?.wins ?: 0
        val losses: Int = data.standingsResponse?.losses ?: 0
        val ties: Int = data.standingsResponse?.ties ?: 0
        nflRecordDataModel.carouselProgress = when {
            (wins + losses + ties) > 0 -> ((wins * 1.0f) / ((wins + losses + ties) * 1.0f)) * 100.0f
            else -> 0f
        }
        nflRecordDataModel.carouselValue = DataBarUtil.getRotoRecordForTeam(
                wins = wins, losses = losses, otl = 0, ties = ties, shootoutLosses = 0,
                isDataBar = false, isDataMenuCarousel = true)

        carouselsDataList.add(nflRecordDataModel)

        // NFL red zone carousel model (using carouselResponse)
        val nflRedZoneDataModel = DataMenuOverviewCarouselDataModel()
        nflRedZoneDataModel.carouselIsPlaceHolder = false
        if (LocalizationManager.isInitialized()) {
            nflRedZoneDataModel.carouselTitle = LocalizationManager.DataMenu.RedZonePercent
            nflRedZoneDataModel.carouselLabel = LocalizationManager.DataMenu
                    .getRedZoneScores(String.format("%.0f", (data.carouselResponse?.redZoneSuccesses?.toFloat() ?: 0f)))
        }
        nflRedZoneDataModel.carouselProgress = (data.carouselResponse?.redZoneEfficiency?.toFloat() ?: 0f) * 100.0f
        // % is not in String.format(...) is because % is for input value
        // So need to put % outside of String.format(...)
        nflRedZoneDataModel.carouselValue = String.format("%.1f", ((data.carouselResponse?.redZoneEfficiency?.toFloat() ?: 0f) * 100.0f)) + "%"

        carouselsDataList.add(nflRedZoneDataModel)

        // NFL third down carousel model (using carouselResponse)
        val nflThirdDownDataModel = DataMenuOverviewCarouselDataModel()
        nflThirdDownDataModel.carouselIsPlaceHolder = false
        val gamesPlayed: Int = data.carouselResponse?.gamesPlayed ?: 0
        if (LocalizationManager.isInitialized()) {
            nflThirdDownDataModel.carouselTitle = LocalizationManager.DataMenu.ThirdDownPercent
            nflThirdDownDataModel.carouselLabel = LocalizationManager.DataMenu
                    .getThirdDownEfficiency(String.format("%.2f", when {
                        gamesPlayed > 0 -> (data.carouselResponse?.thirdDownAttempts?.toFloat() ?: 0f) / (gamesPlayed * 1.0f)
                        else -> 0f
                    }))
        }
        nflThirdDownDataModel.carouselProgress = (data.carouselResponse?.thirdDownEfficiency?.toFloat() ?: 0f) * 100.0f
        // % is not in String.format(...) is because % is for input value
        // So need to put % outside of String.format(...)
        nflThirdDownDataModel.carouselValue = String.format("%.1f", ((data.carouselResponse?.thirdDownEfficiency?.toFloat() ?: 0f) * 100.0f)) + "%"

        carouselsDataList.add(nflThirdDownDataModel)

        carouselsOverviewDataModel.carouselList = carouselsDataList

        formattedList.add(carouselsOverviewDataModel)

        // CTA button
        // 1. Roster (off season) or Score (not off season)
        // 2. Schedule
        // 3. Standings
        if (view.isDataBarGameStateOffseason() && view.getConfig()?.dataMenu?.activeDetailsPages?.roster == true) {
            val ctaOneOverviewDataModel = DataMenuOverviewDataModel()
            formattedList.add(ctaOneOverviewDataModel)
            ctaOneOverviewDataModel.ctaId = DataMenuCTAId.ROSTER
            if (LocalizationManager.isInitialized()) {
                ctaOneOverviewDataModel.ctaTitle = LocalizationManager.DataMenu.Roster
                ctaOneOverviewDataModel.ctaSubtitle = ""
                DataMenuDataManager.subscribe(object : DataMenuRosterWatcher {
                    override fun onDataReady(data: RotoResponse<RotoPlayer>) {
                        ctaOneOverviewDataModel.ctaSubtitle = LocalizationManager.DataMenu.getPlayers(data.roster?.size.toString())
                        view.refreshAdapterContent(formattedList.indexOf(ctaOneOverviewDataModel))
                    }
                })
            }
        } else if (!view.isDataBarGameStateOffseason() && view.getConfig()?.dataMenu?.activeDetailsPages?.score == true) {
            val ctaOneOverviewDataModel = DataMenuOverviewDataModel()
            formattedList.add(ctaOneOverviewDataModel)
            ctaOneOverviewDataModel.ctaId = DataMenuCTAId.SCORE
            if (LocalizationManager.isInitialized()) {
                ctaOneOverviewDataModel.ctaTitle = LocalizationManager.DataMenu.Score
                ctaOneOverviewDataModel.ctaSubtitle = ""
                DataMenuDataManager.subscribe(object : DataMenuBoxScoreWatcherNFL {
                    override fun onDataReady(data: BoxEvent<BoxScoreNFL>) {
                        val homeTeam = data.teams.firstOrNull { it.isHome() }?.abbreviation?.toUpperCase()
                                ?: ""
                        val awayTeam = data.teams.firstOrNull { it.isAway() }?.abbreviation?.toUpperCase()
                                ?: ""
                        ctaOneOverviewDataModel.ctaSubtitle = LocalizationManager.DataMenu.getAwayVsHome(awayTeam, homeTeam)
                        view.refreshAdapterContent(formattedList.indexOf(ctaOneOverviewDataModel))
                    }
                })
            }
        }

        if (view.getConfig()?.dataMenu?.activeDetailsPages?.schedule == true) {
            val ctaTwoOverviewDataModel = DataMenuOverviewDataModel()
            formattedList.add(ctaTwoOverviewDataModel)
            ctaTwoOverviewDataModel.ctaId = DataMenuCTAId.SCHEDULE
            if (LocalizationManager.isInitialized()) {
                ctaTwoOverviewDataModel.ctaTitle = LocalizationManager.DataMenu.Schedule

                DataMenuDataManager.subscribe(object : DataMenuRotoScheduleWatcher {
                    override fun onDataReady(data: RotoResponse<RotoSchedule>) {
                        data.schedule?.let {
                            val event = it.firstOrNull { e1 -> e1.isPreGame() }
                            val preGameStartDate = DateFormatUtils.getDateAsCalendarForFormat(event?.gameDateTime, "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                                    ?: return
                            val formatter: DateFormat = SimpleDateFormat("MMM d yyyy", Locale.ENGLISH)
                            ctaTwoOverviewDataModel.ctaSubtitle = LocalizationManager.DataMenu.getNextGame(formatter.format(preGameStartDate.time))
                            view.refreshAdapterContent(formattedList.indexOf(ctaTwoOverviewDataModel))
                        }
                    }
                })
            }
        }

        if (view.getConfig()?.dataMenu?.activeDetailsPages?.standings == true) {
            val ctaThreeOverviewDataModel = DataMenuOverviewDataModel()
            ctaThreeOverviewDataModel.ctaId = DataMenuCTAId.STANDINGS
            if (LocalizationManager.isInitialized()) {
                ctaThreeOverviewDataModel.ctaTitle = LocalizationManager.DataMenu.Standings
                ctaThreeOverviewDataModel.ctaSubtitle = LocalizationManager.DataMenu
                        .getDivisionRank(StringUtils.getOrdinalString(data.standingsResponse?.rankDivision
                                ?: 0).toLowerCase())
            }
            formattedList.add(ctaThreeOverviewDataModel)
        }

        return formattedList
    }

    /**
     * NHL Stats.com (deprecated)
     */
    @Deprecated("Replace with getNHLRotoOverviewDataFormattedList(...)")
    private fun getNHLOverviewDataFormattedList(data: DataMenuOverviewModel<StatsNHLTeamStats>): List<DataMenuOverviewDataModel> {
        val formattedList: MutableList<DataMenuOverviewDataModel> = mutableListOf()

        // Set up carousels
        val carouselsOverviewDataModel = DataMenuOverviewDataModel()
        val carouselsDataList: MutableList<DataMenuOverviewCarouselDataModel> = mutableListOf()

        // NHL record carousel model
        val nhlRecordDataModel = DataMenuOverviewCarouselDataModel()
        nhlRecordDataModel.carouselIsPlaceHolder = false

        if (LocalizationManager.isInitialized()) {
            nhlRecordDataModel.carouselTitle = LocalizationManager.DataMenu.Record
            nhlRecordDataModel.carouselLabel = LocalizationManager.DataMenu.getPointsPercent(data.standingTeam.record?.percentage)
        }
        nhlRecordDataModel.carouselProgress = (data.standingTeam.record?.percentage?.toFloat() ?: 0f) * 100.0f
        nhlRecordDataModel.carouselValue = DataBarUtil.getRecordForTeam(data.standingTeam.record, false, false, false)

        carouselsDataList.add(nhlRecordDataModel)

        // NHL power play carousel model
        val nhlPowerPlayDataModel = DataMenuOverviewCarouselDataModel()
        nhlPowerPlayDataModel.carouselIsPlaceHolder = false
        if (LocalizationManager.isInitialized()) {
            nhlPowerPlayDataModel.carouselTitle = LocalizationManager.DataMenu.PowerPlayPercent
        }
        data.statsEventSplit.teamStats?.let {
            for (statsNHLTeamStats in it) {
                nhlPowerPlayDataModel.carouselProgress = statsNHLTeamStats.goals.powerPlay.toFloat() / statsNHLTeamStats.powerPlayOpportunities.toFloat() * 100.0f
                // % is not in String.format(...) is because % is for input value
                // So need to put % outside of String.format(...)
                nhlPowerPlayDataModel.carouselValue = String.format("%.1f", statsNHLTeamStats.goals.powerPlay.toFloat() / statsNHLTeamStats.powerPlayOpportunities.toFloat() * 100.0f) + "%"
                if (LocalizationManager.isInitialized()) {
                    nhlPowerPlayDataModel.carouselLabel = LocalizationManager.DataMenu
                            .getPowerPlayGoalsPerGame("." + String.format("%.0f", (statsNHLTeamStats.goals.powerPlay.toFloat() / statsNHLTeamStats.gamesPlayed.toFloat() * 1000.0f)))
                }
                break
            }
        }

        carouselsDataList.add(nhlPowerPlayDataModel)

        // NHL save percentage carousel model
        val nhlSavePercentageDataModel = DataMenuOverviewCarouselDataModel()
        nhlSavePercentageDataModel.carouselIsPlaceHolder = false
        if (LocalizationManager.isInitialized()) {
            nhlSavePercentageDataModel.carouselTitle = LocalizationManager.DataMenu.SavePercent
        }
        data.statsEventSplit.teamStats?.let {
            for (statsNHLTeamStats in it) {
                nhlSavePercentageDataModel.carouselProgress = statsNHLTeamStats.savePercentage.toFloat() * 100.0f
                // % is not in String.format(...) is because % is for input value
                // So need to put % outside of String.format(...)
                nhlSavePercentageDataModel.carouselValue = String.format("%.1f", (statsNHLTeamStats.savePercentage.toFloat() * 100.0f)) + "%"
                if (LocalizationManager.isInitialized()) {
                    nhlSavePercentageDataModel.carouselLabel = LocalizationManager.DataMenu
                            .getShotsAgainstPerGame(String.format("%.1f", (statsNHLTeamStats.shotsAgainst.toFloat() / statsNHLTeamStats.gamesPlayed.toFloat())))
                }
                break
            }
        }

        carouselsDataList.add(nhlSavePercentageDataModel)

        carouselsOverviewDataModel.carouselList = carouselsDataList

        formattedList.add(carouselsOverviewDataModel)

        // CTA button
        // 1. Roster (off season) or Score (not off season)
        // 2. Schedule
        // 3. Standings
        if (view.isDataBarGameStateOffseason() && view.getConfig()?.dataMenu?.activeDetailsPages?.roster == true) {
            val ctaOneOverviewDataModel = DataMenuOverviewDataModel()
            formattedList.add(ctaOneOverviewDataModel)
            ctaOneOverviewDataModel.ctaId = DataMenuCTAId.ROSTER
            if (LocalizationManager.isInitialized()) {
                ctaOneOverviewDataModel.ctaTitle = LocalizationManager.DataMenu.Roster
                ctaOneOverviewDataModel.ctaSubtitle = ""
                DataMenuDataManager.subscribe(object : DataMenuRosterWatcher {
                    override fun onDataReady(data: RotoResponse<RotoPlayer>) {
                        ctaOneOverviewDataModel.ctaSubtitle = LocalizationManager.DataMenu.getPlayers(data.roster?.size.toString())
                        view.refreshAdapterContent(formattedList.indexOf(ctaOneOverviewDataModel))
                    }
                })
            }
        } else if (!view.isDataBarGameStateOffseason() && view.getConfig()?.dataMenu?.activeDetailsPages?.score == true) {
            val ctaOneOverviewDataModel = DataMenuOverviewDataModel()
            formattedList.add(ctaOneOverviewDataModel)
            ctaOneOverviewDataModel.ctaId = DataMenuCTAId.SCORE
            if (LocalizationManager.isInitialized()) {
                ctaOneOverviewDataModel.ctaTitle = LocalizationManager.DataMenu.Score
                ctaOneOverviewDataModel.ctaSubtitle = ""
                DataMenuDataManager.subscribe(object : DataMenuBoxScoreWatcherNHL {
                    override fun onDataReady(data: BoxEvent<BoxScoreNHL>) {
                        val homeTeam = data.teams.firstOrNull { it.isHome() }?.abbreviation?.toUpperCase()
                                ?: ""
                        val awayTeam = data.teams.firstOrNull { it.isAway() }?.abbreviation?.toUpperCase()
                                ?: ""
                        ctaOneOverviewDataModel.ctaSubtitle = LocalizationManager.DataMenu.getAwayVsHome(awayTeam, homeTeam)
                        view.refreshAdapterContent(formattedList.indexOf(ctaOneOverviewDataModel))
                    }
                })
            }
        }

        if (view.getConfig()?.dataMenu?.activeDetailsPages?.schedule == true) {
            val ctaTwoOverviewDataModel = DataMenuOverviewDataModel()
            formattedList.add(ctaTwoOverviewDataModel)
            ctaTwoOverviewDataModel.ctaId = DataMenuCTAId.SCHEDULE
            if (LocalizationManager.isInitialized()) {
                ctaTwoOverviewDataModel.ctaTitle = LocalizationManager.DataMenu.Schedule

                DataMenuDataManager.subscribe(object : DataMenuRotoScheduleWatcher {
                    override fun onDataReady(data: RotoResponse<RotoSchedule>) {
                        data.schedule?.let {
                            val event = it.firstOrNull { e1 -> e1.isPreGame() }
                            val preGameStartDate = DateFormatUtils.getDateAsCalendarForFormat(event?.gameDateTime, "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                                    ?: return
                            val formatter: DateFormat = SimpleDateFormat("MMM d yyyy", Locale.ENGLISH)
                            ctaTwoOverviewDataModel.ctaSubtitle = LocalizationManager.DataMenu.getNextGame(formatter.format(preGameStartDate.time))
                            view.refreshAdapterContent(formattedList.indexOf(ctaTwoOverviewDataModel))
                        }
                    }
                })
            }
        }

        if (view.getConfig()?.dataMenu?.activeDetailsPages?.standings == true) {
            val ctaThreeOverviewDataModel = DataMenuOverviewDataModel()
            ctaThreeOverviewDataModel.ctaId = DataMenuCTAId.STANDINGS
            if (LocalizationManager.isInitialized()) {
                ctaThreeOverviewDataModel.ctaTitle = LocalizationManager.DataMenu.Standings
                ctaThreeOverviewDataModel.ctaSubtitle = LocalizationManager.DataMenu
                        .getDivisionRank(StringUtils.getOrdinalString(data.standingTeam.division?.rank ?: 0).toLowerCase())
            }
            formattedList.add(ctaThreeOverviewDataModel)
        }

        return formattedList
    }

    /**
     * NHL Roto
     */
    private fun getNHLRotoOverviewDataFormattedList(data: RotoResponseOverview<RotoStandings, RotoNHLCarousel>): List<DataMenuOverviewDataModel> {
        val formattedList: MutableList<DataMenuOverviewDataModel> = mutableListOf()

        // Set up carousels
        val carouselsOverviewDataModel = DataMenuOverviewDataModel()
        val carouselsDataList: MutableList<DataMenuOverviewCarouselDataModel> = mutableListOf()

        // NHL record carousel model (using standingsResponse)
        val nhlRecordDataModel = DataMenuOverviewCarouselDataModel()
        nhlRecordDataModel.carouselIsPlaceHolder = false
        val wins: Int = data.standingsResponse?.wins ?: 0
        val losses: Int = data.standingsResponse?.losses ?: 0
        val otl: Int = data.standingsResponse?.otLosses ?: 0
        val standingsPoints: Int = data.standingsResponse?.standingsPoints ?: 0
        if (LocalizationManager.isInitialized()) {
            nhlRecordDataModel.carouselTitle = LocalizationManager.DataMenu.Record
            nhlRecordDataModel.carouselLabel = LocalizationManager.DataMenu.getPointsPercent(
                    ".${String.format("%.0f", when {
                        (wins + losses + otl) > 0 -> ((standingsPoints * 1.0f) / ((wins + losses + otl) * 2.0f)) * 1000.0f
                        else -> 0f
                    })}"
            )
        }
        nhlRecordDataModel.carouselProgress = when {
            (wins + losses + otl) > 0 -> ((standingsPoints * 1.0f) / ((wins + losses + otl) * 2.0f)) * 100.0f
            else -> 0f
        }
        nhlRecordDataModel.carouselValue = DataBarUtil.getRotoRecordForTeam(
                wins = wins, losses = losses, otl = otl, ties = 0, shootoutLosses = 0,
                isDataBar = false, isDataMenuCarousel = true)

        carouselsDataList.add(nhlRecordDataModel)

        // NHL power play carousel model (using carouselResponse)
        val nhlPowerPlayDataModel = DataMenuOverviewCarouselDataModel()
        nhlPowerPlayDataModel.carouselIsPlaceHolder = false
        val gamesPlayed: Int = data.carouselResponse?.gamesPlayed ?: 0
        val powerPlayGoals: Int = data.carouselResponse?.powerPlayGoals ?: 0
        val powerPlayOpportunities: Int = data.carouselResponse?.powerPlayOpportunities ?: 0
        if (LocalizationManager.isInitialized()) {
            nhlPowerPlayDataModel.carouselTitle = LocalizationManager.DataMenu.PowerPlayPercent
            nhlPowerPlayDataModel.carouselLabel = LocalizationManager.DataMenu
                    .getPowerPlayGoalsPerGame(
                            ".${String.format("%.0f", when {
                                gamesPlayed > 0 -> ((powerPlayGoals * 1.0f) / (gamesPlayed * 1.0f)) * 1000.0f
                                else -> 0f
                            })}"
                    )
        }
        nhlPowerPlayDataModel.carouselProgress = when {
            powerPlayOpportunities > 0 -> ((powerPlayGoals * 1.0f) / (powerPlayOpportunities * 1.0f)) * 100.0f
            else -> 0f
        }
        // % is not in String.format(...) is because % is for input value
        // So need to put % outside of String.format(...)
        nhlPowerPlayDataModel.carouselValue = String.format("%.1f", nhlPowerPlayDataModel.carouselProgress) + "%"

        carouselsDataList.add(nhlPowerPlayDataModel)

        // NHL save percentage carousel model (using carouselResponse)
        val nhlSavePercentageDataModel = DataMenuOverviewCarouselDataModel()
        nhlSavePercentageDataModel.carouselIsPlaceHolder = false
        val shotsAgainst: Int = data.carouselResponse?.shotsAgainst ?: 0
        if (LocalizationManager.isInitialized()) {
            nhlSavePercentageDataModel.carouselTitle = LocalizationManager.DataMenu.SavePercent
            nhlSavePercentageDataModel.carouselLabel = LocalizationManager.DataMenu
                    .getShotsAgainstPerGame(
                            String.format("%.1f", when {
                                gamesPlayed > 0 -> (shotsAgainst * 1.0f) / (gamesPlayed * 1.0f)
                                else -> 0f
                            })
                    )
        }
        nhlSavePercentageDataModel.carouselProgress = (data.carouselResponse?.savePercentage?.toFloat() ?: 0f) * 100.0f
        // % is not in String.format(...) is because % is for input value
        // So need to put % outside of String.format(...)
        nhlSavePercentageDataModel.carouselValue = String.format("%.1f", ((data.carouselResponse?.savePercentage?.toFloat() ?: 0f) * 100.0f)) + "%"

        carouselsDataList.add(nhlSavePercentageDataModel)

        carouselsOverviewDataModel.carouselList = carouselsDataList

        formattedList.add(carouselsOverviewDataModel)

        // CTA button
        // 1. Roster (off season) or Score (not off season)
        // 2. Schedule
        // 3. Standings
        if (view.isDataBarGameStateOffseason() && view.getConfig()?.dataMenu?.activeDetailsPages?.roster == true) {
            val ctaOneOverviewDataModel = DataMenuOverviewDataModel()
            formattedList.add(ctaOneOverviewDataModel)
            ctaOneOverviewDataModel.ctaId = DataMenuCTAId.ROSTER
            if (LocalizationManager.isInitialized()) {
                ctaOneOverviewDataModel.ctaTitle = LocalizationManager.DataMenu.Roster
                ctaOneOverviewDataModel.ctaSubtitle = ""
                DataMenuDataManager.subscribe(object : DataMenuRosterWatcher {
                    override fun onDataReady(data: RotoResponse<RotoPlayer>) {
                        ctaOneOverviewDataModel.ctaSubtitle = LocalizationManager.DataMenu.getPlayers(data.roster?.size.toString())
                        view.refreshAdapterContent(formattedList.indexOf(ctaOneOverviewDataModel))
                    }
                })
            }
        } else if (!view.isDataBarGameStateOffseason() && view.getConfig()?.dataMenu?.activeDetailsPages?.score == true) {
            val ctaOneOverviewDataModel = DataMenuOverviewDataModel()
            formattedList.add(ctaOneOverviewDataModel)
            ctaOneOverviewDataModel.ctaId = DataMenuCTAId.SCORE
            if (LocalizationManager.isInitialized()) {
                ctaOneOverviewDataModel.ctaTitle = LocalizationManager.DataMenu.Score
                ctaOneOverviewDataModel.ctaSubtitle = ""
                DataMenuDataManager.subscribe(object : DataMenuBoxScoreWatcherNHL {
                    override fun onDataReady(data: BoxEvent<BoxScoreNHL>) {
                        val homeTeam = data.teams.firstOrNull { it.isHome() }?.abbreviation?.toUpperCase()
                                ?: ""
                        val awayTeam = data.teams.firstOrNull { it.isAway() }?.abbreviation?.toUpperCase()
                                ?: ""
                        ctaOneOverviewDataModel.ctaSubtitle = LocalizationManager.DataMenu.getAwayVsHome(awayTeam, homeTeam)
                        view.refreshAdapterContent(formattedList.indexOf(ctaOneOverviewDataModel))
                    }
                })
            }
        }

        if (view.getConfig()?.dataMenu?.activeDetailsPages?.schedule == true) {
            val ctaTwoOverviewDataModel = DataMenuOverviewDataModel()
            formattedList.add(ctaTwoOverviewDataModel)
            ctaTwoOverviewDataModel.ctaId = DataMenuCTAId.SCHEDULE
            if (LocalizationManager.isInitialized()) {
                ctaTwoOverviewDataModel.ctaTitle = LocalizationManager.DataMenu.Schedule

                DataMenuDataManager.subscribe(object : DataMenuRotoScheduleWatcher {
                    override fun onDataReady(data: RotoResponse<RotoSchedule>) {
                        data.schedule?.let {
                            val event = it.firstOrNull { e1 -> e1.isPreGame() }
                            val preGameStartDate = DateFormatUtils.getDateAsCalendarForFormat(event?.gameDateTime, "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                                    ?: return
                            val formatter: DateFormat = SimpleDateFormat("MMM d yyyy", Locale.ENGLISH)
                            ctaTwoOverviewDataModel.ctaSubtitle = LocalizationManager.DataMenu.getNextGame(formatter.format(preGameStartDate.time))
                            view.refreshAdapterContent(formattedList.indexOf(ctaTwoOverviewDataModel))
                        }
                    }
                })
            }
        }

        if (view.getConfig()?.dataMenu?.activeDetailsPages?.standings == true) {
            val ctaThreeOverviewDataModel = DataMenuOverviewDataModel()
            ctaThreeOverviewDataModel.ctaId = DataMenuCTAId.STANDINGS
            if (LocalizationManager.isInitialized()) {
                ctaThreeOverviewDataModel.ctaTitle = LocalizationManager.DataMenu.Standings
                ctaThreeOverviewDataModel.ctaSubtitle = LocalizationManager.DataMenu
                        .getDivisionRank(StringUtils.getOrdinalString(data.standingsResponse?.rankDivision
                                ?: 0).toLowerCase())
            }
            formattedList.add(ctaThreeOverviewDataModel)
        }

        return formattedList
    }

    /**
     * This method is used to generate data menu place holder list
     */
    private fun getPlaceHolderItemList(): MutableList<DataMenuOverviewDataModel> {
        val placeHolderItemList: MutableList<DataMenuOverviewDataModel> = mutableListOf()

        // Set up carousel
        val carouselsOverviewDataModel = DataMenuOverviewDataModel()
        val carouselsDataList: MutableList<DataMenuOverviewCarouselDataModel> = mutableListOf()

        val placeHolderCarouselDataModel = DataMenuOverviewCarouselDataModel()
        placeHolderCarouselDataModel.carouselIsPlaceHolder = true

        carouselsDataList.add(placeHolderCarouselDataModel)

        carouselsOverviewDataModel.carouselList = carouselsDataList

        placeHolderItemList.add(carouselsOverviewDataModel)

        // CTA button
        // 1. Roster (off season) or Score (not off season)
        // 2. Schedule
        // 3. Standings
        if (view.isDataBarGameStateOffseason() && view.getConfig()?.dataMenu?.activeDetailsPages?.roster == true) {
            val ctaOneOverviewDataModel = DataMenuOverviewDataModel()
            placeHolderItemList.add(ctaOneOverviewDataModel)
            ctaOneOverviewDataModel.ctaId = DataMenuCTAId.ROSTER
            if (LocalizationManager.isInitialized()) {
                ctaOneOverviewDataModel.ctaTitle = LocalizationManager.DataMenu.Roster
                ctaOneOverviewDataModel.ctaSubtitle = ""
                DataMenuDataManager.subscribe(object : DataMenuRosterWatcher {
                    override fun onDataReady(data: RotoResponse<RotoPlayer>) {
                        ctaOneOverviewDataModel.ctaSubtitle = LocalizationManager.DataMenu.getPlayers(data.roster?.size.toString())
                        view.refreshAdapterContent(placeHolderItemList.indexOf(ctaOneOverviewDataModel))
                    }
                })
            }
        } else if (!view.isDataBarGameStateOffseason() && view.getConfig()?.dataMenu?.activeDetailsPages?.score == true) {
            val ctaOneOverviewDataModel = DataMenuOverviewDataModel()
            ctaOneOverviewDataModel.ctaId = DataMenuCTAId.SCORE
            if (LocalizationManager.isInitialized()) {
                ctaOneOverviewDataModel.ctaTitle = LocalizationManager.DataMenu.Score
                ctaOneOverviewDataModel.ctaSubtitle = ""
            }
            placeHolderItemList.add(ctaOneOverviewDataModel)
        }

        if (view.getConfig()?.dataMenu?.activeDetailsPages?.schedule == true) {
            val ctaTwoOverviewDataModel = DataMenuOverviewDataModel()
            placeHolderItemList.add(ctaTwoOverviewDataModel)
            ctaTwoOverviewDataModel.ctaId = DataMenuCTAId.SCHEDULE
            if (LocalizationManager.isInitialized()) {
                ctaTwoOverviewDataModel.ctaTitle = LocalizationManager.DataMenu.Schedule
                ctaTwoOverviewDataModel.ctaSubtitle = ""

                DataMenuDataManager.subscribe(object : DataMenuRotoScheduleWatcher {
                    override fun onDataReady(data: RotoResponse<RotoSchedule>) {
                        data.schedule?.let {
                            val event = it.firstOrNull { e1 -> e1.isPreGame() }
                            val preGameStartDate = DateFormatUtils.getDateAsCalendarForFormat(event?.gameDateTime, "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                                    ?: return
                            val formatter: DateFormat = SimpleDateFormat("MMM d yyyy", Locale.ENGLISH)
                            ctaTwoOverviewDataModel.ctaSubtitle = LocalizationManager.DataMenu.getNextGame(formatter.format(preGameStartDate.time))
                            view.refreshAdapterContent(placeHolderItemList.indexOf(ctaTwoOverviewDataModel))
                        }
                    }
                })
            }
        }

        if (view.getConfig()?.dataMenu?.activeDetailsPages?.standings == true) {
            val ctaThreeOverviewDataModel = DataMenuOverviewDataModel()
            placeHolderItemList.add(ctaThreeOverviewDataModel)
            ctaThreeOverviewDataModel.ctaId = DataMenuCTAId.STANDINGS
            if (LocalizationManager.isInitialized()) {
                ctaThreeOverviewDataModel.ctaTitle = LocalizationManager.DataMenu.Standings
                ctaThreeOverviewDataModel.ctaSubtitle = ""
                DataMenuDataManager.subscribe(object : DataMenuStandingWatcher {
                    override fun onDataReady(data: StatsTeam) {
                        ctaThreeOverviewDataModel.ctaSubtitle = LocalizationManager.DataMenu
                                .getDivisionRank(StringUtils.getOrdinalString(data.division?.rank ?: 0).toLowerCase())
                        view.refreshAdapterContent(placeHolderItemList.indexOf(ctaThreeOverviewDataModel))
                    }
                })
            }
        }

        return placeHolderItemList
    }

}