package org.wycliffeassociates.translationrecorder.persistence.entity

import android.arch.persistence.room.*

/**
 * Created by sarabiaj on 3/29/2018.
 */

@Entity(tableName = "chunks",
        foreignKeys = [
            ForeignKey(
                    entity = ProjectEntity::class,
                    childColumns = ["project_fk"],
                    parentColumns = ["id"],
                    onDelete = ForeignKey.CASCADE
            ),
            ForeignKey(
                    entity = ChapterEntity::class,
                    childColumns = ["chapter_fk"],
                    parentColumns = ["id"],
                    onDelete = ForeignKey.CASCADE
            ),
            ForeignKey(
                    entity = TakeEntity::class,
                    childColumns = ["chosen_take_fk"],
                    parentColumns = ["id"],
                    onDelete = ForeignKey.CASCADE
            )
        ],
        indices = [
            Index(
                    value = ["chapter_fk", "start"],
                    unique = true
            )
        ]
)
data class ChunkEntity(
        @PrimaryKey(autoGenerate = true)
        var id: Int? = null,
        @ColumnInfo(name = "project_fk")
        var project: Int,
        @ColumnInfo(name = "chapter_fk")
        var chapter: Int,
        var start: Int,
        var end: Int,
        @ColumnInfo(name = "chosen_take_fk")
        var chosenTake: Int
)