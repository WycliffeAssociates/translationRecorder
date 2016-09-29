package wycliffeassociates.recordingapp.widgets;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.support.v7.app.AlertDialog;
import android.view.View;

import java.io.File;
import java.lang.ref.SoftReference;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import wycliffeassociates.recordingapp.FilesPage.FileNameExtractor;
import wycliffeassociates.recordingapp.Playback.PlaybackScreen;
import wycliffeassociates.recordingapp.ProjectManager.Project;
import wycliffeassociates.recordingapp.ProjectManager.ProjectDatabaseHelper;
import wycliffeassociates.recordingapp.ProjectManager.RatingDialog;
import wycliffeassociates.recordingapp.ProjectManager.UnitCardAdapter;
import wycliffeassociates.recordingapp.R;
import wycliffeassociates.recordingapp.Recording.RecordingScreen;
import wycliffeassociates.recordingapp.Recording.WavFile;
import wycliffeassociates.recordingapp.Reporting.Logger;
import wycliffeassociates.recordingapp.Utils;

/**
 * Created by leongv on 7/28/2016.
 */
public class UnitCard {

    public static int NO_TAKES = -1;
    public static int MIN_TAKE_THRESHOLD = 2;

    public interface OnClickListener extends View.OnClickListener {
        void onClick(View v, UnitCardAdapter.ViewHolder vh, List<Integer> expandedCards, int position);
    }

    // Constants
    //public static boolean RATING_MODE = true;
    //public static boolean CHECKING_MODE = false;

    // State
    private boolean mIsExpanded = false;
    private int mTakeIndex = 0;
    private boolean mIsEmpty = true;
    private int mCurrentTakeRating;

    // Attributes
    private UnitCardAdapter.ViewHolder mViewHolder;
    private String mTitle;
    private final Project mProject;
    private final int mChapter;
    private final int mFirstVerse;
    private final int mEndVerse;
    private int mTakeCount;
    private int mSelectedTake;
    private SoftReference<List<File>> mTakeList;
    private SoftReference<AudioPlayer> mAudioPlayer;
    private Activity mCtx;
    private Resources.Theme mTheme;


    // Constructors
    public UnitCard(Activity ctx, Project project, int chapter, int firstVerse, int endVerse) {
        mTitle = Utils.capitalizeFirstLetter(project.getMode()) + " " + firstVerse;
        mFirstVerse = firstVerse;
        mEndVerse = endVerse;
        mChapter = chapter;
        mProject = project;
        mCtx = ctx;
        mTheme = mCtx.getTheme();
        refreshTakeCount();
    }


    // Setters
    public void setTitle(String title) {
        mTitle = title;
    }

    public void setViewHolder(UnitCardAdapter.ViewHolder vh) {
        mViewHolder = vh;
    }


    // Getters
    public String getTitle() {
        return mTitle;
    }

    public int getTakeCount() {
        return mTakeCount;
    }

    public int getStartVerse() {
        return mFirstVerse;
    }

    public boolean isExpanded() {
        return mIsExpanded;
    }

    public boolean isEmpty() {
        return mIsEmpty;
    }


    // Private Methods
    private AudioPlayer initializeAudioPlayer() {
        AudioPlayer ap = new AudioPlayer();
        if (mViewHolder != null) {
            ap.refreshView(mViewHolder.elapsed, mViewHolder.duration, mViewHolder.takePlayPauseBtn, mViewHolder.seekBar);
        }
        mAudioPlayer = new SoftReference<AudioPlayer>(ap);
        return ap;
    }

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

    private void refreshAudioPlayer() {
        AudioPlayer ap = getAudioPlayer();
        if (!ap.isLoaded()) {
            ap.reset();
            List<File> takes = getTakeList();
            if (mTakeIndex < takes.size()) {
                ap.loadFile(getTakeList().get(mTakeIndex));
            }
        }
        ap.refreshView(mViewHolder.elapsed, mViewHolder.duration, mViewHolder.takePlayPauseBtn, mViewHolder.seekBar);
    }

    private List<File> getTakeList() {
        List<File> takes = null;
        if (mTakeList != null) {
            takes = mTakeList.get();
        }
        if (takes == null) {
            takes = populateTakeList();
        }
        return takes;
    }

    private List<File> populateTakeList() {
        File root = Project.getProjectDirectory(mProject);
        String chap = FileNameExtractor.chapterIntToString(mProject, mChapter);
        File folder = new File(root, chap);
        File[] files = folder.listFiles();
        FileNameExtractor fne;
        int first = mFirstVerse;
        int end = mEndVerse;
        if (mProject.getMode().equals("verse") || first == end) {
            end = -1;
        }
        //Get only the files of the appropriate unit
        List<File> resultFiles = new ArrayList<>();
        if (files != null) {
            for (File file : files) {
                fne = new FileNameExtractor(file);
                if (fne.getStartVerse() == first && fne.getEndVerse() == end) {
                    resultFiles.add(file);
                }
            }
        }
        Collections.sort(resultFiles, new Comparator<File>() {
            @Override
            public int compare(File f, File s) {
                Long first = f.lastModified();
                Long second = s.lastModified();
                return first.compareTo(second);
            }
        });
        mTakeList = new SoftReference<>(resultFiles);
        return resultFiles;
    }

