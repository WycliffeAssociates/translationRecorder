package wycliffeassociates.recordingapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.action.CoordinatesProvider;
import android.support.test.espresso.action.GeneralClickAction;
import android.support.test.espresso.action.Press;
import android.support.test.espresso.action.Tap;
import android.support.test.espresso.matcher.BoundedMatcher;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.util.Log;
import android.view.View;
import android.widget.NumberPicker;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import wycliffeassociates.recordingapp.SettingsPage.Settings;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.is;
import static wycliffeassociates.recordingapp.TestUtils.clickXY;
import static wycliffeassociates.recordingapp.TestUtils.hasNumberPickerDisplayedValue;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class RecordingChunkPickerTest {


    @Rule
    public ActivityTestRule<MainMenu> mActivityRule = new ActivityTestRule<MainMenu>(MainMenu.class);

    @Before
    public void resetPreferences() {
        Context context = InstrumentationRegistry.getTargetContext();
        String defaultPreferencesName = context.getPackageName() + "_preferences";
        SharedPreferences prefs = context.getSharedPreferences(defaultPreferencesName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Settings.KEY_PREF_SOURCE, "ulb");
        editor.putString(Settings.KEY_PREF_LANG, "en");
        editor.putString(Settings.KEY_PREF_BOOK, "gen");
        editor.putString(Settings.KEY_PREF_CHAPTER, "01");
        editor.putString(Settings.KEY_PREF_CHUNK, "01");
        editor.putString(Settings.KEY_PREF_TAKE, "01");
        editor.putString(Settings.KEY_PREF_FILENAME, "en_ulb_gen_01-01_01");
        editor.putString(Settings.KEY_PREF_CHUNK_VERSE, "chunk");
        editor.commit();
    }

    @Test
    public void testChunkPicker() {
        // Touch the "new_record" button (the big microphone)
        onView(withId(R.id.new_record)).perform(click());
        // TODO: What's the right way to wait for the UI to update the picker?
        try {
            Thread.sleep(2500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Check to see if this is a Samsung model
        boolean isSamsung = Build.MODEL.startsWith("SM");

        // Go forwards
        String expectedValues[] = {"1", "3", "6", "9", "11", "14", "16", "20", "22", "24", "26", "28", "30", "1"};
        for (String expectedValue: expectedValues) {
            onView(withId(R.id.numberPicker)).check(matches(hasNumberPickerDisplayedValue(is(expectedValue))));
            if (isSamsung) {
                // Samsung's number picker is the inverse of the stock one
                // (clicking on the bottom increments it)
                onView(withId(R.id.numberPicker)).perform(clickXY(10, 150));
            } else {
                onView(withId(R.id.numberPicker)).perform(clickXY(10, 10));
            }
        }

        // Now go backwards
        for (int i = expectedValues.length - 1; i > 0; i--) {
            String expectedValue = expectedValues[i];
            if (isSamsung) {
                // Samsung's number picker is the inverse of the stock one
                // (clicking on the top decrements it)
                onView(withId(R.id.numberPicker)).perform(clickXY(10, 10));
            } else {
                onView(withId(R.id.numberPicker)).perform(clickXY(10, 200));
            }
            onView(withId(R.id.numberPicker)).check(matches(hasNumberPickerDisplayedValue(is(expectedValue))));
        }

    }

    @Test
    public void testInitialValue() {
        // Set to chunk 20
        Context context = InstrumentationRegistry.getTargetContext();
        String defaultPreferencesName = context.getPackageName() + "_preferences";
        SharedPreferences prefs = context.getSharedPreferences(defaultPreferencesName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Settings.KEY_PREF_CHUNK, "20");
        editor.putString(Settings.KEY_PREF_FILENAME, "en_ulb_gen_01-20_01");
        editor.commit();
        // Touch the "new_record" button (the big microphone)
        onView(withId(R.id.new_record)).perform(click());
        // TODO: What's the right way to wait for the UI to update the picker?
        try {
            Thread.sleep(2500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // Confirm that chunk picker shows chunk 20
        onView(withId(R.id.numberPicker)).check(matches(hasNumberPickerDisplayedValue(is("20"))));

    }
}
