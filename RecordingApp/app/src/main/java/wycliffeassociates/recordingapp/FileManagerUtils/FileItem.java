package wycliffeassociates.recordingapp.FileManagerUtils;

import java.util.Date;

public class FileItem implements Comparable<FileItem> {

    public static final boolean DIRECTORY = true;
    public static final boolean FILE = false;
    private String mName;
    private Date mDate;
    private int mDuration;
    private boolean mState;
    //private double FileSize;
    private boolean mType;

    public FileItem(String name, boolean type){
        mName = name;
        Date t = new Date();
        mDate = t;
        mDuration = 0;
        mState = false;
        mType = type;
    }

    public FileItem(String name, Date date, int duration, boolean type){
        mName = name;
        mDate = date;
        mDuration = duration;
        mState = false;
        mType = type;
    }

    //create a shallow copy of another audio item
    public FileItem(FileItem item){
        mName = item.getName();
        mDate = item.getDate();
        mDuration = item.getDuration();
        mState = item.getState();
        //mFileSize = item.getSize();
        mType = item.getType();
    }

    public boolean isDirectory(){
        if(mType == DIRECTORY){
            return true;
        }
        return false;
    }

    public boolean getType(){
        return mType;
    }

    public void setName(String name){
        mName = name;
    }

    public void setDate(Date date){
        mDate = date;
    }

    public void setDuration(int duration){ mDuration = duration; }

    public void setState(boolean state){ mState = state;}

    public String getName() { return mName; }

    public Date getDate(){ return mDate; }

    public int getDuration(){ return mDuration; }

    public boolean getState(){ return mState; }

    public String toString() {
        return getName();
    }

    public int compareTo(FileItem cmp) {
        return getName().compareTo(cmp.getName());
    }

}
