package org.wycliffeassociates.translationrecorder.project.components;

import org.wycliffeassociates.translationrecorder.Utils;

import java.util.ArrayList;

/**
 * Created by sarabiaj on 1/15/2016.
 */
public class Book extends ProjectComponent {

    public static class Chunk {
        public int chapterId;
        public int chunkId;
        public int startVerse;
        public int endVerse;
    }

    private int mNumChapters;
    private String mAnthology;
    private ArrayList<ArrayList<Chunk>> mChunks;
    private int mOrder;


    public Book(String slug, String name, String anthology, int chapters, ArrayList<ArrayList<Chunk>> chunks, int order){
        super(slug, name);
        mNumChapters = chapters;
        mAnthology = anthology;
        mChunks = chunks;
        mOrder = order;
    }

    public Book(String slug, String name, String anthology, int order){
        super(slug, name);
        mNumChapters = 0;
        mAnthology = anthology;
        mChunks = null;
        mOrder = order;
    }

    public int getNumChapters() {
        return mNumChapters;
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

    @Override
    public String getLabel() {
        String label = "";
        String[] resourceLabels = mName.split(" ");
        for(String part : resourceLabels) {
            label += " " + Utils.capitalizeFirstLetter(part);
        }
        return label;
    }

    @Override
    public int compareTo(Object another) {
        return new Integer(mOrder).compareTo(((Book)another).getOrder());
    }
}
