package org.wycliffeassociates.translationrecorder.widgets;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.view.View;

import com.door43.tools.reporting.Logger;

import org.wycliffeassociates.translationrecorder.Playback.PlaybackActivity;
import org.wycliffeassociates.translationrecorder.ProjectManager.adapters.UnitCardAdapter;
import org.wycliffeassociates.translationrecorder.ProjectManager.dialogs.RatingDialog;
import org.wycliffeassociates.translationrecorder.R;
import org.wycliffeassociates.translationrecorder.Recording.RecordingActivity;
import org.wycliffeassociates.translationrecorder.data.model.Project;
import org.wycliffeassociates.translationrecorder.data.model.ProjectFileUtils;
import org.wycliffeassociates.translationrecorder.data.model.ProjectPatternMatcher;
import org.wycliffeassociates.translationrecorder.project.TakeInfo;
import org.wycliffeassociates.translationrecorder.wav.WavFile;

import java.io.File;
import java.lang.ref.SoftReference;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Created by leongv on 7/28/2016.
 */
public class UnitCard {

    public interface DatabaseAccessor {
        void updateSelectedTake(TakeInfo takeInfo);
        int selectedTakeNumber(TakeInfo takeInfo);
        int takeCount(Project project, int chapter, int firstVerse);
        void deleteTake(TakeInfo takeInfo);
        void removeSelectedTake(TakeInfo takeInfo);
        void selectTake(TakeInfo takeInfo);
        int takeRating(TakeInfo takeInfo);
    }

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
    private SoftReference<List<File>> mTakeList;
    private SoftReference<AudioPlayer> mAudioPlayer;

    // Constructors
    public UnitCard(DatabaseAccessor db, Project project, String title, int chapter, int firstVerse, int endVerse) {
        mTitle = title;
        mFirstVerse = firstVerse;
        mEndVerse = endVerse;
        mChapter = chapter;
        mProject = project;
        refreshTakeCount(db);
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
        File root = ProjectFileUtils.getProjectDirectory(mProject);
        String chap = ProjectFileUtils.chapterIntToString(mProject, mChapter);
        File folder = new File(root, chap);
        File[] files = folder.listFiles();
        ProjectPatternMatcher ppm;
        int first = mFirstVerse;
        int end = mEndVerse;
        //Get only the files of the appropriate unit
        List<File> resultFiles = new ArrayList<>();
        if (files != null) {
            for (File file : files) {
                ppm = mProject.getPatternMatcher();
                ppm.match(file);
                if(ppm.matched()) {
                    TakeInfo ti = ppm.getTakeInfo();
                    if (ti != null && ti.getStartVerse() == first && ti.getEndVerse() == end) {
                        resultFiles.add(file);
                    }
                }
            }
        }
        Collections.sort(resultFiles, new Comparator<File>() {
            @Override
            public int compare(File f, File s) {
                ProjectPatternMatcher ppm = mProject.getPatternMatcher();
                ProjectPatternMatcher ppm2 = mProject.getPatternMatcher();
                ppm.match(f);
                TakeInfo takeInfo = ppm.getTakeInfo();
                ppm2.match(s);
                TakeInfo takeInfo2 = ppm.getTakeInfo();


//                Long first = f.lastModified();
//                Long second = s.lastModified();
                //Change to take name rather than last modified because editing verse markers modifies the file
                //this means that adding verse markers would change the postition in the list when returning to the card
                Integer first = takeInfo.getTake();
                Integer second = takeInfo2.getTake();
                return first.compareTo(second);
            }
        });
        mTakeList = new SoftReference<>(resultFiles);
        return resultFiles;
    }

    private void refreshTakes(DatabaseAccessor db) {
        //if the soft reference still has the takes, cool, if not, repopulate them
        List<File> takes = getTakeList();
        refreshTakeText(takes);
        if (takes.size() > 0) {
            File take = takes.get(mTakeIndex);
            refreshTakeRating(db, take);
            refreshSelectedTake(db, take);
        }
    }

    private void refreshSelectedTake(DatabaseAccessor db, File take) {
        if (mViewHolder != null) {
            ProjectPatternMatcher ppm = mProject.getPatternMatcher();
            ppm.match(take);
            TakeInfo takeInfo = ppm.getTakeInfo();
            int chosen = db.selectedTakeNumber(takeInfo);
            mViewHolder.takeSelectBtn.setActivated(chosen == takeInfo.getTake());
        }
    }

    private void refreshTakeRating(DatabaseAccessor db, File take) {
        ProjectPatternMatcher ppm = mProject.getPatternMatcher();
        ppm.match(take);
        TakeInfo takeInfo = ppm.getTakeInfo();
        Logger.w(this.toString(), "Refreshing take rating for " + take.getName());
        mCurrentTakeRating = db.takeRating(takeInfo);
        if (mViewHolder != null) {
            mViewHolder.takeRatingBtn.setStep(mCurrentTakeRating);
            mViewHolder.takeRatingBtn.invalidate();
        }
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
        File dir = ProjectFileUtils.getProjectDirectory(project);
        String chapterString = ProjectFileUtils.chapterIntToString(project, chapter);
        File chapterDir = new File(dir, chapterString);
        if (chapterDir.exists()) {
            File[] files = chapterDir.listFiles();
            if (files != null) {
                for (File f : files) {
                    ProjectPatternMatcher ppm = mProject.getPatternMatcher();
                    ppm.match(f);
                    if (ppm.matched()) {
                        TakeInfo takeInfo = ppm.getTakeInfo();
                        if (takeInfo != null && takeInfo.getStartVerse() == startVerse) {
                            mIsEmpty = false;
                            return;
                        }
                    }
                }
            }
        }
        mIsEmpty = true;
    }

