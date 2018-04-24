package org.wycliffeassociates.translationrecorder.persistence.repository.impl

import org.wycliffeassociates.translationrecorder.data.model.Anthology
import org.wycliffeassociates.translationrecorder.data.repository.AnthologyRepository
import org.wycliffeassociates.translationrecorder.persistence.mapping.AnthologyMapper
import org.wycliffeassociates.translationrecorder.persistence.repository.dao.AnthologyDao

/**
 * Created by sarabiaj on 4/24/2018.
 */

class AnthologyRepoImpl(val anthDao: AnthologyDao) : AnthologyRepository {
    override fun insert(anthology: Anthology): Long {
        return anthDao.insert(AnthologyMapper().mapToEntity(anthology))
    }

    override fun insertAll(anthologies: List<Anthology>): List<Long> {
        val mapper = AnthologyMapper()
        val entities = anthologies.map { mapper.mapToEntity(it) }
        return anthDao.insertAll(entities)
    }

    override fun update(anthology: Anthology) {
        return anthDao.update(AnthologyMapper().mapToEntity(anthology))
    }

    override fun delete(anthology: Anthology) {
        return anthDao.delete(AnthologyMapper().mapToEntity(anthology))
    }

    override fun getAll(): List<Anthology> {
        val mapper = AnthologyMapper()
        val entities = anthDao.getAll()
        return entities.map { mapper.mapFromEntity(it) }
    }

    override fun getById(id: Int): Anthology {
        return AnthologyMapper().mapFromEntity(anthDao.getById(id))
    }

}