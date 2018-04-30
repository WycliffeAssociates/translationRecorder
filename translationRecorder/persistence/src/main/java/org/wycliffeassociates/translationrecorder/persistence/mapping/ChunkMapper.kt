package org.wycliffeassociates.translationrecorder.persistence.mapping

import org.wycliffeassociates.translationrecorder.chunkplugin.Chapter
import org.wycliffeassociates.translationrecorder.chunkplugin.Chunk
import org.wycliffeassociates.translationrecorder.chunkplugin.ChunkPlugin
import org.wycliffeassociates.translationrecorder.data.model.Project
import org.wycliffeassociates.translationrecorder.persistence.entity.ChunkEntity

/**
 * Created by sarabiaj on 4/26/2018.
 */

class ChunkMapper(val project: Project, val plugin: ChunkPlugin, val chapter: Chapter) : Mapper<ChunkEntity, Chunk> {
    override fun mapFromEntity(type: ChunkEntity): Chunk {
        val chunk = plugin.getChunks(chapter.number)[type.order]
        return chunk
    }

    override fun mapToEntity(type: Chunk): ChunkEntity {
        return ChunkEntity(type.id, project.id!!, chapter.id!!, type.order, type.startVerse, type.endVerse, type.chosenTake)
    }
}