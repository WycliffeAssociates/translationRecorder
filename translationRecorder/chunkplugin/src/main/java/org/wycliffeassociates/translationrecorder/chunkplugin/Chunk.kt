package org.wycliffeassociates.translationrecorder.chunkplugin

/**
 * Created by sarabiaj on 7/27/2017.
 */

abstract class Chunk(
        var id: Int? = null,
        val label: String,
        val order: Int,
        val startVerse: Int,
        val endVerse: Int,
        val numMarkers: Int,
        val chosenTake: Int? = null
)
