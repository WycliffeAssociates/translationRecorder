package org.wycliffeassociates.translationrecorder.Recording.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.door43.tools.reporting.Logger;

import org.wycliffeassociates.translationrecorder.R;
import org.wycliffeassociates.translationrecorder.Recording.UnitPicker;
import org.wycliffeassociates.translationrecorder.Utils;
import org.wycliffeassociates.translationrecorder.chunkplugin.ChunkPlugin;
import org.wycliffeassociates.translationrecorder.database.ProjectDatabaseHelper;
import org.wycliffeassociates.translationrecorder.project.Project;

import java.io.IOException;

/**
 * Created by sarabiaj on 2/20/2017.
 */

public class FragmentRecordingFileBar extends Fragment {


    private static final String KEY_PROJECT = "key_project";
    public static final String KEY_CHAPTER = "key_chapter";
    public static final String KEY_UNIT = "key_unit";

    private TextView mBookView;
    private TextView mSourceView;
    private TextView mLanguageView;
    private UnitPicker mUnitPicker;
    private UnitPicker mChapterPicker;
    private TextView mModeView;
    private Project mProject;
    private ChunkPlugin mChunks;
    private int mChapter = ChunkPlugin.DEFAULT_CHAPTER;
    private int mUnit = ChunkPlugin.DEFAULT_UNIT;
    private Handler mHandler;

    private OnUnitChangedListener mOnUnitChangedListener;
    private FragmentRecordingControls.Mode mMode;

    public interface OnUnitChangedListener {
        void onUnitChanged(Project project, String fileName, int chapter);
    }

    public static FragmentRecordingFileBar newInstance(
            Project project,
            int chapter,
            int unit,
            FragmentRecordingControls.Mode mode
    ) {
        FragmentRecordingFileBar f = new FragmentRecordingFileBar();
        Bundle args = new Bundle();
        args.putParcelable(KEY_PROJECT, project);
        args.putInt(KEY_CHAPTER, chapter);
        args.putInt(KEY_UNIT, unit);
        f.setArguments(args);
        f.setMode(mode);
        return f;
    }

    private void setMode(FragmentRecordingControls.Mode mode) {
        mMode = mode;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof OnUnitChangedListener) {
            mOnUnitChangedListener = (OnUnitChangedListener) activity;
        } else {
            throw new RuntimeException(
                    "Attempted to attach activity which does not implement OnUnitChangedListener"
            );
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mOnUnitChangedListener = null;
    }

    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
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
            if (mMode == FragmentRecordingControls.Mode.INSERT_MODE) {
                disablePickers();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Logger.e(this.toString(), "onViewCreate", e);
        }
    }

    private void loadArgs(Bundle args) {
        mProject = (Project) args.getParcelable(KEY_PROJECT);
        mChapter = args.getInt(KEY_CHAPTER);
        mUnit = args.getInt(KEY_UNIT);
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
        if (mProject.getBookSlug().equals("")) {
            Logger.e(this.toString(), "Project book is empty string " + mProject);
        }

        String languageCode = mProject.getTargetLanguageSlug();
        mLanguageView.setText(languageCode.toUpperCase());
        mLanguageView.postInvalidate();

        String bookCode = mProject.getBookSlug();
        ProjectDatabaseHelper db = new ProjectDatabaseHelper(getActivity());
        String bookName = db.getBookName(bookCode);
        mBookView.setText(bookName);
        mBookView.postInvalidate();
        mModeView.setText(Utils.capitalizeFirstLetter(mProject.getModeName()));
        mSourceView.setText(mProject.getVersionSlug().toUpperCase());
    }

    private void initializePickers() throws IOException {
        mChunks = mProject.getChunkPlugin(getActivity());
        mChunks.parseChunks(mProject.getChunksFile(getActivity()));
        mChunks.initialize(mChapter, mUnit);
        initializeUnitPicker();
        initializeChapterPicker();
    }

    private void initializeChapterPicker() {
        final String[] values = mChunks.getChapterDisplayLabels();
        if (values != null && values.length > 0) {
            mChapterPicker.setDisplayedValues(values);
            mChapterPicker.setCurrent(mChunks.getChapterLabelIndex());
            mChapterPicker.setOnValueChangedListener(new UnitPicker.OnValueChangeListener() {
                @Override
                public void onValueChange(
                        UnitPicker picker,
                        int oldVal,
                        int newVal,
                        UnitPicker.DIRECTION direction
                ) {
                    if (direction == UnitPicker.DIRECTION.INCREMENT) {
                        mChunks.nextChapter();
                    } else {
                        mChunks.previousChapter();
                    }
                    mUnitPicker.setCurrent(0);
                    initializeUnitPicker();
                    mOnUnitChangedListener.onUnitChanged(
                            mProject,
                            mProject.getFileName(
                                    mChunks.getChapter(),
                                    mChunks.getStartVerse(),
                                    mChunks.getEndVerse()
                            ),
                            mChunks.getChapter()
                    );
                }
            });
        } else {
            Logger.e(this.toString(), "values was null or of zero length");
        }
    }

    private void initializeUnitPicker() {
        final String[] values = mChunks.getChunkDisplayLabels();
        mUnitPicker.setDisplayedValues(values);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (values != null && values.length > 0) {
                    mUnitPicker.setCurrent(mChunks.getStartVerseLabelIndex());
                    mOnUnitChangedListener.onUnitChanged(
                            mProject,
                            mProject.getFileName(
                                    mChunks.getChapter(),
                                    mChunks.getStartVerse(),
                                    mChunks.getEndVerse()
                            ),
                            mChunks.getChapter()
                    );
                    //reinitialize all of the filenames
                    mUnitPicker.setOnValueChangedListener(new UnitPicker.OnValueChangeListener() {
                        @Override
                        public void onValueChange(
                                UnitPicker picker,
                                int oldVal,
                                int newVal,
                                UnitPicker.DIRECTION direction
                        ) {
                            if (direction == UnitPicker.DIRECTION.INCREMENT) {
                                mChunks.nextChunk();
                            } else {
                                mChunks.previousChunk();
                            }
                            mOnUnitChangedListener.onUnitChanged(mProject,
                                    mProject.getFileName(
                                            mChunks.getChapter(),
                                            mChunks.getStartVerse(),
                                            mChunks.getEndVerse()
                                    ),
                                    mChunks.getChapter()
                            );
                        }
                    });
                } else {
                    Logger.e(this.toString(), "values was null or of zero length");
                }
            }
        });
    }

    public String getStartVerse() {
        return String.valueOf(mChunks.getStartVerse());
    }

    public String getEndVerse() {
        return String.valueOf(mChunks.getEndVerse());
    }

    public int getUnit() {
        return mChunks.getStartVerse();
    }

    public int getChapter() {
        return mChunks.getChapter();
    }

    public void disablePickers() {
        mUnitPicker.displayIncrementDecrement(false);
        mChapterPicker.displayIncrementDecrement(false);
    }
}
