package org.wycliffeassociates.translationrecorder.data.repository

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
}