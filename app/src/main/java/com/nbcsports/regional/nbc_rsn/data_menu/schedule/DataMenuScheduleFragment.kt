package com.nbcsports.regional.nbc_rsn.data_menu.schedule

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.View
import com.nbcsports.regional.nbc_rsn.R
import com.nbcsports.regional.nbc_rsn.common.TeamManager
import com.nbcsports.regional.nbc_rsn.data_menu.components.SubMenuBaseFragment
import com.nbcsports.regional.nbc_rsn.utils.DataMenuUtils
import com.nbcsports.regional.nbc_rsn.localization.LocalizationManager
import kotlinx.android.synthetic.main.fragment_data_menu_schedule.*

class DataMenuScheduleFragment : SubMenuBaseFragment() {

    private val scheduleAdapter = DataMenuScheduleAdapter()

    override fun getSubLayout(): Int {
        return R.layout.fragment_data_menu_schedule
    }

    override fun getTitle(): String {
        return if (LocalizationManager.isInitialized()) {
            "${TeamManager.getInstance()?.getSelectedTeam()?.displayName} ${LocalizationManager.DataMenu.Schedule}"
        } else {
            "${TeamManager.getInstance()?.getSelectedTeam()?.displayName} ${getString(R.string.schedule)}"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataMenuUtils.DATA_MENU_SCHEDULE_IS_OPENED = true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        schedule_list.adapter = scheduleAdapter
        schedule_list.layoutManager = LinearLayoutManager(context)

        scheduleAdapter.subscribe()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        scheduleAdapter.unsubscribe()
        DataMenuUtils.DATA_MENU_SCHEDULE_IS_OPENED = false
    }
}