    public void refreshTakeCount(DatabaseAccessor db) {
        //Need to check both chapter and unit first
        /*if (db.chapterExists(mProject, mChapter) && db.unitExists(mProject, mChapter, mFirstVerse)) {
            int unitId = db.getUnitId(mProject, mChapter, mFirstVerse);
            mTakeCount = db.getTakeCount(unitId);
            db.close();
        } else {
            mTakeCount = NO_TAKES;
        }*/
        mTakeCount = db.takeCount(mProject, mChapter, mFirstVerse);
    }

    public void expand(DatabaseAccessor db) {
        refreshTakes(db);
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

    public void raise(Context context) {
        if (mViewHolder == null) {
            return;
        }
        mViewHolder.cardView.setCardElevation(8f);
        mViewHolder.cardContainer.setBackgroundColor(context.getResources().getColor(R.color.accent));
        mViewHolder.unitTitle.setTextColor(context.getResources().getColor(R.color.text_light));
        mViewHolder.unitActions.setEnabled(false);
    }

    public void drop(Context context) {
        if (mViewHolder == null) {
            return;
        }
        mViewHolder.cardView.setCardElevation(2f);
        mViewHolder.cardContainer.setBackgroundColor(context.getResources().getColor(R.color.card_bg));
        mViewHolder.unitTitle.setTextColor(
                context.getResources().getColor((isEmpty()) ? R.color.primary_text_disabled_material_light : R.color.primary_text_default_material_light)
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

    public View.OnClickListener getUnitRecordOnClick(final Context context) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pauseAudio();
                mProject.loadProjectIntoPreferences(context);
                view.getContext().startActivity(RecordingActivity.Companion.getNewRecordingIntent(context, mProject, mChapter, mFirstVerse));
            }
        };
    }

    public View.OnClickListener getUnitExpandOnClick(final DatabaseAccessor db, final int position, final List<Integer> expandedCards) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isExpanded()) {
                    expand(db);
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

    public View.OnClickListener getTakeIncrementOnClick(final DatabaseAccessor db) {
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
                    refreshTakes(db);
                    refreshAudioPlayer();
                }
            }
        };
    }

    public View.OnClickListener getTakeDecrementOnClick(final DatabaseAccessor db) {
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
                    refreshTakes(db);
                    refreshAudioPlayer();
                }
            }
        };
    }

    public View.OnClickListener getTakeDeleteOnClick(final Context ctx, final DatabaseAccessor db, final int position, final UnitCardAdapter adapter) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pauseAudio();
                final List<File> takes = getTakeList();
                if (takes.size() > 0) {
                    AlertDialog dialog = new AlertDialog.Builder(ctx)
                            .setTitle("Delete take?")
                            .setIcon(R.drawable.ic_delete_black_36dp)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    File selectedFile = takes.get(mTakeIndex);
                                    ProjectPatternMatcher ppm = mProject.getPatternMatcher();
                                    ppm.match(selectedFile);
                                    TakeInfo takeInfo = ppm.getTakeInfo();
                                    db.deleteTake(takeInfo);
                                    takes.get(mTakeIndex).delete();
                                    takes.remove(mTakeIndex);
                                    //keep the same index in the list, unless the one removed was the last take.
                                    if (mTakeIndex > takes.size() - 1) {
                                        mTakeIndex--;
                                        //make sure the index is not negative
                                        mTakeIndex = Math.max(mTakeIndex, 0);
                                    }
                                    refreshTakes(db);
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
                    Intent intent = PlaybackActivity.getPlaybackIntent(v.getContext(), wavFile, mProject, mChapter, mFirstVerse);
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

    public View.OnClickListener getTakeRatingOnClick(final Activity context) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<File> takes = getTakeList();
                if (takes.size() > 0) {
                    pauseAudio();
                    String name = takes.get(mTakeIndex).getName();
                    ProjectPatternMatcher ppm = mProject.getPatternMatcher();
                    ppm.match(name);
                    TakeInfo takeInfo = ppm.getTakeInfo();
                    RatingDialog dialog = RatingDialog.newInstance(takeInfo, mCurrentTakeRating);
                    dialog.show(context.getFragmentManager(), "single_take_rating");
                }
            }
        };
    }

    public View.OnClickListener getTakeSelectOnClick(final DatabaseAccessor db) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<File> takes = getTakeList();
                if (takes.size() > 0) {
                    ProjectPatternMatcher ppm = mProject.getPatternMatcher();
                    ppm.match(takes.get(mTakeIndex));
                    TakeInfo takeInfo = ppm.getTakeInfo();
                    if (view.isActivated()) {
                        view.setActivated(false);
                        db.removeSelectedTake(takeInfo);
                    } else {
                        view.setActivated(true);
                        db.selectTake(takeInfo);
                    }
                }
            }
        };
    }

}
