package org.wycliffeassociates.translationrecorder.persistence.mapping

import org.wycliffeassociates.translationrecorder.data.model.Version
import org.wycliffeassociates.translationrecorder.persistence.entity.VersionEntity

/**
 * Created by sarabiaj on 4/5/2018.
 */

class VersionMapper() : Mapper<VersionEntity, Version> {
    override fun mapFromEntity(type: VersionEntity): Version {
        return Version(type.id, type.slug, type.name)
    }

    override fun mapToEntity(type: Version): VersionEntity {
        return VersionEntity(type.id, type.name, type.slug)
    }

}