    private void refreshTakes() {
        //if the soft reference still has the takes, cool, if not, repopulate them
        List<File> takes = getTakeList();
        refreshTakeText(takes);
        if (takes.size() > 0) {
            File take = takes.get(mTakeIndex);
            refreshTakeRating(take);
            refreshSelectedTake(take);
        }
    }

    private void refreshSelectedTake(File take) {
        if (mViewHolder != null) {
            ProjectDatabaseHelper db = new ProjectDatabaseHelper(mCtx);
            FileNameExtractor fne = new FileNameExtractor(take);
            int chosen = db.getSelectedTakeNumber(fne);
            mViewHolder.takeSelectBtn.setActivated(chosen == fne.getTake());
            db.close();
        }
    }

    private void refreshTakeRating(File take) {
        ProjectDatabaseHelper db = new ProjectDatabaseHelper(mCtx);
        FileNameExtractor fne = new FileNameExtractor(take);
        Logger.w(this.toString(), "Refreshing take rating for " + take.getName());
        mCurrentTakeRating = db.getTakeRating(fne);
        if (mViewHolder != null) {
            mViewHolder.takeRatingBtn.setStep(mCurrentTakeRating);
            mViewHolder.takeRatingBtn.invalidate();
        }
        db.close();
    }

    private void refreshTakeText(List<File> takes) {
        if (mViewHolder == null) {
            return;
        }

        final String text;
        if (takes.size() > 0) {
            text = "Take " + (mTakeIndex + 1) + " of " + takes.size();
            long created = takes.get(mTakeIndex).lastModified();
            mViewHolder.currentTakeTimeStamp.setText(convertTime(created));
        } else {
            text = "Take 0 of " + takes.size();
            mViewHolder.currentTakeTimeStamp.setText("");
        }
        mViewHolder.currentTake.setText(text);
        mViewHolder.currentTake.invalidate();
    }

    private String convertTime(long time) {
        Date date = new Date(time);
        Format format = new SimpleDateFormat("MMMM d, yyyy  HH:mm ");
        return format.format(date);
    }


    // Public API
    public void refreshUnitStarted(Project project, int chapter, int startVerse) {
        File dir = Project.getProjectDirectory(project);
        String chapterString = FileNameExtractor.chapterIntToString(project, chapter);
        File chapterDir = new File(dir, chapterString);
        if (chapterDir.exists()) {
            File[] files = chapterDir.listFiles();
            if (files != null) {
                for (File f : files) {
                    FileNameExtractor fne = new FileNameExtractor(f);
                    if (fne.getStartVerse() == startVerse) {
                        mIsEmpty = false;
                        return;
                    }
                }
            }
        }
        mIsEmpty = true;
    }

    public void refreshTakeCount() {
        ProjectDatabaseHelper db = new ProjectDatabaseHelper(mCtx);
        if (db.unitExists(mProject, mChapter, mFirstVerse)) {
            int unitId = db.getUnitId(mProject, mChapter, mFirstVerse);
            mTakeCount = db.getTakeCount(unitId);
            db.close();
        } else {
            mTakeCount = NO_TAKES;
        }
    }

    public void expand() {
        refreshTakes();
        refreshAudioPlayer();
        mIsExpanded = true;
        if (mViewHolder != null) {
            mViewHolder.takeCountContainer.setVisibility(View.GONE);
            mViewHolder.cardBody.setVisibility(View.VISIBLE);
            mViewHolder.cardFooter.setVisibility(View.VISIBLE);
            mViewHolder.unitActions.setActivated(true);
        }
    }

    public void collapse() {
        mIsExpanded = false;
        if (mViewHolder != null) {
            mViewHolder.takeCountContainer.setVisibility(mTakeCount >= MIN_TAKE_THRESHOLD ? View.VISIBLE : View.GONE);
            mViewHolder.cardBody.setVisibility(View.GONE);
            mViewHolder.cardFooter.setVisibility(View.GONE);
            mViewHolder.unitActions.setActivated(false);
        }
    }

    public void raise() {
        if (mViewHolder == null) {
            return;
        }
        mViewHolder.cardView.setCardElevation(8f);
        mViewHolder.cardContainer.setBackgroundColor(mCtx.getResources().getColor(R.color.accent));
        mViewHolder.unitTitle.setTextColor(mCtx.getResources().getColor(R.color.text_light));
        mViewHolder.unitActions.setEnabled(false);
    }

    public void drop() {
        if (mViewHolder == null) {
            return;
        }
        mViewHolder.cardView.setCardElevation(2f);
        mViewHolder.cardContainer.setBackgroundColor(mCtx.getResources().getColor(R.color.card_bg));
        mViewHolder.unitTitle.setTextColor(
                mCtx.getResources().getColor((isEmpty()) ? R.color.primary_text_disabled_material_light : R.color.primary_text_default_material_light)
        );
        mViewHolder.unitActions.setEnabled(true);
    }

