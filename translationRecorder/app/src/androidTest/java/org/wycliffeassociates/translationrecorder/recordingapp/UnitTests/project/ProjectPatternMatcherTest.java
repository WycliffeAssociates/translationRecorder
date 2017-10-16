package org.wycliffeassociates.translationrecorder.recordingapp.UnitTests.project;

import org.junit.Test;
import org.wycliffeassociates.translationrecorder.project.ProjectPatternMatcher;
import org.wycliffeassociates.translationrecorder.project.ProjectSlugs;
import org.wycliffeassociates.translationrecorder.project.TakeInfo;

import static org.junit.Assert.assertEquals;

/**
 * Created by sarabiaj on 4/25/2017.
 */

public class ProjectPatternMatcherTest {



    @Test
    public void test(){
        String regexBible = "([a-zA-Z]{2,3}[-[\\d\\w]+]*)_([a-zA-Z]{3})_b([\\d]{2})_([1-3]*[a-zA-Z]+)_c([\\d]{2,3})_v([\\d]{2,3})(-([\\d]{2,3}))?(_t([\\d]{2}))?(.wav)?";
        //lang, version booknum book chapter start end take
        String groupsBible = "1 2 3 4 5 6 8 10";

        ProjectPatternMatcher ppmOT = new ProjectPatternMatcher(regexBible, groupsBible);
        ProjectPatternMatcher ppmNT = new ProjectPatternMatcher(regexBible, groupsBible);

        String[] validOTChunks =
                {
                    "en_ulb_b01_gen_c01_v01-02_t02.wav"
                };
        ProjectSlugs[] validOTChunkSlugs =
                {
                    new ProjectSlugs("en", "ulb", 1, "gen")
                };
        TakeInfo[] validOTChunkInfo =
                {
                    new TakeInfo(validOTChunkSlugs[0], 1, 1, 2, 2)
                };

        String[] validOTVerses =
                {
                    "en_ulb_b01_gen_c03_v16_t02.wav"
                };
        ProjectSlugs[] validOTVerseSlugs =
                {
                        new ProjectSlugs("en", "ulb", 1, "gen")
                };
        TakeInfo[] validOTVerseInfo =
                {
                    new TakeInfo(validOTVerseSlugs[0], 3, 16, -1, 2)
                };

        String[] invalidOTChunks =
                {
                    "en_ulb_gen_c01_v01-02_t02.wav",
                    "en_ulb_b01_gen_c01_v01-02-t02.wav"
                };
        String[] invalidOTVerses =
                {
                    "en_ulb_b01_gen_c01_v0102_t02.wav",
                    "en_ulb_b01_gen_c01_v01-02-t02.wav",
                    "en_ulb_gen_c01_v01-02_t02.wav"
                };

        matchStrings(ppmOT, validOTChunks, true, validOTChunkSlugs, validOTChunkInfo);
        matchStrings(ppmOT, validOTVerses, true, validOTVerseSlugs, validOTVerseInfo);
        matchStrings(ppmOT, invalidOTChunks, false, null, null);
        matchStrings(ppmOT, invalidOTVerses, false, null, null);

        matchStrings(ppmNT, validOTChunks, true, validOTChunkSlugs, validOTChunkInfo);
        matchStrings(ppmNT, validOTVerses, true, validOTVerseSlugs, validOTVerseInfo);
        matchStrings(ppmNT, invalidOTChunks, false, null, null);
        matchStrings(ppmNT, invalidOTVerses, false, null, null);

    }

    public void matchStrings(ProjectPatternMatcher ppm, String[] strings, boolean shouldMatch, ProjectSlugs[] slugs, TakeInfo[] takeInfos){
        for(int i = 0; i < strings.length; i++) {
            ppm.match(strings[i]);
            assertEquals(strings[i], ppm.matched(), shouldMatch);
            if(ppm.matched()) {
                compareSlugs(slugs[i], ppm.getProjectSlugs());
                compareTakeInfo(takeInfos[i], ppm.getTakeInfo());
            }
        }
    }

    public void compareTakeInfo(TakeInfo expected, TakeInfo actual) {
        //assertEquals(expected.getNameWithoutTake(), actual.getNameWithoutTake());
        assertEquals(expected.getChapter(), actual.getChapter());
        assertEquals(expected.getStartVerse(), actual.getStartVerse());
        assertEquals(expected.getEndVerse(), actual.getEndVerse());
        assertEquals(expected.getTake(), actual.getTake());
    }

    public void compareSlugs(ProjectSlugs expected, ProjectSlugs actual){
        assertEquals(expected.getBook(), actual.getBook());
        assertEquals(expected.getLanguage(), actual.getLanguage());
        assertEquals(expected.getVersion(), actual.getVersion());
        assertEquals(expected.getBookNumber(), actual.getBookNumber());
    }
}
