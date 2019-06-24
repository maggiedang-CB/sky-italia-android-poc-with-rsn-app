package com.nbcsports.regional.nbc_rsn.data_menu

import android.os.Bundle
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import com.nbcsports.regional.nbc_rsn.R
import com.nbcsports.regional.nbc_rsn.common.BaseFragment
import com.nbcsports.regional.nbc_rsn.common.Config
import com.nbcsports.regional.nbc_rsn.common.Constants.*
import com.nbcsports.regional.nbc_rsn.common.Team
import com.nbcsports.regional.nbc_rsn.data_menu.components.DataMenuAdapter
import com.nbcsports.regional.nbc_rsn.data_menu.models.DataMenuOverviewDataModel

class DataMenuFragment : BaseFragment(), DataMenuContract.View {

    companion object {
        fun newInstance(team: Team?, config: Config?, isOffseason: Boolean): DataMenuFragment {
            val dataMenuFragment = DataMenuFragment()

            val args = Bundle()
            args.putParcelable(TEAM_KEY, team)
            args.putParcelable(CONFIG_KEY, config)
            args.putBoolean(DATA_MENU_IS_OFF_SEASON_KEY, isOffseason)
            dataMenuFragment.arguments = args

            return dataMenuFragment
        }
    }

    private var dataMenuRootBackgroundLayerView: View? = null
    private var mainRecyclerView: RecyclerView? = null

    private var presenter: DataMenuContract.Presenter? = null
    private var currentTeam: Team? = null
    private var config: Config? = null
    private var isCurrentlyOffseason: Boolean = false
    private var dataMenuAdapter: DataMenuAdapter? = null

    override fun getLayout(): Int {
        return R.layout.data_menu_layout
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataMenuPresenter(this)
        currentTeam = arguments?.getParcelable(TEAM_KEY)
        config = arguments?.getParcelable(CONFIG_KEY)
        isCurrentlyOffseason = arguments?.getBoolean(DATA_MENU_IS_OFF_SEASON_KEY) ?: isCurrentlyOffseason

        DataMenuDataManager.update(currentTeam?.statsTeamID!!)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dataMenuRootBackgroundLayerView = view.findViewById(R.id.data_menu_root_background_layer_view)
        mainRecyclerView = view.findViewById(R.id.data_menu_main_recycler_view)

        initMainRecyclerView()

        presenter?.subscribe(currentTeam)
    }

    override fun onDestroy() {
        dataMenuAdapter?.onDestroy()
        presenter?.unsubscribe()
        super.onDestroy()
    }

    override fun setPresenter(presenter: DataMenuContract.Presenter) {
        this.presenter = presenter
    }

    override fun setDataMenuColor(percent: Float) {
        // Modify root background layer alpha
        dataMenuRootBackgroundLayerView?.alpha = percent
        // Modify recycler view alpha
        mainRecyclerView?.alpha = percent
    }

    override fun setMainRecyclerViewData(itemList: List<DataMenuOverviewDataModel>) {
        activity?.runOnUiThread {
            dataMenuAdapter?.setData(itemList)
        }
    }

    override fun isDataBarGameStateOffseason(): Boolean {
        return isCurrentlyOffseason
    }

    override fun getConfig(): Config? {
        return config
    }

    override fun refreshAdapterContent(position: Int) {
        if (position >= 0){
            activity?.runOnUiThread {
                dataMenuAdapter?.notifyItemChanged(position)
            }
        }
    }

    private fun initMainRecyclerView() {
        if (currentTeam != null && config != null){
            dataMenuAdapter = DataMenuAdapter(this, (currentTeam as Team), (config as Config))
            mainRecyclerView?.apply {
                layoutManager = LinearLayoutManager(context)
                itemAnimator = DefaultItemAnimator()
                adapter = dataMenuAdapter
                isMotionEventSplittingEnabled = false
            }
        }
    }

}