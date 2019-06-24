package com.nbcsports.regional.nbc_rsn.settings.team_news

data class TeamNewsOptStatus (
        val teamId : String,
        var teamNewsOptStatus: Boolean,
        var gameStartOptStatus: Boolean,
        var finalScoreOptStatus: Boolean
) {
    override fun equals(other: Any?): Boolean {

        other as TeamNewsOptStatus

        if (this.teamId != other.teamId) return false
        if (this.teamNewsOptStatus != other.teamNewsOptStatus) return false
        if (this.gameStartOptStatus != other.gameStartOptStatus) return false
        if (this.finalScoreOptStatus != other.finalScoreOptStatus) return false

        return true
    }

    override fun hashCode(): Int {

        var result = teamId.hashCode()

        result = 31 * result + teamNewsOptStatus.hashCode()
        result = 31 * result + gameStartOptStatus.hashCode()
        result = 31 * result + finalScoreOptStatus.hashCode()

        return result
    }
}