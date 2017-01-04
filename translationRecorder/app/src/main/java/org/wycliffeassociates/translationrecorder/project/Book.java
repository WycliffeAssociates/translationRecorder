package org.wycliffeassociates.translationrecorder.project;

import java.util.ArrayList;

/**
 * Created by sarabiaj on 1/15/2016.
 */
public class Book {

    public static class Chunk {
        public int chapterId;
        public int chunkId;
        public int startVerse;
        public int endVerse;
    }

    private int mNumChapters;
    private String mSlug;
    private String mName;
    private String mAnthology;
    private ArrayList<ArrayList<Chunk>> mChunks;
    private int mOrder;


    public Book(String slug, String name, String anthology, int chapters, ArrayList<ArrayList<Chunk>> chunks, int order){
        mNumChapters = chapters;
        mName = name;
        mSlug = slug;
        mAnthology = anthology;
        mChunks = chunks;
        mOrder = order;
    }

    public Book(String slug, String name, String anthology, int order){
        mNumChapters = 0;
        mName = name;
        mSlug = slug;
        mAnthology = anthology;
        mChunks = null;
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

    public String getAnthology() {
        return mAnthology;
    }

    public ArrayList<ArrayList<Chunk>> getChunks() {
        return mChunks;
    }

    public int getOrder() {
        return mOrder;
    }
}
