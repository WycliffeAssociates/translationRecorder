package wycliffeassociates.recordingapp.ProjectManager;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

import wycliffeassociates.recordingapp.R;

/**
 * Created by leongv on 8/8/2016.
 */
public class CheckLevelButton extends ImageView {

    public static final int LEVEL_ZERO = 0;
    public static final int LEVEL_ONE = 1;
    public static final int LEVEL_TWO = 2;
    public static final int LEVEL_THREE = 3;

    private static final int[] STATE_ZERO = {R.attr.state_zero};
    private static final int[] STATE_ONE = {R.attr.state_one};
    private static final int[] STATE_TWO = {R.attr.state_two};
    private static final int[] STATE_THREE = {R.attr.state_three};

    private int mCheckLevel = 0;

    // Constructor
    public CheckLevelButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    // Setters
    public void setCheckLevel(int checkLevel) {
        mCheckLevel = checkLevel;
    }

    @Override
    public int[] onCreateDrawableState(int extraSpace) {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 4);
        switch (mCheckLevel) {
            case LEVEL_ONE:
                mergeDrawableStates(drawableState, STATE_ONE);
                break;
            case LEVEL_TWO:
                mergeDrawableStates(drawableState, STATE_TWO);
                break;
            case LEVEL_THREE:
                mergeDrawableStates(drawableState, STATE_THREE);
                break;
            default:
                mergeDrawableStates(drawableState, STATE_ZERO);
//                break;
        }
        return drawableState;
    }

    // NOTE: This is written only for testing purposes. We eventually want to remove this (or make
    // it private).
    public void incrementCheckLevel() {
        if (mCheckLevel >= LEVEL_THREE) {
            mCheckLevel = LEVEL_ZERO;
        } else {
            mCheckLevel += 1;
        }
        refreshDrawableState();
    }
}
