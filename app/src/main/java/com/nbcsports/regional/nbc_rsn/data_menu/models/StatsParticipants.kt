package com.nbcsports.regional.nbc_rsn.data_menu.models

import android.os.Parcelable
import com.nbcsports.regional.nbc_rsn.data_bar.StatsDate
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class StatsPlayer(
        val playerId: Int = -1,
        val firstName: String = "",
        val lastName: String = "",
        val uniform: String = "",
        val height: StatsPlayerHeight = StatsPlayerHeight(),
        val weight: StatsPlayerWeight = StatsPlayerWeight(),
        val birth: StatsPlayerBirth = StatsPlayerBirth(),
        val positions: Array<StatsPlayerPosition> = emptyArray(),
        val sequence: Int = -1
) : Parcelable, Comparable<StatsPlayer> {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as StatsPlayer

        if (playerId != other.playerId) return false
        if (firstName != other.firstName) return false
        if (lastName != other.lastName) return false
        if (uniform != other.uniform) return false
        if (height != other.height) return false
        if (weight != other.weight) return false
        if (birth != other.birth) return false
        if (!Arrays.equals(positions, other.positions)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = playerId
        result = 31 * result + firstName.hashCode()
        result = 31 * result + lastName.hashCode()
        result = 31 * result + uniform.hashCode()
        result = 31 * result + height.hashCode()
        result = 31 * result + weight.hashCode()
        result = 31 * result + birth.hashCode()
        result = 31 * result + Arrays.hashCode(positions)
        return result
    }
    
    fun getFullName(): String {
        return if (firstName.isNotEmpty() && lastName.isNotEmpty()) {
            "${firstName.take(1)}. $lastName"
        } else {
            "-"
        }
    }

    override fun compareTo(other: StatsPlayer): Int {
        // first compare last name
        return lastName.compareTo(other.lastName).let {
            // if last name is the same, compare first name
            if (it == 0) firstName.compareTo(other.firstName) else it
        }
    }
}

@Parcelize
data class StatsPlayerWeight(
        val pounds: Double = -1.0
) : Parcelable

@Parcelize
data class StatsPlayerHeight(
        val inches: Double = -1.0
) : Parcelable

@Parcelize
data class StatsPlayerBirth(
        val birthDate: StatsDate = StatsDate()
) : Parcelable

@Parcelize
data class StatsPlayerPosition(
        val positionId: Int = -1,
        val name: String = "",
        val abbreviation: String = "",
        val sequence: Int = -1
) : Parcelable

@Parcelize
data class StatsInjuryDetail(
        val player: StatsPlayer,
        val injuryDetails: Array<StatsInjuryDetailItem>
) : Parcelable

@Parcelize
data class StatsInjuryDetailItem(
        val sequence: Int,
        val information: String,
        val returnDate: StatsDate?
) : Parcelable

@Parcelize
data class StatsInjuries(
        val player: StatsPlayer,
        val description: String,
        val status: StatsInjuryStatus
) : Parcelable

@Parcelize
data class StatsInjuryStatus(
        val statusId: Int,
        val description: String
) : Parcelable
