package wycliffeassociates.recordingapp.ProjectManager;

import android.animation.StateListAnimator;
import android.graphics.drawable.Drawable;
import android.os.Build;
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
import wycliffeassociates.recordingapp.widgets.FourStepImageView;
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
    private ActionMode mActionMode;


    // Constructor
    public UnitCardAdapter(AppCompatActivity context, Project project, int chapter, List<UnitCard> unitCardList) {
        mUnitCardList = unitCardList;
        mCtx = context;
        mProject = project;
        mChapterNum = chapter;
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
        public TextView unitTitle, currentTake, elapsed, duration, currentTakeTimeStamp;
        public LinearLayout cardBody, cardContainer, unitActions;
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
        public void  bindViewHolder(ViewHolder holder, int position, UnitCard uc) {
            // Capture the UnitCard object
            unitCard = uc;
            unitCard.setViewHolder(holder);
            // Set card views based on the UnitCard object
            unitTitle.setText(unitCard.getTitle());
            // Expand card if it's already expanded before
            if (unitCard.isExpanded()) {
                unitCard.expand();
            } else {
                unitCard.collapse();
            }
            // Raise card, and show appropriate visual cue, if it's already selected
            if (mMultiSelector.isSelected(position, 0)) {
                mSelectedCards.add(this);
                unitCard.raise();
            } else {
                mSelectedCards.remove(this);
                unitCard.drop();
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
        holder.unitRecordBtn.setOnClickListener(unitCard.getUnitRecordOnClick());
        holder.unitExpandBtn.setOnClickListener(unitCard.getUnitExpandOnClick(position, mExpandedCards));
        holder.takeDeleteBtn.setOnClickListener(unitCard.getTakeDeleteOnClick(position, this));
        holder.takePlayPauseBtn.setOnClickListener(unitCard.getTakePlayPauseOnClick());
        holder.takeEditBtn.setOnClickListener(unitCard.getTakeEditOnClickListener());
        holder.takeRatingBtn.setOnClickListener(unitCard.getTakeRatingOnClick());
        holder.takeSelectBtn.setOnClickListener(unitCard.getTakeSelectOnClick());
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

}
