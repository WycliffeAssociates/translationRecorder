package org.wycliffeassociates.translationrecorder.project;

/**
 * Created by sarabiaj on 3/22/2017.
 */

public class Version {

    private String mSlug;
    private String mName;

    public Version(String slug, String name) {
        mSlug = slug;
        mName = name;
    }

    public String getName() {
        return mName;
    }

    public String getSlug() {
        return mSlug;
    }
}
