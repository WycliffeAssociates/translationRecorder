package wycliffeassociates.recordingapp.FilesPage;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Environment;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import wycliffeassociates.recordingapp.ProjectManager.Project;
import wycliffeassociates.recordingapp.R;
import wycliffeassociates.recordingapp.SettingsPage.Settings;

/**
 * Created by sarabiaj on 3/15/2016.
 */
public class FileNameExtractor {
    private String mLang ="";
    private String mSource ="";
    private String mBook = "";
    private String mProject ="";
    private int mChap;
    private int mChunk;
    private int mStartVerse;
    private int mEndVerse;
    private int mTake;
    private int mBookNum;
    private boolean mMatched = false;

    public FileNameExtractor(String file){
        extractData(file);
    }

    public FileNameExtractor(File file){
        extractData(file.getName());
    }

    public FileNameExtractor(String lang, String source, String bookNum, String book, String project, String chapter,
                             String startV, String endV, String take){
        mLang = lang;
        mSource = source;
        try {
            mBookNum = Integer.parseInt(bookNum);
        } catch (NumberFormatException e){
            mBookNum = -1;
        }
        mBook = book;
        mProject = project;
        try{
            mChap = Integer.parseInt(chapter);
        } catch (NumberFormatException e){
            mChap = -1;
        }
        try {
            mStartVerse = Integer.parseInt(startV);
        } catch (NumberFormatException e){
            mStartVerse = -1;
        }
        try {
            mEndVerse = Integer.parseInt(endV);
        } catch (NumberFormatException e){
            mEndVerse = -1;
        }
        try{
            mTake = (take != null)? Integer.parseInt(take) : -1;
        } catch(NumberFormatException e){
            mTake = -1;
        }
    }

    private FileNameExtractor(Project project, int chapter, int startVerse, int endVerse){
        this(project.getTargetLanguage(), project.getSource(), project.getBookNumber(), project.getSlug(), project.getProject(), chapterIntToString(project, chapter), unitIntToString(startVerse),
                unitIntToString(endVerse), "00");
    }

    public static String chapterIntToString(Project project, int chapter){
        String result;
        if(project.getSlug().compareTo("psa") == 0){
            result = String.format("%03d", chapter);
        } else {
            result = String.format("%02d", chapter);
        }
        return result;
    }

    public static String unitIntToString(int unit){
        return String.format("%02d", unit);
    }


    public FileNameExtractor(SharedPreferences pref){
        this(pref.getString(Settings.KEY_PREF_LANG, ""),
                pref.getString(Settings.KEY_PREF_SOURCE, ""),
                pref.getString(Settings.KEY_PREF_BOOK_NUM, ""),
                pref.getString(Settings.KEY_PREF_BOOK, ""),
                pref.getString(Settings.KEY_PREF_PROJECT, ""),
                pref.getString(Settings.KEY_PREF_CHAPTER, ""),
                pref.getString(Settings.KEY_PREF_START_VERSE, ""),
                pref.getString(Settings.KEY_PREF_END_VERSE, ""),
                pref.getString(Settings.KEY_PREF_TAKE, ""));
    }

    private void extractData(String file){
        //includes the wav extention, could replace this with .*?
        //String FILENAME_PATTERN = "([a-zA-Z]{2,3}[-[\\d\\w]+]*)_([a-zA-Z]{3})_([1-3]*[a-zA-Z]+)_([0-9]{2})-([0-9]{2})(_([0-9]{2}))?.*";

        String UNDERSCORE = "_";
        String LANGUAGE = "([a-zA-Z]{2,3}[-[\\d\\w]+]*)";
        String PROJECT = "(([a-zA-Z]{3})_b([\\d]{2})_([1-3]*[a-zA-Z]+)|obs)";
        String CHAPTER = "c([\\d]{2,3})";
        String VERSE = "v([\\d]{2,3})(-([\\d]{2,3}))?";
        String TAKE = "(_t([\\d]{2}))?";
        String FILENAME_PATTERN = LANGUAGE + UNDERSCORE + PROJECT + UNDERSCORE + CHAPTER +
                UNDERSCORE + VERSE + TAKE + ".*";
        Pattern p = Pattern.compile(FILENAME_PATTERN);
        Matcher m = p.matcher(file);
        boolean found = m.find();
        //System.out.println("file is " + file + "\npattern is " + p.pattern());
        //m.group starts with the pattern, so the first group is at 1
        if(found){
            mLang = m.group(1);
            mProject = m.group(2);
            mSource = m.group(3);
            mBookNum = (m.group(4) != null)? Integer.parseInt(m.group(4)) : -1;
            mBook = m.group(5);
            mChap = Integer.parseInt(m.group(6));
            mStartVerse = Integer.parseInt(m.group(7));
            mEndVerse = (m.group(9) != null)? Integer.parseInt(m.group(9)) : -1;
            mTake = (m.group(11) != null)? Integer.parseInt(m.group(11)) : 0;
            mMatched = true;
        } else {
            mMatched = false;
        }
    }

