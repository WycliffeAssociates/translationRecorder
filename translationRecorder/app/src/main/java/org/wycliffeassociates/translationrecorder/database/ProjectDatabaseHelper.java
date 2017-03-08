package org.wycliffeassociates.translationrecorder.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDoneException;
import android.database.sqlite.SQLiteOpenHelper;

import org.wycliffeassociates.translationrecorder.FilesPage.FileNameExtractor;
import org.wycliffeassociates.translationrecorder.ProjectManager.Project;
import org.wycliffeassociates.translationrecorder.ProjectManager.tasks.resync.ProjectListResyncTask;
import org.wycliffeassociates.translationrecorder.Reporting.Logger;
import org.wycliffeassociates.translationrecorder.project.Book;
import org.wycliffeassociates.translationrecorder.project.Language;
import org.wycliffeassociates.translationrecorder.wav.WavFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sarabiaj on 5/10/2016.
 */
public class ProjectDatabaseHelper extends SQLiteOpenHelper {

    public void updateSourceAudio(int projectId, Project projectContainingUpdatedSource) {
        int sourceLanguageId = getLanguageId(projectContainingUpdatedSource.getSourceLanguage());
        final String replaceTakeWhere = String.format("%s=?", ProjectContract.ProjectEntry._ID);
        SQLiteDatabase db = getWritableDatabase();
        ContentValues replaceWith = new ContentValues();
        replaceWith.put(ProjectContract.ProjectEntry.PROJECT_SOURCE_LANGUAGE_FK, String.valueOf(sourceLanguageId));
        replaceWith.put(ProjectContract.ProjectEntry.PROJECT_SOURCE_AUDIO_PATH, projectContainingUpdatedSource.getSourceAudioPath());
        db.update(ProjectContract.ProjectEntry.TABLE_PROJECT, replaceWith, replaceTakeWhere, new String[]{String.valueOf(projectId)});
    }

    public List<Project> projectsNeedingResync(List<Project> allProjects) {
        List<Project> needingResync = new ArrayList<>();
        if(allProjects != null) {
            for(Project p : allProjects) {
                if (!projectExists(p)) {
                    needingResync.add(p);
                }
            }
        }
        return needingResync;
    }

    public interface OnLanguageNotFound {
        String requestLanguageName(String languageCode);
    }

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "translation_projects";
    private Language[] languages;


