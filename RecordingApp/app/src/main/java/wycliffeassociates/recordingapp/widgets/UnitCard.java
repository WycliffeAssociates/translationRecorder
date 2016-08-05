package wycliffeassociates.recordingapp.widgets;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.support.v4.util.Pair;
import android.support.v7.util.SortedList;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import wycliffeassociates.recordingapp.FilesPage.FileNameExtractor;
import wycliffeassociates.recordingapp.ProjectManager.Project;
import wycliffeassociates.recordingapp.ProjectManager.UnitCardAdapter;
import wycliffeassociates.recordingapp.R;
import wycliffeassociates.recordingapp.Recording.RecordingScreen;
import wycliffeassociates.recordingapp.Utils;

/**
 * Created by leongv on 7/28/2016.
 */
public class UnitCard {

    public interface OnClickListener extends View.OnClickListener{
        void onClick(View v, UnitCardAdapter.ViewHolder vh, List<Integer> expandedCards, int position);

    }

    // State
    private boolean mIsExpanded = false;
    private int mTakeIndex = 0;

    // Attributes
    private String mTitle;
    private Activity mCtx;
    private final Project mProject;
    private final String mChapter;
    private final String mFirstVerse;
    private final String mEndVerse;
    private SoftReference<List<File>> mTakeList;
    private SoftReference<AudioPlayer> mAudioPlayer;


