package org.wycliffeassociates.translationrecorder.persistence.repository

import android.arch.persistence.room.*
import org.wycliffeassociates.translationrecorder.persistence.entity.LanguageEntity

/**
 * Created by sarabiaj on 3/28/2018.
 */
@Dao
interface LanguageDao {

    @Insert
    fun insert(language: LanguageEntity)

    @Insert
    fun insertAll(languages: List<LanguageEntity>)

    @Update
    fun update(language: LanguageEntity)

    @Delete
    fun delete(language: LanguageEntity)

    @Query("SELECT * FROM languages")
    fun getLanguages(): List<LanguageEntity>
}