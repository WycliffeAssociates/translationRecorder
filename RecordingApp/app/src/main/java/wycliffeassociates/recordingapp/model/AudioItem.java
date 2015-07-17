package wycliffeassociates.recordingapp.model;

import java.util.Date;

/**
 * Class for storing information about audio items
 */
public class AudioItem implements Comparable<AudioItem> {

    /**
     * Name of the audio item in user-readable format.
     */
    private String aName;

    /**
     * Last Modified date of audio item
     */
    private Date aDate;

    /**
     * Duration of the audio item in seconds (unimplemented)
     */
    private int aDuration;

    //private double FileSize;


    public AudioItem(String name){
        this.aName = name;
        Date t= new Date();
        this.aDate = t;
        this.aDuration = 0;
    }
    public AudioItem(String name, Date date, int duration){
        this.aName = name;
        this.aDate = date;
        this.aDuration = duration;
    }

    //create a shallow copy of another audio item
    public AudioItem(AudioItem item){
        aName = item.aName;
        aDate = item.aDate;
        aDuration = item.aDuration;
        //this.FileSize = item.FileSize;

    }

    public void setName(String name){
        this.aName = name;
    }

    public void setDate(Date date){
        this.aDate = date;
    }

    public void setDuration(int duration){
        this.aDuration = duration;
    }

    public String getName() { return aName; }

    public Date getDate(){ return aDate; }

    public int getDuration(){ return aDuration; }

    public String toString() {
        return getName();
    }

    public int compareTo(AudioItem cmp) {
        return this.getName().compareTo(cmp.getName());
    }

}
