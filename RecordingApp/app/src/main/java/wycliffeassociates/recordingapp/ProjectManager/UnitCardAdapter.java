package wycliffeassociates.recordingapp.ProjectManager;

import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bignerdranch.android.multiselector.ModalMultiSelectorCallback;
import com.bignerdranch.android.multiselector.MultiSelector;
import com.bignerdranch.android.multiselector.SwappingHolder;

import java.util.ArrayList;
import java.util.List;

import wycliffeassociates.recordingapp.R;
import wycliffeassociates.recordingapp.widgets.UnitCard;

/**
 * Created by leongv on 7/28/2016.
 */
public class UnitCardAdapter extends RecyclerView.Adapter<UnitCardAdapter.ViewHolder> {

    private AppCompatActivity mCtx;
    private Project mProject;
    private int mChapterNum;
    private List<UnitCard> mUnitCardList;
    private List<Integer> mExpandedCards = new ArrayList<>();
    private List<ViewHolder> mSelectedCards = new ArrayList<>();
    private MultiSelector mMultiSelector = new MultiSelector();


    // Constructor
    public UnitCardAdapter(AppCompatActivity context, Project project, int chapter, List<UnitCard> unitCardList) {
        mUnitCardList = unitCardList;
        mCtx = context;
        mProject = project;
        mChapterNum = chapter;
    }


