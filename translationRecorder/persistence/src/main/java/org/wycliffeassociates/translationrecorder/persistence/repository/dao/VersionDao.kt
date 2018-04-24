package org.wycliffeassociates.translationrecorder.persistence.repository.dao

import android.arch.persistence.room.*
import org.wycliffeassociates.translationrecorder.persistence.entity.VersionEntity

/**
 * Created by sarabiaj on 3/28/2018.
 */
@Dao
interface VersionDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insert(version: VersionEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertAll(versions: List<VersionEntity>): List<Long>

    @Update(onConflict = OnConflictStrategy.ABORT)
    fun update(version: VersionEntity)

    @Delete
    fun delete(version: VersionEntity)

    @Query("SELECT * FROM versions")
    fun getAll(): List<VersionEntity>

    @Query("SELECT * FROM versions WHERE id = :id")
    fun getById(id: Int): VersionEntity
}