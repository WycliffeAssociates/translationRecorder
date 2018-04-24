package org.wycliffeassociates.translationrecorder.data.repository

import org.wycliffeassociates.translationrecorder.data.model.Version

/**
 * Created by sarabiaj on 3/28/2018.
 */

interface VersionRepository : Repository<Version> {
    override fun getById(id: Int) : Version
    override fun getAll() : List<Version>
    override fun insert(version: Version): Long
    override fun insertAll(versions: List<Version>): List<Long>
    override fun update(version: Version)
    override fun delete(version: Version)
}