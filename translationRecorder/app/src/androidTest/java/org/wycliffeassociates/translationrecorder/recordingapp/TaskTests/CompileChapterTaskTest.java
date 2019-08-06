package org.wycliffeassociates.translationrecorder.recordingapp.TaskTests;

import androidx.test.rule.ActivityTestRule;
import android.test.suitebuilder.annotation.SmallTest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.wycliffeassociates.translationrecorder.MainMenu;
import org.wycliffeassociates.translationrecorder.ProjectManager.tasks.CompileChapterTask;
import org.wycliffeassociates.translationrecorder.database.ProjectDatabaseHelper;
import org.wycliffeassociates.translationrecorder.project.Project;
import org.wycliffeassociates.translationrecorder.recordingapp.ProjectMockingUtil;
import org.wycliffeassociates.translationrecorder.widgets.ChapterCard;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Created by sarabiaj on 9/28/2017.
 */

@RunWith(Parameterized.class)
@SmallTest
//This test is to ensure that the files are properly sorted when submitted to the wav compile method
//It assumes that the compile method is tested in a WavFile unit test.
public class CompileChapterTaskTest {

    static ActivityTestRule<MainMenu> mainMenuActivityTestRule = new ActivityTestRule<MainMenu>(MainMenu.class);

    @Parameterized.Parameters
    public static Iterable<Object[]> data() {

        //Project notesProject = ProjectMockingUtil.createNotesTestProject(mainMenuActivityTestRule);
        Project bibleProject = ProjectMockingUtil.createBibleTestProject(mainMenuActivityTestRule);

        return Arrays.asList(new Object[][] {
                //Bible Projects
                {
                       Arrays.asList(
                               "en_ulb_b01_gen_c01_v01_t01.wav",
                               "en_ulb_b01_gen_c01_v03_t01.wav",
                               "en_ulb_b01_gen_c01_v02_t01.wav"),
                        Arrays.asList(
                                "en_ulb_b01_gen_c01_v01_t01.wav",
                                "en_ulb_b01_gen_c01_v02_t01.wav",
                                "en_ulb_b01_gen_c01_v03_t01.wav"
                        ),
                        bibleProject
                },
                {
                        Arrays.asList(
                                "en_ulb_b01_gen_c01_v01_t01.wav",
                                "en_ulb_b01_gen_c01_v02_t01.wav",
                                "en_ulb_b01_gen_c01_v03_t01.wav"),
                        Arrays.asList(
                                "en_ulb_b01_gen_c01_v01_t01.wav",
                                "en_ulb_b01_gen_c01_v02_t01.wav",
                                "en_ulb_b01_gen_c01_v03_t01.wav"
                        ),
                        bibleProject
                },
                {
                        Arrays.asList(
                                "en_ulb_b01_gen_c01_v03_t01.wav",
                                "en_ulb_b01_gen_c01_v02_t01.wav",
                                "en_ulb_b01_gen_c01_v01_t01.wav"),
                        Arrays.asList(
                                "en_ulb_b01_gen_c01_v01_t01.wav",
                                "en_ulb_b01_gen_c01_v02_t01.wav",
                                "en_ulb_b01_gen_c01_v03_t01.wav"
                        ),
                        bibleProject
                },
        });
    }

    private final List<String> mUnsortedList;
    private final List<String> mSortedList;
    private final Project mProject;

    public CompileChapterTaskTest(List<String> unsortedList, List<String> sortedList, Project project) {
        mUnsortedList = unsortedList;
        mSortedList = sortedList;
        mProject = project;
    }

    @Before
    public void setUp() {

    }

    @Test
    public void sortFilesTest(){
        assertEquals("Sorted and unsorted lists should be the same size", mUnsortedList.size(), mSortedList.size());
        ProjectDatabaseHelper db = new ProjectDatabaseHelper(mainMenuActivityTestRule.getActivity());
        ChapterCard chapterCard = new ChapterCard(mProject, "", 1, mSortedList.size(), db);
        Map<ChapterCard, List<String>> map = new HashMap<>();
        map.put(chapterCard, mUnsortedList);
        CompileChapterTask cct = new CompileChapterTask(0, map, mProject);
        cct.sortFilesInChapter(mUnsortedList);
        for(int i = 0; i < mSortedList.size(); i++) {
            assertEquals("Unsorted list should be sorted and match the sorted list", mUnsortedList.get(i), mSortedList.get(i));
        }
    }


}
