package org.wycliffeassociates.translationrecorder.login.interfaces

import org.wycliffeassociates.translationrecorder.wav.WavFile
import java.io.File

/**
 * Created by sarabiaj on 5/3/2018.
 */
interface OnProfileCreatedListener {
    fun onProfileCreated(wav: WavFile, audio: File, hash: String)
}