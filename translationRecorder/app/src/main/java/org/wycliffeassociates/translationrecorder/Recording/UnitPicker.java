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
        void onValueChange(UnitPicker picker, int oldVal, int newVal, DIRECTION direction);
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

    public enum DIRECTION {
        INCREMENT,
        DECREMENT
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

        mIncrementButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                increment();
            }
        });

        mDecrementButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                decrement();
            }
        });
    }

    private void updateText() {
        mText.setText(mDisplayedValues[mCurrent]);
    }

    private void changeValueByOne(DIRECTION direction) {
        mText.clearFocus();
        if (mDisplayedValues == null || mDisplayedValues.length <= 0) {
            return;
        }
        int previous = mCurrent;
        if (direction == DIRECTION.INCREMENT) {
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
        mOnValueChangeListener.onValueChange(this, previous, mCurrent, direction);
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
        changeValueByOne(DIRECTION.INCREMENT);
    }

    public void decrement() {
        changeValueByOne(DIRECTION.DECREMENT);
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
