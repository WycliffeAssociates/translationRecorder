package org.wycliffeassociates.translationrecorder.persistence.entity

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

/**
 * Created by sarabiaj on 3/29/2018.
 */

@Entity(tableName = "users")
data class UserEntity(
        @PrimaryKey(autoGenerate = true)
        val id: Int,
        val audio: String,
        val hash: String,
        val name: String
)