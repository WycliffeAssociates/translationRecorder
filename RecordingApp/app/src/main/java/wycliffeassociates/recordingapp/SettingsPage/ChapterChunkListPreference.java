package wycliffeassociates.recordingapp.SettingsPage;

import android.content.Context;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by sarabiaj on 1/20/2016.
 */
public class ChapterChunkListPreference extends ListPreference {

    public static final String KEY_PREF_LANG = "pref_lang";
    public static final String KEY_PREF_BOOK = "pref_book";
    public static final String KEY_PREF_CHAPTER = "pref_chapter";
    public static final String KEY_PREF_CHUNK = "pref_chunk";
    private static final String KEY_PREF_FILENAME = "pref_filename";
    private static final String KEY_PREF_DRAFT = "pref_draft";

    public HashMap<String, Book> mBooks;


    public void init(){
        ParseJSON parse = new ParseJSON(getContext());
        mBooks = parse.getBooksMap();
        String bookCode = getSharedPreferences().getString(KEY_PREF_BOOK, "gen");
        int chapter = Integer.parseInt(getSharedPreferences().getString(KEY_PREF_CHAPTER, "1"));
        String chunkString = getSharedPreferences().getString(KEY_PREF_CHUNK, "1");
//            int chunk = Integer.parseInt(getSharedPreferences().getString(KEY_PREF_CHUNK, "1"));
        // Find first number in chunk description
        Matcher matcher = Pattern.compile("\\d+").matcher(chunkString);
        matcher.find();
        int chunk = Integer.valueOf(matcher.group());
        if(this.getKey().compareTo(KEY_PREF_CHAPTER) == 0){
            int numChapters = mBooks.get(bookCode).getNumChapters();
            String[] chapterList = new String[numChapters];
            for(int i =0; i < numChapters; i++){
                chapterList[i] = String.valueOf(i+1);
            }
            super.setEntries(chapterList);
            super.setEntryValues(chapterList);
            getSharedPreferences().edit().putString(KEY_PREF_CHUNK, "1").commit();
        } else {
            int numChapters = mBooks.get(bookCode).getNumChapters();
            if(chapter > numChapters){
                getSharedPreferences().edit().putString(KEY_PREF_CHAPTER, "1").commit();
                chapter = 1;
            }
            ArrayList<Book.Chunk> chunks = mBooks.get(bookCode).getChunks().get(chapter-1);
            int numChunks = chunks.size();
            String[] chunkList = new String[numChunks];
            for(int i =0; i < numChunks; i++){
//                    chunkList[i] = String.valueOf(i+1);
                Book.Chunk chunkObj = chunks.get(i);
                chunkList[i] = chunkObj.chunkId + ": v" + chunkObj.startVerse + " - v" + chunkObj.endVerse;
            }
            super.setEntries(chunkList);
            super.setEntryValues(chunkList);
        }
    }

    @Override
    protected View onCreateDialogView() {
        init();
        return super.onCreateDialogView();
    }

    @Override
    protected void onDialogClosed(boolean positiveResult){
        super.onDialogClosed(positiveResult);
    }

    public ChapterChunkListPreference(Context context) {
        super(context);
    }

//    public ChapterChunkListPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes){
//        super(context,attrs,defStyleAttr,defStyleRes);
//    }
//
//    public ChapterChunkListPreference(Context context, AttributeSet attrs, int defStyleAttr){
//        super(context,attrs,defStyleAttr);
//    }

    public ChapterChunkListPreference(Context context, AttributeSet attrs){
        super(context,attrs);
    }
}
