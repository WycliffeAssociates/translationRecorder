package wycliffeassociates.recordingapp.Playback.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import wycliffeassociates.recordingapp.R;
import wycliffeassociates.recordingapp.widgets.FourStepImageView;

/**
 * Created by sarabiaj on 11/4/2016.
 */

public class FragmentFileBar extends Fragment {

    public static String KEY_LANGUAGE = "language";
    public static String KEY_VERSION = "version";
    public static String KEY_BOOK = "book";
    public static String KEY_CHAPTER_LABEL = "chapter_label";
    public static String KEY_CHAPTER_NUMBER = "chapter_number";
    public static String KEY_UNIT_LABEL = "unit";
    public static String KEY_UNIT_NUMBER = "unit_number";

    private FourStepImageView mRateBtn;

    TextView mLangView, mSourceView, mBookView, mChapterView, mChapterLabel, mUnitView, mUnitLabel;
    ImageView mRerecordBtn, mInsertBtn;

    public static FragmentFileBar newInstance(String language, String version, String book, String chapterLabel,
                                              String chapterNumber, String unitLabel, String unitNumber){
        FragmentFileBar f = new FragmentFileBar();
        Bundle args = new Bundle();
        args.putString(KEY_LANGUAGE, language);
        args.putString(KEY_VERSION, version);
        args.putString(KEY_BOOK, book);
        args.putString(KEY_CHAPTER_LABEL, chapterLabel);
        args.putString(KEY_CHAPTER_NUMBER, chapterNumber);
        args.putString(KEY_UNIT_LABEL, unitLabel);
        args.putString(KEY_UNIT_NUMBER, unitNumber);
        f.setArguments(args);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        return inflater.inflate(R.layout.fragment_file_view, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findViews();
        setText();
    }

    private void setText(){
        Bundle args = getArguments();
        mLangView.setText(args.getString(KEY_LANGUAGE));
        mSourceView.setText(args.getString(KEY_VERSION));
        mBookView.setText(args.getString(KEY_BOOK));
        mChapterLabel.setText(args.getString(KEY_CHAPTER_LABEL));
        mChapterView.setText(args.getString(KEY_CHAPTER_NUMBER));
        mUnitLabel.setText(args.getString(KEY_UNIT_LABEL));
        mUnitView.setText(args.getString(KEY_UNIT_NUMBER));
    }

    private void findViews(){
        View view = getView();
        mLangView = (TextView) view.findViewById(R.id.file_language);
        mSourceView = (TextView) view.findViewById(R.id.file_project);
        mBookView = (TextView) view.findViewById(R.id.file_book);
        mChapterView = (TextView) view.findViewById(R.id.file_chapter);
        mChapterLabel = (TextView) view.findViewById(R.id.file_chapter_label);
        mUnitView = (TextView) view.findViewById(R.id.file_unit);
        mUnitLabel = (TextView) view.findViewById(R.id.file_unit_label);
        mRerecordBtn = (ImageView) view.findViewById(R.id.btn_rerecord);
        mInsertBtn = (ImageView) view.findViewById(R.id.btn_insert_record);
        mRateBtn = (FourStepImageView) view.findViewById(R.id.rateTakeBtn);

//        mEnterVerseMarkerMode = (ImageButton) findViewById(R.id.btn_enter_verse_marker_mode);
//        mExitVerseMarkerMode = (ImageButton) findViewById(R.id.btn_exit_verse_marker_mode);
    }
}
