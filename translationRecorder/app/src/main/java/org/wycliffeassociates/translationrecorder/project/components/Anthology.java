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
    private String mFormat;
    private int mMask;

    int LANGUAGE =      0b0111111111;
    int RESOURCE =      0b1011111111;
    int ANTHOLOGY =     0b1101111111;
    int VERSION =       0b1110111111;
    int BOOK_NUMBER =   0b1111011111;
    int BOOK =          0b1111101111;
    int CHAPTER =       0b1111110111;
    int START_VERSE =   0b1111111011;
    int END_VERSE =     0b1111111101;
    int TAKE =          0b1111111110;
    int MATCH =         0b1111111111;

    public Anthology(String slug, String name, String resource, String regex, String groups, String mask) {
        super(slug, name);
        mResource = resource;
        mRegex = regex;
        mGroups = groups;
        mMask = Integer.parseInt(mask, 2);
        mFormat = computeFormat(mMask);
    }

    public String computeFormat(int mask) {
        StringBuilder sb = new StringBuilder();
        if((mask | LANGUAGE) == MATCH) {
            sb.append("%s_");
        }
        if((mask | RESOURCE) == MATCH) {
            sb.append("%s_");
        }
        if((mask | ANTHOLOGY) == MATCH) {
            sb.append("%s_");
        }
        if((mask | VERSION) == MATCH) {
            sb.append("%s_");
        }
        if((mask | BOOK_NUMBER) == MATCH) {
            sb.append("b%02d_");
        }
        if((mask | BOOK) == MATCH) {
            sb.append("%s_");
        }
        if((mask | CHAPTER) == MATCH) {
            sb.append("c%02d_");
        }
        if((mask | START_VERSE) == MATCH) {
            sb.append("v%02d");
        }
        if((mask | END_VERSE) == MATCH) {
            sb.append("-%02d");
        }
        if((mask | TAKE) == MATCH) {
            sb.append("_t%02d");
        }
        return sb.toString();
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
