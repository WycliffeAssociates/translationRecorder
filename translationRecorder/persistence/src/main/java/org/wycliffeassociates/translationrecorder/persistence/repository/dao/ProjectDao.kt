package org.wycliffeassociates.translationrecorder.persistence.repository.dao

import android.arch.persistence.room.*
import org.wycliffeassociates.translationrecorder.persistence.entity.ProjectEntity

/**
 * Created by sarabiaj on 3/28/2018.
 */
@Dao
interface ProjectDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insert(project: ProjectEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertAll(projects: List<ProjectEntity>): List<Long>

    @Update(onConflict = OnConflictStrategy.ABORT)
    fun update(project: ProjectEntity)

    @Delete
    fun delete(project: ProjectEntity)

    @Query("SELECT * FROM projects")
    fun getAll(): List<ProjectEntity>

    @Query("SELECT * FROM projects WHERE id = :id")
    fun getById(id: Int): ProjectEntity
}