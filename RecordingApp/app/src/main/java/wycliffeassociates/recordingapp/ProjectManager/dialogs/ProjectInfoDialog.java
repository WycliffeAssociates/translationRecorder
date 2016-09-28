package wycliffeassociates.recordingapp.ProjectManager.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import wycliffeassociates.recordingapp.FilesPage.Export.AppExport;
import wycliffeassociates.recordingapp.FilesPage.Export.Export;
import wycliffeassociates.recordingapp.FilesPage.Export.FolderExport;
import wycliffeassociates.recordingapp.FilesPage.Export.S3Export;
import wycliffeassociates.recordingapp.ProjectManager.Project;
import wycliffeassociates.recordingapp.database.ProjectDatabaseHelper;
import wycliffeassociates.recordingapp.R;

/**
 * Created by sarabiaj on 6/27/2016.
 */
public class ProjectInfoDialog extends DialogFragment {

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

        TextView title = (TextView) view.findViewById(R.id.title);
        TextView projectTitle = (TextView) view.findViewById(R.id.project_title);
        TextView languageTitle = (TextView) view.findViewById(R.id.language_title);
        TextView translator = (TextView) view.findViewById(R.id.translators);
        TextView translationType = (TextView) view.findViewById(R.id.translation_type_title);
        TextView unitType = (TextView) view.findViewById(R.id.unit_title);

        String languageCode = mProject.getTargetLanguage();
        String language = db.getLanguageName(languageCode);
        String bookCode = mProject.getSlug();
        String book = db.getBookName(bookCode);
        String translation = mProject.getSource();
        if (mProject.isOBS()) {
            bookCode = "obs";
            book = "Open Bible Stories";
            translationType.setVisibility(View.GONE);
            unitType.setVisibility(View.GONE);
        }
        String translators = mProject.getContributors();

        title.setText(book + " - " + language);
        projectTitle.setText(book + " (" + bookCode + ")");
        languageTitle.setText(language + " (" + languageCode + ")");
        translator.setText(translators);
        if (translation.compareTo("ulb") == 0) {
            translationType.setText("Unlocked Literal Bible (" + translation + ")");
        } else if (translation.compareTo("udb") == 0) {
            translationType.setText("Unlocked Dynamic Bible (" + translation + ")");
        } else {
            translationType.setText("Regular (" + translation.toUpperCase() + ")");
        }

        unitType.setText(mProject.getMode());

        ImageButton deleteButton = (ImageButton) view.findViewById(R.id.delete_button);
        ImageButton sourceButton = (ImageButton) view.findViewById(R.id.export_as_source_btn);
        ImageButton sdcard_button = (ImageButton) view.findViewById(R.id.sdcard_button);
        ImageButton folderButton = (ImageButton) view.findViewById(R.id.folder_button);
        ImageButton publishButton = (ImageButton) view.findViewById(R.id.publish_button);
        ImageButton otherButton = (ImageButton) view.findViewById(R.id.other_button);

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


        builder.setView(view);
        return builder.create();
    }

}
