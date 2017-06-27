package org.wycliffeassociates.translationrecorder.project;

import android.content.Context;
import android.util.JsonReader;

import org.wycliffeassociates.translationrecorder.database.ProjectDatabaseHelper;
import org.wycliffeassociates.translationrecorder.project.components.Book;
import org.wycliffeassociates.translationrecorder.project.components.Version;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sarabiaj on 3/20/2017.
 */

public class ProjectPlugin {

    File pluginDir;

    String resource;
    String slug;
    String name;
    String regex;
    String groups;
    String mask;
    String booksPath;
    String chunksPath;

    String versionsPath;
    //groups
    int language = -1;
    int version = -1;
    int bookNumber = -1;
    int book = -1;
    int chapter = -1;
    int startVerse = -1;
    int endVerse = -1;

    int take = -1;

    public ProjectPlugin(File pluginDir, File plugin) throws IOException {
        this.pluginDir = pluginDir;
        Reader reader = new FileReader(plugin);
        init(reader);
    }

    public void importProjectPlugin(Context context, File pluginDir) throws IOException {
        Reader bookReader = new FileReader(new File(pluginDir, "Books/" + booksPath));
        List<Book> books = readBooks(new JsonReader(bookReader));
        Reader versionReader = new FileReader(new File(pluginDir, "Versions/" + versionsPath));
        List<Version> versions = readVersions(new JsonReader(versionReader));
//        Reader chunksReader = new FileReader(new File(pluginDir, "Chunks/" + chunksPath));
//        readChunks(new JsonReader(chunksReader));

        importPluginToDatabase(context, books.toArray(new Book[books.size()]), versions.toArray(new Version[versions.size()]));
    }

    private void init(Reader reader) throws IOException {
        JsonReader jsonReader = new JsonReader(reader);
        readPlugin(jsonReader);
        groups = createMatchGroups();
    }

    private List<Book> readBooks(JsonReader jsonReader) throws IOException {
        List<Book> bookList = new ArrayList<>();
        jsonReader.beginArray();
        while (jsonReader.hasNext()) {
            jsonReader.beginObject();
            String slug = null;
            int num = 0;
            String anth = null;
            String name = null;
            while (jsonReader.hasNext()) {
                String key = jsonReader.nextName();
                if (key.equals("slug")) {
                    slug = jsonReader.nextString();
                } else if (key.equals("num")) {
                    num = jsonReader.nextInt();
                } else if (key.equals("anth")) {
                    anth = jsonReader.nextString();
                } else if (key.equals("name")) {
                    name = jsonReader.nextString();
                }
            }
            bookList.add(new Book(slug, name, anth, num));
            jsonReader.endObject();
        }
        jsonReader.endArray();
        return bookList;
    }

    private void readChunks(JsonReader jsonReader) {

    }

    private List<Version> readVersions(JsonReader jsonReader) throws IOException {
        List<Version> bookList = new ArrayList<>();
        jsonReader.beginArray();
        while (jsonReader.hasNext()) {
            jsonReader.beginObject();
            String slug = null;
            String name = null;
            while (jsonReader.hasNext()) {
                String key = jsonReader.nextName();
                if (key.equals("slug")) {
                    slug = jsonReader.nextString();
                } else if (key.equals("name")) {
                    name = jsonReader.nextString();
                }
            }
            bookList.add(new Version(slug, name));
            jsonReader.endObject();
        }
        jsonReader.endArray();
        return bookList;
    }

    private void importPluginToDatabase(Context ctx, Book[] books, Version[] versions) {
        ProjectDatabaseHelper db = new ProjectDatabaseHelper(ctx);
        db.addAnthology(slug, name, resource, regex, groups, mask);
        db.addBooks(books);
        db.addVersions(versions);
        db.addVersionRelationships(slug, versions);
        db.close();
    }

    private String createMatchGroups() {
        StringBuilder groups = new StringBuilder();
        groups.append(language);
        groups.append(" ");
        groups.append(version);
        groups.append(" ");
        groups.append(bookNumber);
        groups.append(" ");
        groups.append(book);
        groups.append(" ");
        groups.append(chapter);
        groups.append(" ");
        groups.append(startVerse);
        groups.append(" ");
        groups.append(endVerse);
        groups.append(" ");
        groups.append(take);
        return groups.toString();
    }

    private void readPlugin(JsonReader jsonReader) throws IOException {
        jsonReader.beginObject();
        while (jsonReader.hasNext()) {
            String key = jsonReader.nextName();
            if (key.equals("resource")) {
                resource = jsonReader.nextString();
            } else if (key.equals("books")) {
                booksPath = jsonReader.nextString();
            } else if (key.equals("chunks")) {
                chunksPath = jsonReader.nextString();
            } else if (key.equals("versions")) {
                versionsPath = jsonReader.nextString();
            } else if (key.equals("anthology")) {
                readAnthologySection(jsonReader);
            }
        }
        jsonReader.endObject();
    }

    private void readAnthologySection(JsonReader jsonReader) throws IOException {
        jsonReader.beginObject();
        while (jsonReader.hasNext()) {
            String key = jsonReader.nextName();
            if (key.equals("slug")) {
                slug = jsonReader.nextString();
            } else if (key.equals("name")) {
                name = jsonReader.nextString();
            } else if (key.equals("file_conv")) {
                mask = jsonReader.nextString();
            } else if (key.equals("parser")) {
                readParserSection(jsonReader);
            }
        }
        jsonReader.endObject();
    }

    private void readParserSection(JsonReader jsonReader) throws IOException {
        jsonReader.beginObject();
        while (jsonReader.hasNext()) {
            String key = jsonReader.nextName();
            if (key.equals("regex")) {
                regex = jsonReader.nextString();
            } else if (key.equals("groups")) {
                readGroupsSection(jsonReader);
            }
        }
        jsonReader.endObject();
    }

    private void readGroupsSection(JsonReader jsonReader) throws IOException {
        jsonReader.beginObject();
        while (jsonReader.hasNext()) {
            String key = jsonReader.nextName();
            if (key.equals("language")) {
                language = jsonReader.nextInt();
            } else if (key.equals("version")) {
                version = jsonReader.nextInt();
            } else if (key.equals("book_number")) {
                bookNumber = jsonReader.nextInt();
            } else if (key.equals("book")) {
                book = jsonReader.nextInt();
            } else if (key.equals("chapter")) {
                chapter = jsonReader.nextInt();
            } else if (key.equals("start_verse")) {
                startVerse = jsonReader.nextInt();
            } else if (key.equals("end_verse")) {
                endVerse = jsonReader.nextInt();
            } else if (key.equals("take")) {
                take = jsonReader.nextInt();
            }
        }
        jsonReader.endObject();
    }

    public String getBooksPath() {
        return booksPath;
    }

    public String getChunksPath() {
        return chunksPath;
    }

    public String getVersionsPath() {
        return versionsPath;
    }
}