    // Constructors
    public UnitCard(Activity ctx, Project project, String chapter, String firstVerse, String endVerse) {
        mTitle = Utils.capitalizeFirstLetter(project.getMode()) + " " + firstVerse;
        mFirstVerse = firstVerse;
        mEndVerse = endVerse;
        mChapter = chapter;
        mProject = project;
        mCtx = ctx;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getTitle() {
        return mTitle;
    }

    public void expand(UnitCardAdapter.ViewHolder vh) {
        refreshTakes(vh);
        refreshAudioPlayer(vh);
        vh.mCardBody.setVisibility(View.VISIBLE);
        vh.mCardFooter.setVisibility(View.VISIBLE);
        vh.mUnitPlayBtn.setVisibility(View.GONE);
        mIsExpanded = true;
    }

    private void refreshAudioPlayer(UnitCardAdapter.ViewHolder vh){
        AudioPlayer ap = getAudioPlayer(vh);
        ap.reset();
        List<File> takes = getTakeList();
        if(mTakeIndex < takes.size()) {
            ap.loadFile(getTakeList().get(mTakeIndex));
        }
    }

    private AudioPlayer initializeAudioPlayer(UnitCardAdapter.ViewHolder vh){
        AudioPlayer ap = new AudioPlayer(vh.mProgress, vh.mDuration, vh.mPlayTakeBtn, vh.mPauseTakeBtn, vh.mSeekBar);
        mAudioPlayer = new SoftReference<AudioPlayer>(ap);
        return ap;
    }

    private List<File> getTakeList(){
        List<File> takes = null;
        if(mTakeList != null){
            takes = mTakeList.get();
        }
        if(takes == null){
            takes = populateTakeList();
        }
        return takes;
    }

    private AudioPlayer getAudioPlayer(UnitCardAdapter.ViewHolder vh){
        AudioPlayer ap = null;
        if(mAudioPlayer != null){
            ap = mAudioPlayer.get();
        }
        if(ap == null){
            ap = initializeAudioPlayer(vh);
        }
        return ap;
    }

    public void collapse(UnitCardAdapter.ViewHolder vh) {
        vh.mCardBody.setVisibility(View.GONE);
        vh.mCardFooter.setVisibility(View.GONE);
        vh.mUnitPlayBtn.setVisibility(View.VISIBLE);
        mIsExpanded = false;
    }

    private void refreshTakes(UnitCardAdapter.ViewHolder vh){
        //if the soft reference still has the takes, cool, if not, repopulate them
        List<File> takes = getTakeList();
        refreshTakeText(takes, vh.mCurrentTake);
    }

    private void refreshTakeText(List<File> takes, final TextView takeView){
        final String text;
        if (takes.size() > 0) {
            text = "Take " + (mTakeIndex + 1) + " of " + takes.size();
        } else {
            text = "Take 0 of " + takes.size();
        }
        takeView.setText(text);
        takeView.invalidate();
    }

    private List<File> populateTakeList(){
        File root = Project.getProjectDirectory(mProject);
        String chap = String.valueOf(mChapter);
        if(chap.length() == 1){
            chap = "0" + chap;
        }
        File folder = new File(root, chap);
        File[] files = folder.listFiles();
        FileNameExtractor fne;
        int first = Integer.parseInt(mFirstVerse);
        int end = Integer.parseInt(mEndVerse);
        if(mProject.getMode().compareTo("verse") == 0){
            end = -1;
        }
        //Get only the files of the appropriate unit
        List<File> resultFiles = new ArrayList<>();
        if(files != null){
            for(File file : files){
                fne = new FileNameExtractor(file);
                if(fne.getStartVerse() == first && fne.getEndVerse() == end){
                    resultFiles.add(file);
                }
            }
        }
        Collections.sort(resultFiles, new Comparator<File>(){
            @Override
            public int compare(File f, File s){
                Long first = f.lastModified();
                Long second = s.lastModified();
                return first.compareTo(second);
            }
        });
        mTakeList = new SoftReference<>(resultFiles);
        return resultFiles;
    }

    public void raise(UnitCardAdapter.ViewHolder vh) {
        vh.mCardView.setCardElevation(8f);
        vh.mCardContainer.setBackgroundColor(mCtx.getResources().getColor(R.color.accent));
        vh.mUnitTitle.setTextColor(mCtx.getResources().getColor(R.color.text_light));

        // Different implementations that are based on API version. Kind off ridiculous.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            vh.mUnitRecordBtn.setImageDrawable(
                mCtx.getResources().getDrawable(R.drawable.ic_record, mCtx.getTheme())
            );
            vh.mUnitPlayBtn.setImageDrawable(
                mCtx.getResources().getDrawable(R.drawable.ic_play_white, mCtx.getTheme())
            );
        } else {
            vh.mUnitRecordBtn.setImageDrawable(
                mCtx.getResources().getDrawable(R.drawable.ic_record)
            );
            vh.mUnitPlayBtn.setImageDrawable(
                mCtx.getResources().getDrawable(R.drawable.ic_play_white)
            );
        }
    }

    public void drop(UnitCardAdapter.ViewHolder vh) {
        vh.mCardView.setCardElevation(2f);
        vh.mCardContainer.setBackgroundColor(mCtx.getResources().getColor(R.color.card_bg));
        vh.mUnitTitle.setTextColor(
            mCtx.getResources().getColor(R.color.primary_text_default_material_light)
        );

        // Different implementations that are based on API version. Possibly worse than browser
        // compatibility.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            vh.mUnitRecordBtn.setImageDrawable(
                mCtx.getResources().getDrawable(
                    R.drawable.ic_microphone_grey600_36dp, mCtx.getTheme()
                )
            );
            vh.mUnitPlayBtn.setImageDrawable(
                mCtx.getResources().getDrawable(R.drawable.ic_play_blue, mCtx.getTheme())
            );
        } else {
            vh.mUnitRecordBtn.setImageDrawable(
                mCtx.getResources().getDrawable(R.drawable.ic_microphone_grey600_36dp)
            );
            vh.mUnitPlayBtn.setImageDrawable(
                mCtx.getResources().getDrawable(R.drawable.ic_play_blue)
            );
        }
    }

    public boolean isExpanded() {
        return mIsExpanded;
    }

    public View.OnClickListener getUnitRecordOnClick(final Project project, final int chapter) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Project.loadProjectIntoPreferences(view.getContext(), project);
                int startVerse = Integer.parseInt(mFirstVerse);
                view.getContext().startActivity(RecordingScreen.getNewRecordingIntent(view.getContext(), project, chapter, startVerse));
            }
        };
    }

    public View.OnClickListener getIncrementTakeOnClickListener(final TextView takeView, final UnitCardAdapter.ViewHolder vh){
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<File> takes = getTakeList();
                if(takes.size() > 0) {
                    mTakeIndex++;
                    if (mTakeIndex >= takes.size()) {
                        mTakeIndex = 0;
                    }
                    refreshTakeText(takes, takeView);
                    refreshAudioPlayer(vh);
                }
            }
        };
    }

    public View.OnClickListener getDecrementTakeOnClickListener(final TextView takeView, final UnitCardAdapter.ViewHolder vh){
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<File> takes = getTakeList();
                if(takes.size() > 0) {
                    mTakeIndex--;
                    if (mTakeIndex < 0) {
                        mTakeIndex = takes.size() - 1;
                    }
                    refreshTakeText(takes, takeView);
                    refreshAudioPlayer(vh);
                }
            }
        };
    }

    public View.OnClickListener getPlayTakeOnClickListener(final UnitCardAdapter.ViewHolder vh){
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AudioPlayer ap = getAudioPlayer(vh);
                ap.play();
            }
        };
    }

    public View.OnClickListener getPauseTakeOnClickListener(final UnitCardAdapter.ViewHolder vh){
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AudioPlayer ap = getAudioPlayer(vh);
                ap.pause();
            }
        };
    }

}
