package com.nbcsports.regional.nbc_rsn.authentication;

import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.nbcsports.regional.nbc_rsn.R;
import com.nbcsports.regional.nbc_rsn.authentication.authentication_views.AuthenticationLogoView;
import com.squareup.picasso.Picasso;

import timber.log.Timber;

public class ProviderLogoAdapter extends BaseAdapter {

    private ProviderInfo[] logoUrls;
    private final int TOP_PROVIDER_LOGO_COUNT = 9;

    public ProviderLogoAdapter(ProviderInfo[] logoUrls) {
        this.logoUrls = logoUrls;
    }

    public int getCount() {
        //Set item count according to annotations, if the list does not have enough item, display an empty grid place holder
        return TOP_PROVIDER_LOGO_COUNT;
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        AuthenticationLogoView imageView = (AuthenticationLogoView) LayoutInflater.from(parent.getContext()).inflate(R.layout.authentication_company_logo, parent, false);
        imageView.setBackgroundColor(Color.WHITE);

        if (position < logoUrls.length
                && logoUrls[position] != null
                && ! TextUtils.isEmpty(logoUrls[position].getLogoUrl())) {

            Picasso.get()
                    .load(logoUrls[position].getLogoUrl())
                    .fit()
                    .centerInside()
                    .into(imageView, new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {
                            //Do nothing, image loaded successfully
                        }

                        @Override
                        public void onError(Exception e) {
                            e.printStackTrace();
                            Timber.d("Error when loading the image");
                        }
                    });
        }
        return imageView;
    }
}

