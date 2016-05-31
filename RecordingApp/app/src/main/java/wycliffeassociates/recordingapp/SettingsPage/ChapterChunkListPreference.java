package wycliffeassociates.recordingapp.SettingsPage;

import android.content.Context;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;

import wycliffeassociates.recordingapp.project.Book;
import wycliffeassociates.recordingapp.project.ParseJSON;

/**
 * Created by sarabiaj on 1/20/2016.
 */
public class ChapterChunkListPreference extends ListPreference {

    public static final String KEY_PREF_LANG = "pref_lang";
    public static final String KEY_PREF_BOOK = "pref_book";
    public static final String KEY_PREF_CHAPTER = "pref_chapter";
    public static final String KEY_PREF_CHUNK = "pref_chunk";
    public static final String KEY_PREF_VERSE = "pref_verse";
    private static final String KEY_PREF_FILENAME = "pref_filename";
    private static final String KEY_PREF_DRAFT = "pref_draft";

    public HashMap<String, Book> mBooks;


    public void init(){
        ParseJSON parse = new ParseJSON(getContext());
        mBooks = parse.getBooksMap();
        String bookCode = getSharedPreferences().getString(KEY_PREF_BOOK, "gen");
        int chapter = Integer.parseInt(getSharedPreferences().getString(KEY_PREF_CHAPTER, "1"));
        int numChapters = parse.getNumChapters(bookCode);
        String src = getSharedPreferences().getString(Settings.KEY_PREF_SOURCE, "udb");

        //if the selected preference was chapter
        if (this.getKey().compareTo(KEY_PREF_CHAPTER) == 0){
            String[] chapterList = new String[numChapters];
            for(int i =0; i < numChapters; i++){
                chapterList[i] = String.valueOf(i+1);
            }
            super.setEntries(chapterList);
            super.setEntryValues(chapterList);
            getSharedPreferences().edit().putString(KEY_PREF_CHUNK, "1").commit();
            getSharedPreferences().edit().putString(KEY_PREF_VERSE, "1").commit();
            super.setValue(getSharedPreferences().getString(KEY_PREF_CHAPTER, "1"));
        } else if (this.getKey().compareTo(KEY_PREF_CHUNK) == 0){
            if(chapter > numChapters){
                getSharedPreferences().edit().putString(KEY_PREF_CHAPTER, "1").commit();
                chapter = 1;
            }
            ArrayList<Pair<Integer,Integer>> chunks = parse.getChunks(bookCode, src).get(chapter-1);
            int numChunks = chunks.size();
            String[] chunkList = new String[numChunks];
            //Chunks are labeled by start verse, rather than sequential numbers
            for(int i =0; i < numChunks; i++){
                chunkList[i] = String.valueOf(chunks.get(i).first);
            }
            super.setEntries(chunkList);
            super.setEntryValues(chunkList);
            super.setValue(getSharedPreferences().getString(KEY_PREF_CHUNK, "1"));
        //else the verse was edited
        } else {
            if(chapter > numChapters){
                getSharedPreferences().edit().putString(KEY_PREF_CHAPTER, "1").commit();
                chapter = 1;
            }
            ArrayList<Pair<Integer,Integer>> verses = parse.getVerses(bookCode, src).get(chapter-1);
            int numVerses = verses.size();
            String[] verseList = new String[numVerses];
            //Chunks are labeled by start verse, rather than sequential numbers
            for(int i =0; i < numVerses; i++){
                verseList[i] = String.valueOf(verses.get(i).first);
            }
            super.setEntries(verseList);
            super.setEntryValues(verseList);
            super.setValue(getSharedPreferences().getString(KEY_PREF_VERSE, "1"));
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
