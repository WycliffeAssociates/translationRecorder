package wycliffeassociates.recordingapp.ProjectManager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import wycliffeassociates.recordingapp.FilesPage.FileNameExtractor;

import static wycliffeassociates.recordingapp.ProjectManager.ProjectContract.ProjectEntry.TABLE_PROJECT;
import static wycliffeassociates.recordingapp.ProjectManager.ProjectContract.ProjectEntry.COLUMN_TARGET_LANG;
import static wycliffeassociates.recordingapp.ProjectManager.ProjectContract.ProjectEntry.COLUMN_SLUG;
import static wycliffeassociates.recordingapp.ProjectManager.ProjectContract.ProjectEntry.COLUMN_SOURCE;
import static wycliffeassociates.recordingapp.ProjectManager.ProjectContract.ProjectEntry.COLUMN_PROJECT;
import static wycliffeassociates.recordingapp.ProjectManager.ProjectContract.ProjectEntry.COLUMN_MODE;
import static wycliffeassociates.recordingapp.ProjectManager.ProjectContract.ProjectEntry.COLUMN_SOURCE_LANG;
import static wycliffeassociates.recordingapp.ProjectManager.ProjectContract.ProjectEntry.COLUMN_BOOK_NUM;
import static wycliffeassociates.recordingapp.ProjectManager.ProjectContract.ProjectEntry.COLUMN_CONTRIBUTORS;
import static wycliffeassociates.recordingapp.ProjectManager.ProjectContract.ProjectEntry.COLUMN_SOURCE_AUDIO_PATH;

import static wycliffeassociates.recordingapp.ProjectManager.ProjectContract.ProjectEntry.TABLE_TAKES;
import static wycliffeassociates.recordingapp.ProjectManager.ProjectContract.ProjectEntry.COLUMN_CHAPTER;
import static wycliffeassociates.recordingapp.ProjectManager.ProjectContract.ProjectEntry.COLUMN_START_VS;
import static wycliffeassociates.recordingapp.ProjectManager.ProjectContract.ProjectEntry.COLUMN_END_VS;
import static wycliffeassociates.recordingapp.ProjectManager.ProjectContract.ProjectEntry.COLUMN_RATING;
import static wycliffeassociates.recordingapp.ProjectManager.ProjectContract.ProjectEntry.COLUMN_CHECKING;
import static wycliffeassociates.recordingapp.ProjectManager.ProjectContract.ProjectEntry.COLUMN_TAKE_NUM;




/**
 * Created by sarabiaj on 5/10/2016.
 */
public class ProjectDatabaseHelper extends SQLiteOpenHelper {

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

