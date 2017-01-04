package org.wycliffeassociates.translationrecorder.ProjectManager;

import org.wycliffeassociates.translationrecorder.widgets.ChapterCard;

import java.util.List;

/**
 * Created by sarabiaj on 8/26/2016.
 */
public interface DelegateCompile {
    void compile(List<ChapterCard> cards, int[] modifiedIndices);
}
