package org.wycliffeassociates.translationrecorder.persistence.mapping

import org.wycliffeassociates.translationrecorder.chunkplugin.Mode
import org.wycliffeassociates.translationrecorder.persistence.entity.ModeEntity

/**
 * Created by sarabiaj on 4/5/2018.
 */

class ModeMapper() : Mapper<ModeEntity, Mode> {
    override fun mapFromEntity(type: ModeEntity): Mode {
        val unit = if (type.unit == Mode.UNIT.SINGLE.ordinal) Mode.UNIT.SINGLE else Mode.UNIT.MULTI
        return Mode(type.id, type.slug, type.name, unit)
    }

    override fun mapToEntity(type: Mode): ModeEntity {
        return ModeEntity(type.id, type.name, type.slug, type.unit.ordinal)
    }

}