package wycliffeassociates.recordingapp.model;

import android.app.Application;

import java.util.ArrayList;

public class LanguageHolder extends Application {
    private ArrayList<Language> languageList;

    public ArrayList<Language> getData(){ return languageList; }
    public void setData(ArrayList<Language> languageList){ this.languageList = languageList; }

}