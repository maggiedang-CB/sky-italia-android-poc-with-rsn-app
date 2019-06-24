package com.nbcsports.regional.nbc_rsn.persistentplayer.ads;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;

public class Midroll implements Parcelable {

    public static class Collection extends ArrayList<Midroll> {

    }
    @Expose long cueID;
    @Expose
    String cueType;
    @Expose
    String key;
    @Expose
    String value;
    @Expose boolean pictureByPictureAd;

    public long getCueID() {
        return cueID;
    }

    public void setCueID(long cueID) {
        this.cueID = cueID;
    }

    public String getCueType() {
        return cueType;
    }

    public void setCueType(String cueType) {
        this.cueType = cueType;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isPictureByPictureAd() {
        return pictureByPictureAd;
    }

    public void setPictureByPictureAd(boolean pictureByPictureAd) {
        this.pictureByPictureAd = pictureByPictureAd;
    }

    public String getFreewheelParam() {
        String param = getKey() + "=" + getValue();
        // if there is no param, bail out
        if (param.length() == 1) {
            param = "";
        }
        return param;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(cueID);
        dest.writeString(cueType);
        dest.writeString(key);
        dest.writeString(value);
        dest.writeString(pictureByPictureAd ? "1" : "0");
    }

    public static final Parcelable.Creator<Midroll> CREATOR = new Parcelable.Creator<Midroll>() {
        @Override
        public Midroll[] newArray(final int size) {
            return new Midroll[size];
        }

        @Override
        public Midroll createFromParcel(final Parcel source) {
            final Midroll copy = new Midroll();
            copy.cueID = source.readLong();
            copy.cueType = source.readString();
            copy.key = source.readString();
            copy.value = source.readString();
            copy.pictureByPictureAd = source.readString().equals("1");
            return copy;
        }
    };

    @Override
    public String toString() {
        return "Midroll{" +
                "cueID=" + cueID +
                ", cueType='" + cueType + '\'' +
                ", key='" + key + '\'' +
                ", value='" + value + '\'' +
                ", pictureByPictureAd=" + pictureByPictureAd +
                '}';
    }
}

