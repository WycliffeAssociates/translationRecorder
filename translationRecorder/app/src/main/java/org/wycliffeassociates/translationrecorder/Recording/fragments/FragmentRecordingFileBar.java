package org.wycliffeassociates.translationrecorder.Recording.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.wycliffeassociates.translationrecorder.ProjectManager.Project;
import org.wycliffeassociates.translationrecorder.R;
import org.wycliffeassociates.translationrecorder.Recording.UnitPicker;
import org.wycliffeassociates.translationrecorder.Reporting.Logger;
import org.wycliffeassociates.translationrecorder.database.ProjectDatabaseHelper;
import org.wycliffeassociates.translationrecorder.project.Chunks;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by sarabiaj on 2/20/2017.
 */

public class FragmentRecordingFileBar extends Fragment {

    private static final int DEFAULT_CHAPTER = 1;
    private static final int DEFAULT_UNIT = 1;
    private static final String KEY_PROJECT = "key_project";

    private TextView mBookView;
    private TextView mSourceView;
    private TextView mLanguageView;
    private UnitPicker mUnitPicker;
    private UnitPicker mChapterPicker;
    private TextView mModeView;
    private Project mProject;
    private boolean isChunkMode;
    private Chunks mChunks;
    private List<Map<String, String>> mChunksList;
    private int mChapter = DEFAULT_CHAPTER;
    private int mUnit = DEFAULT_UNIT;
    private Handler mHandler;
    private String mStartVerse;
    private String mEndVerse;
    private int mNumChapters;

    public static FragmentRecordingFileBar newInstance(Project project){
        FragmentRecordingFileBar f = new FragmentRecordingFileBar();
        Bundle args = new Bundle();
        args.putParcelable(KEY_PROJECT, project);
        f.setArguments(args);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_recording_file_bar, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mHandler = new Handler(Looper.getMainLooper());
        findViews();
        loadArgs(getArguments());
        initializeViews();
        try {
            initializePickers();
        } catch (IOException e) {
            e.printStackTrace();
            Logger.e(this.toString(), "onViewCreate", e);
        }
    }

    private void loadArgs(Bundle args) {
        mProject = (Project) args.getParcelable(KEY_PROJECT);
    }

    private void findViews() {
        View view = getView();
        mBookView = (TextView) view.findViewById(R.id.file_book);
        mSourceView = (TextView) view.findViewById(R.id.file_project);
        mLanguageView = (TextView) view.findViewById(R.id.file_language);
        mUnitPicker = (UnitPicker) view.findViewById(R.id.unit_picker);
        mChapterPicker = (UnitPicker) view.findViewById(R.id.chapter_picker);
        mModeView = (TextView) view.findViewById(R.id.file_unit_label);
    }

    private void initializeViews() {
        //Logging to help track issue #669
        if(mProject.getSlug().equals("")) {
            Logger.e(this.toString(), "Project book is empty string " + mProject);
        }

        String languageCode = mProject.getTargetLanguage();
        mLanguageView.setText(languageCode.toUpperCase());
        mLanguageView.postInvalidate();

        String bookCode = mProject.getSlug();
        ProjectDatabaseHelper db = new ProjectDatabaseHelper(getActivity());
        String bookName = db.getBookName(bookCode);
        mBookView.setText(bookName);
        mBookView.postInvalidate();

        if (isChunkMode) {
            mModeView.setText("Chunk");
        } else {
            mModeView.setText("Verse");
        }

        mSourceView.setText(mProject.getVersion().toUpperCase());
    }

    private void initializePickers() throws IOException {
        if (mProject.isOBS()) {
            //mNumChapters = OBS_SIZE;
        } else {
            mChunks = new Chunks(getActivity(), mProject.getSlug());
            mNumChapters = mChunks.getNumChapters();
            mChunksList = mChunks.getChunks(mProject, mChapter);
        }
        initializeUnitPicker();
        initializeChapterPicker();
    }

