package org.wycliffeassociates.translationrecorder.persistence.repository

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import org.wycliffeassociates.translationrecorder.persistence.entity.*


/**
 * Created by sarabiaj on 4/11/2018.
 */

@Database(entities = arrayOf(
        AnthologyEntity::class,
        BookEntity::class,
        ChapterEntity::class,
        ChunkEntity::class,
        LanguageEntity::class,
        ModeEntity::class,
        ProjectEntity::class,
        TakeEntity::class,
        VersionEntity::class,
        UserEntity::class
), version = 1)
abstract class RoomDb : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun anthologyDao(): AnthologyDao
    abstract fun languageDao(): AnthologyDao
    abstract fun bookDao(): AnthologyDao
    abstract fun chapterDao(): AnthologyDao
    abstract fun chunkDao(): AnthologyDao
    abstract fun modeDao(): AnthologyDao
    abstract fun projectDao(): AnthologyDao
    abstract fun versionDao(): VersionDao
    abstract fun takeDao(): AnthologyDao
}
