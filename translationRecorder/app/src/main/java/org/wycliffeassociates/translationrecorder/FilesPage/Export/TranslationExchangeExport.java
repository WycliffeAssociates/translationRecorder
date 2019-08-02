package org.wycliffeassociates.translationrecorder.FilesPage.Export;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.util.Log;
import com.door43.tools.reporting.Logger;
import net.gotev.uploadservice.*;
import org.wycliffeassociates.translationrecorder.FilesPage.FeedbackDialog;
import org.wycliffeassociates.translationrecorder.R;
import org.wycliffeassociates.translationrecorder.SettingsPage.Settings;
import org.wycliffeassociates.translationrecorder.TranslationRecorderApp;
import org.wycliffeassociates.translationrecorder.database.ProjectDatabaseHelper;
import org.wycliffeassociates.translationrecorder.project.Project;
import org.wycliffeassociates.translationrecorder.project.components.User;

import java.io.File;

/**
 * Created by sarabiaj on 11/16/2017.
 */

public class TranslationExchangeExport extends Export {

    TranslationExchangeDiff mDiffer;
    ProjectDatabaseHelper db;

    public TranslationExchangeExport(File projectToExport, Project project, ProjectDatabaseHelper db) {
        super(projectToExport, project);
        mDirectoryToZip = null;
        this.db = db;
    }

    @Override
    protected void initialize() {
        mDiffer = new TranslationExchangeDiff(
                (TranslationRecorderApp) mCtx.getActivity().getApplication(),
                mProject
        );
        mDiffer.computeDiff(outputFile(), this);
    }

    @Override
    public void onComplete(int id) {
        mFilesToZip = mDiffer.getDiff();
        super.onComplete(id);
        if(id == TranslationExchangeDiff.DIFF_ID) {
            super.initialize();
        }
    }

    @Override
    protected void handleUserInput() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Context ctx = mCtx.getActivity().getApplicationContext();
                uploadBinary(ctx, outputFile());
            }
        });
        thread.start();
    }

    public void uploadBinary(Context context, File file) {
        try {
            // starting from 3.1+, you can also use content:// URI string instead of absolute file
            String filePath = file.getAbsolutePath();
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
            int userId = pref.getInt(Settings.KEY_USER, 1);
            User user = db.getUser(userId);
            String hash = user.getHash();
            String uploadId =
                    new BinaryUploadRequest(context, "http://opentranslationtools.org/api/upload/zip")
                            .addHeader("tr-user-hash", hash)
                            .addHeader("tr-file-name", file.getName())
                            .setFileToUpload(filePath)
                            .setNotificationConfig(getNotificationConfig())
                            .setDelegate(getUploadStatusDelegate())
                            .setAutoDeleteFilesAfterSuccessfulUpload(true)
                            .startUpload();

        } catch (Exception exc) {
            Log.e("AndroidUploadService", exc.getMessage(), exc);
        }
    }

    protected UploadStatusDelegate getUploadStatusDelegate() {
        UploadStatusDelegate uploadStatusDelegate = new UploadStatusDelegate() {
            @Override
            public void onProgress(Context context, UploadInfo uploadInfo) {
            }

            @Override
            public void onError(
                    Context context,
                    UploadInfo uploadInfo,
                    ServerResponse serverResponse,
                    Exception exception
            ) {
                String message;
                if (serverResponse != null) {
                    message = String.format("code: %s: %s",
                            serverResponse.getHttpCode(),
                            serverResponse.getBodyAsString()
                    );
                    Logger.e(TranslationExchangeExport.class.toString(), message, exception);
                } else if (exception != null) {
                    message = exception.getMessage();
                    Logger.e(TranslationExchangeExport.class.toString(), "Error: " + message, exception);
                } else {
                    message = "An error occurred without a response or exception, upload percent is "
                            + uploadInfo.getProgressPercent();
                    Logger.e(TranslationExchangeExport.class.toString(), message);
                }

                FeedbackDialog fd = FeedbackDialog.newInstance(
                        "Project upload",
                        "Project upload failed: " + message
                );
                fd.show(mCtx.getFragmentManager(), "UPLOAD_FEEDBACK");
            }

            @Override
            public void onCompleted(Context context, UploadInfo uploadInfo, ServerResponse serverResponse) {
                Logger.e(
                        TranslationExchangeExport.class.toString(),
                        "code: " + serverResponse.getHttpCode() + " " + serverResponse.getBodyAsString()
                );
                mZipFile.delete();

                FeedbackDialog fd = FeedbackDialog.newInstance(
                        "Project upload",
                        "Project has been successfully uploaded."
                );
                fd.show(mCtx.getFragmentManager(), "title");
            }

            @Override
            public void onCancelled(Context context, UploadInfo uploadInfo) {
                Logger.e(TranslationExchangeExport.class.toString(), "Cancelled upload");
                if (uploadInfo != null) {
                    Logger.e(
                            TranslationExchangeExport.class.toString(),
                            "Upload percent was " + uploadInfo.getProgressPercent()
                    );
                }
            }
        };
        return uploadStatusDelegate;
    }

    protected UploadNotificationConfig getNotificationConfig() {
        UploadNotificationConfig config = new UploadNotificationConfig();

        config.getProgress().iconResourceID = R.drawable.ic_upload;
        config.getProgress().iconColorResourceID = Color.BLUE;

        config.getCompleted().iconResourceID = R.drawable.ic_upload_success;
        config.getCompleted().iconColorResourceID = Color.GREEN;

        config.getError().iconResourceID = R.drawable.ic_upload_error;
        config.getError().iconColorResourceID = Color.RED;

        config.getCancelled().iconResourceID = R.drawable.ic_cancelled;
        config.getCancelled().iconColorResourceID = Color.YELLOW;

        return config;
    }
}
