package org.wycliffeassociates.translationrecorder.persistence.mapping

import org.wycliffeassociates.translationrecorder.data.model.Take
import org.wycliffeassociates.translationrecorder.persistence.entity.TakeEntity

/**
 * Created by sarabiaj on 4/26/2018.
 */

class TakeMapper() : Mapper<TakeEntity, Take> {
    override fun mapFromEntity(type: TakeEntity): Take {
        Take(type.id, type.file, , type.timestamp)
    }

    override fun mapToEntity(type: Take): TakeEntity {
        TakeEntity(type.id, type.)
    }

}