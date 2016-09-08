package wycliffeassociates.recordingapp.ProjectManager;

import android.provider.BaseColumns;

/**
 * Created by sarabiaj on 5/19/2016.
 */
public final class ProjectContract {

    public ProjectContract() {}

    public static abstract class LanguageEntry implements BaseColumns {
        public static final String TABLE_LANGUAGE = "languages";
        public static final String LANGUAGE_CODE = "code";
        public static final String LANGUAGE_NAME = "name";
        public static final String LANGUAGE_UNIQUE_CONSTRAINT = "cols_unique";

        public static final String CREATE_LANGUAGE_TABLE = "CREATE TABLE " + TABLE_LANGUAGE + " ("
                + _ID + " INTEGER PRIMARY KEY,"
                + LANGUAGE_CODE + TEXTCOMMA
                + LANGUAGE_NAME + TEXTCOMMA
                + "CONSTRAINT " + LANGUAGE_UNIQUE_CONSTRAINT + " UNIQUE(" + LANGUAGE_CODE + ")"
                + ");";
    }

    public static abstract class BookEntry implements BaseColumns {
        public static final String TABLE_BOOK = "books";
        public static final String BOOK_SLUG = "slug";
        public static final String BOOK_NAME = "name";
        public static final String BOOK_NUMBER = "number";
        public static final String BOOK_ANTHOLOGY = "anthology";
        public static final String BOOK_UNIQUE_CONSTRAINT = "cols_unique";

        public static final String CREATE_BOOK_TABLE = "CREATE TABLE " + TABLE_BOOK + " ("
                + _ID + " INTEGER PRIMARY KEY,"
                + BOOK_SLUG + TEXTCOMMA
                + BOOK_NAME + TEXTCOMMA
                + BOOK_NUMBER + INTCOMMA
                + BOOK_ANTHOLOGY + TEXTCOMMA
                + "CONSTRAINT " + BOOK_UNIQUE_CONSTRAINT + " UNIQUE(" + BOOK_SLUG + ")"
                + ");";
    }

    public static abstract class ChapterEntry implements BaseColumns {
        public static final String TABLE_CHAPTER = "chapters";
        public static final String CHAPTER_PROJECT_FK = "project_fk";
        public static final String CHAPTER_NUMBER = "number";
        public static final String CHAPTER_NOTES = "notes";
        public static final String CHAPTER_PROGRESS = "progress";
        public static final String CHAPTER_CHECKING_LEVEL = "checking";
        public static final String CHAPTER_UNIQUE_CONSTRAINT = "cols_unique";

        public static final String CREATE_CHAPTER_TABLE = "CREATE TABLE " + TABLE_CHAPTER + " ("
                + _ID + " INTEGER PRIMARY KEY,"
                + CHAPTER_PROJECT_FK + INTCOMMA
                + CHAPTER_NUMBER + INTCOMMA
                + CHAPTER_NOTES + TEXTCOMMA
                + CHAPTER_PROGRESS + INTCOMMA
                + CHAPTER_CHECKING_LEVEL + INTCOMMA
                + "FOREIGN KEY(" + CHAPTER_PROJECT_FK + ") REFERENCES " + ProjectEntry.TABLE_PROJECT + "(" + _ID + ")"
                + "CONSTRAINT " + CHAPTER_UNIQUE_CONSTRAINT + " UNIQUE(" + CHAPTER_PROJECT_FK + "," +  CHAPTER_NUMBER + ")"
                + ");";
    }

    public static abstract class UnitEntry implements BaseColumns {
        public static final String TABLE_UNIT = "units";
        public static final String UNIT_PROJECT_FK = "project_fk";
        public static final String UNIT_CHAPTER_FK = "chapter_fk";
        public static final String UNIT_START_VERSE = "start_verse";
        public static final String UNIT_END_VERSE = "end_verse";
        public static final String UNIT_NOTES = "notes";
        public static final String UNIT_CHOSEN_TAKE_FK = "chosen_take_fk";
        public static final String UNIT_UNIQUE_CONSTRAINT = "cols_unique";

        public static final String CREATE_UNIT_TABLE = "CREATE TABLE " + TABLE_UNIT + " ("
                + _ID + " INTEGER PRIMARY KEY, "
                + UNIT_PROJECT_FK + INTCOMMA
                + UNIT_CHAPTER_FK + INTCOMMA
                + UNIT_START_VERSE + INTCOMMA
                + UNIT_END_VERSE + INTCOMMA
                + UNIT_NOTES + TEXTCOMMA
                + UNIT_CHOSEN_TAKE_FK + INTCOMMA
                + "FOREIGN KEY(" + UNIT_PROJECT_FK + ") REFERENCES " + ProjectEntry.TABLE_PROJECT + "(" + _ID + ")"
                + "FOREIGN KEY(" + UNIT_CHAPTER_FK + ") REFERENCES " + ChapterEntry.TABLE_CHAPTER + "(" + _ID + ")"
                + "FOREIGN KEY(" + UNIT_CHOSEN_TAKE_FK + ") REFERENCES " + TakeEntry.TABLE_TAKE + "(" + _ID + ")"
                + "CONSTRAINT " + UNIT_UNIQUE_CONSTRAINT + " UNIQUE(" + UNIT_PROJECT_FK + "," +  UNIT_CHAPTER_FK + "," + UNIT_START_VERSE + ")"
                + ");";
    }

