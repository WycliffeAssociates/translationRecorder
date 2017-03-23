package org.wycliffeassociates.translationrecorder.project;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.JsonReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import static android.R.attr.key;

/**
 * Created by sarabiaj on 3/20/2017.
 */

public class Project {

    File pluginDir;

    String resource;
    String slug;
    String name;
    String regex;
    String booksPath;
    String chunksPath;
    String versionsPath;

    //groups
    int language;
    int version;
    int bookNumber;
    int book;
    int chapter;
    int startVerse;
    int endVerse;
    int take;

    static final int LANGUAGE = 0x1;
    static final int VERSION = 0x2;
    static final int BOOK_NUMBER = 0x3;
    static final int BOOK = 0x4;
    static final int CHAPTER = 0x5;
    static final int START_VERSE = 0x6;
    static final int END_VERSE = 0x7;
    static final int TAKE = 0x8;

    public Project(){}

    public void importProjectPlugin(Context context, File pluginDir, File plugin) throws IOException {
        this.pluginDir = pluginDir;
        FileInputStream fis = new FileInputStream(plugin);
        Reader reader = new FileReader(plugin);
        init(reader);

        Reader bookReader = new FileReader(new File(pluginDir, booksPath));
        readBooks(new JsonReader(bookReader));
        Reader versionReader = new FileReader(new File(pluginDir, versionsPath));
        readVersions(new JsonReader(versionReader));
        Reader chunksReader = new FileReader(new File(pluginDir, chunksPath));
        readVersions(new JsonReader(chunksReader));
    }

    private void init(Reader reader) throws IOException {
        JsonReader jsonReader = new JsonReader(reader);
        readPlugin(jsonReader);
        int mask = createMatchGroupMask();
        importPluginToDatabase();
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
                if(key.equals("slug")) {
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
                if(key.equals("slug")) {
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

    private void importPluginToDatabase() {
        addBooks();
        addVersions();
        addVersionRelationships();
    }

    private int createMatchGroupMask(){
        int mask = 0;
        if(language > 0) {
            mask |= LANGUAGE;
        }
        if(version > 0) {
            mask |= VERSION;
        }
        if(bookNumber > 0){
            mask |= BOOK_NUMBER;
        }
        if(book > 0) {
            mask |= BOOK;
        }
        if(chapter > 0) {
            mask |= CHAPTER;
        }
        if(startVerse > 0) {
            mask |= START_VERSE;
        }
        if(endVerse > 0) {
            mask |= END_VERSE;
        }
        if(take > 0) {
            mask |= TAKE;
        }
        return mask;
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
}
