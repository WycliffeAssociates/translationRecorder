package org.wycliffeassociates.translationrecorder.Recording;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import org.wycliffeassociates.translationrecorder.R;

/**
 * Created by leongv on 4/13/2016.
 */
public class UnitPicker extends LinearLayout {

    private int layout = R.layout.unit_picker;
    private ImageButton mIncrementButton;
    private ImageButton mDecrementButton;
    private EditText mText;
    private String[] mDisplayedValues;
    private int mCurrent;
    private OnValueChangeListener mOnValueChangeListener;

    public interface OnValueChangeListener {
        void onValueChange(UnitPicker picker, int oldVal, int newVal);
    }

    public UnitPicker(Context context) {
        this(context, null);
    }

    public UnitPicker(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public UnitPicker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        inflate(getContext(), layout, this);
        mIncrementButton = (ImageButton) findViewById(R.id.increment);
        mDecrementButton = (ImageButton) findViewById(R.id.decrement);
        mText = (EditText) findViewById(R.id.text);
        mCurrent = 0;
        mText.setEnabled(false);
        mText.setFocusable(false);
        mText.setFocusableInTouchMode(false);

        OnClickListener onClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                changeValueByOne(v.getId() == R.id.increment);
            }
        };

        mIncrementButton.setOnClickListener(onClickListener);
        mDecrementButton.setOnClickListener(onClickListener);
    }

    private void updateText() {
        mText.setText(mDisplayedValues[mCurrent]);
    }

    private void changeValueByOne(boolean increment) {
        mText.clearFocus();
        if (mDisplayedValues == null || mDisplayedValues.length <= 0) {
            return;
        }
        int previous = mCurrent;
        if (increment) {
            if (mCurrent + 1 < mDisplayedValues.length) {
                setCurrent(mCurrent + 1);
            } else {
                setCurrent(0);
            }
        } else {
            if (mCurrent - 1 >= 0) {
                setCurrent(mCurrent - 1);
            } else {
                setCurrent(mDisplayedValues.length - 1);
            }
        }
        updateText();
        mOnValueChangeListener.onValueChange(this, previous, mCurrent);
    }


    public void setOnValueChangedListener(OnValueChangeListener onValueChangedListener) {
        mOnValueChangeListener = onValueChangedListener;
    }

    public void setCurrent(int idx) {
        if (idx == mCurrent) {
            return;
        }
        mCurrent = idx;
        updateText();
    }

    public String getCurrent() {
        return mDisplayedValues[mCurrent];
    }

    public int getCurrentIndex() {
        return mCurrent;
    }

    public void setDisplayedValues(String[] displayedValues) {
        if (displayedValues == mDisplayedValues) {
            return;
        }
        mDisplayedValues = displayedValues;
        updateText();
    }

    public String[] getDisplayedValues() {
        return mDisplayedValues;
    }

    public String getCurrentDisplayedValue() {
        return mDisplayedValues[mCurrent];
    }

    public void increment() {
        changeValueByOne(true);
    }

    public void decrement() {
        changeValueByOne(false);
    }

    public void displayIncrementDecrement(boolean display) {
        if (display) {
            mIncrementButton.setVisibility(View.VISIBLE);
            mDecrementButton.setVisibility(View.VISIBLE);
        } else {
            mIncrementButton.setVisibility(View.GONE);
            mDecrementButton.setVisibility(View.GONE);
        }
    }

}
