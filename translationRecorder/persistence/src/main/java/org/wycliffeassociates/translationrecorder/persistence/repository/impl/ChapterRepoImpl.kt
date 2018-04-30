package org.wycliffeassociates.translationrecorder.persistence.repository.impl

import org.wycliffeassociates.translationrecorder.chunkplugin.Chapter
import org.wycliffeassociates.translationrecorder.chunkplugin.ChunkPlugin
import org.wycliffeassociates.translationrecorder.data.model.Project
import org.wycliffeassociates.translationrecorder.data.repository.ChapterRepository
import org.wycliffeassociates.translationrecorder.persistence.mapping.ChapterMapper
import org.wycliffeassociates.translationrecorder.persistence.repository.dao.ChapterDao

/**
 * Created by sarabiaj on 4/26/2018.
 */

class ChapterRepoImpl(
        val project: Project,
        val plugin: ChunkPlugin,
        val chapterDao: ChapterDao
) : ChapterRepository {
    val mapper = ChapterMapper(project, plugin)

    override fun getById(id: Int): Chapter {
        return mapper.mapFromEntity(chapterDao.getById(id))
    }

    override fun getAll(): List<Chapter> {
        return chapterDao.getAll().map { mapper.mapFromEntity(it) }
    }

    override fun insert(project: Chapter): Long {
        return chapterDao.insert(mapper.mapToEntity(project))
    }

    override fun insertAll(projects: List<Chapter>): List<Long> {
        return chapterDao.insertAll(projects.map { mapper.mapToEntity(it) })
    }

    override fun update(project: Chapter) {
        return chapterDao.update(mapper.mapToEntity(project))
    }

    override fun delete(project: Chapter) {
        return chapterDao.delete(mapper.mapToEntity(project))
    }

}