package com.nbcsports.regional.nbc_rsn.urban_airship

import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.view.View
import com.clearbridge.bottom_notification_banner.BottomNotificationBanner
import com.clearbridge.bottom_notification_banner.BottomNotificationData
import com.clearbridge.notification_banner.NotificationBanner
import com.clearbridge.notification_banner.NotificationData
import com.google.android.exoplayer2.upstream.HttpDataSource
import com.nbcsports.regional.nbc_rsn.BuildConfig
import com.nbcsports.regional.nbc_rsn.EntryActivity
import com.nbcsports.regional.nbc_rsn.MainActivity
import com.nbcsports.regional.nbc_rsn.R
import com.nbcsports.regional.nbc_rsn.authentication.Auth
import com.nbcsports.regional.nbc_rsn.common.Config
import com.nbcsports.regional.nbc_rsn.common.MediaSource
import com.nbcsports.regional.nbc_rsn.common.Team
import com.nbcsports.regional.nbc_rsn.common.TeamManager
import com.nbcsports.regional.nbc_rsn.deeplink.Deeplink
import com.nbcsports.regional.nbc_rsn.localization.LocalizationManager
import com.nbcsports.regional.nbc_rsn.utils.ConnectionUtils
import com.nbcsports.regional.nbc_rsn.utils.FtueUtil
import com.urbanairship.UAirship
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import org.apache.commons.lang3.StringUtils
import java.io.IOException
import java.lang.ref.WeakReference
import java.net.ConnectException
import java.net.UnknownHostException
import java.util.ArrayList
import java.util.HashSet
import java.util.concurrent.TimeUnit

object NotificationsManagerKt: NotificationBanner.BannerOnShowListener {

    private const val BREAKING_TAG: String = "BREAKING-NEWS"
    private const val TEAM_ID_PLACEHOLDER: String = "{TEAM_ID}"
    private const val FTUE_NOTIFICATION_REMOVE_DELAY: Long = 5000
    private const val PUSH_SHOWN_DURATION: Int = 10000
    const val SYSTEM_SHOWN_DURATION: Int = 5000

    private var breakingNewsTag: String? = null
    private var teamNewsTag: String? = null
    private var gameStartTag: String? = null
    private var finalScoreTag: String? = null
    private var compositeDisposable: CompositeDisposable? = CompositeDisposable()

    private var mainActivityReference: WeakReference<MainActivity>? = null

    fun setActivityWeakReference(activity: MainActivity) {
        if (mainActivityReference?.get() == null){
            mainActivityReference = WeakReference<MainActivity>(activity)
            activity.notificationBanner?.let {
                it.onShowListener = this
            }
        }
    }

    fun release() {
        mainActivityReference?.get()?.notificationBanner?.let {
            it.onShowListener = null
        }
        mainActivityReference?.get()?.bottomNotificationBanner?.let {
            it.onShowListener = null
        }
        compositeDisposable?.dispose()
        compositeDisposable = null
        mainActivityReference?.clear()
        mainActivityReference = null
    }

    fun showPushNotification(tags: List<String>?, message: String?, notificationId: Int) {
        showPushNotification(tags = tags, message = message, notificationId = notificationId, deeplinkUri = "")
    }

    fun showPushNotification(tags: List<String>?, message: String?, notificationId: Int, deeplinkUri: String?) {
        if (tags == null || tags.isEmpty()
                || StringUtils.isEmpty(message)
                || !isNotificationsEnabled()){
            return
        }
        mainActivityReference?.get()?.let {
            val currentTags: Set<String> = UAirship.shared().pushManager?.tags ?: HashSet<String>()
            val tag: String = tags[0].toUpperCase()

            val breakingNewsTag: String = it.config?.pushNotificationTags?.breakingNewsTag ?: ""

            when {
                (!currentTags.contains(element = breakingNewsTag) && isBreakingTag(tag = tag)) -> {
                    it.removePostedNotification(notificationId)
                }
                else -> {
                    val notification: NotificationData = NotificationData(!isBreakingTag(tag),
                            tag,
                            message,
                            it.window.statusBarColor,
                            createCallbackFromUri(deeplinkUri))

                    it.showNotification(notification, PUSH_SHOWN_DURATION)

                    if (it.isActivityVisible){
                        it.removePostedNotification(notificationId)
                    }
                }
            }
        }
    }

    fun showAuthMessage(string: String?) {
        if (StringUtils.isEmpty(string)) return
        mainActivityReference?.get()?.let {
            val title: String = it.resources.getString(R.string.alert_system_default_text)
            val message: String = string!!

            if (!StringUtils.isEmpty(title) && !StringUtils.isEmpty(message)){
                val notificationData: NotificationData = NotificationData(
                        false,
                        title,
                        message,
                        it.window.statusBarColor,
                        null)
                it.showNotification(notificationData)
            }
        }
    }

