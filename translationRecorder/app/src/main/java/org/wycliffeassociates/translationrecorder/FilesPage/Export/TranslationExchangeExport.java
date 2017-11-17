package org.wycliffeassociates.translationrecorder.FilesPage.Export;

import org.wycliffeassociates.translationrecorder.FilesPage.Manifest;
import org.wycliffeassociates.translationrecorder.database.ProjectDatabaseHelper;
import org.wycliffeassociates.translationrecorder.project.Project;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by sarabiaj on 11/16/2017.
 */

public class TranslationExchangeExport extends Export {
    public TranslationExchangeExport(File projectToExport, Project project){
        super(projectToExport, project);
    }

    /**
     * Exports to a folder or SD card by starting a wrapper activity around the Storage Access Framework
     */
    public void export(){
        Manifest manifest = new Manifest();
        try {
            manifest.createManifestFile(mCtx.getActivity(), mProject, new File(mProjectToExport, "manifest.json"), new ProjectDatabaseHelper(mCtx.getActivity()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        zipFiles(this);
    }

    @Override
    protected void handleUserInput() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL("http://10.0.0.1/api/upload/zip");
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    try {
                        urlConnection.setDoOutput(true);
                        urlConnection.setChunkedStreamingMode(0);

                        OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
                        FileInputStream fis = new FileInputStream(mZipFile);
                        BufferedInputStream bis = new BufferedInputStream(fis);
                        byte[] buf = new byte[1024];
                        bis.read(buf);
                        do {
                            out.write(buf);
                        } while(bis.read(buf) != -1);
                        out.close();
                        System.out.println(urlConnection.getResponseCode());
                        System.out.println(urlConnection.getResponseMessage());
                    } finally {
                        urlConnection.disconnect();
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }
}
