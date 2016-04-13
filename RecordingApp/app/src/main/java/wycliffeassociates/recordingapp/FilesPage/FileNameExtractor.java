package wycliffeassociates.recordingapp.FilesPage;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by sarabiaj on 3/15/2016.
 */
public class FileNameExtractor {
    private String mLang ="";
    private String mSource ="";
    private String mBook ="";
    private int mChap;
    private int mChunk;
    private int mTake;
    private boolean mMatched = false;

    public FileNameExtractor(String file){
        extractData(file);
    }

    public FileNameExtractor(File file){
        extractData(file.getName());
    }

    private void extractData(String file){
        //includes the wav extention, could replace this with .*?
        String FILENAME_PATTERN = "([a-zA-Z]+)_([a-zA-Z]{3})_([1-3]*[a-zA-Z]+)_([0-9]{2})-([0-9]{2})_([0-9]{2}).*";
        Pattern p = Pattern.compile(FILENAME_PATTERN);
        Matcher m = p.matcher(file);
        boolean found = m.find();
        System.out.println("file is " + file + "\npattern is " + p.pattern());
        //m.group starts with the pattern, so the first group is at 1
        if(found){
            mLang = m.group(1);
            mSource = m.group(2);
            mBook = m.group(3);
            mChap = Integer.parseInt(m.group(4));
            mChunk = Integer.parseInt(m.group(5));
            mTake = Integer.parseInt(m.group(6));
            mMatched = true;
        } else {
            mMatched = false;
        }
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

    public int getChapter(){
        return mChap;
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
