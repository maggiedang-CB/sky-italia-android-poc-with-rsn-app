package com.nbcsports.regional.nbc_rsn.editorial_detail

import android.graphics.drawable.GradientDrawable
import androidx.viewpager.widget.ViewPager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView

import com.clearbridge.pull_to_refresh.PullLayout
import com.jakewharton.rxbinding2.view.RxView
import com.nbcsports.regional.nbc_rsn.MainActivity
import com.nbcsports.regional.nbc_rsn.R
import com.nbcsports.regional.nbc_rsn.common.Constants
import com.nbcsports.regional.nbc_rsn.common.Team
import com.nbcsports.regional.nbc_rsn.common.components.ViewHolderTypeFeedTextOnly
import com.nbcsports.regional.nbc_rsn.editorial_detail.models.EditorialDetailItem
import com.nbcsports.regional.nbc_rsn.team_feed.components.FragmentLifeCycleListener
import com.nbcsports.regional.nbc_rsn.team_feed.components.ViewHolderTypeBase
import com.nbcsports.regional.nbc_rsn.common.FeedComponent
import com.nbcsports.regional.nbc_rsn.editorial_detail.components.*
import com.nbcsports.regional.nbc_rsn.utils.NativeShareUtils

import com.nbcsports.regional.nbc_rsn.extensions.fromInt
import com.nbcsports.regional.nbc_rsn.navigation.NavigationManager
import com.nbcsports.regional.nbc_rsn.urban_airship.NotificationsManagerKt
import com.nbcsports.regional.nbc_rsn.utils.FtueUtil

import java.util.ArrayList
import java.util.concurrent.TimeUnit

import io.reactivex.android.schedulers.AndroidSchedulers


