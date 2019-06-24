package com.nbcsports.regional.nbc_rsn.authentication;

import androidx.annotation.NonNull;

import com.nbcsports.regional.nbc_rsn.R;
import com.nbcsports.regional.nbc_rsn.RsnApplication;

import lombok.Getter;
import lombok.Setter;

public class ProviderInfo implements Comparable<ProviderInfo> {
    private String mvpdName;
    @Getter
    private String mvpdID;
    private String status;
    private String mvpdUrl;
    private String logoUrl;

    @Setter
    @Getter
    private String mvpdBarLogoUrl;

    ProviderInfo() {
        this.mvpdName = RsnApplication.getInstance().getString(R.string.stream_authentication_empty_name_indicator);
        this.mvpdID = "";
        this.mvpdUrl = "";
        this.status = "";
        this.logoUrl = "";
    }

    ProviderInfo(String mvpdName) {
        this.mvpdName = mvpdName;
        this.mvpdID = "";
        this.mvpdUrl = "";
        this.status = "";
        this.logoUrl = "";
    }

    ProviderInfo(String mvpdName, String mvpdID, String status, String mvpdUrl) {
        this.mvpdName = mvpdName;
        this.mvpdID = mvpdID;
        this.mvpdUrl = mvpdUrl;
        this.status = status;
        this.logoUrl = "";
    }

    public String getMvpdName() {
        return mvpdName;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    @Override
    public int compareTo(@NonNull ProviderInfo o) {
        return mvpdName.compareToIgnoreCase(o.getMvpdName());
    }
}
