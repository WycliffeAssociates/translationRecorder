package org.wycliffeassociates.translationrecorder.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDoneException;
import android.database.sqlite.SQLiteOpenHelper;

import org.wycliffeassociates.translationrecorder.project.FileNameExtractor;
import org.wycliffeassociates.translationrecorder.project.Project;
import org.wycliffeassociates.translationrecorder.ProjectManager.tasks.resync.ProjectListResyncTask;
import org.wycliffeassociates.translationrecorder.Reporting.Logger;
import org.wycliffeassociates.translationrecorder.project.components.Anthology;
import org.wycliffeassociates.translationrecorder.project.components.Book;
import org.wycliffeassociates.translationrecorder.project.components.Language;
import org.wycliffeassociates.translationrecorder.project.components.Version;
import org.wycliffeassociates.translationrecorder.wav.WavFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sarabiaj on 5/10/2016.
 */
public class ProjectDatabaseHelper extends SQLiteOpenHelper {

    public void updateSourceAudio(int projectId, Project projectContainingUpdatedSource) {
        int sourceLanguageId = getLanguageId(projectContainingUpdatedSource.getSourceLanguageSlug());
        final String replaceTakeWhere = String.format("%s=?", ProjectContract.ProjectEntry._ID);
        SQLiteDatabase db = getWritableDatabase();
        ContentValues replaceWith = new ContentValues();
        replaceWith.put(ProjectContract.ProjectEntry.PROJECT_SOURCE_LANGUAGE_FK, String.valueOf(sourceLanguageId));
        replaceWith.put(ProjectContract.ProjectEntry.PROJECT_SOURCE_AUDIO_PATH, projectContainingUpdatedSource.getSourceAudioPath());
        db.update(ProjectContract.ProjectEntry.TABLE_PROJECT, replaceWith, replaceTakeWhere, new String[]{String.valueOf(projectId)});
    }

