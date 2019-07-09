package org.wycliffeassociates.translationrecorder.project.components;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by sarabiaj on 3/28/2017.
 */

public abstract class ProjectComponent implements Comparable, Parcelable {

    protected String mSlug;
    protected String mName;
    protected int mSort;

    public ProjectComponent(String slug, String name) {
        mSlug = slug;
        mName = name;
    }

    public ProjectComponent(String slug, String name, int sort) {
        mSlug = slug;
        mName = name;
        mSort = sort;
    }

    public String getSlug() {
        return mSlug;
    }

    public String getName() {
        return mName;
    }

    public int getSort() {
        return mSort;
    }

    public abstract String getLabel();

    @Override
    public int compareTo(Object another) {
        if(this instanceof Anthology) {
            int anotherSort = ((ProjectComponent)another).getSort();
            return mSort - anotherSort;
        } else {
            String anotherCode = ((ProjectComponent)another).getSlug();
            return mSlug.compareToIgnoreCase(anotherCode);
        }
    }

    public boolean displayMoreIcon() {
        return true;
    }

    public boolean displayItemIcon() {
        return true;
    }

    public ProjectComponent(Parcel in) {
        mSlug = in.readString();
        mName = in.readString();
    }
}
