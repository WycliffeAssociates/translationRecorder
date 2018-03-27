package org.wycliffeassociates.translationrecorder.data.components

/**
 * Created by sarabiaj on 7/5/2017.
 */

data class ModeEntity(val slug: String, val name: String, val unit: UNIT)

enum class UNIT {
    SINGLE,
    MULTI
}