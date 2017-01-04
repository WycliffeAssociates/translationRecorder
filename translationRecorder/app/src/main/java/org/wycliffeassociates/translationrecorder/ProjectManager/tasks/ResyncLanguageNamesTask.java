package org.wycliffeassociates.translationrecorder.ProjectManager.tasks;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.wycliffeassociates.translationrecorder.project.ParseJSON;
import org.wycliffeassociates.translationrecorder.utilities.Task;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.wycliffeassociates.translationrecorder.database.ProjectDatabaseHelper;
import org.wycliffeassociates.translationrecorder.project.Language;

/**
 * Created by sarabiaj on 12/14/2016.
 */

public class ResyncLanguageNamesTask extends Task {
    Context mCtx;

    public ResyncLanguageNamesTask(int taskTag, Context ctx) {
        super(taskTag);
        mCtx = ctx;
    }

    @Override
    public void run() {
        try {
            URL url = new URL("http://td.unfoldingword.org/exports/langnames.json");
            HttpURLConnection urlConnection = null;
            try {
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                StringBuilder json = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    json.append(line);
                }
                try {
                    JSONArray jsonObject = new JSONArray(json.toString());
                    ParseJSON parseJSON = new ParseJSON(mCtx);
                    Language[] languages = parseJSON.pullLangNames(jsonObject);
                    ProjectDatabaseHelper db = new ProjectDatabaseHelper(mCtx);
                    db.addLanguages(languages);
                    db.close();
                    onTaskCompleteDelegator();
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(mCtx, "Languages successfully updated!", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                urlConnection.disconnect();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
}
