package com.nbcsports.regional.nbc_rsn.data_menu.components

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import com.nbcsports.regional.nbc_rsn.R
import com.nbcsports.regional.nbc_rsn.common.BaseFragment
import com.nbcsports.regional.nbc_rsn.common.TeamManager
import com.nbcsports.regional.nbc_rsn.localization.LocalizationManager
import com.nbcsports.regional.nbc_rsn.navigation.NavigationManager
import kotlinx.android.synthetic.main.fragment_sub_menu.*
import org.joda.time.LocalDateTime

open class SubMenuBaseFragment : BaseFragment() {

    open fun getSubLayout(): Int {
        return 0
    }

    override fun getLayout(): Int {
        return R.layout.fragment_sub_menu
    }

    open fun getTitle(): String {
        return ""
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        if (getSubLayout() != 0) {
            val childView = inflater.inflate(getSubLayout(), container, false)
            (view?.findViewById(R.id.data_menu_sub_frame_layout) as FrameLayout).addView(childView)
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvUpdated = view.findViewById<TextView>(R.id.db_standings_updated)

        // show date
        val date = LocalDateTime.now()
        if (LocalizationManager.isInitialized()) {
            val updated = LocalizationManager.DataMenu.DataDetailUpdated
            val time = date.toString("M.dd.YY")
            tvUpdated.text = LocalizationManager.DataMenu.getDataDetailUpdatedFormat(updated, time)
        } else {
            tvUpdated.text = "UPDATED ${date.toString("M.dd.YY")}"
        }

        // close this fragment
        standings_close.setOnClickListener {
            NavigationManager.getInstance().popAndRemoveFragmentFromBackStack(this)
        }

        TeamManager.getInstance()?.getSelectedTeam()?.let {
            db_standings_primary_color?.setBackgroundColor(Color.parseColor(it.primaryColor))
            db_standings_secondary_color?.setBackgroundColor(Color.parseColor(it.secondaryColor))
        }

        // set title
        db_standings_title?.text = getTitle()
    }
}