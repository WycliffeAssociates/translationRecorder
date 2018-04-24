package org.wycliffeassociates.translationrecorder.persistence.repository.dao

import android.arch.persistence.room.*
import org.wycliffeassociates.translationrecorder.persistence.entity.LanguageEntity

/**
 * Created by sarabiaj on 3/28/2018.
 */
@Dao
interface LanguageDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insert(language: LanguageEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertAll(languages: List<LanguageEntity>): List<Long>

    @Update(onConflict = OnConflictStrategy.ABORT)
    fun update(language: LanguageEntity)

    @Delete
    fun delete(language: LanguageEntity)

    @Query("SELECT * FROM languages")
    fun getAll(): List<LanguageEntity>

    @Query("SELECT * FROM languages WHERE id = :id")
    fun getById(id: Int): LanguageEntity
}