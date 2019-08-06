package org.wycliffeassociates.translationrecorder.FilesPage.Export;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.core.content.FileProvider;
import org.wycliffeassociates.translationrecorder.BuildConfig;
import org.wycliffeassociates.translationrecorder.project.Project;

import java.io.File;

/**
 * Created by sarabiaj on 12/10/2015.
 */
public class AppExport extends Export {

    public AppExport(File exportProject, Project project){
        super(exportProject, project);
    }

    @Override
    protected void handleUserInput() {
            exportZipApplications(mZipFile);
    }

    /**
     *  Passes zip file URI to relevant audio applications.
     *      @param zipFile a list of filenames to be exported
     */
    private void exportZipApplications(File zipFile){
        Intent shareIntent = new Intent(this.mCtx.getActivity(), AppExport.ShareZipToApps.class);
        shareIntent.putExtra("zipPath", zipFile.getAbsolutePath());
        mCtx.startActivity(shareIntent);
    }

    public static class ShareZipToApps extends Activity{
        File mFile;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            String path = getIntent().getStringExtra("zipPath");
            Intent sendIntent = new Intent();
            sendIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            mFile = new File (path);
            Uri audioUri = FileProvider.getUriForFile(
                    ShareZipToApps.this,
                    BuildConfig.APPLICATION_ID + ".provider",
                    mFile
            );
            sendIntent.setAction(Intent.ACTION_SEND);
            //send individual URI
            sendIntent.putExtra(Intent.EXTRA_STREAM, audioUri);
            //open
            sendIntent.setType("application/zip");
            this.startActivityForResult(Intent.createChooser(sendIntent, "Export Zip"), 3);
        }

        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if(mFile != null){
                mFile.delete();
            }
        }
    }
}
