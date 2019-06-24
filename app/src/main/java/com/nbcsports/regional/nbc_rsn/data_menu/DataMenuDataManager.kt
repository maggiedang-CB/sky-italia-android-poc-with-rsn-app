package com.nbcsports.regional.nbc_rsn.data_menu

import android.os.Parcelable
import android.util.SparseArray
import androidx.core.util.forEach
import androidx.core.util.getOrDefault
import com.google.gson.GsonBuilder
import com.nbcsports.regional.nbc_rsn.common.Config
import com.nbcsports.regional.nbc_rsn.common.Team
import com.nbcsports.regional.nbc_rsn.common.TeamManager
import com.nbcsports.regional.nbc_rsn.data_bar.*
import com.nbcsports.regional.nbc_rsn.data_menu.api.DataMenuRotoAPI
import com.nbcsports.regional.nbc_rsn.data_menu.api.DataMenuStatsComAPI
import com.nbcsports.regional.nbc_rsn.data_menu.standings.*
import com.nbcsports.regional.nbc_rsn.data_menu.intent.*
import com.nbcsports.regional.nbc_rsn.data_menu.models.*
import com.nbcsports.regional.nbc_rsn.extensions.asFormattedCalendarDate
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.exceptions.Exceptions
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*

object DataMenuDataManager : DataMenuContract.DataManager {

    private val MAX_OLDEST_YEAR_DATA = 2017

    private val subscriptions: CompositeDisposable = CompositeDisposable()
    private var okHttpClientStatsCom: OkHttpClient.Builder = OkHttpClient.Builder()
    private var okHttpClientRoto: OkHttpClient.Builder = OkHttpClient.Builder()

    // APIs
    private var dataStatsComApi: DataMenuStatsComAPI? = null
    private var dataMenuRotoApi: DataMenuRotoAPI? = null

    // region Subjects
    /**
     * Use this instead of [subscriptions] so we can remove a single watcher.
     */
    private val subjectDisposables = SparseArray<Disposable>()

    private val subjectRoster = BehaviorSubject.create<RotoResponse<RotoPlayer>>()
    private val subjectRotoSchedule = BehaviorSubject.create<RotoResponse<RotoSchedule>>()

    // if you add another type of data, don't forget to add it in subscribe(DataMenuContract.DataWatcher<T>)
    private val subjectStandings = BehaviorSubject.create<StatsTeam>()
    private val subjectSeasonLeagueStandings = BehaviorSubject.create<LeagueStandings>()

    // if you add another data with generic type, don't forget the following
    // 1. Implement a method like subscribeOverview(DataMenuContract.DataWatcher<T>)
    // 2. Call your method in subscribe(DataMenuContract.DataWatcher<T>)
    private val subjectOverview = BehaviorSubject.create<DataMenuOverviewModel<out Parcelable>>()
    private val subjectRotoOverview = BehaviorSubject.create<RotoResponseOverview<out Parcelable, out Parcelable>>()
    private val subjectBoxScore = BehaviorSubject.create<BoxEvent<out Parcelable>>()
    // endregion

    // shortcut variable
    var currentSeasonId = 0
        private set
    private val liveEventIds: SparseArray<Long> = SparseArray()

    init {
        okHttpClientStatsCom.addInterceptor { chain: Interceptor.Chain? ->
            val request = chain?.request()?.newBuilder()?.addHeader("x-api-key", DataBarConfig.STATS_API_KEY)?.build()
            chain?.proceed(request!!)
        }
        okHttpClientRoto.addInterceptor { chain: Interceptor.Chain? ->
            val request = chain?.request()?.newBuilder()?.addHeader("x-api-key", DataBarConfig.ROTO_API_KEY)?.build()
            chain?.proceed(request!!)
        }
    }

