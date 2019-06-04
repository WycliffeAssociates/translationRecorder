package org.wycliffeassociates.translationrecorder.recordingapp.IntentTests;

import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import androidx.test.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wycliffeassociates.translationrecorder.MainMenu;
import org.wycliffeassociates.translationrecorder.Playback.PlaybackActivity;
import org.wycliffeassociates.translationrecorder.Recording.RecordingActivity;
import org.wycliffeassociates.translationrecorder.Recording.UnitPicker;
import org.wycliffeassociates.translationrecorder.Recording.fragments.FragmentRecordingFileBar;
import org.wycliffeassociates.translationrecorder.chunkplugin.Chapter;
import org.wycliffeassociates.translationrecorder.chunkplugin.ChunkPlugin;
import org.wycliffeassociates.translationrecorder.database.ProjectDatabaseHelper;
import org.wycliffeassociates.translationrecorder.project.ChunkPluginLoader;
import org.wycliffeassociates.translationrecorder.project.Project;
import org.wycliffeassociates.translationrecorder.project.components.Anthology;
import org.wycliffeassociates.translationrecorder.project.components.Book;
import org.wycliffeassociates.translationrecorder.project.components.Language;
import org.wycliffeassociates.translationrecorder.project.components.Mode;
import org.wycliffeassociates.translationrecorder.project.components.Version;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static androidx.test.InstrumentationRegistry.getInstrumentation;
import static org.junit.Assert.assertEquals;
import static org.wycliffeassociates.translationrecorder.recordingapp.ProjectMockingUtil.createBibleTestProject;
import static org.wycliffeassociates.translationrecorder.recordingapp.ProjectMockingUtil.createNotesTestProject;

/**
 * Created by sarabiaj on 8/30/2017.
 */

@RunWith(AndroidJUnit4.class)
@LargeTest
public class RecordingActivityIntentTest {

    private static final int TIME_OUT = 5000; /* miliseconds */

    @Rule
    public ActivityTestRule<RecordingActivity> mActivityRule =
            new ActivityTestRule<>(
                    RecordingActivity.class,
                    true,
                    false
            );

    @Rule
    public ActivityTestRule<MainMenu> mSplashScreenRule =
            new ActivityTestRule<>(
                    MainMenu.class,
                    true,
                    false
            );

    @Test
    public void testNewProjects() {
        Project project = createBibleTestProject(mSplashScreenRule);
        testRecordingActivityDataFlow(project, 1, 1);
        System.out.println("Passed chapter 1 unit 1!");
        project = createBibleTestProject(mSplashScreenRule);
        testRecordingActivityDataFlow(project, 1, 2);
        System.out.println("Passed chapter 1 unit 2!");
        project = createBibleTestProject(mSplashScreenRule);
        testRecordingActivityDataFlow(project, 2, 1);
        System.out.println("Passed chapter 2 unit 1!");
        project = createBibleTestProject(mSplashScreenRule);
        testRecordingActivityDataFlow(project, 2, 2);
        System.out.println("Passed chapter 2 unit 2!");

        //use chunk 3 since there is no chunk 2
        Project projectNotes = createNotesTestProject(mSplashScreenRule);
        testRecordingActivityDataFlow(projectNotes, 1, 1);
        System.out.println("Passed chunk 1 text 1!");
        projectNotes = createNotesTestProject(mSplashScreenRule);
        testRecordingActivityDataFlow(projectNotes, 1, 2);
        System.out.println("Passed chunk 1 ref 1!");
        projectNotes = createNotesTestProject(mSplashScreenRule);
        testRecordingActivityDataFlow(projectNotes, 3, 1);
        System.out.println("Passed chunk 3 text 1!");
        projectNotes = createNotesTestProject(mSplashScreenRule);
        testRecordingActivityDataFlow(projectNotes, 3, 2);
        System.out.println("Passed chunk 3 ref 1!");
    }

    public void testRecordingActivityDataFlow(Project project, int chapter, int unit) {
        //construct Intent to initialize playback activity based on parameters
        Intent intent = RecordingActivity.getNewRecordingIntent(
                InstrumentationRegistry.getContext(),
                project,
                chapter,
                unit
        );

        //launch our activity with this intent
        mActivityRule.launchActivity(intent);
        RecordingActivity recordingActivity = mActivityRule.getActivity();
        testRecordingActivityInitialization(recordingActivity, project, chapter, unit);
        testFlowToPlaybackActivity(recordingActivity, chapter, unit);
    }

