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
import java.io.RandomAccessFile;
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
//            byte[] headerBuffer = new byte[44];
//            FileOutputStream fos = new FileOutputStream(testFile);
//            fos.write(headerBuffer);
//            fos.close();
            Project project = new Project("en", "", "01", "gen", "ulb", "chunk", "ot", "", "");
            wavFile = new WavFile(testFile);
            wavFile.setMetadata(project, "1", "1", "1");
            String metadata = wavFile.getMetadata();
            System.out.println(metadata);
            int size = wavFile.writeMetadata();
            WavFileWriter.overwriteHeaderData(testFile, 44, size);
            wavFile = new WavFile(testFile);
            String parsedMetadata = wavFile.getMetadata();
            assertEquals(parsedMetadata, metadata);
            assertEquals(wavFile.getTotalMetadataLength() % 4, 0);
        } catch (JSONException e){
            Assert.fail("Test failed : " + e.getMessage());
        } catch (IOException e) {
            Assert.fail("Test failed : " + e.getMessage());
        }
    }

    @Test
    public void testWavFile(){
        try{
            File testFile = File.createTempFile("test1", "wav");
            byte[] headerBuffer = new byte[]{1,2,3,4};
            FileOutputStream fos = new FileOutputStream(testFile);
            fos.write(headerBuffer);
            fos.close();
            System.out.println(testFile.length());
            wavFile = new WavFile(testFile);
            System.out.println(testFile.length());
            RandomAccessFile raf = new RandomAccessFile(testFile, "r");
            raf.seek(44);
            byte[] compare = new byte[headerBuffer.length];
            raf.read(compare);
            for(int i = 0; i < headerBuffer.length; i++) {
                assertEquals(headerBuffer[i], compare[i]);
            }
            assertEquals(wavFile.getTotalAudioLength(), 4);
            assertEquals(wavFile.getTotalDataLength(), testFile.length() - 8);
            assertEquals(wavFile.getTotalMetadataLength(), 0);
            assertEquals(wavFile.getMetadata(), "");

            Project p1 = new Project("aaa", "aaa","aaa","aaa","aaa","aaa","aaa","aaa","aaa");
            wavFile.setMetadata(p1);
            wavFile.writeMetadata();
            String metadataString =  "{\"project\":\"aaa\",\"language\":\"aaa\",\"source\":\"aaa\",\"slug\":\"aaa\",\"book_number\":\"aaa\",\"mode\":\"aaa\",\"startv\":\"\",\"endv\":\"\"}";
            assertEquals(wavFile.getMetadata(), metadataString);
            assertEquals(wavFile.getTotalAudioLength(), 4);
            assertEquals(wavFile.getTotalDataLength(), testFile.length() - 8);
            assertEquals(wavFile.getTotalMetadataLength(), metadataString.length() + 20 + getWordAlignmentPadding(metadataString.length()));
            assertEquals(wavFile.getTotalMetadataLength() % 4, 0);

            Project p2 = new Project("", "","","","","","","","");
            wavFile.setMetadata(p2);
            wavFile.writeMetadata();
            metadataString = "{\"project\":\"\",\"language\":\"\",\"source\":\"\",\"slug\":\"\",\"book_number\":\"\",\"mode\":\"\",\"startv\":\"\",\"endv\":\"\"}";
            assertEquals(wavFile.getMetadata(), metadataString);
            //assertEquals(wavFile.getTotalMetadataLength(), (new String("{\"project\":\"\",\"language\":\"en\",\"source\":\"\",\"slug\":\"\",\"book_number\":\"\",\"mode\":\"\",\"startv\":\"\",\"endv\":\"\"}")).length());
            assertEquals(wavFile.getTotalAudioLength(), 4);
            assertEquals(wavFile.getTotalDataLength(), testFile.length() - 8);
            assertEquals(wavFile.getTotalMetadataLength(), metadataString.length() + 20 + getWordAlignmentPadding(metadataString.length()));
            assertEquals(wavFile.getTotalMetadataLength() % 4, 0);

        } catch (IOException e){
            e.printStackTrace();
        } catch (JSONException e){
            e.printStackTrace();
        }
    }

    public int getWordAlignmentPadding(int length){
        int padding = length % 4;
        if(padding != 0){
            padding = 4 - padding;
        }
        return padding;
    }
}


