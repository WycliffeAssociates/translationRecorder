package org.wycliffeassociates.translationrecorder.ProjectManager.adapters;

import android.os.Build;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bignerdranch.android.multiselector.MultiSelector;
import com.bignerdranch.android.multiselector.SwappingHolder;

import org.wycliffeassociates.translationrecorder.R;
import org.wycliffeassociates.translationrecorder.database.ProjectDatabaseHelper;
import org.wycliffeassociates.translationrecorder.project.Project;
import org.wycliffeassociates.translationrecorder.project.TakeInfo;
import org.wycliffeassociates.translationrecorder.widgets.FourStepImageView;
import org.wycliffeassociates.translationrecorder.widgets.OnCardExpandedListener;
import org.wycliffeassociates.translationrecorder.widgets.UnitCard;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by leongv on 7/28/2016.
 */
public class UnitCardAdapter extends RecyclerView.Adapter<UnitCardAdapter.ViewHolder> implements UnitCard.DatabaseAccessor, OnCardExpandedListener {

    private AppCompatActivity mCtx;
    private RecyclerView recyclerView;
    private Project mProject;
    private int mChapterNum;
    private List<UnitCard> mUnitCardList;
    private List<Integer> mExpandedCards = new ArrayList<>();
    private List<ViewHolder> mSelectedCards = new ArrayList<>();
    private MultiSelector mMultiSelector = new MultiSelector();
    private ActionMode mActionMode;
    private ProjectDatabaseHelper db;



    // Constructor
    public UnitCardAdapter(
            AppCompatActivity context,
            Project project,
            int chapter,
            List<UnitCard> unitCardList,
            ProjectDatabaseHelper db
    ) {
        mUnitCardList = unitCardList;
        mCtx = context;
        mProject = project;
        mChapterNum = chapter;
        this.db = db;
    }

    @Override
    public void updateSelectedTake(TakeInfo takeInfo) {
        db.setSelectedTake(takeInfo);
    }

    @Override
    public int selectedTakeNumber(TakeInfo takeInfo) {
        int selectedTakeNumber = db.getSelectedTakeNumber(takeInfo);
        return selectedTakeNumber;
    }

    @Override
    public int takeRating(TakeInfo takeInfo) {
        int takeRating = db.getTakeRating(takeInfo);
        return takeRating;
    }

    @Override
    public int takeCount(Project project, int chapter, int firstVerse) {
        int takeCount = 0;
        if (db.chapterExists(mProject, chapter) && db.unitExists(mProject, chapter, firstVerse)) {
            int unitId = db.getUnitId(project, chapter, firstVerse);
            takeCount = db.getTakeCount(unitId);
        }
        return takeCount;
    }

    @Override
    public void deleteTake(TakeInfo takeInfo) {
        db.deleteTake(takeInfo);
    }

    @Override
    public void removeSelectedTake(TakeInfo takeInfo) {
        db.removeSelectedTake(takeInfo);
    }

    @Override
    public void selectTake(TakeInfo takeInfo) {
        db.setSelectedTake(takeInfo);
    }

    @Override
    public void onCardExpanded(int position) {
        recyclerView.getLayoutManager().scrollToPosition(position);
    }

//    private ActionMode.Callback mMultiSelectMode = new ModalMultiSelectorCallback(mMultiSelector) {
//
//        @Override
//        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
//            mMultiSelector.clearSelections();
//            mMultiSelector.setSelectable(true);
//            return false;
//        }
//
//        @Override
//        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
//            mCtx.getMenuInflater().inflate(R.menu.unit_menu, menu);
//            return true;
//        }
//
//        @Override
//        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
//            switch (item.getItemId()) {
//                case R.id.set_units_checking_level:
//                    System.out.println("Placeholder: Set checking level for selected units");
//                    break;
//                default:
//                    System.out.println("Default action");
//                    break;
//            }
//            return false;
//        }
//
//        @Override
//        public void onDestroyActionMode(ActionMode actionMode) {
//            mMultiSelector.setSelectable(false);
//            mMultiSelector.clearSelections();
//            for (ViewHolder vh : mSelectedCards) {
//                vh.unitCard.drop();
//            }
//            mSelectedCards.clear();
//        }
//    };


