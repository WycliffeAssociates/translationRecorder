package org.wycliffeassociates.translationrecorder.FilesPage.Export;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.wycliffeassociates.translationrecorder.FilesPage.Manifest;
import org.wycliffeassociates.translationrecorder.TranslationRecorderApp;
import org.wycliffeassociates.translationrecorder.database.ProjectDatabaseHelper;
import org.wycliffeassociates.translationrecorder.project.Project;
import org.wycliffeassociates.translationrecorder.project.ProjectFileUtils;
import org.wycliffeassociates.translationrecorder.project.ProjectPatternMatcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by sarabiaj on 1/24/2018.
 */

public class TranslationExchangeDiff {

    public static int DIFF_ID = 1;

    final Project mProject;
    final TranslationRecorderApp mApp;
    volatile ArrayList<File> mFilesToUpload;

    //Arraylist explicitly specified because of zip4j dependency
    public TranslationExchangeDiff(TranslationRecorderApp app, Project project) {
        mApp = app;
        mProject = project;
    }


    public String constructProjectQueryParameters(Project project) {
        return String.format("lang=%s&book=%s&anth=%s&version=%s",
                project.getTargetLanguageSlug(),
                project.getBookSlug(),
                project.getAnthologySlug(),
                project.getVersionSlug()
        );
    }

    public Map<String, String> getUploadedFilesList(Project project, TranslationRecorderApp app) {
        try {
            String query = constructProjectQueryParameters(project);
            URL url = new URL("http://opentranslationtools.org/api/exclude_files/?" + query);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
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
        }
        return new HashMap<>();
    }

    private ArrayList<File> stageNewFiles(Map<String, String> existingFiles) throws IOException {
        //get local takes for that project
        final ArrayList<File> filesInProject = new ArrayList<>(
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
                String hash = new String(Hex.encodeHex(DigestUtils.md5(new FileInputStream(f))));
                //compare hash to hash received from tE
                if (hash.equals(existingFiles.get(f.getName()))) {
                    iter.remove();
                } else {
                    System.out.println(f.getName());
                    System.out.println(hash);
                    System.out.println(existingFiles.get(f.getName()));
                }
            }
        }
        return filesInProject;
    }

    //gets the map of filenames to their md5 hashes
    private Map<String, String> parseJsonOutput(String json) {
        HashMap<String, String> map = new HashMap<>();
        JsonArray ja = new JsonParser().parse(json).getAsJsonArray();
        Iterator<JsonElement> iter = ja.iterator();
        while (iter.hasNext()) {
            JsonObject jo = iter.next().getAsJsonObject();
            String file = jo.get("name").getAsString();
            String hash = jo.get("md5hash").getAsString();
            map.put(file, hash);
        }
        return map;
    }

    public void computeDiff(final File outFile, final SimpleProgressCallback progressCallback) {
        Thread diff = new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            progressCallback.onStart(DIFF_ID);
                            mFilesToUpload = stageNewFiles(getUploadedFilesList(mProject, mApp));
                            Manifest manifest = new Manifest(mProject, ProjectFileUtils.getProjectDirectory(mProject));
                            manifest.setProgressCallback(progressCallback);
                            File mani = manifest.createManifestFile(mApp, mApp.getDatabase());
                            mFilesToUpload.add(mani);
                            List<File> userFiles = manifest.getUserFiles();
                            for(File file : userFiles) {
                                mFilesToUpload.add(file);
                            }
                            progressCallback.onComplete(DIFF_ID);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
        );
        diff.start();
    }

    public ArrayList<File> getDiff() {
        return mFilesToUpload;
    }
}
