package org.wycliffeassociates.translationrecorder.recordingapp.ActivityTests;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import androidx.test.InstrumentationRegistry;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.matcher.BoundedMatcher;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;
import androidx.recyclerview.widget.RecyclerView;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.View;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wycliffeassociates.translationrecorder.ProjectManager.activities.ActivityUnitList;
import org.wycliffeassociates.translationrecorder.ProjectManager.adapters.UnitCardAdapter;
import org.wycliffeassociates.translationrecorder.R;
import org.wycliffeassociates.translationrecorder.Recording.RecordingActivity;
import org.wycliffeassociates.translationrecorder.TestUtils.FragmentTestActivity;
import org.wycliffeassociates.translationrecorder.chunkplugin.Chunk;
import org.wycliffeassociates.translationrecorder.chunkplugin.ChunkPlugin;
import org.wycliffeassociates.translationrecorder.project.ChunkPluginLoader;
import org.wycliffeassociates.translationrecorder.project.Project;
import org.wycliffeassociates.translationrecorder.widgets.UnitCard;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertEquals;
import static org.wycliffeassociates.translationrecorder.recordingapp.ProjectMockingUtil.createBibleTestProject;
import static org.wycliffeassociates.translationrecorder.recordingapp.ProjectMockingUtil.createNotesTestProject;

/**
 * Created by sarabiaj on 9/21/2017.
 */

@RunWith(AndroidJUnit4.class)
@LargeTest
public class ActivityUnitListTest  {

    @Rule
    public ActivityTestRule<ActivityUnitList> mActivityUnitListRule =
            new ActivityTestRule<>(
                    ActivityUnitList.class,
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
    public void ActivityUnitListTest() throws IllegalAccessException, NoSuchFieldException, IOException {
        Project bibleProject = createBibleTestProject(mTestActivity);
        Project notesProject = createNotesTestProject(mTestActivity);

        testClickingUnitCard(bibleProject);
        testClickingUnitCard(notesProject);
    }

    public void testClickingUnitCard(Project project) throws IllegalAccessException, NoSuchFieldException, IOException {
        Context ctx = InstrumentationRegistry.getContext();
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        Instrumentation.ActivityMonitor unitListMonitor = new Instrumentation.ActivityMonitor(
                ActivityUnitList.class.getName(),
                null,
                false
        );
        instrumentation.addMonitor(unitListMonitor);
        mActivityUnitListRule.launchActivity(
                ActivityUnitList.getActivityUnitListIntent(ctx, project, 1)
        );
        Activity unitListActivity = unitListMonitor.waitForActivity();
        Field recyclerViewField = unitListActivity.getClass().getDeclaredField("mUnitList");
        recyclerViewField.setAccessible(true);
        RecyclerView rv = (RecyclerView) recyclerViewField.get(unitListActivity);

        ChunkPlugin plugin = project.getChunkPlugin(new ChunkPluginLoader(unitListActivity));

        List<Chunk> units = plugin.getChunks(1);
        //number of children in the recycler view should match the number of units
        assertEquals("Number of units vs number in adapter", units.size(), rv.getAdapter().getItemCount());

        Instrumentation.ActivityMonitor activityMonitor = new Instrumentation.ActivityMonitor(RecordingActivity.class.getName(), null, false);
        InstrumentationRegistry.getInstrumentation().addMonitor(activityMonitor);
        for(int i = 0; i < units.size(); i++) {
            final UnitCard cc = ((UnitCardAdapter)rv.getAdapter()).getItem(i);
            assertEquals(cc.getStartVerse(), units.get(i).getStartVerse());



            //this hack seems to work? the sleep is necessary probably to give enough time for the data to bind to the view holder
            //fumbling around first with the scrolling seems to be necessary for it to not throw an exception saying one of the unit numbers can't match
            onView(withId(R.id.unit_list)).perform(RecyclerViewActions.scrollToHolder(withUnitNumber(cc.getStartVerse())));
            onView(withId(R.id.unit_list)).perform(RecyclerViewActions.scrollToHolder(withUnitNumber(units.get(0).getStartVerse())));
            onView(withId(R.id.unit_list)).perform(RecyclerViewActions.scrollToHolder(withUnitNumber(cc.getStartVerse())));
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            View view = ((UnitCardAdapter.ViewHolder)(rv.findViewHolderForAdapterPosition(i))).unitRecordBtn;
            int unitCardStartVerse = ((UnitCardAdapter.ViewHolder)(rv.findViewHolderForAdapterPosition(i))).unitCard.getStartVerse();
            view.callOnClick();

            while(activityMonitor.getHits() < i+1) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            RecordingActivity aul = (RecordingActivity) activityMonitor.getLastActivity();
            if(aul == null) {
                aul = (RecordingActivity) activityMonitor.waitForActivity();
            }
//            Field unitField = RecordingActivity.class.getDeclaredField("mInitialUnit");
//            unitField.setAccessible(true);
            //int unitListUnit = unitField.getInt(aul);
            int unitListUnit = aul.getIntent().getIntExtra(RecordingActivity.KEY_UNIT, -1);
            System.out.println("units.get is " + units.get(i).getStartVerse());
            System.out.println("i is " + i);
            System.out.println("unitListUnit is " + unitListUnit);
            System.out.println("unitcard start verse is " + unitCardStartVerse);
            assertEquals(unitListUnit, units.get(i).getStartVerse());
            System.out.println("SUCCESS: UnitListUnit " + unitListUnit + " and Unit Number " + units.get(i).getStartVerse() + " are the same!");
            aul.finish();
            aul = null;
        }
        InstrumentationRegistry.getInstrumentation().removeMonitor(activityMonitor);
        unitListActivity.finish();
        instrumentation.removeMonitor(unitListMonitor);

    }

    public static Matcher<RecyclerView.ViewHolder> withUnitNumber(final int unitNumber)
    {
        return new BoundedMatcher<RecyclerView.ViewHolder, UnitCardAdapter.ViewHolder>(UnitCardAdapter.ViewHolder.class)
        {
            @Override
            protected boolean matchesSafely(UnitCardAdapter.ViewHolder item)
            {
                return item.unitCard.getStartVerse() == unitNumber;
            }

            @Override
            public void describeTo(Description description)
            {
                description.appendText("view holder with unit number: " + unitNumber);
            }
        };
    }

}
