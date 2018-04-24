package org.wycliffeassociates.translationrecorder.data.repository

import org.wycliffeassociates.translationrecorder.data.model.Anthology

/**
 * Created by sarabiaj on 3/28/2018.
 */

interface AnthologyRepository : Repository<Anthology> {
    override fun getById(id: Int): Anthology
    override fun getAll(): List<Anthology>
    override fun insert(anthology: Anthology): Long
    override fun insertAll(anthologies: List<Anthology>): List<Long>
    override fun update(anthology: Anthology)
    override fun delete(anthology: Anthology)
}