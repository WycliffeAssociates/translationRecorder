package org.wycliffeassociates.translationrecorder.persistence.repository.dao

import android.arch.persistence.room.*
import org.wycliffeassociates.translationrecorder.persistence.entity.AnthologyEntity

/**
 * Created by sarabiaj on 3/28/2018.
 */
@Dao
interface AnthologyDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insert(anthology: AnthologyEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertAll(anthologies: List<AnthologyEntity>): List<Long>

    @Update(onConflict = OnConflictStrategy.ABORT)
    fun update(anthology: AnthologyEntity)

    @Delete
    fun delete(anthology: AnthologyEntity)

    @Query("SELECT * FROM anthologies")
    fun getAnthologies(): List<AnthologyEntity>

    @Query("SELECT * FROM anthologies WHERE id = :id")
    fun getById(id: Int): AnthologyEntity
}