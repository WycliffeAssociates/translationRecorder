package org.wycliffeassociates.translationrecorder.login.interfaces

import java.io.File

/**
 * Created by sarabiaj on 5/3/2018.
 */
interface OnProfileCreatedListener {
    fun onProfileCreated(audio: File, hash: String)
}