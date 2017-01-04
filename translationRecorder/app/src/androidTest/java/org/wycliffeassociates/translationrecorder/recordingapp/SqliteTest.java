package org.wycliffeassociates.translationrecorder.recordingapp;

import android.app.Activity;
import android.support.test.rule.ActivityTestRule;

import org.json.JSONException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.wycliffeassociates.translationrecorder.FilesPage.FileNameExtractor;
import org.wycliffeassociates.translationrecorder.ProjectManager.Project;
import org.wycliffeassociates.translationrecorder.SplashScreen;
import org.wycliffeassociates.translationrecorder.database.ProjectDatabaseHelper;
import org.wycliffeassociates.translationrecorder.project.Book;
import org.wycliffeassociates.translationrecorder.project.Language;
import org.wycliffeassociates.translationrecorder.project.ParseJSON;

import static junit.framework.Assert.assertEquals;

/**
 * Created by sarabiaj on 5/11/2016.
 */
public class SqliteTest {

    ProjectDatabaseHelper mDb;
    Activity mCtx;

    @Rule
    public ActivityTestRule<SplashScreen> mActivityRule = new ActivityTestRule<>(
            SplashScreen.class);

    @Before
    public void setUp() {
        mCtx = mActivityRule.getActivity();
        mDb = new ProjectDatabaseHelper(mCtx);
    }

    @Test
    public void testDatabase() {
        mDb.deleteAllTables();
        mDb = null;
        mDb = new ProjectDatabaseHelper(mCtx);
        configureConstants(mDb);
        assertEquals(0, mDb.getNumProjects());
        Project p = new Project("en", "cmn-x-omc", 41, "mat", "ulb", "verse", "nt", "Joe", "C:/nope/not/a/real/path");
        Project p2 = new Project("en", "en", 42, "mrk", "udb", "chunk", "nt", "J", "C:/nope/not/a/real/path");
        //Project p3 = new Project("en", "en", null, null, null, null, "obs", "J", "C:/nope/not/a/real/path");

        mDb.addProject(p);
        mDb.addProject(p2);
        //mDb.addProject(p3);
        assertEquals(2, mDb.getNumProjects());

        mDb.addChapter(p, 1);
        mDb.addUnit(p, 1, 1);

        Project databaseProject1 = mDb.getAllProjects().get(0);
        Project databaseProject2 = mDb.getAllProjects().get(1);
        //Project databaseProject3 = mDb.getAllProjects().get(2);

        assertEquals(p.getTargetLanguage(), databaseProject1.getTargetLanguage());
        assertEquals(p.getSourceLanguage(), databaseProject1.getSourceLanguage());
        assertEquals(p.getSlug(), databaseProject1.getSlug());
        assertEquals(p.getVersion(), databaseProject1.getVersion());
        assertEquals(p.getMode(), databaseProject1.getMode());
        assertEquals(p.getBookNumber(), databaseProject1.getBookNumber());
        assertEquals(p.getAnthology(), databaseProject1.getAnthology());
        assertEquals(p.getContributors(), databaseProject1.getContributors());
        assertEquals(p.getSourceAudioPath(), databaseProject1.getSourceAudioPath());

        assertEquals(p2.getTargetLanguage(), databaseProject2.getTargetLanguage());
        assertEquals(p2.getSourceLanguage(), databaseProject2.getSourceLanguage());
        assertEquals(p2.getSlug(), databaseProject2.getSlug());
        assertEquals(p2.getVersion(), databaseProject2.getVersion());
        assertEquals(p2.getMode(), databaseProject2.getMode());
        assertEquals(p2.getBookNumber(), databaseProject2.getBookNumber());
        assertEquals(p2.getAnthology(), databaseProject2.getAnthology());
        assertEquals(p2.getContributors(), databaseProject2.getContributors());
        assertEquals(p2.getSourceAudioPath(), databaseProject2.getSourceAudioPath());

        FileNameExtractor fne = new FileNameExtractor("en_ulb_b41_mat_c01_v01_t01.wav");
        FileNameExtractor fne2 = new FileNameExtractor("en_ulb_b41_mat_c01_v01_t02.wav");

        mDb.addTake(fne, "en_ulb_b41_mat_c01_v01_t01.wav", 465123564, 0);

        assertEquals(true, mDb.takeExists(fne));
        assertEquals(false, mDb.takeExists(fne2));

//        assertEquals(p3.getTargetLanguage(), databaseProject3.getTargetLanguage());
//        assertEquals(p3.getSourceLanguage(), databaseProject3.getSourceLanguage());
//        assertEquals(p3.getSlug(), databaseProject3.getSlug());
//        assertEquals(p3.getVersion(), databaseProject3.getVersion());
//        assertEquals(p3.getMode(), databaseProject3.getMode());
//        assertEquals(p3.getBookNumber(), databaseProject3.getBookNumber());
//        assertEquals(p3.getAnthology(), databaseProject3.getAnthology());
//        assertEquals(p3.getContributors(), databaseProject3.getContributors());
//        assertEquals(p3.getSourceAudioPath(), databaseProject3.getSourceAudioPath());
    }

    private void configureConstants(ProjectDatabaseHelper db) {
        ParseJSON parse = new ParseJSON(mCtx);
        try {
            Book[] books = parse.pullBooks();
            Language[] languages = parse.pullLangNames();
            for (Book book : books) {
                db.addBook(book.getSlug(), book.getName(), book.getAnthology(), book.getOrder());
            }
            for (Language language : languages) {
                db.addLanguage(language.getCode(), language.getName());
            }
            System.out.println("Proof: en is " + db.getLanguageName("en"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
