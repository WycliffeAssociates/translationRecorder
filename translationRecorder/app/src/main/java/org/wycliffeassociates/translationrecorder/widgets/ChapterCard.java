package org.wycliffeassociates.translationrecorder.widgets;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;

import org.wycliffeassociates.translationrecorder.ProjectManager.adapters.ChapterCardAdapter;
import org.wycliffeassociates.translationrecorder.ProjectManager.dialogs.CheckingDialog;
import org.wycliffeassociates.translationrecorder.ProjectManager.dialogs.CompileDialog;
import org.wycliffeassociates.translationrecorder.R;
import org.wycliffeassociates.translationrecorder.Recording.RecordingActivity;
import org.wycliffeassociates.translationrecorder.chunkplugin.ChunkPlugin;
import org.wycliffeassociates.translationrecorder.database.ProjectDatabaseHelper;
import org.wycliffeassociates.translationrecorder.project.Project;
import org.wycliffeassociates.translationrecorder.project.ProjectFileUtils;

import java.io.File;
import java.lang.ref.SoftReference;
import java.util.List;

/**
 * Created by leongv on 8/15/2016.
 */
public class ChapterCard {

    public interface OnClickListener extends View.OnClickListener {
        void onClick(
                View v,
                ChapterCardAdapter.ViewHolder vh,
                List<Integer> expandedCards,
                int position
        );
    }

    public interface ChapterDB {
        int checkingLevel(Project project, int chapter);
    }

    public interface ChapterProgress {
        void updateChapterProgress(int chapter);
        int chapterProgress(int chapter);
    }

    // Constants
    public int MIN_CHECKING_LEVEL = 0;
    public int MAX_CHECKING_LEVEL = 3;
    public int MIN_PROGRESS = 0;
    public int MAX_PROGRESS = 100;

    // Attributes
    private Project mProject;
    private ChapterCardAdapter.ViewHolder mViewHolder;
    private SoftReference<AudioPlayer> mAudioPlayer;
    private File mChapterWav;
    private String mTitle;
    private final int mChapter;
    private int mCheckingLevel = 0;
    private int mProgress = 0;
    private int mUnitCount;
    private int mUnitStarted = 0;

    // State
    private boolean mIsEmpty = true;
    private boolean mCanCompile = false;
    private boolean mIsCompiled = false;
    private boolean mIsExpanded = false;
    private boolean mIconsClickable = true;

    private ProjectDatabaseHelper db;

    // Constructor
    public ChapterCard(Project proj, String title, int chapter, int unitCount, ProjectDatabaseHelper db) {
        mProject = proj;
        mTitle = title;
        mChapter = chapter;
        mUnitCount = unitCount;
        this.db = db;
    }

    public void refreshIsEmpty() {
        mIsEmpty = mProgress == 0;
    }

    public void refreshChapterCompiled(int chapter) {
        if (!mCanCompile) {
            return;
        }
        File dir = ProjectFileUtils.getProjectDirectory(mProject);
        String chapterString = ProjectFileUtils.chapterIntToString(mProject, chapter);
        File chapterDir = new File(dir, chapterString);
        if (chapterDir.exists()) {
            mChapterWav = new File(chapterDir, mProject.getChapterFileName(chapter));
            if (mChapterWav.exists()) {
                mIsCompiled = true;
                return;
            }
        }
        mIsCompiled = false;
    }

    public void refreshCheckingLevel(ChapterDB chapterDb, Project project, int chapter) {
        if (mIsCompiled) {
            mCheckingLevel = chapterDb.checkingLevel(project, chapter);
        }
    }

    public void refreshProgress() {
        int progress = calculateProgress();
        if (progress != mProgress) {
            setProgress(progress);
            saveProgressToDB(progress);
        }
    }


    // Setters
    public void setViewHolder(ChapterCardAdapter.ViewHolder vh) {
        mViewHolder = vh;
    }

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

    public void setIconsEnabled(boolean enabled) {
        if (mViewHolder == null) {
            return;
        }
        mViewHolder.checkLevelBtn.setEnabled(enabled);
        mViewHolder.compileBtn.setEnabled(enabled);
        mViewHolder.recordBtn.setEnabled(enabled);
        mViewHolder.expandBtn.setEnabled(enabled);
    }

    public void setIconsClickable(boolean clickable) {
        mIconsClickable = clickable;
    }

    public void setNumOfUnitStarted(int count) {
        mUnitStarted = count;
    }


    // Getters
    public ChapterCardAdapter.ViewHolder getViewHolder() {
        return mViewHolder;
    }

    public String getTitle() {
        return mTitle;
    }

    public int getCheckingLevel() {
        return mCheckingLevel;
    }

    public int getProgress() {
        return mProgress;
    }