    /**
     * Callbacks are based on tag's value.
     */
    private fun createCallbackFromUri(uri: String?): View.OnClickListener? {
        if (StringUtils.isEmpty(uri)) return null
        return object: View.OnClickListener {
            override fun onClick(v: View?) {
                mainActivityReference?.get()?.let {
                    val deeplinkIntent: Intent = Intent(it, EntryActivity::class.java)
                    deeplinkIntent.apply {
                        action = Intent.ACTION_VIEW
                        data = Uri.parse(uri)
                    }
                    if (Deeplink.getDeeplinkFromIntent(deeplinkIntent) != null){
                        it.startActivity(deeplinkIntent)
                    }
                }
            }
        }
    }

    fun showNetworkError() {
        mainActivityReference?.get()?.let {
            val res: Resources = it.resources

            // Uses default values for title and message if Localizations are null
            var title: String = res.getString(R.string.alert_system_default_text)
            var message: String = res.getString(R.string.alert_network_error_default_text)

            if (LocalizationManager.isInitialized()){
                title = LocalizationManager.Alerts.NetworkError.Title
                message = LocalizationManager.Alerts.NetworkError.Message
            }

            if (!StringUtils.isEmpty(title) && !StringUtils.isEmpty(message)){
                val notification: NotificationData = NotificationData(
                        false,
                        title,
                        message,
                        it.window.statusBarColor,
                        null)
                it.showNotification(notification)
            }
        }
    }

    fun showPlaybackError(errorCode: String?) {
        if (StringUtils.isEmpty(errorCode)) return
        mainActivityReference?.get()?.let {
            val res: Resources = it.resources

            // Use defaults for title and message if Localizations is null.
            var title: String = res.getString(R.string.alert_system_default_text)
            var message: String = res.getString(R.string.alert_playback_error_default_text)

            if (LocalizationManager.isInitialized()){
                var errorCodeMessage: String = ""
                if (BuildConfig.DEBUG){
                    errorCodeMessage = String.format("(%s)", errorCode)
                }
                title = LocalizationManager.Alerts.PlaybackError.Title
                message = LocalizationManager.Alerts.PlaybackError.Message+" "+errorCodeMessage
            }

            if (!StringUtils.isEmpty(title) && !StringUtils.isEmpty(message)){
                val notification: NotificationData = NotificationData(
                        false,
                        title,
                        message,
                        it.window.statusBarColor,
                        null)
                it.showNotification(notification)
            }
        }
    }

    fun showServerError() {
        mainActivityReference?.get()?.let {
            val res: Resources = it.resources

            // Uses default values for title and message if Localizations are null
            var title: String = res.getString(R.string.alert_system_default_text)
            var message: String = res.getString(R.string.alert_server_error_default_text)

            if (LocalizationManager.isInitialized()){
                title = LocalizationManager.Alerts.ServerError.Title
                message = LocalizationManager.Alerts.ServerError.Message
            }

            if (!StringUtils.isEmpty(title) && !StringUtils.isEmpty(message)){
                val notification: NotificationData = NotificationData(
                        false,
                        title,
                        message,
                        it.window.statusBarColor,
                        null)
                it.showNotification(notification)
            }
        }
    }

    fun showWifiError() {
        mainActivityReference?.get()?.let {
            val res: Resources = it.resources

            // Uses default values for title and message if Localizations are null
            var title: String = res.getString(R.string.alert_system_default_text)
            var message: String = res.getString(R.string.alert_wifi_error_default_text)

            if (LocalizationManager.isInitialized()){
                title = LocalizationManager.Alerts.WifiError.Title
                message = LocalizationManager.Alerts.WifiError.Message
            }

            if (!StringUtils.isEmpty(title) && !StringUtils.isEmpty(message)){
                val notification: NotificationData = NotificationData(
                        false,
                        title,
                        message,
                        it.window.statusBarColor,
                        null)
                it.showNotification(notification)
            }
        }
    }

    fun showLogOut() {
        mainActivityReference?.get()?.let {
            val res: Resources = it.resources

            // Uses default values for title and message if Localizations are null
            var title: String = res.getString(R.string.alert_system_default_text)
            var message: String = res.getString(R.string.logout_successfully)

            if (LocalizationManager.isInitialized()){
                title = LocalizationManager.Alerts.LogOut.Title
                message = LocalizationManager.Alerts.LogOut.Message
            }

            if (!StringUtils.isEmpty(title) && !StringUtils.isEmpty(message)){
                val notification: NotificationData = NotificationData(
                        false,
                        title,
                        message,
                        it.window.statusBarColor,
                        null)
                it.showNotification(notification)
            }
        }
    }

    fun showAuthNError() {
        mainActivityReference?.get()?.let {
            val res: Resources = it.resources

            // Uses default values for title and message if Localizations are null
            var title: String = res.getString(R.string.alert_system_default_text)
            var message: String = res.getString(R.string.alert_authn_error)

            if (!StringUtils.isEmpty(it.localizations?.Alerts?.AuthError?.Title)){
                title = it.localizations?.Alerts?.AuthError?.Title!!
            }
            if (!StringUtils.isEmpty(it.localizations?.Alerts?.AuthError?.Message)){
                message = it.localizations?.Alerts?.AuthError?.Message!!
            }

            if (!StringUtils.isEmpty(title) && !StringUtils.isEmpty(message)){
                val notification: NotificationData = NotificationData(
                        false,
                        title,
                        message,
                        it.window.statusBarColor,
                        null)
                it.showNotification(notification)
            }
        }
    }

