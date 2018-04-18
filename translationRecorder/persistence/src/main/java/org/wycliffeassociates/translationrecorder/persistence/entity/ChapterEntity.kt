package org.wycliffeassociates.translationrecorder.persistence.entity

import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.PrimaryKey

/**
 * Created by sarabiaj on 3/29/2018.
 */

@Entity(
        tableName = "chapters",
        foreignKeys = [
                ForeignKey(
                        entity = ProjectEntity::class,
                        parentColumns = ["id"],
                        childColumns = ["project_fk"],
                        onDelete = ForeignKey.CASCADE
                )
        ]
)
data class ChapterEntity(
        @PrimaryKey(autoGenerate = true)
        var id: Int? = null,
        var project: Int,
        var number: Int,
        var progress: Int,
        var checkingLevel: Int
)