package org.wycliffeassociates.translationrecorder.data.repository

import org.wycliffeassociates.translationrecorder.data.model.Book

/**
 * Created by sarabiaj on 3/28/2018.
 */

interface BookRepository : Repository<Book> {
    override fun getById(id: Int): Book
    override fun getAll(): List<Book>
    override fun insert(book: Book): Long
    override fun insertAll(books: List<Book>): List<Long>
    override fun update(book: Book)
    override fun delete(book: Book)
}