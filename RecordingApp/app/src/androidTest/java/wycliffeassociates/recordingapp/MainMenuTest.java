package wycliffeassociates.recordingapp;

import android.support.test.espresso.intent.Intents;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import wycliffeassociates.recordingapp.FilesPage.AudioFiles;
import wycliffeassociates.recordingapp.Recording.RecordingScreen;
import wycliffeassociates.recordingapp.SettingsPage.Settings;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class MainMenuTest {

    @Rule
    public ActivityTestRule<MainMenu> mActivityRule = new ActivityTestRule<>(
            MainMenu.class);

    @Test
    public void testLaunchAudioFiles() {
        // Start recording intents
        Intents.init();
        // Touch the "new_record" button (the big microphone)
        onView(withId(R.id.files)).perform(click());
        // Verify that an Intent was sent to open the Recording Screen
        Intents.intended(hasComponent(AudioFiles.class.getName()));
        // Stop recording intents
        Intents.release();
    }

    @Test
    public void testLaunchRecordingScreen() {
        // Start recording intents
        Intents.init();
        // Touch the "new_record" button (the big microphone)
        onView(withId(R.id.new_record)).perform(click());
        // Verify that an Intent was sent to open the Recording Screen
        Intents.intended(hasComponent(RecordingScreen.class.getName()));
        // Stop recording intents
        Intents.release();
    }

    @Test
    public void testLaunchSettings() {
        // Start recording intents
        Intents.init();
        // Touch the "new_record" button (the big microphone)
        onView(withId(R.id.settings)).perform(click());
        // Verify that an Intent was sent to open the Recording Screen
        Intents.intended(hasComponent(Settings.class.getName()));
        // Stop recording intents
        Intents.release();
    }
}
