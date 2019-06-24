package com.nbcsports.regional.nbc_rsn.team_view

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.viewpager.widget.ViewPager
import android.text.format.DateFormat
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.jakewharton.rxbinding2.view.RxView
import com.nbcsports.regional.nbc_rsn.MainActivity
import com.nbcsports.regional.nbc_rsn.MainPresenter
import com.nbcsports.regional.nbc_rsn.R
import com.nbcsports.regional.nbc_rsn.analytics.TrackingHelper.PageInfo
import com.nbcsports.regional.nbc_rsn.analytics.TrackingHelper.TrackingHelper
import com.nbcsports.regional.nbc_rsn.authentication.Auth
import com.nbcsports.regional.nbc_rsn.authentication.MvpdLogoListener
import com.nbcsports.regional.nbc_rsn.authentication.StreamAuthenticationContract
import com.nbcsports.regional.nbc_rsn.authentication.StreamAuthenticationPresenter
import com.nbcsports.regional.nbc_rsn.common.*
import com.nbcsports.regional.nbc_rsn.common.Constants.CONFIG_KEY
import com.nbcsports.regional.nbc_rsn.common.Constants.TEAM_KEY
import com.nbcsports.regional.nbc_rsn.data_bar.*
import com.nbcsports.regional.nbc_rsn.data_menu.models.RotoDataBar
import com.nbcsports.regional.nbc_rsn.debug_options.DebugPresenter
import com.nbcsports.regional.nbc_rsn.editorial_detail.EditorialDetailTemplateFragment
import com.nbcsports.regional.nbc_rsn.extensions.d
import com.nbcsports.regional.nbc_rsn.localization.LocalizationManager
import com.nbcsports.regional.nbc_rsn.persistentplayer.Injection
import com.nbcsports.regional.nbc_rsn.persistentplayer.PersistentPlayer
import com.nbcsports.regional.nbc_rsn.persistentplayer.PersistentPlayerContract
import com.nbcsports.regional.nbc_rsn.persistentplayer.PlayerConstants
import com.nbcsports.regional.nbc_rsn.rating_review.RatingReview._PREF_KEY_SHOW_RATE_DIALOG_STATE
import com.nbcsports.regional.nbc_rsn.rating_review.SHOW_RATE_DIALOG_STATE
import com.nbcsports.regional.nbc_rsn.stepped_story.SteppedStoryFragment
import com.nbcsports.regional.nbc_rsn.team_feed.template.TeamFeedFragment
import com.nbcsports.regional.nbc_rsn.urban_airship.NotificationsManagerKt
import com.nbcsports.regional.nbc_rsn.utils.*
import com.squareup.picasso.Picasso
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.subjects.BehaviorSubject
import kotlinx.android.synthetic.main.databar_layout.*
import kotlinx.android.synthetic.main.team_container.*
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import java.util.concurrent.TimeUnit
import kotlin.math.max

class TeamContainerFragment : BaseFragment(), DataBarContract.View, TeamFeedFragment.DataBarScrolling, MvpdLogoListener {

    companion object {
        fun newInstance(team: Team, adapter: TeamsPagerAdapter,
                        pager: ViewPager, config: Config): TeamContainerFragment {
            val teamViewContainer = TeamContainerFragment()

            teamViewContainer.adapter = adapter
            teamViewContainer.pager = pager

            val args = Bundle()
            args.putParcelable(TEAM_KEY, team)
            args.putParcelable(CONFIG_KEY, config)
            teamViewContainer.arguments = args

            return teamViewContainer
        }
    }

    private val GAME_TIME_FORMAT_AMFM = "h:mma"
    private val GAME_TIME_FORMAT_24HR = "kk:mm"
    private val NEXT_GAME_FORMAT_AMFM = "MMM d, h:mma"
    private val NEXT_GAME_FORMAT_24HR = "MMM d, kk:mm"
    private val NEXT_GAME_FORMAT_DATE = "MMM d"

    private val AWAY = "away"
    private val HOME = "home"

    private val DATA_BAR_SHADOW_ELEVATION = DisplayUtils.dpToPx(10).toFloat()
    private val DATA_BAR_DEFAULT_ELEVATION = DisplayUtils.dpToPx(1).toFloat()

    var adapter: TeamsPagerAdapter? = null
    var pager: ViewPager? = null

    private var dataBarPresenter: DataBarContract.Presenter? = null
    private var dataBarTouchListener: DataBarTouchListener? = null
    private var currentTeam: Team? = null

    private var databarInfo: RotoDataBar? = null

    private var sportLeague: SportLeague? = null
    private var gameState: GameState? = null
    private var persistentPlayer: PersistentPlayer? = null
    private var liveInAppDisposable: Disposable? = null
    private var streamAuthenticationPresenter: StreamAuthenticationContract.Presenter? = null
    private var streamAuthSuccess: Boolean = false
    private var scoreSize: Float = 30f

    private var dataMenuEnabled: Boolean = false
    private var dataMenuIsOpen: Boolean = false

    override fun getLayout(): Int {
        return R.layout.team_container
    }

    // region Lifecycle Methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //this.trackingPageName = "TeamContainerFragment"
        currentTeam = arguments?.getParcelable(TEAM_KEY)
        val config = arguments?.getParcelable<Config>(CONFIG_KEY)

        // init databar
        dataBarPresenter = DataBarPresenter()

        val fragment = childFragmentManager.findFragmentById(R.id.team_view_content)
        if (fragment == null) {
            childFragmentManager
                    .beginTransaction()
                    .add(R.id.team_view_content, TeamFeedFragment.newInstance(currentTeam, config))
                    .commit()
        }
        // need the persistent player to check LivePP state in the DataBar
        persistentPlayer = Injection.providePlayer(context as PersistentPlayerContract.Main.View?)

