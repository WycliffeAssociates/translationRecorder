package org.wycliffeassociates.translationrecorder.persistence.repository.dao

import android.arch.persistence.room.*
import org.wycliffeassociates.translationrecorder.persistence.entity.TakeEntity

/**
 * Created by sarabiaj on 3/28/2018.
 */
@Dao
interface TakeDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insert(take: TakeEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertAll(takes: List<TakeEntity>)

    @Update(onConflict = OnConflictStrategy.ABORT)
    fun update(take: TakeEntity)

    @Delete
    fun delete(take: TakeEntity)

    @Query("SELECT * FROM takes")
    fun getTakes(): List<TakeEntity>
}