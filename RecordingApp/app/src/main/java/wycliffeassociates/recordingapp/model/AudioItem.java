package wycliffeassociates.recordingapp.model;

/**
 * Created by Butler on 7/13/2015.
 */
public class AudioItem implements Comparable<AudioItem> {
    private String Name;

    private double FileSize;

    public AudioItem(String name){
        this.Name = name;
    }

    public AudioItem(AudioItem item){
        this.Name = item.Name;
        this.FileSize = item.FileSize;

    }

    public String getName() {
        return Name;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public int compareTo(AudioItem cmp) {
        return this.getName().compareTo(cmp.getName());
    }

}
