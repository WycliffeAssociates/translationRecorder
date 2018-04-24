package org.wycliffeassociates.translationrecorder.persistence.repository.impl

import org.wycliffeassociates.translationrecorder.data.model.Book
import org.wycliffeassociates.translationrecorder.data.repository.BookRepository
import org.wycliffeassociates.translationrecorder.persistence.mapping.BookMapper
import org.wycliffeassociates.translationrecorder.persistence.repository.dao.AnthologyDao
import org.wycliffeassociates.translationrecorder.persistence.repository.dao.BookDao

/**
 * Created by sarabiaj on 4/24/2018.
 */

class BookRepoImpl(val bookDao: BookDao, val anthDao: AnthologyDao): BookRepository {

    val mapper = BookMapper(anthDao)

    override fun insert(book: Book): Long {
        return bookDao.insert(BookMapper(anthDao).mapToEntity(book))
    }

    override fun insertAll(books: List<Book>): List<Long> {
        return bookDao.insertAll(books.map { mapper.mapToEntity(it)})
    }

    override fun update(book: Book) {
        return bookDao.update(mapper.mapToEntity(book))
    }

    override fun delete(book: Book) {
        return bookDao.delete(mapper.mapToEntity(book))
    }

    override fun getAll(): List<Book> {
        return bookDao.getAll().map { mapper.mapFromEntity(it) }
    }

    override fun getById(id: Int): Book {
        return mapper.mapFromEntity(bookDao.getById(id))
    }
}