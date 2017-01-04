package org.wycliffeassociates.translationrecorder.ProjectManager.tasks;

import org.wycliffeassociates.translationrecorder.utilities.Task;
import org.wycliffeassociates.translationrecorder.widgets.ChapterCard;

import java.util.List;

/**
 * Created by sarabiaj on 9/27/2016.
 */
public class CompileChapterTask extends Task {

    List<ChapterCard> mCardsToCompile;

    public CompileChapterTask(int taskTag, List<ChapterCard> cardsToCompile) {
        super(taskTag);
        mCardsToCompile = cardsToCompile;
    }

    @Override
    public void run() {
        for (int i = 0; i < mCardsToCompile.size(); i++) {
            mCardsToCompile.get(i).compile();
            onTaskProgressUpdateDelegator((int) ((i / (float) mCardsToCompile.size()) * 100));
        }
        onTaskCompleteDelegator();
    }
}