    fun setConfigInfo(configInfo: Config.DataMenuConfigInfo) {
        val retrofitStatsApi = Retrofit.Builder()
                .baseUrl(configInfo.statsBaseUrl)
                .addConverterFactory(
                        GsonConverterFactory.create(
                                GsonBuilder()
                                        .registerTypeAdapterFactory(StatsApiJsonFactory())
                                        .create()
                        )
                )
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
                .client(okHttpClientStatsCom.build())
                .build()

        val retrofitRotoApi = Retrofit.Builder()
                .baseUrl(configInfo.dataMenuBaseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
                .client(okHttpClientRoto.build())
                .build()

        dataStatsComApi = retrofitStatsApi.create(DataMenuStatsComAPI::class.java)
        dataMenuRotoApi = retrofitRotoApi.create(DataMenuRotoAPI::class.java)
    }

    fun update(statsTeamId: Int) {
        getOverview(statsTeamId)
        getTeamRoster(statsTeamId)
        getLeagueStandings(statsTeamId)
        getRotoSchedule(statsTeamId)
        getRotoScheduleAndBoxScore(statsTeamId)
    }

    @Synchronized
    fun notifyEventStatus(teamId: Int, eventId: Long, gameStatusId: Int) {
        when (gameStatusId) {
            // Live-Game
            GameState.IN_PROGRESS.eid, GameState.DELAYED.eid -> liveEventIds.put(teamId, eventId)
            else -> liveEventIds.remove(teamId)
        }
    }

    // region Subscribe
    fun <T : Parcelable> subscribe(newWatcher: DataMenuContract.DataWatcher<T>) {
        (trySubscribeToSubject(subjectStandings, { it.teamId >= 0 }, newWatcher as? DataMenuStandingWatcher)
                ?: trySubscribeToSubject(subjectRoster, {
                    it.roster?.isNotEmpty() ?: false
                }, newWatcher as? DataMenuRosterWatcher)
                ?: trySubscribeToSubject(subjectRotoSchedule, { it.schedule?.isNotEmpty() == true }, newWatcher as? DataMenuRotoScheduleWatcher)
                ?: trySubscribeToSubject(subjectSeasonLeagueStandings, { it.tabs != null }, newWatcher as? DataMenuLeagueStandingsWatcher)
                ?: subscribeBoxScore(newWatcher)
                ?: subscribeOverview(newWatcher)
                ?: subscribeRotoOverview(newWatcher)
                )?.let { subjectDisposables.append(newWatcher.hashCode(), it) }
    }

    private inline fun <reified T : Parcelable> trySubscribeToSubject(
            targetSubject: BehaviorSubject<T>,
            noinline filter: (T) -> Boolean,
            watcher: DataMenuContract.DataWatcher<T>?
    ): Disposable? {
        watcher ?: return null
        return targetSubject.filter(filter).subscribe(watcher::onDataReady)
    }

    // region Subscribe box score
    private fun <T : Parcelable> subscribeBoxScore(newWatcher: DataMenuContract.DataWatcher<T>): Disposable? {
        return trySubscribeToBoxScoreSubject(newWatcher as? DataMenuBoxScoreWatcherMLB)
                ?: trySubscribeToBoxScoreSubject(newWatcher as? DataMenuBoxScoreWatcherNBA)
                ?: trySubscribeToBoxScoreSubject(newWatcher as? DataMenuBoxScoreWatcherNHL)
                ?: trySubscribeToBoxScoreSubject(newWatcher as? DataMenuBoxScoreWatcherNFL)
    }

    private inline fun <reified T : Parcelable> trySubscribeToBoxScoreSubject(
            watcher: DataMenuBoxScoreWatcher<T>?
    ): Disposable? {
        watcher ?: return null
        return subjectBoxScore.filter { it.eventId >= 0 }.subscribe { data -> watcher.onDataReady(data as BoxEvent<T>) }
    }
    // endregion

    // region Subscribe Overview
    private fun <T : Parcelable> subscribeOverview(newWatcher: DataMenuContract.DataWatcher<T>): Disposable? {
        return trySubscribeToOverviewSubject(newWatcher as? DataMenuOverviewWatcherNFL)
                ?: trySubscribeToOverviewSubject(newWatcher as? DataMenuOverviewWatcherNBA)
                ?: trySubscribeToOverviewSubject(newWatcher as? DataMenuOverviewWatcherNHL)
    }

    private inline fun <reified T : Parcelable> trySubscribeToOverviewSubject(
            watcher: DataMenuOverviewWatcher<T>?
    ): Disposable? {
        watcher ?: return null
        return subjectOverview.filter { it.standingTeam.teamId >= 0 }
                .subscribe { data ->
                    data.statsEventSplit.castTo<T>()?.let {
                        watcher.onDataReady(DataMenuOverviewModel(it, data.standingTeam))
                    }
                }
    }
    // endregion

    // region Subscribe Roto Overview
    private fun <T : Parcelable> subscribeRotoOverview(newWatcher: DataMenuContract.DataWatcher<T>): Disposable? {
        return trySubscribeToRotoOverviewSubject(newWatcher as? DataMenuRotoOverviewWatcherMLB)
                ?: trySubscribeToRotoOverviewSubject(newWatcher as? DataMenuRotoOverviewWatcherNBA)
                ?: trySubscribeToRotoOverviewSubject(newWatcher as? DataMenuRotoOverviewWatcherNFL)
                ?: trySubscribeToRotoOverviewSubject(newWatcher as? DataMenuRotoOverviewWatcherNHL)
    }

    private inline fun <reified T : Parcelable> trySubscribeToRotoOverviewSubject(
            watcher: DataMenuContract.DataWatcher<T>?
    ): Disposable? {
        watcher ?: return null
        return subjectRotoOverview.subscribe { data ->
            watcher.onDataReady(data as T)
        }
    }
    // endregion

    // endregion

    // region Unsubscribe
    fun <T : Parcelable> unsubscribe(watcherToRemove: DataMenuContract.DataWatcher<T>) {
        watcherToRemove.hashCode().let {
            subjectDisposables[it]?.dispose()
            subjectDisposables.remove(it)
        }
    }

    @Synchronized
    fun unsubscribeAll() {
        subscriptions.clear()
        subjectDisposables.forEach { _, disposable -> disposable.dispose() }
        subjectDisposables.clear()

        // now reset subjects, so when new watchers subscribe they won't get data from the last team.
        subjectRoster.onNext(RotoResponse())
        subjectRotoSchedule.onNext(RotoResponse())

        subjectStandings.onNext(StatsTeam())
        subjectSeasonLeagueStandings.onNext(LeagueStandings())

        subjectOverview.onNext(DataMenuOverviewModel())
        subjectRotoOverview.onNext(RotoResponseOverview())
        subjectBoxScore.onNext(BoxEvent())
    }
    // endregion

    // region Roster
    private fun getTeamRoster(statsTeamId: Int) {
        val team = TeamManager.getInstance()?.getTeamByStatsId(statsTeamId)
        val (_, leagueName) = DataBarUtil.getRequestParams(team)

        subscriptions.add(
                DataMenuDataManager.getTeamRoster(statsTeamId, leagueName)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            subjectRoster.onNext(it)
                        }
        )
    }

