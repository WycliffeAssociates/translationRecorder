package wycliffeassociates.recordingapp.widgets;

import android.content.Context;
import android.support.v4.util.Pair;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.LinearLayout;

import java.util.List;

import wycliffeassociates.recordingapp.ProjectManager.Project;
import wycliffeassociates.recordingapp.ProjectManager.UnitCardAdapter;
import wycliffeassociates.recordingapp.R;
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
    private boolean mIsSelected = false;

    // Attributes
    private String mTitle;
    private Context mCtx;


    // Constructors
    public UnitCard(Context ctx, String mode, String firstVerse) {
        this.init();
        mTitle = mode + " " + firstVerse;
        mFirstVerse = firstVerse;
        mCtx = ctx;
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

    public void raise(CardView card, LinearLayout container) {
        System.out.println("Raise");
        card.setCardElevation(12f);
        container.setBackgroundColor(mCtx.getResources().getColor(R.color.accent));
        mIsSelected = true;
    }

    public void drop(CardView card, LinearLayout container) {
        System.out.println("Drop");
        card.setCardElevation(2f);
        container.setBackgroundColor(mCtx.getResources().getColor(R.color.card_bg));
        mIsSelected = false;
    }

//    public void toggleRaiseDrop(CardView card) {
//        System.out.println("Toggle Raise/Drop");
//        if (mIsSelected) {
//            drop(card);
//        } else
//            raise(card);
//    }

    public boolean isExpanded() {
        return mIsExpanded;
    }

    public boolean isSelected() { return mIsSelected; }

    public void setSelected(boolean isSelected) { mIsSelected = isSelected; }

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
