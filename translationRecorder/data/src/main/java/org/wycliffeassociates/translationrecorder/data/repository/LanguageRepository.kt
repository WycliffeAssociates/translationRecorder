package org.wycliffeassociates.translationrecorder.data.repository

import org.wycliffeassociates.translationrecorder.data.model.Language

/**
 * Created by sarabiaj on 3/28/2018.
 */

interface LanguageRepository : Repository<Language> {
    override fun getById(id: Int): Language
    override fun getAll(): List<Language>
    override fun insert(language: Language): Long
    override fun insertAll(languages: List<Language>): List<Long>
    override fun update(language: Language)
    override fun delete(language: Language)
}