package wycliffeassociates.recordingapp.FilesPage;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import wycliffeassociates.recordingapp.R;
import wycliffeassociates.recordingapp.SettingsPage.Settings;

/**
 * Created by sarabiaj on 3/15/2016.
 */
public class FileNameExtractor {
    private String mLang ="";
    private String mSource ="";
    private String mBook ="";
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
        mBookNum = (bookNum != null)? Integer.parseInt(bookNum) : -1;
        mBook = book;
        mProject = project;
        mChap = (chapter != null)? Integer.parseInt(chapter) : -1;
        mStartVerse = (startV != null)? Integer.parseInt(startV) : -1;
        mEndVerse = (endV != null)? Integer.parseInt(endV) : -1;
        mTake = (take != null)? Integer.parseInt(take) : -1;
    }

    public FileNameExtractor(SharedPreferences pref){
        this(pref.getString(Settings.KEY_PREF_LANG, null),
                pref.getString(Settings.KEY_PREF_SOURCE, null),
                pref.getString(Settings.KEY_PREF_BOOK_NUM, null),
                pref.getString(Settings.KEY_PREF_BOOK, null),
                pref.getString(Settings.KEY_PREF_PROJECT, null),
                pref.getString(Settings.KEY_PREF_CHAPTER, null),
                pref.getString(Settings.KEY_PREF_START_VERSE, null),
                pref.getString(Settings.KEY_PREF_END_VERSE, null),
                pref.getString(Settings.KEY_PREF_TAKE, null));
    }

    private void extractData(String file){
        //includes the wav extention, could replace this with .*?
        //String FILENAME_PATTERN = "([a-zA-Z]{2,3}[-[\\d\\w]+]*)_([a-zA-Z]{3})_([1-3]*[a-zA-Z]+)_([0-9]{2})-([0-9]{2})(_([0-9]{2}))?.*";

        String UNDERSCORE = "_";
        String LANGUAGE = "([a-zA-Z]{2,3}[-[\\d\\w]+]*)";
        String PROJECT = "(([a-zA-Z]{3})_b([0-9]{2})_([1-3]*[a-zA-Z]+)|obs)";
        String CHAPTER = "c([0-9]{2,3})";
        String VERSE = "v([0-9]{2,3})(-([0-9]{2,3}))?";
        String TAKE = "(_t([0-9]{2}))?";
        String FILENAME_PATTERN = LANGUAGE + UNDERSCORE + PROJECT + UNDERSCORE + CHAPTER +
                UNDERSCORE + VERSE + TAKE + ".*";
        Pattern p = Pattern.compile(FILENAME_PATTERN);
        Matcher m = p.matcher(file);
        boolean found = m.find();
        System.out.println("file is " + file + "\npattern is " + p.pattern());
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
            String end = (mEndVerse != -1)? String.format("-%02d", mEndVerse) : "";
            if(mBook.compareTo("psa") == 0 && mChap != 119){
                name = mLang + "_" + mSource + "_b" + mBookNum + "_" + mBook + "_c" + String.format("%03d", mChap) + "_v" + String.format("%02d", mStartVerse) + end;
            } else if(mBook.compareTo("psa") == 0){
                end = (mEndVerse != -1)? String.format("-%03d", mEndVerse) : "";
                name = mLang + "_" + mSource + "_b" + mBookNum + "_" + mBook + "_c" + String.format("%03d", mChap) + "_v" + String.format("%03d", mStartVerse) + end;
            } else {
                name = mLang + "_" + mSource + "_b" + mBookNum + "_" + mBook + "_c" + String.format("%02d", mChap) + "_v" + String.format("%02d", mStartVerse) + end;
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
        FileNameExtractor fne = new FileNameExtractor(filename);
        String inLang = fne.getLang();
        String inSource = fne.getSource();
        String inBook = fne.getBook();
        int inChap = fne.getChapter();
        int inChunk = fne.getChunk();
        int maxTake = fne.getTake();
        for(File f : files){
            fne = new FileNameExtractor(f);
            //check in order of most unique to least unique
            //ie. more files will share the same language name than chunk number
            if(inChunk == fne.getChunk()){
                if(inChap == fne.getChapter()){
                    if(inBook.compareTo(fne.getBook()) == 0){
                        if(inSource.compareTo(fne.getSource()) == 0){
                            if(inLang.compareTo(fne.getLang()) == 0){
                                if(fne.getTake() > maxTake){
                                    maxTake = fne.getTake();
                                }
                            }
                        }
                    }
                }
            }
        }
        return maxTake;
    }
}
