package com.nbcsports.regional.nbc_rsn.data_menu.intent

import android.os.Parcelable
import com.nbcsports.regional.nbc_rsn.data_bar.Season
import com.nbcsports.regional.nbc_rsn.data_bar.StatsTeam
import com.nbcsports.regional.nbc_rsn.data_menu.standings.LeagueStandings
import com.nbcsports.regional.nbc_rsn.data_menu.DataMenuContract
import com.nbcsports.regional.nbc_rsn.data_menu.models.*

/*
* Note: Since we are using BehaviorSubject, so no notifyWatcher or notifyAllWatchers is needed
* */

// region Overview Watchers
interface DataMenuOverviewWatcher<T: Parcelable>: DataMenuContract.DataWatcher<DataMenuOverviewModel<T>>

interface DataMenuOverviewWatcherNFL: DataMenuOverviewWatcher<StatsNFLTeamStats>
interface DataMenuOverviewWatcherNHL: DataMenuOverviewWatcher<StatsNHLTeamStats>
interface DataMenuOverviewWatcherNBA: DataMenuOverviewWatcher<StatsNBATeamStats>
// For MLB Overview, please use DataMenuStandingWatcher
// endregion

// region Roto Overview Watchers
interface DataMenuRotoOverviewWatcherMLB: DataMenuContract.DataWatcher<RotoResponseOverview<RotoStandings, RotoMLBCarousel>>
interface DataMenuRotoOverviewWatcherNBA: DataMenuContract.DataWatcher<RotoResponseOverview<RotoStandings, RotoNBACarousel>>
interface DataMenuRotoOverviewWatcherNFL: DataMenuContract.DataWatcher<RotoResponseOverview<RotoStandings, RotoNFLCarousel>>
interface DataMenuRotoOverviewWatcherNHL: DataMenuContract.DataWatcher<RotoResponseOverview<RotoStandings, RotoNHLCarousel>>
// endregion

// schedules
interface DataMenuScheduleWatcher: DataMenuContract.DataWatcher<Season>

// region Box score watchers
interface DataMenuBoxScoreWatcher<T: Parcelable>: DataMenuContract.DataWatcher<BoxEvent<T>>

interface DataMenuBoxScoreWatcherMLB: DataMenuBoxScoreWatcher<BoxScoreMLB>
interface DataMenuBoxScoreWatcherNBA: DataMenuBoxScoreWatcher<BoxScoreNBA>
interface DataMenuBoxScoreWatcherNHL: DataMenuBoxScoreWatcher<BoxScoreNHL>
interface DataMenuBoxScoreWatcherNFL: DataMenuBoxScoreWatcher<BoxScoreNFL>
// endregion

//roster
interface DataMenuRosterWatcher: DataMenuContract.DataWatcher<RotoResponse<RotoPlayer>>

// schedules
interface DataMenuRotoScheduleWatcher: DataMenuContract.DataWatcher<RotoResponse<RotoSchedule>>

// standings
interface DataMenuLeagueStandingsWatcher: DataMenuContract.DataWatcher<LeagueStandings>
interface DataMenuStandingWatcher: DataMenuContract.DataWatcher<StatsTeam>