    public ProjectDatabaseHelper(Context ctx){
        super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(ProjectContract.CREATE_PROFILE_TABLE);
        db.execSQL(ProjectContract.CREATE_TAKES_TABLE);
        System.out.println();
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(ProjectContract.DELETE_ENTRIES);
        db.execSQL(ProjectContract.DELETE_TAKES);
        onCreate(db);

    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public void clearTable(){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM " + ProjectContract.ProjectEntry.TABLE_PROJECT);
    }

    public String getProjectQuery(Project project){
        String query = "SELECT * FROM " + ProjectContract.ProjectEntry.TABLE_PROJECT + " WHERE " +
                ProjectContract.ProjectEntry.COLUMN_SLUG + " = '" + project.getSlug() + "' AND " +
                ProjectContract.ProjectEntry.COLUMN_SOURCE + " = '" + project.getSource() + "' AND " +
                ProjectContract.ProjectEntry.COLUMN_TARGET_LANG + " = '" + project.getTargetLanguage() + "' AND " +
                ProjectContract.ProjectEntry.COLUMN_MODE + " = '" + project.getMode() + "' AND " +
                ProjectContract.ProjectEntry.COLUMN_PROJECT + " = '" + project.getProject() + "'";
        return query;
    }

    public String getTakeQuery(FileNameExtractor fne){
        String query = "SELECT * FROM " + TABLE_TAKES + " WHERE " +
                COLUMN_TARGET_LANG + " = '" + fne.getLang() + "' AND " +
                COLUMN_SOURCE + " = '" + fne.getSource() + "' AND " +
                COLUMN_SLUG + " = '" + fne.getBook() + "' AND " +
                COLUMN_MODE + " = '" + fne.getMode() + "' AND " +
                COLUMN_CHAPTER + " = " + fne.getChapter() + " AND " +
                COLUMN_START_VS + " = " + fne.getStartVerse() + " AND " +
                COLUMN_END_VS + " = " + fne.getEndVerse() + " AND " +
                COLUMN_TAKE_NUM + " = " + fne.getTake() + "";
        return query;
    }

    public boolean deleteProject(Project p){
        SQLiteDatabase db = getWritableDatabase();

        //Delete the project entry from the projects table as well as all individual files related from the takes table
        boolean success = (db.delete(TABLE_PROJECT,
                COLUMN_PROJECT + "=? AND " +
                COLUMN_SLUG + "=? AND " +
                COLUMN_TARGET_LANG + "=? AND " +
                COLUMN_MODE + "=? AND " +
                COLUMN_SOURCE + "=?",
                new String[]{
                        p.getProject(),
                        p.getSlug(),
                        p.getTargetLanguage(),
                        p.getMode(),
                        p.getSource()
                }
        ) > 0) && (db.delete(TABLE_PROJECT,
                COLUMN_TARGET_LANG + "=? AND " +
                COLUMN_SOURCE + "=? AND " +
                COLUMN_SLUG + "=? AND " +
                COLUMN_MODE + "=?",
                new String[]{
                        p.getTargetLanguage(),
                        p.getSource(),
                        p.getSlug(),
                        p.getMode()
                }
        ) > 0);
        db.close();
        return success;
    }

    public void deleteTake(FileNameExtractor fne){
        SQLiteDatabase db = getWritableDatabase();
        String delete = "DELETE FROM " + TABLE_TAKES + " WHERE " +
                COLUMN_TARGET_LANG + " = '" + fne.getLang() + "' AND " +
                COLUMN_SOURCE + " = '" + fne.getSource() + "' AND " +
                COLUMN_SLUG + " = '" + fne.getBook() + "' AND " +
                COLUMN_CHAPTER + " = " + fne.getChapter() + " AND " +
                COLUMN_START_VS + " = " + fne.getStartVerse() + " AND " +
                COLUMN_END_VS + " = " + fne.getEndVerse() + " AND " +
                COLUMN_TAKE_NUM + " = " + fne.getTake() + "";
        db.execSQL(delete);
        db.close();
    }

    public void addProject(Project p){
        SQLiteDatabase db = getWritableDatabase();
        if(!checkIfExists(p)) {
            ContentValues cv = new ContentValues();
            cv.put(COLUMN_TARGET_LANG, p.getTargetLanguage());
            cv.put(COLUMN_SOURCE_LANG, p.getSourceLanguage());
            cv.put(COLUMN_SLUG, p.getSlug());
            cv.put(COLUMN_SOURCE, p.getSource());
            cv.put(COLUMN_MODE, p.getMode());
            cv.put(COLUMN_BOOK_NUM, p.getBookNumber());
            cv.put(COLUMN_PROJECT, p.getProject());
            cv.put(COLUMN_CONTRIBUTORS, p.getContributors());
            cv.put(COLUMN_SOURCE_AUDIO_PATH, p.getSourceAudioPath());
            long result = db.insert(ProjectContract.ProjectEntry.TABLE_PROJECT, null, cv);
        }
        db.close();
    }

    public void addTake(FileNameExtractor fne, int rating, int checking){
        SQLiteDatabase db = getWritableDatabase();
        if(!checkIfExists(fne)){
            ContentValues cv = new ContentValues();
            cv.put(COLUMN_TARGET_LANG, fne.getLang());
            cv.put(COLUMN_SOURCE, fne.getSource());
            cv.put(COLUMN_SLUG, fne.getBook());
            cv.put(COLUMN_MODE, fne.getMode());
            cv.put(COLUMN_CHAPTER, fne.getChapter());
            cv.put(COLUMN_START_VS, fne.getStartVerse());
            cv.put(COLUMN_END_VS, fne.getEndVerse());
            cv.put(COLUMN_TAKE_NUM, fne.getTake());
            cv.put(COLUMN_RATING, rating);
            cv.put(COLUMN_CHECKING, checking);
            long result = db.insert(TABLE_TAKES, null, cv);
        }
        db.close();
    }

    private void getProjectTableIds(Cursor cursor){
        mIdSlug = cursor.getColumnIndex(COLUMN_SLUG);
        mIdTargetLang = cursor.getColumnIndex(COLUMN_TARGET_LANG);
        mIdSourceLang = cursor.getColumnIndex(COLUMN_SOURCE_LANG);
        mIdSource = cursor.getColumnIndex(COLUMN_SOURCE);
        mIdMode = cursor.getColumnIndex(COLUMN_MODE);
        mIdBookNum = cursor.getColumnIndex(COLUMN_BOOK_NUM);
        mIdProject = cursor.getColumnIndex(COLUMN_PROJECT);
        mIdContributors = cursor.getColumnIndex(COLUMN_CONTRIBUTORS);
        mIdSourceAudioPath = cursor.getColumnIndex(COLUMN_SOURCE_AUDIO_PATH);
    }

    public boolean checkIfExists(Project project){
        String query = getProjectQuery(project);
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        if(cursor.getCount() > 0){
            return true;
        } else {
            return false;
        }
    }

    public boolean checkIfExists(FileNameExtractor fne){
        String query = getTakeQuery(fne);

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
        getProjectTableIds(cursor);

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

    private int getIntFromTakeTable(FileNameExtractor fne, String column){
        String query = getTakeQuery(fne);
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        getProjectTableIds(cursor);
        int val = 0;
        if(cursor.moveToFirst()){
            val = cursor.getInt(cursor.getColumnIndex(column));
        }
        cursor.close();
        db.close();
        return val;
    }

    private int getCheckingLevel(FileNameExtractor fne, String column){
        String query = getTakeQuery(fne);
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        getProjectTableIds(cursor);
        int val = 0;
        //if there are multiple takes, the checking level is 0, otherwise check the value
        if(cursor.getCount() == 1) {
            if (cursor.moveToFirst()) {
                val = cursor.getInt(cursor.getColumnIndex(column));
            }
        }
        cursor.close();
        db.close();
        return val;
    }

    private void setIntInTakeTable(FileNameExtractor fne, String column, int value){
        String insertCommand = "UPDATE " + TABLE_TAKES + " SET " + column + "=" + value
                + " WHERE " +
                COLUMN_TARGET_LANG + " = '" + fne.getLang() + "' AND " +
                COLUMN_SOURCE + " = '" + fne.getSource() + "' AND " +
                COLUMN_SLUG + " = '" + fne.getBook() + "' AND " +
                COLUMN_CHAPTER + " = " + fne.getChapter() + " AND " +
                COLUMN_START_VS + " = " + fne.getStartVerse() + " AND " +
                COLUMN_END_VS + " = " + fne.getEndVerse() + " AND " +
                COLUMN_TAKE_NUM + " = " + fne.getTake() + "";

        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(insertCommand);
        db.close();
    }

    public int getRating(FileNameExtractor fne){
        return getIntFromTakeTable(fne, COLUMN_RATING);
    }

    public int getCheckingLevel(FileNameExtractor fne){
        return getIntFromTakeTable(fne, COLUMN_CHECKING);
    }

    public void setRating(FileNameExtractor fne, int rating){
        setIntInTakeTable(fne, COLUMN_RATING, rating);
    }

    public void setCheckingLevel(FileNameExtractor fne, int checkingLevel){
        setIntInTakeTable(fne, COLUMN_CHECKING, checkingLevel);
    }
}
