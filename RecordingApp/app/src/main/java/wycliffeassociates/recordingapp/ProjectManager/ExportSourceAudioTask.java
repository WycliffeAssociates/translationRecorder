package wycliffeassociates.recordingapp.ProjectManager;

import com.wycliffeassociates.io.ArchiveOfHolding;

import org.apache.commons.io.FileUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;

import wycliffeassociates.recordingapp.Reporting.Logger;
import wycliffeassociates.recordingapp.Utils;
import wycliffeassociates.recordingapp.utilities.Task;

/**
 * Created by sarabiaj on 9/23/2016.
 */
public class ExportSourceAudioTask extends Task {

    Project mProject;
    BufferedOutputStream mOutput;
    File mBookFolder;
    File mStagingRoot;
    ActivityProjectManager mActivity;

    public ExportSourceAudioTask(Project project, File bookFolder, File stagingRoot, BufferedOutputStream output){
        mProject = project;
        mOutput = output;
        mStagingRoot = stagingRoot;
        mBookFolder = bookFolder;
    }

    @Override
    public void run() {
        File input = stageFilesForArchive(mProject, mBookFolder, mStagingRoot);
        onTaskProgressUpdateDelegator(25);
        createSourceAudio(mProject, input, mOutput);
        onTaskProgressUpdateDelegator(75);
        onTaskCompleteDelegator();
        onComplete();
    }

    public void createSourceAudio(final Project project, final File input, final BufferedOutputStream output){
        try {
            final ArchiveOfHolding aoh = new ArchiveOfHolding();
            aoh.createArchiveOfHolding(input, output);
        } catch (IOException e) {

        } finally {
            try {
                output.flush();
                output.close();
            } catch (IOException e){

            }
            Utils.deleteRecursive(input);
        }
    }

    private File stageFilesForArchive(Project project, File input, File stagingRoot){
        File root = new File(stagingRoot, project.getTargetLanguage() + Utils.capitalizeFirstLetter(project.getSlug()));
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

    public void setActivity(ActivityProjectManager apm){
        mActivity = apm;
    }

    private void onComplete(){
        mActivity.onSourceAudioExported();
    }
}
