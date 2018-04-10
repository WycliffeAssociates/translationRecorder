package org.wycliffeassociates.translationrecorder.data.repository

import org.wycliffeassociates.translationrecorder.data.model.Project

/**
 * Created by sarabiaj on 3/28/2018.
 */

interface ProjectRepository {
    fun getProject(id: Long) : Project
    fun getAllProjects() : List<Project>
    fun addProject(project: Project)
    fun updateProject(project: Project)
    fun deleteProject(project: Project)
}
