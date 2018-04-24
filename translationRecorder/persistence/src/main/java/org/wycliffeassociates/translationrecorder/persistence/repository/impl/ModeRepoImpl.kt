package org.wycliffeassociates.translationrecorder.persistence.repository.impl

import org.wycliffeassociates.translationrecorder.chunkplugin.Mode
import org.wycliffeassociates.translationrecorder.data.repository.ModeRepository
import org.wycliffeassociates.translationrecorder.persistence.mapping.ModeMapper
import org.wycliffeassociates.translationrecorder.persistence.repository.dao.ModeDao

/**
 * Created by sarabiaj on 4/24/2018.
 */

class ModeRepoImpl(val modeDao: ModeDao) : ModeRepository {
    val mapper = ModeMapper()

    override fun getById(id: Int): Mode {
        return mapper.mapFromEntity(modeDao.getById(id))
    }

    override fun getAll(): List<Mode> {
        return modeDao.getAll().map { mapper.mapFromEntity(it) }
    }

    override fun insert(mode: Mode): Long {
        return modeDao.insert(mapper.mapToEntity(mode))
    }

    override fun insertAll(modes: List<Mode>): List<Long> {
        return modeDao.insertAll(modes.map { mapper.mapToEntity(it) })
    }

    override fun update(mode: Mode) {
        return modeDao.update(mapper.mapToEntity(mode))
    }

    override fun delete(mode: Mode) {
        return modeDao.delete(mapper.mapToEntity(mode))
    }

}