package wycliffeassociates.recordingapp.ProjectManager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

import wycliffeassociates.recordingapp.FilesPage.FileNameExtractor;

import static wycliffeassociates.recordingapp.ProjectManager.ProjectContract.*;

/**
 * Created by sarabiaj on 5/10/2016.
 */
public class ProjectDatabaseHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "translation_projects";


    public ProjectDatabaseHelper(Context ctx){
        super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(LanguageEntry.CREATE_LANGUAGE_TABLE);
        db.execSQL(BookEntry.CREATE_BOOK_TABLE);
        db.execSQL(ProjectEntry.CREATE_PROJECT_TABLE);
        db.execSQL(ChapterEntry.CREATE_CHAPTER_TABLE);
        db.execSQL(UnitEntry.CREATE_UNIT_TABLE);
        db.execSQL(TakeEntry.CREATE_TAKE_TABLE);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DELETE_LANGUAGE);
        db.execSQL(DELETE_BOOKS);
        db.execSQL(DELETE_PROJECTS);
        db.execSQL(DELETE_CHAPTERS);
        db.execSQL(DELETE_UNITS);
        db.execSQL(DELETE_TAKES);
        onCreate(db);

    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public void deleteAllTables(){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(DELETE_LANGUAGE);
        db.execSQL(DELETE_BOOKS);
        db.execSQL(DELETE_PROJECTS);
        db.execSQL(DELETE_CHAPTERS);
        db.execSQL(DELETE_UNITS);
        db.execSQL(DELETE_TAKES);
        onCreate(db);
    }

    public boolean languageExists(String code){
        SQLiteDatabase db = getReadableDatabase();
        final String languageCountQuery = "SELECT COUNT(*) FROM " + LanguageEntry.TABLE_LANGUAGE + " WHERE " + LanguageEntry.LANGUAGE_CODE + "=?";
        boolean exists =  (DatabaseUtils.longForQuery(db, languageCountQuery, new String[]{code})) > 0;
        return exists;
    }

    public boolean bookExists(String slug){
        SQLiteDatabase db = getReadableDatabase();
        final String bookCountQuery = "SELECT COUNT(*) FROM " + BookEntry.TABLE_BOOK + " WHERE " + BookEntry.BOOK_SLUG + "=?";
        boolean exists = (DatabaseUtils.longForQuery(db, bookCountQuery, new String[]{slug})) > 0;
        return exists;
    }

    public boolean projectExists(Project project){
        String languageCode = project.getTargetLanguage();
        int languageId = getLanguageId(languageCode);
        String slug = project.getSlug();
        int bookId = getBookId(slug);
        SQLiteDatabase db = getReadableDatabase();
        final String projectCountQuery = "SELECT COUNT(*) FROM " + ProjectEntry.TABLE_PROJECT + " WHERE " + ProjectEntry.PROJECT_TARGET_LANGUAGE_FK + "=?"
                + " AND " + ProjectEntry.PROJECT_BOOK_FK + "=? AND" + ProjectEntry.PROJECT_VERSION + "=?";
        boolean exists = (DatabaseUtils.longForQuery(db, projectCountQuery, new String[]{String.valueOf(languageId),String.valueOf(bookId), project.getSource()})) > 0;
        return exists;
    }

    public boolean chapterExists(Project project, int chapter){
        String projectId = String.valueOf(getProjectId(project));
        SQLiteDatabase db = getReadableDatabase();
        final String chapterCountQuery = String.format("SELECT COUNT(*) FROM %s WHERE %s=? AND %s=?",
                ChapterEntry.TABLE_CHAPTER, ChapterEntry.CHAPTER_PROJECT_FK, ChapterEntry.CHAPTER_NUMBER);
        boolean exists = (DatabaseUtils.longForQuery(db, chapterCountQuery, new String[]{projectId, String.valueOf(chapter)})) > 0;
        return exists;
    }

    public boolean unitExists(Project project, int chapter, int startVerse){
        String projectId = String.valueOf(getProjectId(project));
        String chapterId = String.valueOf(getChapterId(project, chapter));
        SQLiteDatabase db = getReadableDatabase();
        final String unitCountQuery = String.format("SELECT COUNT(*) FROM %s WHERE %s=? AND %s=? AND %s=?",
                UnitEntry.TABLE_UNIT, UnitEntry.UNIT_PROJECT_FK, UnitEntry.UNIT_CHAPTER_FK, UnitEntry.UNIT_START_VERSE);
        boolean exists = (DatabaseUtils.longForQuery(db, unitCountQuery, new String[]{projectId, chapterId, String.valueOf(startVerse)})) > 0;
        return exists;
    }

    public boolean takeExists(Project project, int chapter, int startVerse, int take){
        String unitId = String.valueOf(getUnitId(project, chapter, startVerse));
        SQLiteDatabase db = getReadableDatabase();
        final String takeCountQuery = String.format("SELECT COUNT(*) FROM %s WHERE %s=? AND %s=?",
                TakeEntry.TABLE_TAKE, TakeEntry.TAKE_UNIT_FK, TakeEntry.TAKE_NUMBER);
        boolean exists = (DatabaseUtils.longForQuery(db, takeCountQuery, new String[]{unitId, String.valueOf(take)})) > 0;
        return exists;
    }

    public int getLanguageId(String code){
        SQLiteDatabase db = getReadableDatabase();
        final String languageIdQuery = "SELECT " + LanguageEntry._ID + " FROM " + LanguageEntry.TABLE_LANGUAGE + " WHERE " + LanguageEntry.LANGUAGE_CODE + "=?";
        int id = (int)DatabaseUtils.longForQuery(db, languageIdQuery, new String[]{code});
        return id;
    }

    public int getBookId(String slug){
        SQLiteDatabase db = getReadableDatabase();
        final String bookIdQuery = "SELECT " + BookEntry._ID + " FROM " + BookEntry.TABLE_BOOK + " WHERE " + BookEntry.BOOK_SLUG + "=?";
        int id = (int)DatabaseUtils.longForQuery(db, bookIdQuery, new String[]{slug});
        return id;
    }

    public int getProjectId(Project project){
        return getProjectId(project.getTargetLanguage(), project.getSlug(), project.getMode());
    }

    public int getProjectId(String languageCode, String slug, String version){
        String languageId = String.valueOf(getLanguageId(languageCode));
        String bookId = String.valueOf(getBookId(slug));
        SQLiteDatabase db = getReadableDatabase();
        final String projectIdQuery = String.format("SELECT %s FROM %s WHERE %s=? AND %s=? AND %s",
                ProjectEntry._ID, ProjectEntry.TABLE_PROJECT, ProjectEntry.PROJECT_TARGET_LANGUAGE_FK, ProjectEntry.PROJECT_BOOK_FK, ProjectEntry.PROJECT_VERSION);
        int id = (int)DatabaseUtils.longForQuery(db, projectIdQuery, new String[]{languageId, bookId, version});
        return id;
    }

    public int getChapterId(Project project, int chapter){
        return getChapterId(project.getTargetLanguage(), project.getSlug(), project.getMode(), chapter);
    }

    public int getChapterId(String languageCode, String slug, String version, int chapter){
        String projectId = String.valueOf(getProjectId(languageCode, slug, version));
        SQLiteDatabase db = getReadableDatabase();
        final String chapterIdQuery = String.format("SELECT %s FROM %s WHERE %s=? AND %s=?",
                ChapterEntry._ID, ChapterEntry.TABLE_CHAPTER, ChapterEntry.CHAPTER_PROJECT_FK, ChapterEntry.CHAPTER_NUMBER);
        int id = (int)DatabaseUtils.longForQuery(db, chapterIdQuery, new String[]{projectId, String.valueOf(chapter)});
        return id;
    }

    public int getUnitId(Project project, int chapter, int startVerse){
        return getUnitId(project.getTargetLanguage(), project.getSlug(), project.getMode(), chapter, startVerse);
    }

    public int getUnitId(String languageCode, String slug, String version, int chapter, int startVerse){
        String projectId = String.valueOf(getProjectId(languageCode, slug, version));
        String chapterId = String.valueOf(getChapterId(languageCode, slug, version, chapter));
        SQLiteDatabase db = getReadableDatabase();
        final String unitIdQuery = String.format("SELECT %s FROM %s WHERE %s=? AND %s=? AND %s=?",
                UnitEntry._ID, UnitEntry.TABLE_UNIT, UnitEntry.UNIT_PROJECT_FK, UnitEntry.UNIT_CHAPTER_FK, UnitEntry.UNIT_START_VERSE);
        int id = (int) DatabaseUtils.longForQuery(db, unitIdQuery, new String[]{projectId, chapterId, String.valueOf(startVerse)});
        return id;
    }

    public int getTakeId(FileNameExtractor fne){
        String unitId = String.valueOf(getUnitId(fne.getLang(), fne.getBook(), fne.getMode(), fne.getChapter(), fne.getStartVerse()));
        SQLiteDatabase db = getReadableDatabase();
        final String takeIdQuery = String.format("SELECT %s FROM %s WHERE %s=? AND %s=?",
                TakeEntry._ID, TakeEntry.TABLE_TAKE, TakeEntry.TAKE_UNIT_FK, TakeEntry.TAKE_NUMBER);
        int id = (int) DatabaseUtils.longForQuery(db, unitId, new String[]{unitId, String.valueOf(fne.getTake())});
        return id;
    }

    public String getLanguageName(String code){
        SQLiteDatabase db = getReadableDatabase();
        final String languageNameQuery = String.format("SELECT %s FROM %s WHERE %s=?",
                LanguageEntry.LANGUAGE_NAME, LanguageEntry.TABLE_LANGUAGE, LanguageEntry.LANGUAGE_CODE);
        String name = DatabaseUtils.stringForQuery(db, languageNameQuery, new String[]{code});
        return name;
    }

    public String getLanguageCode(int id){
        SQLiteDatabase db = getReadableDatabase();
        final String languageNameQuery = String.format("SELECT %s FROM %s WHERE %s=?",
                LanguageEntry.LANGUAGE_CODE, LanguageEntry.TABLE_LANGUAGE, LanguageEntry._ID);
        String code = DatabaseUtils.stringForQuery(db, languageNameQuery, new String[]{String.valueOf(id)});
        return code;
    }

    public String getBookName(String slug){
        SQLiteDatabase db = getReadableDatabase();
        final String bookNameQuery = String.format("SELECT %s FROM %s WHERE %s=?",
                BookEntry.BOOK_NAME, BookEntry.TABLE_BOOK, BookEntry.BOOK_SLUG);
        String name = DatabaseUtils.stringForQuery(db, bookNameQuery, new String[]{slug});
        return name;
    }

    public String getBookSlug(int id){
        SQLiteDatabase db = getReadableDatabase();
        final String bookSlugQuery = String.format("SELECT %s FROM %s WHERE %s=?",
                BookEntry.BOOK_SLUG, BookEntry.TABLE_BOOK, BookEntry._ID);
        String slug = DatabaseUtils.stringForQuery(db, bookSlugQuery, new String[]{String.valueOf(id)});
        return slug;
    }

    public String getBookAnthology(String slug){
        SQLiteDatabase db = getReadableDatabase();
        final String bookNameQuery = String.format("SELECT %s FROM %s WHERE %s=?",
                BookEntry.BOOK_ANTHOLOGY, BookEntry.TABLE_BOOK, BookEntry.BOOK_SLUG);
        String anthology = DatabaseUtils.stringForQuery(db, bookNameQuery, new String[]{slug});
        return anthology;
    }

    public int getBookNumber(String slug){
        SQLiteDatabase db = getReadableDatabase();
        final String bookNameQuery = String.format("SELECT %s FROM %s WHERE %s=?",
                BookEntry.BOOK_NUMBER, BookEntry.TABLE_BOOK, BookEntry.BOOK_SLUG);
        int number = (int)DatabaseUtils.longForQuery(db, bookNameQuery, new String[]{slug});
        return number;
    }

    public void addLanguage(String code, String name){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(LanguageEntry.LANGUAGE_CODE, code);
        cv.put(LanguageEntry.LANGUAGE_NAME, name);
        long result = db.insert(LanguageEntry.TABLE_LANGUAGE, null, cv);
    }

    public void addBook(String slug, String name){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(BookEntry.BOOK_SLUG, slug);
        cv.put(BookEntry.BOOK_NAME, name);
        long result = db.insert(BookEntry.TABLE_BOOK, null, cv);
    }

    public void addProject(Project p){
        int targetLanguageId = getLanguageId(p.getTargetLanguage());
        int sourceLanguageId = getLanguageId(p.getSourceLanguage());
        int bookId = getBookId(p.getSlug());

        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(ProjectEntry.PROJECT_TARGET_LANGUAGE_FK, targetLanguageId);
        cv.put(ProjectEntry.PROJECT_SOURCE_LANGUAGE_FK, sourceLanguageId);
        cv.put(ProjectEntry.PROJECT_BOOK_FK, bookId);
        cv.put(ProjectEntry.PROJECT_VERSION, p.getSource());
        cv.put(ProjectEntry.PROJECT_MODE, p.getMode());
        cv.put(ProjectEntry.PROJECT_CONTRIBUTORS, p.getContributors());
        cv.put(ProjectEntry.PROJECT_SOURCE_AUDIO_PATH, p.getSourceAudioPath());
        cv.put(ProjectEntry.PROJECT_NOTES, "");
        cv.put(ProjectEntry.PROJECT_PROGRESS, "");

        long result = db.insert(ProjectEntry.TABLE_PROJECT, null, cv);
    }

    public void addTake(FileNameExtractor fne, int rating){
        int unitId = getUnitId(fne.getLang(), fne.getBook(), fne.getSource(), fne.getChapter(), fne.getStartVerse());

        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(TakeEntry.TAKE_UNIT_FK, unitId);
        cv.put(TakeEntry.TAKE_RATING, rating);
        cv.put(TakeEntry.TAKE_NOTES, "");
        cv.put(TakeEntry.TAKE_NUMBER, fne.getTake());
        long result = db.insert(TakeEntry.TABLE_TAKE, null, cv);
    }

    public List<Project> getAllProjects(){
        List<Project> projectList = new ArrayList<>();
        String query = "SELECT * FROM " + ProjectContract.ProjectEntry.TABLE_PROJECT;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        if(cursor.moveToFirst()){
            do {
                Project project = new Project();
                project.setSource(cursor.getString(cursor.getColumnIndex(ProjectEntry.PROJECT_VERSION)));
                String targetLanguageCode = getLanguageCode(cursor.getInt(cursor.getColumnIndex(ProjectEntry.PROJECT_TARGET_LANGUAGE_FK)));
                project.setTargetLanguage(targetLanguageCode);
                String sourceLanguageCode = getLanguageCode(cursor.getInt(cursor.getColumnIndex(ProjectEntry.PROJECT_SOURCE_LANGUAGE_FK)));
                project.setSourceLanguage(sourceLanguageCode);
                project.setMode(cursor.getString(cursor.getColumnIndex(ProjectEntry.PROJECT_MODE)));
                String slug = getBookSlug(cursor.getInt(cursor.getColumnIndex(ProjectEntry.PROJECT_BOOK_FK)));
                project.setSlug(slug);
                String anthology = getBookAnthology(slug);
                project.setProject(anthology);
                int number = getBookNumber(slug);
                project.setBookNumber(number);
                project.setContributors(cursor.getString(cursor.getColumnIndex(ProjectEntry.PROJECT_CONTRIBUTORS)));
                project.setSourceAudioPath(cursor.getString(cursor.getColumnIndex(ProjectEntry.PROJECT_SOURCE_AUDIO_PATH)));
                projectList.add(project);
            } while(cursor.moveToNext());
        }
        cursor.close();
        return projectList;
    }

    public int getNumProjects(){
        SQLiteDatabase db = getReadableDatabase();
        String countQuery = "SELECT * FROM " + ProjectContract.ProjectEntry.TABLE_PROJECT;
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    public int getTakeRating(FileNameExtractor fne){
        String unitId = String.valueOf(getUnitId(fne.getLang(), fne.getBook(), fne.getSource(), fne.getChapter(), fne.getStartVerse()));
        SQLiteDatabase db = getReadableDatabase();
        final String getTake = String.format("SELECT %s FROM %s WHERE %s=? AND %s=?",
                TakeEntry.TAKE_RATING, TakeEntry.TABLE_TAKE, TakeEntry.TAKE_UNIT_FK, TakeEntry.TAKE_NUMBER);
        int rating = (int)DatabaseUtils.longForQuery(db, getTake, new String[]{unitId, String.valueOf(fne.getTake())});
        return rating;
    }

    public int getChosenTake(Project project, int chapter, int startVerse){
        String unitId = String.valueOf(getUnitId(project, chapter, startVerse));
        SQLiteDatabase db = getReadableDatabase();
        final String getTake = String.format("SELECT %s FROM %s WHERE %s=? AND %s=?",
                UnitEntry.UNIT_CHOSEN_TAKE, UnitEntry.TABLE_UNIT, UnitEntry._ID);
        int take = (int)DatabaseUtils.longForQuery(db, getTake, new String[]{unitId});
        return take;
    }

    public void setTakeRating(FileNameExtractor fne, int rating){
        String unitId = String.valueOf(getUnitId(fne.getLang(), fne.getBook(), fne.getSource(), fne.getChapter(), fne.getStartVerse()));
        SQLiteDatabase db = getReadableDatabase();
        final String replaceTakeWhere = String.format("WHERE %s=? AND %s=?",
                TakeEntry.TABLE_TAKE, TakeEntry.TAKE_UNIT_FK, TakeEntry.TAKE_NUMBER);
        ContentValues replaceWith = new ContentValues();
        replaceWith.put(TakeEntry.TAKE_RATING, rating);
        db.update(TakeEntry.TAKE_RATING, replaceWith, replaceTakeWhere, new String[]{unitId, String.valueOf(fne.getTake())});
    }

    public void deleteProject(Project p){
        String projectId = String.valueOf(getProjectId(p));
        SQLiteDatabase db = getWritableDatabase();
        final String deleteTakes = String.format("DELETE FROM %s WHERE %s IN (SELECT %s FROM %s WHERE %s=?)",
                TakeEntry.TABLE_TAKE, TakeEntry.TAKE_UNIT_FK, UnitEntry._ID, UnitEntry.TABLE_UNIT, UnitEntry._ID);
        db.execSQL(deleteTakes, new String[]{projectId});
        final String deleteUnits = String.format("DELETE FROM %s WHERE %s=?",
                UnitEntry.TABLE_UNIT, UnitEntry.UNIT_PROJECT_FK);
        db.execSQL(deleteUnits, new String[]{projectId});
        final String deleteChapters = String.format("DELETE FROM %s WHERE %s=?",
                ChapterEntry.TABLE_CHAPTER, ChapterEntry.CHAPTER_PROJECT_FK);
        db.execSQL(deleteChapters, new String[]{projectId});
        final String deleteProject = String.format("DELETE FROM %s WHERE %s=?",
                ProjectEntry.TABLE_PROJECT, ProjectEntry._ID);
        db.execSQL(deleteProject, new String[]{projectId});
    }

    public void deleteTake(FileNameExtractor fne){
        String unitId = String.valueOf(getUnitId(fne.getLang(), fne.getBook(), fne.getSource(), fne.getChapter(), fne.getStartVerse()));
        SQLiteDatabase db = getWritableDatabase();
        final String deleteTake = String.format("DELETE FROM %s WHERE %s=? AND %s=?",
                TakeEntry.TABLE_TAKE, TakeEntry.TAKE_UNIT_FK, TakeEntry.TAKE_NUMBER);
        db.execSQL(deleteTake, new String[]{unitId, String.valueOf(fne.getTake())});
    }


}
