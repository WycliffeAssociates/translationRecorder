package org.wycliffeassociates.translationrecorder.persistence.mapping

import org.wycliffeassociates.translationrecorder.data.model.Project
import org.wycliffeassociates.translationrecorder.persistence.entity.ProjectEntity
import org.wycliffeassociates.translationrecorder.persistence.repository.dao.*

/**
 * Created by sarabiaj on 4/5/2018.
 */

class ProjectMapper(
        val languageDao: LanguageDao,
        val anthologyDao: AnthologyDao,
        val versionDao: VersionDao,
        val bookDao: BookDao,
        val modeDao: ModeDao
) : Mapper<ProjectEntity, Project> {
    override fun mapFromEntity(type: ProjectEntity): Project {
        val language = LanguageMapper().mapFromEntity(languageDao.getById(type.language))
        val anthology = AnthologyMapper().mapFromEntity(anthologyDao.getById(type.anthology))
        val version = VersionMapper().mapFromEntity(versionDao.getById(type.version))
        val book = BookMapper(anthologyDao).mapFromEntity(bookDao.getById(type.book))
        val mode = ModeMapper().mapFromEntity(modeDao.getById(type.mode))
        return Project(type.id, language, anthology, version, book, mode)
    }

    override fun mapToEntity(type: Project): ProjectEntity {
        return ProjectEntity(
                type.id,
                type.language.id,
                type.anthology.id,
                type.version.id,
                type.book.id,
                type.mode.id
        )
    }

}