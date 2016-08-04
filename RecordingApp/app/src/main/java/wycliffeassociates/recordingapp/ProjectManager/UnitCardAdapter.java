package wycliffeassociates.recordingapp.ProjectManager;

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
            notifyDataSetChanged();
        }
    };

    public class ViewHolder extends SwappingHolder implements View.OnClickListener, View.OnLongClickListener {

        public RelativeLayout mCardHeader, mCardFooter;
        public TextView mUnitTitle, mCurrentTake;
        public LinearLayout mCardBody, mCardContainer;
        public ImageView mUnitRecordBtn, mUnitPlayBtn, mPrevTakeBtn, mNextTakeBtn;
        public ImageButton mDeleteTakeBtn, mPlayTakeBtn, mEditTakeBtn;
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

            // Buttons
            mUnitRecordBtn = (ImageView) view.findViewById(R.id.unitRecordBtn);
            mUnitPlayBtn = (ImageView) view.findViewById(R.id.unitPlayBtn);
            mDeleteTakeBtn = (ImageButton) view.findViewById(R.id.deleteTakeBtn);
            mPlayTakeBtn = (ImageButton) view.findViewById(R.id.playTakeBtn);
            mEditTakeBtn = (ImageButton) view.findViewById(R.id.editTakeBtn);
            mPrevTakeBtn = (ImageView) view.findViewById(R.id.prevTakeBtn);
            mNextTakeBtn = (ImageView) view.findViewById(R.id.nextTakeBtn);

            view.setOnClickListener(this);
            view.setOnLongClickListener(this);
            view.setLongClickable(true);
        }

        // Called on onBindViewHolder
        public void  bindViewHolder(ViewHolder holder, int position, UnitCard unitCard) {
            mUnitCard = unitCard;
            mUnitTitle.setText(unitCard.getTitle());
            mPosition = position;
            setListeners(mUnitCard, this, position);
            if (mExpandedCards.contains(position)) {
                unitCard.expand(holder.mCardBody, holder.mCardFooter, holder.mUnitPlayBtn);
            } else {
                unitCard.collapse(holder.mCardBody, holder.mCardFooter, holder.mUnitPlayBtn);
            }
        }

        @Override
        public void onClick(View view) {
            if (mUnitCard == null) {
                return;
            }
            if(mMultiSelector.isSelectable()) {
                if(mUnitCard.isExpanded()){
                    toggleExpansion(this, mExpandedCards, mPosition);
                }
                mMultiSelector.tapSelection(this);
            } else {
                toggleExpansion(this, mExpandedCards, mPosition);
            }
        }

        @Override
        public boolean onLongClick(View view) {
            AppCompatActivity activity = (AppCompatActivity) mCtx;
            activity.startSupportActionMode(mMultiSelectMode);
            mMultiSelector.setSelected(this, true);
            if(mUnitCard.isExpanded()){
                toggleExpansion(this, mExpandedCards, mPosition);
            }
            return true;
        }
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
        System.out.println("onBindViewHolder: " + position + " " + mExpandedCards);
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
        System.out.println("Card Header Click");
        if (!vh.mUnitCard.isExpanded()) {
            //vh.setIsRecyclable(false);
            vh.mUnitCard.expand(vh.mCardBody, vh.mCardFooter, vh.mUnitPlayBtn);
            if (!expandedCards.contains(position)) {
                expandedCards.add(position);
            }
        } else {
            //vh.setIsRecyclable(true);
            vh.mUnitCard.collapse(vh.mCardBody, vh.mCardFooter, vh.mUnitPlayBtn);
            if (expandedCards.contains(position)) {
                expandedCards.remove(expandedCards.indexOf(position));
            }
        }
    }

    // Set listeners for unit and take actions
    private void setListeners(final UnitCard unitCard, final ViewHolder holder, final int position) {

        holder.mUnitRecordBtn.setOnClickListener(unitCard.getUnitRecordOnClick(mProject, mChapterNum));

        holder.mUnitPlayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("Play Unit");
            }
        });

        holder.mDeleteTakeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("Delete Take");
            }
        });

        holder.mPlayTakeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("Play Take");
            }
        });

        holder.mEditTakeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("Edit Take");
            }
        });

        holder.mNextTakeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("Next Take");
            }
        });

        holder.mPrevTakeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("Previous Take");
            }
        });
    };

}
