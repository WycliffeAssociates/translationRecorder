package org.wycliffeassociates.translationrecorder.recordingapp;

import androidx.test.espresso.ViewAction;
import androidx.test.espresso.action.CoordinatesProvider;
import androidx.test.espresso.action.GeneralClickAction;
import androidx.test.espresso.action.Press;
import androidx.test.espresso.action.Tap;
import androidx.test.espresso.matcher.BoundedMatcher;
import android.view.View;
import android.widget.NumberPicker;

import org.hamcrest.Description;
import org.hamcrest.Matcher;

/**
 * Created by oliverc on 3/3/2016.
 */
public class TestUtils {

    public static Matcher<View> hasNumberPickerValue(final Matcher<Integer> intMatcher) {
        return new BoundedMatcher<View, NumberPicker>(NumberPicker.class) {

            @Override
            public void describeTo(final Description description) {
                description.appendText("With number picker index value: ");
                intMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(final NumberPicker view) {
                return intMatcher.matches(view.getValue());
            }
        };
    }

    public static Matcher<View> hasNumberPickerDisplayedValue(final Matcher<String> stringMatcher) {
        return new BoundedMatcher<View, NumberPicker>(NumberPicker.class) {

            @Override
            public void describeTo(final Description description) {
                description.appendText("With number picker displayed value: ");
                stringMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(final NumberPicker view) {
                int index = view.getValue();
                String[] displayedValues = view.getDisplayedValues();
                return stringMatcher.matches(displayedValues[index-1]);
            }
        };
    }

    public static ViewAction clickXY(final int x, final int y){
        return new GeneralClickAction(
                Tap.SINGLE,
                new CoordinatesProvider() {
                    @Override
                    public float[] calculateCoordinates(View view) {

                        final int[] screenPos = new int[2];
                        view.getLocationOnScreen(screenPos);

                        final float screenX = screenPos[0] + x;
                        final float screenY = screenPos[1] + y;
                        float[] coordinates = {screenX, screenY};

                        return coordinates;
                    }
                },
                Press.FINGER);
    }

}
