package org.wycliffeassociates.translationrecorder.persistence.model

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.PrimaryKey

/**
 * Created by sarabiaj on 3/27/2018.
 */

@Entity(tableName = "project")
data class Project(
        @ColumnInfo(name = "id")
        @PrimaryKey(autoGenerate = true)
        val id: Long,
        @ColumnInfo(name = "language_fk")
        @ForeignKey
        val language: Int,
        @ColumnInfo(name = "anthology_fk")
        @ForeignKey
        val anthology: Int,
        @ColumnInfo(name = "version_fk")
        @ForeignKey
        val version: Int,
        @ColumnInfo(name = "book_fk")
        @ForeignKey
        val book: Int,
        @ColumnInfo(name = "mode_fk")
        @ForeignKey
        val mode: Int
)