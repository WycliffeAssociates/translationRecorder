package wycliffeassociates.recordingapp.ProjectManager;

import android.media.Rating;
import android.provider.BaseColumns;
import static wycliffeassociates.recordingapp.ProjectManager.ProjectContract.ProjectEntry.TABLE_PROJECT;
import static wycliffeassociates.recordingapp.ProjectManager.ProjectContract.ProjectEntry.TABLE_TAKES;
import static wycliffeassociates.recordingapp.ProjectManager.ProjectContract.ProjectEntry.COLUMN_TARGET_LANG;
import static wycliffeassociates.recordingapp.ProjectManager.ProjectContract.ProjectEntry.COLUMN_SLUG;
import static wycliffeassociates.recordingapp.ProjectManager.ProjectContract.ProjectEntry.COLUMN_SOURCE;
import static wycliffeassociates.recordingapp.ProjectManager.ProjectContract.ProjectEntry.COLUMN_PROJECT;
import static wycliffeassociates.recordingapp.ProjectManager.ProjectContract.ProjectEntry.COLUMN_MODE;
import static wycliffeassociates.recordingapp.ProjectManager.ProjectContract.ProjectEntry.COLUMN_SOURCE_LANG;
import static wycliffeassociates.recordingapp.ProjectManager.ProjectContract.ProjectEntry.COLUMN_CHAPTER;
import static wycliffeassociates.recordingapp.ProjectManager.ProjectContract.ProjectEntry.COLUMN_START_VS;
import static wycliffeassociates.recordingapp.ProjectManager.ProjectContract.ProjectEntry.COLUMN_END_VS;
import static wycliffeassociates.recordingapp.ProjectManager.ProjectContract.ProjectEntry.COLUMN_RATING;
import static wycliffeassociates.recordingapp.ProjectManager.ProjectContract.ProjectEntry.COLUMN_CHECKING;
import static wycliffeassociates.recordingapp.ProjectManager.ProjectContract.ProjectEntry.COLUMN_TAKE_NUM;

/**
 * Created by sarabiaj on 5/19/2016.
 */
public final class ProjectContract {

    public ProjectContract() {}

    public static abstract class ProjectEntry implements BaseColumns{
        public static final String TABLE_PROJECT = "projects";
        public static final String KEY_ID = "key_id";
        public static final String COLUMN_TARGET_LANG = "key_target_lang";
        public static final String COLUMN_SOURCE_LANG = "key_source_lang";
        public static final String COLUMN_SLUG = "key_slug";
        public static final String COLUMN_BOOK_NUM = "key_book_num";
        public static final String COLUMN_SOURCE = "key_source";
        public static final String COLUMN_PROJECT= "key_project";
        public static final String COLUMN_MODE = "key_mode";
        public static final String COLUMN_CONTRIBUTORS = "key_contributors";
        public static final String COLUMN_SOURCE_AUDIO_PATH = "key_source_audio_path";
        public static final String TABLE_TAKES = "takes";
        public static final String COLUMN_CHAPTER = "key_chapter";
        public static final String COLUMN_START_VS = "key_start_vs";
        public static final String COLUMN_END_VS = "key_end_vs";
        public static final String COLUMN_RATING = "key_rating";
        public static final String COLUMN_CHECKING = "key_checking";
        public static final String COLUMN_TAKE_NUM = "key_take_num";
    }

    public static final String TEXT = " TEXT";
    public static final String INTEGER = " INTEGER";
    public static final String COMMA = ",";
    public static final String TEXTCOMMA = " TEXT,";
    public static final String INTCOMMA = " INTEGER,";

    public static final String CREATE_PROFILE_TABLE = "CREATE TABLE " + ProjectEntry.TABLE_PROJECT + " ("
            + ProjectEntry._ID + " INTEGER PRIMARY KEY,"
            + ProjectEntry.COLUMN_TARGET_LANG + TEXTCOMMA
            + ProjectEntry.COLUMN_SOURCE_LANG + TEXTCOMMA
            + ProjectEntry.COLUMN_SLUG + TEXTCOMMA
            + ProjectEntry.COLUMN_BOOK_NUM + TEXTCOMMA
            + ProjectEntry.COLUMN_SOURCE + TEXTCOMMA
            + ProjectEntry.COLUMN_PROJECT + TEXTCOMMA
            + ProjectEntry.COLUMN_MODE + TEXTCOMMA
            + ProjectEntry.COLUMN_CONTRIBUTORS + TEXTCOMMA
            + ProjectEntry.COLUMN_SOURCE_AUDIO_PATH + TEXT
            + " )";

    public static final String CREATE_TAKES_TABLE = "CREATE TABLE " + TABLE_TAKES + " ("
            + ProjectEntry._ID + " INTEGER PRIMARY KEY,"
            + COLUMN_TARGET_LANG + TEXTCOMMA
            + COLUMN_SLUG + TEXTCOMMA
            + COLUMN_SOURCE + TEXTCOMMA
            + COLUMN_MODE + TEXTCOMMA
            + COLUMN_CHAPTER + INTCOMMA
            + COLUMN_START_VS + INTCOMMA
            + COLUMN_END_VS + INTCOMMA
            + COLUMN_TAKE_NUM + INTCOMMA
            + COLUMN_RATING + INTCOMMA
            + COLUMN_CHECKING + INTEGER
            + " )";

    public static final String DELETE_ENTRIES = "DROP TABLE IF EXISTS " + ProjectEntry.TABLE_PROJECT;
    public static final String DELETE_TAKES = "DROP TABLE IF EXISTS " + ProjectEntry.TABLE_TAKES;
}

