package wycliffeassociates.recordingapp.ProjectManager;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import wycliffeassociates.recordingapp.R;

/**
 * Created by sarabiaj on 6/27/2016.
 */
public class ProjectInfoDialog extends DialogFragment {

    public interface InfoDialogCallback {
        void onDelete();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.project_layout_dialog, null);

        TextView title = (TextView) view.findViewById(R.id.title);
        TextView projectTitle = (TextView) view.findViewById(R.id.project_title);
        TextView languageTitle = (TextView) view.findViewById(R.id.language_title);

        title.setText("Project - Language");
        projectTitle.setText("project");
        languageTitle.setText("language");

        ImageButton deleteButton = (ImageButton) view.findViewById(R.id.delete_button);
        ImageButton sdcard_button = (ImageButton) view.findViewById(R.id.sdcard_button);
        ImageButton folderButton = (ImageButton) view.findViewById(R.id.folder_button);
        ImageButton publishButton = (ImageButton) view.findViewById(R.id.publish_button);
        ImageButton otherButton = (ImageButton) view.findViewById(R.id.other_button);

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                ((InfoDialogCallback)getActivity()).onDelete();
            }
        });

        builder.setView(view);
        return builder.create();
    }

}
