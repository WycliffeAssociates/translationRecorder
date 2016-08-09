package wycliffeassociates.recordingapp.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.ImageView;

import wycliffeassociates.recordingapp.R;

/**
 * Created by leongv on 8/9/2016.
 */
public class FourStepImageView extends ImageView {

    // Constants
    public static final int MIN_STEP = 0;
    public static final int MAX_STEP = 3;

    // States
    private static final int[] LIGHT_MODE = {R.attr.light_mode};
    private static final int[] STEP_ZERO = {R.attr.step_zero};
    private static final int[] STEP_ONE = {R.attr.step_one};
    private static final int[] STEP_TWO = {R.attr.step_two};
    private static final int[] STEP_THREE = {R.attr.step_three};

    // Attributes
    private boolean mLightMode;
    private int mStep;



    // Constructor
    public FourStepImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.four_step_image_view);

        mLightMode = attributes.getBoolean(R.styleable.four_step_image_view_light_mode, false);
        mStep = attributes.getInteger(R.styleable.four_step_image_view_step, 0);

        attributes.recycle();
    }



    // Overrides
    @Override
    public int[] onCreateDrawableState(int extraSpace) {
        int[] drawableState = super.onCreateDrawableState(extraSpace + 2);
        if (mLightMode) {
            mergeDrawableStates(drawableState, LIGHT_MODE);
        }
        switch (mStep) {
            case 1: {
                mergeDrawableStates(drawableState, STEP_ONE);
                break;
            }
            case 2: {
                mergeDrawableStates(drawableState, STEP_TWO);
                break;
            }
            case 3: {
                mergeDrawableStates(drawableState, STEP_THREE);
                break;
            }
            default: {
                mergeDrawableStates(drawableState, STEP_ZERO);
            }
        }
        return drawableState;
    }



    // Setters
    public void setStep(int step) {
        if (step >= MIN_STEP && step <= MAX_STEP) {
            mStep = step;
        } else {
            mStep = 0;
        }
        refreshDrawableState();
    }

    public void setLightMode(boolean lightMode) {
        mLightMode = lightMode;
        refreshDrawableState();
    }



    // Getters
    public int getStep() {
        return mStep;
    }

    public boolean getLightMode() {
        return mLightMode;
    }



    // Public API
    public void incrementStep() {
        // NOTE: This is written only for testing purposes. We eventually want to remove this (or make
        // it private).
        System.out.println("Increment step");
        if (mStep >= MAX_STEP) {
            mStep = MIN_STEP;
        } else {
            mStep += 1;
        }
        refreshDrawableState();
    }

}
