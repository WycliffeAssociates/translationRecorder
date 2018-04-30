package org.wycliffeassociates.translationrecorder.persistence.mapping

import org.wycliffeassociates.translationrecorder.chunkplugin.Chapter
import org.wycliffeassociates.translationrecorder.chunkplugin.ChunkPlugin
import org.wycliffeassociates.translationrecorder.data.model.Project
import org.wycliffeassociates.translationrecorder.persistence.entity.ChapterEntity

/**
 * Created by sarabiaj on 4/26/2018.
 */

class ChapterMapper(val project: Project, val plugin: ChunkPlugin) : Mapper<ChapterEntity, Chapter> {
    override fun mapFromEntity(type: ChapterEntity): Chapter {
        val chapter = plugin.getChapter(type.number)
        chapter.id  = type.id
        chapter.checkingLevel = type.checkingLevel
        chapter.progress = type.progress
        return chapter
    }

    override fun mapToEntity(type: Chapter): ChapterEntity {
        return ChapterEntity(type.id, project.id!!, type.number, type.progress, type.checkingLevel)
    }

}