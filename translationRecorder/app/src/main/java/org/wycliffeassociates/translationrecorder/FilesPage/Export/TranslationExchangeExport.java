package org.wycliffeassociates.translationrecorder.FilesPage.Export;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.util.Log;

import com.door43.tools.reporting.Logger;

import net.gotev.uploadservice.BinaryUploadRequest;
import net.gotev.uploadservice.ServerResponse;
import net.gotev.uploadservice.UploadInfo;
import net.gotev.uploadservice.UploadNotificationConfig;
import net.gotev.uploadservice.UploadStatusDelegate;

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

    public TranslationExchangeExport(File projectToExport, Project project) {
        super(projectToExport, project);
        mDirectoryToZip = null;
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
//                try {
//                    URL url = new URL("https://te.loc/api/upload/zip");
//                    HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
//                    urlConnection.setSSLSocketFactory(getSSLSocketFactory());
//                    try {
//                        urlConnection.setDoOutput(true);
//                        urlConnection.setChunkedStreamingMode(0);
//
//                        OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
//                        FileInputStream fis = new FileInputStream(mZipFile);
//                        BufferedInputStream bis = new BufferedInputStream(fis);
//                        byte[] buf = new byte[1024];
//                        bis.read(buf);
//                        do {
//                            out.write(buf);
//                        } while(bis.read(buf) != -1);
//                        out.close();
//                        System.out.println(urlConnection.getResponseCode());
//                        System.out.println(urlConnection.getResponseMessage());
//                    } finally {
//                        urlConnection.disconnect();
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
            }
        });
        thread.start();
    }

    public void uploadBinary(final Context context, File file) {
        try {
            // starting from 3.1+, you can also use content:// URI string instead of absolute file
            String filePath = file.getAbsolutePath();
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
            int userId = pref.getInt(Settings.KEY_USER, 1);
            ProjectDatabaseHelper db = new ProjectDatabaseHelper(context);
            User user = db.getUser(userId);
            String hash = user.getHash();
            String uploadId =
                    new BinaryUploadRequest(context, "http://opentranslationtools.org/api/upload/zip")
                            .addHeader("tr-user-hash", hash)
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
            public void onError(Context context, UploadInfo uploadInfo, ServerResponse serverResponse, Exception exception) {
                if (serverResponse != null) {
                    Logger.e(TranslationExchangeExport.class.toString(), "code: " + serverResponse.getHttpCode() + " " + serverResponse.getBodyAsString(), exception);
                } else if (exception != null) {
                    Logger.e(TranslationExchangeExport.class.toString(), "error", exception);
                } else {
                    Logger.e(TranslationExchangeExport.class.toString(), "an error occured without a response or exception, upload percent is " + uploadInfo.getProgressPercent());
                }
            }

            @Override
            public void onCompleted(Context context, UploadInfo uploadInfo, ServerResponse serverResponse) {
                Logger.e(TranslationExchangeExport.class.toString(), "code: " + serverResponse.getHttpCode() + " " + serverResponse.getBodyAsString());
                mZipFile.delete();
            }

            @Override
            public void onCancelled(Context context, UploadInfo uploadInfo) {
                Logger.e(TranslationExchangeExport.class.toString(), "Cancelled upload");
                if (uploadInfo != null) {
                    Logger.e(TranslationExchangeExport.class.toString(), "Upload percent was " + uploadInfo.getProgressPercent());
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