    fun showFtueFabFlickMessage() {
        mainActivityReference?.get()?.let {
            val res: Resources = it.resources

            // Uses default values for title and message if Localizations are null
            var title: String = res.getString(R.string.ftue_fab_flick_tag)
            var message: String = res.getString(R.string.ftue_fab_flick_message)

            if (LocalizationManager.isInitialized()){
                title = LocalizationManager.Alerts.FTUEFlick.Title
                message = LocalizationManager.Alerts.FTUEFlick.Message
            }

            if (!StringUtils.isEmpty(title) && !StringUtils.isEmpty(message)){
                val notification: NotificationData = NotificationData(
                        true,
                        title,
                        message,
                        it.window.statusBarColor,
                        null,
                        true)
                it.showNotification(notification)
            }
        }
    }

    fun showFtueFabTapMessage() {
        mainActivityReference?.get()?.let {
            val res: Resources = it.resources

            // Uses default values for title and message if Localizations are null
            var title: String = res.getString(R.string.ftue_fab_tap_tag)
            var message: String = res.getString(R.string.ftue_fab_tap_message)

            if (LocalizationManager.isInitialized()){
                title = LocalizationManager.Alerts.FTUETap.Title
                message = LocalizationManager.Alerts.FTUETap.Message
            }

            if (!StringUtils.isEmpty(title) && !StringUtils.isEmpty(message)){
                val notification: NotificationData = NotificationData(
                        true,
                        title,
                        message,
                        it.window.statusBarColor,
                        null,
                        true)
                it.showNotification(notification)
            }
        }
    }

    fun showFtueDataMenuMessage() {
        mainActivityReference?.get()?.let {
            val res: Resources = it.resources

            // Uses default values for title and message if Localizations are null
            var title: String = res.getString(R.string.ftue_data_menu_tag)
            var message: String = res.getString(R.string.ftue_data_menu_message)

            if (LocalizationManager.isInitialized()){
                title = LocalizationManager.Alerts.FTUEDataMenu.Title
                message = LocalizationManager.Alerts.FTUEDataMenu.Message
            }

            if (!StringUtils.isEmpty(title) && !StringUtils.isEmpty(message)){
                val notification: BottomNotificationData = BottomNotificationData(
                        true,
                        title,
                        message,
                        it.window.statusBarColor,
                        null,
                        true)
                it.showBottomNotification(notification)
            }
        }
    }

    fun showCellularDataNotAllowError() {
        mainActivityReference?.get()?.let {
            val res: Resources = it.resources

            // Uses default values for title and message if Localizations are null
            var title: String = res.getString(R.string.alert_system_default_text)
            var message: String = res.getString(R.string.alert_cellular_data_not_allow_default_text)

            if (LocalizationManager.isInitialized()){
                title = LocalizationManager.Alerts.CellularStreamError.Title
                message = LocalizationManager.Alerts.CellularStreamError.Message
            }

            if (!StringUtils.isEmpty(title) && !StringUtils.isEmpty(message)){
                val notification: NotificationData = NotificationData(
                        false,
                        title,
                        message,
                        it.window.statusBarColor,
                        null)
                it.showNotification(notification)
            }
        }
    }

    fun showLocationPermissionError(clickListener: View.OnClickListener) {
        mainActivityReference?.get()?.let {
            val res: Resources = it.resources

            // Uses default values for title and message
            val title: String = res.getString(R.string.alert_location_permission_title)
            val message: String = res.getString(R.string.alert_location_permission)

            if (!StringUtils.isEmpty(title) && !StringUtils.isEmpty(message)){
                val notification: NotificationData = NotificationData(
                        false,
                        title,
                        message,
                        it.window.statusBarColor,
                        clickListener)
                it.showNotification(notification)
            }
        }
    }

    fun updateReturnStatusBarColor(teamColor: Int) {
        mainActivityReference?.get()?.notificationBanner?.updateReturnStatusBarColor(teamColor)
    }

    fun UAFirstLaunch() {
        // Called on first ever launch to set up UA notifications
        UAirship.shared().pushManager?.apply { userNotificationsEnabled = true }
        setBreakingNewsOptIn(true)

        val teamIds: List<String> = ArrayList<String>()
        mainActivityReference?.get()?.teamManager?.usersTeams?.let {
            for (team: Team in it){
                (teamIds as ArrayList<String>).add(team.teamId)
            }
        }
        setTeamNotificationsOptIn(teamIds = teamIds, optIn = true)
    }

    fun loadNotificationTags(config: Config?) {
        config?.pushNotificationTags?.let {
            breakingNewsTag = it.breakingNewsTag
            teamNewsTag = it.teamNewsTag
            gameStartTag = it.teamGameStartTag
            finalScoreTag = it.teamFinalScoreTag
        }
    }

    fun isNotificationShowing(): Boolean {
        return mainActivityReference?.get()?.notificationBanner?.isBannerShowing == true
    }

