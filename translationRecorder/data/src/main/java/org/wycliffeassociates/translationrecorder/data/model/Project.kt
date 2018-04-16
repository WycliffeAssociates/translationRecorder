package org.wycliffeassociates.translationrecorder.data.model

import org.wycliffeassociates.translationrecorder.project.FileName
import org.wycliffeassociates.translationrecorder.project.ProjectSlugs

/**
 * Created by sarabiaj on 5/10/2016.
 */
data class Project(
        val id: Int,
        val language: Language,
        val anthology: Anthology,
        val version: Version,
        val book: Book,
        val mode: Mode
) {

    var mFileName: FileName? = null

    fun getChapterFileName(chapter: Int): String {
        return language.slug +
                "_" + anthology.slug +
                "_" + version.slug +
                "_" + book.slug +
                "_c" + String.format("%02d", chapter) +
                ".wav"
    }

    fun getFileName(chapter: Int, vararg verses: Int): String {
        if (mFileName == null) {
            mFileName = FileName(language, anthology, version, book)
        }
        return mFileName!!.getFileName(chapter, *verses)
    }

    fun getProjectSlugs(): ProjectSlugs {
        return ProjectSlugs(getTargetLanguageSlug(), getVersionSlug(), Integer.parseInt(getBookNumber()), getBookSlug())
    }

    fun getTargetLanguageSlug(): String {
        return language.slug
    }

//    fun getSourceLanguageSlug(): String {
//      return mSourceLanguage.slug
//    }

    fun getAnthologySlug(): String {
        return anthology.slug
    }

    fun getBookSlug(): String {
        return book.slug
    }

    fun getBookName(): String {
        return book.name
    }

    fun getVersionSlug(): String {
        return version.slug
    }

    fun getModeSlug(): String {
        return mode.slug
    }

    fun getModeType(): Mode.UNIT {
        return mode.unit
    }

    fun getModeName(): String {
        return mode.name
    }

    fun getBookNumber(): String {
        return book.order.toString()
    }

//    fun getSourceAudioPath(): String {
//         return mSourceAudioPath
//    }
}