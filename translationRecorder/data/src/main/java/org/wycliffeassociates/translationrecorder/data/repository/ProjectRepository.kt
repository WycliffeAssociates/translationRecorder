package org.wycliffeassociates.translationrecorder.data.repository

import org.wycliffeassociates.translationrecorder.data.model.Project

/**
 * Created by sarabiaj on 3/28/2018.
 */

interface ProjectRepository : Repository<Project> {
    override fun getById(id: Int): Project
    override fun getAll(): List<Project>
    override fun insert(project: Project): Long
    override fun insertAll(project: List<Project>): List<Long>
    override fun update(project: Project)
    override fun delete(project: Project)
}