    private fun isBreakingTag(tag: String?): Boolean {
        return tag?.toUpperCase()?.equals(BREAKING_TAG) ?: false
    }

    private fun isNotificationsEnabled(): Boolean {
        return UAirship.shared().pushManager?.userNotificationsEnabled ?: false
    }

    fun setAllowNotificationsOptIn(optIn: Boolean) {
        UAirship.shared().pushManager?.apply { userNotificationsEnabled = optIn }
    }

    fun setBreakingNewsOptIn(optIn: Boolean) {
        val currentTags: Set<String> = UAirship.shared().pushManager?.tags ?: HashSet<String>()
        breakingNewsTag?.let {
            when (optIn){
                true -> {(currentTags as HashSet<String>).add(it)}
                else -> {(currentTags as HashSet<String>).remove(it)}
            }
        }
        UAirship.shared().pushManager?.apply { tags = currentTags }
    }

    fun setTeamNotificationsOptIn(teamId: String, optIn: Boolean) {
        val currentTags: Set<String> = UAirship.shared().pushManager?.tags ?: HashSet<String>()
        when (optIn){
            true -> {
                (currentTags as HashSet<String>).add(getTeamNewsTagForTeam(teamId))
                (currentTags as HashSet<String>).add(getGameStartTagForTeam(teamId))
                (currentTags as HashSet<String>).add(getFinalScoreTagForTeam(teamId))
            }
            else -> {
                (currentTags as HashSet<String>).remove(getTeamNewsTagForTeam(teamId))
                (currentTags as HashSet<String>).remove(getGameStartTagForTeam(teamId))
                (currentTags as HashSet<String>).remove(getFinalScoreTagForTeam(teamId))
            }
        }
        UAirship.shared().pushManager?.apply { tags = currentTags }
    }

    fun setTeamNotificationsOptIn(teamIds: List<String>, optIn: Boolean) {
        val currentTags: Set<String> = UAirship.shared().pushManager?.tags ?: HashSet<String>()
        for (teamId: String in teamIds){
            when (optIn){
                true -> {
                    (currentTags as HashSet<String>).add(getTeamNewsTagForTeam(teamId))
                    (currentTags as HashSet<String>).add(getGameStartTagForTeam(teamId))
                    (currentTags as HashSet<String>).add(getFinalScoreTagForTeam(teamId))
                }
                else -> {
                    (currentTags as HashSet<String>).remove(getTeamNewsTagForTeam(teamId))
                    (currentTags as HashSet<String>).remove(getGameStartTagForTeam(teamId))
                    (currentTags as HashSet<String>).remove(getFinalScoreTagForTeam(teamId))
                }
            }
        }
        UAirship.shared().pushManager?.apply { tags = currentTags }
    }

    fun setTeamNewsOptIn(teamId: String, optIn: Boolean) {
        val currentTags: Set<String> = UAirship.shared().pushManager?.tags ?: HashSet<String>()
        val teamNewsTagForTeam: String = getTeamNewsTagForTeam(teamId = teamId)
        when (optIn){
            true -> {
                (currentTags as HashSet<String>).add(teamNewsTagForTeam)
            }
            else -> {
                (currentTags as HashSet<String>).remove(teamNewsTagForTeam)
            }
        }
        UAirship.shared().pushManager?.apply { tags = currentTags }
    }

    fun setGameStartOptIn(teamId: String, optIn: Boolean) {
        val currentTags: Set<String> = UAirship.shared().pushManager?.tags ?: HashSet<String>()
        val gameStartTagForTeam: String = getGameStartTagForTeam(teamId = teamId)
        when (optIn){
            true -> {
                (currentTags as HashSet<String>).add(gameStartTagForTeam)
            }
            else -> {
                (currentTags as HashSet<String>).remove(gameStartTagForTeam)
            }
        }
        UAirship.shared().pushManager?.apply { tags = currentTags }
    }

    fun setFinalScoreOptIn(teamId: String, optIn: Boolean) {
        val currentTags: Set<String> = UAirship.shared().pushManager?.tags ?: HashSet<String>()
        val finalScoreTagForTeam: String = getFinalScoreTagForTeam(teamId = teamId)
        when (optIn){
            true -> {
                (currentTags as HashSet<String>).add(finalScoreTagForTeam)
            }
            else -> {
                (currentTags as HashSet<String>).remove(finalScoreTagForTeam)
            }
        }
        UAirship.shared().pushManager?.apply { tags = currentTags }
    }

    fun isOptedInBreakingNews(): Boolean {
        return UAirship.shared().pushManager?.tags?.contains(breakingNewsTag) ?: false
    }

    fun isOptedInTeamNews(teamId: String): Boolean {
        return UAirship.shared().pushManager?.tags?.contains(getTeamNewsTagForTeam(teamId)) ?: false
    }

    fun isOptedInGameStart(teamId: String): Boolean {
        return UAirship.shared().pushManager?.tags?.contains(getGameStartTagForTeam(teamId)) ?: false
    }

    fun isOptedInFinalScore(teamId: String): Boolean {
        return UAirship.shared().pushManager?.tags?.contains(getFinalScoreTagForTeam(teamId)) ?: false
    }

