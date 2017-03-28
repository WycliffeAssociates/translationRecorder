package org.wycliffeassociates.translationrecorder.project.components;

/**
 * Created by sarabiaj on 3/28/2017.
 */

public abstract class ProjectComponent implements Comparable {

    protected String mSlug;
    protected String mName;

    public ProjectComponent(String slug, String name) {
        mSlug = slug;
        mName = name;
    }

    public String getSlug() {
        return mSlug;
    }

    public String getName() {
        return mName;
    }

    public abstract String getLabel();

    @Override
    public int compareTo(Object another) {
        String anotherCode = ((ProjectComponent)another).getSlug();
        return mSlug.compareToIgnoreCase(anotherCode);
    }

    public boolean displayMoreIcon() {
        return true;
    }

    public boolean displayItemIcon() {
        return true;
    }
}
