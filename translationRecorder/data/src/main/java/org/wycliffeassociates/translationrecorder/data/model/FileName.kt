package org.wycliffeassociates.translationrecorder.project

import org.wycliffeassociates.translationrecorder.data.model.Anthology
import org.wycliffeassociates.translationrecorder.data.model.Book
import org.wycliffeassociates.translationrecorder.data.model.Language
import org.wycliffeassociates.translationrecorder.data.model.Version

/**
 * Created by Joe on 6/27/2017.
 */

class FileName(language: Language, anthology: Anthology, version: Version, book: Book) {

    var mPattern: String
    var mMask: String

    init {
        mMask = anthology.mask
        mPattern = computeFileNameFormat(mMask, language, anthology, version, book)
    }

    fun getFileName(chapter: Int, vararg verses: Int): String {
        val mask = Integer.parseInt(mMask, 2)
        val sb = StringBuilder(mPattern)
        if (mask or CHAPTER == MATCH) {
            sb.append(String.format("c%02d_", chapter))
        }
        if (mask or START_VERSE == MATCH) {
            sb.append(String.format("v%02d", verses[0]))
        }
        if (mask or END_VERSE == MATCH) {
            if (verses.size > 1 && verses[0] != verses[1] && verses[1] != -1) {
                sb.append(String.format("-%02d", verses[1]))
            }
        }
        return sb.toString()
    }

    private fun computeFileNameFormat(maskString: String, language: Language, anthology: Anthology, version: Version, book: Book): String {
        val mask = Integer.parseInt(maskString, 2)
        val sb = StringBuilder()
        if (mask or LANGUAGE == MATCH) {
            sb.append(language.slug+ "_")
        }
        if (mask or RESOURCE == MATCH) {
            sb.append(anthology.resource + "_")
        }
        if (mask or ANTHOLOGY == MATCH) {
            sb.append(anthology.slug + "_")
        }
        if (mask or VERSION == MATCH) {
            sb.append(version.slug+ "_")
        }
        if (mask or BOOK_NUMBER == MATCH) {
            sb.append(String.format("b%02d_", book.order))
        }
        if (mask or BOOK == MATCH) {
            sb.append(book.slug + "_")
        }
        mPattern = sb.toString()
        return mPattern
    }

    companion object {

        private val LANGUAGE = 511
        private val RESOURCE = 767
        private val ANTHOLOGY = 895
        private val VERSION = 959
        private val BOOK_NUMBER = 991
        private val BOOK = 1007
        private val CHAPTER = 1015
        private val START_VERSE = 1019
        private val END_VERSE = 1021
        private val TAKE = 1022
        private val MATCH = 1023
    }
}