    public String getProject(){
        return mProject;
    }

    public String getLang(){
        return mLang;
    }

    public String getSource(){
        return mSource;
    }

    public String getBook(){
        return mBook;
    }

    public int getBookNumber() {
        return mBookNum;
    }

    public int getChapter(){
        return mChap;
    }

    public int getStartVerse(){
        return mStartVerse;
    }

    public int getEndVerse(){
        return mEndVerse;
    }

    public int getChunk(){
        return mChunk;
    }

    public int getTake(){
        return mTake;
    }

    public boolean matched(){
        return mMatched;
    }

    public static File getDirectoryFromFile(SharedPreferences pref, File file){
        FileNameExtractor fne = new FileNameExtractor(file);
        String root = pref.getString("root_directory", "");
        File out = new File(new File(root), fne.getLang() + "/" + fne.getSource() + "/" + fne.getBook() + "/" + String.format("%02d", fne.getChapter()));
        return out;
    }

    public static File getDirectoryFromProject(Project project, int chapter){
        File root = new File(Environment.getExternalStorageDirectory(), "TranslationRecorder");
        return new File(root, project.getTargetLanguage() + "/" + project.getSource() + "/" + project.getSlug() + "/" + chapterIntToString(project, chapter));
    }

    public static File getFileFromFileName(SharedPreferences pref, File file){
        File dir = getDirectoryFromFile(pref, file);
        if(file.getName().contains(".wav")) {
            return new File(dir, file.getName());
        } else {
            return new File(dir, file.getName() + ".wav");
        }
    }

    public String getNameWithoutTake(){
        if(mProject != null && mProject.compareTo("obs") == 0){
            return mLang + "_obs_c" + String.format("%02d", mChap) + "_v" + String.format("%02d", mStartVerse);
        } else {
            String name;
            String end = (mEndVerse != -1 && mStartVerse != mEndVerse)? String.format("-%02d", mEndVerse) : "";
            if(mBook.compareTo("psa") == 0 && mChap != 119){
                name = mLang + "_" + mSource + "_b" + String.format("%02d", mBookNum) + "_" + mBook + "_c" + String.format("%03d", mChap) + "_v" + String.format("%02d", mStartVerse) + end;
            } else if(mBook.compareTo("psa") == 0){
                end = (mEndVerse != -1)? String.format("-%03d", mEndVerse) : "";
                name = mLang + "_" + mSource + "_b" + String.format("%02d", mBookNum) + "_" + mBook + "_c" + String.format("%03d", mChap) + "_v" + String.format("%03d", mStartVerse) + end;
            } else {
                name = mLang + "_" + mSource + "_b" + String.format("%02d", mBookNum) + "_" + mBook + "_c" + String.format("%02d", mChap) + "_v" + String.format("%02d", mStartVerse) + end;
            }
            return name;
        }
    }

    public static String getNameWithoutTake(String name){
        FileNameExtractor fne = new FileNameExtractor(name);
        return fne.getNameWithoutTake();
    }

    public static String getNameWithoutTake(SharedPreferences pref){
        FileNameExtractor fne = new FileNameExtractor(pref);
        return fne.getNameWithoutTake();
    }

    public static String getNameWithoutExtention(File file){
        String name = file.getName();
        if(name.contains(".wav")){
            name = name.replace(".wav", "");
        }
        return name;
    }

    public static File getFileFromFileName(SharedPreferences pref, String file){
        return getFileFromFileName(pref, new File(file));
    }

    public static int getLargestTake(File directory, File filename){
        File[] files = directory.listFiles();
        if(files == null){
            return 0;
        }
        FileNameExtractor inputFNE = new FileNameExtractor(filename);
        int maxTake = inputFNE.getTake();
        for(File f : files){
            FileNameExtractor fne = new FileNameExtractor(f);
            if((inputFNE.getNameWithoutTake()).compareTo((fne.getNameWithoutTake())) == 0){
                maxTake = (maxTake < fne.getTake())? fne.getTake() : maxTake;
            }
        }
        return maxTake;
    }

    private static int getLargestTake(File directory, String nameWithoutTake){
        File[] files = directory.listFiles();
        if(files == null){
            return 0;
        }
        int maxTake = 0;
        for(File f : files){
            FileNameExtractor fne = new FileNameExtractor(f);
            if(nameWithoutTake.compareTo((fne.getNameWithoutTake())) == 0){
                maxTake = (maxTake < fne.getTake())? fne.getTake() : maxTake;
            }
        }
        return maxTake;
    }

    public static File createFile(Project project, int chapter, int startVerse, int endVerse) {
        FileNameExtractor fne = new FileNameExtractor(project, chapter, startVerse, endVerse);
        File dir = fne.getDirectoryFromProject(project, chapter);
        String nameWithoutTake = fne.getNameWithoutTake();
        int take = fne.getLargestTake(dir, nameWithoutTake)+1;
        return new File(dir, nameWithoutTake + "_t" + String.format("%02d", take) + ".wav");

    }
}
