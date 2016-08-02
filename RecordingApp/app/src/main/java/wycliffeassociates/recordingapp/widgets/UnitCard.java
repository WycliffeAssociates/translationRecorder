package wycliffeassociates.recordingapp.widgets;

import android.support.v4.util.Pair;
import android.view.View;

import java.util.List;

import wycliffeassociates.recordingapp.ProjectManager.Project;
import wycliffeassociates.recordingapp.ProjectManager.UnitCardAdapter;
import wycliffeassociates.recordingapp.Recording.RecordingScreen;

/**
 * Created by leongv on 7/28/2016.
 */
//public class UnitCard extends FrameLayout{
public class UnitCard {

    private final String mFirstVerse;

    public interface OnClickListener extends View.OnClickListener{
        void onClick(View v, UnitCardAdapter.ViewHolder vh, List<Integer> expandedCards, int position);
    }

    // State
    private boolean mIsExpanded = false;

    // Attributes
    private String mTitle;


    // Constructors
    public UnitCard(String mode, String firstVerse) {
        this.init();
        mTitle = mode + " " + firstVerse;
        mFirstVerse = firstVerse;
    }


    // Private API
    private void init() {

    }


    // Public API
    public void setTitle(String title) {
        mTitle = title;
    }

    public String getTitle() {
        return mTitle;
    }

    public void toggleExpansion(View body, View footer, View unitPlayBtn) {
        if (mIsExpanded) {
            this.collapse(body, footer, unitPlayBtn);
        } else {
            this.expand(body, footer, unitPlayBtn);
        }
    }

    public void expand(View body, View footer, View unitPlayBtn) {
        System.out.println("Expand");
        body.setVisibility(View.VISIBLE);
        footer.setVisibility(View.VISIBLE);
        unitPlayBtn.setVisibility(View.GONE);
        mIsExpanded = true;
    }

    public void collapse(View body, View footer, View unitPlayBtn) {
        System.out.println("Collapse");
        body.setVisibility(View.GONE);
        footer.setVisibility(View.GONE);
        unitPlayBtn.setVisibility(View.VISIBLE);
        mIsExpanded = false;
    }

    public boolean isExpanded() {
        return mIsExpanded;
    }

    public View.OnClickListener createHeaderOnClick(final UnitCardAdapter.ViewHolder vh, final List<Integer> expandedCards, final int position) {
        return (new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("Card Header Click");
                if (!mIsExpanded) {
                    vh.setIsRecyclable(false);
                    expand(vh.mCardBody, vh.mCardFooter, vh.mUnitPlayBtn);
                    if (!expandedCards.contains(position)) {
                        expandedCards.add(position);
                    }
                } else {
                    vh.setIsRecyclable(true);
                    collapse(vh.mCardBody, vh.mCardFooter, vh.mUnitPlayBtn);
                    if (expandedCards.contains(position)) {
                        expandedCards.remove(expandedCards.indexOf(position));
                    }
                }
            }
        });
    }

    public View.OnClickListener getUnitRecordOnClick(final Project project, final int chapter) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("Record Unit");
                Project.loadProjectIntoPreferences(view.getContext(), project);
                int startVerse = Integer.parseInt(mFirstVerse);
                view.getContext().startActivity(RecordingScreen.getNewRecordingIntent(view.getContext(), project, chapter, startVerse));
            }
        };
    }

}
