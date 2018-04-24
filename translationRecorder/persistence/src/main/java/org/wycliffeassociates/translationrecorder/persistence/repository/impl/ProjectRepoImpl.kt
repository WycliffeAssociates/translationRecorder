package org.wycliffeassociates.translationrecorder.persistence.repository.impl

import org.wycliffeassociates.translationrecorder.data.model.Project
import org.wycliffeassociates.translationrecorder.data.repository.ProjectRepository
import org.wycliffeassociates.translationrecorder.persistence.mapping.ProjectMapper
import org.wycliffeassociates.translationrecorder.persistence.repository.dao.*

/**
 * Created by sarabiaj on 4/24/2018.
 */

class ProjectRepoImpl(
        val projectDao: ProjectDao,
        val languageDao: LanguageDao,
        val versionDao: VersionDao,
        val anthologyDao: AnthologyDao,
        val bookDao: BookDao,
        val modeDao: ModeDao
) : ProjectRepository {
    val mapper = ProjectMapper(languageDao, anthologyDao, versionDao, bookDao, modeDao)

    override fun getById(id: Int): Project {
        return mapper.mapFromEntity(projectDao.getById(id))
    }

    override fun getAll(): List<Project> {
        return projectDao.getAll().map { mapper.mapFromEntity(it) }
    }

    override fun insert(project: Project): Long {
        return projectDao.insert(mapper.mapToEntity(project))
    }

    override fun insertAll(projects: List<Project>): List<Long> {
        return projectDao.insertAll(projects.map { mapper.mapToEntity(it) })
    }

    override fun update(project: Project) {
        return projectDao.update(mapper.mapToEntity(project))
    }

    override fun delete(project: Project) {
        return projectDao.delete(mapper.mapToEntity(project))
    }

}