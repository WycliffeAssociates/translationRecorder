package org.wycliffeassociates.translationrecorder.persistence.repository

import android.arch.persistence.room.*
import org.wycliffeassociates.translationrecorder.persistence.entity.BookEntity

/**
 * Created by sarabiaj on 3/28/2018.
 */
@Dao
interface BookDao {

    @Insert
    fun insert(book: BookEntity)

    @Insert
    fun insertAll(books: List<BookEntity>)

    @Update
    fun update(book: BookEntity)

    @Delete
    fun delete(book: BookEntity)

    @Query("SELECT * FROM books")
    fun getBooks(): List<BookEntity>
}