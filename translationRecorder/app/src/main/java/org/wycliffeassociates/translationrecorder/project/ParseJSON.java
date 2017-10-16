package org.wycliffeassociates.translationrecorder.project;

import android.content.Context;
import android.util.Pair;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.wycliffeassociates.translationrecorder.project.components.Book;
import org.wycliffeassociates.translationrecorder.project.components.Language;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
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
    private Book[] mBooks;

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
        String json = loadJSONFromAsset("note_books.json");
        JSONArray booksJSON = new JSONArray(json);
        for(int i = 0; i < booksJSON.length(); i++){
            JSONObject bookObj = booksJSON.getJSONObject(i);
            String name = bookObj.getString("name");
            String slug = bookObj.getString("slug");
            String anthology = bookObj.getString("anth");
            int order = bookObj.getInt("num");
            Book book = new Book(slug, name, anthology, order);
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
        mBooks = books.toArray(new Book[books.size()]);
    }



    public int getNumChapters(String bookCode){



        try {
            String json = loadJSONFromAsset("chunks/" + bookCode + "/en/" + "udb" + "/chunks.json");
            JSONArray arrayOfChunks = new JSONArray(json);
            int numChapters = 1;
            //loop through the all the chunks
            for (int i = 0; i < arrayOfChunks.length(); i++) {
                JSONObject jsonChunk = arrayOfChunks.getJSONObject(i);
                String id = jsonChunk.getString("id");
                int chapter = Integer.parseInt(id.substring(0, id.lastIndexOf('-')));
                if(chapter > numChapters){
                    numChapters = chapter;
                }
            }
            return numChapters;
        } catch (JSONException e){
            e.printStackTrace();
        }
        return 1;
    }

    /**
     * Generates a 2d ArrayList of chunks indexed by chapters
     * @param bookCode the code of the book to pull chunk info from
     * @return a 2d ArrayList of chunks
     * @throws JSONException
     */
    public ArrayList<ArrayList<Pair<Integer,Integer>>> getChunks(String bookCode, String source){
        try {
            //FIXME: no folder for "reg"
            if(source.compareTo("reg") == 0){
                source = "ulb";
            }
            String json = loadJSONFromAsset("chunks/" + bookCode + "/en/" + source + "/chunks.json");
            JSONArray arrayOfChunks = new JSONArray(json);
            ArrayList<ArrayList<Pair<Integer, Integer>>> chunksInBook = new ArrayList<>();
            //loop through the all the chunks
            for (int i = 0; i < arrayOfChunks.length(); i++) {
                JSONObject jsonChunk = arrayOfChunks.getJSONObject(i);
                String id = jsonChunk.getString("id");
                int chapter = Integer.parseInt(id.substring(0, id.lastIndexOf('-')));
                //if a chapter hasn't been appended yet, append it
                if (chunksInBook.size() >= chapter - 1) {
                    chunksInBook.add(new ArrayList<Pair<Integer,Integer>>());
                }
                //add the chunk to that chapter
                chunksInBook.get(chapter - 1).add(new Pair<>(jsonChunk.getInt("firstvs"), jsonChunk.getInt("lastvs")));
            }
            return chunksInBook;
        } catch (JSONException e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Generates a 2d ArrayList of verses indexed by chapters
     * @param bookCode the code of the book to pull verse info from
     * @return a 2d ArrayList of verses
     * @throws JSONException
     */
    public ArrayList<ArrayList<Pair<Integer,Integer>>> getVerses(String bookCode, String source){
        try {
            //FIXME: no folder for "reg"
            if(source.compareTo("reg") == 0){
                source = "ulb";
            }
            String json = loadJSONFromAsset("chunks/" + bookCode + "/en/" + source + "/chunks.json");
            JSONArray arrayOfVerses = new JSONArray(json);
            ArrayList<ArrayList<Pair<Integer,Integer>>> versesInBook = new ArrayList<>();
            int lastChapter = 0;
            //loop through the all the verses
            for (int i = 0; i < arrayOfVerses.length(); i++) {
                JSONObject jsonChunk = arrayOfVerses.getJSONObject(i);
                String id = jsonChunk.getString("id");
                int chapter = Integer.parseInt(id.substring(0, id.lastIndexOf('-')));
                //if a chapter hasn't been appended yet, append it
                if (versesInBook.size() >= chapter - 1) {
                    versesInBook.add(new ArrayList<Pair<Integer,Integer>>());
                }
                //add the chunk to that chapter
                versesInBook.get(chapter - 1).add(new Pair<>(jsonChunk.getInt("lastvs"),jsonChunk.getInt("lastvs")));
                if(chapter > lastChapter){
                    lastChapter = chapter;
                }
            }

            ArrayList<ArrayList<Pair<Integer,Integer>>> verses = new ArrayList<>();
            for(int idx = 0; idx < lastChapter; idx++){
                if(idx >= verses.size()){
                    verses.add(new ArrayList<Pair<Integer,Integer>>());
                }
                int numVerses = versesInBook.get(idx).get(versesInBook.get(idx).size()-1).first;
                for(int i = 1; i <= numVerses; i++){
                   verses.get(idx).add(new Pair<>(i,i));
                }
            }
            return verses;
        } catch (JSONException e){
            e.printStackTrace();
        }
        return null;
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

    public Book[] pullBooks(){
        getBooksMap();
        return mBooks;
    }

    public String[] getLanguages(){
        try {
            pullLangNames();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return mLanguages;
    }

    public Language[] pullLangNames() throws JSONException {
        String json = loadJSONFromAsset("langnames.json");
        JSONArray langArray = new JSONArray(json);
        return pullLangNames(langArray);
    }

    public Language[] pullLangNames(JSONArray langArray) throws JSONException {
        ArrayList<Language> languageList = new ArrayList<>();
        for(int i = 0; i < langArray.length(); i++){
            JSONObject langObj = langArray.getJSONObject(i);
            Language ln = new Language(langObj.getString("lc"),langObj.getString("ln"));
            languageList.add(ln);
        }
        mLanguages = new String[languageList.size()];
        for (int a = 0; a < mLanguages.length; a++) {
            mLanguages[a] = (languageList.get(a)).getSlug() + " - " +
                    (languageList.get(a)).getName();
        }
        Language[] languages = new Language[languageList.size()];
        for(int i = 0; i < languageList.size(); i++){
            languages[i] = languageList.get(i);
        }
        return languages;
    }

    public static Language[] getLanguages(Context ctx){
        ParseJSON parse = new ParseJSON(ctx);
        Language[] languages= null;
        try {
            languages = parse.pullLangNames();
        } catch (JSONException e){
            e.printStackTrace();
        }
        return languages;
    }

    public static Book[] getBooks(Context ctx, String testament){
        ParseJSON parse = new ParseJSON(ctx);
        ArrayList<Book> books= new ArrayList<>(Arrays.asList(parse.pullBooks()));
        for(int i = 0; i < books.size(); i++){
            if(testament.compareTo("nt") == 0){
                if(books.get(i).getOrder() < 40){
                    books.remove(i);
                    i--;
                }
            } else {
                if(books.get(i).getOrder() > 39){
                    books.remove(i);
                    i--;
                }
            }
        }
        Book[] bookArray = new Book[books.size()];
        return books.toArray(bookArray);
    }

}
