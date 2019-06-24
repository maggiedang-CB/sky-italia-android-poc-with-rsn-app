package com.nbcsports.regional.nbc_rsn.team_feed

import android.graphics.Color
import com.clearbridge.pull_to_refresh.RefreshLayout
import com.nbcsports.regional.nbc_rsn.MainActivity
import com.nbcsports.regional.nbc_rsn.analytics.TrackingHelper.PageInfo
import com.nbcsports.regional.nbc_rsn.authentication.MvpdLogoListener
import com.nbcsports.regional.nbc_rsn.common.*
import com.nbcsports.regional.nbc_rsn.data_menu.datamenuftue.DataMenuFtueManager
import com.nbcsports.regional.nbc_rsn.deeplink.Deeplink
import com.nbcsports.regional.nbc_rsn.deeplink.DeeplinkManager
import com.nbcsports.regional.nbc_rsn.editorial_detail.EditorialDetailInterface
import com.nbcsports.regional.nbc_rsn.fabigation.fabtapftue.FabTapFtueManager
import com.nbcsports.regional.nbc_rsn.stepped_story.SteppedStoryFragment
import com.nbcsports.regional.nbc_rsn.team_feed.components.TeamViewComponentsAdapter
import com.nbcsports.regional.nbc_rsn.urban_airship.NotificationsManagerKt
import com.nbcsports.regional.nbc_rsn.utils.*
import org.apache.commons.lang3.StringUtils
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import timber.log.Timber

