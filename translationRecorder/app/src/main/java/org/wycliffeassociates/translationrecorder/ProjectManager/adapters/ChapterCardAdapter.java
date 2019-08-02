package org.wycliffeassociates.translationrecorder.ProjectManager.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
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
import com.filippudak.ProgressPieView.ProgressPieView;

import org.wycliffeassociates.translationrecorder.ProjectManager.activities.ActivityUnitList;
import org.wycliffeassociates.translationrecorder.ProjectManager.dialogs.CheckingDialog;
import org.wycliffeassociates.translationrecorder.ProjectManager.dialogs.CompileDialog;
import org.wycliffeassociates.translationrecorder.R;
import org.wycliffeassociates.translationrecorder.database.ProjectDatabaseHelper;
import org.wycliffeassociates.translationrecorder.project.Project;
import org.wycliffeassociates.translationrecorder.widgets.ChapterCard;
import org.wycliffeassociates.translationrecorder.widgets.FourStepImageView;
import org.wycliffeassociates.translationrecorder.widgets.OnCardExpandedListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by leongv on 8/15/2016.
 */
public class ChapterCardAdapter extends RecyclerView.Adapter<ChapterCardAdapter.ViewHolder> implements ChapterCard.ChapterDB, OnCardExpandedListener {

    // Attributes
    private AppCompatActivity mCtx;
    private RecyclerView recyclerView;
    private Project mProject;
    private List<ChapterCard> mChapterCardList;
    private List<Integer> mExpandedCards = new ArrayList<>();
    private List<Integer> mSelectedCards = new ArrayList<>();
    private MultiSelector mMultiSelector = new MultiSelector();
    private ActionMode mActionMode;
    private ProjectDatabaseHelper db;

    private int RAISED_CARD_BACKGROUND_COLOR;
    private int DROPPED_CARD_BACKGROUND_COLOR;
    private int RAISED_CARD_TEXT_COLOR;
    private int DROPPED_CARD_TEXT_COLOR;
    private int DROPPED_CARD_EMPTY_TEXT_COLOR;

    // Constructor
    public ChapterCardAdapter(
            AppCompatActivity context,
            Project project,
            List<ChapterCard> chapterCardList,
            ProjectDatabaseHelper db
    ) {
        mCtx = context;
        mProject = project;
        mChapterCardList = chapterCardList;
        this.db = db;

        initializeColors(context);
    }

    private void initializeColors(Context mCtx) {
        RAISED_CARD_BACKGROUND_COLOR = mCtx.getResources().getColor(R.color.accent);
        RAISED_CARD_TEXT_COLOR = mCtx.getResources().getColor(R.color.text_light);

        DROPPED_CARD_BACKGROUND_COLOR = mCtx.getResources().getColor(R.color.card_bg);
        DROPPED_CARD_EMPTY_TEXT_COLOR = mCtx.getResources().getColor(R.color.primary_text_disabled_material_light);
        DROPPED_CARD_TEXT_COLOR = mCtx.getResources().getColor(R.color.primary_text_default_material_light);
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
            mActionMode = actionMode;
            mCtx.getMenuInflater().inflate(R.menu.chapter_menu, menu);
            setIconsClickable(false);
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            int[] chapters = new int[mSelectedCards.size()];
            for (int i = 0; i < mSelectedCards.size(); i++) {
                chapters[i] = mSelectedCards.get(i);
            }
            switch (item.getItemId()) {
                case R.id.chapters_checking_level:
                    CheckingDialog dialog = CheckingDialog.newInstance(mProject, chapters, getCommonCheckingLevel());
                    dialog.show(mCtx.getFragmentManager(), "multi_chapter_checking_level");
                    break;
                case R.id.chapters_compile:
                    boolean[] isCompiled = new boolean[mSelectedCards.size()];
                    for (int i = 0; i < mSelectedCards.size(); i++) {
                        isCompiled[i] = mChapterCardList.get(mSelectedCards.get(i)).isCompiled();
                    }
                    CompileDialog compileDialog = CompileDialog.newInstance(mProject, chapters, isCompiled);
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
            mActionMode = null;
            setIconsClickable(true);
        }
    };

    @Override
    public int checkingLevel(Project project, int chapter) {
        int checkingLevel = db.getChapterCheckingLevel(project, chapter);
        return checkingLevel;
    }

    @Override
    public void onCardExpanded(int position) {
        recyclerView.getLayoutManager().scrollToPosition(position);
    }