        streamAuthenticationPresenter = com.nbcsports.regional.nbc_rsn.authentication.Injection.provideStreamAuthentication(context as StreamAuthenticationContract.View)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.getParcelable<Config>(CONFIG_KEY)?.let {
            // Check if data menu is enable and whether league is active
            if (it.dataMenu != null) dataMenuEnabled = it.dataMenu.isEnabled || PreferenceUtils.getBoolean(DebugPresenter.DATA_MENU_ENABLE, false)

            if (dataMenuEnabled
                    && (currentTeam?.league?.toUpperCase() in (it.dataMenu?.activeLeagues
                            ?: listOf()))) {
                // Initialize data bar on touch listener
                dataBarTouchListener = DataBarTouchListener(this, childFragmentManager,
                        team_container_root_constraint_layout, team_view_content, data_menu_content,
                        team_view_and_data_menu_middle_layer_relative_layout,
                        databar_and_data_menu_gap_cover_layer_relative_layout,
                        team_view_mvpd_bar)
                databar_layout?.apply {
                    setOnTouchListener(dataBarTouchListener)
                }
            }

        }
    }

    fun isCurrentSteppedFragment(): Boolean {
        if (!isAdded) return false // cannot get childFragmentManager if this fragment is not attached
        val teamFeedFragment = childFragmentManager.findFragmentById(R.id.team_view_content) as? TeamFeedFragment

        val fragment = teamFeedFragment?.childFragmentManager?.findFragmentById(R.id.editorial_detail)
        fragment?.let {
            return it is SteppedStoryFragment
        }
        return false
    }

    fun isCurrentEditorialDetailFragment(): Boolean {
        if (!isAdded) return false // cannot get childFragmentManager if this fragment is not attached
        val teamFeedFragment = childFragmentManager.findFragmentById(R.id.team_view_content) as? TeamFeedFragment

        val fragment = teamFeedFragment?.childFragmentManager?.findFragmentById(R.id.editorial_detail)
        fragment?.let {
            return it is EditorialDetailTemplateFragment
        }
        return false
    }

    override fun onResume() {
        super.onResume()
        d("onResume() called ${currentTeam?.teamId}, ${currentTeam?.statsTeamID}")
        //Always hide the keyboard after share
        //https://stackoverflow.com/questions/15362003/keyboard-not-closing-after-returning-from-email-client
        //As explained, If I check if the keyboard is open, it will return false since it is not the app that called the keyboard, but the email client.
        //After some millisecond, the focus changed from outside the app (Email-client) to the app, and then we perform the hide keyboard action
        val hideKeyboard = { KeyboardUtils.hideKeyboard(activity) }
        view?.post(hideKeyboard)
        dataBarPresenter?.subscribe(this)
        loadDataBar(currentTeam)

        // show shadow below data bar if user has not opened data menu before
        displayDataBarElevation()

    }

    override fun onStop() {
        super.onStop()
        d("onStop() called ${currentTeam?.teamId}, ${currentTeam?.statsTeamID}")
        dataBarPresenter?.unsubscribe()
        if (databarDisposable != null) {
            compositeDisposable.remove(databarDisposable!!)
            databarDisposable = null
        }
        stopBlinkText()
    }
    // endregion

    // region Data Menu
    fun onDataMenuOpened() {
        if (dataMenuIsOpen) {
            return
        }
        dataMenuIsOpen = true
        displayDataBarElevation()
    }


    fun onDataMenuClosed() {
        if (!dataMenuIsOpen) {
            return
        }
        dataMenuIsOpen = false
        displayDataBarElevation()
    }


    // If the user has never opened the data menu, show shadow to
    // indicate that data bar can be dragged down to open data menu
    fun displayDataBarElevation() {
        if (databar_layout == null) {
            return; }
        if (!dataMenuEnabled || FtueUtil.hasOpenedDataMenu()) {
            databar_layout.elevation = DATA_BAR_DEFAULT_ELEVATION
            if (team_view_mvpd_bar != null) {
                team_view_mvpd_bar.elevation = DATA_BAR_DEFAULT_ELEVATION
            }
        } else {
            // 1. If going to show data bar shadow for ftue, uncomment this piece of code
//            databar_layout.elevation = DATA_BAR_SHADOW_ELEVATION
//            if (team_view_mvpd_bar != null) {
//                team_view_mvpd_bar.elevation = DATA_BAR_SHADOW_ELEVATION
//            }
            // 2. If going to show data bar shadow for ftue, remove this piece of code
            databar_layout.elevation = DATA_BAR_DEFAULT_ELEVATION
            if (team_view_mvpd_bar != null) {
                team_view_mvpd_bar.elevation = DATA_BAR_DEFAULT_ELEVATION
            }
        }
    }

    /**
     * This method is used to close data menu if opened
     *
     * 1. Perform action down to reset all variables
     * 2. Perform action up to close data menu
     */
    fun closeDataMenu() {
        val motionEventDown = MotionEvent.obtain(SystemClock.uptimeMillis(),
                SystemClock.uptimeMillis(),
                MotionEvent.ACTION_DOWN,
                databar_layout?.x ?: 0f,
                databar_layout?.y ?: 0f,
                0)
        dataBarTouchListener?.onTouch(databar_layout, motionEventDown)
        val motionEventUp = MotionEvent.obtain(SystemClock.uptimeMillis(),
                SystemClock.uptimeMillis(),
                MotionEvent.ACTION_UP,
                databar_layout?.x ?: 0f,
                databar_layout?.y ?: 0f,
                0)
        dataBarTouchListener?.onTouch(databar_layout, motionEventUp)
    }

    /**
     * This method is used to reset activity's orientation base on conditions
     * Note: by calling setPortraitOnly() with deviceIsLockedToPortrait()
     *       it can handle both system's orientation setting and persistentPlayer's status
     *       at the same time
     *       especially:
     *           1. mini player -> able to landscape -> open data menu -> video finished
     *              -> close data menu -> should be portrait only
     *           2. medium player -> able to landscape -> open data menu
     *              -> disable system's auto rotate setting -> close data menu
     *              -> should be portrait only -> open data menu
     *              -> enable system's auto rotate setting -> close data menu
     *              -> should be able to landscape
     */
    fun resetOrientationAfterDataMenuIsClosed() {
        activity?.let {
            persistentPlayer?.setPortraitOnly(MainPresenter.deviceIsLockedToPortrait(it))
        }
    }

    // endregion

    // region Fab Button
    fun closeAllPages() {
        if (!isAdded) return
        val fragment = childFragmentManager.findFragmentById(R.id.team_view_content)
        if (fragment is TeamFeedFragment) {
            fragment.closeAllPages(true)
        }
    }

    /**
     * This method is used to check
     *
     * 1. If at least one editorial details view is opened
     *    If so, return true
     *    Otherwise, return false
     */
    fun hasAtLeastOneStackableViewOpen(): Boolean {
        if (!isAdded) return false
        val fragment = childFragmentManager.findFragmentById(R.id.team_view_content)
        return fragment is TeamFeedFragment && fragment.hasAtLeastOneStackableViewOpened()
    }

    /**
     * This method is used to scroll the team feed to the top
     */
    fun scrollTeamFeedToTheTop() {
        if (!isAdded) return
        val fragment = childFragmentManager.findFragmentById(R.id.team_view_content)
        if (fragment is TeamFeedFragment) {
            fragment.scrollTeamFeedToTheTop(true)
        }
    }

    /**
     * This function checks if there is available editorial fragment in the back stack and remove it
     * from the back stack if available,
     * <p>
     * Return true if there is a editorial page to close
     * <p>
     * Return false otherwise
     *
     * @return
     */
    fun closeLastPage(): Boolean {
        if (!isAdded) return false
        val fragment = childFragmentManager.findFragmentById(R.id.team_view_content)
        if (fragment is TeamFeedFragment && fragment.childFragmentManager.backStackEntryCount > 0) {
            fragment.closePage(true)
            return true
        }
        return false
    }

    // endregion

    // region Data Bar
    private var databarDisposable: Disposable? = null

    private fun loadDataBar(team: Team?) {
        val statsTeamId = team?.statsTeamID
        val leagueName = team?.league?.toLowerCase()

        when (leagueName) {
            "mlb" -> sportLeague = SportLeague.MLB
            "nba" -> sportLeague = SportLeague.NBA
            "nfl" -> sportLeague = SportLeague.NFL
            "nhl" -> sportLeague = SportLeague.NHL
        }
        if (sportLeague == null) return

        d("statsTeamId: $statsTeamId, sportLeague: $sportLeague, sportName: ${sportLeague?.sportName}")
        if (context == null || statsTeamId == null || statsTeamId < 0) return

        if (databarDisposable == null) {
            databarDisposable = (activity as MainActivity).dataBarManager
                    .getTeamSubject(statsTeamId = statsTeamId)
                    ?.let { subject: BehaviorSubject<RotoDataBar?> ->
                        d("subscribing to subject $statsTeamId")
                        subject.subscribe { dataBarPresenter?.handleData(it) }
                    }
            compositeDisposable.add(databarDisposable!!)
        }
    }

    override fun hideDataBar() {
        this.gameState = null
        databar_layout?.visibility = View.GONE
    }

    override fun showDataBar() {
        this.databarInfo?.let {
            if (it.isSeasonActive()) {
                dataBarPresenter?.showEvent(it)
            } else {
                showOffseasonState(it)
            }
        }
    }

    private fun showLogoAndCity(databar: RotoDataBar) {
        databar_city_name_team_away.text = databar.awayAbbr
        databar_city_name_team_home.text = databar.homeAbbr

        val teamManager = (activity as MainActivity).teamManager
        val awayLogoPath = teamManager.getLogoUrl(databar.awayGlobalId)
        val homeLogoPath = teamManager.getLogoUrl(databar.homeGlobalId)

        if (awayLogoPath?.isNotEmpty() == true) {
            Picasso.get().load(awayLogoPath)
                    .error(R.drawable.ic_peacock_square)
                    .resizeDimen(R.dimen.fab_logo_max_size, R.dimen.fab_logo_max_size)
                    .centerInside()
                    .into(databar_logo_team_away)
        }
        if (homeLogoPath?.isNotEmpty() == true) {
            Picasso.get().load(homeLogoPath)
                    .error(R.drawable.ic_peacock_square)
                    .resizeDimen(R.dimen.fab_logo_max_size, R.dimen.fab_logo_max_size)
                    .centerInside()
                    .into(databar_logo_team_home)
        }
    }

    // region Show Events
    override fun showGameDayState(databar: RotoDataBar, gameState: GameState) {
        this.gameState = gameState
        this.databarInfo = databar

        if (isCurrentSteppedFragment()) {
            databar_layout.visibility = View.GONE
            return
        }

        databar_layout.visibility = View.VISIBLE
        mlb_runners_on_base.visibility = View.INVISIBLE

        databar_score_team_home.visibility = View.INVISIBLE
        databar_score_team_away.visibility = View.INVISIBLE
        databar_game_segment.visibility = View.INVISIBLE
        databar_flex.visibility = View.INVISIBLE
        databar_detail.visibility = View.INVISIBLE

        databar_possession_indicator_team_away.visibility = View.INVISIBLE
        databar_possession_indicator_team_home.visibility = View.INVISIBLE

        databar_bottom_bar.visibility = View.GONE

        showLogoAndCity(databar)

        // Add blink text for "Live In-App"
        val tvStationText = DataBarUtil.getTvStations(databar.stationCallLetters)
        if (liveInAppDisposable == null || liveInAppDisposable?.isDisposed == true) {
            liveInAppDisposable = Observable
                    .interval(0, 5, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
                    .subscribe {
                        databar_flex.visibility = View.VISIBLE
                        databar_flex.text = when (persistentPlayer?.state) {
                            PlayerConstants.State.SHOWING_AUTHENTICATED, PlayerConstants.State.SHOWING_NOT_AUTHENTICATED -> {
                                if (databar_flex.text.toString().equals(tvStationText, ignoreCase = true)) {
                                    if (LocalizationManager.isInitialized()) {
                                        LocalizationManager.DataBar.LiveInApp
                                    } else {
                                        getString(R.string.live_in_app)
                                    }
                                } else {
                                    tvStationText
                                }
                            }
                            else -> tvStationText
                        }
                    }
        }


        // Set 24-hour or AM/PM format
        val eventStartTime = getEventStartTime(databar)
        val formattedStartTime = if (DateFormat.is24HourFormat(context)) {
            eventStartTime.toString(GAME_TIME_FORMAT_24HR)
        } else {
            eventStartTime.toString(GAME_TIME_FORMAT_AMFM)
        }

        // Show game time or status
        when (gameState) {
            GameState.PRE_GAME -> {
                databar_game_clock.visibility = View.VISIBLE
                databar_game_clock.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                databar_game_clock.text = if (LocalizationManager.isInitialized() && databar.isTBA) {
                    "${LocalizationManager.DataBar.Today} ${LocalizationManager.DataMenu.ScheduleTBD}"
                } else if (LocalizationManager.isInitialized() && !databar.isTBA) {
                    "${LocalizationManager.DataBar.Today} $formattedStartTime"
                } else if (!LocalizationManager.isInitialized() && databar.isTBA) {
                    "${getString(R.string.databar_today)} ${getString(R.string.schedule_summary_tbd)}"
                } else {
                    "${getString(R.string.databar_today)} $formattedStartTime"
                }.toUpperCase()
            }
            GameState.POSTPONED -> {
                databar_game_clock.visibility = View.VISIBLE
                databar_game_clock.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                databar_game_clock.text = if (LocalizationManager.isInitialized()) {
                    LocalizationManager.DataBar.Postponed
                } else {
                    getString(R.string.game_postponed)
                }.toUpperCase()
            }
            else -> databar_game_clock.visibility = View.INVISIBLE
        }
    }

    @SuppressLint("DefaultLocale")
    override fun showLiveGameState(databar: RotoDataBar, gameState: GameState) {
        this.gameState = gameState
        this.databarInfo = databar

        if (isCurrentSteppedFragment()) {
            databar_layout.visibility = View.GONE
            return
        }

        stopBlinkText()

        databar_layout.visibility = View.VISIBLE

        databar_game_segment.visibility = View.VISIBLE
        databar_flex.visibility = View.GONE
        databar_detail.visibility = View.VISIBLE

        databar_bottom_bar.visibility = View.GONE

        showLogoAndCity(databar)
        showLiveScore(databar)
    }

    override fun showPostGameState(databar: RotoDataBar, gameState: GameState) {
        this.gameState = gameState
        this.databarInfo = databar

        if (isCurrentSteppedFragment()) {
            databar_layout.visibility = View.GONE
            return
        }

        stopBlinkText()

        databar_layout.visibility = View.VISIBLE
        mlb_runners_on_base.visibility = View.INVISIBLE

        databar_game_segment.visibility = View.GONE
        databar_flex.visibility = View.VISIBLE
        databar_detail.visibility = View.GONE

        databar_possession_indicator_team_away.visibility = View.INVISIBLE
        databar_score_team_away.visibility = View.VISIBLE
        databar_game_clock.visibility = View.GONE
        databar_score_team_home.visibility = View.VISIBLE
        databar_possession_indicator_team_home.visibility = View.INVISIBLE

        databar_bottom_bar.visibility = View.GONE

        showLogoAndCity(databar)

        // Show score
        if (sportLeague === SportLeague.MLB) {
            databar_score_team_away.text = databar.awayRuns.toString()
            databar_score_team_home.text = databar.homeRuns.toString()
        } else {
            databar_score_team_away.text = databar.awayScore.toString()
            databar_score_team_home.text = databar.homeScore.toString()
        }

        // Show Game State
        if (LocalizationManager.isInitialized()) {
            databar_flex.text = when (gameState) {
                GameState.SUSPENDED -> LocalizationManager.DataBar.Suspended
                GameState.CANCELLED -> LocalizationManager.DataBar.Cancelled
                else -> LocalizationManager.DataBar.FinalScore
            }
        } else {
            databar_flex.text = when (gameState) {
                GameState.SUSPENDED -> getString(R.string.game_suspended)
                GameState.CANCELLED -> getString(R.string.game_cancelled)
                else -> getString(R.string.final_score)
            }
        }
    }

    override fun showUpcomingGameState(databar: RotoDataBar, gameState: GameState) {
        this.gameState = gameState
        this.databarInfo = databar

        if (isCurrentSteppedFragment()) {
            databar_layout.visibility = View.GONE
            return
        }

        stopBlinkText()

        databar_layout.visibility = View.VISIBLE
        mlb_runners_on_base.visibility = View.INVISIBLE

        databar_game_segment.visibility = View.GONE
        databar_flex.visibility = View.VISIBLE
        databar_detail.visibility = View.GONE

        databar_possession_indicator_team_away.visibility = View.INVISIBLE
        databar_score_team_away.visibility = View.INVISIBLE
        databar_game_clock.visibility = View.VISIBLE
        databar_score_team_home.visibility = View.INVISIBLE
        databar_possession_indicator_team_home.visibility = View.INVISIBLE

        databar_bottom_bar.visibility = View.GONE

        showLogoAndCity(databar)
        val tvStationText = DataBarUtil.getTvStations(databar.stationCallLetters)

        databar_flex.text = tvStationText

        // Set 24-hour or AM/PM format
        val eventStartTime = getEventStartTime(databar)
        val formattedStartTime = if (DateFormat.is24HourFormat(context) && !databar.isTBA) {
            eventStartTime.toString(NEXT_GAME_FORMAT_24HR)
        } else if (!DateFormat.is24HourFormat(context) && !databar.isTBA) {
            eventStartTime.toString(NEXT_GAME_FORMAT_AMFM)
        } else {
            eventStartTime.toString(NEXT_GAME_FORMAT_DATE)
        }

        databar_game_clock.text = if(LocalizationManager.isInitialized() && databar.isTBA) {
            "$formattedStartTime, ${LocalizationManager.DataMenu.ScheduleTBD}"
        } else if (!LocalizationManager.isInitialized() && databar.isTBA) {
            "$formattedStartTime, ${getString(R.string.schedule_summary_tbd)}"
        } else {
            formattedStartTime
        }.toUpperCase()
    }

    override fun showOffseasonState(databar: RotoDataBar) {
        this.gameState = GameState.OFF_SEASON
        this.databarInfo = databar

        if (isCurrentSteppedFragment()) {
            databar_layout.visibility = View.GONE
            return
        }

        stopBlinkText()
        databar_layout.visibility = View.VISIBLE
        mlb_runners_on_base.visibility = View.GONE

        // we're not showing score
        databar_score_team_home.visibility = View.INVISIBLE
        databar_score_team_away.visibility = View.INVISIBLE

        databar_game_segment.visibility = View.INVISIBLE
        databar_game_segment.text = ""
        databar_flex.visibility = View.VISIBLE

        databar_flex.text = "${databar.season} Record"
        databar_detail.visibility = View.INVISIBLE
        databar_detail.text = ""

        databar_possession_indicator_team_away.visibility = View.INVISIBLE
        databar_possession_indicator_team_home.visibility = View.INVISIBLE
        databar_bottom_bar.visibility = View.GONE

        // set left side text and icon
        databar_city_name_team_away.text = databar.teamAbbr
        val logoPath = currentTeam?.let {
            (activity as MainActivity).teamManager.getLogoUrl(it.statsTeamID)
        }

        logoPath?.let {
            Picasso.get().load(it)
                    .error(R.drawable.ic_peacock_square)
                    .resizeDimen(R.dimen.fab_logo_max_size, R.dimen.fab_logo_max_size)
                    .centerInside()
                    .into(databar_logo_team_away)
        }

        // set right side text and icon
        databar_city_name_team_home.text = currentTeam?.league
        databar_logo_team_home.setImageResource(when (currentTeam?.league?.toLowerCase()) {
            "mlb" -> R.drawable.mlb
            "nba" -> R.drawable.nba
            "nfl" -> R.drawable.nfl
            "nhl" -> R.drawable.nhl
            else -> 0
        })

        // set team record text in 'clock'
        val record = Record(wins = databar.wins, losses = databar.losses, ties = databar.ties, otl = databar.otLosses, overtimeLosses = databar.otLosses)
        val recordString = sportLeague?.let {
            DataBarUtil.getRecordForTeam(record = record, isDataBar = true, isDataMenuCarousel = false, isCarouselNFL = it.leagueName.equals("nfl", ignoreCase = true))
        } ?: ""
        databar_game_clock.setTextSize(TypedValue.COMPLEX_UNIT_SP, getClockSize())
        databar_game_clock.visibility = View.VISIBLE
        databar_game_clock.text = recordString
    }

    // endregion

    private fun updateGameSegment(databar: RotoDataBar?) {
        if (databar == null) return

        val period = databar.period
        val gameSegment: String = when (sportLeague) {
            SportLeague.MLB -> {
                // Handle top and bottom of inning
                val inningDivisionId = if (databar.isBottomHalf) R.drawable.mlb_inning_bottom else R.drawable.mlb_inning_top
                val inningDrawable = ResourcesCompat.getDrawable(resources, inningDivisionId, null)
                databar_game_segment.compoundDrawablePadding = DisplayUtils.dpToPx(2)
                databar_game_segment.setCompoundDrawablesWithIntrinsicBounds(inningDrawable, null, null, null)

                // Show inning
                StringUtils.getOrdinalString(databar.period)
            }
            SportLeague.NHL -> {
                if (period > 3) {
                    if (LocalizationManager.isInitialized()) LocalizationManager.DataBar.Overtime else "OT"
                } else {
                    StringUtils.getOrdinalString(period)
                }
            }
            SportLeague.NFL, SportLeague.NBA -> {
                if (period > 4) {
                    if (LocalizationManager.isInitialized()) LocalizationManager.DataBar.Overtime else "OT"
                } else {
                    if (LocalizationManager.isInitialized()) {
                        "${StringUtils.getOrdinalString(period)} ${LocalizationManager.DataBar.Quarter}"
                    } else {
                        "${StringUtils.getOrdinalString(period)} QTR"
                    }
                }
            }
            else -> ""
        }

        databar_game_segment.text = gameSegment
        databar_game_segment.visibility = View.VISIBLE
    }

    @SuppressLint("DefaultLocale")
    private fun updateDatabarDetail(databar: RotoDataBar?) {
        if (databar == null) return

        when (sportLeague) {
            SportLeague.MLB -> {
                // Show outs
                val numOuts = databar.outs
                val outString: String = if (LocalizationManager.isInitialized()) {
                    when (numOuts) {
                        1 -> "$numOuts ${LocalizationManager.DataBar.Out}"
                        else -> "$numOuts ${LocalizationManager.DataBar.Outs}"
                    }
                } else {
                    resources.getQuantityString(R.plurals.num_outs, numOuts, numOuts)
                }

                databar_detail.apply {
                    text = outString
                    visibility = View.VISIBLE
                }
            }
            SportLeague.NFL -> {
                // Downs and yardage
                val downs = StringUtils.getOrdinalString(databar.down)
                val distance = databar.distance
                databar_detail.apply {
                    text = String.format("%s & %d", downs, distance)
                    visibility = View.VISIBLE
                }
            }
            SportLeague.NHL -> {
                var arrow: Drawable? = null
                var transparency = 0.2f

                if (databar.isAwayPowerplay()) {
                    arrow = ResourcesCompat.getDrawable(resources, R.drawable.ic_black_triangle_left, null)
                    transparency = 0.5f

                } else if (databar.isHomePowerplay()) {
                    arrow = ResourcesCompat.getDrawable(resources, R.drawable.ic_black_triangle_right, null)
                    transparency = 0.5f
                }

                // Power-Play
                databar_detail.apply {
                    text = if (LocalizationManager.isInitialized()) LocalizationManager.DataBar.PowerPlay else getString(R.string.powerplay)
                    setTextColor(ContextCompat.getColor(context, R.color.black))
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                    compoundDrawablePadding = DisplayUtils.dpToPx(4)
                    setCompoundDrawablesWithIntrinsicBounds(arrow, null, null, null)
                    alpha = transparency
                    visibility = View.VISIBLE
                }
            }
            SportLeague.NBA -> databar_detail.visibility = View.GONE
        }
    }

    @SuppressLint("DefaultLocale")
    private fun showLiveScore(databar: RotoDataBar) {
        if (context == null) return

        val awayScore: String
        val homeScore: String

        updateDatabarDetail(databar)
        updateGameSegment(databar)

        // Handle scores
        if (sportLeague === SportLeague.MLB) {
            // Show runs
            awayScore = databar.awayRuns.toString()
            homeScore = databar.homeRuns.toString()

            // Show base runner
            showBaseRunners(databar)
        } else {
            awayScore = databar.awayScore.toString()
            homeScore = databar.homeScore.toString()

            mlb_runners_on_base.visibility = View.GONE
        }
        databar_score_team_away.text = awayScore
        databar_score_team_away.visibility = View.VISIBLE

        databar_score_team_home.text = homeScore
        databar_score_team_home.visibility = View.VISIBLE


        // Handle NFL specific indicators
        if (sportLeague === SportLeague.NFL) {
            // Possession indicator
            when (databar.posessionTeamGlobalId) {
                databar.awayGlobalId -> {
                    databar_possession_indicator_team_away.visibility = View.VISIBLE
                    databar_possession_indicator_team_home.visibility = View.INVISIBLE
                }
                databar.homeGlobalId -> {
                    databar_possession_indicator_team_away.visibility = View.INVISIBLE
                    databar_possession_indicator_team_home.visibility = View.VISIBLE
                }
                else -> {
                    databar_possession_indicator_team_away.visibility = View.INVISIBLE
                    databar_possession_indicator_team_home.visibility = View.INVISIBLE
                }
            }

            // away timeouts
            databar.awayTimeouts?.let {
                away_timeout_1.setImageResource(if (it >= 1) R.drawable.nfl_timeout else R.drawable.nfl_no_timeout)
                away_timeout_2.setImageResource(if (it >= 2) R.drawable.nfl_timeout else R.drawable.nfl_no_timeout)
                away_timeout_3.setImageResource(if (it >= 3) R.drawable.nfl_timeout else R.drawable.nfl_no_timeout)
            }

            // home timeouts
            databar.homeTimeouts?.let {
                home_timeout_1.setImageResource(if (it >= 1) R.drawable.nfl_timeout else R.drawable.nfl_no_timeout)
                home_timeout_2.setImageResource(if (it >= 2) R.drawable.nfl_timeout else R.drawable.nfl_no_timeout)
                home_timeout_3.setImageResource(if (it >= 3) R.drawable.nfl_timeout else R.drawable.nfl_no_timeout)
            }

            databar_bottom_bar.visibility = View.VISIBLE
        } else {
            databar_bottom_bar.visibility = View.GONE
            databar_possession_indicator_team_away.visibility = View.GONE
            databar_possession_indicator_team_home.visibility = View.GONE
        }

        databar_game_clock.setTextSize(TypedValue.COMPLEX_UNIT_SP, getClockSize())

        // Handle Game Clock
        when (gameState) {
            GameState.IN_PROGRESS -> run {
                if (sportLeague === SportLeague.MLB) {
                    databar_game_clock.visibility = View.GONE
                } else {
                    when (persistentPlayer?.state) {
                        PlayerConstants.State.SHOWING_AUTHENTICATED ->
                            // LivePP state only if authenticated & authorized
                            databar_game_clock.visibility = View.GONE
                        else -> {
                            // show the game clock
                            if (sportLeague === SportLeague.NBA && databar.gameClockSeconds != null && databar.gameClockSeconds in 0f..10f) {
                                // NBA should show decimal seconds if available below 10 seconds
                                databar_game_clock.text = databar.gameClockSeconds.toString()
                            } else {
                                databar_game_clock.text = databar.gameClock
                            }
                            databar_game_clock.visibility = View.VISIBLE
                        }
                    }
                }
                return@run
            }
            GameState.DELAYED -> run {
                databar_game_clock.text = if (LocalizationManager.isInitialized()) {
                    LocalizationManager.DataBar.Delayed
                } else {
                    getString(R.string.game_delayed)
                }
                databar_game_clock.visibility = View.VISIBLE
                return@run
            }
            else -> databar_game_clock.visibility = View.GONE
        }
    }

    private fun showBaseRunners(databar: RotoDataBar?) {
        if (databar?.baseRunners == null || gameState != GameState.IN_PROGRESS) {
            mlb_runners_on_base.visibility = View.GONE
            return
        }

        val resId = when (databar.baseRunners) {
            1 -> R.drawable.mlb_1st  // 1st
            2 -> R.drawable.mlb_2nd  // 2nd
            3 -> R.drawable.mlb_1st_2nd  // 1st & 2nd
            4 -> R.drawable.mlb_3rd  // 3rd
            5 -> R.drawable.mlb_1st_3rd // 1st & 3rd
            6 -> R.drawable.mlb_2nd_3rd // 2nd & 3rd
            7 -> R.drawable.mlb_bases_loaded // loaded
            else -> R.drawable.mlb_based_empty // no runners
        }

        mlb_runners_on_base.apply {
            setImageResource(resId)
            visibility = View.VISIBLE
        }
    }

    private fun stopBlinkText() {
        liveInAppDisposable?.dispose()
        liveInAppDisposable = null
    }

    private fun getEventStartTime(databar: RotoDataBar): DateTime {
        val localZone = DateTime().zone
        return DateTime(databar.gameDateTimeUTC, DateTimeZone.UTC).withZone(localZone)
    }

    override fun setPresenter(presenter: DataBarContract.Presenter) {
        dataBarPresenter = presenter
    }

    override fun onDataBarScroll(dx: Int, dy: Int) {
        val maxHeight = 350f  // arbitrary height for scrolling, this can be changed
        val percent = (maxHeight - dy) / maxHeight

        when {
            percent in 0f..1f -> setDataBarSize(percent)
            percent < 0f -> setDataBarSize(0f)
            else -> setDataBarSize(1f)
        }
    }

    private fun setDataBarSize(percent: Float) {
        // Remove databar_top_bar.measure(0, 0), it is because every time
        // when databar_layout change visibility, the child layout of databar_layout
        // will show in wrong position when using databar_top_bar.measure(0, 0)
        //databar_top_bar.measure(0, 0)
        val barHeight = DisplayUtils.pxToDp(databar_top_bar.height)

        // get logo size
        val minLogoSize = resources.getDimension(R.dimen.fab_logo_min_size)
        val maxLogoSize = resources.getDimension(R.dimen.fab_logo_max_size)
        val logoSize = ((maxLogoSize - minLogoSize) * percent + minLogoSize).toInt()

        // away logo
        databar_logo_team_away.layoutParams.apply {
            width = logoSize
            height = logoSize
        }

        // home logo
        databar_logo_team_home.layoutParams.apply {
            width = logoSize
            height = logoSize
        }

        // Set Fading position for top row text
        val fadeStart = 0.7f
        val alphaPercent = if (percent < fadeStart) 0f else (percent - fadeStart) * (1f / (1f - fadeStart))

        // resize, it is possible that databar_top_bar.height or databar_top_bar.measuredHeight
        // is equal to 0 at the very beginning of databar visibility changing
        val paddingHeight: Int = when (barHeight) {
            0 -> 78
            else -> max(Math.round(DisplayUtils.dpToPx(barHeight) * percent), 1)
        }

        (databar_top_padding.layoutParams as ConstraintLayout.LayoutParams).apply {
            height = paddingHeight
        }

        if (percent <= 0f) {
            databar_city_name_team_away.visibility = View.GONE
            databar_city_name_team_home.visibility = View.GONE

            if (gameState != null) {
                when (gameState) {
                    GameState.CANCELLED, GameState.FINAL, GameState.SUSPENDED -> databar_flex.alpha = 1f
                    else -> databar_flex.alpha = 0f
                }
            }

        } else if (percent > 0f && percent <= 1f) {
            databar_city_name_team_away.alpha = alphaPercent
            databar_city_name_team_home.alpha = alphaPercent

            // resize scores: min=20sp, max=30sp
            scoreSize = 20 + percent * 10
            databar_score_team_away.setTextSize(TypedValue.COMPLEX_UNIT_SP, scoreSize)
            databar_score_team_home.setTextSize(TypedValue.COMPLEX_UNIT_SP, scoreSize)

            // Handle text sizes based on Game State
            when (gameState) {
                GameState.PRE_GAME, GameState.POSTPONED, GameState.DELAYED, GameState.IN_PROGRESS, GameState.OFF_SEASON -> {
                    databar_flex.alpha = alphaPercent
                }
                GameState.SUSPENDED, GameState.CANCELLED, GameState.FINAL -> {
                    databar_flex.alpha = 1f
                }
            }

            databar_game_clock.setTextSize(TypedValue.COMPLEX_UNIT_SP, getClockSize())

            // update visibility
            databar_city_name_team_away.visibility = View.VISIBLE
            databar_city_name_team_home.visibility = View.VISIBLE
        }
    }

    fun setDataBarColor(percent: Float) {
        // Set background layer alpha
        databar_root_background_layer_relative_layout?.apply {
            alpha = percent
        }
        // Set city name of away team text color
        databar_city_name_team_away?.apply {
            when {
                percent < 0.5f -> setTextColor(ContextCompat.getColor(context, R.color.black50))
                else -> setTextColor(ContextCompat.getColor(context, R.color.white50))
            }
        }
        // Set game segment text color
        databar_game_segment?.apply {
            when {
                percent < 0.5f -> setTextColor(ContextCompat.getColor(context, R.color.black50))
                else -> setTextColor(ContextCompat.getColor(context, R.color.white50))
            }
        }
        // Set data bar flex text color
        databar_flex?.apply {
            when {
                percent < 0.5f -> setTextColor(ContextCompat.getColor(context, R.color.black50))
                else -> setTextColor(ContextCompat.getColor(context, R.color.white50))
            }
        }
        // Set data bar detail text color
        databar_detail?.apply {
            when {
                percent < 0.5f -> setTextColor(ContextCompat.getColor(context, R.color.black50))
                else -> setTextColor(ContextCompat.getColor(context, R.color.white50))
            }
        }
        // Set city name of home team text color
        databar_city_name_team_home?.apply {
            when {
                percent < 0.5f -> setTextColor(ContextCompat.getColor(context, R.color.black50))
                else -> setTextColor(ContextCompat.getColor(context, R.color.white50))
            }
        }
        // Set score of away team text color
        databar_score_team_away?.apply {
            when {
                percent < 0.5f -> setTextColor(ContextCompat.getColor(context, R.color.black))
                else -> setTextColor(ContextCompat.getColor(context, R.color.white))
            }
        }
        // Set game clock text color
        databar_game_clock?.apply {
            when {
                percent < 0.5f -> setTextColor(ContextCompat.getColor(context, R.color.black))
                else -> setTextColor(ContextCompat.getColor(context, R.color.white))
            }
        }
        // Set score of home team text color
        databar_score_team_home?.apply {
            when {
                percent < 0.5f -> setTextColor(ContextCompat.getColor(context, R.color.black))
                else -> setTextColor(ContextCompat.getColor(context, R.color.white))
            }
        }
    }

    /**
     * This method is used to check if data bar is showing off season content
     */
    fun isDataBarGameStateOffseason(): Boolean {
        return gameState == GameState.OFF_SEASON
    }

    private fun getClockSize(): Float {
        return when (gameState) {
            GameState.IN_PROGRESS -> scoreSize
            GameState.DELAYED -> 16f
            else -> 16f
        }
    }

    // endregion

    // region MVPD
    private fun checkAuthAndShowMvpdHeader() {
        streamAuthenticationPresenter?.checkAuthNStatus(
                PreferenceUtils.getString(StreamAuthenticationPresenter.LAST_SUCCESSFUL_MVPD, ""),
                object : DisposableObserver<Auth>() {
                    override fun onNext(t: Auth) {
                        d("streamauthentication checkAuthNStatus logged in mvpd:${t.authNToken?.mvpd}, team:${currentTeam?.displayName}")

                        if (t.authNToken?.mvpd.equals(streamAuthenticationPresenter!!.config.adobePass.tempPassProvider, ignoreCase = true)) {
                            streamAuthSuccess = false
                            hideMvpdHeader()
                        } else {
                            streamAuthSuccess = true
                            displayMvpdHeader(t)
                        }
                    }

                    override fun onError(e: Throwable) {
                        d("streamauthentication checkAuthNStatus logged out")
                        streamAuthSuccess = false
                        hideMvpdHeader()
                    }

                    override fun onComplete() {

                    }
                })
    }

    private fun displayMvpdHeader(auth: Auth) {
        if (team_view_mvpd_bar == null || auth.teamViewLogoUrl.isNullOrEmpty()) return
        team_view_mvpd_bar.setBackgroundColor(Color.WHITE)

        Picasso.get()
                .load(auth.teamViewLogoUrl)
                .into(team_view_mvpd_bar_logo, object : com.squareup.picasso.Callback {
                    override fun onSuccess() {
                        if (!isCurrentEditorialDetailFragment() && !isCurrentSteppedFragment()) {
                            showMvpdHeader()
                        }
                    }

                    override fun onError(e: Exception) {
                        e.printStackTrace()
                        hideMvpdHeader()
                    }
                })

        if (auth.mvpdRedirectUrl.isNullOrEmpty()) {
            NotificationsManagerKt.showServerError()
            return
        }

        compositeDisposable.add(RxView.clicks(team_view_mvpd_bar_logo)
                .debounce(500L, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { _ ->
                    if (!DataMenuUtils.DATA_MENU_IS_OPENED) {
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.data = Uri.parse(auth.mvpdRedirectUrl)
                        context?.startActivity(intent)
                    }
                })
    }

    /**
     * This method is used to hide MVPD logo on team view
     */
    fun hideMvpdHeader() {
        if (team_view_mvpd_bar != null) {
            team_view_mvpd_bar.clearAnimation()
            team_view_mvpd_bar.animation = transitionAnimation(team_view_mvpd_bar, R.anim.header_fade_out, false)
        }
    }

    /**
     * This method is used to show MVPD logo on team view
     */
    fun showMvpdHeader() {
        if (streamAuthSuccess && team_view_mvpd_bar != null) {
            team_view_mvpd_bar.clearAnimation()
            team_view_mvpd_bar.animation = transitionAnimation(team_view_mvpd_bar, R.anim.header_fade_in, true)
        }
    }

    // endregion

    fun transitionAnimation(headerView: View, headerAnimationId: Int, isShowing: Boolean): Animation {
        val headerAniamtion = AnimationUtils.loadAnimation(context, headerAnimationId)

        headerAniamtion.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {

            }

            override fun onAnimationEnd(animation: Animation) {
                if (isShowing) {
                    headerView.visibility = View.VISIBLE
                } else {
                    headerView.visibility = View.GONE
                }
            }

            override fun onAnimationRepeat(animation: Animation) {

            }
        })
        return headerAniamtion
    }

    override fun onLiveAssetAdded(liveAsset: Asset) {
        if (streamAuthenticationPresenter?.config != null) {
            checkAuthAndShowMvpdHeader()
        }
    }

    override fun resetMvpdLogo() {
        streamAuthSuccess = false
        hideMvpdHeader()

        // Checks whether to show the Rate Dialog as long as LivePP is not active
        if (activity != null
                && currentTeam == TeamManager.getInstance()?.getSelectedTeam()
                && persistentPlayer?.state == PlayerConstants.State.NOT_SHOWING
                && PreferenceUtils.getString(_PREF_KEY_SHOW_RATE_DIALOG_STATE, "").equals(SHOW_RATE_DIALOG_STATE.YES.name, ignoreCase = true)) {
            (activity as MainActivity).showRateReviewDialog()
        }
    }

    // region Analytics
    fun trackPage() {
        TrackingHelper.trackPageEvent(pageInfo)
    }

    override fun getPageInfo(): PageInfo {
        return PageInfo(
                contextData = false,
                team = currentTeam?.displayName ?: "",
                league = currentTeam?.league ?: ""
        )
    }

    // endregion
}
