package org.wycliffeassociates.translationrecorder.persistence.model.entity

/**
 * Created by sarabiaj on 3/28/2018.
 */

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "modes")
data class Mode(
        @PrimaryKey(autoGenerate = true)
        val id: Int,
        val name: String,
        val slug: String,
        val unit: Int
)