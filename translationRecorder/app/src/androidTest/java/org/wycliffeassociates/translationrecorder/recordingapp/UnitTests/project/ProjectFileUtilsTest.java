package org.wycliffeassociates.translationrecorder.recordingapp.UnitTests.project;

import org.junit.Test;
import org.wycliffeassociates.translationrecorder.project.Project;
import org.wycliffeassociates.translationrecorder.project.ProjectFileUtils;
import org.wycliffeassociates.translationrecorder.project.ProjectPatternMatcher;
import org.wycliffeassociates.translationrecorder.project.ProjectSlugs;
import org.wycliffeassociates.translationrecorder.project.TakeInfo;
import org.wycliffeassociates.translationrecorder.project.components.Anthology;
import org.wycliffeassociates.translationrecorder.project.components.Book;
import org.wycliffeassociates.translationrecorder.project.components.Language;
import org.wycliffeassociates.translationrecorder.project.components.Mode;
import org.wycliffeassociates.translationrecorder.project.components.Version;

import java.io.File;

import static org.junit.Assert.assertEquals;

/**
 * Created by sarabiaj on 4/27/2017.
 */

public class ProjectFileUtilsTest {

    String regex = "([a-zA-Z]{2,3}[-[\\d\\w]+]*)_([a-zA-Z]{3})_b([\\d]{2})_([1-3]*[a-zA-Z]+)_c([\\d]{2,3})"
            + "_v([\\d]{2,3})(-([\\d]{2,3}))?(_t([\\d]{2}))?(.wav)?";
    //lang, version booknum book chapter start end take
    String groups = "1 2 3 4 5 6 8 10";
    Language language = new Language("en", "English");
    Book book = new Book("gen", "Genesis", "ot", 1);
    Version version = new Version("ulb", "Unlocked Literal Bible");
    String mask = "1111111111";
    int sort = 1;

    String jarName = "biblechunk.jar";
    String className = "org.wycliffeassociates.translationrecorder.biblechunk.BibleChunkPlugin";
    Anthology anthology = new Anthology(
            "ot",
            "Old Testament",
            "bible",
            sort,
            regex,
            groups,
            mask,
            jarName,
            className
    );
    Mode mode = new Mode("chunk", "chunk", "chunk");
    Project project = new Project(language, anthology, book, version, mode);

    @Test
    public void testCreateFile(){
        File file = ProjectFileUtils.createFile(project, 1, 1, 2);

        ProjectPatternMatcher ppm = project.getPatternMatcher();
        ppm.match(file);
        assertEquals(true, ppm.matched());
        ProjectSlugs slugs = ppm.getProjectSlugs();
        assertEquals(language.getSlug(), slugs.getLanguage());
        assertEquals(book.getSlug(), slugs.getBook());
        assertEquals(version.getSlug(), slugs.getVersion());
        assertEquals(book.getOrder(), slugs.getBookNumber());

        TakeInfo info = ppm.getTakeInfo();
        assertEquals(1, info.getChapter());
        assertEquals(1, info.getStartVerse());
        assertEquals(2, info.getEndVerse());
    }

    @Test
    public void testGetNameFromProject(){
        String name = project.getFileName(1, 1, 2);
        String expected = "en_ulb_b01_gen_c01_v01-02";
        assertEquals(expected, name);

        String name2 = project.getFileName(1, 1, -1);
        String expected2 = "en_ulb_b01_gen_c01_v01";
        assertEquals(expected, name);
    }

    @Test
    public void testGetParentDirectory(){
        String name = project.getFileName(1, 1, 2);
        String expected = "en_ulb_b01_gen_c01_v01-02_t01.wav";
        TakeInfo ti = new TakeInfo(project.getProjectSlugs(), 1, 1, 2, 1);
//        ProjectFileUtils.getParentDirectory();
        assertEquals(expected, name);

        String name2 = project.getFileName(1, 1, -1);
        String expected2 = "en_ulb_b01_gen_c01_v01";
        assertEquals(expected, name);
    }

}
