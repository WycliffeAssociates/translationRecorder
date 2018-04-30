package org.wycliffeassociates.translationrecorder.persistence.repository.dao

import android.arch.persistence.room.*
import org.wycliffeassociates.translationrecorder.persistence.entity.ChapterEntity

/**
 * Created by sarabiaj on 3/28/2018.
 */
@Dao
interface ChapterDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insert(chapter: ChapterEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertAll(chapters: List<ChapterEntity>): List<Long>

    @Update(onConflict = OnConflictStrategy.ABORT)
    fun update(chapter: ChapterEntity)

    @Delete
    fun delete(chapter: ChapterEntity)

    @Query("SELECT * FROM chapters")
    fun getAll(): List<ChapterEntity>

    @Query("SELECT * FROM chapters WHERE id = :id")
    fun getById(id: Int): ChapterEntity
}