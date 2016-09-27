package wycliffeassociates.recordingapp.ProjectManager.tasks;

import java.util.List;

import wycliffeassociates.recordingapp.utilities.Task;
import wycliffeassociates.recordingapp.widgets.ChapterCard;

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
