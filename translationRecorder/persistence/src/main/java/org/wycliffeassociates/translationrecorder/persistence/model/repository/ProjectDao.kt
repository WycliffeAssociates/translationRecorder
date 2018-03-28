package org.wycliffeassociates.translationrecorder.persistence.model.repository

import android.arch.persistence.room.*
import org.wycliffeassociates.translationrecorder.persistence.model.entity.Project

/**
 * Created by sarabiaj on 3/28/2018.
 */
@Dao
interface ProjectDao {

    @Insert
    fun insertAll(projects: List<Project>)

    @Update
    fun update(project: Project)

    @Delete
    fun delete(project: Project)

    @Query("SELECT * FROM projects")
    fun getProjects(): List<Project>

    @Query("SELECT * FROM projects WHERE name LIKE :name LIMIT 1")
    fun getProjectByName(name: String): Project
}