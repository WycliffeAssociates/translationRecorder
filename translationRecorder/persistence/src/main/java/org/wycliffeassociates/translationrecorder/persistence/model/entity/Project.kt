package org.wycliffeassociates.translationrecorder.persistence.model.entity

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.PrimaryKey
import junit.runner.Version
import org.wycliffeassociates.translationrecorder.persistence.model.entity.Anthology
import org.wycliffeassociates.translationrecorder.persistence.model.entity.Book
import org.wycliffeassociates.translationrecorder.persistence.model.entity.Language
import org.wycliffeassociates.translationrecorder.persistence.model.entity.Mode

/**
 * Created by sarabiaj on 3/27/2018.
 */
@Entity(tableName = "project",
        foreignKeys = arrayOf(
                ForeignKey(
                        entity = Language::class,
                        parentColumns = arrayOf("id"),
                        childColumns = arrayOf("language_fk"),
                        onDelete = ForeignKey.CASCADE
                ),
                ForeignKey(
                        entity = Book::class,
                        parentColumns = arrayOf("id"),
                        childColumns = arrayOf("book_fk"),
                        onDelete = ForeignKey.CASCADE
                ),
                ForeignKey(
                        entity = Version::class,
                        parentColumns = arrayOf("id"),
                        childColumns = arrayOf("version_fk"),
                        onDelete = ForeignKey.CASCADE
                ),
                ForeignKey(
                        entity = Anthology::class,
                        parentColumns = arrayOf("id"),
                        childColumns = arrayOf("anthology_fk"),
                        onDelete = ForeignKey.CASCADE
                ),
                ForeignKey(
                        entity = Mode::class,
                        parentColumns = arrayOf("id"),
                        childColumns = arrayOf("mode_fk"),
                        onDelete = ForeignKey.CASCADE
                )
        )
)
data class Project(
        @ColumnInfo(name = "id")
        @PrimaryKey(autoGenerate = true)
        val id: Long,
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