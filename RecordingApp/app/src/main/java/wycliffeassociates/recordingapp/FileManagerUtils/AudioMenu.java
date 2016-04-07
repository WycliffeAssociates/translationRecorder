package wycliffeassociates.recordingapp.FileManagerUtils;

import java.util.ArrayList;
import java.util.Collection;

//Unused
public class AudioMenu {
    private String folderName;
    private Collection<FileItem> mFileItems;

    public AudioMenu(String name){
        folderName = name;
        mFileItems = new ArrayList<FileItem>();

    }

    public boolean addAudioItem(FileItem audio){
        return mFileItems.add(audio);
    }

    public String getAudioPB(){
        return folderName;
    }

    public Collection<FileItem> getmFileItems(){
        return mFileItems;
    }

}