    public boolean canCompile() {
        return mCanCompile;
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

    public boolean areIconsClickable() {
        return mIconsClickable;
    }


    // Private Methods
    private AudioPlayer getAudioPlayer() {
        AudioPlayer ap = null;
        if (mAudioPlayer != null) {
            ap = mAudioPlayer.get();
        }
        if (ap == null) {
            ap = initializeAudioPlayer();
        }
        return ap;
    }

    private AudioPlayer initializeAudioPlayer() {
        AudioPlayer ap = new AudioPlayer();
        if (mViewHolder != null) {
            ap.refreshView(
                    mViewHolder.elapsed,
                    mViewHolder.duration,
                    mViewHolder.playPauseBtn,
                    mViewHolder.seekBar
            );
        }
        mAudioPlayer = new SoftReference<AudioPlayer>(ap);
        return ap;
    }

    private void refreshAudioPlayer() {
        AudioPlayer ap = getAudioPlayer();
        if (!ap.isLoaded()) {
            ap.reset();
            ap.loadFile(mChapterWav);
        }
        ap.refreshView(
                mViewHolder.elapsed,
                mViewHolder.duration,
                mViewHolder.playPauseBtn,
                mViewHolder.seekBar
        );
    }

    private int calculateProgress() {
        return Math.round(((float) mUnitStarted / mUnitCount) * 100);
    }

    private void saveProgressToDB(int progress) {
        if (db.chapterExists(mProject, mChapter)) {
            int chapterId = db.getChapterId(mProject, mChapter);
            db.setChapterProgress(chapterId, progress);
        }
    }


    // Public API
    public void expand() {
        refreshAudioPlayer();
        if (mViewHolder != null) {
            mViewHolder.cardBody.setVisibility(View.VISIBLE);
            mViewHolder.expandBtn.setActivated(true);
        }
        mIsExpanded = true;
    }

    public void collapse() {
        if (mViewHolder != null) {
            mViewHolder.cardBody.setVisibility(View.GONE);
            mViewHolder.expandBtn.setActivated(false);
        }
        mIsExpanded = false;
    }

    public void raise(int backgroundColor, int textColor) {
        if (mViewHolder != null) {
            mViewHolder.cardView.setCardElevation(8f);
            mViewHolder.cardContainer.setBackgroundColor(
                    //mCtx.getResources().getColor(R.color.accent)
                    backgroundColor
            );
            mViewHolder.title.setTextColor(
                    //mCtx.getResources().getColor(R.color.text_light)
                    textColor
            );
            // Compile button activated status gets reset by multiSelector.
            // This is a way to correct it.
            mViewHolder.compileBtn.setActivated(canCompile());
        }
        setIconsEnabled(false);
    }

    public void drop(int backgroundColor, int textColor, int emptyTextColor) {
        if (mViewHolder != null) {
            mViewHolder.cardView.setCardElevation(2f);
            mViewHolder.cardContainer.setBackgroundColor(backgroundColor);
            mViewHolder.title.setTextColor(
                (isEmpty())
                        ? emptyTextColor
                        : textColor
            );
            // Compile button activated status gets reset by multiSelector.
            // This is a way to correct it.
            mViewHolder.compileBtn.setActivated(canCompile());
        }
        setIconsEnabled(true);
    }

    public void playAudio() {
        AudioPlayer ap = getAudioPlayer();
        if (ap != null) {
            ap.play();
        }
    }

    public void pauseAudio() {
        AudioPlayer ap = getAudioPlayer();
        if (ap != null) {
            ap.pause();
        }
    }

    public void destroyAudioPlayer() {
        if (mAudioPlayer != null) {
            AudioPlayer ap = mAudioPlayer.get();
            if (ap != null) {
                ap.cleanup();
            }
            mAudioPlayer = null;
        }
    }

//    public void setCanCompile(boolean canCompile){
//        mCanCompile = canCompile;
//    }

    public void refreshCanCompile() {
        mCanCompile = mProgress == 100;
    }

    public void compile() {
        mIsCompiled = true;
        setCheckingLevel(0);
    }

    public View.OnClickListener getCheckLevelOnClick(final FragmentManager fm) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!areIconsClickable()) {
                    return;
                }
                pauseAudio();
                CheckingDialog dialog = CheckingDialog.newInstance(
                        mProject,
                        mViewHolder.getAdapterPosition(),
                        mCheckingLevel);
                dialog.show(fm, "single_chapter_checking_level");
            }
        };
    }

    public View.OnClickListener getCompileOnClick(final FragmentManager fm) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (canCompile()) {
                    if (!areIconsClickable()) {
                        return;
                    }
                    pauseAudio();
                    //pass in chapter index, not chapter number
                    CompileDialog dialog = CompileDialog.newInstance(
                            mProject,
                            mViewHolder.getAdapterPosition(),
                            isCompiled());
                    dialog.show(fm, "single_compile_chapter");
                }
            }
        };
    }

    public View.OnClickListener getRecordOnClick(final Context context) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!areIconsClickable()) {
                    return;
                }
                pauseAudio();
                destroyAudioPlayer();
                int chapter = mChapter;
                Intent intent = RecordingActivity.getNewRecordingIntent(
                        context,
                        mProject,
                        chapter,
                        ChunkPlugin.DEFAULT_UNIT);
                context.startActivity(intent);
            }
        };
    }

    public View.OnClickListener getExpandOnClick(final OnCardExpandedListener listener, final int position) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!areIconsClickable()) {
                    return;
                }
                if (mIsExpanded) {
                    pauseAudio();
                    collapse();
                } else {
                    expand();
                    listener.onCardExpanded(position);
                }
            }
        };
    }

    public View.OnClickListener getDeleteOnClick(final ChapterCardAdapter adapter, final Context context) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pauseAudio();
                AlertDialog dialog = new AlertDialog.Builder(context)
                        .setTitle("Delete Chapter Recording?")
                        .setIcon(R.drawable.ic_delete_black_36dp)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                destroyAudioPlayer();
                                mChapterWav.delete();
                                mIsCompiled = false;
                                collapse();
                                db.setCheckingLevel(mProject, mChapter, 0);
                                adapter.notifyItemChanged(mViewHolder.getAdapterPosition());
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .create();
                dialog.show();
            }
        };
    }

    public View.OnClickListener getPlayPauseOnClick() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mViewHolder.playPauseBtn.isActivated()) {
                    pauseAudio();
                } else {
                    playAudio();
                }
            }
        };
    }

    public int getChapterNumber() {
        return mChapter;
    }
}
