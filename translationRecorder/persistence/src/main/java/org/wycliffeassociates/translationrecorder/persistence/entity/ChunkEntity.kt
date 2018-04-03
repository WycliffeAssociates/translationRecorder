package org.wycliffeassociates.translationrecorder.persistence.entity

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.PrimaryKey

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
        ]
)
data class ChunkEntity(
        @PrimaryKey(autoGenerate = true)
        val id: Int,
        @ColumnInfo(name = "project_fk")
        val project: Int,
        @ColumnInfo(name = "chapter_fk")
        val chapter: Int,
        val start: Int,
        val end: Int,
        @ColumnInfo(name = "chosen_take_fk")
        val chosenTake: Int
)