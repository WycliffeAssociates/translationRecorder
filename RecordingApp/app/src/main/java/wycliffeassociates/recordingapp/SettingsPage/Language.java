package wycliffeassociates.recordingapp.SettingsPage;

public class Language {

    private String mLanguageCode;
    private String mLanguageName;

    public Language(String lc, String ln){
        mLanguageCode = lc;
        mLanguageName = ln;
    }

    public String getCode(){
        return mLanguageCode;
    }
    public String getName(){
        return mLanguageName;
    }
}