package com.nbcsports.regional.nbc_rsn.data_menu.roster

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import com.nbcsports.regional.nbc_rsn.R
import com.nbcsports.regional.nbc_rsn.common.TeamManager
import com.nbcsports.regional.nbc_rsn.data_menu.models.*
import com.nbcsports.regional.nbc_rsn.localization.LocalizationManager
import com.nbcsports.regional.nbc_rsn.utils.DateFormatUtils.getDaysAgo
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.roster_filter_bar.view.*
import kotlinx.android.synthetic.main.roster_player_item.view.*
import org.joda.time.DateTime
import org.joda.time.Interval
import org.joda.time.format.DateTimeFormat
import java.util.regex.Pattern

class RosterAdapter(val dataMenuRosterFragment: RosterFragment,
                    var sortOption: RosterAdapter.SortOption)
    : RecyclerView.Adapter<RosterAdapter.RosterItemViewHolder>() {

    private val VIEW_HOLDER_TYPE_FILTER_BAR = 0
    private val VIEW_HOLDER_TYPE_PLAYER_INFO = 1

    enum class SortOption { NAME, POSITION, AGE, HEIGHT, WEIGHT, JERSEY }

    private val sortOptionNames: Array<String> = if (LocalizationManager.isInitialized()) {
        arrayOf(LocalizationManager.DataMenu.RosterSortName,
                LocalizationManager.DataMenu.RosterSortPosition,
                LocalizationManager.DataMenu.RosterSortAge,
                LocalizationManager.DataMenu.RosterSortHeight,
                LocalizationManager.DataMenu.RosterSortWeight,
                LocalizationManager.DataMenu.RosterSortJersey)
    } else {
        arrayOf("")
    }

    var rosters = mutableListOf<RotoPlayer>()
        set(value) {
            field = value
            sortRoster()
            notifyDataSetChanged()
        }

    init {
        sortBy(sortOption)
    }

    fun sortBy(_sortOption: SortOption) {
        sortOption = _sortOption
        sortRoster()
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int = when (position) {
        0 -> VIEW_HOLDER_TYPE_FILTER_BAR
        else -> VIEW_HOLDER_TYPE_PLAYER_INFO
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RosterItemViewHolder {
        return if (viewType == VIEW_HOLDER_TYPE_FILTER_BAR) {
            DataMenuHeaderViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.roster_filter_bar, parent, false))
        } else { // if viewType == VIEW_HOLDER_TYPE_PLAYER_INFO
            DataMenuRosterViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.roster_player_item, parent, false))
        }
    }

    override fun onBindViewHolder(viewHolder: RosterItemViewHolder, position: Int) {
        viewHolder.bindView(position)
    }

    override fun getItemCount(): Int {
        return rosters.size + 1  // add 1 for filter bar;
    }

    inner class DataMenuHeaderViewHolder(itemView: View) : RosterItemViewHolder(itemView) {
        override fun bindView(position: Int) {}

        init {
            // set up spinner
            val spinner = itemView.findViewById<Spinner>(R.id.sort_option_selector)
            val arrayAdapter = ArrayAdapter<String>(itemView.context, R.layout.data_menu_spinner_item, sortOptionNames)
            spinner.adapter = arrayAdapter
            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {}

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val sortOptionList = RosterAdapter.SortOption.values()
                    if (position > sortOptionList.size - 1) return  // sort option out of bounds of options
                    // sort the players by the option selected by the user
                    sortBy(sortOptionList[position])
                }
            }

            // clicking anywhere on filter bar will trigger selector
            itemView.roster_filter_bar_bg.setOnClickListener {
                spinner.performClick()
            }

            // load team logo
            TeamManager.getInstance()?.getSelectedTeam()?.logoUrl?.let {
                if (it.isNotEmpty()) {
                    val teamLogo = itemView.findViewById<ImageView>(R.id.team_logo)
                    Picasso.get().load(it).into(teamLogo)
                }
            }
        }
    }

    private fun getAgeFromBirthDate(birthDate: String): Int {
        if (birthDate.isEmpty()) return -1
        val dateTime = DateTime.parse(birthDate, DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"))
        return Interval(dateTime, DateTime.now()).toPeriod().years
    }

    inner class DataMenuRosterViewHolder(itemView: View) : RosterItemViewHolder(itemView) {
        override fun bindView(position: Int) {
            getRosterPlayers(position)
        }

        val yearsOldString = if (LocalizationManager.isInitialized()) {
            LocalizationManager.DataMenu.RosterYearOld
        } else {
            dataMenuRosterFragment.resources.getString(R.string.roster_years_old)
        }

        // ex. 196 lbs
        val poundsString = if (LocalizationManager.isInitialized()) {
            LocalizationManager.DataMenu.RosterPoundsUnit
        } else {
            dataMenuRosterFragment.resources.getString(R.string.roster_pounds_unit)
        }


        private fun getRosterPlayers(position: Int) {
            val curPlayer = rosters.get(position - 1)
            val injuries = ""
            val ageString = if (getAgeFromBirthDate(curPlayer.birthDate) == -1) {
                ""
            } else {
                "${getAgeFromBirthDate(curPlayer.birthDate)}$yearsOldString"
            }

            setViewData(
                    playerName = "${curPlayer.firstName?.get(0)}. ${curPlayer.lastName}",
                    position = curPlayer.position,
                    jersey = if (curPlayer.jersey?.isEmpty()!!) "-" else curPlayer.jersey,
                    height = curPlayer.height,
                    age = ageString,
                    weight = "${curPlayer.weight} $poundsString",
                    injuries = injuries
            )
        }

        /*
        legacy injury handling
        val curPlayer = participants.players[position - 1] // subtract 1 because the filter bar is the first ViewHolder

            val injuredIndex = findInInjured(curPlayer.playerId)
            val injuries = if (injuredIndex != -1) {
                // check if injury is "current"
                getInjuredText(participants.injuries[injuredIndex])
            } else {
                ""
            }

            // return the index of this player in participants.injuries. If this player is not injured, return -1
    private fun findInInjured(playerId: Int): Int =
            participants.injuries.indexOfFirst { it.player.playerId == playerId }


         */

        private fun setViewData(playerName: String = "", position: String = "", jersey: String = "", height: String = "", age: String = "", weight: String = "", injuries: String = "") {
            itemView.tv_player_name.text = playerName
            itemView.tv_player_position.text = position
            itemView.tv_player_number.text = jersey
            itemView.tv_player_height.text = height
            itemView.tv_player_age.text = age
            itemView.tv_player_weight.text = weight
            if (injuries.isEmpty()) {
                itemView.tv_player_injured.visibility = View.GONE
            } else {
                itemView.tv_player_injured.text = injuries
                itemView.tv_player_injured.visibility = View.VISIBLE
            }
        }
    }

    // returns injury information. Returns empty string if all of player's injuries are in the past
    fun getInjuredText(injuries: StatsInjuryDetail): String {
        // first, we get the injury with the largest "sequence" number
        val mostRecentInjury: StatsInjuryDetailItem? = injuries.injuryDetails.maxBy { it.sequence }
                ?: return ""

        // now check if this injury is still present (i.e not in the past)
        return if (getDaysAgo(mostRecentInjury?.returnDate) <= 0) {
            return mostRecentInjury?.information ?: ""
        } else {
            // if injury is in the past, return empty string
            ""
        }
    }

    private fun sortRoster() {
        val nameComparator = compareBy<RotoPlayer>({ it.lastName }, { it.firstName })
        val positionComparator = compareBy<RotoPlayer>({ it.position }, { it.lastName }, { it.firstName })
        val ageComparator = compareBy<RotoPlayer>({ DateTime.parse(it.birthDate) }, { it.lastName }, { it.firstName })
        val heightComparator = compareBy<RotoPlayer>({

            val input = it.height
            val regex = "(\\d+)'(\\d+)\\\""
            val pattern = Pattern.compile(regex)
            val matcher = pattern.matcher(input)

            if (matcher.find()) {
                val feet = Integer.parseInt(matcher.group(1))
                val inches = Integer.parseInt(matcher.group(2))
                return@compareBy feet * 12 + inches
            } else {
                return@compareBy it.height
            }
        }, { it.lastName }, { it.firstName })
        val weightComparator = compareBy<RotoPlayer>({ it.weight }, { it.lastName }, { it.firstName })
        val jerseyComparator = compareBy<RotoPlayer>({ if (it.jersey?.isBlank()!!) 100 else it.jersey.toInt() }, { it.lastName }, { it.firstName })

        when (sortOption) {
            SortOption.NAME -> rosters.sortWith(nameComparator)
            SortOption.POSITION -> rosters.sortWith(positionComparator)
            SortOption.AGE -> rosters.sortWith(ageComparator)
            SortOption.HEIGHT -> rosters.sortWith(heightComparator)
            SortOption.WEIGHT -> rosters.sortWith(weightComparator)
            SortOption.JERSEY -> rosters.sortWith(jerseyComparator)
        }
    }

    abstract class RosterItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bindView(position: Int)
    }

}