package org.wycliffeassociates.translationrecorder.recordingapp.ActivityTests;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v7.widget.RecyclerView;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.View;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wycliffeassociates.translationrecorder.ProjectManager.activities.ActivityChapterList;
import org.wycliffeassociates.translationrecorder.ProjectManager.activities.ActivityUnitList;
import org.wycliffeassociates.translationrecorder.ProjectManager.adapters.ChapterCardAdapter;
import org.wycliffeassociates.translationrecorder.TestUtils.FragmentTestActivity;
import org.wycliffeassociates.translationrecorder.chunkplugin.Chapter;
import org.wycliffeassociates.translationrecorder.chunkplugin.ChunkPlugin;
import org.wycliffeassociates.translationrecorder.project.Project;
import org.wycliffeassociates.translationrecorder.widgets.ChapterCard;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.wycliffeassociates.translationrecorder.recordingapp.ProjectMockingUtil.createBibleTestProject;
import static org.wycliffeassociates.translationrecorder.recordingapp.ProjectMockingUtil.createNotesTestProject;

/**
 * Created by sarabiaj on 9/21/2017.
 */

@RunWith(AndroidJUnit4.class)
@LargeTest
public class ActivityChapterListTest  {

    @Rule
    public ActivityTestRule<ActivityChapterList> mActivityChapterListRule =
            new ActivityTestRule<>(
                    ActivityChapterList.class,
                    true,
                    false
            );

    @Rule
    public ActivityTestRule<FragmentTestActivity> mTestActivity =
            new ActivityTestRule<>(
                    FragmentTestActivity.class,
                    true,
                    false
            );

    @Test
    public void ActivityChapterListTest() throws IllegalAccessException, NoSuchFieldException, IOException {
        Project bibleProject = createBibleTestProject(mTestActivity);
        Project notesProject = createNotesTestProject(mTestActivity);

        testClickingChapterCard(bibleProject);
        testClickingChapterCard(notesProject);
    }

    public void testClickingChapterCard(Project project) throws IllegalAccessException, NoSuchFieldException, IOException {
        Context ctx = InstrumentationRegistry.getContext();
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        Instrumentation.ActivityMonitor chapterListMonitor = new Instrumentation.ActivityMonitor(
                ActivityChapterList.class.getName(),
                null,
                false
        );
        instrumentation.addMonitor(chapterListMonitor);
        mActivityChapterListRule.launchActivity(
                ActivityChapterList.getActivityUnitListIntent(ctx, project)
        );
        Activity chapterListActivity = chapterListMonitor.waitForActivity();
        Field recyclerViewField = chapterListActivity.getClass().getDeclaredField("mChapterList");
        recyclerViewField.setAccessible(true);
        RecyclerView rv = (RecyclerView) recyclerViewField.get(chapterListActivity);

        ChunkPlugin plugin = project.getChunkPlugin(chapterListActivity);

        List<Chapter> chapters = plugin.getChapters();
        //number of children in the recycler view should match the number of chapters
        assertEquals("Number of chapters vs number in adapter", chapters.size(), rv.getAdapter().getItemCount());


        for(int i = 0; i < chapters.size(); i++) {
            final ChapterCard cc = ((ChapterCardAdapter)rv.getAdapter()).getItem(i);
            assertEquals(cc.getChapterNumber(), chapters.get(i).getNumber());
            View v = rv.getChildAt(0);
            final ChapterCardAdapter.ViewHolder ccvh = (ChapterCardAdapter.ViewHolder) rv.getChildViewHolder(v);

            //the problem here is that binding view holders requires running on the ui thread
            //binding the chapter card to the view holder seems to be the only way I can definitely
            //access the chapter card and be able to click on it
            chapterListActivity.runOnUiThread(new Runnable() {
                                                  @Override
                                                  public void run() {
                                                      ccvh.bindViewHolder(ccvh, 0, cc);
                                                  }
                                              });
                    //rv.getAdapter().bindViewHolder(ccvh, i);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Instrumentation.ActivityMonitor activityMonitor = new Instrumentation.ActivityMonitor(ActivityUnitList.class.getName(), null, false);
            InstrumentationRegistry.getInstrumentation().addMonitor(activityMonitor);
            ccvh.onClick(ccvh.cardView);
            ActivityUnitList aul = (ActivityUnitList) activityMonitor.waitForActivity();

            Field chapterField = ActivityUnitList.class.getDeclaredField("mChapterNum");
            chapterField.setAccessible(true);
            int unitListChapter = chapterField.getInt(aul);
            assertEquals(unitListChapter, chapters.get(i).getNumber());
            System.out.println("SUCCESS: UnitListChapter " + unitListChapter + " and Chapter Number " + chapters.get(i).getNumber() + " are the same!");
            aul.finish();
            InstrumentationRegistry.getInstrumentation().removeMonitor(activityMonitor);
        }
        chapterListActivity.finish();
        instrumentation.removeMonitor(chapterListMonitor);

    }

}
