package wycliffeassociates.recordingapp.SettingsPage;

import android.app.Application;
import android.content.Context;

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
 * Created by Abi on 7/29/2015.
 */
public class ParseJSON {

    private Context mCtx;
    private HashMap<String, Book> mBooksMap;
    private String[] mBooksList;
    private String[] mLanguages;

    public ParseJSON(Context ctx){
        mCtx = ctx;
    }

    private String loadJSONFromAsset(String filename) {
        String json = null;
        try {
            InputStream is = mCtx.getAssets().open(filename);
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

    private void pullBookInfo() throws JSONException {
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
        mBooksMap = new HashMap<>();
        for(Book b : books) {
            mBooksMap.put(b.getSlug(), b);
        }

        int i = 0;
        mBooksList = new String[books.size()];

        for(Book b : books){
            mBooksList[i] = b.getSlug() + " - " + b.getName();
            i++;
        }
    }

    public HashMap<String, Book> getBooksMap(){
        try {
            pullBookInfo();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return mBooksMap;
    }

    public String[] getBooksList(){
        try {
            pullBookInfo();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return mBooksList;
    }

    public String[] getLanguages(){
        try {
            pullLangNames();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return mLanguages;
    }

    private void pullLangNames() throws JSONException {
        ArrayList<Language> languageList = new ArrayList<>();
        String json = loadJSONFromAsset("langnames.json");
        JSONArray langArray = new JSONArray(json);
        for(int i = 0; i < langArray.length(); i++){
            JSONObject langObj = langArray.getJSONObject(i);
            Language ln = new Language(langObj.getString("lc"),langObj.getString("ln"));
            languageList.add(ln);
        }
        mLanguages = new String[languageList.size()];
        for (int a = 0; a < mLanguages.length; a++) {
            mLanguages[a] = (languageList.get(a)).getCode() + " - " +
                    (languageList.get(a)).getName();
        }
    }

}
