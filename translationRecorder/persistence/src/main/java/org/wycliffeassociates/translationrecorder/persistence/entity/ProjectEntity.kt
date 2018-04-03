package org.wycliffeassociates.translationrecorder.persistence.entity

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.PrimaryKey

/**
 * Created by sarabiaj on 3/27/2018.
 */
@Entity(tableName = "projects",
        foreignKeys = [
                ForeignKey(
                        entity = LanguageEntity::class,
                        parentColumns = ["id"],
                        childColumns = ["language_fk"],
                        onDelete = ForeignKey.CASCADE
                ),
                ForeignKey(
                        entity = BookEntity::class,
                        parentColumns = ["id"],
                        childColumns = ["book_fk"],
                        onDelete = ForeignKey.CASCADE
                ),
                ForeignKey(
                        entity = VersionEntity::class,
                        parentColumns = ["id"],
                        childColumns = ["version_fk"],
                        onDelete = ForeignKey.CASCADE
                ),
                ForeignKey(
                        entity = AnthologyEntity::class,
                        parentColumns = ["id"],
                        childColumns = ["anthology_fk"],
                        onDelete = ForeignKey.CASCADE
                ),
                ForeignKey(
                        entity = ModeEntity::class,
                        parentColumns = ["id"],
                        childColumns = ["mode_fk"],
                        onDelete = ForeignKey.CASCADE
                )
        ]
)
data class ProjectEntity(
        @PrimaryKey(autoGenerate = true)
        val id: Int,
        @ColumnInfo(name = "language_fk")
        val language: Int,
        @ColumnInfo(name = "anthology_fk")
        val anthology: Int,
        @ColumnInfo(name = "version_fk")
        val version: Int,
        @ColumnInfo(name = "book_fk")
        val book: Int,
        @ColumnInfo(name = "mode_fk")
        val mode: Int
)