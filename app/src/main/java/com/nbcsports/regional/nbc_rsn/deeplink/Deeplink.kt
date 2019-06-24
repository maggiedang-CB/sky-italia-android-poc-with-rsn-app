package com.nbcsports.regional.nbc_rsn.deeplink

import android.content.Intent
import android.net.Uri
import android.os.Parcelable
import com.nbcsports.regional.nbc_rsn.BuildConfig
import com.nbcsports.regional.nbc_rsn.EntryActivity
import com.nbcsports.regional.nbc_rsn.MainActivity
import com.nbcsports.regional.nbc_rsn.common.Team
import com.nbcsports.regional.nbc_rsn.common.TeamManager
import kotlinx.android.parcel.Parcelize

@Parcelize
class Deeplink (
        val team: Team,
        val componentId: String,
        val action: String,
        val type: Deeplink.Type
) : Parcelable {

    enum class Type {
        TEAM, COMPONENT
    }

    constructor(team: Team) : this(team, "", "", Type.TEAM)

    constructor(team: Team, componentId: String, action: String) :
        this(team, componentId, action, Type.COMPONENT)

    fun isAction(action: String) : Boolean {
        return action.equals(this.action, true)
    }

    fun isType(type : Type) : Boolean {
        return type == this.type
    }

    companion object {

        const val TEAM_ID_KEY       = "team-id"
        const val COMPONENT_ID_KEY  = "component-id"
        const val ACTION_KEY        = "action"
        const val ACTION_LINK_TO    = "linkto"
        const val ACTION_OPEN       = "open"

        private const val TEAM_ID_KEY_INDEX       = 0
        private const val TEAM_ID_INDEX           = 1
        private const val COMPONENT_ID_KEY_INDEX  = 2
        private const val COMPONENT_ID_INDEX      = 3
        private const val ACTION_KEY_INDEX        = 4
        private const val ACTION_INDEX            = 5

        private const val COMPONENT_PARAM_LEN   = 6
        private const val TEAM_PARAM_LEN        = 2

        /***
         * Returns Deeplink if intent is a valid deeplink intent
         * Returns null otherwise
         */
        @JvmStatic
        fun getDeeplinkFromIntent(intent : Intent?) : Deeplink? {
            if (intent == null || intent.data == null
                    || intent.data.host == null
                    || intent.data.path == null) return null

            val uri = intent.data
            val params : Array<String?> = uri.path.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (params.isNotEmpty()) {
                params[TEAM_ID_KEY_INDEX] = uri.host

                val manager = TeamManager.getInstance()
                if (isValidFullParam(params)) {
                    val team: Team? = manager?.getUserTeamByTeamId(params[TEAM_ID_INDEX]!!)
                    if (team != null) {
                        return Deeplink(team, params[COMPONENT_ID_INDEX]!!, params[ACTION_INDEX]!!)
                    }

                } else if (isValidTeamParam(params)) {
                    val team: Team? = manager?.getUserTeamByTeamId(params[TEAM_ID_INDEX]!!)
                    if (team != null) {
                        return Deeplink(team)
                    }
                }
            }

            return null
        }

        @JvmStatic
        private fun isValidFullParam(params : Array<String?>) : Boolean {
            if (params.size != COMPONENT_PARAM_LEN) return false
            for (i in params.indices){
                if (params[i].isNullOrBlank()) return false
                params[i] = params[i]!!.trim()
            }
            if (!params[TEAM_ID_KEY_INDEX].equals(TEAM_ID_KEY, true)) return false
            if (!params[COMPONENT_ID_KEY_INDEX].equals(COMPONENT_ID_KEY, true)) return false
            if (!params[ACTION_KEY_INDEX].equals(ACTION_KEY, true)) return false
            if (!params[ACTION_INDEX].equals(ACTION_LINK_TO, true)
                    && !params[ACTION_INDEX].equals(ACTION_OPEN, true)) return false
            return true
        }

        @JvmStatic
        private fun isValidTeamParam(params : Array<String?>) : Boolean {
            if (params.size != TEAM_PARAM_LEN) return false
            for (i in params.indices){
                if (params[i].isNullOrEmpty()) return false
                params[i] = params[i]!!.trim()
            }
            if (!params[TEAM_ID_KEY_INDEX].equals(TEAM_ID_KEY, true)) return false
            return true
        }

        /***
         * Returns a Deeplink that will open up the TeamView if there exists a team in User's Teams
         *  where teamValue is equal to a user team's teamId or displayName
         */
        @JvmStatic
        fun getDeeplinkForTeamView(teamValue: String?): Deeplink? {
            val teamManager = TeamManager.getInstance()

            if (teamManager == null || teamValue.isNullOrBlank()){
                return null
            }

            for (team in teamManager.usersTeams) {
                if (team.teamId.equals(teamValue, ignoreCase = true) || team.displayName.equals(teamValue, ignoreCase = true)) {
                    return Deeplink(team)
                }
            }
            return null
        }

        /***
         * Returns a Deeplink that will open the component if there exists a team in UserTeams
         *  where teamValue is equal to a user's team's teamId or displayName
         * Otherwise, returns null
         */
        @JvmStatic
        fun getDeeplinkForEditorial(teamValue: String?, componentId: String?): Deeplink? {
            val teamManager = TeamManager.getInstance()

            if (teamManager == null || teamValue.isNullOrBlank() || componentId.isNullOrBlank()) {
                return null
            }

            for (team in teamManager.usersTeams) {
                if (team.teamId.equals(teamValue, ignoreCase = true) || team.displayName.equals(teamValue, ignoreCase = true)) {
                    return Deeplink(team, componentId!!, Deeplink.ACTION_OPEN)
                }
            }
            return null
        }

        /***
         *
         * Returns an Intent that can be started to execute the Deeplink provided.
         * Returns null if the Deeplink or MainActivity is invalid (has null value)
         */
        @JvmStatic
        fun getIntentFromDeeplink(mainActivity: MainActivity?, deeplink: Deeplink?): Intent? {
            if (deeplink == null || mainActivity == null) return null

            val deeplinkIntent = Intent(mainActivity, EntryActivity::class.java)
            deeplinkIntent.action = Intent.ACTION_VIEW

            val uriBuilder = StringBuilder(BuildConfig.APPLICATION_ID) // begin with app id
            uriBuilder.append("://")
                    .append(TEAM_ID_KEY)
                    .append("/")
                    .append(deeplink.team.teamId) // completed uri for team deeplink

            val uri: String
            if (deeplink.type === Deeplink.Type.TEAM) {
                // build the uri
                uri = uriBuilder.toString()
                deeplinkIntent.data = Uri.parse(uri)

            } else {
                // add parts for component and action
                uriBuilder.append("/")
                        .append(COMPONENT_ID_KEY)
                        .append("/")
                        .append(deeplink.componentId)
                        .append("/")
                        .append(ACTION_KEY)
                        .append("/")
                        .append(deeplink.action)

                uri = uriBuilder.toString()
                deeplinkIntent.data = Uri.parse(uri)
            }
            return deeplinkIntent
        }
    }
}
