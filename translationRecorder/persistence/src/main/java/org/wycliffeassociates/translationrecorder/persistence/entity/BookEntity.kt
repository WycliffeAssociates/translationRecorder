package org.wycliffeassociates.translationrecorder.persistence.entity

import android.arch.persistence.room.*

/**
 * Created by sarabiaj on 3/28/2018.
 */

@Entity(tableName = "books",
        foreignKeys = [
                ForeignKey(
                        entity = AnthologyEntity::class,
                        parentColumns = ["id"],
                        childColumns = ["anthology_fk"],
                        onDelete = ForeignKey.CASCADE
                )
        ],
        indices = [
            Index(
                    value = ["name", "slug", "anthology"],
                    unique = true
            )
        ]
)
data class BookEntity(
        @PrimaryKey(autoGenerate = true)
        var id: Int? = null,
        var name: String,
        var slug: String,
        var number: Int,
        @ColumnInfo(name = "anthology_fk")
        var anthology: Int
)