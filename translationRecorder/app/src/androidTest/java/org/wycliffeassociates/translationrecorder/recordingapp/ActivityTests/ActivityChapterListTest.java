package org.wycliffeassociates.translationrecorder.recordingapp.ActivityTests;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import androidx.test.InstrumentationRegistry;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.matcher.BoundedMatcher;
import androidx.test.rule.ActivityTestRule;
import androidx.recyclerview.widget.RecyclerView;
import android.test.suitebuilder.annotation.LargeTest;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.wycliffeassociates.translationrecorder.ProjectManager.activities.ActivityChapterList;
import org.wycliffeassociates.translationrecorder.ProjectManager.activities.ActivityUnitList;
import org.wycliffeassociates.translationrecorder.ProjectManager.adapters.ChapterCardAdapter;
import org.wycliffeassociates.translationrecorder.R;
import org.wycliffeassociates.translationrecorder.TestUtils.FragmentTestActivity;
import org.wycliffeassociates.translationrecorder.chunkplugin.Chapter;
import org.wycliffeassociates.translationrecorder.chunkplugin.ChunkPlugin;
import org.wycliffeassociates.translationrecorder.project.ChunkPluginLoader;
import org.wycliffeassociates.translationrecorder.project.Project;
import org.wycliffeassociates.translationrecorder.recordingapp.ProjectMockingUtil;
import org.wycliffeassociates.translationrecorder.widgets.ChapterCard;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertEquals;

/**
 * Created by sarabiaj on 9/21/2017.
 */

@RunWith(Parameterized.class)
@LargeTest
public class ActivityChapterListTest  {

    @Rule
    public ActivityTestRule<ActivityChapterList> mActivityChapterListRule =
            new ActivityTestRule<>(
                    ActivityChapterList.class,
                    true,
                    false
            );

    @ClassRule
    public static ActivityTestRule<FragmentTestActivity> mTestActivity =
            new ActivityTestRule<>(
                    FragmentTestActivity.class,
                    true,
                    false
            );

    @Parameterized.Parameters
    public static Iterable<Object[]> data() throws IOException {

        //Project notesProject = ProjectMockingUtil.createNotesTestProject(mainMenuActivityTestRule);
        Project bibleProject = ProjectMockingUtil.createBibleTestProject(mTestActivity);

        ArrayList<Object[]> bibleTestArgs = new ArrayList<>();
        ChunkPlugin plugin = bibleProject.getChunkPlugin(new ChunkPluginLoader(mTestActivity.getActivity()));
        int numChapters  = plugin.numChapters();

        for(int i = 1; i < numChapters +1; i++) {
            bibleTestArgs.add(new Object[]{i, bibleProject});
        }
        return bibleTestArgs;

//        return Arrays.asList(new Object[][] {
//                //Bible Projects
//                {
//                        1,
//                        bibleProject
//                },
//                {
//                        2,
//                        bibleProject
//                }
//        });
    }

    private final int i;
    private final Project project;

    public ActivityChapterListTest(int chapter, Project project) {
        i = chapter;
        this.project = project;
    }


//    @Test
//    public void ActivityChapterListTest() throws IllegalAccessException, NoSuchFieldException, IOException {
//        Project bibleProject = createBibleTestProject(mTestActivity);
//        Project notesProject = createNotesTestProject(mTestActivity);
//
//        testClickingChapterCard(bibleProject);
//        testClickingChapterCard(notesProject);
//    }
    @Test
    public void testClickingChapterCard() throws IllegalAccessException, NoSuchFieldException, IOException {
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

        ChunkPlugin plugin = project.getChunkPlugin(new ChunkPluginLoader(chapterListActivity));

        List<Chapter> chapters = plugin.getChapters();
        //number of children in the recycler view should match the number of chapters
        assertEquals("Number of chapters vs number in adapter", chapters.size(), rv.getAdapter().getItemCount());


        //for(int i = 0; i < chapters.size(); i++) {
            final ChapterCard cc = ((ChapterCardAdapter)rv.getAdapter()).getItem(i-1);
            assertEquals(cc.getChapterNumber(), chapters.get(i-1).getNumber());
            Instrumentation.ActivityMonitor activityMonitor = new Instrumentation.ActivityMonitor(ActivityUnitList.class.getName(), null, false);
            InstrumentationRegistry.getInstrumentation().addMonitor(activityMonitor);


            //this hack seems to work? the sleep is necessary probably to give enough time for the data to bind to the view holder
            //fumbling around first with the scrolling seems to be necessary for it to not throw an exception saying one of the chapter numbers can't match
            onView(withId(R.id.chapter_list)).perform(RecyclerViewActions.scrollToHolder(withChapterNumber(cc.getChapterNumber())));

            //onView(withId(R.id.chapter_list)).perform(RecyclerViewActions.scrollToHolder(withChapterNumber(chapters.get(0).getNumber())));
            //onView(withId(R.id.chapter_list)).perform(RecyclerViewActions.scrollToHolder(withChapterNumber(cc.getChapterNumber())));
//            try {
//                Thread.sleep(100);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }

            ChapterCardAdapter.ViewHolder view = (ChapterCardAdapter.ViewHolder) rv.findViewHolderForAdapterPosition(i-1);
            view.onClick(view.cardHeader);
            //onView(withId(R.id.chapter_list)).perform(RecyclerViewActions.actionOnHolderItem(withChapterNumber(cc.getChapterNumber()), click()));


            ActivityUnitList aul = (ActivityUnitList) activityMonitor.waitForActivity();


            Field chapterField = ActivityUnitList.class.getDeclaredField("mChapterNum");
            chapterField.setAccessible(true);
            int unitListChapter = chapterField.getInt(aul);
            System.out.println("chapters.get is " + chapters.get(i-1).getNumber());
            System.out.println("i is " + i);
            System.out.println("unitListChapter is " + unitListChapter);
            assertEquals(unitListChapter, chapters.get(i-1).getNumber());
            System.out.println("SUCCESS: UnitListChapter " + unitListChapter + " and Chapter Number " + chapters.get(i-1).getNumber() + " are the same!");
            aul.finish();
            InstrumentationRegistry.getInstrumentation().removeMonitor(activityMonitor);
        //}
        chapterListActivity.finish();
        instrumentation.removeMonitor(chapterListMonitor);

    }

    public static Matcher<RecyclerView.ViewHolder> withChapterNumber(final int chapterNumber)
    {
        return new BoundedMatcher<RecyclerView.ViewHolder, ChapterCardAdapter.ViewHolder>(ChapterCardAdapter.ViewHolder.class)
        {
            @Override
            protected boolean matchesSafely(ChapterCardAdapter.ViewHolder item)
            {
                return item.chapterCard.getChapterNumber() == chapterNumber;
            }

            @Override
            public void describeTo(Description description)
            {
                description.appendText("view holder with chapter number: " + chapterNumber);
            }
        };
    }

}
