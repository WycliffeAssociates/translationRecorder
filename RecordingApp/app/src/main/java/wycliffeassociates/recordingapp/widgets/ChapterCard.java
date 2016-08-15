package wycliffeassociates.recordingapp.widgets;

import android.app.Activity;
import android.view.View;

import java.lang.ref.SoftReference;
import java.util.List;

import wycliffeassociates.recordingapp.ProjectManager.Project;
import wycliffeassociates.recordingapp.ProjectManager.ChapterCardAdapter;
import wycliffeassociates.recordingapp.ProjectManager.UnitCardAdapter;
import wycliffeassociates.recordingapp.R;

/**
 * Created by leongv on 8/15/2016.
 */
public class ChapterCard {

    public interface OnClickListener extends View.OnClickListener {
        void onClick(View v, ChapterCardAdapter.ViewHolder vh, List<Integer> expandedCards, int position);
    }

    // Constants
    public int MIN_CHECKING_LEVEL = 0;
    public int MAX_CHECKING_LEVEL = 3;
    public int MIN_PROGRESS = 0;
    public int MAX_PROGRESS = 100;

    // Attributes
    private Activity mCtx;
    private Project mProject;
    private SoftReference<AudioPlayer> mAudioPlayer;
    private String mTitle = "";
    private int mCheckingLevel = 0;
    private int mProgress = 0;

    // State
    private boolean mIsEmpty = true;
    private boolean mIsCompiled = false;
    private boolean mIsExpanded = false;

    // Constructor
    public ChapterCard(Activity ctx, Project proj) {
        mCtx = ctx;
        mProject = proj;
    }

    // Setters
    public void setTitle(String title) {
        mTitle = title;
    }

    public void setCheckingLevel(int level) {
        if (level < MIN_CHECKING_LEVEL) {
            mCheckingLevel = MIN_CHECKING_LEVEL;
        } else if (level > MAX_CHECKING_LEVEL) {
            mCheckingLevel = MAX_CHECKING_LEVEL;
        } else {
            mCheckingLevel = level;
        }
    }

    public void setProgress(int progress) {
        if (progress < MIN_PROGRESS) {
            mProgress = MIN_PROGRESS;
        } else if (progress > MAX_PROGRESS) {
            mProgress = MAX_PROGRESS;
        } else {
            mProgress = progress;
        }
    }

    // Getters
    public String getTitle() {
        return mTitle;
    }

    public int getCheckingLevel() {
        return mCheckingLevel;
    }

    public int getProgress() {
        return mProgress;
    }

    public boolean isEmpty() {
        return mIsEmpty;
    }

    public boolean isCompiled() {
        return mIsCompiled;
    }

    public boolean isExpanded() {
        return mIsExpanded;
    }

    // Public API
    public void expand(ChapterCardAdapter.ViewHolder vh) {
        // refreshAudioPlayer(vh);
        vh.mCardBody.setVisibility(View.VISIBLE);
        vh.mExpandBtn.setActivated(true);
        mIsExpanded = true;
    }

    public void collapse(ChapterCardAdapter.ViewHolder vh) {
        vh.mCardBody.setVisibility(View.GONE);
        vh.mExpandBtn.setActivated(false);
        mIsExpanded = false;
    }

    public void raise(ChapterCardAdapter.ViewHolder vh) {
         vh.mCardView.setCardElevation(8f);
         vh.mCardContainer.setBackgroundColor(mCtx.getResources().getColor(R.color.accent));
         vh.mTitle.setTextColor(mCtx.getResources().getColor(R.color.text_light));
         vh.mActions.setEnabled(false);
    }

    public void drop(ChapterCardAdapter.ViewHolder vh) {
         vh.mCardView.setCardElevation(2f);
         vh.mCardContainer.setBackgroundColor(mCtx.getResources().getColor(R.color.card_bg));
         vh.mTitle.setTextColor(
                 mCtx.getResources().getColor(R.color.primary_text_default_material_light)
         );
         vh.mActions.setEnabled(true);
    }

    public View.OnClickListener getCheckLevelOnClick(ChapterCardAdapter.ViewHolder vh) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("Check level");
            }
        };
    }

    public View.OnClickListener getCompileOnClick(ChapterCardAdapter.ViewHolder vh) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("Compile");
            }
        };
    }

    public View.OnClickListener getRecordOnClick(ChapterCardAdapter.ViewHolder vh) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("Record");
            }
        };
    }

    public View.OnClickListener getExpandOnClick(final ChapterCardAdapter.ViewHolder vh) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("Expand");
                if (mIsExpanded) {
                    collapse(vh);
                } else {
                    expand(vh);
                }
            }
        };
    }

    public View.OnClickListener getDeleteOnClick(ChapterCardAdapter.ViewHolder vh) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("Delete");
            }
        };
    }

    public View.OnClickListener getPlayPauseOnClick(ChapterCardAdapter.ViewHolder vh) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("Play/Pause");
            }
        };
    }

}
