package org.wycliffeassociates.translationrecorder.persistence.repository.dao

import android.arch.persistence.room.*
import org.wycliffeassociates.translationrecorder.persistence.entity.BookEntity

/**
 * Created by sarabiaj on 3/28/2018.
 */
@Dao
interface BookDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insert(book: BookEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertAll(books: List<BookEntity>): List<Long>

    @Update(onConflict = OnConflictStrategy.ABORT)
    fun update(book: BookEntity)

    @Delete
    fun delete(book: BookEntity)

    @Query("SELECT * FROM books")
    fun getBooks(): List<BookEntity>

    @Query("SELECT * FROM books WHERE id = :id")
    fun getById(id: Int): BookEntity
}