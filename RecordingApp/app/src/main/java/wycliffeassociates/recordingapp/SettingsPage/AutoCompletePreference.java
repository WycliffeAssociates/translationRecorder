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
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

/**
 * Created by leongv on 1/19/2016.
 */
public class AutoCompletePreference extends EditTextPreference {


    private static AutoCompleteTextView mACTV = null;
    private final String TAG = "AutoCompletePreference";
    // NOTE: For example only
    private static final String[] COUNTRIES = new String[] {
            "German", "Germany", "Germ", "Geronimo", "Gelatin", "Garbage", "Goal", "Golf"
    };


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

        // NOTE: For example only. Insert a diff adapter here.
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_dropdown_item_1line, COUNTRIES);

        // Construct a new editable autocomplete object with the appropriate params and id that the
        //    TextEditPreference is expecting
        mACTV = new AutoCompleteTextView(getContext());
        mACTV.setLayoutParams(params);
        mACTV.setId(android.R.id.edit);
        mACTV.setText(currentValue);

        // Further modification on editable autocomplete object
        mACTV.setThreshold(1);
        mACTV.setAdapter(adapter);

        // Swap the old view with the new in the layout
        parent.removeView(editText);
        parent.addView(mACTV);
    }

    /*
     * NOTE: The base class doesn't handle this correctly.
     */
    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if (positiveResult && mACTV != null) {
            String value = mACTV.getText().toString();
            if (callChangeListener(value)) {
                setText(value);
            }
        }
    }

    /*
     * Return the custom AutoCompleteTextView instead of the default one
     */
    @Override
    public EditText getEditText() {
        return mACTV;
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
            imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
}
