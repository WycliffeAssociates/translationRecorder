package org.wycliffeassociates.translationrecorder.persistence

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.wycliffeassociates.translationrecorder.data.model.Language
import org.wycliffeassociates.translationrecorder.persistence.entity.LanguageEntity
import org.wycliffeassociates.translationrecorder.persistence.mapping.LanguageMapper
import org.wycliffeassociates.translationrecorder.persistence.repository.RoomDb

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see [Testing documentation](http://d.android.com/tools/testing)
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    @Throws(Exception::class)
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getTargetContext()
        val db = RoomDb.getInstance(appContext)
        val dao = db!!.languageDao()
        val language = Language(null, "en", "English")
        val id = dao.insert(LanguageMapper().mapToEntity(language))

        var something: LanguageEntity
        something = dao.getById(49)
        var stuff = something.name
        println(stuff)
        val (_, _, name) = LanguageMapper().mapFromEntity(dao.getById(id.toInt()))


        assertEquals(name, language.name)
    }
}
