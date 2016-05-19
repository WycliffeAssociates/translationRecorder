package wycliffeassociates.recordingapp;

import android.app.Activity;
import android.support.test.rule.ActivityTestRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import wycliffeassociates.recordingapp.ProjectManager.DatabaseHelper;
import wycliffeassociates.recordingapp.ProjectManager.Project;

import static junit.framework.Assert.assertEquals;

/**
 * Created by sarabiaj on 5/11/2016.
 */
public class SqliteTest {

    DatabaseHelper mDb;
    Activity mCtx;

    @Rule
    public ActivityTestRule<MainMenu> mActivityRule = new ActivityTestRule<>(
            MainMenu.class);

    @Before
    public void setUp() {
        mCtx = mActivityRule.getActivity();
        mDb = new DatabaseHelper(mCtx);
    }

    @Test
    public void testNoCuts() {
        mDb.clearTable();
        assertEquals(0, mDb.getNumProjects());
        Project p = new Project("en", "cmn-x-omc", 41, "mat", "ulb", "verse", "ot", "Joe");
        mDb.addProject(p);
        assertEquals(1, mDb.getNumProjects());
        Project p2 = mDb.getAllProjects().get(0);
        assertEquals(p.getTargetLang(), p2.getTargetLang());
        assertEquals(p.getSrcLang(), p2.getSrcLang());
        assertEquals(p.getSlug(), p2.getSlug());
        assertEquals(p.getSource(), p2.getSource());
        assertEquals(p.getMode(), p2.getMode());
        assertEquals(p.getBookNumber(), p2.getBookNumber());
        assertEquals(p.getProject(), p2.getProject());
        assertEquals(p.getContributors(), p2.getContributors());

    }


}
