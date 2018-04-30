package org.wycliffeassociates.translationrecorder.data.repository

import org.wycliffeassociates.translationrecorder.chunkplugin.ChunkPlugin
import org.wycliffeassociates.translationrecorder.data.model.Project

/**
 * Created by sarabiaj on 4/24/2018.
 */
interface AppDatabase {
    fun anthologyRepo(): AnthologyRepository
    fun bookRepo(): BookRepository
    fun languageRepo(): LanguageRepository
    fun modeRepo(): ModeRepository
    fun projectRepo(): ProjectRepository
    fun versionRepo(): VersionRepository
    fun chapterRepo(project: Project, plugin: ChunkPlugin): ChapterRepository
}