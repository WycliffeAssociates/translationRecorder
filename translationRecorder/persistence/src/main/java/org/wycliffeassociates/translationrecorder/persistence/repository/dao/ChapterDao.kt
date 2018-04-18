package org.wycliffeassociates.translationrecorder.persistence.repository.dao

import android.arch.persistence.room.*
import org.wycliffeassociates.translationrecorder.persistence.entity.ChapterEntity

/**
 * Created by sarabiaj on 3/28/2018.
 */
@Dao
interface ChapterDao {

    @Insert
    fun insert(chapter: ChapterEntity)

    @Insert
    fun insertAll(chapters: List<ChapterEntity>)

    @Update
    fun update(chapter: ChapterEntity)

    @Delete
    fun delete(chapter: ChapterEntity)

    @Query("SELECT * FROM chapters")
    fun getChapters(): List<ChapterEntity>
}