    private fun getTeamRoster(statsTeamId: Int, leagueName: String): Observable<RotoResponse<RotoPlayer>> {
        return Observable.defer<Response<RotoResponse<RotoPlayer>>> {
            dataMenuRotoApi?.getRotoRoster(league = leagueName.toUpperCase(), teamId = statsTeamId)
        }.subscribeOn(Schedulers.io())
                .map { it.body()!! }
    }
    // endregion

    // region Schedules
    private fun getRotoSchedule(statsTeamId: Int) {
        val team = TeamManager.getInstance()?.getTeamByStatsId(statsTeamId)
        val (_, leagueName) = DataBarUtil.getRequestParams(team)
        val season = DataBarUtil.getCurrentSeasonForDataMenu(team)

        DataMenuDataManager.getRotoSchedule(statsTeamId, leagueName, season)
    }

    private fun getRotoSchedule(statsTeamId: Int, leagueName: String, season: Int) {
        if (season <= MAX_OLDEST_YEAR_DATA) return

        subscriptions.add(
                Observable.defer<Response<RotoResponse<RotoSchedule>>> {
                    dataMenuRotoApi?.getRotoSchedule(league = leagueName.toUpperCase(), teamId = statsTeamId, season = season)
                }.subscribeOn(Schedulers.io())
                        .map { it.body()!! }
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { response ->
                            if (response?.schedule.isNullOrEmpty()) {
                                DataMenuDataManager.getRotoSchedule(statsTeamId, leagueName, season - 1)
                                return@subscribe
                            }
                            subjectRotoSchedule.onNext(response)
                        }
        )
    }

    private fun getRotoScheduleAndBoxScore(statsTeamId: Int) {
        val team = TeamManager.getInstance()?.getTeamByStatsId(statsTeamId)
        val (_, leagueName) = DataBarUtil.getRequestParams(team)
        val season = DataBarUtil.getCurrentSeasonForDataMenu(team)

        DataMenuDataManager.getRotoScheduleAndBoxScore(statsTeamId, team, leagueName, season)
    }

