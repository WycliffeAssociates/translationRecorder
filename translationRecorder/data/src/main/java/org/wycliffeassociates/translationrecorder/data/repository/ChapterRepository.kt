package org.wycliffeassociates.translationrecorder.data.repository

import org.wycliffeassociates.translationrecorder.chunkplugin.Chapter

/**
 * Created by sarabiaj on 4/26/2018.
 */
interface ChapterRepository : Repository<Chapter> {
    override fun getById(id: Int): Chapter
    override fun getAll(): List<Chapter>
    override fun insert(language: Chapter): Long
    override fun insertAll(languages: List<Chapter>): List<Long>
    override fun update(language: Chapter)
    override fun delete(language: Chapter)
}