    public class ViewHolder extends SwappingHolder implements View.OnClickListener,
            View.OnLongClickListener {

        public RelativeLayout cardHeader, cardFooter;
        public SeekBar seekBar;
        public TextView unitTitle, currentTake, elapsed, duration, currentTakeTimeStamp, takeCount;
        public LinearLayout takeCountContainer, cardBody, cardContainer, unitActions;
        public ImageView unitRecordBtn, unitExpandBtn, prevTakeBtn, nextTakeBtn;
        public ImageButton takeDeleteBtn, takePlayPauseBtn, takeEditBtn, takeSelectBtn;
        public FourStepImageView takeRatingBtn;
        public UnitCard unitCard;
        public CardView cardView;

        public ViewHolder(View view) {
            super(view, mMultiSelector);
            // Containers
            cardView = (CardView) view.findViewById(R.id.unitCard);
            cardContainer = (LinearLayout) view.findViewById(R.id.unitCardContainer);
            cardHeader = (RelativeLayout) view.findViewById(R.id.cardHeader);
            cardBody = (LinearLayout) view.findViewById(R.id.cardBody);
            cardFooter = (RelativeLayout) view.findViewById(R.id.cardFooter);
            unitActions = (LinearLayout) view.findViewById(R.id.unitActions);

            // Views
            unitTitle = (TextView) view.findViewById(R.id.unitTitle);
            takeCountContainer = (LinearLayout) view.findViewById(R.id.take_count_container);
            takeCount = (TextView) view.findViewById(R.id.take_count);
            currentTake = (TextView) view.findViewById(R.id.currentTakeView);
            currentTakeTimeStamp = (TextView) view.findViewById(R.id.currentTakeTimeStamp);
            seekBar = (SeekBar) view.findViewById(R.id.seekBar);
            elapsed = (TextView) view.findViewById(R.id.timeElapsed);
            duration = (TextView) view.findViewById(R.id.timeDuration);
            currentTakeTimeStamp = (TextView) view.findViewById(R.id.currentTakeTimeStamp);

            // Buttons
            takeRatingBtn = (FourStepImageView) view.findViewById(R.id.rateTakeBtn);
            unitRecordBtn = (ImageView) view.findViewById(R.id.unitRecordBtn);
            unitExpandBtn = (ImageView) view.findViewById(R.id.unitExpandBtn);
            takeDeleteBtn = (ImageButton) view.findViewById(R.id.deleteTakeBtn);
            takePlayPauseBtn = (ImageButton) view.findViewById(R.id.playTakeBtn);
            takeEditBtn = (ImageButton) view.findViewById(R.id.editTakeBtn);
            takeSelectBtn = (ImageButton) view.findViewById(R.id.selectTakeBtn);
            prevTakeBtn = (ImageView) view.findViewById(R.id.prevTakeBtn);
            nextTakeBtn = (ImageView) view.findViewById(R.id.nextTakeBtn);

            view.setOnClickListener(this);
            view.setOnLongClickListener(this);
            view.setLongClickable(true);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                setSelectionModeStateListAnimator(null);
                setDefaultModeStateListAnimator(null);
            }
            setSelectionModeBackgroundDrawable(null);
            setDefaultModeBackgroundDrawable(null);
        }

