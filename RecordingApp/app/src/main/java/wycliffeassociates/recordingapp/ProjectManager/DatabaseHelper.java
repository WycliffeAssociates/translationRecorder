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

    private int mIdSlug;
    private int mIdTargetLang;
    private int mIdSourceLang;
    private int mIdSource;
    private int mIdMode;
    private int mIdProject;
    private int mIdContributors;
    private int mIdBookNum;
    private int mIdSourceAudioPath;

    public DatabaseHelper(Context ctx){
        super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(ProjectContract.CREATE_PROFILE_TABLE);
        System.out.println();
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(ProjectContract.DELETE_ENTRIES);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public void clearTable(){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM " + ProjectContract.ProjectEntry.TABLE_PROJECT);
    }

    public void addProject(Project p){
        SQLiteDatabase db = getWritableDatabase();
        if(!checkIfExists(p)) {
            ContentValues cv = new ContentValues();
            cv.put(ProjectContract.ProjectEntry.COLUMN_TARGET_LANG, p.getTargetLang());
            cv.put(ProjectContract.ProjectEntry.COLUMN_SOURCE_LANG, p.getSrcLang());
            cv.put(ProjectContract.ProjectEntry.COLUMN_SLUG, p.getSlug());
            cv.put(ProjectContract.ProjectEntry.COLUMN_SOURCE, p.getSource());
            cv.put(ProjectContract.ProjectEntry.COLUMN_MODE, p.getMode());
            cv.put(ProjectContract.ProjectEntry.COLUMN_BOOK_NUM, p.getBookNumber());
            cv.put(ProjectContract.ProjectEntry.COLUMN_PROJECT, p.getProject());
            cv.put(ProjectContract.ProjectEntry.COLUMN_CONTRIBUTORS, p.getContributors());
            cv.put(ProjectContract.ProjectEntry.COLUMN_SOURCE_AUDIO_PATH, p.getSourceAudioPath());
            long result = db.insert(ProjectContract.ProjectEntry.TABLE_PROJECT, null, cv);
        }
        db.close();
    }

    private void getIds(Cursor cursor){
        mIdSlug = cursor.getColumnIndex(ProjectContract.ProjectEntry.COLUMN_SLUG);
        mIdTargetLang = cursor.getColumnIndex(ProjectContract.ProjectEntry.COLUMN_TARGET_LANG);
        mIdSourceLang = cursor.getColumnIndex(ProjectContract.ProjectEntry.COLUMN_SOURCE_LANG);
        mIdSource = cursor.getColumnIndex(ProjectContract.ProjectEntry.COLUMN_SOURCE);
        mIdMode = cursor.getColumnIndex(ProjectContract.ProjectEntry.COLUMN_MODE);
        mIdBookNum = cursor.getColumnIndex(ProjectContract.ProjectEntry.COLUMN_BOOK_NUM);
        mIdProject = cursor.getColumnIndex(ProjectContract.ProjectEntry.COLUMN_PROJECT);
        mIdContributors = cursor.getColumnIndex(ProjectContract.ProjectEntry.COLUMN_CONTRIBUTORS);
        mIdSourceAudioPath = cursor.getColumnIndex(ProjectContract.ProjectEntry.COLUMN_SOURCE_AUDIO_PATH);
    }

    public boolean checkIfExists(Project project){
        String query = "SELECT * FROM " + ProjectContract.ProjectEntry.TABLE_PROJECT + " WHERE " +
                ProjectContract.ProjectEntry.COLUMN_SLUG + " = '" + project.getSlug() + "' AND " +
                ProjectContract.ProjectEntry.COLUMN_SOURCE + " = '" + project.getSource() + "' AND " +
                ProjectContract.ProjectEntry.COLUMN_TARGET_LANG + " = '" + project.getTargetLang() + "' AND " +
                ProjectContract.ProjectEntry.COLUMN_MODE + " = '" + project.getMode() + "' AND " +
                ProjectContract.ProjectEntry.COLUMN_PROJECT + " = '" + project.getProject() + "'";

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        if(cursor.getCount() > 0){
            return true;
        } else {
            return false;
        }
    }

    public List<Project> getAllProjects(){
        List<Project> projectList = new ArrayList<>();
        String query = "SELECT * FROM " + ProjectContract.ProjectEntry.TABLE_PROJECT;
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
                project.setProject(cursor.getString(mIdProject));
                project.setBookNumber(cursor.getString(mIdBookNum));
                project.setContributors(cursor.getString(mIdContributors));
                project.setSourceAudioPath(cursor.getString(mIdSourceAudioPath));
                projectList.add(project);
            } while(cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return projectList;
    }

    public int getNumProjects(){
        SQLiteDatabase db = getReadableDatabase();
        String countQuery = "SELECT * FROM " + ProjectContract.ProjectEntry.TABLE_PROJECT;
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        db.close();
        return count;
    }
}