    fun getTeamNewsTagForTeam(teamId: String): String {
        if (StringUtils.isEmpty(teamNewsTag)){
            loadNotificationTags(mainActivityReference?.get()?.config)
        }
        return teamNewsTag?.replace(oldValue = TEAM_ID_PLACEHOLDER, newValue = teamId) ?: ""
    }

    fun getGameStartTagForTeam(teamId: String): String {
        if (StringUtils.isEmpty(gameStartTag)){
            loadNotificationTags(mainActivityReference?.get()?.config)
        }
        return gameStartTag?.replace(oldValue = TEAM_ID_PLACEHOLDER, newValue = teamId) ?: ""
    }

    fun getFinalScoreTagForTeam(teamId: String): String {
        if (StringUtils.isEmpty(finalScoreTag)){
            loadNotificationTags(mainActivityReference?.get()?.config)
        }
        return finalScoreTag?.replace(oldValue = TEAM_ID_PLACEHOLDER, newValue = teamId) ?: ""
    }

    fun updateNotificationOptInStatuses(oldList: List<Team>, newList: List<Team>) {

        val oldTeamIds: ArrayList<String> = ArrayList<String>()
        for (team: Team in oldList){
            oldTeamIds.add(team.teamId)
        }

        val newTeamIds: ArrayList<String> = ArrayList<String>()
        for (team: Team in newList){
            newTeamIds.add(team.teamId)
        }

        val newlyAddedTeamIds: ArrayList<String> = ArrayList<String>(newTeamIds)
        newlyAddedTeamIds.removeAll(oldTeamIds)
        if (!newlyAddedTeamIds.isEmpty()) {
            setTeamNotificationsOptIn(newlyAddedTeamIds, true)
        }

        val newlyRemovedTeamIds: ArrayList<String> = ArrayList<String>(oldTeamIds)
        newlyRemovedTeamIds.removeAll(newTeamIds)
        if (!newlyRemovedTeamIds.isEmpty()) {
            setTeamNotificationsOptIn(newlyRemovedTeamIds, false)
        }
    }

    fun hideBanner() {
        if (mainActivityReference?.get()?.notificationBanner?.isBannerShowing == true){
            mainActivityReference?.get()?.notificationBanner?.post {
                // Need to check for nullity again because Runnable is run later
                mainActivityReference?.get()?.notificationBanner?.hideBanner()
            }
        }
    }

    fun isConntectedWifi(): Boolean {
        // TODO: is there a better solution than this?
        return mainActivityReference?.get()?.context?.let {
            ConnectionUtils.isConnectedWifi(it)
        } ?: false
    }

    fun handleException(e: Throwable?, team: Team?): Boolean {
        when {
            (e is ConnectException || e is UnknownHostException) -> { // No connection || can not determine IP address
                when (isConntectedWifi()){
                    true -> { showWifiError() }
                    else -> { showNetworkError() }
                }
                return true
            }
            (e is IOException) -> {
                when {
                    (team != null) -> {
                        val currentTeam: Team? = TeamManager.getInstance()?.getSelectedTeam()
                        if (team.equals(currentTeam)){
                            showServerError()
                        }
                    }
                    else -> {
                        showServerError()
                    }
                }
                return true
            }
            else -> {
                return false
            }
        }
    }

    override fun onBannerShow(notificationData: NotificationData?) {
        mainActivityReference?.get()?.let {
            val res: Resources = it.resources

            // Uses default values for title and message if Localizations are null
            var bannerTitle: String = res.getString(R.string.ftue_fab_flick_tag)
            var bannerMsg: String = res.getString(R.string.ftue_fab_flick_message)

            if (LocalizationManager.isInitialized()){
                bannerTitle = LocalizationManager.Alerts.FTUEFlick.Title
                bannerMsg = LocalizationManager.Alerts.FTUEFlick.Message
            }

            if (FtueUtil.fabHasNeverBeenFlicked()
                    || !StringUtils.equalsIgnoreCase(bannerTitle, notificationData?.title)
                    || !StringUtils.equalsIgnoreCase(bannerMsg, notificationData?.message)) {
                return
            }

            if (compositeDisposable == null){
                compositeDisposable = CompositeDisposable()
            }

            // Remove the FTUE notification if user has already used the flick functionality after 5 seconds
            (compositeDisposable as CompositeDisposable)
                    .add(Observable.timer(FTUE_NOTIFICATION_REMOVE_DELAY, TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.computation())
                    .subscribeWith(object: DisposableObserver<Long>() {
                        override fun onNext(aLong: Long) {}
                        override fun onError(e: Throwable) {}
                        override fun onComplete() {
                            it.notificationBanner?.hideBanner()
                            dispose()
                        }
                    }))

        }
    }

