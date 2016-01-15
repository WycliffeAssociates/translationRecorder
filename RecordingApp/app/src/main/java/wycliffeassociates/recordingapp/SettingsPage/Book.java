package wycliffeassociates.recordingapp.SettingsPage;

import java.util.ArrayList;

/**
 * Created by sarabiaj on 1/15/2016.
 */
public class Book {

    private int mNumChapters;
    private String mSlug;
    private String mName;
    private ArrayList<Integer> mChunks;
    private int mOrder;

    public Book(String slug, String name, int chapters, ArrayList<Integer> chunks, int order){
        mNumChapters = chapters;
        mName = name;
        mSlug = slug;
        mChunks = chunks;
        mOrder = order;
    }

    public int getNumChapters() {
        return mNumChapters;
    }

    public String getSlug() {
        return mSlug;
    }

    public String getName() {
        return mName;
    }

    public ArrayList<Integer> getChunks() {
        return mChunks;
    }

    public int getOrder() {
        return mOrder;
    }
}
