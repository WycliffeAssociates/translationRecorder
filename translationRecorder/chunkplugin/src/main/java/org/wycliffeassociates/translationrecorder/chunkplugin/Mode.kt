package org.wycliffeassociates.translationrecorder.chunkplugin

/**
 * Created by sarabiaj on 7/5/2017.
 */

data class Mode(val id: Int? = null, val slug: String, val name: String, val unit: UNIT) {

    enum class UNIT {
        SINGLE,
        MULTI
    }
}