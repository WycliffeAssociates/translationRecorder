package wycliffeassociates.recordingapp.ProjectManager;

import java.util.List;

import wycliffeassociates.recordingapp.widgets.ChapterCard;

/**
 * Created by sarabiaj on 8/26/2016.
 */
public interface DelegateCompile {
    void compile(List<ChapterCard> cards, int[] modifiedIndices);
}
