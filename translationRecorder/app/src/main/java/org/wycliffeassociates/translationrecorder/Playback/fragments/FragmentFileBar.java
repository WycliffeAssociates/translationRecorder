package org.wycliffeassociates.translationrecorder.Playback.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import org.wycliffeassociates.translationrecorder.Playback.interfaces.VerseMarkerModeToggler;
import org.wycliffeassociates.translationrecorder.R;
import org.wycliffeassociates.translationrecorder.Utils;
import org.wycliffeassociates.translationrecorder.chunkplugin.ChunkPlugin;
import org.wycliffeassociates.translationrecorder.widgets.FourStepImageView;

/**
 * Created by sarabiaj on 11/4/2016.
 */

public class FragmentFileBar extends Fragment {

    private InsertCallback mInsertCallback;
    private ChunkPlugin.TYPE mUnitType;

    public void onRatingChanged(int mRating) {
        mRateBtn.setStep(mRating);
    }

    public interface RerecordCallback {
        void onRerecord();
    }

    public interface RatingCallback {
        void onOpenRating(FourStepImageView view);
    }

    public interface InsertCallback {
        void onInsert();
    }

    public static String KEY_LANGUAGE = "language";
    public static String KEY_VERSION = "version";
    public static String KEY_BOOK = "book";
    public static String KEY_CHAPTER_LABEL = "chapter_label";
    public static String KEY_CHAPTER_NUMBER = "chapter_number";
    public static String KEY_UNIT_LABEL = "unit";
    public static String KEY_UNIT_NUMBER = "unit_number";
    public static String KEY_UNIT_TYPE = "unit_type";

    private FourStepImageView mRateBtn;

    TextView mLangView, mSourceView, mBookView, mChapterView, mChapterLabel, mUnitView, mUnitLabel;
    ImageView mRerecordBtn, mInsertBtn;
    private ImageButton mEnterVerseMarkerMode;


    private VerseMarkerModeToggler mModeToggleCallback;
    private RatingCallback mRatingCallback;
    private RerecordCallback mRerecordCallback;

    public static FragmentFileBar newInstance(String language, String version, String book, String chapterLabel,
                                              String chapterNumber, String unitLabel, String unitNumber,
                                              ChunkPlugin.TYPE unitType){
        FragmentFileBar f = new FragmentFileBar();
        Bundle args = new Bundle();
        args.putString(KEY_LANGUAGE, language.toUpperCase());
        args.putString(KEY_VERSION, version.toUpperCase());
        args.putString(KEY_BOOK, book.toUpperCase());
        args.putString(KEY_CHAPTER_LABEL, chapterLabel);
        args.putString(KEY_CHAPTER_NUMBER, chapterNumber);
        args.putString(KEY_UNIT_LABEL, Utils.capitalizeFirstLetter(unitLabel));
        args.putString(KEY_UNIT_NUMBER, unitNumber);
        args.putSerializable(KEY_UNIT_TYPE, unitType);
        f.setArguments(args);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_file_view, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findViews();
        setText();
        setClickListeners();
        if(mUnitType == ChunkPlugin.TYPE.SINGLE) {
            mEnterVerseMarkerMode.setVisibility(View.GONE);
        }
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
        mUnitType = (ChunkPlugin.TYPE) args.getSerializable(KEY_UNIT_TYPE);
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
        mRateBtn = (FourStepImageView) view.findViewById(R.id.btn_rate);

        mEnterVerseMarkerMode = (ImageButton) view.findViewById(R.id.btn_enter_verse_marker_mode);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mModeToggleCallback = (VerseMarkerModeToggler) activity;
        mRerecordCallback = (RerecordCallback) activity;
        mRatingCallback = (RatingCallback) activity;
        mInsertCallback = (InsertCallback) activity;
    }

    private void setClickListeners(){
        mEnterVerseMarkerMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mModeToggleCallback.onEnableVerseMarkerMode();
            }
        });

        mRateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRatingCallback.onOpenRating(mRateBtn);
            }
        });

        mRerecordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRerecordCallback.onRerecord();
            }
        });
        mInsertBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mInsertCallback.onInsert();
            }
        });
    }
}
