package org.wycliffeassociates.translationrecorder.persistence.repository.dao

import android.arch.persistence.room.*
import org.wycliffeassociates.translationrecorder.persistence.entity.ModeEntity

/**
 * Created by sarabiaj on 3/28/2018.
 */
@Dao
interface ModeDao {

    @Insert
    fun insert(mode: ModeEntity)

    @Insert
    fun insertAll(modes: List<ModeEntity>)

    @Update
    fun update(mode: ModeEntity)

    @Delete
    fun delete(mode: ModeEntity)

    @Query("SELECT * FROM modes")
    fun getModes(): List<ModeEntity>

    @Query("SELECT * FROM modes WHERE id = :id")
    fun getById(id: Int): ModeEntity
}