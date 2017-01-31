package org.wycliffeassociates.translationrecorder.widgets;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;

import org.wycliffeassociates.translationrecorder.FilesPage.FileNameExtractor;
import org.wycliffeassociates.translationrecorder.ProjectManager.Project;
import org.wycliffeassociates.translationrecorder.ProjectManager.adapters.ChapterCardAdapter;
import org.wycliffeassociates.translationrecorder.ProjectManager.dialogs.CheckingDialog;
import org.wycliffeassociates.translationrecorder.ProjectManager.dialogs.CompileDialog;
import org.wycliffeassociates.translationrecorder.R;
import org.wycliffeassociates.translationrecorder.Recording.RecordingScreen;
import org.wycliffeassociates.translationrecorder.database.ProjectDatabaseHelper;
import org.wycliffeassociates.translationrecorder.wav.WavFile;

import java.io.File;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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


    // Constructor
    public ChapterCard(Activity ctx, Project proj, int chapter, int unitCount) {
        mCtx = ctx;
        mProject = proj;
        mTitle = "Chapter " + chapter;
        mChapter = chapter;
        mUnitCount = unitCount;
    }

//    public void refreshChapterStarted(Project project, int chapter){
//        File dir = Project.getProjectDirectory(project);
//        String chapterString = FileNameExtractor.chapterIntToString(project, chapter);
//        File[] files = dir.listFiles();
//        if(files != null) {
//            for (File f : files) {
//                if (f.getName().equals(chapterString)) {
//                    mIsEmpty = false;
//                    return;
//                }
//            }
//        }
//        mIsEmpty = true;
//    }

    public void refreshIsEmpty() {
        mIsEmpty = mProgress == 0;
    }

    public void refreshChapterCompiled(int chapter) {
        if (!mCanCompile) {
            return;
        }
        File dir = Project.getProjectDirectory(mProject);
        String chapterString = FileNameExtractor.chapterIntToString(mProject, chapter);
        File chapterDir = new File(dir, chapterString);
        if (chapterDir.exists()) {
            mChapterWav = new File(chapterDir, "chapter.wav");
            if (mChapterWav.exists()) {
                mIsCompiled = true;
                return;
            }
        }
        mIsCompiled = false;
    }

    public void refreshCheckingLevel(Project project, int chapter) {
        if (mIsCompiled) {
            ProjectDatabaseHelper db = new ProjectDatabaseHelper(mCtx);
            mCheckingLevel = db.getChapterCheckingLevel(project, chapter);
            db.close();
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
            ap.refreshView(mViewHolder.elapsed, mViewHolder.duration, mViewHolder.playPauseBtn, mViewHolder.seekBar);
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
        ap.refreshView(mViewHolder.elapsed, mViewHolder.duration, mViewHolder.playPauseBtn, mViewHolder.seekBar);
    }

    private int calculateProgress() {
        return Math.round(((float) mUnitStarted / mUnitCount) * 100);
    }

    private void saveProgressToDB(int progress) {
        ProjectDatabaseHelper db = new ProjectDatabaseHelper(mCtx);
        if (db.chapterExists(mProject, mChapter)) {
            int chapterId = db.getChapterId(mProject, mChapter);
            db.setChapterProgress(chapterId, progress);
        }
        db.close();
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

    public void raise() {
        if (mViewHolder != null) {
            mViewHolder.cardView.setCardElevation(8f);
            mViewHolder.cardContainer.setBackgroundColor(mCtx.getResources().getColor(R.color.accent));
            mViewHolder.title.setTextColor(mCtx.getResources().getColor(R.color.text_light));
            // Compile button activated status gets reset by multiSelector. This is a way to correct it.
            mViewHolder.compileBtn.setActivated(canCompile());
        }
        setIconsEnabled(false);
    }

    public void drop() {
        if (mViewHolder != null) {
            mViewHolder.cardView.setCardElevation(2f);
            mViewHolder.cardContainer.setBackgroundColor(mCtx.getResources().getColor(R.color.card_bg));
            mViewHolder.title.setTextColor(
                    mCtx.getResources().getColor((isEmpty()) ? R.color.primary_text_disabled_material_light : R.color.primary_text_default_material_light)
            );
            // Compile button activated status gets reset by multiSelector. This is a way to correct it.
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
        ProjectDatabaseHelper db = new ProjectDatabaseHelper(mCtx);
        List<String> files = db.getTakesForChapterCompilation(mProject, mChapter);
        Collections.sort(files, new Comparator<String>() {
            @Override
            public int compare(String lhs, String rhs) {
                FileNameExtractor fneLeft = new FileNameExtractor(lhs);
                FileNameExtractor fneRight = new FileNameExtractor(rhs);
                int startLeft = fneLeft.getStartVerse();
                int startRight = fneRight.getStartVerse();
                if (startLeft < startRight) {
                    return -1;
                } else if (startLeft == startRight) {
                    return 0;
                } else {
                    return 1;
                }
            }
        });
        List<WavFile> wavFiles = new ArrayList<>();
        File base = FileNameExtractor.getParentDirectory(mProject, mChapter);
        for (String s : files) {
            File f = new File(base, s);
            wavFiles.add(new WavFile(f));
        }
        WavFile.compileChapter(mProject, mChapter, wavFiles);
        mIsCompiled = true;
        setCheckingLevel(0);
    }

    public View.OnClickListener getCheckLevelOnClick() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!areIconsClickable()) {
                    return;
                }
                pauseAudio();
                CheckingDialog dialog = CheckingDialog.newInstance(mProject, mChapter - 1, mCheckingLevel);
                dialog.show(mCtx.getFragmentManager(), "single_chapter_checking_level");
            }
        };
    }

    public View.OnClickListener getCompileOnClick() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (canCompile()) {
                    if (!areIconsClickable()) {
                        return;
                    }
                    pauseAudio();
                    //pass in chapter index, not chapter number
                    CompileDialog dialog = CompileDialog.newInstance(mProject, mChapter - 1, isCompiled());
                    dialog.show(mCtx.getFragmentManager(), "single_compile_chapter");
                }
            }
        };
    }

    public View.OnClickListener getRecordOnClick() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!areIconsClickable()) {
                    return;
                }
                pauseAudio();
                destroyAudioPlayer();
                int chapter = mViewHolder.getAdapterPosition() + 1;
                Intent intent = RecordingScreen.getNewRecordingIntent(mCtx, mProject, chapter, 1);
                mCtx.startActivity(intent);
            }
        };
    }

    public View.OnClickListener getExpandOnClick() {
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
                }
            }
        };
    }

    public View.OnClickListener getDeleteOnClick(final ChapterCardAdapter adapter) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pauseAudio();
                AlertDialog dialog = new AlertDialog.Builder(mCtx)
                        .setTitle("Delete Chapter Recording?")
                        .setIcon(R.drawable.ic_delete_black_36dp)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                destroyAudioPlayer();
                                mChapterWav.delete();
                                mIsCompiled = false;
                                collapse();
                                ProjectDatabaseHelper db = new ProjectDatabaseHelper(mCtx);
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
}