    fun isFtueFabFlickBannerShowing(): Boolean {
        if (mainActivityReference?.get()?.notificationBanner?.currentNotification == null){
            return false
        }
        mainActivityReference?.get()?.let {
            val res: Resources = it.resources

            // Uses default values for title and message if Localizations are null
            var title: String = res.getString(R.string.ftue_fab_flick_tag)
            var message: String = res.getString(R.string.ftue_fab_flick_message)

            if (LocalizationManager.isInitialized()){
                title = LocalizationManager.Alerts.FTUEFlick.Title
                message = LocalizationManager.Alerts.FTUEFlick.Message
            }

            val banner: NotificationBanner = it.notificationBanner
            val bannerTitle: String? = banner.currentNotification?.title
            val bannerMessage: String? = banner.currentNotification?.message

            return banner.isBannerShowing
                    && StringUtils.equalsIgnoreCase(title, bannerTitle)
                    && StringUtils.equalsIgnoreCase(message, bannerMessage)
        } ?: return false
    }

    fun isFtueFabTapBannerShowing(): Boolean {
        if (mainActivityReference?.get()?.notificationBanner?.currentNotification == null){
            return false
        }
        mainActivityReference?.get()?.let {
            val res: Resources = it.resources

            // Uses default values for title and message if Localizations are null
            var title: String = res.getString(R.string.ftue_fab_tap_tag)
            var message: String = res.getString(R.string.ftue_fab_tap_message)

            if (LocalizationManager.isInitialized()){
                title = LocalizationManager.Alerts.FTUETap.Title
                message = LocalizationManager.Alerts.FTUETap.Message
            }

            val banner: NotificationBanner = it.notificationBanner
            val bannerTitle: String? = banner.currentNotification?.title
            val bannerMessage: String? = banner.currentNotification?.message

            return banner.isBannerShowing
                    && StringUtils.equalsIgnoreCase(title, bannerTitle)
                    && StringUtils.equalsIgnoreCase(message, bannerMessage)
        } ?: return false
    }

    fun isFtueDataMenuBottomBannerShowing(): Boolean {
        if (mainActivityReference?.get()?.bottomNotificationBanner?.currentNotification == null){
            return false
        }
        mainActivityReference?.get()?.let {
            val res: Resources = it.resources

            // Uses default values for title and message if Localizations are null
            var title: String = res.getString(R.string.ftue_data_menu_tag)
            var message: String = res.getString(R.string.ftue_data_menu_message)

            if (LocalizationManager.isInitialized()){
                title = LocalizationManager.Alerts.FTUEDataMenu.Title
                message = LocalizationManager.Alerts.FTUEDataMenu.Message
            }

            val banner: BottomNotificationBanner = it.bottomNotificationBanner
            val bannerTitle: String? = banner.currentNotification?.title
            val bannerMessage: String? = banner.currentNotification?.message

            return (banner.isBannerReadyToShow() || banner.isBannerShowing())
                    && StringUtils.equalsIgnoreCase(title, bannerTitle)
                    && StringUtils.equalsIgnoreCase(message, bannerMessage)
        } ?: return false
    }

    fun isBannerShowing(): Boolean {
        if (mainActivityReference?.get()?.notificationBanner?.currentNotification == null){
            return false
        }
        return mainActivityReference?.get()?.notificationBanner?.isBannerShowing == true
    }

    fun isBottomBannerReadyToShowOrShowing(): Boolean {
        if (mainActivityReference?.get()?.bottomNotificationBanner?.currentNotification == null){
            return false
        }
        return mainActivityReference?.get()?.bottomNotificationBanner?.isBannerReadyToShow() == true
                || mainActivityReference?.get()?.bottomNotificationBanner?.isBannerShowing() == true
    }

    fun showAuthZError(auth: Auth?) {
        mainActivityReference?.get()?.let {
            // For showing authz error, we do an additional check to see if the error is supposed to show.
            // if tokenized url is not empty, that means we are calling this banner without a legit auth z error
            // so let's ignore the request.
            if (!StringUtils.isEmpty(auth?.nbcToken?.tokenizedUrl)){
                return
            }
            val res: Resources = it.resources

            // Uses default values for title and message if Localizations are null
            var title: String = res.getString(R.string.alert_system_default_text)
            var message: String = res.getString(R.string.alert_authz_error)

            if (LocalizationManager.isInitialized()){
                title = LocalizationManager.Alerts.AuthorizeError.Title
                message = LocalizationManager.Alerts.AuthorizeError.Message
            }

            if (!StringUtils.isEmpty(title) && !StringUtils.isEmpty(message)){
                val notification: NotificationData = NotificationData(
                        false,
                        title,
                        message,
                        it.window.statusBarColor,
                        null)
                it.showNotification(notification)
            }
        }
    }

