package org.wycliffeassociates.translationrecorder.recordingapp.AdapterTests;

import android.content.Context;
import androidx.test.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.wycliffeassociates.translationrecorder.ProjectManager.activities.ActivityChapterList;
import org.wycliffeassociates.translationrecorder.ProjectManager.adapters.ChapterCardAdapter;
import org.wycliffeassociates.translationrecorder.chunkplugin.Chapter;
import org.wycliffeassociates.translationrecorder.chunkplugin.ChunkPlugin;
import org.wycliffeassociates.translationrecorder.database.ProjectDatabaseHelper;
import org.wycliffeassociates.translationrecorder.project.ChunkPluginLoader;
import org.wycliffeassociates.translationrecorder.project.Project;
import org.wycliffeassociates.translationrecorder.widgets.ChapterCard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.wycliffeassociates.translationrecorder.recordingapp.ProjectMockingUtil.createBibleTestProject;
import static org.wycliffeassociates.translationrecorder.recordingapp.ProjectMockingUtil.createNotesTestProject;

/**
 * Created by sarabiaj on 9/20/2017.
 */

@RunWith(AndroidJUnit4.class)
public class ChapterCardAdapterTest {

    @Rule
    public ActivityTestRule<ActivityChapterList> mActivityChapterListRule =
            new ActivityTestRule<>(
                    ActivityChapterList.class,
                    true,
                    false
            );

    ChapterCardAdapter mBibleAdapter;
    ChapterCardAdapter mNotesAdapter;
    Project bibleProject;
    Project notesProject;

    @Before
    public void setUp() {
        ProjectDatabaseHelper db = new ProjectDatabaseHelper(mActivityChapterListRule.getActivity());
        bibleProject = createBibleTestProject(mActivityChapterListRule);
        notesProject = createNotesTestProject(mActivityChapterListRule);
        try {
            mBibleAdapter = new ChapterCardAdapter(
                    mActivityChapterListRule.getActivity(),
                    bibleProject,
                    createChapterCardList(bibleProject),
                    db
            );
            mNotesAdapter = new ChapterCardAdapter(
                    mActivityChapterListRule.getActivity(),
                    notesProject,
                    createChapterCardList(notesProject),
                    db
            );

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    List<ChapterCard> createChapterCardList(Project project) throws IOException {
        Context ctx = InstrumentationRegistry.getContext();
        ChunkPlugin plugin = project.getChunkPlugin(new ChunkPluginLoader(ctx));
        List<Chapter> chapters = plugin.getChapters();
        List<ChapterCard> cards = new ArrayList<>();
        for(Chapter chapter : chapters) {
//            cards.add(new ChapterCard(
//                    mActivityChapterListRule.getActivity(),
//                    project,
//                    chapter.getNumber(),
//                    chapter.getChunks().size()
//            ));
        }
        return cards;
    }

}
