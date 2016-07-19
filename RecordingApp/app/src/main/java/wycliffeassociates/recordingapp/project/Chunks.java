package wycliffeassociates.recordingapp.project;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.ArrayAdapter;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import wycliffeassociates.recordingapp.ProjectManager.Project;

/**
 * Created by sarabiaj on 6/29/2016.
 */
public class Chunks {

    ArrayList<ArrayList<Map<String, String>>> mChunks;
    ArrayList<ArrayList<Map<String, String>>> mVerses;
    ArrayList<Map<String, String>> mParsedChunks;
    int mNumChapters = 0;
    public static String FIRST_VERSE = "firstvs";
    public static String LAST_VERSE = "lastvs";

    public Chunks(Context ctx, String slug) throws IOException{
        parseChunksJSON(ctx, slug);
        generateChunks(mParsedChunks);
        generateVerses();
    }

    private void parseChunksJSON(Context ctx, String slug) throws IOException{
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<Map<String, String>>>(){}.getType();
        String filename = "chunks/" + slug + "/en/" + "ulb" + "/chunks.json";
        InputStream is = ctx.getAssets().open(filename);
        InputStreamReader isr = new InputStreamReader(is);
        JsonReader json = new JsonReader(isr);
        mParsedChunks = gson.fromJson(json, type);
        json.close();
        isr.close();
        is.close();
        String id = mParsedChunks.get(mParsedChunks.size()-1).get("id");
        mNumChapters = Integer.parseInt(id.substring(0, id.lastIndexOf("-")));
    }

    private void generateChunks(List<Map<String, String>> parsedChunks){
        mChunks = new ArrayList<>();
        ArrayList<Map<String, String>> temp = new ArrayList<>();
        int currentChapter = 1;
        String chunkId;
        int chapter;
        for(Map<String, String> chunk : parsedChunks){
            chunkId = chunk.get("id");
            chapter = Integer.parseInt(chunkId.substring(0, chunkId.lastIndexOf("-")));
            if(chapter == currentChapter){
                temp.add(chunk);
            } else {
                mChunks.add(temp);
                temp = new ArrayList<>();
                temp.add(chunk);
                currentChapter = chapter;
            }
        }
        //add last
        mChunks.add(temp);
    }

    private void generateVerses(){
        mVerses = new ArrayList<>();
        ArrayList<Map<String, String>> temp = new ArrayList<>();
        for(List<Map<String, String>> chapter : mChunks){
            int length = Integer.parseInt(chapter.get(chapter.size()-1).get(LAST_VERSE));
            for(int i = 1; i <= length; i++){
                Map<String, String> verse = new HashMap<>();
                String verseNumber = String.valueOf(i);
                verse.put(FIRST_VERSE, verseNumber);
                verse.put(LAST_VERSE, verseNumber);
                temp.add(verse);
            }
            mVerses.add(temp);
            temp = new ArrayList<>();
        }
    }

    public int getNumChapters(){
        return mNumChapters;
    }

    public int getNumChunks(Project project, int chapter){
        if(project.getMode().compareTo("chunk") == 0) {
            return mChunks.get(chapter - 1).size();
        } else {
            return mVerses.get(chapter - 1).size();
        }
    }

    public List<Map<String,String>> getChunks(Project project, int chapter) {
        if(project.getMode().compareTo("chunk") == 0) {
            return mChunks.get(chapter - 1);
        } else {
            return mVerses.get(chapter - 1);
        }
    }

}
