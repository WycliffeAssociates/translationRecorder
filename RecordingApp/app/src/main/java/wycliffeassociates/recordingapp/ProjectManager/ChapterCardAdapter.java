package wycliffeassociates.recordingapp.ProjectManager;

import android.content.Intent;
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
import android.widget.Toast;

import com.bignerdranch.android.multiselector.ModalMultiSelectorCallback;
import com.bignerdranch.android.multiselector.MultiSelector;
import com.bignerdranch.android.multiselector.SwappingHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import wycliffeassociates.recordingapp.R;
import wycliffeassociates.recordingapp.Recording.RecordingScreen;
import wycliffeassociates.recordingapp.widgets.ChapterCard;
import wycliffeassociates.recordingapp.widgets.FourStepImageView;

/**
 * Created by leongv on 8/15/2016.
 */
public class ChapterCardAdapter extends RecyclerView.Adapter<ChapterCardAdapter.ViewHolder> {

    private int MULTI_CHECK_LEVEL_BTN = 0;
    private int MULTI_COMPILE_BTN = 1;

    private AppCompatActivity mCtx;
    private Project mProject;
    private List<ChapterCard> mChapterCardList;
    private List<Integer> mExpandedCards = new ArrayList<>();
    private List<Integer> mSelectedCards = new ArrayList<>();
    private MultiSelector mMultiSelector = new MultiSelector();
    private ActionMode mActionMode;


    // Constructor
    public ChapterCardAdapter(AppCompatActivity context, Project project, List<ChapterCard> chapterCardList) {
        mCtx = context;
        mProject = project;
        mChapterCardList = chapterCardList;
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
            mCtx.getMenuInflater().inflate(R.menu.chapter_menu, menu);
            setIconsClickable(false);
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            int[] chapters = new int[mSelectedCards.size()];
            for(int i = 0; i < mSelectedCards.size(); i++){
                chapters[i] = mSelectedCards.get(i) + 1;
            }
            switch (item.getItemId()) {
                case R.id.chapters_checking_level:
                    // NOTE: Currently only pass in placeholder text. Replace with real chapter names.
                    CheckingDialog dialog = CheckingDialog.newInstance(mProject, chapters);
                    dialog.show(mCtx.getFragmentManager(), "multi_chapter_checking_level");
                    break;
                case R.id.chapters_compile:
                    CompileDialog compileDialog = CompileDialog.newInstance(mProject, chapters);
                    compileDialog.show(mCtx.getFragmentManager(), "multi_chapter_compile");
                    break;
                default:
                    System.out.println("Default action");
                    break;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode actionMode) {
            mMultiSelector.setSelectable(false);
            mMultiSelector.clearSelections();
            for (Integer i : mSelectedCards) {
                notifyItemChanged(i);
            }
            mSelectedCards.clear();
            setIconsClickable(true);
        }

    };


