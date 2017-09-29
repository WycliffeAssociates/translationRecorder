package org.wycliffeassociates.translationrecorder.ProjectManager.tasks;

import org.wycliffeassociates.translationrecorder.project.Project;
import org.wycliffeassociates.translationrecorder.project.ProjectFileUtils;
import org.wycliffeassociates.translationrecorder.project.ProjectPatternMatcher;
import org.wycliffeassociates.translationrecorder.project.TakeInfo;
import org.wycliffeassociates.translationrecorder.utilities.Task;
import org.wycliffeassociates.translationrecorder.wav.WavFile;
import org.wycliffeassociates.translationrecorder.widgets.ChapterCard;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Created by sarabiaj on 9/27/2016.
 */
public class CompileChapterTask extends Task {

    Map<ChapterCard, List<String>> mCardsToCompile;
    final Project mProject;

    public CompileChapterTask(
            int taskTag,
            Map<ChapterCard, List<String>> cardsToCompile,
            final Project project
    ) {
        super(taskTag);
        mCardsToCompile = cardsToCompile;
        mProject = project;
    }

    @Override
    public void run() {
        int currentCard = 0;
        int totalCards = mCardsToCompile.size();
        for (Map.Entry<ChapterCard, List<String>> entry : mCardsToCompile.entrySet()) {
            List<String> files = entry.getValue();
            ChapterCard chapterCard = entry.getKey();
            sortFilesInChapter(files);
            List<WavFile> wavFiles = getWavFilesFromName(files, chapterCard.getChapterNumber());
            WavFile.compileChapter(mProject, chapterCard.getChapterNumber(), wavFiles);
            onTaskProgressUpdateDelegator((int) ((currentCard / (float) totalCards) * 100));
            currentCard++;
        }

        onTaskCompleteDelegator();
    }

    public void sortFilesInChapter(List<String> files) {
        Collections.sort(files, new Comparator<String>() {
            @Override
            public int compare(String lhs, String rhs) {
                ProjectPatternMatcher ppmLeft = mProject.getPatternMatcher();
                ppmLeft.match(lhs);
                TakeInfo takeInfoLeft = ppmLeft.getTakeInfo();

                ProjectPatternMatcher ppmRight = mProject.getPatternMatcher();
                ppmRight.match(rhs);
                TakeInfo takeInfoRight = ppmRight.getTakeInfo();

                int startLeft = takeInfoLeft.getStartVerse();
                int startRight = takeInfoRight.getStartVerse();
                return Integer.compare(startLeft, startRight);
            }
        });
    }

    public List<WavFile> getWavFilesFromName(List<String> files, int chapterNumber) {
        List<WavFile> wavFiles = new ArrayList<>();
        File base = ProjectFileUtils.getParentDirectory(mProject, chapterNumber);
        for (String s : files) {
            File f = new File(base, s);
            wavFiles.add(new WavFile(f));
        }
        return wavFiles;
    }
}
