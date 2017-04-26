/*
package org.wycliffeassociates.translationrecorder.recordingapp;

import android.os.Bundle;
import android.os.Parcel;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;

import org.wycliffeassociates.translationrecorder.MainMenu;
import org.wycliffeassociates.translationrecorder.project.Project;
import org.wycliffeassociates.translationrecorder.wav.WavFile;
import org.wycliffeassociates.translationrecorder.wav.WavMetadata;
import org.wycliffeassociates.translationrecorder.wav.WavOutputStream;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class MetadataTest {

    WavFile wavFile;
    File testFile;
    Project project;
    String chapter = "01";
    String startVerse = "01";
    String endVerse = "02";

    File testFile2;
    Project project2;
    String chapter2 = "014";
    String startVerse2 = "01";
    String endVerse2 = "01";

    @Rule
    public ActivityTestRule<MainMenu> mActivityRule = new ActivityTestRule<>(
            MainMenu.class);

    @Before
    public void setUp() {
        try {
            testFile = File.createTempFile("test", "wav");
            testFile2 = File.createTempFile("test2", "wav");

            deleteFileIfExists(testFile);
            deleteFileIfExists(testFile2);

            project = new Project("en", "", "01", "gen", "ulb", "chunk", "ot", "", "");
            project2 = new Project("cmn", "", "543", "eph", "reg", "verse", "nt", "", "");
        } catch (IOException e) {
            Assert.fail("Test failed : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void deleteFileIfExists(File file){
        if(file.exists() && file.length() != 0){
            file.delete();
        }
    }

    @Test
    public void testMetadata() {
        try {
            WavMetadata meta = new WavMetadata(project, chapter, startVerse, endVerse);
            JSONObject preWav = meta.toJSON();
            WavFile wav = new WavFile(testFile, meta);
            assertEquals(wav.getFile().length(), 44);
            try (WavOutputStream wos = new WavOutputStream(wav)) {
                for (int i = 0; i < 1000; i++) {
                    wos.write(i);
                }
            }
            JSONObject postWav = wav.getMetadata().toJSON();
            //Test: may need to modify the comparejson method
            compareJson(preWav, postWav);
            wav.addMarker("1", 0);
            wav.addMarker("2", 500);

            //Test
            assertEquals(1000, wav.getTotalAudioLength());

            //Try passing in via a bundle
            Bundle bundle = new Bundle();
            Parcel parcel = Parcel.obtain();
            parcel.writeParcelable(wav, 0);
            parcel.setDataPosition(0);
            wav = null;
            meta = null;
            meta = new WavMetadata(project2, chapter2, startVerse2, endVerse2);
            wav = new WavFile(testFile2, meta);
            wav.addMarker("1", 0);

            //Test
            testParcel(parcel, project, chapter, startVerse, endVerse);
        } catch (IOException e) {
        }
    }

    public void compareJson(JSONObject one, JSONObject two){
        try {
            assertEquals(one.getString("anthology"), two.getString("anthology"));
            assertEquals(one.getString("language"), two.getString("language"));
            assertEquals(one.getString("version"), two.getString("version"));
            assertEquals(one.getString("slug"), two.getString("slug"));
            assertEquals(one.getString("book_number"), two.getString("book_number"));
            assertEquals(one.getString("mode"), two.getString("mode"));
            assertEquals(one.getString("chapter"), two.getString("chapter"));
            assertEquals(one.getString("startv"), two.getString("startv"));
            assertEquals(one.getString("endv"), two.getString("endv"));
            assertEquals(one.has("markers"), two.has("markers"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void testParcel(Parcel parcel, Project project, String chapter, String startVerse, String endVerse){
        WavFile parceled = WavFile.CREATOR.createFromParcel(parcel);
        WavMetadata metadata = parceled.getMetadata();
        assertEquals(project.getAnthologySlug(), metadata.getAnthology());
        assertEquals(project.getMode(), metadata.getMode());
        assertEquals(project.getBookSlug(), metadata.getSlug());
        assertEquals(project.getTargetLanguageSlug(), metadata.getLanguage());
        assertEquals(project.getBookNumber(), metadata.getBookNumber());
        assertEquals(project.getVersionSlug(), metadata.getVersion());
        assertEquals(chapter, metadata.getChapter());
        assertEquals(startVerse, metadata.getStartVerse());
        assertEquals(endVerse, metadata.getEndVerse());
    }

    public int getWordAlignmentPadding(int length){
        int padding = length % 4;
        if(padding != 0){
            padding = 4 - padding;
        }
        return padding;
    }
}


*/
