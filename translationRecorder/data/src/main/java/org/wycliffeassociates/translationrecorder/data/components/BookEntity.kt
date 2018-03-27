package org.wycliffeassociates.translationrecorder.data.components

/**
 * Created by sarabiaj on 1/15/2016.
 */
class BookEntity(val slug: String,
                 val name: String,
                 val anthology: AnthologyEntity,
                 val order: Int)