package org.wycliffeassociates.translationrecorder.data.repository

import org.wycliffeassociates.translationrecorder.data.model.Take

/**
 * Created by sarabiaj on 4/26/2018.
 */
interface TakeRepository : Repository<Take> {
    override fun getById(id: Int): Take
    override fun getAll(): List<Take>
    override fun insert(anthology: Take): Long
    override fun insertAll(anthologies: List<Take>): List<Long>
    override fun update(anthology: Take)
    override fun delete(anthology: Take)
}