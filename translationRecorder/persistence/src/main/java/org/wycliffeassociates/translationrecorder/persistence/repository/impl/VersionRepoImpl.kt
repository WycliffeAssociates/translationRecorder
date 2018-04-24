package org.wycliffeassociates.translationrecorder.persistence.repository.impl

import org.wycliffeassociates.translationrecorder.data.model.Version
import org.wycliffeassociates.translationrecorder.data.repository.VersionRepository
import org.wycliffeassociates.translationrecorder.persistence.mapping.VersionMapper
import org.wycliffeassociates.translationrecorder.persistence.repository.dao.VersionDao

/**
 * Created by sarabiaj on 4/24/2018.
 */

class VersionRepoImpl(val versionDao: VersionDao): VersionRepository {
    val mapper = VersionMapper()

    override fun getById(id: Int): Version {
        return mapper.mapFromEntity(versionDao.getById(id))
    }

    override fun getAll(): List<Version> {
        return versionDao.getAll().map { mapper.mapFromEntity(it) }
    }

    override fun insert(version: Version): Long {
        return versionDao.insert(mapper.mapToEntity(version))
    }

    override fun insertAll(versions: List<Version>): List<Long> {
        return versionDao.insertAll(versions.map { mapper.mapToEntity(it) })
    }

    override fun update(version: Version) {
        return versionDao.update(mapper.mapToEntity(version))
    }

    override fun delete(version: Version) {
        return versionDao.delete(mapper.mapToEntity(version))
    }

}