package org.wycliffeassociates.translationrecorder.persistence.entity

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

/**
 * Created by sarabiaj on 3/28/2018.
 */
@Entity(tableName = "anthologies")
data class AnthologyEntity(
        @PrimaryKey(autoGenerate = true)
        val id: Int,
        val name: String,
        val slug: String,
        val resource: String,
        val regex: String,
        val groups: String,
        val mask: String,
        val pluginJarName: String,
        val pluginClassName: String
)