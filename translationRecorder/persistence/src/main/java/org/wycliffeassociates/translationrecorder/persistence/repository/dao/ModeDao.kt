package org.wycliffeassociates.translationrecorder.persistence.repository.dao

import android.arch.persistence.room.*
import org.wycliffeassociates.translationrecorder.persistence.entity.ModeEntity

/**
 * Created by sarabiaj on 3/28/2018.
 */
@Dao
interface ModeDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insert(mode: ModeEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertAll(modes: List<ModeEntity>)

    @Update(onConflict = OnConflictStrategy.ABORT)
    fun update(mode: ModeEntity)

    @Delete
    fun delete(mode: ModeEntity)

    @Query("SELECT * FROM modes")
    fun getModes(): List<ModeEntity>

    @Query("SELECT * FROM modes WHERE id = :id")
    fun getById(id: Int): ModeEntity
}