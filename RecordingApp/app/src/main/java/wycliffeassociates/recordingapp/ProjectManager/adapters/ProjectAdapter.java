package wycliffeassociates.recordingapp.ProjectManager.adapters;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.filippudak.ProgressPieView.ProgressPieView;

import java.io.IOException;
import java.util.List;

import wycliffeassociates.recordingapp.ProjectManager.Project;
import wycliffeassociates.recordingapp.ProjectManager.activities.ActivityChapterList;
import wycliffeassociates.recordingapp.ProjectManager.dialogs.ProjectInfoDialog;
import wycliffeassociates.recordingapp.R;
import wycliffeassociates.recordingapp.Recording.RecordingScreen;
import wycliffeassociates.recordingapp.Reporting.Logger;
import wycliffeassociates.recordingapp.database.ProjectDatabaseHelper;
import wycliffeassociates.recordingapp.project.Chunks;

/**
 *
 * Creates a custom view for the audio entries in the file screen.
 *
 */
public class ProjectAdapter extends ArrayAdapter {
    //class for caching the views in a row
    private static class ViewHolder {
        TextView mLanguage, mBook;
        ImageButton mRecord, mInfo;
        LinearLayout mTextLayout;
        ProgressPieView mProgressPie;
    }

    LayoutInflater mLayoutInflater;
    List<Project> mProjectList;
    Activity mCtx;
    ProjectDatabaseHelper mDb;

    public ProjectAdapter(Activity context, List<Project> projectList) {
        super(context, R.layout.project_list_item, projectList);
        mCtx = context;
        mProjectList = projectList;
        mLayoutInflater = context.getLayoutInflater();
        mDb = new ProjectDatabaseHelper(context);
    }

    public View getView(final int position, View convertView, final ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.project_list_item, null);
            holder = new ViewHolder();
            holder.mBook = (TextView) convertView.findViewById(R.id.book_text_view);
            holder.mLanguage = (TextView) convertView.findViewById(R.id.language_text_view);
            holder.mInfo = (ImageButton) convertView.findViewById(R.id.info_button);
            holder.mRecord = (ImageButton) convertView.findViewById(R.id.record_button);
            holder.mTextLayout = (LinearLayout) convertView.findViewById(R.id.text_layout);
            holder.mProgressPie = (ProgressPieView) convertView.findViewById(R.id.progress_pie);

            // Link the cached views to the convertView
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        initializeProjectCard(mCtx, mProjectList.get(position), mDb, holder.mLanguage, holder.mBook, holder.mInfo, holder.mRecord, holder.mTextLayout, holder.mProgressPie);

        return convertView;
    }

    public static void initializeProjectCard(final Activity ctx, final Project project, ProjectDatabaseHelper dB, TextView languageView, TextView bookView,
                                             ImageButton infoView, ImageButton recordView, LinearLayout textLayout, ProgressPieView progressPie) {

        if(project.isOBS()){
            bookView.setText("Open Bible Stories");
        } else {
            String book = dB.getBookName(project.getSlug());
            bookView.setText(book);
        }

        String language = dB.getLanguageName(project.getTargetLanguage());
        languageView.setText(language);

        // Calculate project's progress
        if (dB.projectExists(project)) {
            try {
                // TODO: This is a bottle neck. Please optimize the progress calculation.
                Chunks chunks = new Chunks(ctx.getBaseContext(), project.getSlug());
                int chapterCount = chunks.getNumChapters();
                int projectId = dB.getProjectId(project);
                int progress = Math.round((float)dB.getProjectProgressSum(projectId) / chapterCount);
                progressPie.setProgress(progress);
            } catch (IOException e) {
                Logger.e("ProjectAdapter init project card", "IOException", e);
            }
        }

        recordView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Project.loadProjectIntoPreferences(ctx, project);
                //TODO: should find place left off at?
                v.getContext().startActivity(RecordingScreen.getNewRecordingIntent(v.getContext(), project, 1, 1));
            }
        });

        infoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment info = new ProjectInfoDialog();
                Bundle args = new Bundle();
                args.putParcelable(Project.PROJECT_EXTRA, project);
                info.setArguments(args);
                info.show(ctx.getFragmentManager(), "title");
            }
        });

        textLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), ActivityChapterList.class);
                intent.putExtra(Project.PROJECT_EXTRA, project);
                v.getContext().startActivity(intent);
            }
        });


    }

    public static void initializeProjectCard(final Activity ctx, final Project project, ProjectDatabaseHelper db, View projectCard) {
        TextView languageView = (TextView) projectCard.findViewById(R.id.language_text_view);
        TextView bookView = (TextView) projectCard.findViewById(R.id.book_text_view);
        ImageButton info = (ImageButton) projectCard.findViewById(R.id.info_button);
        ImageButton record = (ImageButton) projectCard.findViewById(R.id.record_button);
        LinearLayout textLayout = (LinearLayout) projectCard.findViewById(R.id.text_layout);
        ProgressPieView progressPie = (ProgressPieView) projectCard.findViewById(R.id.progress_pie);
        initializeProjectCard(ctx, project, db, languageView, bookView, info, record, textLayout, progressPie);
    }
}
