package com.nbcsports.regional.nbc_rsn.data_bar

import com.nbcsports.regional.nbc_rsn.common.Config
import com.nbcsports.regional.nbc_rsn.common.Constants
import com.nbcsports.regional.nbc_rsn.common.Team
import com.nbcsports.regional.nbc_rsn.common.TeamManager
import com.nbcsports.regional.nbc_rsn.data_menu.DataMenuDataManager
import com.nbcsports.regional.nbc_rsn.data_menu.models.RotoDataBar
import com.nbcsports.regional.nbc_rsn.data_menu.models.RotoResponse
import com.nbcsports.regional.nbc_rsn.extensions.d
import com.nbcsports.regional.nbc_rsn.extensions.e
import com.nbcsports.regional.nbc_rsn.utils.PreferenceUtils
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.joda.time.DateTime
import org.joda.time.LocalTime
import org.joda.time.format.DateTimeFormat
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object DataBarManager {
    val REFRESH_RATE_SLOW = 600L
    val REFRESH_RATE_FAST = 15L

    private lateinit var dataBarRotoAPI: DataBarRotoAPI
    private var okHttpClient: OkHttpClient.Builder = OkHttpClient.Builder()
    var affiliates: List<Affiliate>? = null

    init {
        okHttpClient.addInterceptor { chain: Interceptor.Chain? ->
            val request = chain?.request()?.newBuilder()?.addHeader("x-api-key", DataBarConfig.ROTO_API_KEY)?.build()
            chain?.proceed(request!!)
        }
    }

    /**
     * Observers can subscribe to the BehaviourSubject to listen for data. BehaviorSubjects are used
     * because observers receive a copy of the most recently emitted value to start.
     */
    private val subjectMap = mutableMapOf<Int, BehaviorSubject<RotoDataBar?>>()
    private val timestampMap = mutableMapOf<Int, DateTime?>()
    /**
     * Store the refresh interval for each team.
     */
    private val delayMap = mutableMapOf<Int, Long>()
    private var prevNextFlipTime: Int = 0

    private var compositeDisposable: CompositeDisposable = CompositeDisposable()
    private var onceDisposable = CompositeDisposable()

    fun setTeams(list: List<Team>?) {
        if (list == null) return

        subjectMap.clear()
        timestampMap.clear()
        delayMap.clear()
        for (team in list) {
            subjectMap[team.statsTeamID] = BehaviorSubject.create()
        }

        // request new data
        requestData()
    }

    fun setConfigInfo(configInfo: Config.DataBarConfigInfo) {
        isDataBarEnabled = configInfo.enabled
        activeLeagues = configInfo.activeLeagues.map { it.toLowerCase() }
        liveGameDelay = configInfo.liveGameDelay.toLong()

        prevNextFlipTime = configInfo.prevNextFlipTime

        val retrofit = Retrofit.Builder()
                .baseUrl(configInfo.rotoBaseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
                .client(okHttpClient.build())
                .build()

        dataBarRotoAPI = retrofit.create(DataBarRotoAPI::class.java)
    }

    fun getTeamSubject(statsTeamId: Int): BehaviorSubject<RotoDataBar?>? {
        return subjectMap[statsTeamId]
    }

    // default delay to 30 sec
    private var liveGameDelay: Long = 30
    var isDataBarEnabled: Boolean = true
    private var activeLeagues: List<String> = listOf()

    private fun getNextOrPrevGame(): String {
        val currentTime = LocalTime.now()
        val startTime = if (prevNextFlipTime in 0..23) {
            LocalTime(prevNextFlipTime, 0, 0) // switch over at 11 am
        } else {
            LocalTime(0, 0, 0)
        }

        return if (currentTime.isBefore(startTime)) {
            "prev"
        } else {
            "next"
        }
    }

    private fun requestData() {
        requestOnce()

        // clean up existing disposables before continuing
        compositeDisposable.clear()

        for (statsTeamId in subjectMap.keys) {
            val team = TeamManager.getInstance()?.getTeamByStatsId(statsTeamId)
            val (_, leagueName) = DataBarUtil.getRequestParams(team)

            delayMap[statsTeamId] = REFRESH_RATE_FAST
            compositeDisposable.add(

                    /**
                     * We defer an observable to update the Refresh Rate based on the Game State.
                     *
                     */
                    Observable.defer {
                        dataBarRotoAPI.getDataBarInfo(league = leagueName.toUpperCase(), teamId = statsTeamId, nextOrPrev = getNextOrPrevGame())
                    }
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .delay(liveGameDelay, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
                            .repeatWhen { o -> o.flatMap { Observable.timer(getDelayTime(statsTeamId), TimeUnit.SECONDS) } }
                            .retryWhen { errors -> errors.flatMap { Observable.timer(getDelayTime(statsTeamId), TimeUnit.SECONDS) } }
                            .subscribe(
                                    { resp: Response<RotoResponse<RotoDataBar>>? ->
                                        val body = resp?.body()

                                        val databar = body?.databar?.first()

                                        d("reqData() received data for $statsTeamId-${team?.teamId}, databar=$databar")

                                        if (body == null || !resp.isSuccessful || databar == null || databar.isOffseasonMode()) {
                                            requestOnceOffseason(statsTeamId, true)
                                        } else {
                                            // return the refresh rate
                                            if (databar.isSeasonActive()) {
                                                DataMenuDataManager.notifyEventStatus(statsTeamId, databar.gameGlobalId.toLong(), databar.gameStatusId)

                                                // EMIT data
                                                emitData(databar, leagueName, statsTeamId)

                                                // return the updated refresh rate delay
                                                delayMap[statsTeamId] = databar.getRefreshRate()
                                            } else {
                                                delayMap[statsTeamId] = REFRESH_RATE_SLOW
                                            }
                                        }
                                    },
                                    { error ->
                                        e("$statsTeamId-${team?.teamId} reqData() error -> $error")
                                        requestOnceOffseason(statsTeamId, true)
                                    }
                            ))
        }
    }

    /*
    Due to the liveGameDelay value, on app launch the databar will not appear.
    This function will force the databar to appear initially and then proceed with the
    standard refresh interval.
     */
    private fun requestOnce() {
        onceDisposable.clear()

        for (statsTeamId in subjectMap.keys) {
            val team = TeamManager.getInstance()?.getTeamByStatsId(statsTeamId)
            val (_, leagueName) = DataBarUtil.getRequestParams(team)

            onceDisposable.add(
                    Observable.defer {
                        dataBarRotoAPI.getDataBarInfo(league = leagueName.toUpperCase(), teamId = statsTeamId, nextOrPrev = getNextOrPrevGame())
                    }
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .doOnNext { resp: Response<RotoResponse<RotoDataBar>>? ->
                                val body = resp?.body()

                                val databar = body?.databar?.first()

                                d("reqOnce received data for $statsTeamId-${team?.teamId}, databar=$databar")

                                if (body != null && resp.isSuccessful && databar != null && databar.isSeasonActive()) {
                                    DataMenuDataManager.notifyEventStatus(statsTeamId, databar.gameGlobalId.toLong(), databar.gameStatusId)

                                    // EMIT data
                                    emitData(databar, leagueName, statsTeamId)
                                } else {
                                    requestOnceOffseason(statsTeamId)
                                }
                            }
                            .doOnError { error ->
                                e("$statsTeamId-${team?.teamId} reqOnce() error -> $error")
                                requestOnceOffseason(statsTeamId)
                            }
                            .subscribe()
            )
        }
    }

    private fun requestOnceOffseason(statsTeamId: Int, refreshDelayDuration: Boolean = false) {
        val team = TeamManager.getInstance()?.getTeamByStatsId(statsTeamId)
        val (_, leagueName) = DataBarUtil.getRequestParams(team)
        onceDisposable.add(
                Observable.defer {
                    dataBarRotoAPI.getDataBarInfo(league = leagueName.toUpperCase(), teamId = statsTeamId, nextOrPrev = getNextOrPrevGame())
                }
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .retry()
                        .doOnNext { resp: Response<RotoResponse<RotoDataBar>>? ->
                            val body = resp?.body()

                            val databar = body?.databar?.first()
                            d("reqOnceOffseason received data for $statsTeamId-${team?.teamId}, databar=$databar")

                            if (body != null && resp.isSuccessful && databar != null) {
                                // so that presenter knows this needs to be handled as off season
                                emitData(databar, leagueName, statsTeamId)
                            }
                            if (refreshDelayDuration) {
                                delayMap[statsTeamId] = REFRESH_RATE_SLOW
                            }
                        }
                        .doOnError { error ->
                            e("$statsTeamId-${team?.teamId} error -> $error")
                            delayMap[statsTeamId] = REFRESH_RATE_SLOW
                        }
                        .subscribe()
        )
    }

    private fun emitData(body: RotoDataBar, leagueName: String, statsTeamId: Int): RotoDataBar {
        /*
         * The endTimestamp is used to ensure we don't get old data.
         * If for some reason this date isn't available, we'll ignore it and emit anyway
         */
        val respTimestamp = try {
            DateTime.parse(body.lastUpdateUTC, DateTimeFormat.forPattern("yyyy-MM-dd\'T\'HH:mm:ss.SSSSSSSZ"))
        } catch (e: Exception) {
            null
        }

        if (isDataBarEnabled
                && leagueName.toLowerCase() in activeLeagues
                && PreferenceUtils.getBoolean(Constants.PREF_KEY_DATABAR_ENABLED, isDataBarEnabled)
                && (timestampMap[statsTeamId] == null || respTimestamp == null || timestampMap[statsTeamId]!!.isBefore(respTimestamp.toInstant()))) {

            // update timestamp
            respTimestamp.let { timestamp ->
                timestampMap[statsTeamId] = timestamp
            }
            subjectMap[statsTeamId]?.onNext(body)
        }
        return body
    }

    private fun getDelayTime(statsTeamId: Int): Long {
        return delayMap[statsTeamId] ?: REFRESH_RATE_FAST
    }

    private fun RotoDataBar.getRefreshRate(): Long {
        return when (gameStatusId) {
            // Pre-Game
            GameState.PRE_GAME.eid -> 60L
            GameState.POSTPONED.eid -> 180L

            // Live-Game
            GameState.IN_PROGRESS.eid -> REFRESH_RATE_FAST
            GameState.DELAYED.eid -> REFRESH_RATE_FAST

            // Post-Game
            GameState.SUSPENDED.eid -> 300L
            GameState.CANCELLED.eid -> 600L
            GameState.FINAL.eid -> 600L
            else -> REFRESH_RATE_SLOW
        }
    }

    fun subscribe() {
        requestData()
    }

    fun unsubscribe() {
        compositeDisposable.clear()
        onceDisposable.clear()
    }
}
