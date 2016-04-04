package wycliffeassociates.recordingapp.FilesPage.Export;

import android.content.Intent;
import java.util.ArrayList;
import wycliffeassociates.recordingapp.FileManagerUtils.FileItem;
import wycliffeassociates.recordingapp.FilesPage.AudioFilesAdapter;
import wycliffeassociates.recordingapp.FilesPage.FTPActivity;


/**
 * Created by sarabiaj on 12/10/2015.
 */
public class FtpExport extends Export{

    public FtpExport(ArrayList<FileItem> fileItemList, AudioFilesAdapter adapter, String currentDir){
        super(fileItemList, adapter, currentDir);
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
