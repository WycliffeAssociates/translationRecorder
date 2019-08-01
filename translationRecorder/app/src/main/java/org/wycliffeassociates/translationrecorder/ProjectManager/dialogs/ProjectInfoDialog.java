package org.wycliffeassociates.translationrecorder.ProjectManager.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import org.wycliffeassociates.translationrecorder.FilesPage.Export.AppExport;
import org.wycliffeassociates.translationrecorder.FilesPage.Export.Export;
import org.wycliffeassociates.translationrecorder.FilesPage.Export.FolderExport;
import org.wycliffeassociates.translationrecorder.FilesPage.Export.TranslationExchangeExport;
import org.wycliffeassociates.translationrecorder.R;
import org.wycliffeassociates.translationrecorder.TranslationRecorderApp;
import org.wycliffeassociates.translationrecorder.database.ProjectDatabaseHelper;
import org.wycliffeassociates.translationrecorder.project.Project;
import org.wycliffeassociates.translationrecorder.project.ProjectFileUtils;
import org.wycliffeassociates.translationrecorder.project.SourceAudioActivity;

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
    ProjectDatabaseHelper db;
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
        db = ((TranslationRecorderApp)activity.getApplication()).getDatabase();
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

        mProject = getArguments().getParcelable(Project.PROJECT_EXTRA);

        mTitle = (TextView) view.findViewById(R.id.title);
        mProjectTitle = (TextView) view.findViewById(R.id.project_title);
        mLanguageTitle = (TextView) view.findViewById(R.id.language_title);
        mTranslator = (TextView) view.findViewById(R.id.translators);
        mTranslationType = (TextView) view.findViewById(R.id.translation_type_title);
        mUnitType = (TextView) view.findViewById(R.id.unit_title);
        mSourceLanguage = (TextView) view.findViewById(R.id.source_audio_language);
        mSourceLocation = (TextView) view.findViewById(R.id.source_audio_location);

        String languageCode = mProject.getTargetLanguageSlug();
        String language = db.getLanguageName(languageCode);
        String bookCode = mProject.getBookSlug();
        String book = db.getBookName(bookCode);
        String translation = mProject.getVersionSlug();
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

        mUnitType.setText(mProject.getModeName());

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
                mExp = new FolderExport(ProjectFileUtils.getProjectDirectory(mProject), mProject);
                mExportDelegator.delegateExport(mExp);
            }
        };
        sdcard_button.setOnClickListener(localExport);
        folderButton.setOnClickListener(localExport);

        View.OnClickListener tEExport = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mExp = new TranslationExchangeExport(ProjectFileUtils.getProjectDirectory(mProject), mProject);
                mExportDelegator.delegateExport(mExp);
            }
        };
        publishButton.setOnClickListener(tEExport);


        otherButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mExp = new AppExport(ProjectFileUtils.getProjectDirectory(mProject), mProject);
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
        String sourceLanguageCode = mProject.getSourceLanguageSlug();
        String sourceLanguageName = (db.languageExists(sourceLanguageCode))? db.getLanguageName(sourceLanguageCode) : "";
        mSourceLanguage.setText(String.format("%s - (%s)", sourceLanguageName, sourceLanguageCode));
        mSourceLocation.setText(mProject.getSourceAudioPath());
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            if(requestCode == SOURCE_AUDIO_REQUEST) {
                int projectId = db.getProjectId(mProject);
                Project updatedProject = data.getParcelableExtra(Project.PROJECT_EXTRA);
                if(updatedProject.getSourceLanguageSlug() != null && !updatedProject.getSourceLanguageSlug().equals("")) {
                    mProject = updatedProject;
                    db.updateSourceAudio(projectId, mProject);
                    setSourceAudioTextInfo();
                }
            }
        }
    }
}
