package org.wycliffeassociates.translationrecorder.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDoneException;
import android.database.sqlite.SQLiteOpenHelper;

import com.door43.tools.reporting.Logger;

import org.wycliffeassociates.translationrecorder.ProjectManager.tasks.resync.ProjectListResyncTask;
import org.wycliffeassociates.translationrecorder.project.Project;
import org.wycliffeassociates.translationrecorder.project.ProjectFileUtils;
import org.wycliffeassociates.translationrecorder.project.ProjectPatternMatcher;
import org.wycliffeassociates.translationrecorder.project.ProjectSlugs;
import org.wycliffeassociates.translationrecorder.project.TakeInfo;
import org.wycliffeassociates.translationrecorder.project.components.Anthology;
import org.wycliffeassociates.translationrecorder.project.components.Book;
import org.wycliffeassociates.translationrecorder.project.components.Language;
import org.wycliffeassociates.translationrecorder.project.components.Mode;
import org.wycliffeassociates.translationrecorder.project.components.User;
import org.wycliffeassociates.translationrecorder.project.components.Version;
import org.wycliffeassociates.translationrecorder.wav.WavFile;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    public List<Project> projectsNeedingResync(Set<Project> allProjects) {
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

    private static final int DATABASE_VERSION = 3;
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
        db.execSQL(ProjectContract.ModeEntry.CREATE_MODE_TABLE);
        db.execSQL(ProjectContract.VersionEntry.CREATE_VERSION_TABLE);
        db.execSQL(ProjectContract.VersionRelationshipEntry.CREATE_VERSION_RELATIONSHIP_TABLE);
        db.execSQL(ProjectContract.UserEntry.CREATE_USER_TABLE);
        //db.execSQL(ProjectContract.ModeRelationshipEntry.CREATE_MODE_RELATIONSHIP_TABLE);
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
        db.execSQL(ProjectContract.DELETE_MODES);
        db.execSQL(ProjectContract.DELETE_VERSION_RELATIONSHIPS);
        db.execSQL(ProjectContract.DELETE_USERS);
        //db.execSQL(ProjectContract.DELETE_MODE_RELATIONSHIPS);
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
        db.execSQL(ProjectContract.DELETE_MODES);
        db.execSQL(ProjectContract.DELETE_VERSION_RELATIONSHIPS);
        db.execSQL(ProjectContract.DELETE_USERS);
        //db.execSQL(ProjectContract.DELETE_MODE_RELATIONSHIPS);
        onCreate(db);
    }

    public boolean languageExists(String languageSlug) {
        SQLiteDatabase db = getReadableDatabase();
        final String languageCountQuery = "SELECT COUNT(*) FROM " + ProjectContract.LanguageEntry.TABLE_LANGUAGE + " WHERE " + ProjectContract.LanguageEntry.LANGUAGE_CODE + "=?";
        boolean exists = (DatabaseUtils.longForQuery(db, languageCountQuery, new String[]{languageSlug})) > 0;
        return exists;
    }

    public boolean bookExists(String bookSlug) {
        SQLiteDatabase db = getReadableDatabase();
        final String bookCountQuery = "SELECT COUNT(*) FROM " + ProjectContract.BookEntry.TABLE_BOOK + " WHERE " + ProjectContract.BookEntry.BOOK_SLUG + "=?";
        boolean exists = (DatabaseUtils.longForQuery(db, bookCountQuery, new String[]{bookSlug})) > 0;
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
        return exists;
    }

    public boolean takeExists(Project project, int chapter, int startVerse, int take) {
        String unitId = String.valueOf(getUnitId(project, chapter, startVerse));
        SQLiteDatabase db = getReadableDatabase();
        final String takeCountQuery = String.format("SELECT COUNT(*) FROM %s WHERE %s=? AND %s=?",
                ProjectContract.TakeEntry.TABLE_TAKE, ProjectContract.TakeEntry.TAKE_UNIT_FK, ProjectContract.TakeEntry.TAKE_NUMBER);
        boolean exists = (DatabaseUtils.longForQuery(db, takeCountQuery, new String[]{unitId, String.valueOf(take)})) > 0;
        return exists;
    }

    public boolean takeExists(TakeInfo takeInfo) {
        ProjectSlugs slugs = takeInfo.getProjectSlugs();
        String unitId = String.valueOf(getUnitId(slugs.getLanguage(), slugs.getBook(), slugs.getVersion(), takeInfo.getChapter(), takeInfo.getStartVerse()));
        SQLiteDatabase db = getReadableDatabase();
        final String takeCountQuery = String.format("SELECT COUNT(*) FROM %s WHERE %s=? AND %s=?",
                ProjectContract.TakeEntry.TABLE_TAKE,
                ProjectContract.TakeEntry.TAKE_UNIT_FK,
                ProjectContract.TakeEntry.TAKE_NUMBER
        );
        boolean exists = (DatabaseUtils.longForQuery(db, takeCountQuery, new String[]{unitId, String.valueOf(takeInfo.getTake())})) > 0;
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
                ProjectContract.ProjectEntry._ID,
                ProjectContract.ProjectEntry.TABLE_PROJECT,
                ProjectContract.ProjectEntry.PROJECT_TARGET_LANGUAGE_FK,
                ProjectContract.ProjectEntry.PROJECT_BOOK_FK,
                ProjectContract.ProjectEntry.PROJECT_VERSION_FK
        );
        int id = -1;
        try {
            id = (int) DatabaseUtils.longForQuery(db, projectIdQuery, new String[]{languageId, bookId, versionId});
        } catch (SQLiteDoneException e) {
            throw new IllegalArgumentException("Project not found in database");
        }
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
                ProjectContract.ChapterEntry._ID,
                ProjectContract.ChapterEntry.TABLE_CHAPTER,
                ProjectContract.ChapterEntry.CHAPTER_PROJECT_FK,
                ProjectContract.ChapterEntry.CHAPTER_NUMBER
        );
        int id = -1;
        try {
            id = (int) DatabaseUtils.longForQuery(db, chapterIdQuery, new String[]{projectId, String.valueOf(chapter)});
        } catch (SQLiteDoneException e) {
            throw new IllegalArgumentException("Chapter not found in database");
        }
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
                ProjectContract.UnitEntry._ID,
                ProjectContract.UnitEntry.TABLE_UNIT,
                ProjectContract.UnitEntry.UNIT_PROJECT_FK,
                ProjectContract.UnitEntry.UNIT_CHAPTER_FK,
                ProjectContract.UnitEntry.UNIT_START_VERSE
        );
        int id = -1;
        try {
            id = (int) DatabaseUtils.longForQuery(db, unitIdQuery, new String[]{projectId, chapterId, String.valueOf(startVerse)});
        } catch (SQLiteDoneException e) {
            throw new IllegalArgumentException("Unit not found in database");
        }
        return id;
    }

    public int getTakeId(TakeInfo takeInfo) throws IllegalArgumentException {
        ProjectSlugs slugs = takeInfo.getProjectSlugs();
        Logger.w(this.toString(), "Attempting to get take id for " + slugs.getLanguage() + " " + slugs.getBook() + " " + slugs.getVersion() + " verse start " + takeInfo.getStartVerse() + " take " + takeInfo.getTake());
        String unitId = String.valueOf(getUnitId(slugs.getLanguage(), slugs.getBook(), slugs.getVersion(), takeInfo.getChapter(), takeInfo.getStartVerse()));
        SQLiteDatabase db = getReadableDatabase();
        final String takeIdQuery = String.format("SELECT %s FROM %s WHERE %s=? AND %s=?",
                ProjectContract.TakeEntry._ID, ProjectContract.TakeEntry.TABLE_TAKE, ProjectContract.TakeEntry.TAKE_UNIT_FK, ProjectContract.TakeEntry.TAKE_NUMBER);
        int id = -1;
        try {
            id = (int) DatabaseUtils.longForQuery(db, takeIdQuery, new String[]{unitId, String.valueOf(takeInfo.getTake())});
        } catch (SQLiteDoneException e) {
            throw new IllegalArgumentException("Take not found in database.");
        }
        return id;
    }

    public int getModeId(String modeSlug, String anthologySlug) throws IllegalArgumentException {
        SQLiteDatabase db = getReadableDatabase();
        final String takeIdQuery = String.format("SELECT %s FROM %s WHERE %s=? AND %s=?",
                ProjectContract.ModeEntry._ID,
                ProjectContract.ModeEntry.TABLE_MODE,
                ProjectContract.ModeEntry.MODE_SLUG,
                ProjectContract.ModeEntry.MODE_ANTHOLOGY_FK
        );
        int id = -1;
        try {
            id = (int) DatabaseUtils.longForQuery(db, takeIdQuery, new String[]{modeSlug, String.valueOf(getAnthologyId(anthologySlug))});
        } catch (SQLiteDoneException e) {
            throw new IllegalArgumentException("Mode not found in database.");
        }
        return id;
    }

    public int getTakeCount(int unitId) throws IllegalArgumentException {
        int count = -1;
        String stringifiedId = String.valueOf(unitId);
        SQLiteDatabase db = getReadableDatabase();
        final String query = String.format("SELECT COUNT(*) FROM %s WHERE %s=?",
                ProjectContract.TakeEntry.TABLE_TAKE,
                ProjectContract.TakeEntry.TAKE_UNIT_FK
        );
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
            throw new IllegalArgumentException("Language: " + languageSlug + " not found.");
        }
        return name;
    }

    public String getLanguageCode(int id) throws IllegalArgumentException {
        SQLiteDatabase db = getReadableDatabase();
        final String languageSlugQuery = String.format("SELECT %s FROM %s WHERE %s=?",
                ProjectContract.LanguageEntry.LANGUAGE_CODE, ProjectContract.LanguageEntry.TABLE_LANGUAGE, ProjectContract.LanguageEntry._ID);
        String code;
        try {
            code = DatabaseUtils.stringForQuery(db, languageSlugQuery, new String[]{String.valueOf(id)});
        } catch (SQLiteDoneException e) {
            throw new IllegalArgumentException("Language id not found in database.");
        }
        return code;
    }

    public Language getLanguage(int id) throws IllegalArgumentException {
        SQLiteDatabase db = getReadableDatabase();
        String query = String.format("SELECT * FROM %s WHERE %s=%s", ProjectContract.LanguageEntry.TABLE_LANGUAGE, ProjectContract.LanguageEntry._ID, String.valueOf(id));
        Cursor cursor = db.rawQuery(query, null);
        Language language;
        if (cursor.moveToFirst()) {
            String languageSlug = cursor.getString(cursor.getColumnIndex(ProjectContract.LanguageEntry.LANGUAGE_CODE));
            String languageName = cursor.getString(cursor.getColumnIndex(ProjectContract.LanguageEntry.LANGUAGE_NAME));
            language = new Language(languageSlug, languageName);
        } else {
            throw new IllegalArgumentException("Language id not found in database.");
        }
        return language;
    }

    public User getUser(int id) throws IllegalArgumentException {
        SQLiteDatabase db = getReadableDatabase();
        String query = String.format("SELECT * FROM %s WHERE %s=%s", ProjectContract.UserEntry.TABLE_USER, ProjectContract.UserEntry._ID, String.valueOf(id));
        Cursor cursor = db.rawQuery(query, null);
        User user;
        if (cursor.moveToFirst()) {
            int userId = cursor.getInt(cursor.getColumnIndex(ProjectContract.UserEntry._ID));
            File audio = new File(cursor.getString(cursor.getColumnIndex(ProjectContract.UserEntry.USER_AUDIO)));
            String hash = cursor.getString(cursor.getColumnIndex(ProjectContract.UserEntry.USER_HASH));
            user = new User(userId, audio, hash);
        } else {
            throw new IllegalArgumentException("User id not found in database.");
        }
        return user;
    }

    public void addUser(User user) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(ProjectContract.UserEntry.USER_AUDIO, user.getAudio().getAbsolutePath());
        cv.put(ProjectContract.UserEntry.USER_HASH, user.getHash());
        long result = db.insertWithOnConflict(ProjectContract.UserEntry.TABLE_USER, null, cv, SQLiteDatabase.CONFLICT_IGNORE);
        user.setId((int)result);
    }

    public List<User> getAllUsers() {
        List<User> userList = new ArrayList<>();
        String query = "SELECT * FROM " + ProjectContract.UserEntry.TABLE_USER;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndex(ProjectContract.UserEntry._ID));
                File audio = new File(cursor.getString(cursor.getColumnIndex(ProjectContract.UserEntry.USER_AUDIO)));
                String hash = cursor.getString(cursor.getColumnIndex(ProjectContract.UserEntry.USER_HASH));
                User user = new User(id, audio, hash);
                userList.add(user);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return userList;
    }

    public int deleteUser(String hash) {
        SQLiteDatabase db = getWritableDatabase();
        final String deleteWhere = String.format("%s=?", ProjectContract.UserEntry.USER_HASH);
        int result = db.delete(ProjectContract.UserEntry.TABLE_USER, deleteWhere, new String[]{ hash });
        return result;
    }

    public String getBookName(String bookSlug) throws IllegalArgumentException {
        SQLiteDatabase db = getReadableDatabase();
        final String bookNameQuery = String.format("SELECT %s FROM %s WHERE %s=?",
                ProjectContract.BookEntry.BOOK_NAME,
                ProjectContract.BookEntry.TABLE_BOOK,
                ProjectContract.BookEntry.BOOK_SLUG
        );
        String name;
        try {
            name = DatabaseUtils.stringForQuery(db, bookNameQuery, new String[]{bookSlug});
        } catch (SQLiteDoneException e) {
            throw new IllegalArgumentException("Book slug: " + bookSlug + " not found in database.");
        }
        return name;
    }

    public String getBookSlug(int id) throws IllegalArgumentException {
        SQLiteDatabase db = getReadableDatabase();
        final String bookSlugQuery = String.format("SELECT %s FROM %s WHERE %s=?",
                ProjectContract.BookEntry.BOOK_SLUG,
                ProjectContract.BookEntry.TABLE_BOOK,
                ProjectContract.BookEntry._ID
        );
        String slug;
        try {
            slug = DatabaseUtils.stringForQuery(db, bookSlugQuery, new String[]{String.valueOf(id)});
        } catch (SQLiteDoneException e) {
            throw new IllegalArgumentException("Book id not found in database.");
        }
        return slug;
    }

    public Mode getMode(int id) throws IllegalArgumentException {
        SQLiteDatabase db = getReadableDatabase();
        String query = String.format("SELECT * FROM %s WHERE %s=%s", ProjectContract.ModeEntry.TABLE_MODE, ProjectContract.ModeEntry._ID, String.valueOf(id));
        Cursor cursor = db.rawQuery(query, null);
        Mode mode;
        if (cursor.moveToFirst()) {
            String modeSlug = cursor.getString(cursor.getColumnIndex(ProjectContract.ModeEntry.MODE_SLUG));
            String modeName = cursor.getString(cursor.getColumnIndex(ProjectContract.ModeEntry.MODE_NAME));
            String modeType = cursor.getString(cursor.getColumnIndex(ProjectContract.ModeEntry.MODE_TYPE));

            mode = new Mode(modeSlug, modeName, modeType);
        } else {
            throw new IllegalArgumentException("Book id not found in database.");
        }
        return mode;
    }

    public Book getBook(int id) throws IllegalArgumentException {
        SQLiteDatabase db = getReadableDatabase();
        String query = String.format("SELECT * FROM %s WHERE %s=%s", ProjectContract.BookEntry.TABLE_BOOK, ProjectContract.BookEntry._ID, String.valueOf(id));
        Cursor cursor = db.rawQuery(query, null);
        Book book;
        if (cursor.moveToFirst()) {
            String bookSlug = cursor.getString(cursor.getColumnIndex(ProjectContract.BookEntry.BOOK_SLUG));
            String bookName = cursor.getString(cursor.getColumnIndex(ProjectContract.BookEntry.BOOK_NAME));
            int bookNumber = cursor.getInt(cursor.getColumnIndex(ProjectContract.BookEntry.BOOK_NUMBER));
            String anthology = getAnthologySlug(cursor.getInt(cursor.getColumnIndex(ProjectContract.BookEntry.BOOK_ANTHOLOGY_FK)));
            book = new Book(bookSlug, bookName, anthology, bookNumber);
        } else {
            throw new IllegalArgumentException("Book id not found in database.");
        }
        return book;
    }

    public String getVersionName(int id) throws IllegalArgumentException {
        SQLiteDatabase db = getReadableDatabase();
        final String versionSlugQuery = String.format("SELECT %s FROM %s WHERE %s=?",
                ProjectContract.VersionEntry.VERSION_NAME, ProjectContract.VersionEntry.TABLE_VERSION, ProjectContract.VersionEntry._ID);
        String name;
        try {
            name = DatabaseUtils.stringForQuery(db, versionSlugQuery, new String[]{String.valueOf(id)});
        } catch (SQLiteDoneException e) {
            throw new IllegalArgumentException("Version id not found in database.");
        }
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
            throw new IllegalArgumentException("Version id not found in database.");
        }
        return slug;
    }

    public Version getVersion(int id) throws IllegalArgumentException {
        SQLiteDatabase db = getReadableDatabase();
        String query = String.format("SELECT * FROM %s WHERE %s=%s", ProjectContract.VersionEntry.TABLE_VERSION, ProjectContract.VersionEntry._ID, String.valueOf(id));
        Cursor cursor = db.rawQuery(query, null);
        Version version;
        if (cursor.moveToFirst()) {
            String versionSlug = cursor.getString(cursor.getColumnIndex(ProjectContract.VersionEntry.VERSION_SLUG));
            String versionName = cursor.getString(cursor.getColumnIndex(ProjectContract.VersionEntry.VERSION_NAME));
            version = new Version(versionSlug, versionName);
        } else {
            throw new IllegalArgumentException("Version id not found in database.");
        }
        cursor.close();
        return version;
    }

    public String getAnthologySlug(int id) throws IllegalArgumentException {
        SQLiteDatabase db = getReadableDatabase();
        final String anthologySlugQuery = String.format("SELECT %s FROM %s WHERE %s=?",
                ProjectContract.AnthologyEntry.ANTHOLOGY_SLUG, ProjectContract.AnthologyEntry.TABLE_ANTHOLOGY, ProjectContract.AnthologyEntry._ID);
        String slug;
        try {
            slug = DatabaseUtils.stringForQuery(db, anthologySlugQuery, new String[]{String.valueOf(id)});
        } catch (SQLiteDoneException e) {
            throw new IllegalArgumentException("Anthology id not found in database.");
        }
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

    public Anthology getAnthology(int id) throws IllegalArgumentException {
        SQLiteDatabase db = getReadableDatabase();
        String query = String.format("SELECT * FROM %s WHERE %s=%s", ProjectContract.AnthologyEntry.TABLE_ANTHOLOGY, ProjectContract.AnthologyEntry._ID, String.valueOf(id));
        Cursor cursor = db.rawQuery(query, null);
        Anthology anthology;
        if (cursor.moveToFirst()) {
            String anthologySlug = cursor.getString(cursor.getColumnIndex(ProjectContract.AnthologyEntry.ANTHOLOGY_SLUG));
            String anthologyName = cursor.getString(cursor.getColumnIndex(ProjectContract.AnthologyEntry.ANTHOLOGY_NAME));
            String resourceSlug = cursor.getString(cursor.getColumnIndex(ProjectContract.AnthologyEntry.ANTHOLOGY_RESOURCE));
            int sort = cursor.getInt(cursor.getColumnIndex(ProjectContract.AnthologyEntry.ANTHOLOGY_SORT));
            String regex = cursor.getString(cursor.getColumnIndex(ProjectContract.AnthologyEntry.ANTHOLOGY_REGEX));
            String groups = cursor.getString(cursor.getColumnIndex(ProjectContract.AnthologyEntry.ANTHOLOGY_GROUPS));
            String mask = cursor.getString(cursor.getColumnIndex(ProjectContract.AnthologyEntry.ANTHOLOGY_MASK));
            String pluginClassName = cursor.getString(cursor.getColumnIndex(ProjectContract.AnthologyEntry.PLUGIN_CLASS));
            String pluginJarName = cursor.getString(cursor.getColumnIndex(ProjectContract.AnthologyEntry.PLUGIN_JAR));
            anthology = new Anthology(anthologySlug, anthologyName, resourceSlug, sort, regex, groups, mask, pluginJarName, pluginClassName);
        } else {
            throw new IllegalArgumentException("Anthology id " + id + " not found in database.");
        }
        return anthology;
    }

    public List<ProjectPatternMatcher> getProjectPatternMatchers(){
        SQLiteDatabase db = getReadableDatabase();
        String query = String.format("SELECT * FROM %s", ProjectContract.AnthologyEntry.TABLE_ANTHOLOGY);
        Cursor cursor = db.rawQuery(query, null);
        List<ProjectPatternMatcher> patterns = new ArrayList<>();
        while (cursor.moveToNext()) {
            String regex = cursor.getString(cursor.getColumnIndex(ProjectContract.AnthologyEntry.ANTHOLOGY_REGEX));
            String groups = cursor.getString(cursor.getColumnIndex(ProjectContract.AnthologyEntry.ANTHOLOGY_GROUPS));
            patterns.add(new ProjectPatternMatcher(regex, groups));
        }
        cursor.close();
        return patterns;
    }

    public int getBookNumber(String bookSlug) throws IllegalArgumentException {
        SQLiteDatabase db = getReadableDatabase();
        final String bookNameQuery = String.format("SELECT %s FROM %s WHERE %s=?",
                ProjectContract.BookEntry.BOOK_NUMBER, ProjectContract.BookEntry.TABLE_BOOK, ProjectContract.BookEntry.BOOK_SLUG);
        int number = -1;
        try {
            number = (int) DatabaseUtils.longForQuery(db, bookNameQuery, new String[]{bookSlug});
        } catch (SQLiteDoneException e) {
            throw new IllegalArgumentException("Book slug: " + bookSlug + " not found in database.");
        }
        return number;
    }

    public void addLanguage(String languageSlug, String name) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(ProjectContract.LanguageEntry.LANGUAGE_CODE, languageSlug);
        cv.put(ProjectContract.LanguageEntry.LANGUAGE_NAME, name);
        long result = db.insertWithOnConflict(ProjectContract.LanguageEntry.TABLE_LANGUAGE, null, cv, SQLiteDatabase.CONFLICT_IGNORE);
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

    public void addAnthology(String anthologySlug, String name, String resource, int sort,
                             String regex, String groups, String mask, String jarName, String className) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(ProjectContract.AnthologyEntry.ANTHOLOGY_SLUG, anthologySlug);
        cv.put(ProjectContract.AnthologyEntry.ANTHOLOGY_NAME, name);
        cv.put(ProjectContract.AnthologyEntry.ANTHOLOGY_RESOURCE, resource);
        cv.put(ProjectContract.AnthologyEntry.ANTHOLOGY_SORT, sort);
        cv.put(ProjectContract.AnthologyEntry.ANTHOLOGY_REGEX, regex);
        cv.put(ProjectContract.AnthologyEntry.ANTHOLOGY_GROUPS, groups);
        cv.put(ProjectContract.AnthologyEntry.ANTHOLOGY_MASK, mask);
        cv.put(ProjectContract.AnthologyEntry.PLUGIN_JAR, jarName);
        cv.put(ProjectContract.AnthologyEntry.PLUGIN_CLASS, className);
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

    public void addMode(String slug, String name, String type, String anthologySlug) {
        int anthId = getAnthologyId(anthologySlug);
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(ProjectContract.ModeEntry.MODE_SLUG, slug);
        cv.put(ProjectContract.ModeEntry.MODE_NAME, name);
        cv.put(ProjectContract.ModeEntry.MODE_TYPE, type);
        cv.put(ProjectContract.ModeEntry.MODE_ANTHOLOGY_FK, anthId);
        long result = db.insertWithOnConflict(ProjectContract.ModeEntry.TABLE_MODE, null, cv, SQLiteDatabase.CONFLICT_IGNORE);
    }

    public void addModes(Mode[] modes, String anthologySlug) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            for (Mode m : modes) {
                addMode(m.getSlug(), m.getName(), m.getTypeString(), anthologySlug);
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

//    public void addModeRelationships(String anthologySlug, Mode[] modes) {
//        int anthId = getAnthologyId(anthologySlug);
//        SQLiteDatabase db = getWritableDatabase();
//        for(Mode m : modes) {
//            int modeId = getModeId(m.getSlug());
//            ContentValues cv = new ContentValues();
//            cv.put(ProjectContract.ModeRelationshipEntry.ANTHOLOGY_FK, anthId);
//            cv.put(ProjectContract.ModeRelationshipEntry.MODE_FK, modeId);
//            long result = db.insertWithOnConflict(ProjectContract.ModeRelationshipEntry.TABLE_MODE_RELATIONSHIP, null, cv, SQLiteDatabase.CONFLICT_IGNORE);
//        }
//    }

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
        int modeId = getModeId(p.getModeSlug(), p.getAnthologySlug());

        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(ProjectContract.ProjectEntry.PROJECT_TARGET_LANGUAGE_FK, targetLanguageId);
        if (sourceLanguageId != null) {
            cv.put(ProjectContract.ProjectEntry.PROJECT_SOURCE_LANGUAGE_FK, sourceLanguageId);
        }
        cv.put(ProjectContract.ProjectEntry.PROJECT_BOOK_FK, bookId);
        cv.put(ProjectContract.ProjectEntry.PROJECT_VERSION_FK, versionId);
        cv.put(ProjectContract.ProjectEntry.PROJECT_MODE_FK, modeId);
        cv.put(ProjectContract.ProjectEntry.PROJECT_CONTRIBUTORS, p.getContributors());
        cv.put(ProjectContract.ProjectEntry.PROJECT_SOURCE_AUDIO_PATH, p.getSourceAudioPath());
        cv.put(ProjectContract.ProjectEntry.PROJECT_NOTES, "");
        cv.put(ProjectContract.ProjectEntry.PROJECT_PROGRESS, 0);

        long result = db.insert(ProjectContract.ProjectEntry.TABLE_PROJECT, null, cv);
    }

    public void addProject(String languageSlug, String bookSlug, String versionSlug, String modeSlug) throws IllegalArgumentException {
        int targetLanguageId = getLanguageId(languageSlug);
        int bookId = getBookId(bookSlug);
        int versionId = getVersionId(versionSlug);
        String anthologySlug = getAnthologySlug(bookSlug);
        int modeId = getModeId(modeSlug, anthologySlug);

        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(ProjectContract.ProjectEntry.PROJECT_TARGET_LANGUAGE_FK, targetLanguageId);
        cv.put(ProjectContract.ProjectEntry.PROJECT_BOOK_FK, bookId);
        cv.put(ProjectContract.ProjectEntry.PROJECT_VERSION_FK, versionId);
        cv.put(ProjectContract.ProjectEntry.PROJECT_MODE_FK, modeId);
        cv.put(ProjectContract.ProjectEntry.PROJECT_NOTES, "");
        cv.put(ProjectContract.ProjectEntry.PROJECT_PROGRESS, 0);

        long result = db.insert(ProjectContract.ProjectEntry.TABLE_PROJECT, null, cv);
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
    }

    public void addTake(TakeInfo takeInfo, String takeFilename, String modeSlug, long timestamp, int rating, int userId) {
        ProjectSlugs slugs = takeInfo.getProjectSlugs();
        String bookSlug = slugs.getBook();
        String languageSlug = slugs.getLanguage();
        String versionSlug = slugs.getVersion();
        int chapter = takeInfo.getChapter();
        int start = takeInfo.getStartVerse();
        if (!projectExists(languageSlug, bookSlug, versionSlug)) {
            addProject(languageSlug, bookSlug, versionSlug, modeSlug);
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
        cv.put(ProjectContract.TakeEntry.TAKE_NUMBER, takeInfo.getTake());
        cv.put(ProjectContract.TakeEntry.TAKE_FILENAME, takeFilename);
        cv.put(ProjectContract.TakeEntry.TAKE_TIMESTAMP, timestamp);
        cv.put(ProjectContract.TakeEntry.TAKE_USER_FK, userId);
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
                Version version = getVersion(cursor.getInt(cursor.getColumnIndex(ProjectContract.ProjectEntry.PROJECT_VERSION_FK)));
                project.setVersion(version);
                Language targetLanguage = getLanguage(cursor.getInt(cursor.getColumnIndex(ProjectContract.ProjectEntry.PROJECT_TARGET_LANGUAGE_FK)));
                project.setTargetLanguage(targetLanguage);
                int sourceLanguageIndex = cursor.getColumnIndex(ProjectContract.ProjectEntry.PROJECT_SOURCE_LANGUAGE_FK);
                //Source language could be null
                if (cursor.getType(sourceLanguageIndex) == Cursor.FIELD_TYPE_INTEGER) {
                    Language sourceLanguage = getLanguage(cursor.getInt(cursor.getColumnIndex(ProjectContract.ProjectEntry.PROJECT_SOURCE_LANGUAGE_FK)));
                    project.setSourceLanguage(sourceLanguage);
                    project.setSourceAudioPath(cursor.getString(cursor.getColumnIndex(ProjectContract.ProjectEntry.PROJECT_SOURCE_AUDIO_PATH)));
                }
                Mode mode = getMode(cursor.getInt(cursor.getColumnIndex(ProjectContract.ProjectEntry.PROJECT_MODE_FK)));
                project.setMode(mode);
                Book book = getBook(cursor.getInt(cursor.getColumnIndex(ProjectContract.ProjectEntry.PROJECT_BOOK_FK)));
                project.setBook(book);
                Anthology anthology = getAnthology(getAnthologyId(book.getAnthology()));
                project.setAnthology(anthology);
                project.setContributors(cursor.getString(cursor.getColumnIndex(ProjectContract.ProjectEntry.PROJECT_CONTRIBUTORS)));
                projectList.add(project);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return projectList;
    }


    public Project getProject(int projectId) {
        String query = "SELECT * FROM " + ProjectContract.ProjectEntry.TABLE_PROJECT + " WHERE " + ProjectContract.ProjectEntry._ID + " =" + String.valueOf(projectId);
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        Project project = null;
        if (cursor.moveToFirst()) {
            project = new Project();
            Version version = getVersion(cursor.getInt(cursor.getColumnIndex(ProjectContract.ProjectEntry.PROJECT_VERSION_FK)));
            project.setVersion(version);
            Language targetLanguage = getLanguage(cursor.getInt(cursor.getColumnIndex(ProjectContract.ProjectEntry.PROJECT_TARGET_LANGUAGE_FK)));
            project.setTargetLanguage(targetLanguage);
            int sourceLanguageIndex = cursor.getColumnIndex(ProjectContract.ProjectEntry.PROJECT_SOURCE_LANGUAGE_FK);
            //Source language could be null
            if (cursor.getType(sourceLanguageIndex) == Cursor.FIELD_TYPE_INTEGER) {
                Language sourceLanguage = getLanguage(cursor.getInt(cursor.getColumnIndex(ProjectContract.ProjectEntry.PROJECT_SOURCE_LANGUAGE_FK)));
                project.setSourceLanguage(sourceLanguage);
                project.setSourceAudioPath(cursor.getString(cursor.getColumnIndex(ProjectContract.ProjectEntry.PROJECT_SOURCE_AUDIO_PATH)));
            }
            Mode mode = getMode(cursor.getInt(cursor.getColumnIndex(ProjectContract.ProjectEntry.PROJECT_MODE_FK)));
            project.setMode(mode);
            Book book = getBook(cursor.getInt(cursor.getColumnIndex(ProjectContract.ProjectEntry.PROJECT_BOOK_FK)));
            project.setBook(book);
            Anthology anthology = getAnthology(getAnthologyId(book.getAnthology()));
            project.setAnthology(anthology);
            project.setContributors(cursor.getString(cursor.getColumnIndex(ProjectContract.ProjectEntry.PROJECT_CONTRIBUTORS)));
        }
        cursor.close();
        return project;
    }

    public Project getProject(String languageSlug, String versionSlug, String bookSlug) {
        if(projectExists(languageSlug, bookSlug, versionSlug)) {
            int id = getProjectId(languageSlug, bookSlug, versionSlug);
            return getProject(id);
        } else {
            return null;
        }
    }

    public int getNumProjects() {
        SQLiteDatabase db = getReadableDatabase();
        String countQuery = "SELECT * FROM " + ProjectContract.ProjectEntry.TABLE_PROJECT;
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    public int getChapterCheckingLevel(Project project, int chapter) {
        String chapterId = String.valueOf(getChapterId(project, chapter));
        SQLiteDatabase db = getReadableDatabase();
        final String getChapter = String.format("SELECT %s FROM %s WHERE %s=?",
                ProjectContract.ChapterEntry.CHAPTER_CHECKING_LEVEL,
                ProjectContract.ChapterEntry.TABLE_CHAPTER,
                ProjectContract.ChapterEntry._ID
        );
        int checkingLevel = (int) DatabaseUtils.longForQuery(db, getChapter, new String[]{chapterId});
        return checkingLevel;
    }

    public int getTakeRating(TakeInfo takeInfo) {
        ProjectSlugs slugs = takeInfo.getProjectSlugs();
        String unitId = String.valueOf(getUnitId(slugs.getLanguage(), slugs.getBook(), slugs.getVersion(), takeInfo.getChapter(), takeInfo.getStartVerse()));
        SQLiteDatabase db = getReadableDatabase();
        final String getTake = String.format("SELECT %s FROM %s WHERE %s=? AND %s=?",
                ProjectContract.TakeEntry.TAKE_RATING,
                ProjectContract.TakeEntry.TABLE_TAKE,
                ProjectContract.TakeEntry.TAKE_UNIT_FK,
                ProjectContract.TakeEntry.TAKE_NUMBER
        );
        int rating = (int) DatabaseUtils.longForQuery(db, getTake, new String[]{unitId, String.valueOf(takeInfo.getTake())});
        return rating;
    }

    public User getTakeUser(TakeInfo takeInfo) {
        ProjectSlugs slugs = takeInfo.getProjectSlugs();
        String unitId = String.valueOf(getUnitId(slugs.getLanguage(), slugs.getBook(), slugs.getVersion(), takeInfo.getChapter(), takeInfo.getStartVerse()));
        SQLiteDatabase db = getReadableDatabase();
        final String getTake = String.format("SELECT %s FROM %s WHERE %s=? AND %s=?",
                ProjectContract.TakeEntry.TAKE_USER_FK,
                ProjectContract.TakeEntry.TABLE_TAKE,
                ProjectContract.TakeEntry.TAKE_UNIT_FK,
                ProjectContract.TakeEntry.TAKE_NUMBER
        );
        int userId = (int) DatabaseUtils.longForQuery(db, getTake, new String[]{unitId, String.valueOf(takeInfo.getTake())});
        User user = getUser(userId);
        return user;
    }

    private int getSelectedTakeId(int unitId) {
        SQLiteDatabase db = getReadableDatabase();
        final String getTake = String.format("SELECT %s FROM %s WHERE %s=?",
                ProjectContract.UnitEntry.UNIT_CHOSEN_TAKE_FK,
                ProjectContract.UnitEntry.TABLE_UNIT, ProjectContract.UnitEntry._ID
        );
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
                return takeNum;
            }
        }
        return -1;
    }

    public int getSelectedTakeNumber(TakeInfo takeInfo) {
        ProjectSlugs slugs = takeInfo.getProjectSlugs();
        return getSelectedTakeNumber(slugs.getLanguage(), slugs.getBook(), slugs.getVersion(), takeInfo.getChapter(), takeInfo.getStartVerse());
    }

    public void setSelectedTake(TakeInfo takeInfo) {
        ProjectSlugs slugs = takeInfo.getProjectSlugs();
        int unitId = getUnitId(slugs.getLanguage(), slugs.getBook(), slugs.getVersion(), takeInfo.getChapter(), takeInfo.getStartVerse());
        int takeId = getTakeId(takeInfo);
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
    }

    public void setTakeRating(TakeInfo takeInfo, int rating) {
        ProjectSlugs projectSlugs = takeInfo.getProjectSlugs();
        int unitId = getUnitId(projectSlugs.getLanguage(), projectSlugs.getBook(), projectSlugs.getVersion(), takeInfo.getChapter(), takeInfo.getStartVerse());
        SQLiteDatabase db = getReadableDatabase();
        final String replaceTakeWhere = String.format("%s=? AND %s=?",
                ProjectContract.TakeEntry.TAKE_UNIT_FK,
                ProjectContract.TakeEntry.TAKE_NUMBER
        );
        ContentValues replaceWith = new ContentValues();
        replaceWith.put(ProjectContract.TakeEntry.TAKE_RATING, rating);
        int result = db.update(ProjectContract.TakeEntry.TABLE_TAKE, replaceWith, replaceTakeWhere, new String[]{String.valueOf(unitId), String.valueOf(takeInfo.getTake())});
        if (result > 0) {
            autoSelectTake(unitId);
        }
    }

    public void setCheckingLevel(Project project, int chapter, int checkingLevel) {
        String chapterId = String.valueOf(getChapterId(project, chapter));
        SQLiteDatabase db = getReadableDatabase();
        final String replaceChapterWhere = String.format("%s=?", ProjectContract.ChapterEntry._ID);
        ContentValues replaceWith = new ContentValues();
        replaceWith.put(ProjectContract.ChapterEntry.CHAPTER_CHECKING_LEVEL, checkingLevel);
        db.update(ProjectContract.ChapterEntry.TABLE_CHAPTER, replaceWith, replaceChapterWhere, new String[]{chapterId});
    }

    public void setChapterProgress(int chapterId, int progress) {
        final String whereClause = String.format("%s=?", ProjectContract.ChapterEntry._ID);
        String chapterIdString = String.valueOf(chapterId);
        SQLiteDatabase db = getReadableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(ProjectContract.ChapterEntry.CHAPTER_PROGRESS, progress);
        db.update(ProjectContract.ChapterEntry.TABLE_CHAPTER, contentValues, whereClause, new String[]{chapterIdString});
    }

    public int getChapterProgress(int chapterId) {
        String chapterIdString = String.valueOf(chapterId);
        SQLiteDatabase db = getReadableDatabase();
        final String query = String.format("SELECT %s FROM %s WHERE %s=?",
                ProjectContract.ChapterEntry.CHAPTER_PROGRESS,
                ProjectContract.ChapterEntry.TABLE_CHAPTER,
                ProjectContract.ChapterEntry._ID
        );
        float progress = DatabaseUtils.longForQuery(db, query, new String[]{chapterIdString});
        return Math.round(progress);
    }

    public int getProjectProgressSum(int projectId) {
        String projectIdString = String.valueOf(projectId);
        SQLiteDatabase db = getReadableDatabase();
        final String query = String.format("SELECT SUM(%s) FROM %s WHERE %s=?",
                ProjectContract.ChapterEntry.CHAPTER_PROGRESS, ProjectContract.ChapterEntry.TABLE_CHAPTER, ProjectContract.ChapterEntry.CHAPTER_PROJECT_FK);
        int progress = (int) DatabaseUtils.longForQuery(db, query, new String[]{projectIdString});
        return progress;
    }

    public int getProjectProgress(int projectId) {
        String projectIdString = String.valueOf(projectId);
        SQLiteDatabase db = getReadableDatabase();
        final String query = String.format("SELECT %s FROM %s WHERE %s=?",
                ProjectContract.ProjectEntry.PROJECT_PROGRESS,
                ProjectContract.ProjectEntry.TABLE_PROJECT,
                ProjectContract.ProjectEntry._ID
        );
        float progress = DatabaseUtils.longForQuery(db, query, new String[]{projectIdString});
        return Math.round(progress);
    }

    public void setProjectProgress(int projectId, int progress) {
        final String whereClause = String.format("%s=?", ProjectContract.ProjectEntry._ID);
        String projectIdString = String.valueOf(projectId);
        SQLiteDatabase db = getReadableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(ProjectContract.ProjectEntry.PROJECT_PROGRESS, progress);
        db.update(ProjectContract.ProjectEntry.TABLE_PROJECT, contentValues, whereClause, new String[]{projectIdString});
    }

    public void removeSelectedTake(TakeInfo takeInfo) {
        ProjectSlugs slugs = takeInfo.getProjectSlugs();
        String unitId = String.valueOf(getUnitId(slugs.getLanguage(), slugs.getBook(), slugs.getVersion(), takeInfo.getChapter(), takeInfo.getStartVerse()));
        SQLiteDatabase db = getReadableDatabase();
        final String replaceTakeWhere = String.format("%s=?", ProjectContract.UnitEntry._ID);
        ContentValues replaceWith = new ContentValues();
        replaceWith.putNull(ProjectContract.UnitEntry.UNIT_CHOSEN_TAKE_FK);
        db.update(ProjectContract.UnitEntry.TABLE_UNIT, replaceWith, replaceTakeWhere, new String[]{unitId});
    }

    public void deleteProject(Project p) {
        String projectId = String.valueOf(getProjectId(p));
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        final String deleteTakes = String.format("DELETE FROM %s WHERE %s IN (SELECT %s FROM %s WHERE %s=?)",
                ProjectContract.TakeEntry.TABLE_TAKE,
                ProjectContract.TakeEntry.TAKE_UNIT_FK,
                ProjectContract.UnitEntry._ID,
                ProjectContract.UnitEntry.TABLE_UNIT,
                ProjectContract.UnitEntry.UNIT_PROJECT_FK
        );
        db.execSQL(deleteTakes, new String[]{projectId});
        final String deleteUnits = String.format("DELETE FROM %s WHERE %s=?",
                ProjectContract.UnitEntry.TABLE_UNIT,
                ProjectContract.UnitEntry.UNIT_PROJECT_FK
        );
        db.execSQL(deleteUnits, new String[]{projectId});
        final String deleteChapters = String.format("DELETE FROM %s WHERE %s=?",
                ProjectContract.ChapterEntry.TABLE_CHAPTER,
                ProjectContract.ChapterEntry.CHAPTER_PROJECT_FK
        );
        db.execSQL(deleteChapters, new String[]{projectId});
        final String deleteProject = String.format("DELETE FROM %s WHERE %s=?",
                ProjectContract.ProjectEntry.TABLE_PROJECT,
                ProjectContract.ProjectEntry._ID
        );
        db.execSQL(deleteProject, new String[]{projectId});
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    public void deleteTake(TakeInfo takeInfo) {
        ProjectSlugs slugs = takeInfo.getProjectSlugs();
        int unitId = getUnitId(slugs.getLanguage(), slugs.getBook(), slugs.getVersion(), takeInfo.getChapter(), takeInfo.getStartVerse());
        int takeId = getTakeId(takeInfo);
        SQLiteDatabase db = getWritableDatabase();
        final String deleteWhere = String.format("%s=? AND %s=?",
                ProjectContract.TakeEntry.TAKE_UNIT_FK,
                ProjectContract.TakeEntry.TAKE_NUMBER
        );
//        final String deleteTake = String.format("DELETE FROM %s WHERE %s=? AND %s=?",
//                TakeEntry.TABLE_TAKE, TakeEntry.TAKE_UNIT_FK, TakeEntry.TAKE_NUMBER);
        //db.execSQL(deleteTake, new String[]{String.valueOf(unitId), String.valueOf(takeInfo.getTake())});
        int takeSelected = getSelectedTakeId(unitId);
        int result = db.delete(ProjectContract.TakeEntry.TABLE_TAKE, deleteWhere, new String[]{String.valueOf(unitId), String.valueOf(takeInfo.getTake())});
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
    public Map<Integer, Integer> getNumStartedUnitsInProject(Project project) {
        String projectId = String.valueOf(getProjectId(project));
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
        Map<Integer, Integer> numStartedUnits = new HashMap<>();
        if (c.getCount() > 0) {
            c.moveToFirst();
            do {
                int chapterNum = c.getInt(0);
                int unitCount = c.getInt(1);
                numStartedUnits.put(chapterNum, unitCount);
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
        importTakesToDatabase(project, takes, callback, onCorruptFile);
        if (projectExists(project)) {
            int projectId = getProjectId(project);
            String where = String.format("%s.%s=?",
                    ProjectContract.UnitEntry.TABLE_UNIT,
                    ProjectContract.UnitEntry.UNIT_PROJECT_FK
            );
            String[] whereArgs = new String[]{String.valueOf(projectId)};
            removeTakesWithNoFiles(takes, where, whereArgs);
        }
    }

    public void resyncChapterWithFilesystem(Project project, int chapter, List<File> takes, OnLanguageNotFound callback, OnCorruptFile onCorruptFile) {
        importTakesToDatabase(project, takes, callback, onCorruptFile);
        if (projectExists(project) && chapterExists(project, chapter)) {
            int projectId = getProjectId(project);
            int chapterId = getChapterId(project, chapter);
            String whereClause = String.format("%s.%s=? AND %s.%s=?",
                    ProjectContract.UnitEntry.TABLE_UNIT,
                    ProjectContract.UnitEntry.UNIT_PROJECT_FK,
                    ProjectContract.UnitEntry.TABLE_UNIT,
                    ProjectContract.UnitEntry.UNIT_CHAPTER_FK
            );
            String[] whereArgs = new String[]{String.valueOf(projectId), String.valueOf(chapterId)};
            removeTakesWithNoFiles(takes, whereClause, whereArgs);
        }
    }

//    private void resyncWithFilesystem(List<File> takes, String whereClause, String[] whereArgs, OnLanguageNotFound callback){
//        importTakesToDatabase(takes, callback);
//        removeTakesWithNoFiles(takes, whereClause, whereArgs);
//    }

    private void importTakesToDatabase(Project project, List<File> takes, OnLanguageNotFound callback, OnCorruptFile corruptFileCallback) {
        SQLiteDatabase db = getWritableDatabase();
        //create a temporary table to store take names from the filesystem
        db.execSQL(ProjectContract.DELETE_TEMP);
        db.execSQL(ProjectContract.TempEntry.CREATE_TEMP_TABLE);
        db.beginTransaction();
        //add all the take names to the temp table
        for (File f : takes) {
            ContentValues cv = new ContentValues();
            ProjectPatternMatcher ppm = project.getPatternMatcher();
            ppm.match(f);
            if (ppm.matched()) {
                cv.put(ProjectContract.TempEntry.TEMP_TAKE_NAME, f.getName());
                cv.put(ProjectContract.TempEntry.TEMP_TIMESTAMP, f.lastModified());
                db.insert(ProjectContract.TempEntry.TABLE_TEMP, null, cv);
            }
        }
        //compare the names of all takes from the filesystem with the takes already in the database
        //names that do not have a match (are null in the left join) in the database need to be added
        final String getMissingTakes = String.format("SELECT t1.%s, t1.%s FROM %s AS t1 LEFT JOIN %s AS t2 ON t1.%s=t2.%s WHERE t2.%s IS NULL",
                ProjectContract.TempEntry.TEMP_TAKE_NAME,
                ProjectContract.TempEntry.TEMP_TIMESTAMP,
                ProjectContract.TempEntry.TABLE_TEMP,
                ProjectContract.TakeEntry.TABLE_TAKE,
                ProjectContract.TempEntry.TEMP_TAKE_NAME,
                ProjectContract.TakeEntry.TAKE_FILENAME,
                ProjectContract.TakeEntry.TAKE_FILENAME
        );
        Cursor c = db.rawQuery(getMissingTakes, null);
        //loop through all of the missing takes and add them to the db
        if (c.getCount() > 0) {
            int nameIndex = c.getColumnIndex(ProjectContract.TempEntry.TEMP_TAKE_NAME);
            int timestampIndex = c.getColumnIndex(ProjectContract.TempEntry.TEMP_TIMESTAMP);
            c.moveToFirst();
            do {
                String takeName = c.getString(nameIndex);
                ProjectPatternMatcher ppm = project.getPatternMatcher();
                ppm.match(takeName);
                TakeInfo takeInfo = ppm.getTakeInfo();
                ProjectSlugs slugs = takeInfo.getProjectSlugs();
                if (!languageExists(slugs.getLanguage())) {
                    if (callback != null) {
                        String name = callback.requestLanguageName(slugs.getLanguage());
                        addLanguage(slugs.getLanguage(), name);
                    } else {
                        addLanguage(slugs.getLanguage(), "???"); //missingno
                    }
                }
                //Need to get the mode out of the metadata because chunks of only one verse are indistinguishable from verse mode
                File dir = ProjectFileUtils.getParentDirectory(project, takeName);
                File file = new File(dir, c.getString(nameIndex));
                try {
                    WavFile wav = new WavFile(file);
                    //default user; currently not enough info to be able to figure it out
                    addTake(takeInfo, c.getString(nameIndex), wav.getMetadata().getModeSlug(), c.getLong(timestampIndex), 0, 1);
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
                ProjectContract.TakeEntry.TABLE_TAKE,
                ProjectContract.TakeEntry.TAKE_FILENAME,
                ProjectContract.TakeEntry.TABLE_TAKE,
                ProjectContract.TakeEntry._ID, //select
                ProjectContract.TakeEntry.TABLE_TAKE,
                ProjectContract.UnitEntry.TABLE_UNIT, //tables to join takes left join units
                ProjectContract.TakeEntry.TABLE_TAKE,
                ProjectContract.TakeEntry.TAKE_UNIT_FK,
                ProjectContract.UnitEntry.TABLE_UNIT,
                ProjectContract.UnitEntry._ID, //ON takes.unit_fk = units._id
                whereClause
        ); //ie WHERE units.chapter_fk = ?

        final String danglingReferences = String.format("SELECT takefilename, takeid FROM (%s) LEFT JOIN %s as temps ON temps.%s=takefilename WHERE temps.%s IS NULL",
                allTakesFromAProject,
                ProjectContract.TempEntry.TABLE_TEMP,
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


    public void resyncDbWithFs(Project project, List<File> takes, OnLanguageNotFound callback, OnCorruptFile corruptFileCallback) {
        SQLiteDatabase db = getWritableDatabase();
        //create a temporary table to store take names from the filesystem
        db.execSQL(ProjectContract.DELETE_TEMP);
        db.execSQL(ProjectContract.TempEntry.CREATE_TEMP_TABLE);
        db.beginTransaction();
        //add all the take names to the temp table
        for (File f : takes) {
            ContentValues cv = new ContentValues();
            ProjectPatternMatcher ppm = project.getPatternMatcher();
            ppm.match(f);
            if (ppm.matched()) {
                cv.put(ProjectContract.TempEntry.TEMP_TAKE_NAME, f.getName());
                cv.put(ProjectContract.TempEntry.TEMP_TIMESTAMP, f.lastModified());
                db.insert(ProjectContract.TempEntry.TABLE_TEMP, null, cv);
            }
        }
        //compare the names of all takes from the filesystem with the takes already in the database
        //names that do not have a match (are null in the left join) in the database need to be added
        final String getMissingTakes = String.format("SELECT t1.%s, t1.%s FROM %s AS t1 LEFT JOIN %s AS t2 ON t1.%s=t2.%s WHERE t2.%s IS NULL",
                ProjectContract.TempEntry.TEMP_TAKE_NAME,
                ProjectContract.TempEntry.TEMP_TIMESTAMP,
                ProjectContract.TempEntry.TABLE_TEMP,
                ProjectContract.TakeEntry.TABLE_TAKE,
                ProjectContract.TempEntry.TEMP_TAKE_NAME,
                ProjectContract.TakeEntry.TAKE_FILENAME,
                ProjectContract.TakeEntry.TAKE_FILENAME
        );
        Cursor c = db.rawQuery(getMissingTakes, null);
        //loop through all of the missing takes and add them to the db
        if (c.getCount() > 0) {
            int nameIndex = c.getColumnIndex(ProjectContract.TempEntry.TEMP_TAKE_NAME);
            int timestampIndex = c.getColumnIndex(ProjectContract.TempEntry.TEMP_TIMESTAMP);
            c.moveToFirst();
            do {
                String takeName = c.getString(nameIndex);
                ProjectPatternMatcher ppm = project.getPatternMatcher();
                ppm.match(takeName);
                TakeInfo takeInfo = ppm.getTakeInfo();
                ProjectSlugs slugs = takeInfo.getProjectSlugs();
                if (!languageExists(slugs.getLanguage())) {
                    if (callback != null) {
                        String name = callback.requestLanguageName(slugs.getLanguage());
                        addLanguage(slugs.getLanguage(), name);
                    } else {
                        addLanguage(slugs.getLanguage(), "???"); //missingno
                    }
                }
                //Need to get the mode out of the metadata because chunks of only one verse are indistinguishable from verse mode
                File dir = ProjectFileUtils.getParentDirectory(project, takeName);
                File file = new File(dir, c.getString(nameIndex));
                try {
                    WavFile wav = new WavFile(file);
                    //default user
                    addTake(takeInfo, c.getString(nameIndex), wav.getMetadata().getModeSlug(), c.getLong(timestampIndex), 0, 1);
                } catch (IllegalArgumentException e) {
                    //TODO: corrupt file, prompt to fix maybe? or delete? At least tell which file is causing a problem
                    Logger.e(this.toString(), "Error loading wav file named: " + dir + "/" + c.getString(nameIndex), e);
                    corruptFileCallback.onCorruptFile(file);
                }
            } while (c.moveToNext());
        }
        c.close();
        //find all the takes in the db that do not have a match in the filesystem
//        final String deleteDanglingReferences = String.format("SELECT t1.%s, t1.%s FROM %s AS t1 LEFT JOIN %s AS t2 ON t1.%s=t2.%s WHERE t2.%s IS NULL",
//                ProjectContract.TakeEntry.TAKE_FILENAME, ProjectContract.TakeEntry._ID,
//                ProjectContract.TakeEntry.TABLE_TAKE, ProjectContract.TempEntry.TABLE_TEMP,
//                ProjectContract.TempEntry.TEMP_TAKE_NAME, ProjectContract.TakeEntry.TAKE_FILENAME,
//                ProjectContract.TakeEntry.TAKE_FILENAME);

       // select * from takes as t1
        // left join units on (t1.unit_fk=units._id AND units.project_fk=1)
        // left join stuff as t2 on t1.filename=t2.filename
        // where t2.filename is null and project_fk is not null group by number
        final String deleteDanglingReferences = String.format(
                "SELECT t1.%s, t1.%s FROM %s AS t1 " + //t1.filename t1.timestamp from takes as t1
                "LEFT JOIN %s ON t1.%s=%s.%s AND %s.%s=?" + //units on t1.unit_fk=units._id and units.project_fk=?
                "LEFT JOIN %s AS t2 ON t1.%s=t2.%s " + //temp as t2 on t1.filename=t2.filename
                "WHERE t2.%s IS NULL AND %s IS NOT NULL " + //t2.filename is null and project_fk is not null
                "GROUP BY %s", //number
                ProjectContract.TakeEntry.TAKE_FILENAME,
                ProjectContract.TakeEntry._ID,
                ProjectContract.TakeEntry.TABLE_TAKE,
                ProjectContract.UnitEntry.TABLE_UNIT,
                ProjectContract.TakeEntry.TAKE_UNIT_FK,
                ProjectContract.UnitEntry.TABLE_UNIT,
                ProjectContract.UnitEntry._ID,
                ProjectContract.UnitEntry.TABLE_UNIT,
                ProjectContract.UnitEntry.UNIT_PROJECT_FK,
                ProjectContract.TempEntry.TABLE_TEMP,
                ProjectContract.TempEntry.TEMP_TAKE_NAME,
                ProjectContract.TakeEntry.TAKE_FILENAME,
                ProjectContract.TakeEntry.TAKE_FILENAME,
                ProjectContract.UnitEntry.UNIT_PROJECT_FK,
                ProjectContract.TakeEntry.TAKE_NUMBER
        );


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

    public void resyncBookWithFs(Project project, List<File> takes, OnLanguageNotFound callback) {
        SQLiteDatabase db = getWritableDatabase();
        //create a temporary table to store take names from the filesystem
        db.execSQL(ProjectContract.DELETE_TEMP);
        db.execSQL(ProjectContract.TempEntry.CREATE_TEMP_TABLE);
        db.beginTransaction();
        //add all the take names to the temp table
        for (File f : takes) {
            ContentValues cv = new ContentValues();
            ProjectPatternMatcher ppm = project.getPatternMatcher();
            ppm.match(f);
            if (ppm.matched()) {
                cv.put(ProjectContract.TempEntry.TEMP_TAKE_NAME, f.getName());
                cv.put(ProjectContract.TempEntry.TEMP_TIMESTAMP, f.lastModified());
                db.insert(ProjectContract.TempEntry.TABLE_TEMP, null, cv);
            }
        }
        //compare the names of all takes from the filesystem with the takes already in the database
        //names that do not have a match (are null in the left join) in the database need to be added
        final String getMissingTakes = String.format("SELECT t1.%s, t1.%s FROM %s AS t1 LEFT JOIN %s AS t2 ON t1.%s=t2.%s WHERE t2.%s IS NULL",
                ProjectContract.TempEntry.TEMP_TAKE_NAME,
                ProjectContract.TempEntry.TEMP_TIMESTAMP,
                ProjectContract.TempEntry.TABLE_TEMP,
                ProjectContract.TakeEntry.TABLE_TAKE,
                ProjectContract.TempEntry.TEMP_TAKE_NAME,
                ProjectContract.TakeEntry.TAKE_FILENAME,
                ProjectContract.TakeEntry.TAKE_FILENAME
        );
        Cursor c = db.rawQuery(getMissingTakes, null);
        //loop through all of the missing takes and add them to the db
        if (c.getCount() > 0) {
            int nameIndex = c.getColumnIndex(ProjectContract.TempEntry.TEMP_TAKE_NAME);
            int timestampIndex = c.getColumnIndex(ProjectContract.TempEntry.TEMP_TIMESTAMP);
            c.moveToFirst();
            do {
                String takeName = c.getString(nameIndex);
                ProjectPatternMatcher ppm = project.getPatternMatcher();
                ppm.match(takeName);
                TakeInfo takeInfo = ppm.getTakeInfo();
                ProjectSlugs slugs = takeInfo.getProjectSlugs();
                if (!languageExists(slugs.getLanguage())) {
                    if (callback != null) {
                        String name = callback.requestLanguageName(slugs.getLanguage());
                        addLanguage(slugs.getLanguage(), name);
                    } else {
                        addLanguage(slugs.getLanguage(), "???"); //missingno
                    }
                }
                //Need to get the mode out of the metadata because chunks of only one verse are indistinguishable from verse mode
                File dir = ProjectFileUtils.getParentDirectory(takeInfo);
                WavFile wav = new WavFile(new File(dir, c.getString(nameIndex)));
                addTake(takeInfo, c.getString(nameIndex), wav.getMetadata().getModeSlug(), c.getLong(timestampIndex), 0, 1);
            } while (c.moveToNext());
        }
        c.close();
        //find all the takes in the db that do not have a match in the filesystem
        final String deleteDanglingReferences = String.format("SELECT t1.%s, t1.%s FROM %s AS t1 LEFT JOIN %s AS t2 ON t1.%s=t2.%s WHERE t2.%s IS NULL",
                ProjectContract.TakeEntry.TAKE_FILENAME,
                ProjectContract.TakeEntry._ID,
                ProjectContract.TakeEntry.TABLE_TAKE,
                ProjectContract.TempEntry.TABLE_TEMP,
                ProjectContract.TempEntry.TEMP_TAKE_NAME,
                ProjectContract.TakeEntry.TAKE_FILENAME,
                ProjectContract.TakeEntry.TAKE_FILENAME
        );
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
                ProjectContract.TakeEntry._ID,
                ProjectContract.TakeEntry.TABLE_TAKE,
                ProjectContract.TakeEntry.TAKE_UNIT_FK,
                ProjectContract.TakeEntry.TAKE_RATING,
                ProjectContract.TakeEntry.TAKE_TIMESTAMP
        );
        Cursor c = db.rawQuery(autoSelect, new String[]{String.valueOf(unitId)});
        if (c.getCount() > 0) {
            c.moveToFirst();
            int takeId = c.getInt(0);
            setSelectedTake(unitId, takeId);
        }
        c.close();
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
        return languageList.toArray(new Language[languageList.size()]);
    }

    public Anthology[] getAnthologies() {
        List<Anthology> anthologyList = new ArrayList<>();
        String query = "SELECT * FROM " + ProjectContract.AnthologyEntry.TABLE_ANTHOLOGY +
                " ORDER BY " + ProjectContract.AnthologyEntry.ANTHOLOGY_SORT + " ASC";
        SQLiteDatabase db = getReadableDatabase();
        db.beginTransaction();
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                String anthologySlug = cursor.getString(cursor.getColumnIndex(ProjectContract.AnthologyEntry.ANTHOLOGY_SLUG));
                String anthologyName = cursor.getString(cursor.getColumnIndex(ProjectContract.AnthologyEntry.ANTHOLOGY_NAME));
                String resource = cursor.getString(cursor.getColumnIndex(ProjectContract.AnthologyEntry.ANTHOLOGY_RESOURCE));
                int sort = cursor.getInt(cursor.getColumnIndex(ProjectContract.AnthologyEntry.ANTHOLOGY_SORT));
                String regex = cursor.getString(cursor.getColumnIndex(ProjectContract.AnthologyEntry.ANTHOLOGY_REGEX));
                String groups = cursor.getString(cursor.getColumnIndex(ProjectContract.AnthologyEntry.ANTHOLOGY_GROUPS));
                String mask = cursor.getString(cursor.getColumnIndex(ProjectContract.AnthologyEntry.ANTHOLOGY_MASK));
                String jarName = cursor.getString(cursor.getColumnIndex(ProjectContract.AnthologyEntry.PLUGIN_JAR));
                String className = cursor.getString(cursor.getColumnIndex(ProjectContract.AnthologyEntry.PLUGIN_CLASS));
                anthologyList.add(new Anthology(anthologySlug, anthologyName, resource, sort, regex, groups, mask, jarName, className));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.endTransaction();
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
        return versionList.toArray(new Version[versionList.size()]);
    }

    public Mode[] getModes(String anthologySlug) {
        int anthId = getAnthologyId(anthologySlug);
        List<Mode> modeList = new ArrayList<>();
        String query = "SELECT * FROM " + ProjectContract.ModeEntry.TABLE_MODE +
                " WHERE " + ProjectContract.ModeEntry.MODE_ANTHOLOGY_FK + "=" + String.valueOf(anthId);
        SQLiteDatabase db = getReadableDatabase();
        db.beginTransaction();
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                int modeId = cursor.getInt(cursor.getColumnIndex(ProjectContract.ModeEntry._ID));
                Mode mode = getMode(modeId);
                modeList.add(mode);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.endTransaction();
        return modeList.toArray(new Mode[modeList.size()]);
    }
}
