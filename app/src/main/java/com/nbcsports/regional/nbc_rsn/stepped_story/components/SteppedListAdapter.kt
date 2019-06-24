package com.nbcsports.regional.nbc_rsn.stepped_story.components

import android.graphics.Color
import android.os.Build
import androidx.recyclerview.widget.RecyclerView
import android.text.Html
import android.text.Spanned
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.nbcsports.regional.nbc_rsn.R
import com.nbcsports.regional.nbc_rsn.common.FeedComponent
import com.nbcsports.regional.nbc_rsn.common.Team
import com.nbcsports.regional.nbc_rsn.stepped_story.SteppedStoryContract
import com.nbcsports.regional.nbc_rsn.stepped_story.SteppedStoryPresenter
import com.nbcsports.regional.nbc_rsn.stepped_story.intent.SteppedStoryClickListenerFactory
import com.nbcsports.regional.nbc_rsn.stepped_story.template.SteppedStoryCustomTextView
import com.nbcsports.regional.nbc_rsn.team_feed.components.ViewHolderTypeBase
import io.reactivex.disposables.CompositeDisposable

class SteppedListAdapter(
        private val team: Team
) : RecyclerView.Adapter<SteppedListAdapter.ViewHolder>() {

    private var cover: SteppedComponent? = null
    private var feedComponent: FeedComponent? = null
    private val list: MutableList<SteppedComponent> = mutableListOf()
    private var shouldShowOverview: Boolean = false
    private var clickFactory: SteppedStoryClickListenerFactory? = null
    private var steppedCoverRef: SteppedCoverLayout? = null
    private var playNonFeatureGifs: Boolean = false
    private var presenter: SteppedStoryPresenter? = null

    private var shouldRecover = false

    enum class ViewType {
        COVER,
        OVERVIEW,
        UP_NEXT,
        LIST,
        UNKNOWN
    }

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
    var viewHelper: SteppedStoryContract.ViewHelper? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when (com.nbcsports.regional.nbc_rsn.extensions.fromInt<ViewType>(viewType)) {
            ViewType.OVERVIEW -> SteppedCoverViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.holder_cover_layout, parent, false),
                    viewType, team, playNonFeatureGifs, steppedCoverRef, presenter)
            ViewType.UP_NEXT -> {
                val vh = SteppedUpNextViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.teamview_card_type_feed_text_only, parent, false), viewType, team)
                clickFactory?.let {
                    compositeDisposable.add(it.getClickListener(vh))
                }
                vh
            }
            else -> SteppedItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.holder_item_layout, parent, false), viewType,
                    team, playNonFeatureGifs)
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (position == list.size) return ViewType.UP_NEXT.ordinal

        val componentType = list[position].componentType

        return when (componentType?.toLowerCase()) {
            "overview" -> ViewType.OVERVIEW
            "ordered_entry" -> ViewType.LIST
            else -> ViewType.UNKNOWN
        }.ordinal
    }

    override fun getItemCount(): Int {
        return if (list.isEmpty()) 0 else list.size + 1
    }

    fun setData(data: List<SteppedComponent>, feedComponent: FeedComponent?) {
        if (data.isNullOrEmpty()) return
        this.list.clear()
        this.feedComponent = feedComponent

        val (coverList, filteredList) = data.partition { it.componentType.equals("cover", ignoreCase = true) }
        this.cover = coverList.firstOrNull()

        // remove cover element
        shouldShowOverview = data.any { it.componentType.equals("overview", ignoreCase = true) }

        this.list.addAll(filteredList)
        notifyDataSetChanged()
    }

    fun requestRecoverTitlePosition() {
        shouldRecover = true
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Since height might have been adjusted for other parts of the stepped story, we need to
        // reset it to wrap content.
        val lp = holder.itemView.layoutParams
        lp.height = ViewGroup.LayoutParams.WRAP_CONTENT
        holder.itemView.layoutParams = lp

        when (holder) {
            is SteppedUpNextViewHolder -> {
                val fc = FeedComponent(FeedComponent.Type.COMPONENT)

                feedComponent?.let {
                    fc.title = it.title
                    fc.author = it.author
                    fc.tag = it.tag
                    fc.contentType = it.contentType
                    fc.cardType = it.cardType
                    fc.imageAssetUrl = it.imageAssetUrl
                    fc.publishedDate = it.publishedDate
                }
                holder.mItem = fc

                holder.setCardAttributes()
                holder.setCardAttributesForEditorialDetailFragment()
            }
            is SteppedItemViewHolder -> {
                val sectionIndex = position + (if (shouldShowOverview) 0 else 1)
                val sectionTotal = itemCount + (if (shouldShowOverview) -1 else 0) - 1  // subtract 1 for ignoring UP NEXT
                holder.bind(list[position], sectionIndex, sectionTotal, position, cover)

                // overview is missing, therefore we need to show the first ordered entry correctly
                if ((!shouldShowOverview && position == 0) || shouldRecover) {
                    // If two view holders exist on screen, only the top one will be updated.
                    shouldRecover = false
                    holder.animateToNormalImmediately()
                }

                if (position == 0){
                    holder.setUpForSteppedCover(steppedCoverRef)
                }
            }
            is SteppedCoverViewHolder -> {
                // Prevent the bottom item view holder update itself.
                shouldRecover = false
                if (cover != null) {
                    cover?.let {
                        val component = SteppedComponent(
                                componentType = list[position].componentType,
                                variation = it.variation,
                                coverTitle = it.coverTitle,
                                coverImage = it.coverImage,
                                authorReference = it.authorReference,
                                publishedDate = it.publishedDate,
                                componentId = list[position].componentId,
                                displayText = list[position].displayText,
                                entryImage = it.entryImage,
                                mediaSource = it.mediaSource
                        )

                        holder.bind(component)
                    }
                } else {
                    holder.bind(list[position])
                }
            }
        }
    }

    override fun onViewAttachedToWindow(holder: ViewHolder) {
        super.onViewAttachedToWindow(holder)
        viewHelper?.adjustHeightIfNeeded(holder)
        if (holder is SteppedItemViewHolder && holder.isVariationVideo) {
            holder.playSteppedItemVideo()
        }
    }

    override fun onViewDetachedFromWindow(holder: ViewHolder) {
        super.onViewDetachedFromWindow(holder)
        if (holder is SteppedItemViewHolder && holder.isVariationVideo) {
            holder.releaseExoPlayer()
        }
    }

    fun setSteppedStoryClickListenerFactory(clickFactory: SteppedStoryClickListenerFactory?) {
        this.clickFactory = clickFactory
    }

    fun setSteppedCoverLayout(steppedCoverLayout: SteppedCoverLayout?) {
        this.steppedCoverRef = steppedCoverLayout
    }

    fun setPlayNonFeatureGifs(playNonFeatureGifs: Boolean) {
        this.playNonFeatureGifs = playNonFeatureGifs
    }

    fun setSteppedStoryPresenter(presenter: SteppedStoryPresenter?) {
        this.presenter = presenter
    }

    fun unsubscribe() {
        compositeDisposable.clear()
        compositeDisposable.dispose()
    }

    abstract class ViewHolder(itemView: View, itemViewType: Int, val team: Team) : ViewHolderTypeBase(itemView, itemViewType) {

        internal val BR_TAG = "<br>"
        internal val P_TAG = "</*p>"
        internal val NEW_LINE_CHAR_PATTERN = "(\r)?\n"

        /**
         * Get height of the preview. Preview is the part that will show when user scroll to the
         * bottom of the previous stepped story. Then scrolling will be disabled until user scroll
         * harder.
         */
        open fun getPreviewHeight(): Int {
            return itemView.resources.displayMetrics.widthPixels * 9 / 16
        }

        /**
         * Whether allow this view holder to be scrolled to top upon user's scroll-down attempt.
         * Return false will make this ViewHolder be treated as the last item of the RecyclerView.
         */
        open fun allowScrollTo(): Boolean {
            return true
        }

        open fun bind(steppedComponent: SteppedComponent) {

        }

        open fun setAttributes(tv: TextView, text: String?, color: String) {
            val rawDisplayText = text?.replace(NEW_LINE_CHAR_PATTERN, BR_TAG)

            val spannedDisplayText: Spanned
            spannedDisplayText = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) { // Build.VERSION_CODES.N is 24
                Html.fromHtml(rawDisplayText, Html.FROM_HTML_MODE_LEGACY)
            } else {
                Html.fromHtml(rawDisplayText)
            }

            tv.apply {
                if (this is SteppedStoryCustomTextView){
                    movementMethod = SteppedStoryCustomTextView.
                            LocalLinkMovementMethod.getInstance()
                }
                tv.setLinkTextColor(Color.parseColor(color))
                setText(spannedDisplayText)
            }
        }
    }
}