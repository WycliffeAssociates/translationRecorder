package wycliffeassociates.recordingapp.ProjectManager;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.wycliffeassociates.io.ArchiveOfHolding;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

import wycliffeassociates.recordingapp.Reporting.Logger;
import wycliffeassociates.recordingapp.Utils;

/**
 * Created by sarabiaj on 9/21/2016.
 */
public class SourceAudioTaskFragment extends Fragment {

    private ProgressDialog mPd;
    private volatile boolean mInProgress = false;

    public interface SourceAudioExportCallback{
        void onSourceAudioExported();
    }

    SourceAudioExportCallback mProgressUpdateCallback;
    Handler mHandler;

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        mProgressUpdateCallback = (SourceAudioExportCallback) activity;
        if(mInProgress){
            displayDialog();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        mHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void onDetach(){
        super.onDetach();
        mPd.dismiss();
        mProgressUpdateCallback = null;
    }

    private void onSourceAudioCompiled(){
        mInProgress = false;
        mPd.dismiss();
        mProgressUpdateCallback.onSourceAudioExported();
    }
    
    private void displayDialog(){
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mPd = new ProgressDialog(getActivity());
                mPd.setTitle("Compiling Source Audio");
                mPd.setMessage("Please Wait...");
                mPd.setIndeterminate(true);
                mPd.setCancelable(false);
                mPd.show();
            }
        });

    }

    public void createSourceAudio(final Project project, final File bookFolder, final File output){
        mInProgress = true;
        displayDialog();
        Thread create = new Thread(new Runnable() {
            @Override
            public void run() {
                File input = stageFilesForArchive(project, bookFolder);
                try {
                    final ArchiveOfHolding aoh = new ArchiveOfHolding();
                    aoh.createArchiveOfHolding(input, output, true);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            onSourceAudioCompiled();
                        }
                    });

                } finally {
                   Utils.deleteRecursive(input);
                }
            }
        });
        create.start();
    }

    private File stageFilesForArchive(Project project, File input){
        File root = new File(getActivity().getFilesDir(), project.getTargetLanguage() + Utils.capitalizeFirstLetter(project.getSlug()));
        File lang = new File(root, project.getTargetLanguage());
        File version = new File(lang, project.getSource());
        File book = new File(version, project.getSlug());
        book.mkdirs();
        for(File c : input.listFiles()){
            if(c.isDirectory()){
                File chapter = new File(book, c.getName());
                chapter.mkdir();
                for(File f : c.listFiles()){
                    if(!f.isDirectory()) {
                        try {
                            FileUtils.copyFileToDirectory(f, chapter);
                        } catch (IOException e) {
                            Logger.e(this.toString(), "IOException staging files for archive", e);
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        return root;
    }
}
