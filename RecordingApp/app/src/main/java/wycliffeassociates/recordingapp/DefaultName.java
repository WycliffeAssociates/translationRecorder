package wycliffeassociates.recordingapp;

import android.app.Activity;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.io.File;

import wycliffeassociates.recordingapp.R;

/**
 * Setting the default name in format appName-deviceUUID-language-book-Incremented#.wav
 * Created by Abi on 7/29/2015.
 */
public class DefaultName extends Activity
{
    /**
     * The unique identifier of the device
     */
    private String deviceID;

    /**
     * The name of this app (for identifying on the server)
     */
    private String appName;

    /**
     * The language code for this translation
     */
    private String language;

    /**
     * The book being translated
     */
    private String book;

    /**
     * The counter that increments every time a new recording is made
     */
    private static int counter;

    /**
     * The default name of the file based on other info provided by the user
     */
    private String defaultName;

    /**
     * True if the user wants to name according to lang & book code
     */
    private boolean useDefaults;

    /**
     * Path to the json file for language code
     */
    File langCodePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.default_name);

        //Initialize all class variables
        deviceID = getDeviceID();
        appName = getAppName();
        language = "";
        book = "";
        //TODO: get the counter from settings.java
        counter = 1;
        defaultName = getDefaultName();

        langCodePath = getDir("TranslationCodes", MODE_PRIVATE);

        String[] list = getFilesDir().list();
        for(int i = 0; i < list.length; i++)
             System.out.println("ABI: " + list[i]);
        System.out.println("ABI: " + list.length);


    }

    /**
     * Getter for the unique identifier of the device
     * @return the unique identifier of the device
     */
    public String getDeviceID() {
        return Secure.ANDROID_ID;
    }

    /**
     * Getter for the name of the app
     * @return the name of the app
     */
    public String getAppName() {
        String name = getResources().getString(R.string.app_name);
        name.replace(" ","");
        name.toLowerCase();
        return name;
    }

    /**
     * Getter for the language of the translation
     * @return The language
     */
    public String getLanguage() {
        return language;
    }

    /**
     * Setter for the language of the translation
     * @param language The language of the translation
     */
    public void setLanguage(String language) {
        this.language = language;
    }

    /**
     * The getter for the book being translated
     * @return the book being translated
     */
    public String getBook() {
        return book;
    }

    /**
     * THe setter for the book being translated
     * @param book The book being translated
     */
    public void setBook(String book) {
        this.book = book;
    }

    /**
     * The getter for the recording number
     * @return The recording number
     */
    public int getCounter() {
        return counter;
    }

    /**
     * Increments the counter variable
     */
    public void incrementCounter(){
        counter++;
    }

    /**
     * Resets the counter to 1
     */
    public void resetCounter(){
        counter = 1;
    }

    /**
     * setter for the recording number
     * @param counter The recording number
     */
    public void setCounter(int counter) {
        this.counter = counter;
    }

    /**
     * Getter for the default name
     * @return The default name
     */
    public String getDefaultName() {
        if(useDefaults)
             return getAppName() + "-" + getDeviceID() + "-" + getLanguage() + "-"  + getBook() + "-" + getCounter() + ".wav";
        else//TODO: add in filename given by user
            return getCounter() + ".wav";
    }

    /**
     * Checks if defaults are being used in default name
     * @return True if defaults are being used, false otherwise
     */
    public boolean isUseDefaults() {
        return useDefaults;
    }

    /**
     * Setter for whether or not defaults are used in name
     * @param useDefaults True if defaults are used in name, false otherwise
     */
    public void setUseDefaults(boolean useDefaults) {
        this.useDefaults = useDefaults;
    }
}
