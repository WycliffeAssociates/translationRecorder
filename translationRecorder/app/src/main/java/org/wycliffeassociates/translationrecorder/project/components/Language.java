package org.wycliffeassociates.translationrecorder.project.components;

import android.content.Context;

import org.wycliffeassociates.translationrecorder.Utils;
import org.wycliffeassociates.translationrecorder.database.ProjectDatabaseHelper;

public class Language extends ProjectComponent {

    public Language(String slug, String name){
        super(slug, name);
    }

    public static Language[] getLanguages(Context ctx) {
        ProjectDatabaseHelper db = new ProjectDatabaseHelper(ctx);
        Language[] languages =  db.getLanguages();
        db.close();
        return languages;
    }

    @Override
    public String getLabel(){
        return Utils.capitalizeFirstLetter(mName);
    }

    @Override
    public boolean displayItemIcon() {
        return false;
    }
}