package org.wycliffeassociates.translationrecorder.data.repository

import org.wycliffeassociates.translationrecorder.chunkplugin.Mode


/**
 * Created by sarabiaj on 3/28/2018.
 */

interface ModeRepository : Repository<Mode> {
    override fun getById(id: Int): Mode
    override fun getAll(): List<Mode>
    override fun insert(mode: Mode): Long
    override fun insertAll(modes: List<Mode>): List<Long>
    override fun update(mode: Mode)
    override fun delete(mode: Mode)
}