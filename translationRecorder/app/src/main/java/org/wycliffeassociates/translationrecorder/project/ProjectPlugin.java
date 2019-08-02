package org.wycliffeassociates.translationrecorder.project;

import android.content.Context;
import android.util.JsonReader;

import org.wycliffeassociates.translationrecorder.database.ProjectDatabaseHelper;
import org.wycliffeassociates.translationrecorder.project.components.Book;
import org.wycliffeassociates.translationrecorder.project.components.Mode;
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
    String jarPath;
    String className;
    String versionsPath;
    int sort;

    //groups
    int language = -1;
    int version = -1;
    int bookNumber = -1;
    int book = -1;
    int chapter = -1;
    int startVerse = -1;

    int endVerse = -1;
    int take = -1;
    List<Mode> modes = new ArrayList<>();


    public ProjectPlugin(File pluginDir, File plugin) throws IOException {
        this.pluginDir = pluginDir;
        Reader reader = new FileReader(plugin);
        init(reader);
    }

    public void importProjectPlugin(File pluginDir, ProjectDatabaseHelper db) throws IOException {
        Reader bookReader = new FileReader(new File(pluginDir, "Books/" + booksPath));
        List<Book> books = readBooks(new JsonReader(bookReader));
        Reader versionReader = new FileReader(new File(pluginDir, "Versions/" + versionsPath));
        List<Version> versions = readVersions(new JsonReader(versionReader));
//        Reader chunksReader = new FileReader(new File(pluginDir, "Chunks/" + chunksPath));
//        readChunks(new JsonReader(chunksReader));

        importPluginToDatabase(
                books.toArray(new Book[books.size()]),
                versions.toArray(new Version[versions.size()]),
                modes.toArray(new Mode[modes.size()]),
                db
        );
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
        List<Version> versionList = new ArrayList<>();
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
            versionList.add(new Version(slug, name));
            jsonReader.endObject();
        }
        jsonReader.endArray();
        return versionList;
    }

    private void importPluginToDatabase(
            Book[] books,
            Version[] versions,
            Mode[] modes,
            ProjectDatabaseHelper db
    ) {
        db.addAnthology(slug, name, resource, sort, regex, groups, mask, jarPath, className);
        db.addBooks(books);
        db.addVersions(versions);
        db.addModes(modes, slug);
        db.addVersionRelationships(slug, versions);
        //db.addModeRelationships(slug, modes);
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
            } else if (key.equals("modes")) {
                readModesSection(jsonReader);
            } else  if (key.equals("chunk_plugin")) {
                readChunkSection(jsonReader);
            } else  if (key.equals("sort")) {
                sort = jsonReader.nextInt();
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

    public void readModesSection(JsonReader jsonReader) throws IOException {
        jsonReader.beginArray();
        while (jsonReader.hasNext()) {
            jsonReader.beginObject();
            String name = null;
            String type = null;
            while (jsonReader.hasNext()) {
                String key = jsonReader.nextName();
                if (key.equals("name")) {
                    name = jsonReader.nextString();
                } else if (key.equals("type")) {
                    type = jsonReader.nextString();
                }
            }
            jsonReader.endObject();
            modes.add(new Mode(name, name, type));
        }
        jsonReader.endArray();
    }

    public void readChunkSection(JsonReader jsonReader) throws IOException
    {
        jsonReader.beginObject();
        while (jsonReader.hasNext()) {
            String key = jsonReader.nextName();
            if(key.equals("jar")) {
                jarPath = jsonReader.nextString();
            } else if (key.equals("class")) {
                className = jsonReader.nextString();
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

    public String getJarPath() {
        return jarPath;
    }
}
