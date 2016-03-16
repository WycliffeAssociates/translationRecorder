package wycliffeassociates.recordingapp.FilesPage;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by sarabiaj on 3/15/2016.
 */
public class FileNameExtractor {
    private String mLang;
    private String mSource;
    private String mBook;
    private int mChap;
    private int mChunk;
    private int mTake;

    public FileNameExtractor(String file){
        extractData(file);
    }

    public FileNameExtractor(File file){
        extractData(file.getName());
    }

    private void extractData(String file){
        //includes the wav extention, could replace this with .*?
        Pattern p = Pattern.compile("([a-zA-Z]+)_([a-zA-Z]{3})_([1-3]*[a-zA-Z]+)_([0-9]{2})-([0-9]{2})_([0-9]{2})\\.wav");
        Matcher m = p.matcher(file);
        m.find();

        //m.group starts with the pattern, so the first group is at 1
        mLang = m.group(1);
        mSource = m.group(2);
        mBook = m.group(3);
        mChap = Integer.parseInt(m.group(4));
        mChunk = Integer.parseInt(m.group(5));
        mTake = Integer.parseInt(m.group(6));
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

    public static int getLargestTake(File directory, File filename){
        File[] files = directory.listFiles();
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