    fun showEntitlementError(authError: IOException, mediaSource: MediaSource) {
        val errorChecker: ErrorChecker = ErrorChecker(mediaSource)
        if (errorChecker.isShown(authError)){
            return
        }

        mainActivityReference?.get()?.let {
            val res: Resources = it.resources
            val cause: Throwable? = authError.cause
            if (cause is HttpDataSource.InvalidResponseCodeException){
                var title: String = ""
                var message: String = ""

                val httpError: HttpDataSource.InvalidResponseCodeException = cause
                val fields: Map<String, List<String>>? = httpError.headerFields
                val field: List<String> = fields?.get("entitlement_error") ?: ArrayList<String>()
                if (field.isNotEmpty()){
                    val error: String = field[0]
                    val errorInt: Int = Integer.parseInt(error)

                    when (errorInt) {
                        10, 450 -> {
                            if (LocalizationManager.isInitialized()) {
                                title = LocalizationManager.Alerts.NetworkError.Title
                                message = LocalizationManager.Alerts.NetworkError.Message
                            } else {
                                title = res.getString(R.string.alert_system_default_text)
                                message = res.getString(R.string.alert_network_error_default_text)
                            }
                        }
                        100, 200, 210 -> {
                            if (LocalizationManager.isInitialized()) {
                                title = LocalizationManager.Alerts.GMOEntitlementError.Title
                                message = LocalizationManager.Alerts.GMOEntitlementError.Message
                            } else {
                                title = res.getString(R.string.alert_system_default_text)
                                message = res.getString(R.string.alert_entitlement_error_gmo)
                            }
                        }
                        300, 310, 320, 400, 410, 420, 430, 440 -> {
                            if (LocalizationManager.isInitialized()) {
                                title = LocalizationManager.Alerts.RSNEntitlementError.Title
                                message = LocalizationManager.Alerts.RSNEntitlementError.Message
                            } else {
                                title = res.getString(R.string.alert_system_default_text)
                                message = res.getString(R.string.alert_entitlement_error_rsn)
                            }
                        }
                    }

                    if (!StringUtils.isEmpty(title) && !StringUtils.isEmpty(message)){
                        val notification: NotificationData = NotificationData(
                                false,
                                title,
                                message,
                                it.window.statusBarColor,
                                null)
                        errorChecker.setShown(authError, true)
                        it.showNotification(notification)
                    }
                }
            }
        }
    }

    /**
     * This method is used to show chromecast failed notification banner
     *
     * @param error
     */
    fun showChromecastFailed(error: String) {
        if (isChromecastFailedBannerShowing(error)){
            return
        }
        mainActivityReference?.get()?.let {
            val res: Resources = it.resources

            // Uses default values for title and message if Localizations are null
            var title: String = res.getString(R.string.chromecast_failed_title)
            var message: String = "${res.getString(R.string.chromecast_failed_message)}$error"

            if (LocalizationManager.isInitialized()){
                title = LocalizationManager.Alerts.ChromecastFailed.Title
                message = LocalizationManager.Alerts.ChromecastFailed.getMessage(error)
            }

            if (!StringUtils.isEmpty(title) && !StringUtils.isEmpty(message)){
                val notification: NotificationData = NotificationData(
                        false,
                        title,
                        message,
                        it.window.statusBarColor,
                        null)
                it.showNotification(notification)
            }
        }
    }

    /**
     * This method is used to check whether chromecast failed notification banner
     * is showing
     *
     * @param error
     * @return true if banner is showing
     *         false, otherwise
     */
    private fun isChromecastFailedBannerShowing(error: String): Boolean {
        if (mainActivityReference?.get()?.notificationBanner?.currentNotification == null){
            return false
        }
        mainActivityReference?.get()?.let {
            val res: Resources = it.resources

            // Uses default values for title and message if Localizations are null
            var title: String = res.getString(R.string.chromecast_failed_title)
            var message: String = "${res.getString(R.string.chromecast_failed_message)}$error"

            if (LocalizationManager.isInitialized()){
                title = LocalizationManager.Alerts.ChromecastFailed.Title
                message = LocalizationManager.Alerts.ChromecastFailed.getMessage(error)
            }

            val banner: NotificationBanner = it.notificationBanner
            val bannerTitle: String? = banner.currentNotification?.title
            val bannerMessage: String? = banner.currentNotification?.message

            return banner.isBannerShowing
                    && StringUtils.equalsIgnoreCase(title, bannerTitle)
                    && StringUtils.equalsIgnoreCase(message, bannerMessage)
        } ?: return false
    }

    /**
     * This method is used to show chromecast error notification banner
     *
     * @param error
     */
    fun showChromecastError(error: String) {
        if (isChromecastErrorBannerShowing(error)){
            return
        }
        mainActivityReference?.get()?.let {
            val res: Resources = it.resources

            // Uses default values for title and message if Localizations are null
            var title: String = res.getString(R.string.chromecast_error_title)
            var message: String = "${res.getString(R.string.chromecast_error_message)}$error"

            if (LocalizationManager.isInitialized()){
                title = LocalizationManager.Alerts.ChromecastError.Title
                message = LocalizationManager.Alerts.ChromecastError.getMessage(error)
            }

            if (!StringUtils.isEmpty(title) && !StringUtils.isEmpty(message)){
                val notification: NotificationData = NotificationData(
                        false,
                        title,
                        message,
                        it.window.statusBarColor,
                        null)
                it.showNotification(notification)
            }
        }
    }

