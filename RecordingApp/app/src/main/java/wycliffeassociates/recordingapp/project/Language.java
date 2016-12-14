package wycliffeassociates.recordingapp.project;

import android.content.Context;

import wycliffeassociates.recordingapp.SettingsPage.Settings;
import wycliffeassociates.recordingapp.database.ProjectDatabaseHelper;

public class Language implements Comparable {

    private String mLanguageCode;
    private String mLanguageName;

    public Language(String lc, String ln){
        mLanguageCode = lc;
        mLanguageName = ln;
    }

    @Override
    public int compareTo(Object another) {
        String anotherCode = ((Language)another).getCode();
        return mLanguageCode.compareToIgnoreCase(anotherCode);
    }

    public String getCode(){
        return mLanguageCode;
    }
    public String getName(){
        return mLanguageName;
    }

    public static Language[] getLanguages(Context ctx) {
        ProjectDatabaseHelper db = new ProjectDatabaseHelper(ctx);
        Language[] languages =  db.getLanguages();
        db.close();
        return languages;
    }
}