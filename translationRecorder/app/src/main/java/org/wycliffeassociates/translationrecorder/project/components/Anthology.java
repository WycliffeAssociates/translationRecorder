package org.wycliffeassociates.translationrecorder.project.components;

import org.wycliffeassociates.translationrecorder.Utils;

/**
 * Created by sarabiaj on 3/28/2017.
 */

public class Anthology extends ProjectComponent {

    private String mResource;

    public Anthology(String slug, String name, String resource) {
        super(slug, name);
        mResource = resource;
    }

    public String getResource(){
        return mResource;
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
}
