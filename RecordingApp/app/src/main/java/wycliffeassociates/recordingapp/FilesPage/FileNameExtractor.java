package wycliffeassociates.recordingapp.FilesPage;

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
}
