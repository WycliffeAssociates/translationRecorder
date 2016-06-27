package wycliffeassociates.recordingapp.ProjectManager;

import android.provider.BaseColumns;

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
    }

    public static final String TEXT = " TEXT";
    public static final String COMMA = ",";
    public static final String TEXTCOMMA = " TEXT,";

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

    public static final String DELETE_ENTRIES = "DROP TABLE IF EXISTS " + ProjectEntry.TABLE_PROJECT;
}

