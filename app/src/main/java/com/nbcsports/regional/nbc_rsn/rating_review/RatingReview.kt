package com.nbcsports.regional.nbc_rsn.rating_review

import android.app.Activity
import android.os.Build
import com.nbcsports.regional.nbc_rsn.extensions.isWithinLastXDays
import com.nbcsports.regional.nbc_rsn.utils.PreferenceUtils
import org.joda.time.DateTime
import timber.log.Timber
import kotlin.collections.ArrayList

public enum class SHOW_RATE_DIALOG_STATE {
    NO, YES, DONE
}

object RatingReview {

    private val _PREF_KEY_LAST_RATED_VERSIONCODE = "lastRatedVersionCode"
    private val _PREF_KEY_LAST_OPENING_DATES = "lastOpeningDates"

    public val _PREF_KEY_SHOW_RATE_DIALOG_STATE = "showRateDialog"

    private var numAppLaunches: Int = 8
    private var numDaysBeforePruning: Int = 14

    fun init(reviewPromptLaunchesThreshold: Int, reviewPromptDaysMaximum: Int) {
        this.numAppLaunches = reviewPromptLaunchesThreshold
        this.numDaysBeforePruning = reviewPromptDaysMaximum
    }

    /**
     * Checks whether to show the rate & review dialog.
     * <p>
     * Do not show if:
     * - stream is playing
     * - user has already seen the dialog for this version.
     * <p>
     * Show if:
     * - user has opened the app 4 times in the past two weeks.
     */
    fun shouldShowRateDialog(activity: Activity?): Boolean {
        Timber.d("shouldShowRateDialog: ${PreferenceUtils.getString(_PREF_KEY_SHOW_RATE_DIALOG_STATE, "")}")
        if (activity == null) return false

        val packageInfo = activity.packageManager.getPackageInfo(activity.packageName, 0)
        val currentVersionNumber: Long = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo.longVersionCode
        } else {
            packageInfo.versionCode.toLong()
        }

        val lastRatedVersionNumber = PreferenceUtils.getString(_PREF_KEY_LAST_RATED_VERSIONCODE, "")

        /*
            if (lastRatedVersionCode is old or empty) {
                - get list of lastOpenings
                - add current date to list
                - sort list
                - filter items older than 2 weeks and shorten to size of NUMBER_OF_APP_LAUNCHES
                - if list is >= NUMBER_OF_APP_LAUNCHES
                    - show rating
            }
             */


        val lastOpenings = PreferenceUtils.getList<Long>(_PREF_KEY_LAST_OPENING_DATES, Long::class.javaObjectType, ArrayList())
        if (lastRatedVersionNumber.isEmpty() || lastRatedVersionNumber.toLong() < currentVersionNumber) {

            // add current date
            lastOpenings.add(DateTime().millis)
            lastOpenings.sort()

            Timber.d("lastOpenings -> $lastOpenings, type=${lastOpenings::class.java}")

            // filter out entries older than NUMBER_OF_DAYS_BEFORE_PRUNING
            val recentItems = ArrayList(lastOpenings
                    .filter { DateTime().withMillis(it).isWithinLastXDays(numDaysBeforePruning) }
                    .take(numAppLaunches))

            Timber.d("recentItems -> %s", recentItems.toString())

            // write dates back to pref
            PreferenceUtils.setList<Long>(_PREF_KEY_LAST_OPENING_DATES, recentItems)

            // show dialog if threshold met and disable showing it until next version upgrade
            if (recentItems.size >= numAppLaunches) {
                PreferenceUtils.setString(_PREF_KEY_LAST_RATED_VERSIONCODE, currentVersionNumber.toString())
                return true
            }
        } else {
            Timber.d("lastRatedVersionNumber = $lastRatedVersionNumber, currentVersionNumber = $currentVersionNumber, lastOpenings = $lastOpenings")
        }

        return false
    }

    fun clearHistory(activity: Activity?) {
        Timber.d("activity -> $activity")
        if (activity == null) return

        PreferenceUtils.setList<Long>(_PREF_KEY_LAST_OPENING_DATES, ArrayList())
        PreferenceUtils.setString(_PREF_KEY_SHOW_RATE_DIALOG_STATE, "")
        PreferenceUtils.setString(_PREF_KEY_LAST_RATED_VERSIONCODE, "")

        Timber.d("Clear history -> ${PreferenceUtils.getList<Long>(_PREF_KEY_LAST_OPENING_DATES, Long::class.javaObjectType, ArrayList())}")
    }
}
