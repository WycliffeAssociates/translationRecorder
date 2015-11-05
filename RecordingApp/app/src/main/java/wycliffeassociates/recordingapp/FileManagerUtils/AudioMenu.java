package wycliffeassociates.recordingapp.FileManagerUtils;

import java.util.ArrayList;
import java.util.Collection;

//Unused
public class AudioMenu {
    private String folderName;
    private Collection<AudioItem> mAudioItems;

    public AudioMenu(String name){
        folderName = name;
        mAudioItems = new ArrayList<AudioItem>();

    }

    public boolean addAudioItem(AudioItem audio){
        return mAudioItems.add(audio);
    }

    public String getAudioPB(){
        return folderName;
    }

    public Collection<AudioItem> getmAudioItems(){
        return mAudioItems;
    }

}
