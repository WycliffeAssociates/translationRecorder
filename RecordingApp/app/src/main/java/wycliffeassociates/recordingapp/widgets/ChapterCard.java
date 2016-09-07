package wycliffeassociates.recordingapp.widgets;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;

import java.io.File;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import wycliffeassociates.recordingapp.FilesPage.FileNameExtractor;
import wycliffeassociates.recordingapp.ProjectManager.ChapterCardAdapter;
import wycliffeassociates.recordingapp.ProjectManager.CheckingDialog;
import wycliffeassociates.recordingapp.ProjectManager.CompileDialog;
import wycliffeassociates.recordingapp.ProjectManager.Project;
import wycliffeassociates.recordingapp.ProjectManager.ProjectDatabaseHelper;
import wycliffeassociates.recordingapp.R;
import wycliffeassociates.recordingapp.Recording.RecordingScreen;
import wycliffeassociates.recordingapp.Recording.WavFile;

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
    private final int mChapter;

    // Attributes
    private Activity mCtx;
    private Project mProject;
    private SoftReference<AudioPlayer> mAudioPlayer;
    private File mChapterWav;
    private String mTitle = "";
    private int mCheckingLevel = 0;
    private int mProgress = 0;

    // State
    private boolean mIsEmpty = true;
    private boolean mIsCompiled = true;
    private boolean mIsExpanded = false;
    private boolean mCanCompile = true;
    private boolean mIconsClickable = true;


    // Constructor
    public ChapterCard(Activity ctx, Project proj, int chapter) {
        mCtx = ctx;
        mProject = proj;
        mTitle = "Chapter " + chapter;
        mCanCompile = false;
        mIsCompiled = false;
        mChapter = chapter;
        refreshChapterStarted(proj, chapter);
        refreshChapterCompiled(proj, chapter);
    }

    public void refreshChapterStarted(Project project, int chapter){
        File dir = Project.getProjectDirectory(project);
        String chapterString = FileNameExtractor.chapterIntToString(project, chapter);
        File[] files = dir.listFiles();
        if(files != null) {
            for (File f : files) {
                if (f.getName().equals(chapterString)) {
                    mIsEmpty = false;
                    return;
                }
            }
        }
        mIsEmpty = true;
    }

    public void refreshChapterCompiled(Project project, int chapter){
        File dir = Project.getProjectDirectory(project);
        String chapterString = FileNameExtractor.chapterIntToString(project, chapter);
        File chapterDir = new File(dir, chapterString);
        if(chapterDir.exists()) {
            mChapterWav = new File(chapterDir, "chapter.wav");
            if(mChapterWav.exists()){
                mIsCompiled = true;
                return;
            }
        }
        mIsCompiled = false;
    }

    public void refreshCheckingLevel(Project project, int chapter){
        if(mIsCompiled){
            ProjectDatabaseHelper db = new ProjectDatabaseHelper(mCtx);
            mCheckingLevel = db.getChapterCheckingLevel(project, chapter);
        }
    }

    public void refreshProgress(Project project, int chapter) {
        // TODO: Set actual progress here
        setProgress((int) Math.round(Math.random() * 100));
    }


    // Setters
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

    public void setIconsEnabled(ChapterCardAdapter.ViewHolder vh, boolean enabled) {
        vh.mCheckLevelBtn.setEnabled(enabled);
        vh.mCompileBtn.setEnabled(enabled);
        vh.mRecordBtn.setEnabled(enabled);
        vh.mExpandBtn.setEnabled(enabled);
    }

    public void setIconsClickable(boolean clickable) {
        mIconsClickable = clickable;
    }

    public void setIconsClickable(ChapterCardAdapter.ViewHolder vh) {
        vh.mCheckLevelBtn.setClickable(mIconsClickable);
        vh.mCompileBtn.setClickable(mIconsClickable);
        vh.mRecordBtn.setClickable(mIconsClickable);
        vh.mExpandBtn.setClickable(mIconsClickable);
    }


    // Getters
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

    public boolean areIconsClickable() { return mIconsClickable; }


    // Private Methods
    private AudioPlayer getAudioPlayer(ChapterCardAdapter.ViewHolder vh) {
        AudioPlayer ap = null;
        if (mAudioPlayer != null) {
            ap = mAudioPlayer.get();
        }
        if (ap == null) {
            ap = initializeAudioPlayer(vh);
        }
        return ap;
    }

    private AudioPlayer initializeAudioPlayer(ChapterCardAdapter.ViewHolder vh) {
        AudioPlayer ap = new AudioPlayer(vh.mElapsed, vh.mDuration, vh.mPlayPauseBtn, vh.mSeekBar);
        mAudioPlayer = new SoftReference<AudioPlayer>(ap);
        return ap;
    }

    private void refreshAudioPlayer(ChapterCardAdapter.ViewHolder vh) {
        AudioPlayer ap = getAudioPlayer(vh);
        if (!ap.isLoaded()) {
            ap.reset();
            ap.loadFile(mChapterWav);
        }
        ap.refreshView(vh.mElapsed, vh.mDuration, vh.mPlayPauseBtn, vh.mSeekBar);
    }


    // Public API
    public void expand(ChapterCardAdapter.ViewHolder vh) {
        refreshAudioPlayer(vh);
        vh.mCardBody.setVisibility(View.VISIBLE);
        vh.mExpandBtn.setActivated(true);
        mIsExpanded = true;
    }

    public void collapse(ChapterCardAdapter.ViewHolder vh) {
        vh.mCardBody.setVisibility(View.GONE);
        vh.mExpandBtn.setActivated(false);
        mIsExpanded = false;
    }

    public void raise(ChapterCardAdapter.ViewHolder vh) {
        vh.mCardView.setCardElevation(8f);
        vh.mCardContainer.setBackgroundColor(mCtx.getResources().getColor(R.color.accent));
        vh.mTitle.setTextColor(mCtx.getResources().getColor(R.color.text_light));
        setIconsEnabled(vh, false);
        // Compile button activated status gets reset by multiSelector. This is a way to correct it.
        vh.mCompileBtn.setActivated(canCompile());
    }

    public void drop(ChapterCardAdapter.ViewHolder vh) {
        vh.mCardView.setCardElevation(2f);
        vh.mCardContainer.setBackgroundColor(mCtx.getResources().getColor(R.color.card_bg));
        vh.mTitle.setTextColor(
                mCtx.getResources().getColor((isEmpty())? R.color.primary_text_disabled_material_light : R.color.primary_text_default_material_light)
        );
        setIconsEnabled(vh, true);
        // Compile button activated status gets reset by multiSelector. This is a way to correct it.
        vh.mCompileBtn.setActivated(canCompile());
    }

    public void playAudio(ChapterCardAdapter.ViewHolder vh) {
        getAudioPlayer(vh).play();
    }

    public void pauseAudio(ChapterCardAdapter.ViewHolder vh) {
        getAudioPlayer(vh).pause();
    }

    public void destroyAudioPlayer() {
        if (mAudioPlayer != null) {
            AudioPlayer ap = mAudioPlayer.get();
            ap.cleanup();
            mAudioPlayer = null;
        }
    }

    public void setCanCompile(boolean canCompile){
        mCanCompile = canCompile;
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
                if(startLeft < startRight){
                    return -1;
                } else if(startLeft == startRight) {
                    return 0;
                } else {
                    return 1;
                }
            }
        });
        List<WavFile> wavFiles = new ArrayList<>();
        File base = FileNameExtractor.getDirectoryFromProject(mProject, mChapter);
        for(String s : files){
            File f = new File(base, s);
            wavFiles.add(new WavFile(f));
        }
        WavFile.compileChapter(mProject, mChapter, wavFiles);
        mIsCompiled = true;
        setCheckingLevel(0);
    }

    public View.OnClickListener getCheckLevelOnClick(final ChapterCardAdapter.ViewHolder vh) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pauseAudio(vh);
                CheckingDialog dialog = CheckingDialog.newInstance(mProject, mChapter-1, mCheckingLevel);
                dialog.show(mCtx.getFragmentManager(), "single_chapter_checking_level");
            }
        };
    }

    public View.OnClickListener getCompileOnClick(final ChapterCardAdapter.ViewHolder vh, final ChapterCardAdapter adapter) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(canCompile()) {
                    pauseAudio(vh);
                    //pass in chapter index, not chapter number
                    CompileDialog dialog = CompileDialog.newInstance(mProject, mChapter-1, isCompiled());
                    dialog.show(mCtx.getFragmentManager(), "single_compile_chapter");
                }
            }
        };
    }

    public View.OnClickListener getRecordOnClick(final ChapterCardAdapter.ViewHolder vh) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pauseAudio(vh);
                destroyAudioPlayer();
                int chapter = vh.getAdapterPosition() + 1;
                Intent intent = RecordingScreen.getNewRecordingIntent(mCtx, mProject, chapter, 1);
                mCtx.startActivity(intent);
            }
        };
    }

    public View.OnClickListener getExpandOnClick(final ChapterCardAdapter.ViewHolder vh) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mIsExpanded) {
                    pauseAudio(vh);
                    collapse(vh);
                } else {
                    expand(vh);
                }
            }
        };
    }

    public View.OnClickListener getDeleteOnClick(final ChapterCardAdapter.ViewHolder vh, final ChapterCardAdapter adapter) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pauseAudio(vh);
                AlertDialog dialog = new AlertDialog.Builder(mCtx)
                    .setTitle("Delete Chapter Recording?")
                    .setIcon(R.drawable.ic_delete_black_36dp)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            destroyAudioPlayer();
                            mChapterWav.delete();
                            mIsCompiled = false;
                            collapse(vh);
                            ProjectDatabaseHelper db = new ProjectDatabaseHelper(mCtx);
                            db.setCheckingLevel(mProject, mChapter, 0);
                            adapter.notifyItemChanged(vh.getAdapterPosition());
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

    public View.OnClickListener getPlayPauseOnClick(final ChapterCardAdapter.ViewHolder vh) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(vh.mPlayPauseBtn.isActivated()) {
                    pauseAudio(vh);
                } else {
                    playAudio(vh);
                }
            }
        };
    }
}
