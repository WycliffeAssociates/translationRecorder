package wycliffeassociates.recordingapp;

import android.app.Activity;
import android.content.Context;
import android.view.inputmethod.InputMethodManager;
import android.view.View;

import java.io.File;

import wycliffeassociates.recordingapp.Reporting.Logger;

/**
 * Created by sarabiaj on 7/1/2016.
 */
public class Utils {
    private Utils() {
    }

    public static void closeKeyboard(Activity ctx) {
        InputMethodManager inputManager = (InputMethodManager) ctx.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (ctx.getCurrentFocus() != null) {
            inputManager.hideSoftInputFromWindow(ctx.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    //http://stackoverflow.com/questions/13410949/how-to-delete-folder-from-internal-storage-in-android
    public static void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                deleteRecursive(child);
            }
        }
        fileOrDirectory.delete();
    }

    // http://stackoverflow.com/questions/5725892/how-to-capitalize-the-first-letter-of-word-in-a-string-using-java
    public static String capitalizeFirstLetter(String string) {
        return string.substring(0, 1).toUpperCase() + string.substring(1).toLowerCase();
    }

    public static void showButton(View view) {
        if (view == null) {
            Logger.i("Utils.hideButton()", "A null view is trying to be shown");
            return;
        }
        view.setVisibility(View.VISIBLE);
    }

    public static void showButton(View[] views) {
        for (View v : views) {
            showButton(v);
        }
    }

    public static void hideButton(View view) {
        if (view == null) {
            Logger.i("Utils.hideButton()", "A null view is trying to be hid");
            return;
        }
        view.setVisibility(View.GONE);
    }

    public static void hideButton(View[] views) {
        for (View v : views) {
            hideButton(v);
        }
    }
}
