package org.wycliffeassociates.translationrecorder.data.model

import org.wycliffeassociates.translationrecorder.chunkplugin.Mode

/**
 * Created by sarabiaj on 4/9/2018.
 */

data class Take(
        var id: Int? = null,
        var file: String,
        var unit: Mode,
        var timestamp: Long
)