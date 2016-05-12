package wycliffeassociates.recordingapp.ProjectManager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sarabiaj on 5/10/2016.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "translation_projects";
    private static final String TABLE_PROJECT = "projects";
    private static final String KEY_ID = "key_id";
    private static final String KEY_TARGET_LANG = "key_target_lang";
    private static final String KEY_SOURCE_LANG = "key_source_lang";
    private static final String KEY_SLUG = "key_slug";
    private static final String KEY_SOURCE = "key_source";
    private static final String KEY_MODE = "key_mode";
    //private static final String KEY_USER = "key_user";

    private static final String TEXT = " TEXT";
    private static final String COMMA = ",";
    private static final String TEXTCOMMA = " TEXT,";

    private int mIdSlug;
    private int mIdTargetLang;
    private int mIdSourceLang;
    private int mIdSource;
    private int mIdMode;

    public DatabaseHelper(Context ctx){
        super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_PROFILE_TABLE = "CREATE TABLE " + TABLE_PROJECT + "(" + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_TARGET_LANG + TEXTCOMMA + KEY_SOURCE_LANG + TEXTCOMMA + KEY_SLUG + TEXTCOMMA + KEY_SOURCE
                + TEXT + ")";
        db.execSQL(CREATE_PROFILE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void addProject(Project p){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(KEY_TARGET_LANG, p.getTargetLang());
        cv.put(KEY_SOURCE_LANG, p.getSrcLang());
        cv.put(KEY_SLUG, p.getSlug());
        cv.put(KEY_SOURCE, p.getSource());
        cv.put(KEY_MODE, p.getMode());
        db.insert(TABLE_PROJECT, null, cv);
        db.close();
    }

    private void getIds(Cursor cursor){
        mIdSlug = cursor.getColumnIndex(KEY_SLUG);
        mIdTargetLang = cursor.getColumnIndex(KEY_TARGET_LANG);
        mIdSourceLang = cursor.getColumnIndex(KEY_SOURCE_LANG);
        mIdSource = cursor.getColumnIndex(KEY_SOURCE);
        mIdMode = cursor.getColumnIndex(KEY_MODE);
    }

    public List<Project> getAllProjects(){
        List<Project> projectList = new ArrayList<>();
        String query = "SELECT * FROM " + TABLE_PROJECT;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        getIds(cursor);

        if(cursor.moveToFirst()){
            do {
                Project project = new Project();
                project.setSource(cursor.getString(mIdSource));
                project.setTargetLanguage(cursor.getString(mIdTargetLang));
                project.setSourceLanguage(cursor.getString(mIdSourceLang));
                project.setMode(cursor.getString(mIdMode));
                project.setSlug(cursor.getString(mIdSlug));
                projectList.add(project);
            } while(cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return projectList;
    }

    public int getNumProjects(){
        String countQuery = "SELECT * FROM " + TABLE_PROJECT;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.close();
        db.close();
        return cursor.getCount();
    }
}
