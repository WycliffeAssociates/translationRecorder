package org.wycliffeassociates.translationrecorder.data.model

/**
 * Created by sarabiaj on 3/28/2017.
 */

data class Anthology(
        var id: Int? = null,
        var slug: String,
        var name: String,
        var resource: String,
        var regex: String,
        var groups: String,
        var mask: String,
        var pluginJarName: String,
        var pluginClassName: String
)
