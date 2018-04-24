package org.wycliffeassociates.translationrecorder.persistence.repository

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context
import org.wycliffeassociates.translationrecorder.data.repository.*
import org.wycliffeassociates.translationrecorder.persistence.entity.*
import org.wycliffeassociates.translationrecorder.persistence.repository.dao.*
import org.wycliffeassociates.translationrecorder.persistence.repository.impl.*


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
abstract class RoomDb : RoomDatabase(), AppDatabase {
    abstract fun languageDao(): LanguageDao
    abstract fun versionDao(): VersionDao
    abstract fun anthologyDao(): AnthologyDao
    abstract fun bookDao(): BookDao
    abstract fun modeDao(): ModeDao
    abstract fun projectDao(): ProjectDao
    abstract fun chapterDao(): ChapterDao
    abstract fun chunkDao(): ChunkDao
    abstract fun takeDao(): TakeDao
    abstract fun userDao(): UserDao

    lateinit var languageRepo: LanguageRepository
    lateinit var versionRepo: VersionRepository
    lateinit var anthRepo: AnthologyRepository
    lateinit var bookRepo: BookRepository
    lateinit var modeRepo: ModeRepository
    lateinit var projectRepo: ProjectRepository
    //lateinit var chapterRepo: ChapterRepository
    //lateinit var chunkRepo: ChunkRepository
    //lateinit var takeRepo: TakeRepository
    //lateinit var userRepo: UserRepository

    override fun languageRepo(): LanguageRepository {
        return languageRepo
    }

    override fun versionRepo(): VersionRepository {
        return versionRepo
    }

    override fun anthologyRepo(): AnthologyRepository {
        return anthRepo
    }

    override fun bookRepo(): BookRepository {
        return bookRepo
    }

    override fun modeRepo(): ModeRepository {
        return modeRepo
    }

    override fun projectRepo(): ProjectRepository {
        return projectRepo
    }

    private fun init() {
        languageRepo = LanguageRepoImpl(languageDao())
        versionRepo = VersionRepoImpl(versionDao())
        anthRepo = AnthologyRepoImpl(anthologyDao())
        bookRepo = BookRepoImpl(bookDao(), anthologyDao())
        modeRepo = ModeRepoImpl(modeDao())
        projectRepo = ProjectRepoImpl(
                projectDao(),
                languageDao(),
                versionDao(),
                anthologyDao(),
                bookDao(),
                modeDao()
        )
    }

    companion object {
        private var INSTANCE: RoomDb? = null

        fun getInstance(context: Context): RoomDb? {
            if (INSTANCE == null) {
                synchronized(RoomDb::class) {
                    INSTANCE = Room.databaseBuilder(
                                context.getApplicationContext(),
                                RoomDb::class.java, "tr.db"
                            )
                            .allowMainThreadQueries()
                            .build()
                    INSTANCE!!.init()
                }
            }
            return INSTANCE
        }

        fun destroyInstance() {
            INSTANCE = null
        }
    }
}
