package org.wycliffeassociates.translationrecorder.FilesPage.Export;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;

import com.amazonaws.util.Md5Utils;
import com.door43.tools.reporting.Logger;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.gotev.uploadservice.BinaryUploadRequest;
import net.gotev.uploadservice.ServerResponse;
import net.gotev.uploadservice.UploadInfo;
import net.gotev.uploadservice.UploadNotificationConfig;
import net.gotev.uploadservice.UploadStatusDelegate;

import org.apache.commons.io.FileUtils;
import org.wycliffeassociates.translationrecorder.FilesPage.Manifest;
import org.wycliffeassociates.translationrecorder.R;
import org.wycliffeassociates.translationrecorder.database.ProjectDatabaseHelper;
import org.wycliffeassociates.translationrecorder.project.Project;
import org.wycliffeassociates.translationrecorder.project.ProjectFileUtils;
import org.wycliffeassociates.translationrecorder.project.ProjectPatternMatcher;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

/**
 * Created by sarabiaj on 11/16/2017.
 */

public class TranslationExchangeExport extends Export {
    public TranslationExchangeExport(File projectToExport, Project project) {
        super(projectToExport, project);
    }



    private void stageNewFiles(Map<String, String> existingFiles) throws IOException {
        //get local takes for that project
        List<File> filesInProject = new ArrayList<>(
                FileUtils.listFiles(
                        ProjectFileUtils.getProjectDirectory(mProject),
                        new String[]{"wav"},
                        true
                )
        );
        Iterator<File> iter = filesInProject.iterator();
        ProjectPatternMatcher ppm = mProject.getPatternMatcher();
        while (iter.hasNext()) {
            File f = iter.next();
            //remove files already in tE, or files that don't match the file convention
            if (!ppm.match(f.getName())) {
                iter.remove();
            } else if (existingFiles.containsKey(f.getName())) {
                //compute the md5 hash and convert to string
                byte[] bytes = Md5Utils.computeMD5Hash(f);
                StringBuilder hexString = new StringBuilder();
                for (int i = 0; i < bytes.length; i++) {
                    String hex = Integer.toHexString(0xFF & bytes[i]);
                    if (hex.length() == 1) {
                        hexString.append('0');
                    }
                    hexString.append(hex);
                }
                String hash = hexString.toString();
                //compare hash to hash received from tE
                if(hash.equals(existingFiles.get(f.getName()))) {
                    iter.remove();
                } else {
                    System.out.println(f.getName());
                    System.out.println(hash);
                    System.out.println(existingFiles.get(f.getName()));

                }
            }
        }
        File root = new File(mCtx.getActivity().getExternalCacheDir(), "upload");
        StringBuilder dirName = new StringBuilder();
        dirName.append(mProject.getTargetLanguageSlug());
        dirName.append("_");
        dirName.append(mProject.getVersionSlug());
        dirName.append("_");
        dirName.append(mProject.getBookSlug());

        File temp = new File(root, dirName.toString());
        if (temp.exists()) {
            FileUtils.deleteDirectory(temp);
        }
        temp.mkdirs();
        for (File f : filesInProject) {
            FileUtils.copyFileToDirectory(f, temp);
        }

        dirName.append(".zip");
        mZipFile = new File(root, dirName.toString());
        if (mZipFile.exists()) {
            mZipFile.delete();
        }
        mProjectToExport = temp;
    }

    /**
     * Exports to a folder or SD card by starting a wrapper activity around the Storage Access Framework
     */
    public void export() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Manifest manifest = new Manifest();
                try {
                    stageNewFiles(getUploadedFilesList());
                    manifest.createManifestFile(
                            mCtx.getActivity(),
                            mProject,
                            new File(mProjectToExport, "manifest.json"),
                            new ProjectDatabaseHelper(mCtx.getActivity())
                    );
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mCtx.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        zipFiles(TranslationExchangeExport.this, mZipFile);
                    }
                });
            }
        });
        thread.start();
    }

    public String constructProjectQueryParameters(Project project) {
        return String.format("lang=%s&book=%s&anth=%s&version=%s",
                project.getTargetLanguageSlug(),
                project.getBookSlug(),
                project.getAnthologySlug(),
                project.getVersionSlug()
        );
    }

    public Map<String, String> getUploadedFilesList() {
        try {
            String query = constructProjectQueryParameters(mProject);
            URL url = new URL("https://te.loc/api/exclude_files/?" + query);

            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setSSLSocketFactory(getSSLSocketFactory());
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + conn.getResponseCode());
            }
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream())
            );
            String output;
            StringBuilder builder = new StringBuilder();
            while ((output = br.readLine()) != null) {
                builder.append(output);
            }
            return parseJsonOutput(builder.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
        return new HashMap<>();
    }

    //gets the map of filenames to their md5 hashes
    private Map<String, String> parseJsonOutput(String json) {
        HashMap<String, String> map = new HashMap<>();
        JsonArray ja = new JsonParser().parse(json).getAsJsonArray();
        Iterator<JsonElement> iter = ja.iterator();
        while(iter.hasNext()) {
            JsonObject jo = iter.next().getAsJsonObject();
            String file = jo.get("name").getAsString();
            String hash = jo.get("md5hash").getAsString();
            map.put(file, hash);
        }
        return map;
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
                try {
                    FileUtils.deleteDirectory(mProjectToExport);
                } catch (IOException e) {
                    e.printStackTrace();
                }
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
