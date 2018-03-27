package org.wycliffeassociates.translationrecorder.data.components

/**
 * Created by sarabiaj on 5/10/2016.
 */
data class ProjectEntity(
        val language: LanguageEntity,
        val anthology: AnthologyEntity,
        val version: VersionEntity,
        val book: BookEntity,
        val mode: ModeEntity
)