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

import wycliffeassociates.recordingapp.project.ParseJSON;

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
        ParseJSON parse = new ParseJSON(getContext());

        // NOTE: For example only. Insert a different adapter here.
        if(this.getKey().compareTo(KEY_PREF_LANG) == 0){
            mLanguages = parse.getLanguages();
            adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_dropdown_item_1line, mLanguages);
        } else {
            mBooks = parse.getBooksList();
            adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_dropdown_item_1line, mBooks);
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