    public ProjectDatabaseHelper(Context ctx){
        super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(ProjectContract.LanguageEntry.CREATE_LANGUAGE_TABLE);
        db.execSQL(ProjectContract.BookEntry.CREATE_BOOK_TABLE);
        db.execSQL(ProjectContract.ProjectEntry.CREATE_PROJECT_TABLE);
        db.execSQL(ProjectContract.ChapterEntry.CREATE_CHAPTER_TABLE);
        db.execSQL(ProjectContract.UnitEntry.CREATE_UNIT_TABLE);
        db.execSQL(ProjectContract.TakeEntry.CREATE_TAKE_TABLE);
        //db.close();
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(ProjectContract.DELETE_LANGUAGE);
        db.execSQL(ProjectContract.DELETE_BOOKS);
        db.execSQL(ProjectContract.DELETE_PROJECTS);
        db.execSQL(ProjectContract.DELETE_CHAPTERS);
        db.execSQL(ProjectContract.DELETE_UNITS);
        db.execSQL(ProjectContract.DELETE_TAKES);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public void deleteAllTables(){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(ProjectContract.DELETE_LANGUAGE);
        db.execSQL(ProjectContract.DELETE_BOOKS);
        db.execSQL(ProjectContract.DELETE_PROJECTS);
        db.execSQL(ProjectContract.DELETE_CHAPTERS);
        db.execSQL(ProjectContract.DELETE_UNITS);
        db.execSQL(ProjectContract.DELETE_TAKES);
        onCreate(db);
    }

    public boolean languageExists(String code){
        SQLiteDatabase db = getReadableDatabase();
        final String languageCountQuery = "SELECT COUNT(*) FROM " + ProjectContract.LanguageEntry.TABLE_LANGUAGE + " WHERE " + ProjectContract.LanguageEntry.LANGUAGE_CODE + "=?";
        boolean exists =  (DatabaseUtils.longForQuery(db, languageCountQuery, new String[]{code})) > 0;
        //db.close();
        return exists;
    }

    public boolean bookExists(String slug){
        SQLiteDatabase db = getReadableDatabase();
        final String bookCountQuery = "SELECT COUNT(*) FROM " + ProjectContract.BookEntry.TABLE_BOOK + " WHERE " + ProjectContract.BookEntry.BOOK_SLUG + "=?";
        boolean exists = (DatabaseUtils.longForQuery(db, bookCountQuery, new String[]{slug})) > 0;
        //db.close();
        return exists;
    }

    public boolean projectExists(Project project){
        return projectExists(project.getTargetLanguage(), project.getSlug(), project.getVersion());
    }

    public boolean projectExists(String languageCode, String slug, String version){
        if (!languageExists(languageCode)) {
            return false;
        }
        int languageId = getLanguageId(languageCode);
        int bookId = getBookId(slug);
        SQLiteDatabase db = getReadableDatabase();
        final String projectCountQuery = "SELECT COUNT(*) FROM " + ProjectContract.ProjectEntry.TABLE_PROJECT + " WHERE " + ProjectContract.ProjectEntry.PROJECT_TARGET_LANGUAGE_FK + "=?"
                + " AND " + ProjectContract.ProjectEntry.PROJECT_BOOK_FK + "=? AND " + ProjectContract.ProjectEntry.PROJECT_VERSION + "=?";
        boolean exists = (DatabaseUtils.longForQuery(db, projectCountQuery, new String[]{String.valueOf(languageId),String.valueOf(bookId), version})) > 0;
        //db.close();
        return exists;
    }

    public boolean chapterExists(Project project, int chapter){
        return chapterExists(project.getTargetLanguage(), project.getSlug(), project.getVersion(), chapter);
    }

    public boolean chapterExists(String languageCode, String slug, String version, int chapter){
        String projectId = String.valueOf(getProjectId(languageCode, slug, version));
        SQLiteDatabase db = getReadableDatabase();
        final String chapterCountQuery = String.format("SELECT COUNT(*) FROM %s WHERE %s=? AND %s=?",
                ProjectContract.ChapterEntry.TABLE_CHAPTER, ProjectContract.ChapterEntry.CHAPTER_PROJECT_FK, ProjectContract.ChapterEntry.CHAPTER_NUMBER);
        boolean exists = (DatabaseUtils.longForQuery(db, chapterCountQuery, new String[]{projectId, String.valueOf(chapter)})) > 0;
        //db.close();
        return exists;
    }

    public boolean unitExists(Project project, int chapter, int startVerse){
        return unitExists(project.getTargetLanguage(), project.getSlug(), project.getVersion(), chapter, startVerse);
    }

    public boolean unitExists(String languageCode, String slug, String version, int chapter, int startVerse){
        String projectId = String.valueOf(getProjectId(languageCode, slug, version));
        String chapterId = String.valueOf(getChapterId(languageCode, slug, version, chapter));
        SQLiteDatabase db = getReadableDatabase();
        final String unitCountQuery = String.format("SELECT COUNT(*) FROM %s WHERE %s=? AND %s=? AND %s=?",
                ProjectContract.UnitEntry.TABLE_UNIT, ProjectContract.UnitEntry.UNIT_PROJECT_FK, ProjectContract.UnitEntry.UNIT_CHAPTER_FK, ProjectContract.UnitEntry.UNIT_START_VERSE);
        boolean exists = (DatabaseUtils.longForQuery(db, unitCountQuery, new String[]{projectId, chapterId, String.valueOf(startVerse)})) > 0;
        //db.close();
        return exists;
    }

    public boolean takeExists(Project project, int chapter, int startVerse, int take){
        String unitId = String.valueOf(getUnitId(project, chapter, startVerse));
        SQLiteDatabase db = getReadableDatabase();
        final String takeCountQuery = String.format("SELECT COUNT(*) FROM %s WHERE %s=? AND %s=?",
                ProjectContract.TakeEntry.TABLE_TAKE, ProjectContract.TakeEntry.TAKE_UNIT_FK, ProjectContract.TakeEntry.TAKE_NUMBER);
        boolean exists = (DatabaseUtils.longForQuery(db, takeCountQuery, new String[]{unitId, String.valueOf(take)})) > 0;
        //db.close();
        return exists;
    }

    public boolean takeExists(FileNameExtractor fne){
        String unitId = String.valueOf(getUnitId(fne.getLang(), fne.getBook(), fne.getSource(), fne.getChapter(), fne.getStartVerse()));
        SQLiteDatabase db = getReadableDatabase();
        final String takeCountQuery = String.format("SELECT COUNT(*) FROM %s WHERE %s=? AND %s=?",
                ProjectContract.TakeEntry.TABLE_TAKE, ProjectContract.TakeEntry.TAKE_UNIT_FK, ProjectContract.TakeEntry.TAKE_NUMBER);
        boolean exists = (DatabaseUtils.longForQuery(db, takeCountQuery, new String[]{unitId, String.valueOf(fne.getTake())})) > 0;
        //db.close();
        return exists;
    }

    public int getLanguageId(String code) throws IllegalArgumentException {
        SQLiteDatabase db = getReadableDatabase();
        final String languageIdQuery = "SELECT " + ProjectContract.LanguageEntry._ID + " FROM " + ProjectContract.LanguageEntry.TABLE_LANGUAGE + " WHERE " + ProjectContract.LanguageEntry.LANGUAGE_CODE + "=?";
        int id = -1;
        try {
            id = (int) DatabaseUtils.longForQuery(db, languageIdQuery, new String[]{code});
        } catch (SQLiteDoneException e){
            //db.close();
            throw new IllegalArgumentException("Language code: " + code + " is not in the database.");
        }
        //db.close();
        return id;
    }

    public int getBookId(String slug) throws IllegalArgumentException {
        SQLiteDatabase db = getReadableDatabase();
        final String bookIdQuery = "SELECT " + ProjectContract.BookEntry._ID + " FROM " + ProjectContract.BookEntry.TABLE_BOOK + " WHERE " + ProjectContract.BookEntry.BOOK_SLUG + "=?";
        int id = -1;
        try {
            id = (int) DatabaseUtils.longForQuery(db, bookIdQuery, new String[]{slug});
        } catch (SQLiteDoneException e){
            //db.close();
            throw new IllegalArgumentException("Book slug: " + slug + " is not in the database.");
        }
        //db.close();
        return id;
    }

    public int getProjectId(Project project) throws IllegalArgumentException {
        return getProjectId(project.getTargetLanguage(), project.getSlug(), project.getVersion());
    }

    public int getProjectId(String languageCode, String slug, String version) throws IllegalArgumentException {
//        Logger.w(this.toString(), "Trying to get project Id for " + languageCode + " " + slug + " " + version);
        String languageId = String.valueOf(getLanguageId(languageCode));
        String bookId = String.valueOf(getBookId(slug));
        SQLiteDatabase db = getReadableDatabase();
        final String projectIdQuery = String.format("SELECT %s FROM %s WHERE %s=? AND %s=? AND %s=?",
                ProjectContract.ProjectEntry._ID, ProjectContract.ProjectEntry.TABLE_PROJECT, ProjectContract.ProjectEntry.PROJECT_TARGET_LANGUAGE_FK, ProjectContract.ProjectEntry.PROJECT_BOOK_FK, ProjectContract.ProjectEntry.PROJECT_VERSION);
        int id = -1;
        try {
            id = (int) DatabaseUtils.longForQuery(db, projectIdQuery, new String[]{languageId, bookId, version});
        } catch (SQLiteDoneException e) {
            //db.close();
            throw new IllegalArgumentException("Project not found in database");
        }
        //db.close();
        return id;
    }

    public int getChapterId(Project project, int chapter) throws IllegalArgumentException{
        return getChapterId(project.getTargetLanguage(), project.getSlug(), project.getVersion(), chapter);
    }

    public int getChapterId(String languageCode, String slug, String version, int chapter){
//        Logger.w(this.toString(), "trying to get chapter id for chapter " + chapter);
        String projectId = String.valueOf(getProjectId(languageCode, slug, version));
        SQLiteDatabase db = getReadableDatabase();
        final String chapterIdQuery = String.format("SELECT %s FROM %s WHERE %s=? AND %s=?",
                ProjectContract.ChapterEntry._ID, ProjectContract.ChapterEntry.TABLE_CHAPTER, ProjectContract.ChapterEntry.CHAPTER_PROJECT_FK, ProjectContract.ChapterEntry.CHAPTER_NUMBER);
        int id = -1;
        try {
            id = (int) DatabaseUtils.longForQuery(db, chapterIdQuery, new String[]{projectId, String.valueOf(chapter)});
        } catch (SQLiteDoneException e){
            //db.close();
            throw new IllegalArgumentException("Chapter not found in database");
        }
        //db.close();
        return id;
    }

    public int getUnitId(Project project, int chapter, int startVerse) throws IllegalArgumentException{
        return getUnitId(project.getTargetLanguage(), project.getSlug(), project.getVersion(), chapter, startVerse);
    }

    public int getUnitId(String languageCode, String slug, String version, int chapter, int startVerse) throws IllegalArgumentException{
//        Logger.w(this.toString(), "Trying to get unit Id for start verse " + startVerse);
        String projectId = String.valueOf(getProjectId(languageCode, slug, version));
        String chapterId = String.valueOf(getChapterId(languageCode, slug, version, chapter));
        SQLiteDatabase db = getReadableDatabase();
        final String unitIdQuery = String.format("SELECT %s FROM %s WHERE %s=? AND %s=? AND %s=?",
                ProjectContract.UnitEntry._ID, ProjectContract.UnitEntry.TABLE_UNIT, ProjectContract.UnitEntry.UNIT_PROJECT_FK, ProjectContract.UnitEntry.UNIT_CHAPTER_FK, ProjectContract.UnitEntry.UNIT_START_VERSE);
        int id = -1;
        try {
            id = (int) DatabaseUtils.longForQuery(db, unitIdQuery, new String[]{projectId, chapterId, String.valueOf(startVerse)});
        } catch (SQLiteDoneException e){
            //db.close();
            throw new IllegalArgumentException("Unit not found in database");
        }
        //db.close();
        return id;
    }

    public int getTakeId(FileNameExtractor fne) throws IllegalArgumentException{
        Logger.w(this.toString(), "Attempting to get take id for " + fne.getLang() + " " + fne.getBook() + " " + fne.getSource() + " verse start " + fne.getStartVerse() +  " take " + fne.getTake());
        String unitId = String.valueOf(getUnitId(fne.getLang(), fne.getBook(), fne.getSource(), fne.getChapter(), fne.getStartVerse()));
        SQLiteDatabase db = getReadableDatabase();
        final String takeIdQuery = String.format("SELECT %s FROM %s WHERE %s=? AND %s=?",
                ProjectContract.TakeEntry._ID, ProjectContract.TakeEntry.TABLE_TAKE, ProjectContract.TakeEntry.TAKE_UNIT_FK, ProjectContract.TakeEntry.TAKE_NUMBER);
        int id = -1;
        try {
            id = (int) DatabaseUtils.longForQuery(db, takeIdQuery, new String[]{unitId, String.valueOf(fne.getTake())});
        } catch (SQLiteDoneException e){
            //db.close();
            throw new IllegalArgumentException("Take not found in database.");
        }
        //db.close();
        return id;
    }

    public int getTakeCount(int unitId) throws IllegalArgumentException {
        int count = -1;
        String stringifiedId = String.valueOf(unitId);
        SQLiteDatabase db = getReadableDatabase();
        final String  query = String.format("SELECT COUNT(*) FROM %s WHERE %s=?",
                ProjectContract.TakeEntry.TABLE_TAKE, ProjectContract.TakeEntry.TAKE_UNIT_FK);
        try {
            count = (int) DatabaseUtils.longForQuery(db, query, new String[]{stringifiedId});
        } catch (SQLiteDoneException e) {
            throw new IllegalArgumentException("Take count cannot be retrieved for unitId: " + stringifiedId);
        }
        return count;
    }

    public String getLanguageName(String code) throws IllegalArgumentException{
        SQLiteDatabase db = getReadableDatabase();
        final String languageNameQuery = String.format("SELECT %s FROM %s WHERE %s=?",
                ProjectContract.LanguageEntry.LANGUAGE_NAME, ProjectContract.LanguageEntry.TABLE_LANGUAGE, ProjectContract.LanguageEntry.LANGUAGE_CODE);
        String name;
        try {
            name = DatabaseUtils.stringForQuery(db, languageNameQuery, new String[]{code});
        } catch (SQLiteDoneException e){
            //db.close();
            throw new IllegalArgumentException("Language: " + code + " not ");
        }
        //db.close();
        return name;
    }

    public String getLanguageCode(int id) throws IllegalArgumentException{
        SQLiteDatabase db = getReadableDatabase();
        final String languageNameQuery = String.format("SELECT %s FROM %s WHERE %s=?",
                ProjectContract.LanguageEntry.LANGUAGE_CODE, ProjectContract.LanguageEntry.TABLE_LANGUAGE, ProjectContract.LanguageEntry._ID);
        String code;
        try {
            code = DatabaseUtils.stringForQuery(db, languageNameQuery, new String[]{String.valueOf(id)});
        } catch (SQLiteDoneException e){
            //db.close();
            throw new IllegalArgumentException("Language id not found in database.");
        }
        //db.close();
        return code;
    }

    public String getBookName(String slug) throws IllegalArgumentException{
        SQLiteDatabase db = getReadableDatabase();
        final String bookNameQuery = String.format("SELECT %s FROM %s WHERE %s=?",
                ProjectContract.BookEntry.BOOK_NAME, ProjectContract.BookEntry.TABLE_BOOK, ProjectContract.BookEntry.BOOK_SLUG);
        String name;
        try {
            name = DatabaseUtils.stringForQuery(db, bookNameQuery, new String[]{slug});
        } catch (SQLiteDoneException e){
            //db.close();
            throw new IllegalArgumentException("Book slug: " + slug + " not found in database.");
        }
        //db.close();
        return name;
    }

    public String getBookSlug(int id) throws IllegalArgumentException{
        SQLiteDatabase db = getReadableDatabase();
        final String bookSlugQuery = String.format("SELECT %s FROM %s WHERE %s=?",
                ProjectContract.BookEntry.BOOK_SLUG, ProjectContract.BookEntry.TABLE_BOOK, ProjectContract.BookEntry._ID);
        String slug;
        try {
            slug = DatabaseUtils.stringForQuery(db, bookSlugQuery, new String[]{String.valueOf(id)});
        } catch (SQLiteDoneException e){
            //db.close();
            throw new IllegalArgumentException("Book id not found in database.");
        }
        //db.close();
        return slug;
    }

    public String getBookAnthology(String slug) throws IllegalArgumentException{
        SQLiteDatabase db = getReadableDatabase();
        final String bookNameQuery = String.format("SELECT %s FROM %s WHERE %s=?",
                ProjectContract.BookEntry.BOOK_ANTHOLOGY, ProjectContract.BookEntry.TABLE_BOOK, ProjectContract.BookEntry.BOOK_SLUG);
        String anthology = DatabaseUtils.stringForQuery(db, bookNameQuery, new String[]{slug});
        //db.close();
        return anthology;
    }

    public int getBookNumber(String slug) throws IllegalArgumentException{
        SQLiteDatabase db = getReadableDatabase();
        final String bookNameQuery = String.format("SELECT %s FROM %s WHERE %s=?",
                ProjectContract.BookEntry.BOOK_NUMBER, ProjectContract.BookEntry.TABLE_BOOK, ProjectContract.BookEntry.BOOK_SLUG);
        int number = -1;
        try {
            number = (int) DatabaseUtils.longForQuery(db, bookNameQuery, new String[]{slug});
        } catch (SQLiteDoneException e){
            //db.close();
            throw new IllegalArgumentException("Book slug: " + slug + " not found in database.");
        }
        //db.close();
        return number;
    }

    public void  addLanguage(String code, String name){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(ProjectContract.LanguageEntry.LANGUAGE_CODE, code);
        cv.put(ProjectContract.LanguageEntry.LANGUAGE_NAME, name);
        long result = db.insertWithOnConflict(ProjectContract.LanguageEntry.TABLE_LANGUAGE, null, cv, SQLiteDatabase.CONFLICT_IGNORE);
        //db.close();
    }

    public void addBook(String slug, String name, String anthology, int bookNumber){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(ProjectContract.BookEntry.BOOK_SLUG, slug);
        cv.put(ProjectContract.BookEntry.BOOK_NAME, name);
        cv.put(ProjectContract.BookEntry.BOOK_ANTHOLOGY, anthology);
        cv.put(ProjectContract.BookEntry.BOOK_NUMBER, bookNumber);
        long result = db.insertWithOnConflict(ProjectContract.BookEntry.TABLE_BOOK, null, cv, SQLiteDatabase.CONFLICT_IGNORE);
        //db.close();
    }

    public void addProject(Project p) throws IllegalArgumentException{
        int targetLanguageId = getLanguageId(p.getTargetLanguage());
        Integer sourceLanguageId = null;
        if(p.getSourceLanguage() != null && !p.getSourceLanguage().equals("")) {
            sourceLanguageId = getLanguageId(p.getSourceLanguage());
        }
        int bookId = getBookId(p.getSlug());

        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(ProjectContract.ProjectEntry.PROJECT_TARGET_LANGUAGE_FK, targetLanguageId);
        if(sourceLanguageId != null) {
            cv.put(ProjectContract.ProjectEntry.PROJECT_SOURCE_LANGUAGE_FK, sourceLanguageId);
        }
        cv.put(ProjectContract.ProjectEntry.PROJECT_BOOK_FK, bookId);
        cv.put(ProjectContract.ProjectEntry.PROJECT_VERSION, p.getVersion());
        cv.put(ProjectContract.ProjectEntry.PROJECT_MODE, p.getMode());
        cv.put(ProjectContract.ProjectEntry.PROJECT_CONTRIBUTORS, p.getContributors());
        cv.put(ProjectContract.ProjectEntry.PROJECT_SOURCE_AUDIO_PATH, p.getSourceAudioPath());
        cv.put(ProjectContract.ProjectEntry.PROJECT_NOTES, "");
        cv.put(ProjectContract.ProjectEntry.PROJECT_PROGRESS, 0);

        long result = db.insert(ProjectContract.ProjectEntry.TABLE_PROJECT, null, cv);
        //db.close();
    }

    public void addProject(String languageCode, String slug, String version, String mode) throws IllegalArgumentException{
        int targetLanguageId = getLanguageId(languageCode);
        Integer sourceLanguageId = null;

        int bookId = getBookId(slug);

        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(ProjectContract.ProjectEntry.PROJECT_TARGET_LANGUAGE_FK, targetLanguageId);
        if(sourceLanguageId != null) {
            cv.put(ProjectContract.ProjectEntry.PROJECT_SOURCE_LANGUAGE_FK, sourceLanguageId);
        }
        cv.put(ProjectContract.ProjectEntry.PROJECT_BOOK_FK, bookId);
        cv.put(ProjectContract.ProjectEntry.PROJECT_VERSION, version);
        cv.put(ProjectContract.ProjectEntry.PROJECT_MODE, mode);
        cv.put(ProjectContract.ProjectEntry.PROJECT_NOTES, "");
        cv.put(ProjectContract.ProjectEntry.PROJECT_PROGRESS, 0);

        long result = db.insert(ProjectContract.ProjectEntry.TABLE_PROJECT, null, cv);
        //db.close();
    }

    public void addChapter(Project project, int chapter) throws IllegalArgumentException{
        addChapter(project.getTargetLanguage(), project.getSlug(), project.getVersion(), chapter);
    }

    public void addChapter(String languageCode, String slug, String version, int chapter) throws IllegalArgumentException {
        int projectId = getProjectId(languageCode, slug, version);

        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(ProjectContract.ChapterEntry.CHAPTER_PROJECT_FK, projectId);
        cv.put(ProjectContract.ChapterEntry.CHAPTER_NUMBER, chapter);
        long result = db.insert(ProjectContract.ChapterEntry.TABLE_CHAPTER, null, cv);
        //db.close();
    }

    public void addUnit(Project project, int chapter, int startVerse) throws IllegalArgumentException{
        addUnit(project.getTargetLanguage(), project.getSlug(), project.getVersion(), chapter, startVerse);
    }

    public void addUnit(String languageCode, String slug, String version, int chapter, int startVerse) throws IllegalArgumentException {
        int projectId = getProjectId(languageCode, slug, version);
        int chapterId = getChapterId(languageCode, slug, version, chapter);

        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(ProjectContract.UnitEntry.UNIT_PROJECT_FK, projectId);
        cv.put(ProjectContract.UnitEntry.UNIT_CHAPTER_FK, chapterId);
        cv.put(ProjectContract.UnitEntry.UNIT_START_VERSE, startVerse);
        long result = db.insert(ProjectContract.UnitEntry.TABLE_UNIT, null, cv);
        //db.close();
    }

    public void addTake(FileNameExtractor fne, String takeFilename, String recordingMode, long timestamp, int rating) {
        String book = fne.getBook();
        String language = fne.getLang();
        String version = fne.getSource();
        int chapter = fne.getChapter();
        int start = fne.getStartVerse();
        if(!projectExists(language, book, version)){
            addProject(language, book, version, recordingMode);
            addChapter(language, book, version, chapter);
            addUnit(language, book, version, chapter, start);
        //If the chapter doesn't exist, then the unit can't either
        } else if(!chapterExists(language, book, version, chapter)){
            addChapter(language, book, version, chapter);
            addUnit(language, book, version, chapter, start);
        //chapter could exist, but unit may not yet
        } else if (!unitExists(language, book, version, chapter, start)){
            addUnit(language, book, version, chapter, start);
        }
        int unitId = getUnitId(language, book, version, chapter, start);

        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(ProjectContract.TakeEntry.TAKE_UNIT_FK, unitId);
        cv.put(ProjectContract.TakeEntry.TAKE_RATING, rating);
        cv.put(ProjectContract.TakeEntry.TAKE_NOTES, "");
        cv.put(ProjectContract.TakeEntry.TAKE_NUMBER, fne.getTake());
        cv.put(ProjectContract.TakeEntry.TAKE_FILENAME, takeFilename);
        cv.put(ProjectContract.TakeEntry.TAKE_TIMESTAMP, timestamp);
        long result = db.insertWithOnConflict(ProjectContract.TakeEntry.TABLE_TAKE, null, cv, SQLiteDatabase.CONFLICT_IGNORE);
        if(result > 0){
            autoSelectTake(unitId);
        }
    }

    public List<Project> getAllProjects(){
        List<Project> projectList = new ArrayList<>();
        String query = "SELECT * FROM " + ProjectContract.ProjectEntry.TABLE_PROJECT;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        if(cursor.moveToFirst()){
            do {
                Project project = new Project();
                project.setVersion(cursor.getString(cursor.getColumnIndex(ProjectContract.ProjectEntry.PROJECT_VERSION)));
                String targetLanguageCode = getLanguageCode(cursor.getInt(cursor.getColumnIndex(ProjectContract.ProjectEntry.PROJECT_TARGET_LANGUAGE_FK)));
                project.setTargetLanguage(targetLanguageCode);
                int sourceLanguageIndex = cursor.getColumnIndex(ProjectContract.ProjectEntry.PROJECT_SOURCE_LANGUAGE_FK);
                //Source language could be null
                if(cursor.getType(sourceLanguageIndex) == Cursor.FIELD_TYPE_INTEGER) {
                    String sourceLanguageCode = getLanguageCode(cursor.getInt(cursor.getColumnIndex(ProjectContract.ProjectEntry.PROJECT_SOURCE_LANGUAGE_FK)));
                    project.setSourceLanguage(sourceLanguageCode);
                    project.setSourceAudioPath(cursor.getString(cursor.getColumnIndex(ProjectContract.ProjectEntry.PROJECT_SOURCE_AUDIO_PATH)));
                }
                project.setMode(cursor.getString(cursor.getColumnIndex(ProjectContract.ProjectEntry.PROJECT_MODE)));
                String slug = getBookSlug(cursor.getInt(cursor.getColumnIndex(ProjectContract.ProjectEntry.PROJECT_BOOK_FK)));
                project.setSlug(slug);
                String anthology = getBookAnthology(slug);
                project.setProject(anthology);
                int number = getBookNumber(slug);
                project.setBookNumber(number);
                project.setContributors(cursor.getString(cursor.getColumnIndex(ProjectContract.ProjectEntry.PROJECT_CONTRIBUTORS)));

                projectList.add(project);
            } while(cursor.moveToNext());
        }
        cursor.close();
        //db.close();
        return projectList;
    }


    public Project getProject(int projectId){
        String query = "SELECT * FROM " + ProjectContract.ProjectEntry.TABLE_PROJECT + " WHERE " + ProjectContract.ProjectEntry._ID + " =" + String.valueOf(projectId);
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        Project project = null;
        if(cursor.moveToFirst()){
            project = new Project();
            project.setVersion(cursor.getString(cursor.getColumnIndex(ProjectContract.ProjectEntry.PROJECT_VERSION)));
            String targetLanguageCode = getLanguageCode(cursor.getInt(cursor.getColumnIndex(ProjectContract.ProjectEntry.PROJECT_TARGET_LANGUAGE_FK)));
            project.setTargetLanguage(targetLanguageCode);
            int sourceLanguageIndex = cursor.getColumnIndex(ProjectContract.ProjectEntry.PROJECT_SOURCE_LANGUAGE_FK);
            //Source language could be null
            if(cursor.getType(sourceLanguageIndex) == Cursor.FIELD_TYPE_INTEGER) {
                String sourceLanguageCode = getLanguageCode(cursor.getInt(cursor.getColumnIndex(ProjectContract.ProjectEntry.PROJECT_SOURCE_LANGUAGE_FK)));
                project.setSourceLanguage(sourceLanguageCode);
                project.setSourceAudioPath(cursor.getString(cursor.getColumnIndex(ProjectContract.ProjectEntry.PROJECT_SOURCE_AUDIO_PATH)));
            }
            project.setMode(cursor.getString(cursor.getColumnIndex(ProjectContract.ProjectEntry.PROJECT_MODE)));
            String slug = getBookSlug(cursor.getInt(cursor.getColumnIndex(ProjectContract.ProjectEntry.PROJECT_BOOK_FK)));
            project.setSlug(slug);
            String anthology = getBookAnthology(slug);
            project.setProject(anthology);
            int number = getBookNumber(slug);
            project.setBookNumber(number);
            project.setContributors(cursor.getString(cursor.getColumnIndex(ProjectContract.ProjectEntry.PROJECT_CONTRIBUTORS)));

        }
        cursor.close();
        return project;
    }


    public int getNumProjects(){
        SQLiteDatabase db = getReadableDatabase();
        String countQuery = "SELECT * FROM " + ProjectContract.ProjectEntry.TABLE_PROJECT;
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        //db.close();
        return count;
    }

    public int getChapterCheckingLevel(Project project, int chapter){
        String chapterId = String.valueOf(getChapterId(project, chapter));
        SQLiteDatabase db = getReadableDatabase();
        final String getChapter = String.format("SELECT %s FROM %s WHERE %s=?",
                ProjectContract.ChapterEntry.CHAPTER_CHECKING_LEVEL, ProjectContract.ChapterEntry.TABLE_CHAPTER, ProjectContract.ChapterEntry._ID);
        int checkingLevel = (int)DatabaseUtils.longForQuery(db, getChapter, new String[]{chapterId});
        //db.close();
        return checkingLevel;
    }

    public int getTakeRating(FileNameExtractor fne){
        String unitId = String.valueOf(getUnitId(fne.getLang(), fne.getBook(), fne.getSource(), fne.getChapter(), fne.getStartVerse()));
        SQLiteDatabase db = getReadableDatabase();
        final String getTake = String.format("SELECT %s FROM %s WHERE %s=? AND %s=?",
                ProjectContract.TakeEntry.TAKE_RATING, ProjectContract.TakeEntry.TABLE_TAKE, ProjectContract.TakeEntry.TAKE_UNIT_FK, ProjectContract.TakeEntry.TAKE_NUMBER);
        int rating = (int)DatabaseUtils.longForQuery(db, getTake, new String[]{unitId, String.valueOf(fne.getTake())});
        //db.close();
        return rating;
    }

    private int getSelectedTakeId(int unitId){
        SQLiteDatabase db = getReadableDatabase();
        final String getTake = String.format("SELECT %s FROM %s WHERE %s=?",
                ProjectContract.UnitEntry.UNIT_CHOSEN_TAKE_FK, ProjectContract.UnitEntry.TABLE_UNIT, ProjectContract.UnitEntry._ID);
        //int take = (int)DatabaseUtils.longForQuery(db, getTake, new String[]{unitId});
        Cursor cursor = db.rawQuery(getTake, new String[]{String.valueOf(unitId)});
        int takeIdCol = cursor.getColumnIndex(ProjectContract.UnitEntry.UNIT_CHOSEN_TAKE_FK);
        if(cursor.moveToFirst()) {
            if (!cursor.isNull(takeIdCol)) {
                int takeId = cursor.getInt(takeIdCol);
                cursor.close();
                return takeId;
            }
        }
        cursor.close();
        //db.close();
        return -1;
    }

    public int getSelectedTakeId(String languageCode, String slug, String version, int chapter, int startVerse){
        int unitId = getUnitId(languageCode, slug, version, chapter, startVerse);
        return getSelectedTakeId(unitId);
    }

    public int getSelectedTakeNumber(String languageCode, String slug, String version, int chapter, int startVerse){
        SQLiteDatabase db = getReadableDatabase();
        int takeId = getSelectedTakeId(languageCode, slug, version, chapter, startVerse);
        if(takeId != -1){
            final String getTakeNumber = String.format("SELECT %s FROM %s WHERE %s=?", ProjectContract.TakeEntry.TAKE_NUMBER, ProjectContract.TakeEntry.TABLE_TAKE, ProjectContract.TakeEntry._ID);
            Cursor cursor = db.rawQuery(getTakeNumber, new String[]{String.valueOf(takeId)});
            if(cursor.moveToFirst()) {
                int takeNumCol = cursor.getColumnIndex(ProjectContract.TakeEntry.TAKE_NUMBER);
                int takeNum = cursor.getInt(takeNumCol);
                cursor.close();
                //db.close();
                return takeNum;
            }
        }
        return -1;
    }

    public int getSelectedTakeNumber(FileNameExtractor fne){
        return getSelectedTakeNumber(fne.getLang(), fne.getBook(), fne.getSource(), fne.getChapter(), fne.getStartVerse());
    }

    public void setSelectedTake(File take){
        FileNameExtractor fne = new FileNameExtractor(take);
        setSelectedTake(fne);
    }

    public void setSelectedTake(FileNameExtractor fne){
        int unitId = getUnitId(fne.getLang(), fne.getBook(), fne.getSource(), fne.getChapter(), fne.getStartVerse());
        int takeId = getTakeId(fne);
        setSelectedTake(unitId, takeId);
    }

    public void setSelectedTake(int unitId, int takeId){
        String unitIdString = String.valueOf(unitId);
        String takeIdString = String.valueOf(takeId);
        SQLiteDatabase db = getReadableDatabase();
        final String replaceTakeWhere = String.format("%s=?", ProjectContract.UnitEntry._ID);
        ContentValues replaceWith = new ContentValues();
        replaceWith.put(ProjectContract.UnitEntry.UNIT_CHOSEN_TAKE_FK, takeIdString);
        db.update(ProjectContract.UnitEntry.TABLE_UNIT, replaceWith, replaceTakeWhere, new String[]{unitIdString});
        //db.close();
    }

    public void setTakeRating(FileNameExtractor fne, int rating){
        int unitId = getUnitId(fne.getLang(), fne.getBook(), fne.getSource(), fne.getChapter(), fne.getStartVerse());
        SQLiteDatabase db = getReadableDatabase();
        final String replaceTakeWhere = String.format("%s=? AND %s=?",
                ProjectContract.TakeEntry.TAKE_UNIT_FK, ProjectContract.TakeEntry.TAKE_NUMBER);
        ContentValues replaceWith = new ContentValues();
        replaceWith.put(ProjectContract.TakeEntry.TAKE_RATING, rating);
        int result = db.update(ProjectContract.TakeEntry.TABLE_TAKE, replaceWith, replaceTakeWhere, new String[]{String.valueOf(unitId), String.valueOf(fne.getTake())});
        if(result > 0){
            autoSelectTake(unitId);
        }
        //db.close();
    }

    public void setCheckingLevel(Project project, int chapter, int checkingLevel){
        String chapterId = String.valueOf(getChapterId(project, chapter));
        SQLiteDatabase db = getReadableDatabase();
        final String replaceChapterWhere = String.format("%s=?", ProjectContract.ChapterEntry._ID);
        ContentValues replaceWith = new ContentValues();
        replaceWith.put(ProjectContract.ChapterEntry.CHAPTER_CHECKING_LEVEL, checkingLevel);
        db.update(ProjectContract.ChapterEntry.TABLE_CHAPTER, replaceWith, replaceChapterWhere, new String[]{chapterId});
        //db.close();
    }

    public void setChapterProgress(int chapterId, int progress) {
        final String whereClause = String.format("%s=?", ProjectContract.ChapterEntry._ID);
        String chapterIdString = String.valueOf(chapterId);
        SQLiteDatabase db = getReadableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(ProjectContract.ChapterEntry.CHAPTER_PROGRESS, progress);
        db.update(ProjectContract.ChapterEntry.TABLE_CHAPTER, contentValues, whereClause, new String[]{chapterIdString});
        db.close();
    }

    public int getChapterProgress(int chapterId) {
        String chapterIdString = String.valueOf(chapterId);
        SQLiteDatabase db = getReadableDatabase();
        final String query = String.format("SELECT %s FROM %s WHERE %s=?",
                ProjectContract.ChapterEntry.CHAPTER_PROGRESS, ProjectContract.ChapterEntry.TABLE_CHAPTER, ProjectContract.ChapterEntry._ID);
        float progress = DatabaseUtils.longForQuery(db, query, new String[]{chapterIdString});
        db.close();
        return Math.round(progress);
    }

    public int getProjectProgressSum(int projectId) {
        String projectIdString = String.valueOf(projectId);
        SQLiteDatabase db = getReadableDatabase();
        final String query = String.format("SELECT SUM(%s) FROM %s WHERE %s=?",
                ProjectContract.ChapterEntry.CHAPTER_PROGRESS, ProjectContract.ChapterEntry.TABLE_CHAPTER, ProjectContract.ChapterEntry.CHAPTER_PROJECT_FK);
        int progress = (int) DatabaseUtils.longForQuery(db, query, new String[]{projectIdString});
        db.close();
        return progress;
    }

    public void removeSelectedTake(FileNameExtractor fne){
        String unitId = String.valueOf(getUnitId(fne.getLang(), fne.getBook(), fne.getSource(), fne.getChapter(), fne.getStartVerse()));
        SQLiteDatabase db = getReadableDatabase();
        final String replaceTakeWhere = String.format("%s=?",
                ProjectContract.UnitEntry._ID);
        ContentValues replaceWith = new ContentValues();
        replaceWith.putNull(ProjectContract.UnitEntry.UNIT_CHOSEN_TAKE_FK);
        db.update(ProjectContract.UnitEntry.TABLE_UNIT, replaceWith, replaceTakeWhere, new String[]{unitId});
        //db.close();
    }

    public void deleteProject(Project p){
        String projectId = String.valueOf(getProjectId(p));
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        final String deleteTakes = String.format("DELETE FROM %s WHERE %s IN (SELECT %s FROM %s WHERE %s=?)",
                ProjectContract.TakeEntry.TABLE_TAKE, ProjectContract.TakeEntry.TAKE_UNIT_FK, ProjectContract.UnitEntry._ID, ProjectContract.UnitEntry.TABLE_UNIT, ProjectContract.UnitEntry.UNIT_PROJECT_FK);
        db.execSQL(deleteTakes, new String[]{projectId});
        final String deleteUnits = String.format("DELETE FROM %s WHERE %s=?",
                ProjectContract.UnitEntry.TABLE_UNIT, ProjectContract.UnitEntry.UNIT_PROJECT_FK);
        db.execSQL(deleteUnits, new String[]{projectId});
        final String deleteChapters = String.format("DELETE FROM %s WHERE %s=?",
                ProjectContract.ChapterEntry.TABLE_CHAPTER, ProjectContract.ChapterEntry.CHAPTER_PROJECT_FK);
        db.execSQL(deleteChapters, new String[]{projectId});
        final String deleteProject = String.format("DELETE FROM %s WHERE %s=?",
                ProjectContract.ProjectEntry.TABLE_PROJECT, ProjectContract.ProjectEntry._ID);
        db.execSQL(deleteProject, new String[]{projectId});
        db.setTransactionSuccessful();
        db.endTransaction();
        //db.close();
    }

    public void deleteTake(FileNameExtractor fne){
        int unitId = getUnitId(fne.getLang(), fne.getBook(), fne.getSource(), fne.getChapter(), fne.getStartVerse());
        int takeId = getTakeId(fne);
        SQLiteDatabase db = getWritableDatabase();
        final String deleteWhere = String.format("%s=? AND %s=?",
                ProjectContract.TakeEntry.TAKE_UNIT_FK, ProjectContract.TakeEntry.TAKE_NUMBER);
//        final String deleteTake = String.format("DELETE FROM %s WHERE %s=? AND %s=?",
//                TakeEntry.TABLE_TAKE, TakeEntry.TAKE_UNIT_FK, TakeEntry.TAKE_NUMBER);
        //db.execSQL(deleteTake, new String[]{String.valueOf(unitId), String.valueOf(fne.getTake())});
        int takeSelected = getSelectedTakeId(unitId);
        int result = db.delete(ProjectContract.TakeEntry.TABLE_TAKE, deleteWhere, new String[]{String.valueOf(unitId), String.valueOf(fne.getTake())});
        if(result > 0 && takeSelected == takeId){
            autoSelectTake(unitId);
        }
    }

    /**
     * Computes the number of
     * @param project
     * @param numUnits
     * @return
     */
    public int[] getNumStartedUnitsInProject(Project project, int numUnits) {
        String projectId = String.valueOf(getProjectId(project));
//        final String numUnitsStarted = String.format("SELECT c.%s, COUNT(u.%s) FROM %s c LEFT JOIN %s u ON c.%s=u.%s LEFT JOIN %s t ON t.%s=u.%s WHERE c.%s=? AND t.%s IS NOT NULL GROUP BY c.%s",
//                ChapterEntry.CHAPTER_NUMBER, UnitEntry._ID, ChapterEntry.TABLE_CHAPTER, UnitEntry.TABLE_UNIT, ChapterEntry._ID, UnitEntry.UNIT_CHAPTER_FK, TakeEntry.TABLE_TAKE, TakeEntry.TAKE_UNIT_FK, UnitEntry._ID, ChapterEntry.CHAPTER_PROJECT_FK, TakeEntry._ID, ChapterEntry.CHAPTER_NUMBER);
        // SELECT number, count(_id) from (select u._id, c.number FROM chapters c LEFT JOIN units u ON c._id=u.chapter_fk LEFT JOIN takes t ON t.unit_fk=u._id WHERE c.project=2 AND t._id IS NOT NULL group by u._id ,c.number) group by number
        final String numUnitsStarted = String.format(
                "SELECT %s, COUNT(%s) FROM " +
                    "(SELECT u.%s, c.%s " +
                        "FROM %s c " +
                        "LEFT JOIN %s u " +
                            "ON c.%s=u.%s " +
                        "LEFT JOIN %s t " +
                            "ON t.%s=u.%s " +
                        "WHERE c.%s=? " +
                            "AND t.%s IS NOT NULL " +
                        "GROUP BY u.%s, c.%s) " +
                "GROUP BY %s",
                ProjectContract.ChapterEntry.CHAPTER_NUMBER, ProjectContract.ChapterEntry._ID,
                ProjectContract.UnitEntry._ID, ProjectContract.ChapterEntry.CHAPTER_NUMBER,
                ProjectContract.ChapterEntry.TABLE_CHAPTER,
                ProjectContract.UnitEntry.TABLE_UNIT,
                ProjectContract.ChapterEntry._ID, ProjectContract.UnitEntry.UNIT_CHAPTER_FK,
                ProjectContract.TakeEntry.TABLE_TAKE,
                ProjectContract.TakeEntry.TAKE_UNIT_FK, ProjectContract.UnitEntry._ID,
                ProjectContract.ChapterEntry.CHAPTER_PROJECT_FK,
                ProjectContract.TakeEntry._ID,
                ProjectContract.UnitEntry._ID, ProjectContract.ChapterEntry.CHAPTER_NUMBER,
                ProjectContract.ChapterEntry.CHAPTER_NUMBER);

        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(numUnitsStarted, new String[]{projectId});
        int[] numStartedUnits = new int[numUnits];

        if(c.getCount() > 0) {
            c.moveToFirst();
            do {
                int chapterNum = c.getInt(0);
                int unitCount = c.getInt(1);
                numStartedUnits[chapterNum-1] = unitCount;
            } while (c.moveToNext());
            return numStartedUnits;
        }
        return numStartedUnits;
    }

    public List<String> getTakesForChapterCompilation(Project project, int chapter){
        String chapterId = String.valueOf(getChapterId(project, chapter));

        final String chapterCompilationQuery = String.format(
                "SELECT name, MAX(score) FROM " +
                    "(SELECT u.%s as uid, t.%s AS name, (%s * 1000 + t.%s) + CASE WHEN %s IS NOT NULL AND %s=t.%s THEN 10000 ELSE 1 END AS score " + //_id, name, rating, _id, chosen_take_fk, chosen_take_fk, _id
                        "FROM %s c " + //chapters
                            "INNER JOIN %s u ON c.%s=u.%s " + //units, id, chapter_fk
                            "INNER JOIN %s t ON t.%s=u.%s " + //takes, unit_fk, id
                        "WHERE c.%s=?) " + //id, project_fk
                "GROUP BY uid",
                ProjectContract.UnitEntry._ID, ProjectContract.TakeEntry.TAKE_FILENAME, ProjectContract.TakeEntry.TAKE_RATING, ProjectContract.TakeEntry.TAKE_NUMBER, ProjectContract.UnitEntry.UNIT_CHOSEN_TAKE_FK, ProjectContract.UnitEntry.UNIT_CHOSEN_TAKE_FK, ProjectContract.TakeEntry._ID,
                ProjectContract.ChapterEntry.TABLE_CHAPTER,
                ProjectContract.UnitEntry.TABLE_UNIT, ProjectContract.ChapterEntry._ID, ProjectContract.UnitEntry.UNIT_CHAPTER_FK,
                ProjectContract.TakeEntry.TABLE_TAKE, ProjectContract.TakeEntry.TAKE_UNIT_FK, ProjectContract.UnitEntry._ID,
                ProjectContract.ChapterEntry._ID
        );
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(chapterCompilationQuery, new String[]{chapterId});
        List<String> takesToCompile = null;
        if(c.getCount() > 0){
            takesToCompile  = new ArrayList<>();
            c.moveToFirst();
            do {
                takesToCompile.add(c.getString(0));
            } while(c.moveToNext());
        }
        return takesToCompile;
    }

    public void resyncProjectWithFilesystem(Project project, List<File> takes, OnLanguageNotFound callback){
        importTakesToDatabase(takes, callback);
        if(projectExists(project)) {
            int projectId = getProjectId(project);
            String where = String.format("%s.%s=?",
                    ProjectContract.UnitEntry.TABLE_UNIT, ProjectContract.UnitEntry.UNIT_PROJECT_FK);
            String[] whereArgs = new String[]{String.valueOf(projectId)};
            removeTakesWithNoFiles(takes, where, whereArgs);
        }
    }

    public void resyncChapterWithFilesystem(Project project, int chapter, List<File> takes, OnLanguageNotFound callback){
        importTakesToDatabase(takes, callback);
        if(projectExists(project) && chapterExists(project, chapter)) {
            int projectId = getProjectId(project);
            int chapterId = getChapterId(project, chapter);
            String whereClause = String.format("%s.%s=? AND %s.%s=?",
                    ProjectContract.UnitEntry.TABLE_UNIT, ProjectContract.UnitEntry.UNIT_PROJECT_FK,
                    ProjectContract.UnitEntry.TABLE_UNIT, ProjectContract.UnitEntry.UNIT_CHAPTER_FK);
            String[] whereArgs = new String[]{String.valueOf(projectId), String.valueOf(chapterId)};
            removeTakesWithNoFiles(takes, whereClause, whereArgs);
        }
    }

//    private void resyncWithFilesystem(List<File> takes, String whereClause, String[] whereArgs, OnLanguageNotFound callback){
//        importTakesToDatabase(takes, callback);
//        removeTakesWithNoFiles(takes, whereClause, whereArgs);
//    }

    private void importTakesToDatabase(List<File> takes, OnLanguageNotFound callback){
        SQLiteDatabase db = getWritableDatabase();
        //create a temporary table to store take names from the filesystem
        db.execSQL(ProjectContract.DELETE_TEMP);
        db.execSQL(ProjectContract.TempEntry.CREATE_TEMP_TABLE);
        db.beginTransaction();
        //add all the take names to the temp table
        for(File f : takes){
            ContentValues cv = new ContentValues();
            FileNameExtractor fne = new FileNameExtractor(f);
            if(fne.matched()) {
                cv.put(ProjectContract.TempEntry.TEMP_TAKE_NAME, f.getName());
                cv.put(ProjectContract.TempEntry.TEMP_TIMESTAMP, f.lastModified());
                db.insert(ProjectContract.TempEntry.TABLE_TEMP, null, cv);
            }
        }
        //compare the names of all takes from the filesystem with the takes already in the database
        //names that do not have a match (are null in the left join) in the database need to be added
        final String getMissingTakes = String.format("SELECT t1.%s, t1.%s FROM %s AS t1 LEFT JOIN %s AS t2 ON t1.%s=t2.%s WHERE t2.%s IS NULL",
                ProjectContract.TempEntry.TEMP_TAKE_NAME, ProjectContract.TempEntry.TEMP_TIMESTAMP, ProjectContract.TempEntry.TABLE_TEMP, ProjectContract.TakeEntry.TABLE_TAKE, ProjectContract.TempEntry.TEMP_TAKE_NAME, ProjectContract.TakeEntry.TAKE_FILENAME, ProjectContract.TakeEntry.TAKE_FILENAME);
        Cursor c = db.rawQuery(getMissingTakes, null);
        //loop through all of the missing takes and add them to the db
        if(c.getCount() > 0){
            int nameIndex = c.getColumnIndex(ProjectContract.TempEntry.TEMP_TAKE_NAME);
            int timestampIndex = c.getColumnIndex(ProjectContract.TempEntry.TEMP_TIMESTAMP);
            c.moveToFirst();
            do {
                FileNameExtractor fne = new FileNameExtractor(c.getString(nameIndex));
                if(!languageExists(fne.getLang())) {
                    if(callback != null) {
                        String name = callback.requestLanguageName(fne.getLang());
                        addLanguage(fne.getLang(), name);
                    } else {
                        addLanguage(fne.getLang(), "???"); //missingno
                    }
                }
                //Need to get the mode out of the metadata because chunks of only one verse are indistinguishable from verse mode
                File dir = fne.getParentDirectory();
                WavFile wav = new WavFile(new File(dir, c.getString(nameIndex)));
                addTake(fne, c.getString(nameIndex), wav.getMetadata().getMode(), c.getLong(timestampIndex), 0);
            } while (c.moveToNext());
        }
        c.close();
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    /**
     * Removes takes from the database that adhere to the where clause and do not appear in the provided takes list
     * Example: takes contains a list of all takes in a chapter, the where clause matches takes with that projectId and chapterId
     * and the result is that all database entries with that projectId and chapterId without a matching file in the takes list are removed
     * from the database.
     *
     * This is used to resync part of the database in the event that a user manually removed a file from an external file manager application
     *
     * @param takes the list of files to NOT be removed from the database
     * @param whereClause which takes should be cleared from the database
     */
    private void removeTakesWithNoFiles(List<File> takes, String whereClause, String[] whereArgs) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        final String allTakesFromAProject = String.format("SELECT %s.%s as takefilename, %s.%s as takeid from %s LEFT JOIN %s ON %s.%s=%s.%s WHERE %s",
                ProjectContract.TakeEntry.TABLE_TAKE, ProjectContract.TakeEntry.TAKE_FILENAME, ProjectContract.TakeEntry.TABLE_TAKE, ProjectContract.TakeEntry._ID, //select
                ProjectContract.TakeEntry.TABLE_TAKE, ProjectContract.UnitEntry.TABLE_UNIT, //tables to join takes left join units
                ProjectContract.TakeEntry.TABLE_TAKE, ProjectContract.TakeEntry.TAKE_UNIT_FK, ProjectContract.UnitEntry.TABLE_UNIT, ProjectContract.UnitEntry._ID, //ON takes.unit_fk = units._id
                whereClause); //ie WHERE units.chapter_fk = ?

        final String danglingReferences = String.format("SELECT takefilename, takeid FROM (%s) LEFT JOIN %s as temps ON temps.%s=takefilename WHERE temps.%s IS NULL",
                allTakesFromAProject, ProjectContract.TempEntry.TABLE_TEMP,
                ProjectContract.TempEntry.TEMP_TAKE_NAME,
                ProjectContract.TempEntry.TEMP_TAKE_NAME
                );

//        //find all the takes in the db that do not have a match in the filesystem
//        final String deleteDanglingReferences = String.format("SELECT t1.%s, t1.%s FROM %s AS t1 LEFT JOIN %s AS t2 ON t1.%s=t2.%s WHERE t2.%s IS NULL",
//                ProjectContract.TakeEntry.TAKE_FILENAME, ProjectContract.TakeEntry._ID, ProjectContract.TakeEntry.TABLE_TAKE, ProjectContract.TempEntry.TABLE_TEMP, ProjectContract.TempEntry.TEMP_TAKE_NAME, ProjectContract.TakeEntry.TAKE_FILENAME, ProjectContract.TakeEntry.TAKE_FILENAME);
        //Cursor c = db.rawQuery(deleteDanglingReferences, null);
        Cursor c = db.rawQuery(danglingReferences, whereArgs);
        //for each of these takes that do not have a corresponding match, remove them from the database
        if(c.getCount() > 0) {
            int idIndex = c.getColumnIndex("takeid");
            final String deleteTake = String.format("%s=?", ProjectContract.TakeEntry._ID);
            final String removeSelectedTake = String.format("%s=?", ProjectContract.UnitEntry.UNIT_CHOSEN_TAKE_FK);
            c.moveToFirst();
            do {
                ContentValues cv = new ContentValues();
                cv.putNull(ProjectContract.UnitEntry.UNIT_CHOSEN_TAKE_FK);
                db.update(ProjectContract.UnitEntry.TABLE_UNIT, cv, removeSelectedTake, new String[] {String.valueOf(c.getInt(idIndex))});
                db.delete(ProjectContract.TakeEntry.TABLE_TAKE, deleteTake, new String[]{String.valueOf(c.getInt(idIndex))});
            } while (c.moveToNext());
        }
        c.close();
        db.execSQL(ProjectContract.DELETE_TEMP);
        db.setTransactionSuccessful();
        db.endTransaction();
    }


    public void resyncDbWithFs(List<File> takes, OnLanguageNotFound callback) {
        SQLiteDatabase db = getWritableDatabase();
        //create a temporary table to store take names from the filesystem
        db.execSQL(ProjectContract.DELETE_TEMP);
        db.execSQL(ProjectContract.TempEntry.CREATE_TEMP_TABLE);
        db.beginTransaction();
        //add all the take names to the temp table
        for(File f : takes){
            ContentValues cv = new ContentValues();
            FileNameExtractor fne = new FileNameExtractor(f);
            if(fne.matched()) {
                cv.put(ProjectContract.TempEntry.TEMP_TAKE_NAME, f.getName());
                cv.put(ProjectContract.TempEntry.TEMP_TIMESTAMP, f.lastModified());
                db.insert(ProjectContract.TempEntry.TABLE_TEMP, null, cv);
            }
        }
        //compare the names of all takes from the filesystem with the takes already in the database
        //names that do not have a match (are null in the left join) in the database need to be added
        final String getMissingTakes = String.format("SELECT t1.%s, t1.%s FROM %s AS t1 LEFT JOIN %s AS t2 ON t1.%s=t2.%s WHERE t2.%s IS NULL",
                ProjectContract.TempEntry.TEMP_TAKE_NAME, ProjectContract.TempEntry.TEMP_TIMESTAMP, ProjectContract.TempEntry.TABLE_TEMP, ProjectContract.TakeEntry.TABLE_TAKE, ProjectContract.TempEntry.TEMP_TAKE_NAME, ProjectContract.TakeEntry.TAKE_FILENAME, ProjectContract.TakeEntry.TAKE_FILENAME);
        Cursor c = db.rawQuery(getMissingTakes, null);
        //loop through all of the missing takes and add them to the db
        if(c.getCount() > 0){
            int nameIndex = c.getColumnIndex(ProjectContract.TempEntry.TEMP_TAKE_NAME);
            int timestampIndex = c.getColumnIndex(ProjectContract.TempEntry.TEMP_TIMESTAMP);
            c.moveToFirst();
            do {
                FileNameExtractor fne = new FileNameExtractor(c.getString(nameIndex));
                if(!languageExists(fne.getLang())) {
                    if(callback != null) {
                        String name = callback.requestLanguageName(fne.getLang());
                        addLanguage(fne.getLang(), name);
                    } else {
                        addLanguage(fne.getLang(), "???"); //missingno
                    }
                }
                //Need to get the mode out of the metadata because chunks of only one verse are indistinguishable from verse mode
                File dir = fne.getParentDirectory();
                try {
                    WavFile wav = new WavFile(new File(dir, c.getString(nameIndex)));
                    addTake(fne, c.getString(nameIndex), wav.getMetadata().getMode(), c.getLong(timestampIndex), 0);
                } catch (IllegalArgumentException e) {
                    //TODO: corrupt file, prompt to fix maybe? or delete? At least tell which file is causing a problem
                    Logger.e(this.toString(), "Error loading wav file named: " + dir + "/" + c.getString(nameIndex), e);
                    throw new RuntimeException(e);
                }
            } while (c.moveToNext());
        }
        c.close();
        //find all the takes in the db that do not have a match in the filesystem
        final String deleteDanglingReferences = String.format("SELECT t1.%s, t1.%s FROM %s AS t1 LEFT JOIN %s AS t2 ON t1.%s=t2.%s WHERE t2.%s IS NULL",
                ProjectContract.TakeEntry.TAKE_FILENAME, ProjectContract.TakeEntry._ID, ProjectContract.TakeEntry.TABLE_TAKE, ProjectContract.TempEntry.TABLE_TEMP, ProjectContract.TempEntry.TEMP_TAKE_NAME, ProjectContract.TakeEntry.TAKE_FILENAME, ProjectContract.TakeEntry.TAKE_FILENAME);
        c = db.rawQuery(deleteDanglingReferences, null);
        //for each of these takes that do not have a corresponding match, remove them from the database
        if(c.getCount() > 0) {
            int idIndex = c.getColumnIndex(ProjectContract.TakeEntry._ID);
            final String deleteTake = String.format("%s=?", ProjectContract.TakeEntry._ID);
            final String removeSelectedTake = String.format("%s=?", ProjectContract.UnitEntry.UNIT_CHOSEN_TAKE_FK);
            c.moveToFirst();
            do {
                ContentValues cv = new ContentValues();
                cv.putNull(ProjectContract.UnitEntry.UNIT_CHOSEN_TAKE_FK);
                db.update(ProjectContract.UnitEntry.TABLE_UNIT, cv, removeSelectedTake, new String[] {String.valueOf(c.getInt(idIndex))});
                db.delete(ProjectContract.TakeEntry.TABLE_TAKE, deleteTake, new String[]{String.valueOf(c.getInt(idIndex))});
            } while (c.moveToNext());
        }
        c.close();
        db.setTransactionSuccessful();
        db.endTransaction();
        db.execSQL(ProjectContract.DELETE_TEMP);
    }

    public void resyncBookWithFs(List<File> takes, OnLanguageNotFound callback) {
        SQLiteDatabase db = getWritableDatabase();
        //create a temporary table to store take names from the filesystem
        db.execSQL(ProjectContract.DELETE_TEMP);
        db.execSQL(ProjectContract.TempEntry.CREATE_TEMP_TABLE);
        db.beginTransaction();
        //add all the take names to the temp table
        for(File f : takes){
            ContentValues cv = new ContentValues();
            FileNameExtractor fne = new FileNameExtractor(f);
            if(fne.matched()) {
                cv.put(ProjectContract.TempEntry.TEMP_TAKE_NAME, f.getName());
                cv.put(ProjectContract.TempEntry.TEMP_TIMESTAMP, f.lastModified());
                db.insert(ProjectContract.TempEntry.TABLE_TEMP, null, cv);
            }
        }
        //compare the names of all takes from the filesystem with the takes already in the database
        //names that do not have a match (are null in the left join) in the database need to be added
        final String getMissingTakes = String.format("SELECT t1.%s, t1.%s FROM %s AS t1 LEFT JOIN %s AS t2 ON t1.%s=t2.%s WHERE t2.%s IS NULL",
                ProjectContract.TempEntry.TEMP_TAKE_NAME, ProjectContract.TempEntry.TEMP_TIMESTAMP, ProjectContract.TempEntry.TABLE_TEMP, ProjectContract.TakeEntry.TABLE_TAKE, ProjectContract.TempEntry.TEMP_TAKE_NAME, ProjectContract.TakeEntry.TAKE_FILENAME, ProjectContract.TakeEntry.TAKE_FILENAME);
        Cursor c = db.rawQuery(getMissingTakes, null);
        //loop through all of the missing takes and add them to the db
        if(c.getCount() > 0){
            int nameIndex = c.getColumnIndex(ProjectContract.TempEntry.TEMP_TAKE_NAME);
            int timestampIndex = c.getColumnIndex(ProjectContract.TempEntry.TEMP_TIMESTAMP);
            c.moveToFirst();
            do {
                FileNameExtractor fne = new FileNameExtractor(c.getString(nameIndex));
                if(!languageExists(fne.getLang())) {
                    if(callback != null) {
                        String name = callback.requestLanguageName(fne.getLang());
                        addLanguage(fne.getLang(), name);
                    } else {
                        addLanguage(fne.getLang(), "???"); //missingno
                    }
                }
                //Need to get the mode out of the metadata because chunks of only one verse are indistinguishable from verse mode
                File dir = fne.getParentDirectory();
                WavFile wav = new WavFile(new File(dir, c.getString(nameIndex)));
                addTake(fne, c.getString(nameIndex), wav.getMetadata().getMode(), c.getLong(timestampIndex), 0);
            } while (c.moveToNext());
        }
        c.close();
        //find all the takes in the db that do not have a match in the filesystem
        final String deleteDanglingReferences = String.format("SELECT t1.%s, t1.%s FROM %s AS t1 LEFT JOIN %s AS t2 ON t1.%s=t2.%s WHERE t2.%s IS NULL",
                ProjectContract.TakeEntry.TAKE_FILENAME, ProjectContract.TakeEntry._ID, ProjectContract.TakeEntry.TABLE_TAKE, ProjectContract.TempEntry.TABLE_TEMP, ProjectContract.TempEntry.TEMP_TAKE_NAME, ProjectContract.TakeEntry.TAKE_FILENAME, ProjectContract.TakeEntry.TAKE_FILENAME);
        c = db.rawQuery(deleteDanglingReferences, null);
        //for each of these takes that do not have a corresponding match, remove them from the database
        if(c.getCount() > 0) {
            int idIndex = c.getColumnIndex(ProjectContract.TakeEntry._ID);
            final String deleteTake = String.format("%s=?", ProjectContract.TakeEntry._ID);
            final String removeSelectedTake = String.format("%s=?", ProjectContract.UnitEntry.UNIT_CHOSEN_TAKE_FK);
            c.moveToFirst();
            do {
                ContentValues cv = new ContentValues();
                cv.putNull(ProjectContract.UnitEntry.UNIT_CHOSEN_TAKE_FK);
                db.update(ProjectContract.UnitEntry.TABLE_UNIT, cv, removeSelectedTake, new String[] {String.valueOf(c.getInt(idIndex))});
                db.delete(ProjectContract.TakeEntry.TABLE_TAKE, deleteTake, new String[]{String.valueOf(c.getInt(idIndex))});
            } while (c.moveToNext());
        }
        db.setTransactionSuccessful();
        db.endTransaction();
        db.execSQL(ProjectContract.DELETE_TEMP);
    }

    public List<Project> resyncProjectsWithFs(List<Project> allProjects, ProjectListResyncTask projectLevelResync) {
        List<Project> newProjects = new ArrayList<>();
        for (Project p : allProjects) {
            if(!languageExists(p.getTargetLanguage())) {
                String name = projectLevelResync.requestLanguageName(p.getTargetLanguage());
                addLanguage(p.getTargetLanguage(), name);
            }
            if(!projectExists(p)) {
                newProjects.add(p);
            }
            addProject(p);
        }
        return newProjects;
    }

    public void autoSelectTake(int unitId){
        SQLiteDatabase db = getReadableDatabase();
        final String autoSelect = String.format("SELECT %s FROM %s WHERE %s=? ORDER BY %s DESC, %s DESC LIMIT 1",
                ProjectContract.TakeEntry._ID, ProjectContract.TakeEntry.TABLE_TAKE, ProjectContract.TakeEntry.TAKE_UNIT_FK, ProjectContract.TakeEntry.TAKE_RATING, ProjectContract.TakeEntry.TAKE_TIMESTAMP);
        Cursor c = db.rawQuery(autoSelect, new String[]{String.valueOf(unitId)});
        if(c.getCount() > 0){
            c.moveToFirst();
            int takeId = c.getInt(0);
            setSelectedTake(unitId, takeId);
        }
    }

    public void addLanguages(Language[] languages) {
        SQLiteDatabase db = getReadableDatabase();
        db.beginTransaction();
        try {
            for (Language l : languages) {
                addLanguage(l.getCode(), l.getName());
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public void addBooks(Book[] books){
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            for(Book b : books){
                addBook(b.getSlug(), b.getName(), b.getAnthology(), b.getOrder());
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public Language[] getLanguages() {
        List<Language> languageList = new ArrayList<>();
        String query = "SELECT * FROM " + ProjectContract.LanguageEntry.TABLE_LANGUAGE;
        SQLiteDatabase db = getReadableDatabase();
        db.beginTransaction();
        Cursor cursor = db.rawQuery(query, null);
        if(cursor.moveToFirst()){
            do {
                String languageCode = cursor.getString(cursor.getColumnIndex(ProjectContract.LanguageEntry.LANGUAGE_CODE));
                String languageName = cursor.getString(cursor.getColumnIndex(ProjectContract.LanguageEntry.LANGUAGE_NAME));
                languageList.add(new Language(languageCode, languageName));
            } while(cursor.moveToNext());
        }
        cursor.close();
        db.endTransaction();
        //db.close();
        return languageList.toArray(new Language[languageList.size()]);
    }
}
