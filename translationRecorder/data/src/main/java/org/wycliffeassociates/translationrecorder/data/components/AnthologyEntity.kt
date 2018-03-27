package org.wycliffeassociates.translationrecorder.data.components

/**
 * Created by sarabiaj on 3/28/2017.
 */

data class AnthologyEntity (val slug: String, val name: String, val resource: String,
                            val regex: String, val groups: String, val mask: String,
                            val pluginJarName: String, val pluginClassName: String){

//    fun getLabel(): String {
//        var label = ""
//        label += Utils.capitalizeFirstLetter(resource)
//        label += ":"
//        val resourceLabels = mName.split(" ".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
//        for (part in resourceLabels) {
//            label += " " + Utils.capitalizeFirstLetter(part)
//        }
//        return label
//    }
}
