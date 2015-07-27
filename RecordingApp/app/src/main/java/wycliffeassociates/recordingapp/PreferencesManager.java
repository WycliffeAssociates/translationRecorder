package wycliffeassociates.recordingapp;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * Implementation of SharedPreferences for storing persistent key-value pair data
 *
 * SharedPreferences is a Singleton object, allowing multiple references.
 */
public class PreferencesManager {


    private static final String PREF_NAME = "wycliffeassociates.recordingapp.properties";

    private static PreferencesManager sInstance;
    //private final Preferences mPref;
    private final SharedPreferences mPref;

    public PreferencesManager(Context context) {
        mPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized void initializeInstance(Context context) {
        if (sInstance == null) {
            sInstance = new PreferencesManager(context);
        }
    }

    public static synchronized PreferencesManager getInstance() {
        if (sInstance == null) {
            throw new IllegalStateException(PreferencesManager.class.getSimpleName() +
                    " is not initialized, call initializeInstance(..) method first.");
        }
        return sInstance;
    }

    /**
     * Removes specific key's value.
     * @param key
     *          the key whose value will be removed
     */
    public void remove(String key) {
        mPref.edit().remove(key).commit();
    }

    /**
     * Resets all preferences to the default instantiation.  Uses defaultPreferences to reset.
     * @param name
     *          the string to specify which preference to reset
     *
     *          additionally "all"
     */
    public void resetPreferences(String name){

        //create editor
        SharedPreferences.Editor editor = mPref.edit();

        //clear preferences
        editor.clear();

        //get default preferences
        HashMap<String, Object> temp = (HashMap) defaultPreferences("reset", true);

        //set default preferences
        if(name.toLowerCase().equals("all")){
            editor.putString("fileName", (String) temp.get("fileName"));
            editor.putInt   ("fileCounter", (int) temp.get("fileCounter"));
            editor.putString("fileDirectory", (String) temp.get("fileDirectory"));
            editor.putString("fileFolder", (String) temp.get("fileFolder"));
            editor.putString("exportDirectory", (String) temp.get("exportDirectory"));
            editor.putString("Language", (String) temp.get("Language"));
        }else if(name.equals("fileCounter")){
            editor.putInt(name, (int) temp.get(name));
        }else{
            editor.putString(name, (String) temp.get(name));
        }
        //asynchronous commit
        editor.apply();
    }

    /**
     * Sets preferences based on function parameters
     *
     * @param name
     *            the key/identifier of the preference
     * @param obj
     *            the value to be set for the preference
     */
    public void setPreferences(String name, Object obj){
        SharedPreferences.Editor editor = mPref.edit();

        //edit preferences -- type casting Object (generics better?)
        if(obj instanceof Integer) {
            editor.putInt(name, (int) obj);
        }else if(obj instanceof String) {
            editor.putString(name, (String) obj);
        }else if(obj instanceof Boolean){
            editor.putBoolean(name, (Boolean) obj);
        }
        //asynchronous commit
        editor.apply();

    }

    /**
     * Gets preferences based on key/identifier, or attempts to initialize.
     *
     * @param name
     *            the key/identifier of the preference
     * @return
     *            the value of the preference, or null if invalid
     */
    public Object getPreferences(String name){
        Map mp = mPref.getAll();

        Object ret = null;

        if(name.toLowerCase().equals("all")){
            ret = mp;
        }else {
            //if existing
            if (mp.containsKey(name)) {
                ret = mp.get(name);
            } else {
                Object defaultPref = defaultPreferences(name, false);
                ret = defaultPref;
                if (defaultPref == null) {
                    //name isn't a valid preference -- error
                } else {
                    //instantiate preference with default
                    setPreferences(name, defaultPref);
                }
            }
        }

        //returns option, null if invalid
        return ret;
    }

    /**
     * Holder of all default preferences.  Used to by functions to initialize and hard reset.
     *
     * @param name
     *            the key/identifier of the preference
     * @param flag
     *            a flag for returning the HashMap, or returning a single value
     * @return
     *            the corresponding value of the key
     */
    private Object defaultPreferences(String name, boolean flag){
        HashMap<String, Object> prefs = new HashMap<String, Object>();

        //======
        //DEFAULTS
        prefs.put("fileName", "Recording");
        prefs.put("fileCounter", 1);
        prefs.put("fileDirectory", Environment.getExternalStorageDirectory().getPath().toString());
        prefs.put("fileFolder", "AudioRecorder");
        prefs.put("exportDirectory", Environment.getExternalStorageDirectory().getPath().toString());
        //prefs.put("fileFolder", "AudioRecorder");
        prefs.put("Language", "EN");
        //======

        Object ret = null;
        //Return entire HashMap for all default preferences
        if(flag){
            ret = prefs;
            return ret;
        }

        //return default preference
        if(prefs.containsKey(name)) {
            ret = prefs.get(name);
        }else{
            //not a pref
        }
        return ret;
    }
}