package org.wycliffeassociates.translationrecorder.persistence.repository

import android.arch.persistence.room.*
import org.wycliffeassociates.translationrecorder.persistence.entity.ProjectEntity

/**
 * Created by sarabiaj on 3/28/2018.
 */
@Dao
interface ProjectDao {

    @Insert
    fun insert(project: ProjectEntity)

    @Insert
    fun insertAll(projects: List<ProjectEntity>)

    @Update
    fun update(project: ProjectEntity)

    @Delete
    fun delete(project: ProjectEntity)

    @Query("SELECT * FROM projects")
    fun getProjects(): List<ProjectEntity>
}