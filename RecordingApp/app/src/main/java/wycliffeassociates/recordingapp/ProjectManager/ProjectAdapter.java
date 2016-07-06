package wycliffeassociates.recordingapp.ProjectManager;

import android.app.Activity;

import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import wycliffeassociates.recordingapp.ConstantsDatabaseHelper;
import wycliffeassociates.recordingapp.ProjectManager.ActivityChapterList;
import wycliffeassociates.recordingapp.ProjectManager.Project;
import wycliffeassociates.recordingapp.ProjectManager.ProjectInfoDialog;
import wycliffeassociates.recordingapp.R;
import wycliffeassociates.recordingapp.Recording.RecordingScreen;
import wycliffeassociates.recordingapp.SettingsPage.Settings;

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
    }

    LayoutInflater mLayoutInflater;
    List<Project> mProjectList;
    Activity mCtx;
    ConstantsDatabaseHelper mDb;

    public ProjectAdapter(Activity context, List<Project> projectList) {
        super(context, R.layout.project_list_item, projectList);
        mCtx = context;
        mProjectList = projectList;
        mLayoutInflater = context.getLayoutInflater();
        mDb = new ConstantsDatabaseHelper(context);
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

            // Link the cached views to the convertView
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        initializeProjectCard(mCtx, mProjectList.get(position), mDb, holder.mLanguage, holder.mBook, holder.mInfo, holder.mRecord, holder.mTextLayout);

        return convertView;
    }

    public static void initializeProjectCard(final Activity ctx, final Project project, ConstantsDatabaseHelper dB, TextView languageView, TextView bookView,
                                             ImageButton infoView, ImageButton recordView, LinearLayout textLayout) {

        if(project.isOBS()){
            bookView.setText("Open Bible Stories");
        } else {
            String book = dB.getBookName(project.getSlug());
            bookView.setText(book);
        }

        String language = dB.getLanguageName(project.getTargetLanguage());
        languageView.setText(language);

        recordView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Project.loadProjectIntoPreferences(ctx, project);
                Settings.updateFilename(ctx);
                v.getContext().startActivity(new Intent(v.getContext(), RecordingScreen.class));
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

    public static void initializeProjectCard(final Activity ctx, final Project project, ConstantsDatabaseHelper db, View projectCard) {
        TextView languageView = (TextView) projectCard.findViewById(R.id.language_text_view);
        TextView bookView = (TextView) projectCard.findViewById(R.id.book_text_view);
        ImageButton info = (ImageButton) projectCard.findViewById(R.id.info_button);
        ImageButton record = (ImageButton) projectCard.findViewById(R.id.record_button);
        LinearLayout textLayout = (LinearLayout) projectCard.findViewById(R.id.text_layout);
        initializeProjectCard(ctx, project, db, languageView, bookView, info, record, textLayout);
    }
}
