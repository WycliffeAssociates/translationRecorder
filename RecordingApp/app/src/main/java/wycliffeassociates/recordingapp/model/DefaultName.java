package wycliffeassociates.recordingapp.model;

import android.app.Activity;
import android.os.Bundle;
import android.provider.Settings.Secure;

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
    private int counter;

    /**
     * The default name of the file based on other info provided by the user
     */
    private String defaultName;

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
        counter = 0;
        defaultName = getDefaultName();

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
        if(name.contains(" "))
            name.replace(" ","");
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
        return getAppName() + "-" + getDeviceID() + "-" + getLanguage() + "-"  + getBook() + "-" + getCounter() + ".wav";
    }
}
