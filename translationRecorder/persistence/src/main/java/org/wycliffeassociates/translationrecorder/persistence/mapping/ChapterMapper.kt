package org.wycliffeassociates.translationrecorder.persistence.mapping

import org.wycliffeassociates.translationrecorder.chunkplugin.Chapter
import org.wycliffeassociates.translationrecorder.data.model.Project
import org.wycliffeassociates.translationrecorder.persistence.entity.ChapterEntity

/**
 * Created by sarabiaj on 4/5/2018.
 */

class ChapterMapper(val project: Project) : Mapper<ChapterEntity, Chapter> {

    override fun mapFromEntity(type: ChapterEntity): Chapter {

    }

    override fun mapToEntity(type: Chapter): ChapterEntity {
        return ChapterEntity(type.id, project.id, type.number, type.progress, type.checkingLevel)
    }

}