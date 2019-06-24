package com.nbcsports.regional.nbc_rsn.persistentplayer.intent;

import android.content.Context;
import androidx.fragment.app.FragmentActivity;
import android.view.View;

import com.nbcsports.regional.nbc_rsn.persistentplayer.PersistentPlayerBottomSheet;

public class OnKebabClickListener implements View.OnClickListener {
    private final PersistentPlayerBottomSheet bottomSheet;
    private final Context context;

    public OnKebabClickListener(PersistentPlayerBottomSheet bottomSheet, Context context) {
        this.bottomSheet = bottomSheet;
        this.context = context;
    }

    @Override
    public void onClick(View view) {
        bottomSheet.show(((FragmentActivity) context).getSupportFragmentManager(), "persistent.player.botom.sheet");
    }
}