    private fun getRotoScheduleAndBoxScore(statsTeamId: Int, team: Team? = null, leagueName: String, season: Int) {
        if (season <= MAX_OLDEST_YEAR_DATA) return

        subscriptions.add(
                Observable.defer<Response<RotoResponse<RotoSchedule>>> {
                    dataMenuRotoApi?.getRotoSchedule(league = leagueName.toUpperCase(), teamId = statsTeamId, season = season)
                }.subscribeOn(Schedulers.io())
                        .map { it.body()!! }
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnNext { response ->
                            if (response?.schedule.isNullOrEmpty()) {
                                DataMenuDataManager.getRotoScheduleAndBoxScore(statsTeamId, team, leagueName, season - 1)
                                throw Exceptions.propagate(Throwable("getRotoScheduleAndBoxScore exception"))
                            }
                            subjectRotoSchedule.onNext(response)
                        }
                        .flatMap {
                            DataMenuDataManager.getEventBoxScore(team, it.schedule!!)
                        }
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            subjectBoxScore.onNext(it)
                        }
        )
    }
    // endregion

    // region Overview
    private fun getOverview(statsTeamId: Int) {
        val team = TeamManager.getInstance()?.getTeamByStatsId(statsTeamId)
        val (_, leagueName) = DataBarUtil.getRequestParams(team)
        val season = DataBarUtil.getCurrentSeasonForDataMenu(team)

        DataMenuDataManager.getRotoOverview(statsTeamId, leagueName, season)
    }

    @Deprecated("Replace with getRotoOverview(...)")
    private fun getOverview(statsTeamId: Int, leagueName: String, season: Int) {
        if (season <= MAX_OLDEST_YEAR_DATA) return

        // MLB doesn't require a TeamStats call
        subscriptions.add(
                if (leagueName.toLowerCase() == "mlb") {
                    DataMenuDataManager.getTeamStanding(statsTeamId)
                            .subscribeOn(AndroidSchedulers.mainThread())
                            .subscribe { subjectStandings.onNext(it) }
                } else {
                    Observable.zip(
                            getTeamStanding(statsTeamId),
                            getTeamStatsIfNeeded(statsTeamId),
                            BiFunction<StatsTeam, StatsEventSplit<out Parcelable>?, Unit> { statsTeam, resultStats ->
                                subjectOverview.onNext(DataMenuOverviewModel(resultStats, statsTeam))
                            }
                    ).subscribeOn(AndroidSchedulers.mainThread())
                            .doOnError {
                                DataMenuDataManager.getOverview(statsTeamId, leagueName, season - 1)
                            }
                            .subscribe()
                }
        )
    }

    /**
     * This method is used to get overview data from Roto including:
     *     1. Team standings
     *     2. Carousel
     */
    private fun getRotoOverview(statsTeamId: Int, leagueName: String, season: Int) {
        if (season <= MAX_OLDEST_YEAR_DATA) return

        when (leagueName.toLowerCase()) {
            "mlb" -> subscriptions.add(
                    Observable.zip(
                            getRotoTeamStanding(statsTeamId = statsTeamId, leagueName = leagueName, season = season),
                            getRotoCarouselMLB(statsTeamId = statsTeamId, leagueName = leagueName),
                            BiFunction<RotoStandings?, RotoMLBCarousel?, RotoResponseOverview<RotoStandings, RotoMLBCarousel>>
                            {rotoStandings, rotoMLBCarousel ->
                                return@BiFunction RotoResponseOverview(rotoStandings, rotoMLBCarousel)
                            }
                    ).subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .doOnError {
                                DataMenuDataManager.getRotoOverview(statsTeamId = statsTeamId,
                                        leagueName = leagueName, season = season - 1)
                            }
                            .subscribe { response ->
                                response?.let {
                                    subjectRotoOverview.onNext(it)
                                } ?: DataMenuDataManager.getRotoOverview(statsTeamId = statsTeamId,
                                        leagueName = leagueName, season = season - 1)
                            }
            )
            "nba" -> subscriptions.add(
                    Observable.zip(
                            getRotoTeamStanding(statsTeamId = statsTeamId, leagueName = leagueName, season = season),
                            getRotoCarouselNBA(statsTeamId = statsTeamId, leagueName = leagueName),
                            BiFunction<RotoStandings?, RotoNBACarousel?, RotoResponseOverview<RotoStandings, RotoNBACarousel>>
                            {rotoStandings, rotoNBACarousel ->
                                return@BiFunction RotoResponseOverview(rotoStandings, rotoNBACarousel)
                            }
                    ).subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .doOnError {
                                DataMenuDataManager.getRotoOverview(statsTeamId = statsTeamId,
                                        leagueName = leagueName, season = season - 1)
                            }
                            .subscribe { response ->
                                response?.let {
                                    subjectRotoOverview.onNext(it)
                                } ?: DataMenuDataManager.getRotoOverview(statsTeamId = statsTeamId,
                                        leagueName = leagueName, season = season - 1)
                            }
            )
            "nfl" -> subscriptions.add(
                    Observable.zip(
                            getRotoTeamStanding(statsTeamId = statsTeamId, leagueName = leagueName, season = season),
                            getRotoCarouselNFL(statsTeamId = statsTeamId, leagueName = leagueName),
                            BiFunction<RotoStandings?, RotoNFLCarousel?, RotoResponseOverview<RotoStandings, RotoNFLCarousel>>
                            {rotoStandings, rotoNFLCarousel ->
                                return@BiFunction RotoResponseOverview(rotoStandings, rotoNFLCarousel)
                            }
                    ).subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .doOnError {
                                DataMenuDataManager.getRotoOverview(statsTeamId = statsTeamId,
                                        leagueName = leagueName, season = season - 1)
                            }
                            .subscribe { response ->
                                response?.let {
                                    subjectRotoOverview.onNext(it)
                                } ?: DataMenuDataManager.getRotoOverview(statsTeamId = statsTeamId,
                                        leagueName = leagueName, season = season - 1)
                            }
            )
            "nhl" -> subscriptions.add(
                    Observable.zip(
                            getRotoTeamStanding(statsTeamId = statsTeamId, leagueName = leagueName, season = season),
                            getRotoCarouselNHL(statsTeamId = statsTeamId, leagueName = leagueName),
                            BiFunction<RotoStandings?, RotoNHLCarousel?, RotoResponseOverview<RotoStandings, RotoNHLCarousel>>
                            {rotoStandings, rotoNHLCarousel ->
                                return@BiFunction RotoResponseOverview(rotoStandings, rotoNHLCarousel)
                            }
                    ).subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .doOnError {
                                DataMenuDataManager.getRotoOverview(statsTeamId = statsTeamId,
                                        leagueName = leagueName, season = season - 1)
                            }
                            .subscribe { response ->
                                response?.let {
                                    subjectRotoOverview.onNext(it)
                                } ?: DataMenuDataManager.getRotoOverview(statsTeamId = statsTeamId,
                                        leagueName = leagueName, season = season - 1)
                            }
            )
        }
    }

    @Deprecated("Replace with getRotoTeamStanding(...)")
    private fun getTeamStanding(statsTeamId: Int): Observable<StatsTeam> {
        val team = TeamManager.getInstance()?.getTeamByStatsId(statsTeamId)
        val (sportName, leagueName) = DataBarUtil.getRequestParams(team)
        return Observable.defer<Response<DataBarModel>> {
            dataStatsComApi?.getStanding(sportName, leagueName, statsTeamId)
        }.subscribeOn(Schedulers.io())
                .map { it.body()?.apiResults?.get(0)?.league?.season?.eventType?.get(0)?.conferences?.get(0)?.divisions?.get(0)?.teams?.get(0)!! }
                .observeOn(AndroidSchedulers.mainThread())
    }

    private fun getRotoTeamStanding(
            statsTeamId: Int, leagueName: String, season: Int
    ): Observable<RotoStandings?> {
        return Observable.defer<Response<RotoResponse<RotoStandings>>> {
            dataMenuRotoApi?.getRotoStandings(league = leagueName.toUpperCase(), season = season)
        }.subscribeOn(Schedulers.io())
                .map { it.body()?.standings?.firstOrNull { s1 -> s1.teamGlobalId == statsTeamId } }
    }

    @Deprecated("")
    private fun getTeamStatsIfNeeded(statsTeamId: Int): Observable<out StatsEventSplit<out Parcelable>> {
        val leagueName = TeamManager.getInstance()?.getTeamByStatsId(statsTeamId)?.league?.toLowerCase()
        return dataStatsComApi?.let {
            when (leagueName) {
                "nfl" -> fetchTeamStats(statsTeamId, it::getNFLTeamStats)
                "nhl" -> fetchTeamStats(statsTeamId, it::getNHLTeamStats)
                "nba" -> fetchTeamStats(statsTeamId, it::getNBATeamStats)
                else -> null
            }
        } ?: fetchTeamStats(statsTeamId, null)
        // this if-null value has to exist because we can't pass null into Observable.zip
    }

    @Deprecated("")
    private fun <T : Parcelable> fetchTeamStats(
            statsTeamId: Int,
            apiCall: ((Int) -> Observable<Response<StatsResponse<T>>>)?
    ): Observable<StatsEventSplit<T>> {
        return Observable.defer<Response<StatsResponse<T>>> {
            apiCall!!(statsTeamId)
        }.subscribeOn(Schedulers.io())
                .map { it.body()?.apiResults?.get(0)?.league?.teams?.get(0)?.seasons?.get(0)?.eventType?.get(0)?.splits?.get(0)!! }
    }

    /**
     * This method is used to get MLB carousel data from Roto
     */
    private fun getRotoCarouselMLB(
            statsTeamId: Int, leagueName: String
    ): Observable<RotoMLBCarousel?> {
        return Observable.defer<Response<RotoResponse<RotoMLBCarousel>>> {
            dataMenuRotoApi?.getRotoCarouselMLB(league = leagueName.toUpperCase(),
                    teamId = statsTeamId)
        }.subscribeOn(Schedulers.io())
                .map { it.body()?.carousel?.firstOrNull() }
    }

    /**
     * This method is used to get NBA carousel data from Roto
     */
    private fun getRotoCarouselNBA(
            statsTeamId: Int, leagueName: String
    ): Observable<RotoNBACarousel?> {
        return Observable.defer<Response<RotoResponse<RotoNBACarousel>>> {
            dataMenuRotoApi?.getRotoCarouselNBA(league = leagueName.toUpperCase(),
                    teamId = statsTeamId)
        }.subscribeOn(Schedulers.io())
                .map { it.body()?.carousel?.firstOrNull() }
    }

    /**
     * This method is used to get NFL carousel data from Roto
     */
    private fun getRotoCarouselNFL(
            statsTeamId: Int, leagueName: String
    ): Observable<RotoNFLCarousel?> {
        return Observable.defer<Response<RotoResponse<RotoNFLCarousel>>> {
            dataMenuRotoApi?.getRotoCarouselNFL(league = leagueName.toUpperCase(),
                    teamId = statsTeamId)
        }.subscribeOn(Schedulers.io())
                .map { it.body()?.carousel?.firstOrNull() }
    }

    /**
     * This method is used to get NHL carousel data from Roto
     */
    private fun getRotoCarouselNHL(
            statsTeamId: Int, leagueName: String
    ): Observable<RotoNHLCarousel?> {
        return Observable.defer<Response<RotoResponse<RotoNHLCarousel>>> {
            dataMenuRotoApi?.getRotoCarouselNHL(league = leagueName.toUpperCase(),
                    teamId = statsTeamId)
        }.subscribeOn(Schedulers.io())
                .map { it.body()?.carousel?.firstOrNull() }
    }
    // endregion

    // region Season Standing
    private fun getLeagueStandings(statsTeamId: Int) {
        val team = TeamManager.getInstance()?.getTeamByStatsId(statsTeamId)
        val (sportName, leagueName) = DataBarUtil.getRequestParams(team)
        val season = DataBarUtil.getCurrentSeasonForDataMenu(team)

        DataMenuDataManager.getLeagueStandings(statsTeamId, sportName, leagueName, season)
    }

    private fun getLeagueStandings(statsTeamId: Int, sportName: String, leagueName: String, season: Int) {
        if (season <= MAX_OLDEST_YEAR_DATA) return  // in case season keeps decrementing

        subscriptions.add(
                Observable.defer<Response<RotoResponse<RotoStandings>>> {
                    dataMenuRotoApi?.getRotoStandings(league = leagueName.toUpperCase(), season = season)
                }.subscribeOn(Schedulers.io())
                        .map {
                            it.body()
                        }
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { response ->
                            if (response?.standings.isNullOrEmpty()) {
                                DataMenuDataManager.getLeagueStandings(statsTeamId, sportName, leagueName, season - 1)
                                return@subscribe
                            }

                            DataMenuDataUtils.updateStandingsTableData(data = response?.standings, sportName = sportName)?.let {
                                subjectSeasonLeagueStandings.onNext(it)
                            }
                        }
        )
    }
    // endregion

    // region Box Score
    private fun getEventBoxScore(team: Team?, scheduleList: Array<RotoSchedule>): Observable<out BoxEvent<out Parcelable>>? {
        val leagueName = team?.league?.toUpperCase() ?: ""
        val teamId = team?.statsTeamID ?: -1

        return dataMenuRotoApi?.let {
            when (leagueName) {
                "MLB" -> fetchEventRoto(teamId, leagueName, scheduleList, it::getRotoScoresMLB)
                "NBA" -> fetchEventRoto(teamId, leagueName, scheduleList, it::getRotoScoresNBA)
                "NFL" -> fetchEventRoto(teamId, leagueName, scheduleList, it::getRotoScoresNFL)
                "NHL" -> fetchEventRoto(teamId, leagueName, scheduleList, it::getRotoScoresNHL)
                else -> null
            }
        }
    }

    private fun <T : Parcelable> fetchEventRoto(
            teamId: Int,
            leagueName: String,
            scheduleList: Array<RotoSchedule>,
            apiCall: (String, Int) -> Observable<Response<RotoResponseScores<T>>>
    ): Observable<BoxEvent<out Parcelable>>? {
        return if (leagueName.isNotEmpty()) {
            Observable.defer<Response<RotoResponseScores<T>>> {
                // eventIds are same gameId since they are from Stats.com
                var gameId = liveEventIds.getOrDefault(teamId, -1)
                if (gameId == -1L) {
                    gameId = findLatestEventId(scheduleList)
                }

                if (gameId < 0) {
                    throw Throwable("Box Score not available for latest event")
                }
                apiCall(leagueName, gameId.toInt())
            }.subscribeOn(Schedulers.io())
                    .map { resp ->
                        val body = resp.body() ?: return@map null
                        when (leagueName) {
                            "MLB" -> body.convertToBoxEvent(teamId, BoxScoreMLB())
                            "NBA" -> body.convertToBoxEvent(teamId, BoxScoreNBA())
                            "NFL" -> body.convertToBoxEvent(teamId, BoxScoreNFL())
                            "NHL" -> body.convertToBoxEvent(teamId, BoxScoreNHL())
                            else -> return@map null
                        }
                    }
        } else {
            null
        }
    }

    fun findLatestEventId(scheduleList: Array<RotoSchedule>): Long {
        if (scheduleList.isEmpty()) return -1

        val today = Calendar.getInstance()
        // first, if today is on the day of the last game, return the last game directly
        val lastGameDate = scheduleList[scheduleList.lastIndex].gameDateTime.asFormattedCalendarDate()
                ?: return -1
        if (today.after(lastGameDate)) {
            // box score should not be shown now
            return -1
        } else if (!today.before(lastGameDate)) {
            return scheduleList[scheduleList.lastIndex].gameGlobalId.toLong()
        }

        // then, if today is on the day of the first game, return the first game directly
        val firstGameDate = scheduleList[0].gameDateTime.asFormattedCalendarDate()
                ?: return -1
        if (today.before(firstGameDate)) {
            // box score should not be shown now
            return -1
        } else if (!today.after(firstGameDate)) {
            return scheduleList[0].gameGlobalId.toLong()
        }

        // then let's perform a binary search to locate the closest complete/in-progress game to today
        var targetIndex = -1
        var hasError = false
        var lp = 0
        var rp = scheduleList.lastIndex
        while (targetIndex < 0 || hasError) {
            val candidateIndex = (lp + rp) / 2
            val candidateDate = scheduleList[candidateIndex].gameDateTime.asFormattedCalendarDate()
            when {
                candidateIndex <= lp -> targetIndex = lp // lp points at the closest complete game
                candidateDate == null -> hasError = true // which exits the while loop
                candidateDate.before(today) -> lp = candidateIndex // move the left pointer
                candidateDate.after(today) -> rp = candidateIndex // move the right pointer
                else -> targetIndex = candidateIndex // today is the day! exit
            }
        }

        return scheduleList[targetIndex].gameGlobalId.toLong()
    }

    // endregion
}