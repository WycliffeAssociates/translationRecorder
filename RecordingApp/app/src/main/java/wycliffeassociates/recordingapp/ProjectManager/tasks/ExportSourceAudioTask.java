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
        onTaskProgressUpdateDelegator(10);
        createSourceAudio(mProject, input, mOutput);
        onTaskCompleteDelegator();
    }

    public void createSourceAudio(final Project project, final File input, final BufferedOutputStream output) {
        try {
            final ArchiveOfHolding aoh = new ArchiveOfHolding(new ArchiveOfHolding.OnProgressListener() {
                @Override
                public void onProgressUpdate(int i) {
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
        File root = new File(stagingRoot, project.getTargetLanguage() + Utils.capitalizeFirstLetter(project.getSlug()));
        File lang = new File(root, project.getTargetLanguage());
        File version = new File(lang, project.getSource());
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
