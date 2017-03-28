package org.wycliffeassociates.translationrecorder.project.components;

import org.wycliffeassociates.translationrecorder.Utils;

/**
 * Created by sarabiaj on 3/22/2017.
 */

public class Version extends ProjectComponent {

    public Version(String slug, String name) {
        super(slug, name);
    }

    @Override
    public String getLabel() {
        String label = "";
        label += mSlug.toUpperCase();
        label += ":";
        String[] resourceLabels = mName.split(" ");
        for(String part : resourceLabels) {
            label += " " + Utils.capitalizeFirstLetter(part);
        }
        return label;
    }
}
