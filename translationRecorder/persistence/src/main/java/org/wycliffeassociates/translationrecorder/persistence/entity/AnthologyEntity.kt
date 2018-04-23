package org.wycliffeassociates.translationrecorder.persistence.entity

import android.arch.persistence.room.Entity
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey

/**
 * Created by sarabiaj on 3/28/2018.
 */
@Entity(tableName = "anthologies",
        indices = [
            Index(
                    value = ["name", "slug"],
                    unique = true
            )
        ]
)
data class AnthologyEntity(
        @PrimaryKey(autoGenerate = true)
        var id: Int? = null,
        var name: String,
        var slug: String,
        var resource: String,
        var regex: String,
        var groups: String,
        var mask: String,
        var pluginJarName: String,
        var pluginClassName: String
)