    public class ViewHolder extends SwappingHolder implements View.OnClickListener,
            View.OnLongClickListener {

        public ChapterCard chapterCard;
        public CardView cardView;
        public RelativeLayout cardHeader;
        public LinearLayout cardBody, cardContainer, actions;
        public TextView title, elapsed, duration;
        public ImageView recordBtn, compileBtn, expandBtn;
        public ImageButton deleteBtn, playPauseBtn;
        public FourStepImageView checkLevelBtn;
        public SeekBar seekBar;
        public ProgressPieView progressPie;

        public ViewHolder(View view) {
            super(view, mMultiSelector);
            // Containers
            cardView = (CardView) view.findViewById(R.id.chapter_card);
            cardContainer = (LinearLayout) view.findViewById(R.id.chapter_card_container);
            cardHeader = (RelativeLayout) view.findViewById(R.id.card_header);
            cardBody = (LinearLayout) view.findViewById(R.id.card_body);
            actions = (LinearLayout) view.findViewById(R.id.actions);

            // Views
            title = (TextView) view.findViewById(R.id.title);
            seekBar = (SeekBar) view.findViewById(R.id.seek_bar);
            elapsed = (TextView) view.findViewById(R.id.time_elapsed);
            duration = (TextView) view.findViewById(R.id.time_duration);
            progressPie = (ProgressPieView) view.findViewById(R.id.progress_pie);

            // Buttons
            checkLevelBtn = (FourStepImageView) view.findViewById(R.id.check_level_btn);
            recordBtn = (ImageView) view.findViewById(R.id.record_btn);
            compileBtn = (ImageView) view.findViewById(R.id.compile_btn);
            expandBtn = (ImageView) view.findViewById(R.id.expand_btn);
            deleteBtn = (ImageButton) view.findViewById(R.id.delete_chapter_audio_btn);
            playPauseBtn = (ImageButton) view.findViewById(R.id.play_pause_chapter_btn);

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

        public void bindViewHolder(ViewHolder holder, int pos, ChapterCard cc) {
            chapterCard = cc;
            chapterCard.setViewHolder(holder);

            // Title
            title.setText(chapterCard.getTitle());

            // Progress Pie
            progressPie.setProgress(chapterCard.getProgress());

            // Checking Level
            chapterCard.refreshCheckingLevel(ChapterCardAdapter.this, mProject, chapterCard.getChapterNumber());
            checkLevelBtn.setStep(chapterCard.getCheckingLevel());

            // Compile
            compileBtn.setActivated(chapterCard.canCompile());

            // Checking Level and Expansion
            if (chapterCard.isCompiled()) {
                checkLevelBtn.setVisibility(View.VISIBLE);
                expandBtn.setVisibility(View.VISIBLE);
            } else {
                checkLevelBtn.setVisibility(View.INVISIBLE);
                expandBtn.setVisibility(View.INVISIBLE);
            }

            // Expand card if it's already expanded before
            if (chapterCard.isExpanded()) {
                chapterCard.expand();
            } else {
                chapterCard.collapse();
            }

            // Raise card, and show appropriate visual cue, if it's already selected
            if (mMultiSelector.isSelected(pos, 0)) {
                chapterCard.raise(RAISED_CARD_BACKGROUND_COLOR, RAISED_CARD_TEXT_COLOR);
                if (!mSelectedCards.contains(getAdapterPosition())) {
                    mSelectedCards.add(getAdapterPosition());
                }
            } else {
                mSelectedCards.remove((Integer) getAdapterPosition());
                chapterCard.drop(
                        DROPPED_CARD_BACKGROUND_COLOR,
                        DROPPED_CARD_TEXT_COLOR,
                        DROPPED_CARD_EMPTY_TEXT_COLOR
                );
            }

            // Clickable
            chapterCard.setIconsClickable(!isInActionMode());

            setListeners(this, chapterCard);
        }

        @Override
        public void onClick(View view) {
            // Completing a chapter (hence can be compiled) is the minimum requirements to
            //    include a chapter in multi-selection
            if (chapterCard == null) {
                return;
            }

            if (mMultiSelector.isSelectable()) {
                if (!chapterCard.canCompile()) {
                    return;
                }

                // Close card if it is expanded in multi-select mode
                if (chapterCard.isExpanded()) {
                    toggleExpansion(this, mExpandedCards, this.getAdapterPosition());
                }

                mMultiSelector.tapSelection(this);

                // Raise/drop card
                if (mMultiSelector.isSelected(this.getAdapterPosition(), 0)) {
                    mSelectedCards.add(getAdapterPosition());
                    chapterCard.raise(RAISED_CARD_BACKGROUND_COLOR, RAISED_CARD_TEXT_COLOR);
                } else {
                    mSelectedCards.remove((Integer) getAdapterPosition());
                    chapterCard.drop(
                            DROPPED_CARD_BACKGROUND_COLOR,
                            DROPPED_CARD_TEXT_COLOR,
                            DROPPED_CARD_EMPTY_TEXT_COLOR
                    );                }

                setAvailableActions();

                // Finish action mode if all cards are de-selected
                if (mActionMode != null && mSelectedCards.size() <= 0) {
                    mActionMode.finish();
                }

            } else {
                chapterCard.pauseAudio();
                chapterCard.destroyAudioPlayer();
                Intent intent = ActivityUnitList.getActivityUnitListIntent(mCtx, mProject, chapterCard.getChapterNumber());
                mCtx.startActivity(intent);
            }
        }

        @Override
        public boolean onLongClick(View view) {
            // Completing a chapter (hence can be compiled) is the minimum requirements to
            //    include a chapter in multi-selection
            if (!chapterCard.canCompile()) {
                return false;
            }

            mCtx.startSupportActionMode(mMultiSelectMode);
            mMultiSelector.setSelected(this, true);

            // Close card if it is expanded on entering multi-select mode
            if (chapterCard.isExpanded()) {
                toggleExpansion(this, mExpandedCards, this.getAdapterPosition());
            }

            chapterCard.raise(RAISED_CARD_BACKGROUND_COLOR, RAISED_CARD_TEXT_COLOR);

            setAvailableActions();

            return true;
        }
    }


    // Overrides
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


    // Getters
    public List<ChapterCard> getSelectedCards() {
        List<ChapterCard> cards = new ArrayList<>();
        for (int i = getItemCount(); i >= 0; i--) {
            if (mMultiSelector.isSelected(i, 0)) {
                cards.add(mChapterCardList.get(i));
            }
        }
        return cards;
    }

    public boolean isInActionMode() {
        return mActionMode != null;
    }

    public ActionMode getActionMode() {
        return mActionMode;
    }


    // Private Methods
    private void setListeners(final ViewHolder holder, final ChapterCard chapterCard) {
        holder.checkLevelBtn.setOnClickListener(chapterCard.getCheckLevelOnClick(mCtx.getFragmentManager()));
        holder.compileBtn.setOnClickListener(chapterCard.getCompileOnClick(mCtx.getFragmentManager()));
        holder.recordBtn.setOnClickListener(chapterCard.getRecordOnClick(mCtx));
        holder.expandBtn.setOnClickListener(chapterCard.getExpandOnClick(this, holder.getAdapterPosition()));
        holder.deleteBtn.setOnClickListener(chapterCard.getDeleteOnClick(this, mCtx));
        holder.playPauseBtn.setOnClickListener(chapterCard.getPlayPauseOnClick());
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

    private int getCommonCheckingLevel() {
        List<ChapterCard> selectedCards = getSelectedCards();
        int length = selectedCards.size();
        int checkingLevel = length >= 1 ? selectedCards.get(0).getCheckingLevel() : CheckingDialog.NO_LEVEL_SELECTED;

        // If there are more items, check if their checking level is similar. If not, set the
        // checking level to an empty value
        for (int i = 1; i < length; i++) {
            if (selectedCards.get(i).getCheckingLevel() != checkingLevel) {
                checkingLevel = CheckingDialog.NO_LEVEL_SELECTED;
                break;
            }
        }

        return checkingLevel;
    }


    // Public API
    public void toggleExpansion(final ChapterCardAdapter.ViewHolder vh, final List<Integer> expandedCards, final int position) {
        if (!vh.chapterCard.isExpanded()) {
            vh.chapterCard.expand();
            if (!expandedCards.contains(position)) {
                expandedCards.add(position);
            }
        } else {
            vh.chapterCard.collapse();
            if (expandedCards.contains(position)) {
                expandedCards.remove(expandedCards.indexOf(position));
            }
        }
    }

    public void setIconsClickable(boolean clickable) {
        for (int i = 0; i < mChapterCardList.size(); i++) {
            mChapterCardList.get(i).setIconsClickable(clickable);
            notifyItemChanged(i);
        }
    }

    public void exitCleanUp() {
        for (ChapterCard cc : mChapterCardList) {
            if (cc.isExpanded()) {
                cc.destroyAudioPlayer();
            }
        }
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
    }

    public ChapterCard getItem(int index) {
        return mChapterCardList.get(index);
    }

}
