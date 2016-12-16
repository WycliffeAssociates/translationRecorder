package wycliffeassociates.recordingapp.ProjectManager.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import wycliffeassociates.recordingapp.FilesPage.Export.AppExport;
import wycliffeassociates.recordingapp.FilesPage.Export.Export;
import wycliffeassociates.recordingapp.FilesPage.Export.FolderExport;
import wycliffeassociates.recordingapp.FilesPage.Export.S3Export;
import wycliffeassociates.recordingapp.ProjectManager.Project;
import wycliffeassociates.recordingapp.R;
import wycliffeassociates.recordingapp.SettingsPage.Settings;
import wycliffeassociates.recordingapp.database.ProjectDatabaseHelper;
import wycliffeassociates.recordingapp.project.SourceAudioActivity;

import static android.app.Activity.RESULT_OK;

/**
 * Created by sarabiaj on 6/27/2016.
 */
public class ProjectInfoDialog extends DialogFragment {

    private static final int SOURCE_AUDIO_REQUEST = 223;

    public interface InfoDialogCallback {
        void onDelete(final Project project);
    }

    public interface ExportDelegator {
        void delegateExport(Export exp);
    }

    public interface SourceAudioDelegator {
        void delegateSourceAudio(Project project);
    }

    Project mProject;
    ExportDelegator mExportDelegator;
    Export mExp;
    public static final String PROJECT_FRAGMENT_TAG = "project_tag";

    TextView mTitle;
    TextView mProjectTitle;
    TextView mLanguageTitle;
    TextView mTranslator;
    TextView mTranslationType;
    TextView mUnitType;
    TextView mSourceLanguage;
    TextView mSourceLocation;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mExportDelegator = (ExportDelegator) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mExportDelegator = null;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.project_layout_dialog, null);

        ProjectDatabaseHelper db = new ProjectDatabaseHelper(getActivity());

        mProject = getArguments().getParcelable(Project.PROJECT_EXTRA);

        mTitle = (TextView) view.findViewById(R.id.title);
        mProjectTitle = (TextView) view.findViewById(R.id.project_title);
        mLanguageTitle = (TextView) view.findViewById(R.id.language_title);
        mTranslator = (TextView) view.findViewById(R.id.translators);
        mTranslationType = (TextView) view.findViewById(R.id.translation_type_title);
        mUnitType = (TextView) view.findViewById(R.id.unit_title);
        mSourceLanguage = (TextView) view.findViewById(R.id.source_audio_language);
        mSourceLocation = (TextView) view.findViewById(R.id.source_audio_location);

        String languageCode = mProject.getTargetLanguage();
        String language = db.getLanguageName(languageCode);
        String bookCode = mProject.getSlug();
        String book = db.getBookName(bookCode);
        String translation = mProject.getVersion();
        if(mProject.isOBS()){
            bookCode = "obs";
            book = "Open Bible Stories";
            mTranslationType.setVisibility(View.GONE);
            mUnitType.setVisibility(View.GONE);
        }
        String translators = mProject.getContributors();

        mTitle.setText(book + " - " + language);
        mProjectTitle.setText(book + " (" + bookCode + ")");
        mLanguageTitle.setText(language + " (" + languageCode + ")");
        mTranslator.setText(translators);
        if (translation.equals("ulb")) {
            mTranslationType.setText("Unlocked Literal Bible (" + translation + ")");
        } else if (translation.equals("udb")) {
            mTranslationType.setText("Unlocked Dynamic Bible (" + translation + ")");
        } else {
            mTranslationType.setText("Regular (" + translation.toUpperCase() + ")");
        }

        setSourceAudioTextInfo();

        mUnitType.setText(mProject.getMode());

        ImageButton deleteButton = (ImageButton) view.findViewById(R.id.delete_button);
        ImageButton sourceButton = (ImageButton) view.findViewById(R.id.export_as_source_btn);
        ImageButton sdcard_button = (ImageButton) view.findViewById(R.id.sdcard_button);
        ImageButton folderButton = (ImageButton) view.findViewById(R.id.folder_button);
        ImageButton publishButton = (ImageButton) view.findViewById(R.id.publish_button);
        ImageButton otherButton = (ImageButton) view.findViewById(R.id.other_button);
        ImageButton editSourceLanguage = (ImageButton) view.findViewById(R.id.edit_source_language);
        ImageButton editSourceLocation = (ImageButton) view.findViewById(R.id.edit_source_location);

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                ((InfoDialogCallback) getActivity()).onDelete(mProject);
            }
        });

        View.OnClickListener localExport = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mExp = new FolderExport(Project.getProjectDirectory(mProject), mProject);
                mExportDelegator.delegateExport(mExp);
            }
        };
        sdcard_button.setOnClickListener(localExport);
        folderButton.setOnClickListener(localExport);

        publishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mExp = new S3Export(Project.getProjectDirectory(mProject), mProject);
                mExportDelegator.delegateExport(mExp);
            }
        });

        otherButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mExp = new AppExport(Project.getProjectDirectory(mProject), mProject);
                mExportDelegator.delegateExport(mExp);
            }
        });

        sourceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((SourceAudioDelegator) mExportDelegator).delegateSourceAudio(mProject);
            }
        });

        editSourceLanguage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = SourceAudioActivity.getSourceAudioIntent(getActivity(), mProject);
                startActivityForResult(intent, SOURCE_AUDIO_REQUEST);
            }
        });

        editSourceLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = SourceAudioActivity.getSourceAudioIntent(getActivity(), mProject);
                startActivityForResult(intent, SOURCE_AUDIO_REQUEST);
            }
        });

        builder.setView(view);
        return builder.create();
    }

    private void setSourceAudioTextInfo() {
        ProjectDatabaseHelper db = new ProjectDatabaseHelper(getActivity());
        String sourceLanguageCode = mProject.getSourceLanguage();
        String sourceLanguageName = (db.languageExists(sourceLanguageCode))? db.getLanguageName(sourceLanguageCode) : "";
        mSourceLanguage.setText(String.format("%s - (%s)", sourceLanguageName, sourceLanguageCode));
        mSourceLocation.setText(mProject.getSourceAudioPath());
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            if(requestCode == SOURCE_AUDIO_REQUEST) {
                ProjectDatabaseHelper db = new ProjectDatabaseHelper(getActivity());
                int projectId = db.getProjectId(mProject);
                Project updatedProject = data.getParcelableExtra(Project.PROJECT_EXTRA);
                if(updatedProject.getSourceLanguage() != null && !updatedProject.getSourceLanguage().equals("")) {
                    mProject = updatedProject;
                    db.updateSourceAudio(projectId, mProject);
                    db.close();
                    setSourceAudioTextInfo();
                    SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    //If the project was the recent project that is stored in the settings, reflect the changes there
                    if (pref.getString(Settings.KEY_PREF_LANG, "").equals(mProject.getTargetLanguage())
                            && pref.getString(Settings.KEY_PREF_BOOK, "").equals(mProject.getSlug())
                            && pref.getString(Settings.KEY_PREF_VERSION, "").equals(mProject.getVersion())
                            && pref.getString(Settings.KEY_PREF_CHUNK_VERSE, "").equals(mProject.getMode())) {
                        pref.edit().putString(Settings.KEY_PREF_LANG_SRC, mProject.getSourceLanguage()).commit();
                        pref.edit().putString(Settings.KEY_PREF_SRC_LOC, mProject.getSourceAudioPath()).commit();
                    }
                }
            }
        }
    }
}
