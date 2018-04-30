package org.wycliffeassociates.translationrecorder.persistence.repository.dao

import android.arch.persistence.room.*
import org.wycliffeassociates.translationrecorder.persistence.entity.ChunkEntity

/**
 * Created by sarabiaj on 3/28/2018.
 */
@Dao
interface ChunkDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insert(chunk: ChunkEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertAll(chunks: List<ChunkEntity>): List<Long>

    @Update(onConflict = OnConflictStrategy.ABORT)
    fun update(chunk: ChunkEntity)

    @Delete
    fun delete(chunk: ChunkEntity)

    @Query("SELECT * FROM chunks")
    fun getAll(): List<ChunkEntity>

    @Query("SELECT * FROM chunks WHERE id = :id")
    fun getById(id: Int): ChunkEntity
}