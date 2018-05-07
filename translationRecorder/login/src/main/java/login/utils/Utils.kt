package login.utils

import android.app.Fragment
import android.app.FragmentManager
import android.os.Bundle
import org.wycliffeassociates.translationrecorder.login.R


/**
 * Created by Dilip Maharjan on 04/26/2018
 */

fun fragmentTransaction(fragmentManager: FragmentManager, fragment: Fragment, tag: String, addToBackStack: Boolean, data: String) {
    val fm = fragmentManager.beginTransaction()
    if (!data.isEmpty()) {
        var bundle = Bundle()
        bundle.putString("data", data)
        fragment.arguments = bundle
    }
    fm.replace(R.id.fragment_container, fragment)

    if (addToBackStack) {
        fm.addToBackStack("tag")
    }
    fm.commit()
}