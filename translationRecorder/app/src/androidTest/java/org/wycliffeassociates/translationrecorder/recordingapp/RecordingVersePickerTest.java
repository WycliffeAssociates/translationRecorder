package org.wycliffeassociates.translationrecorder.recordingapp;

import androidx.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class RecordingVersePickerTest {
//
//
//    @Rule
//    public ActivityTestRule<MainMenu> mActivityRule = new ActivityTestRule<MainMenu>(MainMenu.class);
//
//    @Before
//    public void resetPreferences() {
//        Context context = InstrumentationRegistry.getTargetContext();
//        String defaultPreferencesName = context.getPackageName() + "_preferences";
//        SharedPreferences prefs = context.getSharedPreferences(defaultPreferencesName, Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = prefs.edit();
//        editor.putString(Settings.KEY_PREF_VERSION, "ulb");
//        editor.putString(Settings.KEY_PREF_LANG, "en");
//        editor.putString(Settings.KEY_PREF_BOOK, "gen");
//        editor.putString(Settings.KEY_PREF_CHAPTER, "01");
//        editor.putString(Settings.KEY_PREF_VERSE, "01");
//        editor.putString(Settings.KEY_PREF_TAKE, "01");
//        editor.putString(Settings.KEY_PREF_FILENAME, "en_ulb_gen_01-01_01");
//        editor.putString(Settings.KEY_PREF_CHUNK_VERSE, "verse");
//        editor.commit();
//    }
//
//    @Test
//    public void testVersePicker() {
//
//        // Touch the "new_record" button (the big microphone)
//        onView(withId(R.id.new_record)).perform(click());
//        // TODO: What's the right way to wait for the UI to update the picker?
//        try {
//            Thread.sleep(2500);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//        // Check to see if this is a Samsung model
//        boolean isSamsung = Build.MODEL.startsWith("SM");
//
//        // Go forwards
//        for (int i = 1; i <= 31; i++) {
//            onView(withId(R.id.numberPicker)).check(matches(hasNumberPickerDisplayedValue(is(new Integer(i).toString()))));
//            if (isSamsung) {
//                // Samsung's number picker is the inverse of the stock one
//                // (clicking on the bottom increments it)
//                onView(withId(R.id.numberPicker)).perform(clickXY(10, 150));
//            } else {
//                onView(withId(R.id.numberPicker)).perform(clickXY(10, 10));
//            }
//            // Test for wraparound
//            if (i == 31) {
//                onView(withId(R.id.numberPicker)).check(matches(hasNumberPickerDisplayedValue(is("1"))));
//            }
//        }
//
//        // Go backwards
//        for (int i = 31; i >= 1; i--) {
//            if (isSamsung) {
//                // Samsung's number picker is the inverse of the stock one
//                // (clicking on the top decrements it)
//                onView(withId(R.id.numberPicker)).perform(clickXY(10, 10));
//            } else {
//                onView(withId(R.id.numberPicker)).perform(clickXY(10, 200));
//            }
//            onView(withId(R.id.numberPicker)).check(matches(hasNumberPickerDisplayedValue(is(new Integer(i).toString()))));
//            // Test for wraparound
//            if (i == 1) {
//                if (isSamsung) {
//                    // Samsung's number picker is the inverse of the stock one
//                    // (clicking on the top decrements it)
//                    onView(withId(R.id.numberPicker)).perform(clickXY(10, 10));
//                } else {
//                    onView(withId(R.id.numberPicker)).perform(clickXY(10, 200));
//                }
//                onView(withId(R.id.numberPicker)).check(matches(hasNumberPickerDisplayedValue(is("31"))));
//            }
//        }
//
//    }
//
//
//    @Test
//    public void testInitialValue() {
//
//        // Set verse preference to "20"
//        Context context = InstrumentationRegistry.getTargetContext();
//        String defaultPreferencesName = context.getPackageName() + "_preferences";
//        SharedPreferences prefs = context.getSharedPreferences(defaultPreferencesName, Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = prefs.edit();
//        editor.putString(Settings.KEY_PREF_VERSE, "20");
//        editor.putString(Settings.KEY_PREF_FILENAME, "en_ulb_gen_01-20_01");
//        editor.commit();
//
//        // Touch the "new_record" button (the big microphone)
//        onView(withId(R.id.new_record)).perform(click());
//
//        // TODO: What's the right way to wait for the UI to update the picker?
//        try {
//            Thread.sleep(2500);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//        // Confirm that chunk picker shows chunk 20
//        onView(withId(R.id.numberPicker)).check(matches(hasNumberPickerDisplayedValue(is("20"))));
//
//    }

}
