package org.wycliffeassociates.translationrecorder.data.repository

import org.wycliffeassociates.translationrecorder.data.components.Language

/**
 * Created by sarabiaj on 3/28/2018.
 */

interface LanguageRepository {
    fun getLanguage(id: Long) : Language
    fun getLanguages() : List<Language>
    fun addLanguage(language: Language)
    fun updateLanguage(language: Language)
    fun deleteLanguage(language: Language)
}