    /**
     * This method is used to check whether chromecast error notification banner
     * is showing
     *
     * @param error
     * @return true if banner is showing
     *         false, otherwise
     */
    private fun isChromecastErrorBannerShowing(error: String): Boolean {
        if (mainActivityReference?.get()?.notificationBanner?.currentNotification == null){
            return false
        }
        mainActivityReference?.get()?.let {
            val res: Resources = it.resources

            // Uses default values for title and message if Localizations are null
            var title: String = res.getString(R.string.chromecast_error_title)
            var message: String = "${res.getString(R.string.chromecast_error_message)}$error"

            if (LocalizationManager.isInitialized()){
                title = LocalizationManager.Alerts.ChromecastError.Title
                message = LocalizationManager.Alerts.ChromecastError.getMessage(error)
            }

            val banner: NotificationBanner = it.notificationBanner
            val bannerTitle: String? = banner.currentNotification?.title
            val bannerMessage: String? = banner.currentNotification?.message

            return banner.isBannerShowing
                    && StringUtils.equalsIgnoreCase(title, bannerTitle)
                    && StringUtils.equalsIgnoreCase(message, bannerMessage)
        } ?: return false
    }

    /**
     * This method is used to show chromecast ended notification banner
     */
    fun showChromecastEnded() {
        if (isChromecastEndedBannerShowing()){
            return
        }
        mainActivityReference?.get()?.let {
            val res: Resources = it.resources

            // Uses default values for title and message if Localizations are null
            var title: String = res.getString(R.string.chromecast_ended_title)
            var message: String = res.getString(R.string.chromecast_ended_message)

            if (LocalizationManager.isInitialized()){
                title = LocalizationManager.Alerts.ChromecastEnded.Title
                message = LocalizationManager.Alerts.ChromecastEnded.Message
            }

            if (!StringUtils.isEmpty(title) && !StringUtils.isEmpty(message)){
                val notification: NotificationData = NotificationData(
                        false,
                        title,
                        message,
                        it.window.statusBarColor,
                        null)
                it.showNotification(notification)
            }
        }
    }

    /**
     * This method is used to check whether chromecast ended notification banner
     * is showing
     *
     * @return true if banner is showing
     *         false, otherwise
     */
    private fun isChromecastEndedBannerShowing(): Boolean {
        if (mainActivityReference?.get()?.notificationBanner?.currentNotification == null){
            return false
        }
        mainActivityReference?.get()?.let {
            val res: Resources = it.resources

            // Uses default values for title and message if Localizations are null
            var title: String = res.getString(R.string.chromecast_ended_title)
            var message: String = res.getString(R.string.chromecast_ended_message)

            if (LocalizationManager.isInitialized()){
                title = LocalizationManager.Alerts.ChromecastEnded.Title
                message = LocalizationManager.Alerts.ChromecastEnded.Message
            }

            val banner: NotificationBanner = it.notificationBanner
            val bannerTitle: String? = banner.currentNotification?.title
            val bannerMessage: String? = banner.currentNotification?.message

            return banner.isBannerShowing
                    && StringUtils.equalsIgnoreCase(title, bannerTitle)
                    && StringUtils.equalsIgnoreCase(message, bannerMessage)
        } ?: return false
    }

    /**
     * This method is used to show chromecast asset error notification banner
     */
    fun showChromecastAssetError() {
        if (isChromecastAssetErrorBannerShowing()){
            return
        }
        mainActivityReference?.get()?.let {
            val res: Resources = it.resources

            // Uses default values for title and message if Localizations are null
            var title: String = res.getString(R.string.chromecast_asset_error_title)
            var message: String = res.getString(R.string.chromecast_asset_error_message)

            if (LocalizationManager.isInitialized()){
                title = LocalizationManager.Alerts.ChromecastAssetError.Title
                message = LocalizationManager.Alerts.ChromecastAssetError.Message
            }

            if (!StringUtils.isEmpty(title) && !StringUtils.isEmpty(message)){
                val notification: NotificationData = NotificationData(
                        false,
                        title,
                        message,
                        it.window.statusBarColor,
                        null)
                it.showNotification(notification)
            }
        }
    }

    /**
     * This method is used to check whether chromecast asset error notification banner
     * is showing
     *
     * @return true if banner is showing
     *         false, otherwise
     */
    private fun isChromecastAssetErrorBannerShowing(): Boolean {
        if (mainActivityReference?.get()?.notificationBanner?.currentNotification == null){
            return false
        }
        mainActivityReference?.get()?.let {
            val res: Resources = it.resources

            // Uses default values for title and message if Localizations are null
            var title: String = res.getString(R.string.chromecast_asset_error_title)
            var message: String = res.getString(R.string.chromecast_asset_error_message)

            if (LocalizationManager.isInitialized()){
                title = LocalizationManager.Alerts.ChromecastAssetError.Title
                message = LocalizationManager.Alerts.ChromecastAssetError.Message
            }

            val banner: NotificationBanner = it.notificationBanner
            val bannerTitle: String? = banner.currentNotification?.title
            val bannerMessage: String? = banner.currentNotification?.message

            return banner.isBannerShowing
                    && StringUtils.equalsIgnoreCase(title, bannerTitle)
                    && StringUtils.equalsIgnoreCase(message, bannerMessage)
        } ?: return false
    }
}