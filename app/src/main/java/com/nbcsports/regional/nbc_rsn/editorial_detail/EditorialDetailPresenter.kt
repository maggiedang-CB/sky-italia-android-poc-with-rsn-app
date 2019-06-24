package com.nbcsports.regional.nbc_rsn.editorial_detail

import android.app.Activity
import android.graphics.Color
import com.google.gson.Gson
import com.nbcsports.regional.nbc_rsn.analytics.TrackingHelper.PageInfo
import com.nbcsports.regional.nbc_rsn.common.*
import com.nbcsports.regional.nbc_rsn.extensions.d
import com.nbcsports.regional.nbc_rsn.team_feed.components.TeamViewComponentsAdapter
import com.nbcsports.regional.nbc_rsn.team_feed.components.TeamViewComponentsAdapter.*
import com.nbcsports.regional.nbc_rsn.urban_airship.NotificationsManagerKt
import com.nbcsports.regional.nbc_rsn.utils.ActivityUtils
import com.nbcsports.regional.nbc_rsn.utils.FtueUtil
import com.nbcsports.regional.nbc_rsn.utils.NativeShareUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class EditorialDetailPresenter(var view: EditorialDetailTemplateFragment?, val config: Config?) : EditorialDetailContract.Presenter {

    var gson: Gson = Gson()

    private lateinit var team: Team
    private lateinit var teamFeedComponentList: ArrayList<FeedComponent>
    private var componentId: String = ""
    private var selectedFeedComponentPosition: Int = 0

    init {
        view?.setPresenter(this)
    }

    override fun setTeam(team: Team?) {
        if (team == null) return
        this.team = team
    }

    override fun setTeamFeedComponentList(teamFeedComponentList: ArrayList<FeedComponent>?) {
        if (teamFeedComponentList == null) return
        this.teamFeedComponentList = teamFeedComponentList
    }

    override fun setSelectedFeedComponentPosition(selectedFeedComponentPosition: Int) {
        this.selectedFeedComponentPosition = selectedFeedComponentPosition
    }

    override fun setComponentId(componentId: String) {
        this.componentId = componentId
    }

    override fun getTeamviewItemData() {
        if (team == null) {
            return
        }

        var editorialDetailsUrl = ""
        if (config?.editorialDetailsUrl != null) {
            editorialDetailsUrl = config.editorialDetailsUrl.replace("[id]", componentId)
            d("editorialDetailsUrl: $editorialDetailsUrl")
        }

        /**
         * Not handling by CompositeDisposable is because onDestroy() of
         * EditorialDetailTemplateFragment will be called multiple times
         * while deeplinking, that means CompositeDisposable.clear() will
         * be called multiple times and causing
         * "java.io.InterruptedIOException: thread interrupted"
         * is thrown on getEditorialDetailsFromServer(...)
         */
        EditorialDataManager.getInstance()!!.getEditorialDetailsFromServer(editorialDetailsUrl, gson)
                .flatMap<EditorialDetailsFeed>(EditorialDataManager.getInstance()!!.setDeeplinkToMediaSource(team.teamId, componentId))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { editorialDetailsFeed ->
                            if (editorialDetailsFeed?.editorialDetail == null
                                    || editorialDetailsFeed.editorialDetail.components == null) {
                                return@subscribe
                            }

                            view?.showTeamviewListItems(team, teamFeedComponentList, editorialDetailsFeed.editorialDetail.components)
                            view?.getEditorialDetailsPageChangeListener()?.setEditorialDetailsFeed(editorialDetailsFeed)
                        },
                        {

                            // On error on getting editorial details from server,
                            // simply close editorial fragment and return to the team view fragment
                            view?.doExit(false)
                        }
                )
    }

    override fun share(title: String?) {
        // Check if data menu ftue is in process and not done yet and is shown long
        // enough, if so, marks data menu ftue as done (scenario 5)
        if (!FtueUtil.hasDoneDataMenuFtue()
                && NotificationsManagerKt.isFtueDataMenuBottomBannerShowing()
                && FtueUtil.isDataMenuMsgShownLongEnough()){
            // Set data menu ftue done to true
            FtueUtil.setHasDoneDataMenuFtue(true)
        }
        val feedComponent = teamFeedComponentList.get(selectedFeedComponentPosition)
        var titleString = feedComponent.title
        if (title != null){
            titleString = title
        }
        val shareInfo = NativeShareUtils.generateShareInfo(view, feedComponent, titleString, null, team.teamId)
        if (shareInfo != null) {
            NativeShareUtils.share(shareInfo)
        }
    }

    override fun getPageInfo(): PageInfo? {
        var section = ""
        var subSection = ""
        var league = ""
        var author = ""
        val feedComponent = teamFeedComponentList[selectedFeedComponentPosition]
        val cardType = feedComponent.cardType
        val contentType = feedComponent.contentType
        val type = feedComponent.type
        val componentTag = feedComponent.tag
        val itemViewType = TeamViewComponentsAdapter.getItemViewType(type, cardType, contentType)
        when (itemViewType) {
            VIEW_TYPE_Feed_Standard_Image, VIEW_TYPE_F1_Standard_Image, VIEW_TYPE_Feed_Standard_Video, VIEW_TYPE_F1_Standard_Video, VIEW_TYPE_Feed_Standard_Text -> {
                section = "Article"
                league = team.league
                author = feedComponent.author
                subSection = componentTag
            }
            VIEW_TYPE_Feed_Standard_Podcast, VIEW_TYPE_F1_Standard_Audio -> {
                section = "Podcast"
                league = team.league
                author = feedComponent.author
            }
            else -> section = "default"
        }
        return PageInfo(false, team.regionName, team.displayName, section, subSection, feedComponent.contentType, "", league, author, feedComponent.componentId, feedComponent.title)
    }

    override fun setAppropriateStatusBarColor(activity: Activity?, isCurrentSteppedFragment: Boolean) {
        activity?.let {
            if (isTeamDisplayNameTheSameAsSelected()){
                if (isCurrentSteppedFragment){
                    ActivityUtils.setStatusBarColor(it, Color.BLACK)
                } else {
                    ActivityUtils.setStatusBarColor(it, Color.parseColor(team.primaryColor))
                }
            }
        }
    }

    fun isTeamDisplayNameTheSameAsSelected(): Boolean {
        val selectedTeam = TeamManager.getInstance()?.getSelectedTeam() ?: return false
        return selectedTeam.displayName.equals(team.displayName, ignoreCase = true)
    }

    fun unsubscribe() {
        view = null
    }
}