        // Called on onBindViewHolder, when the view is visible on the screen
        public void bindViewHolder(ViewHolder holder, int position, UnitCard uc) {
            // Capture the UnitCard object
            unitCard = uc;
            unitCard.setViewHolder(holder);
            // Set card views based on the UnitCard object
            unitTitle.setText(unitCard.getTitle());
            unitCard.refreshTakeCount();
            takeCount.setText(String.valueOf(unitCard.getTakeCount()));
            // Expand card if it's already expanded before
            if (unitCard.isExpanded()) {
                unitCard.expand();
            } else {
                unitCard.collapse();
            }
            // Raise card, and show appropriate visual cue, if it's already selected
            if (mMultiSelector.isSelected(position, 0)) {
                mSelectedCards.add(this);
                unitCard.raise(mCtx);
            } else {
                mSelectedCards.remove(this);
                unitCard.drop(mCtx);
            }
            // Hide expand icon if it's empty
            if (unitCard.isEmpty()) {
                unitExpandBtn.setVisibility(View.INVISIBLE);
            } else {
                unitExpandBtn.setVisibility(View.VISIBLE);
            }

            setListeners(unitCard, this);
        }

        @Override
        public void onClick(View view) {
            // NOTE: There is no action that needs multi-select at unit level at this point
//            if (unitCard == null) {
//                return;
//            }
//
//            if(mMultiSelector.isSelectable() && !unitCard.isEmpty()) {
//
//                // Close card if it is expanded in multi-select mode
//                if(unitCard.isExpanded()){
//                    toggleExpansion(this, mExpandedCards);
//                }
//
//                // Select/de-select item
//                mMultiSelector.tapSelection(this);
//
//                // Raise/drop card
//                if (mMultiSelector.isSelected(this.getAdapterPosition(), 0)) {
//                    mSelectedCards.add(this);
//                    unitCard.raise();
//                } else {
//                    mSelectedCards.remove(this);
//                    unitCard.drop();
//                }
//
//                // Finish action mode if all cards are de-selected
//                if (mActionMode != null && mSelectedCards.size() <= 0) {
//                    mActionMode.finish();
//                }
//            }
        }

        @Override
        public boolean onLongClick(View view) {
            // NOTE: There is no action that needs multi-select at unit level at this point
//            if (!unitCard.isEmpty()) {
//                mActionMode = mCtx.startSupportActionMode(mMultiSelectMode);
//                mMultiSelector.setSelected(this, true);
//
//                // Close card if it is expanded on entering multi-select mode
//                if(unitCard.isExpanded()){
//                    toggleExpansion(this.getAdapterPosition(), mExpandedCards);
//                }
//
//                mSelectedCards.add(this);
//                unitCard.raise();
//            }
            return true;
        }

        public void pausePlayers() {
            for(UnitCard uc: mUnitCardList) {
                if (uc.isExpanded()) {
                    uc.pauseAudio();
                }
            }
        }
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.unit_card, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        UnitCard unitCard = mUnitCardList.get(position);
        holder.bindViewHolder(holder, position, unitCard);
    }

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

    private void setListeners(final UnitCard unitCard, final ViewHolder holder) {
        int position = holder.getAdapterPosition();
        holder.unitRecordBtn.setOnClickListener(unitCard.getUnitRecordOnClick(mCtx, db));
        holder.unitExpandBtn.setOnClickListener(unitCard.getUnitExpandOnClick(position, mExpandedCards, this));
        holder.takeDeleteBtn.setOnClickListener(unitCard.getTakeDeleteOnClick(mCtx, this, position, this));
        holder.takePlayPauseBtn.setOnClickListener(unitCard.getTakePlayPauseOnClick());
        holder.takeEditBtn.setOnClickListener(unitCard.getTakeEditOnClickListener());
        holder.takeRatingBtn.setOnClickListener(unitCard.getTakeRatingOnClick(mCtx));
        holder.takeSelectBtn.setOnClickListener(unitCard.getTakeSelectOnClick(this));
        holder.nextTakeBtn.setOnClickListener(unitCard.getTakeIncrementOnClick());
        holder.prevTakeBtn.setOnClickListener(unitCard.getTakeDecrementOnClick());
    }

    public void exitCleanUp() {
        for (UnitCard uc : mUnitCardList) {
            if (uc.isExpanded()) {
                uc.destroyAudioPlayer();
            }
        }
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
    }

    public UnitCard getItem(int id) {
        return mUnitCardList.get(id);
    }

}
