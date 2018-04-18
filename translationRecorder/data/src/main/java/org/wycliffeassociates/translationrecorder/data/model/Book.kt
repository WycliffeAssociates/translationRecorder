package org.wycliffeassociates.translationrecorder.data.model

/**
 * Created by sarabiaj on 1/15/2016.
 */
class Book(
        var id: Int? = null,
        var slug: String,
        var name: String,
        var anthology: Anthology,
        var order: Int
)