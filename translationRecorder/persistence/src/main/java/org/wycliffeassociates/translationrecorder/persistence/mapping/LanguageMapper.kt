package org.wycliffeassociates.translationrecorder.persistence.mapping

import org.wycliffeassociates.translationrecorder.data.model.Language
import org.wycliffeassociates.translationrecorder.persistence.entity.LanguageEntity

/**
 * Created by sarabiaj on 4/3/2018.
 */

class LanguageMapper() : Mapper<LanguageEntity, Language> {
    override fun mapFromEntity(type: LanguageEntity): Language {
        return Language(type.id, type.slug, type.name)
    }

    override fun mapToEntity(type: Language): LanguageEntity {
        return LanguageEntity(type.id, type.name, type.slug)
    }
}