package wycliffeassociates.recordingapp;

import android.app.Application;
import android.test.ApplicationTestCase;

import wycliffeassociates.recordingapp.Playback.Editing.CutOp;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);
    }

    @Override
    protected void setUp() throws Exception {
        // Do context mocking here
        createApplication();
    }

    final public void testCutOp1() {
        Application application = getApplication();
        System.out.println("Files dir: " + application.getFilesDir());
        CutOp cut = new CutOp();
        assertEquals("Cut initialized, should be empty", cut.hasCut(), false);
        cut.cut(1, 3);
        assertEquals("Cut added, should have a cut", cut.hasCut(), true);
        assertEquals("1 should skip to 3", cut.skip(1), 3);
        assertEquals("2 should skip to 3", cut.skip(2), 3);
        assertEquals("3 should not skip", cut.skip(3), -1);





    }

}
