package org.wycliffeassociates.translationrecorder.data.components

/**
 * Created by sarabiaj on 3/28/2017.
 */

data class Anthology(
        val slug: String,
        val name: String,
        val resource: String,
        val regex: String,
        val groups: String,
        val mask: String,
        val pluginJarName: String,
        val pluginClassName: String
)
