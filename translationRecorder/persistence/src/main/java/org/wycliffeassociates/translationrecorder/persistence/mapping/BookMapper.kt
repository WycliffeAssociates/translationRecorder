package org.wycliffeassociates.translationrecorder.persistence.mapping

import org.wycliffeassociates.translationrecorder.data.components.Book
import org.wycliffeassociates.translationrecorder.persistence.entity.BookEntity
import org.wycliffeassociates.translationrecorder.persistence.repository.AnthologyDao

/**
 * Created by sarabiaj on 4/3/2018.
 */

class BookMapper(val repo: AnthologyDao) : Mapper<BookEntity, Book> {
    override fun mapFromEntity(type: BookEntity): Book {
        val anthEnt = repo.findById(type.anthology)
        val anth = AnthologyMapper().mapFromEntity(anthEnt)
        return Book(type.id, type.slug, type.name, anth, type.number)
    }

    override fun mapToEntity(type: Book): BookEntity {
        return BookEntity(type.id, type.name, type.slug, type.order, type.anthology.id)
    }

}