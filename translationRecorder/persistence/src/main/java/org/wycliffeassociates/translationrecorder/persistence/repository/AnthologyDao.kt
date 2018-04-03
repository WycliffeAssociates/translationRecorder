package org.wycliffeassociates.translationrecorder.persistence.repository

import android.arch.persistence.room.*
import org.wycliffeassociates.translationrecorder.persistence.entity.AnthologyEntity

/**
 * Created by sarabiaj on 3/28/2018.
 */
@Dao
interface AnthologyDao {

    @Insert
    fun insert(anthology: AnthologyEntity)

    @Insert
    fun insertAll(anthologies: List<AnthologyEntity>)

    @Update
    fun update(anthology: AnthologyEntity)

    @Delete
    fun delete(anthology: AnthologyEntity)

    @Query("SELECT * FROM anthologies")
    fun getAnthologies(): List<AnthologyEntity>

    @Query("SELECT * FROM anthologies WHERE id = :id")
    fun findById(id: Int) : AnthologyEntity
}