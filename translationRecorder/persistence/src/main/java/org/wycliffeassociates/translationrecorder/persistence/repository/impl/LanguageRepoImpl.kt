package org.wycliffeassociates.translationrecorder.persistence.repository.impl

import org.wycliffeassociates.translationrecorder.data.model.Language
import org.wycliffeassociates.translationrecorder.data.repository.LanguageRepository
import org.wycliffeassociates.translationrecorder.persistence.mapping.LanguageMapper
import org.wycliffeassociates.translationrecorder.persistence.repository.dao.LanguageDao

/**
 * Created by sarabiaj on 4/24/2018.
 */

class LanguageRepoImpl(val languageDao: LanguageDao): LanguageRepository {

    val mapper = LanguageMapper()

    override fun getById(id: Int): Language {
        return mapper.mapFromEntity(languageDao.getById(id))
    }

    override fun getAll(): List<Language> {
        return languageDao.getAll().map { mapper.mapFromEntity(it) }
    }

    override fun insert(language: Language): Long {
        return languageDao.insert(mapper.mapToEntity(language))
    }

    override fun insertAll(languages: List<Language>): List<Long> {
        return languageDao.insertAll(languages.map { mapper.mapToEntity(it) })
    }

    override fun update(language: Language) {
        return languageDao.update(mapper.mapToEntity(language))
    }

    override fun delete(language: Language) {
        return languageDao.delete(mapper.mapToEntity(language))
    }

}