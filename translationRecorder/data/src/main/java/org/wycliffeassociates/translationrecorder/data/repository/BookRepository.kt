package org.wycliffeassociates.translationrecorder.data.repository

import java.awt.print.Book

/**
 * Created by sarabiaj on 3/28/2018.
 */

interface BookRepository {
    fun getBook(id: Long): Book
    fun getAllBooks(): List<Book>
    fun addBook(book: Book)
    fun updateBook(book: Book)
    fun deleteBook(book: Book)
}