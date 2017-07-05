package org.wycliffeassociates.translationrecorder.project.components;

import android.os.Parcel;
import android.os.Parcelable;

import org.wycliffeassociates.translationrecorder.Utils;

/**
 * Created by sarabiaj on 7/5/2017.
 */

public class Mode extends ProjectComponent {

    private String mType;

    public Mode(String slug, String name, String type) {
        super(slug, name);
        mType = type;
    }

    public Mode(Parcel in) {
        super(in);
        mType = in.readString();
    }

    @Override
    public String getLabel() {
        return Utils.capitalizeFirstLetter(mName);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mSlug);
        parcel.writeString(mName);
        parcel.writeString(mType);
    }

    public static final Parcelable.Creator<Mode> CREATOR = new Parcelable.Creator<Mode>() {
        public Mode createFromParcel(Parcel in) {
            return new Mode(in);
        }

        public Mode[] newArray(int size) {
            return new Mode[size];
        }
    };
}
