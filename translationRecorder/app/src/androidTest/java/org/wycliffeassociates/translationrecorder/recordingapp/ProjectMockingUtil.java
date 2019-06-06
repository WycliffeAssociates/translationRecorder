package org.wycliffeassociates.translationrecorder.recordingapp;

import android.content.Context;
import android.content.Intent;
import androidx.test.rule.ActivityTestRule;

import org.wycliffeassociates.translationrecorder.database.ProjectDatabaseHelper;
import org.wycliffeassociates.translationrecorder.project.Project;
import org.wycliffeassociates.translationrecorder.project.components.Anthology;

/**
 * Created by sarabiaj on 9/20/2017.
 */

public class ProjectMockingUtil {
    public static Project createBibleTestProject(ActivityTestRule<?> activityTestRule) {
        Project project = new Project();
        activityTestRule.launchActivity(new Intent());
        Context ctx = activityTestRule.getActivity();
        ProjectDatabaseHelper db = new ProjectDatabaseHelper(ctx);
        Anthology anthology = db.getAnthology(db.getAnthologyId("ot"));
        project.setAnthology(anthology);
        project.setBook(db.getBook(db.getBookId("gen")));
        project.setMode(db.getMode(db.getModeId("verse", anthology.getSlug())));
        project.setVersion(db.getVersion(db.getVersionId("ulb")));
        project.setTargetLanguage(db.getLanguage(db.getLanguageId("en")));
        db.close();
        activityTestRule.getActivity().finish();
        return project;
    }

    public static Project createNotesTestProject(ActivityTestRule<?> activityTestRule) {
        Project project = new Project();
        activityTestRule.launchActivity(new Intent());
        Context ctx = activityTestRule.getActivity();
        ProjectDatabaseHelper db = new ProjectDatabaseHelper(ctx);
        Anthology anthology = db.getAnthology(db.getAnthologyId("tn"));
        project.setAnthology(anthology);
        project.setBook(db.getBook(db.getBookId("gen-ch-1")));
        project.setMode(db.getMode(db.getModeId("note", anthology.getSlug())));
        project.setVersion(db.getVersion(db.getVersionId("ulb")));
        project.setTargetLanguage(db.getLanguage(db.getLanguageId("en")));
        db.close();
        activityTestRule.getActivity().finish();
        return project;
    }

}
