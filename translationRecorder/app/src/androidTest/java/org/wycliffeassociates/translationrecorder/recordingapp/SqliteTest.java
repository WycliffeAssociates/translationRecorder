/*
package org.wycliffeassociates.translationrecorder.recordingapp;

import android.app.Activity;
import android.support.test.rule.ActivityTestRule;

import org.json.JSONException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.wycliffeassociates.translationrecorder.project.FileNameExtractor;
import org.wycliffeassociates.translationrecorder.project.Project;
import org.wycliffeassociates.translationrecorder.SplashScreen;
import org.wycliffeassociates.translationrecorder.database.ProjectDatabaseHelper;
import org.wycliffeassociates.translationrecorder.project.components.Book;
import org.wycliffeassociates.translationrecorder.project.components.Language;
import org.wycliffeassociates.translationrecorder.project.ParseJSON;

import static junit.framework.Assert.assertEquals;

*/
/**
 * Created by sarabiaj on 5/11/2016.
 *//*

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

        assertEquals(p.getTargetLanguageSlug(), databaseProject1.getTargetLanguageSlug());
        assertEquals(p.getSourceLanguageSlug(), databaseProject1.getSourceLanguageSlug());
        assertEquals(p.getBookSlug(), databaseProject1.getBookSlug());
        assertEquals(p.getVersionSlug(), databaseProject1.getVersionSlug());
        assertEquals(p.getModeSlug(), databaseProject1.getModeSlug());
        assertEquals(p.getBookNumber(), databaseProject1.getBookNumber());
        assertEquals(p.getAnthologySlug(), databaseProject1.getAnthologySlug());
        assertEquals(p.getContributors(), databaseProject1.getContributors());
        assertEquals(p.getSourceAudioPath(), databaseProject1.getSourceAudioPath());

        assertEquals(p2.getTargetLanguageSlug(), databaseProject2.getTargetLanguageSlug());
        assertEquals(p2.getSourceLanguageSlug(), databaseProject2.getSourceLanguageSlug());
        assertEquals(p2.getBookSlug(), databaseProject2.getBookSlug());
        assertEquals(p2.getVersionSlug(), databaseProject2.getVersionSlug());
        assertEquals(p2.getModeSlug(), databaseProject2.getModeSlug());
        assertEquals(p2.getBookNumber(), databaseProject2.getBookNumber());
        assertEquals(p2.getAnthologySlug(), databaseProject2.getAnthologySlug());
        assertEquals(p2.getContributors(), databaseProject2.getContributors());
        assertEquals(p2.getSourceAudioPath(), databaseProject2.getSourceAudioPath());

        FileNameExtractor fne = new FileNameExtractor("en_ulb_b41_mat_c01_v01_t01.wav");
        FileNameExtractor fne2 = new FileNameExtractor("en_ulb_b41_mat_c01_v01_t02.wav");

        mDb.addTake(fne, "en_ulb_b41_mat_c01_v01_t01.wav", "verse", 465123564, 0);

        assertEquals(true, mDb.takeExists(fne));
        assertEquals(false, mDb.takeExists(fne2));

//        assertEquals(p3.getTargetLanguageSlug(), databaseProject3.getTargetLanguageSlug());
//        assertEquals(p3.getSourceLanguageSlug(), databaseProject3.getSourceLanguageSlug());
//        assertEquals(p3.getBookSlug(), databaseProject3.getBookSlug());
//        assertEquals(p3.getVersionSlug(), databaseProject3.getVersionSlug());
//        assertEquals(p3.getModeSlug(), databaseProject3.getModeSlug());
//        assertEquals(p3.getBookNumber(), databaseProject3.getBookNumber());
//        assertEquals(p3.getAnthologySlug(), databaseProject3.getAnthologySlug());
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
                db.addLanguage(language.getSlug(), language.getName());
            }
            System.out.println("Proof: en is " + db.getLanguageName("en"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
*/