    public void playAudio() {
        getAudioPlayer().play();
    }

    public void pauseAudio() {
        getAudioPlayer().pause();
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

    public View.OnClickListener getUnitRecordOnClick() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pauseAudio();
                Project.loadProjectIntoPreferences(mCtx, mProject);
                view.getContext().startActivity(RecordingScreen.getNewRecordingIntent(mCtx, mProject, mChapter, mFirstVerse));
            }
        };
    }

    public View.OnClickListener getUnitExpandOnClick(final int position, final List<Integer> expandedCards) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isExpanded()) {
                    expand();
                    if (!expandedCards.contains(position)) {
                        expandedCards.add(position);
                    }
                } else {
                    pauseAudio();
                    collapse();
                    if (expandedCards.contains(position)) {
                        expandedCards.remove(expandedCards.indexOf(position));
                    }
                }
            }
        };
    }

    public View.OnClickListener getTakeIncrementOnClick() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<File> takes = getTakeList();
                if (takes.size() > 0) {
                    mTakeIndex++;
                    if (mTakeIndex >= takes.size()) {
                        mTakeIndex = 0;
                    }
                    destroyAudioPlayer();
                    refreshTakes();
                    refreshAudioPlayer();
                }
            }
        };
    }

    public View.OnClickListener getTakeDecrementOnClick() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<File> takes = getTakeList();
                if (takes.size() > 0) {
                    mTakeIndex--;
                    if (mTakeIndex < 0) {
                        mTakeIndex = takes.size() - 1;
                    }
                    destroyAudioPlayer();
                    refreshTakes();
                    refreshAudioPlayer();
                }
            }
        };
    }

    public View.OnClickListener getTakeDeleteOnClick(final int position, final UnitCardAdapter adapter) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pauseAudio();
                final List<File> takes = getTakeList();
                if (takes.size() > 0) {
                    AlertDialog dialog = new AlertDialog.Builder(mCtx)
                            .setTitle("Delete take?")
                            .setIcon(R.drawable.ic_delete_black_36dp)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    File selectedFile = takes.get(mTakeIndex);
                                    FileNameExtractor fne = new FileNameExtractor(selectedFile);
                                    ProjectDatabaseHelper db = new ProjectDatabaseHelper(mCtx);
                                    db.deleteTake(fne);
                                    db.close();
                                    takes.get(mTakeIndex).delete();
                                    takes.remove(mTakeIndex);
                                    //keep the same index in the list, unless the one removed was the last take.
                                    if (mTakeIndex > takes.size() - 1) {
                                        mTakeIndex--;
                                        //make sure the index is not negative
                                        mTakeIndex = Math.max(mTakeIndex, 0);
                                    }
                                    refreshTakes();
                                    if (takes.size() > 0) {
                                        AudioPlayer ap = getAudioPlayer();
                                        ap.reset();
                                        ap.loadFile(takes.get(mTakeIndex));
                                    } else {
                                        mIsEmpty = true;
                                        collapse();
                                        destroyAudioPlayer();
                                        adapter.notifyItemChanged(position);
                                    }
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
            }
        };
    }

    public View.OnClickListener getTakeEditOnClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<File> takes = getTakeList();
                if (takes.size() > 0) {
                    pauseAudio();
                    WavFile wavFile = new WavFile(takes.get(mTakeIndex));
                    Intent intent = PlaybackScreen.getPlaybackIntent(v.getContext(), wavFile, mProject, mChapter, mFirstVerse);
                    v.getContext().startActivity(intent);
                }
            }
        };
    }

    public View.OnClickListener getTakePlayPauseOnClick() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mViewHolder.takePlayPauseBtn.isActivated()) {
                    pauseAudio();
                } else {
                    playAudio();
                }
            }
        };
    }

    public View.OnClickListener getTakeRatingOnClick() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<File> takes = getTakeList();
                if (takes.size() > 0) {
                    pauseAudio();
                    String name = takes.get(mTakeIndex).getName();
                    RatingDialog dialog = RatingDialog.newInstance(name, mCurrentTakeRating);
                    dialog.show(mCtx.getFragmentManager(), "single_take_rating");
                }
            }
        };
    }

    public View.OnClickListener getTakeSelectOnClick() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<File> takes = getTakeList();
                if (takes.size() > 0) {
                    ProjectDatabaseHelper db = new ProjectDatabaseHelper(mCtx);
                    FileNameExtractor fne = new FileNameExtractor(takes.get(mTakeIndex));
                    if (view.isActivated()) {
                        view.setActivated(false);
                        db.removeSelectedTake(fne);
                    } else {
                        view.setActivated(true);
                        db.setSelectedTake(fne);
                    }
                    db.close();
                }
            }
        };
    }

}
