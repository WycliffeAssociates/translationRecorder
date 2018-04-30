package org.wycliffeassociates.translationrecorder.persistence.repository.dao

import android.arch.persistence.room.*
import org.wycliffeassociates.translationrecorder.persistence.entity.TakeEntity

/**
 * Created by sarabiaj on 3/28/2018.
 */
@Dao
interface TakeDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insert(take: TakeEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertAll(takes: List<TakeEntity>): List<Long>

    @Update(onConflict = OnConflictStrategy.ABORT)
    fun update(take: TakeEntity)

    @Delete
    fun delete(take: TakeEntity)

    @Query("SELECT * FROM takes")
    fun getAll(): List<TakeEntity>

    @Query("SELECT * FROM takes WHERE id = :id")
    fun getById(id: Int): TakeEntity
}