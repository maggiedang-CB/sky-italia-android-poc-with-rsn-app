package com.nbcsports.regional.nbc_rsn.data_menu.viewholders

import android.graphics.Paint
import android.graphics.Rect
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.TextViewCompat
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.nbcsports.regional.nbc_rsn.R
import com.nbcsports.regional.nbc_rsn.data_menu.DataMenuContract
import kotlinx.android.synthetic.main.view_holder_data_menu_player_name.view.*
import kotlinx.android.synthetic.main.view_holder_data_menu_score_card.view.*
import kotlin.math.roundToInt

class DataMenuScoreCardVH(
        root: View,
        factory: DataMenuContract.ScoreCardFactory,
        viewType: Int
) : RecyclerView.ViewHolder(root) {

    init {
        val namePresenter = factory.getPlayerNamePresenter()
        itemView.list_player_name.adapter = DataMenuScorePlayerAdapter(namePresenter, viewType)
        itemView.list_player_name.layoutManager = LinearLayoutManager(root.context)
        itemView.longest_name.text = namePresenter.getLongestName(viewType)

        val paint = Paint()
        paint.typeface = ResourcesCompat.getFont(root.context, R.font.founders_grotesk_mono_app_regular)
        paint.textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12f, root.resources.displayMetrics)

        val statsPresenter = factory.getStatsPresenter()
        itemView.list_stats.adapter = DataMenuScoreStatsAdapter(factory.getStatsPresenter(), viewType, paint)
        itemView.list_stats.layoutManager = GridLayoutManager(root.context, statsPresenter.getRowCount(viewType), GridLayoutManager.HORIZONTAL, false)

        itemView.card_title.text = factory.getCardTitle(viewType).toLowerCase().split(" ").joinToString(" ") { it.capitalize() }
    }

    fun hideTitle() {
        itemView.card_title.visibility = View.GONE
    }

    fun refresh() {
        itemView.list_player_name.adapter?.notifyDataSetChanged()
        itemView.list_stats.adapter?.notifyDataSetChanged()
    }
}

class DataMenuScorePlayerAdapter(
        private val presenter: DataMenuContract.ScorePlayerPresenter,
        private val viewType: Int
) : RecyclerView.Adapter<DataMenuScorePlayerVH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataMenuScorePlayerVH {
        return DataMenuScorePlayerVH(LayoutInflater.from(parent.context).inflate(R.layout.view_holder_data_menu_player_name, parent, false))
    }

    override fun getItemCount(): Int {
        return presenter.getNameCount(viewType)
    }

    override fun onBindViewHolder(holder: DataMenuScorePlayerVH, position: Int) {
        presenter.bindView(holder, position, viewType)
    }

}

class DataMenuScorePlayerVH(root: View) : RecyclerView.ViewHolder(root),
        DataMenuContract.ScorePlayerView {
    override fun bind(name: String) {
        itemView.player_name.text = if (name.isEmpty()) "-" else name
    }
}

class DataMenuScoreStatsAdapter(
        private val presenter: DataMenuContract.ScoreStatsPresenter,
        private val viewType: Int,
        private val textPaint: Paint
) : RecyclerView.Adapter<DataMenuScoreStatsVH>() {

    private val textWidthMap: HashMap<String, Int> = HashMap(presenter.getColumnCount(viewType))

    override fun onCreateViewHolder(parent: ViewGroup, innerViewType: Int): DataMenuScoreStatsVH {
        val root = LayoutInflater.from(parent.context).inflate(R.layout.view_holder_data_menu_score_stats, parent, false)
        return if (innerViewType == 0) {
            DataMenuScoreStatsHeaderVH(root, viewType)
        } else {
            DataMenuScoreStatsVH(root)
        }
    }

    override fun getItemCount(): Int {
        return presenter.getRowCount(viewType) * presenter.getColumnCount(viewType)
    }

    override fun onBindViewHolder(holder: DataMenuScoreStatsVH, position: Int) {
        if (holder is DataMenuScoreStatsHeaderVH) {
            presenter.bindHeader(holder, position, viewType)
        } else {
            presenter.bindView(holder, position, viewType)
        }

        // adjust width
        val longestString = presenter.getColumnLongestString(position, viewType)
        if (!textWidthMap.containsKey(longestString)) {
            val textRect = Rect()
            textPaint.getTextBounds(longestString, 0, longestString.length, textRect)
            textWidthMap[longestString] = textRect.width()
        }

        textWidthMap[longestString]?.let {
            holder.itemView.post {
                holder.itemView.apply {
                    layoutParams.width = it + paddingStart + paddingEnd
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position % presenter.getRowCount(viewType) == 0) 0 else 1
    }

}



open class DataMenuScoreStatsVH(root: View) : RecyclerView.ViewHolder(root), DataMenuContract.ScoreStatsView {
    override fun bind(content: String) {
        (itemView as? AppCompatTextView)?.text = content
    }

    override fun bind(value: Float) {
        (itemView as? AppCompatTextView)?.text = if (value < 0) "-" else
                itemView.resources.getString(R.string.float_format, value)
    }

    override fun bind(value: Int) {
        (itemView as? AppCompatTextView)?.text = if (value < 0) "-" else value.toString()
    }
}

class DataMenuScoreStatsHeaderVH(root: View, viewType: Int) : DataMenuScoreStatsVH(root) {
    init {
        (root as? AppCompatTextView)?.apply {
            when {
                viewType < 0 -> TextViewCompat.setTextAppearance(this, R.style.DataMenuScoreColumnHeader)
                else -> TextViewCompat.setTextAppearance(this, R.style.DataMenuScoringSummaryColumnHeader)
            }
            setPadding(paddingLeft, paddingTop, paddingRight, resources.getDimension(R.dimen.datamenu_score_column_header_bottom_padding).roundToInt())
            invalidate()
        }
    }
}