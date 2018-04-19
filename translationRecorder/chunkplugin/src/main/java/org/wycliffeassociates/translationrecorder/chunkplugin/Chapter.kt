package org.wycliffeassociates.translationrecorder.chunkplugin

/**
 * Created by sarabiaj on 7/27/2017.
 */

abstract class Chapter {

    var id: Int? = null
    var progress: Int = 0
    var checkingLevel: Int = 0
    //    List<Chunk> mChunks;
    //    String mLabel;
    //    int mNumber;
    //
    //    public Chapter(int number, String label, List<Chunk> chunks) {
    //        mChunks = chunks;
    //        mLabel = label;
    //        mNumber = number;
    //    }

    abstract val chunks: List<Chunk>

    abstract val name: String

    abstract val label: String

    abstract val number: Int

    abstract val chunkDisplayValues: Array<String>

    abstract fun addChunk(chunk: Chunk)

}
