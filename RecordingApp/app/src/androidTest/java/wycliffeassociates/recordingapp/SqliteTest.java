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
    public void testDatabase() {
        mDb.clearTable();
        assertEquals(0, mDb.getNumProjects());
        Project p = new Project("en", "cmn-x-omc", 41, "mat", "ulb", "verse", "nt", "Joe", "C:/nope/not/a/real/path");
        Project p2 = new Project("en", "en", 42, "mrk", "udb", "chunk", "nt", "J", "C:/nope/not/a/real/path");
        Project p3 = new Project("en", "en", null, null, null, null, "obs", "J", "C:/nope/not/a/real/path");

        mDb.addProject(p);
        mDb.addProject(p2);
        mDb.addProject(p3);
        assertEquals(3, mDb.getNumProjects());

        Project databaseProject1 = mDb.getAllProjects().get(0);
        Project databaseProject2 = mDb.getAllProjects().get(1);
        Project databaseProject3 = mDb.getAllProjects().get(2);

        assertEquals(p.getTargetLang(), databaseProject1.getTargetLang());
        assertEquals(p.getSrcLang(), databaseProject1.getSrcLang());
        assertEquals(p.getSlug(), databaseProject1.getSlug());
        assertEquals(p.getSource(), databaseProject1.getSource());
        assertEquals(p.getMode(), databaseProject1.getMode());
        assertEquals(p.getBookNumber(), databaseProject1.getBookNumber());
        assertEquals(p.getProject(), databaseProject1.getProject());
        assertEquals(p.getContributors(), databaseProject1.getContributors());
        assertEquals(p.getSourceAudioPath(), databaseProject1.getSourceAudioPath());

        assertEquals(p2.getTargetLang(), databaseProject2.getTargetLang());
        assertEquals(p2.getSrcLang(), databaseProject2.getSrcLang());
        assertEquals(p2.getSlug(), databaseProject2.getSlug());
        assertEquals(p2.getSource(), databaseProject2.getSource());
        assertEquals(p2.getMode(), databaseProject2.getMode());
        assertEquals(p2.getBookNumber(), databaseProject2.getBookNumber());
        assertEquals(p2.getProject(), databaseProject2.getProject());
        assertEquals(p2.getContributors(), databaseProject2.getContributors());
        assertEquals(p2.getSourceAudioPath(), databaseProject2.getSourceAudioPath());

        assertEquals(p3.getTargetLang(), databaseProject3.getTargetLang());
        assertEquals(p3.getSrcLang(), databaseProject3.getSrcLang());
        assertEquals(p3.getSlug(), databaseProject3.getSlug());
        assertEquals(p3.getSource(), databaseProject3.getSource());
        assertEquals(p3.getMode(), databaseProject3.getMode());
        assertEquals(p3.getBookNumber(), databaseProject3.getBookNumber());
        assertEquals(p3.getProject(), databaseProject3.getProject());
        assertEquals(p3.getContributors(), databaseProject3.getContributors());
        assertEquals(p3.getSourceAudioPath(), databaseProject3.getSourceAudioPath());
    }
}
