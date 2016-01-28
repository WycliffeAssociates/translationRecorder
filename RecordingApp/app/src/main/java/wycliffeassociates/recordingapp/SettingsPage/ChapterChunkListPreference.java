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

    public String loadJSONFromAsset(String filename) {
        String json = null;
        try {
            InputStream is = getContext().getAssets().open(filename);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    public void pullBookInfo() throws JSONException{
        ArrayList<Book> books = new ArrayList<>();
        String json = loadJSONFromAsset("chunks.json");
        JSONArray booksJSON = new JSONArray(json);
        for(int i = 0; i < booksJSON.length(); i++){
            JSONObject bookObj = booksJSON.getJSONObject(i);
            String name = bookObj.getString("name");
            String slug = bookObj.getString("slug");
            int chapters = bookObj.getInt("chapters");
            int order = bookObj.getInt("sort");
            JSONArray chunkArrayJSON = bookObj.getJSONArray("chunks");
            ArrayList<ArrayList<Book.Chunk>> chunks = new ArrayList<>();
            for(int j = 0; j < chunkArrayJSON.length(); j++){
                ArrayList<Book.Chunk> chunksInChapter = new ArrayList<>();
                // chunks.add(chunkArrayJSON.getInt(j));
                JSONArray chunkListObj = chunkArrayJSON.getJSONArray(j);
                for (int k = 0; k < chunkListObj.length(); k++) {
                    JSONObject chunkObj = chunkListObj.getJSONObject(k);
                    Book.Chunk chunk = new Book.Chunk();
                    chunk.chapterId = chunkObj.getInt("chapter_id");
                    chunk.chunkId = chunkObj.getInt("chunk_id");
                    chunk.startVerse = chunkObj.getInt("start_verse");
                    chunk.endVerse = chunkObj.getInt("end_verse");
                    chunksInChapter.add(chunk);
                }
                chunks.add(chunksInChapter);
            }
            Book book = new Book(slug, name, chapters, chunks, order);
            books.add(book);
        }
        Collections.sort(books, new Comparator<Book>() {
            @Override
            public int compare(Book lhs, Book rhs) {
                if (lhs.getOrder() > rhs.getOrder()) {
                    return 1;
                } else if (lhs.getOrder() < rhs.getOrder()) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });
        mBooks = new HashMap<>();
        for(Book b : books) {
            mBooks.put(b.getSlug(), b);
        }
    }
    public void init(){
        try {
            pullBookInfo();
            String bookCode = getSharedPreferences().getString(KEY_PREF_BOOK, "gen");
            int chapter = Integer.parseInt(getSharedPreferences().getString(KEY_PREF_CHAPTER, "1"));
            int chunk = Integer.parseInt(getSharedPreferences().getString(KEY_PREF_CHUNK, "1"));
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
        } catch (JSONException e) {
            e.printStackTrace();
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
