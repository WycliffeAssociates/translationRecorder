package org.wycliffeassociates.translationrecorder.project.components;

import android.os.Parcel;
import android.os.Parcelable;

import org.wycliffeassociates.translationrecorder.Utils;

/**
 * Created by sarabiaj on 3/28/2017.
 */

public class Anthology extends ProjectComponent implements Parcelable {

    private String mResource;
    private String mRegex;
    private String mGroups;

    public Anthology(String slug, String name, String resource, String regex, String mask) {
        super(slug, name);
        mResource = resource;
        mRegex = regex;
        mGroups = mask;
    }

    public String getResource(){
        return mResource;
    }

    public String getRegex() {
        return mRegex;
    }

    public String getMatchGroups(){
        return mGroups;
    }

    @Override
    public String getLabel() {
        String label = "";
        label += Utils.capitalizeFirstLetter(mResource);
        label += ":";
        String[] resourceLabels = mName.split(" ");
        for(String part : resourceLabels) {
            label += " " + Utils.capitalizeFirstLetter(part);
        }
        return label;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mSlug);
        dest.writeString(mName);
        dest.writeString(mResource);
        dest.writeString(mRegex);
        dest.writeString(mGroups);
    }

    public static final Parcelable.Creator<Anthology> CREATOR = new Parcelable.Creator<Anthology>() {
        public Anthology createFromParcel(Parcel in) {
            return new Anthology(in);
        }

        public Anthology[] newArray(int size) {
            return new Anthology[size];
        }
    };

    public Anthology(Parcel in) {
        super(in);
        mResource = in.readString();
        mRegex = in.readString();
        mGroups = in.readString();
    }
}
