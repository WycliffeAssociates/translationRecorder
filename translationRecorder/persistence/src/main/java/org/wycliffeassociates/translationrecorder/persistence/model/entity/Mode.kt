package org.wycliffeassociates.translationrecorder.persistence.model.entity

/**
 * Created by sarabiaj on 3/28/2018.
 */

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "mode")
data class Mode(
        @ColumnInfo(name = "id")
        @PrimaryKey(autoGenerate = true)
        val id: Long,
        val name: String,
        val slug: String,
        val unit: Int
)