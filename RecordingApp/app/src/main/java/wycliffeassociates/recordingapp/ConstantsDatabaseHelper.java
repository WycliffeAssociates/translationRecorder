package wycliffeassociates.recordingapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import wycliffeassociates.recordingapp.project.Book;
import wycliffeassociates.recordingapp.project.Language;


/**
 * Created by sarabiaj on 6/28/2016.
 */
public class ConstantsDatabaseHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "constants_database";

    private int mIdSlug;
    private int mIdBook;
    private int mIdBookNum;
    private int mIdLanguage;
    private int mIdLanguageCode;

    public ConstantsDatabaseHelper(Context ctx){
        super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(ConstantsContract.CREATE_LANGUAGE_TABLE);
        db.execSQL(ConstantsContract.CREATE_BOOK_TABLE);
        System.out.println();
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(ConstantsContract.DELETE_LANGUAGES);
        db.execSQL(ConstantsContract.DELETE_BOOKS);
        onCreate(db);

    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public void clearBookTable(){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM " + ConstantsContract.ConstantsEntry.TABLE_BOOKS);
    }

    public void clearLanguageTable(){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM " + ConstantsContract.ConstantsEntry.TABLE_LANGUAGES);
    }

    public void addLanguage(Language lang){
        SQLiteDatabase db = getWritableDatabase();
        if(!checkIfExists(lang)) {
            ContentValues cv = new ContentValues();
            cv.put(ConstantsContract.ConstantsEntry.COLUMN_LANGUAGE, lang.getName());
            cv.put(ConstantsContract.ConstantsEntry.COLUMN_LANGUAGE_CODE, lang.getCode());
            long result = db.insert(ConstantsContract.ConstantsEntry.TABLE_LANGUAGES, null, cv);
        }
        db.close();
    }

    public void addBook(Book book){
        SQLiteDatabase db = getWritableDatabase();
        if(!checkIfExists(book)) {
            ContentValues cv = new ContentValues();
            cv.put(ConstantsContract.ConstantsEntry.COLUMN_BOOK, book.getName());
            cv.put(ConstantsContract.ConstantsEntry.COLUMN_SLUG, book.getSlug());
            cv.put(ConstantsContract.ConstantsEntry.COLUMN_BOOK_NUMBER, book.getOrder());
            long result = db.insert(ConstantsContract.ConstantsEntry.TABLE_BOOKS, null, cv);
        }
        db.close();
    }

    public boolean checkIfExists(Language lang){
        String query = "SELECT * FROM " + ConstantsContract.ConstantsEntry.TABLE_LANGUAGES + " WHERE " +
                ConstantsContract.ConstantsEntry.COLUMN_LANGUAGE + " = \"" + lang.getName() + "\" AND " +
                ConstantsContract.ConstantsEntry.COLUMN_LANGUAGE_CODE + " = '" + lang.getCode() + "'";

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        if(cursor != null && cursor.getCount() > 0){
            return true;
        } else {
            return false;
        }
    }

    public boolean checkIfExists(Book book){
        String query = "SELECT * FROM " + ConstantsContract.ConstantsEntry.TABLE_BOOKS + " WHERE " +
                ConstantsContract.ConstantsEntry.COLUMN_BOOK + " = '" + book.getName() + "' AND " +
                ConstantsContract.ConstantsEntry.COLUMN_SLUG + " = '" + book.getSlug() + "'";

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        if(cursor.getCount() > 0){
            return true;
        } else {
            return false;
        }
    }

    public String getLanguageName(String languageCode){
        String query = "SELECT * FROM " + ConstantsContract.ConstantsEntry.TABLE_LANGUAGES + " WHERE " +
                ConstantsContract.ConstantsEntry.COLUMN_LANGUAGE_CODE + " = \"" + languageCode + "\"";

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        if(cursor.getCount() >  0){
            cursor.moveToFirst();
            return cursor.getString(cursor.getColumnIndex(ConstantsContract.ConstantsEntry.COLUMN_LANGUAGE));
        } else {
            return "";
        }
    }

    public String getBookName(String bookCode){
        String query = "SELECT * FROM " + ConstantsContract.ConstantsEntry.TABLE_BOOKS + " WHERE " +
                ConstantsContract.ConstantsEntry.COLUMN_SLUG + " = '" + bookCode + "'";

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        if(cursor.getCount() >  0){
            cursor.moveToFirst();
            return cursor.getString(cursor.getColumnIndex(ConstantsContract.ConstantsEntry.COLUMN_BOOK));
        } else {
            return "";
        }
    }

}
