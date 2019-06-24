package com.nbcsports.regional.nbc_rsn.editorial_detail.components;

import android.graphics.Color;
import android.os.Build;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

import com.nbcsports.regional.nbc_rsn.R;
import com.nbcsports.regional.nbc_rsn.common.Team;
import com.nbcsports.regional.nbc_rsn.editorial_detail.models.EditorialDetailItem;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ViewHolderTypeBodyText extends ViewHolderEditorialTypeBase {

    private Team team;
    protected final static String BR_TAG = "<br>";
    protected final static String NEW_LINE_CHAR_PATTERN = "(\r)?\n";

    @BindView(R.id.displayText)
    TextView displayText;

    public ViewHolderTypeBodyText(View view, Team team, int itemViewType) {
        super(view, itemViewType);

        ButterKnife.bind(this, view);
        this.team = team;
    }

    public void setAttributes() {
        // The following line is to enable hyperlinks like <a href="http://someawesomesite.com">link</a>
        // (because its TextView xml version android:autoLink="all" did not work for me),
        // Do not use both java and xml simultaneously.
        displayText.setMovementMethod(LinkMovementMethod.getInstance());

        // Set color of links in html text, if any.
        //TODO: Do we need to ensure "body copy text links will use the team's primary color as an underline",
        //TODO: as per Zachary Ruden's comment to RSNAPP-272?
        displayText.setLinkTextColor(Color.parseColor(team.getPrimaryColor()));

        // Finally, set the html text to the TextView
        String rawDisplayText = editorialDetailItem.getBodyText();
        rawDisplayText = rawDisplayText.replaceAll(NEW_LINE_CHAR_PATTERN, BR_TAG);

        Spanned spannedDisplayText;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) { // Build.VERSION_CODES.N is 24
            spannedDisplayText = Html.fromHtml(rawDisplayText, Html.FROM_HTML_MODE_LEGACY);
        } else {
            spannedDisplayText = Html.fromHtml(rawDisplayText);
        }
        displayText.setText(spannedDisplayText);
    }

    @Override
    public void bind(EditorialDetailItem editorialDetailItem) {
        super.bind(editorialDetailItem);
        setAttributes();
    }
}

