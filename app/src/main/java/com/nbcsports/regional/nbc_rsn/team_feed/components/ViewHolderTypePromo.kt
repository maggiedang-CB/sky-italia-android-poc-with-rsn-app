package com.nbcsports.regional.nbc_rsn.team_feed.components

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.view.View
import android.widget.RelativeLayout
import com.nbcsports.regional.nbc_rsn.R
import com.nbcsports.regional.nbc_rsn.common.ExternalLinkProvider
import com.nbcsports.regional.nbc_rsn.common.IntentHelper
import com.nbcsports.regional.nbc_rsn.extensions.e
import com.nbcsports.regional.nbc_rsn.team_view.PeacockImageView
import kotlinx.android.synthetic.main.teamview_card_type_promo.view.*

class ViewHolderTypePromo internal constructor(view: View, viewType: Int) : ViewHolderTypeBase(view, viewType), ExternalLinkProvider {

    var externalPromoImageView: PeacockImageView? = null
    var externalPromoForegroundRelativeLayout: RelativeLayout? = null

    val foregroundAlpha = 0.8f

    init {
        externalPromoImageView = view.findViewById(R.id.external_promo_image_view)
        externalPromoForegroundRelativeLayout = view.findViewById(R.id.external_promo_foreground_relative_layout)
    }

    override fun getExternalLink(): String {
        return mItem.streamUrl ?: ""
    }

    fun setCardAttributes(playNonFeatureGifs: Boolean, primaryColor: String, intentHelper: IntentHelper) {
        itemView.title.text = mItem.title
        checkAndAdjustTitleTextSize(itemView.title)
        itemView.description.text = mItem.description
        externalPromoImageView?.apply {
            loadImage(context, playNonFeatureGifs, mItem.imageAssetUrl, primaryColor, null)
        }
        externalPromoForegroundRelativeLayout?.apply {
            setBackgroundColor(Color.parseColor(primaryColor))
            alpha = foregroundAlpha
        }
        itemView.setOnClickListener {
            mItem?.streamUrl?.apply {
                if (!this.isEmpty()){
                    try {
                        val uri: Uri = Uri.parse(this)
                        val browserIntent = Intent(Intent.ACTION_VIEW, uri)
                        intentHelper.startActivity(browserIntent)
                    } catch (e: Exception){
                        e(e)
                    }
                }
            }
        }
    }
}