package org.wycliffeassociates.translationrecorder.project.components;

import android.os.Parcel;
import android.os.Parcelable;

import org.wycliffeassociates.translationrecorder.Utils;

/**
 * Created by sarabiaj on 3/28/2017.
 */

public class Anthology extends ProjectComponent implements Parcelable {

    private String mPluginClassName;
    private String mPluginJarName;
    private String mResource;
    private int mSort;
    private String mRegex;
    private String mGroups;
    private String mMask;

    public Anthology(String slug, String name, String resource, int sort,
                     String regex, String groups, String mask, String pluginJarName, String pluginClassName) {
        super(slug, name, sort);
        mResource = resource;
        mSort = sort;
        mRegex = regex;
        mGroups = groups;
        mMask = mask;
        mPluginJarName = pluginJarName;
        mPluginClassName = pluginClassName;
    }

    public String getMask(){
        return mMask;
    }

    public String getResource(){
        return mResource;
    }

    public int getSort(){
        return mSort;
    }

    public String getRegex() {
        return mRegex;
    }

    public String getMatchGroups(){
        return mGroups;
    }

    public String getPluginClassName(){
        return mPluginClassName;
    }

    public String getPluginFilename(){
        return mPluginJarName;
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
        dest.writeInt(mSort);
        dest.writeString(mRegex);
        dest.writeString(mGroups);
        dest.writeString(mMask);
        dest.writeString(mPluginJarName);
        dest.writeString(mPluginClassName);
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
        mSort = in.readInt();
        mRegex = in.readString();
        mGroups = in.readString();
        mMask = in.readString();
        mPluginJarName = in.readString();
        mPluginClassName = in.readString();
    }
}
