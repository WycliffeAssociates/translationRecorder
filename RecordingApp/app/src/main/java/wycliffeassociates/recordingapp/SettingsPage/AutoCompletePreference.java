package wycliffeassociates.recordingapp.SettingsPage;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.preference.EditTextPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;

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
 * Created by leongv on 1/19/2016.
 */
public class AutoCompletePreference extends EditTextPreference {


    private static MyAutoCompleteTextView mEditText = null;
    private final String TAG = "AutoCompletePreference";
    public static final String KEY_PREF_LANG = "pref_lang";
    public static final String KEY_PREF_BOOK = "pref_book";
    public static final String KEY_PREF_CHAPTER = "pref_chapter";
    public static final String KEY_PREF_CHUNK = "pref_chunk";
    private static final String KEY_PREF_FILENAME = "pref_filename";
    private static final String KEY_PREF_DRAFT = "pref_draft";

    public String[] mBooks;
    public String[] mLanguages;
    ArrayAdapter<String> adapter;
    String[] COUNTRIES = {"hi"};

    public AutoCompletePreference(Context context) {
        super(context);
    }

    public AutoCompletePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AutoCompletePreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


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

    public void pullLangNames() throws JSONException {
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
            //System.out.println(listHolder[a]);
        }
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
        int i = 0;
        mBooks = new String[books.size()];

        for(Book b : books){
            mBooks[i] = b.getSlug() + " - " + b.getName();
            i++;
        }
    }

    /*
     *
     */
    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        // Find the current EditText object and copy its layout params
        final EditText editText = (EditText)view.findViewById(android.R.id.edit);
        ViewGroup.LayoutParams params = editText.getLayoutParams();
        ViewGroup parent = (ViewGroup) editText.getParent();
        String currentValue = editText.getText().toString();

        // NOTE: For example only. Insert a different adapter here.
        if(this.getKey().compareTo(KEY_PREF_LANG) == 0){
            try {
                pullLangNames();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_dropdown_item_1line, mLanguages);
        } else {
            try{
                pullBookInfo();

                adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_dropdown_item_1line, mBooks);
            } catch (JSONException e){
                e.printStackTrace();
            }
        }
        //ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_dropdown_item_1line, COUNTRIES);

        // Construct a new editable autocomplete object with the appropriate params and id that the
        //    TextEditPreference is expecting
        mEditText = new MyAutoCompleteTextView(getContext());
        mEditText.setLayoutParams(params);
        mEditText.setId(android.R.id.edit);
        mEditText.setText(currentValue);

        // Further modification on editable autocomplete object
        mEditText.setSingleLine(true);
        mEditText.setThreshold(1);
        mEditText.setAdapter(adapter);

        // Swap the old view with the new in the layout
        parent.removeView(editText);
        parent.addView(mEditText);


    }

    /*
     * NOTE: The base class doesn't handle this correctly.
     */
    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if (positiveResult && mEditText != null) {
            String value = mEditText.getText().toString();
            if (callChangeListener(value)) {
                setText(value);
                String[] temp = value.toString().split(" - ");//we only want the language code
                if(temp.length > 1) {
                    String stripCode = temp[0];
                    String key = "";
                    if(this.getKey().compareTo(KEY_PREF_BOOK) == 0){
                        key = KEY_PREF_BOOK;
                    } else {
                        key = KEY_PREF_LANG;
                    }
                    getSharedPreferences().edit().putString(key, stripCode).commit();
                    getSharedPreferences().edit().putString(KEY_PREF_CHAPTER, "1").commit();
                    getSharedPreferences().edit().putString(KEY_PREF_CHUNK, "1").commit();
                }
            }
        }
    }

    /*
     * Return the custom MyAutoCompleteTextView instead of the default one
     */
    @Override
    public EditText getEditText() {
        return mEditText;
    }

    /*
     * Manually close the soft keyboard when dialog is dismissed
     * NOTE: The base class implementation is buggy
     */
    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        View view = ((Activity) getContext()).getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
