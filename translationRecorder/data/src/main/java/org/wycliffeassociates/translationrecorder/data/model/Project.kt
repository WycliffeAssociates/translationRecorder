package org.wycliffeassociates.translationrecorder.data.model

import org.wycliffeassociates.translationrecorder.chunkplugin.ChunkPlugin
import org.wycliffeassociates.translationrecorder.chunkplugin.Mode
import org.wycliffeassociates.translationrecorder.project.FileName
import java.io.IOException
import java.io.InputStream


/**
 * Created by sarabiaj on 5/10/2016.
 */
data class Project(
        var id: Int? = null,
        var language: Language,
        var anthology: Anthology,
        var version: Version,
        var book: Book,
        var mode: Mode
) {

    interface ProjectPluginLoader {
        fun loadChunkPlugin(anthology: Anthology, book: Book, type: Mode.UNIT): ChunkPlugin
        fun chunksInputStream(anthology: Anthology, book: Book): InputStream
    }

    companion object {
        lateinit var PROJECT_EXTRA: String
        init {
            PROJECT_EXTRA = "project"
        }
    }

    var mFileName: FileName? = null

    fun getChapterFileName(chapter: Int): String {
        return language.slug +
                "_" + anthology.slug +
                "_" + version.slug +
                "_" + book.slug +
                "_c" + String.format("%02d", chapter) +
                ".wav"
    }

    @Throws(IOException::class)
    fun getChunkPlugin(pluginLoader: ProjectPluginLoader): ChunkPlugin {
        return pluginLoader.loadChunkPlugin(anthology, book, getModeType())
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

    fun getPatternMatcher(): ProjectPatternMatcher {
        return ProjectPatternMatcher(anthology.regex, anthology.groups)
    }

    fun isOBS(): Boolean {
        return anthology.slug.compareTo("obs") === 0
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