package com.nbcsports.regional.nbc_rsn.authentication;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.nbcsports.regional.nbc_rsn.R;
import com.nbcsports.regional.nbc_rsn.RsnApplication;

import java.util.ArrayList;

public class ProviderNameAdapter extends RecyclerView.Adapter<ProviderNameViewHolder> {
    private ArrayList<ProviderInfo> providerName = new ArrayList<>();
    private ProviderNameListener providerInfoListener;
    private int itemRequired;

    ProviderNameAdapter(ArrayList<ProviderInfo> providerName, ProviderNameListener providerInfoListener, int itemRequired) {
        this.providerName = providerName;
        this.providerInfoListener = providerInfoListener;
        this.itemRequired = itemRequired;
    }

    @NonNull
    @Override
    public ProviderNameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ProviderNameViewHolder providerNameViewHolder = new ProviderNameViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.authentication_company_name, parent, false));
        providerNameViewHolder.setOnClickListener(providerInfoListener);
        return providerNameViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ProviderNameViewHolder holder, int position) {
        holder.bindTo(providerName.get(position));
    }

    @Override
    public int getItemCount() {
        return providerName.size();
    }

    public void setCompanyInfo(ArrayList<ProviderInfo> filteredCompanyInfos) {
        providerName.clear();

        if (filteredCompanyInfos.size() == 0) {
            providerName.add(new ProviderInfo(RsnApplication.getInstance().getResources().getString(R.string.authentication_search_no_result_found)));
        }
        providerName.addAll(filteredCompanyInfos);
        for (int i = 0; i < (itemRequired - filteredCompanyInfos.size()); i++) {
            //Adding place holder items to ensure the search bar stay at top of page, if there is not enough item inside Recyclerview
            providerName.add(new ProviderInfo());
        }
        notifyDataSetChanged();
    }

    public void setProviderInfoListener(ProviderNameListener providerNameListener) {
        this.providerInfoListener = providerNameListener;
    }
}
