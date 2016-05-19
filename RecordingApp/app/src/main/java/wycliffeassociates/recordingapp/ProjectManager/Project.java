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
    String mProject;
    String mContributors;
    String mBookNum;

    public Project(){
    }

    public Project(String tLang, String sLang, String bookNum, String slug, String src, String mode, String project, String contributors){
        mTargetLang = tLang;
        mSrcLang = sLang;
        mSlug = slug;
        mBookNum = bookNum;
        mSource = src;
        mMode = mode;
        mProject = project;
        mContributors = contributors;
    }

    public Project(String tLang, String sLang, int bookNum, String slug, String src, String mode, String project, String contributors){
        mTargetLang = tLang;
        mSrcLang = sLang;
        mSlug = slug;
        mBookNum = String.valueOf(bookNum);
        mSource = src;
        mMode = mode;
        mProject = project;
        mContributors = contributors;
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

    public String getContributors(){
        return mContributors;
    }

    public String getBookNumber(){
        return mBookNum;
    }

    public String getProject(){
        return mProject;
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

    public void setProject(String project){
        mProject = project;
    }

    public void setContributors(String contributors){
        mContributors = contributors;
    }

    public void setBookNumber(String bookNumber){
        mBookNum = bookNumber;
    }

    public void setBookNumber(int bookNumber){
        mBookNum = String.valueOf(bookNumber);
    }
}