    private ActionMode.Callback mMultiSelectMode = new ModalMultiSelectorCallback(mMultiSelector) {

        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            mMultiSelector.clearSelections();
            mMultiSelector.setSelectable(true);
            return false;
        }

        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            mCtx.getMenuInflater().inflate(R.menu.unit_menu, menu);
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            System.out.println("onActionItemClicked");
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode actionMode) {
            mMultiSelector.setSelectable(false);
            mMultiSelector.clearSelections();
            for (ViewHolder vh : mSelectedCards) {
                vh.mUnitCard.drop(vh);
            }
            mSelectedCards.clear();
            notifyDataSetChanged();
        }
    };


    public class ViewHolder extends SwappingHolder implements View.OnClickListener, View.OnLongClickListener {

        public RelativeLayout mCardHeader, mCardFooter;
        public TextView mUnitTitle, mCurrentTake, mProgress, mDuration;
        public LinearLayout mCardBody, mCardContainer;
        public ImageView mUnitRecordBtn, mUnitPlayBtn, mPrevTakeBtn, mNextTakeBtn;
        public ImageButton mDeleteTakeBtn, mPlayTakeBtn, mEditTakeBtn, mPauseTakeBtn;
        public SeekBar mSeekBar;
        public UnitCard mUnitCard;
        public CardView mCardView;
        public int mPosition;

        public ViewHolder(View view) {
            super(view, mMultiSelector);
            // Containers
            mCardView = (CardView) view.findViewById(R.id.unitCard);
            mCardContainer = (LinearLayout) view.findViewById(R.id.unitCardContainer);
            mCardHeader = (RelativeLayout) view.findViewById(R.id.cardHeader);
            mCardBody = (LinearLayout) view.findViewById(R.id.cardBody);
            mCardFooter = (RelativeLayout) view.findViewById(R.id.cardFooter);

            // Views
            mUnitTitle = (TextView) view.findViewById(R.id.unitTitle);
            mCurrentTake = (TextView) view.findViewById(R.id.currentTakeView);
            mSeekBar = (SeekBar) view.findViewById(R.id.seekBar);
            mProgress = (TextView) view.findViewById(R.id.timeElapsed);
            mDuration = (TextView) view.findViewById(R.id.timeDuration);

            // Buttons
            mUnitRecordBtn = (ImageView) view.findViewById(R.id.unitRecordBtn);
            mUnitPlayBtn = (ImageView) view.findViewById(R.id.unitPlayBtn);
            mDeleteTakeBtn = (ImageButton) view.findViewById(R.id.deleteTakeBtn);
            mPlayTakeBtn = (ImageButton) view.findViewById(R.id.playTakeBtn);
            mEditTakeBtn = (ImageButton) view.findViewById(R.id.editTakeBtn);
            mPrevTakeBtn = (ImageView) view.findViewById(R.id.prevTakeBtn);
            mNextTakeBtn = (ImageView) view.findViewById(R.id.nextTakeBtn);
            mPauseTakeBtn = (ImageButton) view.findViewById(R.id.pauseTakeBtn);

            view.setOnClickListener(this);
            view.setOnLongClickListener(this);
            view.setLongClickable(true);
        }

        // Called on onBindViewHolder, when the view is visible on the screen
        public void  bindViewHolder(ViewHolder holder, int position, UnitCard unitCard) {
            // Capture the UnitCard object
            mUnitCard = unitCard;

            // Set card views based on the UnitCard object
            mUnitTitle.setText(unitCard.getTitle());
            mPosition = position;
            setListeners(mUnitCard, this);

            // Expand card if it's already expanded before
            if (mExpandedCards.contains(position)) {
                unitCard.expand(holder);
            } else {
                unitCard.collapse(holder);
            }

            // Raise card, and show appropriate visual cue, if it's already selected
            if (mMultiSelector.isSelected(position, 0)) {
                mSelectedCards.add(this);
                unitCard.raise(holder);
            } else {
                mSelectedCards.remove(this);
                unitCard.drop(holder);
            }
        }

        @Override
        public void onClick(View view) {
            if (mUnitCard == null) {
                return;
            }

            if(mMultiSelector.isSelectable()) {
                // Close card if it is expanded in multi-select mode
                if(mUnitCard.isExpanded()){
                    toggleExpansion(this, mExpandedCards, mPosition);
                }

                // Select/de-select item
                mMultiSelector.tapSelection(this);

                // Raise/drop card
                if (mMultiSelector.isSelected(mPosition, 0)) {
                    mSelectedCards.add(this);
                    mUnitCard.raise(this);
                } else {
                    mSelectedCards.remove(this);
                    mUnitCard.drop(this);
                }

            } else {
                toggleExpansion(this, mExpandedCards, mPosition);
            }
        }

        @Override
        public boolean onLongClick(View view) {
            mCtx.startSupportActionMode(mMultiSelectMode);
            mMultiSelector.setSelected(this, true);

            // Close card if it is expanded on entering multi-select mode
            if(mUnitCard.isExpanded()){
                toggleExpansion(this, mExpandedCards, mPosition);
            }

            mSelectedCards.add(this);
            mUnitCard.raise(this);
            return true;
        }

        @Override
        public void setDefaultModeBackgroundDrawable(Drawable defaultModeBackgroundDrawable) {}

        @Override
        public void setSelectionModeBackgroundDrawable(Drawable selectionModeBackgroundDrawable) {}
    }


    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        System.out.println("onCreateViewHolder");
        View v = LayoutInflater.from(parent.getContext())
                               .inflate(R.layout.unit_card, parent, false);
        // Set the view's size, margins, padding and layout params here
        return new ViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        UnitCard unitCard = mUnitCardList.get(position);
        holder.bindViewHolder(holder, position, unitCard);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mUnitCardList.size();
    }

    public List<UnitCard> getSelectedCards() {
        List<UnitCard> cards = new ArrayList<>();
        for (int i = getItemCount(); i >= 0; i--) {
            if (mMultiSelector.isSelected(i, 0)) {
                cards.add(mUnitCardList.get(i));
            }
        }
        return cards;
    }


    public void toggleExpansion(final UnitCardAdapter.ViewHolder vh, final List<Integer> expandedCards, final int position) {
        if (!vh.mUnitCard.isExpanded()) {
            vh.mUnitCard.expand(vh);
            if (!expandedCards.contains(position)) {
                expandedCards.add(position);
            }
        } else {
            vh.mUnitCard.collapse(vh);
            if (expandedCards.contains(position)) {
                expandedCards.remove(expandedCards.indexOf(position));
            }
        }
    }

    // Set listeners for unit and take actions
    private void setListeners(final UnitCard unitCard, final ViewHolder holder) {

        holder.mUnitRecordBtn.setOnClickListener(unitCard.getUnitRecordOnClick(mProject, mChapterNum));

        holder.mUnitPlayBtn.setOnClickListener(unitCard.getPlayLatestTakeOnClickListener());

        holder.mDeleteTakeBtn.setOnClickListener(unitCard.getDeleteTakeOnClickListener(holder));

        holder.mPlayTakeBtn.setOnClickListener(unitCard.getPlayTakeOnClickListener(holder));

        holder.mPauseTakeBtn.setOnClickListener(unitCard.getPauseTakeOnClickListener(holder));

        holder.mEditTakeBtn.setOnClickListener(unitCard.getEditOnClickListener());

        holder.mNextTakeBtn.setOnClickListener(unitCard.getIncrementTakeOnClickListener(holder.mCurrentTake, holder));

        holder.mPrevTakeBtn.setOnClickListener(unitCard.getDecrementTakeOnClickListener(holder.mCurrentTake, holder));
    };

}
