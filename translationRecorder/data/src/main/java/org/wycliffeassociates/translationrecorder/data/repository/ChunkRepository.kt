package org.wycliffeassociates.translationrecorder.data.repository

import org.wycliffeassociates.translationrecorder.chunkplugin.Chunk

/**
 * Created by sarabiaj on 4/26/2018.
 */
interface ChunkRepository : Repository<Chunk> {
    override fun getById(id: Int): Chunk
    override fun getAll(): List<Chunk>
    override fun insert(language: Chunk): Long
    override fun insertAll(languages: List<Chunk>): List<Long>
    override fun update(language: Chunk)
    override fun delete(language: Chunk)
}