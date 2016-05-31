package wycliffeassociates.recordingapp.FileManagerUtils;

import android.app.Application;

import java.util.ArrayList;

import wycliffeassociates.recordingapp.project.Language;

public class LanguageHolder extends Application {
    private ArrayList<Language> languageList;

    public ArrayList<Language> getData(){ return languageList; }
    public void setData(ArrayList<Language> languageList){ this.languageList = languageList; }

}