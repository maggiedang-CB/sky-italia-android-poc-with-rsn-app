package com.nbcsports.regional.nbc_rsn.authentication;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.nbcsports.regional.nbc_rsn.R;
import com.nbcsports.regional.nbc_rsn.RsnApplication;

public class ProviderNameViewHolder extends RecyclerView.ViewHolder {
    TextView providerTextview;
    ProviderInfo providerInfo;
    ProviderNameListener onClickListener;

    public ProviderNameViewHolder(View itemView) {
        super(itemView);
        providerTextview = (TextView) itemView;
        providerTextview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                providerTextview.setClickable(false);
                if (!providerInfo.getMvpdName().equalsIgnoreCase(RsnApplication.getInstance().getString(R.string.stream_authentication_empty_name_indicator))) {
                    onClickListener.onItemClick(providerInfo);
                }
            }
        });
    }

    public void bindTo(ProviderInfo providerInfo) {
        //Removed empty check since already done in StreamAuthenticationFragment

        this.providerInfo = providerInfo;
        if (providerInfo.getMvpdName().equals(RsnApplication.getInstance().getString(R.string.stream_authentication_empty_name_indicator))) {
            providerTextview.setText("");
        } else {
            providerTextview.setText(providerInfo.getMvpdName());
        }
    }

    public void setOnClickListener(ProviderNameListener onClickListener) {
        this.onClickListener = onClickListener;
    }
}
