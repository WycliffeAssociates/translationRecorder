package org.wycliffeassociates.translationrecorder.data.components

/**
 * Created by sarabiaj on 5/10/2016.
 */
data class Project(
        val language: Language,
        val anthology: Anthology,
        val version: Version,
        val book: Book,
        val mode: Mode
)