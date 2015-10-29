package wycliffeassociates.recordingapp.SettingsPage;

public class Language {

    private boolean mGatewayLangauge;
    private String mLanguageDirection;
    private String mLanguageCode;
    private String mLanguageName;
    private String mCountryCode;
    private int mPrimaryKey;


    public Language(boolean gw, String ld, String lc, String ln, String cc, int pk){
        mGatewayLangauge = gw;
        mLanguageDirection = ld;
        mLanguageCode = lc;
        mLanguageName = ln;
        mCountryCode = cc;
        mPrimaryKey = pk;
    }

    public Language(Boolean gw){
        //pull langconfig
        mGatewayLangauge = gw;
        mLanguageDirection = null;
        mLanguageCode = null;
        mLanguageName = "spencer";
        mCountryCode = null;
        mPrimaryKey = -1;
    }

    public Language(){
        //pull langconfig
        mGatewayLangauge = false;
        mLanguageDirection = null;
        mLanguageCode = null;
        mLanguageName = "spencer";
        mCountryCode = null;
        mPrimaryKey = -1;
    }

    public Boolean getGatewayLangauge(){
        return mGatewayLangauge;
    }
    public String getLanguageDirection(){
        return mLanguageDirection;
    }
    public String getCode(){
        return mLanguageCode;
    }
    public String getName(){
        return mLanguageName;
    }
    public String getCountryCode(){
        return mCountryCode;
    }
    public int getPrimaryKey(){
        return mPrimaryKey;
    }
    public void setPrimaryKey(int pk){
        mPrimaryKey = pk;
    }
}