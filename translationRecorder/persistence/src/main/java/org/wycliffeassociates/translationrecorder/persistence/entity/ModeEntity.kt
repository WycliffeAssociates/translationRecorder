package org.wycliffeassociates.translationrecorder.persistence.entity

/**
 * Created by sarabiaj on 3/28/2018.
 */

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "modes")
data class ModeEntity(
        @PrimaryKey(autoGenerate = true)
        var id: Int? = null,
        var name: String,
        var slug: String,
        var unit: Int
)