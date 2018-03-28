package org.wycliffeassociates.translationrecorder.data.repository

import org.wycliffeassociates.translationrecorder.data.components.Version

/**
 * Created by sarabiaj on 3/28/2018.
 */

interface VersionRepository {
    fun getVersion(id: Long) : Version
    fun getVersions() : List<Version>
    fun addVersion(version: Version)
    fun updateVersion(version: Version)
    fun deleteVersion(version: Version)
}