    public class ViewHolder extends SwappingHolder implements View.OnClickListener,
            View.OnLongClickListener {

        public ChapterCard mChapterCard;
        public CardView mCardView;
        public RelativeLayout mCardHeader;
        public LinearLayout mCardBody, mCardContainer, mActions;
        public TextView mTitle, mElapsed, mDuration;
        public ImageView mRecordBtn, mCompileBtn, mExpandBtn;
        public ImageButton mDeleteBtn, mPlayPauseBtn;
        public FourStepImageView mCheckLevelBtn;
        public SeekBar mSeekBar;

        public ViewHolder(View view) {
            super(view, mMultiSelector);
            // Containers
            mCardView = (CardView) view.findViewById(R.id.chapter_card);
            mCardContainer = (LinearLayout) view.findViewById(R.id.chapter_card_container);
            mCardHeader = (RelativeLayout) view.findViewById(R.id.card_header);
            mCardBody = (LinearLayout) view.findViewById(R.id.card_body);
            mActions = (LinearLayout) view.findViewById(R.id.actions);

            // Views
            mTitle = (TextView) view.findViewById(R.id.title);
            mSeekBar = (SeekBar) view.findViewById(R.id.seek_bar);
            mElapsed = (TextView) view.findViewById(R.id.time_elapsed);
            mDuration = (TextView) view.findViewById(R.id.time_duration);

            // Buttons
            mCheckLevelBtn = (FourStepImageView) view.findViewById(R.id.check_level_btn);
            mRecordBtn = (ImageView) view.findViewById(R.id.record_btn);
            mCompileBtn = (ImageView) view.findViewById(R.id.compile_btn);
            mExpandBtn = (ImageView) view.findViewById(R.id.expand_btn);
            mDeleteBtn = (ImageButton) view.findViewById(R.id.delete_chapter_audio_btn);
            mPlayPauseBtn = (ImageButton) view.findViewById(R.id.play_pause_chapter_btn);

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
        public void  bindViewHolder(ViewHolder holder, int position, ChapterCard chapterCard) {
            // Capture the ChapterCard object
            mChapterCard = chapterCard;
            // Set card views based on the ChapterCard object
            mTitle.setText(chapterCard.getTitle());
            chapterCard.refreshCheckingLevel(mProject, position+1);
            mCheckLevelBtn.setStep(chapterCard.getCheckingLevel());
            holder.mCompileBtn.setActivated(mChapterCard.canCompile());
            if (mChapterCard.isCompiled()) {
                mCheckLevelBtn.setVisibility(View.VISIBLE);
                mExpandBtn.setVisibility(View.VISIBLE);
            } else {
                mCheckLevelBtn.setVisibility(View.INVISIBLE);
                mExpandBtn.setVisibility(View.INVISIBLE);
            }

            setListeners(this, mChapterCard);

            // Expand card if it's already expanded before
            if (mChapterCard.isExpanded()) {
                chapterCard.expand(holder);
            } else {
                chapterCard.collapse(holder);
            }


            // Raise card, and show appropriate visual cue, if it's already selected
            if (mMultiSelector.isSelected(position, 0)) {
                mSelectedCards.add(getAdapterPosition());
                chapterCard.raise(holder);
            } else {
                mSelectedCards.remove((Integer)getAdapterPosition());
                chapterCard.drop(holder);
            }

            mChapterCard.setIconsClickable(holder);
        }

        @Override
        public void onClick(View view) {
            // Completing a chapter (hence can be compiled) is the minimum requirements to
            //    include a chapter in multi-selection
            if (mChapterCard == null) {
                return;
            }

            if(mMultiSelector.isSelectable() && mChapterCard.canCompile()) {
                // Close card if it is expanded in multi-select mode
                if(mChapterCard.isExpanded()){
                    toggleExpansion(this, mExpandedCards, this.getAdapterPosition());
                }

                mMultiSelector.tapSelection(this);

                // Raise/drop card
                if (mMultiSelector.isSelected(this.getAdapterPosition(), 0)) {
                    mSelectedCards.add(getAdapterPosition());
                    mChapterCard.raise(this);
                } else {
                    mSelectedCards.remove((Integer)getAdapterPosition());
                    mChapterCard.drop(this);
                }

                setAvailableActions();

                // Finish action mode if all cards are de-selected
                if (mActionMode != null && mSelectedCards.size() <= 0) {
                    mActionMode.finish();
                }

            } else {
                Intent intent = ActivityUnitList.getActivityVerseListIntent(mCtx, mProject, getAdapterPosition()+1);
                mCtx.startActivity(intent);
            }
        }

        @Override
        public boolean onLongClick(View view) {
            // Completing a chapter (hence can be compiled) is the minimum requirements to
            //    include a chapter in multi-selection
            if (!mChapterCard.canCompile()) {
                return false;
            }

            mActionMode = mCtx.startSupportActionMode(mMultiSelectMode);
            mMultiSelector.setSelected(this, true);

            // Close card if it is expanded on entering multi-select mode
            if(mChapterCard.isExpanded()){
                toggleExpansion(this, mExpandedCards, this.getAdapterPosition());
            }

            mChapterCard.raise(this);

            setAvailableActions();

            return true;
        }
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.chapter_card, parent, false);
        // Set the view's size, margins, padding and layout params here
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ChapterCard chapterCard = mChapterCardList.get(position);
        holder.bindViewHolder(holder, position, chapterCard);
    }

    @Override
    public int getItemCount() {
        return mChapterCardList.size();
    }


    // Private Methods
    private void setListeners(final ViewHolder holder, final ChapterCard chapterCard) {
        holder.mCheckLevelBtn.setOnClickListener(chapterCard.getCheckLevelOnClick(holder));
        holder.mCompileBtn.setOnClickListener(chapterCard.getCompileOnClick(holder, this));
        holder.mRecordBtn.setOnClickListener(chapterCard.getRecordOnClick(holder));
        holder.mExpandBtn.setOnClickListener(chapterCard.getExpandOnClick(holder));
        holder.mDeleteBtn.setOnClickListener(chapterCard.getDeleteOnClick(holder, this));
        holder.mPlayPauseBtn.setOnClickListener(chapterCard.getPlayPauseOnClick(holder));
    }

    private void setAvailableActions() {
        if (mActionMode == null) {
            return;
        }

        boolean checkEnabled = true;

        for (ChapterCard chapterCard : getSelectedCards()) {
            if (!chapterCard.isCompiled()) {
                checkEnabled = false;
                break;
            }
        }

        mActionMode.getMenu().findItem(R.id.chapters_checking_level).setEnabled(checkEnabled);
    }


    // Public API
    public void toggleExpansion(final ChapterCardAdapter.ViewHolder vh, final List<Integer> expandedCards, final int position) {
        if (!vh.mChapterCard.isExpanded()) {
            vh.mChapterCard.expand(vh);
            if (!expandedCards.contains(position)) {
                expandedCards.add(position);
            }
        } else {
            vh.mChapterCard.collapse(vh);
            if (expandedCards.contains(position)) {
                expandedCards.remove(expandedCards.indexOf(position));
            }
        }
    }

    public boolean isInActionMode() {
        return mActionMode != null;
    }

    public ActionMode getActionMode() {
        return mActionMode;
    }

    public void setIconsClickable(boolean clickable) {
        for (int i = 0; i < mChapterCardList.size(); i++) {
            mChapterCardList.get(i).setIconsClickable(clickable);
            notifyItemChanged(i);
        }
    }

    public List<ChapterCard> getSelectedCards() {
        List<ChapterCard> cards = new ArrayList<>();
        for (int i = getItemCount(); i >= 0; i--) {
            if (mMultiSelector.isSelected(i, 0)) {
                cards.add(mChapterCardList.get(i));
            }
        }
        return cards;
    }

}
