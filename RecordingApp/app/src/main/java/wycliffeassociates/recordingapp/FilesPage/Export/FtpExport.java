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

    @Override
    public boolean export() {
        Intent intent = new Intent(mCtx.getActivity(), FTPActivity.class);
        mCtx.startActivityForResult(intent, 0);
        return false;
    }
}
