package wycliffeassociates.recordingapp;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.util.Pair;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

import wycliffeassociates.recordingapp.Playback.Editing.CutOp;
import wycliffeassociates.recordingapp.Recording.WavFile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class MetadataTest {

    WavFile wavFile;
    @Rule
    public ActivityTestRule<MainMenu> mActivityRule = new ActivityTestRule<>(
            MainMenu.class);

    @Before
    public void setUp() {
        wavFile = new WavFile();
    }

    @Test
    public void testMetadata() {
        String metadata = "{\"book\":\"mat\",\"lang\":\"en\",\"chap\":1,\"startv\":1,\"endv\":2,\"marker1\":1234}";
        System.out.println(metadata);
        System.out.println();
        byte[] data = WavFile.convertToMetadata(metadata);
        String result = WavFile.readMetadata(data).toString();
        System.out.println(result);
        assertEquals(metadata, result);
    }
}
