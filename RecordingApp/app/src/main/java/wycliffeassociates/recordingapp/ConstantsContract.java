package wycliffeassociates.recordingapp;

import android.provider.BaseColumns;

/**
 * Created by sarabiaj on 6/28/2016.
 */
public final class ConstantsContract {

    /**
     * Created by sarabiaj on 5/19/2016.
     */

    public ConstantsContract() {}

    public static abstract class ConstantsEntry implements BaseColumns {
        public static final String TABLE_LANGUAGES = "languages";
        public static final String TABLE_BOOKS = "books";
        public static final String KEY_ID = "key_id";
        public static final String COLUMN_LANGUAGE = "key_language";
        public static final String COLUMN_LANGUAGE_CODE = "key_language_code";
        public static final String COLUMN_SLUG = "key_slug";
        public static final String COLUMN_BOOK_NUMBER = "key_book_number";
        public static final String COLUMN_BOOK = "key_book";
    }

    public static final String TEXT = " TEXT";
    public static final String INTEGER = " INTEGER";
    public static final String COMMA = ",";
    public static final String TEXTCOMMA = " TEXT,";

    public static final String CREATE_LANGUAGE_TABLE = "CREATE TABLE " + ConstantsEntry.TABLE_LANGUAGES + " ("
            + ConstantsEntry._ID + " INTEGER PRIMARY KEY,"
            + ConstantsEntry.COLUMN_LANGUAGE + TEXTCOMMA
            + ConstantsEntry.COLUMN_LANGUAGE_CODE + TEXT
            + " )";

    public static final String DELETE_LANGUAGES = "DROP TABLE IF EXISTS " + ConstantsEntry.TABLE_LANGUAGES;

    public static final String CREATE_BOOK_TABLE = "CREATE TABLE " + ConstantsEntry.TABLE_BOOKS + " ("
            + ConstantsEntry._ID + " INTEGER PRIMARY KEY,"
            + ConstantsEntry.COLUMN_BOOK + TEXTCOMMA
            + ConstantsEntry.COLUMN_SLUG + TEXTCOMMA
            + ConstantsEntry.COLUMN_BOOK_NUMBER + INTEGER
            + " )";

    public static final String DELETE_BOOKS = "DROP TABLE IF EXISTS " + ConstantsEntry.TABLE_BOOKS;
}