    private void initializeUnitPicker() {
        final String[] values = new String[mChunksList.size()];
        if (isChunkMode) {
            setDisplayValuesAsRange(values);
        } else {
            for (int i = 0; i < mChunksList.size(); i++) {
                values[i] = mChunksList.get(i).get(Chunks.FIRST_VERSE);
            }
        }
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Logger.w(this.toString(), "Initializing unit picker");
                if (values != null && values.length > 0) {
                    mUnitPicker.setDisplayedValues(values);
                    mUnitPicker.setCurrent(getChunkIndex(mChunksList, mUnit));
                    setChunk(getChunkIndex(mChunksList, mUnit) + 1);
                    //reinitialize all of the filenames
                    mUnitPicker.setOnValueChangedListener(new UnitPicker.OnValueChangeListener() {
                        @Override
                        public void onValueChange(UnitPicker picker, int oldVal, int newVal) {
                            Logger.w(this.toString(), "User changed unit");
                            setChunk(newVal + 1);
//                            mSrcPlayer.reset(mProject, FileNameExtractor.getNameFromProject(mProject, mChapter,
//                                    Integer.parseInt(mStartVerse), Integer.parseInt(mEndVerse)), mChapter);
                        }
                    });
                } else {
                    Logger.e(this.toString(), "values was null or of zero length");
                }
            }
        });
    }

    private void initializeChapterPicker() {
        Logger.w(this.toString(), "Initializing chapter picker");
        int numChapters = mChunks.getNumChapters();
        final String[] values = new String[numChapters];
        for (int i = 0; i < numChapters; i++) {
            values[i] = String.valueOf(i + 1);
        }
        if (values != null && values.length > 0) {
            mChapterPicker.setDisplayedValues(values);
            mChapterPicker.setCurrent(mChapter - 1);
            mChapterPicker.setOnValueChangedListener(new UnitPicker.OnValueChangeListener() {
                @Override
                public void onValueChange(UnitPicker picker, int oldVal, int newVal) {
                    Logger.w(this.toString(), "User changed chapter");
                    mUnit = 1;
                    mChapter = newVal + 1;
                    mUnitPicker.setCurrent(0);
                    mChunksList = mChunks.getChunks(mProject, mChapter);
                    initializeUnitPicker();
//                    mSrcPlayer.reset(mProject, FileNameExtractor.getNameFromProject(mProject, mChapter,
//                            Integer.parseInt(mStartVerse), Integer.parseInt(mEndVerse)), mChapter);
                }
            });
        } else {
            Logger.e(this.toString(), "values was null or of zero length");
        }
    }

    private int getChunkIndex(List<Map<String, String>> chunks, int chunk) {
        for (int i = 0; i < chunks.size(); i++) {
            if (Integer.parseInt(chunks.get(i).get(Chunks.FIRST_VERSE)) == chunk) {
                return i;
            }
        }
        return 1;
    }

    private void setDisplayValuesAsRange(String[] values) {
        Map<String, String> chunk;
        String firstVerse, lastVerse;

        for (int i = 0; i < mChunksList.size(); i++) {
            chunk = mChunksList.get(i);
            firstVerse = chunk.get(Chunks.FIRST_VERSE);
            lastVerse = chunk.get(Chunks.LAST_VERSE);
            if (firstVerse.compareTo(lastVerse) == 0) {
                values[i] = firstVerse;
            } else {
                values[i] = firstVerse.concat("-").concat(lastVerse);
            }
        }
    }

    /**
     * Sets the chunk by indexing the chunk list with the provided index
     *
     * @param idx
     */
    private void setChunk(int idx) {
        if (mChunks != null) {
            mStartVerse = mChunksList.get(idx - 1).get(Chunks.FIRST_VERSE);
            mEndVerse = mChunksList.get(idx - 1).get(Chunks.LAST_VERSE);
            mUnit = Integer.parseInt(mStartVerse);
        }
    }

    public String getStartVerse() {
        return mStartVerse;
    }

    public String getEndVerse() {
        return mEndVerse;
    }

    public void disablePickers(){
        mUnitPicker.displayIncrementDecrement(false);
        mChapterPicker.displayIncrementDecrement(false);
    }
}
