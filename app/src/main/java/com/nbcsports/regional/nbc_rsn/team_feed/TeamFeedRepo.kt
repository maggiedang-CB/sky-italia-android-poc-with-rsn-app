package com.nbcsports.regional.nbc_rsn.team_feed

import com.clearbridge.pull_to_refresh.RefreshLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nbcsports.regional.nbc_rsn.common.*
import com.nbcsports.regional.nbc_rsn.debug_options.DebugPresenter
import com.nbcsports.regional.nbc_rsn.extensions.d
import com.nbcsports.regional.nbc_rsn.team_feed.auto_refresh.AutoRefreshManager
import com.nbcsports.regional.nbc_rsn.urban_airship.NotificationsManagerKt
import com.nbcsports.regional.nbc_rsn.utils.DateFormatUtils
import com.nbcsports.regional.nbc_rsn.utils.DisplayUtils
import com.nbcsports.regional.nbc_rsn.utils.FtueUtil
import com.nbcsports.regional.nbc_rsn.utils.PreferenceUtils
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.Single
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Function3
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import timber.log.Timber
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit

class TeamFeedRepo(
        private val listener: TeamFeedContract.RepoListener,
        private val config: Config,
        private val teamContentUrl: String,
        private val teamId: String
) {

    private val subscriptions = CompositeDisposable()
    private val gson = Gson()
    private var latestTeamViewFeed: TeamViewFeed? = null

    private var liveAssetsAutoRefreshDO: DisposableObserver<DateTime>? = null
    private var teamFeedAutoRefreshDO: DisposableObserver<DateTime>? = null

    fun unsubscribe(teamName: String) {
        // Remove live assets auto refresh disposable observer
        // and team feed auto refresh disposable observer
        AutoRefreshManager.removeDisposable(liveAssetsAutoRefreshDO, teamName)
        AutoRefreshManager.removeDisposable(teamFeedAutoRefreshDO, teamName)

        subscriptions.clear()
    }

    // region Fetch methods
    fun getTeamFeedData(isBackAndFore: Boolean) {
        // team.contentUrl
        Observable.zip(
                getTeamFromServer(teamContentUrl),
                getLiveAssets(),
                BiFunction<TeamViewFeed, TeamViewFeed, Unit> { teadViewFeed, assetsPlaceHolder ->
                    teadViewFeed._247Assets = assetsPlaceHolder._247Assets
                    teadViewFeed.liveAssets = assetsPlaceHolder.liveAssets
                    latestTeamViewFeed = teadViewFeed
                }
        ).flatMap{ getNTPTimeTVF() }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<DateTime> {
                    override fun onSubscribe(d: Disposable) {
                        subscriptions.add(d)
                    }

                    override fun onNext(currentTime: DateTime) {
                        listener.onTeamFeedSucceed(latestTeamViewFeed, currentTime, isBackAndFore, false, false)
                    }

                    override fun onError(e: Throwable) {
                        listener.onTeamFeedFailed(e)
                    }

                    override fun onComplete() {}
                })
    }

    /**
     * Get team feed for normal manual refresh
     * Including app Background -> Foreground
     *
     * Also save the new loaded team content into share preference
     *
     * Also save the pre load time stamp into share preference
     *
     * @param url
     * @return
     */
    private fun getTeamFromServer(url: String): Observable<TeamViewFeed> {

        return Observable.create<TeamViewFeed> { emitter ->
            Timber.d("This is the enter point: Start updating latestTeamViewFeed (teamFeed)")
            Timber.d("team feed url: %s", url)

            val client = OkHttpClient()

            // When url is blank, IllegalStateException will be thrown and emitter.onError will
            // be triggered
            val request = Request.Builder()
                    .url(url)
                    .get()
                    .build()

            val response = client.newCall(request).execute()

            val responseBodyString = response.body()!!.string()

            // Update team content on share preference
            val teamFullIdString = teamId + Constants.PRE_LOAD_TEAM_CONTENT_SUFFIX
            PreferenceUtils.setString(teamFullIdString,
                    responseBodyString)

            // Parse response from server to TeamViewFeed object
            val teamViewFeed = parseResponse(response, responseBodyString)

            // Save time stamp into share preference
            val teamPreLoadTimeStamp = teamId + Constants.PRE_LOAD_TEAM_TIME_STAMP_SUFFIX
            val deviceTimeString = DateFormatUtils.getCurrentDateTimeWithTimeZone(DateTimeZone.UTC)
                    .toString(DateFormatUtils.NTP_DATE_PATTERN)
            PreferenceUtils.setString(teamPreLoadTimeStamp, deviceTimeString)

            emitter.onNext(teamViewFeed)
            emitter.onComplete()
        }.subscribeOn(Schedulers.io())
    }

    @Throws(IOException::class)
    private fun parseResponse(response: Response, responseBodyString: String): TeamViewFeed {
        if (!response.isSuccessful) {
            throw IOException()
        }
        val teamViewFeed = gson.fromJson(responseBodyString, TeamViewFeed::class.java)
        if (teamViewFeed?.teamView?.components == null) {
            throw IOException()
        }
        return teamViewFeed
    }

    /**
     * Fetch live assets.
     *
     * Also save the new loaded live assets into share preference
     *
     * Also save the pre load time stamp into share preference
     *
     * @return Observable<TeamViewFeed>
     */
    private fun getLiveAssets(): Observable<TeamViewFeed> {
        return Observable.create<TeamViewFeed> { emitter ->
            val client = OkHttpClient()

            val request = Request.Builder()
                    .url(config.liveAssetsUrl)
                    .get()
                    .build()

            val response = client.newCall(request).execute()

            val responseBodyString = response.body()!!.string()

            // Update live assets on share preference
            PreferenceUtils.setString(Constants.PRE_LOAD_LIVE_ASSETS_KEY, responseBodyString)

            // Parse response from server to liveAssets list object
            val listType = object : TypeToken<List<Asset>>() {}.type
            val liveAssets = gson.fromJson<List<Asset>>(responseBodyString, listType)

            // TODO: begin to filter for free vs. non free when authentication is complete.
            //liveAssets = filterAssets(liveAssets); // filter the live assets based on authentication

            // 247 assets will not have homeTeam/awayTeam populated, so removeAssetsNotInUsersTeams() will remove
            // them from liveAssets making them not showing up in the team view. To fix it, make a copy of liveAssets
            // and set it to 247Assets, this list will get used in the 247 filtering process, TeamFeedPresenter.find247Asset()
            val _247Assets = removeAssets247WithSameChannelOrRequestorId(ArrayList<Asset>(liveAssets))

            // this is no longer requied since another filter is implemented:
            // TeamFeedPresenter.findLiveAsset()
            // leaving it here for now to test this further
            // liveAssets = removeAssetsNotInUsersTeams(liveAssets);
            LiveAssetManager.getInstance().liveAssets = liveAssets

            // Save time stamp into share preference
            val teamPreLoadTimeStamp = teamId + Constants.PRE_LOAD_TEAM_TIME_STAMP_SUFFIX
            val deviceTimeString = DateFormatUtils.getCurrentDateTimeWithTimeZone(DateTimeZone.UTC)
                    .toString(DateFormatUtils.NTP_DATE_PATTERN)
            PreferenceUtils.setString(teamPreLoadTimeStamp, deviceTimeString)
            emitter.onNext(TeamViewFeed(null, liveAssets, _247Assets))
        }.onErrorReturn {
            // Keep assets un-change if there is thread interrupted
            TeamViewFeed(
                    null,
                    latestTeamViewFeed?.liveAssets?.let { ArrayList<Asset>(it) } ?: ArrayList(),
                    latestTeamViewFeed?._247Assets?.let { ArrayList<Asset>(it) } ?: ArrayList()
            )
        }.subscribeOn(Schedulers.io())
    }

    /**
     * This method is used to get NTP time from time.google.com
     * and convert the time to joda DateTime
     * and save it into TeamFeedFragment instance
     *
     * If NTP request fail, the device time will be used after
     * converting into UTC time
     *
     * UTC time zone is the same as GMT
     * So we can convert the local time in either UTC or GMT
     * But server is using UTC, so we better use UTC
     *
     * Also save the new loaded NTP time into share preference
     * Or device time if loading fail
     *
     * @param teamFeedFragment
     * @return be ignored
     */
    fun getNTPTimeTVF(): Observable<DateTime> {
        return Observable.create<DateTime> { emitter ->
            Timber.d("This is the enter point: start getting NTP time...")

            val client = OkHttpClient()
            val request = Request.Builder()
                    .url("https://time.google.com")
                    .get()
                    .build()
            val response = client.newCall(request).execute()
            Timber.d("This is the enter point: NTP response (%s)", response.header(Constants.NTP_DATE_KEY))

            // Update NTP time on share preference
            PreferenceUtils.setString(Constants.PRE_LOAD_NTP_TIME_KAY, response.header(Constants.NTP_DATE_KEY)!!)

            // Parse NTP time from time.google.com to DateTime object
            val currentDateTimeUTC = DateFormatUtils.parseDateTimeFromNTPResponse(response.header(Constants.NTP_DATE_KEY))
            Timber.d("This is the enter point: NTP parse response (%s)", currentDateTimeUTC)
            Timber.d("This is the enter point: Success in getting NTP time")

            emitter.onNext(currentDateTimeUTC)
        }.onErrorReturn { e ->
            Timber.d("This is the enter point: fail in getting NTP time (%s)", e.message)

            // Update NTP time (device time) on share preference
            val deviceTimeString = DateFormatUtils.getCurrentDateTimeWithTimeZone(DateTimeZone.UTC)
                    .toString(DateFormatUtils.NTP_DATE_PATTERN)
            PreferenceUtils.setString(Constants.PRE_LOAD_NTP_TIME_KAY,
                    deviceTimeString)

            // Parse device time to DateTime object
            val currentDateTimeUTC = DateFormatUtils.getCurrentDateTimeWithTimeZone(DateTimeZone.UTC)
            Timber.d("This is the enter point: use device's time instead (%s)", currentDateTimeUTC)
            currentDateTimeUTC
        }.subscribeOn(Schedulers.io())
    }

    /**
     * Remove assets from assets247 if the channel or requestorId of those assets
     * are the same as any team that have live asset in json.
     *
     * Also check asset's start time, if start time is passed,
     * then remove the asset from assets247
     *
     * @param assets247
     * @return filtered assets247
     */
    private fun removeAssets247WithSameChannelOrRequestorId(assets247: List<Asset>?): List<Asset> {
        if (assets247 == null) return ArrayList()

        // Get all master teams' home team priorities
        // And put them into an array list
        // For each priority, add the related requestor id and team id combination
        // to an array list
        val masterTeamHomeTeamPrioritiesArrayList = ArrayList<String>()
        val masterTeamRequestorIdWithTeamIdArrayList = ArrayList<String>()
        for ((teamId, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, requestorId, homeTeamPriorityList) in TeamManager.getInstance()!!.getMasterList()) {
            masterTeamHomeTeamPrioritiesArrayList.addAll(homeTeamPriorityList)
            val requestorIdWithTeamIdArray = arrayOfNulls<String>(homeTeamPriorityList.size)
            Arrays.fill(requestorIdWithTeamIdArray, "$requestorId---$teamId")
            masterTeamRequestorIdWithTeamIdArrayList.addAll(Arrays.asList<String>(*requestorIdWithTeamIdArray))
        }

        // Lowercase all master teams' home team priorities
        for (i in masterTeamHomeTeamPrioritiesArrayList.indices) {
            val priorityString = masterTeamHomeTeamPrioritiesArrayList[i].toLowerCase()
            masterTeamHomeTeamPrioritiesArrayList[i] = priorityString
        }

        // For every asset from assets247, if home team or away team is contained in
        // master teams' home team priorities array list, then
        // 1. Check whether team's requestor id and asset's channel
        //    (or asset's requestor id) are the same.
        // 2. Check whether asset's start time is already passed
        val channelOrRequestorIdWithTeamIdArrayList = ArrayList<String>()
        for (itemAsset in assets247) {
            // Get masterTeamRequestorIdWithTeamId string position
            var priorityIndex = masterTeamHomeTeamPrioritiesArrayList.indexOf(itemAsset.homeTeam.toLowerCase())
            if (priorityIndex == -1) {
                priorityIndex = masterTeamHomeTeamPrioritiesArrayList.indexOf(itemAsset.awayTeam.toLowerCase())
            }
            // If position is found
            if (priorityIndex != -1) {
                // Get channel or requestor id from asset base on availability
                var channelOrRequestorId = ""
                if (itemAsset.channel != null && !itemAsset.channel.isEmpty()) {
                    channelOrRequestorId = itemAsset.channel
                } else if (itemAsset.requestorId != null && !itemAsset.requestorId.isEmpty()) {
                    channelOrRequestorId = itemAsset.requestorId
                }

                // Get device's current UTC time
                var currentDateTimeUTCMillis = DateFormatUtils.getCurrentDateTimeWithTimeZone(DateTimeZone.UTC).millis
                // Compare device's current UTC time with saved Internet UTC time
                // And use the latest one
                val ntpTimePreLoad = PreferenceUtils.getString(Constants.PRE_LOAD_NTP_TIME_KAY, "")
                if (!ntpTimePreLoad.isEmpty()){
                    val ntpDateTimeUTCMillis = DateFormatUtils.parseDateTimeFromNTPResponse(ntpTimePreLoad).millis
                    if (ntpDateTimeUTCMillis > currentDateTimeUTCMillis){
                        currentDateTimeUTCMillis = ntpDateTimeUTCMillis
                    }
                }

                // LiveAssetStartBuffer in config is second
                // So it needs to be converted to millisecond
                val timeBefore = config.liveAssetStartBuffer * 1000L

                // Check:
                // 1. Asset's channel or requestor id is found, and asset's start time is passed with LiveAssetStartBuffer
                // 2. Asset's channel or requestor id is found, and asset's start time is not passed with LiveAssetStartBuffer
                if (!channelOrRequestorId.isEmpty()
                        && (itemAsset.getStartDateTime().millis - currentDateTimeUTCMillis) <= timeBefore) {
                    // Get masterTeamRequestorIdWithTeamId string
                    val masterTeamRequestorIdWithTeamIdString = masterTeamRequestorIdWithTeamIdArrayList[priorityIndex]
                    // Convert masterTeamRequestorIdWithTeamId string into masterTeamRequestorId string
                    val masterTeamRequestorIdString = masterTeamRequestorIdWithTeamIdString
                            .substring(0, masterTeamRequestorIdWithTeamIdString.indexOf("---"))
                    // Check:
                    // 1. Whether Asset's channel or requestor id is matched
                    // 2. Whether masterTeamRequestorIdWithTeamId string is already added
                    if (masterTeamRequestorIdString.equals(channelOrRequestorId, ignoreCase = true)
                            && !channelOrRequestorIdWithTeamIdArrayList.contains(masterTeamRequestorIdWithTeamIdString)) {
                        // Add masterTeamRequestorIdWithTeamId
                        channelOrRequestorIdWithTeamIdArrayList.add(masterTeamRequestorIdWithTeamIdString)
                    }
                } else if (!channelOrRequestorId.isEmpty()){
                    // Get masterTeamRequestorIdWithTeamId string
                    val masterTeamRequestorIdWithTeamIdString = masterTeamRequestorIdWithTeamIdArrayList[priorityIndex]
                    // Convert masterTeamRequestorIdWithTeamId string into masterTeamRequestorId string
                    val masterTeamRequestorIdString = masterTeamRequestorIdWithTeamIdString
                            .substring(0, masterTeamRequestorIdWithTeamIdString.indexOf("---"))
                    // Check:
                    // 1. Whether Asset's channel or requestor id is matched
                    if (masterTeamRequestorIdString.equals(channelOrRequestorId, ignoreCase = true)) {
                        // Remove masterTeamRequestorIdWithTeamId
                        channelOrRequestorIdWithTeamIdArrayList.remove(masterTeamRequestorIdWithTeamIdString)
                    }
                }
            }
        }

        // Remove convert channelOrRequestorIdWithTeamId string to channelOrRequestorId string
        val channelOrRequestorIdArrayList = ArrayList<String>()
        for (channelOrRequestorIdWithTeamIdString in channelOrRequestorIdWithTeamIdArrayList){
            val channelOrRequestorIdString = channelOrRequestorIdWithTeamIdString
                    .substring(0, channelOrRequestorIdWithTeamIdString.indexOf("---"))
            channelOrRequestorIdArrayList.add(channelOrRequestorIdString)
        }

        // Lowercase all channel or requestor id
        for (i in channelOrRequestorIdArrayList.indices) {
            val channelOrRequestorIdString = channelOrRequestorIdArrayList[i].toLowerCase()
            channelOrRequestorIdArrayList[i] = channelOrRequestorIdString
        }

        // Check every asset of assets247, if asset's channel or requestor id is not contained in
        // channel or requestor id array list, then add the asset into validAssets247
        val validAssets247 = ArrayList<Asset>()
        for (itemAsset in assets247) {
            var channelOrRequestorId = ""
            if (itemAsset.channel != null && !itemAsset.channel.isEmpty()) {
                channelOrRequestorId = itemAsset.channel
            } else if (itemAsset.requestorId != null && !itemAsset.requestorId.isEmpty()) {
                channelOrRequestorId = itemAsset.requestorId
            }
            if (!channelOrRequestorIdArrayList.contains(channelOrRequestorId.toLowerCase())) {
                validAssets247.add(itemAsset)
            }
        }
        return validAssets247
    }
    // endregion

    // region Auto-update methods
    /**
     * Set up live assets auto refresh disposable observer
     * and team feed auto refresh disposable observer
     * Add them into AutoRefreshManager for management
     *
     * For testing, change config.getLiveDataRefreshInterval() to second > 0 (e.g. 20)
     * change config.getTeamViewRefreshInterval() to second > 0 (e.g. 40)
     */
    fun setUpLiveAssetsAndTeamFeedAutoRefreshDO(teamName: String) {
        // team.displayName,
        // config.liveDataRefreshInterval, config.teamViewRefreshInterval

        // Set up live assets auto refresh disposable observer
        // null or disposed
        if (liveAssetsAutoRefreshDO?.isDisposed != false) {
            if (config.liveDataRefreshInterval != -1L) {
                liveAssetsAutoRefreshDO = getLiveAssetsDisposableObserver(config.liveDataRefreshInterval)
                Timber.i("This is the enter point: start adding live asset disposable: %s", teamName)
                AutoRefreshManager.addDisposable(liveAssetsAutoRefreshDO, teamName)
            }
        }

        // Set up team feed auto refresh disposable observer
        // null or disposed
        if (teamFeedAutoRefreshDO?.isDisposed != false) {
            if (config.teamViewRefreshInterval != -1L) {
                teamFeedAutoRefreshDO = getTeamFeedDisposableObserver(config.teamViewRefreshInterval)
                Timber.i("This is the enter point: start adding team feed disposable: %s", teamName)
                AutoRefreshManager.addDisposable(teamFeedAutoRefreshDO, teamName)
            }
        }
    }

    /**
     * This method is used to generate a new disposable observer
     * for live assets auto refresh
     *
     * @param interval
     * @param teamFeedFragment
     * @return DisposableObserver<DateTime>
     */
    private fun getLiveAssetsDisposableObserver(interval: Long): DisposableObserver<DateTime> {
        return Observable.interval(interval, TimeUnit.SECONDS)
                .flatMap { getLiveAssets() }
                .doOnNext {liveAssetPlaceHolder ->
                    latestTeamViewFeed?._247Assets = liveAssetPlaceHolder._247Assets
                    latestTeamViewFeed?.liveAssets = liveAssetPlaceHolder.liveAssets
                }
                .flatMap { getNTPTimeTVF() }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableObserver<DateTime>() {
                    override fun onNext(currentTime: DateTime) {
                        // Update team medium view holder and/or others
                        Timber.i("This is the enter point: latestTeamViewFeed update success (liveAssets)")
                        listener.onTeamFeedSucceed(latestTeamViewFeed, currentTime, false, true, false)
                    }

                    override fun onError(e: Throwable) {
                        Timber.i("This is the enter point: latestTeamViewFeed update fail (liveAssets)")
                    }

                    override fun onComplete() {}
                })
    }

    /**
     * This method is used to generate a new disposable observer
     * for team feed auto refresh
     *
     * @param interval
     * @param teamFeedFragment
     * @return DisposableObserver<DateTime>
     */
    private fun getTeamFeedDisposableObserver(interval: Long): DisposableObserver<DateTime> {
        return Observable.interval(interval, TimeUnit.SECONDS)
                .flatMap{getTeamFromServer(teamContentUrl)}
                .doOnNext { latestTeamViewFeed?.teamView = it.teamView }
                .flatMap { getNTPTimeTVF() }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableObserver<DateTime>() {
                    override fun onNext(currentTime: DateTime) {
                        // Update team medium view holder and/or others
                        Timber.i("This is the enter point: latestTeamViewFeed update success (teamFeed)")
                        listener.onTeamFeedSucceed(latestTeamViewFeed, currentTime, false, false, true)
                    }

                    override fun onError(e: Throwable) {
                        Timber.i("This is the enter point: latestTeamViewFeed update fail (teamFeed)")
                    }

                    override fun onComplete() {}
                })
    }
    // endregion

    // region Preload methods
    /**
     * Get pre loaded team feed content from share preference
     * Get pre loaded live assets from share preference
     * Get pre loaded NTP time (or device time) from share preference
     */
    fun getTeamFeedDataFromPreLoad() {
        // team.teamId
        Single.zip(
                getTeamFromPreLoad(),
                getLiveAssetsFromPreLoad(),
                getNTPTimeForPreLoad(),
                Function3<TeamViewFeed, TeamViewFeed, DateTime, DateTime> { preloadFeed, preloadAssetsPlaceHolder, preloadTime ->
                    preloadFeed._247Assets = preloadAssetsPlaceHolder._247Assets
                    preloadFeed.liveAssets = preloadAssetsPlaceHolder.liveAssets
                    latestTeamViewFeed = preloadFeed
                    preloadTime
                }
        ).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : SingleObserver<DateTime> {
                    override fun onSubscribe(d: Disposable) {
                        subscriptions.add(d)
                    }
                    override fun onSuccess(preloadTime: DateTime) {
                        listener.onTeamFeedSucceed(latestTeamViewFeed, preloadTime, false, false, false)
                        listener.onPreloadSucceed()
                    }

                    override fun onError(e: Throwable) {
                        listener.onPreloadFailed(e)
                    }
                })
    }

    /**
     * Get pre loaded team feed content from share preference
     * And parse it to TeamViewFeed object
     *
     * @param teamId
     * @return Observable<TeamViewFeed>
     */
    private fun getTeamFromPreLoad(): Single<TeamViewFeed> {
        return Single.create<TeamViewFeed> { emitter ->
            val teamContentPreLoad = PreferenceUtils.getString(
                    teamId + Constants.PRE_LOAD_TEAM_CONTENT_SUFFIX, "")

            if (teamContentPreLoad.isEmpty()) {
                throw IOException()
            }
            val teamViewFeed = gson.fromJson(teamContentPreLoad, TeamViewFeed::class.java) ?: throw IOException()

            emitter.onSuccess(teamViewFeed)
        }.subscribeOn(Schedulers.io())
    }

    /**
     * Get pre loaded live assets from share preference
     * And put it into TeamViewFeed
     *
     * @param teamFeedFragment
     * @return Function<TeamViewFeed></TeamViewFeed>, ObservableSource<TeamViewFeed>>
     */
    private fun getLiveAssetsFromPreLoad(): Single<TeamViewFeed> {
        return Single.create<TeamViewFeed> {

            val liveAssetsPreLoad = PreferenceUtils.getString(
                    Constants.PRE_LOAD_LIVE_ASSETS_KEY, "[]")

            val listType = object : TypeToken<List<Asset>>() {}.type
            val gson = Gson()
            val liveAssets = gson.fromJson<List<Asset>>(liveAssetsPreLoad, listType)

            // TODO: begin to filter for free vs. non free when authentication is complete.
            //liveAssets = filterAssets(liveAssets); // filter the live assets based on authentication

            // 247 assets will not have homeTeam/awayTeam populated, so removeAssetsNotInUsersTeams() will remove
            // them from liveAssets making them not showing up in the team view. To fix it, make a copy of liveAssets
            // and set it to 247Assets, this list will get used in the 247 filtering process, TeamFeedPresenter.find247Asset()
            val _247Assets = removeAssets247WithSameChannelOrRequestorId(ArrayList(liveAssets))

            // this is no longer requied since another filter is implemented:
            // TeamFeedPresenter.findLiveAsset()
            // leaving it here for now to test this further
            // liveAssets = removeAssetsNotInUsersTeams(liveAssets);
            LiveAssetManager.getInstance().liveAssets = liveAssets

            it.onSuccess(TeamViewFeed(null, liveAssets, _247Assets))
        }.subscribeOn(Schedulers.io())
    }

    /**
     * This method is used to get pre loaded NTP time from share preference
     * and convert the time to joda DateTime
     * and save it into TeamFeedFragment instance
     *
     * If pre loaded NTP time does not exist, the device time will be used after
     * converting into UTC time
     *
     * UTC time zone is the same as GMT
     * So we can convert the local time in either UTC or GMT
     * But server is using UTC, so we better use UTC
     *
     * @param teamFeedFragment
     * @return be ignored
     */
    private fun getNTPTimeForPreLoad(): Single<DateTime> {
        return Single.create<DateTime> {
            val ntpTimePreLoad = PreferenceUtils.getString(Constants.PRE_LOAD_NTP_TIME_KAY, "")
            Timber.d("This is the enter point: NTP response (%s)", ntpTimePreLoad)
            val currentDateTimeUTC = DateFormatUtils.parseDateTimeFromNTPResponse(ntpTimePreLoad)
            Timber.d("This is the enter point: NTP parse response (%s)", currentDateTimeUTC)
            Timber.d("This is the enter point: Success in getting NTP time")
            it.onSuccess(currentDateTimeUTC)
        }.onErrorReturn { e ->
            Timber.d("This is the enter point: fail in getting NTP time (%s)", e.message)
            val currentDateTimeUTC = DateFormatUtils.getCurrentDateTimeWithTimeZone(DateTimeZone.UTC)
            Timber.d("This is the enter point: use device's time instead (%s)", currentDateTimeUTC)
            currentDateTimeUTC
        }.subscribeOn(Schedulers.io())
    }
    // endregion

    /**
     * This method is used to keep tracking of whether to show fab tap ftue
     *
     * 1. Stop mirroring the source observable when
     *    a) Refresh layout is not null
     *    b) Refresh layout is visible
     *    c) User doesn't tap fab to close article yet
     *
     * 2. Start mirroring the source observable when
     *    a) User has opened at least five articles
     *    b) There is no banner showing (any)
     *    c) Fab tap banner is showing
     */
    fun getFabTapFtueDisposableObserver(interval: Long, refreshLayout: RefreshLayout?): DisposableObserver<Long> {
        return Observable.interval(interval, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .takeWhile { aLong ->
                    refreshLayout != null
                            && DisplayUtils.isVisible(refreshLayout)
                            && FtueUtil.getFabTapToClose() == 0
                }
                .skipWhile { aLong ->
                    FtueUtil.hasOpenedArticleAtMostFourTimes()
                            || (NotificationsManagerKt.isBannerShowing()
                            && !NotificationsManagerKt.isFtueFabTapBannerShowing())
                }
                .subscribeWith(object : DisposableObserver<Long>() {
                    override fun onNext(t: Long) {
                        // Different scenarios:
                        // 1. a) User doesn't tap fab to close article yet
                        //    b) There are no banner showing (any)
                        //    c) User doesn't see the fab tap banner message yet
                        //    d) At least one article is showing on the current team feed
                        //    This scenario means the fab tap ftue banner need to be shown
                        //    and the fab animation need to be shown
                        //
                        // 2. a) User doesn't tap fab to close article yet
                        //    b) Fab tap banner is not showing
                        //    c) User has seen the fab tap banner message
                        //    d) At least one article is showing on the current team feed
                        //    This scenario means user dismiss the fab tap banner
                        //
                        // 3. a) User has tapped fab to close article
                        //    b) Fab tap banner is showing
                        //    c) User has seen the fab tap banner message
                        //    d) At least one article is showing on the current team feed
                        //    This scenario means user taps fab to close article
                        //    and this scenario is handled by takeWhile{...} and onComplete()
                        //
                        // 4. a) User doesn't tap fab to close article yet
                        //    b) Fab tap banner is showing
                        //    c) User has seen the fab tap banner message
                        //    d) There is no article showing on the current team feed
                        //    This scenario means user closes all articles in the team feed
                        //    without tapping the fab
                        //
                        // 5. a) User doesn't tap fab to close article yet
                        //    b) Fab tap banner is showing
                        //    c) User has seen the fab tap banner message
                        //    d) At least one article is showing on the current team feed
                        //    This scenario means the fab tap ftue is in progress
                        //    Do nothing in this scenario
                        if (FtueUtil.getFabTapToClose() == 0
                                && !NotificationsManagerKt.isBannerShowing()
                                && !FtueUtil.hasViewedFabTapMsg()
                                && listener.hasAtLeastOneStackableViewOpened()) {
                            d("This is the enter point: fab tap ftue scenario 1")
                            // Show fab tap ftue notification banner
                            // Set fab tap message viewed to true
                            listener.onShowFabTapMessage()
                            // Clear team logo on fab
                            // Show fab tap ftue animation
                            listener.onStartFabTapAnimation()
                        } else if (FtueUtil.getFabTapToClose() == 0
                                && !NotificationsManagerKt.isFtueFabTapBannerShowing()
                                && FtueUtil.hasViewedFabTapMsg()
                                && listener.hasAtLeastOneStackableViewOpened()) {
                            d("This is the enter point: fab tap ftue scenario 2")
                            // Record fab tapped to close article
                            // Because fab tap ftue banner is dismissed
                            FtueUtil.recordFabTapToClose()
                            // Clear fab animations
                            // Reset team logo on fab
                            listener.onStopFabTapAnimation()
                        } else if (FtueUtil.getFabTapToClose() == 0
                                && NotificationsManagerKt.isFtueFabTapBannerShowing()
                                && FtueUtil.hasViewedFabTapMsg()
                                && !listener.hasAtLeastOneStackableViewOpened()) {
                            d("This is the enter point: fab tap ftue scenario 4")
                            // Hide fab tap ftue banner
                            // Reset fab tap message viewed to false
                            listener.onHideFabTapMessage()
                            // Clear fab animations
                            // Reset team logo on fab
                            listener.onStopFabTapAnimation()
                        }
                    }

                    override fun onError(e: Throwable) {
                        d("This is the enter point: fab tap ftue on error")
                        if (NotificationsManagerKt.isFtueFabTapBannerShowing()
                                && FtueUtil.hasViewedFabTapMsg()){
                            // Hide fab tap ftue banner
                            // Reset fab tap message viewed to false
                            listener.onHideFabTapMessage()
                            // Clear fab animations
                            // Reset team logo on fab
                            listener.onStopFabTapAnimation()
                        }
                    }

                    override fun onComplete() {
                        d("This is the enter point: fab tap ftue on complete")
                        if (NotificationsManagerKt.isFtueFabTapBannerShowing()
                                && FtueUtil.hasViewedFabTapMsg()){
                            // Hide fab tap ftue banner
                            // Reset fab tap message viewed to false
                            listener.onHideFabTapMessage()
                            // Clear fab animations
                            // Reset team logo on fab
                            listener.onStopFabTapAnimation()
                        }
                    }
                })
    }

    /**
     * This method is used to keep track of whether to show or hide data menu ftue bottom banner
     */
    fun getDataMenuFtueDisposableObserver(interval: Long, refreshLayout: RefreshLayout?): DisposableObserver<Long> {
        return Observable.interval(interval, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .takeWhile { aLong ->
                    refreshLayout != null
                            && DisplayUtils.isVisible(refreshLayout)
                            && !FtueUtil.hasDoneDataMenuFtue()
                }
                .skipWhile { aLong ->
                    (NotificationsManagerKt.isBottomBannerReadyToShowOrShowing()
                            && !NotificationsManagerKt.isFtueDataMenuBottomBannerShowing())
                            || NotificationsManagerKt.isBannerShowing()
                }
                .subscribeWith(object : DisposableObserver<Long>() {
                    override fun onNext(t: Long) {
                        // Different scenarios:
                        // 1. a) User hasn't done the data menu ftue yet
                        //    b) There is no bottom banner showing (any)
                        //    c) Data menu msg (bottom banner) is not viewed
                        //    d) Current fragment is not stepped story
                        //    e) Data menu never be opened
                        //    f) There is no top notification banner showing
                        //    g) Data bar is enabled on config and enabled on debug menu
                        //    h) Data menu is enabled on config or enabled on debug menu
                        //    This scenario is meaningful when sub scenarios are applied
                        //
                        // 2. a) User hasn't done the data menu ftue yet
                        //    b) Data menu ftue bottom banner is not showing
                        //    c) User has seen the data menu ftue bottom banner msg
                        //    This scenario means user dismisses the data menu ftue bottom banner
                        //    while data menu ftue is in process. Then finish data menu ftue
                        //
                        // 3. a) User hasn't done the data menu ftue yet
                        //    b) Data menu ftue bottom banner is showing
                        //    c) User has seen the data menu ftue bottom banner msg
                        //    d) User opens data menu
                        //    This scenario means user opens the data menu while data menu ftue
                        //    is in process. Then finish data menu ftue.
                        //
                        // 4. a) User hasn't done the data menu ftue yet
                        //    b) Data menu ftue bottom banner is showing
                        //    c) This disposable observer is resubscribed
                        //       (e.g. swipes to another team, or put the app into background and then
                        //       foreground)
                        //    This scenario means user swipe to another team or move the app from
                        //    background to foreground while data menu ftue is in process. Then
                        //    finish data menu ftue This scenario is handled
                        //    by TeamFeedPresenter.startDataMenuFtueDisposableObserver(...)
                        //
                        // 5. a) User hasn't done the data menu ftue yet
                        //    b) Data menu ftue bottom banner is showing
                        //    c) User click or long click on any article on team feed or upcoming
                        //       section on editorial details page
                        //    This scenario means user open any article on team feed or open share
                        //    menu by long clicking the article or click upcoming section on
                        //    editorial details page while data menu ftue is in process. Then finish
                        //    data menu ftue. This scenario is handled by
                        //    EditorialDetailComponentsAdapter.mHolderViewCommonOnClickListener(...)
                        //    EditorialDetailComponentsAdapter.setupSharing()
                        //    ViewHolderClickListenerFactory.ViewHolderClickListener()
                        //    ViewHolderClickListenerFactory.ViewHolderShareListener()
                        //    ViewHolderClickListenerFactory.ViewHolderExternalLinkListener()
                        //    MainActivity.tapFab()
                        //    EditorialDetailPresenter.share()
                        //
                        // 6. a) User hasn't done the data menu ftue yet
                        //    b) Data menu ftue bottom banner is showing
                        //    c) User scroll on team feed or editorial details page
                        //    This scenario means user scroll on team feed or editorial details page
                        //    while data menu ftue is in process. Then finish data menu ftue. This
                        //    scenario is handled by DataBarScrollListener.onScrolled()
                        if (!FtueUtil.hasDoneDataMenuFtue()
                                && !NotificationsManagerKt.isBottomBannerReadyToShowOrShowing()
                                && !FtueUtil.hasViewedDataMenuMsg()
                                && !listener.isCurrentSteppedFragment()
                                && !FtueUtil.hasOpenedDataMenu()
                                && !NotificationsManagerKt.isBannerShowing()
                                && (config.dataBar?.enabled == true && PreferenceUtils.getBoolean(Constants.PREF_KEY_DATABAR_ENABLED, true))
                                && (config.dataMenu?.isEnabled == true || PreferenceUtils.getBoolean(DebugPresenter.DATA_MENU_ENABLE, false))){
                            // Different sub scenarios: (Base on scenario 1)
                            // 1.1 a) Both fab flick ftue and fab tap ftue has been done
                            //     This scenario means data menu bottom banner need to be shown
                            //
                            // 1.2 a) Fab flick ftue is done and fab tap ftue is not done yet
                            //     b) No stackable view is opened or total articles opened are
                            //        least than 5 times (counts from the install time
                            //        to current time)
                            //     This scenario means data menu bottom banner need to be shown
                            //
                            // 1.3 a) Fab tap ftue has been done and fab flick ftue is not done yet
                            //     b) User has used fab menu at most once
                            //     This scenario means data menu bottom banner need to be shown
                            if (!FtueUtil.fabHasNeverBeenFlicked()
                                    && FtueUtil.getFabTapToClose() != 0){
                                d("This is the enter point: data menu ftue scenario 1.1")
                                // Set up current time as data menu init show up time
                                FtueUtil.setDataMenuMsgShowUpInitTime()
                                // Show data menu ftue notification banner
                                // Set data menu message viewed to true
                                listener.onShowDataMenuMessage()
                            } else if (!FtueUtil.fabHasNeverBeenFlicked()
                                    && (!listener.hasAtLeastOneStackableViewOpened() || FtueUtil.hasOpenedArticleAtMostFourTimes())){
                                d("This is the enter point: data menu ftue scenario 1.2")
                                // Set up current time as data menu init show up time
                                FtueUtil.setDataMenuMsgShowUpInitTime()
                                // Show data menu ftue notification banner
                                // Set data menu message viewed to true
                                listener.onShowDataMenuMessage()
                            } else if (FtueUtil.getFabTapToClose() != 0
                                    && FtueUtil.hasUsedFabAtMostOnce()){
                                d("This is the enter point: data menu ftue scenario 1.3")
                                // Set up current time as data menu init show up time
                                FtueUtil.setDataMenuMsgShowUpInitTime()
                                // Show data menu ftue notification banner
                                // Set data menu message viewed to true
                                listener.onShowDataMenuMessage()
                            }
                        } else if (!FtueUtil.hasDoneDataMenuFtue()
                                && !NotificationsManagerKt.isFtueDataMenuBottomBannerShowing()
                                && FtueUtil.hasViewedDataMenuMsg()){
                            d("This is the enter point: data menu ftue scenario 2")
                            // Set data menu ftue done to true
                            FtueUtil.setHasDoneDataMenuFtue(true)
                            // Reset data menu message viewed to false
                            FtueUtil.setDataMenuMsgViewed(false)
                        } else if (!FtueUtil.hasDoneDataMenuFtue()
                                && NotificationsManagerKt.isFtueDataMenuBottomBannerShowing()
                                && FtueUtil.hasViewedDataMenuMsg()
                                && FtueUtil.hasOpenedDataMenu()){
                            d("This is the enter point: data menu ftue scenario 3")
                            // Set data menu ftue done to true
                            FtueUtil.setHasDoneDataMenuFtue(true)
                            // Hide data menu ftue bottom banner
                            // Reset data menu message viewed to false
                            listener.onHideDataMenuMessage()
                        }
                    }

                    override fun onError(e: Throwable) {
                        d("This is the enter point: data menu ftue on error")
                        if (NotificationsManagerKt.isFtueDataMenuBottomBannerShowing()){
                            // Hide data menu ftue bottom banner
                            // Reset data menu message viewed to false
                            listener.onHideDataMenuMessage()
                        }
                    }

                    override fun onComplete() {
                        d("This is the enter point: data menu ftue on complete")
                        if (NotificationsManagerKt.isFtueDataMenuBottomBannerShowing()){
                            // Hide data menu ftue bottom banner
                            // Reset data menu message viewed to false
                            listener.onHideDataMenuMessage()
                        }
                    }
                })
    }
}