class EditorialDetailComponentsAdapter(
        val editorialDetailTemplateFragment: EditorialDetailTemplateFragment,
        val items: List<EditorialDetailItem>,
        private val team: Team,
        private val teamFeedComponentList: ArrayList<FeedComponent>,
        private val teamColorGradient: GradientDrawable,
        private val viewPager: ViewPager) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), FragmentLifeCycleListener.Interface {

    private enum class VIEW_TYPE_EDITORIAL {
        UNKNOWN,
        HEADER,
        FOOTER,
        START,
        IMAGE,
        IMAGE_INLINE,
        VIDEO,
        BODY_TEXT_REGULAR,
        BODY_TEXT_DROP_CAP,
        BODY_TEXT_FLAGGED,
        EMBEDDED_TWEET,
        FEED_STANDARD_TEXT
    }

    private var recyclerView: RecyclerView? = null
    private val fragmentLifeCycleListeners = ArrayList<FragmentLifeCycleListener>()

    override fun getItemViewType(position: Int): Int {
        val editorialItem = items[position]
        val type = editorialItem.type
        val componentType = editorialItem.componentType
        val variation = editorialItem.variation

        val indexOfFirstDropCap = items.indexOfFirst { it.variation.equals(Constants.EDITORIAL_COMPONENT_VARIATION_DROP_CAP, ignoreCase = true) }


        return (when (type) {
            EditorialDetailItem.Type.HEADER -> VIEW_TYPE_EDITORIAL.HEADER
            EditorialDetailItem.Type.FOOTER -> VIEW_TYPE_EDITORIAL.FOOTER
            EditorialDetailItem.Type.COMPONENT -> when (componentType) {
                Constants.EDITORIAL_COMPONENT_TYPE_HERO -> when {
                    variation.equals(Constants.EDITORIAL_COMPONENT_VARIATION_IMAGE, ignoreCase = true)
                            || variation.equals(Constants.EDITORIAL_COMPONENT_VARIATION_NOIMAGE, ignoreCase = true)
                            || variation.equals(Constants.EDITORIAL_COMPONENT_VARIATION_NOIMAGE_SPACED, ignoreCase = true) -> VIEW_TYPE_EDITORIAL.IMAGE
                    variation.equals(Constants.EDITORIAL_COMPONENT_VARIATION_VIDEO, ignoreCase = true) -> VIEW_TYPE_EDITORIAL.VIDEO
                    else -> VIEW_TYPE_EDITORIAL.UNKNOWN
                }
                Constants.EDITORIAL_COMPONENT_TYPE_BODY_TEXT -> when {
                    // only show drop cap on the first instance it. Otherwise, default to regular
                    position == indexOfFirstDropCap -> VIEW_TYPE_EDITORIAL.BODY_TEXT_DROP_CAP
                    variation.equals(Constants.EDITORIAL_COMPONENT_VARIATION_REGULAR, ignoreCase = true) || variation.equals(Constants.EDITORIAL_COMPONENT_VARIATION_DROP_CAP, ignoreCase = true) -> VIEW_TYPE_EDITORIAL.BODY_TEXT_REGULAR
                    variation.equals(Constants.EDITORIAL_COMPONENT_VARIATION_FLAGGED, ignoreCase = true) -> VIEW_TYPE_EDITORIAL.BODY_TEXT_FLAGGED
                    else -> VIEW_TYPE_EDITORIAL.UNKNOWN
                }
                Constants.EDITORIAL_COMPONENT_TYPE_IMAGE_INLINE -> VIEW_TYPE_EDITORIAL.IMAGE_INLINE
                Constants.EDITORIAL_COMPONENT_TYPE_EMBEDDED_TWEET -> VIEW_TYPE_EDITORIAL.EMBEDDED_TWEET
                Constants.CARD_TYPE_Feed_Text_Only -> VIEW_TYPE_EDITORIAL.FEED_STANDARD_TEXT
                else -> VIEW_TYPE_EDITORIAL.UNKNOWN
            }
        }).ordinal
    }

    override fun getItemCount(): Int {
        return items.size
    }

    /**
     * By the two callbacks, getItemViewType() and onCreateViewHolder(),
     * we can dynamically apply a respective ViewHolder and layout of a card...
     *
     * @param parent
     * @param viewType
     * @return
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (com.nbcsports.regional.nbc_rsn.extensions.fromInt<VIEW_TYPE_EDITORIAL>(viewType)) {

            // EDITORIAL
            VIEW_TYPE_EDITORIAL.IMAGE -> {
                val holder = ViewHolderTypeHeroImage(
                        this,
                        LayoutInflater.from(parent.context).inflate(R.layout.component_hero_image, parent, false),
                        team,
                        viewType
                )

                RxView.clicks(holder.exitButton)
                        .debounce(300L, TimeUnit.MILLISECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { (editorialDetailTemplateFragment.activity as MainActivity).tapFab(false) }
                holder
            }

            VIEW_TYPE_EDITORIAL.VIDEO -> {
                val holder = ViewHolderTypeHeroVideo(
                        this,
                        LayoutInflater.from(parent.context).inflate(R.layout.component_hero_video, parent, false),
                        team,
                        viewPager,
                        viewType
                )

                RxView.clicks(holder.exitButton)
                        .debounce(300L, TimeUnit.MILLISECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { (editorialDetailTemplateFragment.activity as MainActivity).tapFab(false) }
                holder
            }

            VIEW_TYPE_EDITORIAL.BODY_TEXT_DROP_CAP -> ViewHolderTypeDropCap(LayoutInflater.from(parent.context).inflate(R.layout.component_editorial_drop_cap, parent, false), team, viewType)

            VIEW_TYPE_EDITORIAL.BODY_TEXT_REGULAR, VIEW_TYPE_EDITORIAL.BODY_TEXT_FLAGGED -> ViewHolderTypeBodyText(LayoutInflater.from(parent.context).inflate(R.layout.component_editorial_body_text, parent, false), team, viewType)

            VIEW_TYPE_EDITORIAL.IMAGE_INLINE -> ViewHolderTypeInlineImage(
                    this,
                    LayoutInflater.from(parent.context).inflate(R.layout.component_editorial_inline_image, parent, false),
                    viewType,
                    team
            )

            // feed text only
            VIEW_TYPE_EDITORIAL.FEED_STANDARD_TEXT -> ViewHolderTypeFeedTextOnly(team, LayoutInflater.from(parent.context).inflate(R.layout.teamview_card_type_feed_text_only, parent, false), viewType)
            VIEW_TYPE_EDITORIAL.HEADER -> ViewHolderEditorialTypeHeader(LayoutInflater.from(parent.context).inflate(R.layout.teamview_card_type_header, parent, false), viewType)
            VIEW_TYPE_EDITORIAL.FOOTER -> ViewHolderEditorialTypeFooter(LayoutInflater.from(parent.context).inflate(R.layout.component_editorial_type_footer, parent, false), viewType)
            VIEW_TYPE_EDITORIAL.EMBEDDED_TWEET -> ViewHolderTypeTwitter(parent.context, LayoutInflater.from(parent.context).inflate(R.layout.component_editorial_twitter, parent, false), viewType)
            else -> ViewHolderEditorialTypeBlank(LayoutInflater.from(parent.context).inflate(R.layout.teamview_card_type_blank, parent, false), viewType)
        }
    }

    override fun onBindViewHolder(vh: RecyclerView.ViewHolder, position: Int) {

        val editorialDetailItem = items[position]

        // Generic initialization of the base type
        if (vh is ViewHolderEditorialTypeBase) {
            vh.bind(editorialDetailItem)
        }

        when (fromInt<VIEW_TYPE_EDITORIAL>(vh.itemViewType)) {
            // Concrete initializations of the derived types
            VIEW_TYPE_EDITORIAL.IMAGE -> {
                val holder = vh as ViewHolderTypeHeroImage

                // Remove section for hero image if the article has no hero image
                if(editorialDetailItem.variation.equals(Constants.EDITORIAL_COMPONENT_VARIATION_NOIMAGE, ignoreCase = true))
                    holder.mView.findViewById<View>(R.id.hero_image_container).visibility = View.GONE

                val playNonFeatureGifs = (editorialDetailTemplateFragment.activity as MainActivity).config?.playNonFeatureGifs ?: false
                holder.updateView(playNonFeatureGifs)

                setupSharing(holder.shareButton, editorialDetailItem.heroText)
                holder.mView.setOnClickListener(mHolderViewCommonOnClickListener)
                holder.mView.setOnLongClickListener(holderViewOnLongClickListener)
            }

            VIEW_TYPE_EDITORIAL.VIDEO -> {

                // Note: ViewHolderTypeHeroVideo does not extend ViewHolderEditorialTypeBase
                val holder = vh as ViewHolderTypeHeroVideo
                holder.bind(editorialDetailItem)

                val feedComponent = editorialDetailTemplateFragment.getFeedComponent(editorialDetailTemplateFragment.selectedFeedComponentPosition)
                if (editorialDetailItem.mediaSource != null) {
                    holder.setMediaSource(editorialDetailItem.mediaSource, feedComponent, editorialDetailItem.heroText)
                }
                holder.updateView()

                setupSharing(holder.shareButton, editorialDetailItem.heroText)
                holder.mView.setOnClickListener(holderViewVideoOnClickListener)
                holder.mView.setOnLongClickListener(holderViewOnLongClickListener)
            }

            VIEW_TYPE_EDITORIAL.BODY_TEXT_DROP_CAP -> {
                val holder = vh as ViewHolderTypeDropCap
                holder.mView.setOnClickListener(mHolderViewCommonOnClickListener)
                holder.mView.setOnLongClickListener(holderViewOnLongClickListener)
            }

            VIEW_TYPE_EDITORIAL.BODY_TEXT_FLAGGED, VIEW_TYPE_EDITORIAL.BODY_TEXT_REGULAR -> {
                val holder = vh as ViewHolderTypeBodyText
                holder.mView.setOnClickListener(mHolderViewCommonOnClickListener)
                holder.mView.setOnLongClickListener(holderViewOnLongClickListener)
            }

            VIEW_TYPE_EDITORIAL.FEED_STANDARD_TEXT -> {

                // Note: ViewHolderTypeFeedTextOnly does not extend ViewHolderEditorialTypeBase
                val holder = vh as ViewHolderTypeFeedTextOnly

                val (componentType, variation, componentAssetUrl, author, _, publishedDate, _, _, tag, _, heroText) = editorialDetailItem
                val fc = FeedComponent(FeedComponent.Type.COMPONENT)

                fc.title = heroText
                fc.author = author
                fc.tag = tag
                fc.contentType = componentType
                fc.cardType = variation
                fc.imageAssetUrl = componentAssetUrl
                fc.publishedDate = publishedDate
                holder.mItem = fc

                holder.setCardAttributes(false, true) //(isFirstFeedCard(position));
                holder.setCardAttributesForEditorialDetailFragment()

                holder.mView.setOnClickListener(mHolderViewCommonOnClickListener)
                holder.mView.setOnLongClickListener(holderViewOnLongClickListener)
            }

            VIEW_TYPE_EDITORIAL.HEADER -> {
                val holder = vh as ViewHolderEditorialTypeHeader
                val teamData = editorialDetailItem.team
                if (teamData != null) {
                    holder.setHeaderTextAndColors(teamData)
                }
            }

            VIEW_TYPE_EDITORIAL.IMAGE_INLINE -> {
                val playNonFeatureGifs = (editorialDetailTemplateFragment.activity as MainActivity).config?.playNonFeatureGifs ?: false
                (vh as? ViewHolderTypeInlineImage)?.bind(editorialDetailItem)
                (vh as? ViewHolderTypeInlineImage)?.updateImage(playNonFeatureGifs)
            }

            else -> {

            }
        }
    }

    private fun setupSharing(shareButton: ImageView?, title: String) {
        shareButton?.setOnClickListener { _ ->
            // Check if data menu ftue is in process and not done yet and is shown long
            // enough, if so, marks data menu ftue as done (scenario 5)
            if (!FtueUtil.hasDoneDataMenuFtue()
                    && NotificationsManagerKt.isFtueDataMenuBottomBannerShowing()
                    && FtueUtil.isDataMenuMsgShownLongEnough()){
                // Set data menu ftue done to true
                FtueUtil.setHasDoneDataMenuFtue(true)
            }

            val feedComponent = editorialDetailTemplateFragment.getFeedComponent(editorialDetailTemplateFragment.selectedFeedComponentPosition)
            val titleText = if (title.isEmpty()) feedComponent.title else title
            val shareInfo = NativeShareUtils.generateShareInfo(
                    editorialDetailTemplateFragment,
                    feedComponent,
                    titleText,
                    null,
                    team.teamId
            )

            shareInfo.let { NativeShareUtils.share(it) }
        }
    }

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        super.onViewAttachedToWindow(holder)

        /*
        RecyclerVisibilityScrollListener won't trigger setVisible unless scrolled, so even if a view holder
        is visible to user when list is first drawn, setVisible won't be called. Adding setVisible here will
        ensure that it will get called when list is visible to user.
         */
        if (holder is ViewHolderTypeBase) {
            holder.setVisible(true)
            holder.addPageChangeListener()
        }
    }

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        super.onViewDetachedFromWindow(holder)
        if (holder is ViewHolderTypeBase) {
            holder.setVisible(false)
            holder.removePageChangeListener()
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        this.recyclerView = null
    }

    fun onResume() {
        for (listener in fragmentLifeCycleListeners) {
            listener.onResume()
        }
    }

    fun onPause() {
        for (listener in fragmentLifeCycleListeners) {
            listener.onPause()
        }
    }

    override fun addFragmentLifeCycleListener(fragmentLifeCycleListener: FragmentLifeCycleListener) {
        this.fragmentLifeCycleListeners.add(fragmentLifeCycleListener)
    }

    override fun showNativeShare(title: String, link: String) {
        //((MainActivity) editorialDetailTemplateFragment.getActivity()).shareCard(title, link);
    }

    override fun getRefreshLayout(): PullLayout {
        return editorialDetailTemplateFragment.exitLayout
    }

    fun onDestroy() {
        for (listener in fragmentLifeCycleListeners) {
            listener.onPause()
        }
        fragmentLifeCycleListeners.clear()
    }

    private val shareOnClickListener: View.OnClickListener = View.OnClickListener {
        val feedComponent = editorialDetailTemplateFragment.getFeedComponent(editorialDetailTemplateFragment.selectedFeedComponentPosition)

        val titleText = feedComponent.title
        val shareInfo = NativeShareUtils.generateShareInfo(editorialDetailTemplateFragment, feedComponent, titleText,

                // passing teamFeedFragment = null will prevent closing all the articles
                //  if the article componentId is the same as the article on top of the backstack
                null, team.teamId)

        if (shareInfo != null) {
            NativeShareUtils.share(shareInfo)
        }
    }

    private val holderViewVideoOnClickListener: View.OnClickListener = View.OnClickListener { v ->
        val viewHolder = recyclerView!!.getChildViewHolder(v)
        if (viewHolder != null) {
            val itemPosition = viewHolder.adapterPosition

            if (viewHolder is ViewHolderTypeHeroVideo) {
                val editorialDetailItem = viewHolder.editorialDetailItem

                //TODO: mediaSources in EditorialDetailItem.kt
                if (editorialDetailItem?.mediaSource != null) {
                    val mediaSource = editorialDetailItem.mediaSource
                    if (mediaSource != null) {
                        val androidStreamUrl = mediaSource.streamUrl
                    }
                }

                return@OnClickListener
            }
        }
    }

    internal var holderViewOnLongClickListener: View.OnLongClickListener = View.OnLongClickListener { v ->
        val viewHolder = recyclerView?.getChildViewHolder(v)

        //debug
        //Toast.makeText(editorialDetailTemplateFragment.getActivity(),
        //        "onLongClick on viewHolder: " + viewHolder.toString(),
        //        Toast.LENGTH_SHORT).show();

        return@OnLongClickListener true
    }

    private val mHolderViewCommonOnClickListener: View.OnClickListener = View.OnClickListener { v ->
        val viewHolder = recyclerView!!.getChildViewHolder(v)
        if (viewHolder != null) {
            if (viewHolder is ViewHolderTypeFeedTextOnly) {
                // Check if data menu ftue is in process and not done yet and is shown long
                // enough, if so, marks data menu ftue as done (scenario 5)
                if (!FtueUtil.hasDoneDataMenuFtue()
                        && NotificationsManagerKt.isFtueDataMenuBottomBannerShowing()
                        && FtueUtil.isDataMenuMsgShownLongEnough()){
                    // Set data menu ftue done to true
                    FtueUtil.setHasDoneDataMenuFtue(true)
                }

                val editorialBackgroundViewToFadeTo = editorialDetailTemplateFragment.view!!.findViewById<ViewGroup>(R.id.editorial_background_to_fade_to)

                val newSelectedFeedComponentPosition = editorialDetailTemplateFragment.upNextFeedComponentPosition

                NavigationManager.getInstance().openCommonCardDetailsScreen(
                        editorialBackgroundViewToFadeTo,
                        // Note, editorialDetailTemplateFragment.getFragmentManager() gets the same FragmentManager
                        // instance as teamViewTemplateFragment.getChildFragmentManager() does.
                        editorialDetailTemplateFragment.parentFragment!!.childFragmentManager,
                        team, teamFeedComponentList,
                        newSelectedFeedComponentPosition, null)
            }
        }
    }
}