class TeamFeedPresenter(
        private val view: TeamFeedContract.View,
        private val team: Team,
        private val config: Config
) : TeamFeedContract.RepoListener {

    private var mvpdLogoListener: MvpdLogoListener? = null
    private val repo = TeamFeedRepo(this, config, team.contentUrl, team.teamId)

    // region todo: try to remove
    fun getPrimaryColour(): Int {
        return Color.parseColor(team.primaryColor)
    }

    fun getPageInfo(): PageInfo {
        return PageInfo(contextData = false, league = team.league)
    }

    /**
     * WARNING: DO NOT USE unless you really have no other way.
     * This method only exists because [DeeplinkManager] needs to use it and it can't be
     * refactored easily.
     * It's painful...
     */
    fun getTeam(): Team {
        return team
    }
    // endregion

    // region Organize data and show
    /**
     * This method is used to update the team feed list items
     *
     * @param currentTime current data and time in UTC
     * @param isBackAndFore is the app went to background and then go to foreground again
     * @param isLiveAssetsAutoRefresh is live assets auto refresh triggered
     * @param isTeamFeedAutoRefresh is team feed auto refresh triggered
     */
    private fun showTeamviewListItems(latestTeamViewFeed: TeamViewFeed?, currentTime: DateTime,
            isBackAndFore: Boolean, isLiveAssetsAutoRefresh: Boolean, isTeamFeedAutoRefresh: Boolean) {

        // Instead of doing items = teamViewFeed.getTeamView().getComponents() directly
        // It is because on method showTeamviewListItems, it will
        // modify the teamViewFeed.getTeamView().getComponents()
        // which means there will be 2 Medium feed components,
        // if showTeamviewListItems method is called again
        val items = latestTeamViewFeed?.teamView?.components?.let { ArrayList(it) } ?: return

        // Add Header for Team View template
        val header = FeedComponent(FeedComponent.Type.HEADER)
        if (FeedComponent.Type.COMPONENT == header.type) {
            // There was a bug: the header item's type is FeedComponent.Type.COMPONENT, while it must be FeedComponent.Type.HEADER
            throw IllegalStateException("Wrong header item's type: FeedComponent.Type.COMPONENT, expected: FeedComponent.Type.HEADER")
        }
        items.add(0, header)



        val liveAsset = latestTeamViewFeed.let { findLiveAsset(it.liveAssets, team, currentTime, config) }
        val _247Asset = latestTeamViewFeed.let { find247Asset(it._247Assets, team) }


        // If there is at least one feed card in Team View, add TheFeed label before it.
        var theFeedLabelIndex = -1
        val theFeedLabel = FeedComponent(FeedComponent.Type.THEFEED_LABEL)

        for (i in items.indices) {
            val card = items[i]
            val cardType = card.cardType
            val itemViewType = TeamViewComponentsAdapter.getItemViewType(card.type, cardType, card.contentType) // type is expected to be Type.COMPONENT

            // todo: there was a null check on card.cardType with the following comment, double check that
            // card.cardType is not set and stays null for FeedComponent.Type.HEADER
            if (FeedComponent.Type.THEFEED_LABEL == card.type) {
                break // The Feed label has been already added.
            }

            if (itemViewType >= TeamViewComponentsAdapter.VIEW_TYPE_Feed_START) {
                items.add(i, theFeedLabel)
                theFeedLabelIndex = i + if(liveAsset == null) 0 else 1
                break
            }
        }


        // Check if live asset is existed, if there is no live asset, then reset MVPD logo
        // Otherwise, keep MVPD logo as its current state
        if (liveAsset != null) {
            // Detect live assets and add to team
            addLiveAsset(liveAsset, items, team)
            mvpdLogoListener?.onLiveAssetAdded(liveAsset)
        } else {
            mvpdLogoListener?.resetMvpdLogo()
        }


        if (_247Asset != null) {
            // Detect live assets and add feed promo (24/7) to team view
            add247Asset(_247Asset, items, team)
        }

        // Add footer
        items.add(FeedComponent(FeedComponent.Type.FOOTER))

        // Ensure all the items have current team
        for (item in items) {
            item.set(team)
        }

        // todo: the old code used to skip colour gradient assignment if refreshLayout is null
        view.setTeamColorGradient(ColorUtil.makeGradientDrawable(team.primaryColor))

        Timber.d("showTeamviewListItems: team: %s, selectedTeam: %s",
                team.displayName, TeamManager.getInstance()?.getSelectedTeam()?.displayName)

        when {
            view.updatedAdapter(items, theFeedLabelIndex, team) -> return
            isBackAndFore -> view.updateAdapterForBackAndFore(items, theFeedLabelIndex, team)
            isLiveAssetsAutoRefresh -> view.updateAdapterForLiveAssetsAutoRefresh(items, theFeedLabelIndex, team)
            isTeamFeedAutoRefresh -> view.updateAdapterForTeamFeedAutoRefresh(items, theFeedLabelIndex, team)
            else -> // Show the team's list of items.
                view.setupRecyclerView(items, theFeedLabelIndex, team)
        }
    }

    private fun addLiveAsset(liveAsset: Asset?, items: MutableList<FeedComponent>, team: Team) {
        if (liveAsset != null) {
            val mediumPlayer = FeedComponent(FeedComponent.Type.PERSISTENT_PLAYER_MEDIUM)
            mediumPlayer.mediaSource = MediaSource(liveAsset.pid,
                    liveAsset.id,
                    liveAsset.title,
                    liveAsset.androidStreamUrl,
                    liveAsset.image,
                    liveAsset.channel,
                    true,
                    Deeplink(team),
                    liveAsset.channel,
                    liveAsset
            )
            items.add(0, mediumPlayer)
        }
    }

    private fun add247Asset(liveAsset: Asset?, items: MutableList<FeedComponent>, team: Team) {

        val feedPromo = FeedComponent(FeedComponent.Type.FEED_PROMO)
        var feedPromoAddedAlready = false

        if (liveAsset != null) {
            feedPromo.mediaSource = MediaSource(liveAsset.pid,
                    liveAsset.id,
                    liveAsset.title,
                    liveAsset.androidStreamUrl,
                    liveAsset.image,
                    liveAsset.channel,
                    true,
                    Deeplink(team),
                    liveAsset.channel,
                    liveAsset
            )
        }

        // Put the feed promo into #11 position of standard feeds or the last position if least than 11 standard feeds
        var standardFeedCound = 0
        for (i in items.indices) {
            val item = items[i]
            val cardType = item.cardType
            val contentType = item.contentType
            val type = item.type
            val itemViewType = TeamViewComponentsAdapter.getItemViewType(type, cardType, contentType)
            // Check if feed promo already existed
            if (type === FeedComponent.Type.FEED_PROMO) {
                feedPromoAddedAlready = true
                break
            }
            // If standard feed found
            if (itemViewType >= TeamViewComponentsAdapter.VIEW_TYPE_Feed_START
                    && itemViewType != TeamViewComponentsAdapter.VIEW_TYPE_PERSISTENT_PLAYER_MEDIUM
                    && itemViewType != TeamViewComponentsAdapter.VIEW_TYPE_external_promo
                    && itemViewType != TeamViewComponentsAdapter.VIEW_TYPE_external_promo_text_only) {
                standardFeedCound += 1
            }
            // If already at #11 standard position
            if (standardFeedCound == 11) {
                feedPromoAddedAlready = true
                items.add(i, feedPromo)
                break
            }
        }
        if (!feedPromoAddedAlready) {
            items.add(feedPromo)
        }
    }

    private fun find247Asset(liveAssets: List<Asset>, team: Team): Asset? {

        var liveAsset: Asset? = null
        for (asset in liveAssets) {
            val requestorIsChannel = StringUtils.equalsIgnoreCase(team.requestorId, asset.channel)
            val requestorIsRequestor = StringUtils.equalsIgnoreCase(team.requestorId, asset.requestorId)
            val isGMO = StringUtils.equalsIgnoreCase(asset.source, "gmo")
            if ((requestorIsChannel || requestorIsRequestor) && isGMO) {
                liveAsset = asset
                break
            }
        }
        return liveAsset
    }

    private fun findLiveAsset(liveAssets: List<Asset>?, team: Team, currentDateTimeUTC: DateTime, config: Config): Asset? {
        var liveAsset: Asset? = null
        if (liveAssets != null && !liveAssets.isEmpty()) {

            // Because it should uses the last available live asset in the list
            // So there is no break in this for loop
            for (asset in liveAssets) {
                if (matchRequestorID(team, asset)
                        && (matchTeamOnPriority(team, asset.homeTeam) || matchTeamOnPriority(team, asset.awayTeam))
                        && isStartTimeOk(asset, currentDateTimeUTC, config)) {
                    liveAsset = asset
                }
            }

        }
        return liveAsset
    }

    // region findLiveAsset Helper
    /**
     * This method is used to check whether live asset's requestor id or channel
     * is matched with team's requestor id
     *
     * This check is also applied on 247Assets filtering
     * (i.e. removeAssets247WithSameChannelOrRequestorId(...))
     *
     * @param team
     * @param asset
     * @return true if one of them matched
     * false if none matched
     */
    private fun matchRequestorID(team: Team, asset: Asset): Boolean {
        return StringUtils.equalsIgnoreCase(team.requestorId, asset.requestorId) || StringUtils.equalsIgnoreCase(team.requestorId, asset.channel)
    }

    /**
     * This method is used to check whether live asset's home team or away team
     * is matched with one of the team's home team priorities
     *
     * This check is also applied on 247Assets filtering
     * (i.e. removeAssets247WithSameChannelOrRequestorId(...))
     *
     * @param team
     * @param teamName
     * @return true if one of them matched
     * false if none matched
     */
    private fun matchTeamOnPriority(team: Team, teamName: String): Boolean {
        for (name in team.homeTeamPriorityList) {
            if (StringUtils.equalsIgnoreCase(name, teamName)) {
                return true
            }
        }
        return false
    }

    /**
     * This method is used to compare current time in UTC time zone
     * with asset start time in UTC time zone
     *
     * UTC time zone is the same as GMT
     * So we can convert the local time in either UTC or GMT
     * But server is using UTC, so we better use UTC
     *
     * This check is ignored on 247Assets filtering
     *
     * @param asset
     * @param currentDateTimeUTC
     * @return true if current time is valid to show live asset
     * false otherwise
     */
    private fun isStartTimeOk(asset: Asset?, currentDateTimeUTC: DateTime?, config: Config?): Boolean {
        if (asset == null || asset.start.isEmpty()) {
            return false
        }

        if (currentDateTimeUTC == null) {
            return false
        }

        val assetStartDateTime = asset.getStartDateTime()
        val assetStartDateTimeMillis = assetStartDateTime.millis
        val currentDateTimeUTCMillis = currentDateTimeUTC.millis
        // Get time before live game start from config
        // If config is null, then used the hard coded one
        var timeBefore = DateFormatUtils.timeBeforeLiveStreamStart
        if (config != null) {
            // LiveAssetStartBuffer in config is second
            // So it needs to be converted to millisecond
            timeBefore = config.liveAssetStartBuffer * 1000L
        }

        return assetStartDateTimeMillis - currentDateTimeUTCMillis <= timeBefore
    }
    // endregion
    // endregion

    // region Method for TeamFeedFragment
    fun tryRefresh() {
        val selectedTeam = TeamManager.getInstance()?.getSelectedTeam() ?: return
        team.let { currentTeam ->
            if (currentTeam == selectedTeam) {
                if (isCurrentSteppedFragment()){
                    view.setStatusBarColor(Color.BLACK)
                } else {
                    val teamColor = Color.parseColor(currentTeam.primaryColor)
                    view.setStatusBarColor(teamColor)
                }

                if (!view.resumeAdapter()) {
                    repo.getTeamFeedData(true)
                }
                // This is the additional implementation
                // The code below compare the team pre load time stamp
                // with the current device time
                // If deviceCurrentTimeMillis - timeStampDateTimeMillis
                // >= liveDataRefreshInterval
                // Then reload data from server
                val teamPreLoadTimeStamp = team.teamId + Constants.PRE_LOAD_TEAM_TIME_STAMP_SUFFIX
                val timeStampDateTimeString = PreferenceUtils.getString(teamPreLoadTimeStamp, "")
                if (!timeStampDateTimeString.isEmpty()) {
                    val timeStampDateTime = DateFormatUtils.parseDateTimeFromNTPResponse(timeStampDateTimeString)
                    val deviceCurrentTime = DateFormatUtils.getCurrentDateTimeWithTimeZone(DateTimeZone.UTC)
                    val timeStampDateTimeMillis = timeStampDateTime.millis
                    val deviceCurrentTimeMillis = deviceCurrentTime.millis
                    if (deviceCurrentTimeMillis - timeStampDateTimeMillis >= config.liveDataRefreshInterval * 1000L) {
                        repo.getTeamFeedData(true)
                    }
                }
            }

            // Set up application lifecycle listener,
            // live assets auto refresh disposable observer
            // and team feed auto refresh disposable observer
            view.setUpLifeCycleListenerAndAutoRefreshDO()
        }
    }

    fun refresh(isBackAndFore: Boolean) {
        repo.getTeamFeedData(isBackAndFore)
    }

    fun recordPageScrollStateChanged(state: Int) {
        Timber.d(
                "onPageScrollStateChanged: state=%s, team: %s, selectedTeam: %s",
                when (state) {
                    0 -> "SCROLL_STATE_IDLE=0"
                    1 -> "CROLL_STATE_DRAGGING=1"
                    2 -> "SCROLL_STATE_SETTLING=2\""
                    else -> "unknown"
                },
                team.displayName,
                TeamManager.getInstance()?.getSelectedTeam()?.displayName
        );
    }

    fun setMvpdLogoListener(listener: MvpdLogoListener) {
        mvpdLogoListener = listener
    }

    /**
     * This function evaluates user's fab flick usage status,
     *
     * Returns true if the following conditions are met:
     * 1. This fragment's team is equal to the selected team
     * 2. User has used the fab menu (not flick/fling) more than once
     * 3. User has never used the fab flick/fling functionality
     * 4. There is no fab tap ftue banner showing
     *
     * Returns false otherwise
     *
     * @return : boolean
     */
    fun isUserRequiredToSeeFtueFlick (): Boolean {
        return isTeamDisplayNameTheSameAsSelected()
                && !FtueUtil.hasUsedFabAtMostOnce() // do not show anim on first use of fab menu
                && FtueUtil.fabHasNeverBeenFlicked()
                && !NotificationsManagerKt.isFtueFabTapBannerShowing()
    }

    /**
     * This function evaluates user's fab tap to close article usage status,
     *
     * Returns true if the following conditions are met:
     * 1. This fragment's team is equal to the selected team
     * 2. User doesn't tap fab to close article yet
     *
     * Returns false otherwise
     *
     * @return : boolean
     */
    fun isUserRequiredToSeeFtueTap(): Boolean {
        return isTeamDisplayNameTheSameAsSelected()
                && FtueUtil.getFabTapToClose() == 0
    }

    /**
     * This function evaluates whether user has viewed data menu bottom notification
     *
     * Returns true if the following conditions are met:
     * 1. This fragment's team is equal to the selected team
     * 2. User doesn't see the data menu bottom notification yet
     * 3. User has never open the data menu
     *
     * Returns false otherwise
     *
     * @return : boolean
     */
    fun isUserRequiredToSeeFtueDataMenu(): Boolean {
        return isTeamDisplayNameTheSameAsSelected()
                && !FtueUtil.hasDoneDataMenuFtue()
                && !FtueUtil.hasOpenedDataMenu()
    }

    fun isMoreThanOneTeamInUsersTeams(): Boolean {
        return TeamManager.getInstance()?.usersTeams?.size?.let { it > 1 } == true
    }

    fun isTeamDisplayNameTheSameAsSelected(): Boolean {
        val selectedTeam = TeamManager.getInstance()?.getSelectedTeam() ?: return false
        return selectedTeam.displayName.equals(team.displayName, ignoreCase = true)
    }

    /**
     * Load team data with appropriate method
     *
     * Methods:
     * 1. Load data from pre load json
     * 2. Load data from server
     */
    fun loadTeamDataWithAppropriateMethod() {
        // Check if pre loaded team content exists
        // If so, use pre loaded team content from share preference
        // Otherwise, load it from server
        val teamFeedDataPreLoad = PreferenceUtils.getString(
                team.teamId + Constants.PRE_LOAD_TEAM_CONTENT_SUFFIX, "")
        val liveAssetsPreLoad = PreferenceUtils.getString(
                Constants.PRE_LOAD_LIVE_ASSETS_KEY, "")
        if (teamFeedDataPreLoad.isNotEmpty() && liveAssetsPreLoad.isNotEmpty()) {
            repo.getTeamFeedDataFromPreLoad()
        } else {
            repo.getTeamFeedData(false)
        }
        team.let {
            val selectedTeam = TeamManager.getInstance()?.getSelectedTeam()
            if (it == selectedTeam){
                if (isCurrentSteppedFragment()){
                    view.setStatusBarColor(Color.BLACK)
                } else {
                    val teamColor = Color.parseColor(it.primaryColor)
                    view.setStatusBarColor(teamColor)
                }
            }
        }
    }

    /**
     * Check whether live asset is showing before refresh and after refresh
     *
     * @param previousItems
     * @param currentItems
     * @return true if live asset is showing before refresh and after refresh
     * false otherwise
     */
    fun isMediumViewHolderShowingPreviouslyAndCurrently(
            previousItems: List<FeedComponent>, currentItems: List<FeedComponent>): Boolean {
        var isPreviouslyShowing = false
        for (item in previousItems) {
            if (item.type === FeedComponent.Type.PERSISTENT_PLAYER_MEDIUM) {
                isPreviouslyShowing = true
                break
            }
        }

        var isCurrentlyShowing = false
        for (item in currentItems) {
            if (item.type === FeedComponent.Type.PERSISTENT_PLAYER_MEDIUM) {
                isCurrentlyShowing = true
                break
            }
        }

        return isPreviouslyShowing && isCurrentlyShowing
    }

    /**
     * Check whether live asset is showing before refresh
     * and hide after refresh
     *
     * @param previousItems
     * @param currentItems
     * @return true if live asset is showing before refresh and hide after refresh
     * false otherwise
     */
    fun isMediumViewHolderOnlyShowingPreviously(
            previousItems: List<FeedComponent>, currentItems: List<FeedComponent>): Boolean {
        var isPreviouslyShowing = false
        for (item in previousItems) {
            if (item.type === FeedComponent.Type.PERSISTENT_PLAYER_MEDIUM) {
                isPreviouslyShowing = true
                break
            }
        }

        var isCurrentlyShowing = false
        for (item in currentItems) {
            if (item.type === FeedComponent.Type.PERSISTENT_PLAYER_MEDIUM) {
                isCurrentlyShowing = true
                break
            }
        }

        return isPreviouslyShowing && !isCurrentlyShowing
    }

    /**
     * Check whether live asset is hided before refresh
     * and show after refresh
     *
     * @param previousItems
     * @param currentItems
     * @return true if live asset is hided before refresh and show after refresh
     * false otherwise
     */
    fun isMediumViewHolderOnlyShowingCurrently(
            previousItems: List<FeedComponent>, currentItems: List<FeedComponent>): Boolean {
        var isPreviouslyShowing = false
        for (item in previousItems) {
            if (item.type === FeedComponent.Type.PERSISTENT_PLAYER_MEDIUM) {
                isPreviouslyShowing = true
                break
            }
        }

        var isCurrentlyShowing = false
        for (item in currentItems) {
            if (item.type === FeedComponent.Type.PERSISTENT_PLAYER_MEDIUM) {
                isCurrentlyShowing = true
                break
            }
        }

        return !isPreviouslyShowing && isCurrentlyShowing
    }

    /**
     * Check whether live assets are the same on both before refresh and after refresh
     *
     * @param previousItems
     * @param currentItems
     * @return true if live assets are the same
     * false otherwise
     */
    fun isMediaSourceTheSamePreviouslyAndCurrently(
            previousItems: List<FeedComponent>, currentItems: List<FeedComponent>): Boolean {
        var previousStreamUrl = ""
        for (item in previousItems) {
            if (item.type === FeedComponent.Type.PERSISTENT_PLAYER_MEDIUM) {
                if (item.mediaSource != null) {
                    previousStreamUrl = item.mediaSource!!.streamUrl
                    break
                }
            }
        }

        var currentStreamUrl = ""
        for (item in currentItems) {
            if (item.type === FeedComponent.Type.PERSISTENT_PLAYER_MEDIUM) {
                if (item.mediaSource != null) {
                    currentStreamUrl = item.mediaSource!!.streamUrl
                    break
                }
            }
        }

        return previousStreamUrl.equals(currentStreamUrl, ignoreCase = true)
    }

    fun setUpLiveAssetsAndTeamFeedAutoRefreshDO() {
        repo.setUpLiveAssetsAndTeamFeedAutoRefreshDO(team.displayName)
    }

    fun unsubscribe() {
        repo.unsubscribe(team.displayName)
    }
    // endregion

    // region Repo listener
    override fun onTeamFeedSucceed(latestTeamViewFeed: TeamViewFeed?, currentTime: DateTime, isBackAndFore: Boolean, isLiveAssetsAutoRefresh: Boolean, isTeamFeedAutoRefresh: Boolean) {
        showTeamviewListItems(latestTeamViewFeed, currentTime, isBackAndFore, isLiveAssetsAutoRefresh, isTeamFeedAutoRefresh)
    }

    override fun onTeamFeedFailed(e: Throwable) {
        if (!NotificationsManagerKt.handleException(e, team)) {
            Timber.e(e)
        }

        view.showRefreshError()
    }

    override fun onPreloadSucceed() {
        // Check if this team is equal to the selected team
        // If so update the team content and live assets
        if (team == TeamManager.getInstance()?.getSelectedTeam()) {
            repo.getTeamFeedData(true)
        }
    }

    override fun onPreloadFailed(e: Throwable) {
        repo.getTeamFeedData(false)
    }

    override fun onStartFabTapAnimation() {
        (view.getActivityRef() as? MainActivity)?.let {
            it.switchFabLogo(null)
            it.showFabWithAnimation(100L, true)
        }
    }

    override fun onStopFabTapAnimation() {
        (view.getActivityRef() as? MainActivity)?.let {
            it.clearFabAnimations()
            it.switchFabLogo(team)
        }
    }

    override fun onShowFabTapMessage() {
        NotificationsManagerKt.showFtueFabTapMessage()
        FtueUtil.setFabTapMsgViewed(true)
    }

    override fun onHideFabTapMessage() {
        (view.getActivityRef() as? MainActivity)?.let {
            it.notificationBanner.hideBanner()
            FtueUtil.setFabTapMsgViewed(false)
        }
    }

    override fun onShowDataMenuMessage() {
        NotificationsManagerKt.showFtueDataMenuMessage()
        FtueUtil.setDataMenuMsgViewed(true)
    }

    override fun onHideDataMenuMessage() {
        (view.getActivityRef() as? MainActivity)?.let {
            it.hideBottomNotification()
            FtueUtil.setDataMenuMsgViewed(false)
        }
    }

    override fun hasAtLeastOneStackableViewOpened(): Boolean {
        return (view as? EditorialDetailInterface)
                ?.hasAtLeastOneStackableViewOpened() == true
    }

    override fun isCurrentSteppedFragment(): Boolean {
        view.let {
            val fragment = it.getTopChildFragment()
            return fragment is SteppedStoryFragment
        }
    }
    // endregion

    /**
     * This method is used to
     *
     * 1. Remove all fab tap ftue disposable observers if user has selected more than one team
     * 2. Add a new fab tap ftue disposable observer to FabTapFtueManager
     */
    fun startFabTapFtueDisposableObserver(interval: Long, refreshLayout: RefreshLayout?) {
        if (isMoreThanOneTeamInUsersTeams()){
            FabTapFtueManager.removeAllObservers()
        }
        FabTapFtueManager.addDisposable(repo.getFabTapFtueDisposableObserver(interval, refreshLayout),
                team.displayName)
    }

    /**
     * This method is used to
     *
     * 1. Remove all data menu ftue disposable observers if user has selected more than one team
     * 2. Check if data menu ftue is in process and not done yet and is shown long
     *    enough, if so, marks data menu ftue as done (scenario 4)
     * 3. Add a new data menu ftue disposable observer to DataMenuFtueManager
     */
    fun startDataMenuFtueDisposableObserver(interval: Long, refreshLayout: RefreshLayout?) {
        if (isMoreThanOneTeamInUsersTeams()){
            DataMenuFtueManager.removeAllObservers()
        }
        if (!FtueUtil.hasDoneDataMenuFtue()
                && NotificationsManagerKt.isFtueDataMenuBottomBannerShowing()
                && FtueUtil.isDataMenuMsgShownLongEnough()){
            // Set data menu ftue done to true
            FtueUtil.setHasDoneDataMenuFtue(true)
        }
        DataMenuFtueManager.addDisposable(repo.getDataMenuFtueDisposableObserver(interval, refreshLayout),
                team.displayName)
    }
}