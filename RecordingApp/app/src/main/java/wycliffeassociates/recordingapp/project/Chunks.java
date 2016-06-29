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
import java.util.Map;

/**
 * Created by sarabiaj on 6/29/2016.
 */
public class Chunks {

    ArrayList<ArrayList<Map<String, String>>> mChunks;
    int mNumChapters = 0;

    public Chunks(Context ctx, String slug) throws IOException{
        long start = System.currentTimeMillis();
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<Map<String, String>>>(){}.getType();
        String filename = "chunks/" + slug + "/en/" + "ulb" + "/chunks.json";
        InputStream is = ctx.getAssets().open(filename);
        InputStreamReader isr = new InputStreamReader(is);
        JsonReader json = new JsonReader(isr);
        ArrayList<Map<String, String>> parsedChunks = gson.fromJson(json, type);
        json.close();
        isr.close();
        is.close();
        String id = parsedChunks.get(parsedChunks.size()-1).get("id");
        mNumChapters = Integer.parseInt(id.substring(0, id.lastIndexOf("-")));
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
        long end = System.currentTimeMillis();
        System.out.println("Total time is: " + (end-start) +"ms");
    }

    public int getNumChapters(){
        return mNumChapters;
    }

    public int getNumChunks(int chapter){
        return mChunks.get(chapter-1).size();
    }



}
