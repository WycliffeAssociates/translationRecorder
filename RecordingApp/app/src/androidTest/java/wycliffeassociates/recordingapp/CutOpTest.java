package wycliffeassociates.recordingapp;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.util.Pair;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Vector;

import wycliffeassociates.recordingapp.Playback.Editing.CutOp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class CutOpTest {

    CutOp cutOp = null;

    @Rule
    public ActivityTestRule<MainMenu> mActivityRule = new ActivityTestRule<>(
            MainMenu.class);

    @Before
    public void setUp() {
        cutOp = new CutOp();
    }

    @Test
    public void testNoCuts() {
        assertFalse(cutOp.hasCut());
        assertEquals(0, cutOp.getSizeCut());
        assertEquals(0, cutOp.getSizeCutCmp());
        assertEquals(0, cutOp.getSizeCut());
        assertEquals(-1, cutOp.skip(0));
        assertEquals(-1, cutOp.skip(10));
        assertEquals(-1, cutOp.skip(100));
        assertEquals(-1, cutOp.skip(1000));
    }

    @Test
    public void testSingleCutInMiddle() {
        cutOp.cut(1000, 2000);
        assertTrue(cutOp.hasCut());

        // cutOp.skip() returns -1 if outside a skip,
        // otherwise returns the upper bound of the skip
        assertEquals(-1, cutOp.skip(0));
        assertEquals(-1, cutOp.skip(500));
        assertEquals(2000, cutOp.skip(1000));
        assertEquals(2000, cutOp.skip(1500));
        assertEquals(-1, cutOp.skip(2000));
        assertEquals(-1, cutOp.skip(2500));

        // cutOp.skipReverse() returns Integer.MAX_VALUE if outside a skip,
        // otherwise returns the lower bound of the skip
        assertEquals(Integer.MAX_VALUE, cutOp.skipReverse(0));
        assertEquals(Integer.MAX_VALUE, cutOp.skipReverse(500));
        assertEquals(Integer.MAX_VALUE, cutOp.skipReverse(1000));
        assertEquals(1000, cutOp.skipReverse(1500));
        assertEquals(1000, cutOp.skipReverse(2000));
        assertEquals(Integer.MAX_VALUE, cutOp.skipReverse(2500));

        // cutOp.timeAdjusted returns the offset from the given
        // time taking any cuts into account.
        assertEquals(0, cutOp.timeAdjusted(0));
        assertEquals(500, cutOp.timeAdjusted(500));
        assertEquals(2000, cutOp.timeAdjusted(1000));
        assertEquals(2500, cutOp.timeAdjusted(1500));
        assertEquals(3000, cutOp.timeAdjusted(2000));
        assertEquals(3500, cutOp.timeAdjusted(2500));

        // playbackStart before the cut will return the same values
        // as above.
        assertEquals(0, cutOp.timeAdjusted(0, 0));
        assertEquals(500, cutOp.timeAdjusted(500, 0));
        assertEquals(2000, cutOp.timeAdjusted(1000, 0));
        assertEquals(2500, cutOp.timeAdjusted(1500, 0));
        assertEquals(3000, cutOp.timeAdjusted(2000, 0));
        assertEquals(3500, cutOp.timeAdjusted(2500, 0));
        assertEquals(500, cutOp.timeAdjusted(500, 500));
        assertEquals(2000, cutOp.timeAdjusted(1000, 500));
        assertEquals(2500, cutOp.timeAdjusted(1500, 500));
        assertEquals(3000, cutOp.timeAdjusted(2000, 500));
        assertEquals(2000, cutOp.timeAdjusted(1000, 1000));
        assertEquals(2500, cutOp.timeAdjusted(1500, 1000));
        assertEquals(3000, cutOp.timeAdjusted(2000, 1000));

        // playbackStart *after* the cut will return a value
        // that does not take the previous cut into account
        // (because we started after the cut)
        assertEquals(2000, cutOp.timeAdjusted(2000, 2000));
        assertEquals(2500, cutOp.timeAdjusted(2500, 2000));
        assertEquals(3000, cutOp.timeAdjusted(3000, 2000));

        // reverseTimeAdjusted() accepts an absolute time
        // and returns the relative time -- that is, with the
        // cuts removed
        assertEquals(500, cutOp.reverseTimeAdjusted(500));
        assertEquals(1000, cutOp.reverseTimeAdjusted(1000));
        assertEquals(1000, cutOp.reverseTimeAdjusted(2000));
        assertEquals(1500, cutOp.reverseTimeAdjusted(2500));
        assertEquals(2000, cutOp.reverseTimeAdjusted(3000));


    }

    @Test
    public void testTimeToLoc() {
        // Adding a single millisecond adds 88.2 samples.
        // 1000 ms and 1005ms multiply evenly and so are
        // exact multiples.  Values in the middle should
        // leave off the partial sample and never be an odd number.
        assertEquals(88200, cutOp.timeToUncmpLoc(1000));
        assertEquals(88288, cutOp.timeToUncmpLoc(1001));
        assertEquals(88376, cutOp.timeToUncmpLoc(1002));
        assertEquals(88464, cutOp.timeToUncmpLoc(1003));
        assertEquals(88552, cutOp.timeToUncmpLoc(1004));
        assertEquals(88640, cutOp.timeToUncmpLoc(1005));
    }

    @Test
    public void testTimeToCmpLoc() {
        // ms / 1000 * 441000 / 25 * 2
        // ...but all integer math
        assertEquals(3528, cutOp.timeToCmpLoc(1000));
        assertEquals(3530, cutOp.timeToCmpLoc(1001));
        assertEquals(3534, cutOp.timeToCmpLoc(1002));
        assertEquals(3538, cutOp.timeToCmpLoc(1003));
        assertEquals(3542, cutOp.timeToCmpLoc(1004));
        assertEquals(3544, cutOp.timeToCmpLoc(1005));
    }


}
