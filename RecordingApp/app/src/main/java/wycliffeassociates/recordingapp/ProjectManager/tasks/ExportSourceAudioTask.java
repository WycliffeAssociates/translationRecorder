package wycliffeassociates.recordingapp.ProjectManager.tasks;

import com.wycliffeassociates.io.ArchiveOfHolding;

import org.apache.commons.io.FileUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;

import wycliffeassociates.recordingapp.ProjectManager.Project;
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
    long mTotalStagingSize;
    long mStagingProgress;

    public ExportSourceAudioTask(int taskTag, Project project, File bookFolder, File stagingRoot, BufferedOutputStream output) {
        super(taskTag);
        mProject = project;
        mOutput = output;
        mStagingRoot = stagingRoot;
        mBookFolder = bookFolder;
    }

    @Override
    public void run() {
        mTotalStagingSize = getTotalProgressSize(mBookFolder);
        File input = stageFilesForArchive(mProject, mBookFolder, mStagingRoot);
        //just a guess of progress, giving credit for work done that doesn't have a progress updater
        onTaskProgressUpdateDelegator(10);
        createSourceAudio(mProject, input, mOutput);
        onTaskCompleteDelegator();
    }

    public void createSourceAudio(final Project project, final File input, final BufferedOutputStream output) {
        try {
            final ArchiveOfHolding aoh = new ArchiveOfHolding(new ArchiveOfHolding.OnProgressListener() {
                @Override
                public void onProgressUpdate(int i) {
                    //Consider the staging of files to be half the work; so progress from the aoh
                    // needs to be divided in half and added to the half that is already done
                    onTaskProgressUpdateDelegator((int) (i * .5) + 50);
                }
            });
            aoh.createArchiveOfHolding(input, output);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                output.flush();
                output.close();
            } catch (IOException e) {

            }
            Utils.deleteRecursive(input);
        }
    }

    private long getTotalProgressSize(File input) {
        long total = 0;
        if (input.isDirectory()) {
            for (File f : input.listFiles()) {
                total += getTotalProgressSize(f);
            }
        }
        return total + input.length();
    }

    private File stageFilesForArchive(Project project, File input, File stagingRoot) {
        File root = new File(stagingRoot, project.getTargetLanguage() + "_" + project.getVersion() + "_" + project.getSlug());
        File lang = new File(root, project.getTargetLanguage());
        File version = new File(lang, project.getVersion());
        File book = new File(version, project.getSlug());
        book.mkdirs();
        for (File c : input.listFiles()) {
            if (c.isDirectory()) {
                File chapter = new File(book, c.getName());
                chapter.mkdir();
                for (File f : c.listFiles()) {
                    if (!f.isDirectory()) {
                        try {
                            FileUtils.copyFileToDirectory(f, chapter);
                            mStagingProgress += f.length();
                            //this step accounts for half of the work, so it is multiplied by 50 instead of 100
                            int progressPercentage = (int) ((mStagingProgress / (double) mTotalStagingSize) * 50);
                            onTaskProgressUpdateDelegator(progressPercentage);
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
