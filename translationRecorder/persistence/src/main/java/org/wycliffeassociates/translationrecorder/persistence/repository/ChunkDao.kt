package org.wycliffeassociates.translationrecorder.persistence.repository

import android.arch.persistence.room.*
import org.wycliffeassociates.translationrecorder.persistence.entity.ChunkEntity

/**
 * Created by sarabiaj on 3/28/2018.
 */
@Dao
interface ChunkDao {

    @Insert
    fun insert(chunk: ChunkEntity)

    @Insert
    fun insertAll(chunks: List<ChunkEntity>)

    @Update
    fun update(chunk: ChunkEntity)

    @Delete
    fun delete(chunk: ChunkEntity)

    @Query("SELECT * FROM chunks")
    fun getChunks(): List<ChunkEntity>
}