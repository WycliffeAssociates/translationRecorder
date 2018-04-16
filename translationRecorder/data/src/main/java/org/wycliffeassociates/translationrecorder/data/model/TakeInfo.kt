package org.wycliffeassociates.translationrecorder.project

/**
 * Created by sarabiaj on 4/17/2017.
 */

class TakeInfo(val projectSlugs: ProjectSlugs, val chapter: Int, val startVerse: Int, val endVerse: Int = 0, val take: Int) {

    constructor(slugs: ProjectSlugs,
                chapter: String,
                startVerse: String,
                endVerse: String,
                take: String
    ) : this(slugs, Integer.parseInt(chapter), Integer.parseInt(startVerse), Integer.parseInt(endVerse), Integer.parseInt(take))


    fun equalBaseInfo(takeInfo: TakeInfo?): Boolean {
        if (takeInfo == null) {
            return false
        }
        return if (!(takeInfo is TakeInfo)) {
            false
        } else {
            ((projectSlugs == takeInfo!!.projectSlugs
                    && chapter == takeInfo!!.chapter
                    && startVerse == takeInfo!!.startVerse
                    && endVerse == takeInfo!!.endVerse))
        }
    }


}
