package wycliffeassociates.recordingapp.ProjectManager;

/**
 * Created by sarabiaj on 5/10/2016.
 */
public class Project {

    String mTargetLang;
    String mSrcLang;
    String mSlug;
    String mSource;
    String mMode;

    public Project(){

    }

    public Project(String tLang, String sLang, String slug, String src, String mode){
        mTargetLang = tLang;
        mSrcLang = sLang;
        mSlug = slug;
        mSource = src;
        mMode = mode;
    }

    public String getTargetLang(){
        return mTargetLang;
    }

    public String getSrcLang(){
        return mSrcLang;
    }

    public String getSlug(){
        return mSlug;
    }

    public String getSource(){
        return mSource;
    }

    public String getMode(){
        return mMode;
    }

    public void setTargetLanguage(String target){
        mTargetLang = target;
    }

    public void setSourceLanguage(String source){
        mSrcLang = source;
    }

    public void setSource(String source){
        mSource = source;
    }

    public void setSlug(String slug){
        mSlug = slug;
    }

    public void setMode(String mode){
        mMode = mode;
    }
}
