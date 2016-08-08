package wycliffeassociates.recordingapp.ProjectManager;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.ImageView;

import wycliffeassociates.recordingapp.R;

/**
 * Created by leongv on 8/8/2016.
 */
public class TakeRatingButton extends ImageView {

    public static final int LEVEL_ZERO = 0;
    public static final int LEVEL_ONE = 1;
    public static final int LEVEL_TWO = 2;
    public static final int LEVEL_THREE = 3;

    private static final int[] STATE_LIGHT = {R.attr.state_light};
    private static final int[] STATE_ZERO = {R.attr.state_zero};
    private static final int[] STATE_ONE = {R.attr.state_one};
    private static final int[] STATE_TWO = {R.attr.state_two};
    private static final int[] STATE_THREE = {R.attr.state_three};

    private int mRating = 0;
    private boolean mLightMode;

    // Constructor
    public TakeRatingButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.checking_level);
        mLightMode = attributes.getBoolean(R.styleable.checking_level_state_light, false);
        attributes.recycle();
    }

    // Setters
    public void setRating(int rating) {
        mRating = rating;
    }

    @Override
    public int[] onCreateDrawableState(int extraSpace) {
        int[] drawableState = super.onCreateDrawableState(extraSpace + 5);
        if (mLightMode) {
            mergeDrawableStates(drawableState, STATE_LIGHT);
        }
        switch (mRating) {
            case LEVEL_ONE: {
                mergeDrawableStates(drawableState, STATE_ONE);
                break;
            }
            case LEVEL_TWO: {
                mergeDrawableStates(drawableState, STATE_TWO);
                break;
            }
            case LEVEL_THREE: {
                mergeDrawableStates(drawableState, STATE_THREE);
                break;
            }
            default: {
                mergeDrawableStates(drawableState, STATE_ZERO);
            }
        }
        return drawableState;
    }

    // NOTE: This is written only for testing purposes. We eventually want to remove this (or make
    // it private).
    public void incrementRating() {
        System.out.println("Increment check level");
        if (mRating >= LEVEL_THREE) {
            mRating = LEVEL_ZERO;
        } else {
            mRating += 1;
        }
        refreshDrawableState();
    }
}
