package org.wycliffeassociates.translationrecorder.data.repository

import org.wycliffeassociates.translationrecorder.data.model.Mode


/**
 * Created by sarabiaj on 3/28/2018.
 */

interface ModeRepository {
    fun getMode(id: Long) : Mode
    fun getModes() : List<Mode>
    fun addMode(mode: Mode)
    fun updateMode(mode: Mode)
    fun deleteMode(mode: Mode)
}