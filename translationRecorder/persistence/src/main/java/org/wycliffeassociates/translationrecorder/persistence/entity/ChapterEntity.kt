package org.wycliffeassociates.translationrecorder.persistence.entity

import android.arch.persistence.room.*

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
        ],
        indices = [
            Index(
                    value = ["project_fk", "number"],
                    unique = true
            )
        ]
)
data class ChapterEntity(
        @PrimaryKey(autoGenerate = true)
        var id: Int? = null,
        @ColumnInfo(name = "project_fk")
        var project: Int,
        var number: Int,
        var progress: Int,
        var checkingLevel: Int
)