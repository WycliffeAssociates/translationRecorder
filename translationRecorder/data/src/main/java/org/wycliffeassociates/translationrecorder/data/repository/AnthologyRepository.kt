package org.wycliffeassociates.translationrecorder.data.repository

import org.wycliffeassociates.translationrecorder.data.components.Anthology

/**
 * Created by sarabiaj on 3/28/2018.
 */

interface AnthologyRepository {
    fun getAnthology(id: Long) : Anthology
    fun getAnthologies() : List<Anthology>
    fun addAnthology(anthology: Anthology)
    fun updateAnthology(anthology: Anthology)
    fun deleteAnthology(anthology: Anthology)
}