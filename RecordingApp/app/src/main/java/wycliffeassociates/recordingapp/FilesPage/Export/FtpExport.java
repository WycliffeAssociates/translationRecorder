package wycliffeassociates.recordingapp.FilesPage.Export;

import android.app.Fragment;
import android.content.Intent;
import java.util.ArrayList;
import wycliffeassociates.recordingapp.FileManagerUtils.AudioItem;
import wycliffeassociates.recordingapp.FilesPage.AudioFilesAdapter;
import wycliffeassociates.recordingapp.FilesPage.FTPActivity;


/**
 * Created by sarabiaj on 12/10/2015.
 */
public class FtpExport extends Export{

    public FtpExport(ArrayList<AudioItem> audioItemList, AudioFilesAdapter adapter, String currentDir, Fragment ctx){
        super(audioItemList, adapter, currentDir, ctx);
    }

    /**
     * Uploads files via FTP using the the existing FTP Activity
     */
    //TODO: Test this to see if it still works through this refactoring
    @Override
    public void export() {
        if(mNumFilesToExport > 1){
            zipFiles(this);
        } else {
            handleUserInput();
        }
    }

    @Override
    protected void handleUserInput() {
        Intent intent = new Intent(mCtx.getActivity(), FTPActivity.class);
        mCtx.startActivityForResult(intent, 0);
    }
}
