package org.wycliffeassociates.translationrecorder.data.model

/**
 * Created by sarabiaj on 4/9/2018.
 */

data class Take(
        val id: Int,
        val file: String,

        val unit: Mode,
        val timestamp: Long
)