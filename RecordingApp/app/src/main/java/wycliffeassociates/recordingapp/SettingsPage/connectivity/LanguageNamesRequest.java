package wycliffeassociates.recordingapp.SettingsPage.connectivity;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import wycliffeassociates.recordingapp.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import wycliffeassociates.recordingapp.SettingsPage.Settings;
import wycliffeassociates.recordingapp.model.Language;

public class LanguageNamesRequest extends Activity {

    private ProgressDialog pDialog;

    // URL to get contacts JSON
    private static final String HOST_DOMAIN = "http://td.unfoldingword.org/exports/langnames.json";

    // JSON Node names
    private static final String GATEWAY_LANGUAGE = "gw";
    private static final String LANGAUGE_DIRECTION = "ld";
    private static final String LANGUAGE_CODE = "lc";
    private static final String LANGAUGE_NAME = "ln";
    private static final String COUNTRY_CODE = "cc";
    private static final String PRIMARY_KEY = "pk";

    // contacts JSONArray
    JSONArray langs = null;

    // Hashmap for ListView
    ArrayList<Language> languageList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        //.getMyMemoryState();
        //.MemoryInfo();

        languageList = new ArrayList<Language>();

        // Calling async task to pull json
        new GetContacts().execute();
    }


    @Override
    protected void onStop() {
        super.onStop();
        if (pDialog != null) {
            pDialog.dismiss();
            pDialog = null;
        }
    }

    /**
     * Async task class to get json by making HTTP call
     * */
    private class GetContacts extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(LanguageNamesRequest.this);
            pDialog.setMessage("Updating, Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected Void doInBackground(Void... arg0) {

            String tableName = "langnames";
            SQLiteDatabase audiorecorder = openOrCreateDatabase(tableName, MODE_PRIVATE, null);

            String queryDelete = "DROP TABLE " + tableName;
            String queryCreate = "CREATE TABLE IF NOT EXISTS " + tableName + "(gw INTEGER, ld VARCHAR, " +
                    "lc VARCHAR, ln VARCHAR, cc VARCHAR, pk INTEGER);";
            String queryInsertLang = "INSERT INTO " + tableName + " (gw, ld, lc, ln, cc, pk) " +
                    "VALUES ";
            String queryDuplicate = "WHERE NOT EXISTS ( SELECT * FROM " + tableName + " WHERE pk = ";

            //String queryOnDuplicate = " ON DUPLICATE KEY UPDATE "

            //kill database
            try{
                audiorecorder.execSQL(queryDelete);
            }catch(RuntimeException e){
                //
            }

            //create database
            audiorecorder.execSQL(queryCreate);


            // Creating service handler class instance
            ServiceHandler sh = new ServiceHandler();

            // Making a request to url and getting response
            String jsonStr = "";
            Boolean toggle = false;
            try {
                jsonStr = sh.makeServiceCall(HOST_DOMAIN, ServiceHandler.GET);
            }catch(Exception e){
                toggle = true;
            };

            if (jsonStr != null) {
                toggle = false;
            } else {
                toggle = true;
                Log.e("ServiceHandler", "Couldn't get any data from the url");
            }

            if(toggle) {
                try {

                    InputStream is = getAssets().open("langnames.json");

                    int size = is.available();

                    byte[] buffer = new byte[size];

                    is.read(buffer);

                    is.close();

                    jsonStr = new String(buffer, "UTF-8");


                } catch (IOException ex) {
                    ex.printStackTrace();
                    return null;
                }
            }

            try {
                JSONArray jsonArray = new JSONArray((jsonStr));

                //PreferencesManager pref = new PreferencesManager(LanguageNamesRequest.this, "wycliffeassociates.recordingapp.langnames");
                //pref.resetPreferences("clear");

                for(int i = 0; i < jsonArray.length() ; i++){
                    JSONObject c = jsonArray.getJSONObject(i);

                    //SQL boolean
                    Boolean gw = c.getBoolean(GATEWAY_LANGUAGE);

                    int sqlGW = 0;
                    if(gw){
                        sqlGW = 1;
                    }

                    String ld = c.getString(LANGAUGE_DIRECTION);
                    String lc = c.getString(LANGUAGE_CODE);
                    String ln = c.getString(LANGAUGE_NAME);

                    //SQL injection
                    ld = sanitizeSQL(ld);
                    lc = sanitizeSQL(lc);
                    ln = sanitizeSQL(ln);

                    // JSONObject jsonObj = c.getJSONObject()
                    // String cc = c.getString(COUNTRY_CODE);

                    int pk = c.getInt(PRIMARY_KEY);

                    //INSERT INTO
                    audiorecorder.execSQL(queryInsertLang + "(" +
                            sqlGW + ", '" +
                            ld + "', '" +
                            lc + "', '" +
                            ln + "', '" +
                            "temp" + "', " +
                            pk + ") ");
                    //+ queryDuplicate + pk + ";");


                    //Language temp = new Language(gw, ld, lc, ln, cc, pk);
                    //languageList.add(temp);
                    //pref.setPreferences(Integer.toString(pk), ln);
                    //pref.putObject(Integer.toString(pk), temp);

                }

                audiorecorder.close();
                //System.gc();

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();


            Intent intent = new Intent(LanguageNamesRequest.this, Settings.class);
            startActivityForResult(intent, 0);
        }

        private String sanitizeSQL(String input){
            String output = input;

            //Do more.
            output = output.replaceAll("'", "''");

            return output;
        }

    }
}
