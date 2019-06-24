package com.nbcsports.regional.nbc_rsn.editorial_detail.components

import android.graphics.Color
import android.os.Build
import android.text.Html
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.view.View
import com.nbcsports.regional.nbc_rsn.common.Team
import com.nbcsports.regional.nbc_rsn.editorial_detail.models.EditorialDetailItem
import kotlinx.android.synthetic.main.component_editorial_drop_cap.view.*

class ViewHolderTypeDropCap(view: View?, val team: Team, viewType: Int) : ViewHolderEditorialTypeBase(view, viewType) {

    private val BR_TAG = "<br>"
    private val NEW_LINE_CHAR_PATTERN = "(\r)?\n"

    fun setAttributes() {
        // set the html text to the TextView
        var rawDisplayText = editorialDetailItem.bodyText
        rawDisplayText = rawDisplayText.replace(NEW_LINE_CHAR_PATTERN.toRegex(), BR_TAG)

        val spannedDisplayText: Spanned
        spannedDisplayText = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) { // Build.VERSION_CODES.N is 24
            Html.fromHtml(rawDisplayText, Html.FROM_HTML_MODE_LEGACY)
        } else {
            Html.fromHtml(rawDisplayText)
        }

        itemView.dropCapBodyText.apply {
            movementMethod = LinkMovementMethod.getInstance()

            // Set color of links in html text, if any.
            //TODO: Do we need to ensure "body copy text links will use the team's primary color as an underline",
            //TODO: as per Zachary Ruden's comment to RSNAPP-272?
            setLinkTextColor(Color.parseColor(team.primaryColor))

            text = spannedDisplayText
        }
    }

    override fun bind(editorialDetailItem: EditorialDetailItem?) {
        super.bind(editorialDetailItem)
        setAttributes()

        val bodyText = itemView.dropCapBodyText.text
        itemView.dropCapBodyText.text = bodyText?.substring(1)

        itemView.dropCapLetter?.apply {
            text = bodyText?.get(0).toString()
            setTextColor(Color.parseColor(team.primaryColor))
        }

    }
}
