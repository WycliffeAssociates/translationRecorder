package wycliffeassociates.recordingapp;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.util.Pair;

import org.json.JSONException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

import wycliffeassociates.recordingapp.Playback.Editing.CutOp;
import wycliffeassociates.recordingapp.ProjectManager.Project;
import wycliffeassociates.recordingapp.Recording.WavFile;
import wycliffeassociates.recordingapp.Recording.WavFileWriter;

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
    public void setUp() {}

    @Test
    public void testMetadata() {
        try {
            File testFile = File.createTempFile("test", "wav");
            byte[] headerBuffer = new byte[44];
            FileOutputStream fos = new FileOutputStream(testFile);
            fos.write(headerBuffer);
            fos.close();
            Project project = new Project("en", "", "01", "gen", "ulb", "chunk", "ot", "", "");
            wavFile = new WavFile(testFile, project);
            String metadata = wavFile.getMetadata();
            System.out.println(metadata);
            int size = wavFile.writeMetadata();
            WavFileWriter.overwriteHeaderData(testFile, 44, size);
            wavFile = new WavFile(testFile);
            String parsedMetadata = wavFile.getMetadata();
            assertEquals(parsedMetadata, metadata);
        } catch (JSONException e){
            Assert.fail("Test failed : " + e.getMessage());
        } catch (IOException e) {
            Assert.fail("Test failed : " + e.getMessage());
        }
    }
}
