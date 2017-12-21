package org.wycliffeassociates.translationrecorder.FilesPage.Export;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;

import net.gotev.uploadservice.BinaryUploadRequest;
import net.gotev.uploadservice.UploadNotificationConfig;

import org.wycliffeassociates.translationrecorder.FilesPage.Manifest;
import org.wycliffeassociates.translationrecorder.R;
import org.wycliffeassociates.translationrecorder.database.ProjectDatabaseHelper;
import org.wycliffeassociates.translationrecorder.project.Project;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

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
                Context ctx = mCtx.getActivity().getApplicationContext();
                uploadBinary(ctx, mZipFile);
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
            String uploadId =
                    new BinaryUploadRequest(context, "https://te.loc/api/upload/zip")
                            .setFileToUpload(filePath)
                            .addHeader(file.getName(), new File(filePath).getName())
                            .setNotificationConfig(getNotificationConfig())
                            .setMaxRetries(2)
                            .startUpload();

        } catch (Exception exc) {
            Log.e("AndroidUploadService", exc.getMessage(), exc);
        }
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

    private SSLSocketFactory getSSLSocketFactory() throws CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        // Load CAs from an InputStream
        // (could be from a resource or ByteArrayInputStream or ...)
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        // From https://www.washington.edu/itconnect/security/ca/load-der.crt
        InputStream caInput = new BufferedInputStream(mCtx.getActivity().getAssets().open("rootCA.crt"));
        Certificate ca;
        try {
            ca = cf.generateCertificate(caInput);
            System.out.println("ca=" + ((X509Certificate) ca).getSubjectDN());
        } finally {
            caInput.close();
        }

        // Create a KeyStore containing our trusted CAs
        String keyStoreType = KeyStore.getDefaultType();
        KeyStore keyStore = KeyStore.getInstance(keyStoreType);
        keyStore.load(null, null);
        keyStore.setCertificateEntry("ca", ca);

        // Create a TrustManager that trusts the CAs in our KeyStore
        String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
        tmf.init(keyStore);

        // Create an SSLContext that uses our TrustManager
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, tmf.getTrustManagers(), null);
        return context.getSocketFactory();
    }
}
