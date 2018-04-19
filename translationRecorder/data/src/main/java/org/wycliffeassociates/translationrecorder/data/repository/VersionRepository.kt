package org.wycliffeassociates.translationrecorder.data.repository

import org.wycliffeassociates.translationrecorder.data.model.Version

/**
 * Created by sarabiaj on 3/28/2018.
 */

interface VersionRepository {
    fun getVersion(id: Int) : Version
    fun getVersions() : List<Version>
    fun addVersion(version: Version)
    fun updateVersion(version: Version)
    fun deleteVersion(version: Version)
}