    public List<Project> projectsNeedingResync(List<Project> allProjects) {
        List<Project> needingResync = new ArrayList<>();
        if (allProjects != null) {
            for (Project p : allProjects) {
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

    public interface OnCorruptFile {
        void onCorruptFile(File file);
    }

    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "translation_projects";
    private Language[] languages;


    public ProjectDatabaseHelper(Context ctx) {
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
        db.execSQL(ProjectContract.AnthologyEntry.CREATE_ANTHOLOGY_TABLE);
        db.execSQL(ProjectContract.VersionEntry.CREATE_VERSION_TABLE);
        db.execSQL(ProjectContract.VersionRelationshipEntry.CREATE_VERSION_RELATIONSHIP_TABLE);
        //db.close();
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(ProjectContract.DELETE_LANGUAGE);
        db.execSQL(ProjectContract.DELETE_BOOKS);
        db.execSQL(ProjectContract.DELETE_PROJECTS);
        db.execSQL(ProjectContract.DELETE_CHAPTERS);
        db.execSQL(ProjectContract.DELETE_UNITS);
        db.execSQL(ProjectContract.DELETE_TAKES);
        db.execSQL(ProjectContract.DELETE_ANTHOLOGIES);
        db.execSQL(ProjectContract.DELETE_VERSIONS);
        db.execSQL(ProjectContract.DELETE_VERSION_RELATIONSHIPS);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public void deleteAllTables() {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(ProjectContract.DELETE_LANGUAGE);
        db.execSQL(ProjectContract.DELETE_BOOKS);
        db.execSQL(ProjectContract.DELETE_PROJECTS);
        db.execSQL(ProjectContract.DELETE_CHAPTERS);
        db.execSQL(ProjectContract.DELETE_UNITS);
        db.execSQL(ProjectContract.DELETE_TAKES);
        db.execSQL(ProjectContract.DELETE_ANTHOLOGIES);
        db.execSQL(ProjectContract.DELETE_VERSIONS);
        db.execSQL(ProjectContract.DELETE_VERSION_RELATIONSHIPS);
        onCreate(db);
    }

    public boolean languageExists(String languageSlug) {
        SQLiteDatabase db = getReadableDatabase();
        final String languageCountQuery = "SELECT COUNT(*) FROM " + ProjectContract.LanguageEntry.TABLE_LANGUAGE + " WHERE " + ProjectContract.LanguageEntry.LANGUAGE_CODE + "=?";
        boolean exists = (DatabaseUtils.longForQuery(db, languageCountQuery, new String[]{languageSlug})) > 0;
        //db.close();
        return exists;
    }

    public boolean bookExists(String bookSlug) {
        SQLiteDatabase db = getReadableDatabase();
        final String bookCountQuery = "SELECT COUNT(*) FROM " + ProjectContract.BookEntry.TABLE_BOOK + " WHERE " + ProjectContract.BookEntry.BOOK_SLUG + "=?";
        boolean exists = (DatabaseUtils.longForQuery(db, bookCountQuery, new String[]{bookSlug})) > 0;
        //db.close();
        return exists;
    }

    public boolean projectExists(Project project) {
        return projectExists(project.getTargetLanguageSlug(), project.getBookSlug(), project.getVersionSlug());
    }

    public boolean projectExists(String languageSlug, String bookSlug, String versionSlug) {
        if (!languageExists(languageSlug)) {
            return false;
        }
        int languageId = getLanguageId(languageSlug);
        int bookId = getBookId(bookSlug);
        int versionId = getVersionId(versionSlug);
        SQLiteDatabase db = getReadableDatabase();
        final String projectCountQuery = "SELECT COUNT(*) FROM " + ProjectContract.ProjectEntry.TABLE_PROJECT + " WHERE " + ProjectContract.ProjectEntry.PROJECT_TARGET_LANGUAGE_FK + "=?"
                + " AND " + ProjectContract.ProjectEntry.PROJECT_BOOK_FK + "=? AND " + ProjectContract.ProjectEntry.PROJECT_VERSION_FK + "=?";
        boolean exists = (DatabaseUtils.longForQuery(db, projectCountQuery, new String[]{String.valueOf(languageId), String.valueOf(bookId), String.valueOf(versionId)})) > 0;
        //db.close();
        return exists;
    }

    public boolean chapterExists(Project project, int chapter) {
        return chapterExists(project.getTargetLanguageSlug(), project.getBookSlug(), project.getVersionSlug(), chapter);
    }

    public boolean chapterExists(String languageSlug, String bookSlug, String versionSlug, int chapter) {
        String projectId = String.valueOf(getProjectId(languageSlug, bookSlug, versionSlug));
        SQLiteDatabase db = getReadableDatabase();
        final String chapterCountQuery = String.format("SELECT COUNT(*) FROM %s WHERE %s=? AND %s=?",
                ProjectContract.ChapterEntry.TABLE_CHAPTER, ProjectContract.ChapterEntry.CHAPTER_PROJECT_FK, ProjectContract.ChapterEntry.CHAPTER_NUMBER);
        boolean exists = (DatabaseUtils.longForQuery(db, chapterCountQuery, new String[]{projectId, String.valueOf(chapter)})) > 0;
        //db.close();
        return exists;
    }

    public boolean unitExists(Project project, int chapter, int startVerse) {
        return unitExists(project.getTargetLanguageSlug(), project.getBookSlug(), project.getVersionSlug(), chapter, startVerse);
    }

    public boolean unitExists(String languageSlug, String bookSlug, String versionSlug, int chapter, int startVerse) {
        String projectId = String.valueOf(getProjectId(languageSlug, bookSlug, versionSlug));
        String chapterId = String.valueOf(getChapterId(languageSlug, bookSlug, versionSlug, chapter));
        SQLiteDatabase db = getReadableDatabase();
        final String unitCountQuery = String.format("SELECT COUNT(*) FROM %s WHERE %s=? AND %s=? AND %s=?",
                ProjectContract.UnitEntry.TABLE_UNIT, ProjectContract.UnitEntry.UNIT_PROJECT_FK, ProjectContract.UnitEntry.UNIT_CHAPTER_FK, ProjectContract.UnitEntry.UNIT_START_VERSE);
        boolean exists = (DatabaseUtils.longForQuery(db, unitCountQuery, new String[]{projectId, chapterId, String.valueOf(startVerse)})) > 0;
        //db.close();
        return exists;
    }

    public boolean takeExists(Project project, int chapter, int startVerse, int take) {
        String unitId = String.valueOf(getUnitId(project, chapter, startVerse));
        SQLiteDatabase db = getReadableDatabase();
        final String takeCountQuery = String.format("SELECT COUNT(*) FROM %s WHERE %s=? AND %s=?",
                ProjectContract.TakeEntry.TABLE_TAKE, ProjectContract.TakeEntry.TAKE_UNIT_FK, ProjectContract.TakeEntry.TAKE_NUMBER);
        boolean exists = (DatabaseUtils.longForQuery(db, takeCountQuery, new String[]{unitId, String.valueOf(take)})) > 0;
        //db.close();
        return exists;
    }

    public boolean takeExists(FileNameExtractor fne) {
        String unitId = String.valueOf(getUnitId(fne.getLang(), fne.getBook(), fne.getVersion(), fne.getChapter(), fne.getStartVerse()));
        SQLiteDatabase db = getReadableDatabase();
        final String takeCountQuery = String.format("SELECT COUNT(*) FROM %s WHERE %s=? AND %s=?",
                ProjectContract.TakeEntry.TABLE_TAKE, ProjectContract.TakeEntry.TAKE_UNIT_FK, ProjectContract.TakeEntry.TAKE_NUMBER);
        boolean exists = (DatabaseUtils.longForQuery(db, takeCountQuery, new String[]{unitId, String.valueOf(fne.getTake())})) > 0;
        //db.close();
        return exists;
    }

    public int getLanguageId(String languageSlug) throws IllegalArgumentException {
        SQLiteDatabase db = getReadableDatabase();
        final String languageIdQuery = "SELECT " + ProjectContract.LanguageEntry._ID + " FROM " + ProjectContract.LanguageEntry.TABLE_LANGUAGE + " WHERE " + ProjectContract.LanguageEntry.LANGUAGE_CODE + "=?";
        int id = -1;
        try {
            id = (int) DatabaseUtils.longForQuery(db, languageIdQuery, new String[]{languageSlug});
        } catch (SQLiteDoneException e) {
            throw new IllegalArgumentException("Language slug: " + languageSlug + " is not in the database.");
        }
        return id;
    }

    public int getVersionId(String versionSlug) throws IllegalArgumentException {
        SQLiteDatabase db = getReadableDatabase();
        final String versionIdQuery = "SELECT " + ProjectContract.VersionEntry._ID + " FROM " + ProjectContract.VersionEntry.TABLE_VERSION + " WHERE " + ProjectContract.VersionEntry.VERSION_SLUG + "=?";
        int id = -1;
        try {
            id = (int) DatabaseUtils.longForQuery(db, versionIdQuery, new String[]{versionSlug});
        } catch (SQLiteDoneException e) {
            throw new IllegalArgumentException("Version slug: " + versionSlug + " is not in the database.");
        }
        return id;
    }

    public int getAnthologyId(String anthologySlug) throws IllegalArgumentException {
        SQLiteDatabase db = getReadableDatabase();
        final String anthologyIdQuery = "SELECT " + ProjectContract.AnthologyEntry._ID + " FROM " + ProjectContract.AnthologyEntry.TABLE_ANTHOLOGY + " WHERE " + ProjectContract.AnthologyEntry.ANTHOLOGY_SLUG + "=?";
        int id = -1;
        try {
            id = (int) DatabaseUtils.longForQuery(db, anthologyIdQuery, new String[]{anthologySlug});
        } catch (SQLiteDoneException e) {
            throw new IllegalArgumentException("Anthology slug: " + anthologySlug + " is not in the database.");
        }
        return id;
    }

    public int getBookId(String bookSlug) throws IllegalArgumentException {
        SQLiteDatabase db = getReadableDatabase();
        final String bookIdQuery = "SELECT " + ProjectContract.BookEntry._ID + " FROM " + ProjectContract.BookEntry.TABLE_BOOK + " WHERE " + ProjectContract.BookEntry.BOOK_SLUG + "=?";
        int id = -1;
        try {
            id = (int) DatabaseUtils.longForQuery(db, bookIdQuery, new String[]{bookSlug});
        } catch (SQLiteDoneException e) {
            throw new IllegalArgumentException("Book slug: " + bookSlug + " is not in the database.");
        }
        return id;
    }

    public int getProjectId(Project project) throws IllegalArgumentException {
        return getProjectId(project.getTargetLanguageSlug(), project.getBookSlug(), project.getVersionSlug());
    }

    public int getProjectId(String languageSlug, String bookSlug, String versionSlug) throws IllegalArgumentException {
//        Logger.w(this.toString(), "Trying to get project Id for " + languageSlug + " " + bookSlug + " " + versionSlug);
        String languageId = String.valueOf(getLanguageId(languageSlug));
        String bookId = String.valueOf(getBookId(bookSlug));
        String versionId = String.valueOf(getVersionId(versionSlug));
        SQLiteDatabase db = getReadableDatabase();
        final String projectIdQuery = String.format("SELECT %s FROM %s WHERE %s=? AND %s=? AND %s=?",
                ProjectContract.ProjectEntry._ID, ProjectContract.ProjectEntry.TABLE_PROJECT, ProjectContract.ProjectEntry.PROJECT_TARGET_LANGUAGE_FK, ProjectContract.ProjectEntry.PROJECT_BOOK_FK, ProjectContract.ProjectEntry.PROJECT_VERSION_FK);
        int id = -1;
        try {
            id = (int) DatabaseUtils.longForQuery(db, projectIdQuery, new String[]{languageId, bookId, versionId});
        } catch (SQLiteDoneException e) {
            //db.close();
            throw new IllegalArgumentException("Project not found in database");
        }
        //db.close();
        return id;
    }

    public int getChapterId(Project project, int chapter) throws IllegalArgumentException {
        return getChapterId(project.getTargetLanguageSlug(), project.getBookSlug(), project.getVersionSlug(), chapter);
    }

    public int getChapterId(String languageSlug, String bookSlug, String versionSlug, int chapter) {
//        Logger.w(this.toString(), "trying to get chapter id for chapter " + chapter);
        String projectId = String.valueOf(getProjectId(languageSlug, bookSlug, versionSlug));
        SQLiteDatabase db = getReadableDatabase();
        final String chapterIdQuery = String.format("SELECT %s FROM %s WHERE %s=? AND %s=?",
                ProjectContract.ChapterEntry._ID, ProjectContract.ChapterEntry.TABLE_CHAPTER, ProjectContract.ChapterEntry.CHAPTER_PROJECT_FK, ProjectContract.ChapterEntry.CHAPTER_NUMBER);
        int id = -1;
        try {
            id = (int) DatabaseUtils.longForQuery(db, chapterIdQuery, new String[]{projectId, String.valueOf(chapter)});
        } catch (SQLiteDoneException e) {
            //db.close();
            throw new IllegalArgumentException("Chapter not found in database");
        }
        //db.close();
        return id;
    }

    public int getUnitId(Project project, int chapter, int startVerse) throws IllegalArgumentException {
        return getUnitId(project.getTargetLanguageSlug(), project.getBookSlug(), project.getVersionSlug(), chapter, startVerse);
    }

    public int getUnitId(String languageSlug, String bookSlug, String versionSlug, int chapter, int startVerse) throws IllegalArgumentException {
//        Logger.w(this.toString(), "Trying to get unit Id for start verse " + startVerse);
        String projectId = String.valueOf(getProjectId(languageSlug, bookSlug, versionSlug));
        String chapterId = String.valueOf(getChapterId(languageSlug, bookSlug, versionSlug, chapter));
        SQLiteDatabase db = getReadableDatabase();
        final String unitIdQuery = String.format("SELECT %s FROM %s WHERE %s=? AND %s=? AND %s=?",
                ProjectContract.UnitEntry._ID, ProjectContract.UnitEntry.TABLE_UNIT, ProjectContract.UnitEntry.UNIT_PROJECT_FK, ProjectContract.UnitEntry.UNIT_CHAPTER_FK, ProjectContract.UnitEntry.UNIT_START_VERSE);
        int id = -1;
        try {
            id = (int) DatabaseUtils.longForQuery(db, unitIdQuery, new String[]{projectId, chapterId, String.valueOf(startVerse)});
        } catch (SQLiteDoneException e) {
            //db.close();
            throw new IllegalArgumentException("Unit not found in database");
        }
        //db.close();
        return id;
    }

    public int getTakeId(FileNameExtractor fne) throws IllegalArgumentException {
        Logger.w(this.toString(), "Attempting to get take id for " + fne.getLang() + " " + fne.getBook() + " " + fne.getVersion() + " verse start " + fne.getStartVerse() + " take " + fne.getTake());
        String unitId = String.valueOf(getUnitId(fne.getLang(), fne.getBook(), fne.getVersion(), fne.getChapter(), fne.getStartVerse()));
        SQLiteDatabase db = getReadableDatabase();
        final String takeIdQuery = String.format("SELECT %s FROM %s WHERE %s=? AND %s=?",
                ProjectContract.TakeEntry._ID, ProjectContract.TakeEntry.TABLE_TAKE, ProjectContract.TakeEntry.TAKE_UNIT_FK, ProjectContract.TakeEntry.TAKE_NUMBER);
        int id = -1;
        try {
            id = (int) DatabaseUtils.longForQuery(db, takeIdQuery, new String[]{unitId, String.valueOf(fne.getTake())});
        } catch (SQLiteDoneException e) {
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
        final String query = String.format("SELECT COUNT(*) FROM %s WHERE %s=?",
                ProjectContract.TakeEntry.TABLE_TAKE, ProjectContract.TakeEntry.TAKE_UNIT_FK);
        try {
            count = (int) DatabaseUtils.longForQuery(db, query, new String[]{stringifiedId});
        } catch (SQLiteDoneException e) {
            throw new IllegalArgumentException("Take count cannot be retrieved for unitId: " + stringifiedId);
        }
        return count;
    }

    public String getLanguageName(String languageSlug) throws IllegalArgumentException {
        SQLiteDatabase db = getReadableDatabase();
        final String languageNameQuery = String.format("SELECT %s FROM %s WHERE %s=?",
                ProjectContract.LanguageEntry.LANGUAGE_NAME, ProjectContract.LanguageEntry.TABLE_LANGUAGE, ProjectContract.LanguageEntry.LANGUAGE_CODE);
        String name;
        try {
            name = DatabaseUtils.stringForQuery(db, languageNameQuery, new String[]{languageSlug});
        } catch (SQLiteDoneException e) {
            //db.close();
            throw new IllegalArgumentException("Language: " + languageSlug + " not ");
        }
        //db.close();
        return name;
    }

    public String getLanguageCode(int id) throws IllegalArgumentException {
        SQLiteDatabase db = getReadableDatabase();
        final String languageNameQuery = String.format("SELECT %s FROM %s WHERE %s=?",
                ProjectContract.LanguageEntry.LANGUAGE_CODE, ProjectContract.LanguageEntry.TABLE_LANGUAGE, ProjectContract.LanguageEntry._ID);
        String code;
        try {
            code = DatabaseUtils.stringForQuery(db, languageNameQuery, new String[]{String.valueOf(id)});
        } catch (SQLiteDoneException e) {
            //db.close();
            throw new IllegalArgumentException("Language id not found in database.");
        }
        //db.close();
        return code;
    }

    public String getBookName(String bookSlug) throws IllegalArgumentException {
        SQLiteDatabase db = getReadableDatabase();
        final String bookNameQuery = String.format("SELECT %s FROM %s WHERE %s=?",
                ProjectContract.BookEntry.BOOK_NAME, ProjectContract.BookEntry.TABLE_BOOK, ProjectContract.BookEntry.BOOK_SLUG);
        String name;
        try {
            name = DatabaseUtils.stringForQuery(db, bookNameQuery, new String[]{bookSlug});
        } catch (SQLiteDoneException e) {
            //db.close();
            throw new IllegalArgumentException("Book slug: " + bookSlug + " not found in database.");
        }
        //db.close();
        return name;
    }

    public String getBookSlug(int id) throws IllegalArgumentException {
        SQLiteDatabase db = getReadableDatabase();
        final String bookSlugQuery = String.format("SELECT %s FROM %s WHERE %s=?",
                ProjectContract.BookEntry.BOOK_SLUG, ProjectContract.BookEntry.TABLE_BOOK, ProjectContract.BookEntry._ID);
        String slug;
        try {
            slug = DatabaseUtils.stringForQuery(db, bookSlugQuery, new String[]{String.valueOf(id)});
        } catch (SQLiteDoneException e) {
            //db.close();
            throw new IllegalArgumentException("Book id not found in database.");
        }
        //db.close();
        return slug;
    }


    public String getVersionName(int id) throws IllegalArgumentException {
        SQLiteDatabase db = getReadableDatabase();
        final String versionSlugQuery = String.format("SELECT %s FROM %s WHERE %s=?",
                ProjectContract.VersionEntry.VERSION_NAME, ProjectContract.VersionEntry.TABLE_VERSION, ProjectContract.VersionEntry._ID);
        String name;
        try {
            name = DatabaseUtils.stringForQuery(db, versionSlugQuery, new String[]{String.valueOf(id)});
        } catch (SQLiteDoneException e) {
            //db.close();
            throw new IllegalArgumentException("Version id not found in database.");
        }
        //db.close();
        return name;
    }

    public String getVersionSlug(int id) throws IllegalArgumentException {
        SQLiteDatabase db = getReadableDatabase();
        final String versionSlugQuery = String.format("SELECT %s FROM %s WHERE %s=?",
                ProjectContract.VersionEntry.VERSION_SLUG, ProjectContract.VersionEntry.TABLE_VERSION, ProjectContract.VersionEntry._ID);
        String slug;
        try {
            slug = DatabaseUtils.stringForQuery(db, versionSlugQuery, new String[]{String.valueOf(id)});
        } catch (SQLiteDoneException e) {
            //db.close();
            throw new IllegalArgumentException("Version id not found in database.");
        }
        //db.close();
        return slug;
    }

    public String getAnthologySlug(int id) throws IllegalArgumentException {
        SQLiteDatabase db = getReadableDatabase();
        final String anthologySlugQuery = String.format("SELECT %s FROM %s WHERE %s=?",
                ProjectContract.AnthologyEntry.ANTHOLOGY_SLUG, ProjectContract.AnthologyEntry.TABLE_ANTHOLOGY, ProjectContract.AnthologyEntry._ID);
        String slug;
        try {
            slug = DatabaseUtils.stringForQuery(db, anthologySlugQuery, new String[]{String.valueOf(id)});
        } catch (SQLiteDoneException e) {
            //db.close();
            throw new IllegalArgumentException("Anthology id not found in database.");
        }
        //db.close();
        return slug;
    }

    //TODO
    public String getAnthologySlug(String bookSlug) throws IllegalArgumentException {
        SQLiteDatabase db = getReadableDatabase();
        final String bookNameQuery = String.format("SELECT %s FROM %s WHERE %s=?",
                ProjectContract.BookEntry.BOOK_ANTHOLOGY_FK, ProjectContract.BookEntry.TABLE_BOOK, ProjectContract.BookEntry.BOOK_SLUG);
        int anthologyId = (int)DatabaseUtils.longForQuery(db, bookNameQuery, new String[]{bookSlug});
        return getAnthologySlug(anthologyId);
    }

    public int getBookNumber(String bookSlug) throws IllegalArgumentException {
        SQLiteDatabase db = getReadableDatabase();
        final String bookNameQuery = String.format("SELECT %s FROM %s WHERE %s=?",
                ProjectContract.BookEntry.BOOK_NUMBER, ProjectContract.BookEntry.TABLE_BOOK, ProjectContract.BookEntry.BOOK_SLUG);
        int number = -1;
        try {
            number = (int) DatabaseUtils.longForQuery(db, bookNameQuery, new String[]{bookSlug});
        } catch (SQLiteDoneException e) {
            //db.close();
            throw new IllegalArgumentException("Book slug: " + bookSlug + " not found in database.");
        }
        //db.close();
        return number;
    }

    public void addLanguage(String languageSlug, String name) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(ProjectContract.LanguageEntry.LANGUAGE_CODE, languageSlug);
        cv.put(ProjectContract.LanguageEntry.LANGUAGE_NAME, name);
        long result = db.insertWithOnConflict(ProjectContract.LanguageEntry.TABLE_LANGUAGE, null, cv, SQLiteDatabase.CONFLICT_IGNORE);
        //db.close();
    }

    public void addLanguages(Language[] languages) {
        SQLiteDatabase db = getReadableDatabase();
        db.beginTransaction();
        try {
            for (Language l : languages) {
                addLanguage(l.getSlug(), l.getName());
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public void addAnthology(String anthologySlug, String name, String resource, String regex, int mask) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(ProjectContract.AnthologyEntry.ANTHOLOGY_SLUG, anthologySlug);
        cv.put(ProjectContract.AnthologyEntry.ANTHOLOGY_NAME, name);
        cv.put(ProjectContract.AnthologyEntry.ANTHOLOGY_RESOURCE, resource);
        cv.put(ProjectContract.AnthologyEntry.ANTHOLOGY_REGEX, regex);
        cv.put(ProjectContract.AnthologyEntry.ANTHOLOGY_MASK, mask);
        long result = db.insertWithOnConflict(ProjectContract.AnthologyEntry.TABLE_ANTHOLOGY, null, cv, SQLiteDatabase.CONFLICT_IGNORE);
    }

    public void addBook(String bookSlug, String bookName, String anthologySlug, int bookNumber) {
        int anthologyId = getAnthologyId(anthologySlug);
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(ProjectContract.BookEntry.BOOK_SLUG, bookSlug);
        cv.put(ProjectContract.BookEntry.BOOK_NAME, bookName);
        cv.put(ProjectContract.BookEntry.BOOK_ANTHOLOGY_FK, anthologyId);
        cv.put(ProjectContract.BookEntry.BOOK_NUMBER, bookNumber);
        long result = db.insertWithOnConflict(ProjectContract.BookEntry.TABLE_BOOK, null, cv, SQLiteDatabase.CONFLICT_IGNORE);
        //db.close();
    }

    public void addBooks(Book[] books) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            for (Book b : books) {
                addBook(b.getSlug(), b.getName(), b.getAnthology(), b.getOrder());
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public void addVersion(String versionSlug, String versionName) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(ProjectContract.VersionEntry.VERSION_SLUG, versionSlug);
        cv.put(ProjectContract.VersionEntry.VERSION_NAME, versionName);
        long result = db.insertWithOnConflict(ProjectContract.VersionEntry.TABLE_VERSION, null, cv, SQLiteDatabase.CONFLICT_IGNORE);
    }

    public void addVersions(Version[] versions) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            for (Version v : versions) {
                addVersion(v.getSlug(), v.getName());
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public void addVersionRelationships(String anthologySlug, Version[] versions) {
        int anthId = getAnthologyId(anthologySlug);
        SQLiteDatabase db = getWritableDatabase();
        for(Version v : versions) {
            int versionId = getVersionId(v.getSlug());
            ContentValues cv = new ContentValues();
            cv.put(ProjectContract.VersionRelationshipEntry.ANTHOLOGY_FK, anthId);
            cv.put(ProjectContract.VersionRelationshipEntry.VERSION_FK, versionId);
            long result = db.insertWithOnConflict(ProjectContract.VersionRelationshipEntry.TABLE_VERSION_RELATIONSHIP, null, cv, SQLiteDatabase.CONFLICT_IGNORE);
        }
    }

    public void addProject(Project p) throws IllegalArgumentException {
        int targetLanguageId = getLanguageId(p.getTargetLanguageSlug());
        Integer sourceLanguageId = null;
        if (p.getSourceLanguageSlug() != null && !p.getSourceLanguageSlug().equals("")) {
            sourceLanguageId = getLanguageId(p.getSourceLanguageSlug());
        }
        int bookId = getBookId(p.getBookSlug());
        int versionId = getVersionId(p.getVersionSlug());

        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(ProjectContract.ProjectEntry.PROJECT_TARGET_LANGUAGE_FK, targetLanguageId);
        if (sourceLanguageId != null) {
            cv.put(ProjectContract.ProjectEntry.PROJECT_SOURCE_LANGUAGE_FK, sourceLanguageId);
        }
        cv.put(ProjectContract.ProjectEntry.PROJECT_BOOK_FK, bookId);
        cv.put(ProjectContract.ProjectEntry.PROJECT_VERSION_FK, versionId);
        cv.put(ProjectContract.ProjectEntry.PROJECT_MODE, p.getMode());
        cv.put(ProjectContract.ProjectEntry.PROJECT_CONTRIBUTORS, p.getContributors());
        cv.put(ProjectContract.ProjectEntry.PROJECT_SOURCE_AUDIO_PATH, p.getSourceAudioPath());
        cv.put(ProjectContract.ProjectEntry.PROJECT_NOTES, "");
        cv.put(ProjectContract.ProjectEntry.PROJECT_PROGRESS, 0);

        long result = db.insert(ProjectContract.ProjectEntry.TABLE_PROJECT, null, cv);
        //db.close();
    }

    public void addProject(String languageSlug, String bookSlug, String versionSlug, String mode) throws IllegalArgumentException {
        int targetLanguageId = getLanguageId(languageSlug);
        int bookId = getBookId(bookSlug);
        int versionId = getVersionId(versionSlug);

        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(ProjectContract.ProjectEntry.PROJECT_TARGET_LANGUAGE_FK, targetLanguageId);
        cv.put(ProjectContract.ProjectEntry.PROJECT_BOOK_FK, bookId);
        cv.put(ProjectContract.ProjectEntry.PROJECT_VERSION_FK, versionId);
        cv.put(ProjectContract.ProjectEntry.PROJECT_MODE, mode);
        cv.put(ProjectContract.ProjectEntry.PROJECT_NOTES, "");
        cv.put(ProjectContract.ProjectEntry.PROJECT_PROGRESS, 0);

        long result = db.insert(ProjectContract.ProjectEntry.TABLE_PROJECT, null, cv);
        //db.close();
    }

    public void addChapter(Project project, int chapter) throws IllegalArgumentException {
        addChapter(project.getTargetLanguageSlug(), project.getBookSlug(), project.getVersionSlug(), chapter);
    }

    public void addChapter(String languageSlug, String bookSlug, String versionSlug, int chapter) throws IllegalArgumentException {
        int projectId = getProjectId(languageSlug, bookSlug, versionSlug);

        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(ProjectContract.ChapterEntry.CHAPTER_PROJECT_FK, projectId);
        cv.put(ProjectContract.ChapterEntry.CHAPTER_NUMBER, chapter);
        long result = db.insert(ProjectContract.ChapterEntry.TABLE_CHAPTER, null, cv);
        //db.close();
    }

    public void addUnit(Project project, int chapter, int startVerse) throws IllegalArgumentException {
        addUnit(project.getTargetLanguageSlug(), project.getBookSlug(), project.getVersionSlug(), chapter, startVerse);
    }

    public void addUnit(String languageSlug, String bookSlug, String versionSlug, int chapter, int startVerse) throws IllegalArgumentException {
        int projectId = getProjectId(languageSlug, bookSlug, versionSlug);
        int chapterId = getChapterId(languageSlug, bookSlug, versionSlug, chapter);

        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(ProjectContract.UnitEntry.UNIT_PROJECT_FK, projectId);
        cv.put(ProjectContract.UnitEntry.UNIT_CHAPTER_FK, chapterId);
        cv.put(ProjectContract.UnitEntry.UNIT_START_VERSE, startVerse);
        long result = db.insert(ProjectContract.UnitEntry.TABLE_UNIT, null, cv);
        //db.close();
    }

    public void addTake(FileNameExtractor fne, String takeFilename, String recordingMode, long timestamp, int rating) {
        String bookSlug = fne.getBook();
        String languageSlug = fne.getLang();
        String versionSlug = fne.getVersion();
        int chapter = fne.getChapter();
        int start = fne.getStartVerse();
        if (!projectExists(languageSlug, bookSlug, versionSlug)) {
            addProject(languageSlug, bookSlug, versionSlug, recordingMode);
            addChapter(languageSlug, bookSlug, versionSlug, chapter);
            addUnit(languageSlug, bookSlug, versionSlug, chapter, start);
            //If the chapter doesn't exist, then the unit can't either
        } else if (!chapterExists(languageSlug, bookSlug, versionSlug, chapter)) {
            addChapter(languageSlug, bookSlug, versionSlug, chapter);
            addUnit(languageSlug, bookSlug, versionSlug, chapter, start);
            //chapter could exist, but unit may not yet
        } else if (!unitExists(languageSlug, bookSlug, versionSlug, chapter, start)) {
            addUnit(languageSlug, bookSlug, versionSlug, chapter, start);
        }
        int unitId = getUnitId(languageSlug, bookSlug, versionSlug, chapter, start);

        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(ProjectContract.TakeEntry.TAKE_UNIT_FK, unitId);
        cv.put(ProjectContract.TakeEntry.TAKE_RATING, rating);
        cv.put(ProjectContract.TakeEntry.TAKE_NOTES, "");
        cv.put(ProjectContract.TakeEntry.TAKE_NUMBER, fne.getTake());
        cv.put(ProjectContract.TakeEntry.TAKE_FILENAME, takeFilename);
        cv.put(ProjectContract.TakeEntry.TAKE_TIMESTAMP, timestamp);
        long result = db.insertWithOnConflict(ProjectContract.TakeEntry.TABLE_TAKE, null, cv, SQLiteDatabase.CONFLICT_IGNORE);
        if (result > 0) {
            autoSelectTake(unitId);
        }
    }

    public List<Project> getAllProjects() {
        List<Project> projectList = new ArrayList<>();
        String query = "SELECT * FROM " + ProjectContract.ProjectEntry.TABLE_PROJECT;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                Project project = new Project();
                String versionSlug = getVersionSlug(cursor.getInt(cursor.getColumnIndex(ProjectContract.ProjectEntry.PROJECT_VERSION_FK)));
                project.setVersion(versionSlug);
                String targetLanguageCode = getLanguageCode(cursor.getInt(cursor.getColumnIndex(ProjectContract.ProjectEntry.PROJECT_TARGET_LANGUAGE_FK)));
                project.setTargetLanguage(targetLanguageCode);
                int sourceLanguageIndex = cursor.getColumnIndex(ProjectContract.ProjectEntry.PROJECT_SOURCE_LANGUAGE_FK);
                //Source language could be null
                if (cursor.getType(sourceLanguageIndex) == Cursor.FIELD_TYPE_INTEGER) {
                    String sourceLanguageCode = getLanguageCode(cursor.getInt(cursor.getColumnIndex(ProjectContract.ProjectEntry.PROJECT_SOURCE_LANGUAGE_FK)));
                    project.setSourceLanguage(sourceLanguageCode);
                    project.setSourceAudioPath(cursor.getString(cursor.getColumnIndex(ProjectContract.ProjectEntry.PROJECT_SOURCE_AUDIO_PATH)));
                }
                project.setMode(cursor.getString(cursor.getColumnIndex(ProjectContract.ProjectEntry.PROJECT_MODE)));
                String bookSlug = getBookSlug(cursor.getInt(cursor.getColumnIndex(ProjectContract.ProjectEntry.PROJECT_BOOK_FK)));
                project.setBookSlug(bookSlug);
                String anthology = getAnthologySlug(bookSlug);
                project.setAnthology(anthology);
                int number = getBookNumber(bookSlug);
                project.setBookNumber(number);
                project.setContributors(cursor.getString(cursor.getColumnIndex(ProjectContract.ProjectEntry.PROJECT_CONTRIBUTORS)));

                projectList.add(project);
            } while (cursor.moveToNext());
        }
        cursor.close();
        //db.close();
        return projectList;
    }


    public Project getProject(int projectId) {
        String query = "SELECT * FROM " + ProjectContract.ProjectEntry.TABLE_PROJECT + " WHERE " + ProjectContract.ProjectEntry._ID + " =" + String.valueOf(projectId);
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        Project project = null;
        if (cursor.moveToFirst()) {
            project = new Project();
            int versionId = cursor.getInt(cursor.getColumnIndex(ProjectContract.ProjectEntry.PROJECT_VERSION_FK));
            String versionSlug = getVersionSlug(versionId);
            String versionName = getVersionName(versionId);
            project.setVersion(new Version(versionSlug, versionName));
            int languageId = cursor.getInt(cursor.getColumnIndex(ProjectContract.ProjectEntry.PROJECT_TARGET_LANGUAGE_FK));
            String targetLanguageSlug = getLanguageCode(languageId);
            String targetLanguageName = getLanguageName(languageId);
            project.setTargetLanguage(new Language(targetLanguageSlug, targetLanguageName));
            int sourceLanguageIndex = cursor.getColumnIndex(ProjectContract.ProjectEntry.PROJECT_SOURCE_LANGUAGE_FK);
            //Source language could be null
            if (cursor.getType(sourceLanguageIndex) == Cursor.FIELD_TYPE_INTEGER) {
                int sourceLanguageId = cursor.getInt(cursor.getColumnIndex(ProjectContract.ProjectEntry.PROJECT_SOURCE_LANGUAGE_FK));
                String sourceLanguageSlug = getLanguageCode(sourceLanguageId);
                String sourceLanguageName = getLanguageName(sourceLanguageId);
                project.setSourceLanguage(new Language(sourceLanguageSlug, sourceLanguageName));
                project.setSourceAudioPath(cursor.getString(cursor.getColumnIndex(ProjectContract.ProjectEntry.PROJECT_SOURCE_AUDIO_PATH)));
            }
            project.setMode(cursor.getString(cursor.getColumnIndex(ProjectContract.ProjectEntry.PROJECT_MODE)));

            int bookId = cursor.getInt(cursor.getColumnIndex(ProjectContract.ProjectEntry.PROJECT_BOOK_FK));
            String bookSlug = getBookSlug(bookId);
            String bookName = getBookName(bookSlug);
            int bookNumber = getBookNumber(bookSlug);
            String anthology = getAnthologySlug(bookSlug);
            String anthology = getAnthologyName(bookSlug);
            String anthology = resource(bookSlug);
            project.setAnthology(new Anthology(anthologySlug, anthologyName, resource));

            project.setBook(new Book(bookSlug, bookName, anthologySlug, bookNumber));
            project.setContributors(cursor.getString(cursor.getColumnIndex(ProjectContract.ProjectEntry.PROJECT_CONTRIBUTORS)));

        }
        cursor.close();
        return project;
    }


    public int getNumProjects() {
        SQLiteDatabase db = getReadableDatabase();
        String countQuery = "SELECT * FROM " + ProjectContract.ProjectEntry.TABLE_PROJECT;
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        //db.close();
        return count;
    }

    public int getChapterCheckingLevel(Project project, int chapter) {
        String chapterId = String.valueOf(getChapterId(project, chapter));
        SQLiteDatabase db = getReadableDatabase();
        final String getChapter = String.format("SELECT %s FROM %s WHERE %s=?",
                ProjectContract.ChapterEntry.CHAPTER_CHECKING_LEVEL, ProjectContract.ChapterEntry.TABLE_CHAPTER, ProjectContract.ChapterEntry._ID);
        int checkingLevel = (int) DatabaseUtils.longForQuery(db, getChapter, new String[]{chapterId});
        //db.close();
        return checkingLevel;
    }

    public int getTakeRating(FileNameExtractor fne) {
        String unitId = String.valueOf(getUnitId(fne.getLang(), fne.getBook(), fne.getVersion(), fne.getChapter(), fne.getStartVerse()));
        SQLiteDatabase db = getReadableDatabase();
        final String getTake = String.format("SELECT %s FROM %s WHERE %s=? AND %s=?",
                ProjectContract.TakeEntry.TAKE_RATING, ProjectContract.TakeEntry.TABLE_TAKE, ProjectContract.TakeEntry.TAKE_UNIT_FK, ProjectContract.TakeEntry.TAKE_NUMBER);
        int rating = (int) DatabaseUtils.longForQuery(db, getTake, new String[]{unitId, String.valueOf(fne.getTake())});
        //db.close();
        return rating;
    }

    private int getSelectedTakeId(int unitId) {
        SQLiteDatabase db = getReadableDatabase();
        final String getTake = String.format("SELECT %s FROM %s WHERE %s=?",
                ProjectContract.UnitEntry.UNIT_CHOSEN_TAKE_FK, ProjectContract.UnitEntry.TABLE_UNIT, ProjectContract.UnitEntry._ID);
        //int take = (int)DatabaseUtils.longForQuery(db, getTake, new String[]{unitId});
        Cursor cursor = db.rawQuery(getTake, new String[]{String.valueOf(unitId)});
        int takeIdCol = cursor.getColumnIndex(ProjectContract.UnitEntry.UNIT_CHOSEN_TAKE_FK);
        if (cursor.moveToFirst()) {
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

    public int getSelectedTakeId(String languageSlug, String bookSlug, String versionSlug, int chapter, int startVerse) {
        int unitId = getUnitId(languageSlug, bookSlug, versionSlug, chapter, startVerse);
        return getSelectedTakeId(unitId);
    }

    public int getSelectedTakeNumber(String languageSlug, String bookSlug, String versionSlug, int chapter, int startVerse) {
        SQLiteDatabase db = getReadableDatabase();
        int takeId = getSelectedTakeId(languageSlug, bookSlug, versionSlug, chapter, startVerse);
        if (takeId != -1) {
            final String getTakeNumber = String.format("SELECT %s FROM %s WHERE %s=?", ProjectContract.TakeEntry.TAKE_NUMBER, ProjectContract.TakeEntry.TABLE_TAKE, ProjectContract.TakeEntry._ID);
            Cursor cursor = db.rawQuery(getTakeNumber, new String[]{String.valueOf(takeId)});
            if (cursor.moveToFirst()) {
                int takeNumCol = cursor.getColumnIndex(ProjectContract.TakeEntry.TAKE_NUMBER);
                int takeNum = cursor.getInt(takeNumCol);
                cursor.close();
                //db.close();
                return takeNum;
            }
        }
        return -1;
    }

    public int getSelectedTakeNumber(FileNameExtractor fne) {
        return getSelectedTakeNumber(fne.getLang(), fne.getBook(), fne.getVersion(), fne.getChapter(), fne.getStartVerse());
    }

    public void setSelectedTake(File take) {
        FileNameExtractor fne = new FileNameExtractor(take);
        setSelectedTake(fne);
    }

    public void setSelectedTake(FileNameExtractor fne) {
        int unitId = getUnitId(fne.getLang(), fne.getBook(), fne.getVersion(), fne.getChapter(), fne.getStartVerse());
        int takeId = getTakeId(fne);
        setSelectedTake(unitId, takeId);
    }

    public void setSelectedTake(int unitId, int takeId) {
        String unitIdString = String.valueOf(unitId);
        String takeIdString = String.valueOf(takeId);
        SQLiteDatabase db = getReadableDatabase();
        final String replaceTakeWhere = String.format("%s=?", ProjectContract.UnitEntry._ID);
        ContentValues replaceWith = new ContentValues();
        replaceWith.put(ProjectContract.UnitEntry.UNIT_CHOSEN_TAKE_FK, takeIdString);
        db.update(ProjectContract.UnitEntry.TABLE_UNIT, replaceWith, replaceTakeWhere, new String[]{unitIdString});
        //db.close();
    }

    public void setTakeRating(FileNameExtractor projectSlugs, int rating) {
        int unitId = getUnitId(projectSlugs.getLang(), projectSlugs.getBook(), projectSlugs.getVersion(), projectSlugs.getChapter(), projectSlugs.getStartVerse());
        SQLiteDatabase db = getReadableDatabase();
        final String replaceTakeWhere = String.format("%s=? AND %s=?",
                ProjectContract.TakeEntry.TAKE_UNIT_FK, ProjectContract.TakeEntry.TAKE_NUMBER);
        ContentValues replaceWith = new ContentValues();
        replaceWith.put(ProjectContract.TakeEntry.TAKE_RATING, rating);
        int result = db.update(ProjectContract.TakeEntry.TABLE_TAKE, replaceWith, replaceTakeWhere, new String[]{String.valueOf(unitId), String.valueOf(projectSlugs.getTake())});
        if (result > 0) {
            autoSelectTake(unitId);
        }
        //db.close();
    }

    public void setCheckingLevel(Project project, int chapter, int checkingLevel) {
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

    public void removeSelectedTake(FileNameExtractor fne) {
        String unitId = String.valueOf(getUnitId(fne.getLang(), fne.getBook(), fne.getVersion(), fne.getChapter(), fne.getStartVerse()));
        SQLiteDatabase db = getReadableDatabase();
        final String replaceTakeWhere = String.format("%s=?",
                ProjectContract.UnitEntry._ID);
        ContentValues replaceWith = new ContentValues();
        replaceWith.putNull(ProjectContract.UnitEntry.UNIT_CHOSEN_TAKE_FK);
        db.update(ProjectContract.UnitEntry.TABLE_UNIT, replaceWith, replaceTakeWhere, new String[]{unitId});
        //db.close();
    }

    public void deleteProject(Project p) {
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

    public void deleteTake(FileNameExtractor fne) {
        int unitId = getUnitId(fne.getLang(), fne.getBook(), fne.getVersion(), fne.getChapter(), fne.getStartVerse());
        int takeId = getTakeId(fne);
        SQLiteDatabase db = getWritableDatabase();
        final String deleteWhere = String.format("%s=? AND %s=?",
                ProjectContract.TakeEntry.TAKE_UNIT_FK, ProjectContract.TakeEntry.TAKE_NUMBER);
//        final String deleteTake = String.format("DELETE FROM %s WHERE %s=? AND %s=?",
//                TakeEntry.TABLE_TAKE, TakeEntry.TAKE_UNIT_FK, TakeEntry.TAKE_NUMBER);
        //db.execSQL(deleteTake, new String[]{String.valueOf(unitId), String.valueOf(fne.getTake())});
        int takeSelected = getSelectedTakeId(unitId);
        int result = db.delete(ProjectContract.TakeEntry.TABLE_TAKE, deleteWhere, new String[]{String.valueOf(unitId), String.valueOf(fne.getTake())});
        if (result > 0 && takeSelected == takeId) {
            autoSelectTake(unitId);
        }
    }

    /**
     * Computes the number of
     *
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

        if (c.getCount() > 0) {
            c.moveToFirst();
            do {
                int chapterNum = c.getInt(0);
                int unitCount = c.getInt(1);
                numStartedUnits[chapterNum - 1] = unitCount;
            } while (c.moveToNext());
            return numStartedUnits;
        }
        return numStartedUnits;
    }

    public List<String> getTakesForChapterCompilation(Project project, int chapter) {
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
        if (c.getCount() > 0) {
            takesToCompile = new ArrayList<>();
            c.moveToFirst();
            do {
                takesToCompile.add(c.getString(0));
            } while (c.moveToNext());
        }
        return takesToCompile;
    }

    public void resyncProjectWithFilesystem(Project project, List<File> takes, OnLanguageNotFound callback, OnCorruptFile onCorruptFile) {
        importTakesToDatabase(takes, callback, onCorruptFile);
        if (projectExists(project)) {
            int projectId = getProjectId(project);
            String where = String.format("%s.%s=?",
                    ProjectContract.UnitEntry.TABLE_UNIT, ProjectContract.UnitEntry.UNIT_PROJECT_FK);
            String[] whereArgs = new String[]{String.valueOf(projectId)};
            removeTakesWithNoFiles(takes, where, whereArgs);
        }
    }

    public void resyncChapterWithFilesystem(Project project, int chapter, List<File> takes, OnLanguageNotFound callback, OnCorruptFile onCorruptFile) {
        importTakesToDatabase(takes, callback, onCorruptFile);
        if (projectExists(project) && chapterExists(project, chapter)) {
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

    private void importTakesToDatabase(List<File> takes, OnLanguageNotFound callback, OnCorruptFile corruptFileCallback) {
        SQLiteDatabase db = getWritableDatabase();
        //create a temporary table to store take names from the filesystem
        db.execSQL(ProjectContract.DELETE_TEMP);
        db.execSQL(ProjectContract.TempEntry.CREATE_TEMP_TABLE);
        db.beginTransaction();
        //add all the take names to the temp table
        for (File f : takes) {
            ContentValues cv = new ContentValues();
            FileNameExtractor fne = new FileNameExtractor(f);
            if (fne.matched()) {
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
        if (c.getCount() > 0) {
            int nameIndex = c.getColumnIndex(ProjectContract.TempEntry.TEMP_TAKE_NAME);
            int timestampIndex = c.getColumnIndex(ProjectContract.TempEntry.TEMP_TIMESTAMP);
            c.moveToFirst();
            do {
                FileNameExtractor fne = new FileNameExtractor(c.getString(nameIndex));
                if (!languageExists(fne.getLang())) {
                    if (callback != null) {
                        String name = callback.requestLanguageName(fne.getLang());
                        addLanguage(fne.getLang(), name);
                    } else {
                        addLanguage(fne.getLang(), "???"); //missingno
                    }
                }
                //Need to get the mode out of the metadata because chunks of only one verse are indistinguishable from verse mode
                File dir = fne.getParentDirectory();
                File file = new File(dir, c.getString(nameIndex));
                try {
                    WavFile wav = new WavFile(file);
                    addTake(fne, c.getString(nameIndex), wav.getMetadata().getMode(), c.getLong(timestampIndex), 0);
                } catch (IllegalArgumentException e) {
                    //TODO: corrupt file, prompt to fix maybe? or delete? At least tell which file is causing a problem
                    Logger.e(this.toString(), "Error loading wav file named: " + dir + "/" + c.getString(nameIndex), e);
                    corruptFileCallback.onCorruptFile(file);
                }
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
     * <p>
     * This is used to resync part of the database in the event that a user manually removed a file from an external file manager application
     *
     * @param takes       the list of files to NOT be removed from the database
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
        if (c.getCount() > 0) {
            int idIndex = c.getColumnIndex("takeid");
            final String deleteTake = String.format("%s=?", ProjectContract.TakeEntry._ID);
            final String removeSelectedTake = String.format("%s=?", ProjectContract.UnitEntry.UNIT_CHOSEN_TAKE_FK);
            c.moveToFirst();
            do {
                ContentValues cv = new ContentValues();
                cv.putNull(ProjectContract.UnitEntry.UNIT_CHOSEN_TAKE_FK);
                db.update(ProjectContract.UnitEntry.TABLE_UNIT, cv, removeSelectedTake, new String[]{String.valueOf(c.getInt(idIndex))});
                db.delete(ProjectContract.TakeEntry.TABLE_TAKE, deleteTake, new String[]{String.valueOf(c.getInt(idIndex))});
            } while (c.moveToNext());
        }
        c.close();
        db.execSQL(ProjectContract.DELETE_TEMP);
        db.setTransactionSuccessful();
        db.endTransaction();
    }


    public void resyncDbWithFs(List<File> takes, OnLanguageNotFound callback, OnCorruptFile corruptFileCallback) {
        SQLiteDatabase db = getWritableDatabase();
        //create a temporary table to store take names from the filesystem
        db.execSQL(ProjectContract.DELETE_TEMP);
        db.execSQL(ProjectContract.TempEntry.CREATE_TEMP_TABLE);
        db.beginTransaction();
        //add all the take names to the temp table
        for (File f : takes) {
            ContentValues cv = new ContentValues();
            FileNameExtractor fne = new FileNameExtractor(f);
            if (fne.matched()) {
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
        if (c.getCount() > 0) {
            int nameIndex = c.getColumnIndex(ProjectContract.TempEntry.TEMP_TAKE_NAME);
            int timestampIndex = c.getColumnIndex(ProjectContract.TempEntry.TEMP_TIMESTAMP);
            c.moveToFirst();
            do {
                FileNameExtractor fne = new FileNameExtractor(c.getString(nameIndex));
                if (!languageExists(fne.getLang())) {
                    if (callback != null) {
                        String name = callback.requestLanguageName(fne.getLang());
                        addLanguage(fne.getLang(), name);
                    } else {
                        addLanguage(fne.getLang(), "???"); //missingno
                    }
                }
                //Need to get the mode out of the metadata because chunks of only one verse are indistinguishable from verse mode
                File dir = fne.getParentDirectory();
                File file = new File(dir, c.getString(nameIndex));
                try {
                    WavFile wav = new WavFile(file);
                    addTake(fne, c.getString(nameIndex), wav.getMetadata().getMode(), c.getLong(timestampIndex), 0);
                } catch (IllegalArgumentException e) {
                    //TODO: corrupt file, prompt to fix maybe? or delete? At least tell which file is causing a problem
                    Logger.e(this.toString(), "Error loading wav file named: " + dir + "/" + c.getString(nameIndex), e);
                    corruptFileCallback.onCorruptFile(file);
                }
            } while (c.moveToNext());
        }
        c.close();
        //find all the takes in the db that do not have a match in the filesystem
        final String deleteDanglingReferences = String.format("SELECT t1.%s, t1.%s FROM %s AS t1 LEFT JOIN %s AS t2 ON t1.%s=t2.%s WHERE t2.%s IS NULL",
                ProjectContract.TakeEntry.TAKE_FILENAME, ProjectContract.TakeEntry._ID, ProjectContract.TakeEntry.TABLE_TAKE, ProjectContract.TempEntry.TABLE_TEMP, ProjectContract.TempEntry.TEMP_TAKE_NAME, ProjectContract.TakeEntry.TAKE_FILENAME, ProjectContract.TakeEntry.TAKE_FILENAME);
        c = db.rawQuery(deleteDanglingReferences, null);
        //for each of these takes that do not have a corresponding match, remove them from the database
        if (c.getCount() > 0) {
            int idIndex = c.getColumnIndex(ProjectContract.TakeEntry._ID);
            final String deleteTake = String.format("%s=?", ProjectContract.TakeEntry._ID);
            final String removeSelectedTake = String.format("%s=?", ProjectContract.UnitEntry.UNIT_CHOSEN_TAKE_FK);
            c.moveToFirst();
            do {
                ContentValues cv = new ContentValues();
                cv.putNull(ProjectContract.UnitEntry.UNIT_CHOSEN_TAKE_FK);
                db.update(ProjectContract.UnitEntry.TABLE_UNIT, cv, removeSelectedTake, new String[]{String.valueOf(c.getInt(idIndex))});
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
        for (File f : takes) {
            ContentValues cv = new ContentValues();
            FileNameExtractor fne = new FileNameExtractor(f);
            if (fne.matched()) {
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
        if (c.getCount() > 0) {
            int nameIndex = c.getColumnIndex(ProjectContract.TempEntry.TEMP_TAKE_NAME);
            int timestampIndex = c.getColumnIndex(ProjectContract.TempEntry.TEMP_TIMESTAMP);
            c.moveToFirst();
            do {
                FileNameExtractor fne = new FileNameExtractor(c.getString(nameIndex));
                if (!languageExists(fne.getLang())) {
                    if (callback != null) {
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
        if (c.getCount() > 0) {
            int idIndex = c.getColumnIndex(ProjectContract.TakeEntry._ID);
            final String deleteTake = String.format("%s=?", ProjectContract.TakeEntry._ID);
            final String removeSelectedTake = String.format("%s=?", ProjectContract.UnitEntry.UNIT_CHOSEN_TAKE_FK);
            c.moveToFirst();
            do {
                ContentValues cv = new ContentValues();
                cv.putNull(ProjectContract.UnitEntry.UNIT_CHOSEN_TAKE_FK);
                db.update(ProjectContract.UnitEntry.TABLE_UNIT, cv, removeSelectedTake, new String[]{String.valueOf(c.getInt(idIndex))});
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
            if (!languageExists(p.getTargetLanguageSlug())) {
                String name = projectLevelResync.requestLanguageName(p.getTargetLanguageSlug());
                addLanguage(p.getTargetLanguageSlug(), name);
            }
            if (!projectExists(p)) {
                newProjects.add(p);
            }
            addProject(p);
        }
        return newProjects;
    }

    public void autoSelectTake(int unitId) {
        SQLiteDatabase db = getReadableDatabase();
        final String autoSelect = String.format("SELECT %s FROM %s WHERE %s=? ORDER BY %s DESC, %s DESC LIMIT 1",
                ProjectContract.TakeEntry._ID, ProjectContract.TakeEntry.TABLE_TAKE, ProjectContract.TakeEntry.TAKE_UNIT_FK, ProjectContract.TakeEntry.TAKE_RATING, ProjectContract.TakeEntry.TAKE_TIMESTAMP);
        Cursor c = db.rawQuery(autoSelect, new String[]{String.valueOf(unitId)});
        if (c.getCount() > 0) {
            c.moveToFirst();
            int takeId = c.getInt(0);
            setSelectedTake(unitId, takeId);
        }
    }

    public Language[] getLanguages() {
        List<Language> languageList = new ArrayList<>();
        String query = "SELECT * FROM " + ProjectContract.LanguageEntry.TABLE_LANGUAGE;
        SQLiteDatabase db = getReadableDatabase();
        db.beginTransaction();
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                String languageCode = cursor.getString(cursor.getColumnIndex(ProjectContract.LanguageEntry.LANGUAGE_CODE));
                String languageName = cursor.getString(cursor.getColumnIndex(ProjectContract.LanguageEntry.LANGUAGE_NAME));
                languageList.add(new Language(languageCode, languageName));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.endTransaction();
        //db.close();
        return languageList.toArray(new Language[languageList.size()]);
    }

    public Anthology[] getAnthologies() {
        List<Anthology> anthologyList = new ArrayList<>();
        String query = "SELECT * FROM " + ProjectContract.AnthologyEntry.TABLE_ANTHOLOGY;
        SQLiteDatabase db = getReadableDatabase();
        db.beginTransaction();
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                String anthologySlug = cursor.getString(cursor.getColumnIndex(ProjectContract.AnthologyEntry.ANTHOLOGY_SLUG));
                String anthologyName = cursor.getString(cursor.getColumnIndex(ProjectContract.AnthologyEntry.ANTHOLOGY_NAME));
                String resource = cursor.getString(cursor.getColumnIndex(ProjectContract.AnthologyEntry.ANTHOLOGY_RESOURCE));
                anthologyList.add(new Anthology(anthologySlug, anthologyName, resource));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.endTransaction();
        //db.close();
        return anthologyList.toArray(new Anthology[anthologyList.size()]);
    }

    public Book[] getBooks(String anthologySlug) {
        int anthId = getAnthologyId(anthologySlug);
        List<Book> bookList = new ArrayList<>();
        String query = "SELECT * FROM " + ProjectContract.BookEntry.TABLE_BOOK + " WHERE " +
                ProjectContract.BookEntry.BOOK_ANTHOLOGY_FK + "=" + String.valueOf(anthId) +
                " ORDER BY " + ProjectContract.BookEntry.BOOK_NUMBER + " ASC";
        SQLiteDatabase db = getReadableDatabase();
        db.beginTransaction();
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                String bookSlug = cursor.getString(cursor.getColumnIndex(ProjectContract.BookEntry.BOOK_SLUG));
                String bookName = cursor.getString(cursor.getColumnIndex(ProjectContract.BookEntry.BOOK_NAME));
                int anthologyId = cursor.getInt(cursor.getColumnIndex(ProjectContract.BookEntry.BOOK_ANTHOLOGY_FK));
                int order = cursor.getInt(cursor.getColumnIndex(ProjectContract.BookEntry.BOOK_NUMBER));
                bookList.add(new Book(bookSlug, bookName, getAnthologySlug(anthologyId), order));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.endTransaction();
        //db.close();
        return bookList.toArray(new Book[bookList.size()]);
    }

    public Version[] getVersions(String anthologySlug) {
        int anthId = getAnthologyId(anthologySlug);
        List<Version> versionList = new ArrayList<>();
        String query = "SELECT * FROM " + ProjectContract.VersionRelationshipEntry.TABLE_VERSION_RELATIONSHIP +
                " WHERE " + ProjectContract.VersionRelationshipEntry.ANTHOLOGY_FK + "=" + String.valueOf(anthId);
        SQLiteDatabase db = getReadableDatabase();
        db.beginTransaction();
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                int versionId = cursor.getInt(cursor.getColumnIndex(ProjectContract.VersionRelationshipEntry.VERSION_FK));
                String versionSlug = getVersionSlug(versionId);
                String versionName = getVersionName(versionId);
                versionList.add(new Version(versionSlug, versionName));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.endTransaction();
        //db.close();
        return versionList.toArray(new Version[versionList.size()]);
    }
}
