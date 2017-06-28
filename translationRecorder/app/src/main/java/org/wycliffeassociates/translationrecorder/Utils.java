package org.wycliffeassociates.translationrecorder;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.door43.tools.reporting.Logger;

import java.io.File;

/**
 * Created by sarabiaj on 7/1/2016.
 */
public class Utils {
    private Utils() {
    }

    public static File VISUALIZATION_DIR;

    public static void swapViews(final View[] toShow, final View[] toHide) {
        for (View v : toShow) {
            if (v != null) {
                v.setVisibility(View.VISIBLE);
            }
        }
        for (View v : toHide) {
            if (v != null) {
                v.setVisibility(View.INVISIBLE);
            }
        }
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
            if(fileOrDirectory.listFiles() != null) {
                for (File child : fileOrDirectory.listFiles()) {
                    deleteRecursive(child);
                }
            }
        }
        fileOrDirectory.delete();
    }

    // http://stackoverflow.com/questions/5725892/how-to-capitalize-the-first-letter-of-word-in-a-string-using-java
    public static String capitalizeFirstLetter(String string) {
        return string.substring(0, 1).toUpperCase() + string.substring(1).toLowerCase();
    }

    public static void showView(View view) {
        if (view == null) {
            Logger.i("Utils.showView()", "A null view is trying to be shown");
            return;
        }
        view.setVisibility(View.VISIBLE);
    }

    public static void showView(View[] views) {
        for (View v : views) {
            showView(v);
        }
    }

    public static void hideView(View view) {
        if (view == null) {
            Logger.i("Utils.hideView()", "A null view is trying to be hid");
            return;
        }
        view.setVisibility(View.GONE);
    }

    public static void hideView(View[] views) {
        for (View v : views) {
            hideView(v);
        }
    }
}