    public void testRecordingActivityInitialization(
            RecordingActivity recordingActivity,
            Project project,
            int chapter,
            int unit
    ) {
        try {
            //test initial chapter member variable is what we would expect
            Field chapterField = recordingActivity.getClass().getDeclaredField("mInitialChapter");
            chapterField.setAccessible(true);
            int mChapter = (int) chapterField.get(recordingActivity);
            assertEquals(
                    "Chapter number used for intent vs recording activity member variable",
                    chapter,
                    mChapter
            );

            //test initial chunk member variable is what we would expect
            Field chunkField = recordingActivity.getClass().getDeclaredField("mInitialChunk");
            chunkField.setAccessible(true);
            int mChunk = (int) chapterField.get(recordingActivity);
            assertEquals(unit, mChunk);

            //test that these initial values correctly initialized the fragment field
            Field fragmentField = recordingActivity
                    .getClass()
                    .getDeclaredField("mFragmentRecordingFileBar");
            fragmentField.setAccessible(true);
            FragmentRecordingFileBar frfb =
                    (FragmentRecordingFileBar) fragmentField.get(recordingActivity);
            assertEquals(chapter, frfb.getChapter());
            assertEquals(unit, frfb.getUnit());

            Field chapterPickerField = frfb.getClass().getDeclaredField("mChapterPicker");
            chapterPickerField.setAccessible(true);
            UnitPicker mChapterPicker = (UnitPicker) chapterPickerField.get(frfb);
            assertEquals(
                    project.getFileName(
                        chapter,
                        frfb.getChapter(),
                        Integer.parseInt(frfb.getStartVerse()),
                        Integer.parseInt(frfb.getEndVerse())
                    ),
                    mChapterPicker.getCurrentDisplayedValue()
            );

        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public void testFlowToPlaybackActivity(
            final RecordingActivity recordingActivity,
            int chapter,
            int unit
    ){
        //Attach monitor to listen for the playback activity to launch
        Instrumentation instrumentation = getInstrumentation();
        Instrumentation.ActivityMonitor monitor = instrumentation.addMonitor(
                PlaybackActivity.class.getName(),
                null,
                false
        );

        recordingActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //Fire intent from recording Activity
                recordingActivity.onStartRecording();
            }
        });

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        recordingActivity.onStopRecording();

        //try to get the playback activity now that the intent has fired
        PlaybackActivity pba = (PlaybackActivity) instrumentation.waitForMonitorWithTimeout(
                monitor,
                TIME_OUT
        );

        Intent intent = pba.getIntent();

        int playbackChapter = intent.getIntExtra(PlaybackActivity.KEY_CHAPTER, -1);
        int playbackUnit = intent.getIntExtra(PlaybackActivity.KEY_UNIT, -1);
        assertEquals("chapter and playback initialized chapter", chapter, playbackChapter);
        assertEquals("unit and playback initialized unit", unit, playbackUnit);

        pba.finish();
    }

    //constructs a list of all possible combinations of projects
    List<Project> getProjectsList() {
        ArrayList<Project> projects = new ArrayList<>();
        Context ctx = InstrumentationRegistry.getContext();
        ProjectDatabaseHelper db = new ProjectDatabaseHelper(ctx);
        Anthology[] anthologies = db.getAnthologies();
        Language[] languages = new Language[] {new Language("en", "English")};
        for(Language language : languages) {
            Project project = new Project();
            project.setTargetLanguage(language);
            for (Anthology anthology : anthologies) {
                project.setAnthology(anthology);
                Version[] versions = db.getVersions(anthology.getSlug());
                for (Version version : versions) {
                    project.setVersion(version);
                    Book[] books = db.getBooks(anthology.getSlug());
                    for (Book book : books) {
                        project.setBook(book);
                        Mode[] modes = db.getModes(anthology.getSlug());
                        for (Mode mode : modes) {
                            project.setMode(mode);
                            projects.add(project);
                        }
                    }
                }
            }
        }
        return projects;
    }

    List<Chapter> getChunkPlugin(Context ctx, Project project) throws NoSuchFieldException, IOException, IllegalAccessException {
            ChunkPlugin plugin = project.getChunkPlugin(new ChunkPluginLoader(ctx));
            Field field = ChunkPlugin.class.getDeclaredField("mChapters");
            List<Chapter> chapters = (List<Chapter>) field.get(plugin);
            return chapters;
    }

    int[] getChapterNumbersArray(List<Chapter> chapters) {
        int[] chapterNumbers = new int[chapters.size()];
        for(int i = 0; i < chapterNumbers.length; i++) {
            chapterNumbers[i] = chapters.get(i).getNumber();
        }
        return chapterNumbers;
    }

}
