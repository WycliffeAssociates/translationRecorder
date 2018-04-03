package org.wycliffeassociates.translationrecorder.persistence.mapping

/**
 * Created by sarabiaj on 4/3/2018.
 */

interface Mapper<E, D> {
    fun mapFromEntity(type: E) : D
    fun mapToEntity(type: D) : E
}