    public static abstract class TakeEntry implements BaseColumns {
        public static final String TABLE_TAKE = "takes";
        public static final String TAKE_UNIT_FK = "unit_fk";
        public static final String TAKE_RATING = "rating";
        public static final String TAKE_NOTES = "notes";
        public static final String TAKE_NUMBER = "number";
        public static final String TAKE_FILENAME = "filename";
        public static final String TAKE_TIMESTAMP = "timestamp";
        public static final String TAKE_UNIQUE_CONSTRAINT = "cols_unique";

        public static final String CREATE_TAKE_TABLE = "CREATE TABLE " + TABLE_TAKE + " ("
                + _ID + " INTEGER PRIMARY KEY, "
                + TAKE_UNIT_FK + INTCOMMA
                + TAKE_RATING + INTCOMMA
                + TAKE_NOTES + TEXTCOMMA
                + TAKE_NUMBER + INTCOMMA
                + TAKE_FILENAME + TEXTCOMMA
                + TAKE_TIMESTAMP + INTCOMMA
                + "CONSTRAINT " + TAKE_UNIQUE_CONSTRAINT + " UNIQUE(" + TAKE_UNIT_FK + "," +  TAKE_NUMBER + ")"
                + ");";
    }

    public static abstract class ProjectEntry implements BaseColumns {
        public static final String TABLE_PROJECT = "projects";
        public static final String PROJECT_TARGET_LANGUAGE_FK = "target_language_fk";
        public static final String PROJECT_BOOK_FK = "book_fk";
        public static final String PROJECT_VERSION = "version";
        public static final String PROJECT_MODE = "mode";
        public static final String PROJECT_SOURCE_LANGUAGE_FK = "source_lang_fk";
        public static final String PROJECT_SOURCE_AUDIO_PATH = "source_audio_path";
        public static final String PROJECT_CONTRIBUTORS = "contributors";
        public static final String PROJECT_NOTES = "notes";
        public static final String PROJECT_PROGRESS = "progress";
        public static final String PROJECT_UNIQUE_CONSTRAINT = "cols_unique";

        public static final String CREATE_PROJECT_TABLE = "CREATE TABLE " + TABLE_PROJECT + " ("
                + _ID + " INTEGER PRIMARY KEY,"
                + PROJECT_TARGET_LANGUAGE_FK + INTCOMMA
                + PROJECT_BOOK_FK + TEXTCOMMA
                + PROJECT_VERSION + TEXTCOMMA
                + PROJECT_MODE + TEXTCOMMA
                + PROJECT_SOURCE_LANGUAGE_FK + INTCOMMA
                + PROJECT_SOURCE_AUDIO_PATH + TEXTCOMMA
                + PROJECT_CONTRIBUTORS + TEXTCOMMA
                + PROJECT_NOTES + TEXTCOMMA
                + PROJECT_PROGRESS + INTCOMMA
                + "FOREIGN KEY(" + PROJECT_TARGET_LANGUAGE_FK + ") REFERENCES " + LanguageEntry.TABLE_LANGUAGE + "(" + _ID + ")"
                + "FOREIGN KEY(" + PROJECT_SOURCE_LANGUAGE_FK + ") REFERENCES " + LanguageEntry.TABLE_LANGUAGE + "(" + _ID + ")"
                + "CONSTRAINT " + PROJECT_UNIQUE_CONSTRAINT + " UNIQUE(" + PROJECT_BOOK_FK + "," +  PROJECT_TARGET_LANGUAGE_FK + "," + PROJECT_VERSION + ")"
                + " );";
    }

    public static abstract class TempEntry implements BaseColumns {
        public static final String TABLE_TEMP = "stuff";
        public static final String TEMP_TAKE_NAME = "filename";

        public static final String CREATE_TEMP_TABLE = "CREATE TABLE " + TABLE_TEMP + " ("
                + _ID + " INTEGER PRIMARY KEY,"
                + TEMP_TAKE_NAME + " TEXT"
                + " );";
    }

    public static final String TEXT = " TEXT";
    public static final String INTEGER = " INTEGER";
    public static final String COMMA = ",";
    public static final String TEXTCOMMA = " TEXT,";
    public static final String INTCOMMA = " INTEGER,";

    public static final String DELETE_LANGUAGE = "DROP TABLE IF EXISTS " + LanguageEntry.TABLE_LANGUAGE;
    public static final String DELETE_BOOKS = "DROP TABLE IF EXISTS " + BookEntry.TABLE_BOOK;
    public static final String DELETE_PROJECTS = "DROP TABLE IF EXISTS " + ProjectEntry.TABLE_PROJECT;
    public static final String DELETE_CHAPTERS = "DROP TABLE IF EXISTS " + ChapterEntry.TABLE_CHAPTER;
    public static final String DELETE_UNITS = "DROP TABLE IF EXISTS " + UnitEntry.TABLE_UNIT;
    public static final String DELETE_TAKES = "DROP TABLE IF EXISTS " + TakeEntry.TABLE_TAKE;

    public static final String DELETE_TEMP = "DROP TABLE IF EXISTS stuff";
}

